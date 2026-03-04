import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const service = axios.create({
  // Use proxy base path so callers can use '/user/login' etc.
  // If a caller still prefixes '/api', the interceptor strips it.
  baseURL: '/api',
  timeout: 30000
})

const PUBLIC_PATHS = ['/user/login', '/user/register', '/user/refresh']
const normalizePath = (url = '') => {
  if (url.startsWith('/api/')) return url.slice(4)
  return url
}

let isRefreshing = false
let pendingQueue = []

const enqueueRequest = (resolve, reject) => {
  pendingQueue.push({ resolve, reject })
}

const processQueue = (error, token) => {
  pendingQueue.forEach(({ resolve, reject }) => {
    if (error) reject(error)
    else resolve(token)
  })
  pendingQueue = []
}

const clearAuthStorage = () => {
  localStorage.removeItem('accessToken')
  localStorage.removeItem('refreshToken')
  localStorage.removeItem('userInfo')
  localStorage.removeItem('role')
  localStorage.removeItem('isLogin')
  localStorage.removeItem('token')
}

const refreshAccessToken = async () => {
  const refreshToken = localStorage.getItem('refreshToken')
  if (!refreshToken) throw new Error('No refresh token')
  const res = await service.post(
    '/user/refresh',
    { refreshToken },
    { skipAuthRefresh: true }
  )
  if (res?.code !== '200') {
    throw new Error(res?.msg || 'Refresh failed')
  }
  const newAccessToken = res?.data?.accessToken
  const newRefreshToken = res?.data?.refreshToken
  if (!newAccessToken) throw new Error('Refresh failed')
  localStorage.setItem('accessToken', newAccessToken)
  if (newRefreshToken) localStorage.setItem('refreshToken', newRefreshToken)
  return newAccessToken
}

// Request interceptor: inject token from localStorage (source of truth: `accessToken`).
service.interceptors.request.use(
  (config) => {
    const rawUrl = config.url || ''
    const normalizedUrl = normalizePath(rawUrl)
    if (rawUrl.startsWith('/api/')) config.url = normalizedUrl

    const path = normalizedUrl.split('?')[0]
    const isPublic = PUBLIC_PATHS.includes(path)

    if (!isPublic) {
      const token = localStorage.getItem('accessToken') || localStorage.getItem('token')
      if (token) {
        config.headers['Authorization'] = `Bearer ${token}`
      }
    }
    return config
  },
  (error) => Promise.reject(error)
)

// Response interceptor: normalize backend Result { code, msg, data }.
service.interceptors.response.use(
  (response) => {
    const res = response.data
    const hasResultWrapper = res && Object.prototype.hasOwnProperty.call(res, 'code')
    if (hasResultWrapper) {
      const code = String(res.code)
      if (code !== '200') {
        const message = res.msg || 'Error'
        const err = new Error(message)
        err.code = res.code
        err.data = res.data
        if (code === '401') err.name = 'AuthError'
        return Promise.reject(err)
      }
      // Return full Result for compatibility with existing callers.
      return res
    }
    return res
  },
  (error) => {
    const status = error?.response?.status
    const originalRequest = error?.config || {}
    const normalizedPath = normalizePath(originalRequest.url || '')
    const isPublic = PUBLIC_PATHS.includes(normalizedPath.split('?')[0])

    if (status === 401 && !isPublic && !originalRequest.skipAuthRefresh) {
      if (originalRequest._retry) {
        const err = new Error(error?.message || 'Unauthorized')
        err.name = 'AuthError'
        err.code = 401
        return Promise.reject(err)
      }
      originalRequest._retry = true

      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          enqueueRequest(resolve, reject)
        }).then((token) => {
          originalRequest.headers = originalRequest.headers || {}
          originalRequest.headers['Authorization'] = `Bearer ${token}`
          return service(originalRequest)
        })
      }

      isRefreshing = true
      return new Promise((resolve, reject) => {
        refreshAccessToken()
          .then((token) => {
            processQueue(null, token)
            originalRequest.headers = originalRequest.headers || {}
            originalRequest.headers['Authorization'] = `Bearer ${token}`
            resolve(service(originalRequest))
          })
          .catch((err) => {
            processQueue(err, null)
            clearAuthStorage()
            router.push('/login')
            reject(err)
          })
          .finally(() => {
            isRefreshing = false
          })
      })
    }

    if (status === 401) {
      const err = new Error(error?.message || 'Unauthorized')
      err.name = 'AuthError'
      err.code = 401
      return Promise.reject(err)
    }
    ElMessage.error(error.message || 'Request failed')
    return Promise.reject(error)
  }
)

export default service

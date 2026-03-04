import { computed, ref } from 'vue'
import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', () => {
  const accessToken = ref(localStorage.getItem('accessToken') || localStorage.getItem('token') || '')
  const refreshToken = ref(localStorage.getItem('refreshToken') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))
  const role = ref(localStorage.getItem('role') || '')
  const isLogin = ref(localStorage.getItem('isLogin') || '')

  const getAccessToken = computed(() => accessToken.value)
  const getRefreshToken = computed(() => refreshToken.value)
  const getUserInfo = computed(() => userInfo.value)
  const getRole = computed(() => role.value)
  const getIsLogin = computed(() => isLogin.value)

  const setAuth = (newAccessToken, newRefreshToken, newUserInfo, newRole, newIsLogin = 'true') => {
    accessToken.value = newAccessToken
    refreshToken.value = newRefreshToken
    userInfo.value = newUserInfo
    role.value = newRole
    isLogin.value = newIsLogin

    localStorage.setItem('accessToken', newAccessToken)
    localStorage.setItem('refreshToken', newRefreshToken || '')
    localStorage.setItem('userInfo', JSON.stringify(newUserInfo || {}))
    localStorage.setItem('role', String(newRole || ''))
    localStorage.setItem('isLogin', newIsLogin)
  }

  const logout = () => {
    accessToken.value = ''
    refreshToken.value = ''
    userInfo.value = {}
    role.value = ''
    isLogin.value = ''
    localStorage.clear()
    window.location.href = '/login'
  }

  return {
    accessToken,
    refreshToken,
    userInfo,
    role,
    isLogin,
    getAccessToken,
    getRefreshToken,
    getUserInfo,
    getRole,
    getIsLogin,
    setAuth,
    logout
  }
})

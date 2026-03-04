import request from '@/utils/request'

export const login = (body) => request.post('/user/login', body)
export const refreshToken = (body) => request.post('/user/refresh', body)
export const register = (body) => request.post('/user/register', body)
export const logout = () => request.get('/user/logout')

export const adminGetUsers = (params) => request.get('/user/getUser', { params })
export const updateUser = (body) => request.put('/user/updateUser', body)
export const adminDeleteUser = (id) => request.delete(`/user/${id}`)

import request from '@/utils/request'

// 商家：发货
export const ship = (data) => request.post('/delivery/ship', data)

// 商家：物流列表
export const list = (params) => request.get('/delivery/list', { params })

// 商家：修改物流
export const correct = (data) => request.put('/delivery/correct', data)

// 用户/商家：获取单条物流信息
export const getOne = (params) => request.get('/delivery/getOne', { params })

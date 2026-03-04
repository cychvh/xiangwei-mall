import request from '@/utils/request'

// 用户：订单列表
export const getUserOrders = (params = {}) => {
    // GET /order/userList
    return request.get('/order/userList', { params })
}

// 用户：订单明细
export const getOrderItems = (orderId) => {
    // GET /order/getOrderItem?orderId=xx
    return request.get('/order/getOrderItem', { params: { orderId } })
}

// 用户：新增订单
export const createOrder = (orderDTO) => {
    // POST /order/addOrder
    return request.post('/order/addOrder', orderDTO)
}

// 用户：确认收货
export const confirmReceipt = (orderId) => {
    // PUT /order/confirmReceipt?orderId=xx
    return request.put('/order/confirmReceipt', null, { params: { orderId } })
}

// 商家：订单列表
export const getMerchantOrders = (params = {}) => {
    // GET /order/MerchantList
    return request.get('/order/MerchantList', { params })
}

// 商家：订单明细
export const getMerchantOrderItems = (orderId) => {
    // GET /order/merchantOrderItems?orderId=xx
    return request.get('/order/merchantOrderItems', { params: { orderId } })
}

// 管理员：全部订单列表（你新增的功能）
export const getAdminOrders = (params = {}) => {
    // GET /order/adminList
    return request.get('/order/adminList', { params })
}

/** ---- 兼容旧命名（如果你旧页面还在用） ---- */
export const list = getUserOrders

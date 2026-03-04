import request from '@/utils/request'

// 购物车列表
export const getCart = (params = {}) => {
    // 对应后端：GET /cart/list
    return request.get('/cart/list', { params })
}

// 加入购物车
export const addToCart = (data) => {
    // 对应后端：POST /cart/add (参数：productId, quantity)
    return request.post('/cart/add', null, { params: data })
}

// 删除购物车项
export const deleteCart = (cartId) => {
    // 对应后端：DELETE /cart/delete/{cartId}
    return request.delete(`/cart/delete/${cartId}`)
}

/** ---- 兼容旧命名 ---- */
export const list = getCart
export const add = addToCart
export const del = deleteCart

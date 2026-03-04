import request from '@/utils/request'

// 获取 OSS 预签名参数（直传阿里云 OSS）
export const getOssSignature = () => {
    // 对应后端：GET /get_post_signature_for_oss_upload
    return request.get('/get_post_signature_for_oss_upload', { skipAuthRefresh: true })
}

// 用户/管理员：仅上架商品列表
export const getUserProducts = (params = {}) => {
    // 对应后端：GET /product/AU/list
    return request.get('/product/AU/list', { params })
}

// 用户：商品详情
export const getUserProductDetail = (id) => {
    // 对应后端：GET /product/user/productOne/{id}
    return request.get(`/product/user/productOne/${id}`)
}

// 商家：商品列表
export const getMerchantProducts = (params = {}) => {
    // 对应后端：GET /product/list
    return request.get('/product/list', { params })
}

// 商家：新增商品（JSON 格式，图片 URL 已经包含在 product 数据中）
export const addProduct = (product) => {
    // 对应后端：POST /product/add
    return request.post('/product/add', product)
}

// 商家：更新商品（JSON 格式，图片 URL 已经包含在 product 数据中）
export const updateProduct = (product) => {
    // 对应后端：PUT /product/update
    return request.put('/product/update', product)
}

// 商家：删除商品
export const deleteProduct = (id) => {
    // 对应后端：DELETE /product/{id}
    return request.delete(`/product/${id}`)
}

// 商家：上下架
export const updateProductStatus = (id, status) => {
    // 对应后端：PUT /product/status?id=xx&status=xx
    return request.put('/product/status', null, { params: { id, status } })
}

// 管理员：仅修改分类（categoryname）
export const adminUpdateProductCategory = (product) => {
    // 对应后端：PUT /product/admin/update
    return request.put('/product/admin/update', product)
}

/** ---- 兼容旧命名（避免你其它页面继续报错） ---- */
export const list = getUserProducts
export const getOne = getUserProductDetail

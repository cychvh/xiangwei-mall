import request from '@/utils/request'

// 用户/商家：公告列表
export const getNotices = (params = {}) => {
    // GET /notice/getNotice
    return request.get('/notice/list', { params })
}

// 管理员：公告分页列表
export const getAdminNotices = (params = {}) => {
    // GET /notice/list
    return request.get('/notice/list', { params })
}

// 管理员：新增公告
export const addNotice = (data) => {
    // POST /notice/addNotice
    return request.post('/notice/addNotice', data)
}

// 管理员：删除公告
export const deleteNotice = (id) => {
    // DELETE /notice/deleteNotice?id=xx
    return request.delete('/notice/deleteNotice', { params: { id } })
}

// 管理员：更新公告
export const updateNotice = (data) => {
    // PUT /notice/updateNotice
    return request.put('/notice/updateNotice', data)
}

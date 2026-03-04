import request from '@/utils/request'

export const getAllStats = () => {
    // GET /statistics/getAllStats
    return request.get('/statistics/getAllStats')
}

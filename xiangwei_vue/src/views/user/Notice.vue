<template>
  <div class="merchant-notice"> <el-card>
      <h2 style="margin-bottom: 15px">公告列表</h2>

      <el-table
        :data="noticeList"
        border
        style="width: 100%"
        v-loading="loading"
        empty-text="暂无公告信息"
      >
        <el-table-column prop="id" label="ID" width="80" align="center" />

        <el-table-column prop="title" label="公告标题" width="200" />

        <el-table-column prop="content" label="公告内容" />

        <el-table-column label="发布时间" width="200" align="center">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        style="margin-top: 20px; text-align: right"
        background
        layout="total, prev, pager, next"
        :current-page="pageNum"
        :page-size="pageSize"
        :total="total"
        @current-change="handlePageChange"
      />
    </el-card>
  </div>
</template>

<script>
import { getNotices } from '@/api/notice'

export default {
  name: 'UserNotice', // 如果是用户页面，改为 UserNotice
  data() {
    return {
      noticeList: [],
      pageNum: 1,
      pageSize: 5,
      total: 0,
      loading: false,
      userInfo: null
    }
  },
  created() {
    const user = localStorage.getItem('userInfo')
    if (!user) {
      this.$message.error('未登录，请重新登录')
      return
    }
    this.userInfo = JSON.parse(user)
    this.loadNotice()
  },
  methods: {
    loadNotice() {
      this.loading = true
      
      // 🚀 修复 1：后端已经从 Header 获取 type 了，这里 params 只需要传分页参数即可。
      // (前提是您的 src/utils/request.js 拦截器里已经全局把 type 塞进 Header 了)
      getNotices({
        pageNum: this.pageNum,
        pageSize: this.pageSize
      }).then(res => {
        if (res.code === 200 || res.code === '200') {
          this.noticeList = res.data.records || []
          this.total = res.data.total || 0
        } else {
          this.$message.error(res.msg || '获取公告失败')
        }
      }).catch(err => {
        console.error('获取公告异常:', err)
      }).finally(() => {
        this.loading = false
      })
    },

    handlePageChange(page) {
      this.pageNum = page
      this.loadNotice()
    },

    formatDate(dateStr) {
      if (!dateStr) return '-'
      return String(dateStr).replace('T', ' ')
    }
  }
}
</script>

<style scoped>
.merchant-notice {
  padding: 20px;
}
.user-notice {
  padding: 20px;
}
</style>
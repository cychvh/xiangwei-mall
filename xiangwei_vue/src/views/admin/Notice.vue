<template>
  <div>
    <el-card style="margin-bottom: 15px;">
      <el-input
        v-model="search"
        placeholder="请输入公告内容关键字"
        style="width: 250px; margin-right: 10px"
        clearable
      />
      <el-button type="primary" @click="getNotices">搜索</el-button>
      <el-button type="success" style="margin-left: 10px" @click="openAddDialog">
        新增公告
      </el-button>
    </el-card>

    <el-table :data="notices" border>
      <el-table-column prop="id" label="ID" width="80" align="center" />
      
      <el-table-column prop="title" label="公告标题" width="180" />
      
      <el-table-column prop="content" label="公告内容" />
      <el-table-column prop="createTime" label="发布时间" width="180" />
      
      <el-table-column label="发送对象" width="120" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.type === 'User'" type="success">用户</el-tag>
          <el-tag v-else-if="row.type === 'Merchant'" type="warning">商家</el-tag>
          <el-tag v-else type="info">全部</el-tag>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="180" align="center">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="openEditDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      background
      layout="total, prev, pager, next"
      :current-page="pageNum"
      :page-size="pageSize"
      :total="total"
      @current-change="handlePageChange"
      style="margin-top: 15px; text-align: right"
    />

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" label-width="80px">
        
        <el-form-item label="公告标题">
          <el-input v-model="form.title" placeholder="请输入公告标题" />
        </el-form-item>

        <el-form-item label="公告内容">
          <el-input v-model="form.content" type="textarea" :rows="4" placeholder="请输入具体的公告内容" />
        </el-form-item>

        <el-form-item label="发送对象">
          <el-select v-model="form.type" style="width: 100%;">
            <el-option label="全部" value="All" />
            <el-option label="用户" value="User" />
            <el-option label="商家" value="Merchant" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitNotice">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { addNotice, getAdminNotices, deleteNotice as deleteNoticeApi, updateNotice } from '@/api/notice'
import { ElMessage, ElMessageBox } from 'element-plus'

/* 表格 & 分页 */
const notices = ref([])
const pageNum = ref(1)
const pageSize = ref(5)
const total = ref(0)
const search = ref('')

/* 弹窗 */
const dialogVisible = ref(false)
const dialogTitle = ref('')
const form = ref({
  id: null,
  title: '',   // 🚀 新增了 title 的初始绑定
  content: '',
  type: 'User'
})

/* 获取公告 */
const getNotices = async () => {
  try {
    const res = await getAdminNotices({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      search: search.value
    })

    if (res.code === '200') {
      notices.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    console.error('获取列表失败:', error)
  }
}

/* 新增 */
const openAddDialog = () => {
  dialogTitle.value = '新增公告'
  // 🚀 重置表单时也要清空 title
  form.value = {
    id: null,
    title: '', 
    content: '',
    type: 'User'
  }
  dialogVisible.value = true
}

/* 编辑 */
const openEditDialog = (row) => {
  dialogTitle.value = '编辑公告'
  form.value = { ...row }
  dialogVisible.value = true
}

/* 提交 */
const submitNotice = async () => {
  // 🚀 新增了对 title 的非空校验
  if (!form.value.title) {
    ElMessage.warning('公告标题不能为空')
    return
  }
  if (!form.value.content) {
    ElMessage.warning('公告内容不能为空')
    return
  }

  try {
    const res = form.value.id ? await updateNotice(form.value) : await addNotice(form.value)

    if (res.code === '200') {
      ElMessage.success('操作成功')
      dialogVisible.value = false
      getNotices()
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (error) {
    console.error('提交失败:', error)
  }
}

/* 删除 */
const handleDelete = (id) => {
  ElMessageBox.confirm('确认删除该公告吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteNoticeApi(id)

      if (res.code === '200') {
        ElMessage.success('删除成功')
        getNotices()
      }
    } catch (error) {
      console.error('删除失败:', error)
    }
  }).catch(() => {}) // 防止取消时控制台报 Uncaught error
}

/* 分页 */
const handlePageChange = (page) => {
  pageNum.value = page
  getNotices()
}

onMounted(() => {
  getNotices()
})
</script>
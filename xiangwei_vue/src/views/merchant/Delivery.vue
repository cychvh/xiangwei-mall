<template>
  <el-card class="box-card" style="margin-top: 20px;">
    <div class="card-header">
      <span><el-icon><Van /></el-icon> 物流信息管理</span>
    </div>

    <div style="margin-bottom: 20px; display: flex; gap: 10px; align-items: center;">
      <el-input
        v-model="searchOrderId"
        placeholder="请输入订单编号"
        style="width: 200px"
        clearable
        @clear="handleSearch"
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <el-table
      :data="deliveryList"
      style="width: 100%"
      v-loading="loading"
      stripe
      border
    >
      <el-table-column prop="orderId" label="订单编号" width="120" />
      <el-table-column prop="expressCompany" label="快递公司" width="150" />
      <el-table-column prop="expressNo" label="快递单号" width="180" />
      <el-table-column prop="shipTime" label="发货时间" width="180">
        <template #default="scope">{{ formatTime(scope.row.shipTime) }}</template>
      </el-table-column>
      <el-table-column prop="status" label="物流状态" width="100">
        <template #default="scope">
          <el-tag :type="getStatusTag(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="120" fixed="right">
        <template #default="scope">
          <el-button
            v-if="scope.row.status === 1"
            type="primary"
            link
            :icon="Edit"
            @click="handleEdit(scope.row)"
          >
            修改
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div style="margin-top: 15px; display: flex; justify-content: flex-end;">
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :page-sizes="[10, 20, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>

    <el-dialog
      v-model="dialogVisible"
      title="修改物流信息"
      width="400px"
      destroy-on-close
    >
      <el-form :model="editForm" ref="editFormRef" label-width="80px">
        <el-form-item
          label="快递公司"
          prop="expressCompany"
          :rules="[{ required: true, message: '请输入快递公司', trigger: 'blur' }]"
        >
          <el-input v-model="editForm.expressCompany" placeholder="请输入快递公司" />
        </el-form-item>
        <el-form-item
          label="快递单号"
          prop="expressNo"
          :rules="[{ required: true, message: '请输入快递单号', trigger: 'blur' }]"
        >
          <el-input v-model="editForm.expressNo" placeholder="请输入快递单号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="submitEdit">
            确认修改
          </el-button>
        </span>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Van, Edit, Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { correct, list } from '@/api/delivery'

const deliveryList = ref([])
const loading = ref(false)
const total = ref(0)

const searchOrderId = ref('')

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10
})

const dialogVisible = ref(false)
const submitLoading = ref(false)
const editFormRef = ref(null)
const editForm = reactive({
  id: null,
  orderId: null,
  expressCompany: '',
  expressNo: ''
})

const fetchDeliveryList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: queryParams.pageNum,
      pageSize: queryParams.pageSize
    }
    if (searchOrderId.value) {
      params.orderId = searchOrderId.value
    }

    const res = await list(params)

    if (res.code === 200 || res.code === '200') {
      deliveryList.value = res.data.records
      total.value = res.data.total
    } else {
      ElMessage.error(res.msg || '获取物流信息失败')
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('无法连接到服务器')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchDeliveryList()
})

const handleSearch = () => {
  queryParams.pageNum = 1
  fetchDeliveryList()
}

const resetSearch = () => {
  searchOrderId.value = ''
  queryParams.pageNum = 1
  fetchDeliveryList()
}

const handlePageChange = (page) => {
  queryParams.pageNum = page
  fetchDeliveryList()
}

const handleSizeChange = (size) => {
  queryParams.pageSize = size
  queryParams.pageNum = 1
  fetchDeliveryList()
}

const handleEdit = (row) => {
  editForm.id = row.id
  editForm.orderId = row.orderId
  editForm.expressCompany = row.expressCompany
  editForm.expressNo = row.expressNo

  dialogVisible.value = true
}

const submitEdit = async () => {
  if (!editFormRef.value) return

  await editFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const res = await correct({
          id: editForm.id,
          orderId: editForm.orderId,
          expressCompany: editForm.expressCompany,
          expressNo: editForm.expressNo
        })

        if (res.code === 200 || res.code === '200') {
          ElMessage.success('物流信息更新成功')
          dialogVisible.value = false
          fetchDeliveryList()
        } else {
          ElMessage.error(res.msg || '更新失败')
        }
      } catch (error) {
        ElMessage.error('更新服务异常')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const formatTime = (time) => {
  if (!time) return ''
  return String(time).replace('T', ' ')
}

const getStatusText = (status) => {
  const map = { 1: '已发货', 2: '已签收', 3: '作废', 4: '退货' }
  return map[status] || '未知状态'
}

const getStatusTag = (status) => {
  return { 1: 'warning', 2: 'success', 3: 'info', 4: 'danger' }[status] || ''
}
</script>

<style scoped>
.box-card { padding: 15px; margin-bottom: 20px; }
.card-header { font-weight: bold; font-size: 16px; color: #409EFF; margin-bottom: 10px; display: flex; align-items: center; gap: 5px; }
</style>

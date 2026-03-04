<template>
  <div class="admin-order">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>订单管理</span>
          <el-input
            v-model="orderId"
            placeholder="请输入订单编号"
            clearable
            style="width: 260px"
            @clear="fetchList"
            @keyup.enter="fetchList"
          />
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column prop="id" label="订单编号" width="140" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="totalAmount" label="订单金额" width="120" />
        <el-table-column prop="status" label="订单状态" width="100">
          <template #default="scope">
            <el-tag>{{ statusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, sizes, prev, pager, next"
          :page-sizes="[5, 10, 20]"
          @current-change="fetchList"
          @size-change="fetchList"
        />
      </div>
      <!-- TBD: admin order items endpoint not defined in docs/api-report.md -->
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getAdminOrders } from '@/api/order'

const loading = ref(false)
const tableData = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const orderId = ref('')

const buildParams = () => {
  const params = { pageNum: pageNum.value, pageSize: pageSize.value }
  const raw = String(orderId.value || '').trim()
  if (!raw) return params
  const num = Number(raw)
  if (Number.isNaN(num)) {
    ElMessage.warning('订单编号必须是数字')
    return params
  }
  params.orderId = num
  return params
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getAdminOrders(buildParams())
    if (res.code === '200' || res.code === 200) {
      tableData.value = res.data.records
      total.value = res.data.total
    }
  } catch (err) {
    ElMessage.error(err?.message || '获取订单列表失败')
  } finally {
    loading.value = false
  }
}

const statusText = (s) => ({
  1: '待支付',
  2: '已支付',
  3: '已完成',
  0: '已取消'
}[s] || '未知状态')

fetchList()
</script>

<style scoped>
.admin-order { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.pagination { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>

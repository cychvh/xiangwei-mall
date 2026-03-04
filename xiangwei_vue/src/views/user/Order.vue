<template>
  <div class="order-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>
            <el-icon><List /></el-icon>
            我的订单
          </span>

          <el-input
            v-model="searchName"
            placeholder="按商品名称搜索"
            clearable
            style="width: 260px"
            @clear="fetchOrderList"
          >
            <template #append>
              <el-button :icon="Search" @click="fetchOrderList" />
            </template>
          </el-input>
        </div>
      </template>

      <el-table :data="orderList" v-loading="loading" stripe>
        <el-table-column prop="id" label="订单编号" width="140" />

        <el-table-column label="实付金额" width="120">
          <template #default="scope">
            <span class="money">¥{{ Number(scope.row.totalAmount || 0).toFixed(2) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="订单状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusTag(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="下单时间" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="320" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click="showDetail(scope.row)">
              详情
            </el-button>

            <el-button
              v-if="scope.row.status === 2"
              link
              type="info"
              @click="handleQueryDelivery(scope.row)"
            >
              查询快递
            </el-button>

            <el-button
              v-if="scope.row.status === 2"
              link
              type="success"
              @click="handleConfirm(scope.row)"
            >
              确认收货
            </el-button>

            <el-button
              v-if="scope.row.status === 3"
              link
              type="warning"
              @click="openReviewDialog(scope.row)"
            >
              去评价
            </el-button>

            <el-button
              v-if="scope.row.status === 3"
              link
              type="danger"
              @click="openAfterDialog(scope.row)"
            >
              申请售后
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          layout="total, prev, pager, next"
          :total="total"
          @current-change="fetchOrderList"
        />
      </div>
    </el-card>

    <el-dialog v-model="detailVisible" title="订单详情" width="600px">
      <el-table :data="orderItems" border v-loading="detailLoading">
        <el-table-column prop="productName" label="商品名称" />
        <el-table-column prop="price" label="单价" width="100" />
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column label="小计" width="120">
          <template #default="scope">
            ¥{{ (scope.row.price * scope.row.quantity).toFixed(2) }}
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog v-model="deliveryVisible" title="物流信息" width="420px">
      <div v-if="delivery">
        <p>快递公司：{{ delivery.expressCompany }}</p>
        <p>快递单号：{{ delivery.expressNo }}</p>
        <p>发货时间：{{ formatTime(delivery.shipTime) }}</p>
        <p>
          物流状态：
          <el-tag :type="deliveryStatusTag(delivery.status)">
            {{ deliveryStatusText(delivery.status) }}
          </el-tag>
        </p>
      </div>
    </el-dialog>

    <el-dialog v-model="reviewVisible" title="发表评价" width="500px" @close="resetReviewForm">
      <el-form :model="reviewForm" ref="reviewFormRef" label-width="80px">
        <el-form-item label="商品评分" required>
          <el-rate v-model="reviewForm.rating" show-text />
        </el-form-item>
        
        <el-form-item label="评价内容" required>
          <el-input 
            v-model="reviewForm.content" 
            type="textarea" 
            :rows="4" 
            placeholder="商品满足您的期待吗？说说您的使用心得吧！" 
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="reviewVisible = false">取消</el-button>
          <el-button type="primary" @click="submitReview" :loading="submittingReview">
            提交评价
          </el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="afterVisible" title="申请售后 (退货物流填写)" width="450px" @close="resetAfterForm">
      <el-form :model="afterForm" label-width="100px">
        <el-alert title="请先将商品寄回，并在此填写退货物流信息" type="info" show-icon style="margin-bottom: 20px" :closable="false" />
        
        <el-form-item label="快递公司" required>
          <el-input v-model="afterForm.expressCompany" placeholder="请输入退货快递公司(如：顺丰/圆通)" />
        </el-form-item>
        
        <el-form-item label="快递单号" required>
          <el-input v-model="afterForm.expressNo" placeholder="请输入退货快递单号" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="afterVisible = false">取消</el-button>
          <el-button type="danger" @click="submitAfter" :loading="submittingAfter">
            提交售后申请
          </el-button>
        </span>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, List } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { getOrderItems, getUserOrders, confirmReceipt } from '@/api/order'
import { getOne as getDelivery } from '@/api/delivery'

const loading = ref(false)
const orderList = ref([])
const total = ref(0)
const searchName = ref('')
const query = reactive({ pageNum: 1, pageSize: 10 })

const detailVisible = ref(false)
const detailLoading = ref(false)
const orderItems = ref([])

const deliveryVisible = ref(false)
const delivery = ref(null)

// 评价相关数据
const reviewVisible = ref(false)
const submittingReview = ref(false)
const currentOrder = ref(null)
const reviewForm = reactive({
  orderId: null,
  productId: null,
  rating: 5,
  content: '',
  images: ''
})

// 🚀 新增：售后相关数据
const afterVisible = ref(false)
const submittingAfter = ref(false)
const afterForm = reactive({
  orderId: null,
  expressCompany: '',
  expressNo: ''
})

const fetchOrderList = async () => {
  loading.value = true
  try {
    const res = await getUserOrders({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      productName: searchName.value
    })
    if (res.code === 200 || res.code === '200') {
      orderList.value = res.data.records
      total.value = res.data.total
    }
  } finally {
    loading.value = false
  }
}

const showDetail = async (row) => {
  detailVisible.value = true
  detailLoading.value = true
  try {
    const res = await getOrderItems(row.id)
    if (res.code === 200 || res.code === '200') {
      orderItems.value = res.data
    }
  } finally {
    detailLoading.value = false
  }
}

const handleQueryDelivery = async (row) => {
  const res = await getDelivery({ orderId: row.id })
  if (res.code === 200 || res.code === '200') {
    delivery.value = res.data
    deliveryVisible.value = true
  }
}

const handleConfirm = async (row) => {
  await ElMessageBox.confirm('确认已收到商品？', '确认收货', { type: 'success' })
  await confirmReceipt(row.id)
  ElMessage.success('确认收货成功')
  fetchOrderList()
}

// 打开评价弹窗
const openReviewDialog = async (row) => {
  currentOrder.value = row
  reviewForm.orderId = row.id
  
  try {
    const res = await getOrderItems(row.id)
    if (res.code === 200 || res.code === '200') {
      if (res.data && res.data.length > 0) {
        reviewForm.productId = res.data[0].productId
        reviewVisible.value = true
      } else {
        ElMessage.warning('该订单异常，找不到商品信息')
      }
    }
  } catch (error) {
    ElMessage.error('获取商品信息失败，无法评价')
  }
}

// 提交评价
const submitReview = async () => {
  if (!reviewForm.content.trim()) {
    ElMessage.warning('请填写评价内容')
    return
  }

  submittingReview.value = true
  try {
    const res = await request.post('/product/review/add', reviewForm)
    if (res.code === 200 || res.code === '200') {
      ElMessage.success('评价成功！感谢您的反馈')
      reviewVisible.value = false
    } else {
      ElMessage.error(res.msg || '评价失败')
    }
  } catch (error) {
    console.error('提交评价异常:', error)
  } finally {
    submittingReview.value = false
  }
}

const resetReviewForm = () => {
  reviewForm.orderId = null
  reviewForm.productId = null
  reviewForm.rating = 5
  reviewForm.content = ''
  reviewForm.images = ''
}

// 🚀 新增：打开售后弹窗
const openAfterDialog = (row) => {
  afterForm.orderId = row.id
  afterVisible.value = true
}

// 🚀 新增：提交售后请求
const submitAfter = async () => {
  if (!afterForm.expressCompany.trim() || !afterForm.expressNo.trim()) {
    ElMessage.warning('请完整填写退货的快递公司和单号')
    return
  }

  submittingAfter.value = true
  try {
    // 严格匹配您的后端参数：orderId 为 params，orderDelivery 为 body
    const res = await request.put('/order/after', {
      expressCompany: afterForm.expressCompany,
      expressNo: afterForm.expressNo
    }, {
      params: { orderId: afterForm.orderId }
    })

    if (res.code === 200 || res.code === '200') {
      ElMessage.success('售后申请提交成功！')
      afterVisible.value = false
      fetchOrderList() // 刷新列表，更新订单状态
    } else {
      ElMessage.error(res.msg || '售后申请失败')
    }
  } catch (error) {
    console.error('提交售后异常:', error)
  } finally {
    submittingAfter.value = false
  }
}

// 🚀 新增：重置售后表单
const resetAfterForm = () => {
  afterForm.orderId = null
  afterForm.expressCompany = ''
  afterForm.expressNo = ''
}

const getStatusText = (s) =>
  ({ 1: '已支付', 2: '已发货', 3: '已完成', 0: '已取消', 4: '售后中', 5: '已退款' }[s] || '未知状态')

const getStatusTag = (s) =>
  ({ 1: 'warning', 2: 'primary', 3: 'success', 0: 'info', 4: 'danger', 5: 'info' }[s] || 'info')

const deliveryStatusText = (s) =>
  ({ 1: '已发货', 2: '已签收', 3: '作废' }[s] || '未知状态')

const deliveryStatusTag = (s) =>
  ({ 1: 'primary', 2: 'success', 3: 'info' }[s] || 'info')

const formatTime = (t) => (t ? String(t).replace('T', ' ') : '')

onMounted(fetchOrderList)
</script>

<style scoped>
.order-container {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}
.box-card {
  max-width: 1100px;
  margin: auto;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.money {
  color: #ff4d4f;
  font-weight: bold;
}
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
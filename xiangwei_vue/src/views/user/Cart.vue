<template>
  <div class="cart-container">
    <el-card class="box-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-icon :size="20"><ShoppingCart /></el-icon>
            <span class="header-title">我的购物车</span>
          </div>
          <el-button type="danger" plain size="small" :disabled="!multipleSelection.length" @click="batchDelete">
            批量删除
          </el-button>
        </div>
      </template>

      <el-table
        :data="cartList"
        v-loading="loading"
        row-key="id"
        @selection-change="handleSelectionChange"
        style="width: 100%"
        empty-text="购物车空空如也，快去买买买吧！"
      >
        <el-table-column type="selection" width="55" align="center" />

        <el-table-column label="商品图" width="100">
          <template #default="scope">
            <el-image
              :src="getFullImgUrl(scope.row.productImage)"
              class="cart-product-img"
              fit="cover"
              :preview-src-list="[getFullImgUrl(scope.row.imageurl)]"
              preview-teleported
            >
              <template #error>
                <div class="image-error">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </template>
        </el-table-column>

        <el-table-column label="商品信息" min-width="180">
          <template #default="scope">
            <div class="product-info">
              <div class="name">{{ scope.row.productName || '未知商品' }}</div>
              <div class="id-tag">ID: {{ scope.row.productId }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="单价" width="120">
          <template #default="scope">
            <span class="price-text">￥{{ formatPrice(scope.row.productPrice) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="数量" width="160">
          <template #default="scope">
            <el-input-number
              v-model="scope.row.quantity"
              :min="1"
              :max="99"
              size="small"
              @change="(val) => handleQuantityChange(val, scope.row)"
            />
          </template>
        </el-table-column>

        <el-table-column label="小计" width="120">
          <template #default="scope">
            <span class="subtotal-text">￥{{ formatPrice(scope.row.productPrice * scope.row.quantity) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="100" fixed="right" align="center">
          <template #default="scope">
            <el-popconfirm title="确定从购物车移除吗？" @confirm="removeCartItem(scope.row.id)">
              <template #reference>
                <el-button type="danger" link>删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="cart-footer">
        <div class="footer-left">
          <span>已勾选 <span class="highlight">{{ multipleSelection.length }}</span> 件商品</span>
        </div>
        <div class="footer-right">
          <div class="total-info">
            合计：<span class="total-price">￥{{ formatPrice(totalPrice) }}</span>
          </div>
          <el-button
            type="primary"
            size="large"
            class="checkout-btn"
            :disabled="multipleSelection.length === 0"
            @click="handleCheckout"
          >
            立即结算
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ShoppingCart, Picture } from '@element-plus/icons-vue'
import request from '@/utils/request'

// 图片保存在OSS，直接使用OSS地址
const IMG_BASE_URL = 'https://cycliving-2026.oss-cn-shenzhen.aliyuncs.com'

const loading = ref(false)
const cartList = ref([])
const multipleSelection = ref([])

// --- 计算属性 ---
const totalPrice = computed(() => {
  return multipleSelection.value.reduce((sum, item) => {
    return sum + ((item.productPrice || item.price || 0) * (item.quantity || 0))
  }, 0)
})

// --- 方法 ---

// 格式化价格
const formatPrice = (val) => Number(val || 0).toFixed(2)

// 处理图片URL
const getFullImgUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  // 如果后端返回的是 /files/xxx.png，则拼接，否则可能需要按下载接口拼接
  return `${IMG_BASE_URL}${url.startsWith('/') ? '' : '/'}${url}`
}

// 获取购物车列表
const fetchCartList = async () => {
  loading.value = true
  try {
    const res = await request.get('/cart/list')
    if (res.code === 200 || res.code === '200') {
      // 兼容分页和普通列表返回
      cartList.value = res.data.records || res.data || []
    }
  } catch (error) {
    console.error('获取购物车失败:', error)
    ElMessage.error('无法加载购物车数据')
  } finally {
    loading.value = false
  }
}

// 修改数量 - 后端购物车只有新增和删除，没有更新接口
// 方案：删除旧项，重新添加
const handleQuantityChange = async (val, row) => {
  const qty = Number(val)
  if (!Number.isFinite(qty) || qty < 1) return

  // 注意：后端 CartVO 返回的是 productId（驼峰），不是 productid
  const productId = row.productId || row.productid
  if (!productId) {
    ElMessage.error('商品ID获取失败')
    return
  }

  try {
    // 先删除旧的
    await request.delete(`/cart/delete/${row.id}`)
    // 再添加新的（数量为新数量）
    await request.post('/cart/add', null, { params: { productId: productId, quantity: qty } })
    ElMessage.success('更新成功')
    fetchCartList()
  } catch (err) {
    console.error('更新数量失败详情:', err)
    ElMessage.error('更新数量失败')
    fetchCartList()
  }
}


// 删除单个商品
const removeCartItem = async (cartId) => {
  try {
    const res = await request.delete(`/cart/delete/${cartId}`)
    if (res.code === 200 || res.code === '200') {
      ElMessage.success('移除成功')
      fetchCartList()
    }
  } catch (err) {
    ElMessage.error('删除失败')
  }
}

// 多选处理
const handleSelectionChange = (val) => {
  multipleSelection.value = val
}

// 结算逻辑
const handleCheckout = () => {
  if (!multipleSelection.value.length) return

  ElMessageBox.confirm(
    `本次结算总额为 ￥${formatPrice(totalPrice.value)}，确认提交订单吗？`,
    '确认下单',
    { confirmButtonText: '立即支付', cancelButtonText: '取消', type: 'info' }
  ).then(async () => {
    try {
      // 构建符合后端接收要求的 OrderDTO
      const orderDTO = {
        order: {
          totalAmount: totalPrice.value,
          status: 1 // 1代表待支付或已下单
        },
        orderItems: multipleSelection.value.map(item => ({
          productId: item.productId || item.productid,
          quantity: item.quantity,
          productName: item.productName,
          productPrice: item.productPrice || item.price
        }))
      }

      console.log('发送下单数据:', JSON.stringify(orderDTO))

      const res = await request.post('/order/addOrder', orderDTO)

      if (res.code === 200 || res.code === '200') {
        ElMessage.success('下单成功！')
        fetchCartList() // 刷新购物车
      } else {
        ElMessage.error(res.msg || '下单失败')
      }
    } catch (error) {
      console.error('结算异常详情:', error)
      ElMessage.error('系统繁忙：请求未成功发送到服务器')
    }
  })
}

onMounted(fetchCartList)
</script>

<style scoped>
.cart-container {
  padding: 20px;
  background-color: #f8f9fa;
  min-height: calc(100vh - 100px);
}

.box-card {
  max-width: 1200px;
  margin: 0 auto;
  border-radius: 12px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-title {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.cart-product-img {
  width: 70px;
  height: 70px;
  border-radius: 8px;
  border: 1px solid #ebeef5;
}

.product-info .name {
  font-weight: 500;
  color: #333;
  margin-bottom: 5px;
}

.id-tag {
  font-size: 11px;
  color: #909399;
  background: #f4f4f5;
  padding: 2px 6px;
  border-radius: 4px;
  display: inline-block;
}

.price-text {
  color: #606266;
  font-weight: bold;
}

.subtotal-text {
  color: #f56c6c;
  font-weight: bold;
}

.cart-footer {
  margin-top: 30px;
  padding: 20px;
  background: #fff;
  border-top: 1px solid #ebeef5;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.footer-right {
  display: flex;
  align-items: center;
  gap: 30px;
}

.total-price {
  font-size: 26px;
  color: #f56c6c;
  font-weight: bold;
  margin-left: 10px;
}

.checkout-btn {
  padding: 0 40px;
  font-weight: bold;
  height: 50px;
  font-size: 16px;
}

.image-error {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  color: #dadce0;
}

.highlight {
  color: #409eff;
  font-weight: bold;
  font-size: 18px;
  margin: 0 4px;
}
</style>

<template>
  <div class="product-detail" v-loading="loading">
    <el-breadcrumb separator="/">
      <el-breadcrumb-item to="/user/home">首页</el-breadcrumb-item>
      <el-breadcrumb-item>商品详情</el-breadcrumb-item>
    </el-breadcrumb>

    <el-row :gutter="40" style="margin-top: 30px" v-if="product.id">
      <el-col :span="10">
        <el-image
          :src="getImageUrl(product.imageurl)"
          fit="cover"
          style="width: 100%; height: 320px; border-radius: 8px"
          :preview-src-list="[getImageUrl(product.imageurl)]"
        >
          <template #error>
            <div class="img-error">暂无图片</div>
          </template>
        </el-image>
      </el-col>

      <el-col :span="14">
        <h1>{{ product.name }}</h1>
        <h2 style="color: red">¥ {{ product.price }}</h2>

        <el-descriptions :column="1" border style="margin-top: 20px">
          <el-descriptions-item label="分类">
            {{ product.categoryname }}
          </el-descriptions-item>
          <el-descriptions-item label="产地">
            {{ product.originplace }}
          </el-descriptions-item>
          <el-descriptions-item label="库存">
            {{ product.stock }}
          </el-descriptions-item>
        </el-descriptions>

        <div style="margin: 20px 0">
          <span style="margin-right: 10px">购买数量：</span>

          <el-input-number
            v-if="product.stock > 0"
            v-model="quantity"
            :min="1"
            :max="product.stock"
          />
          <span v-else style="color: #999">暂无库存</span>
        </div>

        <el-button
          type="primary"
          size="large"
          :disabled="product.stock <= 0"
          @click="addToCart"
        >
          加入购物车
        </el-button>
      </el-col>
    </el-row>

    <div class="review-section" v-if="product.id">
      <el-divider content-position="left"><h3>商品评价</h3></el-divider>
      
      <div v-loading="reviewsLoading">
        <el-empty v-if="reviews.length === 0" description="该商品暂无评论，快来抢沙发吧！" />
        
        <div v-else class="review-list">
          <el-card v-for="review in reviews" :key="review.id" class="review-item" shadow="never">
            <div class="review-header">
              <span class="review-user"><el-icon><User /></el-icon> 匿名用户</span>
              <span class="review-time">{{ formatTime(review.createTime) }}</span>
            </div>
            <div class="review-content">
              {{ review.content }}
            </div>
          </el-card>
        </div>

        <el-pagination
          v-if="reviewTotal > 0"
          style="margin-top: 20px; justify-content: flex-end;"
          background
          layout="total, prev, pager, next"
          :current-page="reviewPageNum"
          :page-size="reviewPageSize"
          :total="reviewTotal"
          @current-change="handleReviewPageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User } from '@element-plus/icons-vue' // 引入用户图标
import request from '@/utils/request' // 🚀 新增引入 request 用于请求评论接口
import { addToCart as addToCartAPI } from '@/api/cart'
import { getUserProductDetail } from '@/api/product'

const route = useRoute()
const productId = route.params.id

const loading = ref(false)
const quantity = ref(1)

const product = ref({
  id: null,
  name: '',
  price: 0,
  stock: 0,
  categoryname: '',
  originplace: '',
  imageurl: ''
})

// 🚀 新增：评论相关的响应式数据
const reviews = ref([])
const reviewTotal = ref(0)
const reviewPageNum = ref(1)
const reviewPageSize = ref(10)
const reviewsLoading = ref(false)

// 图片保存在OSS，直接使用OSS地址
const BASE_IMAGE_URL = 'https://cycliving-2026.oss-cn-shenzhen.aliyuncs.com'
const getImageUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return BASE_IMAGE_URL + (url.startsWith('/') ? '' : '/') + url
}

// 时间格式化工具
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  return String(timeStr).replace('T', ' ')
}

const loadProduct = async () => {
  loading.value = true
  try {
    const res = await getUserProductDetail(productId)
    const ok = res && (res.code === '200' || res.code === 200)
    const data = ok ? res.data : res

    if (data && data.id) {
      product.value = data
      quantity.value = product.value.stock > 0 ? 1 : 0
    } else {
      ElMessage.error((res && res.msg) || '商品不存在')
    }
  } catch (e) {
    ElMessage.error(e?.message || '加载商品失败')
  } finally {
    loading.value = false
  }
}

// 🚀 新增：获取商品评论的方法
const loadReviews = async () => {
  reviewsLoading.value = true
  try {
    
    const res = await request.get(`/product/review/list/${productId}`, {
      params: {
        pageNum: reviewPageNum.value,
        pageSize: reviewPageSize.value
      }
    })
    
    if (res.code === 200 || res.code === '200') {
      reviews.value = res.data.records || []
      reviewTotal.value = res.data.total || 0
    }
  } catch (error) {
    console.error('获取商品评论失败:', error)
  } finally {
    reviewsLoading.value = false
  }
}

// 🚀 新增：处理评论分页的方法
const handleReviewPageChange = (page) => {
  reviewPageNum.value = page
  loadReviews()
}

const addToCart = async () => {
  if (quantity.value <= 0) {
    ElMessage.warning('请选择正确的数量')
    return
  }

  try {
    await addToCartAPI({
      productId: product.value.id,
      quantity: quantity.value
    })

    ElMessage.success({
      message: '商品已加入购物车',
      duration: 2000,
      showClose: true
    })
  } catch (e) {
    ElMessage.error(e?.message || '加入购物车失败')
  }
}

onMounted(() => {
  loadProduct()
  loadReviews() // 🚀 初始化时同时加载商品详情和评论列表
})
</script>

<style scoped>
.product-detail {
  padding: 30px;
  max-width: 1200px;
  margin: 0 auto;
}

.img-error {
  width: 100%;
  height: 320px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  color: #999;
}

/* 🚀 新增：评论区样式 */
.review-section {
  margin-top: 50px;
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.review-item {
  border-radius: 8px;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  border-bottom: 1px dashed #ebeef5;
  padding-bottom: 10px;
}

.review-user {
  font-weight: bold;
  color: #409eff;
  display: flex;
  align-items: center;
  gap: 5px;
}

.review-time {
  font-size: 13px;
  color: #909399;
}

.review-content {
  color: #303133;
  line-height: 1.6;
  font-size: 15px;
}
</style>
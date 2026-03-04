<template>
  <div class="user-home">
    <el-carousel height="220px" indicator-position="outside">
      <el-carousel-item v-for="(item, idx) in banners" :key="idx">
        <div class="banner" :style="{ background: item.bg }">
          <div class="banner-title">{{ item.title }}</div>
          <div class="banner-sub">{{ item.sub }}</div>
        </div>
      </el-carousel-item>
    </el-carousel>

    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="输入商品名称搜索"
        clearable
        @keyup.enter="handleSearch"
      >
        <template #append>
          <el-button :icon="Search" @click="handleSearch">搜索</el-button>
        </template>
      </el-input>
    </div>

    <el-row :gutter="16" v-loading="loading">
      <el-col
        v-for="item in productList"
        :key="item.id"
        :xs="12"
        :sm="8"
        :md="6"
        :lg="6"
      >
        <el-card class="product-card" shadow="hover" @click="goDetail(item.id)">
          <el-image :src="getFullImgUrl(item.imageurl)" fit="cover" class="product-img">
            <template #error>
              <div class="img-error">
                <el-icon><Picture /></el-icon>
              </div>
            </template>
          </el-image>
          <div class="product-info">
            <div class="name">{{ item.name }}</div>
            <div class="price">¥{{ Number(item.price || 0).toFixed(2) }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-empty v-if="!loading && productList.length === 0" description="暂无商品" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Picture } from '@element-plus/icons-vue'
import { getUserProducts } from '@/api/product'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const productList = ref([])
const searchKeyword = ref('')

// 图片保存在OSS，直接使用OSS地址
const IMG_BASE_URL = 'https://cycliving-2026.oss-cn-shenzhen.aliyuncs.com'
const getFullImgUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return `${IMG_BASE_URL}${url.startsWith('/') ? '' : '/'}${url}`
}

const banners = [
  { title: '新鲜农场直供', sub: '源头直采 · 绿色健康', bg: 'linear-gradient(135deg, #2ecc71, #16a085)' },
  { title: '特惠水果季', sub: '当季时令水果，满50立减10元', bg: 'linear-gradient(135deg, #f1c40f, #e67e22)' },
  { title: '有机蔬菜礼盒', sub: '送礼自用两相宜，品质保障', bg: 'linear-gradient(135deg, #3498db, #2980b9)' }
]

const getAllProducts = async (name = '') => {
  loading.value = true
  try {
    const res = await getUserProducts({ pageNum: 1, pageSize: 24, search: name })
    productList.value = res.data?.records ?? []
  } catch (e) {
    console.error('获取产品异常:', e)
    ElMessage.error(e?.message || '无法加载产品列表')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => getAllProducts(searchKeyword.value)
const goDetail = (id) => router.push(`/user/product/${id}`)

onMounted(() => getAllProducts())
</script>

<style scoped>
.user-home {
  padding: 20px;
}

.banner {
  height: 220px;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 24px 32px;
  color: #fff;
}

.banner-title {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 8px;
}

.banner-sub {
  font-size: 14px;
  opacity: 0.9;
}

.search-bar {
  margin: 20px 0;
}

.product-card {
  cursor: pointer;
  margin-bottom: 16px;
  border-radius: 12px;
  overflow: hidden;
}

.product-img {
  width: 100%;
  height: 160px;
}

.img-error {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 160px;
  color: #c0c4cc;
  background: #f5f7fa;
}

.product-info {
  padding: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price {
  color: #f56c6c;
  font-weight: bold;
}
</style>

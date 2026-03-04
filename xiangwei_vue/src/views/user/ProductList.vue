<template>
  <div class="product-list-container">
    <el-card class="search-card" shadow="never" style="margin-bottom:20px;">
      <el-input
        v-model="search"
        placeholder="搜索您心仪的农产品..."
        clearable
        @keyup.enter="loadProducts"
        style="width:400px;"
      >
        <template #append>
          <el-button @click="loadProducts">搜索</el-button>
        </template>
      </el-input>
    </el-card>

    <el-row :gutter="20" v-loading="loading" v-if="productList.length">
      <el-col
        v-for="item in productList"
        :key="item.id"
        :xs="12"
        :sm="8"
        :md="6"
        :lg="6"
      >
        <el-card class="product-card" shadow="hover" @click="goDetail(item.id)">
          <el-image :src="getImageUrl(item.imageurl)" fit="cover" class="product-img">
            <template #error>
              <div class="img-error">无图</div>
            </template>
          </el-image>
          <div class="product-info">
            <h4>{{ item.name }}</h4>
            <div class="price">¥{{ item.price }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-empty v-else description="暂无商品"></el-empty>

    <el-pagination
      v-model:current-page="pageNum"
      v-model:page-size="pageSize"
      :total="total"
      layout="total, sizes, prev, pager, next"
      :page-sizes="[8, 16, 32]"
      @current-change="loadProducts"
      @size-change="loadProducts"
      style="margin-top: 20px; text-align:center;"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getUserProducts } from '@/api/product'

const router = useRouter()
const productList = ref([])
const pageNum = ref(1)
const pageSize = ref(8)
const total = ref(0)
const search = ref('')
const loading = ref(false)

// 图片保存在OSS，直接使用OSS地址
const BASE_URL = 'https://cycliving-2026.oss-cn-shenzhen.aliyuncs.com'

const getImageUrl = (url) => {
  if (!url) return ''
  return url.startsWith('http') ? url : BASE_URL + url
}

const loadProducts = async () => {
  loading.value = true
  try {
    const res = await getUserProducts({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      search: search.value
    })
    if (res.code === 200 || res.code === '200') {
      productList.value = res.data.records
      total.value = res.data.total
    }
  } finally {
    loading.value = false
  }
}

const goDetail = (id) => {
  router.push(`/user/product/${id}`)
}

onMounted(loadProducts)
</script>

<style scoped>
.product-card { cursor: pointer; margin-bottom: 20px; border-radius: 12px; overflow:hidden; }
.product-img { width:100%; height:180px; }
.img-error { display:flex; align-items:center; justify-content:center; height:180px; color:#999; }
.product-info { padding:10px; }
.price { color:#f56c6c; font-weight:bold; }
</style>

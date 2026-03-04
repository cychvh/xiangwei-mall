<template>
  <div style="padding: 20px">
    <div style="margin-bottom: 20px; display: flex; gap: 10px;">
      <el-input
        v-model="search"
        placeholder="请输入商品名称搜索"
        style="width: 200px"
        clearable
        @clear="load"
        @keyup.enter="load"
      />
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="success" @click="handleAdd">新增商品</el-button>
    </div>

    <el-table :data="tableData" border v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column label="商品图片" width="100" align="center">
        <template #default="{ row }">
          <el-image
            style="width: 50px; height: 50px"
            :src="getImageUrl(row.imageurl)"
            :preview-src-list="[getImageUrl(row.imageurl)]"
            fit="cover"
          >
            <template #error>
              <div style="font-size: 12px; color: #999;">无图</div>
            </template>
          </el-image>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="商品名称" />
      <el-table-column prop="categoryname" label="分类" />
      <el-table-column prop="price" label="价格">
        <template #default="{ row }">¥{{ row.price }}</template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" />
      <el-table-column prop="originplace" label="产地" />
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '上架' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" align="center">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button
            size="small"
            :type="row.status === 1 ? 'warning' : 'success'"
            @click="toggleStatus(row)"
          >
            {{ row.status === 1 ? '下架' : '上架' }}
          </el-button>
          <el-popconfirm title="确定删除吗？" @confirm="handleDelete(row)">
            <template #reference>
              <el-button size="small" type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <div style="margin-top: 20px; text-align: right;">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :page-sizes="[5, 10, 20]"
        layout="total, sizes, prev, pager, next"
        :total="total"
        @current-change="load"
        @size-change="load"
      />
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑商品' : '新增商品'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="分类"><el-input v-model="form.categoryname" /></el-form-item>
        <el-form-item label="价格"><el-input-number v-model="form.price" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="库存"><el-input-number v-model="form.stock" :min="0" /></el-form-item>
        <el-form-item label="产地"><el-input v-model="form.originplace" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">上架</el-radio>
            <el-radio :label="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="图片">
          <el-upload
            :auto-upload="false"
            :limit="1"
            :file-list="fileList"
            :on-change="handleFileChange"
          >
            <el-button type="primary">选择图片</el-button>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElLoading } from 'element-plus'
import {
  addProduct,
  deleteProduct,
  getMerchantProducts,
  updateProductStatus,
  updateProduct,
  getOssSignature
} from '@/api/product'

const tableData = ref([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(5)
const total = ref(0)
const search = ref('')

const dialogVisible = ref(false)
const isEdit = ref(false)
const fileList = ref([])
const currentFile = ref(null)
const uploading = ref(false)
// 图片保存在OSS，直接使用OSS地址
const BASE_IMAGE_URL = 'https://cycliving-2026.oss-cn-shenzhen.aliyuncs.com'

const form = reactive({
  id: null,
  name: '',
  categoryname: '',
  price: 0,
  stock: 0,
  originplace: '',
  status: 1,
  imageurl: '',
  merchantId: null
})

const getImageUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return BASE_IMAGE_URL + url
}

const handleAdd = () => {
  resetForm()
  isEdit.value = false
  dialogVisible.value = true
}

const handleEdit = (row) => {
  resetForm()
  Object.assign(form, row)
  // 编辑时如果有图片，设置到fileList显示
  if (row.imageurl) {
    fileList.value = [{ name: 'image', url: getImageUrl(row.imageurl) }]
  }
  isEdit.value = true
  dialogVisible.value = true
}

const handleFileChange = (file) => {
  currentFile.value = file.raw
  fileList.value = [file]
}

const resetForm = () => {
  Object.assign(form, {
    id: null,
    name: '',
    categoryname: '',
    price: 0,
    stock: 0,
    originplace: '',
    status: 1,
    imageurl: '',
    merchantId: null
  })
  fileList.value = []
  currentFile.value = null
}

const load = () => {
  loading.value = true
  getMerchantProducts({
    pageNum: pageNum.value,
    pageSize: pageSize.value,
    search: search.value
  }).then(res => {
    tableData.value = res.data.records
    total.value = res.data.total
  }).finally(() => {
    loading.value = false
  })
}

// 上传图片到 OSS
const uploadToOss = async (file) => {
  // 1. 获取 OSS 预签名参数
  const ossRes = await getOssSignature()
  if (ossRes.code != 200) {
    throw new Error(ossRes.msg || '获取上传签名失败')
  }
  const ossData = ossRes.data

  // 2. 生成唯一文件名
  const fileExt = file.name.substring(file.name.lastIndexOf('.'))
  const fileName = `${ossData.dir}/${Date.now()}${Math.random().toString(36).substr(2, 9)}${fileExt}`

  // 3. 构建 FormData 上传到 OSS
  const formData = new FormData()
  formData.append('key', fileName)
  formData.append('policy', ossData.policy)
  formData.append('x-oss-credential', ossData.x_oss_credential)
  formData.append('x-oss-date', ossData.x_oss_date)
  formData.append('x-oss-signature-version', ossData.version)
  formData.append('x-oss-security-token', ossData.security_token)
  formData.append('success_action_status', '200')
  
  
  // 🚀 核心修复：V4 版本必须叫 x-oss-signature ！！！
  formData.append('x-oss-signature', ossData.signature) 
  
  formData.append('file', file) // file 依然保持在最后

  // 4. 上传到 OSS
  const ossUploadRes = await fetch(ossData.host, {
    method: 'POST',
    body: formData
  })

  // 🚀 神级防坑：如果依然报错，直接把阿里云的底层 XML 报错提示打印出来！
  if (!ossUploadRes.ok) {
    const errorXmlText = await ossUploadRes.text()
    console.error('💥 OSS上传失败，阿里云返回的真实原因:', errorXmlText)
    throw new Error('图片上传到阿里云失败，请检查 F12 控制台打印的真实原因')
  }

  // 5. 返回完整图片 URL
  return ossData.host + '/' + fileName
}

const save = async () => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  if (!userInfo.id) {
    ElMessage.error('未获取到用户信息，请重新登录')
    return
  }

  // 表单验证
  if (!form.name) {
    ElMessage.error('请输入商品名称')
    return
  }

  form.merchantId = userInfo.id

  const loadingInstance = ElLoading.service({ text: '处理中...' })

  try {
    // 如果有新选择的图片，先上传到 OSS
    if (currentFile.value) {
      const imageUrl = await uploadToOss(currentFile.value)
      form.imageurl = imageUrl
    }

    // 如果是编辑模式且没有选择新图片，保留原图片
    // form.imageurl 已经是原来的值

    // 提交商品信息（JSON 格式）
    const requestFn = isEdit.value ? updateProduct : addProduct
    await requestFn(form)

    ElMessage.success('操作成功')
    dialogVisible.value = false
    load()
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error(error.message || '操作失败')
  } finally {
    loadingInstance.close()
  }
}

const handleDelete = (row) => {
  deleteProduct(row.id).then(() => {
    ElMessage.success('删除成功')
    load()
  })
}

const toggleStatus = (row) => {
  const status = row.status === 1 ? 0 : 1
  updateProductStatus(row.id, status).then(load)
}

onMounted(load)
</script>

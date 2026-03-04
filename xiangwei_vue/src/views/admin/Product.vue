<template>
  <div class="admin-product">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>商品管理</span>
          <el-input
            v-model="search"
            placeholder="请输入商品名称"
            clearable
            style="width: 260px"
            @clear="fetchList"
            @keyup.enter="fetchList"
          />
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="商品名称" />
        <el-table-column prop="categoryname" label="商品分类" />
        <el-table-column prop="price" label="价格" width="120" />
        <el-table-column prop="stock" label="库存" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140">
          <template #default="scope">
            <el-button type="primary" link @click="openEdit(scope.row)">编辑分类</el-button>
          </template>
        </el-table-column>
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
    </el-card>

    <el-dialog v-model="dialogVisible" title="编辑商品分类" width="420px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="分类名称">
          <el-input v-model="editForm.categoryname" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { adminUpdateProductCategory, getUserProducts } from '@/api/product'

const loading = ref(false)
const tableData = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const search = ref('')

const dialogVisible = ref(false)
const editForm = ref({ id: null, categoryname: '' })

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getUserProducts({ pageNum: pageNum.value, pageSize: pageSize.value, search: search.value })
    if (res.code === '200') {
      tableData.value = res.data.records
      total.value = res.data.total
    } else {
      ElMessage.error(res.msg || '获取商品列表失败')
    }
  } finally {
    loading.value = false
  }
}

const openEdit = (row) => {
  editForm.value = { id: row.id, categoryname: row.categoryname }
  dialogVisible.value = true
}

const submitEdit = async () => {
  const payload = { id: editForm.value.id, categoryname: editForm.value.categoryname }
  const res = await adminUpdateProductCategory(payload)
  if (res.code === '200') {
    ElMessage.success('编辑商品分类成功')
    dialogVisible.value = false
    fetchList()
  } else {
    ElMessage.error(res.msg || '编辑商品分类失败')
  }
}

fetchList()
</script>

<style scoped>
.admin-product { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.pagination { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>

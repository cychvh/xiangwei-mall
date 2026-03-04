<template>
  <div>
    <!-- 搜索 -->
    <div style="display: flex; gap: 10px; align-items: center; margin-bottom: 10px">
      <el-input
          v-model="search"
          placeholder="请输入用户名关键"
          style="width: 250px"
          clearable
          @keyup.enter="handleSearch"
      />
      <el-button type="primary" @click="handleSearch">搜索用户/商家</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <el-tabs v-model="activeTab" type="card" style="margin-top: 10px">
      <!-- 用户列表 -->
      <el-tab-pane :label="`用户列表${users.length}`" name="users">
        <el-table :data="users" border style="width: 100%" v-loading="loading">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="username" label="用户" />
          <el-table-column prop="phone" label="电话" />
          <el-table-column prop="address" label="地址" />
          <el-table-column label="角色" width="120">
            <template #default="{ row }">
              <el-tag v-if="String(row.type)==='2'">用户</el-tag>
              <el-tag v-else-if="String(row.type)==='1'" type="warning">商家</el-tag>
              <el-tag v-else type="info">管理</el-tag>
            </template>
          </el-table-column>

          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="openEditDialog(row)">编辑</el-button>
              <el-button type="danger" size="small" @click="deleteUser(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页（注意：total 是后端返回的全量总数-->
        <el-pagination
            background
            layout="prev, pager, next"
            :current-page="pageNum"
            :page-size="pageSize"
            :total="total"
            @current-change="handlePageChange"
            style="margin-top: 15px; text-align: right"
        />
      </el-tab-pane>

      <!-- 商家列表 -->
      <el-tab-pane :label="`商家列表${merchants.length}`" name="merchants">
        <el-table :data="merchants" border style="width: 100%" v-loading="loading">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="username" label="商家账号" />
          <el-table-column prop="phone" label="电话" />
          <el-table-column prop="address" label="地址" />
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="openEditDialog(row)">编辑</el-button>
              <el-button type="danger" size="small" @click="deleteUser(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
            background
            layout="prev, pager, next"
            :current-page="pageNum"
            :page-size="pageSize"
            :total="total"
            @current-change="handlePageChange"
            style="margin-top: 15px; text-align: right"
        />
      </el-tab-pane>
    </el-tabs>

    <!-- 编辑弹窗（管理员允许username、type；按接口文档-->
    <el-dialog v-model="dialogVisible" title="编辑用户/商家" width="420px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="用户">
          <el-input v-model="form.username" />
        </el-form-item>

        <el-form-item label="角色类型">
          <!-- 只允许在 商家(1) / 用户(2) 间切换（避免误改成管理员 0-->
          <el-select v-model="form.type" style="width: 100%">
            <el-option label="商家" :value="1" />
            <el-option label="用户" :value="2" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminDeleteUser, adminGetUsers, updateUser } from '@/api/user'

const activeTab = ref('users')

const loading = ref(false)
const saving = ref(false)

const pageNum = ref(1)
const pageSize = ref(5)
const total = ref(0)
const search = ref('')

const records = ref([])

const dialogVisible = ref(false)
const form = ref({ id: null, username: '', type: 2 })

/**
 * 兼容两种返回
 * A) api 返回 Result{code,msg,data}（未解包
 * B) api 直接返回 data（已解包
 */
function extractPage(res) {
  // A: Result 包装
  if (res && (res.code === '200' || res.code === 200) && res.data) {
    return res.data
  }
  // B: 已解page 对象
  if (res && typeof res === 'object' && Array.isArray(res.records)) {
    return res
  }
  return null
}

// Fetch users and merchants
// - /user/getUser: search=username keyword, Usertype=type(1 merchant / 2 user)
const getUsers = async () => {
  loading.value = true
  try {
    const searchValue = String(search.value || '').trim()
    const type = activeTab.value === 'merchants' ? 1 : 2
    const res = await adminGetUsers({
      pageNum: pageNum.value,
      size: pageSize.value,
      search: searchValue,
      Usertype: type
    })

    const page = extractPage(res)
    if (!page) {
      ElMessage.error(res?.msg || '获取列表失败')
      records.value = []
      total.value = 0
      return
    }

    records.value = page.records || []
    total.value = page.total ?? records.value.length
  } catch (err) {
    console.error(err)
    ElMessage.error('获取列表失败')
    records.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 用户：type=2；商家：type=1
const users = computed(() => records.value.filter(u => String(u.type) === '2'))
const merchants = computed(() => records.value.filter(u => String(u.type) === '1'))

const handlePageChange = (page) => {
  pageNum.value = page
  getUsers()
}

const handleSearch = () => {
  pageNum.value = 1
  getUsers()
}

const resetSearch = () => {
  search.value = ''
  pageNum.value = 1
  getUsers()
}

const deleteUser = async (id) => {
  try {
    await ElMessageBox.confirm('确认删除吗？', '提示', { type: 'warning' })
    const res = await adminDeleteUser(id)

    // 兼容解包/未解包：只要没抛异常就认为成功，或检code
    if (res?.code && res.code !== '200' && res.code !== 200) {
      ElMessage.error(res.msg || '删除失败')
      return
    }

    ElMessage.success('删除成功')
    getUsers()
  } catch (e) {
    // 用户取消不提
  }
}

const openEditDialog = (row) => {
  // 只拷贝必要字段，避免把无关字段提交给后端
  form.value = {
    id: row.id,
    username: row.username,
    type: Number(row.type) // 保证是数
  }
  // 防止管理员被编辑为管理员 0
  if (form.value.type !== 1 && form.value.type !== 2) form.value.type = 2
  dialogVisible.value = true
}

const submitEdit = async () => {
  if (!form.value.id) return
  if (!form.value.username) {
    ElMessage.warning('用户名不能为')
    return
  }

  saving.value = true
  try {
    // 接口文档：管理员可更username、type
    const payload = {
      id: form.value.id,
      username: form.value.username,
      type: form.value.type
    }

    const res = await updateUser(payload)

    if (res?.code && res.code !== '200' && res.code !== 200) {
      ElMessage.error(res.msg || '修改失败')
      return
    }

    ElMessage.success('修改成功')
    dialogVisible.value = false
    getUsers()
  } catch (err) {
    console.error(err)
    ElMessage.error('修改失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => getUsers())

watch(activeTab, () => {
  pageNum.value = 1
  getUsers()
})
</script>

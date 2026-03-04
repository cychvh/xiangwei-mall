<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2>乡味农产品电商系统</h2>

      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>

        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" />
        </el-form-item>

        <el-button
            type="primary"
            class="w-100"
            @click="handleLogin"
            :loading="loading"
        >
          登录
        </el-button>

        <!-- 新增：注册入口 -->
        <div class="register-row">
          <span>还没有账号？</span>
          <el-button type="primary" link @click="goRegister">
            立即注册
          </el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const form = ref({ username: '', password: '' })
const router = useRouter()
const loading = ref(false)

const goRegister = () => {
  // 如果你的注册路由不是 /register，请把这里改成你项目实际的路径
  router.push('/register')
}

const handleLogin = async () => {
  loading.value = true
  try {
    const res = await request.post('/user/login', form.value)

    const { code, msg, data } = res
    if (code !== '200') {
      ElMessage.error(msg || '登录失败')
      return
    }

    const accessToken = data?.accessToken || data?.token
    const refreshToken = data?.refreshToken
    const user = data?.user
    if (!accessToken || !user) {
      ElMessage.error('返回数据不完整')
      return
    }

    // 存 token & 用户信息（去重）
    localStorage.setItem('accessToken', accessToken)
    localStorage.setItem('token', accessToken) // 可选：兼容旧逻辑
    if (refreshToken) localStorage.setItem('refreshToken', refreshToken)
    localStorage.setItem('userInfo', JSON.stringify(user))
    localStorage.setItem('role', String(user.type)) // 必须是 '0'/'1'/'2'
    localStorage.setItem('isLogin', 'true')

    // 跳转
    const redirectPath =
        user.type === 0 ? '/admin/home' :
            user.type === 1 ? '/merchant/home' :
                '/user/home'

    ElMessage.success('登录成功')
    await router.replace(redirectPath)
  } catch (e) {
    console.error('登录错误', e)
    ElMessage.error(e?.response?.data?.msg || e?.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #2d3a4b;
}
.login-card {
  width: 400px;
  padding: 20px;
}
.w-100 {
  width: 100%;
}
h2 {
  text-align: center;
  margin-bottom: 30px;
}
.register-row {
  margin-top: 12px;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 6px;
  color: #606266;
}
</style>

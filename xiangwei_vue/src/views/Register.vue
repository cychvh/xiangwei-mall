<template>
  <div class="register-container">
    <el-card class="register-card">
      <template #header>
        <div class="register-header">
          <h2>用户注册</h2>
          <p class="header-subtitle">创建新账户</p>
        </div>
      </template>

      <el-form
          ref="registerFormRef"
          :model="registerForm"
          :rules="registerRules"
          label-width="100px"
          @keyup.enter="handleRegister"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
              v-model="registerForm.username"
              placeholder="请输入用户名"
              clearable
          >
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <!-- 新增：注册类型 -->
        <el-form-item label="注册类型" prop="type">
          <el-select v-model="registerForm.type" placeholder="请选择注册类型" style="width: 100%">
            <el-option label="商家" :value="1" />
            <el-option label="用户" :value="2" />
          </el-select>
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="请输入密码"
              show-password
              clearable
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
          <div class="password-tips">
            密码长度6-20位，必须包含字母和数字
          </div>
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              show-password
              clearable
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <!-- 邮箱改为电话 -->
        <el-form-item label="电话号码" prop="phone">
          <el-input
              v-model="registerForm.phone"
              placeholder="请输入电话号码"
              clearable
          >
            <template #prefix>
              <el-icon><Phone /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-button
              type="primary"
              :loading="loading"
              @click="handleRegister"
              style="width: 100%"
          >
            注册
          </el-button>
        </el-form-item>

        <!-- 底部链接区域 -->
        <div class="form-footer">
          <el-link type="info" @click="goToLogin" :underline="false">
            <el-icon><ArrowLeft /></el-icon>
            返回登录
          </el-link>
          <div class="login-link">
            已有账号？
            <el-link type="primary" @click="goToLogin" :underline="false">
              立即登录
            </el-link>
          </div>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Lock, ArrowLeft, Phone } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()

const registerFormRef = ref()
const loading = ref(false)

// 注册表单数据：新增 type、phone
const registerForm = reactive({
  username: '',
  type: 2, // 默认用户=2
  password: '',
  confirmPassword: '',
  phone: ''
})

// 密码强度验证
const validatePassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入密码'))
  } else if (value.length < 6 || value.length > 20) {
    callback(new Error('密码长度必须在6到20个字符之间'))
  } else if (!/(?=.*[a-zA-Z])(?=.*\d)/.test(value)) {
    callback(new Error('密码必须包含字母和数字'))
  } else {
    if (registerForm.confirmPassword && registerFormRef.value) {
      registerFormRef.value.validateField('confirmPassword')
    }
    callback()
  }
}

// 确认密码
const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

// 电话校验：简单国内 11 位手机号（如需支持座机/海外可放宽）
const validatePhone = (rule, value, callback) => {
  const phoneRegex = /^1\d{10}$/
  if (!value) {
    callback(new Error('请输入电话号码'))
  } else if (!phoneRegex.test(value)) {
    callback(new Error('请输入有效的11位手机号'))
  } else {
    callback()
  }
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' },
    {
      pattern: /^[a-zA-Z0-9_]+$/,
      message: '用户名只能包含字母、数字和下划线',
      trigger: 'blur'
    }
  ],
  type: [
    { required: true, message: '请选择注册类型', trigger: 'change' }
  ],
  password: [
    { required: true, validator: validatePassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ],
  phone: [
    { required: true, validator: validatePhone, trigger: 'blur' }
  ]
}

const goToLogin = () => {
  router.push('/login')
}

// 实际注册请求：按后端文档字段
const performRegister = async () => {
  loading.value = true
  try {
    const response = await request.post('/user/register', {
      username: registerForm.username,
      password: registerForm.password,
      phone: registerForm.phone,
      type: registerForm.type // 商家=1 用户=2
    })

    // 兼容 request 是否解包：若未解包，response.code 存在；若解包可能为空
    if (response?.code && response.code !== '200' && response.code !== 200) {
      // 文档：10086 用户名已被使用
      if (response.code === '10086' || response.code === 10086) {
        ElMessage.error(response.msg || '用户名已被使用')
      } else {
        ElMessage.error(response.msg || '注册失败')
      }
      return
    }

    // 未解包成功：code=200；已解包成功：直接走到这里
    ElMessage.success('注册成功！请登录')

    if (registerFormRef.value) registerFormRef.value.resetFields()
    router.push('/login')
  } catch (error) {
    console.error('注册失败:', error)
    // request.js 如果会 reject(Error(msg))，这里 message 就够用
    ElMessage.error(error?.message || '注册失败，请稍后重试')
    throw error
  } finally {
    loading.value = false
  }
}

// 协议弹窗：确认后执行注册
const showAgreement = () => {
  ElMessageBox.alert(
      `
    <div style="max-height: 400px; overflow-y: auto; padding-right: 10px;">
      <h3 style="margin-bottom: 15px; color: #333;">用户注册协议</h3>
      <ol style="color: #666; line-height: 1.6;">
        <li>您需要提供真实、准确、完整的个人信息。</li>
        <li>您需要妥善保管账户信息，不得将账户转让或出借给他人使用。</li>
        <li>您在使用服务时需遵守相关法律法规，不得从事任何违法活动。</li>
        <li>我们承诺保护您的个人信息安全。</li>
      </ol>
      <p style="margin-top: 15px; color: #999; font-size: 12px;">
        点击"同意并注册"即表示您已阅读并同意以上协议。
      </p>
    </div>
    `,
      '用户协议',
      {
        dangerouslyUseHTMLString: true,
        showCancelButton: true,
        confirmButtonText: '同意并注册',
        cancelButtonText: '取消',
        beforeClose: async (action, instance, done) => {
          if (action === 'confirm') {
            instance.confirmButtonLoading = true
            instance.confirmButtonText = '注册中...'
            try {
              await performRegister()
              done()
            } catch {
              instance.confirmButtonLoading = false
              instance.confirmButtonText = '同意并注册'
              return
            }
          } else {
            done()
          }
        }
      }
  ).catch(() => {
    ElMessage.info('已取消注册')
  })
}

const handleRegister = async () => {
  if (!registerFormRef.value) return
  try {
    const valid = await registerFormRef.value.validate()
    if (!valid) return
    showAgreement()
  } catch (error) {
    console.error('表单验证错误:', error)
  }
}

onMounted(() => {
  console.log('注册页面已加载')
})
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  padding: 20px;
}

.register-card {
  width: 500px;
  border-radius: 12px;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.register-header {
  text-align: center;
  color: #333;
}

.register-header h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #333;
}

.header-subtitle {
  margin: 8px 0 0;
  font-size: 14px;
  color: #666;
}

.password-tips {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
  line-height: 1.4;
}

.form-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.form-footer .el-link {
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.login-link {
  font-size: 14px;
  color: #666;
}

.login-link .el-link {
  margin-left: 4px;
}

@media (max-width: 768px) {
  .register-card {
    width: 90%;
    max-width: 400px;
  }

  .register-container {
    padding: 10px;
  }
}

@media (max-width: 480px) {
  .form-footer {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
  }

  :deep(.el-form-item__label) {
    font-size: 14px;
  }
}
</style>

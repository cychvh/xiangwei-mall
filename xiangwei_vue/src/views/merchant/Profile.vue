<template>
  <el-card>
    <template #header>
      <span>个人信息</span>
    </template>

    <el-form label-width="100px">
      <el-form-item label="用户名">
        <el-input v-model="userInfo.username" disabled />
      </el-form-item>

      <el-form-item label="电话">
        <el-input v-model="userInfo.phone" disabled />
      </el-form-item>

      <el-form-item label="收货地址">
        <el-input
          v-model="userInfo.address"
          placeholder="请输入收货地址"
        />
      </el-form-item>

      <el-form-item label="新密码">
        <el-input
          v-model="newPassword"
          type="password"
          placeholder="请输入新密码"
          show-password
        />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="saveUserInfo">保存修改</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script>
import { updateUser } from '@/api/user'

export default {
  name: 'UserProfile',
  data() {
    return {
      userInfo: {
        id: '',
        username: '',
        phone: '',
        address: ''
      },
      newPassword: ''
    }
  },
  created() {
    const user = localStorage.getItem('userInfo')
    if (user) {
      const parsedUser = JSON.parse(user)
      this.userInfo.id = parsedUser.id || ''
      this.userInfo.username = parsedUser.username || ''
      this.userInfo.phone = parsedUser.phone || ''
      this.userInfo.address = parsedUser.address || ''
    } else {
      this.$message.error('未获取到用户信息')
    }
  },
  methods: {
    saveUserInfo() {
      if (!this.userInfo.address && !this.newPassword) {
        this.$message.warning('请修改收货地址或新密码再保存')
        return
      }

      const updateData = {
        id: this.userInfo.id,
        address: this.userInfo.address
      }
      if (this.newPassword) {
        updateData.password = this.newPassword
      }

      updateUser(updateData)
        .then(() => {
          this.$message.success('保存成功')
          const user = JSON.parse(localStorage.getItem('userInfo') || '{}')
          user.address = this.userInfo.address
          localStorage.setItem('userInfo', JSON.stringify(user))
          this.newPassword = ''
        })
        .catch(() => {
          // 错误提示已在拦截器中处理
        })
    }
  }
}
</script>

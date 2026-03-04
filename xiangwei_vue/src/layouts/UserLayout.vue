<template>
  <el-container>
    <el-header class="user-header">
      <div class="logo">乡味 - 用户端</div>
      <el-menu mode="horizontal" router :default-active="$route.path">
        <el-menu-item index="/user/home">首页</el-menu-item>
        <el-menu-item index="/user/cart">购物车</el-menu-item>
        <el-menu-item index="/user/order">订单状态</el-menu-item>
        <el-menu-item index="/user/profile">个人信息</el-menu-item>
        <el-menu-item index="/user/notice">公告</el-menu-item>
        <el-menu-item @click="handleLogout">退出登录</el-menu-item>
      </el-menu>
    </el-header>
    <el-main>
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { useUserStore } from '@/store/user'
import { useRouter } from 'vue-router'
const store = useUserStore()
const router = useRouter()
const handleLogout = () => { store.logout(); router.push('/login') }
</script>

<style scoped>
.user-header { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #ddd; background: #fff; }
.logo { font-size: 20px; font-weight: bold; color: #409EFF; }
</style>
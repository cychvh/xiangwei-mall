<template>
  <el-container>
    <el-header class="merchant-header">
      <div class="logo">乡味 - 商家后台</div>
      <el-menu mode="horizontal" router :default-active="$route.path">
        <el-menu-item index="/merchant/home">首页</el-menu-item>
        <el-menu-item index="/merchant/stat">数据分析</el-menu-item>
        <el-menu-item index="/merchant/product">商品管理</el-menu-item>
        <el-menu-item index="/merchant/order">订单管理</el-menu-item>
        <el-menu-item index="/merchant/profile">个人信息</el-menu-item>
        <el-menu-item index="/merchant/delivery">物流信息</el-menu-item>
        <el-menu-item index="/merchant/notice">公告</el-menu-item>
        <el-menu-item @click="handleLogout">退出</el-menu-item>
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
.merchant-header { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #ddd; background: #fdf6ec; }
.logo { font-size: 20px; font-weight: bold; color: #E6A23C; }
</style>
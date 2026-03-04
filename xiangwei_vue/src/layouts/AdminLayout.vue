<template>
  <el-container>
    <el-header class="admin-header">
      <div class="logo">乡味 - 总管理后台</div>
      <el-menu mode="horizontal" router :default-active="$route.path">
        <el-menu-item index="/admin/home">首页</el-menu-item>
        <el-menu-item index="/admin/user">用户管理</el-menu-item>
        <el-menu-item index="/admin/notice">公告管理</el-menu-item>
        <el-menu-item index="/admin/order">订单管理</el-menu-item>
        <el-menu-item index="/admin/product">商品管理</el-menu-item>
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
.admin-header { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #ddd; background: #303133; color: white; }
.logo { font-size: 20px; font-weight: bold; color: #fff; }
:deep(.el-menu) { background-color: transparent; }
:deep(.el-menu-item) { color: #fff; }
:deep(.el-menu-item.is-active) { color: #409EFF; }
</style>
import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

// 布局组件
import UserLayout from '@/layouts/UserLayout.vue'
import MerchantLayout from '@/layouts/MerchantLayout.vue'
import AdminLayout from '@/layouts/AdminLayout.vue'


const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue')
  },
  // --- 用户路由 ---
  {
    path: '/user',
    component: UserLayout,
    meta: { role: '2' },
    children: [
      { path: 'home', component: () => import('@/views/user/Home.vue') },
      { path: 'product/:id', component: () => import('@/views/user/ProductDetail.vue') },
      { path: 'cart', component: () => import('@/views/user/Cart.vue') },
      { path: 'order', component: () => import('@/views/user/Order.vue') },
      { path: 'profile', component: () => import('@/views/user/Profile.vue') },
      { path: 'notice', component: () => import('@/views/user/Notice.vue') },
      { path: 'all-products', component: () => import('@/views/user/ProductList.vue') }
    ]
  },
  // --- 商家路由 ---
  {
    path: '/merchant',
    component: MerchantLayout,
    meta: { role: '1' },
    children: [
      { path: 'home', component: () => import('@/views/merchant/Home.vue') },
      { path: 'stat', component: () => import('@/views/merchant/Stat.vue') },
      { path: 'product', component: () => import('@/views/merchant/Product.vue') },
      { path: 'order', component: () => import('@/views/merchant/Order.vue') },
      { path: 'profile', component: () => import('@/views/merchant/Profile.vue') },
      { path: 'delivery', component: () => import('@/views/merchant/Delivery.vue') },
      { path: 'notice', component: () => import('@/views/merchant/Notice.vue') }
    ]
  },
  // --- 管理员路由 ---
  {
    path: '/admin',
    component: AdminLayout,
    meta: { role: '0' },
    children: [
      { path: 'home', component: () => import('@/views/admin/Home.vue') },
      { path: 'user', component: () => import('@/views/admin/User.vue') },
      { path: 'notice', component: () => import('@/views/admin/Notice.vue') },
      { path: 'product', component: () => import('@/views/admin/Product.vue') },
      { path: 'order', component: () => import('@/views/admin/Order.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const accessToken = localStorage.getItem('accessToken') || localStorage.getItem('token')
  const role = localStorage.getItem('role')

  // 未登录：放行登录/注册，其它全部去登录
  if (!accessToken) {
    if (to.path === '/login' || to.path === '/register') {
      return next()
    }
    return next('/login')
  }

  // 已登录：禁止访问登录/注册
  if (to.path === '/login' || to.path === '/register') {
    if (role === '0') return next('/admin/home')
    if (role === '1') return next('/merchant/home')
    if (role === '2') return next('/user/home')
    return next()
  }

  // 角色不匹配：跳回对应首页
  if (to.meta.role && to.meta.role !== role) {
    if (role === '0') return next('/admin/home')
    if (role === '1') return next('/merchant/home')
    if (role === '2') return next('/user/home')
    return next('/login')
  }

  next()
})




export default router

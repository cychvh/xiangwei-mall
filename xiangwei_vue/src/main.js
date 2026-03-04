// main.js
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'

const app = createApp(App)

// 注册 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 使用 Pinia、路由和 Element Plus
app.use(createPinia())
app.use(router)
app.use(ElementPlus)

// 全局屏蔽 ResizeObserver 警告
const resizeObserverErr = /ResizeObserver loop limit exceeded|ResizeObserver loop completed with undelivered notifications/
const originalConsoleError = console.error
console.error = (...args) => {
  if (args[0] && resizeObserverErr.test(args[0].toString())) return
  originalConsoleError(...args)
}

app.mount('#app')

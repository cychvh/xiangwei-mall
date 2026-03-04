import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { ElMessage } from 'element-plus'

export const useCartStore = defineStore('cart', () => {
  const cartItems = ref([])

  // 添加商品到购物车
  const addToCart = (product) => {
    const existingItem = cartItems.value.find(item => item.id === product.id)
    if (existingItem) {
      existingItem.quantity++
    } else {
      cartItems.value.push({ ...product, quantity: 1 })
    }
    ElMessage.success(`已将 ${product.name} 加入购物车`)
  }

  // 从购物车移除
  const removeFromCart = (productId) => {
    cartItems.value = cartItems.value.filter(item => item.id !== productId)
  }

  // 清空购物车
  const clearCart = () => {
    cartItems.value = []
  }

  // 计算总价
  const totalPrice = computed(() => {
    return cartItems.value.reduce((total, item) => total + item.price * item.quantity, 0).toFixed(2)
  })

  // 计算总数量
  const totalItems = computed(() => {
    return cartItems.value.reduce((total, item) => total + item.quantity, 0)
  })

  return { cartItems, addToCart, removeFromCart, clearCart, totalPrice, totalItems }
})
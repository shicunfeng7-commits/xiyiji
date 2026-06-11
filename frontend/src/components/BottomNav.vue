<template>
  <van-tabbar v-model="active" :fixed="true" :border="false">
    <van-tabbar-item v-for="item in navItems" :key="item.path" :icon="item.icon" :to="item.path">
      {{ item.label }}
    </van-tabbar-item>
  </van-tabbar>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { getUserInfo } from '../utils/auth'

const currentPath = window.location.hash.replace('#', '')

const isEmployee = computed(() => {
  const userInfo = getUserInfo()
  return userInfo?.role === 'employee'
})

const navItems = computed(() => {
  if (isEmployee.value) {
    return [
      { path: '/user/home', label: '首页', icon: 'home-o' },
      { path: '/user/orders', label: '订单', icon: 'shopping-cart-o' },
      { path: '/employee/available', label: '抢单', icon: 'lightning-o' },
      { path: '/user/profile', label: '我的', icon: 'user-o' },
    ]
  }
  return [
    { path: '/user/home', label: '首页', icon: 'home-o' },
    { path: '/user/orders', label: '订单', icon: 'shopping-cart-o' },
    { path: '/user/profile', label: '我的', icon: 'user-o' },
  ]
})

const active = computed(() => {
  const index = navItems.value.findIndex(item => currentPath.startsWith(item.path))
  return index >= 0 ? index : 0
})
</script>

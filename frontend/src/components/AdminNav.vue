<template>
  <div class="admin-nav">
    <van-tabbar v-model="active" :fixed="true" :border="false">
      <van-tabbar-item v-for="item in navItems" :key="item.path" :icon="item.icon" :to="item.path">
        {{ item.label }}
      </van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const navItems = [
  { path: '/admin/dashboard', label: '看板', icon: 'bar-chart-o' },
  { path: '/admin/orders', label: '订单', icon: 'shopping-cart-o' },
  { path: '/admin/employees', label: '员工', icon: 'user-o' },
  { path: '/admin/employee/audit', label: '审核', icon: 'check-circle-o' },
]

const active = computed(() => {
  let bestIndex = -1
  let bestLen = 0
  navItems.forEach((item, i) => {
    if (route.path.startsWith(item.path) && item.path.length > bestLen) {
      bestIndex = i
      bestLen = item.path.length
    }
  })
  return bestIndex >= 0 ? bestIndex : 0
})
</script>

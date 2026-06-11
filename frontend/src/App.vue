<template>
  <div class="app-container">
    <div class="app-header" v-if="showHeader">
      <div class="header-left" @click="goBack" v-if="!isHome">
        <van-icon name="arrow-left" size="20" color="#1D1D1F" />
      </div>
      <div class="header-title">{{ headerTitle }}</div>
      <div class="header-right">
        <span v-if="isAdmin" class="logout-btn" @click="handleLogout">退出</span>
      </div>
    </div>
    <router-view v-slot="{ Component, route: r }">
      <transition :name="transitionName" mode="out-in">
        <component :is="Component" :key="r.path" />
      </transition>
    </router-view>
    <van-tabbar v-model="active" v-if="showTabbar" active-color="#2B95FF" inactive-color="#86868B" @change="onTabChange" :border="false">
      <van-tabbar-item v-for="tab in tabs" :key="tab.route">
        <span>{{ tab.name }}</span>
        <template #icon>
          <van-icon :name="tab.icon" />
        </template>
      </van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getToken, getUserInfo, removeAuth } from './utils/auth'

const route = useRoute()
const router = useRouter()

const active = ref(0)

// 页面过渡动画方向
const transitionName = ref('slide-left')
let lastTabIndex = 0
watch(active, (val, old) => {
  transitionName.value = val > old ? 'slide-left' : 'slide-right'
})
watch(() => route.path, () => {
  const currentTabs = tabs.value
  const idx = currentTabs.findIndex(tab => route.path.startsWith(tab.route))
  if (idx >= 0) {
    transitionName.value = idx > lastTabIndex ? 'slide-left' : 'slide-right'
    lastTabIndex = idx
  }
})

interface TabConfig {
  name: string
  icon: string
  route: string
}

const tabsConfig: Record<string, TabConfig[]> = {
  guest: [
    { name: '登录', icon: 'contact-o', route: '/login' }
  ],
  user: [
    { name: '首页', icon: 'home-o', route: '/user/home' },
    { name: '订单', icon: 'orders-o', route: '/user/orders' },
    { name: '我的', icon: 'contact-o', route: '/user/profile' }
  ],
  employee: [
    { name: '抢单', icon: 'logistics-o', route: '/employee/available' },
    { name: '订单', icon: 'orders-o', route: '/employee/my-orders' },
    { name: '我的', icon: 'contact-o', route: '/employee/profile' },
  ],
  admin: [
    { name: '看板', icon: 'bar-chart-o', route: '/admin/dashboard' },
    { name: '订单', icon: 'orders-o', route: '/admin/orders' },
    { name: '员工', icon: 'friends-o', route: '/admin/employees' },
    { name: '审核', icon: 'records-o', route: '/admin/employee/audit' },
    { name: '配置', icon: 'setting-o', route: '/admin/time-config' }
  ]
}

const userRole = computed(() => {
  // 依赖 route.path 使 computed 在导航时重新计算
  // localStorage 读取不是响应式的，需要手动建立响应式依赖
  void route.path
  const token = getToken()
  if (!token) return 'guest'
  const userInfo = getUserInfo() as Record<string, unknown> | null
  if (!userInfo) return 'guest'
  const role = userInfo.role as number | string | undefined
  if (role === 0 || role === 'user') return 'user'
  if (role === 1 || role === 'employee') return 'employee'
  if (role === 2 || role === 'admin') return 'admin'
  return 'guest'
})

const tabs = computed(() => {
  return tabsConfig[userRole.value] || tabsConfig.guest
})

// 监听路由变化，同步 active 索引（最长前缀匹配，避免 /admin/employees 误匹配 /admin/employee/audit）
watch(() => route.path, (path) => {
  const currentTabs = tabs.value
  let bestIndex = -1
  let bestLen = 0
  currentTabs.forEach((tab, i) => {
    if (path.startsWith(tab.route) && tab.route.length > bestLen) {
      bestIndex = i
      bestLen = tab.route.length
    }
  })
  if (bestIndex >= 0) {
    active.value = bestIndex
  }
}, { immediate: true })

function onTabChange(index: number) {
  const tab = tabs.value[index]
  if (tab) {
    router.push(tab.route)
  }
}

const showTabbar = computed(() => {
  return route.path !== '/login' &&
         route.path !== '/admin/login' &&
         !route.path.includes('/order/create') &&
         !route.path.includes('/order/pay') &&
         !route.path.includes('/order/detail')
})

const showHeader = computed(() => {
  return route.path !== '/login' &&
         route.path !== '/admin/dashboard' &&
         !route.path.includes('/home')
})

const isHome = computed(() => route.path === '/user/home')
const isAdmin = computed(() => userRole.value === 'admin')

function handleLogout() {
  removeAuth()
  router.push('/login')
}

const headerTitle = computed(() => {
  const map: Record<string, string> = {
    '/user/orders': '我的订单',
    '/user/order/create': '预约清洗',
    '/user/order/pay': '支付',
    '/user/order/detail': '订单详情',
    '/user/profile': '我的',
    '/admin/login': '管理员登录',
    '/admin/dashboard': '数据看板',
    '/admin/orders': '订单管理',
    '/admin/employees': '员工管理',
    '/admin/employee/audit': '员工审核',
    '/admin/time-config': '时间配置',
    '/employee/available': '待抢订单',
    '/employee/my-orders': '我的订单',
    '/employee/profile': '我的',
    '/employee/order-history': '完成订单',
  }
  let bestTitle = 'WashPro'
  let bestLen = 0
  for (const [path, title] of Object.entries(map)) {
    if (route.path.startsWith(path) && path.length > bestLen) {
      bestTitle = title
      bestLen = path.length
    }
  }
  return bestTitle
})

function goBack() {
  router.back()
}
</script>

<style>
.app-container {
  max-width: 430px;
  margin: 0 auto;
  min-height: 100vh;
  background-color: #F5F5F7;
  position: relative;
  padding-bottom: 64px;
}

.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: rgba(255,255,255,0.9);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(0,0,0,0.05);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-left, .header-right {
  width: 40px;
  display: flex;
  align-items: center;
}

.header-title {
  font-size: 17px;
  font-weight: 600;
  color: #1D1D1F;
  letter-spacing: -0.02em;
}

.logout-btn {
  font-size: 14px;
  color: #FF3B30;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
  transition: background 0.2s;
}
.logout-btn:active {
  background: rgba(255, 59, 48, 0.1);
}

/* ===== Apple 风格页面过渡动画 ===== */
.slide-left-enter-active,
.slide-left-leave-active,
.slide-right-enter-active,
.slide-right-leave-active {
  transition: all 0.35s cubic-bezier(0.25, 0.1, 0.25, 1);
}
.slide-left-enter-from {
  opacity: 0;
  transform: translateX(40px);
}
.slide-left-leave-to {
  opacity: 0;
  transform: translateX(-30px);
}
.slide-right-enter-from {
  opacity: 0;
  transform: translateX(-40px);
}
.slide-right-leave-to {
  opacity: 0;
  transform: translateX(30px);
}

/* 页面内容入场微动画 */
.order-card, .stat-card, .feature-card, .order-card, .photo-group {
  animation: cardIn 0.45s cubic-bezier(0.25, 0.1, 0.25, 1) both;
}
.order-card:nth-child(2) { animation-delay: 0.05s; }
.order-card:nth-child(3) { animation-delay: 0.1s; }
.order-card:nth-child(4) { animation-delay: 0.15s; }
.order-card:nth-child(5) { animation-delay: 0.2s; }

@keyframes cardIn {
  from { opacity: 0; transform: translateY(20px); }
  to   { opacity: 1; transform: translateY(0); }
}

/* Tabbar 标签切换微动 */
.van-tabbar-item {
  transition: transform 0.2s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.van-tabbar-item--active {
  transition: transform 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);
}
</style>
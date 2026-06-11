<template>
  <div class="app-container">
    <div class="app-header" v-if="showHeader">
      <div class="header-left" @click="goBack" v-if="!isHome">
        <van-icon name="arrow-left" size="20" color="#1D1D1F" />
      </div>
      <div class="header-title">{{ headerTitle }}</div>
      <div class="header-right">
        <span v-if="isAdmin" class="logout-chip" @click="handleLogout">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
          退出
        </span>
      </div>
    </div>
    <router-view v-slot="{ Component }">
      <transition name="page-slide" mode="out-in">
        <component :is="Component" />
      </transition>
    </router-view>
    <van-tabbar v-model="active" v-if="showTabbar" active-color="#007AFF" inactive-color="#86868B" @change="onTabChange" :border="false">
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
import { showToast as vantToast } from 'vant'

const route = useRoute()
const router = useRouter()
const active = ref(0)

interface TabConfig { name: string; icon: string; route: string }

const tabsConfig: Record<string, TabConfig[]> = {
  guest: [{ name: '登录', icon: 'contact-o', route: '/login' }],
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

const tabs = computed(() => tabsConfig[userRole.value] || tabsConfig.guest)

watch(() => route.path, (path) => {
  const currentTabs = tabs.value
  let bestIndex = -1, bestLen = 0
  currentTabs.forEach((tab, i) => {
    if (path.startsWith(tab.route) && tab.route.length > bestLen) { bestIndex = i; bestLen = tab.route.length }
  })
  if (bestIndex >= 0) active.value = bestIndex
}, { immediate: true })

function onTabChange(index: number) {
  const tab = tabs.value[index]
  if (tab) router.push(tab.route)
}

const showTabbar = computed(() => {
  return route.path !== '/login' && route.path !== '/admin/login' &&
    !route.path.includes('/order/create') && !route.path.includes('/order/pay') && !route.path.includes('/order/detail')
})
const showHeader = computed(() => {
  return route.path !== '/login' && route.path !== '/admin/dashboard' && !route.path.includes('/home')
})
const isHome = computed(() => route.path === '/user/home')
const isAdmin = computed(() => userRole.value === 'admin')

function handleLogout() { removeAuth(); router.push('/login') }

const headerTitle = computed(() => {
  const map: Record<string, string> = {
    '/user/orders': '我的订单', '/user/order/create': '预约清洗', '/user/order/pay': '支付',
    '/user/order/detail': '订单详情', '/user/profile': '我的',
    '/admin/login': '管理员登录', '/admin/dashboard': '数据看板', '/admin/orders': '订单管理',
    '/admin/employees': '员工管理', '/admin/employee/audit': '员工审核', '/admin/time-config': '时间配置',
    '/employee/available': '待抢订单', '/employee/my-orders': '我的订单', '/employee/profile': '我的',
    '/employee/order-history': '完成订单',
  }
  let bestTitle = 'WashPro', bestLen = 0
  for (const [path, title] of Object.entries(map)) {
    if (route.path.startsWith(path) && path.length > bestLen) { bestTitle = title; bestLen = path.length }
  }
  return bestTitle
})

function goBack() { router.back() }
</script>

<style>
.app-container {
  max-width: 100%; width: 100%; margin: 0 auto;
  min-height: 100vh; min-height: 100dvh;
  background: var(--color-bg-deep);
  position: relative; padding-bottom: 64px;
  overflow-x: hidden;
}
@media (min-width: 431px) { .app-container { max-width: 430px; } }

.app-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 12px 16px;
  background: var(--glass-bg); backdrop-filter: blur(var(--glass-blur)); -webkit-backdrop-filter: blur(var(--glass-blur));
  border-bottom: 1px solid var(--glass-border);
  position: sticky; top: 0; z-index: 100;
}
.header-left { width: 36px; display: flex; align-items: center; }
.header-right { display: flex; align-items: center; justify-content: flex-end; min-width: 60px; }
.header-title { font-size: 17px; font-weight: 700; color: var(--color-text-primary); letter-spacing: -0.02em; flex: 1; text-align: center; }

.logout-chip {
  display: inline-flex; align-items: center; gap: 5px;
  font-size: 11px; font-weight: 500; color: var(--color-text-secondary);
  padding: 6px 12px; border-radius: 16px;
  background: var(--glass-bg); backdrop-filter: blur(12px); -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(0,0,0,0.06); box-shadow: var(--shadow-sm);
  cursor: pointer; transition: all var(--transition-fast); white-space: nowrap; flex-shrink: 0;
}
.logout-chip:active { background: rgba(255,59,48,0.06); color: var(--color-danger); }

/* ===== Page Transitions ===== */
.page-slide-enter-active, .page-slide-leave-active { transition: all 0.3s cubic-bezier(0.25, 0.1, 0.25, 1); }
.page-slide-enter-from { opacity: 0; transform: translateX(24px); }
.page-slide-leave-to { opacity: 0; transform: translateX(-24px); }

/* ===== Tabbar ===== */
:deep(.van-tabbar) {
  background: var(--glass-bg) !important; backdrop-filter: blur(var(--glass-blur)) !important;
  -webkit-backdrop-filter: blur(var(--glass-blur)) !important; border-top: 1px solid var(--glass-border) !important;
}
:deep(.van-tabbar-item) {
  color: var(--color-text-tertiary) !important;
  transition: transform 0.25s cubic-bezier(0.34, 1.56, 0.64, 1) !important;
}
:deep(.van-tabbar-item--active) {
  color: var(--color-accent) !important;
  transform: scale(1.08);
}

/* ===== Toast ===== */
:deep(.van-toast) { bottom: 80px !important; top: auto !important; border-radius: 12px !important; }
</style>

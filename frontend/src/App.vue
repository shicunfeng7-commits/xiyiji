<template>
  <div class="app-container">
    <div class="app-header" v-if="showHeader">
      <div class="header-left" @click="goBack" v-if="!isHome">
        <van-icon name="arrow-left" size="20" color="#1D1D1F" />
      </div>
      <div class="header-title">{{ headerTitle }}</div>
      <div class="header-right"></div>
    </div>
    <router-view />
    <div class="app-tabbar" v-if="showTabbar">
      <div
        v-for="tab in tabs"
        :key="tab.path"
        class="tab-item"
        :class="{ active: currentTab === tab.path }"
        @click="switchTab(tab.path)"
      >
        <van-icon :name="tab.icon" size="22" />
        <span class="tab-label">{{ tab.label }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const tabs = [
  { path: '/user/home', label: '首页', icon: 'home-o' },
  { path: '/user/orders', label: '订单', icon: 'bars-o' },
  { path: '/admin/login', label: '管理', icon: 'manager-o' },
  { path: '/employee/login', label: '员工', icon: 'contact-o' },
]

const showTabbar = computed(() => {
  return !route.path.includes('/order/create') && 
         !route.path.includes('/order/pay') &&
         !route.path.includes('/order/detail') &&
         !route.path.includes('/login') === false
})

const showHeader = computed(() => {
  return !route.path.includes('/home')
})

const isHome = computed(() => route.path === '/user/home')

const headerTitle = computed(() => {
  const map: Record<string, string> = {
    '/user/orders': '我的订单',
    '/user/order/create': '预约清洗',
    '/user/order/pay': '支付',
    '/user/order/detail': '订单详情',
    '/admin/login': '管理员登录',
    '/admin/orders': '订单管理',
    '/admin/employees': '员工管理',
    '/employee/login': '员工登录',
    '/employee/available': '待抢订单',
    '/employee/my-orders': '我的订单',
  }
  for (const [path, title] of Object.entries(map)) {
    if (route.path.startsWith(path)) return title
  }
  return 'WashPro'
})

const currentTab = computed(() => {
  for (const tab of tabs) {
    if (route.path.startsWith(tab.path.replace('/home', '').replace('/orders', '').replace('/login', ''))) {
      return tab.path
    }
  }
  return tabs[0].path
})

function switchTab(path: string) {
  router.push(path)
}

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

.app-tabbar {
  position: fixed;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  max-width: 430px;
  width: 100%;
  display: flex;
  justify-content: space-around;
  align-items: center;
  background: rgba(255,255,255,0.92);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  padding: 8px 0;
  padding-bottom: max(8px, env(safe-area-inset-bottom));
  border-top: 1px solid rgba(0,0,0,0.05);
  z-index: 100;
}

.tab-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  padding: 4px 16px;
  cursor: pointer;
  color: #86868B;
  transition: color 0.2s;
}

.tab-item.active {
  color: #2B95FF;
}

.tab-label {
  font-size: 10px;
  font-weight: 500;
  letter-spacing: 0.01em;
}
</style>
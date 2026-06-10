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
import { getToken, getUserInfo } from './utils/auth'

const route = useRoute()
const router = useRouter()

const active = ref(0)

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
    { name: '首页', icon: 'home-o', route: '/user/home' },
    { name: '订单', icon: 'orders-o', route: '/user/orders' },
    { name: '员工', icon: 'logistics-o', route: '/employee/available' },
    { name: '我的', icon: 'contact-o', route: '/user/profile' }
  ],
  admin: [
    { name: '订单', icon: 'orders-o', route: '/admin/orders' },
    { name: '员工', icon: 'friends-o', route: '/admin/employees' },
    { name: '审核', icon: 'records-o', route: '/admin/employee/audit' },
    { name: '配置', icon: 'setting-o', route: '/admin/time-config' }
  ]
}

const userRole = computed(() => {
  const token = getToken()
  if (!token) return 'guest'
  const userInfo = getUserInfo() as Record<string, unknown> | null
  if (!userInfo) return 'guest'
  const role = userInfo.role as number | undefined
  if (role === 0) return 'user'
  if (role === 1) return 'employee'
  if (role === 2) return 'admin'
  return 'guest'
})

const tabs = computed(() => {
  return tabsConfig[userRole.value] || tabsConfig.guest
})

// 监听路由变化，同步 active 索引
watch(() => route.path, (path) => {
  const currentTabs = tabs.value
  const index = currentTabs.findIndex(tab => path.startsWith(tab.route))
  if (index >= 0) {
    active.value = index
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
         route.path !== '/employee/login' &&
         !route.path.includes('/order/create') && 
         !route.path.includes('/order/pay') &&
         !route.path.includes('/order/detail')
})

const showHeader = computed(() => {
  return route.path !== '/login' && !route.path.includes('/home')
})

const isHome = computed(() => route.path === '/user/home')

const headerTitle = computed(() => {
  const map: Record<string, string> = {
    '/user/orders': '我的订单',
    '/user/order/create': '预约清洗',
    '/user/order/pay': '支付',
    '/user/order/detail': '订单详情',
    '/user/profile': '我的',
    '/admin/login': '管理员登录',
    '/admin/orders': '订单管理',
    '/admin/employees': '员工管理',
    '/admin/employee/audit': '员工审核',
    '/admin/time-config': '时间配置',
    '/employee/login': '员工登录',
    '/employee/available': '待抢订单',
    '/employee/my-orders': '我的订单',
  }
  for (const [path, title] of Object.entries(map)) {
    if (route.path.startsWith(path)) return title
  }
  return 'WashPro'
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
</style>
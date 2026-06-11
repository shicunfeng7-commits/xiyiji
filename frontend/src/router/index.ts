import { createRouter, createWebHashHistory } from 'vue-router'
import { getToken, getUserInfo } from '../utils/auth'

const routes = [
  { path: '/', redirect: '/login' },

  // 登录/角色选择
  { path: '/login', name: 'LoginPage', component: () => import('../views/LoginPage.vue') },

  // 用户端
  { path: '/user/home', name: 'UserHome', component: () => import('../views/user/UserHome.vue') },
  { path: '/user/orders', name: 'UserOrders', component: () => import('../views/user/UserOrders.vue') },
  { path: '/user/order/create', name: 'UserOrderCreate', component: () => import('../views/user/UserOrderCreate.vue') },
  { path: '/user/order/pay', name: 'UserOrderPay', component: () => import('../views/user/UserOrderPay.vue') },
  { path: '/user/order/detail', name: 'UserOrderDetail', component: () => import('../views/user/UserOrderDetail.vue') },
  { path: '/user/profile', name: 'UserProfile', component: () => import('../views/user/UserProfile.vue') },

  // 管理端
  { path: '/admin/login', name: 'AdminLogin', component: () => import('../views/admin/AdminLogin.vue') },
  { path: '/admin/dashboard', name: 'AdminDashboard', component: () => import('../views/admin/AdminDashboard.vue') },
  { path: '/admin/orders', name: 'AdminOrders', component: () => import('../views/admin/AdminOrders.vue') },
  { path: '/admin/employees', name: 'AdminEmployees', component: () => import('../views/admin/AdminEmployees.vue') },
  { path: '/admin/employee/audit', name: 'AdminEmployeeAudit', component: () => import('../views/admin/AdminEmployeeAudit.vue') },
  { path: '/admin/time-config', name: 'AdminTimeConfig', component: () => import('../views/admin/AdminTimeConfig.vue') },

  // 员工端
  { path: '/employee/available', name: 'EmployeeAvailable', component: () => import('../views/employee/EmployeeAvailable.vue') },
  { path: '/employee/my-orders', name: 'EmployeeMyOrders', component: () => import('../views/employee/EmployeeMyOrders.vue') },
  { path: '/employee/profile', name: 'EmployeeProfile', component: () => import('../views/employee/EmployeeProfile.vue') },
  { path: '/employee/order-history', name: 'EmployeeOrderHistory', component: () => import('../views/employee/EmployeeOrderHistory.vue') },
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
})

function getRole(): string | null {
  const userInfo = getUserInfo() as Record<string, unknown> | null
  if (!userInfo) return null
  const role = userInfo.role as number | string | undefined
  if (role === 0 || role === 'user') return 'user'
  if (role === 1 || role === 'employee') return 'employee'
  if (role === 2 || role === 'admin') return 'admin'
  return null
}

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = getToken()
  const role = getRole()

  // 登录页和根路径直接放行
  if (to.path === '/login' || to.path === '/') {
    next()
    return
  }

  // 管理端登录页不需要验证角色
  if (to.path === '/admin/login') {
    next()
    return
  }

  // 未登录，跳转到 /login
  if (!token) {
    next('/login')
    return
  }

  // 管理员只能访问管理端页面
  if (role === 'admin' && !to.path.startsWith('/admin/')) {
    next('/admin/dashboard')
    return
  }

  // 非管理员不能访问管理端页面
  if (role !== 'admin' && to.path.startsWith('/admin/')) {
    next('/user/home')
    return
  }

  // 员工只能访问员工端页面和用户端页面（员工同时也是用户）
  // 普通用户不能访问员工端页面
  if (role === 'user' && to.path.startsWith('/employee/')) {
    next('/user/home')
    return
  }

  next()
})

export default router
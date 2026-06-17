import { createRouter, createWebHashHistory } from 'vue-router'
import { getToken, getRole } from '../utils/auth'

const routes = [
  { path: '/', redirect: '/user/home' },

  // 登录已整合到个人中心
  { path: '/login', redirect: '/user/home' },

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

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = getToken()
  const role = getRole()

  // 管理端登录页不需要验证
  if (to.path === '/admin/login') {
    next()
    return
  }

  // 游客可访问的用户端页面
  const guestAllowed = [
    '/user/home', '/user/orders', '/user/order/create', '/user/profile',
    '/user/order/pay', '/user/order/detail'
  ]
  if (!token && guestAllowed.some(p => to.path.startsWith(p))) {
    next()
    return
  }

  // 未登录，跳转到首页
  if (!token) {
    next('/user/home')
    return
  }

  // 管理员只能访问管理端页面（不限制访问用户端页面）
  // if (role === 'admin' && !to.path.startsWith('/admin/')) {
  //   next('/admin/dashboard')
  //   return
  // }

  // 非管理员不能访问管理端页面
  if (role !== 'admin' && to.path.startsWith('/admin/')) {
    next('/user/home')
    return
  }

  // 员工端页面只有员工和管理员能访问
  if (to.path.startsWith('/employee/') && role !== 'employee' && role !== 'admin') {
    next('/user/home')
    return
  }

  // 员工访问"我的"页面时自动跳转到员工个人中心
  if (role === 'employee' && to.path === '/user/profile') {
    next('/employee/profile')
    return
  }

  next()
})

export default router

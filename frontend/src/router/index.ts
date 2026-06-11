import { createRouter, createWebHashHistory } from 'vue-router'
import { getToken } from '../utils/auth'

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
  { path: '/employee/login', name: 'EmployeeLogin', component: () => import('../views/employee/EmployeeLogin.vue') },
  { path: '/employee/available', name: 'EmployeeAvailable', component: () => import('../views/employee/EmployeeAvailable.vue') },
  { path: '/employee/my-orders', name: 'EmployeeMyOrders', component: () => import('../views/employee/EmployeeMyOrders.vue') },
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
})

// 路由守卫：未登录跳转到 /login
router.beforeEach((to, from, next) => {
  const token = getToken()

  // 登录页和根路径直接放行
  if (to.path === '/login' || to.path === '/') {
    next()
    return
  }

  // 管理端登录页、员工端登录页不需要登录
  if (to.path === '/admin/login' || to.path === '/employee/login') {
    next()
    return
  }

  // 未登录时，访问任何需要登录的页面都跳转到 /login
  if (!token) {
    next('/login')
    return
  }

  next()
})

export default router
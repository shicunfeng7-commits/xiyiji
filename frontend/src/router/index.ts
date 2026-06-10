import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/user/home' },
  
  // 用户端
  { path: '/user/home', name: 'UserHome', component: () => import('../views/user/UserHome.vue') },
  { path: '/user/orders', name: 'UserOrders', component: () => import('../views/user/UserOrders.vue') },
  { path: '/user/order/create', name: 'UserOrderCreate', component: () => import('../views/user/UserOrderCreate.vue') },
  { path: '/user/order/pay', name: 'UserOrderPay', component: () => import('../views/user/UserOrderPay.vue') },
  { path: '/user/order/detail', name: 'UserOrderDetail', component: () => import('../views/user/UserOrderDetail.vue') },
  
  // 管理端
  { path: '/admin/login', name: 'AdminLogin', component: () => import('../views/admin/AdminLogin.vue') },
  { path: '/admin/orders', name: 'AdminOrders', component: () => import('../views/admin/AdminOrders.vue') },
  { path: '/admin/employees', name: 'AdminEmployees', component: () => import('../views/admin/AdminEmployees.vue') },
  
  // 员工端
  { path: '/employee/login', name: 'EmployeeLogin', component: () => import('../views/employee/EmployeeLogin.vue') },
  { path: '/employee/available', name: 'EmployeeAvailable', component: () => import('../views/employee/EmployeeAvailable.vue') },
  { path: '/employee/my-orders', name: 'EmployeeMyOrders', component: () => import('../views/employee/EmployeeMyOrders.vue') },
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
})

export default router
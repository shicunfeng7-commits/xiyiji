import axios from 'axios'
import { getToken, removeAuth } from './auth'
import router from '../router'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  timeout: 15000,
})

// 请求拦截器：自动携带 token
request.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error),
)

// 响应拦截器：401 处理
request.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      // /api/user/profile 是 token 验证接口，401 时不自动清除 token
      // 让调用方（checkAuth）自己决定如何处理
      const url = error.config?.url || ''
      if (url.includes('/api/user/profile')) {
        return Promise.reject(error)
      }
      
      // 其他接口 401，清除 token 并跳转
      const currentPath = window.location.hash.replace('#', '') || ''
      const guestPages = ['/user/home', '/user/orders', '/user/order/create', '/user/order/pay', '/user/order/detail', '/user/profile']
      const isGuestPage = guestPages.some(p => currentPath.startsWith(p))
      
      if (!isGuestPage) {
        removeAuth()
        router.push('/user/profile')
      }
    }
    return Promise.reject(error)
  },
)

export function get<T = unknown>(url: string, params?: Record<string, unknown>) {
  return request.get<T>(url, { params })
}

export function post<T = unknown>(url: string, data?: unknown) {
  return request.post<T>(url, data)
}

export function put<T = unknown>(url: string, data?: unknown) {
  return request.put<T>(url, data)
}

export function del<T = unknown>(url: string) {
  return request.delete<T>(url)
}

export default request
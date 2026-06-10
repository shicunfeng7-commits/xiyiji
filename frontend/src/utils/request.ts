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

// 响应拦截器：401 自动登出并跳转
request.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      removeAuth()
      router.push('/login')
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
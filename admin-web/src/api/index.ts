import axios from 'axios'
import { ElMessage } from 'element-plus'

const api = axios.create({
  baseURL: '/api',
  timeout: 15000
})

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

api.interceptors.response.use(
  (response) => {
    const data = response.data
    if (data.code !== 200 && data.code !== 0) {
      ElMessage.error(data.message || '请求失败')
      return Promise.reject(new Error(data.message || '请求失败'))
    }
    return data
  },
  (error) => {
    if (error.response) {
      const { status } = error.response
      if (status === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        window.location.href = '/login'
      } else {
        ElMessage.error(error.response.data?.message || '服务器错误')
      }
    } else {
      ElMessage.error('网络错误，请检查网络连接')
    }
    return Promise.reject(error)
  }
)

// Dashboard API
export const getDashboard = () => api.get('/admin/dashboard')

// Instruments API
export const getInstruments = (params?: any) => api.get('/admin/instruments', { params })
export const getInstrument = (id: number) => api.get(`/admin/instruments/${id}`)
export const createInstrument = (data: any) => api.post('/admin/instruments', data)
export const updateInstrument = (id: number, data: any) => api.put(`/admin/instruments/${id}`, data)
export const deleteInstrument = (id: number) => api.delete(`/admin/instruments/${id}`)

// Scan API
export const scanCheckout = (data: { barcode: string; damaged: boolean }) =>
  api.post('/admin/scan/checkout', data)
export const scanCheckin = (data: { barcode: string; damaged: boolean }) =>
  api.post('/admin/scan/checkin', data)

// Auth API
export const loginApi = (data: { phone: string; password: string }) =>
  api.post('/admin/auth/login', data)

export default api
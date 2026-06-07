import { defineStore } from 'pinia'
import { ref } from 'vue'
import { loginApi } from '../api'
import router from '../router'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<any>(null)

  async function login(phone: string, password: string) {
    const res: any = await loginApi({ phone, password })
    const token = res.data?.token || res.token
    const userData = res.data?.user || res.data
    if (token) {
      localStorage.setItem('token', token)
    }
    if (userData) {
      localStorage.setItem('user', JSON.stringify(userData))
      user.value = userData
    }
    return res
  }

  function logout() {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    user.value = null
    router.push('/login')
  }

  return { user, login, logout }
})
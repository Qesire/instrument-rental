import { defineStore } from 'pinia'
import { ref } from 'vue'
import { loginApi } from '../api'
import router from '../router'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<any>(null)

  async function login(phone: string, password: string) {
    const res: any = await loginApi({ phone, password })
    const loginData = res.data
    if (loginData?.token) {
      localStorage.setItem('token', loginData.token)
      user.value = loginData
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
import { defineStore } from 'pinia'
import authApi from '../api/authApi'
import { ref, computed } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || null)
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))

  const isAuthenticated = computed(() => !!token.value)

  const dashboardRoute = computed(() => {
    switch (user.value?.role) {
      case 'AdminMax': return '/admin-max'
      case 'Admin': return '/consultor'
      case 'Cliente': return '/cliente'
      default: return '/login'
    }
  })

  async function login(email, senha) {
    const response = await authApi.login({ email, senha })
    const data = response.data
    token.value = data.token
    user.value = {
      id: data.userId,
      nome: data.nome,
      role: data.role,
      empresaId: data.empresaId
    }
    localStorage.setItem('token', data.token)
    localStorage.setItem('user', JSON.stringify(user.value))
    return data
  }

  function logout() {
    token.value = null
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  return { token, user, isAuthenticated, dashboardRoute, login, logout }
})
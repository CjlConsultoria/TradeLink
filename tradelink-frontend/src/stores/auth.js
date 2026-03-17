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
      case 'Admin':
        if (user.value?.precisaEscolherPlano) return '/consultor/faturas'
        return '/consultor'
      case 'Cliente':
        if (user.value?.marketplaceBloqueado) return '/cliente/pos-exclusao'
        if (user.value?.clienteExcluido && !user.value?.autoGestaoAtiva) return '/cliente/pos-exclusao'
        if (user.value?.autoCadastro && user.value?.precisaEscolherPlano) return '/cliente/pos-exclusao'
        return '/cliente'
      default: return '/login'
    }
  })

  function saveAuthData(data) {
    token.value = data.token
    user.value = {
      id: data.userId,
      nome: data.nome,
      role: data.role,
      empresaId: data.empresaId,
      clienteExcluido: data.clienteExcluido || false,
      autoGestaoAtiva: data.autoGestaoAtiva || false,
      trialAtivo: data.trialAtivo || false,
      trialFim: data.trialFim || null,
      precisaEscolherPlano: data.precisaEscolherPlano || false,
      autoCadastro: data.autoCadastro || false,
      marketplaceBloqueado: data.marketplaceBloqueado || false,
      origemVinculo: data.origemVinculo || null
    }
    localStorage.setItem('token', data.token)
    localStorage.setItem('user', JSON.stringify(user.value))
    if (data.refreshToken) {
      localStorage.setItem('refreshToken', data.refreshToken)
    }
  }

  async function login(email, senha) {
    const response = await authApi.login({ email, senha })
    const data = response.data
    // Se requer 2FA, não salva token (será salvo após verify-otp)
    if (data.requires2FA) {
      return data
    }
    saveAuthData(data)
    return data
  }

  async function verifyOtp(userId, code) {
    const response = await authApi.verifyOtp({ userId, code })
    const data = response.data
    saveAuthData(data)
    return data
  }

  function logout() {
    token.value = null
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('user')
  }

  return { token, user, isAuthenticated, dashboardRoute, login, verifyOtp, logout }
})

import axios from 'axios'

// API: em produção no Render o front está em tradelink-grun e o back em tradelink-1-ed48
// No app mobile (Capacitor) sempre usa a API de produção.
function getBaseURL() {
  if (import.meta.env.VITE_API_URL) {
    return `${import.meta.env.VITE_API_URL.replace(/\/$/, '')}/api`
  }
  if (typeof window !== 'undefined' && window.location.hostname === 'tradelink-grun.onrender.com') {
    return 'https://tradelink-1-ed48.onrender.com/api'
  }
  // App iOS/Android (Capacitor): usar API de produção
  if (typeof window !== 'undefined' && window.Capacitor?.isNativePlatform?.()) {
    return 'https://tradelink-1-ed48.onrender.com/api'
  }
  return '/api'
}

const baseURL = getBaseURL()

const api = axios.create({
  baseURL,
  headers: { 'Content-Type': 'application/json' }
})

// Flag para evitar múltiplas tentativas simultâneas de refresh
let isRefreshing = false
let failedQueue = []

function processQueue(error, token = null) {
  failedQueue.forEach(prom => {
    if (error) {
      prom.reject(error)
    } else {
      prom.resolve(token)
    }
  })
  failedQueue = []
}

api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  response => response,
  async error => {
    const originalRequest = error.config

    // Tentar refresh token em caso de 401 (token expirado)
    if (error.response && error.response.status === 401 && !originalRequest._retry) {
      // Não tentar refresh em rotas de auth (login, verify-otp, refresh, etc.)
      const url = originalRequest.url || ''
      if (url.includes('/auth/')) {
        localStorage.removeItem('token')
        localStorage.removeItem('refreshToken')
        localStorage.removeItem('user')
        window.location.href = '/login'
        return Promise.reject(error)
      }

      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject })
        }).then(token => {
          originalRequest.headers.Authorization = `Bearer ${token}`
          return api(originalRequest)
        }).catch(err => {
          return Promise.reject(err)
        })
      }

      originalRequest._retry = true
      isRefreshing = true

      const refreshToken = localStorage.getItem('refreshToken')
      if (!refreshToken) {
        isRefreshing = false
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        window.location.href = '/login'
        return Promise.reject(error)
      }

      try {
        const response = await axios.post(`${baseURL}/auth/refresh`, { refreshToken })
        const data = response.data

        if (data.token) {
          localStorage.setItem('token', data.token)
          if (data.refreshToken) {
            localStorage.setItem('refreshToken', data.refreshToken)
          }
          api.defaults.headers.Authorization = `Bearer ${data.token}`
          originalRequest.headers.Authorization = `Bearer ${data.token}`
          processQueue(null, data.token)
          return api(originalRequest)
        } else {
          processQueue(new Error('Refresh failed'), null)
          localStorage.removeItem('token')
          localStorage.removeItem('refreshToken')
          localStorage.removeItem('user')
          window.location.href = '/login'
          return Promise.reject(error)
        }
      } catch (refreshError) {
        processQueue(refreshError, null)
        localStorage.removeItem('token')
        localStorage.removeItem('refreshToken')
        localStorage.removeItem('user')
        window.location.href = '/login'
        return Promise.reject(refreshError)
      } finally {
        isRefreshing = false
      }
    }

    if (error.response && error.response.status === 403 && error.response.data?.bloqueado === true) {
      const data = error.response.data || {}
      const motivo = data.motivo || 'Acesso bloqueado.'
      if (data.bloqueadoPorAdmin === true) {
        try {
          sessionStorage.setItem('motivoBloqueio', motivo)
        } catch (_) {}
        window.location.href = '/acesso-bloqueado'
        return Promise.reject(error)
      }
      const path = typeof window !== 'undefined' ? window.location.pathname : ''
      try {
        const user = JSON.parse(localStorage.getItem('user') || '{}')
        if (user.role === 'Admin' && path !== '/consultor/faturas') {
          window.location.href = '/consultor/faturas'
        } else if (user.role === 'Cliente' && path !== '/acesso-bloqueado') {
          sessionStorage.setItem('motivoBloqueio', motivo || 'Assinatura vencida. Entre em contato com seu consultor.')
          window.location.href = '/acesso-bloqueado'
        }
      } catch (_) {}
      return Promise.reject(error)
    }
    return Promise.reject(error)
  }
)

export default api

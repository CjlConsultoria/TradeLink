import axios from 'axios'

// API: em produção no Render o front está em tradelink-grun e o back em tradelink-1-ed48
function getBaseURL() {
  if (import.meta.env.VITE_API_URL) {
    return `${import.meta.env.VITE_API_URL.replace(/\/$/, '')}/api`
  }
  if (typeof window !== 'undefined' && window.location.hostname === 'tradelink-grun.onrender.com') {
    return 'https://tradelink-1-ed48.onrender.com/api'
  }
  return '/api'
}

const baseURL = getBaseURL()

const api = axios.create({
  baseURL,
  headers: { 'Content-Type': 'application/json' }
})

api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  response => response,
  error => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
      return Promise.reject(error)
    }
    if (error.response && error.response.status === 403 && error.response.data?.bloqueado === true) {
      const path = typeof window !== 'undefined' ? window.location.pathname : ''
      const jaNaPaginaFaturas = path === '/consultor/faturas' || path === '/cliente/faturas'
      if (!jaNaPaginaFaturas) {
        try {
          const user = JSON.parse(localStorage.getItem('user') || '{}')
          if (user.role === 'Admin') window.location.href = '/consultor/faturas'
          else if (user.role === 'Cliente') window.location.href = '/cliente/faturas'
        } catch (_) {}
      }
      return Promise.reject(error)
    }
    return Promise.reject(error)
  }
)

export default api
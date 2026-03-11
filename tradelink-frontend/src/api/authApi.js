import api from './axiosInstance'

export default {
  login(data) {
    return api.post('/auth/login', data)
  },
  register(data) {
    return api.post('/auth/register', data)
  },
  validarConvite(token) {
    return api.get(`/auth/validar-convite/${token}`)
  },
  ativarConta(data) {
    return api.post('/auth/ativar-conta', data)
  },
  autoCadastro(data) {
    return api.post('/auth/auto-cadastro', data)
  },
  verifyOtp(data) {
    return api.post('/auth/verify-otp', data)
  },
  resendOtp(data) {
    return api.post('/auth/resend-otp', data)
  },
  forgotPassword(data) {
    return api.post('/auth/forgot-password', data)
  },
  resetPassword(data) {
    return api.post('/auth/reset-password', data)
  },
  refresh(data) {
    return api.post('/auth/refresh', data)
  }
}
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
  }
}
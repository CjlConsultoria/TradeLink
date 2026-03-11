import api from './axiosInstance'

export default {
  listar(limit = 30) {
    return api.get('/me/notificacoes', { params: { limit } })
  },
  contarNaoLidas() {
    return api.get('/me/notificacoes/count')
  },
  marcarComoLida(id) {
    return api.put(`/me/notificacoes/${id}/lida`)
  },
  marcarTodasComoLidas() {
    return api.put('/me/notificacoes/lidas')
  }
}

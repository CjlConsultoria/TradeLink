import api from './axiosInstance'

export default {
  listar() {
    return api.get('/me/alertas-preco')
  },
  criar(data) {
    return api.post('/me/alertas-preco', data)
  },
  atualizar(id, data) {
    return api.put(`/me/alertas-preco/${id}`, data)
  },
  toggle(id) {
    return api.put(`/me/alertas-preco/${id}/toggle`)
  },
  excluir(id) {
    return api.delete(`/me/alertas-preco/${id}`)
  }
}

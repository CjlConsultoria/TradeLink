import api from './axiosInstance'

export default {
  listar() {
    return api.get('/cliente/portfolio')
  },
  resumo() {
    return api.get('/cliente/portfolio/resumo')
  },
  adicionar(data) {
    return api.post('/cliente/portfolio', data)
  },
  atualizar(id, data) {
    return api.put(`/cliente/portfolio/${id}`, data)
  },
  remover(id) {
    return api.delete(`/cliente/portfolio/${id}`)
  },

  // Consultor
  resumoCliente(clienteId) {
    return api.get(`/consultor/clientes/${clienteId}/portfolio`)
  },
  resumoCarteira(carteiraId) {
    return api.get(`/consultor/carteiras/${carteiraId}/portfolio-resumo`)
  },
  previewPercentual(data) {
    return api.post('/consultor/recomendacoes/preview-percentual', data)
  }
}

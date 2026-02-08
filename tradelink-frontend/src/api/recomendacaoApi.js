import api from './axiosInstance'

export default {
  listarPorCarteira(carteiraId) {
    return api.get(`/consultor/carteiras/${carteiraId}/recomendacoes`)
  },
  criar(carteiraId, data) {
    return api.post(`/consultor/carteiras/${carteiraId}/recomendacoes`, data)
  },
  atualizar(id, data) {
    return api.put(`/consultor/recomendacoes/${id}`, data)
  },
  cancelar(id) {
    return api.patch(`/consultor/recomendacoes/${id}/cancelar`)
  },
  executar(id) {
    return api.patch(`/consultor/recomendacoes/${id}/executar`)
  },
  listarComoCliente(carteiraId) {
    return api.get(`/cliente/carteiras/${carteiraId}/recomendacoes`)
  },
  listarFiltradoCliente(params) {
    return api.get('/cliente/recomendacoes', { params })
  },
  marcarResolvido(recomendacaoId, resolvido) {
    return api.put(`/cliente/recomendacoes/${recomendacaoId}/resolvido`, { resolvido })
  }
}
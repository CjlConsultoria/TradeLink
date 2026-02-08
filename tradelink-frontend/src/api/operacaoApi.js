import api from './axiosInstance'

export default {
  registrar(recomendacaoId, data) {
    return api.post(`/cliente/recomendacoes/${recomendacaoId}/operacoes`, data)
  },
  atualizar(operacaoId, data) {
    return api.put(`/cliente/operacoes/${operacaoId}`, data)
  },
  excluir(operacaoId) {
    return api.delete(`/cliente/operacoes/${operacaoId}`)
  },
  listarPorRecomendacao(recomendacaoId) {
    return api.get(`/cliente/recomendacoes/${recomendacaoId}/operacoes`)
  },
  listarMinhas() {
    return api.get('/cliente/operacoes')
  },
  listarComoConsultor(recomendacaoId) {
    return api.get(`/consultor/recomendacoes/${recomendacaoId}/operacoes`)
  }
}

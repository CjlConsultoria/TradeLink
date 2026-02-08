import api from './axiosInstance'

export default {
  listarUltimas() {
    return api.get('/cotacoes')
  },
  buscar(moeda, parMoeda) {
    return api.get(`/cotacoes/${moeda}/${parMoeda}`)
  },
  historico(moeda, parMoeda, horas = 24) {
    return api.get(`/cotacoes/${moeda}/${parMoeda}/historico`, { params: { horas } })
  },
  refresh() {
    return api.post('/cotacoes/refresh')
  },
  refreshSingle(moeda, parMoeda) {
    return api.get(`/cotacoes/refresh/${encodeURIComponent(moeda)}/${encodeURIComponent(parMoeda)}`)
  },
  listarPaginado(params) {
    return api.get('/cotacoes/paginado', { params })
  }
}
import api from './axiosInstance'

export default {
  operacoes(params = {}) {
    return api.get('/cliente/relatorios/operacoes', { params })
  },
  resumo(params = {}) {
    return api.get('/cliente/relatorios/resumo', { params })
  },
  operacoesPdf(params = {}) {
    return api.get('/cliente/relatorios/operacoes/pdf', { params, responseType: 'blob' })
  },
  resumoPdf(params = {}) {
    return api.get('/cliente/relatorios/resumo/pdf', { params, responseType: 'blob' })
  }
}

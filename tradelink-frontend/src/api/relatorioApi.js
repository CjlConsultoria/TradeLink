import api from './axiosInstance'

export default {
  operacoes(params = {}) {
    return api.get('/consultor/relatorios/operacoes', { params })
  },
  resumo(params = {}) {
    return api.get('/consultor/relatorios/resumo', { params })
  },
  operacoesPdf(params = {}) {
    return api.get('/consultor/relatorios/operacoes/pdf', { params, responseType: 'blob' })
  },
  resumoPdf(params = {}) {
    return api.get('/consultor/relatorios/resumo/pdf', { params, responseType: 'blob' })
  }
}

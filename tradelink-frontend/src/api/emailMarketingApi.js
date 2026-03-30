import api from './axiosInstance'

const BASE = '/admin-max/email-marketing'

export default {
  enviar(contatos, assunto, campanha) {
    return api.post(`${BASE}/enviar`, { contatos, assunto, campanha })
  },
  historico() {
    return api.get(`${BASE}/historico`)
  },
  historicoPorCampanha(campanha) {
    return api.get(`${BASE}/historico/${campanha}`)
  },
  preview(nome) {
    return api.get(`${BASE}/preview`, { params: nome ? { nome } : {} })
  }
}

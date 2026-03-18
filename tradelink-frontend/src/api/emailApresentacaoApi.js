import api from './axiosInstance'

const BASE = '/admin-max/emails-apresentacao'

export default {
  enviar(userIds) {
    return api.post(`${BASE}/enviar`, { userIds })
  },
  historico() {
    return api.get(`${BASE}/historico`)
  },
  preview(userId) {
    return api.get(`${BASE}/preview/${userId}`)
  }
}

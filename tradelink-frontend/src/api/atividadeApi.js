import api from './axiosInstance'

export default {
  listar: (limit = 30) => api.get(`/me/atividades?limit=${limit}`)
}

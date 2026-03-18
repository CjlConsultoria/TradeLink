import api from './axiosInstance'

export default {
  listar: () => api.get('/me/metas'),
  criar: (data) => api.post('/me/metas', data),
  atualizar: (id, data) => api.put(`/me/metas/${id}`, data),
  excluir: (id) => api.delete(`/me/metas/${id}`)
}

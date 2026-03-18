import api from './axiosInstance'

export default {
  listar: () => api.get('/me/favoritos'),
  adicionar: (moeda, parMoeda) => api.post('/me/favoritos', { moeda, parMoeda }),
  remover: (moeda, parMoeda) => api.delete(`/me/favoritos/${moeda}/${parMoeda}`)
}

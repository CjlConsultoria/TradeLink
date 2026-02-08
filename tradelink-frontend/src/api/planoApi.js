import api from './axiosInstance'

export default {
  listar() {
    return api.get('/admin-max/planos')
  },
  listarAtivos() {
    return api.get('/admin-max/planos/ativos')
  },
  buscar(id) {
    return api.get(`/admin-max/planos/${id}`)
  },
  criar(data) {
    return api.post('/admin-max/planos', data)
  },
  atualizar(id, data) {
    return api.put(`/admin-max/planos/${id}`, data)
  },
  desativar(id) {
    return api.delete(`/admin-max/planos/${id}`)
  },
  atribuirEmpresa(empresaId, planoId) {
    return api.put(`/admin-max/empresas/${empresaId}/plano`, { planoId })
  }
}

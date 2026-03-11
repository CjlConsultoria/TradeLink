import api from './axiosInstance'

export default {
  dashboardStats() {
    return api.get('/admin-max/dashboard-stats')
  },
  listar(config = {}) {
    return api.get('/admin-max/empresas', config)
  },
  buscar(id) {
    return api.get(`/admin-max/empresas/${id}`)
  },
  criar(data) {
    return api.post('/admin-max/empresas', data)
  },
  atualizar(id, data) {
    return api.put(`/admin-max/empresas/${id}`, data)
  },
  desativar(id) {
    return api.delete(`/admin-max/empresas/${id}`)
  },
  ativar(id) {
    return api.post(`/admin-max/empresas/${id}/ativar`)
  },
  listarUsuarios(empresaId) {
    return api.get(`/admin-max/empresas/${empresaId}/usuarios`)
  },
  criarConsultor(empresaId, data) {
    return api.post(`/admin-max/empresas/${empresaId}/consultores`, data)
  },
  atribuirPlano(empresaId, planoId) {
    return api.put(`/admin-max/empresas/${empresaId}/plano`, { planoId })
  },
  /** Bloqueia ou desbloqueia o acesso à plataforma da empresa (todos consultores e clientes). */
  bloquearAcesso(empresaId, bloqueado) {
    return api.put(`/admin-max/empresas/${empresaId}/bloquear-acesso`, { bloqueado })
  }
}
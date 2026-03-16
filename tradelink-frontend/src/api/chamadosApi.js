import api from './axiosInstance'

/**
 * API de chamados (tickets de suporte).
 * Cliente e Consultor usam /api/cliente/chamados
 * AdminMax usa /api/admin-max/chamados
 */
function isAdmin() {
  try {
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    return user.role === 'AdminMax'
  } catch {
    return false
  }
}

export default {
  // ─── Cliente / Consultor ───
  criar(data) {
    return api.post('/cliente/chamados', data)
  },
  listar() {
    return api.get('/cliente/chamados')
  },
  buscar(id) {
    return api.get(`/cliente/chamados/${id}`)
  },
  listarRespostas(id) {
    return api.get(`/cliente/chamados/${id}/respostas`)
  },
  responder(id, conteudo) {
    return api.post(`/cliente/chamados/${id}/respostas`, { conteudo })
  },

  // ─── AdminMax ───
  listarTodos(status) {
    const params = status ? { status } : {}
    return api.get('/admin-max/chamados', { params })
  },
  stats() {
    return api.get('/admin-max/chamados/stats')
  },
  buscarAdmin(id) {
    return api.get(`/admin-max/chamados/${id}`)
  },
  listarRespostasAdmin(id) {
    return api.get(`/admin-max/chamados/${id}/respostas`)
  },
  responderAdmin(id, conteudo) {
    return api.post(`/admin-max/chamados/${id}/respostas`, { conteudo })
  },
  atualizarStatus(id, status) {
    return api.put(`/admin-max/chamados/${id}/status`, { status })
  }
}

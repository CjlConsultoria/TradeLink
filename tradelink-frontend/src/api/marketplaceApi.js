import api from './axiosInstance'

// ─── Público (sem auth) ──────────────────────────────────────

export function listarConsultoresPublico(termo) {
  const params = termo ? { termo } : {}
  return api.get('/api/public/marketplace', { params })
}

export function getConsultorDetalhe(empresaId) {
  return api.get(`/api/public/marketplace/${empresaId}`)
}

// ─── Cliente ─────────────────────────────────────────────────

export function solicitarMentoria(data) {
  return api.post('/api/cliente/marketplace/solicitar', data)
}

export function listarMinhasSolicitacoes() {
  return api.get('/api/cliente/marketplace/solicitacoes')
}

export function criarCheckoutMarketplace(solicitacaoId) {
  return api.post(`/api/cliente/marketplace/checkout/${solicitacaoId}`)
}

export function confirmarPagamentoMarketplace(sessionId) {
  return api.post('/api/cliente/marketplace/confirmar-pagamento', { sessionId })
}

export function cancelarSolicitacao(solicitacaoId) {
  return api.post(`/api/cliente/marketplace/cancelar/${solicitacaoId}`)
}

// ─── Consultor ───────────────────────────────────────────────

export function getPerfilMarketplace() {
  return api.get('/api/consultor/marketplace/perfil')
}

export function atualizarPerfilMarketplace(data) {
  return api.put('/api/consultor/marketplace/perfil', data)
}

export function listarSolicitacoesConsultor() {
  return api.get('/api/consultor/marketplace/solicitacoes')
}

export function responderSolicitacao(id, data) {
  return api.put(`/api/consultor/marketplace/solicitacoes/${id}/responder`, data)
}

export function listarClientesMarketplace() {
  return api.get('/api/consultor/marketplace/clientes')
}

export function desvincularClienteMarketplace(clienteId) {
  return api.post(`/api/consultor/marketplace/desvincular/${clienteId}`)
}

// ─── Admin Max ───────────────────────────────────────────────

export function adminListarConsultores() {
  return api.get('/api/admin-max/marketplace/consultores')
}

export function adminAlterarVisibilidade(empresaId, visivel) {
  return api.put(`/api/admin-max/marketplace/consultores/${empresaId}/visibilidade`, { visivel })
}

export function adminAtualizarPerfil(empresaId, data) {
  return api.put(`/api/admin-max/marketplace/consultores/${empresaId}/perfil`, data)
}

export function adminRemoverDoMarketplace(empresaId) {
  return api.delete(`/api/admin-max/marketplace/consultores/${empresaId}`)
}

export function adminListarSolicitacoes(status) {
  const params = status ? { status } : {}
  return api.get('/api/admin-max/marketplace/solicitacoes', { params })
}

export function adminAlterarStatusSolicitacao(id, status) {
  return api.put(`/api/admin-max/marketplace/solicitacoes/${id}/status`, { status })
}

export function adminGetStats() {
  return api.get('/api/admin-max/marketplace/stats')
}

export function adminGetTaxa() {
  return api.get('/api/admin-max/marketplace/taxa')
}

export function adminAlterarTaxa(taxa) {
  return api.put('/api/admin-max/marketplace/taxa', { taxa })
}

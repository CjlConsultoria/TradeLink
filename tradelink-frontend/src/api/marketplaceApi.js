import api from './axiosInstance'

// ─── Público (sem auth) ──────────────────────────────────────

export function listarConsultoresPublico(termo) {
  const params = termo ? { termo } : {}
  return api.get('/public/marketplace', { params })
}

export function getConsultorDetalhe(empresaId) {
  return api.get(`/public/marketplace/${empresaId}`)
}

// ─── Cliente ─────────────────────────────────────────────────

export function solicitarMentoria(data) {
  return api.post('/cliente/marketplace/solicitar', data)
}

export function listarMinhasSolicitacoes() {
  return api.get('/cliente/marketplace/solicitacoes')
}

export function criarCheckoutMarketplace(solicitacaoId) {
  return api.post(`/cliente/marketplace/checkout/${solicitacaoId}`)
}

export function confirmarPagamentoMarketplace(sessionId) {
  return api.post('/cliente/marketplace/confirmar-pagamento', { sessionId })
}

export function cancelarSolicitacao(solicitacaoId) {
  return api.post(`/cliente/marketplace/cancelar/${solicitacaoId}`)
}

export function getMarketplaceSubscription() {
  return api.get('/cliente/marketplace/subscription')
}

export function cancelarMarketplaceSubscription() {
  return api.post('/cliente/marketplace/cancelar-subscription')
}

export function portalPagamentoMarketplace() {
  return api.post('/cliente/marketplace/portal-pagamento')
}

export function downloadFaturaPdf(faturaId) {
  return api.get(`/cliente/faturas/${faturaId}/pdf`, { responseType: 'blob' })
}

// ─── Consultor ───────────────────────────────────────────────

export function getPerfilMarketplace() {
  return api.get('/consultor/marketplace/perfil')
}

export function atualizarPerfilMarketplace(data) {
  return api.put('/consultor/marketplace/perfil', data)
}

export function listarSolicitacoesConsultor() {
  return api.get('/consultor/marketplace/solicitacoes')
}

export function responderSolicitacao(id, data) {
  return api.put(`/consultor/marketplace/solicitacoes/${id}/responder`, data)
}

export function confirmarPagamentoManual(solicitacaoId) {
  return api.post(`/consultor/marketplace/confirmar-pagamento/${solicitacaoId}`)
}

export function listarClientesMarketplace() {
  return api.get('/consultor/marketplace/clientes')
}

export function desvincularClienteMarketplace(clienteId) {
  return api.post(`/consultor/marketplace/desvincular/${clienteId}`)
}

// ─── Admin Max ───────────────────────────────────────────────

export function adminListarConsultores() {
  return api.get('/admin-max/marketplace/consultores')
}

export function adminAlterarVisibilidade(empresaId, visivel) {
  return api.put(`/admin-max/marketplace/consultores/${empresaId}/visibilidade`, { visivel })
}

export function adminAtualizarPerfil(empresaId, data) {
  return api.put(`/admin-max/marketplace/consultores/${empresaId}/perfil`, data)
}

export function adminRemoverDoMarketplace(empresaId) {
  return api.delete(`/admin-max/marketplace/consultores/${empresaId}`)
}

export function adminListarSolicitacoes(status) {
  const params = status ? { status } : {}
  return api.get('/admin-max/marketplace/solicitacoes', { params })
}

export function adminAlterarStatusSolicitacao(id, status) {
  return api.put(`/admin-max/marketplace/solicitacoes/${id}/status`, { status })
}

export function adminGetStats() {
  return api.get('/admin-max/marketplace/stats')
}

export function adminGetTaxa() {
  return api.get('/admin-max/marketplace/taxa')
}

export function adminAlterarTaxa(taxa) {
  return api.put('/admin-max/marketplace/taxa', { taxa })
}

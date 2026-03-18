import api from './axiosInstance'

export default {
  listarAlocacoes(carteiraId) {
    return api.get(`/consultor/carteiras/${carteiraId}/alocacoes`)
  },
  salvarAlocacoes(carteiraId, data) {
    return api.put(`/consultor/carteiras/${carteiraId}/alocacoes`, data)
  },
  analisarCarteira(carteiraId) {
    return api.get(`/consultor/carteiras/${carteiraId}/rebalanceamento`)
  },
  analisarCliente(carteiraId, clienteId) {
    return api.get(`/consultor/carteiras/${carteiraId}/rebalanceamento/${clienteId}`)
  },
  gerarRecomendacoes(carteiraId) {
    return api.post(`/consultor/carteiras/${carteiraId}/rebalanceamento/gerar-recomendacoes`)
  },
  saudeClientes() {
    return api.get('/consultor/saude-clientes')
  },
  analiseConsolidada() {
    return api.get('/consultor/rebalanceamento/consolidado')
  },
  gerarRecomendacoesLote(items) {
    return api.post('/consultor/rebalanceamento/gerar-recomendacoes-lote', { items })
  },
  // Performance / Snapshot
  criarSnapshot(carteiraId) {
    return api.post(`/consultor/carteiras/${carteiraId}/snapshot`)
  },
  performanceConsultor(carteiraId, clienteId) {
    const params = clienteId ? { clienteId } : {}
    return api.get(`/consultor/carteiras/${carteiraId}/performance`, { params })
  },
  // Movimentacoes - Consultor
  registrarMovimentacaoConsultor(carteiraId, clienteId, data) {
    return api.post(`/consultor/carteiras/${carteiraId}/clientes/${clienteId}/movimentacoes`, data)
  },
  listarMovimentacoesCarteira(carteiraId) {
    return api.get(`/consultor/carteiras/${carteiraId}/movimentacoes`)
  },
  saldoClienteConsultor(carteiraId, clienteId) {
    return api.get(`/consultor/carteiras/${carteiraId}/clientes/${clienteId}/saldo`)
  },
  // Cliente
  minhaAlocacao(carteiraId) {
    return api.get(`/cliente/carteiras/${carteiraId}/minha-alocacao`)
  },
  performanceCliente(carteiraId) {
    return api.get(`/cliente/carteiras/${carteiraId}/performance`)
  },
  registrarMovimentacao(carteiraId, data) {
    return api.post(`/cliente/carteiras/${carteiraId}/movimentacoes`, data)
  },
  listarMovimentacoes(carteiraId) {
    return api.get(`/cliente/carteiras/${carteiraId}/movimentacoes`)
  },
  meuSaldo(carteiraId) {
    return api.get(`/cliente/carteiras/${carteiraId}/saldo`)
  }
}

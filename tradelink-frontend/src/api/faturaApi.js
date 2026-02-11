import api from './axiosInstance'

export default {
  /** Consultor: lista faturas da minha empresa + próxima + acessoPermitido */
  listarMinhas() {
    return api.get('/consultor/faturas')
  },
  /** Cliente: lista faturas da minha empresa + próxima + acessoPermitido */
  listarMinhasCliente() {
    return api.get('/cliente/faturas')
  },
  /** Consultor: gera cobrança PIX (retorna clientSecret e paymentIntentId) */
  checkoutPix() {
    return api.post('/consultor/checkout-pix')
  },
  /** Cliente: gera cobrança PIX */
  checkoutPixCliente() {
    return api.post('/cliente/checkout-pix')
  },
  /** Consultor: abre Checkout Stripe (cartão ou boleto) – redireciona para página do Stripe */
  checkoutCartaoBoleto() {
    return api.post('/consultor/checkout-cartao-boleto')
  },
  /** Cliente: abre Checkout Stripe (cartão ou boleto) – redireciona para página do Stripe */
  checkoutCartaoBoletoCliente() {
    return api.post('/cliente/checkout-cartao-boleto')
  },
  /** Consultor: pagamento embutido (cartão, PIX, boleto) na própria tela. Retorna clientSecret e publishableKey. */
  checkoutEmbedded() {
    return api.post('/consultor/checkout-embedded')
  },
  /** Cliente: pagamento embutido (cartão, PIX, boleto) na própria tela. Retorna clientSecret e publishableKey. */
  checkoutEmbeddedCliente() {
    return api.post('/cliente/checkout-embedded')
  },
  /** Consultor: confirma pagamento após retorno do Stripe (session_id na URL) */
  confirmarStripeConsultor(sessionId) {
    return api.post('/consultor/faturas/confirmar-stripe', { sessionId })
  },
  /** Cliente: confirma pagamento após retorno do Stripe (session_id na URL) */
  confirmarStripeCliente(sessionId) {
    return api.post('/cliente/faturas/confirmar-stripe', { sessionId })
  },
  /** Consultor: confirma pagamento embutido (Payment Element) por payment_intent_id — registra fatura e avança período */
  confirmarPagamentoEmbutidoConsultor(paymentIntentId) {
    return api.post('/consultor/faturas/confirmar-pagamento-embutido', { paymentIntentId })
  },
  /** Cliente: confirma pagamento embutido por payment_intent_id */
  confirmarPagamentoEmbutidoCliente(paymentIntentId) {
    return api.post('/cliente/faturas/confirmar-pagamento-embutido', { paymentIntentId })
  },
  /** Consultor: baixar PDF da fatura (retorna blob) */
  getPdfBlobConsultor(faturaId) {
    return api.get(`/consultor/faturas/${faturaId}/pdf`, { responseType: 'blob' })
  },
  /** AdminMax: lista faturas de uma empresa */
  listarPorEmpresa(empresaId) {
    return api.get(`/admin-max/empresas/${empresaId}/faturas`)
  },
  /** AdminMax: criar fatura (data, valor, descricaoServico, observacao) */
  criarFatura(empresaId, data) {
    return api.post(`/admin-max/empresas/${empresaId}/faturas`, data)
  },
  /** AdminMax: atualizar fatura */
  atualizarFatura(empresaId, faturaId, data) {
    return api.put(`/admin-max/empresas/${empresaId}/faturas/${faturaId}`, data)
  },
  /** AdminMax: excluir fatura */
  excluirFatura(empresaId, faturaId) {
    return api.delete(`/admin-max/empresas/${empresaId}/faturas/${faturaId}`)
  },
  /** AdminMax: baixar PDF da fatura (retorna response com data = blob para abrir em nova aba) */
  getPdfBlob(empresaId, faturaId) {
    return api.get(`/admin-max/empresas/${empresaId}/faturas/${faturaId}/pdf`, { responseType: 'blob' })
  },
  /** AdminMax: marcar como pago (outra forma) */
  marcarPago(empresaId, data) {
    return api.post(`/admin-max/empresas/${empresaId}/faturas/marcar-pago`, data || {})
  }
}

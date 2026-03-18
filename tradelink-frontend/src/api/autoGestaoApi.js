import api from './axiosInstance'

export default {
  getStatus() {
    return api.get('/cliente/exclusao/status')
  },
  baixarRelatorioGratuito() {
    return api.get('/cliente/exclusao/relatorio-completo', { responseType: 'blob' })
  },
  checkoutAutoGestao(returnUrl) {
    return api.post('/cliente/exclusao/checkout-autogestao', returnUrl ? { returnUrl } : {})
  },
  checkoutRelatorio() {
    return api.post('/cliente/exclusao/checkout-relatorio')
  },
  baixarRelatorioPago() {
    return api.get('/cliente/exclusao/relatorio-pago', { responseType: 'blob' })
  },
  confirmarPagamento(id) {
    // Aceita payment_intent_id (pi_xxx) ou session_id (cs_xxx)
    const body = id && id.startsWith('cs_') ? { sessionId: id } : { paymentIntentId: id }
    return api.post('/cliente/exclusao/confirmar-pagamento', body)
  }
}

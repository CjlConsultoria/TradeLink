import api from './axiosInstance'

export default {
  /** Cria sessão de checkout Stripe; retorna { data: { checkoutUrl } }. */
  createCheckout(empresaId, planoId) {
    return api.post(`/admin-max/empresas/${empresaId}/checkout`, { planoId })
  },
  /** Verifica se o Stripe está configurado no backend. */
  isConfigured() {
    return api.get('/admin-max/pagamentos/configurado')
  }
}

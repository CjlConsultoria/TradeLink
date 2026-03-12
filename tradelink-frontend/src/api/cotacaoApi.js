import api from './axiosInstance'

export default {
  listarUltimas() {
    return api.get('/cotacoes')
  },
  buscar(moeda, parMoeda) {
    return api.get(`/cotacoes/${moeda}/${parMoeda}`)
  },
  historico(moeda, parMoeda, horas = 24) {
    return api.get(`/cotacoes/${moeda}/${parMoeda}/historico`, { params: { horas } })
  },
  /**
   * Dados historicos OHLCV (candlestick).
   * @param {string} moeda - Ex: USD, BTC
   * @param {string} parMoeda - Ex: BRL, USD
   * @param {number} dias - Quantidade de dias (7, 30, 90, 180, 365, 0=max)
   * @param {string} [de] - ISO datetime inicio (opcional)
   * @param {string} [ate] - ISO datetime fim (opcional)
   */
  ohlcv(moeda, parMoeda, dias = 90, de, ate) {
    const params = { dias }
    if (de) params.de = de
    if (ate) params.ate = ate
    return api.get(`/cotacoes/${moeda}/${parMoeda}/ohlcv`, { params })
  },
  paresDisponiveis() {
    return api.get('/cotacoes/pares-disponiveis')
  },
  refresh() {
    return api.post('/cotacoes/refresh')
  },
  refreshSingle(moeda, parMoeda) {
    return api.get(`/cotacoes/refresh/${encodeURIComponent(moeda)}/${encodeURIComponent(parMoeda)}`)
  },
  listarPaginado(params) {
    return api.get('/cotacoes/paginado', { params })
  }
}

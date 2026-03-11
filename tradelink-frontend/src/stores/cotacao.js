import { defineStore } from 'pinia'
import cotacaoApi from '../api/cotacaoApi'
import { ref } from 'vue'

export const useCotacaoStore = defineStore('cotacao', () => {
  const cotacoes = ref([])
  const loading = ref(false)
  const paresDisponiveis = ref([])
  let pollingInterval = null

  async function fetchLatest() {
    loading.value = true
    try {
      const res = await cotacaoApi.listarUltimas()
      cotacoes.value = res.data
    } catch (e) {
      console.error('Erro ao buscar cotacoes:', e)
    } finally {
      loading.value = false
    }
  }

  function startPolling(intervalMs = 60000) {
    fetchLatest()
    pollingInterval = setInterval(fetchLatest, intervalMs)
  }

  function stopPolling() {
    if (pollingInterval) {
      clearInterval(pollingInterval)
      pollingInterval = null
    }
  }

  async function forceRefresh() {
    loading.value = true
    try {
      const res = await cotacaoApi.refresh()
      cotacoes.value = res.data
    } catch (e) {
      console.error('Erro ao atualizar cotações:', e)
    } finally {
      loading.value = false
    }
  }

  async function refreshSingle(moeda, parMoeda) {
    try {
      const res = await cotacaoApi.refreshSingle(moeda, parMoeda)
      const idx = cotacoes.value.findIndex(c => c.moeda === moeda && c.parMoeda === parMoeda)
      if (idx >= 0) cotacoes.value[idx] = res.data
      else cotacoes.value = [...cotacoes.value, res.data]
      return res.data
    } catch (e) {
      console.error('Erro ao atualizar cotação:', e)
      throw e
    }
  }

  async function fetchOHLCV(moeda, parMoeda, intervalo = '1day', de, ate) {
    try {
      const res = await cotacaoApi.ohlcv(moeda, parMoeda, intervalo, de, ate)
      return res.data
    } catch (e) {
      console.error('Erro ao buscar OHLCV:', e)
      return []
    }
  }

  async function fetchParesDisponiveis() {
    try {
      const res = await cotacaoApi.paresDisponiveis()
      paresDisponiveis.value = res.data
      return res.data
    } catch (e) {
      console.error('Erro ao buscar pares disponíveis:', e)
      return []
    }
  }

  return {
    cotacoes, loading, paresDisponiveis,
    fetchLatest, startPolling, stopPolling,
    forceRefresh, refreshSingle,
    fetchOHLCV, fetchParesDisponiveis
  }
})

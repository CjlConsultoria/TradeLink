import { defineStore } from 'pinia'
import { ref } from 'vue'
import notificacaoApi from '../api/notificacaoApi'

export const useNotificacaoStore = defineStore('notificacao', () => {
  const notificacoes = ref([])
  const naoLidas = ref(0)
  const loading = ref(false)
  let pollInterval = null

  async function carregarContagem() {
    try {
      const res = await notificacaoApi.contarNaoLidas()
      naoLidas.value = res.data?.count || 0
    } catch (_) {}
  }

  async function carregarNotificacoes() {
    loading.value = true
    try {
      const res = await notificacaoApi.listar()
      notificacoes.value = res.data || []
    } catch (_) {
      notificacoes.value = []
    } finally {
      loading.value = false
    }
  }

  async function marcarComoLida(id) {
    try {
      await notificacaoApi.marcarComoLida(id)
      const n = notificacoes.value.find(x => x.id === id)
      if (n && !n.lida) {
        n.lida = true
        naoLidas.value = Math.max(0, naoLidas.value - 1)
      }
    } catch (_) {}
  }

  async function marcarTodasComoLidas() {
    try {
      await notificacaoApi.marcarTodasComoLidas()
      notificacoes.value.forEach(n => { n.lida = true })
      naoLidas.value = 0
    } catch (_) {}
  }

  function iniciarPolling() {
    pararPolling()
    carregarContagem()
    pollInterval = setInterval(carregarContagem, 30_000) // 30s
  }

  function pararPolling() {
    if (pollInterval) {
      clearInterval(pollInterval)
      pollInterval = null
    }
  }

  return {
    notificacoes,
    naoLidas,
    loading,
    carregarContagem,
    carregarNotificacoes,
    marcarComoLida,
    marcarTodasComoLidas,
    iniciarPolling,
    pararPolling
  }
})

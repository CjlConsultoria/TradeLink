import { defineStore } from 'pinia'
import empresaApi from '../api/empresaApi'
import { ref } from 'vue'

export const useEmpresaStore = defineStore('empresa', () => {
  const empresas = ref([])
  const empresaAtual = ref(null)
  const loading = ref(false)
  const listarError = ref(null)

  async function listar() {
    loading.value = true
    listarError.value = null
    const TIMEOUT_MS = 20000
    const controller = new AbortController()
    const timeoutId = setTimeout(() => controller.abort(), TIMEOUT_MS)
    try {
      const res = await empresaApi.listar({ signal: controller.signal })
      clearTimeout(timeoutId)
      const data = res?.data
      empresas.value = Array.isArray(data) ? data : (data?.content != null ? data.content : [])
    } catch (e) {
      clearTimeout(timeoutId)
      console.error('Erro ao listar empresas:', e?.response?.status, e?.response?.data, e)
      empresas.value = []
      if (e?.name === 'AbortError' || e?.code === 'ECONNABORTED') {
        listarError.value = 'Tempo esgotado ao carregar. Verifique se o servidor está online e tente novamente.'
      } else {
        const status = e?.response?.status
        listarError.value = status === 403
          ? 'Sem permissão para listar empresas. Faça login como Super Admin.'
          : (e?.response?.data?.erro || e?.message || 'Erro ao carregar empresas.')
      }
    } finally {
      loading.value = false
    }
  }

  async function buscar(id) {
    loading.value = true
    try {
      const res = await empresaApi.buscar(id)
      empresaAtual.value = res.data
    } finally {
      loading.value = false
    }
  }

  return { empresas, empresaAtual, loading, listarError, listar, buscar }
})
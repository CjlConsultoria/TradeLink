<template>
  <div>
    <div class="flex items-center gap-3 mb-6">
      <router-link :to="voltarPara" class="text-gray-500 hover:text-gray-700 p-1 rounded">
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/></svg>
      </router-link>
      <h2 class="text-2xl font-bold text-gray-900">{{ cotacao?.moeda || moeda }}/{{ cotacao?.parMoeda || parMoeda }}</h2>
    </div>

    <LoadingSpinner v-if="loading && !cotacao" />
    <template v-else>
      <div v-if="erro" class="bg-red-50 border border-red-200 rounded-xl p-6 text-red-700">
        <p>{{ erro }}</p>
      </div>
      <template v-else>
        <div class="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-6">
          <div class="card p-6">
            <p class="text-sm text-gray-500 mb-1">Compra</p>
            <p class="text-xl font-semibold">{{ cotacao ? formatCurrency(cotacao.precoCompra, cotacao.parMoeda) : '-' }}</p>
          </div>
          <div class="card p-6">
            <p class="text-sm text-gray-500 mb-1">Venda</p>
            <p class="text-xl font-semibold">{{ cotacao ? formatCurrency(cotacao.precoVenda, cotacao.parMoeda) : '-' }}</p>
          </div>
          <div class="card p-6">
            <p class="text-sm text-gray-500 mb-1">Variação (24h)</p>
            <p class="text-xl font-semibold" :class="variacaoClass(cotacao?.variacao)">
              {{ cotacao?.variacao != null ? formatPercent(cotacao.variacao) : '-' }}
            </p>
          </div>
        </div>
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
          <div class="card p-6">
            <p class="text-sm text-gray-500 mb-1">Máximo</p>
            <p class="text-lg font-medium">{{ cotacao?.maximo != null ? formatCurrency(cotacao.maximo, cotacao.parMoeda) : '-' }}</p>
          </div>
          <div class="card p-6">
            <p class="text-sm text-gray-500 mb-1">Mínimo</p>
            <p class="text-lg font-medium">{{ cotacao?.minimo != null ? formatCurrency(cotacao.minimo, cotacao.parMoeda) : '-' }}</p>
          </div>
        </div>
        <div class="card p-6 mb-6">
          <p class="text-sm text-gray-500 mb-2">Última atualização: {{ cotacao ? formatDate(cotacao.dataHora) : '-' }} · Fonte: {{ cotacao?.fonte || '-' }}</p>
          <button type="button" @click="atualizarCotacao" :disabled="refreshing" class="px-4 py-2 bg-indigo-600 text-white rounded-lg text-sm hover:bg-indigo-700 disabled:opacity-50">
            {{ refreshing ? 'Atualizando...' : 'Atualizar cotação' }}
          </button>
        </div>

        <div class="card p-6">
          <h3 class="text-lg font-semibold mb-4">Histórico (últimas {{ horas }}h)</h3>
          <div v-if="historicoLoading" class="h-64 flex items-center justify-center text-gray-500">Carregando...</div>
          <div v-else-if="historico.length === 0" class="h-64 flex items-center justify-center text-gray-500">Sem dados de histórico.</div>
          <div v-else class="h-80">
            <canvas ref="chartCanvas"></canvas>
          </div>
          <div class="flex gap-2 mt-3">
            <button v-for="h in [24, 48, 72]" :key="h" type="button" @click="horas = h; carregarHistorico()"
              :class="horas === h ? 'bg-indigo-600 text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'"
              class="px-3 py-1.5 rounded text-sm">{{ h }}h</button>
          </div>
        </div>
      </template>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import cotacaoApi from '../api/cotacaoApi'
import { formatCurrency, formatPercent, formatDate } from '../utils/formatters'
import LoadingSpinner from '../components/common/LoadingSpinner.vue'
import { Chart, registerables } from 'chart.js'

Chart.register(...registerables)

const route = useRoute()
const moeda = computed(() => route.params.moeda || '')
const parMoeda = computed(() => route.params.parMoeda || '')
const basePath = computed(() => route.path.includes('/consultor') ? '/consultor' : '/cliente')
const voltarPara = computed(() => `${basePath.value}/cotacoes`)

const cotacao = ref(null)
const historico = ref([])
const loading = ref(true)
const historicoLoading = ref(false)
const refreshing = ref(false)
const erro = ref('')
const horas = ref(24)
const chartCanvas = ref(null)
let chartInstance = null

function variacaoClass(v) {
  if (v == null) return ''
  const n = Number(v)
  if (n > 0) return 'text-green-600'
  if (n < 0) return 'text-red-600'
  return 'text-gray-600'
}

async function carregar() {
  if (!moeda.value || !parMoeda.value) return
  loading.value = true
  erro.value = ''
  try {
    const res = await cotacaoApi.buscar(moeda.value, parMoeda.value)
    cotacao.value = res.data
  } catch (e) {
    cotacao.value = null
    erro.value = e.response?.status === 404 ? 'Cotação não encontrada.' : e.response?.data?.erro || 'Erro ao carregar.'
  } finally {
    loading.value = false
  }
}

async function carregarHistorico() {
  if (!moeda.value || !parMoeda.value) return
  historicoLoading.value = true
  try {
    const res = await cotacaoApi.historico(moeda.value, parMoeda.value, horas.value)
    historico.value = (res.data || []).slice().reverse()
    nextTick(() => atualizarGrafico())
  } catch (e) {
    historico.value = []
  } finally {
    historicoLoading.value = false
  }
}

function atualizarGrafico() {
  if (!chartCanvas.value || historico.value.length === 0) return
  if (chartInstance) chartInstance.destroy()
  const labels = historico.value.map(c => formatDate(c.dataHora))
  const compra = historico.value.map(c => c.precoCompra != null ? Number(c.precoCompra) : null)
  const venda = historico.value.map(c => c.precoVenda != null ? Number(c.precoVenda) : null)
  chartInstance = new Chart(chartCanvas.value, {
    type: 'line',
    data: {
      labels,
      datasets: [
        { label: 'Compra', data: compra, borderColor: 'rgb(79, 70, 229)', backgroundColor: 'rgba(79, 70, 229, 0.1)', fill: true, tension: 0.3 },
        { label: 'Venda', data: venda, borderColor: 'rgb(34, 197, 94)', backgroundColor: 'rgba(34, 197, 94, 0.1)', fill: true, tension: 0.3 }
      ]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: { legend: { position: 'top' } },
      scales: {
        y: { beginAtZero: false },
        x: { maxTicksLimit: 12 }
      }
    }
  })
}

async function atualizarCotacao() {
  refreshing.value = true
  try {
    const res = await cotacaoApi.refreshSingle(moeda.value, parMoeda.value)
    cotacao.value = res.data
    await carregarHistorico()
  } catch (e) {
    console.error(e)
  } finally {
    refreshing.value = false
  }
}

const INTERVALO_ATUALIZACAO_MS = 3 * 60 * 1000
let intervalId = null
onMounted(() => {
  carregar()
  carregarHistorico()
  intervalId = setInterval(() => { carregar(); carregarHistorico() }, INTERVALO_ATUALIZACAO_MS)
})
onUnmounted(() => {
  if (intervalId) clearInterval(intervalId)
})
watch([moeda, parMoeda], () => { carregar(); carregarHistorico() })
</script>

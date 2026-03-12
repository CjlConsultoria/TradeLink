<template>
  <div>
    <div class="flex items-center gap-3 mb-6">
      <router-link :to="voltarPara" class="text-gray-500 hover:text-gray-700 p-1 rounded">
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/></svg>
      </router-link>
      <h2 class="text-2xl font-bold text-gray-900">{{ cotacao?.moeda || moeda }}/{{ cotacao?.parMoeda || parMoeda }}</h2>
      <span class="text-xs px-2 py-0.5 rounded bg-indigo-100 text-indigo-700 font-medium">{{ cotacao?.fonte || 'TWELVE_DATA' }}</span>
    </div>

    <LoadingSpinner v-if="loading && !cotacao" />
    <template v-else>
      <div v-if="erro" class="bg-red-50 border border-red-200 rounded-xl p-6 text-red-700">
        <p>{{ erro }}</p>
      </div>
      <template v-else>
        <!-- Cards de preço -->
        <div class="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
          <div class="card p-4 lg:p-6">
            <p class="text-xs text-gray-500 mb-1">Preço Atual</p>
            <p class="text-lg lg:text-xl font-semibold">{{ cotacao ? formatCurrency(cotacao.precoCompra, cotacao.parMoeda) : '-' }}</p>
          </div>
          <div class="card p-4 lg:p-6">
            <p class="text-xs text-gray-500 mb-1">Variação (24h)</p>
            <p class="text-lg lg:text-xl font-semibold" :class="variacaoClass(cotacao?.variacao)">
              {{ cotacao?.variacao != null ? formatPercent(cotacao.variacao) : '-' }}
            </p>
          </div>
          <div class="card p-4 lg:p-6">
            <p class="text-xs text-gray-500 mb-1">Máximo</p>
            <p class="text-base lg:text-lg font-medium">{{ cotacao?.maximo != null ? formatCurrency(cotacao.maximo, cotacao.parMoeda) : '-' }}</p>
          </div>
          <div class="card p-4 lg:p-6">
            <p class="text-xs text-gray-500 mb-1">Mínimo</p>
            <p class="text-base lg:text-lg font-medium">{{ cotacao?.minimo != null ? formatCurrency(cotacao.minimo, cotacao.parMoeda) : '-' }}</p>
          </div>
        </div>

        <!-- Info e refresh -->
        <div class="card p-4 mb-6 flex flex-wrap items-center gap-3">
          <p class="text-sm text-gray-500 flex-1">
            Última atualização: {{ cotacao ? formatDate(cotacao.dataHora) : '-' }}
          </p>
          <button type="button" @click="atualizarCotacao" :disabled="refreshing"
            class="px-4 py-2 bg-indigo-600 text-white rounded-lg text-sm hover:bg-indigo-700 disabled:opacity-50 whitespace-nowrap">
            {{ refreshing ? 'Atualizando...' : 'Atualizar cotação' }}
          </button>
        </div>

        <!-- Gráfico OHLCV (Candlestick / Linha) -->
        <div class="card p-4 lg:p-6 mb-6">
          <div class="flex flex-wrap items-center justify-between gap-3 mb-4">
            <h3 class="text-lg font-semibold">Gráfico Histórico</h3>
            <!-- Seletor de período -->
            <div class="flex gap-1 flex-wrap">
              <button v-for="p in periodos" :key="p.value" type="button"
                @click="periodoSelecionado = p.value; carregarOHLCV()"
                :class="periodoSelecionado === p.value ? 'bg-indigo-600 text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'"
                class="px-2.5 py-1 rounded text-xs font-medium transition-colors">
                {{ p.label }}
              </button>
            </div>
          </div>

          <div v-if="ohlcvLoading" class="h-96 flex items-center justify-center text-gray-500">
            <svg class="animate-spin h-6 w-6 mr-2 text-indigo-600" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" fill="none"/><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v4a4 4 0 00-4 4H4z"/></svg>
            Carregando dados...
          </div>
          <div v-else-if="ohlcvDados.length === 0" class="h-96 flex items-center justify-center text-gray-400">
            <div class="text-center">
              <svg class="w-12 h-12 mx-auto mb-3 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"/></svg>
              <p>Sem dados OHLCV disponíveis para este par.</p>
              <p class="text-xs mt-1">Os dados serão carregados automaticamente na próxima atualização.</p>
            </div>
          </div>
          <CotacaoCandlestickChart v-else
            :dados="ohlcvDados"
            :moeda="moeda"
            :parMoeda="parMoeda"
            :altura="400" />
        </div>

        <!-- Tabela OHLCV recente -->
        <div v-if="ohlcvDados.length > 0" class="card p-4 lg:p-6">
          <h3 class="text-lg font-semibold mb-4">Dados Recentes ({{ intervaloAtual }})</h3>
          <div class="overflow-x-auto">
            <table class="w-full text-sm">
              <thead>
                <tr class="text-left text-gray-500 border-b">
                  <th class="pb-2 pr-4">Data</th>
                  <th class="pb-2 pr-4 text-right">Abertura</th>
                  <th class="pb-2 pr-4 text-right">Máxima</th>
                  <th class="pb-2 pr-4 text-right">Mínima</th>
                  <th class="pb-2 pr-4 text-right">Fechamento</th>
                  <th class="pb-2 text-right">Volume</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="d in ohlcvRecentes" :key="d.id" class="border-b border-gray-50 hover:bg-gray-50">
                  <td class="py-2 pr-4 text-gray-700">{{ formatDate(d.dataHora) }}</td>
                  <td class="py-2 pr-4 text-right">{{ formatCurrency(d.open, parMoeda) }}</td>
                  <td class="py-2 pr-4 text-right text-green-600">{{ formatCurrency(d.high, parMoeda) }}</td>
                  <td class="py-2 pr-4 text-right text-red-600">{{ formatCurrency(d.low, parMoeda) }}</td>
                  <td class="py-2 pr-4 text-right font-medium" :class="Number(d.close) >= Number(d.open) ? 'text-green-600' : 'text-red-600'">
                    {{ formatCurrency(d.close, parMoeda) }}
                  </td>
                  <td class="py-2 text-right text-gray-500">{{ d.volume ? Number(d.volume).toLocaleString('pt-BR') : '-' }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </template>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import cotacaoApi from '../api/cotacaoApi'
import { formatCurrency, formatPercent, formatDate } from '../utils/formatters'
import LoadingSpinner from '../components/common/LoadingSpinner.vue'
import CotacaoCandlestickChart from '../components/cotacao/CotacaoCandlestickChart.vue'

const route = useRoute()
const moeda = computed(() => route.params.moeda || '')
const parMoeda = computed(() => route.params.parMoeda || '')
const basePath = computed(() => route.path.includes('/consultor') ? '/consultor' : '/cliente')
const voltarPara = computed(() => `${basePath.value}/cotacoes`)

const cotacao = ref(null)
const ohlcvDados = ref([])
const loading = ref(true)
const ohlcvLoading = ref(false)
const refreshing = ref(false)
const erro = ref('')
const periodoSelecionado = ref('1M')

const periodos = [
  { value: '1S', label: '1S', dias: 7 },
  { value: '1M', label: '1M', dias: 30 },
  { value: '3M', label: '3M', dias: 90 },
  { value: '6M', label: '6M', dias: 180 },
  { value: '1A', label: '1A', dias: 365 },
  { value: 'MAX', label: 'Máx', dias: 0 }
]

const periodoAtual = computed(() => periodos.find(p => p.value === periodoSelecionado.value) || periodos[1])
const intervaloAtual = computed(() => {
  const d = periodoAtual.value.dias
  if (d === 0) return 'Máximo'
  if (d <= 30) return 'Diário'
  if (d <= 180) return 'Semanal'
  return 'Mensal'
})

const ohlcvRecentes = computed(() => {
  if (!ohlcvDados.value || ohlcvDados.value.length === 0) return []
  return [...ohlcvDados.value].reverse().slice(0, 10)
})

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

async function carregarOHLCV() {
  if (!moeda.value || !parMoeda.value) return
  ohlcvLoading.value = true
  try {
    const periodo = periodoAtual.value
    const dias = periodo.dias || 0 // 0 = max
    const res = await cotacaoApi.ohlcv(moeda.value, parMoeda.value, dias)
    ohlcvDados.value = res.data || []
  } catch (e) {
    console.error('Erro ao carregar OHLCV:', e)
    ohlcvDados.value = []
  } finally {
    ohlcvLoading.value = false
  }
}

async function atualizarCotacao() {
  refreshing.value = true
  try {
    const res = await cotacaoApi.refreshSingle(moeda.value, parMoeda.value)
    cotacao.value = res.data
    await carregarOHLCV()
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
  carregarOHLCV()
  intervalId = setInterval(() => { carregar() }, INTERVALO_ATUALIZACAO_MS)
})
onUnmounted(() => {
  if (intervalId) clearInterval(intervalId)
})
watch([moeda, parMoeda], () => { carregar(); carregarOHLCV() })
</script>

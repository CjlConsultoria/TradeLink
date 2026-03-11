<template>
  <div>
    <h2 class="page-title">Heat Map · Cotações</h2>
    <p class="text-sm text-gray-500 mb-4">Visão rápida do mercado. Verde = valorização, Vermelho = desvalorização nas últimas horas.</p>

    <div class="flex flex-wrap gap-2 mb-6">
      <button v-for="cat in categorias" :key="cat.key" type="button"
        @click="filtroCategoria = cat.key"
        class="px-3 py-1.5 rounded-lg text-sm font-medium transition-colors"
        :class="filtroCategoria === cat.key ? 'bg-indigo-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'">
        {{ cat.label }}
      </button>
    </div>

    <div v-if="loading" class="text-center py-12 text-gray-400">Carregando cotações...</div>

    <div v-else class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6 gap-3">
      <div v-for="c in cotacoesFiltradas" :key="c.moeda + c.parMoeda"
        class="relative rounded-xl p-4 cursor-pointer transition-all hover:scale-105 hover:shadow-lg border"
        :class="tileClass(c)"
        @click="irParaDetalhe(c)">
        <div class="flex items-center justify-between mb-1">
          <span class="font-bold text-sm">{{ c.moeda }}</span>
          <span class="text-xs opacity-75">{{ c.parMoeda }}</span>
        </div>
        <div class="text-lg font-bold">{{ formatPreco(c) }}</div>
        <div class="flex items-center gap-1 mt-1">
          <svg v-if="(c.variacao || 0) > 0" class="w-3.5 h-3.5" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M5.293 9.707a1 1 0 010-1.414l4-4a1 1 0 011.414 0l4 4a1 1 0 01-1.414 1.414L10 6.414l-3.293 3.293a1 1 0 01-1.414 0z"/></svg>
          <svg v-else-if="(c.variacao || 0) < 0" class="w-3.5 h-3.5" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M14.707 10.293a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 111.414-1.414L10 13.586l3.293-3.293a1 1 0 011.414 0z"/></svg>
          <span class="text-xs font-semibold">{{ formatVariacao(c.variacao) }}</span>
        </div>
      </div>
    </div>

    <div v-if="!loading && cotacoesFiltradas.length === 0" class="text-center py-12 text-gray-400">
      Nenhuma cotação encontrada para esta categoria.
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import cotacaoApi from '../../api/cotacaoApi'

const router = useRouter()
const authStore = useAuthStore()
const cotacoes = ref([])
const loading = ref(true)
const filtroCategoria = ref('todas')

const categorias = [
  { key: 'todas', label: 'Todas' },
  { key: 'crypto', label: 'Crypto' },
  { key: 'forex', label: 'Forex' },
  { key: 'brl', label: 'vs BRL' }
]

const cryptoMoedas = ['BTC', 'ETH', 'BNB', 'XRP', 'SOL', 'ADA', 'DOGE', 'DOT', 'AVAX', 'MATIC', 'LINK', 'UNI', 'LTC']
const forexMoedas = ['USD', 'EUR', 'GBP', 'JPY', 'CHF', 'CAD', 'AUD', 'NZD', 'CNY', 'ARS', 'MXN']

const cotacoesFiltradas = computed(() => {
  let list = cotacoes.value
  if (filtroCategoria.value === 'crypto') {
    list = list.filter(c => cryptoMoedas.includes(c.moeda))
  } else if (filtroCategoria.value === 'forex') {
    list = list.filter(c => forexMoedas.includes(c.moeda))
  } else if (filtroCategoria.value === 'brl') {
    list = list.filter(c => c.parMoeda === 'BRL')
  }
  return list.sort((a, b) => Math.abs(b.variacao || 0) - Math.abs(a.variacao || 0))
})

function tileClass(c) {
  const v = c.variacao || 0
  if (v > 3) return 'bg-emerald-600 text-white border-emerald-700'
  if (v > 1) return 'bg-emerald-500 text-white border-emerald-600'
  if (v > 0) return 'bg-emerald-50 text-emerald-900 border-emerald-200'
  if (v === 0) return 'bg-gray-50 text-gray-700 border-gray-200'
  if (v > -1) return 'bg-red-50 text-red-900 border-red-200'
  if (v > -3) return 'bg-red-500 text-white border-red-600'
  return 'bg-red-600 text-white border-red-700'
}

function formatPreco(c) {
  const preco = c.precoCompra || c.precoVenda || 0
  if (preco >= 1000) return Number(preco).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
  if (preco >= 1) return Number(preco).toLocaleString('pt-BR', { minimumFractionDigits: 4, maximumFractionDigits: 4 })
  return Number(preco).toLocaleString('pt-BR', { minimumFractionDigits: 6, maximumFractionDigits: 8 })
}

function formatVariacao(v) {
  if (v === null || v === undefined) return '0,00%'
  return (v > 0 ? '+' : '') + Number(v).toFixed(2).replace('.', ',') + '%'
}

function irParaDetalhe(c) {
  const role = authStore.user?.role
  const prefix = role === 'Admin' ? '/consultor' : '/cliente'
  router.push(`${prefix}/cotacoes/${c.moeda}/${c.parMoeda}`)
}

onMounted(async () => {
  try {
    const res = await cotacaoApi.listarUltimas()
    cotacoes.value = res.data || []
  } catch (e) { console.error(e) }
  finally { loading.value = false }
})
</script>

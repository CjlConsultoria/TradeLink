<template>
  <div>
    <div class="flex flex-wrap items-center justify-between gap-3 mb-4">
      <h2 class="text-2xl font-bold text-gray-900">Cotações</h2>
      <div class="flex flex-wrap gap-2">
        <button type="button" @click="mostrarFiltros = !mostrarFiltros" class="px-3 sm:px-4 py-2 bg-gray-100 text-gray-700 rounded-lg text-sm hover:bg-gray-200">
          {{ mostrarFiltros ? 'Ocultar filtros' : 'Mostrar filtros' }}
        </button>
        <button type="button" @click="exportarCsv" class="px-3 py-2 bg-emerald-600 text-white rounded-lg text-sm hover:bg-emerald-700 flex items-center gap-1.5">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/></svg>
          <span class="hidden sm:inline">Exportar CSV</span><span class="sm:hidden">CSV</span>
        </button>
        <button type="button" @click="cotacaoStore.forceRefresh(); carregar()" :disabled="loading" class="px-3 sm:px-4 py-2 bg-indigo-600 text-white rounded-lg text-sm hover:bg-indigo-700 disabled:opacity-50">
          <span class="hidden sm:inline">Atualizar todas</span><span class="sm:hidden">Atualizar</span>
        </button>
      </div>
    </div>

    <div v-if="mostrarFiltros" class="bg-white rounded-xl shadow-sm border border-gray-200 p-4 mb-6">
      <p class="text-sm font-semibold text-gray-700 mb-3">Filtros de pesquisa</p>
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3 sm:gap-4">
        <div>
          <label class="block text-xs text-gray-500 mb-0.5">Moeda</label>
          <input v-model="filtros.moeda" type="text" placeholder="ex: USD, BTC" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm" />
        </div>
        <div>
          <label class="block text-xs text-gray-500 mb-0.5">Par</label>
          <input v-model="filtros.parMoeda" type="text" placeholder="ex: BRL, USD" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm" />
        </div>
        <div>
          <label class="block text-xs text-gray-500 mb-0.5">Fonte</label>
          <select v-model="filtros.fonte" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm">
            <option value="">Todas</option>
            <option value="AWESOME_API">Awesome API</option>
            <option value="COINGECKO">CoinGecko</option>
          </select>
        </div>
        <div>
          <label class="block text-xs text-gray-500 mb-0.5">Data de</label>
          <input v-model="filtros.dataDe" type="date" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm" />
        </div>
        <div>
          <label class="block text-xs text-gray-500 mb-0.5">Data até</label>
          <input v-model="filtros.dataAte" type="date" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm" />
        </div>
        <div>
          <label class="block text-xs text-gray-500 mb-0.5">Preço compra mín</label>
          <input v-model.number="filtros.precoCompraMin" type="number" step="any" placeholder="0" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm" />
        </div>
        <div>
          <label class="block text-xs text-gray-500 mb-0.5">Preço compra máx</label>
          <input v-model.number="filtros.precoCompraMax" type="number" step="any" placeholder="0" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm" />
        </div>
        <div>
          <label class="block text-xs text-gray-500 mb-0.5">Preço venda mín</label>
          <input v-model.number="filtros.precoVendaMin" type="number" step="any" placeholder="0" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm" />
        </div>
        <div>
          <label class="block text-xs text-gray-500 mb-0.5">Preço venda máx</label>
          <input v-model.number="filtros.precoVendaMax" type="number" step="any" placeholder="0" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm" />
        </div>
        <div>
          <label class="block text-xs text-gray-500 mb-0.5">Variação mín (%)</label>
          <input v-model.number="filtros.variacaoMin" type="number" step="any" placeholder="-100" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm" />
        </div>
        <div>
          <label class="block text-xs text-gray-500 mb-0.5">Variação máx (%)</label>
          <input v-model.number="filtros.variacaoMax" type="number" step="any" placeholder="100" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm" />
        </div>
        <div>
          <label class="block text-xs text-gray-500 mb-0.5">Máximo mín</label>
          <input v-model.number="filtros.maximoMin" type="number" step="any" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm" />
        </div>
        <div>
          <label class="block text-xs text-gray-500 mb-0.5">Máximo máx</label>
          <input v-model.number="filtros.maximoMax" type="number" step="any" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm" />
        </div>
        <div>
          <label class="block text-xs text-gray-500 mb-0.5">Mínimo mín</label>
          <input v-model.number="filtros.minimoMin" type="number" step="any" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm" />
        </div>
        <div>
          <label class="block text-xs text-gray-500 mb-0.5">Mínimo máx</label>
          <input v-model.number="filtros.minimoMax" type="number" step="any" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm" />
        </div>
        <div>
          <label class="block text-xs text-gray-500 mb-0.5">Ordenar por</label>
          <select v-model="filtros.ordenarPor" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm">
            <option value="dataHora">Data/Hora</option>
            <option value="moeda">Moeda</option>
            <option value="parMoeda">Par</option>
            <option value="precoCompra">Preço compra</option>
            <option value="precoVenda">Preço venda</option>
            <option value="variacao">Variação</option>
            <option value="maximo">Máximo</option>
            <option value="minimo">Mínimo</option>
            <option value="fonte">Fonte</option>
          </select>
        </div>
        <div>
          <label class="block text-xs text-gray-500 mb-0.5">Direção</label>
          <select v-model="filtros.direcao" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm">
            <option value="DESC">Decrescente</option>
            <option value="ASC">Crescente</option>
          </select>
        </div>
        <div>
          <label class="block text-xs text-gray-500 mb-0.5">Itens por página</label>
          <select v-model.number="filtros.size" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm" @change="carregar()">
            <option :value="5">5</option>
            <option :value="10">10</option>
            <option :value="20">20</option>
            <option :value="50">50</option>
            <option :value="100">100</option>
          </select>
        </div>
      </div>
      <div class="flex gap-2 mt-3">
        <button type="button" @click="carregar()" class="px-4 py-2 bg-indigo-600 text-white rounded-lg text-sm hover:bg-indigo-700">Pesquisar</button>
        <button type="button" @click="limparFiltros(); carregar()" class="px-4 py-2 bg-gray-200 text-gray-700 rounded-lg text-sm hover:bg-gray-300">Limpar filtros</button>
      </div>
    </div>

    <LoadingSpinner v-if="loading" />
    <template v-else>
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
        <div class="overflow-x-auto">
          <table class="w-full text-sm table-responsive">
            <thead class="bg-gray-50 border-b border-gray-200">
              <tr>
                <th class="text-left py-3 px-3 font-medium text-gray-500">Moeda</th>
                <th class="text-left py-3 px-3 font-medium text-gray-500">Par</th>
                <th class="text-right py-3 px-3 font-medium text-gray-500">Compra</th>
                <th class="text-right py-3 px-3 font-medium text-gray-500">Venda</th>
                <th class="text-right py-3 px-3 font-medium text-gray-500">Var.%</th>
                <th class="text-right py-3 px-3 font-medium text-gray-500">Máx</th>
                <th class="text-right py-3 px-3 font-medium text-gray-500">Mín</th>
                <th class="text-left py-3 px-3 font-medium text-gray-500">Data/Hora</th>
                <th class="text-left py-3 px-3 font-medium text-gray-500">Fonte</th>
                <th class="w-10"></th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="c in page.content" :key="c.id"
                class="border-b border-gray-100 hover:bg-gray-50 cursor-pointer"
                @click="abrirDetalhe(c)">
                <td class="py-2 px-3 font-medium">{{ c.moeda }}</td>
                <td class="py-2 px-3">{{ c.parMoeda }}</td>
                <td class="py-2 px-3 text-right">{{ formatCurrency(c.precoCompra, c.parMoeda) }}</td>
                <td class="py-2 px-3 text-right">{{ formatCurrency(c.precoVenda, c.parMoeda) }}</td>
                <td class="py-2 px-3 text-right">
                  <span :class="variacaoClass(c.variacao)">{{ c.variacao != null ? formatPercent(c.variacao) : '-' }}</span>
                </td>
                <td class="py-2 px-3 text-right text-gray-600">{{ c.maximo != null ? formatCurrency(c.maximo, c.parMoeda) : '-' }}</td>
                <td class="py-2 px-3 text-right text-gray-600">{{ c.minimo != null ? formatCurrency(c.minimo, c.parMoeda) : '-' }}</td>
                <td class="py-2 px-3 text-gray-600">{{ formatDate(c.dataHora) }}</td>
                <td class="py-2 px-3 text-gray-500">{{ c.fonte }}</td>
                <td class="py-2 px-1" @click.stop>
                  <button type="button" @click="refreshUma(c)" class="text-gray-400 hover:text-indigo-600 p-0.5 rounded" title="Atualizar">↻</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <p v-if="page.content.length === 0" class="p-6 text-gray-500 text-center">Nenhuma cotação encontrada.</p>

        <div v-if="page.totalPages > 0" class="flex flex-wrap items-center justify-between gap-2 px-4 py-3 border-t border-gray-200 bg-gray-50">
          <p class="text-sm text-gray-600">
            {{ page.totalElements }} registro(s) · página {{ page.number + 1 }} de {{ page.totalPages }}
          </p>
          <div class="flex gap-1">
            <button type="button" :disabled="page.first" @click="irPara(0)" class="px-3 py-1.5 rounded border text-sm disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-200">
              Primeira
            </button>
            <button type="button" :disabled="page.first" @click="irPara(page.number - 1)" class="px-3 py-1.5 rounded border text-sm disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-200">
              Anterior
            </button>
            <button type="button" :disabled="page.last" @click="irPara(page.number + 1)" class="px-3 py-1.5 rounded border text-sm disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-200">
              Próxima
            </button>
            <button type="button" :disabled="page.last" @click="irPara(page.totalPages - 1)" class="px-3 py-1.5 rounded border text-sm disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-200">
              Última
            </button>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import cotacaoApi from '../../api/cotacaoApi'
import { useCotacaoStore } from '../../stores/cotacao'
import { formatCurrency, formatPercent, formatDate } from '../../utils/formatters'
import { exportCsv } from '../../utils/exportCsv'
import LoadingSpinner from '../common/LoadingSpinner.vue'

const route = useRoute()
const router = useRouter()
const cotacaoStore = useCotacaoStore()
const basePath = computed(() => route.path.includes('/consultor') ? '/consultor' : '/cliente')
const loading = ref(false)
const mostrarFiltros = ref(true)
const page = ref({
  content: [],
  totalElements: 0,
  totalPages: 0,
  number: 0,
  size: 20,
  first: true,
  last: true
})

const filtros = ref({
  moeda: '',
  parMoeda: '',
  fonte: '',
  dataDe: '',
  dataAte: '',
  precoCompraMin: null,
  precoCompraMax: null,
  precoVendaMin: null,
  precoVendaMax: null,
  variacaoMin: null,
  variacaoMax: null,
  maximoMin: null,
  maximoMax: null,
  minimoMin: null,
  minimoMax: null,
  ordenarPor: 'dataHora',
  direcao: 'DESC',
  size: 20
})

function variacaoClass(v) {
  if (v == null) return ''
  const n = Number(v)
  if (n > 0) return 'text-green-600 font-medium'
  if (n < 0) return 'text-red-600 font-medium'
  return 'text-gray-600'
}

function buildParams(pageNum = 0) {
  const p = { page: pageNum, size: filtros.value.size }
  if (filtros.value.moeda) p.moeda = filtros.value.moeda
  if (filtros.value.parMoeda) p.parMoeda = filtros.value.parMoeda
  if (filtros.value.fonte) p.fonte = filtros.value.fonte
  if (filtros.value.dataDe) p.dataDe = filtros.value.dataDe
  if (filtros.value.dataAte) p.dataAte = filtros.value.dataAte
  if (filtros.value.precoCompraMin != null && filtros.value.precoCompraMin !== '') p.precoCompraMin = filtros.value.precoCompraMin
  if (filtros.value.precoCompraMax != null && filtros.value.precoCompraMax !== '') p.precoCompraMax = filtros.value.precoCompraMax
  if (filtros.value.precoVendaMin != null && filtros.value.precoVendaMin !== '') p.precoVendaMin = filtros.value.precoVendaMin
  if (filtros.value.precoVendaMax != null && filtros.value.precoVendaMax !== '') p.precoVendaMax = filtros.value.precoVendaMax
  if (filtros.value.variacaoMin != null && filtros.value.variacaoMin !== '') p.variacaoMin = filtros.value.variacaoMin
  if (filtros.value.variacaoMax != null && filtros.value.variacaoMax !== '') p.variacaoMax = filtros.value.variacaoMax
  if (filtros.value.maximoMin != null && filtros.value.maximoMin !== '') p.maximoMin = filtros.value.maximoMin
  if (filtros.value.maximoMax != null && filtros.value.maximoMax !== '') p.maximoMax = filtros.value.maximoMax
  if (filtros.value.minimoMin != null && filtros.value.minimoMin !== '') p.minimoMin = filtros.value.minimoMin
  if (filtros.value.minimoMax != null && filtros.value.minimoMax !== '') p.minimoMax = filtros.value.minimoMax
  if (filtros.value.ordenarPor) p.ordenarPor = filtros.value.ordenarPor
  if (filtros.value.direcao) p.direcao = filtros.value.direcao
  return p
}

async function carregar(pageNum = 0) {
  loading.value = true
  try {
    const res = await cotacaoApi.listarPaginado(buildParams(pageNum))
    page.value = res.data
  } catch (e) {
    console.error(e)
    page.value = { content: [], totalElements: 0, totalPages: 0, number: 0, size: filtros.value.size, first: true, last: true }
  } finally {
    loading.value = false
  }
}

function irPara(num) {
  if (num < 0 || num >= page.value.totalPages) return
  carregar(num)
}

function limparFiltros() {
  filtros.value = {
    moeda: '',
    parMoeda: '',
    fonte: '',
    dataDe: '',
    dataAte: '',
    precoCompraMin: null,
    precoCompraMax: null,
    precoVendaMin: null,
    precoVendaMax: null,
    variacaoMin: null,
    variacaoMax: null,
    maximoMin: null,
    maximoMax: null,
    minimoMin: null,
    minimoMax: null,
    ordenarPor: 'dataHora',
    direcao: 'DESC',
    size: filtros.value.size
  }
}

function abrirDetalhe(c) {
  router.push(`${basePath.value}/cotacoes/${encodeURIComponent(c.moeda)}/${encodeURIComponent(c.parMoeda)}`)
}
async function refreshUma(cotacao) {
  try {
    await cotacaoStore.refreshSingle(cotacao.moeda, cotacao.parMoeda)
    carregar(page.value.number)
  } catch (_) {}
}
function exportarCsv() {
  if (!page.value.content.length) return
  const columns = [
    { key: 'moeda', label: 'Moeda' },
    { key: 'parMoeda', label: 'Par' },
    { key: 'precoCompra', label: 'Preço Compra' },
    { key: 'precoVenda', label: 'Preço Venda' },
    { key: 'variacao', label: 'Variação (%)' },
    { key: 'maximo', label: 'Máximo' },
    { key: 'minimo', label: 'Mínimo' },
    { key: 'dataHora', label: 'Data/Hora' },
    { key: 'fonte', label: 'Fonte' }
  ]
  exportCsv(page.value.content, columns, 'cotacoes')
}

const INTERVALO_ATUALIZACAO_MS = 3 * 60 * 1000
let intervalId = null
onMounted(() => {
  carregar()
  intervalId = setInterval(() => carregar(page.value.number), INTERVALO_ATUALIZACAO_MS)
})
onUnmounted(() => {
  if (intervalId) clearInterval(intervalId)
})
</script>

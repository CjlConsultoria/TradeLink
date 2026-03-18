<template>
  <div>
    <h1 class="page-title">Painel de Rebalanceamento</h1>

    <!-- Loading -->
    <div v-if="loading" class="flex justify-center py-12">
      <LoadingSpinner />
    </div>

    <template v-else-if="dados">
      <!-- Resumo Cards -->
      <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
        <div class="card p-4">
          <p class="text-sm text-gray-500">Total Clientes</p>
          <p class="text-2xl font-bold text-gray-900">{{ dados.resumo.totalClientes }}</p>
        </div>
        <div class="card p-4">
          <p class="text-sm text-gray-500">Desbalanceados</p>
          <p class="text-2xl font-bold text-amber-600">{{ dados.resumo.totalDesbalanceados }}</p>
        </div>
        <div class="card p-4">
          <p class="text-sm text-gray-500">Criticos</p>
          <p class="text-2xl font-bold text-red-600">{{ dados.resumo.totalCriticos }}</p>
        </div>
        <div class="card p-4">
          <p class="text-sm text-gray-500">Valor Total</p>
          <p class="text-2xl font-bold text-indigo-600">{{ formatCurrency(dados.resumo.valorTotalGeral, 'USD') }}</p>
        </div>
      </div>

      <!-- Alerta de criticos -->
      <div v-if="dados.resumo.totalCriticos > 0"
        class="mb-6 flex items-center gap-3 p-3 rounded-xl bg-red-50 border border-red-200">
        <span class="text-xl">🚨</span>
        <p class="text-sm text-red-800">
          <strong>{{ dados.resumo.totalCriticos }} cliente(s) em estado critico</strong> — rebalanceamento urgente recomendado.
        </p>
      </div>

      <!-- Filtros -->
      <div class="card p-4 mb-6">
        <div class="flex flex-wrap gap-3 items-end">
          <div class="flex-1 min-w-[180px]">
            <label class="block text-xs font-medium text-gray-500 mb-1">Buscar cliente</label>
            <input v-model="filtros.nome" type="text" placeholder="Nome do cliente..."
              class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm" />
          </div>
          <div class="min-w-[160px]">
            <label class="block text-xs font-medium text-gray-500 mb-1">Carteira</label>
            <select v-model="filtros.carteiraId" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm">
              <option value="">Todas</option>
              <option v-for="c in carteirasUnicas" :key="c.id" :value="c.id">{{ c.nome }}</option>
            </select>
          </div>
          <div class="min-w-[140px]">
            <label class="block text-xs font-medium text-gray-500 mb-1">Status</label>
            <select v-model="filtros.status" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm">
              <option value="">Todos</option>
              <option value="OK">Balanceado</option>
              <option value="ATENCAO">Atencao</option>
              <option value="CRITICO">Critico</option>
            </select>
          </div>
          <div class="min-w-[120px]">
            <label class="block text-xs font-medium text-gray-500 mb-1">Ativo</label>
            <select v-model="filtros.ativo" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm">
              <option value="">Todos</option>
              <option v-for="a in ativosUnicos" :key="a" :value="a">{{ a }}</option>
            </select>
          </div>
          <button v-if="temFiltroAtivo" @click="limparFiltros"
            class="px-3 py-2 text-sm text-gray-500 hover:text-gray-700">Limpar</button>
        </div>
      </div>

      <!-- Barra de acoes em lote -->
      <div v-if="selecionados.size > 0"
        class="card p-3 mb-4 flex flex-wrap items-center gap-3 sm:gap-4 bg-indigo-50 border-indigo-200">
        <span class="text-sm font-medium text-indigo-700">{{ selecionados.size }} selecionado(s)</span>
        <button @click="gerarRecomendacoesLote" :disabled="gerandoLote"
          class="px-4 py-2 bg-indigo-600 text-white rounded-lg text-sm hover:bg-indigo-700 disabled:opacity-50">
          {{ gerandoLote ? 'Gerando...' : 'Gerar Recomendacoes' }}
        </button>
        <button @click="selecionados.clear()" class="text-sm text-gray-500 hover:text-gray-700">Limpar selecao</button>
      </div>

      <!-- Tabs -->
      <div class="flex gap-2 border-b border-gray-200 mb-4">
        <button @click="tab = 'clientes'" :class="tab === 'clientes'
          ? 'bg-indigo-100 text-indigo-800 border-indigo-500'
          : 'bg-white text-gray-600 border-transparent'"
          class="px-4 py-2 rounded-t-lg border-b-2 text-sm font-medium">
          Por Cliente
        </button>
        <button @click="tab = 'ativos'" :class="tab === 'ativos'
          ? 'bg-indigo-100 text-indigo-800 border-indigo-500'
          : 'bg-white text-gray-600 border-transparent'"
          class="px-4 py-2 rounded-t-lg border-b-2 text-sm font-medium">
          Por Ativo
        </button>
      </div>

      <!-- Tab: Por Cliente -->
      <div v-if="tab === 'clientes'">
        <div v-if="clientesFiltrados.length === 0" class="text-center py-8 text-gray-500 text-sm">
          Nenhum cliente encontrado com os filtros aplicados.
        </div>
        <div v-else class="card overflow-hidden">
          <div class="overflow-x-auto">
            <table class="w-full text-sm">
              <thead class="bg-gray-50">
                <tr>
                  <th class="px-3 py-2 text-left w-10">
                    <input type="checkbox" :checked="todosFiltradosSelecionados" @change="toggleSelecionarTodos"
                      class="rounded border-gray-300" />
                  </th>
                  <th class="px-3 py-2 text-left font-medium text-gray-600 cursor-pointer" @click="ordenar('clienteNome')">
                    Cliente {{ sortIcon('clienteNome') }}
                  </th>
                  <th class="px-3 py-2 text-left font-medium text-gray-600 cursor-pointer" @click="ordenar('carteiraNome')">
                    Carteira {{ sortIcon('carteiraNome') }}
                  </th>
                  <th class="px-3 py-2 text-right font-medium text-gray-600 cursor-pointer" @click="ordenar('valorTotalPortfolio')">
                    Valor Total {{ sortIcon('valorTotalPortfolio') }}
                  </th>
                  <th class="px-3 py-2 text-center font-medium text-gray-600 cursor-pointer" @click="ordenar('statusSaude')">
                    Status {{ sortIcon('statusSaude') }}
                  </th>
                  <th class="px-3 py-2 text-center font-medium text-gray-600">Desbalanceados</th>
                  <th class="px-3 py-2 text-right font-medium text-gray-600">Maior Desvio</th>
                  <th class="px-3 py-2 text-center font-medium text-gray-600 w-10"></th>
                </tr>
              </thead>
              <tbody>
                <template v-for="c in clientesFiltrados" :key="c.clienteId + '-' + c.carteiraId">
                  <tr class="border-t hover:bg-gray-50 cursor-pointer"
                    :class="{ 'bg-red-50': c.statusSaude === 'CRITICO', 'bg-amber-50': c.statusSaude === 'ATENCAO' }"
                    @click="toggleExpand(c)">
                    <td class="px-3 py-2" @click.stop>
                      <input type="checkbox" :checked="isSelected(c)" @change="toggleSelecao(c)"
                        class="rounded border-gray-300" />
                    </td>
                    <td class="px-3 py-2 font-medium text-gray-900">{{ c.clienteNome }}</td>
                    <td class="px-3 py-2 text-gray-600">{{ c.carteiraNome }}</td>
                    <td class="px-3 py-2 text-right font-medium">{{ formatCurrency(c.valorTotalPortfolio, 'USD') }}</td>
                    <td class="px-3 py-2 text-center">
                      <HealthBadge :status="c.statusSaude" />
                    </td>
                    <td class="px-3 py-2 text-center">
                      <span v-if="countDesbalanceados(c) > 0" class="text-amber-600 font-medium">{{ countDesbalanceados(c) }}</span>
                      <span v-else class="text-gray-400">0</span>
                    </td>
                    <td class="px-3 py-2 text-right">
                      <span :class="maiorDesvioClass(c)">{{ formatPercent(maiorDesvio(c)) }}</span>
                    </td>
                    <td class="px-3 py-2 text-center text-gray-400">
                      {{ expandedKey === expandKey(c) ? '▲' : '▼' }}
                    </td>
                  </tr>
                  <!-- Detalhe expandido -->
                  <tr v-if="expandedKey === expandKey(c)">
                    <td colspan="8" class="px-4 py-3 bg-gray-50">
                      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <!-- Tabela de ativos -->
                        <div class="overflow-x-auto">
                          <h4 class="text-xs font-semibold text-gray-500 uppercase mb-2">Alocacao</h4>
                          <table class="w-full text-xs min-w-[400px]">
                            <thead>
                              <tr class="text-gray-500">
                                <th class="text-left py-1">Ativo</th>
                                <th class="text-right py-1">Qtd</th>
                                <th class="text-right py-1">% Atual</th>
                                <th class="text-right py-1">% Ideal</th>
                                <th class="text-right py-1">Diff</th>
                                <th class="text-center py-1">Acao</th>
                              </tr>
                            </thead>
                            <tbody>
                              <tr v-for="a in c.ativos" :key="a.simbolo" class="border-t border-gray-200">
                                <td class="py-1 font-medium">{{ a.simbolo }}</td>
                                <td class="py-1 text-right text-gray-600">{{ fmtQtd(a.quantidade) }}</td>
                                <td class="py-1 text-right">{{ Number(a.percentualAtual).toFixed(1) }}%</td>
                                <td class="py-1 text-right text-gray-500">{{ Number(a.percentualAlvo).toFixed(1) }}%</td>
                                <td class="py-1 text-right" :class="diffClass(a.diferencaPercentual)">
                                  {{ formatPercent(a.diferencaPercentual) }}
                                </td>
                                <td class="py-1 text-center">
                                  <span v-if="a.comprar" class="text-xs px-1.5 py-0.5 rounded bg-green-100 text-green-700">Comprar</span>
                                  <span v-else-if="a.vender" class="text-xs px-1.5 py-0.5 rounded bg-red-100 text-red-700">Vender</span>
                                  <span v-else class="text-gray-400">OK</span>
                                </td>
                              </tr>
                            </tbody>
                          </table>
                        </div>
                        <!-- Acoes sugeridas -->
                        <div v-if="c.acoesSugeridas && c.acoesSugeridas.length">
                          <h4 class="text-xs font-semibold text-gray-500 uppercase mb-2">Acoes Sugeridas</h4>
                          <div class="space-y-1">
                            <div v-for="(acao, i) in c.acoesSugeridas" :key="i"
                              class="flex items-center gap-2 text-xs p-2 rounded"
                              :class="acao.tipo === 'COMPRA' ? 'bg-green-50' : 'bg-red-50'">
                              <span class="px-1.5 py-0.5 rounded text-xs font-medium"
                                :class="acao.tipo === 'COMPRA' ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'">
                                {{ acao.tipo }}
                              </span>
                              <span class="text-gray-700">{{ acao.descricao }}</span>
                              <span class="ml-auto text-gray-500">({{ formatCurrency(acao.valorUsd, 'USD') }})</span>
                            </div>
                          </div>
                        </div>
                        <div v-else>
                          <h4 class="text-xs font-semibold text-gray-500 uppercase mb-2">Acoes Sugeridas</h4>
                          <p class="text-xs text-gray-400">Nenhuma acao necessaria — portfolio balanceado.</p>
                        </div>
                      </div>
                    </td>
                  </tr>
                </template>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <!-- Tab: Por Ativo -->
      <div v-if="tab === 'ativos'">
        <div v-if="ativosAgrupados.length === 0" class="text-center py-8 text-gray-500 text-sm">
          Nenhum ativo encontrado com os filtros aplicados.
        </div>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div v-for="grupo in ativosAgrupados" :key="grupo.simbolo" class="card p-4">
            <div class="flex items-center justify-between mb-3">
              <div>
                <h3 class="font-semibold text-gray-900">{{ grupo.simbolo }}</h3>
                <p class="text-xs text-gray-500">{{ grupo.nome }}</p>
              </div>
              <span class="text-sm text-gray-500 font-medium">Alvo: {{ grupo.percentualAlvo.toFixed(1) }}%</span>
            </div>
            <!-- Contadores -->
            <div class="flex gap-3 mb-3 text-xs">
              <span class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full bg-emerald-50 text-emerald-700">
                <span class="w-1.5 h-1.5 rounded-full bg-emerald-500"></span>
                {{ grupo.ok.length }} OK
              </span>
              <span v-if="grupo.comprar.length" class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full bg-green-50 text-green-700">
                <span class="w-1.5 h-1.5 rounded-full bg-green-500"></span>
                {{ grupo.comprar.length }} Comprar
              </span>
              <span v-if="grupo.vender.length" class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full bg-red-50 text-red-700">
                <span class="w-1.5 h-1.5 rounded-full bg-red-500"></span>
                {{ grupo.vender.length }} Vender
              </span>
            </div>
            <!-- Lista de clientes com acao -->
            <div v-if="grupo.comprar.length" class="mb-2">
              <p class="text-xs font-medium text-green-700 mb-1">Comprar:</p>
              <div v-for="item in grupo.comprar" :key="item.clienteId + '-' + item.carteiraId"
                class="flex items-center gap-2 text-xs py-1 px-2 rounded bg-green-50 mb-1">
                <input type="checkbox" :checked="isSelected(item)" @change="toggleSelecao(item)"
                  class="rounded border-gray-300" />
                <span class="font-medium text-gray-800">{{ item.clienteNome }}</span>
                <span class="text-gray-500">({{ item.carteiraNome }})</span>
                <span class="ml-auto text-green-700">+{{ fmtQtd(item.quantidade) }} {{ grupo.simbolo }}</span>
              </div>
            </div>
            <div v-if="grupo.vender.length">
              <p class="text-xs font-medium text-red-700 mb-1">Vender:</p>
              <div v-for="item in grupo.vender" :key="item.clienteId + '-' + item.carteiraId"
                class="flex items-center gap-2 text-xs py-1 px-2 rounded bg-red-50 mb-1">
                <input type="checkbox" :checked="isSelected(item)" @change="toggleSelecao(item)"
                  class="rounded border-gray-300" />
                <span class="font-medium text-gray-800">{{ item.clienteNome }}</span>
                <span class="text-gray-500">({{ item.carteiraNome }})</span>
                <span class="ml-auto text-red-700">-{{ fmtQtd(item.quantidade) }} {{ grupo.simbolo }}</span>
              </div>
            </div>
            <div v-if="!grupo.comprar.length && !grupo.vender.length" class="text-xs text-gray-400">
              Todos os clientes balanceados para {{ grupo.simbolo }}.
            </div>
          </div>
        </div>
      </div>
    </template>

    <!-- Erro -->
    <div v-else-if="erro" class="text-center py-8">
      <p class="text-red-500 text-sm mb-2">{{ erro }}</p>
      <button @click="carregar" class="text-sm text-indigo-600 hover:underline">Tentar novamente</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import alocacaoApi from '../../api/alocacaoApi'
import { formatCurrency, formatPercent } from '../../utils/formatters'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'
import HealthBadge from '../../components/common/HealthBadge.vue'
import { useToast } from '../../composables/useToast'

const toast = useToast()

const route = useRoute()
const loading = ref(false)
const erro = ref(null)
const dados = ref(null)
const tab = ref('clientes')
const expandedKey = ref(null)
const gerandoLote = ref(false)
const selecionados = ref(new Set())

const filtros = ref({
  nome: '',
  carteiraId: '',
  status: '',
  ativo: ''
})

// Inicializar filtro de carteira se veio da query string
onMounted(() => {
  if (route.query.carteira) {
    filtros.value.carteiraId = Number(route.query.carteira)
  }
  carregar()
})

async function carregar() {
  loading.value = true
  erro.value = null
  try {
    const res = await alocacaoApi.analiseConsolidada()
    dados.value = res.data
  } catch (e) {
    erro.value = e.response?.data?.erro || 'Nao foi possivel carregar os dados de rebalanceamento. Verifique se o servidor esta ativo e tente novamente.'
  } finally {
    loading.value = false
  }
}

// Sorting
const sortField = ref('statusSaude')
const sortAsc = ref(true)

function ordenar(field) {
  if (sortField.value === field) {
    sortAsc.value = !sortAsc.value
  } else {
    sortField.value = field
    sortAsc.value = true
  }
}

function sortIcon(field) {
  if (sortField.value !== field) return ''
  return sortAsc.value ? '↑' : '↓'
}

// Computed: carteiras unicas
const carteirasUnicas = computed(() => {
  if (!dados.value) return []
  const map = new Map()
  dados.value.clientes.forEach(c => {
    if (!map.has(c.carteiraId)) map.set(c.carteiraId, { id: c.carteiraId, nome: c.carteiraNome })
  })
  return [...map.values()].sort((a, b) => a.nome.localeCompare(b.nome))
})

// Computed: ativos unicos
const ativosUnicos = computed(() => {
  if (!dados.value) return []
  const set = new Set()
  dados.value.clientes.forEach(c => {
    c.ativos?.forEach(a => {
      if (a.comprar || a.vender) set.add(a.simbolo)
    })
  })
  return [...set].sort()
})

const temFiltroAtivo = computed(() =>
  filtros.value.nome || filtros.value.carteiraId || filtros.value.status || filtros.value.ativo
)

function limparFiltros() {
  filtros.value = { nome: '', carteiraId: '', status: '', ativo: '' }
}

// Computed: clientes filtrados e ordenados
const clientesFiltrados = computed(() => {
  if (!dados.value) return []
  let lista = [...dados.value.clientes]

  if (filtros.value.nome) {
    const q = filtros.value.nome.toLowerCase()
    lista = lista.filter(c => c.clienteNome.toLowerCase().includes(q))
  }
  if (filtros.value.carteiraId) {
    lista = lista.filter(c => c.carteiraId === Number(filtros.value.carteiraId))
  }
  if (filtros.value.status) {
    lista = lista.filter(c => c.statusSaude === filtros.value.status)
  }
  if (filtros.value.ativo) {
    lista = lista.filter(c =>
      c.ativos?.some(a => a.simbolo === filtros.value.ativo && (a.comprar || a.vender))
    )
  }

  // Ordenacao com prioridade de status
  const statusOrder = { CRITICO: 0, ATENCAO: 1, OK: 2 }
  lista.sort((a, b) => {
    let va, vb
    if (sortField.value === 'statusSaude') {
      va = statusOrder[a.statusSaude] ?? 3
      vb = statusOrder[b.statusSaude] ?? 3
    } else if (sortField.value === 'valorTotalPortfolio') {
      va = Number(a.valorTotalPortfolio) || 0
      vb = Number(b.valorTotalPortfolio) || 0
    } else {
      va = (a[sortField.value] || '').toString().toLowerCase()
      vb = (b[sortField.value] || '').toString().toLowerCase()
    }
    if (va < vb) return sortAsc.value ? -1 : 1
    if (va > vb) return sortAsc.value ? 1 : -1
    return 0
  })

  return lista
})

// Tab Ativos: agrupar por simbolo
const ativosAgrupados = computed(() => {
  if (!dados.value) return []
  const map = new Map()

  for (const c of clientesFiltrados.value) {
    for (const a of (c.ativos || [])) {
      if (!map.has(a.simbolo)) {
        map.set(a.simbolo, {
          simbolo: a.simbolo,
          nome: a.nome,
          percentualAlvo: Number(a.percentualAlvo) || 0,
          ok: [],
          comprar: [],
          vender: []
        })
      }
      const grupo = map.get(a.simbolo)
      const item = { clienteId: c.clienteId, carteiraId: c.carteiraId, clienteNome: c.clienteNome, carteiraNome: c.carteiraNome }

      if (a.comprar) {
        const acao = c.acoesSugeridas?.find(ac => ac.simbolo === a.simbolo && ac.tipo === 'COMPRA')
        item.quantidade = acao?.quantidade || 0
        grupo.comprar.push(item)
      } else if (a.vender) {
        const acao = c.acoesSugeridas?.find(ac => ac.simbolo === a.simbolo && ac.tipo === 'VENDA')
        item.quantidade = acao?.quantidade || 0
        grupo.vender.push(item)
      } else {
        grupo.ok.push(item)
      }
    }
  }

  // Filtrar por ativo se ativo selecionado
  let result = [...map.values()]
  if (filtros.value.ativo) {
    result = result.filter(g => g.simbolo === filtros.value.ativo)
  }

  // Ordenar: mais acoes primeiro
  result.sort((a, b) => (b.comprar.length + b.vender.length) - (a.comprar.length + a.vender.length))
  return result
})

// Selection
function expandKey(c) {
  return c.clienteId + '-' + c.carteiraId
}

function toggleExpand(c) {
  const key = expandKey(c)
  expandedKey.value = expandedKey.value === key ? null : key
}

function isSelected(c) {
  return selecionados.value.has(expandKey(c))
}

function toggleSelecao(c) {
  const key = expandKey(c)
  if (selecionados.value.has(key)) {
    selecionados.value.delete(key)
  } else {
    selecionados.value.add(key)
  }
  // Force reactivity
  selecionados.value = new Set(selecionados.value)
}

const todosFiltradosSelecionados = computed(() => {
  if (clientesFiltrados.value.length === 0) return false
  return clientesFiltrados.value.every(c => selecionados.value.has(expandKey(c)))
})

function toggleSelecionarTodos() {
  if (todosFiltradosSelecionados.value) {
    clientesFiltrados.value.forEach(c => selecionados.value.delete(expandKey(c)))
  } else {
    clientesFiltrados.value.forEach(c => selecionados.value.add(expandKey(c)))
  }
  selecionados.value = new Set(selecionados.value)
}

// Batch actions
async function gerarRecomendacoesLote() {
  if (selecionados.value.size === 0) return
  gerandoLote.value = true
  try {
    const items = [...selecionados.value].map(key => {
      const [clienteId, carteiraId] = key.split('-').map(Number)
      return { clienteId, carteiraId }
    })
    const res = await alocacaoApi.gerarRecomendacoesLote(items)
    toast.success(res.data.mensagem || 'Recomendacoes geradas com sucesso!')
    selecionados.value = new Set()
  } catch (e) {
    toast.error(e.response?.data?.erro || 'Nao foi possivel gerar as recomendacoes. Tente novamente.')
  } finally {
    gerandoLote.value = false
  }
}

// Helpers
function countDesbalanceados(c) {
  return (c.ativos || []).filter(a => a.comprar || a.vender).length
}

function maiorDesvio(c) {
  if (!c.ativos || !c.ativos.length) return 0
  return c.ativos.reduce((max, a) => {
    const abs = Math.abs(Number(a.diferencaPercentual) || 0)
    return abs > max ? abs : max
  }, 0)
}

function maiorDesvioClass(c) {
  const desvio = maiorDesvio(c)
  if (desvio > 15) return 'text-red-600 font-medium'
  if (desvio > 5) return 'text-amber-600'
  return 'text-gray-500'
}

function diffClass(val) {
  const n = Number(val) || 0
  if (n > 5) return 'text-red-600'
  if (n < -5) return 'text-green-600'
  return 'text-gray-500'
}

function fmtQtd(v) {
  if (v == null) return '0'
  const n = Number(v)
  if (n < 1) return n.toFixed(8).replace(/0+$/, '').replace(/\.$/, '')
  return n.toFixed(4).replace(/0+$/, '').replace(/\.$/, '')
}
</script>

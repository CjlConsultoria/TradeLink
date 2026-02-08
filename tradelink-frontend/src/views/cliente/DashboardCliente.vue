<template>
  <div>
    <h2 class="page-title">Dashboard</h2>
    <LoadingSpinner v-if="loading" />
    <template v-else>
      <div class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
        <router-link to="/cliente/carteiras" class="card p-6 block hover:border-indigo-300">
          <p class="text-sm text-gray-500">Carteiras</p>
          <p class="text-3xl font-bold text-indigo-600 mt-1">{{ dashboard?.totalCarteiras || 0 }}</p>
        </router-link>
        <button type="button" class="card p-6 text-left w-full cursor-pointer hover:border-amber-300" @click="irParaAba('pendentes')" title="Ver recomendações pendentes">
          <p class="text-sm text-gray-500">Recomendações pendentes</p>
          <p class="text-3xl font-bold text-amber-600 mt-1">{{ dashboard?.totalPendentes ?? dashboard?.totalRecomendacoesAtivas ?? 0 }}</p>
          <p class="text-xs text-amber-600 mt-1 opacity-80">Clique para ver</p>
        </button>
        <button type="button" class="card p-6 text-left w-full cursor-pointer hover:border-green-300" @click="irParaAba('resolvidas')" title="Ver recomendações resolvidas">
          <p class="text-sm text-gray-500">Recomendações resolvidas</p>
          <p class="text-3xl font-bold text-green-600 mt-1">{{ dashboard?.totalResolvidas ?? 0 }}</p>
          <p class="text-xs text-green-600 mt-1 opacity-80">Clique para ver</p>
        </button>
        <router-link to="/cliente/cotacoes" class="card p-6 block hover:border-indigo-300">
          <p class="text-sm text-gray-500">Cotacoes</p>
          <p class="text-3xl font-bold text-blue-600 mt-1">{{ (dashboard?.cotacoesRecentes || []).length }}</p>
        </router-link>
      </div>

      <!-- Alerta: recomendações pendentes -->
      <div v-if="(dashboard?.totalPendentes ?? 0) > 0" class="mb-6 flex items-center gap-4 p-4 rounded-xl bg-amber-50 border-2 border-amber-300 shadow-sm">
        <div class="flex-shrink-0 w-12 h-12 rounded-full bg-amber-400 flex items-center justify-center text-2xl" aria-hidden="true">!</div>
        <div class="flex-1 min-w-0">
          <p class="font-semibold text-amber-900">Você tem {{ dashboard.totalPendentes }} recomendação(ões) pendente(s)</p>
          <p class="text-sm text-amber-800 mt-0.5">Registre suas operações ou marque como resolvida para acompanhar seu progresso.</p>
        </div>
        <button type="button" @click="irParaAba('pendentes')" class="flex-shrink-0 px-4 py-2 bg-amber-500 text-white font-medium rounded-lg hover:bg-amber-600 transition-colors text-sm">
          Ver pendentes
        </button>
      </div>

      <!-- Recomendações primeiro -->
      <div ref="secaoRecomendacoesRef" class="card p-6 mb-8">
        <h3 class="section-title">Recomendações</h3>
        <div class="flex flex-wrap gap-2 border-b border-gray-200 mb-4">
          <button v-for="t in abasRec" :key="t.id" type="button" @click="abaRec = t.id; carregar(0)"
            :class="abaRec === t.id ? 'bg-indigo-100 text-indigo-800 border-indigo-500' : 'bg-white text-gray-600 border-transparent'"
            class="px-4 py-2 rounded-t-lg border-b-2 text-sm font-medium">
            {{ t.label }}
          </button>
        </div>
        <div class="flex flex-wrap gap-3 mb-4">
          <select v-model="filtros.carteiraId" class="px-3 py-2 border border-gray-300 rounded-lg text-sm w-40">
            <option value="">Todas as carteiras</option>
            <option v-for="c in carteiras" :key="c.id" :value="c.id">{{ c.nome }}</option>
          </select>
          <select v-model="filtros.categoria" class="px-3 py-2 border border-gray-300 rounded-lg text-sm w-40">
            <option value="">Todas (tipo)</option>
            <option value="COMPRA">Compra</option>
            <option value="VENDA">Venda</option>
          </select>
          <input v-model="filtros.nome" type="text" placeholder="Buscar por nome (moeda/par)" class="px-3 py-2 border border-gray-300 rounded-lg text-sm w-48" />
          <button type="button" @click="carregar(0)" class="px-3 py-2 bg-gray-100 rounded-lg text-sm hover:bg-gray-200">Filtrar</button>
        </div>
        <div class="space-y-3">
          <div v-for="r in page.content" :key="r.id" class="border rounded-lg p-4" :class="r.resolvido ? 'bg-green-50 border-green-200' : 'border-gray-200'">
            <div class="flex items-center gap-2 mb-2 flex-wrap">
              <TipoBadge :tipo="r.tipo" />
              <span class="font-semibold">{{ r.moeda }}/{{ r.parMoeda }}</span>
              <StatusBadge :status="r.status" />
              <label class="flex items-center gap-1.5 cursor-not-allowed opacity-70" title="Desabilitado">
                <input type="checkbox" :checked="!!r.resolvido" disabled class="rounded border-gray-300 text-green-600" />
                <span class="text-sm text-gray-500">Resolvida</span>
              </label>
              <span class="text-xs text-gray-400 ml-auto">{{ r.carteiraNome }}</span>
            </div>
            <p v-if="r.resolvidoEm" class="text-xs text-gray-500 mb-1">Marcada como resolvida em {{ formatDate(r.resolvidoEm) }}</p>
            <div class="grid grid-cols-2 md:grid-cols-4 gap-2 text-sm">
              <div><span class="text-gray-500">Entrada:</span> {{ formatCurrency(r.precoEntrada) }}</div>
              <div><span class="text-gray-500">Alvo:</span> {{ formatCurrency(r.precoAlvo) }}</div>
              <div><span class="text-gray-500">Atual:</span> <span class="font-medium text-indigo-600">{{ r.cotacaoAtual ? formatCurrency(r.cotacaoAtual) : '-' }}</span></div>
              <div><span class="text-gray-500">Qtd:</span> {{ r.quantidade || '-' }}</div>
            </div>
          </div>
        </div>
        <EmptyState v-if="!loadingRec && page.content.length === 0" :message="abaRec === 'resolvidas' ? 'Nenhuma recomendação resolvida.' : abaRec === 'pendentes' ? 'Nenhuma recomendação pendente.' : 'Nenhuma recomendação.'" />
        <div v-if="page.totalPages > 1" class="flex items-center justify-between mt-3 pt-3 border-t border-gray-200">
          <p class="text-sm text-gray-500">{{ page.totalElements }} resultado(s) · página {{ page.number + 1 }} de {{ page.totalPages }}</p>
          <div class="flex gap-1">
            <button type="button" :disabled="page.first" @click="carregar(page.number - 1)" class="px-3 py-1 rounded border text-sm disabled:opacity-50">Anterior</button>
            <button type="button" :disabled="page.last" @click="carregar(page.number + 1)" class="px-3 py-1 rounded border text-sm disabled:opacity-50">Próxima</button>
          </div>
        </div>
      </div>

      <!-- Cotações depois -->
      <CotacoesDashboardSection titulo="Cotações" />
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import userApi from '../../api/userApi'
import carteiraApi from '../../api/carteiraApi'
import recomendacaoApi from '../../api/recomendacaoApi'
import { formatCurrency, formatDate } from '../../utils/formatters'
import { useToast } from '../../composables/useToast'
import CotacoesDashboardSection from '../../components/cotacao/CotacoesDashboardSection.vue'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'
import EmptyState from '../../components/common/EmptyState.vue'
import StatusBadge from '../../components/common/StatusBadge.vue'
import TipoBadge from '../../components/common/TipoBadge.vue'

const toast = useToast()

const dashboard = ref(null)
const carteiras = ref([])
const loading = ref(true)
const loadingRec = ref(false)
const abasRec = [
  { id: 'todas', label: 'Todas' },
  { id: 'pendentes', label: 'Pendentes' },
  { id: 'resolvidas', label: 'Resolvidas' }
]
const abaRec = ref('pendentes')
const filtros = ref({ carteiraId: '', categoria: '', nome: '' })
const page = ref({ content: [], totalElements: 0, totalPages: 0, number: 0, first: true, last: true })
const TAMANHO_PAGINA = 10

function irParaAba(aba) {
  abaRec.value = aba
  carregar(0)
  nextTick(() => {
    secaoRecomendacoesRef.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
  })
}

const secaoRecomendacoesRef = ref(null)

async function carregar(pageNum = 0) {
  loadingRec.value = true
  try {
    const params = { page: pageNum, size: TAMANHO_PAGINA }
    if (filtros.value.carteiraId) params.carteiraId = filtros.value.carteiraId
    if (filtros.value.categoria) params.tipo = filtros.value.categoria
    if (filtros.value.nome?.trim()) params.nome = filtros.value.nome.trim()
    if (abaRec.value === 'pendentes') params.resolvido = false
    else if (abaRec.value === 'resolvidas') params.resolvido = true
    const res = await recomendacaoApi.listarFiltradoCliente(params)
    page.value = res.data
  } catch (e) {
    console.error(e)
  } finally {
    loadingRec.value = false
  }
}

onMounted(async () => {
  try {
    const [dashboardRes, cartRes] = await Promise.all([
      userApi.dashboard(),
      carteiraApi.listarComoCliente()
    ])
    dashboard.value = dashboardRes.data
    carteiras.value = cartRes.data || []
    await carregar(0)
  } catch (e) { console.error(e) } finally { loading.value = false }
})
</script>

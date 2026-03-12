<template>
  <div>
    <h2 class="page-title">Dashboard</h2>
    <LoadingSpinner v-if="loading" />
    <template v-else>
      <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-4 sm:gap-6 mb-8" data-onboarding="cards">
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

      <!-- Resumo do Portfolio -->
      <div v-if="portfolioResumo && portfolioResumo.ativos?.length" class="card p-6 mb-8" data-onboarding="portfolio">
        <div class="flex items-baseline justify-between mb-3">
          <h3 class="section-title mb-0">Meu Portfolio</h3>
          <router-link to="/cliente/portfolio" class="text-sm text-indigo-600 hover:underline">Ver detalhes</router-link>
        </div>
        <p class="text-2xl font-bold text-gray-900 mb-3">{{ formatCurrency(portfolioResumo.valorTotalPortfolio) }}</p>
        <div class="flex rounded-full h-3 overflow-hidden mb-3">
          <div v-for="a in portfolioResumo.ativos.filter(x => x.percentualAlocacao > 0)" :key="a.id"
            :style="{ width: a.percentualAlocacao + '%', backgroundColor: getColor(a.simbolo) }"
            :title="a.simbolo + ' ' + a.percentualAlocacao + '%'"
            class="transition-all"></div>
        </div>
        <div class="flex flex-wrap gap-3">
          <span v-for="a in portfolioResumo.ativos.filter(x => x.percentualAlocacao > 0)" :key="a.id" class="text-xs text-gray-600 flex items-center gap-1">
            <span class="w-2.5 h-2.5 rounded-full inline-block" :style="{ backgroundColor: getColor(a.simbolo) }"></span>
            {{ a.simbolo }} {{ a.percentualAlocacao }}%
          </span>
        </div>
      </div>

      <!-- Saude das Carteiras -->
      <div v-if="saudeCarteiras.length" class="card p-6 mb-8">
        <h3 class="section-title">Saude das Carteiras</h3>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
          <router-link v-for="s in saudeCarteiras" :key="s.carteiraId" :to="'/cliente/carteiras/' + s.carteiraId"
            class="flex items-center justify-between p-3 border rounded-lg hover:border-indigo-300 transition-colors"
            :class="{
              'border-emerald-200 bg-emerald-50': s.status === 'OK',
              'border-amber-200 bg-amber-50': s.status === 'ATENCAO',
              'border-red-200 bg-red-50': s.status === 'CRITICO'
            }">
            <div class="flex items-center gap-2">
              <span class="w-3 h-3 rounded-full"
                :class="{
                  'bg-emerald-500': s.status === 'OK',
                  'bg-amber-500': s.status === 'ATENCAO',
                  'bg-red-500': s.status === 'CRITICO'
                }"></span>
              <span class="font-medium text-sm text-gray-900">{{ s.carteiraNome }}</span>
            </div>
            <span class="text-xs px-2 py-0.5 rounded-full font-medium"
              :class="{
                'bg-emerald-100 text-emerald-700': s.status === 'OK',
                'bg-amber-100 text-amber-700': s.status === 'ATENCAO',
                'bg-red-100 text-red-700': s.status === 'CRITICO'
              }">
              {{ s.status === 'OK' ? 'Balanceado' : s.status === 'ATENCAO' ? 'Atencao' : 'Desbalanceado' }}
            </span>
          </router-link>
        </div>
      </div>

      <!-- Recomendacoes primeiro -->
      <div ref="secaoRecomendacoesRef" class="card p-6 mb-8" data-onboarding="recomendacoes">
        <h3 class="section-title">Recomendações</h3>
        <div class="flex flex-wrap gap-2 border-b border-gray-200 mb-4">
          <button v-for="t in abasRec" :key="t.id" type="button" @click="abaRec = t.id; carregar(0)"
            :class="abaRec === t.id ? 'bg-indigo-100 text-indigo-800 border-indigo-500' : 'bg-white text-gray-600 border-transparent'"
            class="px-4 py-2 rounded-t-lg border-b-2 text-sm font-medium">
            {{ t.label }}
          </button>
        </div>
        <div class="flex flex-wrap gap-3 mb-4">
          <select v-model="filtros.carteiraId" class="px-3 py-2 border border-gray-300 rounded-lg text-sm w-full sm:w-40">
            <option value="">Todas as carteiras</option>
            <option v-for="c in carteiras" :key="c.id" :value="c.id">{{ c.nome }}</option>
          </select>
          <select v-model="filtros.categoria" class="px-3 py-2 border border-gray-300 rounded-lg text-sm w-full sm:w-40">
            <option value="">Todas (tipo)</option>
            <option value="COMPRA">Compra</option>
            <option value="VENDA">Venda</option>
          </select>
          <input v-model="filtros.nome" type="text" placeholder="Buscar por nome (moeda/par)" class="px-3 py-2 border border-gray-300 rounded-lg text-sm w-full sm:w-48" />
          <button type="button" @click="carregar(0)" class="px-3 py-2 bg-gray-100 rounded-lg text-sm hover:bg-gray-200">Filtrar</button>
        </div>
        <div class="space-y-3">
          <div v-for="r in page.content" :key="r.id" class="border rounded-lg p-4" :class="r.resolvido ? 'bg-green-50 border-green-200' : 'border-gray-200'">
            <div class="flex items-center gap-2 mb-2 flex-wrap">
              <TipoBadge :tipo="r.tipo" />
              <span class="font-semibold">{{ r.moeda }}/{{ r.parMoeda }}</span>
              <StatusBadge :status="r.status" />
              <label class="flex items-center gap-1.5 cursor-pointer select-none" :class="{ 'opacity-70': togglingResolvido === r.id }">
                <input type="checkbox" :checked="!!r.resolvido" :disabled="togglingResolvido === r.id"
                  class="rounded border-gray-300 text-green-600 focus:ring-indigo-500" @change="toggleResolvido(r)" />
                <span class="text-sm text-gray-600">Resolvida</span>
              </label>
              <span class="text-xs text-gray-400 ml-auto">{{ r.carteiraNome }}</span>
            </div>
            <p v-if="r.resolvidoEm" class="text-xs text-gray-500 mb-1">Marcada como resolvida em {{ formatDate(r.resolvidoEm) }}</p>
            <div class="grid grid-cols-2 md:grid-cols-4 gap-2 text-sm">
              <div><span class="text-gray-500">Entrada:</span> {{ formatCurrency(r.precoEntrada) }}</div>
              <div><span class="text-gray-500">Alvo:</span> {{ formatCurrency(r.precoAlvo) }}</div>
              <div><span class="text-gray-500">Atual:</span> <span class="font-medium text-indigo-600">{{ r.cotacaoAtual ? formatCurrency(r.cotacaoAtual) : '-' }}</span></div>
              <div v-if="!r.modoPercentual"><span class="text-gray-500">Qtd:</span> {{ r.quantidade || '-' }}</div>
              <div v-if="r.modoPercentual"><span class="text-gray-500">%:</span> {{ r.percentual }}%</div>
            </div>
            <div v-if="r.modoPercentual && r.quantidadeCalculadaCliente != null" class="mt-2 bg-indigo-50 rounded-lg px-3 py-2 text-sm">
              <span class="text-indigo-700 font-medium">{{ r.percentual }}% de {{ r.moeda }}</span>
              <span class="text-gray-600"> = {{ formatQtd(r.quantidadeCalculadaCliente) }} {{ r.moeda }}</span>
              <span v-if="r.valorEstimadoCliente != null" class="text-gray-500"> (~{{ formatCurrency(r.valorEstimadoCliente) }})</span>
            </div>
            <div class="mt-3 flex gap-2">
              <button type="button" @click="abrirModalOperacao(r)" class="text-sm px-3 py-1.5 bg-indigo-100 text-indigo-700 rounded-lg hover:bg-indigo-200">
                Registrar operação
              </button>
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

      <!-- Modal Registrar operação -->
      <Teleport to="body">
        <div v-if="modalOperacao.visivel" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50" @click.self="fecharModalOperacao">
          <div class="bg-white rounded-xl shadow-xl max-w-md w-full mx-4 p-6">
            <h3 class="text-lg font-semibold mb-4">Registrar operação — {{ modalOperacao.rec?.moeda }}/{{ modalOperacao.rec?.parMoeda }}</h3>
            <form @submit.prevent="enviarOperacao" class="space-y-3">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Tipo</label>
                <select v-model="modalOperacao.tipo" required class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm">
                  <option value="COMPRA">Compra</option>
                  <option value="VENDA">Venda</option>
                </select>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Preço executado</label>
                <input v-model.number="modalOperacao.precoExecutado" type="number" step="any" required class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm" />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Quantidade</label>
                <input v-model.number="modalOperacao.quantidade" type="number" step="any" required class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm" />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Data execução</label>
                <input v-model="modalOperacao.dataExecucao" type="date" required class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm" />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Observação</label>
                <input v-model="modalOperacao.observacao" type="text" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm" placeholder="Opcional" />
              </div>
              <p v-if="modalOperacao.erro" class="text-sm text-red-500">{{ modalOperacao.erro }}</p>
              <div class="flex gap-2 pt-2">
                <button type="submit" :disabled="modalOperacao.salvando" class="px-4 py-2 bg-indigo-600 text-white rounded-lg text-sm hover:bg-indigo-700 disabled:opacity-50">
                  {{ modalOperacao.salvando ? 'Salvando...' : 'Salvar' }}
                </button>
                <button type="button" @click="fecharModalOperacao" class="px-4 py-2 bg-gray-100 rounded-lg text-sm hover:bg-gray-200">Cancelar</button>
              </div>
            </form>
          </div>
        </div>
      </Teleport>
    </template>

    <!-- Onboarding Overlay -->
    <OnboardingOverlay
      :active="onboarding.active.value"
      :step="onboarding.step.value"
      :current-step="onboarding.currentStep.value"
      :total-steps="onboarding.totalSteps.value"
      :is-first="onboarding.isFirst.value"
      :is-last="onboarding.isLast.value"
      @next="onboarding.next()"
      @prev="onboarding.prev()"
      @skip="onboarding.skip()"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import userApi from '../../api/userApi'
import carteiraApi from '../../api/carteiraApi'
import recomendacaoApi from '../../api/recomendacaoApi'
import portfolioApi from '../../api/portfolioApi'
import alocacaoApi from '../../api/alocacaoApi'
import { formatCurrency, formatDate } from '../../utils/formatters'
import { useToast } from '../../composables/useToast'
import { useOnboarding } from '../../composables/useOnboarding'
import CotacoesDashboardSection from '../../components/cotacao/CotacoesDashboardSection.vue'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'
import EmptyState from '../../components/common/EmptyState.vue'
import StatusBadge from '../../components/common/StatusBadge.vue'
import TipoBadge from '../../components/common/TipoBadge.vue'
import OnboardingOverlay from '../../components/common/OnboardingOverlay.vue'
import operacaoApi from '../../api/operacaoApi'

const toast = useToast()

const onboarding = useOnboarding('cliente-dashboard', [
  {
    target: '[data-onboarding="cards"]',
    title: 'Visao geral',
    message: 'Aqui voce ve um resumo rapido: total de carteiras, recomendacoes pendentes, resolvidas e cotacoes recentes.',
    position: 'bottom'
  },
  {
    target: '[data-onboarding="portfolio"]',
    title: 'Seu Portfolio',
    message: 'Acompanhe a composicao do seu portfolio com a barra de alocacao e veja quanto cada ativo representa.',
    position: 'bottom'
  },
  {
    target: '[data-onboarding="recomendacoes"]',
    title: 'Recomendacoes',
    message: 'Aqui ficam as recomendacoes do seu consultor. Voce pode registrar operacoes ou marcar como resolvida.',
    position: 'top'
  }
])

const portfolioResumo = ref(null)
const saudeCarteiras = ref([])
const cores = ['#6366f1','#f59e0b','#10b981','#ef4444','#3b82f6','#8b5cf6','#ec4899','#14b8a6','#f97316','#84cc16','#06b6d4','#e11d48']
function getColor(simbolo) {
  let hash = 0
  for (let i = 0; i < simbolo.length; i++) hash = simbolo.charCodeAt(i) + ((hash << 5) - hash)
  return cores[Math.abs(hash) % cores.length]
}

function formatQtd(v) {
  if (v == null) return '-'
  return Number(v) < 1 ? Number(v).toFixed(8).replace(/0+$/, '').replace(/\.$/, '') : Number(v).toLocaleString('pt-BR', { maximumFractionDigits: 4 })
}

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
const togglingResolvido = ref(null)

const modalOperacao = ref({
  visivel: false,
  rec: null,
  tipo: 'COMPRA',
  precoExecutado: null,
  quantidade: null,
  dataExecucao: '',
  observacao: '',
  salvando: false,
  erro: ''
})

function abrirModalOperacao(r) {
  const hoje = new Date().toISOString().slice(0, 10)
  modalOperacao.value = {
    visivel: true,
    rec: r,
    tipo: 'COMPRA',
    precoExecutado: r.precoEntrada || null,
    quantidade: r.quantidade || null,
    dataExecucao: hoje,
    observacao: '',
    salvando: false,
    erro: ''
  }
}
function fecharModalOperacao() {
  modalOperacao.value.visivel = false
  modalOperacao.value.rec = null
}
async function enviarOperacao() {
  const m = modalOperacao.value
  if (!m.rec) return
  m.erro = ''
  m.salvando = true
  try {
    await operacaoApi.registrar(m.rec.id, {
      tipo: m.tipo,
      precoExecutado: m.precoExecutado,
      quantidade: m.quantidade,
      dataExecucao: m.dataExecucao,
      observacao: m.observacao || null
    })
    toast.success('Operação registrada.')
    fecharModalOperacao()
    carregar(page.value.number)
    if (dashboard.value != null) dashboard.value.totalPendentes = (dashboard.value.totalPendentes ?? 0) - 1
  } catch (e) {
    m.erro = e.response?.data?.mensagem || e.response?.data?.erro || 'Erro ao registrar.'
    toast.error(m.erro)
  } finally {
    m.salvando = false
  }
}

async function toggleResolvido(r) {
  if (togglingResolvido.value === r.id) return
  const novoResolvido = !(r.resolvido === true)
  togglingResolvido.value = r.id
  try {
    const res = await recomendacaoApi.marcarResolvido(r.id, novoResolvido)
    const updated = res?.data ?? res
    if (updated == null) throw new Error('Resposta inválida')
    const idx = page.value.content.findIndex(x => x.id === r.id)
    if (idx !== -1) {
      const newContent = page.value.content.map((item, i) =>
        i === idx ? { ...item, resolvido: !!updated.resolvido, resolvidoEm: updated.resolvidoEm } : item)
      page.value = { ...page.value, content: newContent }
    }
    if (dashboard.value != null) {
      if (updated.resolvido) {
        dashboard.value.totalResolvidas = (dashboard.value.totalResolvidas ?? 0) + 1
        dashboard.value.totalPendentes = Math.max(0, (dashboard.value.totalPendentes ?? 0) - 1)
      } else {
        dashboard.value.totalResolvidas = Math.max(0, (dashboard.value.totalResolvidas ?? 0) - 1)
        dashboard.value.totalPendentes = (dashboard.value.totalPendentes ?? 0) + 1
      }
    }
    toast.success(updated.resolvido ? 'Marcada como resolvida.' : 'Desmarcada como resolvida.')
  } catch (e) {
    console.error(e)
    toast.error(e.response?.data?.mensagem || e.response?.data?.erro || 'Erro ao atualizar.')
  } finally {
    togglingResolvido.value = null
  }
}

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
      carteiraApi.listarComoCliente(),
      portfolioApi.resumo().then(r => { portfolioResumo.value = r.data }).catch(() => {})
    ])
    dashboard.value = dashboardRes.data
    carteiras.value = cartRes.data || []
    await carregar(0)
    // Carregar saude de cada carteira
    for (const c of carteiras.value) {
      alocacaoApi.minhaAlocacao(c.id).then(r => {
        if (r.data?.statusSaude) {
          saudeCarteiras.value = [...saudeCarteiras.value, { carteiraId: c.id, carteiraNome: c.nome, status: r.data.statusSaude }]
        }
      }).catch(() => {})
    }
  } catch (e) { console.error(e) } finally {
    loading.value = false
    onboarding.autoStart(1000)
  }
})
</script>

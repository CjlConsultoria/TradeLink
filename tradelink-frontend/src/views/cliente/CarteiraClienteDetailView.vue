<template>
  <div>
    <div class="flex items-center gap-3 mb-6">
      <router-link to="/cliente/carteiras" class="text-gray-500 hover:text-gray-700"><svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/></svg></router-link>
      <h2 class="text-2xl font-bold text-gray-900">{{ carteira?.nome || 'Carteira' }}</h2>
    </div>
    <LoadingSpinner v-if="loading" />
    <div v-else-if="erro" class="bg-red-50 border border-red-200 rounded-xl p-6 text-red-700">
      <p class="font-medium">{{ erro }}</p>
      <router-link to="/cliente/carteiras" class="inline-block mt-2 text-sm text-indigo-600 hover:underline">Voltar às carteiras</router-link>
    </div>
    <template v-else-if="carteira">
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6 mb-6">
        <p v-if="carteira.descricao" class="text-gray-600 mb-2">{{ carteira.descricao }}</p>
        <p class="text-sm text-gray-500">Consultor: {{ carteira.consultorNome }}</p>
      </div>
      <!-- Saldo -->
      <div class="mb-6">
        <div class="flex items-center justify-between mb-3">
          <h3 class="text-lg font-semibold">Meu Saldo</h3>
          <button @click="showMovModal = true" class="bg-indigo-600 text-white px-3 py-1.5 rounded-lg text-sm font-medium hover:bg-indigo-700">
            Registrar Aporte / Saque
          </button>
        </div>
        <SaldoCard ref="saldoCardRef" :carteira-id="route.params.id" />
      </div>

      <!-- Historico de Movimentacoes -->
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6 mb-6">
        <h3 class="text-lg font-semibold mb-4">Historico de Movimentacoes</h3>
        <HistoricoMovimentacoes ref="historicoRef" :carteira-id="route.params.id" />
      </div>

      <MovimentacaoModal :show="showMovModal" :carteira-id="route.params.id" @close="showMovModal = false" @saved="onMovSaved" />

      <!-- Minha Alocacao -->
      <div v-if="minhaAlocacao" class="bg-white rounded-xl shadow-sm border border-gray-200 p-6 mb-6">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-lg font-semibold">Minha Alocacao</h3>
          <HealthBadge :status="minhaAlocacao.statusSaude" />
        </div>
        <p class="text-sm text-gray-500 mb-3">Valor total: <span class="font-semibold text-gray-900">{{ formatCurrency(minhaAlocacao.valorTotalPortfolio) }}</span></p>
        <AllocationChart v-if="minhaAlocacao.ativos?.length" :ativos="minhaAlocacao.ativos" modo="comparativo" class="mb-4" />
        <div v-if="minhaAlocacao.ativos?.length" class="overflow-x-auto">
          <table class="w-full text-sm">
            <thead class="bg-gray-50">
              <tr>
                <th class="text-left px-3 py-2 font-medium text-gray-600">Ativo</th>
                <th class="text-right px-3 py-2 font-medium text-gray-600">% Atual</th>
                <th class="text-right px-3 py-2 font-medium text-gray-600">% Ideal</th>
                <th class="text-right px-3 py-2 font-medium text-gray-600">Desvio</th>
                <th class="text-center px-3 py-2 font-medium text-gray-600">Status</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="a in minhaAlocacao.ativos" :key="a.simbolo" class="border-t"
                :class="{ 'bg-red-50': a.vender, 'bg-green-50': a.comprar }">
                <td class="px-3 py-2 font-medium">{{ a.simbolo }} <span class="text-gray-400 text-xs">{{ a.nome }}</span></td>
                <td class="px-3 py-2 text-right">{{ Number(a.percentualAtual).toFixed(1) }}%</td>
                <td class="px-3 py-2 text-right text-gray-500">{{ Number(a.percentualAlvo).toFixed(1) }}%</td>
                <td class="px-3 py-2 text-right font-medium"
                  :class="a.diferencaPercentual > 0 ? 'text-green-600' : a.diferencaPercentual < 0 ? 'text-red-600' : 'text-gray-400'">
                  {{ a.diferencaPercentual > 0 ? '+' : '' }}{{ Number(a.diferencaPercentual).toFixed(1) }}%
                </td>
                <td class="px-3 py-2 text-center">
                  <span v-if="a.comprar" class="text-xs bg-green-100 text-green-700 px-2 py-0.5 rounded-full">COMPRAR</span>
                  <span v-else-if="a.vender" class="text-xs bg-red-100 text-red-700 px-2 py-0.5 rounded-full">VENDER</span>
                  <span v-else class="text-xs text-gray-400">OK</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <div v-if="minhaAlocacao.acoesSugeridas?.length" class="mt-4 space-y-2">
          <p class="text-sm font-medium text-gray-700">Sugestoes</p>
          <div v-for="(acao, i) in minhaAlocacao.acoesSugeridas" :key="i"
            class="flex items-center gap-2 text-sm px-3 py-2 rounded-lg"
            :class="acao.tipo === 'COMPRA' ? 'bg-green-50 text-green-800' : 'bg-red-50 text-red-800'">
            <span class="font-medium">{{ acao.tipo }}</span>
            <span>{{ acao.simbolo }}</span>
            <span class="text-xs opacity-75">{{ acao.descricao }}</span>
          </div>
        </div>
      </div>

      <!-- Performance -->
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6 mb-6">
        <h3 class="text-lg font-semibold mb-4">Performance</h3>
        <PerformanceChart :carteira-id="route.params.id" />
      </div>

      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
        <h3 class="text-lg font-semibold mb-4">Recomendações</h3>
        <div class="flex gap-2 border-b border-gray-200 mb-4">
          <button v-for="t in abasRec" :key="t.id" type="button" @click="abaRec = t.id"
            :class="abaRec === t.id ? 'bg-indigo-100 text-indigo-800 border-indigo-500' : 'bg-white text-gray-600 border-transparent'"
            class="px-4 py-2 rounded-t-lg border-b-2 text-sm font-medium">
            {{ t.label }}
          </button>
        </div>
        <div class="space-y-3">
          <div v-for="r in recomendacoesFiltradas" :key="r.id" class="border border-gray-200 rounded-lg p-4" :class="{ 'bg-green-50 border-green-200': r.resolvido }">
            <div class="flex items-center justify-between gap-2 mb-2 flex-wrap">
              <div class="flex items-center gap-2 flex-wrap">
                <TipoBadge :tipo="r.tipo" />
                <span class="font-semibold">{{ r.moeda }}/{{ r.parMoeda }}</span>
                <StatusBadge :status="r.status" />
                <label class="flex items-center gap-1.5 cursor-pointer select-none ml-2" :class="{ 'opacity-70': togglingResolvido === r.id }">
                  <input type="checkbox" :checked="!!r.resolvido" :disabled="togglingResolvido === r.id"
                    class="rounded border-gray-300 text-green-600 focus:ring-indigo-500" @change="toggleResolvido(r)" />
                  <span class="text-sm text-gray-600">Resolvida</span>
                </label>
              </div>
              <button v-if="r.status === 'ATIVA' || r.status === 'EXECUTADA'" @click="abrirOperacao(r.id, r.modoPercentual ? r.quantidadeCalculadaCliente : null)" class="text-sm text-indigo-600 hover:underline">Registrar Operacao</button>
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
            <p v-if="r.observacao" class="text-xs text-gray-500 mt-2">{{ r.observacao }}</p>
            <div v-if="operacoesPorRec[r.id]?.length" class="mt-3 pt-3 border-t border-gray-100">
              <p class="text-xs font-medium text-gray-500 mb-1">Minhas operações</p>
              <OperacaoList :operacoes="operacoesPorRec[r.id]" :show-actions="true" @edit="abrirEdicaoOperacao" @delete="excluirOperacao" />
            </div>
            <p class="text-xs text-gray-400 mt-1">{{ formatDate(r.createdAt) }}</p>
          </div>
        </div>
        <EmptyState v-if="recomendacoesFiltradas.length === 0" :message="abaRec === 'resolvidas' ? 'Nenhuma recomendação resolvida nesta carteira.' : abaRec === 'pendentes' ? 'Nenhuma recomendação pendente.' : 'Nenhuma recomendação nesta carteira.'" />
      </div>

      <OperacaoForm v-if="showOperacaoForm" :show="showOperacaoForm" :operacao="operacaoToEdit" :quantidade-sugerida="quantidadeSugerida" @close="fecharOperacaoForm" @saved="onOperacaoSaved" />
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import carteiraApi from '../../api/carteiraApi'
import recomendacaoApi from '../../api/recomendacaoApi'
import operacaoApi from '../../api/operacaoApi'
import alocacaoApi from '../../api/alocacaoApi'
import { formatCurrency, formatDate } from '../../utils/formatters'
import { useToast } from '../../composables/useToast'
import { useConfirm } from '../../composables/useConfirm'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'
import EmptyState from '../../components/common/EmptyState.vue'
import StatusBadge from '../../components/common/StatusBadge.vue'
import TipoBadge from '../../components/common/TipoBadge.vue'
import OperacaoForm from '../../components/operacao/OperacaoForm.vue'
import OperacaoList from '../../components/operacao/OperacaoList.vue'
import HealthBadge from '../../components/common/HealthBadge.vue'
import AllocationChart from '../../components/rebalanceamento/AllocationChart.vue'
import PerformanceChart from '../../components/rebalanceamento/PerformanceChart.vue'
import SaldoCard from '../../components/rebalanceamento/SaldoCard.vue'
import MovimentacaoModal from '../../components/rebalanceamento/MovimentacaoModal.vue'
import HistoricoMovimentacoes from '../../components/rebalanceamento/HistoricoMovimentacoes.vue'

const route = useRoute()
const toast = useToast()
const { confirm } = useConfirm()
const carteira = ref(null)
const recomendacoes = ref([])
const loading = ref(true)
const erro = ref(null)
const minhaAlocacao = ref(null)
const showMovModal = ref(false)
const saldoCardRef = ref(null)
const historicoRef = ref(null)
const showOperacaoForm = ref(false)
const operacaoRecomendacaoId = ref(null)
const operacaoToEdit = ref(null)
const quantidadeSugerida = ref(null)
const operacoesPorRecomendacao = ref({})
const abasRec = [
  { id: 'todas', label: 'Todas' },
  { id: 'pendentes', label: 'Pendentes' },
  { id: 'resolvidas', label: 'Resolvidas' }
]
const abaRec = ref('pendentes')

const recomendacoesFiltradas = computed(() => {
  const list = recomendacoes.value || []
  if (abaRec.value === 'pendentes') return list.filter(r => !r.resolvido)
  if (abaRec.value === 'resolvidas') return list.filter(r => !!r.resolvido)
  return list
})

const operacoesPorRec = computed(() => operacoesPorRecomendacao.value)
const togglingResolvido = ref(null)

function formatQtd(v) {
  if (v == null) return '-'
  return Number(v) < 1 ? Number(v).toFixed(8).replace(/0+$/, '').replace(/\.$/, '') : Number(v).toLocaleString('pt-BR', { maximumFractionDigits: 4 })
}

async function toggleResolvido(r) {
  if (togglingResolvido.value === r.id) return
  const novoResolvido = !(r.resolvido === true)
  togglingResolvido.value = r.id
  try {
    const res = await recomendacaoApi.marcarResolvido(r.id, novoResolvido)
    const updated = res?.data ?? res
    if (updated == null) throw new Error('Resposta inválida')
    const idx = recomendacoes.value.findIndex(x => x.id === r.id)
    if (idx !== -1) {
      recomendacoes.value = recomendacoes.value.map((item, i) =>
        i === idx ? { ...item, resolvido: !!updated.resolvido, resolvidoEm: updated.resolvidoEm } : item)
    }
    toast.success(updated.resolvido ? 'Marcada como resolvida.' : 'Desmarcada como resolvida.')
  } catch (e) {
    console.error(e)
    toast.error(e.response?.data?.mensagem || e.response?.data?.erro || 'Erro ao atualizar.')
  } finally {
    togglingResolvido.value = null
  }
}

function abrirOperacao(recomendacaoId, qtdSugerida = null) {
  operacaoRecomendacaoId.value = recomendacaoId
  operacaoToEdit.value = null
  quantidadeSugerida.value = qtdSugerida != null ? Number(qtdSugerida) : null
  showOperacaoForm.value = true
}

function abrirEdicaoOperacao(op) {
  operacaoToEdit.value = op
  operacaoRecomendacaoId.value = null
  showOperacaoForm.value = true
}

function fecharOperacaoForm() {
  showOperacaoForm.value = false
  operacaoRecomendacaoId.value = null
  operacaoToEdit.value = null
}

async function onOperacaoSaved(payload) {
  try {
    if (payload.operacaoId) {
      await operacaoApi.atualizar(payload.operacaoId, {
        tipo: payload.tipo,
        precoExecutado: payload.precoExecutado,
        quantidade: payload.quantidade,
        dataExecucao: payload.dataExecucao,
        observacao: payload.observacao
      })
    } else if (operacaoRecomendacaoId.value) {
      await operacaoApi.registrar(operacaoRecomendacaoId.value, payload)
    }
    await carregarOperacoes()
  } catch (e) {
    console.error(e)
  } finally {
    fecharOperacaoForm()
  }
}

async function excluirOperacao(op) {
  const ok = await confirm({ title: 'Excluir operacao', message: 'Excluir esta operacao? Esta acao nao pode ser desfeita.', confirmText: 'Excluir', variant: 'danger' })
  if (!ok) return
  try {
    await operacaoApi.excluir(op.id)
    await carregarOperacoes()
  } catch (e) {
    console.error(e)
  }
}

async function carregarOperacoes() {
  const recIds = recomendacoes.value.map(r => r.id)
  const map = {}
  for (const id of recIds) {
    try {
      const res = await operacaoApi.listarPorRecomendacao(id)
      map[id] = res.data || []
    } catch (_) {
      map[id] = []
    }
  }
  operacoesPorRecomendacao.value = map
}

onMounted(async () => {
  try {
    const [cartRes, recomRes] = await Promise.all([
      carteiraApi.buscarComoCliente(route.params.id),
      recomendacaoApi.listarComoCliente(route.params.id)
    ])
    carteira.value = cartRes.data
    recomendacoes.value = recomRes.data
    await carregarOperacoes()
    alocacaoApi.minhaAlocacao(route.params.id).then(r => { minhaAlocacao.value = r.data }).catch(() => {})
  } catch (e) {
    carteira.value = null
    erro.value = e.response?.status === 403 ? 'Sem acesso a esta carteira.' : e.response?.data?.erro || 'Não foi possível carregar.'
  } finally {
    loading.value = false
  }
})

function onMovSaved() {
  saldoCardRef.value?.carregar()
  historicoRef.value?.carregar()
}
</script>

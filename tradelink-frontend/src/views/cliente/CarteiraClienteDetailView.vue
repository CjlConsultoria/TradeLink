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
            <div class="flex items-center justify-between gap-2 mb-2">
              <div class="flex items-center gap-2 flex-wrap">
                <TipoBadge :tipo="r.tipo" />
                <span class="font-semibold">{{ r.moeda }}/{{ r.parMoeda }}</span>
                <StatusBadge :status="r.status" />
                <label class="flex items-center gap-1.5 cursor-not-allowed opacity-70 ml-2" title="Desabilitado">
                  <input type="checkbox" :checked="!!r.resolvido" disabled class="rounded border-gray-300 text-green-600" />
                  <span class="text-sm text-gray-500">Resolvida</span>
                </label>
              </div>
              <button v-if="r.status === 'ATIVA' || r.status === 'EXECUTADA'" @click="abrirOperacao(r.id)" class="text-sm text-indigo-600 hover:underline">Registrar Operação</button>
            </div>
            <p v-if="r.resolvidoEm" class="text-xs text-gray-500 mb-1">Marcada como resolvida em {{ formatDate(r.resolvidoEm) }}</p>
            <div class="grid grid-cols-2 md:grid-cols-4 gap-2 text-sm">
              <div><span class="text-gray-500">Entrada:</span> {{ formatCurrency(r.precoEntrada) }}</div>
              <div><span class="text-gray-500">Alvo:</span> {{ formatCurrency(r.precoAlvo) }}</div>
              <div><span class="text-gray-500">Atual:</span> <span class="font-medium text-indigo-600">{{ r.cotacaoAtual ? formatCurrency(r.cotacaoAtual) : '-' }}</span></div>
              <div><span class="text-gray-500">Qtd:</span> {{ r.quantidade || '-' }}</div>
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

      <OperacaoForm v-if="showOperacaoForm" :show="showOperacaoForm" :operacao="operacaoToEdit" @close="fecharOperacaoForm" @saved="onOperacaoSaved" />
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import carteiraApi from '../../api/carteiraApi'
import recomendacaoApi from '../../api/recomendacaoApi'
import operacaoApi from '../../api/operacaoApi'
import { formatCurrency, formatDate } from '../../utils/formatters'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'
import EmptyState from '../../components/common/EmptyState.vue'
import StatusBadge from '../../components/common/StatusBadge.vue'
import TipoBadge from '../../components/common/TipoBadge.vue'
import OperacaoForm from '../../components/operacao/OperacaoForm.vue'
import OperacaoList from '../../components/operacao/OperacaoList.vue'

const route = useRoute()
const carteira = ref(null)
const recomendacoes = ref([])
const loading = ref(true)
const erro = ref(null)
const showOperacaoForm = ref(false)
const operacaoRecomendacaoId = ref(null)
const operacaoToEdit = ref(null)
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

function abrirOperacao(recomendacaoId) {
  operacaoRecomendacaoId.value = recomendacaoId
  operacaoToEdit.value = null
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
  if (!confirm('Excluir esta operação? Esta ação não pode ser desfeita.')) return
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
  } catch (e) {
    carteira.value = null
    erro.value = e.response?.status === 403 ? 'Sem acesso a esta carteira.' : e.response?.data?.erro || 'Não foi possível carregar.'
  } finally {
    loading.value = false
  }
})
</script>

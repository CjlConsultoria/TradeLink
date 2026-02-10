<template>
  <div>
    <div class="flex items-center gap-3 mb-6">
      <router-link to="/consultor/carteiras" class="text-gray-500 hover:text-gray-700"><svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/></svg></router-link>
      <h2 class="text-2xl font-bold text-gray-900">{{ carteira?.nome || 'Carteira' }}</h2>
    </div>
    <LoadingSpinner v-if="loading" />
    <div v-else-if="erro" class="bg-red-50 border border-red-200 rounded-xl p-6 text-red-700">
      <p class="font-medium">{{ erro }}</p>
      <router-link to="/consultor/carteiras" class="inline-block mt-2 text-sm text-indigo-600 hover:underline">Voltar às carteiras</router-link>
    </div>
    <template v-else-if="carteira">
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6 mb-6 flex flex-wrap items-start justify-between gap-4">
        <div>
          <p v-if="carteira.descricao" class="text-gray-600 mb-3">{{ carteira.descricao }}</p>
          <div class="flex gap-6 text-sm text-gray-500"><span>{{ carteira.totalClientes }} clientes</span><span>{{ carteira.totalRecomendacoes }} recomendacoes</span></div>
        </div>
        <button v-if="carteira.totalClientes === 0" @click="excluirCarteira" class="px-4 py-2 text-sm text-red-600 border border-red-300 rounded-lg hover:bg-red-50">
          Excluir carteira
        </button>
      </div>
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6 mb-6">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-lg font-semibold">Clientes</h3>
          <button @click="showClienteForm = !showClienteForm" class="text-sm text-indigo-600 hover:underline">Atribuir Cliente</button>
        </div>
        <div v-if="showClienteForm" class="bg-gray-50 rounded-lg p-4 mb-4">
          <div class="flex gap-3 items-end">
            <select v-model="selectedClienteId" class="flex-1 px-3 py-2 border border-gray-300 rounded-lg text-sm">
              <option value="">Selecione um cliente...</option>
              <option v-for="c in availableClientes" :key="c.id" :value="c.id">{{ c.nome }} ({{ c.email }})</option>
            </select>
            <button @click="atribuirCliente" :disabled="!selectedClienteId" class="bg-indigo-600 text-white px-4 py-2 rounded-lg text-sm hover:bg-indigo-700 disabled:opacity-50">Atribuir</button>
          </div>
        </div>
        <div class="space-y-2">
          <div v-for="c in clientesDaCarteira" :key="c.id" class="flex items-center justify-between py-2 px-3 bg-gray-50 rounded-lg">
            <div><p class="font-medium text-sm">{{ c.nome }}</p><p class="text-xs text-gray-500">{{ c.email }}</p></div>
            <button @click="removerCliente(c.id)" class="text-red-600 text-xs hover:underline">Remover</button>
          </div>
        </div>
      </div>
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-lg font-semibold">Recomendacoes</h3>
          <button @click="showRecomForm = true" class="bg-indigo-600 text-white px-3 py-1.5 rounded-lg text-sm font-medium hover:bg-indigo-700">Nova Recomendacao</button>
        </div>
        <RecomendacaoModal ref="recomModalRef" :show="showRecomForm" :carteira-id="carteira?.id" :carteiras="carteirasParaModal" @close="showRecomForm = false" @saved="onRecomSaved" @criar-carteira="onCriarCarteira" />
        <div class="space-y-3">
          <div v-for="r in recomendacoes" :key="r.id" class="border border-gray-200 rounded-lg p-4">
            <div class="flex items-center justify-between mb-2">
              <div class="flex items-center gap-2">
                <TipoBadge :tipo="r.tipo" />
                <span class="font-semibold">{{ r.moeda }}/{{ r.parMoeda }}</span>
                <StatusBadge :status="r.status" />
                <span v-if="r.totalOperacoesClientes > 0" class="text-xs bg-gray-100 text-gray-700 px-1.5 py-0.5 rounded">{{ r.totalOperacoesClientes }} op.</span>
                <button v-if="r.totalOperacoesClientes > 0" @click="toggleOperacoes(r.id)" class="text-xs text-indigo-600 hover:underline">{{ operacoesAbertas[r.id] ? 'Ocultar' : 'Ver' }} operações</button>
              </div>
              <div v-if="r.status === 'ATIVA'" class="flex gap-2">
                <button @click="executarRecom(r.id)" class="text-xs text-blue-600 hover:underline">Executar</button>
                <button @click="cancelarRecom(r.id)" class="text-xs text-red-600 hover:underline">Cancelar</button>
              </div>
            </div>
            <div class="grid grid-cols-2 md:grid-cols-4 gap-2 text-sm">
              <div><span class="text-gray-500">Entrada:</span> {{ formatCurrency(r.precoEntrada) }}</div>
              <div><span class="text-gray-500">Alvo:</span> {{ formatCurrency(r.precoAlvo) }}</div>
              <div><span class="text-gray-500">Atual:</span> <span :class="r.cotacaoAtual ? 'font-medium text-indigo-600' : ''">{{ r.cotacaoAtual ? formatCurrency(r.cotacaoAtual) : '-' }}</span></div>
              <div><span class="text-gray-500">Qtd:</span> {{ r.quantidade || '-' }}</div>
            </div>
            <div v-if="operacoesAbertas[r.id] && operacoesPorRec[r.id]" class="mt-3 pt-3 border-t border-gray-100">
              <p class="text-xs font-medium text-gray-500 mb-1">Operações dos clientes</p>
              <OperacaoList :operacoes="operacoesPorRec[r.id]" :show-actions="false" />
            </div>
            <p v-if="r.observacao" class="text-xs text-gray-500 mt-2">{{ r.observacao }}</p>
            <p class="text-xs text-gray-400 mt-1">{{ formatDate(r.createdAt) }}</p>
          </div>
        </div>
        <EmptyState v-if="recomendacoes.length === 0" message="Nenhuma recomendacao" />
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import carteiraApi from '../../api/carteiraApi'
import recomendacaoApi from '../../api/recomendacaoApi'
import userApi from '../../api/userApi'
import { formatCurrency, formatDate } from '../../utils/formatters'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'
import EmptyState from '../../components/common/EmptyState.vue'
import StatusBadge from '../../components/common/StatusBadge.vue'
import TipoBadge from '../../components/common/TipoBadge.vue'
import RecomendacaoModal from '../../components/recomendacao/RecomendacaoModal.vue'
import OperacaoList from '../../components/operacao/OperacaoList.vue'
import operacaoApi from '../../api/operacaoApi'

const route = useRoute()
const router = useRouter()
const recomModalRef = ref(null)
const carteirasParaModal = ref([])
const operacoesAbertas = ref({})
const operacoesPorRecomendacao = ref({})
const operacoesPorRec = computed(() => operacoesPorRecomendacao.value)

async function toggleOperacoes(recomendacaoId) {
  if (operacoesAbertas.value[recomendacaoId]) {
    operacoesAbertas.value = { ...operacoesAbertas.value, [recomendacaoId]: false }
    return
  }
  try {
    const res = await operacaoApi.listarComoConsultor(recomendacaoId)
    operacoesPorRecomendacao.value = { ...operacoesPorRecomendacao.value, [recomendacaoId]: res.data || [] }
    operacoesAbertas.value = { ...operacoesAbertas.value, [recomendacaoId]: true }
  } catch (_) {}
}
const carteiraId = route.params.id
const carteira = ref(null)
const clientesDaCarteira = ref([])
const recomendacoes = ref([])
const loading = ref(true)
const erro = ref(null)
const showClienteForm = ref(false)
const selectedClienteId = ref('')
const availableClientes = ref([])
const showRecomForm = ref(false)

async function loadData() {
  loading.value = true
  erro.value = null
  try {
    const [cartRes, cliRes, recomRes, allCliRes, carteirasRes] = await Promise.all([
      carteiraApi.buscar(carteiraId), carteiraApi.listarClientes(carteiraId),
      recomendacaoApi.listarPorCarteira(carteiraId), userApi.listarClientes(),
      carteiraApi.listar()
    ])
    carteira.value = cartRes.data
    clientesDaCarteira.value = cliRes.data
    recomendacoes.value = recomRes.data
    carteirasParaModal.value = carteirasRes.data || []
    const assignedIds = new Set(cliRes.data.map(c => c.id))
    availableClientes.value = allCliRes.data.filter(c => !assignedIds.has(c.id))
  } catch (e) {
    carteira.value = null
    erro.value = e.response?.status === 403 ? 'Sem acesso a esta carteira.' : e.response?.data?.erro || 'Não foi possível carregar os detalhes.'
  } finally {
    loading.value = false
  }
}

async function onCriarCarteira(payload) {
  try {
    const res = await carteiraApi.criar({ nome: payload.nome, descricao: payload.descricao })
    const nova = res.data
    carteirasParaModal.value = [...carteirasParaModal.value, nova]
    recomModalRef.value?.usarCarteiraCriada(nova.id)
  } catch (e) {
    console.error(e)
  }
}
async function atribuirCliente() { if (!selectedClienteId.value) return; await carteiraApi.atribuirCliente(carteiraId, selectedClienteId.value); selectedClienteId.value = ''; loadData() }
async function removerCliente(id) { await carteiraApi.removerCliente(carteiraId, id); loadData() }
async function onRecomSaved(payload) { try { await recomendacaoApi.criar(payload.carteiraId, { tipo: payload.tipo, moeda: payload.moeda, parMoeda: payload.parMoeda, precoEntrada: payload.precoEntrada, precoAlvo: payload.precoAlvo, stopLoss: payload.stopLoss, quantidade: payload.quantidade, observacao: payload.observacao }); showRecomForm.value = false; loadData() } catch (e) { console.error(e) } }
async function executarRecom(id) { await recomendacaoApi.executar(id); loadData() }
async function cancelarRecom(id) { await recomendacaoApi.cancelar(id); loadData() }

async function excluirCarteira() {
  if (!confirm('Excluir esta carteira? Esta ação não pode ser desfeita.')) return
  try {
    await carteiraApi.excluir(carteiraId)
    router.push('/consultor/carteiras')
  } catch (e) {
    alert(e.response?.data?.erro || e.response?.data?.mensagem || 'Erro ao excluir.')
  }
}
onMounted(loadData)
</script>

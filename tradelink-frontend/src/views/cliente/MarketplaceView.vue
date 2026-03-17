<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold text-white">Marketplace de Consultores</h1>
        <p class="text-slate-400 text-sm mt-1">Encontre um consultor para orientar seus investimentos</p>
      </div>
    </div>

    <!-- Busca -->
    <div class="bg-slate-800/50 rounded-xl border border-slate-700/50 p-4">
      <div class="relative">
        <input
          v-model="termo"
          @input="debounceBuscar"
          type="text"
          placeholder="Buscar por nome ou especializacao..."
          class="w-full bg-slate-700/50 text-white rounded-lg pl-10 pr-4 py-3 text-sm border border-slate-600/50 focus:border-indigo-500 focus:outline-none"
        />
        <span class="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400">&#128269;</span>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="flex justify-center py-12">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-400"></div>
    </div>

    <!-- Consultores Grid -->
    <div v-else-if="consultores.length" class="grid sm:grid-cols-2 lg:grid-cols-3 gap-4">
      <div
        v-for="c in consultores"
        :key="c.empresaId"
        class="bg-slate-800/50 rounded-xl border border-slate-700/50 p-5 hover:border-indigo-500/30 transition-all"
      >
        <div class="flex items-center gap-3 mb-3">
          <div class="w-12 h-12 bg-indigo-500/20 rounded-xl flex items-center justify-center overflow-hidden">
            <img v-if="c.marketplaceFotoUrl" :src="c.marketplaceFotoUrl" class="w-full h-full object-cover rounded-xl" />
            <span v-else class="text-2xl">&#128100;</span>
          </div>
          <div>
            <h3 class="text-white font-semibold text-sm">{{ c.nome }}</h3>
            <p v-if="c.marketplaceEspecializacao" class="text-indigo-400 text-xs">{{ c.marketplaceEspecializacao }}</p>
          </div>
        </div>

        <p v-if="c.marketplaceDescricao" class="text-slate-300 text-xs mb-3 line-clamp-3">{{ c.marketplaceDescricao }}</p>

        <div v-if="c.marketplaceExperiencia" class="flex items-center gap-1 text-slate-400 text-xs mb-3">
          <span>&#128197;</span> {{ c.marketplaceExperiencia }}
        </div>

        <div class="border-t border-slate-700/50 pt-3 flex items-center justify-between">
          <div>
            <span class="text-xl font-bold text-white">R$ {{ formatPreco(c.marketplacePrecoBase) }}</span>
            <span class="text-slate-400 text-xs">/mes</span>
          </div>
          <button
            @click="abrirSolicitacao(c)"
            class="bg-indigo-600 text-white px-4 py-2 rounded-lg text-xs font-medium hover:bg-indigo-700 transition-colors"
          >
            Solicitar
          </button>
        </div>
      </div>
    </div>

    <!-- Empty -->
    <div v-else class="text-center py-12">
      <span class="text-4xl mb-4 block">&#128269;</span>
      <p class="text-slate-300 font-medium">Nenhum consultor encontrado</p>
      <p class="text-slate-500 text-sm mt-1">Tente uma busca diferente</p>
    </div>

    <!-- Minhas Solicitacoes -->
    <div v-if="solicitacoes.length" class="mt-8">
      <h2 class="text-lg font-semibold text-white mb-4">Minhas Solicitacoes</h2>
      <div class="space-y-3">
        <div
          v-for="s in solicitacoes"
          :key="s.id"
          class="bg-slate-800/50 rounded-xl border border-slate-700/50 p-4"
        >
          <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
            <div>
              <h3 class="text-white font-medium text-sm">{{ s.empresaNome }}</h3>
              <p class="text-slate-400 text-xs mt-1">
                Enviada em {{ formatDate(s.createdAt) }}
              </p>
            </div>
            <div class="flex items-center gap-2">
              <span :class="statusClass(s.status)" class="px-3 py-1 rounded-full text-xs font-medium">
                {{ statusLabel(s.status) }}
              </span>
              <template v-if="s.status === 'ACEITA'">
                <span class="text-white text-sm font-semibold">R$ {{ formatPreco(s.precoFinal) }}/mes</span>
                <button
                  @click="iniciarPagamento(s.id)"
                  :disabled="checkoutLoading"
                  class="bg-green-600 text-white px-3 py-1 rounded-lg text-xs font-medium hover:bg-green-700 transition-colors"
                >
                  {{ checkoutLoading ? 'Processando...' : 'Pagar' }}
                </button>
              </template>
              <button
                v-if="s.status === 'PENDENTE'"
                @click="cancelar(s.id)"
                class="text-red-400 hover:text-red-300 text-xs"
              >
                Cancelar
              </button>
            </div>
          </div>
          <p v-if="s.mensagemConsultor" class="text-slate-300 text-xs mt-2 italic">
            "{{ s.mensagemConsultor }}"
          </p>
        </div>
      </div>
    </div>

    <!-- Modal Solicitar Mentoria -->
    <div v-if="showModal" class="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 flex items-center justify-center p-4" @click.self="showModal = false">
      <div class="bg-slate-800 rounded-2xl border border-slate-700 p-6 w-full max-w-md">
        <h3 class="text-lg font-semibold text-white mb-4">Solicitar Mentoria</h3>
        <p class="text-slate-300 text-sm mb-4">
          Consultor: <strong class="text-white">{{ selectedConsultor?.nome }}</strong>
          <br />
          Valor mensal: <strong class="text-indigo-400">R$ {{ formatPreco(selectedConsultor?.marketplacePrecoBase) }}</strong>
        </p>
        <textarea
          v-model="mensagem"
          placeholder="Deixe uma mensagem para o consultor (opcional)..."
          rows="3"
          class="w-full bg-slate-700/50 text-white rounded-lg p-3 text-sm border border-slate-600/50 focus:border-indigo-500 focus:outline-none mb-4"
        ></textarea>
        <div class="flex gap-3">
          <button @click="showModal = false" class="flex-1 bg-slate-700 text-slate-300 py-2 rounded-lg text-sm hover:bg-slate-600 transition-colors">
            Cancelar
          </button>
          <button
            @click="enviarSolicitacao"
            :disabled="solicitando"
            class="flex-1 bg-indigo-600 text-white py-2 rounded-lg text-sm font-medium hover:bg-indigo-700 disabled:opacity-50 transition-colors"
          >
            {{ solicitando ? 'Enviando...' : 'Enviar Solicitacao' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import {
  listarConsultoresPublico,
  solicitarMentoria,
  listarMinhasSolicitacoes,
  criarCheckoutMarketplace,
  confirmarPagamentoMarketplace,
  cancelarSolicitacao
} from '../../api/marketplaceApi'
import { useToast } from '../../composables/useToast'

const toast = useToast()
const loading = ref(true)
const consultores = ref([])
const solicitacoes = ref([])
const termo = ref('')
const showModal = ref(false)
const selectedConsultor = ref(null)
const mensagem = ref('')
const solicitando = ref(false)
const checkoutLoading = ref(false)

let debounceTimer = null

onMounted(async () => {
  await carregarDados()
  // Check for payment return
  const params = new URLSearchParams(window.location.search)
  const sessionId = params.get('session_id')
  if (sessionId) {
    try {
      await confirmarPagamentoMarketplace(sessionId)
      toast.success('Pagamento confirmado! Voce esta vinculado ao consultor.')
      window.history.replaceState({}, '', window.location.pathname)
    } catch {
      // ignore
    }
    await carregarSolicitacoes()
  }
})

async function carregarDados() {
  loading.value = true
  try {
    const [res1, res2] = await Promise.all([
      listarConsultoresPublico(termo.value || null),
      listarMinhasSolicitacoes().catch(() => ({ data: [] }))
    ])
    consultores.value = res1.data
    solicitacoes.value = res2.data
  } catch (e) {
    toast.error('Erro ao carregar marketplace')
  } finally {
    loading.value = false
  }
}

async function carregarSolicitacoes() {
  try {
    const res = await listarMinhasSolicitacoes()
    solicitacoes.value = res.data
  } catch {}
}

function debounceBuscar() {
  clearTimeout(debounceTimer)
  debounceTimer = setTimeout(() => carregarDados(), 400)
}

function abrirSolicitacao(consultor) {
  selectedConsultor.value = consultor
  mensagem.value = ''
  showModal.value = true
}

async function enviarSolicitacao() {
  solicitando.value = true
  try {
    await solicitarMentoria({
      empresaId: selectedConsultor.value.empresaId,
      mensagemCliente: mensagem.value || null
    })
    toast.success('Solicitacao enviada com sucesso!')
    showModal.value = false
    await carregarSolicitacoes()
  } catch (e) {
    toast.error(e.response?.data?.message || 'Erro ao enviar solicitacao')
  } finally {
    solicitando.value = false
  }
}

async function iniciarPagamento(solicitacaoId) {
  checkoutLoading.value = true
  try {
    const res = await criarCheckoutMarketplace(solicitacaoId)
    if (res.data.checkoutUrl) {
      window.location.href = res.data.checkoutUrl
    }
  } catch (e) {
    toast.error(e.response?.data?.message || 'Erro ao iniciar pagamento')
    checkoutLoading.value = false
  }
}

async function cancelar(solicitacaoId) {
  if (!confirm('Cancelar esta solicitacao?')) return
  try {
    await cancelarSolicitacao(solicitacaoId)
    toast.success('Solicitacao cancelada')
    await carregarSolicitacoes()
  } catch (e) {
    toast.error(e.response?.data?.message || 'Erro ao cancelar')
  }
}

function formatPreco(v) {
  if (!v) return '0,00'
  return Number(v).toFixed(2).replace('.', ',')
}

function formatDate(d) {
  if (!d) return ''
  return new Date(d).toLocaleDateString('pt-BR')
}

function statusLabel(s) {
  const m = { PENDENTE: 'Pendente', ACEITA: 'Aceita', PAGA: 'Ativa', RECUSADA: 'Recusada', CANCELADA: 'Cancelada' }
  return m[s] || s
}

function statusClass(s) {
  const m = {
    PENDENTE: 'bg-yellow-500/20 text-yellow-400',
    ACEITA: 'bg-blue-500/20 text-blue-400',
    PAGA: 'bg-green-500/20 text-green-400',
    RECUSADA: 'bg-red-500/20 text-red-400',
    CANCELADA: 'bg-slate-500/20 text-slate-400'
  }
  return m[s] || 'bg-slate-500/20 text-slate-400'
}
</script>

<template>
  <div>
    <h2 class="page-title">Marketplace de Consultores</h2>
    <p class="text-sm -mt-4 mb-6" style="color: rgb(var(--tl-text-muted));">Encontre um consultor para orientar seus investimentos</p>

    <!-- Busca -->
    <div class="card p-4 mb-6">
      <div class="relative">
        <svg class="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2" style="color: rgb(var(--tl-text-muted));" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/>
        </svg>
        <input
          v-model="termo"
          @input="debounceBuscar"
          type="text"
          placeholder="Buscar por nome ou especializacao..."
          class="input-base pl-10"
        />
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="flex justify-center py-16">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-500"></div>
    </div>

    <!-- Consultores Grid -->
    <div v-else-if="consultores.length" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 mb-8">
      <div
        v-for="c in consultores"
        :key="c.empresaId"
        class="card p-5"
      >
        <div class="flex items-center gap-3 mb-3">
          <div class="w-11 h-11 bg-indigo-100 rounded-xl flex items-center justify-center shrink-0">
            <span class="text-sm font-bold text-indigo-600">{{ getInitials(c.nome) }}</span>
          </div>
          <div class="min-w-0">
            <h3 class="font-semibold text-sm truncate" style="color: rgb(var(--tl-text));">{{ c.nome }}</h3>
            <p v-if="c.marketplaceEspecializacao" class="text-indigo-600 text-xs truncate">{{ c.marketplaceEspecializacao }}</p>
          </div>
        </div>

        <p v-if="c.marketplaceDescricao" class="text-xs mb-3 line-clamp-3" style="color: rgb(var(--tl-text-muted));">{{ c.marketplaceDescricao }}</p>

        <div class="space-y-1 mb-3">
          <div v-if="c.marketplaceExperiencia" class="flex items-center gap-1.5 text-xs" style="color: rgb(var(--tl-text-muted));">
            <svg class="w-3.5 h-3.5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 13.255A23.931 23.931 0 0112 15c-3.183 0-6.22-.62-9-1.745M16 6V4a2 2 0 00-2-2h-4a2 2 0 00-2 2v2m4 6h.01M5 20h14a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"/>
            </svg>
            {{ c.marketplaceExperiencia }}
          </div>
          <a v-if="c.marketplaceRedeSocial" :href="c.marketplaceRedeSocial" target="_blank" rel="noopener"
            class="flex items-center gap-1.5 text-indigo-600 hover:text-indigo-700 text-xs transition-colors">
            <svg class="w-3.5 h-3.5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1"/>
            </svg>
            <span class="truncate">{{ formatRedeSocial(c.marketplaceRedeSocial) }}</span>
          </a>
        </div>

        <div class="pt-3 flex items-center justify-between" style="border-top: 1px solid rgb(var(--tl-border));">
          <div>
            <span class="text-xl font-bold" style="color: rgb(var(--tl-text));">R$ {{ formatPreco(c.marketplacePrecoBase) }}</span>
            <span class="text-xs" style="color: rgb(var(--tl-text-muted));">/mes</span>
          </div>
          <button
            @click="abrirSolicitacao(c)"
            class="btn-primary text-sm px-4 py-2"
          >
            Solicitar
          </button>
        </div>
      </div>
    </div>

    <!-- Empty -->
    <div v-else class="card p-12 text-center mb-8">
      <svg class="w-12 h-12 mx-auto mb-3" style="color: rgb(var(--tl-border));" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/>
      </svg>
      <p class="font-medium" style="color: rgb(var(--tl-text));">Nenhum consultor encontrado</p>
      <p class="text-sm mt-1" style="color: rgb(var(--tl-text-muted));">Tente uma busca diferente</p>
    </div>

    <!-- Minhas Solicitacoes -->
    <div v-if="solicitacoes.length">
      <h3 class="section-title">Minhas Solicitacoes</h3>
      <div class="space-y-3">
        <div
          v-for="s in solicitacoes"
          :key="s.id"
          class="card p-4 sm:p-5"
        >
          <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
            <div>
              <h4 class="font-semibold text-sm" style="color: rgb(var(--tl-text));">{{ s.empresaNome }}</h4>
              <p class="text-xs mt-0.5" style="color: rgb(var(--tl-text-muted));">
                Enviada em {{ formatDate(s.createdAt) }}
              </p>
            </div>
            <div class="flex items-center gap-2 flex-wrap">
              <span :class="statusClass(s.status)" class="px-3 py-1 rounded-full text-xs font-medium">
                {{ statusLabel(s.status) }}
              </span>
              <template v-if="s.status === 'ACEITA'">
                <span class="text-sm font-bold" style="color: rgb(var(--tl-text));">R$ {{ formatPreco(s.precoFinal) }}/mes</span>
                <button
                  @click="iniciarPagamento(s.id)"
                  :disabled="checkoutLoading"
                  class="px-3 py-1.5 rounded-lg text-xs font-medium bg-green-600 text-white hover:bg-green-700 transition-colors disabled:opacity-50"
                >
                  {{ checkoutLoading ? 'Processando...' : 'Pagar' }}
                </button>
              </template>
              <button
                v-if="s.status === 'PENDENTE'"
                @click="cancelar(s.id)"
                class="text-red-600 hover:text-red-700 text-xs font-medium"
              >
                Cancelar
              </button>
            </div>
          </div>
          <p v-if="s.mensagemConsultor" class="text-xs mt-2 italic" style="color: rgb(var(--tl-text-muted));">
            "{{ s.mensagemConsultor }}"
          </p>
        </div>
      </div>
    </div>

    <!-- Modal Solicitar Mentoria -->
    <div v-if="showModal" class="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div class="fixed inset-0 bg-black/50" @click="showModal = false"></div>
      <div class="rounded-xl shadow-xl p-6 w-full max-w-md relative z-10" style="background: rgb(var(--tl-surface));">
        <h3 class="text-lg font-semibold mb-4" style="color: rgb(var(--tl-text));">Solicitar Mentoria</h3>
        <div class="text-sm mb-4" style="color: rgb(var(--tl-text-muted));">
          <p>Consultor: <strong style="color: rgb(var(--tl-text));">{{ selectedConsultor?.nome }}</strong></p>
          <p class="mt-1">Valor mensal: <strong class="text-indigo-600">R$ {{ formatPreco(selectedConsultor?.marketplacePrecoBase) }}</strong></p>
        </div>
        <textarea
          v-model="mensagem"
          placeholder="Deixe uma mensagem para o consultor (opcional)..."
          rows="3"
          class="input-base resize-none mb-4"
        ></textarea>
        <div class="flex flex-col sm:flex-row gap-3">
          <button @click="showModal = false" class="flex-1 px-4 py-2.5 rounded-lg text-sm font-medium transition-colors" style="border: 1px solid rgb(var(--tl-border)); color: rgb(var(--tl-text));">
            Cancelar
          </button>
          <button
            @click="enviarSolicitacao"
            :disabled="solicitando"
            class="flex-1 btn-primary text-sm"
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
  const params = new URLSearchParams(window.location.search)
  const sessionId = params.get('session_id')
  if (sessionId) {
    try {
      await confirmarPagamentoMarketplace(sessionId)
      toast.success('Pagamento confirmado! Voce esta vinculado ao consultor.')
      window.history.replaceState({}, '', window.location.pathname)
    } catch {}
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
  } catch {
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

function getInitials(nome) {
  if (!nome) return '?'
  return nome.split(' ').map(w => w[0]).filter(Boolean).slice(0, 2).join('').toUpperCase()
}

function formatRedeSocial(url) {
  if (!url) return ''
  try {
    const u = new URL(url)
    return u.hostname.replace('www.', '') + u.pathname.replace(/\/$/, '')
  } catch {
    return url
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
    PENDENTE: 'bg-amber-100 text-amber-800',
    ACEITA: 'bg-blue-100 text-blue-800',
    PAGA: 'bg-green-100 text-green-800',
    RECUSADA: 'bg-red-100 text-red-800',
    CANCELADA: 'bg-gray-100 text-gray-600'
  }
  return m[s] || 'bg-gray-100 text-gray-600'
}
</script>

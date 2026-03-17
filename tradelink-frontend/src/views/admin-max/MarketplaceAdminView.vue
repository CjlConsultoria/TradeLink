<template>
  <div class="space-y-6">
    <div>
      <h1 class="text-2xl font-bold text-white">Marketplace</h1>
      <p class="text-slate-400 text-sm mt-1">Gestao completa do marketplace de consultores</p>
    </div>

    <!-- Stats Cards -->
    <div v-if="stats" class="grid grid-cols-2 lg:grid-cols-4 gap-4">
      <div class="bg-slate-800/50 rounded-xl border border-slate-700/50 p-4">
        <p class="text-slate-400 text-xs">Consultores Ativos</p>
        <p class="text-2xl font-bold text-white mt-1">{{ stats.totalConsultores }}</p>
      </div>
      <div class="bg-slate-800/50 rounded-xl border border-slate-700/50 p-4">
        <p class="text-slate-400 text-xs">Solicitacoes Pendentes</p>
        <p class="text-2xl font-bold text-yellow-400 mt-1">{{ stats.solicitacoesPendentes }}</p>
      </div>
      <div class="bg-slate-800/50 rounded-xl border border-slate-700/50 p-4">
        <p class="text-slate-400 text-xs">Receita Bruta</p>
        <p class="text-2xl font-bold text-green-400 mt-1">R$ {{ formatPreco(stats.receitaBruta) }}</p>
      </div>
      <div class="bg-slate-800/50 rounded-xl border border-slate-700/50 p-4">
        <p class="text-slate-400 text-xs">Taxa Acumulada ({{ stats.taxaPercentual }}%)</p>
        <p class="text-2xl font-bold text-indigo-400 mt-1">R$ {{ formatPreco(stats.taxaAcumulada) }}</p>
      </div>
    </div>

    <!-- Tabs -->
    <div class="flex gap-1 bg-slate-800/50 rounded-xl p-1 border border-slate-700/50">
      <button
        v-for="t in tabs"
        :key="t.key"
        @click="activeTab = t.key"
        :class="activeTab === t.key ? 'bg-indigo-600 text-white' : 'text-slate-400 hover:text-white'"
        class="flex-1 py-2 px-3 rounded-lg text-xs font-medium transition-colors"
      >
        {{ t.label }}
      </button>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="flex justify-center py-12">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-400"></div>
    </div>

    <!-- Tab: Consultores -->
    <div v-else-if="activeTab === 'consultores'" class="space-y-3">
      <div v-if="!consultores.length" class="text-center py-8 text-slate-400">Nenhum consultor com perfil marketplace</div>
      <div
        v-for="c in consultores"
        :key="c.empresaId"
        class="bg-slate-800/50 rounded-xl border border-slate-700/50 p-4"
      >
        <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
          <div>
            <h3 class="text-white font-semibold text-sm">{{ c.nome }}</h3>
            <p class="text-slate-400 text-xs">{{ c.marketplaceEspecializacao || 'Sem especializacao' }}</p>
            <p class="text-slate-500 text-xs mt-1">Preco: R$ {{ formatPreco(c.marketplacePrecoBase) }}/mes</p>
          </div>
          <div class="flex items-center gap-2">
            <span :class="c.marketplaceVisivel ? 'bg-green-500/20 text-green-400' : 'bg-slate-500/20 text-slate-400'" class="px-2 py-1 rounded-full text-xs font-medium">
              {{ c.marketplaceVisivel ? 'Visivel' : 'Invisivel' }}
            </span>
            <button
              @click="toggleVisibilidade(c)"
              class="bg-slate-700 text-slate-300 px-3 py-1 rounded-lg text-xs hover:bg-slate-600 transition-colors"
            >
              {{ c.marketplaceVisivel ? 'Ocultar' : 'Mostrar' }}
            </button>
            <button
              @click="removerDoMarketplace(c.empresaId)"
              class="bg-red-600/20 text-red-400 px-3 py-1 rounded-lg text-xs hover:bg-red-600/30 transition-colors"
            >
              Remover
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Tab: Solicitacoes -->
    <div v-else-if="activeTab === 'solicitacoes'" class="space-y-3">
      <!-- Filter -->
      <div class="flex gap-2 flex-wrap">
        <button
          v-for="f in statusFilters"
          :key="f.value"
          @click="filtroStatus = f.value; carregarSolicitacoes()"
          :class="filtroStatus === f.value ? 'bg-indigo-600 text-white' : 'bg-slate-700 text-slate-300'"
          class="px-3 py-1 rounded-lg text-xs font-medium transition-colors"
        >
          {{ f.label }}
        </button>
      </div>
      <div v-if="!solicitacoesAdmin.length" class="text-center py-8 text-slate-400">Nenhuma solicitacao encontrada</div>
      <div
        v-for="s in solicitacoesAdmin"
        :key="s.id"
        class="bg-slate-800/50 rounded-xl border border-slate-700/50 p-4"
      >
        <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
          <div>
            <p class="text-white text-sm"><strong>{{ s.clienteNome }}</strong> &#8594; {{ s.empresaNome }}</p>
            <p class="text-slate-400 text-xs mt-1">{{ formatDate(s.createdAt) }}</p>
            <p v-if="s.precoFinal" class="text-slate-300 text-xs">Preco: R$ {{ formatPreco(s.precoFinal) }}</p>
          </div>
          <div class="flex items-center gap-2">
            <span :class="statusClass(s.status)" class="px-2 py-1 rounded-full text-xs font-medium">
              {{ statusLabel(s.status) }}
            </span>
            <select
              v-if="s.status !== 'PAGA'"
              @change="alterarStatus(s.id, $event.target.value); $event.target.value = ''"
              class="bg-slate-700 text-slate-300 rounded-lg text-xs px-2 py-1 border border-slate-600"
            >
              <option value="">Alterar...</option>
              <option value="PENDENTE">Pendente</option>
              <option value="ACEITA">Aceita</option>
              <option value="RECUSADA">Recusada</option>
              <option value="CANCELADA">Cancelada</option>
            </select>
          </div>
        </div>
      </div>
    </div>

    <!-- Tab: Configuracoes -->
    <div v-else-if="activeTab === 'config'" class="space-y-4">
      <div class="bg-slate-800/50 rounded-xl border border-slate-700/50 p-5">
        <h2 class="text-white font-semibold mb-4">Taxa da Plataforma</h2>
        <div class="flex items-end gap-4">
          <div>
            <label class="text-slate-300 text-xs font-medium mb-1 block">Percentual (%)</label>
            <input
              v-model.number="novaTaxa"
              type="number"
              step="0.5"
              min="0"
              max="100"
              class="bg-slate-700/50 text-white rounded-lg p-3 text-sm border border-slate-600/50 focus:border-indigo-500 focus:outline-none w-32"
            />
          </div>
          <button
            @click="salvarTaxa"
            :disabled="salvandoTaxa"
            class="bg-indigo-600 text-white px-6 py-3 rounded-lg text-sm font-medium hover:bg-indigo-700 disabled:opacity-50 transition-colors"
          >
            {{ salvandoTaxa ? 'Salvando...' : 'Salvar Taxa' }}
          </button>
        </div>
        <p class="text-slate-500 text-xs mt-3">Esta taxa e aplicada sobre o valor pago pelo cliente no marketplace. O consultor recebe o valor restante.</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import {
  adminListarConsultores, adminAlterarVisibilidade, adminRemoverDoMarketplace,
  adminListarSolicitacoes, adminAlterarStatusSolicitacao,
  adminGetStats, adminGetTaxa, adminAlterarTaxa
} from '../../api/marketplaceApi'
import { useToast } from '../../composables/useToast'

const toast = useToast()
const loading = ref(true)
const activeTab = ref('consultores')
const tabs = [
  { key: 'consultores', label: 'Consultores' },
  { key: 'solicitacoes', label: 'Solicitacoes' },
  { key: 'config', label: 'Configuracoes' }
]
const statusFilters = [
  { value: '', label: 'Todas' },
  { value: 'PENDENTE', label: 'Pendentes' },
  { value: 'ACEITA', label: 'Aceitas' },
  { value: 'PAGA', label: 'Pagas' },
  { value: 'RECUSADA', label: 'Recusadas' },
  { value: 'CANCELADA', label: 'Canceladas' }
]

const stats = ref(null)
const consultores = ref([])
const solicitacoesAdmin = ref([])
const filtroStatus = ref('')
const novaTaxa = ref(15)
const salvandoTaxa = ref(false)

onMounted(async () => {
  await carregarTudo()
})

async function carregarTudo() {
  loading.value = true
  try {
    const [statsRes, consultoresRes, solicitacoesRes, taxaRes] = await Promise.all([
      adminGetStats(),
      adminListarConsultores(),
      adminListarSolicitacoes(),
      adminGetTaxa()
    ])
    stats.value = statsRes.data
    consultores.value = consultoresRes.data
    solicitacoesAdmin.value = solicitacoesRes.data
    novaTaxa.value = taxaRes.data.taxa
  } catch {
    toast.error('Erro ao carregar dados do marketplace')
  } finally {
    loading.value = false
  }
}

async function carregarSolicitacoes() {
  try {
    const res = await adminListarSolicitacoes(filtroStatus.value || null)
    solicitacoesAdmin.value = res.data
  } catch {}
}

async function toggleVisibilidade(c) {
  try {
    await adminAlterarVisibilidade(c.empresaId, !c.marketplaceVisivel)
    c.marketplaceVisivel = !c.marketplaceVisivel
    toast.success('Visibilidade atualizada')
  } catch {
    toast.error('Erro ao alterar visibilidade')
  }
}

async function removerDoMarketplace(empresaId) {
  if (!confirm('Remover este consultor do marketplace completamente?')) return
  try {
    await adminRemoverDoMarketplace(empresaId)
    toast.success('Consultor removido do marketplace')
    const res = await adminListarConsultores()
    consultores.value = res.data
  } catch {
    toast.error('Erro ao remover')
  }
}

async function alterarStatus(id, status) {
  if (!status) return
  try {
    await adminAlterarStatusSolicitacao(id, status)
    toast.success('Status atualizado')
    await carregarSolicitacoes()
  } catch {
    toast.error('Erro ao alterar status')
  }
}

async function salvarTaxa() {
  salvandoTaxa.value = true
  try {
    await adminAlterarTaxa(novaTaxa.value)
    toast.success('Taxa atualizada!')
    const res = await adminGetStats()
    stats.value = res.data
  } catch (e) {
    toast.error(e.response?.data?.error || 'Erro ao salvar taxa')
  } finally {
    salvandoTaxa.value = false
  }
}

function formatPreco(v) {
  if (!v && v !== 0) return '0,00'
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

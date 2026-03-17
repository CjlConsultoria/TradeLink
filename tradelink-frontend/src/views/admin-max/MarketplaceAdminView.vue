<template>
  <div>
    <h2 class="page-title">Marketplace</h2>
    <p class="text-sm -mt-4 mb-6" style="color: rgb(var(--tl-text-muted));">Gestao completa do marketplace de consultores</p>

    <!-- Stats Cards -->
    <div v-if="stats" class="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
      <div class="card p-4">
        <p class="text-xs font-medium" style="color: rgb(var(--tl-text-muted));">Consultores Ativos</p>
        <p class="text-2xl font-bold mt-1" style="color: rgb(var(--tl-text));">{{ stats.totalConsultores }}</p>
      </div>
      <div class="card p-4">
        <p class="text-xs font-medium" style="color: rgb(var(--tl-text-muted));">Solicitacoes Pendentes</p>
        <p class="text-2xl font-bold mt-1 text-amber-500">{{ stats.solicitacoesPendentes }}</p>
      </div>
      <div class="card p-4">
        <p class="text-xs font-medium" style="color: rgb(var(--tl-text-muted));">Receita Bruta</p>
        <p class="text-2xl font-bold mt-1 text-green-600">R$ {{ formatPreco(stats.receitaBruta) }}</p>
      </div>
      <div class="card p-4">
        <p class="text-xs font-medium" style="color: rgb(var(--tl-text-muted));">Taxa Acumulada ({{ stats.taxaPercentual }}%)</p>
        <p class="text-2xl font-bold mt-1 text-indigo-600">R$ {{ formatPreco(stats.taxaAcumulada) }}</p>
      </div>
    </div>

    <!-- Tabs -->
    <div class="card p-1 mb-6">
      <div class="flex gap-1">
        <button
          v-for="t in tabs"
          :key="t.key"
          @click="activeTab = t.key"
          class="flex-1 py-2.5 px-3 rounded-lg text-sm font-medium transition-colors"
          :style="activeTab === t.key
            ? 'background: rgb(var(--tl-primary)); color: white;'
            : 'color: rgb(var(--tl-text-muted));'"
          @mouseenter="activeTab !== t.key && ($event.target.style.color = 'rgb(var(--tl-text))')"
          @mouseleave="activeTab !== t.key && ($event.target.style.color = 'rgb(var(--tl-text-muted))')"
        >
          {{ t.icon }} {{ t.label }}
        </button>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="flex justify-center py-16">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-500"></div>
    </div>

    <!-- Tab: Consultores -->
    <div v-else-if="activeTab === 'consultores'" class="space-y-3">
      <div v-if="!consultores.length" class="card p-12 text-center">
        <svg class="w-12 h-12 mx-auto mb-3" style="color: rgb(var(--tl-border));" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z"/>
        </svg>
        <p class="font-medium" style="color: rgb(var(--tl-text));">Nenhum consultor com perfil marketplace</p>
        <p class="text-sm mt-1" style="color: rgb(var(--tl-text-muted));">Consultores aparecerao aqui ao ativar o marketplace</p>
      </div>

      <div
        v-for="c in consultores"
        :key="c.empresaId"
        class="card p-4 sm:p-5"
      >
        <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
          <div class="flex items-center gap-3 min-w-0">
            <div class="w-10 h-10 bg-indigo-100 rounded-xl flex items-center justify-center shrink-0">
              <span class="text-sm font-bold text-indigo-600">{{ getInitials(c.nome) }}</span>
            </div>
            <div class="min-w-0">
              <h3 class="font-semibold text-sm truncate" style="color: rgb(var(--tl-text));">{{ c.nome }}</h3>
              <p class="text-xs truncate" style="color: rgb(var(--tl-text-muted));">{{ c.marketplaceEspecializacao || 'Sem especializacao' }}</p>
              <p class="text-xs mt-0.5" style="color: rgb(var(--tl-text-muted));">
                Preco: <strong style="color: rgb(var(--tl-text));">R$ {{ formatPreco(c.marketplacePrecoBase) }}/mes</strong>
              </p>
            </div>
          </div>
          <div class="flex items-center gap-2 flex-wrap">
            <span
              :class="c.marketplaceVisivel ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-600'"
              class="px-2.5 py-0.5 rounded-full text-xs font-medium"
            >
              {{ c.marketplaceVisivel ? 'Visivel' : 'Invisivel' }}
            </span>
            <button
              @click="toggleVisibilidade(c)"
              class="px-3 py-1.5 rounded-lg text-xs font-medium transition-colors"
              style="border: 1px solid rgb(var(--tl-border)); color: rgb(var(--tl-text));"
            >
              {{ c.marketplaceVisivel ? 'Ocultar' : 'Mostrar' }}
            </button>
            <button
              @click="removerDoMarketplace(c.empresaId)"
              class="px-3 py-1.5 rounded-lg text-xs font-medium bg-red-50 text-red-600 hover:bg-red-100 transition-colors"
            >
              Remover
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Tab: Solicitacoes -->
    <div v-else-if="activeTab === 'solicitacoes'" class="space-y-4">
      <!-- Status Filter -->
      <div class="flex gap-2 flex-wrap">
        <button
          v-for="f in statusFilters"
          :key="f.value"
          @click="filtroStatus = f.value; carregarSolicitacoes()"
          class="px-3 py-1.5 rounded-lg text-xs font-medium transition-colors"
          :style="filtroStatus === f.value
            ? 'background: rgb(var(--tl-primary)); color: white;'
            : 'border: 1px solid rgb(var(--tl-border)); color: rgb(var(--tl-text-muted));'"
        >
          {{ f.label }}
        </button>
      </div>

      <div v-if="!solicitacoesAdmin.length" class="card p-12 text-center">
        <svg class="w-12 h-12 mx-auto mb-3" style="color: rgb(var(--tl-border));" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"/>
        </svg>
        <p class="font-medium" style="color: rgb(var(--tl-text));">Nenhuma solicitacao encontrada</p>
        <p class="text-sm mt-1" style="color: rgb(var(--tl-text-muted));">{{ filtroStatus ? 'Tente outro filtro de status' : 'Solicitacoes aparecerao aqui' }}</p>
      </div>

      <!-- Solicitacoes Table for desktop, cards for mobile -->
      <div v-else class="card overflow-hidden">
        <div class="overflow-x-auto">
          <table class="w-full table-responsive">
            <thead>
              <tr style="border-bottom: 1px solid rgb(var(--tl-border));">
                <th class="text-left text-xs font-semibold uppercase tracking-wider py-3 px-4" style="color: rgb(var(--tl-text-muted));">Cliente</th>
                <th class="text-left text-xs font-semibold uppercase tracking-wider py-3 px-4" style="color: rgb(var(--tl-text-muted));">Consultor</th>
                <th class="text-left text-xs font-semibold uppercase tracking-wider py-3 px-4" style="color: rgb(var(--tl-text-muted));">Preco</th>
                <th class="text-left text-xs font-semibold uppercase tracking-wider py-3 px-4" style="color: rgb(var(--tl-text-muted));">Data</th>
                <th class="text-left text-xs font-semibold uppercase tracking-wider py-3 px-4" style="color: rgb(var(--tl-text-muted));">Status</th>
                <th class="text-right text-xs font-semibold uppercase tracking-wider py-3 px-4" style="color: rgb(var(--tl-text-muted));">Acao</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="s in solicitacoesAdmin"
                :key="s.id"
                style="border-bottom: 1px solid rgb(var(--tl-border));"
                class="last:border-0"
              >
                <td class="py-3 px-4">
                  <p class="text-sm font-medium" style="color: rgb(var(--tl-text));">{{ s.clienteNome || s.clienteEmail }}</p>
                  <p class="text-xs" style="color: rgb(var(--tl-text-muted));">{{ s.clienteEmail }}</p>
                </td>
                <td class="py-3 px-4">
                  <p class="text-sm" style="color: rgb(var(--tl-text));">{{ s.empresaNome }}</p>
                </td>
                <td class="py-3 px-4">
                  <p v-if="s.precoFinal" class="text-sm font-semibold" style="color: rgb(var(--tl-text));">R$ {{ formatPreco(s.precoFinal) }}</p>
                  <p v-else class="text-xs" style="color: rgb(var(--tl-text-muted));">-</p>
                </td>
                <td class="py-3 px-4">
                  <p class="text-xs" style="color: rgb(var(--tl-text-muted));">{{ formatDate(s.createdAt) }}</p>
                </td>
                <td class="py-3 px-4">
                  <span :class="statusClass(s.status)" class="px-2.5 py-0.5 rounded-full text-xs font-medium">
                    {{ statusLabel(s.status) }}
                  </span>
                </td>
                <td class="py-3 px-4 text-right">
                  <select
                    v-if="s.status !== 'PAGA'"
                    @change="alterarStatus(s.id, $event.target.value); $event.target.value = ''"
                    class="input-base text-xs py-1 px-2 w-auto"
                    style="width: auto; min-width: 7rem;"
                  >
                    <option value="">Alterar...</option>
                    <option value="PENDENTE">Pendente</option>
                    <option value="ACEITA">Aceita</option>
                    <option value="RECUSADA">Recusada</option>
                    <option value="CANCELADA">Cancelada</option>
                  </select>
                  <span v-else class="text-xs" style="color: rgb(var(--tl-text-muted));">—</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Tab: Configuracoes -->
    <div v-else-if="activeTab === 'config'" class="space-y-4">
      <div class="card p-5 sm:p-6">
        <h3 class="section-title">Taxa da Plataforma</h3>
        <p class="text-sm mb-4" style="color: rgb(var(--tl-text-muted));">
          Esta taxa e aplicada sobre o valor pago pelo cliente no marketplace. O consultor recebe o valor restante.
        </p>
        <div class="flex items-end gap-4">
          <div>
            <label class="text-xs font-semibold block mb-1" style="color: rgb(var(--tl-text));">Percentual (%)</label>
            <input
              v-model.number="novaTaxa"
              type="number"
              step="0.5"
              min="0"
              max="100"
              class="input-base text-sm"
              style="width: 8rem;"
            />
          </div>
          <button
            @click="salvarTaxa"
            :disabled="salvandoTaxa"
            class="btn-primary text-sm"
          >
            {{ salvandoTaxa ? 'Salvando...' : 'Salvar Taxa' }}
          </button>
        </div>

        <!-- Preview da taxa -->
        <div v-if="stats" class="mt-6 p-4 rounded-lg" style="background: rgb(var(--tl-surface-alt)); border: 1px solid rgb(var(--tl-border));">
          <p class="text-xs font-semibold mb-2" style="color: rgb(var(--tl-text));">Simulacao (exemplo R$ 100,00/mes)</p>
          <div class="grid grid-cols-3 gap-4">
            <div>
              <p class="text-xs" style="color: rgb(var(--tl-text-muted));">Cliente paga</p>
              <p class="text-sm font-bold" style="color: rgb(var(--tl-text));">R$ 100,00</p>
            </div>
            <div>
              <p class="text-xs" style="color: rgb(var(--tl-text-muted));">Taxa plataforma ({{ novaTaxa }}%)</p>
              <p class="text-sm font-bold text-indigo-600">R$ {{ formatPreco(100 * novaTaxa / 100) }}</p>
            </div>
            <div>
              <p class="text-xs" style="color: rgb(var(--tl-text-muted));">Consultor recebe</p>
              <p class="text-sm font-bold text-green-600">R$ {{ formatPreco(100 - (100 * novaTaxa / 100)) }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Resumo Financeiro -->
      <div v-if="stats" class="card p-5 sm:p-6">
        <h3 class="section-title">Resumo Financeiro</h3>
        <div class="space-y-3">
          <div class="flex justify-between items-center py-2" style="border-bottom: 1px solid rgb(var(--tl-border));">
            <span class="text-sm" style="color: rgb(var(--tl-text-muted));">Receita bruta total</span>
            <span class="text-sm font-bold" style="color: rgb(var(--tl-text));">R$ {{ formatPreco(stats.receitaBruta) }}</span>
          </div>
          <div class="flex justify-between items-center py-2" style="border-bottom: 1px solid rgb(var(--tl-border));">
            <span class="text-sm" style="color: rgb(var(--tl-text-muted));">Taxa acumulada ({{ stats.taxaPercentual }}%)</span>
            <span class="text-sm font-bold text-indigo-600">R$ {{ formatPreco(stats.taxaAcumulada) }}</span>
          </div>
          <div class="flex justify-between items-center py-2">
            <span class="text-sm" style="color: rgb(var(--tl-text-muted));">Repassado a consultores</span>
            <span class="text-sm font-bold text-green-600">R$ {{ formatPreco((stats.receitaBruta || 0) - (stats.taxaAcumulada || 0)) }}</span>
          </div>
        </div>
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
  { key: 'consultores', label: 'Consultores', icon: '👥' },
  { key: 'solicitacoes', label: 'Solicitacoes', icon: '📩' },
  { key: 'config', label: 'Configuracoes', icon: '⚙️' }
]
const statusFilters = [
  { value: '', label: 'Todas' },
  { value: 'PENDENTE', label: 'Pendentes' },
  { value: 'ACEITA', label: 'Aceitas' },
  { value: 'PAGA', label: 'Ativas' },
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

function getInitials(nome) {
  if (!nome) return '?'
  return nome.split(' ').map(w => w[0]).filter(Boolean).slice(0, 2).join('').toUpperCase()
}

function formatPreco(v) {
  if (!v && v !== 0) return '0,00'
  return Number(v).toFixed(2).replace('.', ',')
}

function formatDate(d) {
  if (!d) return ''
  return new Date(d).toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric' })
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

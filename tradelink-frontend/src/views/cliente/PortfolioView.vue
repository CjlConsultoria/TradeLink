<template>
  <div>
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold text-gray-900">Meu Portfolio</h1>
      <button @click="showForm = true; editingAtivo = null" class="px-4 py-2 bg-indigo-600 text-white rounded-lg text-sm hover:bg-indigo-700">
        + Adicionar ativo
      </button>
    </div>

    <!-- Resumo -->
    <div v-if="resumo" class="bg-white rounded-xl shadow-sm border border-gray-100 p-6 mb-6">
      <div class="flex items-baseline gap-2 mb-4">
        <span class="text-sm text-gray-500">Valor total do portfolio</span>
        <span class="text-2xl font-bold text-gray-900">{{ formatCurrency(resumo.valorTotalPortfolio) }}</span>
      </div>
      <!-- Barra de alocacao -->
      <div v-if="resumo.ativos.length" class="flex rounded-full h-3 overflow-hidden mb-3">
        <div v-for="a in resumo.ativos.filter(x => x.percentualAlocacao > 0)" :key="a.id"
          :style="{ width: a.percentualAlocacao + '%', backgroundColor: getColor(a.simbolo) }"
          :title="a.simbolo + ' ' + a.percentualAlocacao + '%'"
          class="transition-all"></div>
      </div>
      <div class="flex flex-wrap gap-3">
        <span v-for="a in resumo.ativos.filter(x => x.percentualAlocacao > 0)" :key="a.id" class="text-xs text-gray-600 flex items-center gap-1">
          <span class="w-2.5 h-2.5 rounded-full inline-block" :style="{ backgroundColor: getColor(a.simbolo) }"></span>
          {{ a.simbolo }} {{ a.percentualAlocacao }}%
        </span>
      </div>
    </div>

    <LoadingSpinner v-if="loading" />

    <!-- Tabela de ativos -->
    <div v-if="!loading && ativos.length" class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-x-auto">
      <table class="w-full text-sm min-w-[600px]">
        <thead>
          <tr class="bg-gray-50 text-gray-600 text-left">
            <th class="px-4 py-3 font-medium">Ativo</th>
            <th class="px-4 py-3 font-medium">Categoria</th>
            <th class="px-4 py-3 font-medium text-right">Quantidade</th>
            <th class="px-4 py-3 font-medium text-right">Preco</th>
            <th class="px-4 py-3 font-medium text-right">Valor</th>
            <th class="px-4 py-3 font-medium text-right">%</th>
            <th class="px-4 py-3 font-medium text-right">Acoes</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="a in ativos" :key="a.id" class="border-t border-gray-100 hover:bg-gray-50">
            <td class="px-4 py-3">
              <div class="font-medium text-gray-900">{{ a.simbolo }}</div>
              <div class="text-xs text-gray-500">{{ a.nome }}</div>
            </td>
            <td class="px-4 py-3">
              <span class="px-2 py-0.5 text-xs rounded-full" :class="catClass(a.categoria)">{{ catLabel(a.categoria) }}</span>
            </td>
            <td class="px-4 py-3 text-right font-mono">{{ formatQtd(a.quantidade) }}</td>
            <td class="px-4 py-3 text-right font-mono">{{ a.precoAtual != null ? formatCurrency(a.precoAtual) : '-' }}</td>
            <td class="px-4 py-3 text-right font-mono font-medium">{{ a.valorTotal != null ? formatCurrency(a.valorTotal) : '-' }}</td>
            <td class="px-4 py-3 text-right">{{ a.percentualAlocacao != null ? a.percentualAlocacao + '%' : '-' }}</td>
            <td class="px-4 py-3 text-right">
              <button @click="editingAtivo = a; showForm = true" class="text-indigo-600 hover:text-indigo-800 text-xs mr-2">Editar</button>
              <button @click="confirmarRemover(a)" class="text-red-500 hover:text-red-700 text-xs">Remover</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <EmptyState v-if="!loading && !ativos.length" message="Nenhum ativo cadastrado. Adicione seu primeiro ativo ao portfolio." />

    <AtivoForm v-if="showForm" :ativo="editingAtivo" @close="showForm = false" @saved="carregar" />
    <ConfirmDialog v-if="showConfirm" :message="confirmMsg" @confirm="removerAtivo" @cancel="showConfirm = false" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import portfolioApi from '../../api/portfolioApi'
import { useToast } from '../../composables/useToast'
import { formatCurrency } from '../../utils/formatters'
import { CATEGORIAS_ATIVO } from '../../utils/constants'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'
import EmptyState from '../../components/common/EmptyState.vue'
import ConfirmDialog from '../../components/common/ConfirmDialog.vue'
import AtivoForm from '../../components/portfolio/AtivoForm.vue'

const toast = useToast()
const loading = ref(true)
const ativos = ref([])
const resumo = ref(null)
const showForm = ref(false)
const editingAtivo = ref(null)
const showConfirm = ref(false)
const confirmMsg = ref('')
const ativoParaRemover = ref(null)

const cores = ['#6366f1','#f59e0b','#10b981','#ef4444','#3b82f6','#8b5cf6','#ec4899','#14b8a6','#f97316','#84cc16','#06b6d4','#e11d48']
function getColor(simbolo) {
  let hash = 0
  for (let i = 0; i < simbolo.length; i++) hash = simbolo.charCodeAt(i) + ((hash << 5) - hash)
  return cores[Math.abs(hash) % cores.length]
}

function catLabel(cat) {
  return CATEGORIAS_ATIVO.find(c => c.value === cat)?.label || cat
}
function catClass(cat) {
  const map = { CRYPTO: 'bg-purple-100 text-purple-700', FOREX: 'bg-blue-100 text-blue-700', ACAO: 'bg-green-100 text-green-700', COMMODITIES: 'bg-yellow-100 text-yellow-700' }
  return map[cat] || 'bg-gray-100 text-gray-700'
}
function formatQtd(v) {
  if (v == null) return '-'
  return Number(v) < 1 ? Number(v).toFixed(8).replace(/0+$/, '').replace(/\.$/, '') : Number(v).toLocaleString('pt-BR', { maximumFractionDigits: 4 })
}

async function carregar() {
  loading.value = true
  try {
    const [ativosRes, resumoRes] = await Promise.all([portfolioApi.listar(), portfolioApi.resumo()])
    ativos.value = ativosRes.data
    resumo.value = resumoRes.data
  } catch (e) {
    toast.error('Erro ao carregar portfolio.')
  } finally {
    loading.value = false
  }
}

function confirmarRemover(a) {
  ativoParaRemover.value = a
  confirmMsg.value = `Remover ${a.simbolo} (${a.nome}) do portfolio?`
  showConfirm.value = true
}

async function removerAtivo() {
  showConfirm.value = false
  try {
    await portfolioApi.remover(ativoParaRemover.value.id)
    toast.success('Ativo removido.')
    carregar()
  } catch (e) {
    toast.error('Erro ao remover ativo.')
  }
}

onMounted(carregar)
</script>

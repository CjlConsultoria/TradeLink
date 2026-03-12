<template>
  <div>
    <h2 class="page-title">Painel Financeiro</h2>

    <LoadingSpinner v-if="loading" text="Carregando dados financeiros..." />

    <template v-else>
      <!-- Cards de resumo -->
      <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4 mb-6">
        <div class="card p-5">
          <p class="text-xs font-medium text-gray-500 uppercase tracking-wider">Receita Total</p>
          <p class="text-2xl font-bold text-green-600 mt-1">{{ formatCurrency(resumo.receitaTotal) }}</p>
        </div>
        <div class="card p-5">
          <p class="text-xs font-medium text-gray-500 uppercase tracking-wider">Receita Mês</p>
          <p class="text-2xl font-bold text-green-600 mt-1">{{ formatCurrency(resumo.receitaMesAtual) }}</p>
        </div>
        <div class="card p-5">
          <p class="text-xs font-medium text-gray-500 uppercase tracking-wider">MRR</p>
          <p class="text-2xl font-bold text-indigo-600 mt-1">{{ formatCurrency(resumo.mrr) }}</p>
          <p class="text-xs text-gray-400 mt-1">Recorrente mensal</p>
        </div>
        <div class="card p-5">
          <p class="text-xs font-medium text-gray-500 uppercase tracking-wider">Pendentes</p>
          <p class="text-2xl font-bold text-amber-600 mt-1">{{ resumo.faturasPendentesCount }}</p>
          <p class="text-xs text-gray-400 mt-1">{{ formatCurrency(resumo.faturasPendentesValor) }}</p>
        </div>
        <div class="card p-5">
          <p class="text-xs font-medium text-gray-500 uppercase tracking-wider">Vencidas</p>
          <p class="text-2xl font-bold text-red-600 mt-1">{{ resumo.faturasVencidasCount }}</p>
          <p class="text-xs text-gray-400 mt-1">{{ formatCurrency(resumo.faturasVencidasValor) }}</p>
        </div>
        <div class="card p-5">
          <p class="text-xs font-medium text-gray-500 uppercase tracking-wider">Inadimplência</p>
          <p class="text-2xl font-bold mt-1" :class="taxaInadimplencia > 20 ? 'text-red-600' : taxaInadimplencia > 10 ? 'text-amber-600' : 'text-green-600'">
            {{ taxaInadimplencia.toFixed(1) }}%
          </p>
          <p class="text-xs text-gray-400 mt-1">vencidas / total</p>
        </div>
      </div>

      <!-- Gráficos -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
        <div class="card p-6">
          <h3 class="section-title mb-4">Receita Mensal</h3>
          <div class="chart-container">
            <Bar v-if="barChartData" :data="barChartData" :options="barChartOptions" />
            <p v-else class="text-sm text-gray-400 text-center py-10">Sem dados de receita</p>
          </div>
        </div>
        <div class="card p-6">
          <h3 class="section-title mb-4">Distribuição por Status</h3>
          <div class="chart-container chart-container--pie">
            <Doughnut v-if="doughnutData" :data="doughnutData" :options="doughnutOptions" />
            <p v-else class="text-sm text-gray-400 text-center py-10">Sem faturas</p>
          </div>
        </div>
      </div>

      <!-- Filtros -->
      <div class="card p-4 mb-4">
        <div class="flex flex-wrap items-center gap-3">
          <div>
            <label class="text-xs text-gray-500 block mb-1">Status</label>
            <select v-model="filtroStatus" class="input-base text-sm py-1.5 px-3 w-40">
              <option value="">Todas</option>
              <option value="PENDENTE">Pendentes</option>
              <option value="PAGA">Pagas</option>
              <option value="VENCIDA">Vencidas</option>
            </select>
          </div>
          <div>
            <label class="text-xs text-gray-500 block mb-1">Empresa</label>
            <input v-model="filtroBusca" type="text" placeholder="Buscar empresa..."
              class="input-base text-sm py-1.5 px-3 w-52" />
          </div>
          <div class="ml-auto text-sm text-gray-500 self-end">
            {{ faturasFiltradas.length }} fatura{{ faturasFiltradas.length !== 1 ? 's' : '' }}
          </div>
        </div>
      </div>

      <!-- Tabela de faturas global -->
      <div class="card p-6">
        <h3 class="section-title mb-4">Todas as Faturas</h3>
        <div class="overflow-x-auto">
          <table v-if="faturasFiltradas.length > 0" class="w-full text-sm">
            <thead>
              <tr class="border-b border-gray-200">
                <th class="text-left py-2 px-2 font-medium text-gray-500">Empresa</th>
                <th class="text-left py-2 px-2 font-medium text-gray-500">Vencimento</th>
                <th class="text-left py-2 px-2 font-medium text-gray-500">Pagamento</th>
                <th class="text-right py-2 px-2 font-medium text-gray-500">Valor</th>
                <th class="text-center py-2 px-2 font-medium text-gray-500">Status</th>
                <th class="text-left py-2 px-2 font-medium text-gray-500">Forma</th>
                <th class="text-center py-2 px-2 font-medium text-gray-500">Ações</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="f in faturasPaginadas" :key="f.id" class="border-b border-gray-100 hover:bg-gray-50">
                <td class="py-2.5 px-2">
                  <router-link v-if="f.empresaId" :to="`/admin-max/empresas/${f.empresaId}`"
                    class="text-indigo-600 hover:underline font-medium">{{ f.empresaNome }}</router-link>
                  <span v-else class="text-gray-400">—</span>
                </td>
                <td class="py-2.5 px-2 text-gray-600">{{ formatDate(f.dataVencimento) }}</td>
                <td class="py-2.5 px-2 text-gray-600">{{ f.dataPagamento ? formatDate(f.dataPagamento) : '—' }}</td>
                <td class="py-2.5 px-2 text-right font-medium">{{ formatCurrency(f.valor) }}</td>
                <td class="py-2.5 px-2 text-center">
                  <span class="px-2 py-0.5 rounded-full text-xs font-medium" :class="badgeStatus(f.status)">
                    {{ labelStatus(f.status) }}
                  </span>
                </td>
                <td class="py-2.5 px-2 text-gray-500 text-xs">{{ labelForma(f.formaPagamento) }}</td>
                <td class="py-2.5 px-2 text-center">
                  <button v-if="f.empresaId" @click="downloadPdf(f)" class="text-indigo-600 hover:text-indigo-800 text-xs font-medium">
                    PDF
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
          <p v-else class="text-sm text-gray-400 text-center py-6">Nenhuma fatura encontrada</p>
        </div>

        <!-- Paginação -->
        <div v-if="totalPaginas > 1" class="flex items-center justify-center gap-2 mt-4 pt-4 border-t border-gray-100">
          <button @click="paginaAtual = Math.max(1, paginaAtual - 1)" :disabled="paginaAtual === 1"
            class="px-3 py-1 text-sm rounded border border-gray-200 hover:bg-gray-50 disabled:opacity-40">
            Anterior
          </button>
          <span class="text-sm text-gray-500">{{ paginaAtual }} / {{ totalPaginas }}</span>
          <button @click="paginaAtual = Math.min(totalPaginas, paginaAtual + 1)" :disabled="paginaAtual === totalPaginas"
            class="px-3 py-1 text-sm rounded border border-gray-200 hover:bg-gray-50 disabled:opacity-40">
            Próxima
          </button>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { Bar, Doughnut } from 'vue-chartjs'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  ArcElement,
  Tooltip,
  Legend
} from 'chart.js'
import faturaApi from '../../api/faturaApi'
import { useToast } from '../../composables/useToast'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'

ChartJS.register(CategoryScale, LinearScale, BarElement, ArcElement, Tooltip, Legend)

const route = useRoute()
const toast = useToast()
const loading = ref(true)

const resumo = ref({
  receitaTotal: 0,
  receitaMesAtual: 0,
  faturasPendentesCount: 0,
  faturasPendentesValor: 0,
  faturasVencidasCount: 0,
  faturasVencidasValor: 0,
  mrr: 0,
  receitaMensal: []
})

const faturas = ref([])
const filtroStatus = ref('')
const filtroBusca = ref('')
const paginaAtual = ref(1)
const itensPorPagina = 15

// Pré-filtrar pelo query param
if (route.query.status) {
  filtroStatus.value = route.query.status
}

const taxaInadimplencia = computed(() => {
  const total = faturas.value.length
  if (total === 0) return 0
  const vencidas = faturas.value.filter(f => f.status === 'VENCIDA').length
  return (vencidas / total) * 100
})

const faturasFiltradas = computed(() => {
  let result = faturas.value
  if (filtroStatus.value) {
    result = result.filter(f => f.status === filtroStatus.value)
  }
  if (filtroBusca.value.trim()) {
    const busca = filtroBusca.value.toLowerCase().trim()
    result = result.filter(f => f.empresaNome && f.empresaNome.toLowerCase().includes(busca))
  }
  return result
})

const totalPaginas = computed(() => Math.max(1, Math.ceil(faturasFiltradas.value.length / itensPorPagina)))

const faturasPaginadas = computed(() => {
  const inicio = (paginaAtual.value - 1) * itensPorPagina
  return faturasFiltradas.value.slice(inicio, inicio + itensPorPagina)
})

// Reset página ao mudar filtro
watch([filtroStatus, filtroBusca], () => { paginaAtual.value = 1 })

function formatCurrency(value) {
  const num = Number(value) || 0
  return num.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })
}

function formatDate(instant) {
  if (!instant) return ''
  return new Date(instant).toLocaleDateString('pt-BR')
}

function formatMesLabel(mesStr) {
  if (!mesStr) return ''
  const [ano, mes] = mesStr.split('-')
  const meses = ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez']
  return `${meses[parseInt(mes, 10) - 1]}/${ano.slice(2)}`
}

function badgeStatus(status) {
  const map = {
    PAGA: 'bg-green-100 text-green-800',
    PENDENTE: 'bg-amber-100 text-amber-800',
    VENCIDA: 'bg-red-100 text-red-800'
  }
  return map[status] || 'bg-gray-100 text-gray-800'
}

function labelStatus(status) {
  const map = { PAGA: 'Paga', PENDENTE: 'Pendente', VENCIDA: 'Vencida' }
  return map[status] || status
}

function labelForma(forma) {
  if (!forma) return '—'
  const map = { STRIPE: 'Stripe', MANUAL: 'Manual', PIX: 'PIX' }
  return map[forma] || forma
}

async function downloadPdf(fatura) {
  try {
    const res = await faturaApi.getPdfBlob(fatura.empresaId, fatura.id)
    const blob = new Blob([res.data], { type: 'application/pdf' })
    const url = URL.createObjectURL(blob)
    window.open(url, '_blank')
    setTimeout(() => URL.revokeObjectURL(url), 60000)
  } catch (e) {
    toast.error('Erro ao baixar PDF')
  }
}

// Charts
const barChartData = computed(() => {
  const data = resumo.value.receitaMensal
  if (!data || data.length === 0) return null
  const hasData = data.some(d => Number(d.valor) > 0)
  if (!hasData) return null
  return {
    labels: data.map(d => formatMesLabel(d.mes)),
    datasets: [{
      label: 'Receita (R$)',
      data: data.map(d => Number(d.valor)),
      backgroundColor: 'rgba(16, 185, 129, 0.8)',
      borderColor: 'rgb(16, 185, 129)',
      borderWidth: 1,
      borderRadius: 6,
      barPercentage: 0.6
    }]
  }
})

const barChartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { display: false },
    tooltip: {
      backgroundColor: 'rgba(15, 23, 42, 0.9)',
      titleFont: { size: 13 },
      bodyFont: { size: 12 },
      padding: 10,
      cornerRadius: 8,
      callbacks: {
        label(ctx) {
          return `R$ ${Number(ctx.raw).toLocaleString('pt-BR', { minimumFractionDigits: 2 })}`
        }
      }
    }
  },
  scales: {
    y: {
      beginAtZero: true,
      ticks: {
        font: { size: 11 },
        color: '#9ca3af',
        callback(val) { return `R$ ${val}` }
      },
      grid: { color: 'rgba(0,0,0,0.04)' }
    },
    x: {
      ticks: { font: { size: 11 }, color: '#9ca3af' },
      grid: { display: false }
    }
  }
}

const doughnutData = computed(() => {
  const pagas = faturas.value.filter(f => f.status === 'PAGA').length
  const pendentes = faturas.value.filter(f => f.status === 'PENDENTE').length
  const vencidas = faturas.value.filter(f => f.status === 'VENCIDA').length
  if (pagas + pendentes + vencidas === 0) return null
  return {
    labels: ['Pagas', 'Pendentes', 'Vencidas'],
    datasets: [{
      data: [pagas, pendentes, vencidas],
      backgroundColor: [
        'rgba(16, 185, 129, 0.85)',
        'rgba(245, 158, 11, 0.85)',
        'rgba(239, 68, 68, 0.85)'
      ],
      borderWidth: 2,
      borderColor: '#ffffff',
      hoverOffset: 6
    }]
  }
})

const doughnutOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      position: 'bottom',
      labels: { usePointStyle: true, pointStyle: 'circle', padding: 14, font: { size: 12 } }
    },
    tooltip: {
      backgroundColor: 'rgba(15, 23, 42, 0.9)',
      titleFont: { size: 13 },
      bodyFont: { size: 12 },
      padding: 10,
      cornerRadius: 8,
      callbacks: {
        label(ctx) {
          const total = ctx.dataset.data.reduce((a, b) => a + b, 0)
          const pct = total > 0 ? ((ctx.raw / total) * 100).toFixed(1) : 0
          return `${ctx.label}: ${ctx.raw} (${pct}%)`
        }
      }
    }
  }
}

onMounted(async () => {
  try {
    const [resumoRes, faturasRes] = await Promise.all([
      faturaApi.financeiroResumo(),
      faturaApi.listarTodas()
    ])
    resumo.value = resumoRes.data
    faturas.value = faturasRes.data
  } catch (e) {
    console.error('Erro ao carregar financeiro:', e)
    toast.error('Erro ao carregar dados financeiros')
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.chart-container {
  position: relative;
  height: 280px;
}
.chart-container--pie {
  height: 300px;
}
</style>

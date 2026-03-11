<template>
  <div>
    <h2 class="page-title">Dashboard</h2>

    <!-- Loading state -->
    <div v-if="loading" class="flex items-center justify-center py-20">
      <div class="flex items-center gap-3 text-gray-500">
        <svg class="animate-spin h-5 w-5" viewBox="0 0 24 24" fill="none">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
        </svg>
        Carregando...
      </div>
    </div>

    <template v-else>
      <!-- Cards de indicadores gerenciais -->
      <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4 mb-6">
        <router-link to="/admin-max/empresas" class="card p-5 block hover:border-indigo-300 transition-colors">
          <p class="text-xs font-medium text-gray-500 uppercase tracking-wider">Empresas</p>
          <p class="text-2xl font-bold text-indigo-600 mt-1">{{ stats.totalEmpresas }}</p>
          <div class="flex gap-2 mt-2">
            <span class="text-xs px-1.5 py-0.5 rounded-full bg-green-100 text-green-700">{{ stats.empresasAtivas }} ativas</span>
            <span v-if="stats.empresasInativas > 0" class="text-xs px-1.5 py-0.5 rounded-full bg-red-100 text-red-700">{{ stats.empresasInativas }}</span>
          </div>
        </router-link>
        <div class="card p-5">
          <p class="text-xs font-medium text-gray-500 uppercase tracking-wider">Consultores</p>
          <p class="text-2xl font-bold text-blue-600 mt-1">{{ stats.totalConsultores }}</p>
        </div>
        <div class="card p-5">
          <p class="text-xs font-medium text-gray-500 uppercase tracking-wider">Clientes</p>
          <p class="text-2xl font-bold text-emerald-600 mt-1">{{ stats.totalClientes }}</p>
        </div>
        <div class="card p-5">
          <p class="text-xs font-medium text-gray-500 uppercase tracking-wider">Carteiras</p>
          <p class="text-2xl font-bold text-purple-600 mt-1">{{ stats.totalCarteiras }}</p>
        </div>
        <div class="card p-5">
          <p class="text-xs font-medium text-gray-500 uppercase tracking-wider">Receita Total</p>
          <p class="text-2xl font-bold text-green-600 mt-1">{{ formatCurrency(stats.receitaTotal) }}</p>
          <p class="text-xs text-gray-400 mt-1">Este mes: {{ formatCurrency(stats.receitaMesAtual) }}</p>
        </div>
        <div class="card p-5">
          <p class="text-xs font-medium text-gray-500 uppercase tracking-wider">Faturas</p>
          <div class="flex items-baseline gap-2 mt-1">
            <span class="text-2xl font-bold text-amber-600">{{ stats.faturasPendentes }}</span>
            <span class="text-xs text-gray-400">pendentes</span>
          </div>
          <p v-if="stats.faturasVencidas > 0" class="text-xs text-red-500 mt-1">{{ stats.faturasVencidas }} vencidas</p>
        </div>
      </div>

      <!-- Graficos -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
        <!-- Crescimento mensal -->
        <div class="card p-6">
          <h3 class="section-title mb-4">Crescimento Mensal</h3>
          <div class="chart-container">
            <Bar v-if="barChartData" :data="barChartData" :options="barChartOptions" />
            <p v-else class="text-sm text-gray-400 text-center py-10">Sem dados de crescimento</p>
          </div>
        </div>

        <!-- Distribuicao por plano -->
        <div class="card p-6">
          <h3 class="section-title mb-4">Empresas por Plano</h3>
          <div class="chart-container chart-container--pie">
            <Doughnut v-if="pieChartData" :data="pieChartData" :options="pieChartOptions" />
            <p v-else class="text-sm text-gray-400 text-center py-10">Sem dados de planos</p>
          </div>
        </div>
      </div>

      <!-- Status assinatura + Empresas recentes -->
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-6">
        <!-- Status de assinatura -->
        <div class="card p-6">
          <h3 class="section-title mb-4">Status Assinatura</h3>
          <div class="space-y-3">
            <div v-for="(count, status) in stats.empresasPorSubscription" :key="status" class="flex items-center justify-between">
              <div class="flex items-center gap-2">
                <span class="w-2.5 h-2.5 rounded-full" :class="subscriptionColor(status)"></span>
                <span class="text-sm text-gray-700">{{ subscriptionLabel(status) }}</span>
              </div>
              <span class="text-sm font-semibold text-gray-900">{{ count }}</span>
            </div>
          </div>
        </div>

        <!-- Empresas recentes -->
        <div class="card p-6 lg:col-span-2">
          <div class="flex items-center justify-between mb-4">
            <h3 class="section-title">Empresas Recentes</h3>
            <router-link to="/admin-max/empresas" class="text-sm text-indigo-600 hover:text-indigo-800 font-medium">Ver todas</router-link>
          </div>
          <div class="overflow-x-auto">
            <table class="w-full text-sm">
              <thead>
                <tr class="border-b border-gray-200">
                  <th class="text-left py-2 px-2 font-medium text-gray-500">Nome</th>
                  <th class="text-left py-2 px-2 font-medium text-gray-500">Plano</th>
                  <th class="text-center py-2 px-2 font-medium text-gray-500">Usuarios</th>
                  <th class="text-center py-2 px-2 font-medium text-gray-500">Status</th>
                  <th class="text-right py-2 px-2 font-medium text-gray-500">Criada em</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="e in stats.empresasRecentes" :key="e.id" class="border-b border-gray-100 hover:bg-gray-50">
                  <td class="py-2.5 px-2">
                    <router-link :to="`/admin-max/empresas/${e.id}`" class="text-indigo-600 hover:underline font-medium">{{ e.nome }}</router-link>
                  </td>
                  <td class="py-2.5 px-2 text-gray-600">{{ e.planoNome }}</td>
                  <td class="py-2.5 px-2 text-center">{{ e.totalUsuarios }}</td>
                  <td class="py-2.5 px-2 text-center">
                    <span class="px-2 py-0.5 rounded-full text-xs font-medium" :class="e.ativa ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'">
                      {{ e.ativa ? 'Ativa' : 'Inativa' }}
                    </span>
                  </td>
                  <td class="py-2.5 px-2 text-right text-gray-500">{{ e.createdAt }}</td>
                </tr>
                <tr v-if="!stats.empresasRecentes || stats.empresasRecentes.length === 0">
                  <td colspan="5" class="py-6 text-center text-gray-400">Nenhuma empresa cadastrada</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <!-- Atalhos rapidos -->
      <div class="card p-6">
        <h3 class="section-title mb-4">Atalhos</h3>
        <div class="grid grid-cols-2 sm:grid-cols-4 gap-3">
          <router-link to="/admin-max/empresas" class="flex items-center gap-3 p-4 rounded-lg border border-gray-200 hover:border-indigo-300 hover:bg-indigo-50 transition-colors">
            <span class="w-10 h-10 rounded-lg bg-indigo-100 flex items-center justify-center text-indigo-600 text-lg font-bold">E</span>
            <span class="font-medium text-gray-800">Empresas</span>
          </router-link>
          <router-link to="/admin-max/planos" class="flex items-center gap-3 p-4 rounded-lg border border-gray-200 hover:border-blue-300 hover:bg-blue-50 transition-colors">
            <span class="w-10 h-10 rounded-lg bg-blue-100 flex items-center justify-center text-blue-600 text-lg font-bold">P</span>
            <span class="font-medium text-gray-800">Planos</span>
          </router-link>
          <router-link to="/admin-max/usuarios" class="flex items-center gap-3 p-4 rounded-lg border border-gray-200 hover:border-purple-300 hover:bg-purple-50 transition-colors">
            <span class="w-10 h-10 rounded-lg bg-purple-100 flex items-center justify-center text-purple-600 text-lg font-bold">U</span>
            <span class="font-medium text-gray-800">Usuarios</span>
          </router-link>
          <router-link to="/admin-max/configuracoes" class="flex items-center gap-3 p-4 rounded-lg border border-gray-200 hover:border-gray-400 hover:bg-gray-50 transition-colors">
            <span class="w-10 h-10 rounded-lg bg-gray-100 flex items-center justify-center text-gray-600 text-lg font-bold">C</span>
            <span class="font-medium text-gray-800">Config</span>
          </router-link>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
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
import empresaApi from '../../api/empresaApi'

ChartJS.register(CategoryScale, LinearScale, BarElement, ArcElement, Tooltip, Legend)

const loading = ref(true)
const stats = ref({
  totalEmpresas: 0,
  empresasAtivas: 0,
  empresasInativas: 0,
  totalConsultores: 0,
  totalClientes: 0,
  totalCarteiras: 0,
  empresasPorSubscription: {},
  empresasPorPlano: [],
  receitaTotal: 0,
  receitaMesAtual: 0,
  faturasPendentes: 0,
  faturasVencidas: 0,
  crescimentoMensal: [],
  empresasRecentes: []
})

function formatCurrency(value) {
  const num = Number(value) || 0
  return num.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })
}

function formatMesLabel(mesStr) {
  if (!mesStr) return ''
  const [ano, mes] = mesStr.split('-')
  const meses = ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez']
  return `${meses[parseInt(mes, 10) - 1]}/${ano.slice(2)}`
}

function subscriptionLabel(status) {
  const labels = {
    NONE: 'Sem assinatura',
    ACTIVE: 'Ativa',
    PAST_DUE: 'Em atraso',
    CANCELLED: 'Cancelada',
    TRIAL: 'Trial'
  }
  return labels[status] || status
}

function subscriptionColor(status) {
  const colors = {
    NONE: 'bg-gray-400',
    ACTIVE: 'bg-green-500',
    PAST_DUE: 'bg-amber-500',
    CANCELLED: 'bg-red-500',
    TRIAL: 'bg-blue-500'
  }
  return colors[status] || 'bg-gray-400'
}

// --- Chart.js configs ---

const CHART_COLORS = [
  'rgba(99, 102, 241, 0.85)',
  'rgba(59, 130, 246, 0.85)',
  'rgba(16, 185, 129, 0.85)',
  'rgba(245, 158, 11, 0.85)',
  'rgba(239, 68, 68, 0.85)',
  'rgba(139, 92, 246, 0.85)',
  'rgba(236, 72, 153, 0.85)',
  'rgba(20, 184, 166, 0.85)'
]

const barChartData = computed(() => {
  const data = stats.value.crescimentoMensal
  if (!data || data.length === 0) return null
  const hasData = data.some(d => d.empresas > 0 || d.usuarios > 0)
  if (!hasData) return null
  return {
    labels: data.map(d => formatMesLabel(d.mes)),
    datasets: [
      {
        label: 'Empresas',
        data: data.map(d => d.empresas),
        backgroundColor: 'rgba(99, 102, 241, 0.8)',
        borderColor: 'rgb(99, 102, 241)',
        borderWidth: 1,
        borderRadius: 6,
        barPercentage: 0.6
      },
      {
        label: 'Usuarios',
        data: data.map(d => d.usuarios),
        backgroundColor: 'rgba(16, 185, 129, 0.8)',
        borderColor: 'rgb(16, 185, 129)',
        borderWidth: 1,
        borderRadius: 6,
        barPercentage: 0.6
      }
    ]
  }
})

const barChartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      position: 'bottom',
      labels: {
        usePointStyle: true,
        pointStyle: 'circle',
        padding: 16,
        font: { size: 12 }
      }
    },
    tooltip: {
      backgroundColor: 'rgba(15, 23, 42, 0.9)',
      titleFont: { size: 13 },
      bodyFont: { size: 12 },
      padding: 10,
      cornerRadius: 8
    }
  },
  scales: {
    y: {
      beginAtZero: true,
      ticks: {
        stepSize: 1,
        font: { size: 11 },
        color: '#9ca3af'
      },
      grid: { color: 'rgba(0,0,0,0.04)' }
    },
    x: {
      ticks: {
        font: { size: 11 },
        color: '#9ca3af'
      },
      grid: { display: false }
    }
  }
}

const pieChartData = computed(() => {
  const data = stats.value.empresasPorPlano
  if (!data || data.length === 0) return null
  return {
    labels: data.map(d => d.nome),
    datasets: [{
      data: data.map(d => d.quantidade),
      backgroundColor: CHART_COLORS.slice(0, data.length),
      borderWidth: 2,
      borderColor: '#ffffff',
      hoverOffset: 6
    }]
  }
})

const pieChartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      position: 'bottom',
      labels: {
        usePointStyle: true,
        pointStyle: 'circle',
        padding: 14,
        font: { size: 12 }
      }
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
    const res = await empresaApi.dashboardStats()
    stats.value = res.data
  } catch (e) {
    console.error('Erro ao carregar dashboard stats:', e)
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

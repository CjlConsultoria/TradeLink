<template>
  <div>
    <h2 class="page-title">Performance · ROI</h2>
    <p class="text-sm text-gray-500 mb-6">Acompanhe a evolução do seu patrimônio e retorno sobre investimento.</p>

    <LoadingSpinner v-if="loading" text="Carregando dados de performance..." />

    <template v-else>
      <!-- Cards de métricas -->
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        <div class="card p-5">
          <p class="text-sm text-gray-500">Total Investido</p>
          <p class="text-2xl font-bold text-gray-900 mt-1">{{ formatCurrency(metricas.totalInvestido) }}</p>
        </div>
        <div class="card p-5">
          <p class="text-sm text-gray-500">Total Vendido</p>
          <p class="text-2xl font-bold text-blue-600 mt-1">{{ formatCurrency(metricas.totalVendido) }}</p>
        </div>
        <div class="card p-5" :class="metricas.resultado >= 0 ? 'border-emerald-200' : 'border-red-200'">
          <p class="text-sm text-gray-500">Resultado</p>
          <p class="text-2xl font-bold mt-1" :class="metricas.resultado >= 0 ? 'text-emerald-600' : 'text-red-600'">
            {{ metricas.resultado >= 0 ? '+' : '' }}{{ formatCurrency(metricas.resultado) }}
          </p>
        </div>
        <div class="card p-5" :class="metricas.roi >= 0 ? 'border-emerald-200' : 'border-red-200'">
          <p class="text-sm text-gray-500">ROI</p>
          <p class="text-2xl font-bold mt-1" :class="metricas.roi >= 0 ? 'text-emerald-600' : 'text-red-600'">
            {{ metricas.roi >= 0 ? '+' : '' }}{{ metricas.roi.toFixed(2) }}%
          </p>
        </div>
      </div>

      <!-- Gráfico de operações acumuladas -->
      <div class="card p-6 mb-6">
        <h3 class="section-title">Evolução Acumulada</h3>
        <div v-if="operacoes.length < 2" class="text-center py-8 text-gray-400">
          Precisa de pelo menos 2 operações para gerar o gráfico.
        </div>
        <canvas v-else ref="chartAcumRef" height="250"></canvas>
      </div>

      <!-- Performance por moeda -->
      <div class="card p-6 mb-6">
        <h3 class="section-title">Performance por Moeda</h3>
        <div v-if="performancePorMoeda.length === 0" class="text-center py-8 text-gray-400">Sem dados.</div>
        <div v-else class="grid grid-cols-1 sm:grid-cols-2 gap-3">
          <canvas ref="chartMoedaRef" height="200"></canvas>
          <div class="space-y-2">
            <div v-for="pm in performancePorMoeda" :key="pm.par"
              class="flex items-center justify-between p-3 rounded-lg border"
              :class="pm.resultado >= 0 ? 'border-emerald-200 bg-emerald-50' : 'border-red-200 bg-red-50'">
              <span class="font-semibold text-sm">{{ pm.par }}</span>
              <div class="text-right">
                <span class="text-sm font-bold" :class="pm.resultado >= 0 ? 'text-emerald-600' : 'text-red-600'">
                  {{ pm.resultado >= 0 ? '+' : '' }}{{ formatCurrency(pm.resultado) }}
                </span>
                <span class="text-xs text-gray-500 ml-1">({{ pm.operacoes }} ops)</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Últimas operações -->
      <div class="card p-6">
        <h3 class="section-title">Últimas Operações</h3>
        <div v-if="operacoes.length === 0" class="text-center py-8 text-gray-400">Nenhuma operação registrada.</div>
        <div v-else class="overflow-x-auto">
          <table class="w-full text-sm">
            <thead>
              <tr class="border-b border-gray-200">
                <th class="text-left py-2 px-3 font-semibold text-gray-700">Data</th>
                <th class="text-left py-2 px-3 font-semibold text-gray-700">Par</th>
                <th class="text-left py-2 px-3 font-semibold text-gray-700">Tipo</th>
                <th class="text-right py-2 px-3 font-semibold text-gray-700">Preço</th>
                <th class="text-right py-2 px-3 font-semibold text-gray-700">Qtd</th>
                <th class="text-right py-2 px-3 font-semibold text-gray-700">Valor</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="op in operacoes.slice(0, 20)" :key="op.id" class="border-b border-gray-100 hover:bg-gray-50">
                <td class="py-2 px-3">{{ formatDate(op.dataExecucao) }}</td>
                <td class="py-2 px-3 font-medium">{{ op.recomendacaoMoedaPar || '-' }}</td>
                <td class="py-2 px-3">
                  <span class="px-2 py-0.5 rounded text-xs font-semibold"
                    :class="op.tipo === 'COMPRA' ? 'bg-emerald-100 text-emerald-700' : 'bg-red-100 text-red-700'">
                    {{ op.tipo }}
                  </span>
                </td>
                <td class="py-2 px-3 text-right">{{ formatCurrency(op.precoExecutado) }}</td>
                <td class="py-2 px-3 text-right">{{ op.quantidade }}</td>
                <td class="py-2 px-3 text-right font-medium">{{ formatCurrency(op.valorOperacao) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, onUnmounted } from 'vue'
import operacaoApi from '../../api/operacaoApi'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'
import { Chart, registerables } from 'chart.js'
Chart.register(...registerables)

const chartAcumRef = ref(null)
const chartMoedaRef = ref(null)
let chartAcum = null
let chartMoeda = null
const loading = ref(true)
const operacoes = ref([])
const metricas = ref({ totalInvestido: 0, totalVendido: 0, resultado: 0, roi: 0 })
const performancePorMoeda = ref([])

onMounted(async () => {
  try {
    const res = await operacaoApi.listarMinhas()
    operacoes.value = (res.data || []).sort((a, b) => new Date(a.dataExecucao) - new Date(b.dataExecucao))
    calcularMetricas()
    await nextTick()
    renderCharts()
  } catch (e) { console.error(e) }
  finally { loading.value = false }
})

function calcularMetricas() {
  let totalCompras = 0
  let totalVendas = 0
  const porMoeda = {}

  for (const op of operacoes.value) {
    const valor = Number(op.valorOperacao || 0)
    const par = op.recomendacaoMoedaPar || 'Outro'

    if (!porMoeda[par]) porMoeda[par] = { compras: 0, vendas: 0, operacoes: 0 }
    porMoeda[par].operacoes++

    if (op.tipo === 'COMPRA') {
      totalCompras += valor
      porMoeda[par].compras += valor
    } else {
      totalVendas += valor
      porMoeda[par].vendas += valor
    }
  }

  const resultado = totalVendas - totalCompras
  metricas.value = {
    totalInvestido: totalCompras,
    totalVendido: totalVendas,
    resultado,
    roi: totalCompras > 0 ? (resultado / totalCompras) * 100 : 0
  }

  performancePorMoeda.value = Object.entries(porMoeda)
    .map(([par, data]) => ({
      par,
      resultado: data.vendas - data.compras,
      operacoes: data.operacoes,
      compras: data.compras,
      vendas: data.vendas
    }))
    .sort((a, b) => b.resultado - a.resultado)
}

function renderCharts() {
  // Gráfico acumulado
  if (chartAcumRef.value && operacoes.value.length >= 2) {
    if (chartAcum) chartAcum.destroy()
    let acum = 0
    const labels = []
    const data = []
    for (const op of operacoes.value) {
      const val = Number(op.valorOperacao || 0)
      acum += op.tipo === 'VENDA' ? val : -val
      labels.push(formatDate(op.dataExecucao))
      data.push(acum)
    }
    chartAcum = new Chart(chartAcumRef.value, {
      type: 'line',
      data: {
        labels,
        datasets: [{
          label: 'Resultado acumulado (R$)',
          data,
          borderColor: acum >= 0 ? '#10b981' : '#ef4444',
          backgroundColor: acum >= 0 ? 'rgba(16,185,129,0.1)' : 'rgba(239,68,68,0.1)',
          fill: true, tension: 0.3, pointRadius: 2, borderWidth: 2
        }, {
          label: 'Zero',
          data: data.map(() => 0),
          borderColor: '#cbd5e1', borderDash: [4, 4], borderWidth: 1, pointRadius: 0
        }]
      },
      options: {
        responsive: true,
        plugins: { legend: { position: 'top' } },
        scales: { y: { ticks: { callback: v => 'R$ ' + Number(v).toLocaleString('pt-BR') } } }
      }
    })
  }

  // Gráfico por moeda (bar)
  if (chartMoedaRef.value && performancePorMoeda.value.length > 0) {
    if (chartMoeda) chartMoeda.destroy()
    const pms = performancePorMoeda.value.slice(0, 8)
    chartMoeda = new Chart(chartMoedaRef.value, {
      type: 'bar',
      data: {
        labels: pms.map(p => p.par),
        datasets: [{
          label: 'Resultado (R$)',
          data: pms.map(p => p.resultado),
          backgroundColor: pms.map(p => p.resultado >= 0 ? '#10b981' : '#ef4444'),
          borderRadius: 6
        }]
      },
      options: {
        responsive: true,
        indexAxis: 'y',
        plugins: { legend: { display: false } },
        scales: { x: { ticks: { callback: v => 'R$ ' + Number(v).toLocaleString('pt-BR') } } }
      }
    })
  }
}

function formatCurrency(v) {
  return 'R$ ' + Number(v || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function formatDate(dt) {
  if (!dt) return '-'
  return new Date(dt).toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit', year: '2-digit' })
}

onUnmounted(() => {
  if (chartAcum) chartAcum.destroy()
  if (chartMoeda) chartMoeda.destroy()
})
</script>

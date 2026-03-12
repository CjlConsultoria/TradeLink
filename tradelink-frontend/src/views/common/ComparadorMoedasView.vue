<template>
  <div>
    <h2 class="page-title">Comparador de Moedas</h2>
    <p class="text-sm text-gray-500 mb-6">Selecione até 3 moedas e compare a variação percentual no período.</p>

    <!-- Seleção de moedas -->
    <div class="card p-4 mb-6">
      <div class="flex flex-wrap items-end gap-3">
        <div v-for="(sel, i) in selecionadas" :key="i" class="flex-1 min-w-[140px] sm:min-w-[180px]">
          <label class="text-xs font-medium text-gray-500 mb-1 block">Moeda {{ i + 1 }}</label>
          <select v-model="selecionadas[i]" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm">
            <option value="">Selecione...</option>
            <option v-for="c in moedasDisponiveis" :key="c.key" :value="c.key">
              {{ c.moeda }}/{{ c.parMoeda }}
            </option>
          </select>
        </div>
        <div class="flex gap-2">
          <button v-if="selecionadas.length < 3" type="button" @click="selecionadas.push('')"
            class="px-3 py-2 bg-gray-100 rounded-lg text-sm hover:bg-gray-200">+ Adicionar</button>
          <button v-if="selecionadas.length > 1" type="button" @click="selecionadas.pop()"
            class="px-3 py-2 bg-red-50 text-red-600 rounded-lg text-sm hover:bg-red-100">Remover</button>
        </div>
      </div>
      <div class="flex flex-wrap gap-2 mt-4">
        <button v-for="p in periodos" :key="p.horas" type="button"
          @click="periodoHoras = p.horas; carregarHistoricos()"
          class="px-3 py-1.5 rounded-lg text-sm font-medium transition-colors"
          :class="periodoHoras === p.horas ? 'bg-indigo-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'">
          {{ p.label }}
        </button>
      </div>
    </div>

    <!-- Gráfico -->
    <div class="card p-6">
      <LoadingSpinner v-if="loadingChart" text="Carregando dados..." />
      <div v-else-if="datasets.length === 0" class="text-center py-12 text-gray-400">
        Selecione ao menos uma moeda acima para comparar.
      </div>
      <canvas v-show="datasets.length > 0 && !loadingChart" ref="chartRef" height="300"></canvas>
    </div>

    <!-- Tabela comparativa -->
    <div v-if="resumoComparativo.length" class="card p-6 mt-6">
      <h3 class="section-title">Resumo Comparativo</h3>
      <div class="overflow-x-auto">
        <table class="w-full text-sm">
          <thead>
            <tr class="border-b border-gray-200">
              <th class="text-left py-2 px-3 font-semibold text-gray-700">Par</th>
              <th class="text-right py-2 px-3 font-semibold text-gray-700">Preço Atual</th>
              <th class="text-right py-2 px-3 font-semibold text-gray-700">Variação no Período</th>
              <th class="text-right py-2 px-3 font-semibold text-gray-700">Máxima</th>
              <th class="text-right py-2 px-3 font-semibold text-gray-700">Mínima</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="r in resumoComparativo" :key="r.key" class="border-b border-gray-100 hover:bg-gray-50">
              <td class="py-2 px-3 font-medium flex items-center gap-2">
                <span class="w-3 h-3 rounded-full" :style="{ backgroundColor: r.color }"></span>
                {{ r.label }}
              </td>
              <td class="py-2 px-3 text-right">{{ r.precoAtual }}</td>
              <td class="py-2 px-3 text-right font-semibold" :class="r.variacao >= 0 ? 'text-emerald-600' : 'text-red-600'">
                {{ r.variacao >= 0 ? '+' : '' }}{{ r.variacao.toFixed(2) }}%
              </td>
              <td class="py-2 px-3 text-right">{{ r.maxima }}</td>
              <td class="py-2 px-3 text-right">{{ r.minima }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, nextTick, onUnmounted } from 'vue'
import cotacaoApi from '../../api/cotacaoApi'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'
import { Chart, registerables } from 'chart.js'
Chart.register(...registerables)

const chartRef = ref(null)
let chartInstance = null
const selecionadas = ref(['', ''])
const moedasDisponiveis = ref([])
const periodoHoras = ref(24)
const loadingChart = ref(false)
const datasets = ref([])
const resumoComparativo = ref([])

const periodos = [
  { label: '6h', horas: 6 },
  { label: '24h', horas: 24 },
  { label: '3d', horas: 72 },
  { label: '7d', horas: 168 },
  { label: '30d', horas: 720 }
]

const cores = ['#6366f1', '#f59e0b', '#10b981']

onMounted(async () => {
  try {
    const res = await cotacaoApi.listarUltimas()
    moedasDisponiveis.value = (res.data || []).map(c => ({
      key: `${c.moeda}/${c.parMoeda}`,
      moeda: c.moeda,
      parMoeda: c.parMoeda
    }))
  } catch (e) { console.error(e) }
})

watch(selecionadas, () => { carregarHistoricos() }, { deep: true })

async function carregarHistoricos() {
  const pares = selecionadas.value.filter(s => s)
  if (pares.length === 0) { datasets.value = []; resumoComparativo.value = []; return }

  loadingChart.value = true
  try {
    const results = await Promise.all(pares.map(par => {
      const [moeda, parMoeda] = par.split('/')
      return cotacaoApi.historico(moeda, parMoeda, periodoHoras.value)
        .then(r => ({ par, data: r.data || [] }))
        .catch(() => ({ par, data: [] }))
    }))

    const newDatasets = []
    const newResumo = []

    results.forEach((result, idx) => {
      if (!result.data.length) return
      const pontos = result.data.sort((a, b) => new Date(a.dataHora) - new Date(b.dataHora))
      const precoInicial = Number(pontos[0].precoCompra || pontos[0].precoVenda)
      const precoFinal = Number(pontos[pontos.length - 1].precoCompra || pontos[pontos.length - 1].precoVenda)

      // Normalizar para variação percentual
      const labels = pontos.map(p => {
        const d = new Date(p.dataHora)
        return periodoHoras.value <= 24
          ? d.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })
          : d.toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit', hour: '2-digit', minute: '2-digit' })
      })

      const dataPercent = pontos.map(p => {
        const preco = Number(p.precoCompra || p.precoVenda)
        return ((preco - precoInicial) / precoInicial * 100)
      })

      const precos = pontos.map(p => Number(p.precoCompra || p.precoVenda))

      newDatasets.push({
        label: result.par,
        data: dataPercent,
        labels,
        borderColor: cores[idx],
        backgroundColor: cores[idx] + '20',
        tension: 0.3,
        pointRadius: 0,
        borderWidth: 2
      })

      newResumo.push({
        key: result.par,
        label: result.par,
        color: cores[idx],
        precoAtual: formatNum(precoFinal),
        variacao: ((precoFinal - precoInicial) / precoInicial) * 100,
        maxima: formatNum(Math.max(...precos)),
        minima: formatNum(Math.min(...precos))
      })
    })

    datasets.value = newDatasets
    resumoComparativo.value = newResumo
    await nextTick()
    renderChart()
  } catch (e) { console.error(e) }
  finally { loadingChart.value = false }
}

function renderChart() {
  if (!chartRef.value || datasets.value.length === 0) return
  if (chartInstance) chartInstance.destroy()

  const maxLabels = Math.max(...datasets.value.map(d => d.labels.length))
  const longestDataset = datasets.value.find(d => d.labels.length === maxLabels)

  chartInstance = new Chart(chartRef.value, {
    type: 'line',
    data: {
      labels: longestDataset?.labels || [],
      datasets: datasets.value.map(d => ({
        label: d.label,
        data: d.data,
        borderColor: d.borderColor,
        backgroundColor: d.backgroundColor,
        tension: d.tension,
        pointRadius: d.pointRadius,
        borderWidth: d.borderWidth,
        fill: false
      }))
    },
    options: {
      responsive: true,
      interaction: { mode: 'index', intersect: false },
      plugins: {
        legend: { position: 'top' },
        tooltip: {
          callbacks: {
            label: ctx => `${ctx.dataset.label}: ${ctx.parsed.y >= 0 ? '+' : ''}${ctx.parsed.y.toFixed(2)}%`
          }
        }
      },
      scales: {
        y: {
          title: { display: true, text: 'Variação (%)' },
          ticks: { callback: v => v.toFixed(1) + '%' }
        },
        x: {
          ticks: { maxTicksLimit: 12 }
        }
      }
    }
  })
}

function formatNum(n) {
  if (n >= 1000) return Number(n).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
  if (n >= 1) return Number(n).toLocaleString('pt-BR', { minimumFractionDigits: 4, maximumFractionDigits: 4 })
  return Number(n).toLocaleString('pt-BR', { minimumFractionDigits: 6, maximumFractionDigits: 8 })
}

onUnmounted(() => { if (chartInstance) chartInstance.destroy() })
</script>

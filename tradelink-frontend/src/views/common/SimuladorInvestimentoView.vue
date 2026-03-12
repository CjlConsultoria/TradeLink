<template>
  <div>
    <h2 class="page-title">Simulador de Investimentos</h2>
    <p class="text-sm text-gray-500 mb-6">Descubra quanto você teria hoje se tivesse investido em uma moeda no passado.</p>

    <!-- Formulário -->
    <div class="card p-6 mb-6">
      <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-4">
        <div>
          <label class="text-xs font-medium text-gray-500 mb-1 block">Moeda</label>
          <select v-model="moedaSelecionada" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm">
            <option value="">Selecione...</option>
            <option v-for="c in moedasDisponiveis" :key="c.key" :value="c.key">
              {{ c.moeda }}/{{ c.parMoeda }}
            </option>
          </select>
        </div>
        <div>
          <label class="text-xs font-medium text-gray-500 mb-1 block">Valor investido (R$)</label>
          <input v-model.number="valorInvestido" type="number" min="1" step="100"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm" placeholder="1000" />
        </div>
        <div>
          <label class="text-xs font-medium text-gray-500 mb-1 block">Período</label>
          <select v-model="periodoHoras" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm">
            <option :value="24">Últimas 24h</option>
            <option :value="72">Últimos 3 dias</option>
            <option :value="168">Última semana</option>
            <option :value="720">Último mês</option>
          </select>
        </div>
        <div class="flex items-end">
          <button type="button" @click="simular" :disabled="!moedaSelecionada || !valorInvestido || loading"
            class="w-full px-4 py-2 bg-indigo-600 text-white rounded-lg text-sm font-medium hover:bg-indigo-700 disabled:opacity-50 transition-colors">
            {{ loading ? 'Simulando...' : 'Simular' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Resultado -->
    <div v-if="resultado" class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4 mb-6">
      <div class="card p-6 text-center">
        <p class="text-sm text-gray-500">Investimento inicial</p>
        <p class="text-2xl font-bold text-gray-900 mt-1">{{ formatCurrency(resultado.valorInicial) }}</p>
      </div>
      <div class="card p-6 text-center" :class="resultado.lucro >= 0 ? 'border-emerald-200' : 'border-red-200'">
        <p class="text-sm text-gray-500">Valor hoje</p>
        <p class="text-2xl font-bold mt-1" :class="resultado.lucro >= 0 ? 'text-emerald-600' : 'text-red-600'">
          {{ formatCurrency(resultado.valorFinal) }}
        </p>
      </div>
      <div class="card p-6 text-center">
        <p class="text-sm text-gray-500">Resultado</p>
        <p class="text-2xl font-bold mt-1" :class="resultado.lucro >= 0 ? 'text-emerald-600' : 'text-red-600'">
          {{ resultado.lucro >= 0 ? '+' : '' }}{{ formatCurrency(resultado.lucro) }}
          <span class="text-sm font-medium">({{ resultado.percentual >= 0 ? '+' : '' }}{{ resultado.percentual.toFixed(2) }}%)</span>
        </p>
      </div>
    </div>

    <!-- Gráfico de evolução -->
    <div v-if="resultado" class="card p-6 mb-6">
      <h3 class="section-title">Evolução do Investimento</h3>
      <canvas ref="chartRef" height="250"></canvas>
    </div>

    <!-- Simulações rápidas -->
    <div v-if="moedasDisponiveis.length && !resultado" class="card p-6">
      <h3 class="section-title">Simulações Rápidas — R$ 1.000 investidos há 7 dias</h3>
      <LoadingSpinner v-if="loadingRapido" size="sm" text="Calculando..." />
      <div v-else class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-3">
        <div v-for="s in simulacoesRapidas" :key="s.par"
          class="p-4 border rounded-lg cursor-pointer hover:border-indigo-300 transition-colors"
          :class="s.lucro >= 0 ? 'bg-emerald-50 border-emerald-200' : 'bg-red-50 border-red-200'"
          @click="moedaSelecionada = s.par; valorInvestido = 1000; periodoHoras = 168; simular()">
          <div class="flex items-center justify-between">
            <span class="font-semibold text-sm">{{ s.par }}</span>
            <span class="text-xs font-bold" :class="s.lucro >= 0 ? 'text-emerald-600' : 'text-red-600'">
              {{ s.lucro >= 0 ? '+' : '' }}{{ s.percentual.toFixed(2) }}%
            </span>
          </div>
          <p class="text-lg font-bold mt-1" :class="s.lucro >= 0 ? 'text-emerald-700' : 'text-red-700'">
            R$ {{ s.valorFinal.toLocaleString('pt-BR', { minimumFractionDigits: 2 }) }}
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, onUnmounted } from 'vue'
import cotacaoApi from '../../api/cotacaoApi'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'
import { Chart, registerables } from 'chart.js'
Chart.register(...registerables)

const chartRef = ref(null)
let chartInstance = null
const moedasDisponiveis = ref([])
const moedaSelecionada = ref('')
const valorInvestido = ref(1000)
const periodoHoras = ref(168)
const loading = ref(false)
const loadingRapido = ref(false)
const resultado = ref(null)
const simulacoesRapidas = ref([])

onMounted(async () => {
  try {
    const res = await cotacaoApi.listarUltimas()
    moedasDisponiveis.value = (res.data || []).map(c => ({
      key: `${c.moeda}/${c.parMoeda}`,
      moeda: c.moeda,
      parMoeda: c.parMoeda
    }))
    carregarSimulacoesRapidas()
  } catch (e) { console.error(e) }
})

async function carregarSimulacoesRapidas() {
  loadingRapido.value = true
  const topPares = moedasDisponiveis.value.slice(0, 9)
  const results = await Promise.all(topPares.map(async par => {
    try {
      const res = await cotacaoApi.historico(par.moeda, par.parMoeda, 168)
      const pontos = (res.data || []).sort((a, b) => new Date(a.dataHora) - new Date(b.dataHora))
      if (pontos.length < 2) return null
      const precoInicial = Number(pontos[0].precoCompra || pontos[0].precoVenda)
      const precoFinal = Number(pontos[pontos.length - 1].precoCompra || pontos[pontos.length - 1].precoVenda)
      const valorFinal = (1000 / precoInicial) * precoFinal
      return {
        par: par.key,
        valorFinal,
        lucro: valorFinal - 1000,
        percentual: ((precoFinal - precoInicial) / precoInicial) * 100
      }
    } catch { return null }
  }))
  simulacoesRapidas.value = results.filter(Boolean).sort((a, b) => b.percentual - a.percentual)
  loadingRapido.value = false
}

async function simular() {
  if (!moedaSelecionada.value || !valorInvestido.value) return
  loading.value = true
  resultado.value = null
  try {
    const [moeda, parMoeda] = moedaSelecionada.value.split('/')
    const res = await cotacaoApi.historico(moeda, parMoeda, periodoHoras.value)
    const pontos = (res.data || []).sort((a, b) => new Date(a.dataHora) - new Date(b.dataHora))
    if (pontos.length < 2) { loading.value = false; return }

    const precoInicial = Number(pontos[0].precoCompra || pontos[0].precoVenda)
    const precoFinal = Number(pontos[pontos.length - 1].precoCompra || pontos[pontos.length - 1].precoVenda)
    const quantidade = valorInvestido.value / precoInicial
    const valorFinal = quantidade * precoFinal
    const lucro = valorFinal - valorInvestido.value
    const percentual = ((precoFinal - precoInicial) / precoInicial) * 100

    resultado.value = {
      valorInicial: valorInvestido.value,
      valorFinal,
      lucro,
      percentual,
      pontos
    }

    await nextTick()
    renderChart(pontos, quantidade)
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

function renderChart(pontos, quantidade) {
  if (!chartRef.value) return
  if (chartInstance) chartInstance.destroy()

  const labels = pontos.map(p => {
    const d = new Date(p.dataHora)
    return periodoHoras.value <= 24
      ? d.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })
      : d.toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit' })
  })
  const valores = pontos.map(p => quantidade * Number(p.precoCompra || p.precoVenda))
  const isPositive = valores[valores.length - 1] >= valorInvestido.value

  chartInstance = new Chart(chartRef.value, {
    type: 'line',
    data: {
      labels,
      datasets: [{
        label: 'Valor do investimento (R$)',
        data: valores,
        borderColor: isPositive ? '#10b981' : '#ef4444',
        backgroundColor: isPositive ? 'rgba(16,185,129,0.1)' : 'rgba(239,68,68,0.1)',
        fill: true,
        tension: 0.3,
        pointRadius: 0,
        borderWidth: 2
      }, {
        label: 'Investimento inicial',
        data: valores.map(() => valorInvestido.value),
        borderColor: '#94a3b8',
        borderDash: [6, 4],
        borderWidth: 1,
        pointRadius: 0,
        fill: false
      }]
    },
    options: {
      responsive: true,
      interaction: { mode: 'index', intersect: false },
      plugins: { legend: { position: 'top' } },
      scales: {
        y: { ticks: { callback: v => 'R$ ' + Number(v).toLocaleString('pt-BR', { minimumFractionDigits: 0 }) } },
        x: { ticks: { maxTicksLimit: 12 } }
      }
    }
  })
}

function formatCurrency(v) {
  return 'R$ ' + Number(v).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

onUnmounted(() => { if (chartInstance) chartInstance.destroy() })
</script>

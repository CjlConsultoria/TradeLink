<template>
  <div>
    <div v-if="loading" class="flex justify-center py-6">
      <LoadingSpinner />
    </div>
    <div v-else-if="erro" class="text-sm text-red-600">{{ erro }}</div>
    <div v-else-if="dados.length">
      <Line :data="chartData" :options="chartOptions" />
    </div>
    <div v-else class="text-center py-6">
      <p class="text-sm text-gray-500 mb-3">Nenhum snapshot disponivel.</p>
      <button v-if="consultor" @click="criarSnapshot" :disabled="criando"
        class="text-sm text-indigo-600 hover:underline">
        {{ criando ? 'Criando...' : 'Criar snapshot agora' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Line } from 'vue-chartjs'
import { Chart as ChartJS, CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Legend, Filler } from 'chart.js'
import alocacaoApi from '../../api/alocacaoApi'
import LoadingSpinner from '../common/LoadingSpinner.vue'

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Legend, Filler)

const props = defineProps({
  carteiraId: [Number, String],
  clienteId: { type: [Number, String], default: null },
  consultor: { type: Boolean, default: false }
})

const dados = ref([])
const loading = ref(false)
const erro = ref('')
const criando = ref(false)

const chartData = ref({ labels: [], datasets: [] })
const chartOptions = {
  responsive: true,
  maintainAspectRatio: true,
  interaction: { mode: 'index', intersect: false },
  plugins: {
    legend: { position: 'top' },
    tooltip: {
      callbacks: {
        label: (ctx) => `${ctx.dataset.label}: $${Number(ctx.parsed.y).toLocaleString('pt-BR', { minimumFractionDigits: 2 })}`
      }
    }
  },
  scales: {
    y: {
      beginAtZero: false,
      ticks: {
        callback: (v) => '$' + Number(v).toLocaleString('pt-BR', { minimumFractionDigits: 0 })
      }
    }
  }
}

async function carregar() {
  loading.value = true
  erro.value = ''
  try {
    const res = props.consultor
      ? await alocacaoApi.performanceConsultor(props.carteiraId, props.clienteId)
      : await alocacaoApi.performanceCliente(props.carteiraId)
    dados.value = res.data || []
    if (dados.value.length) {
      chartData.value = {
        labels: dados.value.map(d => d.data),
        datasets: [
          {
            label: 'Carteira HODL',
            data: dados.value.map(d => d.valorPortfolio),
            borderColor: '#6366f1',
            backgroundColor: 'rgba(99,102,241,0.1)',
            fill: true,
            tension: 0.3
          },
          {
            label: 'Buy & Hold BTC',
            data: dados.value.map(d => d.valorBtcHold),
            borderColor: '#f59e0b',
            backgroundColor: 'rgba(245,158,11,0.1)',
            fill: true,
            tension: 0.3
          }
        ]
      }
    }
  } catch (e) {
    erro.value = e.response?.data?.erro || 'Erro ao carregar performance.'
  } finally {
    loading.value = false
  }
}

async function criarSnapshot() {
  criando.value = true
  try {
    await alocacaoApi.criarSnapshot(props.carteiraId)
    await carregar()
  } catch (e) {
    erro.value = e.response?.data?.erro || 'Erro ao criar snapshot.'
  } finally {
    criando.value = false
  }
}

onMounted(carregar)
defineExpose({ carregar })
</script>

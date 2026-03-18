<template>
  <div class="flex flex-col items-center">
    <div class="w-full max-w-xs">
      <Doughnut :data="chartData" :options="chartOptions" />
    </div>
    <div class="flex gap-4 mt-3 flex-wrap justify-center">
      <span v-for="(item, i) in labels" :key="i" class="flex items-center gap-1.5 text-xs text-gray-600">
        <span class="w-2.5 h-2.5 rounded-full inline-block" :style="{ backgroundColor: colors[i] }"></span>
        {{ item }}
      </span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Doughnut } from 'vue-chartjs'
import { Chart as ChartJS, ArcElement, Tooltip, Legend } from 'chart.js'

ChartJS.register(ArcElement, Tooltip, Legend)

const props = defineProps({
  ativos: { type: Array, default: () => [] },
  alocacoesAlvo: { type: Array, default: () => [] },
  modo: { type: String, default: 'comparativo' } // 'comparativo' | 'atual' | 'ideal'
})

const palette = ['#6366f1','#f59e0b','#10b981','#ef4444','#3b82f6','#8b5cf6','#ec4899','#14b8a6','#f97316','#84cc16','#06b6d4','#e11d48']

const labels = computed(() => {
  if (props.modo === 'ideal') return props.alocacoesAlvo.map(a => a.simbolo)
  return props.ativos.map(a => a.simbolo)
})

const colors = computed(() => labels.value.map((_, i) => palette[i % palette.length]))

const chartData = computed(() => {
  if (props.modo === 'comparativo') {
    return {
      labels: labels.value,
      datasets: [
        {
          label: '% Atual',
          data: props.ativos.map(a => Number(a.percentualAtual || 0).toFixed(1)),
          backgroundColor: colors.value,
          borderWidth: 2,
          borderColor: '#fff'
        },
        {
          label: '% Ideal',
          data: props.ativos.map(a => Number(a.percentualAlvo || 0).toFixed(1)),
          backgroundColor: colors.value.map(c => c + '80'),
          borderWidth: 2,
          borderColor: '#fff'
        }
      ]
    }
  }
  if (props.modo === 'ideal') {
    return {
      labels: labels.value,
      datasets: [{
        data: props.alocacoesAlvo.map(a => Number(a.percentualAlvo || 0).toFixed(1)),
        backgroundColor: colors.value,
        borderWidth: 2,
        borderColor: '#fff'
      }]
    }
  }
  return {
    labels: labels.value,
    datasets: [{
      data: props.ativos.map(a => Number(a.percentualAtual || 0).toFixed(1)),
      backgroundColor: colors.value,
      borderWidth: 2,
      borderColor: '#fff'
    }]
  }
})

const chartOptions = computed(() => ({
  responsive: true,
  maintainAspectRatio: true,
  cutout: props.modo === 'comparativo' ? '40%' : '55%',
  plugins: {
    legend: { display: false },
    tooltip: {
      callbacks: {
        label: (ctx) => `${ctx.dataset.label || ''} ${ctx.label}: ${ctx.parsed}%`
      }
    }
  }
}))
</script>

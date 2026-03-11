<template>
  <div class="relative">
    <!-- Toggle tipo de gráfico -->
    <div class="flex items-center gap-2 mb-3">
      <button v-for="t in tiposGrafico" :key="t.value" type="button" @click="tipoGrafico = t.value"
        :class="tipoGrafico === t.value ? 'bg-indigo-600 text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'"
        class="px-3 py-1.5 rounded text-xs font-medium transition-colors">
        {{ t.label }}
      </button>
    </div>
    <div class="relative" :style="{ height: altura + 'px' }">
      <canvas ref="chartCanvas"></canvas>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, nextTick, onUnmounted } from 'vue'
import { Chart, registerables } from 'chart.js'

Chart.register(...registerables)

const props = defineProps({
  dados: { type: Array, default: () => [] },
  moeda: { type: String, default: '' },
  parMoeda: { type: String, default: '' },
  altura: { type: Number, default: 400 }
})

const chartCanvas = ref(null)
const tipoGrafico = ref('candlestick')
let chartInstance = null

const tiposGrafico = [
  { value: 'candlestick', label: 'Candlestick' },
  { value: 'line', label: 'Linha' }
]

// Plugin customizado para desenhar candlesticks
const candlestickPlugin = {
  id: 'candlestick',
  afterDatasetsDraw(chart) {
    if (tipoGrafico.value !== 'candlestick') return
    const { ctx, chartArea, scales } = chart
    if (!chartArea || !scales.x || !scales.y) return
    const data = props.dados
    if (!data || data.length === 0) return

    const barWidth = Math.max(2, Math.min(12, (chartArea.width / data.length) * 0.6))

    data.forEach((d, i) => {
      const x = scales.x.getPixelForValue(i)
      const openY = scales.y.getPixelForValue(Number(d.open))
      const closeY = scales.y.getPixelForValue(Number(d.close))
      const highY = scales.y.getPixelForValue(Number(d.high))
      const lowY = scales.y.getPixelForValue(Number(d.low))

      const isUp = Number(d.close) >= Number(d.open)
      const color = isUp ? '#22c55e' : '#ef4444'
      const bodyTop = Math.min(openY, closeY)
      const bodyBottom = Math.max(openY, closeY)
      const bodyHeight = Math.max(1, bodyBottom - bodyTop)

      // Pavio (wick)
      ctx.beginPath()
      ctx.strokeStyle = color
      ctx.lineWidth = 1
      ctx.moveTo(x, highY)
      ctx.lineTo(x, lowY)
      ctx.stroke()

      // Corpo (body)
      ctx.fillStyle = isUp ? 'rgba(34, 197, 94, 0.8)' : 'rgba(239, 68, 68, 0.8)'
      ctx.fillRect(x - barWidth / 2, bodyTop, barWidth, bodyHeight)
      ctx.strokeStyle = color
      ctx.lineWidth = 1
      ctx.strokeRect(x - barWidth / 2, bodyTop, barWidth, bodyHeight)
    })
  }
}

function formatLabel(dataHora) {
  if (!dataHora) return ''
  const d = new Date(dataHora)
  const day = String(d.getDate()).padStart(2, '0')
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const year = String(d.getFullYear()).slice(2)
  return `${day}/${month}/${year}`
}

function renderChart() {
  if (!chartCanvas.value || !props.dados || props.dados.length === 0) return
  if (chartInstance) chartInstance.destroy()

  const labels = props.dados.map(d => formatLabel(d.dataHora))
  const closes = props.dados.map(d => Number(d.close))
  const volumes = props.dados.map(d => d.volume ? Number(d.volume) : 0)
  const highs = props.dados.map(d => Number(d.high))
  const lows = props.dados.map(d => Number(d.low))

  // Calcular min/max para escala Y com padding
  const allPrices = [...highs, ...lows].filter(v => v > 0)
  const minPrice = allPrices.length > 0 ? Math.min(...allPrices) * 0.998 : 0
  const maxPrice = allPrices.length > 0 ? Math.max(...allPrices) * 1.002 : 100

  const isCandlestick = tipoGrafico.value === 'candlestick'

  const datasets = []

  if (isCandlestick) {
    // Dataset invisível para criar a escala Y correta
    datasets.push({
      label: 'Preço',
      data: closes,
      borderColor: 'transparent',
      backgroundColor: 'transparent',
      pointRadius: 0,
      yAxisID: 'y'
    })
  } else {
    // Gráfico de linha
    datasets.push({
      label: `${props.moeda}/${props.parMoeda} (Close)`,
      data: closes,
      borderColor: 'rgb(79, 70, 229)',
      backgroundColor: 'rgba(79, 70, 229, 0.1)',
      fill: true,
      tension: 0.3,
      pointRadius: props.dados.length > 60 ? 0 : 2,
      borderWidth: 2,
      yAxisID: 'y'
    })
  }

  // Volume como barras (eixo secundário)
  const hasVolume = volumes.some(v => v > 0)
  if (hasVolume) {
    const volumeColors = props.dados.map(d =>
      Number(d.close) >= Number(d.open) ? 'rgba(34, 197, 94, 0.3)' : 'rgba(239, 68, 68, 0.3)'
    )
    datasets.push({
      label: 'Volume',
      data: volumes,
      type: 'bar',
      backgroundColor: volumeColors,
      yAxisID: 'yVolume',
      barPercentage: 0.8,
      categoryPercentage: 0.9
    })
  }

  const plugins = isCandlestick ? [candlestickPlugin] : []

  chartInstance = new Chart(chartCanvas.value, {
    type: isCandlestick ? 'bar' : 'line',
    data: { labels, datasets },
    plugins,
    options: {
      responsive: true,
      maintainAspectRatio: false,
      interaction: {
        mode: 'index',
        intersect: false
      },
      plugins: {
        legend: {
          display: !isCandlestick,
          position: 'top'
        },
        tooltip: {
          callbacks: {
            title(items) {
              return items[0]?.label || ''
            },
            label(ctx) {
              if (ctx.dataset.yAxisID === 'yVolume') {
                return `Volume: ${Number(ctx.raw).toLocaleString('pt-BR')}`
              }
              const idx = ctx.dataIndex
              const d = props.dados[idx]
              if (!d) return ''
              if (isCandlestick) {
                return [
                  `Abertura: ${Number(d.open).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 8 })}`,
                  `Máxima: ${Number(d.high).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 8 })}`,
                  `Mínima: ${Number(d.low).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 8 })}`,
                  `Fechamento: ${Number(d.close).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 8 })}`
                ]
              }
              return `${ctx.dataset.label}: ${Number(ctx.raw).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 8 })}`
            }
          }
        }
      },
      scales: {
        x: {
          ticks: {
            maxTicksLimit: 12,
            maxRotation: 45,
            font: { size: 10 }
          },
          grid: { display: false }
        },
        y: {
          position: 'right',
          min: minPrice,
          max: maxPrice,
          ticks: {
            font: { size: 10 },
            callback(value) {
              return value.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 4 })
            }
          },
          grid: { color: 'rgba(0,0,0,0.05)' }
        },
        ...(hasVolume ? {
          yVolume: {
            position: 'left',
            display: false,
            beginAtZero: true,
            max: Math.max(...volumes) * 4 // Volume ocupa ~25% do gráfico
          }
        } : {})
      }
    }
  })
}

watch(() => props.dados, () => nextTick(renderChart), { deep: true })
watch(tipoGrafico, () => nextTick(renderChart))

onMounted(() => {
  if (props.dados && props.dados.length > 0) {
    nextTick(renderChart)
  }
})

onUnmounted(() => {
  if (chartInstance) chartInstance.destroy()
})
</script>

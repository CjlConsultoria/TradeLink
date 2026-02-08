<template>
  <div>
    <h2 class="text-2xl font-bold text-gray-900 mb-6">Meus Relatórios</h2>
    <p class="text-gray-600 mb-6">Acompanhe quanto você investiu, quanto vendeu e seu resultado (ganho ou perda) com base nas operações que você registrou.</p>

    <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-4 mb-6">
      <p class="text-sm font-medium text-gray-700 mb-2">Período</p>
      <div class="flex flex-wrap gap-3 items-end">
        <div>
          <label class="block text-xs text-gray-500 mb-0.5">De</label>
          <input v-model="filtros.dataDe" type="date" class="px-3 py-2 border border-gray-300 rounded-lg text-sm" />
        </div>
        <div>
          <label class="block text-xs text-gray-500 mb-0.5">Até</label>
          <input v-model="filtros.dataAte" type="date" class="px-3 py-2 border border-gray-300 rounded-lg text-sm" />
        </div>
        <button type="button" @click="carregar" class="px-4 py-2 bg-indigo-600 text-white rounded-lg text-sm hover:bg-indigo-700">Atualizar</button>
        <button type="button" @click="exportarPdfOperacoes" :disabled="exportandoPdf" class="px-4 py-2 bg-gray-700 text-white rounded-lg text-sm hover:bg-gray-800 disabled:opacity-50">Exportar PDF (operações)</button>
        <button type="button" @click="exportarPdfResumo" :disabled="exportandoPdf" class="px-4 py-2 bg-gray-700 text-white rounded-lg text-sm hover:bg-gray-800 disabled:opacity-50">Exportar PDF (resumo)</button>
      </div>
    </div>

    <div class="flex flex-wrap gap-2 border-b border-gray-200 mb-4">
      <button v-for="t in tabs" :key="t.id" @click="tabAtiva = t.id"
        :class="tabAtiva === t.id ? 'bg-indigo-100 text-indigo-800 border-indigo-500' : 'bg-white text-gray-600 border-transparent'"
        class="px-4 py-2 rounded-t-lg border-b-2 text-sm font-medium">
        {{ t.label }}
      </button>
    </div>

    <LoadingSpinner v-if="loading" />

    <template v-else>
      <!-- Histórico: lista de operações -->
      <div v-show="tabAtiva === 'historico'" class="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
        <table class="w-full text-sm">
          <thead class="bg-gray-50 border-b border-gray-200">
            <tr>
              <th class="text-left py-3 px-3 font-medium text-gray-500">Data</th>
              <th class="text-left py-3 px-3 font-medium text-gray-500">Carteira</th>
              <th class="text-left py-3 px-3 font-medium text-gray-500">Par</th>
              <th class="text-left py-3 px-3 font-medium text-gray-500">Tipo</th>
              <th class="text-right py-3 px-3 font-medium text-gray-500">Preço</th>
              <th class="text-right py-3 px-3 font-medium text-gray-500">Qtd</th>
              <th class="text-right py-3 px-3 font-medium text-gray-500">Valor</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="op in operacoes" :key="op.id" class="border-b border-gray-100 hover:bg-gray-50">
              <td class="py-2 px-3 text-gray-600">{{ formatDate(op.dataExecucao) }}</td>
              <td class="py-2 px-3">{{ op.carteiraNome }}</td>
              <td class="py-2 px-3 font-medium">{{ op.recomendacaoMoedaPar }}</td>
              <td class="py-2 px-3">
                <span class="px-2 py-0.5 rounded text-xs font-medium" :class="op.tipo === 'COMPRA' ? 'bg-emerald-100 text-emerald-800' : 'bg-rose-100 text-rose-800'">{{ op.tipo }}</span>
              </td>
              <td class="py-2 px-3 text-right">{{ formatCurrency(op.precoExecutado) }}</td>
              <td class="py-2 px-3 text-right">{{ op.quantidade }}</td>
              <td class="py-2 px-3 text-right font-medium">{{ formatCurrency(op.valorOperacao) }}</td>
            </tr>
          </tbody>
        </table>
        <p v-if="operacoes.length === 0" class="p-6 text-gray-500 text-center">
          Nenhuma operação no período. Registre compras e vendas nas recomendações das suas carteiras para ver o histórico aqui.
        </p>
      </div>

      <!-- Resumo: ganho/perda total e por moeda -->
      <div v-show="tabAtiva === 'resumo'" class="space-y-6">
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
            <p class="text-sm text-gray-500">Total investido (compras)</p>
            <p class="text-2xl font-bold text-emerald-600 mt-1">{{ formatCurrency(resumo?.valorTotalCompras) }}</p>
            <p class="text-xs text-gray-400 mt-0.5">{{ resumo?.totalCompras ?? 0 }} operação(ões)</p>
          </div>
          <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
            <p class="text-sm text-gray-500">Total vendido</p>
            <p class="text-2xl font-bold text-rose-600 mt-1">{{ formatCurrency(resumo?.valorTotalVendas) }}</p>
            <p class="text-xs text-gray-400 mt-0.5">{{ resumo?.totalVendas ?? 0 }} operação(ões)</p>
          </div>
          <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6 md:col-span-2">
            <p class="text-sm text-gray-500">Resultado (ganho ou perda)</p>
            <p class="text-3xl font-bold mt-1" :class="resultadoPositivo ? 'text-emerald-600' : 'text-rose-600'">
              {{ formatCurrency(resumo?.resultado) }}
            </p>
            <p class="text-sm mt-1" :class="resultadoPositivo ? 'text-emerald-600' : 'text-rose-600'">
              {{ resultadoPositivo ? 'Lucro no período' : 'Prejuízo no período' }}
            </p>
          </div>
        </div>

        <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
          <h3 class="text-lg font-semibold mb-4">Resultado por moeda/par</h3>
          <div class="overflow-x-auto">
            <table class="w-full text-sm">
              <thead class="bg-gray-50 border-b border-gray-200">
                <tr>
                  <th class="text-left py-3 px-3 font-medium text-gray-500">Par</th>
                  <th class="text-right py-3 px-3 font-medium text-gray-500">Total compras</th>
                  <th class="text-right py-3 px-3 font-medium text-gray-500">Total vendas</th>
                  <th class="text-right py-3 px-3 font-medium text-gray-500">Resultado</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in (resumo?.perdasGanhosPorMoeda || [])" :key="item.moedaPar" class="border-b border-gray-100 hover:bg-gray-50">
                  <td class="py-2 px-3 font-medium">{{ item.moedaPar }}</td>
                  <td class="py-2 px-3 text-right">{{ formatCurrency(item.valorTotalCompras) }}</td>
                  <td class="py-2 px-3 text-right">{{ formatCurrency(item.valorTotalVendas) }}</td>
                  <td class="py-2 px-3 text-right font-semibold" :class="Number(item.resultado) >= 0 ? 'text-emerald-600' : 'text-rose-600'">{{ formatCurrency(item.resultado) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
          <p v-if="!(resumo?.perdasGanhosPorMoeda?.length)" class="text-gray-500 py-4 text-center">Nenhum dado por moeda no período.</p>
        </div>
      </div>

      <!-- Gráficos -->
      <div v-show="tabAtiva === 'graficos'" class="space-y-6">
        <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
          <h3 class="text-lg font-semibold mb-4">Operações por dia</h3>
          <div class="h-64">
            <Bar v-if="chartPeriodoData.labels.length" :data="chartPeriodoData" :options="chartOptions" />
            <p v-else class="text-gray-500 text-sm">Sem dados no período.</p>
          </div>
        </div>
        <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
          <h3 class="text-lg font-semibold mb-4">Operações por moeda/par</h3>
          <div class="h-64">
            <Doughnut v-if="chartMoedaData.labels.length" :data="chartMoedaData" :options="{ responsive: true, maintainAspectRatio: false }" />
            <p v-else class="text-gray-500 text-sm">Sem dados no período.</p>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Bar, Doughnut } from 'vue-chartjs'
import { Chart as ChartJS, CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend, ArcElement } from 'chart.js'
import relatorioClienteApi from '../../api/relatorioClienteApi'
import { formatCurrency, formatDate } from '../../utils/formatters'
import { useToast } from '../../composables/useToast'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'

const toast = useToast()

ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend, ArcElement)

const tabs = [
  { id: 'historico', label: 'Histórico de operações' },
  { id: 'resumo', label: 'Resumo (ganho/perda)' },
  { id: 'graficos', label: 'Gráficos' }
]

const loading = ref(false)
const tabAtiva = ref('resumo')
const operacoes = ref([])
const resumo = ref(null)
const filtros = ref({
  dataDe: '',
  dataAte: ''
})
const exportandoPdf = ref(false)

function buildParams() {
  const p = {}
  if (filtros.value.dataDe?.trim()) p.dataDe = filtros.value.dataDe.trim()
  if (filtros.value.dataAte?.trim()) p.dataAte = filtros.value.dataAte.trim()
  return p
}

function downloadBlob(blob, filename) {
  const url = window.URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  a.click()
  window.URL.revokeObjectURL(url)
}

async function exportarPdfOperacoes() {
  exportandoPdf.value = true
  try {
    const res = await relatorioClienteApi.operacoesPdf(buildParams())
    downloadBlob(res.data, 'relatorio-operacoes-cliente.pdf')
    toast.success('PDF exportado.')
  } catch (e) {
    console.error(e)
    toast.error('Não foi possível exportar o PDF.')
  } finally {
    exportandoPdf.value = false
  }
}

async function exportarPdfResumo() {
  exportandoPdf.value = true
  try {
    const res = await relatorioClienteApi.resumoPdf(buildParams())
    downloadBlob(res.data, 'relatorio-resumo-cliente.pdf')
    toast.success('PDF exportado.')
  } catch (e) {
    console.error(e)
    toast.error('Não foi possível exportar o PDF.')
  } finally {
    exportandoPdf.value = false
  }
}

const resultadoPositivo = computed(() => resumo.value?.resultado != null && Number(resumo.value.resultado) >= 0)

const chartOptions = computed(() => ({
  responsive: true,
  maintainAspectRatio: false,
  scales: {
    y: { beginAtZero: true }
  }
}))

const chartPeriodoData = computed(() => {
  const list = resumo.value?.operacoesPorPeriodo || []
  return {
    labels: list.map(x => x.periodo),
    datasets: [{ label: 'Operações', data: list.map(x => x.total), backgroundColor: 'rgba(99, 102, 241, 0.6)' }]
  }
})

const chartMoedaData = computed(() => {
  const list = resumo.value?.perdasGanhosPorMoeda || []
  const colors = ['#6366f1', '#8b5cf6', '#a855f7', '#d946ef', '#ec4899', '#f43f5e', '#f97316', '#eab308']
  return {
    labels: list.map(x => x.moedaPar),
    datasets: [{ data: list.map(x => Number(x.valorTotalCompras || 0) + Number(x.valorTotalVendas || 0)), backgroundColor: list.map((_, i) => colors[i % colors.length]) }]
  }
})

async function carregar() {
  loading.value = true
  try {
    const dataDe = filtros.value.dataDe?.trim() || null
    const dataAte = filtros.value.dataAte?.trim() || null
    const params = {}
    if (dataDe) params.dataDe = dataDe
    if (dataAte) params.dataAte = dataAte
    const [opRes, resumoRes] = await Promise.all([
      relatorioClienteApi.operacoes(params),
      relatorioClienteApi.resumo(params)
    ])
    operacoes.value = opRes.data || []
    resumo.value = resumoRes.data
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

onMounted(() => carregar())
</script>

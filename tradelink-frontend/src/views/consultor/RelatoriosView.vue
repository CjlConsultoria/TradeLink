<template>
  <div>
    <h2 class="text-2xl font-bold text-gray-900 mb-6">Histórico e Relatórios</h2>

    <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-4 mb-6">
      <p class="text-sm font-medium text-gray-700 mb-2">Filtros</p>
      <div class="flex flex-wrap gap-3 items-end">
        <div>
          <label class="block text-xs text-gray-500 mb-0.5">De</label>
          <input v-model="filtros.dataDe" type="date" class="px-3 py-2 border border-gray-300 rounded-lg text-sm" />
        </div>
        <div>
          <label class="block text-xs text-gray-500 mb-0.5">Até</label>
          <input v-model="filtros.dataAte" type="date" class="px-3 py-2 border border-gray-300 rounded-lg text-sm" />
        </div>
        <div>
          <label class="block text-xs text-gray-500 mb-0.5">Carteira</label>
          <select v-model="filtros.carteiraId" class="px-3 py-2 border border-gray-300 rounded-lg text-sm w-48">
            <option value="">Todas</option>
            <option v-for="c in carteiras" :key="c.id" :value="c.id">{{ c.nome }}</option>
          </select>
        </div>
        <div>
          <label class="block text-xs text-gray-500 mb-0.5">Cliente</label>
          <select v-model="filtros.clienteId" class="px-3 py-2 border border-gray-300 rounded-lg text-sm w-48">
            <option value="">Todos</option>
            <option v-for="c in clientes" :key="c.id" :value="c.id">{{ c.nome }}</option>
          </select>
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
      <!-- Histórico: lista de operações (o que o cliente registrou) -->
      <div v-show="tabAtiva === 'historico'" class="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
        <table class="w-full text-sm">
          <thead class="bg-gray-50 border-b border-gray-200">
            <tr>
              <th class="text-left py-3 px-3 font-medium text-gray-500">Data</th>
              <th class="text-left py-3 px-3 font-medium text-gray-500">Cliente</th>
              <th class="text-left py-3 px-3 font-medium text-gray-500">Carteira</th>
              <th class="text-left py-3 px-3 font-medium text-gray-500">Par</th>
              <th class="text-left py-3 px-3 font-medium text-gray-500">Tipo</th>
              <th class="text-right py-3 px-3 font-medium text-gray-500">Preço</th>
              <th class="text-right py-3 px-3 font-medium text-gray-500">Qtd</th>
              <th class="text-right py-3 px-3 font-medium text-gray-500">Valor</th>
              <th class="text-center py-3 px-3 font-medium text-gray-500">Resolvida</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="op in operacoes" :key="op.id" class="border-b border-gray-100 hover:bg-gray-50">
              <td class="py-2 px-3 text-gray-600">{{ formatDate(op.dataExecucao) }}</td>
              <td class="py-2 px-3">{{ op.clienteNome }}</td>
              <td class="py-2 px-3">{{ op.carteiraNome }}</td>
              <td class="py-2 px-3">{{ op.recomendacaoMoedaPar }}</td>
              <td class="py-2 px-3">
                <span class="px-2 py-0.5 rounded text-xs font-medium" :class="op.tipo === 'COMPRA' ? 'bg-emerald-100 text-emerald-800' : 'bg-rose-100 text-rose-800'">{{ op.tipo }}</span>
              </td>
              <td class="py-2 px-3 text-right">{{ formatCurrency(op.precoExecutado) }}</td>
              <td class="py-2 px-3 text-right">{{ op.quantidade }}</td>
              <td class="py-2 px-3 text-right font-medium">{{ formatCurrency(op.valorOperacao) }}</td>
              <td class="py-2 px-3 text-center">
                <span v-if="op.recomendacaoResolvidaPeloCliente" class="text-green-600" title="Cliente marcou como resolvida">✓</span>
                <span v-else class="text-gray-300">—</span>
              </td>
            </tr>
          </tbody>
        </table>
        <p v-if="operacoes.length === 0" class="p-6 text-gray-500 text-center">
          Nenhuma operação no período.
          <span v-if="!filtros.dataDe && !filtros.dataAte && !filtros.carteiraId && !filtros.clienteId" class="block mt-2 text-sm">Verifique se as operações foram registradas pelos clientes nas recomendações das suas carteiras.</span>
          <span v-else class="block mt-2 text-sm">Remova os filtros e clique em Atualizar para ver todos os registros.</span>
        </p>
      </div>

      <!-- Resumo + Gráficos -->
      <div v-show="tabAtiva === 'resumo'" class="space-y-6">
        <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
            <p class="text-sm text-gray-500">Total de operações</p>
            <p class="text-3xl font-bold text-indigo-600 mt-1">{{ resumo?.totalOperacoes ?? 0 }}</p>
          </div>
          <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
            <p class="text-sm text-gray-500">Compras</p>
            <p class="text-3xl font-bold text-emerald-600 mt-1">{{ resumo?.totalCompras ?? 0 }}</p>
          </div>
          <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
            <p class="text-sm text-gray-500">Vendas</p>
            <p class="text-3xl font-bold text-rose-600 mt-1">{{ resumo?.totalVendas ?? 0 }}</p>
          </div>
        </div>
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
            <h3 class="text-lg font-semibold mb-4">Operações por período</h3>
            <div class="h-64">
              <Bar v-if="chartPeriodoData.labels.length" :data="chartPeriodoData" :options="chartOptions" />
              <p v-else class="text-gray-500 text-sm">Sem dados</p>
            </div>
          </div>
          <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
            <h3 class="text-lg font-semibold mb-4">Operações por moeda/par</h3>
            <div class="h-64">
              <Doughnut v-if="chartMoedaData.labels.length" :data="chartMoedaData" :options="{ responsive: true, maintainAspectRatio: false }" />
              <p v-else class="text-gray-500 text-sm">Sem dados</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Por Carteira -->
      <div v-show="tabAtiva === 'carteira'" class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
        <div class="space-y-2">
          <div v-for="r in (resumo?.porCarteira || [])" :key="r.carteiraId" class="flex justify-between items-center py-2 px-3 bg-gray-50 rounded-lg">
            <span class="font-medium">{{ r.carteiraNome }}</span>
            <span class="text-indigo-600 font-semibold">{{ r.total }} operações</span>
          </div>
        </div>
        <p v-if="!(resumo?.porCarteira?.length)" class="text-gray-500">Nenhum dado.</p>
      </div>

      <!-- Por Cliente -->
      <div v-show="tabAtiva === 'cliente'" class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
        <div class="space-y-2">
          <div v-for="r in (resumo?.porCliente || [])" :key="r.clienteId" class="flex justify-between items-center py-2 px-3 bg-gray-50 rounded-lg">
            <span class="font-medium">{{ r.clienteNome }}</span>
            <span class="text-indigo-600 font-semibold">{{ r.total }} operações</span>
          </div>
        </div>
        <p v-if="!(resumo?.porCliente?.length)" class="text-gray-500">Nenhum dado.</p>
      </div>

      <!-- Recomendações resolvidas (cliente marcou como resolvida) -->
      <div v-show="tabAtiva === 'resolvidas'" class="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
        <p class="p-3 text-sm text-gray-600 border-b border-gray-100">Recomendações que os clientes marcaram como resolvidas (com ou sem operação registrada).</p>
        <table class="w-full text-sm">
          <thead class="bg-gray-50 border-b border-gray-200">
            <tr>
              <th class="text-left py-3 px-3 font-medium text-gray-500">Data</th>
              <th class="text-left py-3 px-3 font-medium text-gray-500">Cliente</th>
              <th class="text-left py-3 px-3 font-medium text-gray-500">Carteira</th>
              <th class="text-left py-3 px-3 font-medium text-gray-500">Par</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in (resumo?.recomendacoesResolvidas || [])" :key="item.clienteId + '-' + item.recomendacaoId" class="border-b border-gray-100 hover:bg-gray-50">
              <td class="py-2 px-3 text-gray-600">{{ formatDate(item.resolvidoEm) }}</td>
              <td class="py-2 px-3">{{ item.clienteNome }}</td>
              <td class="py-2 px-3">{{ item.carteiraNome }}</td>
              <td class="py-2 px-3 font-medium">{{ item.recomendacaoMoedaPar }}</td>
            </tr>
          </tbody>
        </table>
        <p v-if="!(resumo?.recomendacoesResolvidas?.length)" class="p-6 text-gray-500 text-center">Nenhuma recomendação marcada como resolvida no período.</p>
      </div>

      <!-- Perdas e ganhos por cliente/moeda -->
      <div v-show="tabAtiva === 'perdasganhos'" class="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
        <p class="p-3 text-sm text-gray-600 border-b border-gray-100">Comparação de valores: total comprado vs total vendido por cliente e moeda/par. Resultado = vendas − compras.</p>
        <table class="w-full text-sm">
          <thead class="bg-gray-50 border-b border-gray-200">
            <tr>
              <th class="text-left py-3 px-3 font-medium text-gray-500">Cliente</th>
              <th class="text-left py-3 px-3 font-medium text-gray-500">Moeda/Par</th>
              <th class="text-right py-3 px-3 font-medium text-gray-500">Total compras</th>
              <th class="text-right py-3 px-3 font-medium text-gray-500">Total vendas</th>
              <th class="text-right py-3 px-3 font-medium text-gray-500">Resultado</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in (resumo?.perdasGanhosPorClienteMoeda || [])" :key="item.clienteId + '-' + item.moedaPar" class="border-b border-gray-100 hover:bg-gray-50">
              <td class="py-2 px-3">{{ item.clienteNome }}</td>
              <td class="py-2 px-3 font-medium">{{ item.moedaPar }}</td>
              <td class="py-2 px-3 text-right">{{ formatCurrency(item.valorTotalCompras) }}</td>
              <td class="py-2 px-3 text-right">{{ formatCurrency(item.valorTotalVendas) }}</td>
              <td class="py-2 px-3 text-right font-semibold" :class="Number(item.resultado) >= 0 ? 'text-emerald-600' : 'text-rose-600'">{{ formatCurrency(item.resultado) }}</td>
            </tr>
          </tbody>
        </table>
        <p v-if="!(resumo?.perdasGanhosPorClienteMoeda?.length)" class="p-6 text-gray-500 text-center">Nenhum dado de perdas/ganhos no período.</p>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Bar, Doughnut } from 'vue-chartjs'
import { Chart as ChartJS, CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend, ArcElement } from 'chart.js'
import relatorioApi from '../../api/relatorioApi'
import carteiraApi from '../../api/carteiraApi'
import userApi from '../../api/userApi'
import { formatCurrency, formatDate } from '../../utils/formatters'
import { useToast } from '../../composables/useToast'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'

const toast = useToast()

ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend, ArcElement)

const tabs = [
  { id: 'historico', label: 'Histórico de operações' },
  { id: 'resumo', label: 'Resumo e gráficos' },
  { id: 'carteira', label: 'Por carteira' },
  { id: 'cliente', label: 'Por cliente' },
  { id: 'resolvidas', label: 'Recomendações resolvidas' },
  { id: 'perdasganhos', label: 'Perdas e ganhos' }
]

const loading = ref(false)
const tabAtiva = ref('historico')
const operacoes = ref([])
const resumo = ref(null)
const carteiras = ref([])
const clientes = ref([])
const filtros = ref({
  dataDe: '',
  dataAte: '',
  carteiraId: '',
  clienteId: ''
})
const exportandoPdf = ref(false)

function buildParamsOperacoes() {
  const p = {}
  if (filtros.value.dataDe?.trim()) p.dataDe = filtros.value.dataDe.trim()
  if (filtros.value.dataAte?.trim()) p.dataAte = filtros.value.dataAte.trim()
  if (filtros.value.carteiraId && String(filtros.value.carteiraId).trim()) p.carteiraId = filtros.value.carteiraId
  if (filtros.value.clienteId && String(filtros.value.clienteId).trim()) p.clienteId = filtros.value.clienteId
  return p
}

function buildParamsResumo() {
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
    const res = await relatorioApi.operacoesPdf(buildParamsOperacoes())
    downloadBlob(res.data, 'relatorio-operacoes-consultor.pdf')
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
    const res = await relatorioApi.resumoPdf(buildParamsResumo())
    downloadBlob(res.data, 'relatorio-resumo-consultor.pdf')
    toast.success('PDF exportado.')
  } catch (e) {
    console.error(e)
    toast.error('Não foi possível exportar o PDF.')
  } finally {
    exportandoPdf.value = false
  }
}

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
  const list = resumo.value?.porMoeda || []
  const colors = ['#6366f1', '#8b5cf6', '#a855f7', '#d946ef', '#ec4899', '#f43f5e', '#f97316', '#eab308']
  return {
    labels: list.map(x => x.moedaPar),
    datasets: [{ data: list.map(x => x.total), backgroundColor: list.map((_, i) => colors[i % colors.length]) }]
  }
})

async function carregar() {
  loading.value = true
  try {
    const dataDe = filtros.value.dataDe?.trim() || null
    const dataAte = filtros.value.dataAte?.trim() || null
    const carteiraId = filtros.value.carteiraId && String(filtros.value.carteiraId).trim() ? filtros.value.carteiraId : null
    const clienteId = filtros.value.clienteId && String(filtros.value.clienteId).trim() ? filtros.value.clienteId : null
    const paramsOperacoes = {}
    if (dataDe) paramsOperacoes.dataDe = dataDe
    if (dataAte) paramsOperacoes.dataAte = dataAte
    if (carteiraId) paramsOperacoes.carteiraId = carteiraId
    if (clienteId) paramsOperacoes.clienteId = clienteId
    const paramsResumo = {}
    if (dataDe) paramsResumo.dataDe = dataDe
    if (dataAte) paramsResumo.dataAte = dataAte
    const [opRes, resumoRes] = await Promise.all([
      relatorioApi.operacoes(paramsOperacoes),
      relatorioApi.resumo(paramsResumo)
    ])
    operacoes.value = opRes.data || []
    resumo.value = resumoRes.data
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  try {
    const [cartRes, cliRes] = await Promise.all([carteiraApi.listar(), userApi.listarClientes()])
    carteiras.value = cartRes.data || []
    clientes.value = cliRes.data || []
  } catch (_) {}
  await carregar()
})
</script>

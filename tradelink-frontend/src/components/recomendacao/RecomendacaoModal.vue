<template>
  <div v-if="show" class="fixed inset-0 z-50 flex items-center justify-center">
    <div class="fixed inset-0 bg-black/50" @click="$emit('close')"></div>
    <div class="bg-white rounded-lg shadow-xl max-w-lg w-full mx-4 max-h-[90vh] overflow-hidden flex flex-col relative z-10">
      <div class="p-4 border-b flex justify-between items-center">
        <h3 class="text-lg font-semibold">Nova Recomendacao</h3>
        <button type="button" @click="$emit('close')" class="text-gray-400 hover:text-gray-600">&#x2715;</button>
      </div>
      <div class="p-4 overflow-y-auto flex-1">
        <div class="mb-4">
          <label class="block text-sm font-medium text-gray-700 mb-1">Carteira</label>
          <select v-model="form.carteiraId" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm" @change="onCarteiraSelect">
            <option value="">Selecione a carteira</option>
            <option value="__nova__">+ Nova carteira</option>
            <option v-for="c in carteiras" :key="c.id" :value="c.id">{{ c.nome }}</option>
          </select>
        </div>
        <div v-if="showNewCarteiraForm" class="mb-4 p-3 bg-gray-50 rounded-lg border border-gray-200">
          <p class="text-sm font-medium text-gray-700 mb-2">Criar nova carteira</p>
          <div class="space-y-2">
            <input v-model="newCarteira.nome" placeholder="Nome da carteira" class="w-full px-3 py-2 border rounded-lg text-sm" />
            <textarea v-model="newCarteira.descricao" placeholder="Descricao (ex: so cripto)" rows="2" class="w-full px-3 py-2 border rounded-lg text-sm"></textarea>
            <div class="flex gap-2">
              <button type="button" @click="criarCarteiraEUsar" class="px-3 py-1.5 text-sm bg-indigo-600 text-white rounded-lg hover:bg-indigo-700">Criar e usar</button>
              <button type="button" @click="showNewCarteiraForm = false; form.carteiraId = carteiras[0]?.id || ''" class="px-3 py-1.5 text-sm text-gray-600 hover:underline">Cancelar</button>
            </div>
          </div>
        </div>
        <form @submit.prevent="submit" class="space-y-4">
          <div class="grid grid-cols-2 gap-3">
            <div>
              <div class="flex items-center gap-1 mb-1">
                <label class="text-sm font-medium text-gray-700">Tipo</label>
                <HelpTip field="tipo" :open="helpOpen" @toggle="toggleHelp" />
              </div>
              <HelpBox field="tipo" :open="helpOpen" text="Tipo da operacao recomendada ao cliente." example="COMPRA para recomendar compra, VENDA para recomendar venda." />
              <select v-model="form.tipo" required class="w-full px-3 py-2 border rounded-lg text-sm">
                <option value="COMPRA">COMPRA</option>
                <option value="VENDA">VENDA</option>
              </select>
            </div>
            <div>
              <div class="flex items-center gap-1 mb-1">
                <label class="text-sm font-medium text-gray-700">Par</label>
                <HelpTip field="par" :open="helpOpen" @toggle="toggleHelp" />
              </div>
              <HelpBox field="par" :open="helpOpen" text="Moeda de referencia para cotacao do ativo." example="BRL = precos em Real, USD = precos em Dolar." />
              <select v-model="form.parMoeda" required class="w-full px-3 py-2 border rounded-lg text-sm">
                <option v-for="p in PARES" :key="p.value" :value="p.value">{{ p.label }}</option>
              </select>
            </div>
          </div>

          <div>
            <div class="flex items-center gap-1 mb-1">
              <label class="text-sm font-medium text-gray-700">Moeda</label>
              <HelpTip field="moeda" :open="helpOpen" @toggle="toggleHelp" />
            </div>
            <HelpBox field="moeda" :open="helpOpen" text="O ativo que sera negociado na recomendacao." example="USD (Dolar), BTC (Bitcoin), EUR (Euro), OURO, etc." />
            <input v-model="searchMoeda" type="text" placeholder="Buscar moeda..." class="w-full px-3 py-2 border rounded-lg text-sm mb-2" />
            <div class="flex gap-2 mb-2 border-b border-gray-200 pb-2">
              <button type="button" v-for="tab in tabs" :key="tab.id" @click="activeTab = tab.id"
                :class="activeTab === tab.id ? 'bg-indigo-100 text-indigo-800' : 'bg-gray-100 text-gray-700'"
                class="px-3 py-1 rounded text-sm">{{ tab.label }}</button>
            </div>
            <div class="max-h-48 overflow-y-auto space-y-1">
              <div v-for="m in moedasFiltradas" :key="m.value" class="flex items-center justify-between py-1.5 px-2 rounded hover:bg-gray-50">
                <label class="flex items-center gap-2 cursor-pointer flex-1">
                  <input type="radio" :value="m.value" v-model="form.moeda" />
                  <span class="font-medium">{{ m.label }}</span>
                  <span v-if="m.popular" class="text-xs bg-amber-100 text-amber-800 px-1.5 py-0.5 rounded">Popular</span>
                </label>
                <span class="text-sm text-indigo-600">{{ precoAtual(m.value) }}</span>
                <button type="button" @click.stop="atualizarCotacao(m.value)" class="text-xs text-gray-500 hover:text-indigo-600 ml-1" title="Atualizar cotacao">&#x21BB;</button>
              </div>
            </div>
          </div>

          <!-- Toggle modo -->
          <div>
            <div class="flex items-center gap-1 mb-2">
              <label class="text-sm font-medium text-gray-700">Modo</label>
              <HelpTip field="modo" :open="helpOpen" @toggle="toggleHelp" />
            </div>
            <HelpBox field="modo" :open="helpOpen" text="Define como a quantidade sera calculada para cada cliente." example="Quantidade fixa: todos recebem o mesmo valor (ex: comprar 100 USD). Percentual: calcula com base no portfolio individual de cada cliente (ex: vender 10% do BTC que cada um possui)." />
            <div class="flex rounded-lg border border-gray-300 overflow-hidden">
              <button type="button" @click="form.modoPercentual = false"
                :class="!form.modoPercentual ? 'bg-indigo-600 text-white' : 'bg-white text-gray-700'"
                class="flex-1 px-3 py-2 text-sm font-medium transition-colors">Quantidade fixa</button>
              <button type="button" @click="form.modoPercentual = true"
                :class="form.modoPercentual ? 'bg-indigo-600 text-white' : 'bg-white text-gray-700'"
                class="flex-1 px-3 py-2 text-sm font-medium transition-colors">Percentual (%)</button>
            </div>
          </div>

          <div class="grid grid-cols-2 gap-3">
            <div v-if="!form.modoPercentual">
              <div class="flex items-center gap-1 mb-1">
                <label class="text-sm font-medium text-gray-700">Quantidade</label>
                <HelpTip field="quantidade" :open="helpOpen" @toggle="toggleHelp" />
              </div>
              <HelpBox field="quantidade" :open="helpOpen" text="Quantidade exata do ativo a ser negociada. Todos os clientes recebem essa mesma recomendacao." example="100 (comprar 100 USD) ou 0.5 (comprar 0.5 BTC)." />
              <input v-model="form.quantidade" type="number" step="any" class="w-full px-3 py-2 border rounded-lg text-sm" />
            </div>
            <div v-if="form.modoPercentual">
              <div class="flex items-center gap-1 mb-1">
                <label class="text-sm font-medium text-gray-700">Percentual (%)</label>
                <HelpTip field="percentual" :open="helpOpen" @toggle="toggleHelp" />
              </div>
              <HelpBox field="percentual" :open="helpOpen" text="Percentual do saldo do ativo que cada cliente deve negociar. O sistema calcula automaticamente a quantidade real para cada cliente." example="10 = vender 10% do BTC de cada cliente. Se o cliente A tem 1 BTC, vende 0.1 BTC. Se o cliente B tem 0.5 BTC, vende 0.05 BTC." />
              <input v-model="form.percentual" type="number" step="0.01" min="0.01" max="100" class="w-full px-3 py-2 border rounded-lg text-sm" placeholder="Ex: 10" />
            </div>
            <div>
              <div class="flex items-center gap-1 mb-1">
                <label class="text-sm font-medium text-gray-700">Preco Entrada</label>
                <HelpTip field="precoEntrada" :open="helpOpen" @toggle="toggleHelp" />
              </div>
              <HelpBox field="precoEntrada" :open="helpOpen" text="Preco sugerido para o cliente executar a operacao." example="Se USD/BRL, coloque 5.27 para recomendar comprar a R$ 5,27 por dolar." />
              <input v-model="form.precoEntrada" type="number" step="any" class="w-full px-3 py-2 border rounded-lg text-sm" />
            </div>
            <div>
              <div class="flex items-center gap-1 mb-1">
                <label class="text-sm font-medium text-gray-700">Preco Alvo</label>
                <HelpTip field="precoAlvo" :open="helpOpen" @toggle="toggleHelp" />
              </div>
              <HelpBox field="precoAlvo" :open="helpOpen" text="Preco objetivo para realizar o lucro. Quando o ativo atingir esse valor, o cliente deve considerar encerrar a posicao." example="5.50 = vender quando USD chegar a R$ 5,50 (lucro de ~4% sobre entrada de 5,27)." />
              <input v-model="form.precoAlvo" type="number" step="any" class="w-full px-3 py-2 border rounded-lg text-sm" />
            </div>
            <div>
              <div class="flex items-center gap-1 mb-1">
                <label class="text-sm font-medium text-gray-700">Stop Loss</label>
                <HelpTip field="stopLoss" :open="helpOpen" @toggle="toggleHelp" />
              </div>
              <HelpBox field="stopLoss" :open="helpOpen" text="Preco limite para cortar prejuizo. Se o ativo cair ate esse valor, o cliente deve encerrar a posicao para limitar a perda." example="5.10 = sair da posicao se USD cair a R$ 5,10 (perda maxima de ~3%)." />
              <input v-model="form.stopLoss" type="number" step="any" class="w-full px-3 py-2 border rounded-lg text-sm" />
            </div>
          </div>

          <!-- Preview percentual -->
          <div v-if="form.modoPercentual && form.percentual > 0 && form.moeda && form.carteiraId && form.carteiraId !== '__nova__'">
            <button type="button" @click="carregarPreview" :disabled="loadingPreview"
              class="text-sm text-indigo-600 hover:text-indigo-800 underline">
              {{ loadingPreview ? 'Carregando...' : 'Ver preview por cliente' }}
            </button>
            <div v-if="preview" class="mt-2 bg-gray-50 rounded-lg border border-gray-200 p-3 text-sm">
              <p class="font-medium text-gray-700 mb-2">{{ form.tipo }} {{ form.percentual }}% de {{ form.moeda }}</p>
              <table class="w-full text-xs">
                <thead>
                  <tr class="text-gray-500 text-left">
                    <th class="pb-1">Cliente</th>
                    <th class="pb-1 text-right">Saldo</th>
                    <th class="pb-1 text-right">Qtd calculada</th>
                    <th class="pb-1 text-right">Valor est.</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="c in preview.clientes" :key="c.clienteId" class="border-t border-gray-100">
                    <td class="py-1">{{ c.clienteNome }}</td>
                    <td class="py-1 text-right font-mono">{{ formatQtd(c.quantidadeAtivo) }}</td>
                    <td class="py-1 text-right font-mono">{{ formatQtd(c.quantidadeCalculada) }}</td>
                    <td class="py-1 text-right font-mono">{{ c.valorEstimado != null ? formatCurrency(c.valorEstimado) : '-' }}</td>
                  </tr>
                </tbody>
              </table>
              <p v-if="!preview.clientes?.length" class="text-gray-500 text-xs">Nenhum cliente nesta carteira.</p>
            </div>
          </div>

          <div>
            <div class="flex items-center gap-1 mb-1">
              <label class="text-sm font-medium text-gray-700">Observacao</label>
              <HelpTip field="observacao" :open="helpOpen" @toggle="toggleHelp" />
            </div>
            <HelpBox field="observacao" :open="helpOpen" text="Texto livre para justificar ou detalhar a recomendacao. Visivel para todos os clientes da carteira." example="'Tendencia de alta no curto prazo' ou 'Diversificar portfolio reduzindo exposicao em cripto'." />
            <textarea v-model="form.observacao" rows="2" class="w-full px-3 py-2 border rounded-lg text-sm"></textarea>
          </div>
          <p v-if="error" class="text-red-500 text-sm">{{ error }}</p>
          <div class="flex justify-end gap-3 pt-2">
            <button type="button" @click="$emit('close')" class="px-4 py-2 text-sm text-gray-700 bg-gray-100 rounded-lg">Cancelar</button>
            <button type="submit" class="px-4 py-2 text-sm text-white bg-indigo-600 rounded-lg hover:bg-indigo-700">Salvar</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, h, defineComponent } from 'vue'
import { MOEDAS_POPULARES, MOEDAS_FOREX, MOEDAS_CRYPTO, MOEDAS_COMMODITIES, PARES } from '../../utils/constants'
import { useCotacaoStore } from '../../stores/cotacao'
import { formatCurrency } from '../../utils/formatters'
import recomendacaoApi from '../../api/recomendacaoApi'

// --- Componentes inline de help ---
const HelpTip = defineComponent({
  props: { field: String, open: Object },
  emits: ['toggle'],
  setup(props, { emit }) {
    return () => h('button', {
      type: 'button',
      class: 'w-4 h-4 rounded-full bg-gray-200 text-gray-500 text-xs flex items-center justify-center hover:bg-indigo-100 hover:text-indigo-600 transition-colors flex-shrink-0',
      onClick: (e) => { e.stopPropagation(); emit('toggle', props.field) },
      title: 'Ajuda'
    }, '?')
  }
})

const HelpBox = defineComponent({
  props: { field: String, open: Object, text: String, example: String },
  setup(props) {
    return () => {
      if (!props.open.has(props.field)) return null
      return h('div', { class: 'mb-2 p-2.5 bg-blue-50 border border-blue-200 rounded-lg text-xs text-blue-800 leading-relaxed' }, [
        h('p', {}, props.text),
        props.example ? h('p', { class: 'mt-1 text-blue-600 font-medium' }, ['Ex: ', props.example]) : null
      ])
    }
  }
})

// --- Help state ---
const helpOpen = ref(new Set())
function toggleHelp(field) {
  const s = new Set(helpOpen.value)
  if (s.has(field)) s.delete(field)
  else s.add(field)
  helpOpen.value = s
}

// --- Props ---
const props = defineProps({
  show: Boolean,
  carteiraId: { type: [Number, String], default: null },
  carteiras: { type: Array, default: () => [] }
})

const emit = defineEmits(['close', 'saved', 'criar-carteira'])

const cotacaoStore = useCotacaoStore()
const searchMoeda = ref('')
const activeTab = ref('populares')
const showNewCarteiraForm = ref(false)
const newCarteira = ref({ nome: '', descricao: '' })
const form = ref({
  carteiraId: props.carteiraId || (props.carteiras[0]?.id ?? '') || '',
  tipo: 'COMPRA',
  moeda: 'USD',
  parMoeda: 'BRL',
  precoEntrada: null,
  precoAlvo: null,
  stopLoss: null,
  quantidade: null,
  percentual: null,
  modoPercentual: false,
  observacao: ''
})
const error = ref('')
const preview = ref(null)
const loadingPreview = ref(false)

const tabs = [
  { id: 'populares', label: 'Populares', list: MOEDAS_POPULARES },
  { id: 'forex', label: 'Forex', list: MOEDAS_FOREX },
  { id: 'crypto', label: 'Crypto', list: MOEDAS_CRYPTO },
  { id: 'commodities', label: 'Commodities', list: MOEDAS_COMMODITIES }
]

const moedasFiltradas = computed(() => {
  const tab = tabs.find(t => t.id === activeTab.value)
  const list = tab ? tab.list : MOEDAS_POPULARES
  const q = (searchMoeda.value || '').toLowerCase()
  if (!q) return list
  return list.filter(m => m.label.toLowerCase().includes(q) || m.value.toLowerCase().includes(q))
})

function precoAtual(moeda) {
  const c = cotacaoStore.cotacoes.find(x => x.moeda === moeda && x.parMoeda === form.value.parMoeda)
  return c ? formatCurrency(c.precoCompra, form.value.parMoeda) : '-'
}

async function atualizarCotacao(moeda) {
  try {
    await cotacaoStore.refreshSingle(moeda, form.value.parMoeda)
  } catch (_) {}
}

function onCarteiraSelect() {
  if (form.value.carteiraId === '__nova__') {
    showNewCarteiraForm.value = true
    newCarteira.value = { nome: '', descricao: '' }
  } else {
    showNewCarteiraForm.value = false
  }
}

function criarCarteiraEUsar() {
  if (!newCarteira.value.nome?.trim()) return
  emit('criar-carteira', { nome: newCarteira.value.nome.trim(), descricao: newCarteira.value.descricao?.trim() || '' })
}

function usarCarteiraCriada(carteiraId) {
  form.value.carteiraId = carteiraId
  showNewCarteiraForm.value = false
  newCarteira.value = { nome: '', descricao: '' }
}

function formatQtd(v) {
  if (v == null) return '-'
  return Number(v) < 1 ? Number(v).toFixed(8).replace(/0+$/, '').replace(/\.$/, '') : Number(v).toLocaleString('pt-BR', { maximumFractionDigits: 4 })
}

async function carregarPreview() {
  loadingPreview.value = true
  preview.value = null
  try {
    const res = await recomendacaoApi.previewPercentual({
      carteiraId: Number(form.value.carteiraId),
      tipo: form.value.tipo,
      moeda: form.value.moeda,
      parMoeda: form.value.parMoeda,
      percentual: Number(form.value.percentual)
    })
    preview.value = res.data
  } catch (e) {
    error.value = 'Erro ao carregar preview.'
  } finally {
    loadingPreview.value = false
  }
}

watch(() => props.show, (v) => {
  if (v) {
    form.value.carteiraId = props.carteiraId || props.carteiras[0]?.id || ''
    form.value.moeda = 'USD'
    form.value.parMoeda = 'BRL'
    form.value.modoPercentual = false
    form.value.percentual = null
    form.value.quantidade = null
    error.value = ''
    preview.value = null
    helpOpen.value = new Set()
    showNewCarteiraForm.value = false
  }
})
watch(() => props.carteiraId, (v) => { form.value.carteiraId = v || props.carteiras[0]?.id || '' })
watch(() => props.carteiras, (v) => { if (v?.length && !form.value.carteiraId) form.value.carteiraId = v[0].id }, { deep: true })

defineExpose({ usarCarteiraCriada })

async function submit() {
  error.value = ''
  const carteiraId = form.value.carteiraId && form.value.carteiraId !== '__nova__' ? form.value.carteiraId : props.carteiraId || props.carteiras[0]?.id
  if (!carteiraId) { error.value = 'Selecione ou crie uma carteira'; return }
  const payload = { carteiraId: Number(carteiraId), ...form.value }
  if (form.value.modoPercentual) {
    payload.quantidade = null
  } else {
    payload.percentual = null
    payload.modoPercentual = false
  }
  emit('saved', payload)
  emit('close')
}
</script>

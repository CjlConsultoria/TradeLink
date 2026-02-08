<template>
  <div v-if="show" class="fixed inset-0 z-50 flex items-center justify-center">
    <div class="fixed inset-0 bg-black/50" @click="$emit('close')"></div>
    <div class="bg-white rounded-lg shadow-xl max-w-lg w-full mx-4 max-h-[90vh] overflow-hidden flex flex-col relative z-10">
      <div class="p-4 border-b flex justify-between items-center">
        <h3 class="text-lg font-semibold">Nova Recomendação</h3>
        <button type="button" @click="$emit('close')" class="text-gray-400 hover:text-gray-600">✕</button>
      </div>
      <div class="p-4 overflow-y-auto flex-1">
        <div class="mb-4">
          <label class="block text-sm font-medium text-gray-700 mb-1">Carteira</label>
          <select v-model="form.carteiraId" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm" @change="onCarteiraSelect">
            <option value="">Selecione a carteira</option>
            <option value="__nova__">➕ Nova carteira</option>
            <option v-for="c in carteiras" :key="c.id" :value="c.id">{{ c.nome }}</option>
          </select>
        </div>
        <div v-if="showNewCarteiraForm" class="mb-4 p-3 bg-gray-50 rounded-lg border border-gray-200">
          <p class="text-sm font-medium text-gray-700 mb-2">Criar nova carteira</p>
          <div class="space-y-2">
            <input v-model="newCarteira.nome" placeholder="Nome da carteira" class="w-full px-3 py-2 border rounded-lg text-sm" />
            <textarea v-model="newCarteira.descricao" placeholder="Descrição (ex: só cripto)" rows="2" class="w-full px-3 py-2 border rounded-lg text-sm"></textarea>
            <div class="flex gap-2">
              <button type="button" @click="criarCarteiraEUsar" class="px-3 py-1.5 text-sm bg-indigo-600 text-white rounded-lg hover:bg-indigo-700">Criar e usar</button>
              <button type="button" @click="showNewCarteiraForm = false; form.carteiraId = carteiras[0]?.id || ''" class="px-3 py-1.5 text-sm text-gray-600 hover:underline">Cancelar</button>
            </div>
          </div>
        </div>
        <form @submit.prevent="submit" class="space-y-4">
          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Tipo</label>
              <select v-model="form.tipo" required class="w-full px-3 py-2 border rounded-lg text-sm">
                <option value="COMPRA">COMPRA</option>
                <option value="VENDA">VENDA</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Par</label>
              <select v-model="form.parMoeda" required class="w-full px-3 py-2 border rounded-lg text-sm">
                <option v-for="p in PARES" :key="p.value" :value="p.value">{{ p.label }}</option>
              </select>
            </div>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Moeda</label>
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
                <button type="button" @click.stop="atualizarCotacao(m.value)" class="text-xs text-gray-500 hover:text-indigo-600 ml-1" title="Atualizar cotação">↻</button>
              </div>
            </div>
          </div>

          <div class="grid grid-cols-2 gap-3">
            <div><label class="block text-sm font-medium text-gray-700 mb-1">Quantidade</label><input v-model="form.quantidade" type="number" step="any" class="w-full px-3 py-2 border rounded-lg text-sm" /></div>
            <div><label class="block text-sm font-medium text-gray-700 mb-1">Preço Entrada</label><input v-model="form.precoEntrada" type="number" step="any" class="w-full px-3 py-2 border rounded-lg text-sm" /></div>
            <div><label class="block text-sm font-medium text-gray-700 mb-1">Preço Alvo</label><input v-model="form.precoAlvo" type="number" step="any" class="w-full px-3 py-2 border rounded-lg text-sm" /></div>
            <div><label class="block text-sm font-medium text-gray-700 mb-1">Stop Loss</label><input v-model="form.stopLoss" type="number" step="any" class="w-full px-3 py-2 border rounded-lg text-sm" /></div>
          </div>
          <div><label class="block text-sm font-medium text-gray-700 mb-1">Observação</label><textarea v-model="form.observacao" rows="2" class="w-full px-3 py-2 border rounded-lg text-sm"></textarea></div>
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
import { ref, computed, watch } from 'vue'
import { MOEDAS_POPULARES, MOEDAS_FOREX, MOEDAS_CRYPTO, MOEDAS_COMMODITIES, PARES } from '../../utils/constants'
import { useCotacaoStore } from '../../stores/cotacao'
import { formatCurrency } from '../../utils/formatters'

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
  observacao: ''
})
const error = ref('')

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

watch(() => props.show, (v) => { if (v) { form.value.carteiraId = props.carteiraId || props.carteiras[0]?.id || ''; form.value.moeda = 'USD'; form.value.parMoeda = 'BRL'; error.value = ''; showNewCarteiraForm.value = false } })
watch(() => props.carteiraId, (v) => { form.value.carteiraId = v || props.carteiras[0]?.id || '' })
watch(() => props.carteiras, (v) => { if (v?.length && !form.value.carteiraId) form.value.carteiraId = v[0].id }, { deep: true })

defineExpose({ usarCarteiraCriada })

async function submit() {
  error.value = ''
  const carteiraId = form.value.carteiraId && form.value.carteiraId !== '__nova__' ? form.value.carteiraId : props.carteiraId || props.carteiras[0]?.id
  if (!carteiraId) { error.value = 'Selecione ou crie uma carteira'; return }
  emit('saved', { carteiraId: Number(carteiraId), ...form.value })
  emit('close')
}
</script>

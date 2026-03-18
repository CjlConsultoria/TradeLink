<template>
  <div class="fixed inset-0 z-50 flex items-center justify-center">
    <div class="fixed inset-0 bg-black/50" @click="$emit('close')"></div>
    <div class="bg-white rounded-lg shadow-xl p-6 max-w-md w-full mx-4 relative z-10">
      <h3 class="text-lg font-semibold mb-4">{{ ativo ? 'Editar ativo' : 'Adicionar ativo' }}</h3>
      <form @submit.prevent="submit" class="space-y-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Simbolo</label>
          <input v-model="form.simbolo" list="simbolos-list" required :disabled="!!ativo" placeholder="Ex: BTC, USD, AAPL" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm" />
          <datalist id="simbolos-list">
            <option v-for="m in sugestoes" :key="m.value" :value="m.value">{{ m.label }}</option>
          </datalist>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Nome</label>
          <input v-model="form.nome" required placeholder="Ex: Bitcoin, Dolar, Microsoft" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Categoria</label>
          <select v-model="form.categoria" required class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm">
            <option v-for="c in categorias" :key="c.value" :value="c.value">{{ c.label }}</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Quantidade</label>
          <input v-model.number="form.quantidade" type="number" step="any" required class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Preco manual (opcional)</label>
          <input v-model.number="form.precoManual" type="number" step="any" placeholder="Usado se nao houver cotacao automatica" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Moeda de referencia</label>
          <select v-model="form.parMoedaReferencia" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm">
            <option value="BRL">BRL</option>
            <option value="USD">USD</option>
          </select>
        </div>
        <p v-if="error" class="text-red-500 text-sm">{{ error }}</p>
        <div class="flex justify-end gap-3">
          <button type="button" @click="$emit('close')" class="px-4 py-2 text-sm text-gray-700 bg-gray-100 rounded-lg">Cancelar</button>
          <button type="submit" :disabled="saving" class="px-4 py-2 text-sm text-white bg-indigo-600 rounded-lg hover:bg-indigo-700 disabled:opacity-50">
            {{ saving ? 'Salvando...' : 'Salvar' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { MOEDAS, CATEGORIAS_ATIVO } from '../../utils/constants'
import portfolioApi from '../../api/portfolioApi'
import { useToast } from '../../composables/useToast'

const props = defineProps({ ativo: Object })
const emit = defineEmits(['close', 'saved'])
const toast = useToast()

const categorias = CATEGORIAS_ATIVO
const sugestoes = MOEDAS

const form = ref({
  simbolo: '',
  nome: '',
  categoria: 'CRYPTO',
  quantidade: null,
  precoManual: null,
  parMoedaReferencia: 'BRL'
})
const error = ref('')
const saving = ref(false)

watch(() => props.ativo, (a) => {
  if (a) {
    form.value = {
      simbolo: a.simbolo,
      nome: a.nome,
      categoria: a.categoria,
      quantidade: a.quantidade,
      precoManual: a.precoManual,
      parMoedaReferencia: a.parMoedaReferencia || 'BRL'
    }
  } else {
    form.value = { simbolo: '', nome: '', categoria: 'CRYPTO', quantidade: null, precoManual: null, parMoedaReferencia: 'BRL' }
  }
}, { immediate: true })

// Auto-fill nome when simbolo matches a known currency
watch(() => form.value.simbolo, (val) => {
  if (!val || props.ativo) return
  const match = MOEDAS.find(m => m.value === val.toUpperCase())
  if (match) {
    form.value.nome = match.label
    form.value.categoria = match.category === 'crypto' ? 'CRYPTO' : match.category === 'forex' ? 'FOREX' : match.category === 'commodities' ? 'COMMODITIES' : form.value.categoria
  }
})

async function submit() {
  error.value = ''
  saving.value = true
  try {
    const payload = { ...form.value }
    if (props.ativo) {
      await portfolioApi.atualizar(props.ativo.id, payload)
      toast.success('Ativo atualizado.')
    } else {
      await portfolioApi.adicionar(payload)
      toast.success('Ativo adicionado.')
    }
    emit('saved')
    emit('close')
  } catch (e) {
    error.value = e.response?.data?.mensagem || e.response?.data?.message || 'Erro ao salvar ativo.'
    toast.error(error.value)
  } finally {
    saving.value = false
  }
}
</script>

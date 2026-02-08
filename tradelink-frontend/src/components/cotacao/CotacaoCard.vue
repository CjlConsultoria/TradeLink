<template>
  <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-4 hover:shadow-md transition-shadow">
    <div class="flex items-center justify-between mb-2">
      <h3 class="font-semibold text-gray-900">{{ cotacao.moeda }}/{{ cotacao.parMoeda }}</h3>
      <span class="flex items-center gap-1">
        <span class="text-xs px-2 py-0.5 rounded-full" :class="variationClass">{{ formatPercent(cotacao.variacao) }}</span>
        <button v-if="showRefresh" type="button" @click="onRefresh" :disabled="refreshing" class="text-gray-400 hover:text-indigo-600 p-0.5 rounded" title="Atualizar cotação">↻</button>
      </span>
    </div>
    <div class="space-y-1">
      <div class="flex justify-between text-sm">
        <span class="text-gray-500">Compra</span>
        <span class="font-medium">{{ formatCurrency(cotacao.precoCompra, currencyCode) }}</span>
      </div>
      <div class="flex justify-between text-sm">
        <span class="text-gray-500">Venda</span>
        <span class="font-medium">{{ formatCurrency(cotacao.precoVenda, currencyCode) }}</span>
      </div>
      <div v-if="cotacao.maximo" class="flex justify-between text-sm">
        <span class="text-gray-500">Max/Min</span>
        <span class="text-xs text-gray-400">{{ formatCurrency(cotacao.maximo, currencyCode) }} / {{ formatCurrency(cotacao.minimo, currencyCode) }}</span>
      </div>
    </div>
    <p class="text-xs text-gray-400 mt-2">{{ formatDate(cotacao.dataHora) }} - {{ cotacao.fonte }}</p>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { formatCurrency, formatPercent, formatDate } from '../../utils/formatters'
import { useCotacaoStore } from '../../stores/cotacao'

const props = defineProps({ cotacao: Object, showRefresh: { type: Boolean, default: false } })
const emit = defineEmits(['refreshed'])
const cotacaoStore = useCotacaoStore()
const refreshing = ref(false)

const currencyCode = computed(() => props.cotacao.parMoeda === 'BRL' ? 'BRL' : 'USD')

async function onRefresh() {
  if (refreshing.value) return
  refreshing.value = true
  try {
    await cotacaoStore.refreshSingle(props.cotacao.moeda, props.cotacao.parMoeda)
    emit('refreshed')
  } finally {
    refreshing.value = false
  }
}
const variationClass = computed(() => {
  const v = Number(props.cotacao.variacao)
  if (v > 0) return 'bg-green-100 text-green-800'
  if (v < 0) return 'bg-red-100 text-red-800'
  return 'bg-gray-100 text-gray-800'
})
</script>

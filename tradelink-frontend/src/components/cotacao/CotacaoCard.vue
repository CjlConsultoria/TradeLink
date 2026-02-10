<template>
  <div
    class="bg-white rounded-xl shadow-sm border border-gray-200 p-4 hover:shadow-md transition-shadow"
    :class="{ 'cursor-pointer': showPopup }"
    @click="showPopup ? abrirPopup() : null"
  >
    <div class="flex items-center justify-between mb-2">
      <h3 class="font-semibold text-gray-900">{{ cotacao.moeda }}/{{ cotacao.parMoeda }}</h3>
      <span class="flex items-center gap-1" @click.stop>
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

    <!-- Popup detalhes -->
    <Teleport to="body">
      <div v-if="popupAberto" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4" @click.self="fecharPopup">
        <div class="bg-white rounded-xl shadow-xl max-w-sm w-full p-5" @click.stop>
          <div class="flex justify-between items-start mb-3">
            <h3 class="font-semibold text-lg text-gray-900">{{ cotacao.moeda }}/{{ cotacao.parMoeda }}</h3>
            <button type="button" @click="fecharPopup" class="text-gray-400 hover:text-gray-600 p-1 rounded">✕</button>
          </div>
          <div class="space-y-2 text-sm">
            <div class="flex justify-between">
              <span class="text-gray-500">Compra</span>
              <span class="font-medium">{{ formatCurrency(cotacao.precoCompra, currencyCode) }}</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-500">Venda</span>
              <span class="font-medium">{{ formatCurrency(cotacao.precoVenda, currencyCode) }}</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-500">Variação</span>
              <span class="font-medium" :class="variationClass">{{ formatPercent(cotacao.variacao) }}</span>
            </div>
            <div v-if="cotacao.maximo != null" class="flex justify-between">
              <span class="text-gray-500">Máximo</span>
              <span class="font-medium">{{ formatCurrency(cotacao.maximo, currencyCode) }}</span>
            </div>
            <div v-if="cotacao.minimo != null" class="flex justify-between">
              <span class="text-gray-500">Mínimo</span>
              <span class="font-medium">{{ formatCurrency(cotacao.minimo, currencyCode) }}</span>
            </div>
            <div class="flex justify-between pt-2 border-t border-gray-100">
              <span class="text-gray-500">Data/Hora</span>
              <span class="text-gray-600">{{ formatDate(cotacao.dataHora) }}</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-500">Fonte</span>
              <span class="text-gray-600">{{ cotacao.fonte || '-' }}</span>
            </div>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { formatCurrency, formatPercent, formatDate } from '../../utils/formatters'
import { useCotacaoStore } from '../../stores/cotacao'

const props = defineProps({
  cotacao: Object,
  showRefresh: { type: Boolean, default: false },
  showPopup: { type: Boolean, default: false }
})
const emit = defineEmits(['refreshed'])
const cotacaoStore = useCotacaoStore()
const refreshing = ref(false)
const popupAberto = ref(false)

function abrirPopup() {
  popupAberto.value = true
}
function fecharPopup() {
  popupAberto.value = false
}

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

<template>
  <div v-if="saldo" class="grid grid-cols-2 md:grid-cols-4 gap-3">
    <div class="bg-white border border-gray-200 rounded-xl p-4">
      <p class="text-xs text-gray-500 mb-1">Saldo Total</p>
      <p class="text-xl font-bold text-gray-900">{{ formatCurrency(saldo.saldoTotal) }}</p>
    </div>
    <div class="bg-white border border-gray-200 rounded-xl p-4">
      <p class="text-xs text-gray-500 mb-1">Investido</p>
      <p class="text-xl font-bold text-indigo-600">{{ formatCurrency(saldo.saldoInvestido) }}</p>
    </div>
    <div class="bg-white border border-gray-200 rounded-xl p-4">
      <p class="text-xs text-gray-500 mb-1">Disponivel</p>
      <p class="text-xl font-bold text-emerald-600">{{ formatCurrency(saldo.saldoDisponivel) }}</p>
    </div>
    <div class="bg-white border border-gray-200 rounded-xl p-4">
      <p class="text-xs text-gray-500 mb-1">Lucro / Perda</p>
      <p class="text-xl font-bold" :class="saldo.lucroPerda >= 0 ? 'text-emerald-600' : 'text-red-600'">
        {{ saldo.lucroPerda >= 0 ? '+' : '' }}{{ formatCurrency(saldo.lucroPerda) }}
      </p>
    </div>
    <div class="bg-gray-50 rounded-lg p-3 col-span-2">
      <div class="flex justify-between text-sm">
        <span class="text-gray-500">Total Aportado</span>
        <span class="font-medium text-gray-900">{{ formatCurrency(saldo.totalAportado) }}</span>
      </div>
      <div class="flex justify-between text-sm mt-1">
        <span class="text-gray-500">Total Sacado</span>
        <span class="font-medium text-gray-900">{{ formatCurrency(saldo.totalSacado) }}</span>
      </div>
    </div>
  </div>
  <div v-else-if="loading" class="flex justify-center py-4">
    <LoadingSpinner />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import alocacaoApi from '../../api/alocacaoApi'
import { formatCurrency } from '../../utils/formatters'
import LoadingSpinner from '../common/LoadingSpinner.vue'

const props = defineProps({
  carteiraId: [Number, String],
  clienteId: { type: [Number, String], default: null },
  consultor: { type: Boolean, default: false }
})

const saldo = ref(null)
const loading = ref(false)

async function carregar() {
  loading.value = true
  try {
    const res = props.consultor && props.clienteId
      ? await alocacaoApi.saldoClienteConsultor(props.carteiraId, props.clienteId)
      : await alocacaoApi.meuSaldo(props.carteiraId)
    saldo.value = res.data
  } catch (_) {
    saldo.value = null
  } finally {
    loading.value = false
  }
}

onMounted(carregar)
defineExpose({ carregar })
</script>

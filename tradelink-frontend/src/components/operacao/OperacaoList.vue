<template>
  <div class="space-y-2">
    <div v-for="op in operacoes" :key="op.id" class="flex items-center justify-between py-2 px-3 bg-gray-50 rounded-lg border border-gray-100 gap-2">
      <div class="flex items-center gap-2 flex-1 min-w-0">
        <span class="text-xs px-2 py-0.5 rounded-full font-medium shrink-0" :class="op.tipo === 'COMPRA' ? 'bg-emerald-100 text-emerald-800' : 'bg-rose-100 text-rose-800'">
          {{ op.tipo }}
        </span>
        <span class="font-medium text-sm">{{ formatCurrency(op.precoExecutado) }}</span>
        <span class="text-gray-500 text-sm">× {{ op.quantidade }}</span>
      </div>
      <div class="text-right text-xs text-gray-500 shrink-0">
        <p>{{ formatDate(op.dataExecucao) }}</p>
        <p v-if="op.clienteNome" class="text-gray-400">{{ op.clienteNome }}</p>
      </div>
      <div v-if="showActions" class="flex items-center gap-1 shrink-0">
        <button type="button" @click="$emit('edit', op)" class="text-indigo-600 hover:text-indigo-800 text-xs font-medium px-2 py-1 rounded" title="Editar">Editar</button>
        <button type="button" @click="$emit('delete', op)" class="text-rose-600 hover:text-rose-800 text-xs font-medium px-2 py-1 rounded" title="Excluir">Excluir</button>
      </div>
    </div>
    <p v-if="operacoes.length === 0" class="text-sm text-gray-500 py-2">Nenhuma operação registrada.</p>
  </div>
</template>

<script setup>
import { formatCurrency, formatDate } from '../../utils/formatters'

defineProps({
  operacoes: { type: Array, default: () => [] },
  showActions: { type: Boolean, default: true }
})
defineEmits(['edit', 'delete'])
</script>

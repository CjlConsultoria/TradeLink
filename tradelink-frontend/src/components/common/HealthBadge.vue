<template>
  <span :class="classes" class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium">
    <span class="w-1.5 h-1.5 rounded-full" :class="dotClass"></span>
    {{ label }}
  </span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  status: { type: String, default: 'OK' }
})

const classes = computed(() => {
  switch (props.status) {
    case 'OK': return 'bg-emerald-50 text-emerald-700'
    case 'ATENCAO': return 'bg-amber-50 text-amber-700'
    case 'CRITICO': return 'bg-red-50 text-red-700'
    default: return 'bg-gray-50 text-gray-600'
  }
})

const dotClass = computed(() => {
  switch (props.status) {
    case 'OK': return 'bg-emerald-500'
    case 'ATENCAO': return 'bg-amber-500'
    case 'CRITICO': return 'bg-red-500'
    default: return 'bg-gray-400'
  }
})

const label = computed(() => {
  switch (props.status) {
    case 'OK': return 'Balanceado'
    case 'ATENCAO': return 'Atencao'
    case 'CRITICO': return 'Desbalanceado'
    default: return props.status
  }
})
</script>

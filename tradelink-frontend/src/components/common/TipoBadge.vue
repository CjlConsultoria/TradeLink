<template>
  <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium" :class="colorClass">{{ tipo }}</span>
</template>

<script setup>
import { computed } from 'vue'
import { TIPO_COLORS } from '../../utils/constants'
const props = defineProps({ tipo: String })
const colorClass = computed(() => TIPO_COLORS[props.tipo] || 'bg-gray-100 text-gray-800')
</script>

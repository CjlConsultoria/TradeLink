<template>
  <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium" :class="colorClass">{{ status }}</span>
</template>

<script setup>
import { computed } from 'vue'
import { STATUS_COLORS } from '../../utils/constants'
const props = defineProps({ status: String })
const colorClass = computed(() => STATUS_COLORS[props.status] || 'bg-gray-100 text-gray-800')
</script>

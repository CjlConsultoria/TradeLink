<template>
  <button
    type="button"
    role="switch"
    :aria-checked="modelValue"
    :disabled="disabled || loading"
    class="inline-flex items-center gap-2 rounded-lg border-2 px-3 py-1.5 text-sm font-medium transition-colors focus:outline-none focus:ring-2 focus:ring-offset-1 disabled:opacity-60 disabled:cursor-not-allowed"
    :class="computedClasses"
    @click="$emit('update:modelValue', !modelValue); $emit('change', !modelValue)"
  >
    <span
      class="relative inline-flex h-5 w-9 shrink-0 rounded-full border-2 transition-colors"
      :class="modelValue ? trackOnClass : trackOffClass"
      aria-hidden="true"
    >
      <span
        class="pointer-events-none inline-block h-4 w-4 transform rounded-full bg-white shadow ring-0 transition translate-y-0.5"
        :class="modelValue ? 'translate-x-4' : 'translate-x-0.5'"
      />
    </span>
    <span>{{ modelValue ? labelOn : labelOff }}</span>
    <span v-if="loading" class="inline-block h-3 w-3 animate-spin rounded-full border-2 border-current border-t-transparent" aria-hidden="true" />
  </button>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  modelValue: { type: Boolean, required: true },
  labelOn: { type: String, default: 'Ligado' },
  labelOff: { type: String, default: 'Desligado' },
  variant: { type: String, default: 'default' },
  disabled: Boolean,
  loading: Boolean
})

defineEmits(['update:modelValue', 'change'])

const variants = {
  default: {
    on: 'border-indigo-500 bg-indigo-50 text-indigo-800 focus:ring-indigo-500',
    off: 'border-gray-300 bg-white text-gray-700 hover:bg-gray-50 focus:ring-gray-400',
    trackOn: 'border-indigo-500 bg-indigo-500',
    trackOff: 'border-gray-300 bg-gray-200'
  },
  success: {
    on: 'border-green-600 bg-green-50 text-green-800 focus:ring-green-500',
    off: 'border-gray-300 bg-white text-gray-700 hover:bg-gray-50 focus:ring-gray-400',
    trackOn: 'border-green-600 bg-green-600',
    trackOff: 'border-gray-300 bg-gray-200'
  },
  danger: {
    on: 'border-red-600 bg-red-50 text-red-800 focus:ring-red-500',
    off: 'border-gray-300 bg-white text-gray-700 hover:bg-gray-50 focus:ring-gray-400',
    trackOn: 'border-red-600 bg-red-600',
    trackOff: 'border-gray-300 bg-gray-200'
  },
  warning: {
    on: 'border-amber-600 bg-amber-50 text-amber-800 focus:ring-amber-500',
    off: 'border-gray-300 bg-white text-gray-700 hover:bg-gray-50 focus:ring-gray-400',
    trackOn: 'border-amber-600 bg-amber-600',
    trackOff: 'border-gray-300 bg-gray-200'
  }
}

const v = variants[props.variant] || variants.default
const computedClasses = computed(() =>
  props.modelValue
    ? v.on
    : v.off
)
const trackOnClass = computed(() => v.trackOn)
const trackOffClass = computed(() => v.trackOff)
</script>

<template>
  <div v-if="show" class="fixed inset-0 z-50 flex items-center justify-center">
    <div class="fixed inset-0 bg-black/50" @click="$emit('cancel')"></div>
    <div class="bg-white rounded-lg shadow-xl p-6 max-w-md w-full mx-4 relative z-10">
      <h3 class="text-lg font-semibold text-gray-900 mb-2">{{ title }}</h3>
      <p class="text-gray-600 mb-6">{{ message }}</p>
      <div class="flex justify-end gap-3">
        <button @click="$emit('cancel')" class="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200">Cancelar</button>
        <button @click="$emit('confirm')" class="px-4 py-2 text-sm font-medium text-white bg-red-600 rounded-lg hover:bg-red-700">Confirmar</button>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  show: Boolean,
  title: { type: String, default: 'Confirmar acao' },
  message: { type: String, default: 'Tem certeza?' }
})
defineEmits(['confirm', 'cancel'])
</script>

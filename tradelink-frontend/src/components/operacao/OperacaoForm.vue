<template>
  <div class="fixed inset-0 z-50 flex items-center justify-center">
    <div class="fixed inset-0 bg-black/50" @click="$emit('close')"></div>
    <div class="bg-white rounded-lg shadow-xl p-6 max-w-md w-full mx-4 relative z-10">
      <h3 class="text-lg font-semibold mb-4">{{ operacao ? 'Editar operação' : 'Registrar Operação' }}</h3>
      <form @submit.prevent="submit" class="space-y-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Tipo</label>
          <select v-model="form.tipo" required class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm">
            <option value="COMPRA">COMPRA</option>
            <option value="VENDA">VENDA</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Preço executado</label>
          <input v-model.number="form.precoExecutado" type="number" step="any" required class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Quantidade</label>
          <input v-model.number="form.quantidade" type="number" step="any" required class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Data de execução</label>
          <input v-model="form.dataExecucao" type="datetime-local" required class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Observação</label>
          <textarea v-model="form.observacao" rows="2" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"></textarea>
        </div>
        <p v-if="error" class="text-red-500 text-sm">{{ error }}</p>
        <div class="flex justify-end gap-3">
          <button type="button" @click="$emit('close')" class="px-4 py-2 text-sm text-gray-700 bg-gray-100 rounded-lg">Cancelar</button>
          <button type="submit" class="px-4 py-2 text-sm text-white bg-indigo-600 rounded-lg hover:bg-indigo-700">Salvar</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({ show: Boolean })
const emit = defineEmits(['close', 'saved'])

const form = ref({
  tipo: 'COMPRA',
  precoExecutado: null,
  quantidade: null,
  dataExecucao: new Date().toISOString().slice(0, 16),
  observacao: ''
})
const error = ref('')

watch(() => props.show, (v) => {
  if (v) {
    form.value = {
      tipo: 'COMPRA',
      precoExecutado: null,
      quantidade: null,
      dataExecucao: new Date().toISOString().slice(0, 16),
      observacao: ''
    }
    error.value = ''
  }
})

function submit() {
  error.value = ''
  const dataExecucao = form.value.dataExecucao ? new Date(form.value.dataExecucao).toISOString() : new Date().toISOString()
  const payload = {
    tipo: form.value.tipo,
    precoExecutado: form.value.precoExecutado,
    quantidade: form.value.quantidade,
    dataExecucao,
    observacao: form.value.observacao || null
  }
  if (props.operacao?.id) payload.operacaoId = props.operacao.id
  emit('saved', payload)
  emit('close')
}
</script>

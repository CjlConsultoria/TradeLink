<template>
  <div v-if="show" class="fixed inset-0 z-50 flex items-center justify-center">
    <div class="fixed inset-0 bg-black/50" @click="$emit('close')"></div>
    <div class="bg-white rounded-xl shadow-xl max-w-md w-full mx-4 p-6 relative z-10">
      <h3 class="text-lg font-semibold mb-4">Registrar {{ tipoLabel }}</h3>
      <form @submit.prevent="salvar" class="space-y-3">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Tipo</label>
          <select v-model="form.tipo" class="w-full px-3 py-2 border rounded-lg text-sm">
            <option value="APORTE">Aporte (Deposito)</option>
            <option value="SAQUE">Saque (Retirada)</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Valor</label>
          <input v-model.number="form.valor" type="number" step="0.01" min="0.01" required class="w-full px-3 py-2 border rounded-lg text-sm" placeholder="0.00" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Moeda</label>
          <select v-model="form.moeda" class="w-full px-3 py-2 border rounded-lg text-sm">
            <option value="USD">USD (Dolar)</option>
            <option value="BRL">BRL (Real)</option>
            <option value="EUR">EUR (Euro)</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Data</label>
          <input v-model="form.dataMovimentacao" type="date" required class="w-full px-3 py-2 border rounded-lg text-sm" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Observacao</label>
          <input v-model="form.observacao" type="text" class="w-full px-3 py-2 border rounded-lg text-sm" placeholder="Opcional" />
        </div>
        <p v-if="erro" class="text-sm text-red-500">{{ erro }}</p>
        <div class="flex gap-2 pt-2">
          <button type="submit" :disabled="saving" class="px-4 py-2 bg-indigo-600 text-white rounded-lg text-sm hover:bg-indigo-700 disabled:opacity-50">
            {{ saving ? 'Salvando...' : 'Salvar' }}
          </button>
          <button type="button" @click="$emit('close')" class="px-4 py-2 bg-gray-100 rounded-lg text-sm hover:bg-gray-200">Cancelar</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import alocacaoApi from '../../api/alocacaoApi'

const props = defineProps({
  show: Boolean,
  carteiraId: [Number, String],
  clienteId: { type: [Number, String], default: null },
  consultor: { type: Boolean, default: false }
})

const emit = defineEmits(['close', 'saved'])

const form = ref({
  tipo: 'APORTE',
  valor: null,
  moeda: 'USD',
  dataMovimentacao: new Date().toISOString().slice(0, 10),
  observacao: ''
})
const saving = ref(false)
const erro = ref('')

const tipoLabel = computed(() => form.value.tipo === 'APORTE' ? 'Aporte' : 'Saque')

watch(() => props.show, (v) => {
  if (v) {
    erro.value = ''
    form.value = {
      tipo: 'APORTE',
      valor: null,
      moeda: 'USD',
      dataMovimentacao: new Date().toISOString().slice(0, 10),
      observacao: ''
    }
  }
})

async function salvar() {
  if (!form.value.valor || form.value.valor <= 0) { erro.value = 'Informe um valor valido.'; return }
  saving.value = true
  erro.value = ''
  try {
    if (props.consultor && props.clienteId) {
      await alocacaoApi.registrarMovimentacaoConsultor(props.carteiraId, props.clienteId, form.value)
    } else {
      await alocacaoApi.registrarMovimentacao(props.carteiraId, form.value)
    }
    emit('saved')
    emit('close')
  } catch (e) {
    erro.value = e.response?.data?.erro || e.response?.data?.mensagem || 'Erro ao registrar.'
  } finally {
    saving.value = false
  }
}
</script>

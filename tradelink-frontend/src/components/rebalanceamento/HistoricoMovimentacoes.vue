<template>
  <div>
    <div v-if="loading" class="flex justify-center py-4">
      <LoadingSpinner />
    </div>
    <div v-else-if="movimentacoes.length" class="overflow-x-auto">
      <table class="w-full text-sm">
        <thead class="bg-gray-50">
          <tr>
            <th class="text-left px-3 py-2 font-medium text-gray-600">Data</th>
            <th class="text-left px-3 py-2 font-medium text-gray-600">Tipo</th>
            <th class="text-right px-3 py-2 font-medium text-gray-600">Valor</th>
            <th class="text-left px-3 py-2 font-medium text-gray-600">Moeda</th>
            <th v-if="showCliente" class="text-left px-3 py-2 font-medium text-gray-600">Cliente</th>
            <th class="text-left px-3 py-2 font-medium text-gray-600">Obs.</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="m in movimentacoes" :key="m.id" class="border-t"
            :class="m.tipo === 'APORTE' ? 'bg-green-50' : 'bg-red-50'">
            <td class="px-3 py-2 text-gray-600">{{ formatData(m.dataMovimentacao) }}</td>
            <td class="px-3 py-2">
              <span class="text-xs font-medium px-2 py-0.5 rounded-full"
                :class="m.tipo === 'APORTE' ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'">
                {{ m.tipo }}
              </span>
            </td>
            <td class="px-3 py-2 text-right font-medium"
              :class="m.tipo === 'APORTE' ? 'text-green-700' : 'text-red-700'">
              {{ m.tipo === 'APORTE' ? '+' : '-' }}{{ formatCurrency(m.valor) }}
            </td>
            <td class="px-3 py-2 text-gray-600">{{ m.moeda }}</td>
            <td v-if="showCliente" class="px-3 py-2 text-gray-600">{{ m.clienteNome }}</td>
            <td class="px-3 py-2 text-gray-400 text-xs">{{ m.observacao || '-' }}</td>
          </tr>
        </tbody>
      </table>
    </div>
    <p v-else class="text-sm text-gray-500 text-center py-4">Nenhuma movimentacao registrada.</p>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import alocacaoApi from '../../api/alocacaoApi'
import { formatCurrency } from '../../utils/formatters'
import LoadingSpinner from '../common/LoadingSpinner.vue'

const props = defineProps({
  carteiraId: [Number, String],
  consultor: { type: Boolean, default: false },
  showCliente: { type: Boolean, default: false }
})

const movimentacoes = ref([])
const loading = ref(false)

function formatData(d) {
  if (!d) return '-'
  const parts = d.split('-')
  return parts.length === 3 ? `${parts[2]}/${parts[1]}/${parts[0]}` : d
}

async function carregar() {
  loading.value = true
  try {
    const res = props.consultor
      ? await alocacaoApi.listarMovimentacoesCarteira(props.carteiraId)
      : await alocacaoApi.listarMovimentacoes(props.carteiraId)
    movimentacoes.value = res.data || []
  } catch (_) {
    movimentacoes.value = []
  } finally {
    loading.value = false
  }
}

onMounted(carregar)
defineExpose({ carregar })
</script>

<template>
  <div>
    <div v-if="loading" class="flex justify-center py-6">
      <LoadingSpinner />
    </div>
    <div v-else-if="erro" class="text-sm text-red-600">{{ erro }}</div>
    <template v-else-if="dados?.carteiras?.length">
      <!-- Resumo geral -->
      <div class="grid grid-cols-3 gap-3 mb-4">
        <div class="bg-gray-50 rounded-lg p-3 text-center">
          <p class="text-2xl font-bold text-gray-900">{{ resumo.totalClientes }}</p>
          <p class="text-xs text-gray-500">Clientes</p>
        </div>
        <div class="bg-amber-50 rounded-lg p-3 text-center">
          <p class="text-2xl font-bold text-amber-600">{{ resumo.totalDesbalanceados }}</p>
          <p class="text-xs text-gray-500">Desbalanceados</p>
        </div>
        <div class="bg-red-50 rounded-lg p-3 text-center">
          <p class="text-2xl font-bold text-red-600">{{ resumo.totalCriticos }}</p>
          <p class="text-xs text-gray-500">Criticos</p>
        </div>
      </div>

      <!-- Grid de carteiras -->
      <div class="space-y-4">
        <div v-for="cart in dados.carteiras" :key="cart.carteiraId"
          class="border border-gray-200 rounded-xl p-4">
          <div class="flex items-center justify-between mb-3">
            <div class="flex items-center gap-2">
              <router-link :to="'/consultor/carteiras/' + cart.carteiraId" class="font-semibold text-gray-900 hover:text-indigo-600">
                {{ cart.carteiraNome }}
              </router-link>
              <span v-if="!cart.temAlocacoes" class="text-xs text-gray-400 bg-gray-100 px-2 py-0.5 rounded">Sem alocacao</span>
            </div>
            <div class="flex items-center gap-2">
              <span v-if="cartDesbalanceados(cart) > 0" class="text-xs text-amber-600">
                {{ cartDesbalanceados(cart) }}/{{ cart.clientes?.length || 0 }} desbal.
              </span>
              <router-link v-if="cart.temAlocacoes" :to="{ path: '/consultor/rebalanceamento', query: { carteira: cart.carteiraId } }"
                class="text-xs text-indigo-600 hover:underline">Ver detalhes</router-link>
            </div>
          </div>
          <div v-if="cart.clientes?.length" class="space-y-2">
            <div v-for="cli in cart.clientes" :key="cli.clienteId"
              class="flex items-center justify-between py-1.5 px-3 bg-gray-50 rounded-lg text-sm">
              <div class="flex items-center gap-2">
                <span class="w-2.5 h-2.5 rounded-full"
                  :class="{
                    'bg-emerald-500': cli.status === 'OK',
                    'bg-amber-500': cli.status === 'ATENCAO',
                    'bg-red-500': cli.status === 'CRITICO'
                  }"></span>
                <span class="text-gray-900">{{ cli.clienteNome }}</span>
              </div>
              <div class="flex items-center gap-3 text-xs text-gray-500">
                <span v-if="cli.totalDesbalanceados > 0" class="text-red-600 font-medium">
                  {{ cli.totalDesbalanceados }} desbal.
                </span>
                <span v-if="cli.maiorDesvio" :class="Math.abs(cli.maiorDesvio) > 10 ? 'text-red-600 font-medium' : ''">
                  {{ cli.maiorDesvio > 0 ? '+' : '' }}{{ Number(cli.maiorDesvio).toFixed(1) }}%
                </span>
                <span>{{ formatCurrency(cli.valorTotal) }}</span>
              </div>
            </div>
          </div>
          <p v-else class="text-xs text-gray-400">Nenhum cliente.</p>
        </div>
      </div>
    </template>
    <p v-else class="text-sm text-gray-500 text-center py-4">Nenhuma carteira encontrada.</p>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import alocacaoApi from '../../api/alocacaoApi'
import { formatCurrency } from '../../utils/formatters'
import LoadingSpinner from '../common/LoadingSpinner.vue'

const dados = ref(null)
const loading = ref(false)
const erro = ref('')

const resumo = computed(() => {
  if (!dados.value?.carteiras) return { totalClientes: 0, totalDesbalanceados: 0, totalCriticos: 0 }
  let totalClientes = 0, totalDesbalanceados = 0, totalCriticos = 0
  for (const cart of dados.value.carteiras) {
    for (const cli of (cart.clientes || [])) {
      totalClientes++
      if (cli.status !== 'OK') totalDesbalanceados++
      if (cli.status === 'CRITICO') totalCriticos++
    }
  }
  return { totalClientes, totalDesbalanceados, totalCriticos }
})

function cartDesbalanceados(cart) {
  return (cart.clientes || []).filter(c => c.status !== 'OK').length
}

async function carregar() {
  loading.value = true
  erro.value = ''
  try {
    const res = await alocacaoApi.saudeClientes()
    dados.value = res.data
  } catch (e) {
    erro.value = e.response?.data?.erro || 'Erro ao carregar saude.'
  } finally {
    loading.value = false
  }
}

onMounted(carregar)
defineExpose({ carregar })
</script>

<template>
  <div>
    <div v-if="loading" class="flex justify-center py-8">
      <LoadingSpinner />
    </div>

    <div v-else-if="erro" class="bg-red-50 border border-red-200 rounded-lg p-4 text-sm text-red-700">
      {{ erro }}
    </div>

    <div v-else-if="dados">
      <!-- Resumo geral -->
      <div class="grid grid-cols-2 md:grid-cols-4 gap-3 mb-6">
        <div class="bg-gray-50 rounded-lg p-3 text-center">
          <p class="text-xs text-gray-500">Margem de Erro</p>
          <p class="text-lg font-semibold text-gray-900">{{ dados.margemErro }}%</p>
        </div>
        <div class="bg-gray-50 rounded-lg p-3 text-center">
          <p class="text-xs text-gray-500">Moeda Ref.</p>
          <p class="text-lg font-semibold text-gray-900">{{ dados.moedaReferencia }}</p>
        </div>
        <div class="bg-gray-50 rounded-lg p-3 text-center">
          <p class="text-xs text-gray-500">Ativos Alvo</p>
          <p class="text-lg font-semibold text-gray-900">{{ dados.alocacoesAlvo?.length || 0 }}</p>
        </div>
        <div class="bg-gray-50 rounded-lg p-3 text-center">
          <p class="text-xs text-gray-500">Clientes</p>
          <p class="text-lg font-semibold text-gray-900">{{ dados.clientes?.length || 0 }}</p>
        </div>
      </div>

      <!-- Cards de clientes -->
      <div v-if="dados.clientes?.length" class="space-y-4">
        <div v-for="cliente in dados.clientes" :key="cliente.clienteId"
          class="border rounded-xl overflow-hidden transition-all"
          :class="clienteAberto === cliente.clienteId ? 'border-indigo-300 shadow-md' : 'border-gray-200'">

          <!-- Header do card -->
          <div class="flex items-center justify-between p-4 cursor-pointer hover:bg-gray-50"
            @click="toggleCliente(cliente.clienteId)">
            <div class="flex items-center gap-3">
              <HealthBadge :status="cliente.statusSaude" />
              <div>
                <p class="font-semibold text-gray-900">{{ cliente.clienteNome }}</p>
                <p class="text-sm text-gray-500">{{ formatCurrency(cliente.valorTotalPortfolio) }}</p>
              </div>
            </div>
            <svg class="w-5 h-5 text-gray-400 transition-transform" :class="{ 'rotate-180': clienteAberto === cliente.clienteId }" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
            </svg>
          </div>

          <!-- Conteudo expandido -->
          <div v-if="clienteAberto === cliente.clienteId" class="border-t border-gray-100">
            <!-- Grafico -->
            <div v-if="cliente.ativos?.length" class="p-4 bg-gray-50">
              <AllocationChart :ativos="cliente.ativos" :alocacoes-alvo="dados.alocacoesAlvo" modo="comparativo" />
              <p class="text-xs text-center text-gray-400 mt-2">Anel externo: Atual | Anel interno: Ideal</p>
            </div>

            <!-- Tabela de ativos -->
            <div class="overflow-x-auto">
              <table class="w-full text-sm">
                <thead class="bg-gray-50">
                  <tr>
                    <th class="text-left px-3 py-2 font-medium text-gray-600">Ativo</th>
                    <th class="text-right px-3 py-2 font-medium text-gray-600">Qtd</th>
                    <th class="text-right px-3 py-2 font-medium text-gray-600">Preco</th>
                    <th class="text-right px-3 py-2 font-medium text-gray-600">Valor</th>
                    <th class="text-right px-3 py-2 font-medium text-gray-600">% Atual</th>
                    <th class="text-right px-3 py-2 font-medium text-gray-600">% Ideal</th>
                    <th class="text-right px-3 py-2 font-medium text-gray-600">Diff</th>
                    <th class="text-center px-3 py-2 font-medium text-gray-600">Acao</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="ativo in cliente.ativos" :key="ativo.simbolo"
                    class="border-t"
                    :class="{
                      'bg-red-50': ativo.vender,
                      'bg-green-50': ativo.comprar
                    }">
                    <td class="px-3 py-2">
                      <span class="font-medium">{{ ativo.simbolo }}</span>
                      <span class="text-gray-400 text-xs ml-1">{{ ativo.nome }}</span>
                    </td>
                    <td class="px-3 py-2 text-right font-mono text-xs">{{ formatQtd(ativo.quantidade) }}</td>
                    <td class="px-3 py-2 text-right">{{ ativo.precoAtual ? formatCurrency(ativo.precoAtual) : '-' }}</td>
                    <td class="px-3 py-2 text-right font-medium">{{ formatCurrency(ativo.valorUsd) }}</td>
                    <td class="px-3 py-2 text-right">{{ Number(ativo.percentualAtual).toFixed(1) }}%</td>
                    <td class="px-3 py-2 text-right text-gray-500">{{ Number(ativo.percentualAlvo).toFixed(1) }}%</td>
                    <td class="px-3 py-2 text-right font-medium"
                      :class="ativo.diferencaPercentual > 0 ? 'text-green-600' : ativo.diferencaPercentual < 0 ? 'text-red-600' : 'text-gray-400'">
                      {{ ativo.diferencaPercentual > 0 ? '+' : '' }}{{ Number(ativo.diferencaPercentual).toFixed(1) }}%
                    </td>
                    <td class="px-3 py-2 text-center">
                      <span v-if="ativo.comprar" class="text-xs bg-green-100 text-green-700 px-2 py-0.5 rounded-full font-medium">COMPRAR</span>
                      <span v-else-if="ativo.vender" class="text-xs bg-red-100 text-red-700 px-2 py-0.5 rounded-full font-medium">VENDER</span>
                      <span v-else class="text-xs text-gray-400">OK</span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <!-- Acoes sugeridas -->
            <div v-if="cliente.acoesSugeridas?.length" class="p-4 border-t border-gray-100">
              <p class="text-sm font-medium text-gray-700 mb-2">Acoes Sugeridas</p>
              <div class="space-y-2">
                <div v-for="(acao, i) in cliente.acoesSugeridas" :key="i"
                  class="flex items-center gap-2 text-sm px-3 py-2 rounded-lg"
                  :class="acao.tipo === 'COMPRA' ? 'bg-green-50 text-green-800' : 'bg-red-50 text-red-800'">
                  <span class="font-medium">{{ acao.tipo }}</span>
                  <span>{{ acao.simbolo }}</span>
                  <span class="text-xs opacity-75">{{ acao.descricao }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <p v-else class="text-sm text-gray-500 text-center py-6">Nenhum cliente nesta carteira.</p>

      <!-- Botao gerar recomendacoes -->
      <div v-if="dados.clientes?.some(c => c.acoesSugeridas?.length)" class="mt-6 flex justify-end">
        <button @click="gerarRecomendacoes" :disabled="gerando"
          class="bg-indigo-600 text-white px-5 py-2.5 rounded-lg text-sm font-medium hover:bg-indigo-700 disabled:opacity-50 flex items-center gap-2">
          <svg v-if="gerando" class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/></svg>
          {{ gerando ? 'Gerando...' : 'Gerar Recomendacoes Automaticas' }}
        </button>
      </div>
    </div>

    <div v-else class="text-center py-8">
      <p class="text-gray-500 mb-3">Clique em "Analisar" para ver o rebalanceamento.</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import alocacaoApi from '../../api/alocacaoApi'
import { formatCurrency } from '../../utils/formatters'
import LoadingSpinner from '../common/LoadingSpinner.vue'
import HealthBadge from '../common/HealthBadge.vue'
import AllocationChart from './AllocationChart.vue'

const props = defineProps({
  carteiraId: [Number, String]
})

const emit = defineEmits(['recomendacoes-geradas'])

const dados = ref(null)
const loading = ref(false)
const erro = ref('')
const clienteAberto = ref(null)
const gerando = ref(false)

function formatQtd(v) {
  if (v == null) return '-'
  return Number(v) < 1 ? Number(v).toFixed(8).replace(/0+$/, '').replace(/\.$/, '') : Number(v).toLocaleString('pt-BR', { maximumFractionDigits: 4 })
}

function toggleCliente(id) {
  clienteAberto.value = clienteAberto.value === id ? null : id
}

async function analisar() {
  loading.value = true
  erro.value = ''
  dados.value = null
  try {
    const res = await alocacaoApi.analisarCarteira(props.carteiraId)
    dados.value = res.data
    if (res.data.clientes?.length === 1) {
      clienteAberto.value = res.data.clientes[0].clienteId
    }
  } catch (e) {
    erro.value = e.response?.data?.erro || e.response?.data?.mensagem || 'Erro ao analisar rebalanceamento.'
  } finally {
    loading.value = false
  }
}

async function gerarRecomendacoes() {
  gerando.value = true
  try {
    await alocacaoApi.gerarRecomendacoes(props.carteiraId)
    emit('recomendacoes-geradas')
  } catch (e) {
    erro.value = e.response?.data?.erro || 'Erro ao gerar recomendacoes.'
  } finally {
    gerando.value = false
  }
}

defineExpose({ analisar })
</script>

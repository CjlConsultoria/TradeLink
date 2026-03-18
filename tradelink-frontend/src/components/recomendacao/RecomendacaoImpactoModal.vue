<template>
  <div v-if="show" class="fixed inset-0 z-50 flex items-center justify-center">
    <div class="fixed inset-0 bg-black/60" @click="$emit('close')"></div>
    <div class="bg-white rounded-xl shadow-2xl max-w-4xl w-full mx-4 max-h-[90vh] overflow-hidden flex flex-col relative z-10">
      <!-- Header -->
      <div class="p-4 border-b bg-gray-50 flex justify-between items-start">
        <div>
          <h3 class="text-lg font-bold text-gray-900 flex items-center gap-2">
            Impacto da Recomendacao por Cliente
          </h3>
          <div v-if="dados" class="flex items-center gap-3 mt-1.5 text-sm">
            <span :class="dados.tipo === 'COMPRA' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'"
              class="px-2 py-0.5 rounded font-semibold text-xs">{{ dados.tipo }}</span>
            <span class="font-semibold text-gray-900">{{ dados.moeda }}/{{ dados.parMoeda }}</span>
            <span class="text-gray-500">{{ dados.carteiraNome }}</span>
          </div>
        </div>
        <button @click="$emit('close')" class="text-gray-400 hover:text-gray-600 text-xl leading-none">&times;</button>
      </div>

      <!-- Loading -->
      <div v-if="loading" class="flex-1 flex items-center justify-center p-10">
        <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600"></div>
      </div>

      <!-- Error -->
      <div v-else-if="erro" class="p-6 text-center">
        <p class="text-red-600 text-sm">{{ erro }}</p>
        <button @click="carregar" class="mt-2 text-indigo-600 text-sm hover:underline">Tentar novamente</button>
      </div>

      <!-- Content -->
      <div v-else-if="dados" class="flex-1 overflow-y-auto p-4">
        <!-- Summary row -->
        <div class="grid grid-cols-2 md:grid-cols-4 gap-3 mb-4">
          <div class="bg-gray-50 rounded-lg p-3 text-center">
            <div class="text-xs text-gray-500 mb-0.5">Preco Entrada</div>
            <div class="font-bold text-gray-900">{{ formatCurrency(dados.precoEntrada, dados.parMoeda) }}</div>
          </div>
          <div class="bg-gray-50 rounded-lg p-3 text-center">
            <div class="text-xs text-gray-500 mb-0.5">Preco Alvo</div>
            <div class="font-bold text-gray-900">{{ formatCurrency(dados.precoAlvo, dados.parMoeda) }}</div>
          </div>
          <div class="bg-gray-50 rounded-lg p-3 text-center">
            <div class="text-xs text-gray-500 mb-0.5">Qtd Referencia</div>
            <div class="font-bold text-gray-900">{{ formatQtd(dados.quantidade) }}</div>
          </div>
          <div class="bg-gray-50 rounded-lg p-3 text-center">
            <div class="text-xs text-gray-500 mb-0.5">% Alvo Carteira</div>
            <div class="font-bold text-indigo-600">{{ dados.percentualAlvo != null ? dados.percentualAlvo + '%' : '-' }}</div>
          </div>
        </div>

        <div v-if="dados.observacao" class="bg-blue-50 border border-blue-200 rounded-lg p-3 mb-4 text-sm text-blue-800">
          {{ dados.observacao }}
        </div>

        <div class="text-xs text-gray-500 mb-2">Margem de erro: {{ dados.margemErro }}% | Moeda ref.: {{ dados.moedaReferencia }}</div>

        <!-- Per-client cards -->
        <div class="space-y-4">
          <div v-for="cli in dados.clientes" :key="cli.clienteId"
            class="border rounded-xl overflow-hidden"
            :class="cli.statusAntes === 'CRITICO' ? 'border-red-300' : cli.statusAntes === 'ATENCAO' ? 'border-amber-300' : 'border-gray-200'">

            <!-- Client header -->
            <div class="px-4 py-3 flex items-center justify-between cursor-pointer select-none"
              :class="cli.statusAntes === 'CRITICO' ? 'bg-red-50' : cli.statusAntes === 'ATENCAO' ? 'bg-amber-50' : 'bg-gray-50'"
              @click="toggleCliente(cli.clienteId)">
              <div class="flex items-center gap-3">
                <span class="font-semibold text-gray-900 text-sm">{{ cli.clienteNome }}</span>
                <span class="text-xs text-gray-500">{{ formatCurrency(cli.valorTotalPortfolio, dados.moedaReferencia) }}</span>
              </div>
              <div class="flex items-center gap-2">
                <!-- Status transition -->
                <StatusPill :status="cli.statusAntes" />
                <span class="text-gray-400">&rarr;</span>
                <StatusPill :status="cli.statusDepois" />
                <!-- Expand arrow -->
                <svg class="w-4 h-4 text-gray-400 transition-transform" :class="{ 'rotate-180': expandidos.has(cli.clienteId) }" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"/></svg>
              </div>
            </div>

            <!-- Client detail (expandable) -->
            <div v-if="expandidos.has(cli.clienteId)" class="px-4 py-3 bg-white">
              <!-- Qty and value for this client -->
              <div v-if="cli.quantidadeCliente && cli.quantidadeCliente > 0" class="flex gap-4 mb-3 text-sm">
                <div class="bg-indigo-50 rounded-lg px-3 py-2">
                  <span class="text-gray-500 text-xs">Qtd para este cliente:</span>
                  <span class="font-bold text-indigo-700 ml-1">{{ formatQtd(cli.quantidadeCliente) }} {{ dados.moeda }}</span>
                </div>
                <div v-if="cli.valorEstimado" class="bg-indigo-50 rounded-lg px-3 py-2">
                  <span class="text-gray-500 text-xs">Valor estimado:</span>
                  <span class="font-bold text-indigo-700 ml-1">{{ formatCurrency(cli.valorEstimado, dados.moedaReferencia) }}</span>
                </div>
              </div>
              <div v-else class="text-xs text-gray-500 mb-3 italic">Este cliente nao necessita ajuste para este ativo.</div>

              <!-- Before / After comparison -->
              <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
                <!-- ANTES -->
                <div>
                  <div class="text-xs font-semibold text-gray-500 uppercase mb-1.5 flex items-center gap-1">
                    <span class="w-2 h-2 rounded-full" :class="statusColor(cli.statusAntes)"></span>
                    Antes (Atual)
                  </div>
                  <table class="w-full text-xs">
                    <thead>
                      <tr class="text-gray-400"><th class="text-left py-1">Ativo</th><th class="text-right py-1">% Atual</th><th class="text-right py-1">% Alvo</th><th class="text-right py-1">Desvio</th><th class="text-center py-1">Acao</th></tr>
                    </thead>
                    <tbody>
                      <tr v-for="a in cli.ativosAntes" :key="'a-'+a.simbolo" class="border-t border-gray-50"
                        :class="{ 'bg-red-50': a.comprar || a.vender }">
                        <td class="py-1 font-medium" :class="a.simbolo === dados.moeda ? 'text-indigo-700 font-bold' : 'text-gray-700'">{{ a.simbolo }}</td>
                        <td class="text-right py-1" :class="desvioColor(a)">{{ fmtPct(a.percentualAtual) }}</td>
                        <td class="text-right py-1 text-gray-500">{{ fmtPct(a.percentualAlvo) }}</td>
                        <td class="text-right py-1 font-semibold" :class="desvioColor(a)">{{ fmtDiff(a.diferencaPercentual) }}</td>
                        <td class="text-center py-1">
                          <span v-if="a.comprar" class="text-green-600 font-semibold">Comprar</span>
                          <span v-else-if="a.vender" class="text-red-600 font-semibold">Vender</span>
                          <span v-else class="text-gray-400">OK</span>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>

                <!-- DEPOIS -->
                <div>
                  <div class="text-xs font-semibold text-gray-500 uppercase mb-1.5 flex items-center gap-1">
                    <span class="w-2 h-2 rounded-full" :class="statusColor(cli.statusDepois)"></span>
                    Depois (Projetado)
                  </div>
                  <table class="w-full text-xs">
                    <thead>
                      <tr class="text-gray-400"><th class="text-left py-1">Ativo</th><th class="text-right py-1">% Proj.</th><th class="text-right py-1">% Alvo</th><th class="text-right py-1">Desvio</th><th class="text-center py-1">Acao</th></tr>
                    </thead>
                    <tbody>
                      <tr v-for="a in cli.ativosDepois" :key="'d-'+a.simbolo" class="border-t border-gray-50"
                        :class="{ 'bg-green-50': wasFixedByRec(cli, a), 'bg-red-50': !wasFixedByRec(cli, a) && (a.comprar || a.vender) }">
                        <td class="py-1 font-medium" :class="a.simbolo === dados.moeda ? 'text-indigo-700 font-bold' : 'text-gray-700'">{{ a.simbolo }}</td>
                        <td class="text-right py-1" :class="desvioColor(a)">{{ fmtPct(a.percentualAtual) }}</td>
                        <td class="text-right py-1 text-gray-500">{{ fmtPct(a.percentualAlvo) }}</td>
                        <td class="text-right py-1 font-semibold" :class="desvioColor(a)">{{ fmtDiff(a.diferencaPercentual) }}</td>
                        <td class="text-center py-1">
                          <span v-if="a.comprar" class="text-green-600 font-semibold">Comprar</span>
                          <span v-else-if="a.vender" class="text-red-600 font-semibold">Vender</span>
                          <span v-else class="text-emerald-500 font-semibold">&#10003;</span>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Footer -->
      <div v-if="dados" class="p-3 border-t bg-gray-50 flex justify-between items-center text-xs text-gray-500">
        <span>{{ dados.clientes?.length || 0 }} cliente(s) na carteira</span>
        <button @click="$emit('close')" class="px-4 py-1.5 bg-gray-200 text-gray-700 rounded-lg text-sm font-medium hover:bg-gray-300">Fechar</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, h, defineComponent } from 'vue'
import recomendacaoApi from '../../api/recomendacaoApi'
import { formatCurrency } from '../../utils/formatters'

const props = defineProps({
  show: Boolean,
  recomendacaoId: { type: [Number, String], default: null }
})

defineEmits(['close'])

const dados = ref(null)
const loading = ref(false)
const erro = ref('')
const expandidos = ref(new Set())

// Status pill inline component
const StatusPill = defineComponent({
  props: { status: String },
  setup(props) {
    return () => {
      const cls = props.status === 'CRITICO'
        ? 'bg-red-100 text-red-700 border-red-200'
        : props.status === 'ATENCAO'
          ? 'bg-amber-100 text-amber-700 border-amber-200'
          : 'bg-green-100 text-green-700 border-green-200'
      return h('span', { class: `px-2 py-0.5 rounded-full text-xs font-semibold border ${cls}` }, props.status || 'OK')
    }
  }
})

function statusColor(status) {
  return status === 'CRITICO' ? 'bg-red-500' : status === 'ATENCAO' ? 'bg-amber-500' : 'bg-green-500'
}

function desvioColor(ativo) {
  if (ativo.comprar || ativo.vender) return 'text-red-600'
  return 'text-gray-700'
}

function fmtPct(v) {
  if (v == null) return '-'
  return Number(v).toFixed(1) + '%'
}

function fmtDiff(v) {
  if (v == null) return '-'
  const n = Number(v)
  return (n > 0 ? '+' : '') + n.toFixed(1) + '%'
}

function formatQtd(v) {
  if (v == null) return '-'
  const n = Number(v)
  return n < 1 ? n.toFixed(8).replace(/0+$/, '').replace(/\.$/, '') : n.toLocaleString('pt-BR', { maximumFractionDigits: 4 })
}

function wasFixedByRec(cli, ativoDepois) {
  if (ativoDepois.simbolo !== dados.value?.moeda) return false
  const antes = cli.ativosAntes?.find(a => a.simbolo === ativoDepois.simbolo)
  if (!antes) return false
  return (antes.comprar || antes.vender) && !ativoDepois.comprar && !ativoDepois.vender
}

function toggleCliente(id) {
  const s = new Set(expandidos.value)
  if (s.has(id)) s.delete(id)
  else s.add(id)
  expandidos.value = s
}

async function carregar() {
  if (!props.recomendacaoId) return
  loading.value = true
  erro.value = ''
  dados.value = null
  expandidos.value = new Set()
  try {
    const res = await recomendacaoApi.impactoClientes(props.recomendacaoId)
    dados.value = res.data
    // Auto-expand clients with critical status or that need action
    for (const cli of (res.data.clientes || [])) {
      if (cli.statusAntes === 'CRITICO' || cli.statusAntes === 'ATENCAO' ||
          (cli.quantidadeCliente && Number(cli.quantidadeCliente) > 0)) {
        expandidos.value.add(cli.clienteId)
      }
    }
  } catch (e) {
    erro.value = e.response?.data?.erro || 'Erro ao carregar impacto.'
  } finally {
    loading.value = false
  }
}

watch(() => props.show, (v) => {
  if (v && props.recomendacaoId) {
    carregar()
  }
})
</script>

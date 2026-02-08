<template>
  <div>
    <h2 class="page-title">Faturas e pagamentos</h2>

    <!-- Cliente: não mostra dados da fatura (são do consultor). Apenas mensagem de bloqueio ou em dia. -->
    <template v-if="isCliente">
      <div v-if="acessoBloqueado" class="card p-6 mb-6 border-2 border-amber-400 bg-amber-50">
        <p class="font-semibold text-amber-900">Acesso bloqueado</p>
        <p class="text-sm text-amber-800 mt-1">O acesso à plataforma está bloqueado por falta de pagamento em dia da assinatura da sua empresa.</p>
        <p class="text-sm text-amber-800 mt-2">Entre em contato com seu <strong>consultor</strong> para regularizar a situação e liberar o acesso.</p>
      </div>
      <div v-else class="card p-6 mb-6 border border-gray-200">
        <p class="text-gray-700">Para informações sobre faturas e pagamentos, entre em contato com seu <strong>consultor</strong>.</p>
      </div>
    </template>

    <!-- Consultor: visão completa com próxima fatura, botões de pagamento e histórico -->
    <template v-else>
      <div v-if="acessoBloqueado" class="card p-6 mb-6 border-2 border-amber-400 bg-amber-50">
        <p class="font-semibold text-amber-900">Assinatura em atraso</p>
        <p class="text-sm text-amber-800 mt-1">O acesso está bloqueado por falta de pagamento em dia. Regularize abaixo (cartão, boleto ou com o administrador) para continuar usando a plataforma.</p>
      </div>

      <div class="card p-6 mb-6">
        <h3 class="section-title">Próxima fatura</h3>
        <template v-if="proxima?.temAssinatura || proxima?.dataVencimento">
          <p class="text-gray-600">Vencimento: <strong>{{ formatDate(proxima?.dataVencimento) }}</strong></p>
          <p class="text-xl font-semibold text-indigo-600 mt-1">{{ formatCurrency(proxima?.valor) }}</p>
          <p v-if="proxima?.planoNome" class="text-sm text-gray-500">{{ proxima.planoNome }}</p>
          <div class="flex flex-wrap gap-3 mt-4">
            <button
              type="button"
              class="btn-primary"
              :disabled="loadingCheckout"
              @click="abrirCheckoutCartaoBoleto"
            >
              {{ loadingCheckout ? 'Abrindo...' : 'Pagar com cartão ou boleto' }}
            </button>
            <button
              type="button"
              class="px-4 py-2 rounded-lg text-sm font-medium bg-gray-200 text-gray-500 cursor-not-allowed"
              disabled
              title="Em breve"
            >
              PIX em breve
            </button>
          </div>
        </template>
        <p v-else class="text-gray-500">Nenhum plano ativo. Entre em contato com o administrador.</p>
      </div>

      <div class="card p-6">
        <h3 class="section-title">Histórico de faturas</h3>
        <LoadingSpinner v-if="loading" />
        <template v-else>
          <div class="overflow-x-auto">
            <table class="w-full text-sm">
              <thead>
                <tr class="border-b border-gray-200">
                  <th class="text-left py-3 px-2 font-medium text-gray-500">Vencimento</th>
                  <th class="text-left py-3 px-2 font-medium text-gray-500">Pagamento</th>
                  <th class="text-left py-3 px-2 font-medium text-gray-500">Valor</th>
                  <th class="text-left py-3 px-2 font-medium text-gray-500">Status</th>
                  <th class="text-left py-3 px-2 font-medium text-gray-500">Forma</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="f in faturas" :key="f.id" class="border-b border-gray-100">
                  <td class="py-3 px-2">{{ formatDate(f.dataVencimento) }}</td>
                  <td class="py-3 px-2">{{ f.dataPagamento ? formatDate(f.dataPagamento) : '-' }}</td>
                  <td class="py-3 px-2 font-medium">{{ formatCurrency(f.valor) }}</td>
                  <td class="py-3 px-2">
                    <span class="px-2 py-0.5 rounded-full text-xs" :class="badgeStatus(f.status)">
                      {{ f.status }}
                    </span>
                  </td>
                  <td class="py-3 px-2 text-gray-600">{{ f.formaPagamento || '-' }}</td>
                </tr>
              </tbody>
            </table>
          </div>
          <EmptyState v-if="!loading && faturas.length === 0" message="Nenhuma fatura no histórico." />
        </template>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useToast } from '../../composables/useToast'
import faturaApi from '../../api/faturaApi'
import { formatCurrency, formatDate } from '../../utils/formatters'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'
import EmptyState from '../../components/common/EmptyState.vue'

const route = useRoute()
const toast = useToast()
const isCliente = computed(() => route.path.startsWith('/cliente'))

const loading = ref(true)
const loadingCheckout = ref(false)
const faturas = ref([])
const proxima = ref(null)
const acessoPermitido = ref(true)

const acessoBloqueado = computed(() => !acessoPermitido.value)

async function carregar() {
  loading.value = true
  try {
    const res = isCliente.value ? await faturaApi.listarMinhasCliente() : await faturaApi.listarMinhas()
    const data = res.data
    faturas.value = data.faturas || []
    proxima.value = data.proxima || null
    acessoPermitido.value = data.acessoPermitido !== false
  } catch (e) {
    toast.error('Erro ao carregar faturas.')
  } finally {
    loading.value = false
  }
}

async function abrirCheckoutCartaoBoleto() {
  loadingCheckout.value = true
  try {
    const res = isCliente.value ? await faturaApi.checkoutCartaoBoletoCliente() : await faturaApi.checkoutCartaoBoleto()
    const url = res.data?.checkoutUrl
    if (url) {
      window.location.href = url
      return
    }
    toast.error('URL de pagamento não retornada.')
  } catch (e) {
    toast.error(e.response?.data?.erro || e.response?.data?.mensagem || 'Erro ao abrir pagamento.')
  } finally {
    loadingCheckout.value = false
  }
}

function badgeStatus(status) {
  const map = { PAGA: 'bg-green-100 text-green-800', PENDENTE: 'bg-amber-100 text-amber-800', VENCIDA: 'bg-red-100 text-red-800' }
  return map[status] || 'bg-gray-100 text-gray-700'
}

onMounted(async () => {
  const sessionId = route.query.session_id
  if (sessionId) {
    try {
      const res = isCliente.value
        ? await faturaApi.confirmarStripeCliente(sessionId)
        : await faturaApi.confirmarStripeConsultor(sessionId)
      if (res.data?.confirmado) {
        toast.success('Pagamento confirmado. Fatura registrada.')
      }
    } catch (e) {
      console.error(e)
      toast.error('Não foi possível confirmar o pagamento. Tente recarregar a página.')
    }
    window.history.replaceState({}, '', route.path)
  } else if (route.query.pagamento === 'ok') {
    toast.success('Pagamento realizado. O acesso será liberado em instantes.')
    window.history.replaceState({}, '', route.path)
  }
  await carregar()
})
</script>

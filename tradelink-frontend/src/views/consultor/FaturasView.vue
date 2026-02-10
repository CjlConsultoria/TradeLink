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
              :disabled="loadingCheckout || loadingEmbedded"
              @click="abrirPagamentoEmbutido"
            >
              {{ loadingEmbedded ? 'Abrindo...' : 'Pagar (cartão, PIX ou boleto)' }}
            </button>
            <button
              type="button"
              class="px-4 py-2 rounded-lg text-sm font-medium border border-gray-300 text-gray-600 hover:bg-gray-50"
              :disabled="loadingCheckout || loadingEmbedded"
              @click="abrirCheckoutCartaoBoleto"
            >
              Abrir em outra página
            </button>
          </div>
        </template>

        <!-- Modal: pagamento embutido (Stripe Payment Element) -->
        <div v-if="showModalPagamento" class="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div class="fixed inset-0 bg-black/50" @click="fecharModalPagamento"></div>
          <div class="bg-white rounded-xl shadow-xl max-w-md w-full relative z-10 max-h-[90vh] overflow-y-auto">
            <div class="p-6">
              <h3 class="text-lg font-semibold text-gray-900 mb-2">Pagamento</h3>
              <p class="text-sm text-gray-600 mb-4">Escolha a forma de pagamento abaixo. Você não sai do sistema.</p>
              <div v-if="erroModal" class="mb-4 p-3 rounded-lg bg-red-50 text-red-700 text-sm">{{ erroModal }}</div>
              <div v-if="!publishableKey" class="mb-4 p-3 rounded-lg bg-amber-50 text-amber-800 text-sm">
                Configure <code class="text-xs">STRIPE_PUBLISHABLE_KEY</code> no servidor para pagar aqui. Use "Abrir em outra página" como alternativa.
              </div>
              <div id="payment-element" ref="paymentElementRef" class="min-h-[200px] mb-4"></div>
              <div class="flex gap-3">
                <button type="button" class="flex-1 px-4 py-2 rounded-lg border border-gray-300 text-gray-700 hover:bg-gray-50" :disabled="enviandoPagamento" @click="fecharModalPagamento">Cancelar</button>
                <button type="button" class="flex-1 btn-primary" :disabled="!stripeReady || enviandoPagamento" @click="confirmarPagamento">
                  {{ enviandoPagamento ? 'Processando...' : 'Pagar' }}
                </button>
              </div>
            </div>
          </div>
        </div>
        <p v-else class="text-gray-500">Nenhum plano ativo. Entre em contato com o administrador.</p>
      </div>

      <div class="card p-6">
        <h3 class="section-title">Histórico de faturas</h3>
        <LoadingSpinner v-if="loading" />
        <template v-else>
          <div class="overflow-x-auto">
            <table class="w-full text-sm table-responsive">
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
import { ref, onMounted, computed, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { loadStripe } from '@stripe/stripe-js'
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
const loadingEmbedded = ref(false)
const faturas = ref([])
const proxima = ref(null)
const acessoPermitido = ref(true)

const showModalPagamento = ref(false)
const publishableKey = ref('')
const clientSecret = ref('')
const stripeReady = ref(false)
const enviandoPagamento = ref(false)
const erroModal = ref('')
const paymentElementRef = ref(null)
let stripeInstance = null
let elementsInstance = null
let paymentElementInstance = null

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

async function abrirPagamentoEmbutido() {
  if (isCliente.value) return
  loadingEmbedded.value = true
  erroModal.value = ''
  stripeReady.value = false
  try {
    const res = await faturaApi.checkoutEmbedded()
    const data = res.data || res
    clientSecret.value = data.clientSecret || ''
    publishableKey.value = (data.publishableKey || '').trim()
    if (!clientSecret.value) {
      erroModal.value = 'Não foi possível iniciar o pagamento.'
      return
    }
    showModalPagamento.value = true
    if (!publishableKey.value) return
    await nextTick()
    const stripe = await loadStripe(publishableKey.value)
    if (!stripe) {
      erroModal.value = 'Stripe não carregou. Tente "Abrir em outra página".'
      return
    }
    const elements = stripe.elements({ clientSecret: clientSecret.value })
    const paymentElement = elements.create('payment')
    await nextTick()
    const el = document.getElementById('payment-element')
    if (el) {
      paymentElement.mount('#payment-element')
      stripeInstance = stripe
      elementsInstance = elements
      paymentElementInstance = paymentElement
      stripeReady.value = true
    }
  } catch (e) {
    toast.error(e.response?.data?.erro || e.response?.data?.mensagem || 'Erro ao abrir pagamento.')
    showModalPagamento.value = false
  } finally {
    loadingEmbedded.value = false
  }
}

function fecharModalPagamento() {
  if (paymentElementInstance && paymentElementRef.value) {
    try {
      paymentElementInstance.unmount()
    } catch (_) {}
  }
  paymentElementInstance = null
  elementsInstance = null
  stripeInstance = null
  stripeReady.value = false
  clientSecret.value = ''
  erroModal.value = ''
  showModalPagamento.value = false
}

async function confirmarPagamento() {
  if (!stripeInstance || !elementsInstance || !clientSecret.value) return
  enviandoPagamento.value = true
  erroModal.value = ''
  try {
    const returnUrl = `${window.location.origin}${route.path}?stripe_return=1`
    const { error } = await stripeInstance.confirmPayment({
      elements: elementsInstance,
      confirmParams: { return_url: returnUrl }
    })
    if (error) {
      erroModal.value = error.message || 'Erro no pagamento.'
      return
    }
    fecharModalPagamento()
    toast.success('Pagamento realizado. A fatura será registrada em instantes.')
    await carregar()
  } catch (e) {
    erroModal.value = e.message || 'Erro ao processar.'
  } finally {
    enviandoPagamento.value = false
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
  const stripeReturn = route.query.stripe_return || route.query.redirect_status
  const paymentIntent = route.query.payment_intent
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
  } else if (route.query.pagamento === 'ok' || stripeReturn === 'succeeded' || (stripeReturn === '1' && paymentIntent)) {
    toast.success('Pagamento realizado. O acesso será liberado em instantes.')
    window.history.replaceState({}, '', route.path)
  }
  await carregar()
})
</script>

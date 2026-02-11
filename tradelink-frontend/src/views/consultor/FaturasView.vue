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
          <div class="mt-4 space-y-3">
            <div class="flex flex-col sm:flex-row gap-3">
              <button
                type="button"
                class="flex-1 inline-flex flex-col items-center justify-center px-5 py-3 rounded-xl bg-indigo-600 text-white font-medium hover:bg-indigo-700 focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 disabled:opacity-50 disabled:pointer-events-none transition-colors"
                :disabled="loadingCheckout || loadingEmbedded"
                @click.stop.prevent="abrirPagamentoEmbutido"
              >
                <span>{{ loadingEmbedded ? 'Abrindo...' : 'Pagar aqui (cartão ou boleto)' }}</span>
                <span class="text-xs font-normal text-indigo-100 mt-0.5">Pagamento seguro nesta tela, você não sai do sistema</span>
              </button>
              <button
                type="button"
                class="flex-1 inline-flex flex-col items-center justify-center px-5 py-3 rounded-xl border-2 border-gray-300 text-gray-700 font-medium hover:bg-gray-50 hover:border-gray-400 focus:ring-2 focus:ring-gray-400 focus:ring-offset-2 disabled:opacity-50 disabled:pointer-events-none transition-colors"
                :disabled="loadingCheckout || loadingEmbedded"
                @click.stop.prevent="abrirCheckoutCartaoBoleto"
              >
                <span>Abrir no site do Stripe</span>
                <span class="text-xs font-normal text-gray-500 mt-0.5">Será redirecionado para outra página</span>
              </button>
            </div>
          </div>
        </template>
        <p v-else class="text-gray-500">Nenhum plano ativo. Entre em contato com o administrador.</p>

        <!-- Modal: pagamento embutido (Stripe Payment Element) -->
        <div v-if="showModalPagamento" class="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div class="fixed inset-0 bg-black/50" aria-hidden="true" @click="fecharModalPagamento"></div>
          <div class="bg-white rounded-2xl shadow-2xl max-w-md w-full relative z-10 max-h-[90vh] overflow-y-auto" role="dialog" aria-labelledby="modal-title" @click.stop>
            <div class="p-6">
              <h3 id="modal-title" class="text-xl font-semibold text-gray-900 mb-1">Pagamento da fatura</h3>
              <p class="text-sm text-gray-500 mb-4">Preencha os dados abaixo. O pagamento é processado de forma segura pelo Stripe.</p>
              <!-- Resumo da fatura -->
              <div class="mb-4 p-4 rounded-xl bg-gray-50 border border-gray-200">
                <div class="flex justify-between text-sm">
                  <span class="text-gray-600">Valor a pagar</span>
                  <span class="font-semibold text-gray-900">{{ formatCurrency(proxima?.valor) }}</span>
                </div>
                <div v-if="proxima?.dataVencimento" class="flex justify-between text-sm mt-1">
                  <span class="text-gray-600">Vencimento</span>
                  <span class="text-gray-800">{{ formatDate(proxima.dataVencimento) }}</span>
                </div>
                <div v-if="proxima?.planoNome" class="flex justify-between text-sm mt-1">
                  <span class="text-gray-600">Plano</span>
                  <span class="text-gray-800">{{ proxima.planoNome }}</span>
                </div>
              </div>
              <div v-if="erroModal" class="mb-4 p-3 rounded-lg bg-red-50 text-red-700 text-sm">{{ erroModal }}</div>
              <div v-if="!publishableKey" class="mb-4 p-3 rounded-lg bg-amber-50 text-amber-800 text-sm">
                Configure <code class="text-xs">STRIPE_PUBLISHABLE_KEY</code> no servidor. Como alternativa, use o botão "Abrir no site do Stripe" na tela anterior.
              </div>
              <div class="min-h-[220px] w-full mb-4 relative">
                <div v-if="!stripeReady && showModalPagamento" class="absolute inset-0 flex items-center justify-center bg-gray-50 rounded-lg text-gray-500 text-sm">Carregando opções de pagamento...</div>
                <div id="payment-element" ref="paymentElementRef" class="w-full min-h-[200px]"></div>
              </div>
              <div class="flex gap-3">
                <button type="button" class="flex-1 px-4 py-2.5 rounded-xl border border-gray-300 text-gray-700 font-medium hover:bg-gray-50" :disabled="enviandoPagamento" @click="fecharModalPagamento">Cancelar</button>
                <button type="button" class="flex-1 px-4 py-2.5 rounded-xl bg-indigo-600 text-white font-medium hover:bg-indigo-700 disabled:opacity-50 disabled:pointer-events-none" :disabled="!stripeReady || enviandoPagamento" @click="confirmarPagamento">
                  {{ enviandoPagamento ? 'Processando...' : 'Confirmar pagamento' }}
                </button>
              </div>
            </div>
          </div>
        </div>
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
                  <th class="text-right py-3 px-2 font-medium text-gray-500">Ações</th>
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
                  <td class="py-3 px-2 text-right">
                    <button type="button" class="text-indigo-600 hover:underline text-xs font-medium" @click="baixarPdf(f.id)">Baixar PDF</button>
                  </td>
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
const currentPaymentIntentId = ref('')
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
    currentPaymentIntentId.value = data.paymentIntentId || ''
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
    const elementsOptions = {
      clientSecret: clientSecret.value,
      locale: 'pt-BR',
      appearance: { theme: 'stripe', variables: { borderRadius: '8px' } }
    }
    const name = (data.billingDetailsName || '').trim()
    const email = (data.billingDetailsEmail || '').trim()
    if (name || email) {
      elementsOptions.defaultValues = {
        billingDetails: {
          ...(name && { name }),
          ...(email && { email })
        }
      }
    }
    const elements = stripe.elements(elementsOptions)
    const paymentOptions = { layout: 'tabs' }
    if (name || email) {
      paymentOptions.defaultValues = {
        billingDetails: { ...(name && { name }), ...(email && { email }) }
      }
    }
    const paymentElement = elements.create('payment', paymentOptions)
    await nextTick()
    const el = document.getElementById('payment-element')
    if (!el) {
      erroModal.value = 'Erro ao carregar formulário de pagamento. Tente novamente.'
      return
    }
    el.innerHTML = ''
    await new Promise(r => setTimeout(r, 150))
    paymentElement.mount('#payment-element')
    stripeInstance = stripe
    elementsInstance = elements
    paymentElementInstance = paymentElement
    paymentElement.on('ready', () => {
      stripeReady.value = true
    })
    paymentElement.on('loaderror', (e) => {
      erroModal.value = e?.error?.message || 'Erro ao carregar opções de pagamento.'
    })
  } catch (e) {
    toast.error(e.response?.data?.erro || e.response?.data?.mensagem || 'Erro ao abrir pagamento.')
    showModalPagamento.value = false
  } finally {
    loadingEmbedded.value = false
  }
}

function removerWidgetStripe() {
  try {
    const links = document.querySelectorAll('a[href*="stripe.com"]')
    links.forEach((el) => {
      const parent = el.parentElement
      if (parent && (parent.tagName === 'BODY' || parent.childElementCount === 1)) {
        parent.removeChild(el)
        if (parent.tagName !== 'BODY' && parent.childElementCount === 0) parent.remove()
      } else {
        el.remove()
      }
    })
  } catch (_) {}
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
  currentPaymentIntentId.value = ''
  erroModal.value = ''
  showModalPagamento.value = false
  removerWidgetStripe()
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
    const piId = currentPaymentIntentId.value
    fecharModalPagamento()
    if (piId) {
      try {
        const confirmRes = isCliente.value
          ? await faturaApi.confirmarPagamentoEmbutidoCliente(piId)
          : await faturaApi.confirmarPagamentoEmbutidoConsultor(piId)
        if (confirmRes.data?.confirmado) {
          toast.success('Pagamento confirmado. Fatura registrada e próxima fatura atualizada.')
        }
      } catch (_) {
        toast.success('Pagamento realizado. Se a fatura não aparecer, recarregue a página.')
      }
    } else {
      toast.success('Pagamento realizado. A fatura será registrada em instantes.')
    }
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

async function baixarPdf(faturaId) {
  if (isCliente.value) return
  try {
    const res = await faturaApi.getPdfBlobConsultor(faturaId)
    const url = URL.createObjectURL(res.data)
    window.open(url, '_blank')
    setTimeout(() => URL.revokeObjectURL(url), 60000)
    toast.success('PDF aberto em nova aba.')
  } catch (e) {
    toast.error(e.response?.data?.mensagem || 'Erro ao baixar PDF.')
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
  } else if (paymentIntent && (route.query.pagamento === 'ok' || stripeReturn === 'succeeded' || stripeReturn === '1')) {
    try {
      const confirmRes = isCliente.value
        ? await faturaApi.confirmarPagamentoEmbutidoCliente(paymentIntent)
        : await faturaApi.confirmarPagamentoEmbutidoConsultor(paymentIntent)
      if (confirmRes.data?.confirmado) {
        toast.success('Pagamento confirmado. Fatura registrada e próxima fatura atualizada.')
      } else {
        toast.success('Pagamento realizado. O acesso será liberado em instantes.')
      }
    } catch (e) {
      console.error(e)
      toast.success('Pagamento realizado. Se a fatura não aparecer, recarregue a página.')
    }
    window.history.replaceState({}, '', route.path)
  } else if (route.query.pagamento === 'ok' || stripeReturn === 'succeeded') {
    toast.success('Pagamento realizado. O acesso será liberado em instantes.')
    window.history.replaceState({}, '', route.path)
  }
  await carregar()
  removerWidgetStripe()
})
</script>

<template>
  <!-- Cliente normal (com consultor): delega para FaturasView compartilhado -->
  <FaturasView v-if="!isAutoGestao" />

  <!-- Cliente auto-gestão: visão própria de faturas individuais -->
  <div v-else>
    <h2 class="page-title">Faturas e pagamentos</h2>

    <!-- Banner de acesso bloqueado -->
    <div v-if="acessoBloqueado" class="card p-6 mb-6 border-2 border-amber-400 bg-amber-50">
      <p class="font-semibold text-amber-900">Assinatura vencida</p>
      <p class="text-sm text-amber-800 mt-1">
        Sua assinatura Auto-Gestão está vencida. Renove abaixo para continuar acessando a plataforma.
      </p>
    </div>

    <!-- Próxima fatura / Assinatura -->
    <div class="card p-6 mb-6">
      <h3 class="section-title">Sua assinatura</h3>
      <template v-if="proxima?.temAssinatura || proxima?.dataVencimento">
        <div class="flex items-center gap-3 mb-2">
          <span class="px-3 py-1 rounded-full text-xs font-semibold"
                :class="acessoBloqueado ? 'bg-red-100 text-red-800' : 'bg-green-100 text-green-800'">
            {{ acessoBloqueado ? 'Vencida' : 'Ativa' }}
          </span>
          <span class="text-sm text-gray-500">{{ proxima?.planoNome || 'Auto-Gestão' }}</span>
        </div>
        <p class="text-gray-600">
          {{ acessoBloqueado ? 'Venceu em:' : 'Próximo vencimento:' }}
          <strong>{{ formatDate(proxima?.dataVencimento) }}</strong>
        </p>
        <p class="text-xl font-semibold text-indigo-600 mt-1">{{ formatCurrency(proxima?.valor) }}<span class="text-sm font-normal text-gray-500">/mês</span></p>
        <div class="mt-4">
          <button
            type="button"
            class="inline-flex items-center gap-2 px-5 py-3 rounded-xl bg-indigo-600 text-white font-medium hover:bg-indigo-700 focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 disabled:opacity-50 transition-colors"
            :disabled="loadingCheckout"
            @click="pagarAutoGestao"
          >
            <span>{{ loadingCheckout ? 'Abrindo...' : (acessoBloqueado ? 'Renovar assinatura' : 'Pagar próxima fatura') }}</span>
          </button>
        </div>
      </template>
      <p v-else class="text-gray-500">Nenhuma assinatura ativa.</p>
    </div>

    <!-- Histórico de faturas -->
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
                <th class="text-left py-3 px-2 font-medium text-gray-500">Descrição</th>
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
                <td class="py-3 px-2 text-gray-600">{{ f.descricaoServico || '-' }}</td>
              </tr>
            </tbody>
          </table>
        </div>
        <EmptyState v-if="faturas.length === 0" message="Nenhuma fatura no histórico." />
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import { useToast } from '../../composables/useToast'
import faturaApi from '../../api/faturaApi'
import autoGestaoApi from '../../api/autoGestaoApi'
import { formatCurrency, formatDate } from '../../utils/formatters'
import FaturasView from '../consultor/FaturasView.vue'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'
import EmptyState from '../../components/common/EmptyState.vue'

const route = useRoute()
const authStore = useAuthStore()
const toast = useToast()

const isAutoGestao = authStore.user?.autoGestaoAtiva || (authStore.user?.clienteExcluido && !authStore.user?.empresaId)

const loading = ref(true)
const loadingCheckout = ref(false)
const faturas = ref([])
const proxima = ref(null)
const acessoPermitido = ref(true)
const acessoBloqueado = ref(false)

async function carregar() {
  loading.value = true
  try {
    const res = await faturaApi.listarMinhasCliente()
    const data = res.data
    faturas.value = data.faturas || []
    proxima.value = data.proxima || null
    acessoPermitido.value = data.acessoPermitido !== false
    acessoBloqueado.value = !acessoPermitido.value
  } catch (e) {
    toast.error('Erro ao carregar faturas.')
  } finally {
    loading.value = false
  }
}

async function pagarAutoGestao() {
  loadingCheckout.value = true
  try {
    const returnUrl = `${window.location.origin}/cliente/faturas`
    const res = await autoGestaoApi.checkoutAutoGestao(returnUrl)
    window.location.href = res.data.checkoutUrl
  } catch (e) {
    toast.error(e.response?.data?.erro || 'Erro ao iniciar pagamento.')
    loadingCheckout.value = false
  }
}

function badgeStatus(status) {
  const map = { PAGA: 'bg-green-100 text-green-800', PENDENTE: 'bg-amber-100 text-amber-800', VENCIDA: 'bg-red-100 text-red-800' }
  return map[status] || 'bg-gray-100 text-gray-700'
}

onMounted(async () => {
  if (!isAutoGestao) return // FaturasView handles its own mounting

  // Se retornou do Stripe checkout
  const sessionId = route.query.session_id
  if (sessionId) {
    try {
      await autoGestaoApi.confirmarPagamento(sessionId)
      toast.success('Pagamento confirmado! Sua assinatura foi renovada.')
    } catch (e) {
      // webhook pode já ter processado
      toast.success('Pagamento realizado. A fatura será registrada em instantes.')
    }
    window.history.replaceState({}, '', route.path)
  } else if (route.query.pagamento === 'ok') {
    toast.success('Pagamento realizado com sucesso!')
    window.history.replaceState({}, '', route.path)
  }

  await carregar()
})
</script>

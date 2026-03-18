<template>
  <div>
    <h1 class="page-title">Minha Mentoria</h1>
    <p class="text-muted mb-6">Gerencie sua assinatura de mentoria contratada pelo marketplace.</p>

    <!-- Loading -->
    <div v-if="loading" class="card p-8 text-center">
      <p class="text-muted">Carregando...</p>
    </div>

    <!-- Sem assinatura marketplace -->
    <div v-else-if="!sub || sub.origemVinculo !== 'MARKETPLACE'" class="card p-8 text-center">
      <p class="text-muted">Voce nao possui uma mentoria ativa via marketplace.</p>
      <router-link to="/cliente/marketplace" class="btn-primary mt-4 inline-block">
        Encontrar Consultor
      </router-link>
    </div>

    <!-- Assinatura ativa -->
    <template v-else>
      <!-- Status Card -->
      <div class="card p-6 mb-6">
        <div class="flex items-center justify-between mb-4">
          <h2 class="section-title mb-0">Assinatura</h2>
          <span class="px-3 py-1 rounded-full text-xs font-semibold" :class="statusClass">
            {{ statusLabel }}
          </span>
        </div>

        <div class="grid md:grid-cols-2 lg:grid-cols-4 gap-4">
          <div>
            <p class="text-xs text-muted mb-1">Consultor</p>
            <p class="font-semibold" style="color: var(--tl-text);">{{ sub.empresaNome || '-' }}</p>
          </div>
          <div>
            <p class="text-xs text-muted mb-1">Valor Mensal</p>
            <p class="font-semibold" style="color: var(--tl-text);">
              R$ {{ sub.marketplacePrecoCliente ? Number(sub.marketplacePrecoCliente).toFixed(2).replace('.', ',') : '-' }}
            </p>
          </div>
          <div>
            <p class="text-xs text-muted mb-1">Proximo Vencimento</p>
            <p class="font-semibold" style="color: var(--tl-text);">{{ formatDate(sub.marketplaceCurrentPeriodEnd) }}</p>
          </div>
          <div>
            <p class="text-xs text-muted mb-1">Status</p>
            <p class="font-semibold" style="color: var(--tl-text);">{{ statusLabel }}</p>
          </div>
        </div>
      </div>

      <!-- Faturas / Historico -->
      <div class="card p-6 mb-6">
        <h2 class="section-title mb-4">Historico de Faturas</h2>

        <div v-if="!sub.faturas || sub.faturas.length === 0" class="text-center py-6">
          <p class="text-muted">Nenhuma fatura registrada.</p>
        </div>

        <div v-else class="overflow-x-auto">
          <table class="w-full text-sm" style="color: var(--tl-text);">
            <thead>
              <tr style="border-bottom: 1px solid var(--tl-border);">
                <th class="text-left py-2 px-3 text-xs text-muted font-medium">Data</th>
                <th class="text-left py-2 px-3 text-xs text-muted font-medium">Descricao</th>
                <th class="text-right py-2 px-3 text-xs text-muted font-medium">Valor</th>
                <th class="text-center py-2 px-3 text-xs text-muted font-medium">Status</th>
                <th class="text-center py-2 px-3 text-xs text-muted font-medium">Recibo</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="fatura in sub.faturas" :key="fatura.id" style="border-bottom: 1px solid var(--tl-border);">
                <td class="py-3 px-3">{{ formatDate(fatura.dataPagamento || fatura.dataVencimento) }}</td>
                <td class="py-3 px-3">{{ fatura.descricaoServico || 'Mentoria Marketplace' }}</td>
                <td class="py-3 px-3 text-right font-medium">
                  R$ {{ Number(fatura.valor).toFixed(2).replace('.', ',') }}
                </td>
                <td class="py-3 px-3 text-center">
                  <span class="px-2 py-0.5 rounded-full text-xs font-medium"
                    :class="fatura.status === 'PAGA' ? 'bg-green-100 text-green-800' : fatura.status === 'VENCIDA' ? 'bg-red-100 text-red-800' : 'bg-amber-100 text-amber-800'">
                    {{ fatura.status === 'PAGA' ? 'Paga' : fatura.status === 'VENCIDA' ? 'Vencida' : 'Pendente' }}
                  </span>
                </td>
                <td class="py-3 px-3 text-center">
                  <button
                    v-if="fatura.status === 'PAGA'"
                    @click="baixarRecibo(fatura.id)"
                    :disabled="downloadingId === fatura.id"
                    class="text-xs font-medium px-3 py-1 rounded-lg transition-colors"
                    style="color: var(--tl-primary); border: 1px solid var(--tl-primary);"
                  >
                    {{ downloadingId === fatura.id ? 'Baixando...' : 'PDF' }}
                  </button>
                  <span v-else class="text-xs text-muted">-</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Actions -->
      <div class="card p-6">
        <h2 class="section-title mb-4">Acoes</h2>

        <div class="space-y-4">
          <!-- Atualizar pagamento -->
          <div class="flex items-center justify-between p-4 rounded-lg" style="background: var(--tl-surface-alt, var(--tl-surface)); border: 1px solid var(--tl-border);">
            <div>
              <p class="font-medium" style="color: var(--tl-text);">Atualizar Metodo de Pagamento</p>
              <p class="text-xs text-muted">Altere seu cartao ou metodo de pagamento no portal Stripe.</p>
            </div>
            <button @click="abrirPortal" :disabled="portalLoading" class="btn-primary text-sm">
              {{ portalLoading ? 'Abrindo...' : 'Gerenciar Pagamento' }}
            </button>
          </div>

          <!-- Cancelar -->
          <div class="flex items-center justify-between p-4 rounded-lg" style="background: var(--tl-surface-alt, var(--tl-surface)); border: 1px solid var(--tl-border);">
            <div>
              <p class="font-medium" style="color: var(--tl-text);">Cancelar Assinatura</p>
              <p class="text-xs text-muted">
                O cancelamento sera efetivado no fim do periodo pago.
                Voce continuara com acesso ate {{ formatDate(sub.marketplaceCurrentPeriodEnd) }}.
              </p>
            </div>
            <button
              v-if="sub.marketplaceStatus !== 'CANCELED'"
              @click="confirmarCancelamento"
              :disabled="cancelLoading"
              class="px-4 py-2 rounded-lg text-sm font-medium border transition-colors"
              style="color: var(--tl-text-muted); border-color: var(--tl-border);"
            >
              {{ cancelLoading ? 'Cancelando...' : 'Cancelar' }}
            </button>
            <span v-else class="text-xs font-medium px-3 py-1 rounded-full bg-amber-100 text-amber-800">
              Cancelamento agendado
            </span>
          </div>
        </div>
      </div>

      <!-- Confirmacao de cancelamento -->
      <div v-if="showConfirmCancel" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
        <div class="card p-6 max-w-md w-full">
          <h3 class="section-title mb-2">Confirmar Cancelamento</h3>
          <p class="text-sm text-muted mb-4">
            Tem certeza que deseja cancelar sua mentoria com <strong>{{ sub.empresaNome }}</strong>?
            Voce continuara com acesso ate o fim do periodo pago ({{ formatDate(sub.marketplaceCurrentPeriodEnd) }}).
            Apos isso, sera desvinculado automaticamente.
          </p>
          <div class="flex gap-3 justify-end">
            <button @click="showConfirmCancel = false" class="px-4 py-2 rounded-lg text-sm" style="color: var(--tl-text-muted);">
              Voltar
            </button>
            <button @click="cancelarAssinatura" :disabled="cancelLoading" class="px-4 py-2 rounded-lg text-sm font-medium bg-red-600 text-white hover:bg-red-700 disabled:opacity-50">
              {{ cancelLoading ? 'Cancelando...' : 'Sim, Cancelar' }}
            </button>
          </div>
        </div>
      </div>

      <!-- Mensagens -->
      <div v-if="sucesso" class="mt-4 p-3 rounded-lg bg-green-50 text-green-700 text-sm">
        {{ sucesso }}
      </div>
      <div v-if="erro" class="mt-4 p-3 rounded-lg bg-red-50 text-red-700 text-sm">
        {{ erro }}
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getMarketplaceSubscription, cancelarMarketplaceSubscription, portalPagamentoMarketplace, downloadFaturaPdf } from '../../api/marketplaceApi'

const loading = ref(true)
const sub = ref(null)
const portalLoading = ref(false)
const cancelLoading = ref(false)
const showConfirmCancel = ref(false)
const sucesso = ref('')
const erro = ref('')
const downloadingId = ref(null)

const statusLabel = computed(() => {
  switch (sub.value?.marketplaceStatus) {
    case 'ACTIVE': return 'Ativa'
    case 'PAST_DUE': return 'Pagamento em Atraso'
    case 'CANCELED': return 'Cancelada'
    default: return 'Ativa'
  }
})

const statusClass = computed(() => {
  switch (sub.value?.marketplaceStatus) {
    case 'ACTIVE': return 'bg-green-100 text-green-800'
    case 'PAST_DUE': return 'bg-red-100 text-red-800'
    case 'CANCELED': return 'bg-amber-100 text-amber-800'
    default: return 'bg-green-100 text-green-800'
  }
})

function formatDate(iso) {
  if (!iso) return '-'
  try {
    return new Date(iso).toLocaleDateString('pt-BR')
  } catch {
    return '-'
  }
}

async function loadSubscription() {
  loading.value = true
  try {
    const res = await getMarketplaceSubscription()
    sub.value = res.data
  } catch (e) {
    erro.value = 'Erro ao carregar dados da assinatura.'
  } finally {
    loading.value = false
  }
}

async function abrirPortal() {
  portalLoading.value = true
  erro.value = ''
  try {
    const res = await portalPagamentoMarketplace()
    window.location.href = res.data.portalUrl
  } catch (e) {
    erro.value = e.response?.data?.erro || e.response?.data?.message || 'Erro ao abrir portal de pagamento.'
    portalLoading.value = false
  }
}

function confirmarCancelamento() {
  showConfirmCancel.value = true
}

async function cancelarAssinatura() {
  cancelLoading.value = true
  erro.value = ''
  sucesso.value = ''
  try {
    await cancelarMarketplaceSubscription()
    sucesso.value = 'Assinatura cancelada. Voce continuara com acesso ate o fim do periodo pago.'
    showConfirmCancel.value = false
    await loadSubscription()
  } catch (e) {
    erro.value = e.response?.data?.erro || e.response?.data?.message || 'Erro ao cancelar assinatura.'
  } finally {
    cancelLoading.value = false
  }
}

async function baixarRecibo(faturaId) {
  downloadingId.value = faturaId
  try {
    const res = await downloadFaturaPdf(faturaId)
    const url = window.URL.createObjectURL(new Blob([res.data], { type: 'application/pdf' }))
    const link = document.createElement('a')
    link.href = url
    link.download = `fatura-${faturaId}.pdf`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  } catch (e) {
    erro.value = 'Erro ao baixar recibo.'
  } finally {
    downloadingId.value = null
  }
}

onMounted(loadSubscription)
</script>

<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-900 via-indigo-950 to-purple-950 flex flex-col items-center justify-center p-4">
    <!-- Loading -->
    <div v-if="loading" class="text-center">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-400 mx-auto mb-4"></div>
      <p class="text-slate-300 text-sm">Carregando...</p>
    </div>

    <!-- Content -->
    <div v-else class="w-full max-w-4xl">
      <!-- Header -->
      <div class="text-center mb-8">
        <div class="inline-flex items-center justify-center w-16 h-16 bg-indigo-600/20 rounded-2xl mb-4">
          <span class="text-3xl">&#9670;</span>
        </div>
        <h1 class="text-2xl font-bold text-white mb-2">TradeLink</h1>
        <p class="text-slate-300 text-lg">
          Ola, <strong class="text-white">{{ status?.nomeCliente || 'Cliente' }}</strong>
        </p>
        <p class="text-slate-400 text-sm mt-2 max-w-lg mx-auto">
          Seu consultor desvinculou sua conta do grupo de atendimento.
          Seus dados e historico de investimentos estao preservados.
          Escolha como deseja prosseguir:
        </p>
      </div>

      <!-- Cards -->
      <div class="grid md:grid-cols-2 gap-6 mb-8">

        <!-- Card A: Auto-Gestao -->
        <div class="bg-white/10 backdrop-blur-sm rounded-2xl border border-white/10 p-6 hover:border-indigo-400/30 transition-all">
          <div class="flex items-center gap-3 mb-4">
            <div class="w-10 h-10 bg-indigo-500/20 rounded-xl flex items-center justify-center">
              <span class="text-xl">&#128188;</span>
            </div>
            <h2 class="text-lg font-semibold text-white">Auto-Gestao</h2>
          </div>
          <p class="text-slate-300 text-sm mb-4">
            Continue gerenciando seu portfolio de forma independente. Acesso completo a todas as funcionalidades.
          </p>
          <ul class="space-y-2 mb-6">
            <li class="flex items-center gap-2 text-sm text-slate-300">
              <span class="text-green-400">&#10003;</span> Dashboard completo
            </li>
            <li class="flex items-center gap-2 text-sm text-slate-300">
              <span class="text-green-400">&#10003;</span> Portfolio e cotacoes
            </li>
            <li class="flex items-center gap-2 text-sm text-slate-300">
              <span class="text-green-400">&#10003;</span> Relatorios e historico
            </li>
            <li class="flex items-center gap-2 text-sm text-slate-300">
              <span class="text-green-400">&#10003;</span> Notificacoes personalizadas
            </li>
          </ul>
          <div class="border-t border-white/10 pt-4">
            <div class="flex items-baseline gap-1 mb-3">
              <span class="text-3xl font-bold text-white">R$ {{ status.precoAutoGestao ? Number(status.precoAutoGestao).toFixed(2).replace('.', ',') : '9,99' }}</span>
              <span class="text-slate-400 text-sm">/mes</span>
            </div>
            <button
              @click="iniciarCheckoutAutoGestao"
              :disabled="checkoutLoading"
              class="w-full bg-indigo-600 text-white py-3 px-4 rounded-xl font-medium hover:bg-indigo-700 disabled:opacity-50 transition-colors text-sm"
            >
              {{ checkoutLoading ? 'Processando...' : 'Assinar Auto-Gestao' }}
            </button>
          </div>
        </div>

        <!-- Card B: Relatorio Completo -->
        <div class="bg-white/10 backdrop-blur-sm rounded-2xl border border-white/10 p-6 hover:border-emerald-400/30 transition-all">
          <div class="flex items-center gap-3 mb-4">
            <div class="w-10 h-10 bg-emerald-500/20 rounded-xl flex items-center justify-center">
              <span class="text-xl">&#128196;</span>
            </div>
            <h2 class="text-lg font-semibold text-white">Relatorio Completo</h2>
          </div>
          <p class="text-slate-300 text-sm mb-4">
            Baixe um PDF consolidado com todo seu historico: operacoes, recomendacoes, saldo e performance.
          </p>
          <ul class="space-y-2 mb-6">
            <li class="flex items-center gap-2 text-sm text-slate-300">
              <span class="text-emerald-400">&#10003;</span> Historico de operacoes
            </li>
            <li class="flex items-center gap-2 text-sm text-slate-300">
              <span class="text-emerald-400">&#10003;</span> Resumo de ganhos e perdas
            </li>
            <li class="flex items-center gap-2 text-sm text-slate-300">
              <span class="text-emerald-400">&#10003;</span> Resultado por moeda
            </li>
            <li class="flex items-center gap-2 text-sm text-slate-300">
              <span class="text-emerald-400">&#10003;</span> Dados completos em PDF
            </li>
          </ul>
          <div class="border-t border-white/10 pt-4">
            <!-- Nao baixou ainda: gratis -->
            <template v-if="!status?.relatorioGratisBaixado">
              <div class="flex items-baseline gap-1 mb-3">
                <span class="text-3xl font-bold text-emerald-400">Gratis</span>
                <span class="text-slate-400 text-sm">(1x)</span>
              </div>
              <button
                @click="baixarRelatorioGratis"
                :disabled="downloadLoading"
                class="w-full bg-emerald-600 text-white py-3 px-4 rounded-xl font-medium hover:bg-emerald-700 disabled:opacity-50 transition-colors text-sm"
              >
                {{ downloadLoading ? 'Gerando PDF...' : 'Baixar Gratuitamente' }}
              </button>
            </template>
            <!-- Ja baixou: pode baixar novamente (pago ou gratis se pagou agora) -->
            <template v-else>
              <!-- Se acabou de pagar o relatório, oferece download direto -->
              <template v-if="pagamentoRelatorioOk">
                <div class="flex items-baseline gap-1 mb-3">
                  <span class="text-2xl font-bold text-emerald-400">Pago</span>
                  <span class="text-slate-400 text-sm">&#10003;</span>
                </div>
                <button
                  @click="baixarRelatorioPagoManual"
                  :disabled="downloadLoading"
                  class="w-full bg-emerald-600 text-white py-3 px-4 rounded-xl font-medium hover:bg-emerald-700 disabled:opacity-50 transition-colors text-sm"
                >
                  {{ downloadLoading ? 'Gerando PDF...' : 'Baixar Relatorio (PDF)' }}
                </button>
              </template>
              <!-- Senao, mostra opção de pagar -->
              <template v-else>
                <p class="text-slate-400 text-xs mb-2">
                  Relatorio ja baixado. Baixe novamente por:
                </p>
                <div class="flex items-baseline gap-1 mb-3">
                  <span class="text-2xl font-bold text-white">R$ {{ status.precoRelatorio ? Number(status.precoRelatorio).toFixed(2).replace('.', ',') : '19,90' }}</span>
                  <span class="text-slate-400 text-sm">(avulso)</span>
                </div>
                <button
                  @click="iniciarCheckoutRelatorio"
                  :disabled="checkoutRelatorioLoading"
                  class="w-full bg-slate-600 text-white py-3 px-4 rounded-xl font-medium hover:bg-slate-700 disabled:opacity-50 transition-colors text-sm"
                >
                  {{ checkoutRelatorioLoading ? 'Processando...' : 'Pagar e Baixar Novamente' }}
                </button>
              </template>
            </template>
          </div>
        </div>
      </div>

      <!-- Pagamento OK: relatorio -->
      <div v-if="pagamentoRelatorioOk" class="bg-green-500/10 border border-green-500/20 rounded-xl p-4 mb-6 text-center">
        <p class="text-green-300 text-sm font-medium">
          &#10003; Pagamento confirmado! {{ downloadingPago ? 'Gerando seu PDF...' : 'Download concluido!' }}
        </p>
      </div>

      <!-- Pagamento OK: auto-gestao -->
      <div v-if="pagamentoAutoGestaoOk" class="bg-green-500/10 border border-green-500/20 rounded-xl p-4 mb-6 text-center">
        <p class="text-green-300 text-sm font-medium">
          &#10003; Pagamento confirmado! Ativando sua auto-gestao...
        </p>
      </div>

      <!-- Error -->
      <div v-if="erro" class="bg-red-500/10 border border-red-500/20 rounded-xl p-4 mb-6 text-center">
        <p class="text-red-300 text-sm">{{ erro }}</p>
      </div>

      <!-- Footer -->
      <div class="text-center">
        <button
          @click="logout"
          class="text-slate-400 hover:text-white text-sm transition-colors"
        >
          Sair da conta
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import autoGestaoApi from '../../api/autoGestaoApi'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const loading = ref(true)
const status = ref(null)
const erro = ref('')
const checkoutLoading = ref(false)
const checkoutRelatorioLoading = ref(false)
const downloadLoading = ref(false)
const pagamentoRelatorioOk = ref(false)
const pagamentoAutoGestaoOk = ref(false)
const downloadingPago = ref(false)

async function loadStatus() {
  loading.value = true
  try {
    const res = await autoGestaoApi.getStatus()
    status.value = res.data
    // Se tem auto-gestao ativa, redirecionar para dashboard
    if (status.value.autoGestaoAtiva) {
      authStore.user.autoGestaoAtiva = true
      localStorage.setItem('user', JSON.stringify(authStore.user))
      router.push('/cliente')
      return
    }
  } catch (e) {
    erro.value = 'Erro ao carregar dados. Tente novamente.'
  } finally {
    loading.value = false
  }
}

async function confirmarPagamentoSeNecessario() {
  const sessionId = route.query.session_id
  const piId = route.query.payment_intent
  const tipo = route.query.tipo // 'relatorio' ou undefined (auto-gestao)

  // Confirma pagamento no backend (fallback para quando webhook não chega)
  if (sessionId || piId) {
    try {
      await autoGestaoApi.confirmarPagamento(piId || sessionId)
    } catch (e) {
      // ignora — webhook pode já ter processado
    }
  }

  if (tipo === 'relatorio') {
    // Pagamento de relatório: fazer download automático do PDF
    pagamentoRelatorioOk.value = true
    downloadingPago.value = true
    try {
      const res = await autoGestaoApi.baixarRelatorioPago()
      const blob = new Blob([res.data], { type: 'application/pdf' })
      const url = window.URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = 'relatorio-completo-tradelink.pdf'
      a.click()
      window.URL.revokeObjectURL(url)
    } catch (e) {
      erro.value = 'Pagamento confirmado, mas erro ao gerar PDF. Use o botao abaixo para baixar.'
    } finally {
      downloadingPago.value = false
    }
    await loadStatus()
  } else {
    // Pagamento de auto-gestão: recarrega status (redireciona automaticamente se ativa)
    pagamentoAutoGestaoOk.value = true
    setTimeout(async () => {
      await loadStatus()
    }, 1500)
  }
}

async function baixarRelatorioGratis() {
  downloadLoading.value = true
  erro.value = ''
  try {
    const res = await autoGestaoApi.baixarRelatorioGratuito()
    const blob = new Blob([res.data], { type: 'application/pdf' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = 'relatorio-completo-tradelink.pdf'
    a.click()
    window.URL.revokeObjectURL(url)
    // Atualizar status local
    if (status.value) status.value.relatorioGratisBaixado = true
  } catch (e) {
    erro.value = e.response?.data?.erro || 'Erro ao gerar relatorio.'
  } finally {
    downloadLoading.value = false
  }
}

async function baixarRelatorioPagoManual() {
  downloadLoading.value = true
  erro.value = ''
  try {
    const res = await autoGestaoApi.baixarRelatorioPago()
    const blob = new Blob([res.data], { type: 'application/pdf' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = 'relatorio-completo-tradelink.pdf'
    a.click()
    window.URL.revokeObjectURL(url)
  } catch (e) {
    erro.value = e.response?.data?.erro || 'Erro ao gerar relatorio.'
  } finally {
    downloadLoading.value = false
  }
}

async function iniciarCheckoutAutoGestao() {
  checkoutLoading.value = true
  erro.value = ''
  try {
    const res = await autoGestaoApi.checkoutAutoGestao()
    // Redirecionar para a página de checkout do Stripe (URL real retornada pelo backend)
    window.location.href = res.data.checkoutUrl
  } catch (e) {
    erro.value = e.response?.data?.erro || 'Erro ao iniciar pagamento.'
    checkoutLoading.value = false
  }
}

async function iniciarCheckoutRelatorio() {
  checkoutRelatorioLoading.value = true
  erro.value = ''
  try {
    const res = await autoGestaoApi.checkoutRelatorio()
    // Redirecionar para a página de checkout do Stripe (URL real retornada pelo backend)
    window.location.href = res.data.checkoutUrl
  } catch (e) {
    erro.value = e.response?.data?.erro || 'Erro ao iniciar pagamento.'
    checkoutRelatorioLoading.value = false
  }
}

function logout() {
  authStore.logout()
  router.push('/login')
}

onMounted(async () => {
  // Se veio do Stripe com pagamento OK (via session_id ou payment_intent)
  if (route.query.pagamento === 'ok' || route.query.session_id || route.query.payment_intent) {
    await confirmarPagamentoSeNecessario()
  } else {
    await loadStatus()
  }
})
</script>

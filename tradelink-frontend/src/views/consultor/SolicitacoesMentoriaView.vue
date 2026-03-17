<template>
  <div>
    <h2 class="page-title">Solicitacoes de Mentoria</h2>
    <p class="text-sm text-gray-500 dark:text-gray-400 -mt-4 mb-6">Gerencie solicitacoes recebidas pelo marketplace</p>

    <div v-if="loading" class="flex justify-center py-16">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-500"></div>
    </div>

    <div v-else-if="!solicitacoes.length" class="card p-12 text-center">
      <svg class="w-12 h-12 text-gray-300 dark:text-gray-600 mx-auto mb-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"/>
      </svg>
      <p class="font-medium text-gray-700 dark:text-gray-300">Nenhuma solicitacao recebida</p>
      <p class="text-sm text-gray-500 dark:text-gray-400 mt-1">Ative seu perfil no marketplace para receber solicitacoes</p>
    </div>

    <div v-else class="space-y-4">
      <div
        v-for="s in solicitacoes"
        :key="s.id"
        class="card p-5"
      >
        <div class="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-3">
          <div class="flex-1">
            <div class="flex items-center gap-2 mb-1">
              <h3 class="font-semibold text-gray-900 dark:text-gray-100 text-sm">{{ s.clienteNome || s.clienteEmail }}</h3>
              <span :class="statusClass(s.status)" class="px-2.5 py-0.5 rounded-full text-xs font-medium">
                {{ statusLabel(s.status) }}
              </span>
            </div>
            <p class="text-gray-500 dark:text-gray-400 text-xs">{{ s.clienteEmail }}</p>
            <div v-if="s.mensagemCliente" class="mt-2 p-3 rounded-lg bg-gray-50 dark:bg-slate-800/50 border border-gray-200 dark:border-gray-700">
              <p class="text-gray-600 dark:text-gray-300 text-sm italic">"{{ s.mensagemCliente }}"</p>
            </div>
            <p class="text-gray-400 text-xs mt-2">{{ formatDate(s.createdAt) }}</p>
          </div>

          <!-- Actions for PENDENTE -->
          <div v-if="s.status === 'PENDENTE'" class="flex flex-col gap-2 sm:w-52">
            <div>
              <label class="text-xs text-gray-500 dark:text-gray-400">Preco final (R$/mes):</label>
              <input
                v-model.number="s._precoFinal"
                type="number"
                step="0.01"
                :placeholder="String(s.precoProposto || '0')"
                class="input-base text-sm mt-1"
              />
            </div>
            <textarea
              v-model="s._mensagem"
              placeholder="Mensagem (opcional)"
              rows="2"
              class="input-base text-sm resize-none"
            ></textarea>
            <div class="flex gap-2">
              <button
                @click="responder(s, true)"
                :disabled="respondendo"
                class="flex-1 px-3 py-2 rounded-lg text-xs font-medium bg-green-600 text-white hover:bg-green-700 disabled:opacity-50 transition-colors"
              >
                Aceitar
              </button>
              <button
                @click="responder(s, false)"
                :disabled="respondendo"
                class="flex-1 px-3 py-2 rounded-lg text-xs font-medium bg-red-600 text-white hover:bg-red-700 disabled:opacity-50 transition-colors"
              >
                Recusar
              </button>
            </div>
          </div>

          <!-- Info for other statuses -->
          <div v-else class="text-right shrink-0">
            <p v-if="s.precoFinal" class="font-semibold text-gray-900 dark:text-gray-100 text-sm">R$ {{ formatPreco(s.precoFinal) }}/mes</p>
            <p v-if="s.mensagemConsultor" class="text-gray-500 dark:text-gray-400 text-xs mt-1 italic">"{{ s.mensagemConsultor }}"</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { listarSolicitacoesConsultor, responderSolicitacao } from '../../api/marketplaceApi'
import { useToast } from '../../composables/useToast'

const toast = useToast()
const loading = ref(true)
const respondendo = ref(false)
const solicitacoes = ref([])

onMounted(async () => {
  await carregar()
})

async function carregar() {
  loading.value = true
  try {
    const res = await listarSolicitacoesConsultor()
    solicitacoes.value = res.data.map(s => ({ ...s, _precoFinal: s.precoProposto, _mensagem: '' }))
  } catch {
    toast.error('Erro ao carregar solicitacoes')
  } finally {
    loading.value = false
  }
}

async function responder(s, aceitar) {
  respondendo.value = true
  try {
    await responderSolicitacao(s.id, {
      aceitar,
      precoFinal: s._precoFinal || s.precoProposto,
      mensagemConsultor: s._mensagem || null
    })
    toast.success(aceitar ? 'Solicitacao aceita!' : 'Solicitacao recusada')
    await carregar()
  } catch (e) {
    toast.error(e.response?.data?.message || 'Erro ao responder')
  } finally {
    respondendo.value = false
  }
}

function formatPreco(v) {
  if (!v) return '0,00'
  return Number(v).toFixed(2).replace('.', ',')
}

function formatDate(d) {
  if (!d) return ''
  return new Date(d).toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' })
}

function statusLabel(s) {
  const m = { PENDENTE: 'Pendente', ACEITA: 'Aceita', PAGA: 'Ativa', RECUSADA: 'Recusada', CANCELADA: 'Cancelada' }
  return m[s] || s
}

function statusClass(s) {
  const m = {
    PENDENTE: 'bg-amber-100 text-amber-800 dark:bg-amber-500/20 dark:text-amber-400',
    ACEITA: 'bg-blue-100 text-blue-800 dark:bg-blue-500/20 dark:text-blue-400',
    PAGA: 'bg-green-100 text-green-800 dark:bg-green-500/20 dark:text-green-400',
    RECUSADA: 'bg-red-100 text-red-800 dark:bg-red-500/20 dark:text-red-400',
    CANCELADA: 'bg-gray-100 text-gray-600 dark:bg-gray-500/20 dark:text-gray-400'
  }
  return m[s] || 'bg-gray-100 text-gray-600'
}
</script>

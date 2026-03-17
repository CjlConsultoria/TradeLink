<template>
  <div class="space-y-6">
    <div>
      <h1 class="text-2xl font-bold text-white">Solicitacoes de Mentoria</h1>
      <p class="text-slate-400 text-sm mt-1">Gerencie solicitacoes recebidas pelo marketplace</p>
    </div>

    <div v-if="loading" class="flex justify-center py-12">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-400"></div>
    </div>

    <div v-else-if="!solicitacoes.length" class="text-center py-12">
      <span class="text-4xl mb-4 block">&#128233;</span>
      <p class="text-slate-300 font-medium">Nenhuma solicitacao recebida</p>
      <p class="text-slate-500 text-sm mt-1">Ative seu perfil no marketplace para receber solicitacoes</p>
    </div>

    <div v-else class="space-y-4">
      <div
        v-for="s in solicitacoes"
        :key="s.id"
        class="bg-slate-800/50 rounded-xl border border-slate-700/50 p-5"
      >
        <div class="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-3">
          <div class="flex-1">
            <div class="flex items-center gap-2 mb-2">
              <h3 class="text-white font-semibold text-sm">{{ s.clienteNome || s.clienteEmail }}</h3>
              <span :class="statusClass(s.status)" class="px-2 py-0.5 rounded-full text-xs font-medium">
                {{ statusLabel(s.status) }}
              </span>
            </div>
            <p class="text-slate-400 text-xs">{{ s.clienteEmail }}</p>
            <p v-if="s.mensagemCliente" class="text-slate-300 text-sm mt-2 italic bg-slate-700/30 rounded-lg p-3">
              "{{ s.mensagemCliente }}"
            </p>
            <p class="text-slate-500 text-xs mt-2">{{ formatDate(s.createdAt) }}</p>
          </div>

          <!-- Actions for PENDENTE -->
          <div v-if="s.status === 'PENDENTE'" class="flex flex-col gap-2 sm:w-48">
            <div>
              <label class="text-slate-400 text-xs">Preco (R$/mes):</label>
              <input
                v-model.number="s._precoFinal"
                type="number"
                step="0.01"
                :placeholder="String(s.precoProposto || '0')"
                class="w-full bg-slate-700/50 text-white rounded-lg p-2 text-sm border border-slate-600/50 focus:border-indigo-500 focus:outline-none mt-1"
              />
            </div>
            <textarea
              v-model="s._mensagem"
              placeholder="Mensagem (opcional)"
              rows="2"
              class="w-full bg-slate-700/50 text-white rounded-lg p-2 text-sm border border-slate-600/50 focus:border-indigo-500 focus:outline-none"
            ></textarea>
            <div class="flex gap-2">
              <button
                @click="responder(s, true)"
                :disabled="respondendo"
                class="flex-1 bg-green-600 text-white py-2 rounded-lg text-xs font-medium hover:bg-green-700 disabled:opacity-50 transition-colors"
              >
                Aceitar
              </button>
              <button
                @click="responder(s, false)"
                :disabled="respondendo"
                class="flex-1 bg-red-600 text-white py-2 rounded-lg text-xs font-medium hover:bg-red-700 disabled:opacity-50 transition-colors"
              >
                Recusar
              </button>
            </div>
          </div>

          <!-- Info for other statuses -->
          <div v-else class="text-right">
            <p v-if="s.precoFinal" class="text-white font-semibold text-sm">R$ {{ formatPreco(s.precoFinal) }}/mes</p>
            <p v-if="s.mensagemConsultor" class="text-slate-400 text-xs mt-1">"{{ s.mensagemConsultor }}"</p>
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
    PENDENTE: 'bg-yellow-500/20 text-yellow-400',
    ACEITA: 'bg-blue-500/20 text-blue-400',
    PAGA: 'bg-green-500/20 text-green-400',
    RECUSADA: 'bg-red-500/20 text-red-400',
    CANCELADA: 'bg-slate-500/20 text-slate-400'
  }
  return m[s] || 'bg-slate-500/20 text-slate-400'
}
</script>

<template>
  <div>
    <h2 class="page-title">Chat — Suporte</h2>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- Lista de conversas -->
      <div class="card p-4 lg:col-span-1" style="max-height:75vh;overflow-y:auto;">
        <div class="flex items-center justify-between mb-3">
          <h3 class="section-title !mb-0">Conversas</h3>
          <select v-model="filtro" class="text-xs border rounded px-2 py-1">
            <option value="todas">Todas</option>
            <option value="ABERTA">Abertas</option>
            <option value="FECHADA">Fechadas</option>
          </select>
        </div>
        <div v-if="loadingConversas" class="text-center py-4 text-gray-500">Carregando...</div>
        <div v-else-if="conversasFiltradas.length === 0" class="text-center py-4 text-gray-400 text-sm">Nenhuma conversa.</div>
        <div v-else class="space-y-2">
          <button v-for="c in conversasFiltradas" :key="c.id" type="button"
            @click="selecionarConversa(c)"
            class="w-full text-left p-3 rounded-lg border transition-colors"
            :class="conversaSelecionada?.id === c.id ? 'border-indigo-500 bg-indigo-50' : 'border-gray-200 hover:bg-gray-50'">
            <div class="flex items-center justify-between mb-1">
              <span class="font-medium text-sm text-gray-800">{{ c.nomeUsuario }}</span>
              <span class="text-xs px-1.5 py-0.5 rounded-full"
                :class="c.status === 'ABERTA' ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-500'">
                {{ c.status }}
              </span>
            </div>
            <p class="text-xs text-gray-500 truncate">{{ c.ultimaMensagem || c.assunto || 'Sem mensagens' }}</p>
            <div class="flex items-center justify-between mt-1">
              <span class="text-xs text-gray-400">{{ c.roleUsuario }}</span>
              <span v-if="c.naoLidas > 0" class="text-xs bg-indigo-600 text-white px-1.5 py-0.5 rounded-full">{{ c.naoLidas }}</span>
            </div>
          </button>
        </div>
      </div>

      <!-- Painel de mensagens -->
      <div class="card p-4 lg:col-span-2 flex flex-col" style="max-height:75vh;">
        <template v-if="conversaSelecionada">
          <div class="flex items-center justify-between pb-3 border-b mb-3">
            <div>
              <h3 class="font-semibold text-gray-800">{{ conversaSelecionada.nomeUsuario }}</h3>
              <p class="text-xs text-gray-400">{{ conversaSelecionada.assunto || 'Suporte' }} — {{ conversaSelecionada.roleUsuario }}</p>
            </div>
            <button v-if="conversaSelecionada.status === 'ABERTA'" @click="fecharConversa"
              class="text-xs px-3 py-1.5 bg-red-100 text-red-700 rounded-lg hover:bg-red-200">Fechar</button>
          </div>

          <div ref="chatContainer" class="flex-1 overflow-y-auto space-y-3 mb-3 px-1">
            <div v-for="msg in mensagens" :key="msg.id"
              class="flex" :class="msg.isAdmin ? 'justify-end' : 'justify-start'">
              <div class="max-w-[75%] px-4 py-2.5 rounded-xl text-sm"
                :class="msg.isAdmin ? 'bg-indigo-600 text-white rounded-br-sm' : 'bg-gray-100 text-gray-800 rounded-bl-sm'">
                <p class="whitespace-pre-wrap">{{ msg.conteudo }}</p>
                <p class="text-[10px] mt-1 opacity-60">{{ formatTime(msg.createdAt) }}</p>
              </div>
            </div>
          </div>

          <form @submit.prevent="enviar" class="flex gap-2">
            <input v-model="novaMsg" type="text" class="input flex-1" placeholder="Digite sua resposta..." />
            <button type="submit" :disabled="!novaMsg.trim() || enviando"
              class="btn btn-primary px-5">Enviar</button>
          </form>
        </template>
        <div v-else class="flex-1 flex items-center justify-center text-gray-400">
          Selecione uma conversa para responder.
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import chatApi from '../../api/chatApi'
import { useToast } from '../../composables/useToast'

const toast = useToast()
const loadingConversas = ref(true)
const conversas = ref([])
const conversaSelecionada = ref(null)
const mensagens = ref([])
const novaMsg = ref('')
const enviando = ref(false)
const filtro = ref('todas')
const chatContainer = ref(null)
let pollInterval = null

const conversasFiltradas = computed(() => {
  if (filtro.value === 'todas') return conversas.value
  return conversas.value.filter(c => c.status === filtro.value)
})

async function carregarConversas() {
  try {
    const res = await chatApi.listarTodasConversas()
    conversas.value = res.data || []
  } catch {}
  finally { loadingConversas.value = false }
}

async function selecionarConversa(c) {
  conversaSelecionada.value = c
  try {
    const res = await chatApi.listarMensagensAdmin(c.id)
    mensagens.value = res.data || []
    await nextTick()
    scrollToBottom()
  } catch { toast.error('Erro ao carregar mensagens.') }
}

async function enviar() {
  if (!novaMsg.value.trim() || !conversaSelecionada.value) return
  enviando.value = true
  try {
    const res = await chatApi.enviarMensagemAdmin(conversaSelecionada.value.id, novaMsg.value.trim())
    mensagens.value.push(res.data)
    novaMsg.value = ''
    await nextTick()
    scrollToBottom()
  } catch { toast.error('Erro ao enviar.') }
  finally { enviando.value = false }
}

async function fecharConversa() {
  if (!conversaSelecionada.value) return
  try {
    await chatApi.fecharConversa(conversaSelecionada.value.id)
    conversaSelecionada.value.status = 'FECHADA'
    toast.success('Conversa fechada.')
    await carregarConversas()
  } catch { toast.error('Erro ao fechar.') }
}

function scrollToBottom() {
  if (chatContainer.value) chatContainer.value.scrollTop = chatContainer.value.scrollHeight
}

function formatTime(dt) {
  if (!dt) return ''
  const d = new Date(dt)
  return d.toLocaleString('pt-BR', { day: '2-digit', month: '2-digit', hour: '2-digit', minute: '2-digit' })
}

async function poll() {
  await carregarConversas()
  if (conversaSelecionada.value) {
    try {
      const res = await chatApi.listarMensagensAdmin(conversaSelecionada.value.id)
      if (res.data.length !== mensagens.value.length) {
        mensagens.value = res.data
        await nextTick()
        scrollToBottom()
      }
    } catch {}
  }
}

onMounted(() => {
  carregarConversas()
  pollInterval = setInterval(poll, 5000)
})

onUnmounted(() => {
  if (pollInterval) clearInterval(pollInterval)
})
</script>

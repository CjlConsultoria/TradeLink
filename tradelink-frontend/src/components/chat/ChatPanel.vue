<template>
  <div class="fixed bottom-6 right-6 z-50 w-96 max-w-[calc(100vw-3rem)] bg-white rounded-2xl shadow-2xl border border-gray-200 flex flex-col"
    style="height:500px;max-height:calc(100vh - 6rem);">
    <!-- Header -->
    <div class="flex items-center justify-between px-4 py-3 border-b bg-indigo-600 rounded-t-2xl">
      <div class="flex items-center gap-2">
        <div class="w-8 h-8 rounded-full bg-white/20 flex items-center justify-center text-white text-sm">&#9651;</div>
        <div>
          <h3 class="text-white font-semibold text-sm">Suporte TradeLink</h3>
          <p class="text-indigo-200 text-[11px]">Estamos aqui para ajudar</p>
        </div>
      </div>
      <button type="button" @click="$emit('close')" class="text-white/80 hover:text-white text-lg">&times;</button>
    </div>

    <!-- Mensagens -->
    <div ref="chatBody" class="flex-1 overflow-y-auto p-4 space-y-3">
      <div v-if="loading" class="text-center py-8 text-gray-400 text-sm">Carregando...</div>

      <div v-else-if="mensagens.length === 0" class="text-center py-8">
        <p class="text-gray-400 text-sm mb-2">Nenhuma mensagem ainda.</p>
        <p class="text-gray-400 text-xs">Envie uma mensagem para iniciar o atendimento.</p>
      </div>

      <div v-else v-for="msg in mensagens" :key="msg.id"
        class="flex" :class="msg.isAdmin ? 'justify-start' : 'justify-end'">
        <div class="max-w-[80%] px-3 py-2 rounded-xl text-sm"
          :class="msg.isAdmin ? 'bg-gray-100 text-gray-800 rounded-bl-sm' : 'bg-indigo-600 text-white rounded-br-sm'">
          <p v-if="msg.isAdmin" class="text-[10px] font-semibold text-indigo-600 mb-0.5">Suporte</p>
          <p class="whitespace-pre-wrap">{{ msg.conteudo }}</p>
          <p class="text-[10px] mt-1 opacity-50">{{ formatTime(msg.createdAt) }}</p>
        </div>
      </div>
    </div>

    <!-- Input -->
    <form @submit.prevent="enviar" class="p-3 border-t flex gap-2">
      <input v-model="novaMsg" type="text" class="flex-1 px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:border-indigo-500"
        placeholder="Digite sua mensagem..." />
      <button type="submit" :disabled="!novaMsg.trim() || enviando"
        class="px-4 py-2 bg-indigo-600 text-white rounded-lg text-sm font-medium hover:bg-indigo-700 disabled:opacity-40 transition-colors">
        Enviar
      </button>
    </form>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import chatApi from '../../api/chatApi'

const emit = defineEmits(['close', 'update-badge'])

const loading = ref(true)
const mensagens = ref([])
const novaMsg = ref('')
const enviando = ref(false)
const conversaId = ref(null)
const chatBody = ref(null)
let pollInterval = null

async function iniciarOuCarregar() {
  loading.value = true
  try {
    // Tenta listar conversas existentes
    const res = await chatApi.listarConversas()
    const conversas = res.data || []
    const aberta = conversas.find(c => c.status === 'ABERTA')
    if (aberta) {
      conversaId.value = aberta.id
      await carregarMensagens()
    } else {
      // Inicia nova conversa
      const nova = await chatApi.iniciarConversa('Suporte')
      conversaId.value = nova.data.id
      mensagens.value = []
    }
  } catch {
    // Se falhar, tenta iniciar nova
    try {
      const nova = await chatApi.iniciarConversa('Suporte')
      conversaId.value = nova.data.id
    } catch {}
  }
  loading.value = false
  await nextTick()
  scrollToBottom()
}

async function carregarMensagens() {
  if (!conversaId.value) return
  try {
    const res = await chatApi.listarMensagens(conversaId.value)
    const newMsgs = res.data || []
    if (newMsgs.length !== mensagens.value.length) {
      mensagens.value = newMsgs
      await nextTick()
      scrollToBottom()
      emit('update-badge')
    }
  } catch {}
}

async function enviar() {
  if (!novaMsg.value.trim()) return
  if (!conversaId.value) {
    try {
      const nova = await chatApi.iniciarConversa('Suporte')
      conversaId.value = nova.data.id
    } catch { return }
  }
  enviando.value = true
  try {
    const res = await chatApi.enviarMensagem(conversaId.value, novaMsg.value.trim())
    mensagens.value.push(res.data)
    novaMsg.value = ''
    await nextTick()
    scrollToBottom()
  } catch {}
  enviando.value = false
}

function scrollToBottom() {
  if (chatBody.value) chatBody.value.scrollTop = chatBody.value.scrollHeight
}

function formatTime(dt) {
  if (!dt) return ''
  const d = new Date(dt)
  return d.toLocaleString('pt-BR', { hour: '2-digit', minute: '2-digit' })
}

onMounted(() => {
  iniciarOuCarregar()
  pollInterval = setInterval(carregarMensagens, 5000)
})

onUnmounted(() => {
  if (pollInterval) clearInterval(pollInterval)
})
</script>

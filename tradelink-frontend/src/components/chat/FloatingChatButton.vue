<template>
  <div>
    <!-- Botao flutuante -->
    <button v-if="!chatOpen" type="button" @click="openChat"
      class="fixed bottom-6 right-6 z-50 w-14 h-14 rounded-full bg-indigo-600 text-white shadow-lg hover:bg-indigo-700 transition-all flex items-center justify-center"
      title="Chat com Suporte">
      <svg xmlns="http://www.w3.org/2000/svg" class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
        <path stroke-linecap="round" stroke-linejoin="round" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
      </svg>
      <span v-if="naoLidas > 0"
        class="absolute -top-1 -right-1 w-5 h-5 bg-red-500 text-white text-[10px] font-bold rounded-full flex items-center justify-center">
        {{ naoLidas > 9 ? '9+' : naoLidas }}
      </span>
    </button>

    <!-- Chat panel -->
    <ChatPanel v-if="chatOpen" @close="closeChat" @update-badge="fetchNaoLidas" />
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import chatApi from '../../api/chatApi'
import ChatPanel from './ChatPanel.vue'
import { useChatPanel } from '../../composables/useChatPanel'

const { chatOpen, openChat, closeChat } = useChatPanel()
const naoLidas = ref(0)
let pollInterval = null

async function fetchNaoLidas() {
  try {
    const res = await chatApi.contarNaoLidas()
    naoLidas.value = res.data?.total || 0
  } catch {
    // silently ignore
  }
}

onMounted(() => {
  fetchNaoLidas()
  pollInterval = setInterval(fetchNaoLidas, 15000)
})

onUnmounted(() => {
  if (pollInterval) clearInterval(pollInterval)
})
</script>

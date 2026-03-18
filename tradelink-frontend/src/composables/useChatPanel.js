import { ref } from 'vue'

const chatOpen = ref(false)

export function useChatPanel() {
  function openChat() {
    chatOpen.value = true
  }
  function closeChat() {
    chatOpen.value = false
  }
  function toggleChat() {
    chatOpen.value = !chatOpen.value
  }
  return { chatOpen, openChat, closeChat, toggleChat }
}

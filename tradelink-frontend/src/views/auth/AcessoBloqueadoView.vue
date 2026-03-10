<template>
  <div class="min-h-screen flex flex-col items-center justify-center p-6 bg-gray-50">
    <div class="max-w-md w-full bg-white rounded-2xl shadow-lg border border-gray-200 p-8 text-center">
      <div class="w-16 h-16 mx-auto mb-6 rounded-full bg-red-100 flex items-center justify-center" aria-hidden="true">
        <svg class="w-8 h-8 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
        </svg>
      </div>
      <h1 class="text-xl font-semibold text-gray-900 mb-2">Acesso bloqueado</h1>
      <p class="text-gray-600 mb-6">{{ motivo || 'Seu acesso à plataforma está bloqueado.' }}</p>
      <p class="text-sm text-gray-400 mb-8">Precisa de ajuda? Use o chat de suporte no canto inferior direito.</p>
      <button
        type="button"
        @click="sair"
        class="w-full px-4 py-3 rounded-xl border-2 border-gray-300 text-gray-700 font-medium hover:bg-gray-50"
      >
        Sair
      </button>
    </div>

    <!-- Chat de suporte disponível mesmo com acesso bloqueado -->
    <FloatingChatButton v-if="authStore.isAuthenticated" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import FloatingChatButton from '../../components/chat/FloatingChatButton.vue'

const router = useRouter()
const authStore = useAuthStore()
const motivo = ref('')

onMounted(() => {
  motivo.value = sessionStorage.getItem('motivoBloqueio') || 'Entre em contato com o responsável pelo sistema ou com sua empresa.'
})

function sair() {
  sessionStorage.removeItem('motivoBloqueio')
  authStore.logout()
  router.push('/login')
}
</script>

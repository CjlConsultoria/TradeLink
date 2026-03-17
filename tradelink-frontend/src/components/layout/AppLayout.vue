<template>
  <div class="app-layout">
    <AppSidebar :open="sidebarOpen" :collapsed="sidebarCollapsed" @close="sidebarOpen = false" />
    <div class="app-layout__main" :class="{ 'app-layout__main--collapsed': sidebarCollapsed }">
      <AppHeader @toggle-sidebar="sidebarOpen = !sidebarOpen" @toggle-collapse="toggleCollapse" :collapsed="sidebarCollapsed" />
      <main class="app-layout__content">
        <router-view />
      </main>
    </div>
    <FloatingChatButton v-if="showChatButton" />
    <FloatingActionButton v-if="showFab" @click="showRecomModal = true" />
    <RecomendacaoModal
      ref="recomModalRef"
      :show="showRecomModal"
      :carteiras="fabCarteiras"
      @close="showRecomModal = false"
      @saved="onRecomSaved"
      @criar-carteira="onCriarCarteira"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useAuthStore } from '../../stores/auth'
import { useKeyboardShortcuts } from '../../composables/useKeyboardShortcuts'
import { useToast } from '../../composables/useToast'
import AppHeader from './AppHeader.vue'
import AppSidebar from './AppSidebar.vue'
import FloatingActionButton from '../common/FloatingActionButton.vue'
import FloatingChatButton from '../chat/FloatingChatButton.vue'
import RecomendacaoModal from '../recomendacao/RecomendacaoModal.vue'
import carteiraApi from '../../api/carteiraApi'
import recomendacaoApi from '../../api/recomendacaoApi'

const toast = useToast()

const sidebarOpen = ref(false)
const sidebarCollapsed = ref(localStorage.getItem('tl-sidebar-collapsed') === 'true')
const showRecomModal = ref(false)
const fabCarteiras = ref([])
const recomModalRef = ref(null)

const showFab = computed(() => useAuthStore().user?.role === 'Admin')
const showChatButton = computed(() => {
  const role = useAuthStore().user?.role
  return role === 'Admin' || role === 'Cliente'
})

function toggleCollapse() {
  sidebarCollapsed.value = !sidebarCollapsed.value
  localStorage.setItem('tl-sidebar-collapsed', sidebarCollapsed.value)
}

useKeyboardShortcuts({ onNewRecommendation: () => { showRecomModal.value = true } })

onMounted(async () => {
  if (useAuthStore().user?.role === 'Admin') {
    try {
      const res = await carteiraApi.listar()
      fabCarteiras.value = res.data || []
    } catch (_) {}
  }
})

async function onRecomSaved(payload) {
  try {
    await recomendacaoApi.criar(payload.carteiraId, {
      tipo: payload.tipo,
      moeda: payload.moeda,
      parMoeda: payload.parMoeda,
      precoEntrada: payload.precoEntrada,
      precoAlvo: payload.precoAlvo,
      stopLoss: payload.stopLoss,
      quantidade: payload.quantidade,
      observacao: payload.observacao
    })
    showRecomModal.value = false
    toast.success('Recomendacao criada com sucesso.')
  } catch (e) {
    console.error(e)
    toast.error(e.response?.data?.mensagem || 'Erro ao criar recomendacao.')
  }
}

async function onCriarCarteira(payload) {
  try {
    const res = await carteiraApi.criar({ nome: payload.nome, descricao: payload.descricao })
    const nova = res.data
    fabCarteiras.value = [...fabCarteiras.value, nova]
    recomModalRef.value?.usarCarteiraCriada(nova.id)
    toast.success('Carteira criada e selecionada.')
  } catch (e) {
    console.error(e)
    toast.error(e.response?.data?.mensagem || 'Erro ao criar carteira.')
  }
}
</script>

<style scoped>
.app-layout {
  min-height: 100vh;
  display: flex;
  background: rgb(var(--tl-surface-alt));
}

.app-layout__main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  margin-left: 0;
  transition: margin-left 0.25s ease;
}
@media (min-width: 1024px) {
  .app-layout__main {
    margin-left: 16rem;
  }
  .app-layout__main--collapsed {
    margin-left: 4.5rem;
  }
}

.app-layout__content {
  flex: 1;
  min-width: 0;
  padding: 1rem;
  overflow-x: auto;
}
@media (min-width: 640px) {
  .app-layout__content { padding: 1.25rem; }
}
@media (min-width: 1024px) {
  .app-layout__content { padding: 1.5rem; }
}
</style>

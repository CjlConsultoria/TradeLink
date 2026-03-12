<template>
  <div>
    <h2 class="page-title">Dashboard · Consultor</h2>
    <LoadingSpinner v-if="loading" text="Carregando dashboard..." />
    <template v-else>
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8" data-onboarding="cards">
      <router-link to="/consultor/carteiras" class="card p-6 block hover:border-indigo-300">
        <p class="text-sm text-gray-500">Carteiras</p>
        <p class="text-3xl font-bold text-indigo-600 mt-1">{{ carteiras.length }}</p>
      </router-link>
      <router-link to="/consultor/clientes" class="card p-6 block hover:border-blue-300">
        <p class="text-sm text-gray-500">Clientes</p>
        <p class="text-3xl font-bold text-blue-600 mt-1">{{ clientes.length }}</p>
      </router-link>
      <router-link to="/consultor/cotacoes" class="card p-6 block hover:border-green-300">
        <p class="text-sm text-gray-500">Cotações</p>
        <p class="text-3xl font-bold text-green-600 mt-1">{{ cotacaoStore.cotacoes.length }}</p>
      </router-link>
    </div>
    <div data-onboarding="cotacoes">
      <CotacoesDashboardSection titulo="Cotações em tempo real" :show-refresh="true" />
    </div>

    <!-- Saude dos Portfolios -->
    <div class="card p-6 mb-8" data-onboarding="saude">
      <div class="flex flex-wrap items-center justify-between gap-3 mb-4">
        <h3 class="section-title">Saude dos Portfolios</h3>
        <router-link to="/consultor/rebalanceamento"
          class="px-4 py-2 bg-indigo-600 text-white rounded-lg text-sm hover:bg-indigo-700 whitespace-nowrap">
          Painel de Rebalanceamento
        </router-link>
      </div>
      <SaudeClientesGrid />
    </div>

    <div class="card p-6" data-onboarding="carteiras">
      <div class="flex items-center justify-between mb-4">
        <h3 class="section-title">Minhas Carteiras</h3>
        <router-link to="/consultor/carteiras" class="text-sm text-indigo-600 hover:underline">Ver todas</router-link>
      </div>
      <div class="flex flex-wrap gap-3 mb-4">
        <input v-model="filtros.nome" type="text" placeholder="Buscar por nome" class="px-3 py-2 border border-gray-300 rounded-lg text-sm w-full sm:w-48" />
        <button type="button" @click="paginaAtual = 0" class="px-3 py-2 bg-gray-100 rounded-lg text-sm hover:bg-gray-200">Filtrar</button>
      </div>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <router-link v-for="c in carteirasPaginadas" :key="c.id" :to="'/consultor/carteiras/' + c.id"
          class="block p-4 border border-gray-200 rounded-lg hover:border-indigo-300 hover:bg-indigo-50 transition-colors">
          <h4 class="font-medium text-gray-900">{{ c.nome }}</h4>
          <p class="text-sm text-gray-500 mt-1">{{ c.totalClientes }} clientes - {{ c.totalRecomendacoes }} recomendacoes</p>
        </router-link>
      </div>
      <div v-if="totalPaginas > 1" class="flex flex-wrap items-center justify-between gap-2 mt-3 pt-3 border-t border-gray-200">
        <p class="text-sm text-gray-500">{{ carteirasFiltradas.length }} resultado(s) · página {{ paginaAtual + 1 }} de {{ totalPaginas }}</p>
        <div class="flex gap-1">
          <button type="button" :disabled="paginaAtual === 0" @click="paginaAtual--" class="px-3 py-1 rounded border text-sm disabled:opacity-50">Anterior</button>
          <button type="button" :disabled="paginaAtual >= totalPaginas - 1" @click="paginaAtual++" class="px-3 py-1 rounded border text-sm disabled:opacity-50">Próxima</button>
        </div>
      </div>
    </div>

    </template>

    <!-- Onboarding Overlay -->
    <OnboardingOverlay
      :active="onboarding.active.value"
      :step="onboarding.step.value"
      :current-step="onboarding.currentStep.value"
      :total-steps="onboarding.totalSteps.value"
      :is-first="onboarding.isFirst.value"
      :is-last="onboarding.isLast.value"
      @next="onboarding.next()"
      @prev="onboarding.prev()"
      @skip="onboarding.skip()"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useCotacaoStore } from '../../stores/cotacao'
import { useOnboarding } from '../../composables/useOnboarding'
import carteiraApi from '../../api/carteiraApi'
import userApi from '../../api/userApi'
import CotacoesDashboardSection from '../../components/cotacao/CotacoesDashboardSection.vue'
import SaudeClientesGrid from '../../components/rebalanceamento/SaudeClientesGrid.vue'
import OnboardingOverlay from '../../components/common/OnboardingOverlay.vue'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'

const cotacaoStore = useCotacaoStore()

const onboarding = useOnboarding('consultor-dashboard', [
  {
    target: '[data-onboarding="cards"]',
    title: 'Metricas principais',
    message: 'Veja rapidamente o total de carteiras, clientes e cotacoes disponiveis na plataforma.',
    position: 'bottom'
  },
  {
    target: '[data-onboarding="cotacoes"]',
    title: 'Cotacoes em tempo real',
    message: 'Acompanhe as cotacoes atualizadas automaticamente. Use o botao de atualizar para forcar uma nova consulta.',
    position: 'bottom'
  },
  {
    target: '[data-onboarding="saude"]',
    title: 'Saude dos Portfolios',
    message: 'Monitore a saude de cada cliente. Vermelho indica portfolios que precisam de atencao urgente.',
    position: 'top'
  },
  {
    target: '[data-onboarding="carteiras"]',
    title: 'Suas Carteiras',
    message: 'Gerencie todas as suas carteiras aqui. Clique em uma carteira para ver detalhes e criar recomendacoes.',
    position: 'top'
  }
])

const loading = ref(true)
const carteiras = ref([])
const clientes = ref([])
const filtros = ref({ nome: '' })
const paginaAtual = ref(0)
const TAMANHO_PAGINA = 10

const carteirasFiltradas = computed(() => {
  let list = carteiras.value || []
  if (filtros.value.nome?.trim()) {
    const q = filtros.value.nome.trim().toLowerCase()
    list = list.filter(c => (c.nome || '').toLowerCase().includes(q))
  }
  return list
})

const totalPaginas = computed(() => Math.max(1, Math.ceil(carteirasFiltradas.value.length / TAMANHO_PAGINA)))

const carteirasPaginadas = computed(() => {
  const from = paginaAtual.value * TAMANHO_PAGINA
  return carteirasFiltradas.value.slice(from, from + TAMANHO_PAGINA)
})

onMounted(async () => {
  cotacaoStore.startPolling(60000)
  try {
    const [cartRes, cliRes] = await Promise.all([carteiraApi.listar(), userApi.listarClientes()])
    carteiras.value = cartRes.data
    clientes.value = cliRes.data
  } catch (e) { console.error(e) }
  finally { loading.value = false }
  onboarding.autoStart(1000)
})
onUnmounted(() => cotacaoStore.stopPolling())
</script>

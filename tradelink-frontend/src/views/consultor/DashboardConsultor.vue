<template>
  <div>
    <h2 class="page-title">Dashboard · Consultor</h2>
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
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
    <CotacoesDashboardSection titulo="Cotações em tempo real" :show-refresh="true" />
    <div class="card p-6">
      <div class="flex items-center justify-between mb-4">
        <h3 class="section-title">Minhas Carteiras</h3>
        <router-link to="/consultor/carteiras" class="text-sm text-indigo-600 hover:underline">Ver todas</router-link>
      </div>
      <div class="flex flex-wrap gap-3 mb-4">
        <input v-model="filtros.nome" type="text" placeholder="Buscar por nome" class="px-3 py-2 border border-gray-300 rounded-lg text-sm w-48" />
        <button type="button" @click="paginaAtual = 0" class="px-3 py-2 bg-gray-100 rounded-lg text-sm hover:bg-gray-200">Filtrar</button>
      </div>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <router-link v-for="c in carteirasPaginadas" :key="c.id" :to="'/consultor/carteiras/' + c.id"
          class="block p-4 border border-gray-200 rounded-lg hover:border-indigo-300 hover:bg-indigo-50 transition-colors">
          <h4 class="font-medium text-gray-900">{{ c.nome }}</h4>
          <p class="text-sm text-gray-500 mt-1">{{ c.totalClientes }} clientes - {{ c.totalRecomendacoes }} recomendacoes</p>
        </router-link>
      </div>
      <div v-if="totalPaginas > 1" class="flex items-center justify-between mt-3 pt-3 border-t border-gray-200">
        <p class="text-sm text-gray-500">{{ carteirasFiltradas.length }} resultado(s) · página {{ paginaAtual + 1 }} de {{ totalPaginas }}</p>
        <div class="flex gap-1">
          <button type="button" :disabled="paginaAtual === 0" @click="paginaAtual--" class="px-3 py-1 rounded border text-sm disabled:opacity-50">Anterior</button>
          <button type="button" :disabled="paginaAtual >= totalPaginas - 1" @click="paginaAtual++" class="px-3 py-1 rounded border text-sm disabled:opacity-50">Próxima</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useCotacaoStore } from '../../stores/cotacao'
import carteiraApi from '../../api/carteiraApi'
import userApi from '../../api/userApi'
import CotacoesDashboardSection from '../../components/cotacao/CotacoesDashboardSection.vue'

const cotacaoStore = useCotacaoStore()
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
})
onUnmounted(() => cotacaoStore.stopPolling())
</script>

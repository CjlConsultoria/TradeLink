<template>
  <div>
    <h2 class="page-title">Carteiras</h2>

    <LoadingSpinner v-if="loading" text="Carregando carteiras..." />

    <template v-else>
      <!-- Resumo -->
      <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
        <div class="card p-5">
          <p class="text-xs font-medium text-gray-500 uppercase tracking-wider">Total</p>
          <p class="text-2xl font-bold text-indigo-600 mt-1">{{ carteiras.length }}</p>
        </div>
        <div class="card p-5">
          <p class="text-xs font-medium text-gray-500 uppercase tracking-wider">Ativas</p>
          <p class="text-2xl font-bold text-green-600 mt-1">{{ carteiras.filter(c => c.ativa).length }}</p>
        </div>
        <div class="card p-5">
          <p class="text-xs font-medium text-gray-500 uppercase tracking-wider">Inativas</p>
          <p class="text-2xl font-bold text-red-600 mt-1">{{ carteiras.filter(c => !c.ativa).length }}</p>
        </div>
        <div class="card p-5">
          <p class="text-xs font-medium text-gray-500 uppercase tracking-wider">Total Clientes</p>
          <p class="text-2xl font-bold text-purple-600 mt-1">{{ totalClientesCarteiras }}</p>
        </div>
      </div>

      <!-- Filtros -->
      <div class="card p-4 mb-4">
        <div class="flex flex-wrap items-center gap-3">
          <div>
            <label class="text-xs text-gray-500 block mb-1">Empresa</label>
            <input v-model="filtroBusca" type="text" placeholder="Buscar empresa..."
              class="input-base text-sm py-1.5 px-3 w-52" />
          </div>
          <div>
            <label class="text-xs text-gray-500 block mb-1">Status</label>
            <select v-model="filtroStatus" class="input-base text-sm py-1.5 px-3 w-36">
              <option value="">Todas</option>
              <option value="ativas">Ativas</option>
              <option value="inativas">Inativas</option>
            </select>
          </div>
          <div class="ml-auto text-sm text-gray-500 self-end">
            {{ carteirasFiltradas.length }} carteira{{ carteirasFiltradas.length !== 1 ? 's' : '' }}
          </div>
        </div>
      </div>

      <!-- Tabela -->
      <div class="card p-6">
        <div class="overflow-x-auto">
          <table v-if="carteirasFiltradas.length > 0" class="w-full text-sm">
            <thead>
              <tr class="border-b border-gray-200">
                <th class="text-left py-2 px-2 font-medium text-gray-500">Nome</th>
                <th class="text-left py-2 px-2 font-medium text-gray-500">Empresa</th>
                <th class="text-left py-2 px-2 font-medium text-gray-500">Consultor</th>
                <th class="text-center py-2 px-2 font-medium text-gray-500">Clientes</th>
                <th class="text-center py-2 px-2 font-medium text-gray-500">Recomendações</th>
                <th class="text-center py-2 px-2 font-medium text-gray-500">Status</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="c in carteirasFiltradas" :key="c.id" class="border-b border-gray-100 hover:bg-gray-50">
                <td class="py-2.5 px-2 font-medium text-gray-900">{{ c.nome }}</td>
                <td class="py-2.5 px-2">
                  <router-link :to="`/admin-max/empresas/${c.empresaId}`"
                    class="text-indigo-600 hover:underline">{{ c.empresaNome }}</router-link>
                </td>
                <td class="py-2.5 px-2 text-gray-600">{{ c.consultorNome }}</td>
                <td class="py-2.5 px-2 text-center">{{ c.totalClientes }}</td>
                <td class="py-2.5 px-2 text-center">{{ c.totalRecomendacoes }}</td>
                <td class="py-2.5 px-2 text-center">
                  <span class="px-2 py-0.5 rounded-full text-xs font-medium"
                    :class="c.ativa ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'">
                    {{ c.ativa ? 'Ativa' : 'Inativa' }}
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
          <p v-else class="text-sm text-gray-400 text-center py-6">Nenhuma carteira encontrada</p>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import empresaApi from '../../api/empresaApi'
import { useToast } from '../../composables/useToast'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'

const toast = useToast()
const loading = ref(true)
const carteiras = ref([])
const filtroBusca = ref('')
const filtroStatus = ref('')

const totalClientesCarteiras = computed(() =>
  carteiras.value.reduce((sum, c) => sum + (c.totalClientes || 0), 0)
)

const carteirasFiltradas = computed(() => {
  let result = carteiras.value
  if (filtroStatus.value === 'ativas') result = result.filter(c => c.ativa)
  else if (filtroStatus.value === 'inativas') result = result.filter(c => !c.ativa)
  if (filtroBusca.value.trim()) {
    const busca = filtroBusca.value.toLowerCase().trim()
    result = result.filter(c =>
      (c.empresaNome && c.empresaNome.toLowerCase().includes(busca)) ||
      (c.nome && c.nome.toLowerCase().includes(busca)) ||
      (c.consultorNome && c.consultorNome.toLowerCase().includes(busca))
    )
  }
  return result
})

onMounted(async () => {
  try {
    const res = await empresaApi.listarCarteiras()
    carteiras.value = res.data
  } catch (e) {
    console.error('Erro ao carregar carteiras:', e)
    toast.error('Erro ao carregar carteiras')
  } finally {
    loading.value = false
  }
})
</script>

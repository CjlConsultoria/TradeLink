<template>
  <div>
    <h2 class="page-title">Dashboard · Super Admin</h2>

    <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
      <router-link to="/admin-max/empresas" class="card p-6 block hover:border-indigo-300">
        <p class="text-sm text-gray-500">Total Empresas</p>
        <p class="text-3xl font-bold text-indigo-600 mt-1">{{ empresas.length }}</p>
      </router-link>
      <router-link to="/admin-max/empresas" class="card p-6 block hover:border-blue-300">
        <p class="text-sm text-gray-500">Total Consultores</p>
        <p class="text-3xl font-bold text-blue-600 mt-1">{{ totalConsultores }}</p>
      </router-link>
      <router-link to="/admin-max/empresas" class="card p-6 block hover:border-green-300">
        <p class="text-sm text-gray-500">Total Clientes</p>
        <p class="text-3xl font-bold text-green-600 mt-1">{{ totalClientes }}</p>
      </router-link>
    </div>

    <CotacoesDashboardSection titulo="Cotações" />

    <div class="card p-6">
      <div class="flex items-center justify-between mb-4">
        <h3 class="section-title">Empresas</h3>
        <router-link to="/admin-max/empresas" class="text-sm text-indigo-600 hover:text-indigo-800 font-medium">Ver todas</router-link>
      </div>
      <div class="flex flex-wrap gap-3 mb-4">
        <input v-model="filtros.nome" type="text" placeholder="Buscar por nome" class="px-3 py-2 border border-gray-300 rounded-lg text-sm w-48" />
        <select v-model="filtros.categoria" class="px-3 py-2 border border-gray-300 rounded-lg text-sm w-48">
          <option value="">Todas as categorias (plano)</option>
          <option v-for="p in planos" :key="p.id" :value="p.id">{{ p.nome }}</option>
        </select>
        <button type="button" @click="paginaAtual = 0" class="px-3 py-2 bg-gray-100 rounded-lg text-sm hover:bg-gray-200">Filtrar</button>
      </div>
      <div class="overflow-x-auto">
        <table class="w-full text-sm">
          <thead>
            <tr class="border-b border-gray-200">
              <th class="text-left py-3 px-2 font-medium text-gray-500">Nome</th>
              <th class="text-left py-3 px-2 font-medium text-gray-500">CNPJ</th>
              <th class="text-center py-3 px-2 font-medium text-gray-500">Consultores</th>
              <th class="text-center py-3 px-2 font-medium text-gray-500">Clientes</th>
              <th class="text-center py-3 px-2 font-medium text-gray-500">Status</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="e in empresasPaginadas" :key="e.id" class="border-b border-gray-100 hover:bg-gray-50">
              <td class="py-3 px-2 font-medium">
                <router-link :to="`/admin-max/empresas/${e.id}`" class="text-indigo-600 hover:underline">{{ e.nome }}</router-link>
              </td>
              <td class="py-3 px-2 text-gray-600">{{ e.cnpj }}</td>
              <td class="py-3 px-2 text-center">{{ e.totalConsultores }}</td>
              <td class="py-3 px-2 text-center">{{ e.totalClientes }}</td>
              <td class="py-3 px-2 text-center">
                <span class="px-2 py-0.5 rounded-full text-xs" :class="e.ativo ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'">
                  {{ e.ativo ? 'Ativa' : 'Inativa' }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div v-if="totalPaginas > 1" class="flex items-center justify-between mt-3 pt-3 border-t border-gray-200">
        <p class="text-sm text-gray-500">{{ empresasFiltradas.length }} resultado(s) · página {{ paginaAtual + 1 }} de {{ totalPaginas }}</p>
        <div class="flex gap-1">
          <button type="button" :disabled="paginaAtual === 0" @click="paginaAtual--" class="px-3 py-1 rounded border text-sm disabled:opacity-50">Anterior</button>
          <button type="button" :disabled="paginaAtual >= totalPaginas - 1" @click="paginaAtual++" class="px-3 py-1 rounded border text-sm disabled:opacity-50">Próxima</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useEmpresaStore } from '../../stores/empresa'
import planoApi from '../../api/planoApi'
import CotacoesDashboardSection from '../../components/cotacao/CotacoesDashboardSection.vue'

const empresaStore = useEmpresaStore()
const empresas = computed(() => empresaStore.empresas)
const planos = ref([])
const filtros = ref({ nome: '', categoria: '' })
const paginaAtual = ref(0)
const TAMANHO_PAGINA = 10

const empresasFiltradas = computed(() => {
  let list = empresas.value || []
  if (filtros.value.nome?.trim()) {
    const q = filtros.value.nome.trim().toLowerCase()
    list = list.filter(e => (e.nome || '').toLowerCase().includes(q))
  }
  if (filtros.value.categoria) {
    const id = Number(filtros.value.categoria)
    list = list.filter(e => e.planoId === id)
  }
  return list
})

const totalPaginas = computed(() => Math.max(1, Math.ceil(empresasFiltradas.value.length / TAMANHO_PAGINA)))

const empresasPaginadas = computed(() => {
  const from = paginaAtual.value * TAMANHO_PAGINA
  return empresasFiltradas.value.slice(from, from + TAMANHO_PAGINA)
})

const totalConsultores = computed(() => empresas.value.reduce((sum, e) => sum + (e.totalConsultores || 0), 0))
const totalClientes = computed(() => empresas.value.reduce((sum, e) => sum + (e.totalClientes || 0), 0))

onMounted(async () => {
  empresaStore.listar()
  try {
    const res = await planoApi.listarAtivos()
    planos.value = res.data || []
  } catch (_) {}
})
</script>

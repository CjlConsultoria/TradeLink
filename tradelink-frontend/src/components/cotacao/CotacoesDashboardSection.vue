<template>
  <div class="mb-8">
    <h3 class="section-title">{{ titulo }}</h3>
    <div class="card p-4 mb-4">
      <p class="text-sm font-medium text-gray-700 mb-2">Filtros</p>
      <div class="flex flex-wrap gap-3 items-end">
        <div>
          <label class="block text-xs text-gray-500 mb-0.5">Moeda</label>
          <input v-model="filtros.moeda" type="text" placeholder="Ex: USD" class="px-3 py-2 border border-gray-300 rounded-lg text-sm w-24" />
        </div>
        <div>
          <label class="block text-xs text-gray-500 mb-0.5">Par</label>
          <input v-model="filtros.parMoeda" type="text" placeholder="Ex: BRL" class="px-3 py-2 border border-gray-300 rounded-lg text-sm w-24" />
        </div>
        <div>
          <label class="block text-xs text-gray-500 mb-0.5">Fonte</label>
          <input v-model="filtros.fonte" type="text" placeholder="Ex: AWESOME_API" class="px-3 py-2 border border-gray-300 rounded-lg text-sm w-32" />
        </div>
        <div>
          <label class="block text-xs text-gray-500 mb-0.5">Data de</label>
          <input v-model="filtros.dataDe" type="date" class="px-3 py-2 border border-gray-300 rounded-lg text-sm" />
        </div>
        <div>
          <label class="block text-xs text-gray-500 mb-0.5">Data até</label>
          <input v-model="filtros.dataAte" type="date" class="px-3 py-2 border border-gray-300 rounded-lg text-sm" />
        </div>
        <button type="button" @click="carregar(0)" class="px-4 py-2 bg-indigo-600 text-white rounded-lg text-sm hover:bg-indigo-700">Filtrar</button>
      </div>
    </div>
    <LoadingSpinner v-if="loading" />
    <template v-else>
      <CotacaoGrid :cotacoes="page.content" :show-refresh="showRefresh" :show-popup="true" />
      <div v-if="page.totalPages > 1" class="flex items-center justify-between mt-4 pt-4 border-t border-gray-200">
        <p class="text-sm text-gray-500">{{ page.totalElements }} resultado(s) · página {{ page.number + 1 }} de {{ page.totalPages }}</p>
        <div class="flex gap-1">
          <button type="button" :disabled="page.first" @click="carregar(page.number - 1)" class="px-3 py-1 rounded border text-sm disabled:opacity-50">Anterior</button>
          <button type="button" :disabled="page.last" @click="carregar(page.number + 1)" class="px-3 py-1 rounded border text-sm disabled:opacity-50">Próxima</button>
        </div>
      </div>
      <p v-if="page.content.length === 0" class="text-gray-500 text-sm mt-4">Nenhuma cotação encontrada.</p>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import cotacaoApi from '../../api/cotacaoApi'
import CotacaoGrid from './CotacaoGrid.vue'
import LoadingSpinner from '../common/LoadingSpinner.vue'

defineProps({
  titulo: { type: String, default: 'Cotações em tempo real' },
  showRefresh: { type: Boolean, default: false }
})

const loading = ref(false)
const filtros = ref({ moeda: '', parMoeda: '', fonte: '', dataDe: '', dataAte: '' })
const page = ref({ content: [], totalElements: 0, totalPages: 0, number: 0, first: true, last: true })
const TAMANHO_PAGINA = 12

async function carregar(pageNum = 0) {
  loading.value = true
  try {
    const params = { page: pageNum, size: TAMANHO_PAGINA, ordenarPor: 'dataHora', direcao: 'desc' }
    if (filtros.value.moeda?.trim()) params.moeda = filtros.value.moeda.trim()
    if (filtros.value.parMoeda?.trim()) params.parMoeda = filtros.value.parMoeda.trim()
    if (filtros.value.fonte?.trim()) params.fonte = filtros.value.fonte.trim()
    if (filtros.value.dataDe) params.dataDe = filtros.value.dataDe
    if (filtros.value.dataAte) params.dataAte = filtros.value.dataAte
    const res = await cotacaoApi.listarPaginado(params)
    page.value = res.data
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

onMounted(() => carregar(0))
</script>

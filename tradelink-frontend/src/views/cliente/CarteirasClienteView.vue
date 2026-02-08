<template>
  <div>
    <h2 class="text-2xl font-bold text-gray-900 mb-6">Minhas Carteiras</h2>
    <LoadingSpinner v-if="loading" />
    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      <router-link v-for="c in carteiras" :key="c.id" :to="'/cliente/carteiras/' + c.id"
        class="bg-white rounded-xl shadow-sm border border-gray-200 p-5 hover:shadow-md hover:border-indigo-300 transition-all block">
        <h3 class="font-semibold text-gray-900 mb-1">{{ c.nome }}</h3>
        <p v-if="c.descricao" class="text-sm text-gray-500 mb-2">{{ c.descricao }}</p>
        <p class="text-sm text-gray-600">Consultor: {{ c.consultorNome }}</p>
        <p class="text-sm text-gray-500">{{ c.totalRecomendacoes }} recomendacoes</p>
      </router-link>
    </div>
    <EmptyState v-if="!loading && carteiras.length === 0" message="Nenhuma carteira atribuida" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import carteiraApi from '../../api/carteiraApi'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'
import EmptyState from '../../components/common/EmptyState.vue'
const carteiras = ref([])
const loading = ref(true)
onMounted(async () => { try { carteiras.value = (await carteiraApi.listarComoCliente()).data } catch (e) { console.error(e) } finally { loading.value = false } })
</script>

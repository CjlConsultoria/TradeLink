<template>
  <div>
    <div class="flex items-center justify-between mb-6">
      <h2 class="text-2xl font-bold text-gray-900">Carteiras</h2>
      <button @click="showForm = true" class="bg-indigo-600 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-indigo-700">Nova Carteira</button>
    </div>
    <div v-if="showForm" class="fixed inset-0 z-50 flex items-center justify-center">
      <div class="fixed inset-0 bg-black/50" @click="closeForm"></div>
      <div class="bg-white rounded-lg shadow-xl p-6 max-w-md w-full mx-4 relative z-10">
        <h3 class="text-lg font-semibold mb-4">{{ editingId ? 'Editar' : 'Nova' }} Carteira</h3>
        <form @submit.prevent="salvar" class="space-y-4">
          <div><label class="block text-sm font-medium text-gray-700 mb-1">Nome</label><input v-model="form.nome" required class="w-full px-3 py-2 border border-gray-300 rounded-lg" /></div>
          <div><label class="block text-sm font-medium text-gray-700 mb-1">Descricao</label><textarea v-model="form.descricao" rows="3" class="w-full px-3 py-2 border border-gray-300 rounded-lg"></textarea></div>
          <p v-if="formError" class="text-red-500 text-sm">{{ formError }}</p>
          <div class="flex justify-end gap-3">
            <button type="button" @click="closeForm" class="px-4 py-2 text-sm text-gray-700 bg-gray-100 rounded-lg">Cancelar</button>
            <button type="submit" class="px-4 py-2 text-sm text-white bg-indigo-600 rounded-lg hover:bg-indigo-700">Salvar</button>
          </div>
        </form>
      </div>
    </div>
    <LoadingSpinner v-if="loading" />
    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      <div v-for="c in carteiras" :key="c.id" class="bg-white rounded-xl shadow-sm border border-gray-200 p-5 hover:shadow-md transition-shadow">
        <div class="flex items-center justify-between mb-2">
          <h3 class="font-semibold text-gray-900">{{ c.nome }}</h3>
          <span class="px-2 py-0.5 rounded-full text-xs" :class="c.ativa ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'">{{ c.ativa ? 'Ativa' : 'Inativa' }}</span>
        </div>
        <p v-if="c.descricao" class="text-sm text-gray-500 mb-3">{{ c.descricao }}</p>
        <div class="flex gap-4 text-sm text-gray-600 mb-4"><span>{{ c.totalClientes }} clientes</span><span>{{ c.totalRecomendacoes }} recomendacoes</span></div>
        <div class="flex flex-wrap gap-3 items-center">
          <router-link :to="'/consultor/carteiras/' + c.id" class="text-sm text-indigo-600 hover:underline">Detalhes</router-link>
          <button @click="editar(c)" class="text-sm text-blue-600 hover:underline">Editar</button>
          <button v-if="c.totalClientes === 0" @click="excluirCarteira(c)" class="text-sm text-red-600 hover:underline">Excluir</button>
        </div>
      </div>
    </div>
    <EmptyState v-if="!loading && carteiras.length === 0" message="Nenhuma carteira criada" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useCarteiraStore } from '../../stores/carteira'
import carteiraApi from '../../api/carteiraApi'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'
import EmptyState from '../../components/common/EmptyState.vue'
import { useToast } from '../../composables/useToast'
import { useConfirm } from '../../composables/useConfirm'

const toast = useToast()
const { confirm } = useConfirm()
const carteiraStore = useCarteiraStore()
const carteiras = computed(() => carteiraStore.carteiras)
const loading = computed(() => carteiraStore.loading)
const showForm = ref(false)
const editingId = ref(null)
const form = ref({ nome: '', descricao: '' })
const formError = ref('')

function closeForm() { showForm.value = false; editingId.value = null; form.value = { nome: '', descricao: '' }; formError.value = '' }
function editar(c) { editingId.value = c.id; form.value = { nome: c.nome, descricao: c.descricao || '' }; showForm.value = true }

async function salvar() {
  formError.value = ''
  try {
    if (editingId.value) await carteiraApi.atualizar(editingId.value, form.value)
    else await carteiraApi.criar(form.value)
    closeForm(); carteiraStore.listar()
  } catch (e) { formError.value = e.response?.data?.erro || 'Erro ao salvar' }
}

async function excluirCarteira(c) {
  let msg = `Excluir a carteira "${c.nome}"? Esta acao nao pode ser desfeita.`
  if (c.totalRecomendacoes > 0) {
    msg += `\n\nAtencao: ${c.totalRecomendacoes} recomendacao(oes) serao perdidas permanentemente.`
  }
  const ok = await confirm({ title: 'Excluir carteira', message: msg, confirmText: 'Excluir', variant: c.totalRecomendacoes > 0 ? 'warning' : 'danger' })
  if (!ok) return
  try {
    await carteiraApi.excluir(c.id)
    toast.success('Carteira excluida com sucesso.')
    carteiraStore.listar()
  } catch (e) {
    toast.error(e.response?.data?.erro || e.response?.data?.mensagem || 'Erro ao excluir.')
  }
}
onMounted(() => carteiraStore.listar())
</script>
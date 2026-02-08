<template>
  <div>
    <div class="flex items-center justify-between mb-6">
      <h2 class="text-2xl font-bold text-gray-900">Clientes</h2>
      <button @click="showForm = true" class="bg-indigo-600 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-indigo-700">Novo Cliente</button>
    </div>
    <div v-if="showForm" class="fixed inset-0 z-50 flex items-center justify-center">
      <div class="fixed inset-0 bg-black/50" @click="closeForm"></div>
      <div class="bg-white rounded-lg shadow-xl p-6 max-w-md w-full mx-4 relative z-10">
        <h3 class="text-lg font-semibold mb-4">Novo Cliente</h3>
        <form @submit.prevent="salvar" class="space-y-4">
          <div><label class="block text-sm font-medium text-gray-700 mb-1">Nome</label><input v-model="form.nome" required class="w-full px-3 py-2 border border-gray-300 rounded-lg" /></div>
          <div><label class="block text-sm font-medium text-gray-700 mb-1">E-mail</label><input v-model="form.email" type="email" required class="w-full px-3 py-2 border border-gray-300 rounded-lg" /></div>
          <div><label class="block text-sm font-medium text-gray-700 mb-1">Senha</label><input v-model="form.senha" type="password" required class="w-full px-3 py-2 border border-gray-300 rounded-lg" /></div>
          <p v-if="formError" class="text-red-500 text-sm">{{ formError }}</p>
          <div class="flex justify-end gap-3">
            <button type="button" @click="closeForm" class="px-4 py-2 text-sm text-gray-700 bg-gray-100 rounded-lg">Cancelar</button>
            <button type="submit" class="px-4 py-2 text-sm text-white bg-indigo-600 rounded-lg hover:bg-indigo-700">Criar</button>
          </div>
        </form>
      </div>
    </div>
    <LoadingSpinner v-if="loading" />
    <div v-else class="bg-white rounded-xl shadow-sm border border-gray-200">
      <table class="w-full text-sm">
        <thead><tr class="border-b border-gray-200"><th class="text-left py-3 px-4 font-medium text-gray-500">Nome</th><th class="text-left py-3 px-4 font-medium text-gray-500">E-mail</th><th class="text-center py-3 px-4 font-medium text-gray-500">Status</th></tr></thead>
        <tbody><tr v-for="c in clientes" :key="c.id" class="border-b border-gray-100 hover:bg-gray-50">
          <td class="py-3 px-4 font-medium">{{ c.nome }}</td><td class="py-3 px-4 text-gray-600">{{ c.email }}</td>
          <td class="py-3 px-4 text-center"><span class="px-2 py-0.5 rounded-full text-xs" :class="c.ativo !== false ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'">{{ c.ativo !== false ? 'Ativo' : 'Inativo' }}</span></td>
        </tr></tbody>
      </table>
      <EmptyState v-if="clientes.length === 0" message="Nenhum cliente cadastrado" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import userApi from '../../api/userApi'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'
import EmptyState from '../../components/common/EmptyState.vue'

const clientes = ref([])
const loading = ref(true)
const showForm = ref(false)
const form = ref({ nome: '', email: '', senha: '' })
const formError = ref('')
function closeForm() { showForm.value = false; form.value = { nome: '', email: '', senha: '' }; formError.value = '' }
async function loadClientes() { loading.value = true; try { clientes.value = (await userApi.listarClientes()).data } finally { loading.value = false } }
async function salvar() { formError.value = ''; try { await userApi.criarCliente(form.value); closeForm(); loadClientes() } catch (e) { formError.value = e.response?.data?.erro || 'Erro' } }
onMounted(loadClientes)
</script>
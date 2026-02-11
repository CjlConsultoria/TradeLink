<template>
  <div>
    <div class="flex items-center justify-between mb-6">
      <h2 class="text-2xl font-bold text-gray-900">Planos</h2>
      <button @click="showForm = true" class="bg-indigo-600 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-indigo-700">
        Novo Plano
      </button>
    </div>

    <div v-if="showForm" class="fixed inset-0 z-50 flex items-center justify-center">
      <div class="fixed inset-0 bg-black/50" @click="closeForm"></div>
      <div class="bg-white rounded-lg shadow-xl p-6 max-w-md w-full mx-4 relative z-10">
        <h3 class="text-lg font-semibold mb-4">{{ editingId ? 'Editar' : 'Novo' }} Plano</h3>
        <form @submit.prevent="salvar" class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Nome</label>
            <input v-model="form.nome" required class="w-full px-3 py-2 border border-gray-300 rounded-lg" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Máximo de usuários</label>
            <input v-model.number="form.maxUsuarios" type="number" min="1" required class="w-full px-3 py-2 border border-gray-300 rounded-lg" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Preço</label>
            <input v-model.number="form.preco" type="number" step="0.01" min="0" required class="w-full px-3 py-2 border border-gray-300 rounded-lg" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Stripe Price ID (cobrança mensal)</label>
            <input v-model="form.stripePriceId" type="text" placeholder="price_xxx (opcional)" class="w-full px-3 py-2 border border-gray-300 rounded-lg" />
            <p class="text-xs text-gray-500 mt-0.5">Crie um preço recorrente mensal no Stripe e cole o ID aqui para habilitar "Pagar plano".</p>
          </div>
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
      <div v-for="p in planos" :key="p.id" class="card p-5">
        <div class="flex items-center justify-between mb-3">
          <h3 class="font-semibold text-gray-900">{{ p.nome }}</h3>
          <span class="px-2 py-0.5 rounded-full text-xs" :class="p.ativo ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'">
            {{ p.ativo ? 'Ativo' : 'Inativo' }}
          </span>
        </div>
        <p class="text-sm text-gray-500 mb-1">Até {{ p.maxUsuarios }} usuários</p>
        <p class="text-lg font-semibold text-indigo-600 mb-1">R$ {{ Number(p.preco).toFixed(2) }}</p>
        <p v-if="p.stripePriceId" class="text-xs text-green-600 mb-4">Pagamento Stripe ativo</p>
        <p v-else class="text-xs text-gray-400 mb-4">Sem ID Stripe (só exibição)</p>
        <div class="flex flex-wrap items-center gap-2">
          <button @click="editar(p)" class="text-sm text-blue-600 hover:underline">Editar</button>
          <ToggleSwitch
            :model-value="!!p.ativo"
            label-on="Ativo"
            label-off="Inativo"
            variant="success"
            :loading="loadingAtivo[p.id]"
            @change="(val) => onAtivoChange(p, val)"
          />
        </div>
      </div>
    </div>
    <EmptyState v-if="!loading && planos.length === 0" message="Nenhum plano cadastrado" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useToast } from '../../composables/useToast'
import planoApi from '../../api/planoApi'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'
import EmptyState from '../../components/common/EmptyState.vue'
import ToggleSwitch from '../../components/common/ToggleSwitch.vue'

const toast = useToast()
const loadingAtivo = ref({})
const planos = ref([])
const loading = ref(true)
const showForm = ref(false)
const editingId = ref(null)
const form = ref({ nome: '', maxUsuarios: 1, preco: 0, stripePriceId: '' })
const formError = ref('')

function closeForm() {
  showForm.value = false
  editingId.value = null
  form.value = { nome: '', maxUsuarios: 1, preco: 0, stripePriceId: '' }
  formError.value = ''
}

function editar(plano) {
  editingId.value = plano.id
  form.value = { nome: plano.nome, maxUsuarios: plano.maxUsuarios, preco: plano.preco, stripePriceId: plano.stripePriceId || '' }
  showForm.value = true
}

async function salvar() {
  formError.value = ''
  const eraEdicao = !!editingId.value
  try {
    if (editingId.value) {
      await planoApi.atualizar(editingId.value, form.value)
    } else {
      await planoApi.criar(form.value)
    }
    closeForm()
    loadData()
    toast.success(eraEdicao ? 'Plano atualizado.' : 'Plano criado.')
  } catch (e) {
    formError.value = e.response?.data?.erro || 'Erro ao salvar'
    toast.error(formError.value)
  }
}

async function onAtivoChange(plano, ativo) {
  const msg = ativo ? 'Reativar este plano?' : 'Desativar este plano?'
  if (!confirm(msg)) return
  loadingAtivo.value[plano.id] = true
  try {
    if (ativo) {
      await planoApi.ativar(plano.id)
      toast.success('Plano reativado.')
    } else {
      await planoApi.desativar(plano.id)
      toast.success('Plano desativado.')
    }
    loadData()
  } catch (e) {
    toast.error(e.response?.data?.mensagem || 'Erro ao alterar status.')
  } finally {
    loadingAtivo.value[plano.id] = false
  }
}

async function loadData() {
  loading.value = true
  try {
    const res = await planoApi.listar()
    planos.value = res.data || []
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

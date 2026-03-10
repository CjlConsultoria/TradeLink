<template>
  <div>
    <div class="flex items-center justify-between mb-6">
      <h2 class="text-2xl font-bold text-gray-900">Planos</h2>
      <button @click="abrirNovoPlano" class="bg-indigo-600 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-indigo-700">
        Novo Plano
      </button>
    </div>

    <LoadingSpinner v-if="loading" />
    <template v-else>
      <!-- Plano Auto-Gestão (cliente individual) -->
      <div v-if="planoAutoGestao" class="mb-6">
        <h3 class="text-sm font-semibold text-gray-500 uppercase tracking-wider mb-3">Plano Cliente Individual</h3>
        <div class="card p-5 border-l-4 border-l-emerald-500">
          <div class="flex items-center justify-between mb-3">
            <div class="flex items-center gap-2">
              <h3 class="font-semibold text-gray-900">{{ planoAutoGestao.nome }}</h3>
              <span class="px-2 py-0.5 rounded-full text-xs bg-emerald-100 text-emerald-800">Auto-Gestão</span>
            </div>
            <span class="px-2 py-0.5 rounded-full text-xs" :class="planoAutoGestao.ativo ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'">
              {{ planoAutoGestao.ativo ? 'Ativo' : 'Inativo' }}
            </span>
          </div>
          <p class="text-sm text-gray-500 mb-1">Para clientes individuais (auto-gestão de carteira)</p>
          <p class="text-lg font-semibold text-emerald-600 mb-1">R$ {{ Number(planoAutoGestao.preco).toFixed(2) }} <span class="text-sm font-normal text-gray-400">/mês</span></p>
          <p v-if="planoAutoGestao.stripePriceId" class="text-xs text-green-600 mb-4">Pagamento Stripe ativo</p>
          <p v-else class="text-xs text-gray-400 mb-4">Sem ID Stripe (cobrança via checkout dinâmico)</p>
          <div class="flex flex-wrap items-center gap-2">
            <button @click="editar(planoAutoGestao)" class="text-sm text-blue-600 hover:underline">Editar preço</button>
            <ToggleSwitch
              :model-value="!!planoAutoGestao.ativo"
              label-on="Ativo"
              label-off="Inativo"
              variant="success"
              :loading="loadingAtivo[planoAutoGestao.id]"
              @change="(val) => onAtivoChange(planoAutoGestao, val)"
            />
          </div>
        </div>
      </div>

      <!-- Planos de Consultor -->
      <div>
        <h3 class="text-sm font-semibold text-gray-500 uppercase tracking-wider mb-3">Planos Consultor / Empresa</h3>
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          <div v-for="p in planosConsultor" :key="p.id" class="card p-5">
            <div class="flex items-center justify-between mb-3">
              <h3 class="font-semibold text-gray-900">{{ p.nome }}</h3>
              <span class="px-2 py-0.5 rounded-full text-xs" :class="p.ativo ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'">
                {{ p.ativo ? 'Ativo' : 'Inativo' }}
              </span>
            </div>
            <p class="text-sm text-gray-500 mb-1">Até {{ p.maxUsuarios }} usuários</p>
            <p class="text-lg font-semibold text-indigo-600 mb-1">R$ {{ Number(p.preco).toFixed(2) }} <span class="text-sm font-normal text-gray-400">/mês</span></p>
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
        <EmptyState v-if="planosConsultor.length === 0" message="Nenhum plano de consultor cadastrado" />
      </div>
    </template>

    <!-- Modal Form -->
    <div v-if="showForm" class="fixed inset-0 z-50 flex items-center justify-center">
      <div class="fixed inset-0 bg-black/50" @click="closeForm"></div>
      <div class="bg-white rounded-lg shadow-xl p-6 max-w-md w-full mx-4 relative z-10">
        <h3 class="text-lg font-semibold mb-4">{{ editingId ? 'Editar' : 'Novo' }} Plano</h3>
        <form @submit.prevent="salvar" class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Nome</label>
            <input v-model="form.nome" required class="w-full px-3 py-2 border border-gray-300 rounded-lg" />
          </div>
          <div v-if="!editingId">
            <label class="block text-sm font-medium text-gray-700 mb-1">Tipo</label>
            <select v-model="form.tipo" class="w-full px-3 py-2 border border-gray-300 rounded-lg">
              <option value="CONSULTOR">Consultor / Empresa</option>
              <option value="AUTO_GESTAO">Auto-Gestão (Cliente individual)</option>
            </select>
          </div>
          <div v-if="editingTipo" class="px-3 py-2 bg-gray-50 rounded-lg">
            <span class="text-xs font-medium text-gray-500">Tipo: </span>
            <span class="text-xs px-2 py-0.5 rounded-full" :class="editingTipo === 'AUTO_GESTAO' ? 'bg-emerald-100 text-emerald-800' : 'bg-indigo-100 text-indigo-800'">
              {{ editingTipo === 'AUTO_GESTAO' ? 'Auto-Gestão' : 'Consultor' }}
            </span>
          </div>
          <div v-if="form.tipo !== 'AUTO_GESTAO'">
            <label class="block text-sm font-medium text-gray-700 mb-1">Máximo de usuários</label>
            <input v-model.number="form.maxUsuarios" type="number" min="1" required class="w-full px-3 py-2 border border-gray-300 rounded-lg" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Preço (R$/mês)</label>
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
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useToast } from '../../composables/useToast'
import { useConfirm } from '../../composables/useConfirm'
import planoApi from '../../api/planoApi'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'
import EmptyState from '../../components/common/EmptyState.vue'
import ToggleSwitch from '../../components/common/ToggleSwitch.vue'

const toast = useToast()
const { confirm } = useConfirm()
const loadingAtivo = ref({})
const planos = ref([])
const loading = ref(true)
const showForm = ref(false)
const editingId = ref(null)
const editingTipo = ref(null)
const form = ref({ nome: '', tipo: 'CONSULTOR', maxUsuarios: 1, preco: 0, stripePriceId: '' })
const formError = ref('')

const planosConsultor = computed(() => planos.value.filter(p => p.tipo !== 'AUTO_GESTAO'))
const planoAutoGestao = computed(() => planos.value.find(p => p.tipo === 'AUTO_GESTAO'))

function abrirNovoPlano() {
  editingId.value = null
  editingTipo.value = null
  form.value = { nome: '', tipo: 'CONSULTOR', maxUsuarios: 1, preco: 0, stripePriceId: '' }
  formError.value = ''
  showForm.value = true
}

function closeForm() {
  showForm.value = false
  editingId.value = null
  editingTipo.value = null
  form.value = { nome: '', tipo: 'CONSULTOR', maxUsuarios: 1, preco: 0, stripePriceId: '' }
  formError.value = ''
}

function editar(plano) {
  editingId.value = plano.id
  editingTipo.value = plano.tipo || 'CONSULTOR'
  form.value = {
    nome: plano.nome,
    tipo: plano.tipo || 'CONSULTOR',
    maxUsuarios: plano.maxUsuarios,
    preco: plano.preco,
    stripePriceId: plano.stripePriceId || ''
  }
  showForm.value = true
}

async function salvar() {
  formError.value = ''
  const eraEdicao = !!editingId.value
  const payload = { ...form.value }
  if (payload.tipo === 'AUTO_GESTAO') {
    payload.maxUsuarios = 1
  }
  try {
    if (editingId.value) {
      await planoApi.atualizar(editingId.value, payload)
    } else {
      await planoApi.criar(payload)
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
  const msg = ativo ? 'Reativar este plano?' : 'Desativar este plano? Empresas com este plano nao serao afetadas.'
  const ok = await confirm({ title: ativo ? 'Reativar plano' : 'Desativar plano', message: msg, confirmText: ativo ? 'Reativar' : 'Desativar', variant: ativo ? 'info' : 'warning' })
  if (!ok) return
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

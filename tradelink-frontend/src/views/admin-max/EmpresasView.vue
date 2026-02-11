<template>
  <div>
    <div class="flex items-center justify-between mb-6">
      <h2 class="page-title mb-0">Empresas</h2>
      <button type="button" @click="showForm = true" class="btn-primary">
        Nova Empresa
      </button>
    </div>
    <p v-if="!pagamentoConfigurado" class="text-sm text-gray-500 mb-4">
      Para cobrar planos via cartão, configure o Stripe no backend (stripe.api-key) e o Stripe Price ID em cada plano.
    </p>

    <!-- Form Modal -->
    <div v-if="showForm" class="fixed inset-0 z-50 flex items-center justify-center">
      <div class="fixed inset-0 bg-black/50" @click="closeForm"></div>
      <div class="bg-white rounded-lg shadow-xl p-6 max-w-md w-full mx-4 relative z-10">
        <h3 class="text-lg font-semibold mb-4">{{ editingId ? 'Editar' : 'Nova' }} Empresa</h3>
        <form @submit.prevent="salvar" class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Nome</label>
            <input v-model="form.nome" required class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">CNPJ</label>
            <input v-model="form.cnpj" required class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500" placeholder="00.000.000/0000-00" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Plano</label>
            <select v-model="form.planoId" class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500">
              <option :value="null">Nenhum</option>
              <option v-for="p in planosAtivos" :key="p.id" :value="p.id">{{ p.nome }}</option>
            </select>
          </div>
          <p v-if="formError" class="text-red-500 text-sm">{{ formError }}</p>
          <div class="flex justify-end gap-3">
            <button type="button" @click="closeForm" class="px-4 py-2 text-sm text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200">Cancelar</button>
            <button type="submit" class="px-4 py-2 text-sm text-white bg-indigo-600 rounded-lg hover:bg-indigo-700">Salvar</button>
          </div>
        </form>
      </div>
    </div>

    <LoadingSpinner v-if="loading" />

    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      <div v-for="e in empresas" :key="e.id" class="card p-5">
        <div class="flex items-center justify-between mb-3">
          <h3 class="font-semibold text-gray-900">{{ e.nome }}</h3>
          <div class="flex items-center gap-2">
            <span v-if="e.acessoBloqueadoPorAdmin" class="px-2 py-0.5 rounded-full text-xs bg-red-100 text-red-800" title="Consultores e clientes sem acesso à plataforma">Acesso bloqueado</span>
            <span class="px-2 py-0.5 rounded-full text-xs" :class="e.ativo ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'">
              {{ e.ativo ? 'Ativa' : 'Inativa' }}
            </span>
          </div>
        </div>
        <p class="text-sm text-gray-500 mb-3">{{ e.cnpj }}</p>
        <div v-if="e.planoNome" class="mb-2">
          <p class="text-xs text-gray-500">Plano: {{ e.planoNome }}</p>
          <span v-if="e.subscriptionStatus" class="ml-2 text-xs px-2 py-0.5 rounded-full" :class="badgeAssinatura(e.subscriptionStatus)">
            {{ labelAssinatura(e.subscriptionStatus) }}
          </span>
          <p v-if="e.currentPeriodEnd" class="text-xs text-gray-500 mt-0.5">Próxima cobrança: {{ formatDate(e.currentPeriodEnd) }}</p>
          <div class="mt-1 h-2 bg-gray-200 rounded-full overflow-hidden">
            <div class="h-full bg-indigo-600 rounded-full transition-all" :style="{ width: Math.min(100, (e.totalUsuarios || 0) / (e.maxUsuarios || 1) * 100) + '%' }"></div>
          </div>
          <p class="text-xs text-gray-500 mt-0.5">{{ e.totalUsuarios ?? 0 }} / {{ e.maxUsuarios ?? 0 }} usuários</p>
        </div>
        <div v-else class="text-xs text-gray-400 mb-2">Sem plano</div>
        <div class="flex gap-4 text-sm text-gray-600 mb-4">
          <span>{{ e.totalConsultores }} consultores</span>
          <span>{{ e.totalClientes }} clientes</span>
        </div>
        <div class="flex flex-wrap gap-2">
          <router-link :to="`/admin-max/empresas/${e.id}`" class="text-sm text-indigo-600 hover:underline">Detalhes</router-link>
          <button v-if="planoComPagamento(e)" type="button" @click="irParaCheckout(e)" class="text-sm text-green-600 hover:underline">Pagar plano</button>
          <button type="button" @click="toggleBloqueioAcesso(e)" class="text-sm hover:underline" :class="e.acessoBloqueadoPorAdmin ? 'text-amber-600' : 'text-amber-700'">
            {{ e.acessoBloqueadoPorAdmin ? 'Desbloquear acesso' : 'Bloquear acesso' }}
          </button>
          <button @click="editar(e)" class="text-sm text-blue-600 hover:underline">Editar</button>
          <button v-if="e.ativo" @click="desativar(e.id)" class="text-sm text-red-600 hover:underline">Desativar</button>
        </div>
      </div>
    </div>

    <div v-if="listarError" class="p-4 rounded-xl bg-amber-50 border border-amber-200 text-amber-800 text-sm mb-4">
      <p>{{ listarError }}</p>
      <button type="button" @click="empresaStore.listar()" class="mt-2 px-3 py-1.5 bg-amber-100 hover:bg-amber-200 rounded-lg text-sm font-medium">
        Tentar novamente
      </button>
    </div>
    <EmptyState v-if="!loading && empresas.length === 0 && !listarError" message="Nenhuma empresa cadastrada" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useEmpresaStore } from '../../stores/empresa'
import { useToast } from '../../composables/useToast'
import empresaApi from '../../api/empresaApi'
import planoApi from '../../api/planoApi'
import paymentApi from '../../api/paymentApi'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'
import EmptyState from '../../components/common/EmptyState.vue'
import { formatDate } from '../../utils/formatters'

const toast = useToast()
const empresaStore = useEmpresaStore()
const empresas = computed(() => empresaStore.empresas)
const loading = computed(() => empresaStore.loading)
const listarError = computed(() => empresaStore.listarError)
const planosAtivos = ref([])
const pagamentoConfigurado = ref(false)

const showForm = ref(false)
const editingId = ref(null)
const form = ref({ nome: '', cnpj: '', planoId: null })
const formError = ref('')

function closeForm() {
  showForm.value = false
  editingId.value = null
  form.value = { nome: '', cnpj: '', planoId: null }
  formError.value = ''
}

function editar(empresa) {
  editingId.value = empresa.id
  form.value = { nome: empresa.nome, cnpj: empresa.cnpj, planoId: empresa.planoId ?? null }
  showForm.value = true
}

async function salvar() {
  formError.value = ''
  const eraEdicao = !!editingId.value
  try {
    if (editingId.value) {
      await empresaApi.atualizar(editingId.value, form.value)
    } else {
      await empresaApi.criar(form.value)
    }
    closeForm()
    empresaStore.listar()
    toast.success(eraEdicao ? 'Empresa atualizada.' : 'Empresa criada.')
  } catch (e) {
    formError.value = e.response?.data?.erro || 'Erro ao salvar'
    toast.error(formError.value)
  }
}

async function desativar(id) {
  if (confirm('Desativar esta empresa?')) {
    try {
      await empresaApi.desativar(id)
      empresaStore.listar()
      toast.success('Empresa desativada.')
    } catch (e) {
      toast.error(e.response?.data?.mensagem || 'Erro ao desativar.')
    }
  }
}

async function toggleBloqueioAcesso(empresa) {
  const novoEstado = !empresa.acessoBloqueadoPorAdmin
  const msg = novoEstado ? 'Bloquear acesso à plataforma para todos os consultores e clientes desta empresa?' : 'Desbloquear o acesso à plataforma?'
  if (!confirm(msg)) return
  try {
    await empresaApi.bloquearAcesso(empresa.id, novoEstado)
    empresaStore.listar()
    toast.success(novoEstado ? 'Acesso bloqueado. Consultores e clientes não poderão acessar a plataforma.' : 'Acesso desbloqueado.')
  } catch (e) {
    toast.error(e.response?.data?.mensagem || e.response?.data?.erro || 'Erro ao alterar bloqueio.')
  }
}

function planoComPagamento(empresa) {
  if (!empresa.planoId || !pagamentoConfigurado.value) return false
  const plano = planosAtivos.value.find(p => p.id === empresa.planoId)
  return plano && plano.stripePriceId
}

async function irParaCheckout(empresa) {
  try {
    const res = await paymentApi.createCheckout(empresa.id, empresa.planoId)
    const url = res.data?.checkoutUrl
    if (url) {
      window.location.href = url
    } else {
      toast.error('Resposta inválida do servidor.')
    }
  } catch (e) {
    toast.error(e.response?.data?.mensagem || 'Erro ao abrir pagamento.')
  }
}

function labelStatusPagamento(status) {
  const map = { EM_DIA: 'Em dia', VENCIDO: 'Venceu', EM_ATRASO: 'Em atraso', SEM_ASSINATURA: 'Sem assinatura' }
  return map[status] || status
}

function badgeStatusPagamento(status) {
  const map = { EM_DIA: 'bg-green-100 text-green-800', VENCIDO: 'bg-amber-100 text-amber-800', EM_ATRASO: 'bg-red-100 text-red-800', SEM_ASSINATURA: 'bg-gray-100 text-gray-600' }
  return map[status] || 'bg-gray-100 text-gray-700'
}

function labelAssinatura(status) {
  return labelStatusPagamento(status)
}

function badgeAssinatura(status) {
  return badgeStatusPagamento(status)
}

onMounted(async () => {
  empresaStore.listar()
  try {
    const [planosRes, configRes] = await Promise.all([
      planoApi.listarAtivos(),
      paymentApi.isConfigured().catch(() => ({ data: { configurado: false } }))
    ])
    planosAtivos.value = planosRes.data || []
    pagamentoConfigurado.value = configRes.data?.configurado === true
  } catch (_) {}
})
</script>

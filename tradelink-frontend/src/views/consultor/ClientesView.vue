<template>
  <div>
    <!-- Header + Licença -->
    <div class="flex items-center justify-between mb-4">
      <h2 class="text-2xl font-bold text-gray-900">Clientes</h2>
      <div class="flex items-center gap-3">
        <span v-if="licenca" class="text-xs text-gray-500">
          {{ licenca.totalClientes }} cliente(s)
          <template v-if="licenca.maxUsuarios"> / {{ licenca.maxUsuarios }} max</template>
        </span>
        <button
          @click="showVincularForm = true"
          :disabled="licenca && !licenca.podeConvidar"
          class="bg-emerald-600 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-emerald-700 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors"
        >
          Vincular Existente
        </button>
        <button
          @click="showForm = true"
          :disabled="licenca && !licenca.podeConvidar"
          class="bg-indigo-600 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-indigo-700 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors"
        >
          Convidar Cliente
        </button>
      </div>
    </div>

    <!-- Banner limite atingido -->
    <div v-if="licenca && !licenca.podeConvidar" class="bg-amber-50 border border-amber-200 rounded-lg p-4 mb-4">
      <div class="flex items-start gap-3">
        <span class="text-amber-500 text-lg">⚠️</span>
        <div>
          <p class="text-amber-800 text-sm font-semibold">Limite de usuários atingido</p>
          <p class="text-amber-700 text-xs mt-1">
            Seu plano <strong>{{ licenca.planoNome || 'atual' }}</strong> permite até
            <strong>{{ licenca.maxUsuarios }}</strong> usuários ({{ licenca.totalUsuarios }} em uso).
            Entre em contato para fazer upgrade do plano.
          </p>
        </div>
      </div>
    </div>

    <!-- Modal Convite -->
    <div v-if="showForm" class="fixed inset-0 z-50 flex items-center justify-center">
      <div class="fixed inset-0 bg-black/50" @click="closeForm"></div>
      <div class="bg-white rounded-xl shadow-xl p-6 max-w-md w-full mx-4 relative z-10">
        <h3 class="text-lg font-semibold mb-2">Convidar Cliente</h3>
        <p class="text-sm text-gray-500 mb-5">
          O cliente receberá um e-mail com link para ativar a conta e preencher seus dados.
        </p>
        <form @submit.prevent="convidar" class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">E-mail do cliente *</label>
            <input
              v-model="formEmail"
              type="email"
              required
              class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
              placeholder="cliente@email.com"
              :disabled="enviando"
            />
          </div>
          <p v-if="formError" class="text-red-500 text-sm">{{ formError }}</p>
          <div class="flex justify-end gap-3 pt-2">
            <button type="button" @click="closeForm" class="px-4 py-2 text-sm text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200">Cancelar</button>
            <button
              type="submit"
              :disabled="enviando"
              class="px-4 py-2 text-sm text-white bg-indigo-600 rounded-lg hover:bg-indigo-700 disabled:opacity-50 transition-colors"
            >
              {{ enviando ? 'Enviando...' : 'Enviar Convite' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Modal Vincular Existente -->
    <div v-if="showVincularForm" class="fixed inset-0 z-50 flex items-center justify-center">
      <div class="fixed inset-0 bg-black/50" @click="closeVincularForm"></div>
      <div class="bg-white rounded-xl shadow-xl p-6 max-w-md w-full mx-4 relative z-10">
        <h3 class="text-lg font-semibold mb-2">Vincular Cliente Existente</h3>
        <p class="text-sm text-gray-500 mb-5">
          Informe o e-mail de um cliente que foi desvinculado de outro consultor. Ele será adicionado ao seu grupo.
        </p>
        <form @submit.prevent="vincular" class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">E-mail do cliente *</label>
            <input
              v-model="vincularEmail"
              type="email"
              required
              class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500"
              placeholder="cliente@email.com"
              :disabled="vinculando"
            />
          </div>
          <p v-if="vincularError" class="text-red-500 text-sm">{{ vincularError }}</p>
          <div class="flex justify-end gap-3 pt-2">
            <button type="button" @click="closeVincularForm" class="px-4 py-2 text-sm text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200">Cancelar</button>
            <button
              type="submit"
              :disabled="vinculando"
              class="px-4 py-2 text-sm text-white bg-emerald-600 rounded-lg hover:bg-emerald-700 disabled:opacity-50 transition-colors"
            >
              {{ vinculando ? 'Vinculando...' : 'Vincular ao Meu Grupo' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Tabela -->
    <LoadingSpinner v-if="loading" />
    <div v-else class="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full text-sm table-responsive min-w-[320px]">
          <thead>
            <tr class="border-b border-gray-200">
              <th class="text-left py-3 px-4 font-medium text-gray-500">Nome</th>
              <th class="text-left py-3 px-4 font-medium text-gray-500">E-mail</th>
              <th class="text-center py-3 px-4 font-medium text-gray-500">Status</th>
              <th class="text-right py-3 px-4 font-medium text-gray-500">Ações</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="c in clientes" :key="c.id" class="border-b border-gray-100 hover:bg-gray-50">
              <td class="py-3 px-4 font-medium">{{ c.nome || '—' }}</td>
              <td class="py-3 px-4 text-gray-600">{{ c.email }}</td>
              <td class="py-3 px-4 text-center">
                <span class="px-2 py-0.5 rounded-full text-xs font-medium" :class="statusClass(c)">
                  {{ statusLabel(c) }}
                </span>
              </td>
              <td class="py-3 px-4 text-right flex items-center justify-end gap-2">
                <!-- Reenviar convite para pendentes -->
                <button
                  v-if="c.status === 'PENDENTE'"
                  @click="reenviarConvite(c)"
                  :disabled="reenviandoId === c.id"
                  class="text-indigo-600 hover:text-indigo-800 text-xs font-medium disabled:opacity-50"
                >
                  {{ reenviandoId === c.id ? 'Enviando...' : 'Reenviar Convite' }}
                </button>
                <!-- Toggle ativo/inativo para não-pendentes -->
                <ToggleSwitch
                  v-if="c.status !== 'PENDENTE'"
                  :model-value="c.ativo !== false"
                  label-on="Ativo"
                  label-off="Inativo"
                  variant="success"
                  :loading="loadingAtivo[c.id]"
                  @change="(val) => onAtivoChange(c, val)"
                />
                <button
                  type="button"
                  :disabled="acaoClienteId === c.id"
                  @click="excluir(c)"
                  class="text-red-600 hover:underline text-xs font-medium disabled:opacity-50 ml-1"
                >
                  Excluir
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <EmptyState v-if="clientes.length === 0" message="Nenhum cliente cadastrado. Convide seu primeiro cliente!" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import userApi from '../../api/userApi'
import { useToast } from '../../composables/useToast'
import { useConfirm } from '../../composables/useConfirm'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'
import EmptyState from '../../components/common/EmptyState.vue'
import ToggleSwitch from '../../components/common/ToggleSwitch.vue'

const toast = useToast()
const { confirm } = useConfirm()
const loadingAtivo = ref({})
const clientes = ref([])
const loading = ref(true)
const showForm = ref(false)
const formEmail = ref('')
const formError = ref('')
const enviando = ref(false)
const acaoClienteId = ref(null)
const reenviandoId = ref(null)
const licenca = ref(null)
const showVincularForm = ref(false)
const vincularEmail = ref('')
const vincularError = ref('')
const vinculando = ref(false)

function statusLabel(c) {
  if (c.status === 'PENDENTE') return 'Pendente'
  if (c.status === 'ATIVO') return 'Ativo'
  return 'Inativo'
}

function statusClass(c) {
  if (c.status === 'PENDENTE') return 'bg-yellow-100 text-yellow-800'
  if (c.status === 'ATIVO') return 'bg-green-100 text-green-800'
  return 'bg-red-100 text-red-800'
}

function closeForm() {
  showForm.value = false
  formEmail.value = ''
  formError.value = ''
}

async function loadClientes() {
  loading.value = true
  try {
    clientes.value = (await userApi.listarClientes()).data
  } finally {
    loading.value = false
  }
}

async function loadLicenca() {
  try {
    licenca.value = (await userApi.getLicenca()).data
  } catch (e) {
    // silencioso — não bloqueia a tela
  }
}

async function convidar() {
  formError.value = ''
  enviando.value = true
  try {
    await userApi.convidarCliente(formEmail.value)
    closeForm()
    loadClientes()
    loadLicenca()
    toast.success('Convite enviado! O cliente receberá um e-mail para ativar a conta.')
  } catch (e) {
    formError.value = e.response?.data?.erro || 'Erro ao enviar convite'
  } finally {
    enviando.value = false
  }
}

function closeVincularForm() {
  showVincularForm.value = false
  vincularEmail.value = ''
  vincularError.value = ''
}

async function vincular() {
  vincularError.value = ''
  vinculando.value = true
  try {
    await userApi.vincularCliente(vincularEmail.value)
    closeVincularForm()
    loadClientes()
    loadLicenca()
    toast.success('Cliente vinculado ao seu grupo com sucesso!')
  } catch (e) {
    vincularError.value = e.response?.data?.erro || 'Erro ao vincular cliente'
  } finally {
    vinculando.value = false
  }
}

async function reenviarConvite(c) {
  reenviandoId.value = c.id
  try {
    await userApi.convidarCliente(c.email)
    toast.success('Convite reenviado para ' + c.email)
  } catch (e) {
    toast.error(e.response?.data?.erro || 'Erro ao reenviar convite')
  } finally {
    reenviandoId.value = null
  }
}

async function onAtivoChange(c, ativo) {
  if (!ativo) {
    const ok = await confirm({ title: 'Inativar cliente', message: `Inativar o cliente "${c.nome || c.email}"? Ele nao podera mais acessar o sistema.`, confirmText: 'Inativar', variant: 'warning' })
    if (!ok) return
  }
  loadingAtivo.value[c.id] = true
  try {
    if (ativo) {
      await userApi.ativarCliente(c.id)
      loadClientes()
      toast.success('Cliente reativado.')
    } else {
      await userApi.inativarCliente(c.id)
      loadClientes()
      toast.success('Cliente inativado.')
    }
  } catch (e) {
    toast.error(e.response?.data?.erro || e.response?.data?.mensagem || 'Erro ao alterar status.')
  } finally {
    loadingAtivo.value[c.id] = false
  }
}

async function excluir(c) {
  const ok = await confirm({ title: 'Excluir cliente', message: `Excluir o cliente "${c.nome || c.email}"? Ele sera removido de todas as carteiras e inativado.`, confirmText: 'Excluir', variant: 'danger' })
  if (!ok) return
  acaoClienteId.value = c.id
  userApi.excluirCliente(c.id)
    .then(() => { loadClientes(); loadLicenca(); toast.success('Cliente excluído.') })
    .catch(e => { toast.error(e.response?.data?.erro || e.response?.data?.mensagem || 'Erro ao excluir.') })
    .finally(() => { acaoClienteId.value = null })
}

onMounted(() => {
  loadClientes()
  loadLicenca()
})
</script>

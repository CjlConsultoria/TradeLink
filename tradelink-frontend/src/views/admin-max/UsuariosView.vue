<template>
  <div>
    <h2 class="page-title mb-6">Usuários (Super Admin)</h2>
    <p class="text-sm text-gray-600 mb-4">Gerencie qualquer usuário: editar dados, ativar, inativar.</p>

    <div class="flex flex-wrap gap-3 mb-4">
      <select v-model="filtroRole" class="px-3 py-2 border border-gray-300 rounded-lg text-sm w-40">
        <option value="">Todos os perfis</option>
        <option value="AdminMax">Super Admin</option>
        <option value="Admin">Consultor</option>
        <option value="Cliente">Cliente</option>
      </select>
      <select v-model="filtroEmpresaId" class="px-3 py-2 border border-gray-300 rounded-lg text-sm w-56">
        <option value="">Todas as empresas</option>
        <option v-for="e in empresas" :key="e.id" :value="e.id">{{ e.nome }}</option>
      </select>
      <button type="button" @click="carregar" class="px-4 py-2 bg-gray-100 rounded-lg text-sm hover:bg-gray-200">Filtrar</button>
    </div>

    <LoadingSpinner v-if="loading" />
    <template v-else>
      <div class="overflow-x-auto">
        <table class="w-full border border-gray-200 rounded-lg overflow-hidden">
          <thead class="bg-gray-50">
            <tr>
              <th class="text-left py-3 px-4 text-sm font-medium text-gray-700">Nome</th>
              <th class="text-left py-3 px-4 text-sm font-medium text-gray-700">E-mail</th>
              <th class="text-left py-3 px-4 text-sm font-medium text-gray-700">Perfil</th>
              <th class="text-left py-3 px-4 text-sm font-medium text-gray-700">Empresa</th>
              <th class="text-left py-3 px-4 text-sm font-medium text-gray-700">Status</th>
              <th class="text-left py-3 px-4 text-sm font-medium text-gray-700">Ações</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="u in usuariosFiltrados" :key="u.id" class="border-t border-gray-100 hover:bg-gray-50/50">
              <td class="py-3 px-4 text-sm text-gray-900">{{ u.nome }}</td>
              <td class="py-3 px-4 text-sm text-gray-600">{{ u.email }}</td>
              <td class="py-3 px-4">
                <span class="text-xs px-2 py-0.5 rounded-full" :class="badgeRole(u.role)">{{ labelRole(u.role) }}</span>
              </td>
              <td class="py-3 px-4 text-sm text-gray-600">{{ u.empresaNome || '-' }}</td>
              <td class="py-3 px-4">
                <span class="text-xs px-2 py-0.5 rounded-full" :class="u.ativo ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'">
                  {{ u.ativo ? 'Ativo' : 'Inativo' }}
                </span>
              </td>
              <td class="py-3 px-4">
                <div class="flex flex-wrap items-center gap-2">
                  <button type="button" @click="abrirEdicao(u)" class="text-sm text-indigo-600 hover:underline">Editar</button>
                  <button type="button" @click="abrirModalSenha(u)" class="text-sm text-amber-600 hover:underline">Alterar senha</button>
                  <ToggleSwitch
                    :model-value="!!u.ativo"
                    label-on="Ativo"
                    label-off="Inativo"
                    variant="success"
                    :loading="loadingAtivo[u.id]"
                    @change="(val) => onAtivoChange(u, val)"
                  />
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <p v-if="usuariosFiltrados.length === 0" class="text-gray-500 text-sm mt-4">Nenhum usuário encontrado.</p>
    </template>

    <!-- Modal Editar usuário -->
    <Teleport to="body">
      <div v-if="modal.visivel" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4" @click.self="fecharModal">
        <div class="bg-white rounded-xl shadow-xl max-w-md w-full p-6">
          <h3 class="text-lg font-semibold mb-4">Editar usuário</h3>
          <form @submit.prevent="salvarEdicao" class="space-y-3">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Nome</label>
              <input v-model="modal.nome" type="text" required class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">E-mail</label>
              <input v-model="modal.email" type="email" required class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm" placeholder="exemplo@email.com" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Telefone / WhatsApp</label>
              <input v-model="modal.telefone" type="tel" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm" placeholder="(11) 99999-9999" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Perfil</label>
              <select v-model="modal.role" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm">
                <option value="AdminMax">Super Admin</option>
                <option value="Admin">Consultor</option>
                <option value="Cliente">Cliente</option>
              </select>
            </div>
            <div v-if="modal.role !== 'AdminMax'">
              <label class="block text-sm font-medium text-gray-700 mb-1">Empresa</label>
              <select v-model="modal.empresaId" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm">
                <option :value="null">Nenhuma</option>
                <option v-for="e in empresas" :key="e.id" :value="e.id">{{ e.nome }}</option>
              </select>
            </div>
            <div v-else>
              <input v-model="modal.empresaId" type="hidden" />
            </div>
            <div class="flex items-center gap-2">
              <input v-model="modal.ativo" type="checkbox" id="modal-ativo" class="rounded border-gray-300 text-indigo-600 focus:ring-indigo-500" />
              <label for="modal-ativo" class="text-sm text-gray-700">Usuário ativo</label>
            </div>
            <p v-if="modal.erro" class="text-sm text-red-500">{{ modal.erro }}</p>
            <div class="flex gap-2 pt-2">
              <button type="submit" :disabled="modal.salvando" class="px-4 py-2 bg-indigo-600 text-white rounded-lg text-sm hover:bg-indigo-700 disabled:opacity-50">
                {{ modal.salvando ? 'Salvando...' : 'Salvar' }}
              </button>
              <button type="button" @click="fecharModal" class="px-4 py-2 bg-gray-100 rounded-lg text-sm hover:bg-gray-200">Cancelar</button>
            </div>
          </form>
        </div>
      </div>
    </Teleport>

    <!-- Modal Alterar senha -->
    <Teleport to="body">
      <div v-if="modalSenha.visivel" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4" @click.self="fecharModalSenha">
        <div class="bg-white rounded-xl shadow-xl max-w-sm w-full p-6">
          <h3 class="text-lg font-semibold mb-2">Alterar senha</h3>
          <p class="text-sm text-gray-500 mb-4">{{ modalSenha.nome }} ({{ modalSenha.email }})</p>
          <form @submit.prevent="salvarSenha" class="space-y-3">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Nova senha (mín. 6 caracteres)</label>
              <input v-model="modalSenha.novaSenha" type="password" required minlength="6" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm" placeholder="Nova senha" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Confirmar senha</label>
              <input v-model="modalSenha.confirmarSenha" type="password" required class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm" placeholder="Repita a senha" />
            </div>
            <p v-if="modalSenha.erro" class="text-sm text-red-500">{{ modalSenha.erro }}</p>
            <div class="flex gap-2 pt-2">
              <button type="submit" :disabled="modalSenha.salvando" class="px-4 py-2 bg-amber-600 text-white rounded-lg text-sm hover:bg-amber-700 disabled:opacity-50">
                {{ modalSenha.salvando ? 'Salvando...' : 'Alterar senha' }}
              </button>
              <button type="button" @click="fecharModalSenha" class="px-4 py-2 bg-gray-100 rounded-lg text-sm hover:bg-gray-200">Cancelar</button>
            </div>
          </form>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import userApi from '../../api/userApi'
import empresaApi from '../../api/empresaApi'
import { useToast } from '../../composables/useToast'
import { useConfirm } from '../../composables/useConfirm'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'
import ToggleSwitch from '../../components/common/ToggleSwitch.vue'

const route = useRoute()
const toast = useToast()
const { confirm } = useConfirm()
const loadingAtivo = ref({})
const loading = ref(true)
const usuarios = ref([])
const empresas = ref([])
const filtroRole = ref(route.query.role || '')
const filtroEmpresaId = ref('')

const modal = ref({
  visivel: false,
  id: null,
  nome: '',
  email: '',
  telefone: '',
  role: 'Cliente',
  empresaId: null,
  ativo: true,
  salvando: false,
  erro: ''
})

const usuariosFiltrados = computed(() => {
  let list = usuarios.value
  if (filtroRole.value) list = list.filter(u => u.role === filtroRole.value)
  if (filtroEmpresaId.value) list = list.filter(u => String(u.empresaId) === String(filtroEmpresaId.value))
  return list
})

function labelRole(role) {
  const map = { AdminMax: 'Super Admin', Admin: 'Consultor', Cliente: 'Cliente' }
  return map[role] || role
}
function badgeRole(role) {
  const map = { AdminMax: 'bg-purple-100 text-purple-800', Admin: 'bg-blue-100 text-blue-800', Cliente: 'bg-gray-100 text-gray-800' }
  return map[role] || 'bg-gray-100 text-gray-800'
}

async function carregar() {
  loading.value = true
  try {
    const [usersRes, empRes] = await Promise.all([
      userApi.listarTodos(),
      empresaApi.listar().catch(() => ({ data: [] }))
    ])
    usuarios.value = usersRes.data || []
    empresas.value = empRes.data || []
  } catch (e) {
    console.error(e)
    toast.error('Erro ao carregar usuários.')
  } finally {
    loading.value = false
  }
}

function abrirEdicao(u) {
  modal.value = {
    visivel: true,
    id: u.id,
    nome: u.nome || '',
    email: u.email || '',
    telefone: u.telefone || '',
    role: u.role || 'Cliente',
    empresaId: u.empresaId ?? null,
    ativo: u.ativo !== false,
    salvando: false,
    erro: ''
  }
}
function fecharModal() {
  modal.value.visivel = false
}
async function salvarEdicao() {
  const m = modal.value
  m.erro = ''
  m.salvando = true
  try {
    const data = {
      nome: m.nome.trim(),
      email: m.email.trim(),
      telefone: m.telefone != null ? String(m.telefone).trim() || null : null,
      ativo: m.ativo,
      role: m.role,
      empresaId: m.role === 'AdminMax' ? null : (m.empresaId || null)
    }
    const res = await userApi.atualizarAdmin(m.id, data)
    const updated = res.data
    const idx = usuarios.value.findIndex(x => x.id === m.id)
    if (idx !== -1) usuarios.value[idx] = { ...usuarios.value[idx], ...updated }
    toast.success('Usuário atualizado.')
    fecharModal()
  } catch (e) {
    m.erro = e.response?.data?.mensagem || e.response?.data?.erro || 'Erro ao salvar.'
    toast.error(m.erro)
  } finally {
    m.salvando = false
  }
}

async function onAtivoChange(u, ativo) {
  if (!ativo) {
    const ok = await confirm({ title: 'Inativar usuario', message: `Inativar o usuario ${u.nome}? Ele nao podera mais acessar o sistema.`, confirmText: 'Inativar', variant: 'warning' })
    if (!ok) return
  }
  loadingAtivo.value[u.id] = true
  try {
    if (ativo) {
      await userApi.ativar(u.id)
      const idx = usuarios.value.findIndex(x => x.id === u.id)
      if (idx !== -1) usuarios.value[idx] = { ...usuarios.value[idx], ativo: true }
      toast.success('Usuário ativado.')
    } else {
      await userApi.desativar(u.id)
      const idx = usuarios.value.findIndex(x => x.id === u.id)
      if (idx !== -1) usuarios.value[idx] = { ...usuarios.value[idx], ativo: false }
      toast.success('Usuário inativado.')
    }
  } catch (e) {
    toast.error(e.response?.data?.mensagem || 'Erro ao alterar status.')
  } finally {
    loadingAtivo.value[u.id] = false
  }
}

const modalSenha = ref({
  visivel: false,
  id: null,
  nome: '',
  email: '',
  novaSenha: '',
  confirmarSenha: '',
  salvando: false,
  erro: ''
})
function abrirModalSenha(u) {
  modalSenha.value = {
    visivel: true,
    id: u.id,
    nome: u.nome || '',
    email: u.email || '',
    novaSenha: '',
    confirmarSenha: '',
    salvando: false,
    erro: ''
  }
}
function fecharModalSenha() {
  modalSenha.value.visivel = false
}
async function salvarSenha() {
  const m = modalSenha.value
  m.erro = ''
  if (m.novaSenha.length < 6) {
    m.erro = 'Senha deve ter no mínimo 6 caracteres.'
    return
  }
  if (m.novaSenha !== m.confirmarSenha) {
    m.erro = 'As senhas não coincidem.'
    return
  }
  m.salvando = true
  try {
    await userApi.alterarSenhaAdmin(m.id, { novaSenha: m.novaSenha })
    toast.success('Senha alterada.')
    fecharModalSenha()
  } catch (e) {
    m.erro = e.response?.data?.mensagem || e.response?.data?.erro || 'Erro ao alterar senha.'
    toast.error(m.erro)
  } finally {
    m.salvando = false
  }
}

onMounted(() => carregar())
</script>

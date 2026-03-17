<template>
  <div>
    <h2 class="page-title">Usuarios</h2>
    <p class="text-muted -mt-4 mb-6">Gerencie qualquer usuario: editar dados, ativar, inativar.</p>

    <!-- Filtros -->
    <div class="card p-3 mb-6">
      <div class="flex flex-col sm:flex-row sm:items-center gap-2">
        <div class="relative flex-1">
          <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted pointer-events-none" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/></svg>
          <input
            v-model="busca"
            type="search"
            autocomplete="off"
            placeholder="Buscar por nome ou e-mail..."
            class="input-base w-full pl-9 text-sm"
          />
        </div>
        <div class="flex gap-2">
          <select v-model="filtroRole" class="input-base text-sm w-full sm:w-36">
            <option value="">Todos os perfis</option>
            <option value="AdminMax">Super Admin</option>
            <option value="Admin">Consultor</option>
            <option value="Cliente">Cliente</option>
          </select>
          <select v-model="filtroEmpresaId" class="input-base text-sm w-full sm:w-44">
            <option value="">Todas as empresas</option>
            <option v-for="e in empresas" :key="e.id" :value="e.id">{{ e.nome }}</option>
          </select>
        </div>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="flex justify-center py-16">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2" style="border-color: var(--tl-primary);"></div>
    </div>

    <template v-else>
      <!-- Contagem -->
      <p class="text-xs text-muted mb-3">{{ usuariosFiltrados.length }} usuario(s) encontrado(s)</p>

      <!-- Desktop: tabela -->
      <div class="hidden lg:block card overflow-hidden">
        <table class="w-full">
          <thead>
            <tr style="border-bottom: 1px solid var(--tl-border); background: var(--tl-surface-alt, var(--tl-surface));">
              <th class="text-left py-3 px-4 text-xs font-medium text-muted">Nome</th>
              <th class="text-left py-3 px-4 text-xs font-medium text-muted">E-mail</th>
              <th class="text-left py-3 px-4 text-xs font-medium text-muted">Perfil</th>
              <th class="text-left py-3 px-4 text-xs font-medium text-muted">Empresa</th>
              <th class="text-center py-3 px-4 text-xs font-medium text-muted">Status</th>
              <th class="text-right py-3 px-4 text-xs font-medium text-muted">Acoes</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="u in usuariosPaginados"
              :key="u.id"
              style="border-bottom: 1px solid var(--tl-border);"
              class="transition-colors"
              :style="{ background: 'var(--tl-surface)' }"
              @mouseenter="$event.currentTarget.style.background = 'var(--tl-surface-alt, var(--tl-surface))'"
              @mouseleave="$event.currentTarget.style.background = 'var(--tl-surface)'"
            >
              <td class="py-3 px-4">
                <div class="flex items-center gap-2.5">
                  <div class="w-8 h-8 rounded-full flex items-center justify-center shrink-0 text-xs font-bold"
                    :style="avatarStyle(u.role)">
                    {{ getInitials(u.nome) }}
                  </div>
                  <span class="text-sm font-medium truncate max-w-[180px]" style="color: var(--tl-text);">{{ u.nome || '-' }}</span>
                </div>
              </td>
              <td class="py-3 px-4 text-sm text-muted truncate max-w-[220px]">{{ u.email }}</td>
              <td class="py-3 px-4">
                <span class="text-xs px-2 py-0.5 rounded-full font-medium" :class="badgeRole(u.role)">{{ labelRole(u.role) }}</span>
              </td>
              <td class="py-3 px-4 text-sm text-muted truncate max-w-[160px]">{{ u.empresaNome || '-' }}</td>
              <td class="py-3 px-4 text-center">
                <span class="text-xs px-2 py-0.5 rounded-full font-medium" :class="u.ativo ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'">
                  {{ u.ativo ? 'Ativo' : 'Inativo' }}
                </span>
              </td>
              <td class="py-3 px-4">
                <div class="flex items-center justify-end gap-2">
                  <button @click="abrirEdicao(u)" class="p-1.5 rounded-lg transition-colors hover:bg-black/5" title="Editar" style="color: var(--tl-primary);">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/></svg>
                  </button>
                  <button @click="abrirModalSenha(u)" class="p-1.5 rounded-lg transition-colors hover:bg-black/5" title="Alterar senha" style="color: var(--tl-text-muted);">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z"/></svg>
                  </button>
                  <ToggleSwitch
                    :model-value="!!u.ativo"
                    :loading="loadingAtivo[u.id]"
                    @change="(val) => onAtivoChange(u, val)"
                  />
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Mobile/Tablet: cards -->
      <div class="lg:hidden space-y-3">
        <div
          v-for="u in usuariosPaginados"
          :key="u.id"
          class="card p-4"
        >
          <div class="flex items-start justify-between gap-3">
            <div class="flex items-center gap-3 min-w-0">
              <div class="w-10 h-10 rounded-full flex items-center justify-center shrink-0 text-sm font-bold"
                :style="avatarStyle(u.role)">
                {{ getInitials(u.nome) }}
              </div>
              <div class="min-w-0">
                <p class="text-sm font-semibold truncate" style="color: var(--tl-text);">{{ u.nome || '-' }}</p>
                <p class="text-xs text-muted truncate">{{ u.email }}</p>
              </div>
            </div>
            <span class="text-xs px-2 py-0.5 rounded-full font-medium shrink-0" :class="u.ativo ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'">
              {{ u.ativo ? 'Ativo' : 'Inativo' }}
            </span>
          </div>

          <div class="flex flex-wrap items-center gap-x-4 gap-y-1 mt-3 text-xs text-muted">
            <span class="px-2 py-0.5 rounded-full font-medium" :class="badgeRole(u.role)">{{ labelRole(u.role) }}</span>
            <span v-if="u.empresaNome">{{ u.empresaNome }}</span>
          </div>

          <div class="flex items-center gap-2 mt-3 pt-3" style="border-top: 1px solid var(--tl-border);">
            <button @click="abrirEdicao(u)" class="flex-1 py-2 rounded-lg text-xs font-medium transition-colors" style="color: var(--tl-primary); background: var(--tl-surface-alt, var(--tl-surface));">
              Editar
            </button>
            <button @click="abrirModalSenha(u)" class="flex-1 py-2 rounded-lg text-xs font-medium transition-colors" style="color: var(--tl-text-muted); background: var(--tl-surface-alt, var(--tl-surface));">
              Alterar senha
            </button>
            <ToggleSwitch
              :model-value="!!u.ativo"
              :loading="loadingAtivo[u.id]"
              @change="(val) => onAtivoChange(u, val)"
            />
          </div>
        </div>
      </div>

      <!-- Paginacao -->
      <div v-if="totalPaginas > 1" class="flex items-center justify-center gap-2 mt-6">
        <button
          @click="pagina = pagina - 1"
          :disabled="pagina <= 1"
          class="px-3 py-1.5 rounded-lg text-sm font-medium transition-colors disabled:opacity-30"
          style="color: var(--tl-text-muted); border: 1px solid var(--tl-border);"
        >
          Anterior
        </button>
        <span class="text-sm text-muted px-2">{{ pagina }} / {{ totalPaginas }}</span>
        <button
          @click="pagina = pagina + 1"
          :disabled="pagina >= totalPaginas"
          class="px-3 py-1.5 rounded-lg text-sm font-medium transition-colors disabled:opacity-30"
          style="color: var(--tl-text-muted); border: 1px solid var(--tl-border);"
        >
          Proximo
        </button>
      </div>

      <p v-if="usuariosFiltrados.length === 0" class="text-muted text-sm mt-4 text-center py-8">Nenhum usuario encontrado.</p>
    </template>

    <!-- Modal Editar usuario -->
    <Teleport to="body">
      <div v-if="modal.visivel" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4" @click.self="fecharModal">
        <div class="card p-6 max-w-md w-full max-h-[90vh] overflow-y-auto">
          <h3 class="section-title mb-4">Editar usuario</h3>
          <form @submit.prevent="salvarEdicao" class="space-y-3">
            <div>
              <label class="block text-xs font-medium text-muted mb-1">Nome</label>
              <input v-model="modal.nome" type="text" required class="input-base w-full" />
            </div>
            <div>
              <label class="block text-xs font-medium text-muted mb-1">E-mail</label>
              <input v-model="modal.email" type="email" required class="input-base w-full" placeholder="exemplo@email.com" />
            </div>
            <div>
              <label class="block text-xs font-medium text-muted mb-1">Telefone / WhatsApp</label>
              <input v-model="modal.telefone" type="tel" class="input-base w-full" placeholder="(11) 99999-9999" />
            </div>
            <div>
              <label class="block text-xs font-medium text-muted mb-1">Perfil</label>
              <select v-model="modal.role" class="input-base w-full">
                <option value="AdminMax">Super Admin</option>
                <option value="Admin">Consultor</option>
                <option value="Cliente">Cliente</option>
              </select>
            </div>
            <div v-if="modal.role !== 'AdminMax'">
              <label class="block text-xs font-medium text-muted mb-1">Empresa</label>
              <select v-model="modal.empresaId" class="input-base w-full">
                <option :value="null">Nenhuma</option>
                <option v-for="e in empresas" :key="e.id" :value="e.id">{{ e.nome }}</option>
              </select>
            </div>
            <div class="flex items-center gap-2">
              <input v-model="modal.ativo" type="checkbox" id="modal-ativo" class="rounded" style="accent-color: var(--tl-primary);" />
              <label for="modal-ativo" class="text-sm" style="color: var(--tl-text);">Usuario ativo</label>
            </div>
            <p v-if="modal.erro" class="text-sm text-red-500">{{ modal.erro }}</p>
            <div class="flex gap-2 pt-2">
              <button type="submit" :disabled="modal.salvando" class="btn-primary text-sm">
                {{ modal.salvando ? 'Salvando...' : 'Salvar' }}
              </button>
              <button type="button" @click="fecharModal" class="px-4 py-2 rounded-lg text-sm" style="color: var(--tl-text-muted);">Cancelar</button>
            </div>
          </form>
        </div>
      </div>
    </Teleport>

    <!-- Modal Alterar senha -->
    <Teleport to="body">
      <div v-if="modalSenha.visivel" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4" @click.self="fecharModalSenha">
        <div class="card p-6 max-w-sm w-full">
          <h3 class="section-title mb-2">Alterar senha</h3>
          <p class="text-sm text-muted mb-4">{{ modalSenha.nome }} ({{ modalSenha.email }})</p>
          <form @submit.prevent="salvarSenha" class="space-y-3">
            <div>
              <label class="block text-xs font-medium text-muted mb-1">Nova senha (min. 6 caracteres)</label>
              <input v-model="modalSenha.novaSenha" type="password" required minlength="6" class="input-base w-full" placeholder="Nova senha" />
            </div>
            <div>
              <label class="block text-xs font-medium text-muted mb-1">Confirmar senha</label>
              <input v-model="modalSenha.confirmarSenha" type="password" required class="input-base w-full" placeholder="Repita a senha" />
            </div>
            <p v-if="modalSenha.erro" class="text-sm text-red-500">{{ modalSenha.erro }}</p>
            <div class="flex gap-2 pt-2">
              <button type="submit" :disabled="modalSenha.salvando" class="px-4 py-2 rounded-lg text-sm font-medium bg-amber-600 text-white hover:bg-amber-700 disabled:opacity-50">
                {{ modalSenha.salvando ? 'Salvando...' : 'Alterar senha' }}
              </button>
              <button type="button" @click="fecharModalSenha" class="px-4 py-2 rounded-lg text-sm" style="color: var(--tl-text-muted);">Cancelar</button>
            </div>
          </form>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import userApi from '../../api/userApi'
import empresaApi from '../../api/empresaApi'
import { useToast } from '../../composables/useToast'
import { useConfirm } from '../../composables/useConfirm'
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
const busca = ref('')
const pagina = ref(1)
const porPagina = 20

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
  if (busca.value.trim()) {
    const termo = busca.value.trim().toLowerCase()
    list = list.filter(u =>
      (u.nome && u.nome.toLowerCase().includes(termo)) ||
      (u.email && u.email.toLowerCase().includes(termo))
    )
  }
  return list
})

const totalPaginas = computed(() => Math.ceil(usuariosFiltrados.value.length / porPagina))

const usuariosPaginados = computed(() => {
  const inicio = (pagina.value - 1) * porPagina
  return usuariosFiltrados.value.slice(inicio, inicio + porPagina)
})

// Reset paginacao quando filtros mudam
watch([filtroRole, filtroEmpresaId, busca], () => { pagina.value = 1 })

function getInitials(nome) {
  if (!nome) return '?'
  return nome.split(' ').filter(Boolean).slice(0, 2).map(p => p[0].toUpperCase()).join('')
}

function avatarStyle(role) {
  const styles = {
    AdminMax: 'background: #f3e8ff; color: #7c3aed;',
    Admin: 'background: #dbeafe; color: #2563eb;',
    Cliente: 'background: #f1f5f9; color: #64748b;'
  }
  return styles[role] || styles.Cliente
}

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
    toast.error('Erro ao carregar usuarios.')
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
    toast.success('Usuario atualizado.')
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
      toast.success('Usuario ativado.')
    } else {
      await userApi.desativar(u.id)
      const idx = usuarios.value.findIndex(x => x.id === u.id)
      if (idx !== -1) usuarios.value[idx] = { ...usuarios.value[idx], ativo: false }
      toast.success('Usuario inativado.')
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
    m.erro = 'Senha deve ter no minimo 6 caracteres.'
    return
  }
  if (m.novaSenha !== m.confirmarSenha) {
    m.erro = 'As senhas nao coincidem.'
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

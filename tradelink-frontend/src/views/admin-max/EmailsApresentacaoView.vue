<template>
  <div>
    <!-- Header -->
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 mb-6">
      <div>
        <h2 class="page-title mb-1">Emails de Apresentação</h2>
        <p class="text-sm text-gray-500">Envie guias de onboarding personalizados por perfil</p>
      </div>
      <div class="flex gap-2">
        <button
          v-for="tab in tabs" :key="tab.key"
          @click="abaAtiva = tab.key"
          class="px-4 py-2 rounded-lg text-sm font-medium transition-colors"
          :class="abaAtiva === tab.key
            ? 'bg-indigo-600 text-white shadow-sm'
            : 'bg-gray-100 text-gray-600 hover:bg-gray-200'"
        >
          {{ tab.label }}
        </button>
      </div>
    </div>

    <!-- Stats Cards -->
    <div class="grid grid-cols-2 lg:grid-cols-4 gap-3 sm:gap-4 mb-6" v-if="!loadingUsuarios">
      <div class="card p-3 sm:p-4">
        <p class="text-xs font-medium text-gray-500 uppercase tracking-wide">Total</p>
        <p class="text-xl sm:text-2xl font-bold text-gray-900 mt-1">{{ statsTotal }}</p>
      </div>
      <div class="card p-3 sm:p-4">
        <p class="text-xs font-medium text-blue-600 uppercase tracking-wide">Consultores</p>
        <p class="text-xl sm:text-2xl font-bold text-gray-900 mt-1">{{ statsConsultores }}</p>
      </div>
      <div class="card p-3 sm:p-4">
        <p class="text-xs font-medium text-emerald-600 uppercase tracking-wide">Clientes</p>
        <p class="text-xl sm:text-2xl font-bold text-gray-900 mt-1">{{ statsClientes }}</p>
      </div>
      <div class="card p-3 sm:p-4">
        <p class="text-xs font-medium text-amber-600 uppercase tracking-wide">Auto-Gestão</p>
        <p class="text-xl sm:text-2xl font-bold text-gray-900 mt-1">{{ statsAutoGestao }}</p>
      </div>
    </div>

    <!-- TAB: ENVIAR -->
    <div v-if="abaAtiva === 'enviar'">
      <!-- Barra de ações -->
      <div class="card p-3 sm:p-4 mb-4">
        <div class="flex flex-col sm:flex-row gap-3 sm:items-center">
          <div class="flex gap-2 flex-1">
            <select v-model="filtroRole" class="input w-36 sm:w-40 flex-shrink-0">
              <option value="">Todos os perfis</option>
              <option value="Admin">Consultor</option>
              <option value="Cliente">Cliente</option>
            </select>
            <div class="relative flex-1 min-w-0">
              <input v-model="busca" type="text" placeholder="Buscar..." class="input w-full pl-9" />
              <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/></svg>
            </div>
          </div>
          <div class="flex items-center gap-2 sm:gap-3 justify-between sm:justify-end">
            <span class="text-xs sm:text-sm text-gray-500 whitespace-nowrap">{{ selecionados.length }} selecionado(s)</span>
            <button @click="selecionarTodos" class="btn btn-secondary whitespace-nowrap text-xs sm:text-sm px-2.5 sm:px-3 py-1.5">
              {{ todosVisiveisSelecionados ? 'Desmarcar' : 'Selecionar' }} todos
            </button>
            <button
              @click="enviar"
              :disabled="selecionados.length === 0 || enviando"
              class="btn btn-primary whitespace-nowrap text-xs sm:text-sm px-3 sm:px-4 py-1.5 sm:py-2"
            >
              <template v-if="enviando">
                <svg class="animate-spin -ml-1 mr-1.5 h-3.5 w-3.5 text-white inline" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/></svg>
                Enviando...
              </template>
              <template v-else>Enviar</template>
            </button>
          </div>
        </div>
      </div>

      <!-- Lista de Usuarios -->
      <div class="card overflow-hidden">
        <LoadingSpinner v-if="loadingUsuarios" size="sm" class="py-12" />
        <div v-else-if="usuariosFiltrados.length === 0" class="text-center py-12 text-gray-500">
          <svg class="w-12 h-12 mx-auto text-gray-300 mb-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z"/></svg>
          <p class="font-medium">Nenhum usuário encontrado</p>
          <p class="text-sm mt-1">Tente ajustar os filtros</p>
        </div>
        <template v-else>
          <!-- Desktop Table Header -->
          <div class="hidden md:grid grid-cols-[40px_1fr_100px_120px_80px] gap-4 px-5 py-3 bg-gray-50 border-b border-gray-200 text-xs font-semibold text-gray-500 uppercase tracking-wider">
            <div></div>
            <div>Usuário</div>
            <div>Perfil</div>
            <div>Template</div>
            <div class="text-right">Ação</div>
          </div>

          <!-- Desktop Rows -->
          <div
            v-for="u in usuariosPaginados" :key="u.id"
            class="hidden md:grid grid-cols-[40px_1fr_100px_120px_80px] gap-4 px-5 py-3 items-center border-b border-gray-100 transition-colors cursor-pointer"
            :class="selecionados.includes(u.id) ? 'bg-indigo-50/60' : 'hover:bg-gray-50'"
            @click="toggleSelecionado(u.id)"
          >
            <div>
              <input type="checkbox" :checked="selecionados.includes(u.id)" @click.stop @change="toggleSelecionado(u.id)" class="rounded border-gray-300 text-indigo-600 focus:ring-indigo-500" />
            </div>
            <div class="min-w-0">
              <p class="text-sm font-medium text-gray-900 truncate">{{ u.nome || '(sem nome)' }}</p>
              <p class="text-xs text-gray-500 truncate">{{ u.email }}</p>
            </div>
            <div>
              <span class="text-xs px-2 py-0.5 rounded-full" :class="badgeRole(u.role)">{{ labelRole(u.role) }}</span>
            </div>
            <div>
              <span class="text-xs px-2.5 py-1 rounded-full font-medium" :class="badgeTemplate(detectarTemplate(u))">{{ labelTemplate(detectarTemplate(u)) }}</span>
            </div>
            <div class="text-right">
              <button @click.stop="abrirPreview(u.id)" class="text-xs text-indigo-600 hover:text-indigo-800 font-semibold">Preview</button>
            </div>
          </div>

          <!-- Mobile Cards -->
          <div class="md:hidden divide-y divide-gray-100">
            <div
              v-for="u in usuariosPaginados" :key="'m-' + u.id"
              class="flex items-start gap-3 p-3 transition-colors cursor-pointer"
              :class="selecionados.includes(u.id) ? 'bg-indigo-50/60' : 'hover:bg-gray-50'"
              @click="toggleSelecionado(u.id)"
            >
              <input type="checkbox" :checked="selecionados.includes(u.id)" @click.stop @change="toggleSelecionado(u.id)" class="rounded border-gray-300 text-indigo-600 focus:ring-indigo-500 mt-1 flex-shrink-0" />
              <div class="flex-1 min-w-0">
                <p class="text-sm font-medium text-gray-900 truncate">{{ u.nome || '(sem nome)' }}</p>
                <p class="text-xs text-gray-500 truncate mb-1.5">{{ u.email }}</p>
                <div class="flex items-center gap-2 flex-wrap">
                  <span class="text-[11px] px-2 py-0.5 rounded-full" :class="badgeRole(u.role)">{{ labelRole(u.role) }}</span>
                  <span class="text-[11px] px-2 py-0.5 rounded-full font-medium" :class="badgeTemplate(detectarTemplate(u))">{{ labelTemplate(detectarTemplate(u)) }}</span>
                </div>
              </div>
              <button @click.stop="abrirPreview(u.id)" class="text-xs text-indigo-600 hover:text-indigo-800 font-semibold flex-shrink-0 mt-1">Preview</button>
            </div>
          </div>

          <!-- Paginação -->
          <div class="flex flex-col sm:flex-row items-center justify-between gap-2 px-4 sm:px-5 py-3 bg-gray-50 border-t border-gray-200">
            <p class="text-xs text-gray-500">
              {{ paginaInicio + 1 }}–{{ Math.min(paginaFim, usuariosFiltrados.length) }} de {{ usuariosFiltrados.length }}
            </p>
            <div class="flex items-center gap-1">
              <button @click="pagina = pagina - 1" :disabled="pagina <= 1" class="px-2.5 sm:px-3 py-1.5 text-xs font-medium rounded-md border border-gray-300 bg-white text-gray-700 hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed">
                Anterior
              </button>
              <template v-for="p in paginasVisiveis" :key="p">
                <button v-if="p === '...'" disabled class="px-2 py-1.5 text-xs text-gray-400">...</button>
                <button v-else @click="pagina = p" class="px-2.5 sm:px-3 py-1.5 text-xs font-medium rounded-md border transition-colors" :class="pagina === p ? 'bg-indigo-600 text-white border-indigo-600' : 'border-gray-300 bg-white text-gray-700 hover:bg-gray-50'">
                  {{ p }}
                </button>
              </template>
              <button @click="pagina = pagina + 1" :disabled="pagina >= totalPaginas" class="px-2.5 sm:px-3 py-1.5 text-xs font-medium rounded-md border border-gray-300 bg-white text-gray-700 hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed">
                Próxima
              </button>
            </div>
          </div>
        </template>
      </div>
    </div>

    <!-- TAB: HISTÓRICO -->
    <div v-if="abaAtiva === 'historico'">
      <div class="card overflow-hidden">
        <LoadingSpinner v-if="loadingHistorico" size="sm" class="py-12" />
        <div v-else-if="historico.length === 0" class="text-center py-12 text-gray-500">
          <svg class="w-12 h-12 mx-auto text-gray-300 mb-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"/></svg>
          <p class="font-medium">Nenhum email enviado ainda</p>
          <p class="text-sm mt-1">Selecione destinatários na aba "Enviar"</p>
        </div>
        <template v-else>
          <!-- Desktop Table Header -->
          <div class="hidden md:grid grid-cols-[1fr_120px_140px_90px] gap-4 px-5 py-3 bg-gray-50 border-b border-gray-200 text-xs font-semibold text-gray-500 uppercase tracking-wider">
            <div>Destinatário</div>
            <div>Template</div>
            <div>Enviado</div>
            <div class="text-right">Status</div>
          </div>
          <!-- Desktop Rows -->
          <div
            v-for="h in historicoPaginado" :key="h.id"
            class="hidden md:grid grid-cols-[1fr_120px_140px_90px] gap-4 px-5 py-3 items-center border-b border-gray-100 hover:bg-gray-50 transition-colors"
          >
            <div class="min-w-0">
              <p class="text-sm font-medium text-gray-900 truncate">{{ h.nomeDestinatario || '(sem nome)' }}</p>
              <p class="text-xs text-gray-500 truncate">{{ h.emailDestinatario }}</p>
            </div>
            <div>
              <span class="text-xs px-2.5 py-1 rounded-full font-medium" :class="badgeTemplate(h.tipoTemplate)">{{ labelTemplate(h.tipoTemplate) }}</span>
            </div>
            <div>
              <p class="text-xs text-gray-700">{{ formatData(h.enviadoEm) }}</p>
              <p class="text-xs text-gray-400">por {{ h.nomeEnviadoPor }}</p>
            </div>
            <div class="text-right">
              <span class="text-xs px-2 py-0.5 rounded-full font-medium inline-flex items-center gap-1" :class="h.status === 'ENVIADO' ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'">
                <span class="w-1.5 h-1.5 rounded-full" :class="h.status === 'ENVIADO' ? 'bg-green-500' : 'bg-red-500'"></span>
                {{ h.status === 'ENVIADO' ? 'Enviado' : 'Falha' }}
              </span>
              <p v-if="h.erro" class="text-xs text-red-400 mt-0.5 truncate max-w-[120px]" :title="h.erro">{{ h.erro }}</p>
            </div>
          </div>

          <!-- Mobile Cards -->
          <div class="md:hidden divide-y divide-gray-100">
            <div v-for="h in historicoPaginado" :key="'mh-' + h.id" class="p-3">
              <div class="flex items-start justify-between gap-2 mb-1.5">
                <div class="min-w-0 flex-1">
                  <p class="text-sm font-medium text-gray-900 truncate">{{ h.nomeDestinatario || '(sem nome)' }}</p>
                  <p class="text-xs text-gray-500 truncate">{{ h.emailDestinatario }}</p>
                </div>
                <span class="text-[11px] px-2 py-0.5 rounded-full font-medium inline-flex items-center gap-1 flex-shrink-0" :class="h.status === 'ENVIADO' ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'">
                  <span class="w-1.5 h-1.5 rounded-full" :class="h.status === 'ENVIADO' ? 'bg-green-500' : 'bg-red-500'"></span>
                  {{ h.status === 'ENVIADO' ? 'Enviado' : 'Falha' }}
                </span>
              </div>
              <div class="flex items-center gap-2 flex-wrap">
                <span class="text-[11px] px-2 py-0.5 rounded-full font-medium" :class="badgeTemplate(h.tipoTemplate)">{{ labelTemplate(h.tipoTemplate) }}</span>
                <span class="text-[11px] text-gray-400">{{ formatData(h.enviadoEm) }} por {{ h.nomeEnviadoPor }}</span>
              </div>
              <p v-if="h.erro" class="text-xs text-red-400 mt-1 truncate" :title="h.erro">{{ h.erro }}</p>
            </div>
          </div>

          <!-- Paginação Histórico -->
          <div class="flex flex-col sm:flex-row items-center justify-between gap-2 px-4 sm:px-5 py-3 bg-gray-50 border-t border-gray-200">
            <p class="text-xs text-gray-500">
              {{ histPaginaInicio + 1 }}–{{ Math.min(histPaginaFim, historico.length) }} de {{ historico.length }}
            </p>
            <div class="flex items-center gap-1">
              <button @click="histPagina = histPagina - 1" :disabled="histPagina <= 1" class="px-2.5 sm:px-3 py-1.5 text-xs font-medium rounded-md border border-gray-300 bg-white text-gray-700 hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed">Anterior</button>
              <template v-for="p in histPaginasVisiveis" :key="p">
                <button v-if="p === '...'" disabled class="px-2 py-1.5 text-xs text-gray-400">...</button>
                <button v-else @click="histPagina = p" class="px-2.5 sm:px-3 py-1.5 text-xs font-medium rounded-md border transition-colors" :class="histPagina === p ? 'bg-indigo-600 text-white border-indigo-600' : 'border-gray-300 bg-white text-gray-700 hover:bg-gray-50'">{{ p }}</button>
              </template>
              <button @click="histPagina = histPagina + 1" :disabled="histPagina >= histTotalPaginas" class="px-2.5 sm:px-3 py-1.5 text-xs font-medium rounded-md border border-gray-300 bg-white text-gray-700 hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed">Próxima</button>
            </div>
          </div>
        </template>
      </div>
    </div>

    <!-- Modal Preview -->
    <Teleport to="body">
      <div v-if="preview.visivel" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-2 sm:p-4" @click.self="fecharPreview">
        <div class="bg-white rounded-xl shadow-2xl w-full max-w-4xl max-h-[95vh] sm:max-h-[90vh] flex flex-col">
          <div class="flex items-center justify-between px-4 sm:px-5 py-3 sm:py-4 border-b border-gray-200">
            <h3 class="text-base sm:text-lg font-semibold text-gray-900">Preview do Email</h3>
            <button @click="fecharPreview" class="w-8 h-8 rounded-lg flex items-center justify-center text-gray-400 hover:bg-gray-100 hover:text-gray-600 transition-colors">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/></svg>
            </button>
          </div>
          <div class="flex-1 overflow-auto p-1.5 sm:p-2 bg-gray-100">
            <LoadingSpinner v-if="preview.loading" size="sm" class="py-12" />
            <iframe v-else :srcdoc="preview.html" class="w-full bg-white rounded-lg shadow-sm border-0" style="min-height: 400px; height: 70vh;"></iframe>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import emailApresentacaoApi from '../../api/emailApresentacaoApi'
import userApi from '../../api/userApi'
import { useToast } from '../../composables/useToast'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'

const toast = useToast()
const POR_PAGINA = 10

const tabs = [
  { key: 'enviar', label: 'Enviar' },
  { key: 'historico', label: 'Histórico' }
]

const abaAtiva = ref('enviar')
const loadingUsuarios = ref(true)
const loadingHistorico = ref(false)
const enviando = ref(false)
const usuarios = ref([])
const historico = ref([])
const selecionados = ref([])
const filtroRole = ref('')
const busca = ref('')
const pagina = ref(1)
const histPagina = ref(1)

const preview = ref({ visivel: false, loading: false, html: '' })

// --- Stats ---
const usersAtivos = computed(() => usuarios.value.filter(u => u.ativo !== false && u.role !== 'AdminMax'))
const statsTotal = computed(() => usersAtivos.value.length)
const statsConsultores = computed(() => usersAtivos.value.filter(u => u.role === 'Admin').length)
const statsClientes = computed(() => usersAtivos.value.filter(u => u.role === 'Cliente' && !u.autoGestao).length)
const statsAutoGestao = computed(() => usersAtivos.value.filter(u => u.role === 'Cliente' && u.autoGestao).length)

// --- Filtro ---
const usuariosFiltrados = computed(() => {
  let list = usersAtivos.value
  if (filtroRole.value) list = list.filter(u => u.role === filtroRole.value)
  if (busca.value) {
    const q = busca.value.toLowerCase()
    list = list.filter(u => (u.nome || '').toLowerCase().includes(q) || (u.email || '').toLowerCase().includes(q))
  }
  return list
})

const todosVisiveisSelecionados = computed(() => {
  return usuariosFiltrados.value.length > 0 && usuariosFiltrados.value.every(u => selecionados.value.includes(u.id))
})

// --- Paginação Enviar ---
const totalPaginas = computed(() => Math.max(1, Math.ceil(usuariosFiltrados.value.length / POR_PAGINA)))
const paginaInicio = computed(() => (pagina.value - 1) * POR_PAGINA)
const paginaFim = computed(() => paginaInicio.value + POR_PAGINA)
const usuariosPaginados = computed(() => usuariosFiltrados.value.slice(paginaInicio.value, paginaFim.value))
const paginasVisiveis = computed(() => gerarPaginas(pagina.value, totalPaginas.value))

// --- Paginação Histórico ---
const histTotalPaginas = computed(() => Math.max(1, Math.ceil(historico.value.length / POR_PAGINA)))
const histPaginaInicio = computed(() => (histPagina.value - 1) * POR_PAGINA)
const histPaginaFim = computed(() => histPaginaInicio.value + POR_PAGINA)
const historicoPaginado = computed(() => historico.value.slice(histPaginaInicio.value, histPaginaFim.value))
const histPaginasVisiveis = computed(() => gerarPaginas(histPagina.value, histTotalPaginas.value))

function gerarPaginas(atual, total) {
  if (total <= 7) return Array.from({ length: total }, (_, i) => i + 1)
  const pages = []
  pages.push(1)
  if (atual > 3) pages.push('...')
  for (let i = Math.max(2, atual - 1); i <= Math.min(total - 1, atual + 1); i++) pages.push(i)
  if (atual < total - 2) pages.push('...')
  pages.push(total)
  return pages
}

watch([filtroRole, busca], () => { pagina.value = 1 })

function labelRole(role) {
  const map = { AdminMax: 'Super Admin', Admin: 'Consultor', Cliente: 'Cliente' }
  return map[role] || role
}
function badgeRole(role) {
  const map = { Admin: 'bg-blue-100 text-blue-800', Cliente: 'bg-gray-100 text-gray-700' }
  return map[role] || 'bg-gray-100 text-gray-700'
}
function badgeTemplate(tipo) {
  const map = { CONSULTOR: 'bg-blue-100 text-blue-700', CLIENTE: 'bg-emerald-100 text-emerald-700', CLIENTE_AUTO_GESTAO: 'bg-amber-100 text-amber-700' }
  return map[tipo] || 'bg-gray-100 text-gray-700'
}
function labelTemplate(tipo) {
  const map = { CONSULTOR: 'Consultor', CLIENTE: 'Cliente', CLIENTE_AUTO_GESTAO: 'Auto-Gestão' }
  return map[tipo] || tipo
}

function detectarTemplate(u) {
  if (u.role === 'Admin') return 'CONSULTOR'
  if (u.autoGestao) return 'CLIENTE_AUTO_GESTAO'
  return 'CLIENTE'
}

function toggleSelecionado(id) {
  const idx = selecionados.value.indexOf(id)
  if (idx === -1) selecionados.value.push(id)
  else selecionados.value.splice(idx, 1)
}

function selecionarTodos() {
  if (todosVisiveisSelecionados.value) {
    const ids = usuariosFiltrados.value.map(u => u.id)
    selecionados.value = selecionados.value.filter(id => !ids.includes(id))
  } else {
    const ids = new Set(selecionados.value)
    usuariosFiltrados.value.forEach(u => ids.add(u.id))
    selecionados.value = [...ids]
  }
}

function formatData(dt) {
  if (!dt) return '-'
  const d = new Date(dt)
  return d.toLocaleDateString('pt-BR') + ' ' + d.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })
}

async function carregarUsuarios() {
  loadingUsuarios.value = true
  try {
    const res = await userApi.listarTodos()
    usuarios.value = res.data || []
  } catch (e) {
    toast.error('Erro ao carregar usuários.')
  } finally {
    loadingUsuarios.value = false
  }
}

async function carregarHistorico() {
  loadingHistorico.value = true
  try {
    const res = await emailApresentacaoApi.historico()
    historico.value = res.data || []
  } catch (e) {
    toast.error('Erro ao carregar histórico.')
  } finally {
    loadingHistorico.value = false
  }
}

async function enviar() {
  if (selecionados.value.length === 0) return
  enviando.value = true
  try {
    const res = await emailApresentacaoApi.enviar(selecionados.value)
    const resultados = res.data || []
    const enviados = resultados.filter(r => r.status === 'ENVIADO').length
    const falhas = resultados.filter(r => r.status === 'FALHA').length
    if (enviados > 0) toast.success(`${enviados} email(s) enviado(s) com sucesso!`)
    if (falhas > 0) toast.error(`${falhas} email(s) falharam.`)
    selecionados.value = []
    await carregarHistorico()
  } catch (e) {
    toast.error(e.response?.data?.mensagem || 'Erro ao enviar emails.')
  } finally {
    enviando.value = false
  }
}

async function abrirPreview(userId) {
  preview.value = { visivel: true, loading: true, html: '' }
  try {
    const res = await emailApresentacaoApi.preview(userId)
    preview.value.html = res.data.html
  } catch (e) {
    toast.error('Erro ao carregar preview.')
    preview.value.visivel = false
  } finally {
    preview.value.loading = false
  }
}

function fecharPreview() {
  preview.value.visivel = false
}

watch(abaAtiva, (tab) => {
  if (tab === 'historico' && historico.value.length === 0) carregarHistorico()
})

onMounted(() => carregarUsuarios())
</script>

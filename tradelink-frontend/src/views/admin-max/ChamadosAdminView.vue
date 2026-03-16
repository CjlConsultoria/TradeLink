<template>
  <div class="chamados-admin">
    <h2 class="page-title">Gerenciamento de Chamados</h2>

    <!-- Stats Cards -->
    <div class="stats-grid">
      <div
        v-for="s in statusList"
        :key="s.key"
        class="stat-card"
        :class="{ 'stat-card--active': filtroStatus === s.key }"
        @click="filtrarPorStatus(s.key)"
      >
        <div class="stat-card__icon" :style="{ background: s.bg }">{{ s.icon }}</div>
        <div class="stat-card__info">
          <span class="stat-card__count">{{ stats[s.key] || 0 }}</span>
          <span class="stat-card__label">{{ s.label }}</span>
        </div>
      </div>
    </div>

    <!-- Filtros -->
    <div class="filtros-bar">
      <button
        class="filtro-btn"
        :class="{ 'filtro-btn--active': !filtroStatus }"
        @click="filtrarPorStatus(null)"
      >Todos ({{ totalChamados }})</button>
      <button
        v-for="s in statusList"
        :key="s.key"
        class="filtro-btn"
        :class="{ 'filtro-btn--active': filtroStatus === s.key }"
        @click="filtrarPorStatus(s.key)"
      >{{ s.label }} ({{ stats[s.key] || 0 }})</button>
    </div>

    <LoadingSpinner v-if="loading" />

    <div v-else-if="chamados.length === 0" class="empty-state">
      <div class="empty-icon">🎫</div>
      <h3>Nenhum chamado{{ filtroStatus ? ' com este status' : '' }}</h3>
      <p v-if="filtroStatus">Tente outro filtro de status.</p>
    </div>

    <!-- Tabela -->
    <div v-else class="chamados-table-wrap">
      <table class="chamados-table">
        <thead>
          <tr>
            <th>Numero</th>
            <th>Assunto</th>
            <th>Autor</th>
            <th>Categoria</th>
            <th>Prioridade</th>
            <th>Status</th>
            <th>Atualizado</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="c in chamados" :key="c.id" @click="openChamado(c)" class="table-row">
            <td><span class="chamado-numero">{{ c.numero }}</span></td>
            <td>
              <span class="chamado-assunto">{{ c.assunto }}</span>
              <span v-if="c.naoLidas > 0" class="unread-dot">{{ c.naoLidas }}</span>
            </td>
            <td>
              <div class="autor-cell">
                <span class="autor-nome">{{ c.nomeUsuario }}</span>
                <span class="autor-role" :class="'role--' + c.roleUsuario.toLowerCase()">{{ roleLabel(c.roleUsuario) }}</span>
              </div>
            </td>
            <td><span class="badge-cat" :class="'badge-cat--' + c.categoria.toLowerCase()">{{ categoriaLabel(c.categoria) }}</span></td>
            <td><span class="badge-pri" :class="'badge-pri--' + c.prioridade.toLowerCase()">{{ prioridadeLabel(c.prioridade) }}</span></td>
            <td><span class="badge-status" :class="'badge--' + c.status.toLowerCase()">{{ statusLabel(c.status) }}</span></td>
            <td class="date-cell">{{ formatDate(c.updatedAt) }}</td>
            <td><button class="btn-view" @click.stop="openChamado(c)">Ver</button></td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Mobile: Card list -->
    <div v-if="!loading && chamados.length > 0" class="chamados-mobile-list">
      <div v-for="c in chamados" :key="'m-' + c.id" class="mobile-card" @click="openChamado(c)">
        <div class="mobile-card__top">
          <span class="chamado-numero">{{ c.numero }}</span>
          <span class="badge-status" :class="'badge--' + c.status.toLowerCase()">{{ statusLabel(c.status) }}</span>
        </div>
        <h4>{{ c.assunto }}</h4>
        <div class="mobile-card__meta">
          <span>{{ c.nomeUsuario }} ({{ roleLabel(c.roleUsuario) }})</span>
          <span class="badge-pri" :class="'badge-pri--' + c.prioridade.toLowerCase()">{{ prioridadeLabel(c.prioridade) }}</span>
        </div>
        <div class="mobile-card__footer">
          <span>{{ formatDate(c.updatedAt) }}</span>
          <span v-if="c.naoLidas > 0" class="unread-badge">{{ c.naoLidas }} nova(s)</span>
        </div>
      </div>
    </div>

    <!-- Modal: Detalhe -->
    <div v-if="selectedChamado" class="modal-overlay" @click.self="closeChamado">
      <div class="modal-content">
        <div class="modal-header">
          <div class="modal-header__left">
            <span class="chamado-numero">{{ selectedChamado.numero }}</span>
            <span class="badge-status" :class="'badge--' + selectedChamado.status.toLowerCase()">{{ statusLabel(selectedChamado.status) }}</span>
          </div>
          <button class="modal-close" @click="closeChamado">&times;</button>
        </div>

        <div class="detail-body">
          <h3 class="detail-title">{{ selectedChamado.assunto }}</h3>
          <div class="detail-meta">
            <span class="badge-cat" :class="'badge-cat--' + selectedChamado.categoria.toLowerCase()">{{ categoriaLabel(selectedChamado.categoria) }}</span>
            <span class="badge-pri" :class="'badge-pri--' + selectedChamado.prioridade.toLowerCase()">{{ prioridadeLabel(selectedChamado.prioridade) }}</span>
            <span class="detail-info">{{ selectedChamado.nomeUsuario }} &middot; {{ roleLabel(selectedChamado.roleUsuario) }}</span>
            <span class="detail-date">{{ formatDate(selectedChamado.createdAt) }}</span>
          </div>

          <div class="detail-description">
            <p>{{ selectedChamado.descricao }}</p>
          </div>

          <!-- Status Control -->
          <div class="status-control">
            <label>Alterar Status:</label>
            <select v-model="novoStatus">
              <option v-for="s in statusList" :key="s.key" :value="s.key">{{ s.label }}</option>
            </select>
            <button class="btn-primary btn-sm" :disabled="novoStatus === selectedChamado.status || salvandoStatus" @click="salvarStatus">
              {{ salvandoStatus ? 'Salvando...' : 'Atualizar' }}
            </button>
          </div>

          <!-- Respostas -->
          <div class="respostas-section">
            <h4>Respostas ({{ respostas.length }})</h4>
            <LoadingSpinner v-if="loadingRespostas" />
            <div v-else class="respostas-list" ref="respostasRef">
              <div v-if="respostas.length === 0" class="respostas-empty">
                <p>Nenhuma resposta ainda.</p>
              </div>
              <div
                v-for="r in respostas"
                :key="r.id"
                class="resposta-item"
                :class="{ 'resposta-item--admin': r.isAdmin, 'resposta-item--user': !r.isAdmin }"
              >
                <div class="resposta-header">
                  <span class="resposta-nome">{{ r.nomeRemetente }}</span>
                  <span v-if="r.isAdmin" class="resposta-admin-tag">Suporte</span>
                  <span v-else class="resposta-user-tag">{{ roleLabel(selectedChamado.roleUsuario) }}</span>
                  <span class="resposta-date">{{ formatDateTime(r.createdAt) }}</span>
                </div>
                <p class="resposta-conteudo">{{ r.conteudo }}</p>
              </div>
            </div>

            <div class="resposta-input">
              <textarea
                v-model="novaResposta"
                rows="3"
                placeholder="Responder ao chamado..."
                @keydown.enter.ctrl="enviarResposta"
              ></textarea>
              <button class="btn-primary" :disabled="!novaResposta.trim() || enviandoResposta" @click="enviarResposta">
                {{ enviandoResposta ? 'Enviando...' : 'Responder' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import chamadosApi from '../../api/chamadosApi'
import { useToast } from '../../composables/useToast'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'

const toast = useToast()

const chamados = ref([])
const stats = ref({})
const loading = ref(true)
const filtroStatus = ref(null)
const selectedChamado = ref(null)
const respostas = ref([])
const loadingRespostas = ref(false)
const novaResposta = ref('')
const enviandoResposta = ref(false)
const novoStatus = ref('')
const salvandoStatus = ref(false)
const respostasRef = ref(null)

const statusList = [
  { key: 'ABERTO', label: 'Aberto', icon: '📬', bg: 'rgba(59,130,246,0.1)' },
  { key: 'EM_ANALISE', label: 'Em Analise', icon: '🔍', bg: 'rgba(234,179,8,0.1)' },
  { key: 'EM_ANDAMENTO', label: 'Em Andamento', icon: '⚙️', bg: 'rgba(249,115,22,0.1)' },
  { key: 'PENDENTE', label: 'Pendente', icon: '⏳', bg: 'rgba(239,68,68,0.1)' },
  { key: 'RESOLVIDO', label: 'Resolvido', icon: '✅', bg: 'rgba(34,197,94,0.1)' },
  { key: 'FECHADO', label: 'Fechado', icon: '🔒', bg: 'rgba(100,116,139,0.1)' }
]

const totalChamados = computed(() => Object.values(stats.value).reduce((a, b) => a + b, 0))

function statusLabel(s) {
  const map = { ABERTO: 'Aberto', EM_ANALISE: 'Em Analise', EM_ANDAMENTO: 'Em Andamento', PENDENTE: 'Pendente', RESOLVIDO: 'Resolvido', FECHADO: 'Fechado' }
  return map[s] || s
}
function categoriaLabel(c) {
  return { BUG: 'Bug', MELHORIA: 'Melhoria', DUVIDA: 'Duvida', OUTRO: 'Outro' }[c] || c
}
function prioridadeLabel(p) {
  return { BAIXA: 'Baixa', MEDIA: 'Media', ALTA: 'Alta', CRITICA: 'Critica' }[p] || p
}
function roleLabel(r) {
  return { AdminMax: 'Admin', Admin: 'Consultor', Cliente: 'Cliente' }[r] || r
}
function formatDate(dt) {
  if (!dt) return ''
  return new Date(dt).toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric' })
}
function formatDateTime(dt) {
  if (!dt) return ''
  return new Date(dt).toLocaleString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' })
}

async function loadStats() {
  try { stats.value = (await chamadosApi.stats()).data } catch { /* ignore */ }
}

async function loadChamados() {
  loading.value = true
  try {
    chamados.value = (await chamadosApi.listarTodos(filtroStatus.value)).data
  } catch (e) {
    toast.error('Erro ao carregar chamados')
  } finally {
    loading.value = false
  }
}

function filtrarPorStatus(status) {
  filtroStatus.value = filtroStatus.value === status ? null : status
  loadChamados()
}

async function openChamado(c) {
  selectedChamado.value = c
  novoStatus.value = c.status
  loadingRespostas.value = true
  novaResposta.value = ''
  try {
    respostas.value = (await chamadosApi.listarRespostasAdmin(c.id)).data
    c.naoLidas = 0
    await nextTick()
    scrollToBottom()
  } catch {
    toast.error('Erro ao carregar respostas')
  } finally {
    loadingRespostas.value = false
  }
}

function closeChamado() {
  selectedChamado.value = null
  respostas.value = []
}

async function salvarStatus() {
  if (novoStatus.value === selectedChamado.value.status || salvandoStatus.value) return
  salvandoStatus.value = true
  try {
    const resp = await chamadosApi.atualizarStatus(selectedChamado.value.id, novoStatus.value)
    selectedChamado.value.status = resp.data.status
    toast.success('Status atualizado para ' + statusLabel(resp.data.status))
    loadStats()
    loadChamados()
  } catch (e) {
    toast.error('Erro ao atualizar status')
  } finally {
    salvandoStatus.value = false
  }
}

async function enviarResposta() {
  if (!novaResposta.value.trim() || enviandoResposta.value) return
  enviandoResposta.value = true
  try {
    const resp = await chamadosApi.responderAdmin(selectedChamado.value.id, novaResposta.value)
    respostas.value.push(resp.data)
    novaResposta.value = ''
    toast.success('Resposta enviada!')
    await nextTick()
    scrollToBottom()
  } catch {
    toast.error('Erro ao enviar resposta')
  } finally {
    enviandoResposta.value = false
  }
}

function scrollToBottom() {
  if (respostasRef.value) respostasRef.value.scrollTop = respostasRef.value.scrollHeight
}

onMounted(() => { loadStats(); loadChamados() })
</script>

<style scoped>
.chamados-admin { max-width: 1200px; margin: 0 auto; }
.page-title { font-size: 1.5rem; font-weight: 700; color: #1e293b; margin: 0 0 1.5rem; }

/* Stats */
.stats-grid { display: grid; grid-template-columns: repeat(6, 1fr); gap: 0.75rem; margin-bottom: 1.5rem; }
.stat-card {
  background: #fff; border: 1px solid #e2e8f0; border-radius: 0.75rem; padding: 1rem;
  display: flex; align-items: center; gap: 0.75rem; cursor: pointer; transition: border-color 0.2s, box-shadow 0.2s;
}
.stat-card:hover { border-color: #a5b4fc; }
.stat-card--active { border-color: #6366f1; box-shadow: 0 2px 8px rgba(99,102,241,0.15); }
.stat-card__icon { width: 2.5rem; height: 2.5rem; display: flex; align-items: center; justify-content: center; border-radius: 0.5rem; font-size: 1.125rem; flex-shrink: 0; }
.stat-card__info { display: flex; flex-direction: column; }
.stat-card__count { font-size: 1.25rem; font-weight: 800; color: #1e293b; }
.stat-card__label { font-size: 0.6875rem; color: #64748b; }

/* Filtros */
.filtros-bar { display: flex; gap: 0.5rem; margin-bottom: 1rem; flex-wrap: wrap; }
.filtro-btn {
  padding: 0.375rem 0.75rem; border: 1px solid #e2e8f0; border-radius: 1.5rem; background: #fff;
  font-size: 0.75rem; color: #475569; cursor: pointer; font-weight: 500; transition: all 0.2s;
}
.filtro-btn:hover { border-color: #a5b4fc; }
.filtro-btn--active { background: #6366f1; color: #fff; border-color: #6366f1; }

/* Table */
.chamados-table-wrap { background: #fff; border: 1px solid #e2e8f0; border-radius: 0.75rem; overflow: hidden; }
.chamados-table { width: 100%; border-collapse: collapse; }
.chamados-table th { text-align: left; padding: 0.75rem 1rem; font-size: 0.75rem; font-weight: 600; color: #64748b; background: #f8fafc; border-bottom: 1px solid #e2e8f0; white-space: nowrap; }
.chamados-table td { padding: 0.75rem 1rem; font-size: 0.8125rem; color: #334155; border-bottom: 1px solid #f1f5f9; }
.table-row { cursor: pointer; transition: background 0.15s; }
.table-row:hover { background: #f8fafc; }
.chamado-numero { font-family: monospace; font-weight: 700; color: #6366f1; font-size: 0.75rem; }
.chamado-assunto { font-weight: 600; }
.unread-dot { display: inline-block; min-width: 1.125rem; height: 1.125rem; line-height: 1.125rem; text-align: center; background: #6366f1; color: #fff; font-size: 0.625rem; font-weight: 700; border-radius: 50%; margin-left: 0.5rem; }
.autor-cell { display: flex; flex-direction: column; gap: 0.125rem; }
.autor-nome { font-weight: 600; font-size: 0.8125rem; }
.autor-role { font-size: 0.625rem; font-weight: 600; }
.role--cliente { color: #16a34a; }
.role--admin { color: #6366f1; }
.role--adminmax { color: #dc2626; }
.date-cell { white-space: nowrap; font-size: 0.75rem; color: #94a3b8; }
.btn-view { background: #f1f5f9; border: 1px solid #e2e8f0; padding: 0.25rem 0.625rem; border-radius: 0.375rem; font-size: 0.75rem; color: #6366f1; font-weight: 600; cursor: pointer; }
.btn-view:hover { background: #e0e7ff; }

/* Badges (shared) */
.badge-status, .badge-cat, .badge-pri { font-size: 0.6875rem; font-weight: 600; padding: 0.1875rem 0.5rem; border-radius: 1rem; display: inline-block; white-space: nowrap; }
.badge--aberto { background: rgba(59,130,246,0.1); color: #2563eb; }
.badge--em_analise { background: rgba(234,179,8,0.1); color: #ca8a04; }
.badge--em_andamento { background: rgba(249,115,22,0.1); color: #ea580c; }
.badge--pendente { background: rgba(239,68,68,0.1); color: #dc2626; }
.badge--resolvido { background: rgba(34,197,94,0.1); color: #16a34a; }
.badge--fechado { background: rgba(100,116,139,0.1); color: #64748b; }
.badge-cat--bug { background: rgba(239,68,68,0.1); color: #dc2626; }
.badge-cat--melhoria { background: rgba(34,197,94,0.1); color: #16a34a; }
.badge-cat--duvida { background: rgba(59,130,246,0.1); color: #2563eb; }
.badge-cat--outro { background: rgba(100,116,139,0.1); color: #64748b; }
.badge-pri--baixa { background: rgba(34,197,94,0.1); color: #16a34a; }
.badge-pri--media { background: rgba(234,179,8,0.1); color: #ca8a04; }
.badge-pri--alta { background: rgba(249,115,22,0.1); color: #ea580c; }
.badge-pri--critica { background: rgba(239,68,68,0.1); color: #dc2626; }

/* Mobile card list - hidden on desktop */
.chamados-mobile-list { display: none; }

/* Modal */
.modal-overlay { position: fixed; inset: 0; z-index: 50; display: flex; align-items: center; justify-content: center; background: rgba(0,0,0,0.5); padding: 1rem; }
.modal-content { background: #fff; border-radius: 1rem; width: 100%; max-width: 750px; max-height: 90vh; overflow-y: auto; box-shadow: 0 20px 60px rgba(0,0,0,0.15); }
.modal-header { display: flex; justify-content: space-between; align-items: center; padding: 1.25rem 1.5rem; border-bottom: 1px solid #e2e8f0; }
.modal-header__left { display: flex; align-items: center; gap: 0.75rem; }
.modal-close { background: none; border: none; font-size: 1.5rem; color: #94a3b8; cursor: pointer; }
.modal-close:hover { color: #475569; }

.detail-body { padding: 0 1.5rem 1.5rem; }
.detail-title { font-size: 1.125rem; color: #1e293b; margin: 1rem 0 0.5rem; }
.detail-meta { display: flex; gap: 0.5rem; align-items: center; flex-wrap: wrap; margin-bottom: 1rem; }
.detail-info { font-size: 0.75rem; color: #475569; font-weight: 500; }
.detail-date { font-size: 0.75rem; color: #94a3b8; }
.detail-description { padding: 1rem; background: #f8fafc; border-radius: 0.5rem; border: 1px solid #e2e8f0; margin-bottom: 1rem; }
.detail-description p { margin: 0; font-size: 0.875rem; color: #334155; line-height: 1.6; white-space: pre-wrap; }

/* Status control */
.status-control { display: flex; align-items: center; gap: 0.75rem; margin-bottom: 1rem; padding: 0.75rem 1rem; background: #f8fafc; border-radius: 0.5rem; border: 1px solid #e2e8f0; flex-wrap: wrap; }
.status-control label { font-size: 0.8125rem; font-weight: 600; color: #475569; white-space: nowrap; }
.status-control select { padding: 0.375rem 0.625rem; border: 1px solid #e2e8f0; border-radius: 0.375rem; font-size: 0.8125rem; color: #1e293b; flex: 1; min-width: 140px; }
.btn-primary { background: linear-gradient(135deg, #6366f1, #8b5cf6); color: #fff; border: none; padding: 0.625rem 1.25rem; border-radius: 0.5rem; font-weight: 600; cursor: pointer; font-size: 0.875rem; transition: opacity 0.2s; }
.btn-primary:hover { opacity: 0.9; }
.btn-primary:disabled { opacity: 0.5; cursor: not-allowed; }
.btn-sm { padding: 0.375rem 0.75rem; font-size: 0.75rem; }

/* Respostas */
.respostas-section h4 { font-size: 0.9375rem; color: #1e293b; margin: 0.75rem 0; }
.respostas-list { max-height: 300px; overflow-y: auto; display: flex; flex-direction: column; gap: 0.75rem; padding: 0.25rem; }
.respostas-empty { text-align: center; padding: 1.5rem; color: #94a3b8; font-size: 0.875rem; }
.resposta-item { padding: 0.75rem 1rem; border-radius: 0.75rem; max-width: 85%; }
.resposta-item--user { background: #f1f5f9; align-self: flex-start; border: 1px solid #e2e8f0; }
.resposta-item--admin { background: rgba(99,102,241,0.06); align-self: flex-end; border: 1px solid rgba(99,102,241,0.15); }
.resposta-header { display: flex; align-items: center; gap: 0.5rem; margin-bottom: 0.375rem; flex-wrap: wrap; }
.resposta-nome { font-size: 0.75rem; font-weight: 700; color: #1e293b; }
.resposta-admin-tag { font-size: 0.625rem; font-weight: 600; background: #6366f1; color: #fff; padding: 0.0625rem 0.375rem; border-radius: 0.25rem; }
.resposta-user-tag { font-size: 0.625rem; font-weight: 600; background: #e0e7ff; color: #6366f1; padding: 0.0625rem 0.375rem; border-radius: 0.25rem; }
.resposta-date { font-size: 0.6875rem; color: #94a3b8; }
.resposta-conteudo { margin: 0; font-size: 0.8125rem; color: #334155; line-height: 1.5; white-space: pre-wrap; }
.resposta-input { display: flex; gap: 0.5rem; margin-top: 1rem; align-items: flex-end; }
.resposta-input textarea { flex: 1; padding: 0.625rem 0.75rem; border: 1px solid #e2e8f0; border-radius: 0.5rem; font-size: 0.8125rem; font-family: inherit; resize: none; color: #1e293b; }
.resposta-input textarea:focus { outline: none; border-color: #6366f1; box-shadow: 0 0 0 3px rgba(99,102,241,0.1); }

.empty-state { text-align: center; padding: 3rem 1rem; }
.empty-icon { font-size: 3rem; margin-bottom: 1rem; }
.empty-state h3 { color: #1e293b; margin: 0 0 0.5rem; }
.empty-state p { color: #64748b; margin: 0; }

/* Responsive */
@media (max-width: 1024px) {
  .stats-grid { grid-template-columns: repeat(3, 1fr); }
}
@media (max-width: 768px) {
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
  .chamados-table-wrap { display: none; }
  .chamados-mobile-list { display: flex; flex-direction: column; gap: 0.75rem; }
  .mobile-card { background: #fff; border: 1px solid #e2e8f0; border-radius: 0.75rem; padding: 1rem; cursor: pointer; }
  .mobile-card:hover { border-color: #a5b4fc; }
  .mobile-card__top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.375rem; }
  .mobile-card h4 { margin: 0 0 0.5rem; font-size: 0.9375rem; color: #1e293b; }
  .mobile-card__meta { display: flex; justify-content: space-between; align-items: center; font-size: 0.75rem; color: #64748b; margin-bottom: 0.375rem; }
  .mobile-card__footer { display: flex; justify-content: space-between; align-items: center; font-size: 0.6875rem; color: #94a3b8; }
  .unread-badge { background: #6366f1; color: #fff; padding: 0.125rem 0.5rem; border-radius: 1rem; font-weight: 600; }
  .filtros-bar { overflow-x: auto; flex-wrap: nowrap; padding-bottom: 0.25rem; }
  .modal-content { max-width: 100%; max-height: 95vh; border-radius: 0.75rem; }
  .modal-overlay { padding: 0.5rem; }
  .resposta-item { max-width: 95%; }
}
@media (max-width: 480px) {
  .stats-grid { grid-template-columns: 1fr 1fr; gap: 0.5rem; }
  .stat-card { padding: 0.75rem; }
  .stat-card__icon { width: 2rem; height: 2rem; font-size: 1rem; }
  .stat-card__count { font-size: 1rem; }
  .status-control { flex-direction: column; align-items: stretch; }
}
</style>

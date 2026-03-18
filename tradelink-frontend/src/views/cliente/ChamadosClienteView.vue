<template>
  <div class="chamados-page">
    <div class="chamados-header">
      <h2 class="page-title">Meus Chamados</h2>
      <button class="btn-primary" @click="showForm = true">
        <span>+</span> Novo Chamado
      </button>
    </div>

    <LoadingSpinner v-if="loading" />

    <div v-else-if="erro" class="error-box">
      <p>{{ erro }}</p>
      <button class="btn-secondary" @click="loadChamados">Tentar novamente</button>
    </div>

    <template v-else>
      <!-- Lista de chamados -->
      <div v-if="chamados.length === 0" class="empty-state">
        <div class="empty-icon">🎫</div>
        <h3>Nenhum chamado</h3>
        <p>Abra um chamado para reportar bugs, sugerir melhorias ou tirar duvidas.</p>
        <button class="btn-primary" @click="showForm = true">Abrir Chamado</button>
      </div>

      <div v-else class="chamados-list">
        <div
          v-for="chamado in chamados"
          :key="chamado.id"
          class="chamado-card"
          :class="{ 'chamado-card--selected': selectedId === chamado.id }"
          @click="selectChamado(chamado)"
        >
          <div class="chamado-card__header">
            <span class="chamado-numero">{{ chamado.numero }}</span>
            <span class="chamado-badge" :class="'badge--' + chamado.status.toLowerCase()">
              {{ statusLabel(chamado.status) }}
            </span>
          </div>
          <h4 class="chamado-card__title">{{ chamado.assunto }}</h4>
          <div class="chamado-card__meta">
            <span class="chamado-badge badge--small" :class="'badge-cat--' + chamado.categoria.toLowerCase()">
              {{ categoriaLabel(chamado.categoria) }}
            </span>
            <span class="chamado-badge badge--small" :class="'badge-pri--' + chamado.prioridade.toLowerCase()">
              {{ prioridadeLabel(chamado.prioridade) }}
            </span>
          </div>
          <div class="chamado-card__footer">
            <span class="chamado-date">{{ formatDate(chamado.updatedAt) }}</span>
            <span v-if="chamado.naoLidas > 0" class="chamado-unread">{{ chamado.naoLidas }} nova(s)</span>
          </div>
          <p v-if="chamado.ultimaResposta" class="chamado-card__preview">{{ chamado.ultimaResposta }}</p>
        </div>
      </div>
    </template>

    <!-- Modal: Novo Chamado -->
    <div v-if="showForm" class="modal-overlay" @click.self="showForm = false">
      <div class="modal-content">
        <div class="modal-header">
          <h3>Novo Chamado</h3>
          <button class="modal-close" @click="showForm = false">&times;</button>
        </div>
        <form @submit.prevent="criarChamado" class="chamado-form">
          <div class="form-group">
            <label>Assunto *</label>
            <input v-model="form.assunto" type="text" required maxlength="200" placeholder="Descreva brevemente o problema" />
          </div>
          <div class="form-group">
            <label>Descricao *</label>
            <textarea v-model="form.descricao" required rows="5" placeholder="Detalhe o problema, passos para reproduzir, etc."></textarea>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>Categoria *</label>
              <select v-model="form.categoria" required>
                <option value="">Selecione</option>
                <option value="BUG">Bug</option>
                <option value="MELHORIA">Melhoria</option>
                <option value="DUVIDA">Duvida</option>
                <option value="OUTRO">Outro</option>
              </select>
            </div>
            <div class="form-group">
              <label>Prioridade *</label>
              <select v-model="form.prioridade" required>
                <option value="">Selecione</option>
                <option value="BAIXA">Baixa</option>
                <option value="MEDIA">Media</option>
                <option value="ALTA">Alta</option>
                <option value="CRITICA">Critica</option>
              </select>
            </div>
          </div>
          <div class="form-actions">
            <button type="button" class="btn-secondary" @click="showForm = false">Cancelar</button>
            <button type="submit" class="btn-primary" :disabled="submitting">
              {{ submitting ? 'Enviando...' : 'Abrir Chamado' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Modal: Detalhe do Chamado -->
    <div v-if="selectedChamado" class="modal-overlay" @click.self="closeChamado">
      <div class="modal-content modal-content--large">
        <div class="modal-header">
          <div>
            <span class="chamado-numero">{{ selectedChamado.numero }}</span>
            <span class="chamado-badge" :class="'badge--' + selectedChamado.status.toLowerCase()">
              {{ statusLabel(selectedChamado.status) }}
            </span>
          </div>
          <button class="modal-close" @click="closeChamado">&times;</button>
        </div>

        <h3 class="detail-title">{{ selectedChamado.assunto }}</h3>
        <div class="detail-meta">
          <span class="chamado-badge badge--small" :class="'badge-cat--' + selectedChamado.categoria.toLowerCase()">
            {{ categoriaLabel(selectedChamado.categoria) }}
          </span>
          <span class="chamado-badge badge--small" :class="'badge-pri--' + selectedChamado.prioridade.toLowerCase()">
            {{ prioridadeLabel(selectedChamado.prioridade) }}
          </span>
          <span class="detail-date">Aberto em {{ formatDate(selectedChamado.createdAt) }}</span>
        </div>

        <div class="detail-description">
          <p>{{ selectedChamado.descricao }}</p>
        </div>

        <!-- Respostas -->
        <div class="respostas-section">
          <h4>Respostas ({{ respostas.length }})</h4>
          <LoadingSpinner v-if="loadingRespostas" />
          <div v-else class="respostas-list" ref="respostasContainer">
            <div v-if="respostas.length === 0" class="respostas-empty">
              <p>Nenhuma resposta ainda. Aguarde o retorno da equipe.</p>
            </div>
            <div
              v-for="resp in respostas"
              :key="resp.id"
              class="resposta-item"
              :class="{ 'resposta-item--admin': resp.isAdmin, 'resposta-item--user': !resp.isAdmin }"
            >
              <div class="resposta-header">
                <span class="resposta-nome">{{ resp.nomeRemetente }}</span>
                <span v-if="resp.isAdmin" class="resposta-admin-tag">Suporte</span>
                <span class="resposta-date">{{ formatDateTime(resp.createdAt) }}</span>
              </div>
              <p class="resposta-conteudo">{{ resp.conteudo }}</p>
            </div>
          </div>

          <!-- Campo de resposta -->
          <div v-if="selectedChamado.status !== 'FECHADO'" class="resposta-input">
            <textarea
              v-model="novaResposta"
              rows="3"
              placeholder="Escreva uma resposta..."
              @keydown.enter.ctrl="enviarResposta"
            ></textarea>
            <button class="btn-primary" :disabled="!novaResposta.trim() || enviandoResposta" @click="enviarResposta">
              {{ enviandoResposta ? 'Enviando...' : 'Enviar' }}
            </button>
          </div>
          <div v-else class="chamado-fechado-msg">
            <p>Este chamado foi fechado.</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import chamadosApi from '../../api/chamadosApi'
import { useToast } from '../../composables/useToast'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'

const toast = useToast()

const chamados = ref([])
const loading = ref(true)
const erro = ref('')
const showForm = ref(false)
const submitting = ref(false)
const selectedId = ref(null)
const selectedChamado = ref(null)
const respostas = ref([])
const loadingRespostas = ref(false)
const novaResposta = ref('')
const enviandoResposta = ref(false)
const respostasContainer = ref(null)

const form = ref({
  assunto: '',
  descricao: '',
  categoria: '',
  prioridade: ''
})

function statusLabel(s) {
  const map = {
    ABERTO: 'Aberto', EM_ANALISE: 'Em Analise', EM_ANDAMENTO: 'Em Andamento',
    PENDENTE: 'Pendente', RESOLVIDO: 'Resolvido', FECHADO: 'Fechado'
  }
  return map[s] || s
}

function categoriaLabel(c) {
  const map = { BUG: 'Bug', MELHORIA: 'Melhoria', DUVIDA: 'Duvida', OUTRO: 'Outro' }
  return map[c] || c
}

function prioridadeLabel(p) {
  const map = { BAIXA: 'Baixa', MEDIA: 'Media', ALTA: 'Alta', CRITICA: 'Critica' }
  return map[p] || p
}

function formatDate(dt) {
  if (!dt) return ''
  return new Date(dt).toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric' })
}

function formatDateTime(dt) {
  if (!dt) return ''
  return new Date(dt).toLocaleString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' })
}

async function loadChamados() {
  loading.value = true
  erro.value = ''
  try {
    chamados.value = (await chamadosApi.listar()).data
  } catch (e) {
    erro.value = e.response?.data?.erro || e.response?.data?.message || 'Erro ao carregar chamados'
  } finally {
    loading.value = false
  }
}

async function criarChamado() {
  submitting.value = true
  try {
    const resp = await chamadosApi.criar(form.value)
    chamados.value.unshift(resp.data)
    toast.success('Chamado ' + resp.data.numero + ' criado com sucesso!')
    showForm.value = false
    form.value = { assunto: '', descricao: '', categoria: '', prioridade: '' }
  } catch (e) {
    toast.error(e.response?.data?.erro || e.response?.data?.message || 'Erro ao criar chamado')
  } finally {
    submitting.value = false
  }
}

async function selectChamado(chamado) {
  selectedId.value = chamado.id
  selectedChamado.value = chamado
  loadingRespostas.value = true
  novaResposta.value = ''
  try {
    respostas.value = (await chamadosApi.listarRespostas(chamado.id)).data
    // Atualizar nao lidas
    chamado.naoLidas = 0
    await nextTick()
    scrollToBottom()
  } catch (e) {
    toast.error('Erro ao carregar respostas')
  } finally {
    loadingRespostas.value = false
  }
}

function closeChamado() {
  selectedId.value = null
  selectedChamado.value = null
  respostas.value = []
}

async function enviarResposta() {
  if (!novaResposta.value.trim() || enviandoResposta.value) return
  enviandoResposta.value = true
  try {
    const resp = await chamadosApi.responder(selectedChamado.value.id, novaResposta.value)
    respostas.value.push(resp.data)
    novaResposta.value = ''
    toast.success('Resposta enviada!')
    await nextTick()
    scrollToBottom()
  } catch (e) {
    toast.error('Erro ao enviar resposta')
  } finally {
    enviandoResposta.value = false
  }
}

function scrollToBottom() {
  if (respostasContainer.value) {
    respostasContainer.value.scrollTop = respostasContainer.value.scrollHeight
  }
}

onMounted(() => loadChamados())
</script>

<style scoped>
.chamados-page { max-width: 900px; margin: 0 auto; }
.chamados-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; flex-wrap: wrap; gap: 1rem; }
.page-title { font-size: 1.5rem; font-weight: 700; color: #1e293b; margin: 0; }

.btn-primary {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #fff; border: none; padding: 0.625rem 1.25rem; border-radius: 0.5rem;
  font-weight: 600; cursor: pointer; font-size: 0.875rem; display: inline-flex; align-items: center; gap: 0.375rem;
  transition: opacity 0.2s;
}
.btn-primary:hover { opacity: 0.9; }
.btn-primary:disabled { opacity: 0.5; cursor: not-allowed; }

.btn-secondary {
  background: #fff; color: #475569; border: 1px solid #e2e8f0; padding: 0.625rem 1.25rem;
  border-radius: 0.5rem; font-weight: 600; cursor: pointer; font-size: 0.875rem;
}
.btn-secondary:hover { background: #f8fafc; }

.error-box { background: #fef2f2; border: 1px solid #fecaca; border-radius: 0.75rem; padding: 1.5rem; text-align: center; color: #dc2626; }

.empty-state { text-align: center; padding: 3rem 1rem; }
.empty-icon { font-size: 3rem; margin-bottom: 1rem; }
.empty-state h3 { color: #1e293b; margin: 0 0 0.5rem; }
.empty-state p { color: #64748b; margin: 0 0 1.5rem; }

/* Lista */
.chamados-list { display: flex; flex-direction: column; gap: 0.75rem; }

.chamado-card {
  background: #fff; border: 1px solid #e2e8f0; border-radius: 0.75rem; padding: 1rem 1.25rem;
  cursor: pointer; transition: border-color 0.2s, box-shadow 0.2s;
}
.chamado-card:hover { border-color: #a5b4fc; box-shadow: 0 2px 8px rgba(99,102,241,0.08); }
.chamado-card--selected { border-color: #6366f1; box-shadow: 0 2px 8px rgba(99,102,241,0.15); }

.chamado-card__header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.375rem; }
.chamado-numero { font-size: 0.75rem; font-weight: 700; color: #6366f1; font-family: monospace; }
.chamado-card__title { font-size: 0.9375rem; font-weight: 600; color: #1e293b; margin: 0 0 0.5rem; }
.chamado-card__meta { display: flex; gap: 0.5rem; flex-wrap: wrap; margin-bottom: 0.5rem; }
.chamado-card__footer { display: flex; justify-content: space-between; align-items: center; }
.chamado-date { font-size: 0.75rem; color: #94a3b8; }
.chamado-unread { font-size: 0.6875rem; font-weight: 600; color: #fff; background: #6366f1; padding: 0.125rem 0.5rem; border-radius: 1rem; }
.chamado-card__preview { font-size: 0.8125rem; color: #64748b; margin: 0.5rem 0 0; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

/* Badges */
.chamado-badge { font-size: 0.6875rem; font-weight: 600; padding: 0.1875rem 0.625rem; border-radius: 1rem; display: inline-block; }
.badge--small { font-size: 0.625rem; padding: 0.125rem 0.5rem; }
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

/* Modal */
.modal-overlay {
  position: fixed; inset: 0; z-index: 50; display: flex; align-items: center; justify-content: center;
  background: rgba(0,0,0,0.5); padding: 1rem;
}
.modal-content {
  background: #fff; border-radius: 1rem; width: 100%; max-width: 500px; max-height: 90vh; overflow-y: auto;
  box-shadow: 0 20px 60px rgba(0,0,0,0.15);
}
.modal-content--large { max-width: 700px; }
.modal-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 1.25rem 1.5rem; border-bottom: 1px solid #e2e8f0;
}
.modal-header h3 { margin: 0; font-size: 1.125rem; color: #1e293b; }
.modal-close {
  background: none; border: none; font-size: 1.5rem; color: #94a3b8; cursor: pointer; padding: 0; line-height: 1;
}
.modal-close:hover { color: #475569; }

/* Form */
.chamado-form { padding: 1.5rem; }
.form-group { margin-bottom: 1rem; }
.form-group label { display: block; font-size: 0.8125rem; font-weight: 600; color: #475569; margin-bottom: 0.375rem; }
.form-group input, .form-group textarea, .form-group select {
  width: 100%; padding: 0.625rem 0.75rem; border: 1px solid #e2e8f0; border-radius: 0.5rem;
  font-size: 0.875rem; color: #1e293b; background: #fff; transition: border-color 0.2s;
  font-family: inherit; box-sizing: border-box;
}
.form-group input:focus, .form-group textarea:focus, .form-group select:focus {
  outline: none; border-color: #6366f1; box-shadow: 0 0 0 3px rgba(99,102,241,0.1);
}
.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }
.form-actions { display: flex; justify-content: flex-end; gap: 0.75rem; margin-top: 1.5rem; }

/* Detail */
.detail-title { font-size: 1.125rem; color: #1e293b; margin: 0; padding: 1rem 1.5rem 0.5rem; }
.detail-meta { display: flex; gap: 0.5rem; align-items: center; flex-wrap: wrap; padding: 0 1.5rem 0.75rem; }
.detail-date { font-size: 0.75rem; color: #94a3b8; }
.detail-description {
  margin: 0 1.5rem; padding: 1rem; background: #f8fafc; border-radius: 0.5rem; border: 1px solid #e2e8f0;
}
.detail-description p { margin: 0; font-size: 0.875rem; color: #334155; line-height: 1.6; white-space: pre-wrap; }

/* Respostas */
.respostas-section { padding: 1rem 1.5rem 1.5rem; }
.respostas-section h4 { font-size: 0.9375rem; color: #1e293b; margin: 1rem 0 0.75rem; }
.respostas-list { max-height: 300px; overflow-y: auto; display: flex; flex-direction: column; gap: 0.75rem; padding: 0.25rem; }
.respostas-empty { text-align: center; padding: 1.5rem; color: #94a3b8; font-size: 0.875rem; }

.resposta-item { padding: 0.75rem 1rem; border-radius: 0.75rem; max-width: 85%; }
.resposta-item--user { background: #f1f5f9; align-self: flex-start; border: 1px solid #e2e8f0; }
.resposta-item--admin { background: rgba(99,102,241,0.06); align-self: flex-end; border: 1px solid rgba(99,102,241,0.15); }

.resposta-header { display: flex; align-items: center; gap: 0.5rem; margin-bottom: 0.375rem; flex-wrap: wrap; }
.resposta-nome { font-size: 0.75rem; font-weight: 700; color: #1e293b; }
.resposta-admin-tag { font-size: 0.625rem; font-weight: 600; background: #6366f1; color: #fff; padding: 0.0625rem 0.375rem; border-radius: 0.25rem; }
.resposta-date { font-size: 0.6875rem; color: #94a3b8; }
.resposta-conteudo { margin: 0; font-size: 0.8125rem; color: #334155; line-height: 1.5; white-space: pre-wrap; }

.resposta-input { display: flex; gap: 0.5rem; margin-top: 1rem; align-items: flex-end; }
.resposta-input textarea {
  flex: 1; padding: 0.625rem 0.75rem; border: 1px solid #e2e8f0; border-radius: 0.5rem;
  font-size: 0.8125rem; font-family: inherit; resize: none; color: #1e293b;
}
.resposta-input textarea:focus { outline: none; border-color: #6366f1; box-shadow: 0 0 0 3px rgba(99,102,241,0.1); }

.chamado-fechado-msg { text-align: center; padding: 1rem; color: #94a3b8; font-size: 0.875rem; background: #f8fafc; border-radius: 0.5rem; margin-top: 1rem; }

/* Responsive */
@media (max-width: 640px) {
  .chamados-header { flex-direction: column; align-items: stretch; }
  .chamados-header .btn-primary { text-align: center; justify-content: center; }
  .form-row { grid-template-columns: 1fr; }
  .modal-content, .modal-content--large { max-width: 100%; max-height: 95vh; border-radius: 0.75rem; }
  .modal-overlay { padding: 0.5rem; }
  .resposta-item { max-width: 95%; }
  .respostas-list { max-height: 250px; }
}
</style>

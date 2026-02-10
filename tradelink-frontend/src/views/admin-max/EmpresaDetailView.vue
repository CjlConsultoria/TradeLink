<template>
  <div>
    <div class="flex items-center gap-3 mb-6">
      <router-link to="/admin-max/empresas" class="text-gray-500 hover:text-gray-700">
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/></svg>
      </router-link>
      <h2 class="text-2xl font-bold text-gray-900">{{ empresa?.nome || 'Empresa' }}</h2>
    </div>

    <LoadingSpinner v-if="loading" />

    <template v-else-if="empresa">
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6 mb-6">
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          <div><p class="text-sm text-gray-500">CNPJ</p><p class="font-medium">{{ empresa.cnpj }}</p></div>
          <div><p class="text-sm text-gray-500">Consultores</p><p class="font-medium">{{ empresa.totalConsultores }}</p></div>
          <div><p class="text-sm text-gray-500">Clientes</p><p class="font-medium">{{ empresa.totalClientes }}</p></div>
          <div>
            <p class="text-sm text-gray-500">Pagamento</p>
            <span v-if="empresa.statusPagamento" class="inline-block mt-1 px-2 py-0.5 rounded-full text-xs font-medium" :class="badgeStatus(empresa.statusPagamento)">{{ labelStatus(empresa.statusPagamento) }}</span>
            <p v-if="empresa.currentPeriodEnd" class="text-xs text-gray-500 mt-0.5">Próx. venc.: {{ formatDate(empresa.currentPeriodEnd) }}</p>
          </div>
          <div>
            <p class="text-sm text-gray-500">Plano</p>
            <select v-model="selectedPlanoId" @change="atribuirPlano" class="mt-1 w-full px-3 py-2 border border-gray-300 rounded-lg text-sm">
              <option :value="undefined">Nenhum</option>
              <option v-for="p in planosAtivos" :key="p.id" :value="p.id">{{ p.nome }} ({{ p.maxUsuarios }} usuários)</option>
            </select>
            <p v-if="empresa.planoNome" class="text-xs text-gray-500 mt-1">{{ empresa.totalUsuarios ?? 0 }} / {{ empresa.maxUsuarios ?? 0 }} usuários</p>
          </div>
        </div>
      </div>

      <!-- Canais de notificação (habilitar/desabilitar por empresa) -->
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6 mb-6">
        <h3 class="text-lg font-semibold mb-4">Canais de notificação</h3>
        <p class="text-sm text-gray-500 mb-4">Escolha por quais canais esta empresa pode enviar notificações (nova recomendação, cliente resolveu, etc.).</p>
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          <label class="flex items-center gap-3 p-3 rounded-lg border border-gray-200 hover:bg-gray-50 cursor-pointer">
            <input type="checkbox" v-model="notifEmail" @change="salvarNotificacoes" class="rounded border-gray-300 text-indigo-600 focus:ring-indigo-500" />
            <span class="font-medium">E-mail</span>
          </label>
          <label class="flex items-center gap-3 p-3 rounded-lg border border-gray-200 hover:bg-gray-50 cursor-pointer">
            <input type="checkbox" v-model="notifTelegram" @change="salvarNotificacoes" class="rounded border-gray-300 text-indigo-600 focus:ring-indigo-500" />
            <span class="font-medium">Telegram</span>
          </label>
          <label class="flex items-center gap-3 p-3 rounded-lg border border-gray-200 hover:bg-gray-50 cursor-pointer">
            <input type="checkbox" v-model="notifPush" @change="salvarNotificacoes" class="rounded border-gray-300 text-indigo-600 focus:ring-indigo-500" />
            <span class="font-medium">Push (navegador)</span>
          </label>
          <label class="flex items-center gap-3 p-3 rounded-lg border border-gray-300 bg-gray-50 cursor-not-allowed opacity-80" title="Em breve">
            <input type="checkbox" disabled class="rounded border-gray-300" />
            <span class="font-medium text-gray-500">WhatsApp</span>
            <span class="text-xs text-gray-400">(em breve)</span>
          </label>
          <label class="flex items-center gap-3 p-3 rounded-lg border border-gray-200 hover:bg-gray-50 cursor-pointer">
            <input type="checkbox" v-model="notifSms" @change="salvarNotificacoes" class="rounded border-gray-300 text-indigo-600 focus:ring-indigo-500" />
            <span class="font-medium">SMS</span>
            <span class="text-xs text-gray-400">(provedor pago)</span>
          </label>
        </div>
        <p v-if="notifSalvando" class="text-sm text-gray-500 mt-2">Salvando...</p>
        <p v-if="notifSalvo" class="text-sm text-green-600 mt-2">Preferências salvas.</p>
      </div>

      <!-- Criar Consultor -->
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6 mb-6">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-lg font-semibold">Usuarios</h3>
          <button @click="showConsultorForm = true"
            class="bg-indigo-600 text-white px-3 py-1.5 rounded-lg text-sm font-medium hover:bg-indigo-700">
            Novo Consultor
          </button>
        </div>

        <div v-if="showConsultorForm" class="bg-gray-50 rounded-lg p-4 mb-4">
          <form @submit.prevent="criarConsultor" class="grid grid-cols-1 md:grid-cols-3 gap-3">
            <input v-model="consultorForm.nome" placeholder="Nome" required class="px-3 py-2 border border-gray-300 rounded-lg text-sm" />
            <input v-model="consultorForm.email" type="email" placeholder="E-mail" required class="px-3 py-2 border border-gray-300 rounded-lg text-sm" />
            <input v-model="consultorForm.senha" type="password" placeholder="Senha" required class="px-3 py-2 border border-gray-300 rounded-lg text-sm" />
            <div class="md:col-span-3 flex gap-2">
              <button type="submit" class="bg-indigo-600 text-white px-4 py-2 rounded-lg text-sm hover:bg-indigo-700">Criar</button>
              <button type="button" @click="showConsultorForm = false" class="text-gray-600 text-sm hover:underline">Cancelar</button>
            </div>
          </form>
          <p v-if="consultorError" class="text-red-500 text-sm mt-2">{{ consultorError }}</p>
        </div>

        <div class="overflow-x-auto">
          <table class="w-full text-sm table-responsive">
            <thead>
              <tr class="border-b border-gray-200">
                <th class="text-left py-3 px-2 font-medium text-gray-500">Nome</th>
                <th class="text-left py-3 px-2 font-medium text-gray-500">E-mail</th>
                <th class="text-center py-3 px-2 font-medium text-gray-500">Perfil</th>
                <th class="text-center py-3 px-2 font-medium text-gray-500">Status</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="u in usuarios" :key="u.id" class="border-b border-gray-100">
                <td class="py-3 px-2">{{ u.nome }}</td>
                <td class="py-3 px-2 text-gray-600">{{ u.email }}</td>
                <td class="py-3 px-2 text-center">
                  <span class="px-2 py-0.5 rounded-full text-xs" :class="u.role === 'Admin' ? 'bg-blue-100 text-blue-800' : 'bg-green-100 text-green-800'">
                    {{ u.role === 'Admin' ? 'Consultor' : 'Cliente' }}
                  </span>
                </td>
                <td class="py-3 px-2 text-center">
                  <span class="px-2 py-0.5 rounded-full text-xs" :class="u.ativo ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'">
                    {{ u.ativo ? 'Ativo' : 'Inativo' }}
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <EmptyState v-if="usuarios.length === 0" message="Nenhum usuario nesta empresa" />
      </div>

      <!-- Faturas: criar, editar, excluir, PDF e marcar como pago -->
      <div class="card p-6 mb-6">
        <div class="flex items-center justify-between mb-4 flex-wrap gap-2">
          <h3 class="section-title">Faturas</h3>
          <div class="flex gap-2">
            <button type="button" class="px-4 py-2 bg-indigo-600 text-white rounded-lg text-sm font-medium hover:bg-indigo-700" @click="abrirModalNovaFatura">Nova fatura</button>
            <button v-if="empresa.planoId" type="button" class="btn-primary" @click="showMarcarPago = true">Marcar como pago</button>
          </div>
        </div>
        <p v-if="faturasProxima?.proxima?.planoNome" class="text-sm text-gray-600 mb-2">Próxima: {{ formatDate(faturasProxima.proxima.dataVencimento) }} · {{ formatCurrency(faturasProxima.proxima.valor) }}</p>
        <LoadingSpinner v-if="loadingFaturas" />
        <div v-else class="overflow-x-auto">
          <table class="w-full text-sm table-responsive">
            <thead>
              <tr class="border-b border-gray-200">
                <th class="text-left py-2 px-2 font-medium text-gray-500">Vencimento</th>
                <th class="text-left py-2 px-2 font-medium text-gray-500">Pagamento</th>
                <th class="text-left py-2 px-2 font-medium text-gray-500">Valor</th>
                <th class="text-left py-2 px-2 font-medium text-gray-500">Status</th>
                <th class="text-left py-2 px-2 font-medium text-gray-500">Forma</th>
                <th class="text-left py-2 px-2 font-medium text-gray-500">Descrição</th>
                <th class="text-left py-2 px-2 font-medium text-gray-500">Ações</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="f in faturasProxima?.faturas || []" :key="f.id" class="border-b border-gray-100">
                <td class="py-2 px-2">{{ formatDate(f.dataVencimento) }}</td>
                <td class="py-2 px-2">{{ f.dataPagamento ? formatDate(f.dataPagamento) : '-' }}</td>
                <td class="py-2 px-2">{{ formatCurrency(f.valor) }}</td>
                <td class="py-2 px-2">{{ f.status }}</td>
                <td class="py-2 px-2">{{ f.formaPagamento || '-' }}</td>
                <td class="py-2 px-2 max-w-[180px] truncate" :title="f.descricaoServico || ''">{{ f.descricaoServico || '-' }}</td>
                <td class="py-2 px-2">
                  <button type="button" class="text-indigo-600 hover:underline text-xs mr-2" @click="abrirPdf(f.id)">PDF</button>
                  <button type="button" class="text-amber-600 hover:underline text-xs mr-2" @click="abrirModalEditarFatura(f)">Editar</button>
                  <button type="button" class="text-red-600 hover:underline text-xs" @click="confirmarExcluirFatura(f)">Excluir</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <EmptyState v-if="!loadingFaturas && (!faturasProxima?.faturas || faturasProxima.faturas.length === 0)" message="Nenhuma fatura. Use “Nova fatura” para criar." />
        <!-- Modal Nova fatura -->
        <div v-if="showNovaFatura" class="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div class="absolute inset-0 bg-black/50" @click="showNovaFatura = false"></div>
          <div class="card p-6 max-w-md w-full relative z-10">
            <h3 class="section-title">Nova fatura</h3>
            <form @submit.prevent="criarFatura" class="space-y-3">
              <div>
                <label class="block text-sm text-gray-600 mb-1">Vencimento</label>
                <input v-model="faturaForm.dataVencimento" type="date" required class="input-base w-full" />
              </div>
              <div>
                <label class="block text-sm text-gray-600 mb-1">Valor (R$)</label>
                <input v-model.number="faturaForm.valor" type="number" step="0.01" min="0.01" required class="input-base w-full" />
              </div>
              <div>
                <label class="block text-sm text-gray-600 mb-1">Descrição do serviço</label>
                <input v-model="faturaForm.descricaoServico" type="text" placeholder="Ex.: Assinatura mensal - Plano Premium" class="input-base w-full" />
              </div>
              <div>
                <label class="block text-sm text-gray-600 mb-1">Observação</label>
                <input v-model="faturaForm.observacao" type="text" class="input-base w-full" />
              </div>
              <div class="flex gap-2 pt-2">
                <button type="submit" class="btn-primary flex-1" :disabled="salvandoFatura">Criar</button>
                <button type="button" class="px-4 py-2 border rounded-lg text-sm" @click="showNovaFatura = false">Cancelar</button>
              </div>
            </form>
          </div>
        </div>
        <!-- Modal Editar fatura -->
        <div v-if="showEditarFatura" class="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div class="absolute inset-0 bg-black/50" @click="showEditarFatura = false"></div>
          <div class="card p-6 max-w-md w-full relative z-10 max-h-[90vh] overflow-y-auto">
            <h3 class="section-title">Editar fatura</h3>
            <form @submit.prevent="salvarEdicaoFatura" class="space-y-3">
              <div>
                <label class="block text-sm text-gray-600 mb-1">Status</label>
                <select v-model="editFaturaForm.status" class="input-base w-full">
                  <option value="PENDENTE">Pendente</option>
                  <option value="PAGA">Paga</option>
                  <option value="VENCIDA">Vencida</option>
                </select>
              </div>
              <div>
                <label class="block text-sm text-gray-600 mb-1">Vencimento</label>
                <input v-model="editFaturaForm.dataVencimento" type="date" class="input-base w-full" />
              </div>
              <div v-if="editFaturaForm.status === 'PAGA'">
                <label class="block text-sm text-gray-600 mb-1">Data pagamento</label>
                <input v-model="editFaturaForm.dataPagamento" type="date" class="input-base w-full" />
              </div>
              <div v-if="editFaturaForm.status === 'PAGA'">
                <label class="block text-sm text-gray-600 mb-1">Forma de pagamento</label>
                <select v-model="editFaturaForm.formaPagamento" class="input-base w-full">
                  <option value="">—</option>
                  <option value="PIX">PIX</option>
                  <option value="CARTAO">Cartão</option>
                  <option value="BOLETO">Boleto</option>
                  <option value="MANUAL">Manual</option>
                </select>
              </div>
              <div>
                <label class="block text-sm text-gray-600 mb-1">Valor (R$)</label>
                <input v-model.number="editFaturaForm.valor" type="number" step="0.01" min="0.01" class="input-base w-full" />
              </div>
              <div>
                <label class="block text-sm text-gray-600 mb-1">Descrição do serviço</label>
                <input v-model="editFaturaForm.descricaoServico" type="text" class="input-base w-full" />
              </div>
              <div>
                <label class="block text-sm text-gray-600 mb-1">Observação</label>
                <input v-model="editFaturaForm.observacao" type="text" class="input-base w-full" />
              </div>
              <div class="flex gap-2 pt-2">
                <button type="submit" class="btn-primary flex-1" :disabled="salvandoFatura">Salvar</button>
                <button type="button" class="px-4 py-2 border rounded-lg text-sm" @click="showEditarFatura = false">Cancelar</button>
              </div>
            </form>
          </div>
        </div>
        <!-- Modal Excluir fatura -->
        <div v-if="faturaAExcluir" class="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div class="absolute inset-0 bg-black/50" @click="faturaAExcluir = null"></div>
          <div class="card p-6 max-w-sm w-full relative z-10">
            <h3 class="section-title">Excluir fatura?</h3>
            <p class="text-sm text-gray-600 mb-4">Fatura #{{ faturaAExcluir.id }} · {{ formatCurrency(faturaAExcluir.valor) }}. Esta ação não pode ser desfeita.</p>
            <div class="flex gap-2">
              <button type="button" class="btn-primary flex-1 bg-red-600 hover:bg-red-700" :disabled="salvandoFatura" @click="excluirFatura">Excluir</button>
              <button type="button" class="px-4 py-2 border rounded-lg text-sm" @click="faturaAExcluir = null">Cancelar</button>
            </div>
          </div>
        </div>
        <!-- Modal marcar pago -->
        <div v-if="showMarcarPago" class="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div class="absolute inset-0 bg-black/50" @click="showMarcarPago = false"></div>
          <div class="card p-6 max-w-sm w-full relative z-10">
            <h3 class="section-title">Marcar como pago</h3>
            <p class="text-sm text-gray-500 mb-3">Registre que a empresa pagou por transferência, boleto ou outra forma.</p>
            <input v-model="marcarPagoObs" type="text" placeholder="Observação (opcional)" class="input-base mb-3" />
            <div class="flex gap-2">
              <button type="button" class="btn-primary flex-1" :disabled="salvandoPago" @click="marcarComoPago">Confirmar</button>
              <button type="button" class="px-4 py-2 border rounded-lg text-sm" @click="showMarcarPago = false">Cancelar</button>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useToast } from '../../composables/useToast'
import empresaApi from '../../api/empresaApi'
import planoApi from '../../api/planoApi'
import faturaApi from '../../api/faturaApi'
import { formatCurrency, formatDate } from '../../utils/formatters'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'
import EmptyState from '../../components/common/EmptyState.vue'

const toast = useToast()

const route = useRoute()
const empresa = ref(null)
const usuarios = ref([])
const planosAtivos = ref([])
const selectedPlanoId = ref(null)
const loading = ref(true)

const showConsultorForm = ref(false)
const consultorForm = ref({ nome: '', email: '', senha: '' })
const consultorError = ref('')

const notifEmail = ref(true)
const notifTelegram = ref(true)
const notifPush = ref(true)
const notifSms = ref(false)
const notifSalvando = ref(false)
const notifSalvo = ref(false)

const loadingFaturas = ref(false)
const faturasProxima = ref(null)
const showMarcarPago = ref(false)
const marcarPagoObs = ref('')
const salvandoPago = ref(false)

const showNovaFatura = ref(false)
const showEditarFatura = ref(false)
const salvandoFatura = ref(false)
const faturaForm = ref({ dataVencimento: '', valor: '', descricaoServico: '', observacao: '' })
const editFaturaForm = ref({ status: 'PENDENTE', dataVencimento: '', dataPagamento: '', formaPagamento: '', valor: '', descricaoServico: '', observacao: '' })
const editFaturaId = ref(null)
const faturaAExcluir = ref(null)

function labelStatus(s) {
  const map = { EM_DIA: 'Em dia', VENCIDO: 'Venceu', EM_ATRASO: 'Em atraso', SEM_ASSINATURA: 'Sem assinatura' }
  return map[s] || s
}
function badgeStatus(s) {
  const map = { EM_DIA: 'bg-green-100 text-green-800', VENCIDO: 'bg-amber-100 text-amber-800', EM_ATRASO: 'bg-red-100 text-red-800', SEM_ASSINATURA: 'bg-gray-100 text-gray-600' }
  return map[s] || 'bg-gray-100 text-gray-700'
}

watch(empresa, (e) => {
  if (e) {
    selectedPlanoId.value = e.planoId ?? undefined
    notifEmail.value = e.notificacaoEmail !== false
    notifTelegram.value = e.notificacaoTelegram !== false
    notifPush.value = e.notificacaoPush !== false
    notifSms.value = e.notificacaoSms === true
  }
}, { immediate: true })

async function salvarNotificacoes() {
  if (!empresa.value) return
  notifSalvo.value = false
  notifSalvando.value = true
  try {
    await empresaApi.atualizar(route.params.id, {
      nome: empresa.value.nome,
      cnpj: empresa.value.cnpj,
      planoId: empresa.value.planoId,
      notificacaoEmail: notifEmail.value,
      notificacaoTelegram: notifTelegram.value,
      notificacaoPush: notifPush.value,
      notificacaoWhatsApp: false,
      notificacaoSms: notifSms.value
    })
    empresa.value = { ...empresa.value, notificacaoEmail: notifEmail.value, notificacaoTelegram: notifTelegram.value, notificacaoPush: notifPush.value, notificacaoSms: notifSms.value }
    notifSalvo.value = true
    setTimeout(() => { notifSalvo.value = false }, 2000)
  } catch (e) {
    console.error(e)
  } finally {
    notifSalvando.value = false
  }
}

async function loadFaturas() {
  if (!route.params.id) return
  loadingFaturas.value = true
  try {
    const res = await faturaApi.listarPorEmpresa(route.params.id)
    faturasProxima.value = res.data
  } catch (_) {}
  finally {
    loadingFaturas.value = false
  }
}

async function loadData() {
  loading.value = true
  try {
    const [empRes, usrRes, planosRes] = await Promise.all([
      empresaApi.buscar(route.params.id),
      empresaApi.listarUsuarios(route.params.id),
      planoApi.listarAtivos()
    ])
    empresa.value = empRes.data
    usuarios.value = usrRes.data
    planosAtivos.value = planosRes.data || []
    selectedPlanoId.value = empRes.data?.planoId ?? undefined
    await loadFaturas()
  } finally {
    loading.value = false
  }
}

function isoToDateInput(iso) {
  if (!iso) return ''
  const d = new Date(iso)
  return d.toISOString().slice(0, 10)
}

function abrirModalNovaFatura() {
  faturaForm.value = { dataVencimento: '', valor: '', descricaoServico: '', observacao: '' }
  showNovaFatura.value = true
}

async function criarFatura() {
  salvandoFatura.value = true
  try {
    const data = {
      dataVencimento: faturaForm.value.dataVencimento ? new Date(faturaForm.value.dataVencimento + 'T12:00:00').toISOString() : null,
      valor: faturaForm.value.valor,
      descricaoServico: faturaForm.value.descricaoServico || null,
      observacao: faturaForm.value.observacao || null
    }
    await faturaApi.criarFatura(route.params.id, data)
    toast.success('Fatura criada.')
    showNovaFatura.value = false
    await loadFaturas()
  } catch (e) {
    toast.error(e.response?.data?.mensagem || 'Erro ao criar fatura.')
  } finally {
    salvandoFatura.value = false
  }
}

function abrirModalEditarFatura(f) {
  editFaturaId.value = f.id
  editFaturaForm.value = {
    status: f.status || 'PENDENTE',
    dataVencimento: isoToDateInput(f.dataVencimento),
    dataPagamento: isoToDateInput(f.dataPagamento),
    formaPagamento: f.formaPagamento || '',
    valor: f.valor,
    descricaoServico: f.descricaoServico || '',
    observacao: f.observacao || ''
  }
  showEditarFatura.value = true
}

async function salvarEdicaoFatura() {
  if (!editFaturaId.value) return
  salvandoFatura.value = true
  try {
    const f = editFaturaForm.value
    const data = {
      status: f.status || undefined,
      dataVencimento: f.dataVencimento ? new Date(f.dataVencimento + 'T12:00:00').toISOString() : undefined,
      dataPagamento: f.dataPagamento ? new Date(f.dataPagamento + 'T12:00:00').toISOString() : undefined,
      formaPagamento: f.formaPagamento || undefined,
      valor: f.valor,
      descricaoServico: f.descricaoServico || null,
      observacao: f.observacao || null
    }
    await faturaApi.atualizarFatura(route.params.id, editFaturaId.value, data)
    toast.success('Fatura atualizada.')
    showEditarFatura.value = false
    await loadFaturas()
  } catch (e) {
    toast.error(e.response?.data?.mensagem || 'Erro ao atualizar.')
  } finally {
    salvandoFatura.value = false
  }
}

function confirmarExcluirFatura(f) {
  faturaAExcluir.value = f
}

async function excluirFatura() {
  if (!faturaAExcluir.value) return
  salvandoFatura.value = true
  try {
    await faturaApi.excluirFatura(route.params.id, faturaAExcluir.value.id)
    toast.success('Fatura excluída.')
    faturaAExcluir.value = null
    await loadFaturas()
  } catch (e) {
    toast.error(e.response?.data?.mensagem || 'Erro ao excluir.')
  } finally {
    salvandoFatura.value = false
  }
}

async function abrirPdf(faturaId) {
  try {
    const res = await faturaApi.getPdfBlob(route.params.id, faturaId)
    const url = URL.createObjectURL(res.data)
    window.open(url, '_blank')
    setTimeout(() => URL.revokeObjectURL(url), 60000)
  } catch (e) {
    toast.error('Erro ao abrir PDF.')
  }
}

async function marcarComoPago() {
  salvandoPago.value = true
  try {
    await faturaApi.marcarPago(route.params.id, { observacao: marcarPagoObs.value || undefined })
    toast.success('Pagamento registrado.')
    showMarcarPago.value = false
    marcarPagoObs.value = ''
    await loadData()
  } catch (e) {
    toast.error(e.response?.data?.mensagem || 'Erro ao registrar.')
  } finally {
    salvandoPago.value = false
  }
}

async function atribuirPlano() {
  try {
    const planoId = selectedPlanoId.value === undefined || selectedPlanoId.value === null ? null : selectedPlanoId.value
    await empresaApi.atribuirPlano(route.params.id, planoId)
    const empRes = await empresaApi.buscar(route.params.id)
    empresa.value = empRes.data
  } catch (e) {
    console.error(e)
  }
}

async function criarConsultor() {
  consultorError.value = ''
  try {
    await empresaApi.criarConsultor(route.params.id, consultorForm.value)
    consultorForm.value = { nome: '', email: '', senha: '' }
    showConsultorForm.value = false
    loadData()
  } catch (e) {
    consultorError.value = e.response?.data?.erro || 'Erro ao criar consultor'
  }
}

onMounted(loadData)
</script>

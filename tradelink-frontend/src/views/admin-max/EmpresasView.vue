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
    <div v-if="showForm" class="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div class="fixed inset-0 bg-black/50" @click="closeForm"></div>
      <div class="bg-white rounded-lg shadow-xl p-6 max-w-2xl w-full max-h-[90vh] overflow-y-auto relative z-10">
        <h3 class="text-lg font-semibold mb-4">{{ editingId ? 'Editar' : 'Nova' }} Empresa</h3>
        <form @submit.prevent="salvar" class="space-y-4">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div class="md:col-span-2">
              <label class="block text-sm font-medium text-gray-700 mb-1">Nome da empresa *</label>
              <input v-model="form.nome" required class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500" placeholder="Razão social" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">CNPJ *</label>
              <input
                :value="form.cnpj"
                required
                class="w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-indigo-500"
                :class="cnpjCompleto ? (cnpjInvalido ? 'border-red-500' : 'border-green-600') : 'border-gray-300'"
                placeholder="00.000.000/0001-00"
                maxlength="18"
                @input="form.cnpj = maskCnpj($event.target.value)"
              />
              <p v-if="cnpjCompleto && cnpjInvalido" class="text-red-500 text-xs mt-0.5">CNPJ inválido. Verifique os dígitos.</p>
              <p v-else-if="cnpjCompleto && !cnpjInvalido" class="text-green-600 text-xs mt-0.5">CNPJ válido.</p>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Plano</label>
              <select v-model="form.planoId" class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500">
                <option :value="null">Nenhum</option>
                <option v-for="p in planosAtivos" :key="p.id" :value="p.id">{{ p.nome }}</option>
              </select>
            </div>
          </div>

          <div class="border-t border-gray-200 pt-4">
            <h4 class="text-sm font-medium text-gray-800 mb-3">Endereço</h4>
            <div class="grid grid-cols-1 md:grid-cols-3 gap-3">
              <div>
                <label class="block text-sm text-gray-600 mb-1">CEP</label>
                <div class="relative">
                  <input
                    :value="form.cep"
                    class="w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-indigo-500"
                    :class="cepCompleto ? 'border-green-600' : 'border-gray-300'"
                    placeholder="00000-000"
                    maxlength="9"
                    @input="form.cep = maskCep($event.target.value)"
                    @blur="buscarCep"
                  />
                  <span v-if="loadingCep" class="absolute right-3 top-1/2 -translate-y-1/2 inline-block h-4 w-4 animate-spin rounded-full border-2 border-indigo-500 border-t-transparent" aria-hidden="true" />
                </div>
                <p v-if="cepCompleto" class="text-green-600 text-xs mt-0.5">CEP válido.</p>
                <p v-else class="text-xs text-gray-500 mt-0.5">Digite o CEP e saia do campo para preencher automaticamente (ViaCEP).</p>
              </div>
              <div class="md:col-span-2">
                <label class="block text-sm text-gray-600 mb-1">Logradouro</label>
                <input v-model="form.logradouro" class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500" placeholder="Rua, avenida..." />
              </div>
              <div>
                <label class="block text-sm text-gray-600 mb-1">Número</label>
                <input v-model="form.numero" class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500" placeholder="Nº" />
              </div>
              <div class="md:col-span-2">
                <label class="block text-sm text-gray-600 mb-1">Complemento</label>
                <input v-model="form.complemento" class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500" placeholder="Sala, andar..." />
              </div>
              <div>
                <label class="block text-sm text-gray-600 mb-1">Bairro</label>
                <input v-model="form.bairro" class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500" />
              </div>
              <div>
                <label class="block text-sm text-gray-600 mb-1">Cidade</label>
                <input v-model="form.cidade" class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500" />
              </div>
              <div>
                <label class="block text-sm text-gray-600 mb-1">UF</label>
                <input v-model="form.uf" class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 uppercase" placeholder="SP" maxlength="2" />
              </div>
            </div>
          </div>

          <div class="border-t border-gray-200 pt-4">
            <h4 class="text-sm font-medium text-gray-800 mb-3">Responsável e contato</h4>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
              <div>
                <label class="block text-sm text-gray-600 mb-1">Nome do responsável</label>
                <input v-model="form.nomeResponsavel" class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500" placeholder="Nome completo" />
              </div>
              <div>
                <label class="block text-sm text-gray-600 mb-1">CPF do responsável</label>
                <input
                  :value="form.cpfResponsavel"
                  class="w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-indigo-500"
                  :class="cpfCompleto ? (cpfInvalido ? 'border-red-500' : 'border-green-600') : 'border-gray-300'"
                  placeholder="000.000.000-00"
                  maxlength="14"
                  @input="form.cpfResponsavel = maskCpf($event.target.value)"
                />
                <p v-if="cpfCompleto && cpfInvalido" class="text-red-500 text-xs mt-0.5">CPF inválido.</p>
                <p v-else-if="cpfCompleto && !cpfInvalido" class="text-green-600 text-xs mt-0.5">CPF válido.</p>
              </div>
              <div>
                <label class="block text-sm text-gray-600 mb-1">E-mail alternativo</label>
                <input v-model="form.emailAlternativo" type="email" class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500" placeholder="contato@empresa.com" />
              </div>
              <div>
                <label class="block text-sm text-gray-600 mb-1">Telefone / WhatsApp</label>
                <input v-model="form.telefone" class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500" placeholder="(11) 99999-9999" />
              </div>
            </div>
          </div>

          <p v-if="formError" class="text-red-500 text-sm">{{ formError }}</p>
          <div class="flex justify-end gap-3 pt-2">
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
        <p class="text-sm text-gray-500 mb-3">{{ formatarCnpj(e.cnpj) }}</p>
        <div v-if="e.planoNome" class="mb-2">
          <p class="text-xs text-gray-500">Plano: {{ e.planoNome }}</p>
          <span class="ml-2 text-xs px-2 py-0.5 rounded-full" :class="badgeAssinatura(e.subscriptionStatus || 'NONE')">
            {{ labelAssinatura(e.subscriptionStatus || 'NONE') }}
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
        <div class="flex flex-wrap items-center gap-3">
          <router-link :to="`/admin-max/empresas/${e.id}`" class="text-sm text-indigo-600 hover:underline">Detalhes</router-link>
          <button v-if="planoComPagamento(e)" type="button" @click="irParaCheckout(e)" class="text-sm text-green-600 hover:underline">Pagar plano</button>
          <ToggleSwitch
            :model-value="!!e.acessoBloqueadoPorAdmin"
            label-on="Acesso bloqueado"
            label-off="Acesso liberado"
            variant="warning"
            :loading="loadingBloqueio[e.id]"
            @change="(val) => onBloqueioChange(e, val)"
          />
          <button @click="editar(e)" class="text-sm text-blue-600 hover:underline">Editar</button>
          <ToggleSwitch
            :model-value="!!e.ativo"
            label-on="Ativa"
            label-off="Inativa"
            variant="success"
            :loading="loadingAtivo[e.id]"
            @change="(val) => onAtivoChange(e, val)"
          />
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
import ToggleSwitch from '../../components/common/ToggleSwitch.vue'
import { formatDate } from '../../utils/formatters'
import { isValidCnpj, isValidCpf, formatarCnpj, formatarCep, formatarCpf, apenasDigitos, maskCep, maskCnpj, maskCpf } from '../../utils/validadores'

const toast = useToast()
const empresaStore = useEmpresaStore()
const empresas = computed(() => empresaStore.empresas)
const loading = computed(() => empresaStore.loading)
const listarError = computed(() => empresaStore.listarError)
const planosAtivos = ref([])
const pagamentoConfigurado = ref(false)

const showForm = ref(false)
const editingId = ref(null)
const form = ref(getFormInicial())
const formError = ref('')
const loadingCep = ref(false)
const loadingBloqueio = ref({})
const loadingAtivo = ref({})

function getFormInicial() {
  return {
    nome: '',
    cnpj: '',
    planoId: null,
    cep: '',
    logradouro: '',
    numero: '',
    complemento: '',
    bairro: '',
    cidade: '',
    uf: '',
    nomeResponsavel: '',
    cpfResponsavel: '',
    emailAlternativo: '',
    telefone: '',
    notificacaoEmail: true,
    notificacaoTelegram: true,
    notificacaoPush: true,
    notificacaoWhatsApp: false,
    notificacaoSms: false
  }
}

const cnpjCompleto = computed(() => apenasDigitos(form.value.cnpj).length === 14)
const cnpjInvalido = computed(() => {
  if (!cnpjCompleto.value) return false
  return !isValidCnpj(form.value.cnpj)
})
const cpfCompleto = computed(() => apenasDigitos(form.value.cpfResponsavel).length === 11)
const cpfInvalido = computed(() => {
  if (!cpfCompleto.value) return false
  return !isValidCpf(form.value.cpfResponsavel)
})
const cepCompleto = computed(() => apenasDigitos(form.value.cep).length === 8)

function closeForm() {
  showForm.value = false
  editingId.value = null
  form.value = getFormInicial()
  formError.value = ''
}

function editar(empresa) {
  editingId.value = empresa.id
  const cnpjRaw = empresa.cnpj ?? ''
  const cepRaw = empresa.cep ?? ''
  const cpfRaw = empresa.cpfResponsavel ?? ''
  form.value = {
    nome: empresa.nome ?? '',
    cnpj: apenasDigitos(cnpjRaw).length === 14 ? formatarCnpj(cnpjRaw) : cnpjRaw,
    planoId: empresa.planoId ?? null,
    cep: apenasDigitos(cepRaw).length === 8 ? formatarCep(cepRaw) : cepRaw,
    logradouro: empresa.logradouro ?? '',
    numero: empresa.numero ?? '',
    complemento: empresa.complemento ?? '',
    bairro: empresa.bairro ?? '',
    cidade: empresa.cidade ?? '',
    uf: empresa.uf ?? '',
    nomeResponsavel: empresa.nomeResponsavel ?? '',
    cpfResponsavel: apenasDigitos(cpfRaw).length === 11 ? formatarCpf(cpfRaw) : cpfRaw,
    emailAlternativo: empresa.emailAlternativo ?? '',
    telefone: empresa.telefone ?? '',
    notificacaoEmail: empresa.notificacaoEmail !== false,
    notificacaoTelegram: empresa.notificacaoTelegram !== false,
    notificacaoPush: empresa.notificacaoPush !== false,
    notificacaoWhatsApp: empresa.notificacaoWhatsApp === true,
    notificacaoSms: empresa.notificacaoSms === true
  }
  showForm.value = true
}

async function buscarCep() {
  const cep = apenasDigitos(form.value.cep)
  if (cep.length !== 8) return
  loadingCep.value = true
  formError.value = ''
  try {
    const res = await fetch(`https://viacep.com.br/ws/${cep}/json/`)
    const data = await res.json()
    if (data.erro) {
      toast.error('CEP não encontrado.')
      return
    }
    form.value.logradouro = data.logradouro || form.value.logradouro
    form.value.bairro = data.bairro || form.value.bairro
    form.value.cidade = data.localidade || form.value.cidade
    form.value.uf = data.uf || form.value.uf
    if (data.cep) form.value.cep = data.cep
    toast.success('Endereço preenchido pelo CEP.')
  } catch (e) {
    toast.error('Não foi possível buscar o CEP. Tente novamente.')
  } finally {
    loadingCep.value = false
  }
}

async function salvar() {
  formError.value = ''
  if (cnpjInvalido.value) {
    formError.value = 'CNPJ inválido. Verifique os dígitos.'
    toast.error(formError.value)
    return
  }
  if (cpfInvalido.value) {
    formError.value = 'CPF do responsável inválido.'
    toast.error(formError.value)
    return
  }
  const eraEdicao = !!editingId.value
  try {
    const payload = { ...form.value }
    if (payload.uf) payload.uf = payload.uf.toUpperCase().slice(0, 2)
    if (editingId.value) {
      await empresaApi.atualizar(editingId.value, payload)
    } else {
      await empresaApi.criar(payload)
    }
    closeForm()
    empresaStore.listar()
    toast.success(eraEdicao ? 'Empresa atualizada.' : 'Empresa criada.')
  } catch (e) {
    formError.value = e.response?.data?.erro || 'Erro ao salvar'
    toast.error(formError.value)
  }
}

async function onBloqueioChange(empresa, bloqueado) {
  const msg = bloqueado ? 'Bloquear acesso à plataforma para todos os consultores e clientes desta empresa?' : 'Desbloquear o acesso à plataforma?'
  if (!confirm(msg)) return
  loadingBloqueio.value[empresa.id] = true
  try {
    await empresaApi.bloquearAcesso(empresa.id, bloqueado)
    empresaStore.listar()
    toast.success(bloqueado ? 'Acesso bloqueado.' : 'Acesso desbloqueado.')
  } catch (e) {
    toast.error(e.response?.data?.mensagem || e.response?.data?.erro || 'Erro ao alterar bloqueio.')
  } finally {
    loadingBloqueio.value[empresa.id] = false
  }
}

async function onAtivoChange(empresa, ativo) {
  const msg = ativo ? 'Reativar esta empresa?' : 'Desativar esta empresa?'
  if (!confirm(msg)) return
  loadingAtivo.value[empresa.id] = true
  try {
    if (ativo) {
      await empresaApi.ativar(empresa.id)
      toast.success('Empresa reativada.')
    } else {
      await empresaApi.desativar(empresa.id)
      toast.success('Empresa desativada.')
    }
    empresaStore.listar()
  } catch (e) {
    toast.error(e.response?.data?.mensagem || e.response?.data?.erro || 'Erro ao alterar status.')
  } finally {
    loadingAtivo.value[empresa.id] = false
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
  const map = {
    NONE: 'Sem assinatura',
    ACTIVE: 'Ativo',
    PAST_DUE: 'Em atraso',
    CANCELLED: 'Cancelado',
    TRIAL: 'Trial',
    EM_DIA: 'Em dia',
    VENCIDO: 'Venceu',
    EM_ATRASO: 'Em atraso',
    SEM_ASSINATURA: 'Sem assinatura'
  }
  return map[status] || status || 'Sem assinatura'
}

function badgeStatusPagamento(status) {
  const map = {
    NONE: 'bg-gray-100 text-gray-600',
    ACTIVE: 'bg-green-100 text-green-800',
    PAST_DUE: 'bg-red-100 text-red-800',
    CANCELLED: 'bg-gray-100 text-gray-600',
    TRIAL: 'bg-blue-100 text-blue-800',
    EM_DIA: 'bg-green-100 text-green-800',
    VENCIDO: 'bg-amber-100 text-amber-800',
    EM_ATRASO: 'bg-red-100 text-red-800',
    SEM_ASSINATURA: 'bg-gray-100 text-gray-600'
  }
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

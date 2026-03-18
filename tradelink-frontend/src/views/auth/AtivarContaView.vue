<template>
  <div class="ativar-page">
    <div class="ativar-page__bg">
      <div class="ativar-page__shape ativar-page__shape--1"></div>
      <div class="ativar-page__shape ativar-page__shape--2"></div>
      <div class="ativar-page__shape ativar-page__shape--3"></div>
    </div>

    <div class="ativar-page__content">
      <!-- Loading -->
      <div v-if="tokenStatus === 'loading'" class="ativar-card text-center">
        <div class="ativar-card__brand">
          <div class="ativar-card__logo"><span class="ativar-card__logo-icon">◈</span></div>
          <h1 class="ativar-card__title">TradeLink</h1>
        </div>
        <div class="flex items-center justify-center gap-3 py-8">
          <div class="ativar-spinner"></div>
          <span class="text-gray-500">Validando convite...</span>
        </div>
      </div>

      <!-- Token inválido -->
      <div v-else-if="tokenStatus === 'invalid'" class="ativar-card text-center">
        <div class="ativar-card__brand">
          <div class="ativar-card__logo" style="background:linear-gradient(135deg,#ef4444,#dc2626);">
            <span class="ativar-card__logo-icon">✕</span>
          </div>
          <h1 class="ativar-card__title">Convite Inválido</h1>
        </div>
        <p class="text-gray-500 mb-6">{{ errorMessage }}</p>
        <router-link to="/login" class="inline-block px-6 py-2.5 bg-indigo-600 text-white rounded-lg font-medium hover:bg-indigo-700 transition-colors">
          Ir para Login
        </router-link>
      </div>

      <!-- Formulário de ativação -->
      <div v-else class="ativar-card ativar-card--wide">
        <div class="ativar-card__brand">
          <div class="ativar-card__logo"><span class="ativar-card__logo-icon">◈</span></div>
          <h1 class="ativar-card__title">Ativar Conta</h1>
          <p class="ativar-card__tagline">
            Complete seu cadastro para acessar o <strong>TradeLink</strong>
          </p>
        </div>

        <!-- Info do convite -->
        <div class="flex items-center gap-3 mb-6 p-3 bg-indigo-50 rounded-lg border border-indigo-100">
          <span class="text-indigo-500 text-lg">📧</span>
          <div class="text-sm">
            <span class="text-gray-500">E-mail: </span>
            <strong class="text-gray-800">{{ conviteInfo.email }}</strong>
            <span v-if="conviteInfo.empresaNome" class="ml-2 px-2 py-0.5 bg-indigo-100 text-indigo-700 rounded-full text-xs font-medium">
              {{ conviteInfo.empresaNome }}
            </span>
          </div>
        </div>

        <form @submit.prevent="submit" class="space-y-6">
          <!-- Dados Pessoais -->
          <fieldset>
            <legend class="text-sm font-semibold text-gray-900 mb-3 flex items-center gap-2">
              <span class="w-6 h-6 bg-indigo-100 text-indigo-600 rounded-full flex items-center justify-center text-xs font-bold">1</span>
              Dados Pessoais
            </legend>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div class="md:col-span-2">
                <label class="ativar-label">Nome completo *</label>
                <input v-model="form.nome" type="text" required maxlength="150" class="ativar-input" placeholder="Seu nome completo" />
                <p v-if="errors.nome" class="ativar-error">{{ errors.nome }}</p>
              </div>
              <div>
                <label class="ativar-label">CPF *</label>
                <input v-model="form.cpf" type="text" required maxlength="14" class="ativar-input" placeholder="000.000.000-00"
                  @input="onCpfInput" @blur="validateCpf" />
                <p v-if="errors.cpf" class="ativar-error">{{ errors.cpf }}</p>
              </div>
              <div>
                <label class="ativar-label">WhatsApp *</label>
                <input v-model="form.whatsapp" type="tel" required maxlength="15" class="ativar-input" placeholder="(11) 99999-9999"
                  @input="onWhatsappInput" />
                <p v-if="errors.whatsapp" class="ativar-error">{{ errors.whatsapp }}</p>
              </div>
            </div>
          </fieldset>

          <!-- Endereço -->
          <fieldset>
            <legend class="text-sm font-semibold text-gray-900 mb-3 flex items-center gap-2">
              <span class="w-6 h-6 bg-indigo-100 text-indigo-600 rounded-full flex items-center justify-center text-xs font-bold">2</span>
              Endereço
            </legend>
            <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div>
                <label class="ativar-label">CEP *</label>
                <div class="relative">
                  <input v-model="form.cep" type="text" required maxlength="9" class="ativar-input pr-10" placeholder="00000-000"
                    @input="onCepInput" @blur="buscarCep" />
                  <span v-if="cepLoading" class="absolute right-3 top-1/2 -translate-y-1/2">
                    <span class="ativar-spinner ativar-spinner--sm"></span>
                  </span>
                </div>
                <p v-if="errors.cep" class="ativar-error">{{ errors.cep }}</p>
              </div>
              <div class="md:col-span-2">
                <label class="ativar-label">Logradouro *</label>
                <input v-model="form.logradouro" type="text" required maxlength="200" class="ativar-input" placeholder="Rua, Avenida..." />
              </div>
              <div>
                <label class="ativar-label">Número *</label>
                <input v-model="form.numero" type="text" required maxlength="20" class="ativar-input" placeholder="123" />
              </div>
              <div>
                <label class="ativar-label">Complemento</label>
                <input v-model="form.complemento" type="text" maxlength="100" class="ativar-input" placeholder="Apto, Bloco..." />
              </div>
              <div>
                <label class="ativar-label">Bairro *</label>
                <input v-model="form.bairro" type="text" required maxlength="100" class="ativar-input" />
              </div>
              <div>
                <label class="ativar-label">Cidade *</label>
                <input v-model="form.cidade" type="text" required maxlength="100" class="ativar-input" />
              </div>
              <div>
                <label class="ativar-label">Estado *</label>
                <select v-model="form.estado" required class="ativar-input">
                  <option value="">Selecione</option>
                  <option v-for="uf in UFS" :key="uf" :value="uf">{{ uf }}</option>
                </select>
              </div>
            </div>
          </fieldset>

          <!-- Senha -->
          <fieldset>
            <legend class="text-sm font-semibold text-gray-900 mb-3 flex items-center gap-2">
              <span class="w-6 h-6 bg-indigo-100 text-indigo-600 rounded-full flex items-center justify-center text-xs font-bold">3</span>
              Criar Senha
            </legend>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label class="ativar-label">Senha *</label>
                <input v-model="form.senha" type="password" required minlength="6" class="ativar-input" placeholder="Mínimo 6 caracteres" />
                <!-- Indicador de força -->
                <div v-if="form.senha" class="mt-1.5 flex gap-1">
                  <div v-for="i in 4" :key="i" class="h-1 flex-1 rounded-full transition-colors"
                    :class="i <= senhaForca ? senhaForcaCor : 'bg-gray-200'"></div>
                </div>
                <p v-if="form.senha" class="text-xs mt-1" :class="senhaForcaTexto.cor">{{ senhaForcaTexto.texto }}</p>
              </div>
              <div>
                <label class="ativar-label">Confirmar Senha *</label>
                <input v-model="form.confirmarSenha" type="password" required class="ativar-input" placeholder="Repita a senha" />
                <p v-if="form.confirmarSenha && form.senha !== form.confirmarSenha" class="ativar-error">As senhas não coincidem</p>
                <p v-if="form.confirmarSenha && form.senha === form.confirmarSenha" class="text-xs text-green-600 mt-1">✓ Senhas coincidem</p>
              </div>
            </div>
          </fieldset>

          <!-- Termos -->
          <fieldset>
            <legend class="text-sm font-semibold text-gray-900 mb-3 flex items-center gap-2">
              <span class="w-6 h-6 bg-indigo-100 text-indigo-600 rounded-full flex items-center justify-center text-xs font-bold">4</span>
              Termos de Uso
            </legend>

            <!-- Termos expandíveis -->
            <div class="border border-gray-200 rounded-lg mb-4">
              <button type="button" @click="showTermos = !showTermos"
                class="w-full flex items-center justify-between px-4 py-3 text-sm font-medium text-gray-700 hover:bg-gray-50 transition-colors">
                <span>📄 Ler Termos de Uso</span>
                <span class="transition-transform" :class="showTermos ? 'rotate-180' : ''">▼</span>
              </button>
              <div v-if="showTermos" class="px-4 pb-4 max-h-64 overflow-y-auto border-t border-gray-100">
                <div class="prose prose-sm text-gray-600 mt-3 space-y-3">
                  <h4 class="text-gray-900 font-semibold text-sm">TERMOS DE USO - TRADELINK</h4>

                  <p><strong>1. OBJETO</strong><br>
                  O TradeLink e uma plataforma digital de acompanhamento, organizacao e controle de investimentos,
                  destinada a facilitar a comunicacao entre consultores e seus clientes.</p>

                  <p><strong>2. NATUREZA DO SERVICO</strong><br>
                  O servico prestado pelo TradeLink tem carater exclusivamente <strong>informativo e organizacional</strong>.
                  A plataforma funciona como uma ferramenta de gestao e visualizacao de dados, nao realizando, em nenhuma
                  hipotese, operacoes financeiras, custodia de ativos, intermediacao de valores mobiliarios ou qualquer
                  atividade regulada pela Comissao de Valores Mobiliarios (CVM) ou pelo Banco Central do Brasil.</p>

                  <p><strong>3. ISENCAO DE RESPONSABILIDADE FINANCEIRA</strong><br>
                  O TradeLink <strong>NAO</strong>:<br>
                  - Realiza depositos, saques, transferencias ou movimentacoes financeiras reais;<br>
                  - Custodia ou armazena valores, criptomoedas ou quaisquer ativos financeiros;<br>
                  - Garante retorno financeiro, rentabilidade ou valorizacao de ativos;<br>
                  - Constitui recomendacao de investimento regulamentada;<br>
                  - Substitui a analise independente do investidor.</p>

                  <p><strong>4. DADOS E INFORMACOES</strong><br>
                  Todos os dados exibidos na plataforma sao alimentados exclusivamente pelo proprio usuario (cliente
                  e/ou consultor). As cotacoes exibidas sao obtidas de fontes publicas de terceiros e podem apresentar
                  atrasos ou imprecisoes. O TradeLink nao se responsabiliza pela exatidao, completude ou atualizacao
                  dos dados inseridos pelos usuarios ou fornecidos por APIs externas.</p>

                  <p><strong>5. RECOMENDACOES DO CONSULTOR</strong><br>
                  As recomendacoes de compra, venda ou rebalanceamento geradas na plataforma refletem exclusivamente
                  a opiniao e estrategia do consultor vinculado. O TradeLink nao endossa, valida ou se responsabiliza
                  pelas recomendacoes emitidas. A decisao de executar qualquer operacao e de inteira e exclusiva
                  responsabilidade do cliente.</p>

                  <p><strong>6. RESPONSABILIDADE DO USUARIO</strong><br>
                  O usuario declara estar ciente de que:<br>
                  - E o unico responsavel pelas decisoes de investimento que tomar;<br>
                  - Os valores apresentados na plataforma sao meramente referenciais e nao constituem saldo real;<br>
                  - Deve verificar de forma independente todas as informacoes antes de tomar decisoes financeiras;<br>
                  - Investimentos envolvem riscos, incluindo a possibilidade de perda total do capital investido.</p>

                  <p><strong>7. PROTECAO DE DADOS</strong><br>
                  Os dados pessoais coletados (nome, CPF, endereco, telefone) serao utilizados exclusivamente para
                  fins de identificacao e comunicacao dentro da plataforma, em conformidade com a Lei Geral de
                  Protecao de Dados (LGPD - Lei 13.709/2018).</p>

                  <p><strong>8. ACEITE</strong><br>
                  Ao marcar a caixa de aceite e prosseguir com a ativacao da conta, o usuario declara ter lido,
                  compreendido e concordado integralmente com todos os termos acima estabelecidos.</p>
                </div>
              </div>
            </div>

            <label class="flex items-start gap-3 cursor-pointer select-none">
              <input v-model="form.termoAceito" type="checkbox" class="mt-0.5 w-4 h-4 text-indigo-600 rounded border-gray-300 focus:ring-indigo-500" />
              <span class="text-sm text-gray-700">
                Li e aceito os <button type="button" @click="showTermos = true" class="text-indigo-600 underline font-medium">Termos de Uso</button>
                do TradeLink.
              </span>
            </label>
            <p v-if="errors.termoAceito" class="ativar-error mt-1">{{ errors.termoAceito }}</p>
          </fieldset>

          <!-- Erro geral -->
          <div v-if="submitError" class="p-3 bg-red-50 border border-red-200 rounded-lg text-red-700 text-sm">
            {{ submitError }}
          </div>

          <!-- Submit -->
          <button
            type="submit"
            :disabled="submitting || !form.termoAceito"
            class="w-full py-3 px-6 bg-indigo-600 text-white font-semibold rounded-lg hover:bg-indigo-700 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors text-base"
          >
            <span v-if="!submitting">Ativar Conta</span>
            <span v-else class="flex items-center justify-center gap-2">
              <span class="ativar-spinner ativar-spinner--white"></span>
              Ativando...
            </span>
          </button>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import { useToast } from '../../composables/useToast'
import authApi from '../../api/authApi'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const toast = useToast()

const UFS = ['AC','AL','AM','AP','BA','CE','DF','ES','GO','MA','MG','MS','MT','PA','PB','PE','PI','PR','RJ','RN','RO','RR','RS','SC','SE','SP','TO']

const tokenStatus = ref('loading')
const conviteInfo = ref({})
const errorMessage = ref('')

const form = ref({
  nome: '', cpf: '', whatsapp: '',
  cep: '', logradouro: '', numero: '', complemento: '',
  bairro: '', cidade: '', estado: '',
  senha: '', confirmarSenha: '', termoAceito: false
})
const errors = ref({})
const submitting = ref(false)
const submitError = ref('')
const cepLoading = ref(false)
const showTermos = ref(false)

// ─── Validação CPF ───
function apenasDigitos(v) { return (v || '').replace(/\D/g, '') }

function maskCpf(v) {
  const d = apenasDigitos(v).slice(0, 11)
  if (d.length <= 3) return d
  if (d.length <= 6) return d.slice(0, 3) + '.' + d.slice(3)
  if (d.length <= 9) return d.slice(0, 3) + '.' + d.slice(3, 6) + '.' + d.slice(6)
  return d.slice(0, 3) + '.' + d.slice(3, 6) + '.' + d.slice(6, 9) + '-' + d.slice(9)
}

function isValidCpf(cpf) {
  const d = apenasDigitos(cpf)
  if (d.length !== 11) return false
  if (/^(\d)\1{10}$/.test(d)) return false
  let soma = 0
  for (let i = 0; i < 9; i++) soma += parseInt(d[i]) * (10 - i)
  let resto = soma % 11
  let dv1 = resto < 2 ? 0 : 11 - resto
  if (parseInt(d[9]) !== dv1) return false
  soma = 0
  for (let i = 0; i < 10; i++) soma += parseInt(d[i]) * (11 - i)
  resto = soma % 11
  let dv2 = resto < 2 ? 0 : 11 - resto
  return parseInt(d[10]) === dv2
}

function onCpfInput() {
  form.value.cpf = maskCpf(form.value.cpf)
  errors.value.cpf = ''
}

function validateCpf() {
  if (form.value.cpf && !isValidCpf(form.value.cpf)) {
    errors.value.cpf = 'CPF inválido'
  }
}

// ─── Máscara WhatsApp ───
function maskWhatsapp(v) {
  const d = apenasDigitos(v).slice(0, 11)
  if (d.length <= 2) return d.length ? '(' + d : ''
  if (d.length <= 7) return '(' + d.slice(0, 2) + ') ' + d.slice(2)
  return '(' + d.slice(0, 2) + ') ' + d.slice(2, 7) + '-' + d.slice(7)
}

function onWhatsappInput() {
  form.value.whatsapp = maskWhatsapp(form.value.whatsapp)
}

// ─── CEP + ViaCEP ───
function maskCep(v) {
  const d = apenasDigitos(v).slice(0, 8)
  if (d.length <= 5) return d
  return d.slice(0, 5) + '-' + d.slice(5)
}

function onCepInput() {
  form.value.cep = maskCep(form.value.cep)
  errors.value.cep = ''
}

async function buscarCep() {
  const cep = apenasDigitos(form.value.cep)
  if (cep.length !== 8) return
  cepLoading.value = true
  errors.value.cep = ''
  try {
    const res = await fetch(`https://viacep.com.br/ws/${cep}/json/`)
    const data = await res.json()
    if (data.erro) {
      errors.value.cep = 'CEP não encontrado'
      return
    }
    form.value.logradouro = data.logradouro || form.value.logradouro
    form.value.bairro = data.bairro || form.value.bairro
    form.value.cidade = data.localidade || form.value.cidade
    form.value.estado = data.uf || form.value.estado
  } catch (e) {
    errors.value.cep = 'Erro ao consultar CEP'
  } finally {
    cepLoading.value = false
  }
}

// ─── Força da senha ───
const senhaForca = computed(() => {
  const s = form.value.senha
  if (!s) return 0
  let score = 0
  if (s.length >= 6) score++
  if (s.length >= 8) score++
  if (/[A-Z]/.test(s) && /[a-z]/.test(s)) score++
  if (/\d/.test(s) && /[^a-zA-Z0-9]/.test(s)) score++
  return score
})

const senhaForcaCor = computed(() => {
  const cores = ['', 'bg-red-400', 'bg-yellow-400', 'bg-blue-400', 'bg-green-500']
  return cores[senhaForca.value] || 'bg-gray-200'
})

const senhaForcaTexto = computed(() => {
  const textos = [
    { texto: '', cor: '' },
    { texto: 'Fraca', cor: 'text-red-500' },
    { texto: 'Razoável', cor: 'text-yellow-600' },
    { texto: 'Boa', cor: 'text-blue-600' },
    { texto: 'Forte', cor: 'text-green-600' }
  ]
  return textos[senhaForca.value] || textos[0]
})

// ─── Validação do form ───
function validate() {
  errors.value = {}
  if (!form.value.nome.trim()) errors.value.nome = 'Nome é obrigatório'
  if (!isValidCpf(form.value.cpf)) errors.value.cpf = 'CPF inválido'
  if (apenasDigitos(form.value.whatsapp).length < 10) errors.value.whatsapp = 'WhatsApp inválido'
  if (apenasDigitos(form.value.cep).length !== 8) errors.value.cep = 'CEP inválido'
  if (!form.value.termoAceito) errors.value.termoAceito = 'Você deve aceitar os termos'
  if (form.value.senha !== form.value.confirmarSenha) errors.value.senha = 'Senhas não coincidem'
  return Object.keys(errors.value).length === 0
}

// ─── Submit ───
async function submit() {
  submitError.value = ''
  if (!validate()) return
  submitting.value = true
  try {
    const payload = {
      token: route.params.token,
      nome: form.value.nome.trim(),
      cpf: apenasDigitos(form.value.cpf),
      whatsapp: apenasDigitos(form.value.whatsapp),
      cep: apenasDigitos(form.value.cep),
      logradouro: form.value.logradouro.trim(),
      numero: form.value.numero.trim(),
      complemento: form.value.complemento?.trim() || '',
      bairro: form.value.bairro.trim(),
      cidade: form.value.cidade.trim(),
      estado: form.value.estado,
      senha: form.value.senha,
      confirmarSenha: form.value.confirmarSenha,
      termoAceito: form.value.termoAceito
    }
    const res = await authApi.ativarConta(payload)

    // Auto-login
    authStore.token = res.data.token
    authStore.user = {
      id: res.data.userId,
      nome: res.data.nome,
      role: res.data.role,
      empresaId: res.data.empresaId
    }
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('user', JSON.stringify(authStore.user))

    toast.success('Conta ativada com sucesso! Bem-vindo(a)!')
    router.push('/cliente')
  } catch (e) {
    submitError.value = e.response?.data?.erro || 'Erro ao ativar conta. Tente novamente.'
  } finally {
    submitting.value = false
  }
}

// ─── Validar token ao montar ───
onMounted(async () => {
  try {
    const res = await authApi.validarConvite(route.params.token)
    if (res.data.valido) {
      tokenStatus.value = 'valid'
      conviteInfo.value = res.data
    } else {
      tokenStatus.value = 'invalid'
      errorMessage.value = res.data.mensagem || 'Convite inválido.'
    }
  } catch (e) {
    tokenStatus.value = 'invalid'
    errorMessage.value = 'Erro ao validar convite. Tente novamente.'
  }
})
</script>

<style scoped>
.ativar-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1.5rem;
  position: relative;
  overflow: hidden;
}

.ativar-page__bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgb(99 102 241) 0%, rgb(139 92 246) 50%, rgb(79 70 229) 100%);
}

.ativar-page__shape {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.4;
}
.ativar-page__shape--1 { width: 400px; height: 400px; background: rgb(255 255 255); top: -100px; right: -100px; animation: float 15s ease-in-out infinite; }
.ativar-page__shape--2 { width: 300px; height: 300px; background: rgb(196 181 253); bottom: -50px; left: -50px; animation: float 18s ease-in-out infinite reverse; }
.ativar-page__shape--3 { width: 200px; height: 200px; background: rgb(165 180 252); top: 50%; left: 30%; animation: float 12s ease-in-out infinite; animation-delay: -4s; }

@keyframes float {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(20px, -20px); }
}

.ativar-page__content {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 420px;
}

.ativar-card {
  background: rgba(255, 255, 255, 0.98);
  border-radius: 20px;
  padding: 2rem;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  backdrop-filter: blur(12px);
  animation: card-in 0.5s ease-out;
}

.ativar-card--wide {
  max-width: 640px;
}

.ativar-page__content:has(.ativar-card--wide) {
  max-width: 640px;
}

@keyframes card-in {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.ativar-card__brand {
  text-align: center;
  margin-bottom: 1.5rem;
}

.ativar-card__logo {
  width: 56px;
  height: 56px;
  margin: 0 auto 0.75rem;
  background: linear-gradient(135deg, rgb(99 102 241), rgb(139 92 246));
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 24px rgba(99, 102, 241, 0.35);
}

.ativar-card__logo-icon {
  font-size: 1.75rem;
  color: white;
  font-weight: 700;
  line-height: 1;
}

.ativar-card__title {
  font-size: 1.5rem;
  font-weight: 700;
  color: rgb(17 24 39);
  letter-spacing: -0.03em;
  margin: 0 0 0.375rem;
}

.ativar-card__tagline {
  font-size: 0.875rem;
  color: rgb(107 114 128);
  line-height: 1.5;
  margin: 0;
}

.ativar-label {
  display: block;
  font-size: 0.8125rem;
  font-weight: 500;
  color: rgb(55 65 81);
  margin-bottom: 0.25rem;
}

.ativar-input {
  width: 100%;
  padding: 0.5rem 0.75rem;
  border: 1px solid rgb(209 213 219);
  border-radius: 0.5rem;
  font-size: 0.875rem;
  color: rgb(17 24 39);
  transition: border-color 0.15s, box-shadow 0.15s;
  background: white;
}
.ativar-input:focus {
  outline: none;
  border-color: rgb(99 102 241);
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.15);
}

.ativar-error {
  font-size: 0.75rem;
  color: rgb(220 38 38);
  margin-top: 0.25rem;
}

.ativar-spinner {
  width: 1.25rem;
  height: 1.25rem;
  border: 2px solid rgba(99, 102, 241, 0.2);
  border-top-color: rgb(99 102 241);
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}
.ativar-spinner--sm { width: 0.875rem; height: 0.875rem; }
.ativar-spinner--white { border-color: rgba(255,255,255,0.3); border-top-color: white; }

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Prose-like styling for terms */
.prose h4 { margin: 0 0 0.5rem; }
.prose p { margin: 0 0 0.5rem; font-size: 0.8125rem; line-height: 1.6; }
.prose strong { color: rgb(31 41 55); }
</style>

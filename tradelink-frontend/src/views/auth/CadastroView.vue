<template>
  <div class="cadastro-page">
    <div class="cadastro-page__bg">
      <div class="cadastro-page__shape cadastro-page__shape--1"></div>
      <div class="cadastro-page__shape cadastro-page__shape--2"></div>
      <div class="cadastro-page__shape cadastro-page__shape--3"></div>
    </div>

    <div class="cadastro-page__content">
      <div class="cadastro-card">
        <div class="cadastro-card__brand">
          <div class="cadastro-card__logo"><span class="cadastro-card__logo-icon">&#9670;</span></div>
          <h1 class="cadastro-card__title">TradeLink</h1>
          <p class="cadastro-card__tagline">Crie sua conta e comece <strong>5 dias gratuitos</strong></p>
        </div>

        <!-- Step 1: Tipo -->
        <div v-if="step === 1">
          <p class="text-center text-gray-500 text-sm mb-6">Como voce deseja usar o TradeLink?</p>
          <div class="cadastro-grid-2">
            <button type="button" @click="selectTipo('CLIENTE')"
              class="cadastro-tipo-card"
              :class="form.tipo === 'CLIENTE' ? 'cadastro-tipo-card--selected' : ''">
              <div class="cadastro-tipo-card__icon">&#128200;</div>
              <h3 class="cadastro-tipo-card__title">Sou Investidor</h3>
              <p class="cadastro-tipo-card__desc">Gerencie seu portfolio de investimentos de forma autonoma.</p>
              <p class="cadastro-tipo-card__price">R$ 9,90/mes apos o trial</p>
            </button>
            <button type="button" @click="selectTipo('CONSULTOR')"
              class="cadastro-tipo-card"
              :class="form.tipo === 'CONSULTOR' ? 'cadastro-tipo-card--selected' : ''">
              <div class="cadastro-tipo-card__icon">&#128188;</div>
              <h3 class="cadastro-tipo-card__title">Sou Consultor / Assessor</h3>
              <p class="cadastro-tipo-card__desc">Gerencie carteiras e clientes com ferramentas profissionais.</p>
              <p class="cadastro-tipo-card__price">Planos a partir de R$ 49,90/mes</p>
            </button>
          </div>
          <div class="cadastro-footer">
            <router-link to="/login" class="cadastro-link">Ja tem conta? Entrar</router-link>
            <button type="button" @click="goStep2" :disabled="!form.tipo"
              class="cadastro-btn-primary">Continuar</button>
          </div>
        </div>

        <!-- Step 2: Formulario -->
        <form v-if="step === 2" @submit.prevent="submit" class="cadastro-form">
          <!-- Dados Pessoais -->
          <fieldset class="cadastro-fieldset">
            <legend class="cadastro-legend">
              <span class="cadastro-step-badge">1</span>
              Dados Pessoais
            </legend>
            <div class="cadastro-grid-2">
              <div class="cadastro-col-span-2">
                <label class="cadastro-label">Nome completo *</label>
                <input v-model="form.nome" type="text" required maxlength="150" class="cadastro-input" placeholder="Seu nome completo" />
              </div>
              <div>
                <label class="cadastro-label">E-mail *</label>
                <input v-model="form.email" type="email" required class="cadastro-input" placeholder="seu@email.com" />
              </div>
              <div>
                <label class="cadastro-label">CPF *</label>
                <input v-model="form.cpf" type="text" required maxlength="14" class="cadastro-input" placeholder="000.000.000-00"
                  @input="onCpfInput" />
              </div>
              <div>
                <label class="cadastro-label">WhatsApp</label>
                <input v-model="form.whatsapp" type="tel" maxlength="15" class="cadastro-input" placeholder="(11) 99999-9999"
                  @input="onWhatsappInput" />
              </div>
            </div>
          </fieldset>

          <!-- Dados da Empresa (Consultor) -->
          <fieldset v-if="form.tipo === 'CONSULTOR'" class="cadastro-fieldset">
            <legend class="cadastro-legend">
              <span class="cadastro-step-badge">2</span>
              Dados Profissionais
            </legend>
            <div class="cadastro-grid-2">
              <div class="cadastro-col-span-2">
                <div class="cadastro-radio-group">
                  <label class="cadastro-radio">
                    <input type="radio" v-model="tipoPessoa" value="PF" /> Pessoa Fisica
                  </label>
                  <label class="cadastro-radio">
                    <input type="radio" v-model="tipoPessoa" value="PJ" /> Pessoa Juridica
                  </label>
                </div>
              </div>
              <div v-if="tipoPessoa === 'PJ'">
                <label class="cadastro-label">CNPJ *</label>
                <input v-model="form.cnpj" type="text" maxlength="18" class="cadastro-input" placeholder="00.000.000/0000-00"
                  @input="onCnpjInput" />
              </div>
              <div v-if="tipoPessoa === 'PJ'">
                <label class="cadastro-label">Nome da Empresa *</label>
                <input v-model="form.nomeEmpresa" type="text" maxlength="150" class="cadastro-input" placeholder="Nome da sua empresa" />
              </div>
            </div>
          </fieldset>

          <!-- Endereco -->
          <fieldset class="cadastro-fieldset">
            <legend class="cadastro-legend">
              <span class="cadastro-step-badge">{{ form.tipo === 'CONSULTOR' ? '3' : '2' }}</span>
              Endereco
            </legend>
            <div class="cadastro-grid-3">
              <div>
                <label class="cadastro-label">CEP</label>
                <input v-model="form.cep" type="text" maxlength="9" class="cadastro-input" placeholder="00000-000"
                  @input="onCepInput" />
              </div>
              <div class="cadastro-col-span-2">
                <label class="cadastro-label">Logradouro</label>
                <input v-model="form.logradouro" type="text" class="cadastro-input" placeholder="Rua, Av..." />
              </div>
              <div>
                <label class="cadastro-label">Numero</label>
                <input v-model="form.numero" type="text" maxlength="20" class="cadastro-input" placeholder="123" />
              </div>
              <div>
                <label class="cadastro-label">Complemento</label>
                <input v-model="form.complemento" type="text" class="cadastro-input" placeholder="Apto, Sala..." />
              </div>
              <div>
                <label class="cadastro-label">Bairro</label>
                <input v-model="form.bairro" type="text" class="cadastro-input" />
              </div>
              <div>
                <label class="cadastro-label">Cidade</label>
                <input v-model="form.cidade" type="text" class="cadastro-input" />
              </div>
              <div>
                <label class="cadastro-label">Estado</label>
                <input v-model="form.estado" type="text" maxlength="2" class="cadastro-input" placeholder="SP" />
              </div>
            </div>
          </fieldset>

          <!-- Senha -->
          <fieldset class="cadastro-fieldset">
            <legend class="cadastro-legend">
              <span class="cadastro-step-badge">{{ form.tipo === 'CONSULTOR' ? '4' : '3' }}</span>
              Senha
            </legend>
            <div class="cadastro-grid-2">
              <div>
                <label class="cadastro-label">Senha *</label>
                <input v-model="form.senha" type="password" required minlength="6" class="cadastro-input" placeholder="Minimo 6 caracteres" />
                <div v-if="form.senha" class="cadastro-strength">
                  <div v-for="i in 4" :key="i" class="cadastro-strength__bar"
                    :class="passwordStrength >= i ? strengthColors[passwordStrength] : 'bg-gray-200'"></div>
                </div>
                <p v-if="form.senha" class="cadastro-strength-text"
                  :class="{ 'text-red-500': passwordStrength <= 1, 'text-amber-500': passwordStrength === 2, 'text-blue-500': passwordStrength === 3, 'text-green-600': passwordStrength === 4 }">
                  {{ strengthLabels[passwordStrength] || '' }}
                </p>
              </div>
              <div>
                <label class="cadastro-label">Confirmar senha *</label>
                <input v-model="confirmSenha" type="password" required class="cadastro-input" placeholder="Repita a senha" />
                <p v-if="confirmSenha && form.senha !== confirmSenha" class="cadastro-error">Senhas nao coincidem</p>
                <p v-if="confirmSenha && form.senha === confirmSenha" class="text-xs text-green-600 mt-1">Senhas coincidem</p>
              </div>
            </div>
          </fieldset>

          <!-- Termos -->
          <div class="cadastro-termos">
            <button type="button" @click="showTermos = !showTermos"
              class="cadastro-termos__toggle">
              <span>Ler Termos de Uso</span>
              <span class="cadastro-termos__arrow" :class="showTermos ? 'cadastro-termos__arrow--open' : ''">&#9660;</span>
            </button>
            <div v-if="showTermos" class="cadastro-termos__content">
              <p class="mb-2"><strong>Termos de Uso — TradeLink</strong></p>
              <p class="mb-1">Ao utilizar a plataforma TradeLink, voce concorda com as seguintes condicoes:</p>
              <p class="mb-1">1. O TradeLink e uma ferramenta de gestao de investimentos. Nao constitui recomendacao de investimento.</p>
              <p class="mb-1">2. Seus dados pessoais serao tratados conforme nossa Politica de Privacidade (LGPD).</p>
              <p class="mb-1">3. O periodo de trial gratuito tem duracao de 5 dias corridos.</p>
              <p class="mb-1">4. Apos o trial, e necessario escolher um plano pago para continuar acessando.</p>
              <p class="mb-1">5. Cancelamentos podem ser solicitados a qualquer momento via suporte.</p>
              <p>6. A plataforma pode ser atualizada sem aviso previo para melhorias e correcoes.</p>
            </div>
            <label class="cadastro-termos__check">
              <input type="checkbox" v-model="form.termoAceito" />
              <span>Li e aceito os <button type="button" @click="showTermos = true" class="cadastro-termos__link">Termos de Uso</button></span>
            </label>
          </div>

          <div v-if="error" class="cadastro-alert">{{ error }}</div>

          <div class="cadastro-footer">
            <button type="button" @click="step = 1" class="cadastro-link">Voltar</button>
            <button type="submit" :disabled="submitting || !form.termoAceito"
              class="cadastro-btn-primary cadastro-btn-primary--lg">
              {{ submitting ? 'Criando conta...' : 'Criar Minha Conta' }}
            </button>
          </div>
        </form>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import authApi from '../../api/authApi'

const router = useRouter()
const authStore = useAuthStore()

const step = ref(1)
const tipoPessoa = ref('PF')
const showTermos = ref(false)
const error = ref('')
const submitting = ref(false)
const confirmSenha = ref('')

const form = ref({
  tipo: '',
  nome: '',
  email: '',
  senha: '',
  cpf: '',
  whatsapp: '',
  cep: '',
  logradouro: '',
  numero: '',
  complemento: '',
  bairro: '',
  cidade: '',
  estado: '',
  termoAceito: false,
  cnpj: '',
  nomeEmpresa: '',
  nomeResponsavel: '',
  cpfResponsavel: ''
})

const passwordStrength = computed(() => {
  const s = form.value.senha
  if (!s) return 0
  let score = 0
  if (s.length >= 6) score++
  if (s.length >= 8) score++
  if (/[A-Z]/.test(s) && /[a-z]/.test(s)) score++
  if (/[0-9]/.test(s) || /[^A-Za-z0-9]/.test(s)) score++
  return score
})

const strengthColors = { 1: 'bg-red-400', 2: 'bg-amber-400', 3: 'bg-blue-400', 4: 'bg-green-500' }
const strengthLabels = { 1: 'Fraca', 2: 'Razoavel', 3: 'Boa', 4: 'Forte' }

function selectTipo(tipo) { form.value.tipo = tipo }
function goStep2() { if (form.value.tipo) step.value = 2 }

function maskCpf(v) {
  const d = v.replace(/\D/g, '').slice(0, 11)
  if (d.length <= 3) return d
  if (d.length <= 6) return d.slice(0,3) + '.' + d.slice(3)
  if (d.length <= 9) return d.slice(0,3) + '.' + d.slice(3,6) + '.' + d.slice(6)
  return d.slice(0,3) + '.' + d.slice(3,6) + '.' + d.slice(6,9) + '-' + d.slice(9)
}
function onCpfInput(e) { form.value.cpf = maskCpf(e.target.value) }

function maskPhone(v) {
  const d = v.replace(/\D/g, '').slice(0, 11)
  if (d.length <= 2) return d.length ? '(' + d : ''
  if (d.length <= 7) return '(' + d.slice(0,2) + ') ' + d.slice(2)
  return '(' + d.slice(0,2) + ') ' + d.slice(2,7) + '-' + d.slice(7)
}
function onWhatsappInput(e) { form.value.whatsapp = maskPhone(e.target.value) }

function maskCnpj(v) {
  const d = v.replace(/\D/g, '').slice(0, 14)
  if (d.length <= 2) return d
  if (d.length <= 5) return d.slice(0,2) + '.' + d.slice(2)
  if (d.length <= 8) return d.slice(0,2) + '.' + d.slice(2,5) + '.' + d.slice(5)
  if (d.length <= 12) return d.slice(0,2) + '.' + d.slice(2,5) + '.' + d.slice(5,8) + '/' + d.slice(8)
  return d.slice(0,2) + '.' + d.slice(2,5) + '.' + d.slice(5,8) + '/' + d.slice(8,12) + '-' + d.slice(12)
}
function onCnpjInput(e) { form.value.cnpj = maskCnpj(e.target.value) }

function maskCep(v) {
  const d = v.replace(/\D/g, '').slice(0, 8)
  if (d.length <= 5) return d
  return d.slice(0,5) + '-' + d.slice(5)
}
async function onCepInput(e) {
  form.value.cep = maskCep(e.target.value)
  const cepDigits = form.value.cep.replace(/\D/g, '')
  if (cepDigits.length === 8) {
    try {
      const res = await fetch(`https://viacep.com.br/ws/${cepDigits}/json/`)
      const data = await res.json()
      if (!data.erro) {
        form.value.logradouro = data.logradouro || ''
        form.value.bairro = data.bairro || ''
        form.value.cidade = data.localidade || ''
        form.value.estado = data.uf || ''
      }
    } catch {}
  }
}

async function submit() {
  error.value = ''
  if (form.value.senha !== confirmSenha.value) { error.value = 'As senhas nao coincidem.'; return }
  if (!form.value.termoAceito) { error.value = 'Aceite os termos de uso para continuar.'; return }
  submitting.value = true
  try {
    const payload = { ...form.value }
    if (form.value.tipo === 'CONSULTOR') {
      payload.nomeResponsavel = form.value.nome
      payload.cpfResponsavel = form.value.cpf
      if (tipoPessoa.value === 'PF') payload.cnpj = form.value.cpf
    }
    const res = await authApi.autoCadastro(payload)
    const data = res.data
    authStore.token = data.token
    authStore.user = {
      id: data.userId, nome: data.nome, role: data.role,
      empresaId: data.empresaId || null,
      clienteExcluido: false,
      autoGestaoAtiva: true, trialAtivo: true,
      trialFim: data.trialFim, autoCadastro: true
    }
    localStorage.setItem('token', data.token)
    localStorage.setItem('user', JSON.stringify(authStore.user))
    router.push(authStore.dashboardRoute)
  } catch (e) {
    error.value = e.response?.data?.mensagem || e.response?.data?.erro || 'Erro ao criar conta. Tente novamente.'
  } finally { submitting.value = false }
}
</script>

<style scoped>
.cadastro-page { min-height: 100vh; display: flex; align-items: center; justify-content: center; padding: 1.5rem; position: relative; overflow: hidden; }
.cadastro-page__bg { position: absolute; inset: 0; background: linear-gradient(135deg, rgb(99 102 241) 0%, rgb(139 92 246) 50%, rgb(79 70 229) 100%); }
.cadastro-page__shape { position: absolute; border-radius: 50%; filter: blur(80px); opacity: 0.4; }
.cadastro-page__shape--1 { width: 400px; height: 400px; background: rgb(255 255 255); top: -100px; right: -100px; animation: cfloat 15s ease-in-out infinite; }
.cadastro-page__shape--2 { width: 300px; height: 300px; background: rgb(196 181 253); bottom: -50px; left: -50px; animation: cfloat 18s ease-in-out infinite reverse; }
.cadastro-page__shape--3 { width: 200px; height: 200px; background: rgb(165 180 252); top: 50%; left: 30%; animation: cfloat 12s ease-in-out infinite; animation-delay: -4s; }
@keyframes cfloat { 0%, 100% { transform: translate(0,0); } 50% { transform: translate(20px,-20px); } }

.cadastro-page__content { position: relative; z-index: 1; width: 100%; max-width: 640px; }
.cadastro-card { background: rgba(255,255,255,0.98); border-radius: 20px; padding: 2rem; box-shadow: 0 25px 50px -12px rgba(0,0,0,0.25); backdrop-filter: blur(12px); animation: ccardin 0.5s ease-out; }
@keyframes ccardin { from { opacity:0; transform:translateY(20px); } to { opacity:1; transform:translateY(0); } }

.cadastro-card__brand { text-align: center; margin-bottom: 1.5rem; }
.cadastro-card__logo { width: 56px; height: 56px; margin: 0 auto 0.75rem; background: linear-gradient(135deg,rgb(99 102 241),rgb(139 92 246)); border-radius: 14px; display: flex; align-items: center; justify-content: center; box-shadow: 0 8px 24px rgba(99,102,241,0.35); }
.cadastro-card__logo-icon { font-size: 1.75rem; color: white; font-weight: 700; line-height: 1; }
.cadastro-card__title { font-size: 1.5rem; font-weight: 700; color: rgb(17 24 39); letter-spacing: -0.03em; margin: 0 0 0.375rem; }
.cadastro-card__tagline { font-size: 0.875rem; color: rgb(107 114 128); line-height: 1.5; margin: 0; }

.cadastro-grid-2 { display: grid; grid-template-columns: 1fr; gap: 1rem; }
@media (min-width:640px) { .cadastro-grid-2 { grid-template-columns: 1fr 1fr; } }
.cadastro-grid-3 { display: grid; grid-template-columns: 1fr; gap: 1rem; }
@media (min-width:640px) { .cadastro-grid-3 { grid-template-columns: 1fr 1fr 1fr; } }
.cadastro-col-span-2 { grid-column: 1 / -1; }
@media (min-width:640px) { .cadastro-col-span-2 { grid-column: span 2; } }

.cadastro-tipo-card { padding: 1.25rem; border-radius: 12px; border: 2px solid rgb(229 231 235); background: white; text-align: left; cursor: pointer; transition: border-color 0.2s, background 0.2s, box-shadow 0.2s; }
.cadastro-tipo-card:hover { border-color: rgb(199 210 254); box-shadow: 0 4px 12px rgba(99,102,241,0.1); }
.cadastro-tipo-card--selected { border-color: rgb(99 102 241); background: rgb(238 242 255); box-shadow: 0 4px 12px rgba(99,102,241,0.15); }
.cadastro-tipo-card__icon { font-size: 1.5rem; margin-bottom: 0.5rem; }
.cadastro-tipo-card__title { font-size: 1rem; font-weight: 600; color: rgb(17 24 39); margin: 0 0 0.25rem; }
.cadastro-tipo-card__desc { font-size: 0.8125rem; color: rgb(107 114 128); margin: 0; line-height: 1.5; }
.cadastro-tipo-card__price { font-size: 0.75rem; color: rgb(99 102 241); margin: 0.5rem 0 0; font-weight: 500; }

.cadastro-footer { display: flex; justify-content: space-between; align-items: center; padding-top: 1.25rem; }
.cadastro-form { display: flex; flex-direction: column; gap: 1.5rem; }
.cadastro-fieldset { border: none; padding: 0; margin: 0; }
.cadastro-legend { font-size: 0.875rem; font-weight: 600; color: rgb(17 24 39); margin-bottom: 0.75rem; display: flex; align-items: center; gap: 0.5rem; }
.cadastro-step-badge { width: 24px; height: 24px; background: rgb(238 242 255); color: rgb(99 102 241); border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 0.75rem; font-weight: 700; }
.cadastro-label { display: block; font-size: 0.8125rem; font-weight: 500; color: rgb(55 65 81); margin-bottom: 0.25rem; }
.cadastro-input { width: 100%; padding: 0.5rem 0.75rem; border: 1px solid rgb(209 213 219); border-radius: 0.5rem; font-size: 0.875rem; color: rgb(17 24 39); background: white; transition: border-color 0.15s, box-shadow 0.15s; }
.cadastro-input:focus { outline: none; border-color: rgb(99 102 241); box-shadow: 0 0 0 3px rgba(99,102,241,0.15); }
.cadastro-input::placeholder { color: rgb(156 163 175); }

.cadastro-radio-group { display: flex; gap: 1.5rem; margin-bottom: 0.25rem; }
.cadastro-radio { font-size: 0.875rem; color: rgb(55 65 81); display: flex; align-items: center; gap: 0.375rem; cursor: pointer; }

.cadastro-strength { display: flex; gap: 0.25rem; margin-top: 0.375rem; }
.cadastro-strength__bar { height: 4px; flex: 1; border-radius: 9999px; transition: background-color 0.3s; }
.cadastro-strength-text { font-size: 0.75rem; margin-top: 0.25rem; }
.cadastro-error { font-size: 0.75rem; color: rgb(220 38 38); margin-top: 0.25rem; }
.cadastro-alert { padding: 0.75rem 1rem; background: rgb(254 242 242); color: rgb(185 28 28); font-size: 0.875rem; border-radius: 8px; border: 1px solid rgb(254 202 202); }

.cadastro-termos { border: 1px solid rgb(229 231 235); border-radius: 12px; overflow: hidden; }
.cadastro-termos__toggle { width: 100%; display: flex; align-items: center; justify-content: space-between; padding: 0.75rem 1rem; font-size: 0.875rem; font-weight: 500; color: rgb(55 65 81); background: none; border: none; cursor: pointer; transition: background 0.15s; }
.cadastro-termos__toggle:hover { background: rgb(249 250 251); }
.cadastro-termos__arrow { font-size: 0.625rem; transition: transform 0.2s; }
.cadastro-termos__arrow--open { transform: rotate(180deg); }
.cadastro-termos__content { padding: 0 1rem 0.75rem; max-height: 160px; overflow-y: auto; border-top: 1px solid rgb(243 244 246); font-size: 0.75rem; color: rgb(107 114 128); line-height: 1.6; }
.cadastro-termos__check { display: flex; align-items: center; gap: 0.5rem; padding: 0.75rem 1rem; border-top: 1px solid rgb(243 244 246); font-size: 0.875rem; color: rgb(55 65 81); cursor: pointer; }
.cadastro-termos__link { color: rgb(99 102 241); text-decoration: underline; font-weight: 500; background: none; border: none; cursor: pointer; }

.cadastro-link { font-size: 0.875rem; color: rgb(107 114 128); text-decoration: none; transition: color 0.2s; }
.cadastro-link:hover { color: rgb(99 102 241); }
.cadastro-btn-primary { padding: 0.625rem 1.5rem; background: rgb(99 102 241); color: white; border: none; border-radius: 8px; font-size: 0.875rem; font-weight: 600; cursor: pointer; transition: background 0.2s; }
.cadastro-btn-primary:hover:not(:disabled) { background: rgb(79 70 229); }
.cadastro-btn-primary:disabled { opacity: 0.4; cursor: not-allowed; }
.cadastro-btn-primary--lg { padding: 0.75rem 2rem; font-size: 1rem; }
</style>

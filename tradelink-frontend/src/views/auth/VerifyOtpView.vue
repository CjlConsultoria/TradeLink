<template>
  <div class="login-page">
    <div class="login-page__bg">
      <div class="login-page__shape login-page__shape--1"></div>
      <div class="login-page__shape login-page__shape--2"></div>
      <div class="login-page__shape login-page__shape--3"></div>
    </div>

    <div class="login-page__content">
      <div class="login-card" :class="{ 'login-card--shaking': showShake }">
        <div class="login-card__brand">
          <div class="login-card__logo" aria-hidden="true">
            <span class="login-card__logo-icon">◈</span>
          </div>
          <h1 class="login-card__title">TradeLink</h1>
          <p class="login-card__tagline">Verificacao de seguranca</p>
        </div>

        <div class="otp-card__info">
          <p class="otp-card__description">
            Enviamos um codigo de 6 digitos para o seu e-mail. Digite-o abaixo para continuar.
          </p>
        </div>

        <form @submit.prevent="handleVerify" class="login-card__form">
          <div class="otp-card__inputs">
            <input
              v-for="(_, i) in 6"
              :key="i"
              :ref="el => { if (el) otpRefs[i] = el }"
              v-model="digits[i]"
              type="text"
              inputmode="numeric"
              maxlength="1"
              class="otp-card__digit"
              :disabled="loading"
              @input="onDigitInput(i)"
              @keydown="onDigitKeydown($event, i)"
              @paste="onPaste"
            />
          </div>

          <div v-if="timer > 0" class="otp-card__timer">
            Codigo expira em <strong>{{ formattedTimer }}</strong>
          </div>
          <div v-else class="otp-card__timer otp-card__timer--expired">
            Codigo expirado
          </div>

          <p v-if="error" class="login-card__error" role="alert">
            {{ error }}
          </p>

          <button
            type="submit"
            class="btn-primary login-card__submit"
            :disabled="loading || otpCode.length < 6"
          >
            <span v-if="!loading">Verificar</span>
            <span v-else class="login-card__loading">
              <span class="login-card__spinner" aria-hidden="true"></span>
              Verificando...
            </span>
          </button>
        </form>

        <div class="login-card__links">
          <button
            class="otp-card__resend"
            :disabled="resending || resendCooldown > 0"
            @click="handleResend"
          >
            <span v-if="resending">Reenviando...</span>
            <span v-else-if="resendCooldown > 0">Reenviar codigo ({{ resendCooldown }}s)</span>
            <span v-else>Reenviar codigo</span>
          </button>
          <p class="login-card__hint">
            <router-link to="/login" class="login-card__link login-card__link--subtle">Voltar ao login</router-link>
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import { useToast } from '../../composables/useToast'
import authApi from '../../api/authApi'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const toast = useToast()

const userId = ref(Number(route.query.userId) || null)
const digits = ref(['', '', '', '', '', ''])
const otpRefs = ref([])
const error = ref('')
const loading = ref(false)
const showShake = ref(false)
const resending = ref(false)
const resendCooldown = ref(0)
const timer = ref(300) // 5 minutos em segundos

let timerInterval = null
let cooldownInterval = null

const otpCode = computed(() => digits.value.join(''))

const formattedTimer = computed(() => {
  const min = Math.floor(timer.value / 60)
  const sec = timer.value % 60
  return `${min}:${sec.toString().padStart(2, '0')}`
})

onMounted(() => {
  if (!userId.value) {
    router.push('/login')
    return
  }
  startTimer()
  // Auto-focus no primeiro input
  if (otpRefs.value[0]) {
    otpRefs.value[0].focus()
  }
})

onUnmounted(() => {
  clearInterval(timerInterval)
  clearInterval(cooldownInterval)
})

function startTimer() {
  timer.value = 300
  clearInterval(timerInterval)
  timerInterval = setInterval(() => {
    if (timer.value > 0) {
      timer.value--
    } else {
      clearInterval(timerInterval)
    }
  }, 1000)
}

function onDigitInput(index) {
  const val = digits.value[index]
  // Aceitar apenas numeros
  if (val && !/^\d$/.test(val)) {
    digits.value[index] = ''
    return
  }
  // Auto-avançar para o próximo input
  if (val && index < 5) {
    otpRefs.value[index + 1]?.focus()
  }
  // Auto-submit quando todos os 6 digitos estão preenchidos
  if (otpCode.value.length === 6) {
    handleVerify()
  }
}

function onDigitKeydown(event, index) {
  if (event.key === 'Backspace' && !digits.value[index] && index > 0) {
    otpRefs.value[index - 1]?.focus()
  }
}

function onPaste(event) {
  event.preventDefault()
  const pasted = (event.clipboardData || window.clipboardData).getData('text').trim()
  const nums = pasted.replace(/\D/g, '').slice(0, 6)
  for (let i = 0; i < 6; i++) {
    digits.value[i] = nums[i] || ''
  }
  // Focar no ultimo campo preenchido ou no proximo vazio
  const focusIdx = Math.min(nums.length, 5)
  otpRefs.value[focusIdx]?.focus()
  if (nums.length === 6) {
    handleVerify()
  }
}

async function handleVerify() {
  if (otpCode.value.length < 6) return
  error.value = ''
  loading.value = true
  try {
    const data = await authStore.verifyOtp(userId.value, otpCode.value)
    if (data.bloqueado === true) {
      const role = data.role || authStore.user?.role
      if (data.bloqueadoPorAdmin === true) {
        try {
          sessionStorage.setItem('motivoBloqueio', data.motivoBloqueio || 'Acesso bloqueado. Entre em contato com o responsavel pelo sistema.')
        } catch (_) {}
        router.push('/acesso-bloqueado')
        return
      }
      if (role === 'Admin') {
        router.push('/consultor/faturas')
        return
      }
      try {
        sessionStorage.setItem('motivoBloqueio', data.motivoBloqueio || 'Assinatura vencida. Entre em contato com seu consultor.')
      } catch (_) {}
      router.push('/acesso-bloqueado')
      return
    }
    if (data.clienteExcluido && !data.autoGestaoAtiva) {
      router.push('/cliente/pos-exclusao')
      return
    }
    toast.success(`Bem-vindo(a), ${authStore.user?.nome || 'usuario'}!`)
    router.push(authStore.dashboardRoute)
  } catch (e) {
    const msg = e.response?.data?.mensagem || 'Codigo invalido ou expirado. Tente novamente.'
    error.value = msg
    showShake.value = true
    toast.error(msg)
    // Limpar campos
    digits.value = ['', '', '', '', '', '']
    otpRefs.value[0]?.focus()
    setTimeout(() => { showShake.value = false }, 500)
  } finally {
    loading.value = false
  }
}

async function handleResend() {
  resending.value = true
  try {
    await authApi.resendOtp({ userId: userId.value })
    toast.success('Novo codigo enviado para seu e-mail!')
    startTimer()
    resendCooldown.value = 30
    clearInterval(cooldownInterval)
    cooldownInterval = setInterval(() => {
      if (resendCooldown.value > 0) {
        resendCooldown.value--
      } else {
        clearInterval(cooldownInterval)
      }
    }, 1000)
    // Limpar campos
    digits.value = ['', '', '', '', '', '']
    error.value = ''
    otpRefs.value[0]?.focus()
  } catch (e) {
    toast.error('Falha ao reenviar codigo. Tente novamente.')
  } finally {
    resending.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1.5rem;
  position: relative;
  overflow: hidden;
}

.login-page__bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgb(99 102 241) 0%, rgb(139 92 246) 50%, rgb(79 70 229) 100%);
}

.login-page__shape {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.4;
}
.login-page__shape--1 {
  width: 400px;
  height: 400px;
  background: rgb(255 255 255);
  top: -100px;
  right: -100px;
  animation: float 15s ease-in-out infinite;
}
.login-page__shape--2 {
  width: 300px;
  height: 300px;
  background: rgb(196 181 253);
  bottom: -50px;
  left: -50px;
  animation: float 18s ease-in-out infinite reverse;
}
.login-page__shape--3 {
  width: 200px;
  height: 200px;
  background: rgb(165 180 252);
  top: 50%;
  left: 30%;
  animation: float 12s ease-in-out infinite;
  animation-delay: -4s;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(20px, -20px); }
}

.login-page__content {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 420px;
}

.login-card {
  background: rgba(255, 255, 255, 0.98);
  border-radius: 20px;
  padding: 2.5rem;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  backdrop-filter: blur(12px);
  animation: card-in 0.5s ease-out;
}
.login-card--shaking {
  animation: shake 0.5s ease-in-out;
}

@keyframes card-in {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  20% { transform: translateX(-8px); }
  40% { transform: translateX(8px); }
  60% { transform: translateX(-6px); }
  80% { transform: translateX(6px); }
}

.login-card__brand {
  text-align: center;
  margin-bottom: 1.5rem;
}

.login-card__logo {
  width: 64px;
  height: 64px;
  margin: 0 auto 1rem;
  background: linear-gradient(135deg, rgb(99 102 241), rgb(139 92 246));
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 24px rgba(99, 102, 241, 0.35);
}

.login-card__logo-icon {
  font-size: 2rem;
  color: white;
  font-weight: 700;
  line-height: 1;
}

.login-card__title {
  font-size: 1.75rem;
  font-weight: 700;
  color: rgb(17 24 39);
  letter-spacing: -0.03em;
  margin: 0 0 0.5rem;
}

.login-card__tagline {
  font-size: 0.9375rem;
  color: rgb(107 114 128);
  line-height: 1.5;
  margin: 0;
}

.otp-card__info {
  text-align: center;
  margin-bottom: 1.5rem;
}

.otp-card__description {
  font-size: 0.9375rem;
  color: rgb(107 114 128);
  line-height: 1.5;
  margin: 0;
}

.login-card__form {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.otp-card__inputs {
  display: flex;
  gap: 0.5rem;
  justify-content: center;
}

.otp-card__digit {
  width: 48px;
  height: 56px;
  text-align: center;
  font-size: 1.5rem;
  font-weight: 700;
  color: rgb(17 24 39);
  border: 2px solid rgb(229 231 235);
  border-radius: 12px;
  background: rgb(249 250 251);
  outline: none;
  transition: border-color 0.2s, box-shadow 0.2s;
}
.otp-card__digit:focus {
  border-color: rgb(99 102 241);
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.15);
  background: white;
}
.otp-card__digit:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.otp-card__timer {
  text-align: center;
  font-size: 0.875rem;
  color: rgb(107 114 128);
}
.otp-card__timer--expired {
  color: rgb(239 68 68);
  font-weight: 600;
}

.login-card__error {
  margin: 0;
  padding: 0.75rem 1rem;
  background: rgb(254 242 242);
  color: rgb(185 28 28);
  font-size: 0.875rem;
  border-radius: 8px;
  border: 1px solid rgb(254 202 202);
}

.login-card__submit {
  width: 100%;
  padding: 0.875rem 1.5rem;
  font-size: 1rem;
  margin-top: 0.25rem;
}

.login-card__loading {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.login-card__spinner {
  width: 1.125rem;
  height: 1.125rem;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.login-card__links {
  margin-top: 1.25rem;
  text-align: center;
}

.otp-card__resend {
  display: inline-block;
  background: none;
  border: none;
  color: rgb(99 102 241);
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  padding: 0.5rem 1rem;
  border-radius: 8px;
  transition: background 0.2s, color 0.2s;
  margin-bottom: 0.5rem;
}
.otp-card__resend:hover:not(:disabled) {
  background: rgb(238 242 255);
  color: rgb(79 70 229);
}
.otp-card__resend:disabled {
  color: rgb(156 163 175);
  cursor: not-allowed;
}

.login-card__hint {
  margin: 0;
  font-size: 0.8125rem;
}

.login-card__link {
  color: rgb(99 102 241);
  text-decoration: none;
  font-weight: 600;
  transition: color 0.2s;
}
.login-card__link:hover {
  color: rgb(79 70 229);
  text-decoration: underline;
}

.login-card__link--subtle {
  font-weight: 500;
  color: rgb(156 163 175);
}
.login-card__link--subtle:hover {
  color: rgb(99 102 241);
}
</style>

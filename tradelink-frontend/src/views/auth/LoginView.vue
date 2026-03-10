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
          <p class="login-card__tagline">Consultoria de trading com recomendações e acompanhamento em tempo real.</p>
        </div>

        <form @submit.prevent="handleLogin" class="login-card__form">
          <div class="login-card__field">
            <label for="login-email" class="login-card__label">E-mail</label>
            <input
              id="login-email"
              v-model="email"
              type="email"
              required
              autocomplete="email"
              class="input-base login-card__input"
              placeholder="seu@email.com"
              :disabled="loading"
            />
          </div>
          <div class="login-card__field">
            <label for="login-senha" class="login-card__label">Senha</label>
            <input
              id="login-senha"
              v-model="senha"
              type="password"
              required
              autocomplete="current-password"
              class="input-base login-card__input"
              placeholder="Sua senha"
              :disabled="loading"
            />
          </div>

          <p v-if="error" class="login-card__error" role="alert">
            {{ error }}
          </p>

          <button
            type="submit"
            class="btn-primary login-card__submit"
            :disabled="loading"
          >
            <span v-if="!loading">Entrar</span>
            <span v-else class="login-card__loading">
              <span class="login-card__spinner" aria-hidden="true"></span>
              Entrando...
            </span>
          </button>
        </form>

        <div class="login-card__links">
          <p class="login-card__hint">Use suas credenciais fornecidas pela sua empresa.</p>
          <p class="login-card__signup">
            Não tem conta? <router-link to="/cadastro" class="login-card__link">Cadastre-se gratuitamente</router-link>
          </p>
          <p class="login-card__faq-link">
            <router-link to="/faq" class="login-card__link login-card__link--subtle">Dúvidas? Consulte nosso FAQ</router-link>
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import { useToast } from '../../composables/useToast'

const router = useRouter()
const authStore = useAuthStore()
const toast = useToast()

const email = ref('')
const senha = ref('')
const error = ref('')
const loading = ref(false)
const showShake = ref(false)

async function handleLogin() {
  error.value = ''
  loading.value = true
  try {
    const data = await authStore.login(email.value, senha.value)
    if (data.bloqueado === true) {
      const role = data.role || authStore.user?.role
      // Bloqueio por admin ou empresa inativa: todos vão para tela de bloqueio
      if (data.bloqueadoPorAdmin === true) {
        try {
          sessionStorage.setItem('motivoBloqueio', data.motivoBloqueio || 'Acesso bloqueado. Entre em contato com o responsável pelo sistema.')
        } catch (_) {}
        router.push('/acesso-bloqueado')
        return
      }
      // Bloqueio por pagamento: consultor vai para faturas, cliente para tela de bloqueio
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
    // Cliente excluído (sem empresa, ativo) → tela pós-exclusão
    if (data.clienteExcluido && !data.autoGestaoAtiva) {
      router.push('/cliente/pos-exclusao')
      return
    }
    toast.success(`Bem-vindo(a), ${authStore.user?.nome || 'usuário'}!`)
    router.push(authStore.dashboardRoute)
  } catch (e) {
    const msg = e.response?.data?.mensagem || 'E-mail ou senha incorretos. Tente novamente.'
    error.value = msg
    showShake.value = true
    toast.error(msg)
    setTimeout(() => { showShake.value = false }, 500)
  } finally {
    loading.value = false
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
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
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
  margin-bottom: 2rem;
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

.login-card__form {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.login-card__field {
  display: flex;
  flex-direction: column;
  gap: 0.375rem;
}

.login-card__label {
  font-size: 0.875rem;
  font-weight: 500;
  color: rgb(55 65 81);
}

.login-card__input {
  border: 1px solid rgb(229 231 235);
}
.login-card__input:focus {
  border-color: rgb(99 102 241);
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.15);
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

.login-card__hint {
  margin: 0 0 0.75rem;
  font-size: 0.8125rem;
  color: rgb(156 163 175);
}

.login-card__signup {
  margin: 0 0 0.5rem;
  font-size: 0.875rem;
  color: rgb(107 114 128);
}

.login-card__faq-link {
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

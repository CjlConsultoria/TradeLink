<template>
  <div class="login-page">
    <div class="login-page__bg">
      <div class="login-page__shape login-page__shape--1"></div>
      <div class="login-page__shape login-page__shape--2"></div>
      <div class="login-page__shape login-page__shape--3"></div>
    </div>

    <div class="login-page__content">
      <div class="login-card">
        <div class="login-card__brand">
          <div class="login-card__logo" aria-hidden="true">
            <span class="login-card__logo-icon">◈</span>
          </div>
          <h1 class="login-card__title">TradeLink</h1>
          <p class="login-card__tagline">Recuperacao de senha</p>
        </div>

        <!-- Estado: formulário -->
        <template v-if="!sent">
          <p class="forgot-card__description">
            Informe o e-mail da sua conta e enviaremos um link para redefinir sua senha.
          </p>

          <form @submit.prevent="handleSubmit" class="login-card__form">
            <div class="login-card__field">
              <label for="forgot-email" class="login-card__label">E-mail</label>
              <input
                id="forgot-email"
                v-model="email"
                type="email"
                required
                autocomplete="email"
                class="input-base login-card__input"
                placeholder="seu@email.com"
                :disabled="loading"
              />
            </div>

            <p v-if="error" class="login-card__error" role="alert">
              {{ error }}
            </p>

            <button
              type="submit"
              class="btn-primary login-card__submit"
              :disabled="loading || !email"
            >
              <span v-if="!loading">Enviar Link</span>
              <span v-else class="login-card__loading">
                <span class="login-card__spinner" aria-hidden="true"></span>
                Enviando...
              </span>
            </button>
          </form>
        </template>

        <!-- Estado: enviado -->
        <template v-else>
          <div class="forgot-card__success">
            <div class="forgot-card__success-icon">&#9993;</div>
            <h2 class="forgot-card__success-title">E-mail enviado!</h2>
            <p class="forgot-card__success-text">
              Se o e-mail <strong>{{ email }}</strong> estiver cadastrado, voce recebera um link para redefinir sua senha.
            </p>
            <p class="forgot-card__success-hint">
              Verifique sua caixa de entrada e a pasta de spam.
            </p>
          </div>
        </template>

        <div class="login-card__links">
          <p class="login-card__hint">
            <router-link to="/login" class="login-card__link login-card__link--subtle">Voltar ao login</router-link>
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useToast } from '../../composables/useToast'
import authApi from '../../api/authApi'

const toast = useToast()

const email = ref('')
const error = ref('')
const loading = ref(false)
const sent = ref(false)

async function handleSubmit() {
  error.value = ''
  loading.value = true
  try {
    await authApi.forgotPassword({ email: email.value })
    sent.value = true
    toast.success('Verifique seu e-mail para redefinir a senha.')
  } catch (e) {
    error.value = e.response?.data?.mensagem || 'Erro ao processar solicitacao. Tente novamente.'
    toast.error(error.value)
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

@keyframes card-in {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
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

.forgot-card__description {
  text-align: center;
  font-size: 0.9375rem;
  color: rgb(107 114 128);
  line-height: 1.5;
  margin: 0 0 1.5rem;
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

/* Success state */
.forgot-card__success {
  text-align: center;
  padding: 1rem 0;
}

.forgot-card__success-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.forgot-card__success-title {
  font-size: 1.25rem;
  font-weight: 700;
  color: rgb(17 24 39);
  margin: 0 0 0.75rem;
}

.forgot-card__success-text {
  font-size: 0.9375rem;
  color: rgb(107 114 128);
  line-height: 1.5;
  margin: 0 0 0.5rem;
}

.forgot-card__success-hint {
  font-size: 0.8125rem;
  color: rgb(156 163 175);
  margin: 0;
}
</style>

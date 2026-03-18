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
          <p class="login-card__tagline">Redefinir senha</p>
        </div>

        <!-- Estado: formulário -->
        <template v-if="!done">
          <form @submit.prevent="handleReset" class="login-card__form">
            <div class="login-card__field">
              <label for="new-password" class="login-card__label">Nova senha</label>
              <input
                id="new-password"
                v-model="novaSenha"
                type="password"
                required
                minlength="6"
                autocomplete="new-password"
                class="input-base login-card__input"
                placeholder="Minimo 6 caracteres"
                :disabled="loading"
              />
            </div>

            <div class="login-card__field">
              <label for="confirm-password" class="login-card__label">Confirmar nova senha</label>
              <input
                id="confirm-password"
                v-model="confirmSenha"
                type="password"
                required
                minlength="6"
                autocomplete="new-password"
                class="input-base login-card__input"
                placeholder="Repita a nova senha"
                :disabled="loading"
              />
            </div>

            <p v-if="error" class="login-card__error" role="alert">
              {{ error }}
            </p>

            <button
              type="submit"
              class="btn-primary login-card__submit"
              :disabled="loading || !novaSenha || !confirmSenha"
            >
              <span v-if="!loading">Redefinir Senha</span>
              <span v-else class="login-card__loading">
                <span class="login-card__spinner" aria-hidden="true"></span>
                Redefinindo...
              </span>
            </button>
          </form>
        </template>

        <!-- Estado: sucesso -->
        <template v-else>
          <div class="forgot-card__success">
            <div class="forgot-card__success-icon">&#10003;</div>
            <h2 class="forgot-card__success-title">Senha redefinida!</h2>
            <p class="forgot-card__success-text">
              Sua senha foi alterada com sucesso. Voce ja pode fazer login com a nova senha.
            </p>

            <router-link to="/login" class="btn-primary login-card__submit" style="display:inline-block; text-align:center; text-decoration:none; margin-top:1rem;">
              Ir para Login
            </router-link>
          </div>
        </template>

        <div v-if="!done" class="login-card__links">
          <p class="login-card__hint">
            <router-link to="/login" class="login-card__link login-card__link--subtle">Voltar ao login</router-link>
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useToast } from '../../composables/useToast'
import authApi from '../../api/authApi'

const router = useRouter()
const route = useRoute()
const toast = useToast()

const token = ref('')
const novaSenha = ref('')
const confirmSenha = ref('')
const error = ref('')
const loading = ref(false)
const done = ref(false)

onMounted(() => {
  token.value = route.query.token || ''
  if (!token.value) {
    toast.error('Link invalido ou expirado.')
    router.push('/forgot-password')
  }
})

async function handleReset() {
  error.value = ''

  if (novaSenha.value.length < 6) {
    error.value = 'A senha deve ter pelo menos 6 caracteres.'
    return
  }

  if (novaSenha.value !== confirmSenha.value) {
    error.value = 'As senhas nao coincidem.'
    return
  }

  loading.value = true
  try {
    await authApi.resetPassword({ token: token.value, novaSenha: novaSenha.value })
    done.value = true
    toast.success('Senha redefinida com sucesso!')
  } catch (e) {
    error.value = e.response?.data?.mensagem || 'Erro ao redefinir senha. O link pode ter expirado.'
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
  width: 56px;
  height: 56px;
  margin: 0 auto 1rem;
  background: rgb(220 252 231);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  color: rgb(22 163 74);
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
  margin: 0;
}
</style>

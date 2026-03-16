<template>
  <Teleport to="body">
    <Transition name="terms-fade">
      <div v-if="visible" class="terms-overlay">
        <Transition name="terms-scale">
          <div v-if="visible" class="terms-modal">
            <div class="terms-modal__header">
              <div class="terms-modal__icon">
                <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                  <polyline points="14 2 14 8 20 8"/>
                  <line x1="16" y1="13" x2="8" y2="13"/>
                  <line x1="16" y1="17" x2="8" y2="17"/>
                  <polyline points="10 9 9 9 8 9"/>
                </svg>
              </div>
              <h3 class="terms-modal__title">Termos de Uso e Privacidade</h3>
              <p class="terms-modal__subtitle">Para continuar usando a TradeLink, leia e aceite nossos termos.</p>
            </div>

            <div class="terms-modal__body">
              <div class="terms-modal__scroll">
                <h4>1. Objeto</h4>
                <p>A plataforma TradeLink e uma ferramenta de gestao e acompanhamento de investimentos que conecta consultores financeiros a seus clientes.</p>

                <h4>2. Natureza do Servico</h4>
                <p>O TradeLink nao constitui recomendacao de investimento. As informacoes fornecidas sao de carater educativo e informativo. Decisoes de investimento sao de responsabilidade exclusiva do usuario.</p>

                <h4>3. Isenção de Responsabilidade Financeira</h4>
                <p>A plataforma nao se responsabiliza por perdas financeiras decorrentes de operacoes realizadas com base nas informacoes disponibilizadas. Rentabilidade passada nao garante rentabilidade futura.</p>

                <h4>4. Dados e Privacidade (LGPD)</h4>
                <p>Coletamos apenas os dados necessarios para o funcionamento da plataforma: nome, email, dados de investimento e preferencias. Seus dados sao armazenados de forma segura e nao sao compartilhados com terceiros sem seu consentimento, conforme a Lei Geral de Protecao de Dados (Lei 13.709/2018).</p>

                <h4>5. Cookies</h4>
                <p>Utilizamos cookies essenciais para manter sua sessao ativa e cookies analiticos para melhorar a experiencia. Voce pode gerenciar suas preferencias de cookies a qualquer momento.</p>

                <h4>6. Recomendacoes de Consultores</h4>
                <p>As recomendacoes feitas por consultores na plataforma refletem a opiniao individual de cada profissional. O TradeLink nao endossa nem se responsabiliza pelas recomendacoes individuais.</p>

                <h4>7. Responsabilidade do Usuario</h4>
                <p>O usuario e responsavel por manter suas credenciais seguras, fornecer informacoes veridicas, e tomar decisoes de investimento de forma consciente e informada.</p>

                <h4>8. Periodo de Trial e Pagamento</h4>
                <p>O periodo de trial gratuito tem duracao de 5 dias corridos. Apos o trial, e necessario escolher um plano pago para continuar acessando. Cancelamentos podem ser solicitados a qualquer momento.</p>

                <h4>9. Alteracoes</h4>
                <p>A plataforma pode ser atualizada sem aviso previo para melhorias e correcoes. Alteracoes nos termos serao comunicadas via email ou notificacao na plataforma.</p>
              </div>
            </div>

            <div class="terms-modal__footer">
              <label class="terms-modal__check">
                <input type="checkbox" v-model="accepted" />
                <span>Li e aceito os Termos de Uso e a Politica de Privacidade</span>
              </label>
              <button
                type="button"
                class="terms-modal__btn"
                :disabled="!accepted"
                @click="onAccept"
              >
                Continuar
              </button>
            </div>
          </div>
        </Transition>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useAuthStore } from '../../stores/auth'

const STORAGE_KEY = 'tradelink_terms_accepted'
const visible = ref(false)
const accepted = ref(false)

const authStore = useAuthStore()

function shouldShow() {
  // Only show for logged-in users who haven't accepted yet
  if (!authStore.user) return false
  try {
    return !localStorage.getItem(STORAGE_KEY)
  } catch {
    return true
  }
}

onMounted(() => {
  if (shouldShow()) {
    visible.value = true
  }
})

// Watch for login state changes
watch(() => authStore.user, (user) => {
  if (user && shouldShow()) {
    visible.value = true
  }
})

function onAccept() {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify({
      date: new Date().toISOString(),
      userId: authStore.user?.id || 'unknown'
    }))
  } catch { /* ignore */ }
  visible.value = false
}
</script>

<style>
.terms-overlay {
  position: fixed;
  inset: 0;
  z-index: 10000;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  padding: 1rem;
}

.terms-modal {
  background: #fff;
  border-radius: 16px;
  max-width: 600px;
  width: 100%;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.3);
  overflow: hidden;
}

.terms-modal__header {
  padding: 1.5rem 1.5rem 1rem;
  text-align: center;
  flex-shrink: 0;
}

.terms-modal__icon {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: #eef2ff;
  color: #6366f1;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 1rem;
}

.terms-modal__title {
  font-size: 1.25rem;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 0.375rem;
}

.terms-modal__subtitle {
  font-size: 0.875rem;
  color: #64748b;
  margin: 0;
}

.terms-modal__body {
  flex: 1;
  min-height: 0;
  overflow: hidden;
  padding: 0 1.5rem;
}

.terms-modal__scroll {
  max-height: 40vh;
  overflow-y: auto;
  padding: 1rem;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  font-size: 0.8125rem;
  color: #475569;
  line-height: 1.6;
}

.terms-modal__scroll h4 {
  font-size: 0.875rem;
  font-weight: 600;
  color: #1e293b;
  margin: 1rem 0 0.375rem;
}
.terms-modal__scroll h4:first-child {
  margin-top: 0;
}

.terms-modal__scroll p {
  margin: 0 0 0.5rem;
}

.terms-modal__footer {
  padding: 1rem 1.5rem 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  flex-shrink: 0;
}

.terms-modal__check {
  display: flex;
  align-items: flex-start;
  gap: 0.5rem;
  cursor: pointer;
  font-size: 0.875rem;
  color: #1e293b;
  user-select: none;
}

.terms-modal__check input[type="checkbox"] {
  margin-top: 2px;
  accent-color: #6366f1;
  width: 18px;
  height: 18px;
  flex-shrink: 0;
}

.terms-modal__btn {
  width: 100%;
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 10px;
  background: #6366f1;
  color: #fff;
  font-size: 0.9375rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s, opacity 0.15s;
  font-family: inherit;
}
.terms-modal__btn:hover:not(:disabled) {
  background: #4f46e5;
}
.terms-modal__btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Mobile */
@media (max-width: 639px) {
  .terms-overlay {
    padding: 0.5rem;
    align-items: flex-end;
  }

  .terms-modal {
    max-height: 95vh;
    border-radius: 16px 16px 0 0;
    max-width: 100%;
  }

  .terms-modal__header {
    padding: 1.25rem 1rem 0.75rem;
  }

  .terms-modal__title {
    font-size: 1.125rem;
  }

  .terms-modal__body {
    padding: 0 1rem;
  }

  .terms-modal__scroll {
    max-height: 35vh;
  }

  .terms-modal__footer {
    padding: 1rem;
  }
}

/* Dark mode */
[data-theme="dark"] .terms-modal {
  background: #1e293b;
}
[data-theme="dark"] .terms-modal__title {
  color: #f1f5f9;
}
[data-theme="dark"] .terms-modal__subtitle {
  color: #94a3b8;
}
[data-theme="dark"] .terms-modal__scroll {
  background: #0f172a;
  border-color: #334155;
  color: #cbd5e1;
}
[data-theme="dark"] .terms-modal__scroll h4 {
  color: #f1f5f9;
}
[data-theme="dark"] .terms-modal__check {
  color: #f1f5f9;
}
[data-theme="dark"] .terms-modal__icon {
  background: rgba(99, 102, 241, 0.15);
}

/* Transitions */
.terms-fade-enter-active {
  transition: opacity 0.3s ease;
}
.terms-fade-leave-active {
  transition: opacity 0.2s ease;
}
.terms-fade-enter-from,
.terms-fade-leave-to {
  opacity: 0;
}

.terms-scale-enter-active {
  transition: transform 0.3s ease, opacity 0.3s ease;
}
.terms-scale-leave-active {
  transition: transform 0.2s ease, opacity 0.2s ease;
}
.terms-scale-enter-from {
  transform: scale(0.9);
  opacity: 0;
}
.terms-scale-leave-to {
  transform: scale(0.95);
  opacity: 0;
}
</style>

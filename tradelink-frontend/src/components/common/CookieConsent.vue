<template>
  <Teleport to="body">
    <Transition name="cookie-slide">
      <div v-if="visible" class="cookie-banner">
        <div class="cookie-banner__content">
          <div class="cookie-banner__text">
            <p class="cookie-banner__title">Cookies e Privacidade</p>
            <p class="cookie-banner__desc">
              Utilizamos cookies essenciais para o funcionamento da plataforma e cookies analiticos para melhorar sua experiencia.
              Seus dados sao tratados conforme a LGPD (Lei Geral de Protecao de Dados).
            </p>
          </div>
          <div class="cookie-banner__actions">
            <button type="button" class="cookie-btn cookie-btn--outline" @click="acceptEssential">
              Apenas essenciais
            </button>
            <button type="button" class="cookie-btn cookie-btn--primary" @click="acceptAll">
              Aceitar todos
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const STORAGE_KEY = 'tradelink_cookie_consent'
const visible = ref(false)

onMounted(() => {
  try {
    const consent = localStorage.getItem(STORAGE_KEY)
    if (!consent) {
      visible.value = true
    }
  } catch {
    visible.value = true
  }
})

function acceptAll() {
  saveConsent('all')
}

function acceptEssential() {
  saveConsent('essential')
}

function saveConsent(level) {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify({
      level,
      date: new Date().toISOString()
    }))
  } catch { /* ignore */ }
  visible.value = false
}
</script>

<style>
.cookie-banner {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 9998;
  background: #fff;
  border-top: 1px solid #e5e7eb;
  box-shadow: 0 -4px 24px -4px rgba(0, 0, 0, 0.12);
  padding: 1rem;
}

.cookie-banner__content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.cookie-banner__text {
  flex: 1;
  min-width: 0;
}

.cookie-banner__title {
  font-size: 0.9375rem;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 0.25rem;
}

.cookie-banner__desc {
  font-size: 0.8125rem;
  color: #64748b;
  line-height: 1.5;
  margin: 0;
}

.cookie-banner__actions {
  display: flex;
  gap: 0.5rem;
  flex-shrink: 0;
}

.cookie-btn {
  padding: 0.5rem 1rem;
  border-radius: 8px;
  font-size: 0.8125rem;
  font-weight: 600;
  cursor: pointer;
  border: none;
  white-space: nowrap;
  transition: background 0.15s, color 0.15s;
  font-family: inherit;
}

.cookie-btn--primary {
  background: #6366f1;
  color: #fff;
}
.cookie-btn--primary:hover {
  background: #4f46e5;
}

.cookie-btn--outline {
  background: transparent;
  color: #6366f1;
  border: 1px solid #c7d2fe;
}
.cookie-btn--outline:hover {
  background: #eef2ff;
}

/* Mobile */
@media (max-width: 639px) {
  .cookie-banner {
    padding: 1rem;
  }

  .cookie-banner__content {
    flex-direction: column;
    align-items: stretch;
    gap: 1rem;
  }

  .cookie-banner__actions {
    flex-direction: column;
    gap: 0.5rem;
  }

  .cookie-btn {
    width: 100%;
    padding: 0.75rem 1rem;
    text-align: center;
  }
}

/* Slide transition */
.cookie-slide-enter-active {
  transition: transform 0.4s ease, opacity 0.3s ease;
}
.cookie-slide-leave-active {
  transition: transform 0.3s ease, opacity 0.2s ease;
}
.cookie-slide-enter-from {
  transform: translateY(100%);
  opacity: 0;
}
.cookie-slide-leave-to {
  transform: translateY(100%);
  opacity: 0;
}

/* Dark mode */
[data-theme="dark"] .cookie-banner {
  background: #1e293b;
  border-top-color: #334155;
}
[data-theme="dark"] .cookie-banner__title {
  color: #f1f5f9;
}
[data-theme="dark"] .cookie-banner__desc {
  color: #94a3b8;
}
[data-theme="dark"] .cookie-btn--outline {
  color: #a5b4fc;
  border-color: #4338ca;
}
[data-theme="dark"] .cookie-btn--outline:hover {
  background: rgba(99, 102, 241, 0.1);
}
</style>

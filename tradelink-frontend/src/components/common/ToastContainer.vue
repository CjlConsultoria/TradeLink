<template>
  <Teleport to="body">
    <div class="toast-container" aria-live="polite" aria-atomic="false">
      <TransitionGroup name="toast">
        <div
          v-for="t in toasts"
          :key="t.id"
          class="toast"
          :class="`toast--${t.type}`"
          role="alert"
          @mouseenter="pause(t.id)"
          @mouseleave="resume(t.id)"
        >
          <!-- Ícone SVG -->
          <div class="toast__icon-wrap">
            <svg v-if="t.type === 'success'" class="toast__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
              <path d="M20 6L9 17l-5-5"/>
            </svg>
            <svg v-else-if="t.type === 'error'" class="toast__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10"/>
              <line x1="15" y1="9" x2="9" y2="15"/>
              <line x1="9" y1="9" x2="15" y2="15"/>
            </svg>
            <svg v-else-if="t.type === 'warning'" class="toast__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
              <path d="M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"/>
              <line x1="12" y1="9" x2="12" y2="13"/>
              <line x1="12" y1="17" x2="12.01" y2="17"/>
            </svg>
            <svg v-else class="toast__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10"/>
              <line x1="12" y1="16" x2="12" y2="12"/>
              <line x1="12" y1="8" x2="12.01" y2="8"/>
            </svg>
          </div>

          <!-- Conteúdo -->
          <div class="toast__content">
            <p class="toast__title">{{ titleFor(t.type) }}</p>
            <p class="toast__message">{{ t.message }}</p>
          </div>

          <!-- Botão fechar -->
          <button
            type="button"
            class="toast__close"
            aria-label="Fechar"
            @click="remove(t.id)"
          >
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="18" y1="6" x2="6" y2="18"/>
              <line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
          </button>

          <!-- Barra de progresso -->
          <div v-if="t.duration > 0" class="toast__progress">
            <div
              class="toast__progress-bar"
              :style="{ animationDuration: t.duration + 'ms', animationPlayState: t.paused ? 'paused' : 'running' }"
            />
          </div>
        </div>
      </TransitionGroup>
    </div>
  </Teleport>
</template>

<script setup>
import { useToast } from '../../composables/useToast'

const { toasts, remove, pause, resume } = useToast()

function titleFor(type) {
  const titles = {
    success: 'Sucesso',
    error: 'Erro',
    warning: 'Atenção',
    info: 'Informação'
  }
  return titles[type] ?? ''
}
</script>

<style scoped>
.toast-container {
  position: fixed;
  top: 1.25rem;
  right: 1.25rem;
  z-index: 99999;
  display: flex;
  flex-direction: column;
  gap: 0.625rem;
  max-width: 26rem;
  width: calc(100vw - 2rem);
  pointer-events: none;
}

@media (max-width: 480px) {
  .toast-container {
    top: 0.75rem;
    right: 0.75rem;
    left: 0.75rem;
    max-width: none;
    width: auto;
  }
}

.toast-container > * {
  pointer-events: auto;
}

/* ── Toast base ── */
.toast {
  position: relative;
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  padding: 1rem 1.125rem;
  border-radius: 0.75rem;
  border: 1px solid transparent;
  overflow: hidden;
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  box-shadow:
    0 4px 6px -1px rgba(0, 0, 0, 0.07),
    0 10px 20px -2px rgba(0, 0, 0, 0.05),
    0 0 0 1px rgba(0, 0, 0, 0.03);
  cursor: default;
  transition: box-shadow 0.2s ease, transform 0.15s ease;
}

.toast:hover {
  box-shadow:
    0 8px 16px -2px rgba(0, 0, 0, 0.1),
    0 16px 32px -4px rgba(0, 0, 0, 0.08),
    0 0 0 1px rgba(0, 0, 0, 0.05);
  transform: translateY(-1px);
}

/* ── Ícone ── */
.toast__icon-wrap {
  flex-shrink: 0;
  width: 2rem;
  height: 2rem;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 0.5rem;
}

.toast__icon {
  width: 1.125rem;
  height: 1.125rem;
}

/* ── Conteúdo ── */
.toast__content {
  flex: 1;
  min-width: 0;
  padding-top: 0.125rem;
}

.toast__title {
  margin: 0;
  font-size: 0.8125rem;
  font-weight: 600;
  letter-spacing: 0.01em;
  line-height: 1.2;
}

.toast__message {
  margin: 0.25rem 0 0;
  font-size: 0.8125rem;
  font-weight: 400;
  line-height: 1.45;
  opacity: 0.85;
}

/* ── Botão fechar ── */
.toast__close {
  flex-shrink: 0;
  width: 1.5rem;
  height: 1.5rem;
  padding: 0.25rem;
  border: none;
  background: transparent;
  color: inherit;
  opacity: 0;
  cursor: pointer;
  border-radius: 0.375rem;
  transition: opacity 0.2s, background 0.15s;
}

.toast:hover .toast__close {
  opacity: 0.5;
}

.toast__close:hover {
  opacity: 1 !important;
  background: rgba(0, 0, 0, 0.06);
}

.toast__close svg {
  width: 100%;
  height: 100%;
}

/* ── Barra de progresso ── */
.toast__progress {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.toast__progress-bar {
  height: 100%;
  width: 100%;
  transform-origin: left;
  animation: progress-shrink linear forwards;
  border-radius: 0 3px 3px 0;
}

@keyframes progress-shrink {
  from { transform: scaleX(1); }
  to { transform: scaleX(0); }
}

/* ═══════════════════════════════════════
   Variantes de cor
   ═══════════════════════════════════════ */

/* ── Success ── */
.toast--success {
  background: linear-gradient(135deg, #ecfdf5 0%, #f0fdf4 100%);
  border-color: #bbf7d0;
  color: #14532d;
}

.toast--success .toast__icon-wrap {
  background: #dcfce7;
  color: #16a34a;
}

.toast--success .toast__progress-bar {
  background: linear-gradient(90deg, #22c55e, #16a34a);
}

/* ── Error ── */
.toast--error {
  background: linear-gradient(135deg, #fef2f2 0%, #fff1f2 100%);
  border-color: #fecaca;
  color: #7f1d1d;
}

.toast--error .toast__icon-wrap {
  background: #fee2e2;
  color: #dc2626;
}

.toast--error .toast__progress-bar {
  background: linear-gradient(90deg, #ef4444, #dc2626);
}

/* ── Warning ── */
.toast--warning {
  background: linear-gradient(135deg, #fffbeb 0%, #fefce8 100%);
  border-color: #fde68a;
  color: #78350f;
}

.toast--warning .toast__icon-wrap {
  background: #fef3c7;
  color: #d97706;
}

.toast--warning .toast__progress-bar {
  background: linear-gradient(90deg, #f59e0b, #d97706);
}

/* ── Info ── */
.toast--info {
  background: linear-gradient(135deg, #eff6ff 0%, #f0f9ff 100%);
  border-color: #bfdbfe;
  color: #1e3a5f;
}

.toast--info .toast__icon-wrap {
  background: #dbeafe;
  color: #2563eb;
}

.toast--info .toast__progress-bar {
  background: linear-gradient(90deg, #3b82f6, #2563eb);
}

/* ═══════════════════════════════════════
   Transições
   ═══════════════════════════════════════ */

.toast-enter-active {
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}

.toast-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 1, 1);
}

.toast-enter-from {
  opacity: 0;
  transform: translateX(100%) scale(0.95);
}

.toast-leave-to {
  opacity: 0;
  transform: translateX(30%) scale(0.95);
}

.toast-move {
  transition: transform 0.35s cubic-bezier(0.16, 1, 0.3, 1);
}

/* ═══════════════════════════════════════
   Dark mode
   ═══════════════════════════════════════ */

:root[data-theme="dark"] .toast--success,
.dark .toast--success {
  background: linear-gradient(135deg, #052e16 0%, #064e3b 100%);
  border-color: #065f46;
  color: #bbf7d0;
}

:root[data-theme="dark"] .toast--success .toast__icon-wrap,
.dark .toast--success .toast__icon-wrap {
  background: rgba(22, 163, 74, 0.2);
  color: #4ade80;
}

:root[data-theme="dark"] .toast--error,
.dark .toast--error {
  background: linear-gradient(135deg, #450a0a 0%, #7f1d1d 100%);
  border-color: #991b1b;
  color: #fecaca;
}

:root[data-theme="dark"] .toast--error .toast__icon-wrap,
.dark .toast--error .toast__icon-wrap {
  background: rgba(220, 38, 38, 0.2);
  color: #f87171;
}

:root[data-theme="dark"] .toast--warning,
.dark .toast--warning {
  background: linear-gradient(135deg, #451a03 0%, #78350f 100%);
  border-color: #92400e;
  color: #fde68a;
}

:root[data-theme="dark"] .toast--warning .toast__icon-wrap,
.dark .toast--warning .toast__icon-wrap {
  background: rgba(217, 119, 6, 0.2);
  color: #fbbf24;
}

:root[data-theme="dark"] .toast--info,
.dark .toast--info {
  background: linear-gradient(135deg, #172554 0%, #1e3a5f 100%);
  border-color: #1e40af;
  color: #bfdbfe;
}

:root[data-theme="dark"] .toast--info .toast__icon-wrap,
.dark .toast--info .toast__icon-wrap {
  background: rgba(37, 99, 235, 0.2);
  color: #60a5fa;
}

:root[data-theme="dark"] .toast__progress,
.dark .toast__progress {
  background: rgba(255, 255, 255, 0.08);
}

:root[data-theme="dark"] .toast__close:hover,
.dark .toast__close:hover {
  background: rgba(255, 255, 255, 0.1);
}

:root[data-theme="dark"] .toast,
.dark .toast {
  box-shadow:
    0 4px 6px -1px rgba(0, 0, 0, 0.25),
    0 10px 20px -2px rgba(0, 0, 0, 0.2),
    0 0 0 1px rgba(255, 255, 255, 0.05);
}

:root[data-theme="dark"] .toast:hover,
.dark .toast:hover {
  box-shadow:
    0 8px 16px -2px rgba(0, 0, 0, 0.35),
    0 16px 32px -4px rgba(0, 0, 0, 0.25),
    0 0 0 1px rgba(255, 255, 255, 0.08);
}
</style>

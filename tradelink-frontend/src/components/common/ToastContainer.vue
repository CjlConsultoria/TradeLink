<template>
  <div class="toast-container" aria-live="polite">
    <TransitionGroup name="toast">
      <div
        v-for="t in toasts"
        :key="t.id"
        class="toast"
        :class="`toast--${t.type}`"
        role="alert"
      >
        <span class="toast__icon">{{ icon(t.type) }}</span>
        <p class="toast__message">{{ t.message }}</p>
        <button
          type="button"
          class="toast__close"
          aria-label="Fechar"
          @click="remove(t.id)"
        >
          ×
        </button>
      </div>
    </TransitionGroup>
  </div>
</template>

<script setup>
import { useToast } from '../../composables/useToast'

const { toasts, remove } = useToast()

function icon(type) {
  const icons = { success: '✓', error: '!', warning: '⚠', info: 'ℹ' }
  return icons[type] ?? '•'
}
</script>

<style scoped>
.toast-container {
  position: fixed;
  top: 1rem;
  right: 1rem;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  max-width: 24rem;
  pointer-events: none;
}
.toast-container > * {
  pointer-events: auto;
}

.toast {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  padding: 0.875rem 1rem;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.12), 0 2px 8px rgba(0, 0, 0, 0.08);
  font-size: 0.9375rem;
  line-height: 1.4;
  animation: toast-in 0.35s ease-out;
}
.toast__icon {
  flex-shrink: 0;
  width: 1.5rem;
  height: 1.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  font-weight: 700;
  font-size: 0.875rem;
}
.toast__message {
  flex: 1;
  margin: 0;
  color: inherit;
}
.toast__close {
  flex-shrink: 0;
  width: 1.5rem;
  height: 1.5rem;
  padding: 0;
  border: none;
  background: transparent;
  color: inherit;
  opacity: 0.75;
  font-size: 1.25rem;
  line-height: 1;
  cursor: pointer;
  border-radius: 6px;
  transition: opacity 0.15s;
}
.toast__close:hover {
  opacity: 1;
}

.toast--success {
  background: var(--toast-success-bg, #ecfdf5);
  color: var(--toast-success-text, #065f46);
}
.toast--success .toast__icon {
  background: var(--toast-success-icon-bg, #10b981);
  color: #fff;
}

.toast--error {
  background: var(--toast-error-bg, #fef2f2);
  color: var(--toast-error-text, #991b1b);
}
.toast--error .toast__icon {
  background: var(--toast-error-icon-bg, #ef4444);
  color: #fff;
}

.toast--warning {
  background: var(--toast-warning-bg, #fffbeb);
  color: var(--toast-warning-text, #92400e);
}
.toast--warning .toast__icon {
  background: var(--toast-warning-icon-bg, #f59e0b);
  color: #fff;
}

.toast--info {
  background: var(--toast-info-bg, #eff6ff);
  color: var(--toast-info-text, #1e40af);
}
.toast--info .toast__icon {
  background: var(--toast-info-icon-bg, #3b82f6);
  color: #fff;
}

.toast-enter-active,
.toast-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}
.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateX(1.5rem);
}
.toast-move {
  transition: transform 0.3s ease;
}

@keyframes toast-in {
  from {
    opacity: 0;
    transform: translateX(1.5rem);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}
</style>

<template>
  <Teleport to="body">
    <Transition name="confirm-fade">
      <div v-if="state.visible" class="confirm-overlay" @click.self="onCancel">
        <Transition name="confirm-scale">
          <div v-if="state.visible" class="confirm-modal">
            <div class="confirm-modal__icon" :class="`confirm-modal__icon--${state.variant}`">
              <svg v-if="state.variant === 'danger'" xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
                <line x1="12" y1="9" x2="12" y2="13"/>
                <line x1="12" y1="17" x2="12.01" y2="17"/>
              </svg>
              <svg v-else-if="state.variant === 'warning'" xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"/>
                <line x1="12" y1="8" x2="12" y2="12"/>
                <line x1="12" y1="16" x2="12.01" y2="16"/>
              </svg>
              <svg v-else xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"/>
                <line x1="12" y1="16" x2="12" y2="12"/>
                <line x1="12" y1="8" x2="12.01" y2="8"/>
              </svg>
            </div>

            <h3 class="confirm-modal__title">{{ state.title }}</h3>
            <p class="confirm-modal__message">{{ state.message }}</p>

            <div class="confirm-modal__actions">
              <button type="button" @click="onCancel" class="confirm-modal__btn confirm-modal__btn--cancel">
                {{ state.cancelText }}
              </button>
              <button type="button" @click="onConfirm" class="confirm-modal__btn" :class="`confirm-modal__btn--${state.variant}`">
                {{ state.confirmText }}
              </button>
            </div>
          </div>
        </Transition>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { useConfirm } from '../../composables/useConfirm'
const { state, onConfirm, onCancel } = useConfirm()
</script>

<style>
.confirm-overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  padding: 1rem;
}

.confirm-modal {
  background: white;
  border-radius: 16px;
  padding: 2rem;
  max-width: 420px;
  width: 100%;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  text-align: center;
}

.confirm-modal__icon {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 1rem;
}
.confirm-modal__icon--danger {
  background: rgb(254 242 242);
  color: rgb(239 68 68);
}
.confirm-modal__icon--warning {
  background: rgb(254 252 232);
  color: rgb(234 179 8);
}
.confirm-modal__icon--info {
  background: rgb(238 242 255);
  color: rgb(99 102 241);
}

.confirm-modal__title {
  font-size: 1.125rem;
  font-weight: 600;
  color: rgb(17 24 39);
  margin: 0 0 0.5rem;
}

.confirm-modal__message {
  font-size: 0.875rem;
  color: rgb(107 114 128);
  line-height: 1.6;
  margin: 0 0 1.5rem;
  white-space: pre-line;
}

.confirm-modal__actions {
  display: flex;
  gap: 0.75rem;
  justify-content: center;
}

.confirm-modal__btn {
  padding: 0.625rem 1.5rem;
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 500;
  border: none;
  cursor: pointer;
  transition: background-color 0.2s, transform 0.1s;
}
.confirm-modal__btn:active {
  transform: scale(0.97);
}

.confirm-modal__btn--cancel {
  background: rgb(243 244 246);
  color: rgb(55 65 81);
}
.confirm-modal__btn--cancel:hover {
  background: rgb(229 231 235);
}

.confirm-modal__btn--danger {
  background: rgb(239 68 68);
  color: white;
}
.confirm-modal__btn--danger:hover {
  background: rgb(220 38 38);
}

.confirm-modal__btn--warning {
  background: rgb(234 179 8);
  color: white;
}
.confirm-modal__btn--warning:hover {
  background: rgb(202 138 4);
}

.confirm-modal__btn--info {
  background: rgb(99 102 241);
  color: white;
}
.confirm-modal__btn--info:hover {
  background: rgb(79 70 229);
}

/* Transitions */
.confirm-fade-enter-active {
  transition: opacity 0.2s ease;
}
.confirm-fade-leave-active {
  transition: opacity 0.15s ease;
}
.confirm-fade-enter-from,
.confirm-fade-leave-to {
  opacity: 0;
}

.confirm-scale-enter-active {
  transition: transform 0.2s ease, opacity 0.2s ease;
}
.confirm-scale-leave-active {
  transition: transform 0.15s ease, opacity 0.15s ease;
}
.confirm-scale-enter-from {
  transform: scale(0.9);
  opacity: 0;
}
.confirm-scale-leave-to {
  transform: scale(0.95);
  opacity: 0;
}
</style>

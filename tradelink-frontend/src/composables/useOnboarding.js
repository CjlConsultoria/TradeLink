import { ref, computed } from 'vue'

const STORAGE_PREFIX = 'tradelink_onboarding_done_'

/**
 * Composable de onboarding guiado.
 *
 * @param {string} tourId - Identificador único do tour (ex: 'cliente', 'consultor')
 * @param {Array} steps - Lista de steps: { target, title, message, position? }
 *   - target: CSS selector do elemento a destacar (ex: '.card:first-child')
 *   - title: título do tooltip
 *   - message: texto explicativo
 *   - position: 'bottom' | 'top' | 'left' | 'right' (default: 'bottom')
 */
export function useOnboarding(tourId, steps = []) {
  const active = ref(false)
  const currentStep = ref(0)

  const storageKey = STORAGE_PREFIX + tourId

  const isDone = () => {
    try {
      return localStorage.getItem(storageKey) === 'true'
    } catch {
      return false
    }
  }

  const markDone = () => {
    try {
      localStorage.setItem(storageKey, 'true')
    } catch { /* ignore */ }
  }

  const totalSteps = computed(() => steps.length)
  const step = computed(() => steps[currentStep.value] || null)
  const isFirst = computed(() => currentStep.value === 0)
  const isLast = computed(() => currentStep.value === steps.length - 1)

  function start() {
    if (steps.length === 0) return
    currentStep.value = 0
    active.value = true
  }

  /** Auto-start only if the tour was never completed. */
  function autoStart(delayMs = 800) {
    if (isDone() || steps.length === 0) return
    setTimeout(() => {
      start()
    }, delayMs)
  }

  function next() {
    if (currentStep.value < steps.length - 1) {
      currentStep.value++
    } else {
      finish()
    }
  }

  function prev() {
    if (currentStep.value > 0) {
      currentStep.value--
    }
  }

  function skip() {
    finish()
  }

  function finish() {
    active.value = false
    currentStep.value = 0
    markDone()
  }

  /** Reset so the tour shows again on next visit. */
  function reset() {
    try {
      localStorage.removeItem(storageKey)
    } catch { /* ignore */ }
  }

  return {
    active,
    currentStep,
    totalSteps,
    step,
    isFirst,
    isLast,
    start,
    autoStart,
    next,
    prev,
    skip,
    finish,
    reset,
    isDone
  }
}

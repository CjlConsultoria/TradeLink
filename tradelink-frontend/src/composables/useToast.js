import { ref, readonly } from 'vue'

const toasts = ref([])
const DEFAULT_DURATION = 5000

let _idCounter = 0

/**
 * @param {string} message
 * @param {'success'|'error'|'info'|'warning'} type
 * @param {number} [duration] ms; 0 = não fecha sozinho
 */
function add(message, type = 'info', duration = DEFAULT_DURATION) {
  const id = `toast-${++_idCounter}`
  const createdAt = Date.now()
  const entry = { id, message, type, duration, createdAt, paused: false, remaining: duration }
  toasts.value = [...toasts.value, entry]

  if (duration > 0) {
    const timerId = setTimeout(() => remove(id), duration)
    entry._timerId = timerId
    entry._startTime = createdAt
  }

  // Limite de toasts simultâneos
  if (toasts.value.length > 5) {
    const oldest = toasts.value[0]
    remove(oldest.id)
  }

  return id
}

function remove(id) {
  const toast = toasts.value.find(t => t.id === id)
  if (toast?._timerId) clearTimeout(toast._timerId)
  toasts.value = toasts.value.filter((t) => t.id !== id)
}

function pause(id) {
  const toast = toasts.value.find(t => t.id === id)
  if (!toast || !toast.duration || toast.paused) return
  toast.paused = true
  if (toast._timerId) clearTimeout(toast._timerId)
  toast.remaining = Math.max(0, toast.remaining - (Date.now() - toast._startTime))
}

function resume(id) {
  const toast = toasts.value.find(t => t.id === id)
  if (!toast || !toast.duration || !toast.paused) return
  toast.paused = false
  toast._startTime = Date.now()
  if (toast.remaining > 0) {
    toast._timerId = setTimeout(() => remove(id), toast.remaining)
  }
}

function success(message, duration = DEFAULT_DURATION) {
  return add(message, 'success', duration)
}
function error(message, duration = 7000) {
  return add(message, 'error', duration)
}
function info(message, duration = DEFAULT_DURATION) {
  return add(message, 'info', duration)
}
function warning(message, duration = 6000) {
  return add(message, 'warning', duration)
}

export function useToast() {
  return {
    toasts: readonly(toasts),
    add,
    remove,
    pause,
    resume,
    success,
    error,
    info,
    warning
  }
}

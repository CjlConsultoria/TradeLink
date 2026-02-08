import { ref, readonly } from 'vue'

const toasts = ref([])
const DEFAULT_DURATION = 4500

/**
 * @param {string} message
 * @param {'success'|'error'|'info'|'warning'} type
 * @param {number} [duration] ms; 0 = não fecha sozinho
 */
function add(message, type = 'info', duration = DEFAULT_DURATION) {
  const id = Math.random().toString(36).slice(2)
  const entry = { id, message, type, duration }
  toasts.value = [...toasts.value, entry]
  if (duration > 0) {
    setTimeout(() => remove(id), duration)
  }
  return id
}

function remove(id) {
  toasts.value = toasts.value.filter((t) => t.id !== id)
}

function success(message, duration = DEFAULT_DURATION) {
  return add(message, 'success', duration)
}
function error(message, duration = DEFAULT_DURATION) {
  return add(message, 'error', duration)
}
function info(message, duration = DEFAULT_DURATION) {
  return add(message, 'info', duration)
}
function warning(message, duration = DEFAULT_DURATION) {
  return add(message, 'warning', duration)
}

export function useToast() {
  return {
    toasts: readonly(toasts),
    add,
    remove,
    success,
    error,
    info,
    warning
  }
}

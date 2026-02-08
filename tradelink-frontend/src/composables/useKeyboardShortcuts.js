import { onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

/**
 * Sistema de atalhos de teclado. Ignora quando o foco está em input/textarea/select.
 * @param {Object} opts - { shortcuts: { key: () => void } } ou usa defaults por role
 */
export function useKeyboardShortcuts(opts = {}) {
  const router = useRouter()
  const authStore = useAuthStore()
  const role = authStore.user?.role

  const defaultShortcuts = {
    AdminMax: {
      e: () => router.push('/admin-max/empresas'),
      p: () => router.push('/admin-max/planos')
    },
    Admin: {
      c: () => router.push('/consultor/carteiras'),
      l: () => router.push('/consultor/clientes'),
      q: () => router.push('/consultor/cotacoes'),
      n: () => opts.onNewRecommendation?.()
    },
    Cliente: {
      c: () => router.push('/cliente/carteiras'),
      q: () => router.push('/cliente/cotacoes')
    }
  }

  let shortcuts = opts.shortcuts ?? (role ? defaultShortcuts[role] : {})
  if (role === 'Admin' && opts.onNewRecommendation) {
    shortcuts = { ...shortcuts, n: opts.onNewRecommendation }
  }

  function handleKeyDown(e) {
        if (!shortcuts || Object.keys(shortcuts).length === 0) return
        const tag = e.target?.tagName?.toLowerCase()
        if (['input', 'textarea', 'select'].includes(tag)) return
        const key = e.key?.toLowerCase()
        if (shortcuts[key]) {
          e.preventDefault()
          shortcuts[key]()
        }
  }

  onMounted(() => window.addEventListener('keydown', handleKeyDown))
  onUnmounted(() => window.removeEventListener('keydown', handleKeyDown))

  return { shortcuts: Object.keys(shortcuts || {}) }
}

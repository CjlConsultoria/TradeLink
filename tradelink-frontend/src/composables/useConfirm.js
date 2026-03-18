import { reactive } from 'vue'

const state = reactive({
  visible: false,
  title: '',
  message: '',
  confirmText: 'Confirmar',
  cancelText: 'Cancelar',
  variant: 'danger', // danger | warning | info
  resolve: null
})

function confirm({ title, message, confirmText, cancelText, variant } = {}) {
  state.title = title || 'Confirmação'
  state.message = message || 'Tem certeza que deseja continuar?'
  state.confirmText = confirmText || 'Confirmar'
  state.cancelText = cancelText || 'Cancelar'
  state.variant = variant || 'danger'
  state.visible = true
  return new Promise(resolve => {
    state.resolve = resolve
  })
}

function onConfirm() {
  state.visible = false
  state.resolve?.(true)
  state.resolve = null
}

function onCancel() {
  state.visible = false
  state.resolve?.(false)
  state.resolve = null
}

export function useConfirm() {
  return { state, confirm, onConfirm, onCancel }
}

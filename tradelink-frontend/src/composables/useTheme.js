import { ref, computed, watchEffect } from 'vue'

const STORAGE_KEY = 'tradelink_theme'
const theme = ref(localStorage.getItem(STORAGE_KEY) || 'light')

export function useTheme() {
  function toggle() {
    theme.value = theme.value === 'dark' ? 'light' : 'dark'
  }

  function set(t) {
    theme.value = t
  }

  watchEffect(() => {
    document.documentElement.classList.toggle('dark', theme.value === 'dark')
    localStorage.setItem(STORAGE_KEY, theme.value)
  })

  return {
    theme,
    toggle,
    set,
    isDark: computed(() => theme.value === 'dark')
  }
}

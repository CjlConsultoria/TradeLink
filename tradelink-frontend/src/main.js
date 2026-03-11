import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import './style.css'
const app = createApp(App)
app.use(createPinia())
app.use(router)
app.mount('#app')

// Registrar Service Worker para PWA + Push Notifications
if ('serviceWorker' in navigator) {
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('/sw.js')
      .then(reg => {
        console.log('SW registrado:', reg.scope)
        setInterval(() => reg.update(), 60 * 60 * 1000)
      })
      .catch(err => console.warn('SW falhou:', err))
  })
}
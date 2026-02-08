<template>
  <aside class="sidebar" :class="{ 'sidebar--open': open }">
    <div class="sidebar__brand">
      <span class="sidebar__logo">◈</span>
      <div class="sidebar__info">
        <h2 class="sidebar__title">TradeLink</h2>
        <p class="sidebar__role">{{ roleLabel }}</p>
      </div>
    </div>
    <nav class="sidebar__nav" aria-label="Menu principal">
      <router-link
        v-for="item in menuItems"
        :key="item.to"
        :to="item.to"
        class="sidebar__link"
        :class="{ 'sidebar__link--active': isActive(item.to) }"
      >
        <span class="sidebar__icon" aria-hidden="true">{{ item.icon }}</span>
        <span>{{ item.label }}</span>
      </router-link>
    </nav>
  </aside>
  <div v-if="open" class="sidebar__backdrop" aria-hidden="true" @click="$emit('close')"></div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '../../stores/auth'

const props = defineProps({ open: Boolean })
defineEmits(['close'])
const route = useRoute()
const authStore = useAuthStore()

const roleLabel = computed(() => {
  const map = { AdminMax: 'Super Admin', Admin: 'Consultor', Cliente: 'Cliente' }
  return map[authStore.user?.role] || ''
})

const menuItems = computed(() => {
  switch (authStore.user?.role) {
    case 'AdminMax':
      return [
        { to: '/admin-max', label: 'Dashboard', icon: '📊' },
        { to: '/admin-max/empresas', label: 'Empresas', icon: '🏢' },
        { to: '/admin-max/planos', label: 'Planos', icon: '💰' }
      ]
    case 'Admin':
      return [
        { to: '/consultor', label: 'Dashboard', icon: '📊' },
        { to: '/consultor/carteiras', label: 'Carteiras', icon: '💼' },
        { to: '/consultor/clientes', label: 'Clientes', icon: '👥' },
      { to: '/consultor/cotacoes', label: 'Cotações', icon: '💹' },
      { to: '/consultor/faturas', label: 'Faturas', icon: '🧾' },
      { to: '/consultor/relatorios', label: 'Histórico e Relatórios', icon: '📈' },
      { to: '/consultor/configuracoes', label: 'Notificações', icon: '🔔' }
      ]
    case 'Cliente':
      return [
        { to: '/cliente', label: 'Dashboard', icon: '📊' },
        { to: '/cliente/carteiras', label: 'Carteiras', icon: '💼' },
        { to: '/cliente/cotacoes', label: 'Cotações', icon: '💹' },
        { to: '/cliente/relatorios', label: 'Relatórios', icon: '📈' },
        { to: '/cliente/configuracoes', label: 'Notificações', icon: '🔔' }
      ]
    default:
      return []
  }
})

function isActive(path) {
  if (path === '/admin-max' || path === '/consultor' || path === '/cliente') return route.path === path
  return route.path.startsWith(path)
}
</script>

<style scoped>
.sidebar {
  width: 16rem;
  min-height: 100vh;
  background: linear-gradient(180deg, #1e1b4b 0%, #312e81 100%);
  color: #e0e7ff;
  display: flex;
  flex-direction: column;
  position: fixed;
  top: 0;
  left: 0;
  z-index: 40;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}
@media (max-width: 1023px) {
  .sidebar {
    transform: translateX(-100%);
    box-shadow: none;
  }
  .sidebar--open {
    transform: translateX(0);
    box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  }
}

.sidebar__backdrop {
  display: none;
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  z-index: 30;
}
@media (max-width: 1023px) {
  .sidebar__backdrop { display: block; }
}

.sidebar__brand {
  padding: 1.25rem 1rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.sidebar__logo {
  width: 2.5rem;
  height: 2.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 10px;
  font-size: 1.25rem;
  color: #a5b4fc;
  font-weight: 700;
}

.sidebar__title {
  font-size: 1.0625rem;
  font-weight: 700;
  margin: 0;
  color: #fff;
  letter-spacing: -0.02em;
}

.sidebar__role {
  font-size: 0.75rem;
  color: #a5b4fc;
  margin: 0.125rem 0 0;
}

.sidebar__nav {
  flex: 1;
  padding: 0.75rem;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.sidebar__link {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.625rem 0.75rem;
  border-radius: var(--tl-radius-sm);
  font-size: 0.9375rem;
  color: #c7d2fe;
  text-decoration: none;
  transition: background 0.2s, color 0.2s;
}

.sidebar__link:hover {
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
}

.sidebar__link--active {
  background: rgb(var(--tl-primary));
  color: #fff;
}

.sidebar__icon {
  font-size: 1.125rem;
  opacity: 0.9;
}
</style>

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
      <template v-for="item in menuItems" :key="item.to || item.label">
        <router-link
          v-if="item.to"
          :to="item.to"
          class="sidebar__link"
          :class="{ 'sidebar__link--active': isActive(item.to) }"
          :data-sidebar-link="item.to"
        >
          <span class="sidebar__icon" aria-hidden="true">{{ item.icon }}</span>
          <span>{{ item.label }}</span>
        </router-link>
        <button
          v-else-if="item.action"
          type="button"
          class="sidebar__link"
          @click="handleAction(item.action)"
        >
          <span class="sidebar__icon" aria-hidden="true">{{ item.icon }}</span>
          <span>{{ item.label }}</span>
        </button>
      </template>
    </nav>
  </aside>
  <div v-if="open" class="sidebar__backdrop" aria-hidden="true" @click="$emit('close')"></div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import { useChatPanel } from '../../composables/useChatPanel'

const props = defineProps({ open: Boolean })
const emit = defineEmits(['close'])
const { openChat } = useChatPanel()
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
        { to: '/admin-max/usuarios', label: 'Usuários', icon: '👥' },
        { to: '/admin-max/planos', label: 'Planos', icon: '💰' },
        { to: '/admin-max/financeiro', label: 'Financeiro', icon: '💲' },
        { to: '/admin-max/faq', label: 'FAQ', icon: '❓' },
        { to: '/admin-max/chat', label: 'Chat Suporte', icon: '💬' },
        { to: '/admin-max/emails-apresentacao', label: 'Emails', icon: '📧' },
        { to: '/admin-max/configuracoes', label: 'Configurações', icon: '⚙️' }
      ]
    case 'Admin':
      return [
        { to: '/consultor', label: 'Dashboard', icon: '📊' },
        { to: '/consultor/carteiras', label: 'Carteiras', icon: '💼' },
        { to: '/consultor/kanban', label: 'Kanban', icon: '📋' },
        { to: '/consultor/copy-trading', label: 'Copy Trading', icon: '📑' },
        { to: '/consultor/rebalanceamento', label: 'Rebalanceamento', icon: '⚖️' },
        { to: '/consultor/clientes', label: 'Clientes', icon: '👥' },
        { to: '/consultor/cotacoes', label: 'Cotações', icon: '💹' },
        { to: '/consultor/heatmap', label: 'Heat Map', icon: '🗺️' },
        { to: '/consultor/comparador', label: 'Comparador', icon: '📊' },
        { to: '/consultor/simulador', label: 'Simulador', icon: '🧮' },
        { to: '/consultor/watchlist', label: 'Watchlist', icon: '⭐' },
        { to: '/consultor/alertas-preco', label: 'Alertas de Preço', icon: '🔔' },
        { to: '/consultor/faturas', label: 'Faturas', icon: '🧾' },
        { to: '/consultor/relatorios', label: 'Histórico e Relatórios', icon: '📈' },
        { to: '/consultor/atividades', label: 'Atividades', icon: '🕐' },
        { to: '/consultor/faq', label: 'FAQ', icon: '❓' },
        { label: 'Suporte', icon: '💬', action: 'openChat' },
        { to: '/consultor/configuracoes', label: 'Configurações', icon: '⚙️' }
      ]
    case 'Cliente':
      // Menu reduzido para clientes em auto-gestão (sem consultor)
      if (authStore.user?.autoGestaoAtiva) {
        return [
          { to: '/cliente', label: 'Dashboard', icon: '📊' },
          { to: '/cliente/portfolio', label: 'Meu Portfolio', icon: '📦' },
          { to: '/cliente/performance', label: 'Performance', icon: '📉' },
          { to: '/cliente/metas', label: 'Metas', icon: '🎯' },
          { to: '/cliente/cotacoes', label: 'Cotações', icon: '💹' },
          { to: '/cliente/heatmap', label: 'Heat Map', icon: '🗺️' },
          { to: '/cliente/comparador', label: 'Comparador', icon: '📊' },
          { to: '/cliente/simulador', label: 'Simulador', icon: '🧮' },
          { to: '/cliente/watchlist', label: 'Watchlist', icon: '⭐' },
          { to: '/cliente/alertas-preco', label: 'Alertas de Preço', icon: '🔔' },
          { to: '/cliente/faturas', label: 'Faturas', icon: '🧾' },
          { to: '/cliente/relatorios', label: 'Relatórios', icon: '📈' },
          { to: '/cliente/atividades', label: 'Atividades', icon: '🕐' },
          { to: '/cliente/faq', label: 'FAQ', icon: '❓' },
          { label: 'Suporte', icon: '💬', action: 'openChat' },
          { to: '/cliente/configuracoes', label: 'Configurações', icon: '⚙️' }
        ]
      }
      return [
        { to: '/cliente', label: 'Dashboard', icon: '📊' },
        { to: '/cliente/portfolio', label: 'Meu Portfolio', icon: '📦' },
        { to: '/cliente/carteiras', label: 'Carteiras', icon: '💼' },
        { to: '/cliente/performance', label: 'Performance', icon: '📉' },
        { to: '/cliente/metas', label: 'Metas', icon: '🎯' },
        { to: '/cliente/cotacoes', label: 'Cotações', icon: '💹' },
        { to: '/cliente/heatmap', label: 'Heat Map', icon: '🗺️' },
        { to: '/cliente/comparador', label: 'Comparador', icon: '📊' },
        { to: '/cliente/simulador', label: 'Simulador', icon: '🧮' },
        { to: '/cliente/watchlist', label: 'Watchlist', icon: '⭐' },
        { to: '/cliente/alertas-preco', label: 'Alertas de Preço', icon: '🔔' },
        { to: '/cliente/relatorios', label: 'Relatórios', icon: '📈' },
        { to: '/cliente/atividades', label: 'Atividades', icon: '🕐' },
        { to: '/cliente/faq', label: 'FAQ', icon: '❓' },
        { label: 'Suporte', icon: '💬', action: 'openChat' },
        { to: '/cliente/configuracoes', label: 'Configurações', icon: '⚙️' }
      ]
    default:
      return []
  }
})

function isActive(path) {
  if (path === '/admin-max' || path === '/consultor' || path === '/cliente') return route.path === path
  return route.path.startsWith(path)
}

function handleAction(action) {
  if (action === 'openChat') {
    openChat()
    emit('close')
  }
}
</script>

<style scoped>
.sidebar {
  width: 16rem;
  height: 100vh;
  max-height: 100dvh;
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
  flex-shrink: 0;
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
  overflow-y: auto;
  min-height: 0;
  scrollbar-width: thin;
  scrollbar-color: rgba(255, 255, 255, 0.2) transparent;
}
.sidebar__nav::-webkit-scrollbar {
  width: 4px;
}
.sidebar__nav::-webkit-scrollbar-track {
  background: transparent;
}
.sidebar__nav::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 4px;
}
.sidebar__nav::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.35);
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
  border: none;
  background: none;
  width: 100%;
  text-align: left;
  cursor: pointer;
  font-family: inherit;
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

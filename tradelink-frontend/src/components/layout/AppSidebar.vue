<template>
  <aside class="sidebar" :class="{ 'sidebar--open': open, 'sidebar--collapsed': collapsed }">
    <div class="sidebar__brand">
      <span class="sidebar__logo">◈</span>
      <div v-if="!collapsed" class="sidebar__info">
        <h2 class="sidebar__title">TradeLink</h2>
        <p class="sidebar__role">{{ roleLabel }}</p>
      </div>
    </div>
    <nav class="sidebar__nav" aria-label="Menu principal">
      <template v-for="item in menuItems" :key="item.group || item.to || item.label">
        <!-- Group with children -->
        <template v-if="item.group">
          <button
            type="button"
            class="sidebar__group-header"
            :class="{ 'sidebar__group-header--open': openGroups.has(item.group) }"
            :title="collapsed ? item.group : undefined"
            @click="toggleGroup(item.group)"
          >
            <span class="sidebar__icon" aria-hidden="true">{{ item.icon }}</span>
            <span v-if="!collapsed" class="sidebar__label">{{ item.group }}</span>
            <svg v-if="!collapsed" class="sidebar__chevron" :class="{ 'sidebar__chevron--open': openGroups.has(item.group) }" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 18l6-6-6-6"/></svg>
          </button>
          <div v-if="openGroups.has(item.group) && !collapsed" class="sidebar__children">
            <template v-for="child in item.children" :key="child.to || child.label">
              <router-link
                v-if="child.to"
                :to="child.to"
                class="sidebar__link sidebar__link--child"
                :class="{ 'sidebar__link--active': isActive(child.to) }"
                @click="closeMobile"
              >
                <span class="sidebar__icon sidebar__icon--child" aria-hidden="true">{{ child.icon }}</span>
                <span>{{ child.label }}</span>
              </router-link>
              <button
                v-else-if="child.action"
                type="button"
                class="sidebar__link sidebar__link--child"
                @click="handleAction(child.action)"
              >
                <span class="sidebar__icon sidebar__icon--child" aria-hidden="true">{{ child.icon }}</span>
                <span>{{ child.label }}</span>
              </button>
            </template>
          </div>
          <!-- Collapsed: show popover on hover -->
          <div v-if="collapsed" class="sidebar__collapsed-group">
            <template v-for="child in item.children" :key="child.to || child.label">
              <router-link
                v-if="child.to"
                :to="child.to"
                class="sidebar__link sidebar__link--child"
                :class="{ 'sidebar__link--active': isActive(child.to) }"
                :title="child.label"
                @click="closeMobile"
              >
                <span class="sidebar__icon sidebar__icon--child" aria-hidden="true">{{ child.icon }}</span>
              </router-link>
            </template>
          </div>
        </template>

        <!-- Single item (no group) -->
        <template v-else>
          <router-link
            v-if="item.to"
            :to="item.to"
            class="sidebar__link"
            :class="{ 'sidebar__link--active': isActive(item.to) }"
            :title="collapsed ? item.label : undefined"
            @click="closeMobile"
          >
            <span class="sidebar__icon" aria-hidden="true">{{ item.icon }}</span>
            <span v-if="!collapsed">{{ item.label }}</span>
          </router-link>
          <button
            v-else-if="item.action"
            type="button"
            class="sidebar__link"
            :title="collapsed ? item.label : undefined"
            @click="handleAction(item.action)"
          >
            <span class="sidebar__icon" aria-hidden="true">{{ item.icon }}</span>
            <span v-if="!collapsed">{{ item.label }}</span>
          </button>
        </template>
      </template>
    </nav>
  </aside>
  <div v-if="open" class="sidebar__backdrop" aria-hidden="true" @click="$emit('close')"></div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import { useChatPanel } from '../../composables/useChatPanel'

const props = defineProps({ open: Boolean, collapsed: Boolean })
const emit = defineEmits(['close'])
const { openChat } = useChatPanel()
const route = useRoute()
const authStore = useAuthStore()

const openGroups = ref(new Set())

const roleLabel = computed(() => {
  const map = { AdminMax: 'Super Admin', Admin: 'Consultor', Cliente: 'Cliente' }
  return map[authStore.user?.role] || ''
})

const menuItems = computed(() => {
  switch (authStore.user?.role) {
    case 'AdminMax':
      return [
        { to: '/admin-max', label: 'Dashboard', icon: '📊' },
        {
          group: 'Gestao', icon: '🏢',
          children: [
            { to: '/admin-max/empresas', label: 'Empresas', icon: '🏢' },
            { to: '/admin-max/usuarios', label: 'Usuarios', icon: '👥' },
            { to: '/admin-max/planos', label: 'Planos', icon: '💰' }
          ]
        },
        { to: '/admin-max/financeiro', label: 'Financeiro', icon: '💲' },
        { to: '/admin-max/marketplace', label: 'Marketplace', icon: '🏪' },
        {
          group: 'Comunicacao', icon: '💬',
          children: [
            { to: '/admin-max/chat', label: 'Chat Suporte', icon: '💬' },
            { to: '/admin-max/chamados', label: 'Chamados', icon: '🎫' },
            { to: '/admin-max/faq', label: 'FAQ', icon: '❓' },
            { to: '/admin-max/emails-apresentacao', label: 'Emails', icon: '📧' },
            { to: '/admin-max/email-marketing', label: 'Email Marketing', icon: '📣' }
          ]
        },
        { to: '/admin-max/configuracoes', label: 'Configuracoes', icon: '⚙️' }
      ]
    case 'Admin':
      return [
        { to: '/consultor', label: 'Dashboard', icon: '📊' },
        {
          group: 'Investimentos', icon: '💼',
          children: [
            { to: '/consultor/carteiras', label: 'Carteiras', icon: '💼' },
            { to: '/consultor/copy-trading', label: 'Copy Trading', icon: '📑' },
            { to: '/consultor/rebalanceamento', label: 'Rebalanceamento', icon: '⚖️' },
            { to: '/consultor/kanban', label: 'Kanban', icon: '📋' }
          ]
        },
        {
          group: 'Mercado', icon: '💹',
          children: [
            { to: '/consultor/cotacoes', label: 'Cotacoes', icon: '💹' },
            { to: '/consultor/heatmap', label: 'Heat Map', icon: '🗺️' },
            { to: '/consultor/comparador', label: 'Comparador', icon: '📊' },
            { to: '/consultor/simulador', label: 'Simulador', icon: '🧮' },
            { to: '/consultor/watchlist', label: 'Watchlist', icon: '⭐' },
            { to: '/consultor/alertas-preco', label: 'Alertas de Preco', icon: '🔔' }
          ]
        },
        {
          group: 'Gestao', icon: '👥',
          children: [
            { to: '/consultor/clientes', label: 'Clientes', icon: '👥' },
            { to: '/consultor/faturas', label: 'Faturas', icon: '🧾' },
            { to: '/consultor/relatorios', label: 'Historico e Relatorios', icon: '📈' }
          ]
        },
        {
          group: 'Marketplace', icon: '🏪',
          children: [
            { to: '/consultor/marketplace', label: 'Meu Perfil', icon: '🏪' },
            { to: '/consultor/solicitacoes', label: 'Solicitacoes', icon: '📩' }
          ]
        },
        { to: '/consultor/atividades', label: 'Atividades', icon: '🕐' },
        { to: '/consultor/chamados', label: 'Chamados', icon: '🎫' },
        { to: '/consultor/faq', label: 'FAQ', icon: '❓' },
        { label: 'Suporte', icon: '💬', action: 'openChat' },
        { to: '/consultor/configuracoes', label: 'Configuracoes', icon: '⚙️' }
      ]
    case 'Cliente':
      if (authStore.user?.autoGestaoAtiva) {
        return [
          { to: '/cliente', label: 'Dashboard', icon: '📊' },
          {
            group: 'Portfolio', icon: '📦',
            children: [
              { to: '/cliente/portfolio', label: 'Meu Portfolio', icon: '📦' },
              { to: '/cliente/performance', label: 'Performance', icon: '📉' },
              { to: '/cliente/metas', label: 'Metas', icon: '🎯' }
            ]
          },
          {
            group: 'Mercado', icon: '💹',
            children: [
              { to: '/cliente/cotacoes', label: 'Cotacoes', icon: '💹' },
              { to: '/cliente/heatmap', label: 'Heat Map', icon: '🗺️' },
              { to: '/cliente/comparador', label: 'Comparador', icon: '📊' },
              { to: '/cliente/simulador', label: 'Simulador', icon: '🧮' },
              { to: '/cliente/watchlist', label: 'Watchlist', icon: '⭐' },
              { to: '/cliente/alertas-preco', label: 'Alertas de Preco', icon: '🔔' }
            ]
          },
          {
            group: 'Financeiro', icon: '🧾',
            children: [
              { to: '/cliente/faturas', label: 'Faturas', icon: '🧾' },
              { to: '/cliente/relatorios', label: 'Relatorios', icon: '📈' }
            ]
          },
          { to: '/cliente/marketplace', label: 'Encontrar Consultor', icon: '🔍' },
          { to: '/cliente/atividades', label: 'Atividades', icon: '🕐' },
          { to: '/cliente/chamados', label: 'Chamados', icon: '🎫' },
          { to: '/cliente/faq', label: 'FAQ', icon: '❓' },
          { label: 'Suporte', icon: '💬', action: 'openChat' },
          { to: '/cliente/configuracoes', label: 'Configuracoes', icon: '⚙️' }
        ]
      }
      return [
        { to: '/cliente', label: 'Dashboard', icon: '📊' },
        {
          group: 'Portfolio', icon: '📦',
          children: [
            { to: '/cliente/portfolio', label: 'Meu Portfolio', icon: '📦' },
            { to: '/cliente/carteiras', label: 'Carteiras', icon: '💼' },
            { to: '/cliente/performance', label: 'Performance', icon: '📉' },
            { to: '/cliente/metas', label: 'Metas', icon: '🎯' }
          ]
        },
        {
          group: 'Mercado', icon: '💹',
          children: [
            { to: '/cliente/cotacoes', label: 'Cotacoes', icon: '💹' },
            { to: '/cliente/heatmap', label: 'Heat Map', icon: '🗺️' },
            { to: '/cliente/comparador', label: 'Comparador', icon: '📊' },
            { to: '/cliente/simulador', label: 'Simulador', icon: '🧮' },
            { to: '/cliente/watchlist', label: 'Watchlist', icon: '⭐' },
            { to: '/cliente/alertas-preco', label: 'Alertas de Preco', icon: '🔔' }
          ]
        },
        { to: '/cliente/relatorios', label: 'Relatorios', icon: '📈' },
        ...(authStore.user?.origemVinculo === 'MARKETPLACE'
          ? [{ to: '/cliente/minha-mentoria', label: 'Minha Mentoria', icon: '🎓' }]
          : []),
        { to: '/cliente/atividades', label: 'Atividades', icon: '🕐' },
        { to: '/cliente/chamados', label: 'Chamados', icon: '🎫' },
        { to: '/cliente/faq', label: 'FAQ', icon: '❓' },
        { label: 'Suporte', icon: '💬', action: 'openChat' },
        { to: '/cliente/configuracoes', label: 'Configuracoes', icon: '⚙️' }
      ]
    default:
      return []
  }
})

// Auto-open groups that contain the active route
watch(() => route.path, (path) => {
  for (const item of menuItems.value) {
    if (item.group && item.children) {
      for (const child of item.children) {
        if (child.to && path.startsWith(child.to)) {
          openGroups.value.add(item.group)
          break
        }
      }
    }
  }
}, { immediate: true })

function toggleGroup(group) {
  if (openGroups.value.has(group)) {
    openGroups.value.delete(group)
  } else {
    openGroups.value.add(group)
  }
}

function isActive(path) {
  if (path === '/admin-max' || path === '/consultor' || path === '/cliente') return route.path === path
  return route.path.startsWith(path)
}

function closeMobile() {
  emit('close')
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
  transition: transform 0.25s ease, box-shadow 0.25s ease, width 0.25s ease;
}
@media (max-width: 1023px) {
  .sidebar {
    transform: translateX(-100%);
    box-shadow: none;
    width: 16rem !important;
  }
  .sidebar--open {
    transform: translateX(0);
    box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  }
}
@media (min-width: 1024px) {
  .sidebar--collapsed {
    width: 4.5rem;
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
  overflow: hidden;
}
.sidebar--collapsed .sidebar__brand {
  justify-content: center;
  padding: 1.25rem 0.5rem;
}

.sidebar__logo {
  width: 2.5rem;
  height: 2.5rem;
  min-width: 2.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 10px;
  font-size: 1.25rem;
  color: #a5b4fc;
  font-weight: 700;
}

.sidebar__info {
  overflow: hidden;
}

.sidebar__title {
  font-size: 1.0625rem;
  font-weight: 700;
  margin: 0;
  color: #fff;
  letter-spacing: -0.02em;
  white-space: nowrap;
}

.sidebar__role {
  font-size: 0.75rem;
  color: #a5b4fc;
  margin: 0.125rem 0 0;
  white-space: nowrap;
}

.sidebar__nav {
  flex: 1;
  padding: 0.75rem;
  display: flex;
  flex-direction: column;
  gap: 0.125rem;
  overflow-y: auto;
  min-height: 0;
  scrollbar-width: thin;
  scrollbar-color: rgba(255, 255, 255, 0.2) transparent;
}
.sidebar--collapsed .sidebar__nav {
  padding: 0.5rem;
  align-items: center;
}
.sidebar__nav::-webkit-scrollbar { width: 4px; }
.sidebar__nav::-webkit-scrollbar-track { background: transparent; }
.sidebar__nav::-webkit-scrollbar-thumb { background: rgba(255, 255, 255, 0.2); border-radius: 4px; }
.sidebar__nav::-webkit-scrollbar-thumb:hover { background: rgba(255, 255, 255, 0.35); }

/* Group header */
.sidebar__group-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.5rem 0.75rem;
  border-radius: var(--tl-radius-sm);
  font-size: 0.8125rem;
  font-weight: 600;
  color: #93a0c7;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  text-decoration: none;
  transition: background 0.2s, color 0.2s;
  border: none;
  background: none;
  width: 100%;
  text-align: left;
  cursor: pointer;
  font-family: inherit;
  margin-top: 0.375rem;
}
.sidebar--collapsed .sidebar__group-header {
  justify-content: center;
  padding: 0.5rem;
  width: 2.75rem;
  height: 2.75rem;
}
.sidebar__group-header:hover {
  background: rgba(255, 255, 255, 0.06);
  color: #c7d2fe;
}
.sidebar__group-header--open {
  color: #c7d2fe;
}

.sidebar__chevron {
  margin-left: auto;
  opacity: 0.5;
  transition: transform 0.2s ease;
  flex-shrink: 0;
}
.sidebar__chevron--open {
  transform: rotate(90deg);
  opacity: 0.8;
}

.sidebar__label {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* Children container */
.sidebar__children {
  display: flex;
  flex-direction: column;
  gap: 0.0625rem;
  padding-left: 0.5rem;
  margin-bottom: 0.125rem;
}

/* Collapsed group children (hidden by default, shown on hover) */
.sidebar__collapsed-group {
  display: none;
}
.sidebar--collapsed .sidebar__collapsed-group {
  display: none;
  position: absolute;
  left: 4.5rem;
  background: #1e1b4b;
  border-radius: var(--tl-radius-sm);
  padding: 0.375rem;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.3);
  z-index: 50;
  min-width: 10rem;
  flex-direction: column;
  gap: 0.125rem;
}
.sidebar--collapsed .sidebar__group-header:hover + .sidebar__collapsed-group,
.sidebar--collapsed .sidebar__collapsed-group:hover {
  display: flex;
}
.sidebar--collapsed .sidebar__collapsed-group .sidebar__link--child {
  padding-left: 0.75rem;
}
.sidebar--collapsed .sidebar__collapsed-group .sidebar__link--child span:last-child {
  display: inline;
}

/* Links */
.sidebar__link {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.5rem 0.75rem;
  border-radius: var(--tl-radius-sm);
  font-size: 0.875rem;
  color: #c7d2fe;
  text-decoration: none;
  transition: background 0.2s, color 0.2s;
  border: none;
  background: none;
  width: 100%;
  text-align: left;
  cursor: pointer;
  font-family: inherit;
  white-space: nowrap;
  overflow: hidden;
}
.sidebar--collapsed .sidebar__link {
  justify-content: center;
  padding: 0.5rem;
  width: 2.75rem;
  height: 2.75rem;
}
.sidebar__link:hover {
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
}
.sidebar__link--active {
  background: rgb(var(--tl-primary));
  color: #fff;
}

.sidebar__link--child {
  font-size: 0.8125rem;
  padding: 0.4rem 0.75rem;
}

.sidebar__icon {
  font-size: 1rem;
  opacity: 0.9;
  flex-shrink: 0;
  width: 1.25rem;
  text-align: center;
}
.sidebar__icon--child {
  font-size: 0.875rem;
}
</style>

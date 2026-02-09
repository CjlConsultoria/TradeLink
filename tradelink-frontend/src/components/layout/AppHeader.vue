<template>
  <header class="app-header">
    <div class="app-header__left">
      <button type="button" @click="$emit('toggle-sidebar')" class="app-header__menu" aria-label="Abrir menu">
        <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round" d="M4 6h16M4 12h16M4 18h16"/>
        </svg>
      </button>
      <div class="app-header__brand">
        <span class="app-header__logo">◈</span>
        <span class="app-header__title">TradeLink</span>
      </div>
    </div>
    <div class="app-header__right">
      <span class="app-header__user" :title="user?.nome">{{ user?.nome }}</span>
      <span class="app-header__badge" :class="roleBadge">{{ roleLabel }}</span>
      <button type="button" @click="handleLogout" class="app-header__logout">Sair</button>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import { useToast } from '../../composables/useToast'

defineEmits(['toggle-sidebar'])
const router = useRouter()
const authStore = useAuthStore()
const toast = useToast()
const user = computed(() => authStore.user)

const roleLabel = computed(() => {
  const map = { AdminMax: 'Super Admin', Admin: 'Consultor', Cliente: 'Cliente' }
  return map[user.value?.role] || ''
})
const roleBadge = computed(() => {
  const map = {
    AdminMax: 'app-header__badge--admin-max',
    Admin: 'app-header__badge--consultor',
    Cliente: 'app-header__badge--cliente'
  }
  return map[user.value?.role] || 'app-header__badge--default'
})
function handleLogout() {
  toast.info('Até logo!')
  authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
  padding: 0.75rem 1rem;
  min-height: 3.25rem;
  background: rgb(var(--tl-surface));
  border-bottom: 1px solid rgb(var(--tl-border));
  box-shadow: var(--tl-shadow);
}
@media (min-width: 640px) {
  .app-header { padding: 0.875rem 1.5rem; }
}

.app-header__left {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  min-width: 0;
  flex-shrink: 0;
}

.app-header__menu {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 2.5rem;
  height: 2.5rem;
  color: rgb(var(--tl-text-muted));
  background: transparent;
  border: none;
  border-radius: var(--tl-radius-sm);
  cursor: pointer;
  transition: color 0.2s, background 0.2s;
}
@media (min-width: 1024px) {
  .app-header__menu { display: none; }
}
.app-header__menu:hover {
  color: rgb(var(--tl-primary));
  background: rgb(var(--tl-primary-light));
}

.app-header__brand {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.app-header__logo {
  font-size: 1.25rem;
  color: rgb(var(--tl-primary));
  font-weight: 700;
}

.app-header__title {
  font-size: 1.125rem;
  font-weight: 700;
  color: rgb(var(--tl-text));
  letter-spacing: -0.02em;
}

.app-header__right {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  min-width: 0;
  flex-shrink: 0;
}
@media (min-width: 480px) {
  .app-header__right { gap: 0.75rem; }
}

.app-header__user {
  font-size: 0.8125rem;
  font-weight: 500;
  color: rgb(var(--tl-text));
  max-width: 8rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
@media (min-width: 640px) {
  .app-header__user { max-width: 12rem; font-size: 0.875rem; }
}
@media (min-width: 1024px) {
  .app-header__user { max-width: 20rem; }
}

.app-header__badge {
  font-size: 0.75rem;
  font-weight: 500;
  padding: 0.25rem 0.625rem;
  border-radius: 9999px;
}
.app-header__badge--admin-max { background: #f3e8ff; color: #6b21a8; }
.app-header__badge--consultor { background: #dbeafe; color: #1d4ed8; }
.app-header__badge--cliente { background: #dcfce7; color: #15803d; }
.app-header__badge--default { background: #f3f4f6; color: #374151; }

.app-header__logout {
  font-size: 0.875rem;
  font-weight: 500;
  color: rgb(var(--tl-error));
  background: transparent;
  border: none;
  padding: 0.375rem 0.75rem;
  border-radius: var(--tl-radius-sm);
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
}
.app-header__logout:hover {
  background: #fef2f2;
  color: #b91c1c;
}
</style>

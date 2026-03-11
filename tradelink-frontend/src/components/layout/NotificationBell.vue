<template>
  <div class="notif-bell" ref="container">
    <button type="button" class="notif-bell__btn" @click="toggle" :title="`${naoLidas} notificacao(es) nao lida(s)`">
      <svg xmlns="http://www.w3.org/2000/svg" class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
        <path stroke-linecap="round" stroke-linejoin="round" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
      </svg>
      <span v-if="naoLidas > 0" class="notif-bell__badge">{{ naoLidas > 9 ? '9+' : naoLidas }}</span>
    </button>

    <!-- Dropdown -->
    <div v-if="open" class="notif-bell__dropdown">
      <div class="notif-bell__header">
        <h4 class="notif-bell__title">Notificacoes</h4>
        <button v-if="naoLidas > 0" type="button" @click="marcarTodas" class="notif-bell__mark-all">
          Marcar todas como lidas
        </button>
      </div>

      <div v-if="loading" class="notif-bell__loading">Carregando...</div>

      <div v-else-if="notificacoes.length === 0" class="notif-bell__empty">
        Nenhuma notificacao
      </div>

      <div v-else class="notif-bell__list">
        <div
          v-for="n in notificacoes"
          :key="n.id"
          class="notif-bell__item"
          :class="{ 'notif-bell__item--unread': !n.lida }"
          @click="handleClick(n)"
        >
          <div class="notif-bell__icon" :class="tipoClass(n.tipo)">
            {{ tipoIcon(n.tipo) }}
          </div>
          <div class="notif-bell__content">
            <p class="notif-bell__item-title">{{ n.titulo }}</p>
            <p class="notif-bell__item-msg">{{ n.mensagem }}</p>
            <p class="notif-bell__item-time">{{ n.createdAt }}</p>
          </div>
          <span v-if="!n.lida" class="notif-bell__dot"></span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useNotificacaoStore } from '../../stores/notificacao'

const router = useRouter()
const store = useNotificacaoStore()
const container = ref(null)
const open = ref(false)

const naoLidas = computed(() => store.naoLidas)
const notificacoes = computed(() => store.notificacoes)
const loading = computed(() => store.loading)

function toggle() {
  open.value = !open.value
  if (open.value) {
    store.carregarNotificacoes()
  }
}

function handleClick(n) {
  if (!n.lida) {
    store.marcarComoLida(n.id)
  }
  if (n.link) {
    open.value = false
    router.push(n.link)
  }
}

function marcarTodas() {
  store.marcarTodasComoLidas()
}

function tipoIcon(tipo) {
  const icons = {
    RECOMENDACAO: 'R',
    ALERTA: '!',
    OPERACAO: 'O',
    SISTEMA: 'S'
  }
  return icons[tipo] || 'N'
}

function tipoClass(tipo) {
  const classes = {
    RECOMENDACAO: 'notif-bell__icon--rec',
    ALERTA: 'notif-bell__icon--alert',
    OPERACAO: 'notif-bell__icon--op',
    SISTEMA: 'notif-bell__icon--sys'
  }
  return classes[tipo] || 'notif-bell__icon--sys'
}

function handleClickOutside(e) {
  if (container.value && !container.value.contains(e.target)) {
    open.value = false
  }
}

onMounted(() => {
  store.iniciarPolling()
  document.addEventListener('click', handleClickOutside)
})

onBeforeUnmount(() => {
  store.pararPolling()
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.notif-bell {
  position: relative;
}

.notif-bell__btn {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 2.25rem;
  height: 2.25rem;
  color: rgb(var(--tl-text-muted));
  background: transparent;
  border: none;
  border-radius: var(--tl-radius-sm);
  cursor: pointer;
  transition: color 0.2s, background 0.2s;
}
.notif-bell__btn:hover {
  color: rgb(var(--tl-primary));
  background: rgb(var(--tl-primary-light));
}

.notif-bell__badge {
  position: absolute;
  top: 2px;
  right: 2px;
  min-width: 1rem;
  height: 1rem;
  padding: 0 0.25rem;
  font-size: 0.625rem;
  font-weight: 700;
  line-height: 1rem;
  text-align: center;
  color: white;
  background: rgb(var(--tl-error));
  border-radius: 9999px;
}

.notif-bell__dropdown {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: 0.5rem;
  width: 22rem;
  max-height: 28rem;
  background: white;
  border: 1px solid rgb(var(--tl-border));
  border-radius: var(--tl-radius);
  box-shadow: 0 10px 40px rgba(0,0,0,0.12);
  overflow: hidden;
  z-index: 50;
}

.notif-bell__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.75rem 1rem;
  border-bottom: 1px solid rgb(var(--tl-border));
}

.notif-bell__title {
  font-size: 0.875rem;
  font-weight: 600;
  color: rgb(var(--tl-text));
  margin: 0;
}

.notif-bell__mark-all {
  font-size: 0.75rem;
  color: rgb(var(--tl-primary));
  background: none;
  border: none;
  cursor: pointer;
  font-weight: 500;
}
.notif-bell__mark-all:hover { text-decoration: underline; }

.notif-bell__loading,
.notif-bell__empty {
  padding: 2rem 1rem;
  text-align: center;
  font-size: 0.875rem;
  color: rgb(var(--tl-text-muted));
}

.notif-bell__list {
  max-height: 24rem;
  overflow-y: auto;
}

.notif-bell__item {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  padding: 0.75rem 1rem;
  border-bottom: 1px solid rgba(var(--tl-border), 0.5);
  cursor: pointer;
  transition: background 0.15s;
}
.notif-bell__item:hover { background: rgba(var(--tl-primary-light), 0.5); }
.notif-bell__item--unread { background: rgba(99, 102, 241, 0.04); }
.notif-bell__item:last-child { border-bottom: none; }

.notif-bell__icon {
  width: 2rem;
  height: 2rem;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.75rem;
  font-weight: 700;
  flex-shrink: 0;
}
.notif-bell__icon--rec { background: #dbeafe; color: #1d4ed8; }
.notif-bell__icon--alert { background: #fef3c7; color: #d97706; }
.notif-bell__icon--op { background: #dcfce7; color: #15803d; }
.notif-bell__icon--sys { background: #e0e7ff; color: #4338ca; }

.notif-bell__content {
  flex: 1;
  min-width: 0;
}

.notif-bell__item-title {
  font-size: 0.8125rem;
  font-weight: 600;
  color: rgb(var(--tl-text));
  margin: 0;
  line-height: 1.3;
}

.notif-bell__item-msg {
  font-size: 0.75rem;
  color: rgb(var(--tl-text-muted));
  margin: 0.125rem 0 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.notif-bell__item-time {
  font-size: 0.6875rem;
  color: #9ca3af;
  margin: 0.25rem 0 0;
}

.notif-bell__dot {
  width: 0.5rem;
  height: 0.5rem;
  border-radius: 50%;
  background: rgb(var(--tl-primary));
  flex-shrink: 0;
  margin-top: 0.25rem;
}
</style>

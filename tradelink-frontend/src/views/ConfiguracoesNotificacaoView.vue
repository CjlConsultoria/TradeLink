<template>
  <div>
    <h2 class="page-title">Configurações</h2>
    <p class="text-gray-600 mb-6">Notificações e alteração de senha.</p>

    <div class="flex gap-2 mb-6 border-b border-gray-200">
      <button type="button" @click="abaAtiva = 'notificacoes'"
        :class="abaAtiva === 'notificacoes' ? 'border-b-2 border-indigo-600 text-indigo-600 font-medium' : 'text-gray-500 hover:text-gray-700'"
        class="pb-2 px-1 text-sm">Notificações</button>
      <button type="button" @click="abaAtiva = 'senha'"
        :class="abaAtiva === 'senha' ? 'border-b-2 border-indigo-600 text-indigo-600 font-medium' : 'text-gray-500 hover:text-gray-700'"
        class="pb-2 px-1 text-sm">Alterar senha</button>
    </div>

    <LoadingSpinner v-if="loading" />

    <template v-else>
      <!-- Aba Senha -->
      <div v-show="abaAtiva === 'senha'" class="space-y-6 max-w-xl">
        <div class="card p-6">
          <h3 class="section-title">Alterar senha</h3>
          <p class="text-sm text-gray-500 mb-3">Troque a senha do seu acesso (recomendado após o primeiro login).</p>
          <div class="space-y-2 max-w-xs">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Senha atual</label>
              <input v-model="senhaAtual" type="password" placeholder="Senha atual"
                class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Nova senha</label>
              <input v-model="novaSenha" type="password" placeholder="Mínimo 6 caracteres"
                class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm" />
            </div>
            <button type="button" @click="trocarSenha" :disabled="salvandoSenha"
              class="px-4 py-2 bg-indigo-600 text-white rounded-lg text-sm hover:bg-indigo-700 disabled:opacity-50">
              {{ salvandoSenha ? 'Salvando...' : 'Alterar senha' }}
            </button>
            <p v-if="erroSenha" class="text-sm text-red-500">{{ erroSenha }}</p>
          </div>
        </div>
      </div>

      <!-- Aba Notificações -->
      <div v-show="abaAtiva === 'notificacoes'" class="space-y-6 max-w-xl">
      <div v-if="empresaNotif" class="mb-6 p-4 card bg-gray-50/50">
        <p class="text-sm font-medium text-gray-700">Canais habilitados pela sua empresa</p>
        <p class="text-sm text-gray-500 mt-1">
          E-mail: {{ empresaNotif.notificacaoEmail ? 'Sim' : 'Não' }} ·
          Telegram: {{ empresaNotif.notificacaoTelegram ? 'Sim' : 'Não' }} ·
          Push: {{ empresaNotif.notificacaoPush ? 'Sim' : 'Não' }}
        </p>
      </div>

        <!-- Telegram -->
        <div class="card p-6">
          <h3 class="section-title">Telegram</h3>
          <p class="text-sm text-gray-500 mb-3">{{ config?.telegramInstrucoes || 'Informe seu Chat ID do Telegram para receber notificações.' }}</p>
          <div class="flex gap-2 flex-wrap">
            <input v-model="telegramChatId" type="text" placeholder="Ex: 123456789"
              class="px-3 py-2 border border-gray-300 rounded-lg text-sm w-48" />
            <button type="button" @click="salvarTelegram" :disabled="salvandoTelegram"
              class="px-4 py-2 bg-indigo-600 text-white rounded-lg text-sm hover:bg-indigo-700 disabled:opacity-50">
              {{ salvandoTelegram ? 'Salvando...' : 'Salvar' }}
            </button>
          </div>
          <p v-if="me?.telegramChatId" class="text-xs text-green-600 mt-2">Vinculado: {{ me.telegramChatId }}</p>
        </div>

        <!-- Push (navegador) -->
        <div class="card p-6">
          <h3 class="section-title">Notificações no navegador</h3>
          <p class="text-sm text-gray-500 mb-3">Receba notificações mesmo com a aba fechada (quando a empresa tiver Push habilitado).</p>
          <template v-if="!vapidPublicKey">
            <p class="text-sm text-amber-600">Push não está configurado no servidor. Entre em contato com o administrador.</p>
          </template>
          <template v-else>
            <button v-if="!me?.pushInscrito" type="button" @click="ativarPush" :disabled="salvandoPush"
              class="px-4 py-2 bg-indigo-600 text-white rounded-lg text-sm hover:bg-indigo-700 disabled:opacity-50">
              {{ salvandoPush ? 'Ativando...' : 'Ativar notificações no navegador' }}
            </button>
            <p v-else class="text-sm text-green-600">Notificações no navegador ativadas.</p>
            <p v-if="erroPush" class="text-sm text-red-500 mt-2">{{ erroPush }}</p>
          </template>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useToast } from '../composables/useToast'
import userApi from '../api/userApi'
import LoadingSpinner from '../components/common/LoadingSpinner.vue'

const toast = useToast()

const loading = ref(true)
const me = ref(null)
const empresaNotif = ref(null)
const config = ref(null)
const vapidPublicKey = ref('')
const telegramChatId = ref('')
const salvandoTelegram = ref(false)
const salvandoPush = ref(false)
const erroPush = ref('')
const abaAtiva = ref('notificacoes')
const senhaAtual = ref('')
const novaSenha = ref('')
const salvandoSenha = ref(false)
const erroSenha = ref('')

async function load() {
  loading.value = true
  try {
    const [meRes, configRes, empRes] = await Promise.all([
      userApi.me(),
      userApi.configNotificacao(),
      userApi.empresaNotificacoes().catch(() => ({ data: null }))
    ])
    me.value = meRes.data
    config.value = configRes.data
    vapidPublicKey.value = configRes.data?.vapidPublicKey || ''
    empresaNotif.value = empRes.data
    telegramChatId.value = me.value?.telegramChatId || ''
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function trocarSenha() {
  erroSenha.value = ''
  if (!senhaAtual.value?.trim() || !novaSenha.value?.trim()) {
    erroSenha.value = 'Preencha senha atual e nova senha.'
    return
  }
  if (novaSenha.value.length < 6) {
    erroSenha.value = 'Nova senha deve ter no mínimo 6 caracteres.'
    return
  }
  salvandoSenha.value = true
  try {
    await userApi.trocarSenha(senhaAtual.value, novaSenha.value)
    toast.success('Senha alterada com sucesso.')
    senhaAtual.value = ''
    novaSenha.value = ''
  } catch (e) {
    erroSenha.value = e.response?.data?.mensagem || e.response?.data?.erro || 'Erro ao alterar senha.'
    toast.error(erroSenha.value)
  } finally {
    salvandoSenha.value = false
  }
}

async function salvarTelegram() {
  salvandoTelegram.value = true
  try {
    const res = await userApi.atualizarTelegramChatId(telegramChatId.value?.trim() || null)
    me.value = res.data
    toast.success('Telegram vinculado com sucesso.')
  } catch (e) {
    console.error(e)
    toast.error(e.response?.data?.mensagem || 'Erro ao salvar.')
  } finally {
    salvandoTelegram.value = false
  }
}

function urlBase64ToUint8Array(base64String) {
  const padding = '='.repeat((4 - (base64String.length % 4)) % 4)
  const base64 = (base64String + padding).replace(/-/g, '+').replace(/_/g, '/')
  const rawData = atob(base64)
  const output = new Uint8Array(rawData.length)
  for (let i = 0; i < rawData.length; ++i) output[i] = rawData.charCodeAt(i)
  return output
}

async function ativarPush() {
  erroPush.value = ''
  if (!vapidPublicKey.value) return
  salvandoPush.value = true
  try {
    if (!('serviceWorker' in navigator) || !('PushManager' in window)) {
      erroPush.value = 'Seu navegador não suporta notificações push.'
      return
    }
    const reg = await navigator.serviceWorker.register('/sw.js', { scope: '/' })
    await reg.update()
    const permission = await Notification.requestPermission()
    if (permission !== 'granted') {
      erroPush.value = 'Permissão de notificação negada.'
      toast.warning(erroPush.value)
      return
    }
    const sub = await reg.pushManager.subscribe({
      userVisibleOnly: true,
      applicationServerKey: urlBase64ToUint8Array(vapidPublicKey.value)
    })
    const endpoint = sub.endpoint
    const key = sub.getKey('p256dh')
    const auth = sub.getKey('auth')
    if (!key || !auth) {
      erroPush.value = 'Não foi possível obter chaves da inscrição.'
      toast.error(erroPush.value)
      return
    }
    const subscription = {
      endpoint,
      keys: {
        p256dh: btoa(String.fromCharCode.apply(null, new Uint8Array(key))).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, ''),
        auth: btoa(String.fromCharCode.apply(null, new Uint8Array(auth))).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '')
      }
    }
    await userApi.registrarPushSubscription(subscription)
    me.value = { ...me.value, pushInscrito: true }
    toast.success('Notificações no navegador ativadas.')
  } catch (e) {
    console.error(e)
    erroPush.value = e.response?.data?.erro || e.message || 'Erro ao ativar push.'
    toast.error(erroPush.value)
  } finally {
    salvandoPush.value = false
  }
}

onMounted(load)
</script>

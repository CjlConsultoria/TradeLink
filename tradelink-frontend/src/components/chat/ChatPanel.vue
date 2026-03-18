<template>
  <div class="fixed bottom-6 right-6 z-50 w-96 max-w-[calc(100vw-3rem)] bg-white rounded-2xl shadow-2xl border border-gray-200 flex flex-col"
    style="height:560px;max-height:calc(100vh - 6rem);">
    <!-- Header -->
    <div class="flex items-center justify-between px-4 py-3 border-b bg-indigo-600 rounded-t-2xl">
      <div class="flex items-center gap-2">
        <div class="w-8 h-8 rounded-full bg-white/20 flex items-center justify-center text-white text-sm">&#9651;</div>
        <div>
          <h3 class="text-white font-semibold text-sm">Suporte TradeLink</h3>
          <p class="text-indigo-200 text-[11px]">{{ headerSubtitle }}</p>
        </div>
      </div>
      <button type="button" @click="$emit('close')" class="text-white/80 hover:text-white text-lg">&times;</button>
    </div>

    <!-- FASE 1: Triagem IA -->
    <template v-if="fase === 'triagem'">
      <div ref="triagemBody" class="flex-1 overflow-y-auto p-4 space-y-3">
        <!-- Mensagens da triagem -->
        <div v-for="(msg, idx) in triagemMsgs" :key="idx"
          class="flex" :class="msg.from === 'bot' ? 'justify-start' : 'justify-end'">
          <div class="max-w-[85%] px-3 py-2 rounded-xl text-sm"
            :class="msg.from === 'bot' ? 'bg-gray-100 text-gray-800 rounded-bl-sm' : 'bg-indigo-600 text-white rounded-br-sm'">
            <p v-if="msg.from === 'bot'" class="text-[10px] font-semibold text-indigo-600 mb-0.5">🤖 Assistente IA</p>
            <p class="whitespace-pre-wrap" v-html="msg.text"></p>
          </div>
        </div>
        <!-- Opções rápidas -->
        <div v-if="quickOptions.length > 0" class="space-y-1.5">
          <button v-for="opt in quickOptions" :key="opt.id" type="button"
            @click="onClickOption(opt)"
            class="w-full text-left px-3 py-2 bg-indigo-50 hover:bg-indigo-100 border border-indigo-200 rounded-lg text-sm text-indigo-700 transition-colors">
            {{ opt.label }}
          </button>
        </div>
        <!-- Digitando... -->
        <div v-if="botTyping" class="flex justify-start">
          <div class="bg-gray-100 px-3 py-2 rounded-xl rounded-bl-sm text-sm text-gray-500">
            <span class="inline-flex gap-1">
              <span class="w-1.5 h-1.5 bg-gray-400 rounded-full animate-bounce" style="animation-delay:0ms"></span>
              <span class="w-1.5 h-1.5 bg-gray-400 rounded-full animate-bounce" style="animation-delay:150ms"></span>
              <span class="w-1.5 h-1.5 bg-gray-400 rounded-full animate-bounce" style="animation-delay:300ms"></span>
            </span>
          </div>
        </div>
      </div>

      <!-- Input triagem -->
      <form @submit.prevent="enviarTriagem" class="p-3 border-t flex gap-2">
        <input v-model="triagemInput" type="text"
          class="flex-1 px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:border-indigo-500"
          placeholder="Digite sua dúvida..." />
        <button type="submit" :disabled="!triagemInput.trim()"
          class="px-4 py-2 bg-indigo-600 text-white rounded-lg text-sm font-medium hover:bg-indigo-700 disabled:opacity-40 transition-colors">
          Enviar
        </button>
      </form>
    </template>

    <!-- FASE 2: Fila de espera -->
    <template v-if="fase === 'fila'">
      <div class="flex-1 flex flex-col items-center justify-center p-6 text-center">
        <div class="w-16 h-16 rounded-full bg-indigo-100 flex items-center justify-center mb-4">
          <svg class="w-8 h-8 text-indigo-600 animate-spin" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
        </div>
        <h3 class="font-semibold text-gray-800 mb-1">Conectando ao suporte...</h3>
        <p class="text-sm text-gray-500 mb-3">Estamos transferindo você para um atendente humano.</p>
        <div class="bg-amber-50 border border-amber-200 rounded-lg px-4 py-2.5 mb-4">
          <p class="text-xs text-amber-700 font-medium">⏳ Tempo estimado de espera</p>
          <p class="text-lg font-bold text-amber-800">{{ tempoEspera }}</p>
        </div>
        <p class="text-xs text-gray-400">Sua posição na fila: <strong>{{ posicaoFila }}°</strong></p>
        <button type="button" @click="voltarTriagem"
          class="mt-4 text-sm text-indigo-600 hover:text-indigo-700 font-medium">
          ← Voltar para o assistente IA
        </button>
      </div>
    </template>

    <!-- FASE 3: Chat humano -->
    <template v-if="fase === 'chat'">
      <div ref="chatBody" class="flex-1 overflow-y-auto p-4 space-y-3">
        <div v-if="loading" class="text-center py-8 text-gray-400 text-sm">Carregando...</div>

        <div v-else-if="mensagens.length === 0" class="text-center py-8">
          <p class="text-gray-400 text-sm mb-2">Conectado ao suporte!</p>
          <p class="text-gray-400 text-xs">Um atendente responderá em breve.</p>
        </div>

        <div v-else v-for="msg in mensagens" :key="msg.id"
          class="flex" :class="msg.isAdmin ? 'justify-start' : 'justify-end'">
          <div class="max-w-[80%] px-3 py-2 rounded-xl text-sm"
            :class="msg.isAdmin ? 'bg-gray-100 text-gray-800 rounded-bl-sm' : 'bg-indigo-600 text-white rounded-br-sm'">
            <p v-if="msg.isAdmin" class="text-[10px] font-semibold text-indigo-600 mb-0.5">Suporte</p>
            <p class="whitespace-pre-wrap">{{ msg.conteudo }}</p>
            <p class="text-[10px] mt-1 opacity-50">{{ formatTime(msg.createdAt) }}</p>
          </div>
        </div>
      </div>

      <!-- Input chat humano -->
      <form @submit.prevent="enviar" class="p-3 border-t flex gap-2">
        <input v-model="novaMsg" type="text"
          class="flex-1 px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:border-indigo-500"
          placeholder="Digite sua mensagem..." />
        <button type="submit" :disabled="!novaMsg.trim() || enviando"
          class="px-4 py-2 bg-indigo-600 text-white rounded-lg text-sm font-medium hover:bg-indigo-700 disabled:opacity-40 transition-colors">
          Enviar
        </button>
      </form>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import chatApi from '../../api/chatApi'
import publicApi from '../../api/publicApi'

const emit = defineEmits(['close', 'update-badge'])

// ── Estado geral ──
const fase = ref('triagem') // 'triagem' | 'fila' | 'chat'

// ── Triagem IA ──
const triagemMsgs = ref([])
const triagemInput = ref('')
const quickOptions = ref([])
const botTyping = ref(false)
const faqData = ref([])
const triagemBody = ref(null)
const perguntasFeitas = ref(0)

// ── Fila ──
const tempoEspera = ref('2-5 min')
const posicaoFila = ref(1)

// ── Chat humano ──
const loading = ref(false)
const mensagens = ref([])
const novaMsg = ref('')
const enviando = ref(false)
const conversaId = ref(null)
const chatBody = ref(null)
let pollInterval = null

const headerSubtitle = computed(() => {
  if (fase.value === 'triagem') return '🤖 Assistente IA'
  if (fase.value === 'fila') return '⏳ Aguardando atendente...'
  return '👤 Atendimento humano'
})

// ── Categorias de opções rápidas ──
const categorias = [
  { id: 'conta', label: '👤 Minha Conta e Cadastro' },
  { id: 'carteira', label: '💼 Carteiras e Alocação' },
  { id: 'recomendacao', label: '📋 Recomendações e Operações' },
  { id: 'pagamento', label: '💳 Pagamentos e Planos' },
  { id: 'portfolio', label: '📦 Meu Portfolio' },
  { id: 'outro', label: '💬 Falar com atendente humano' }
]

// ── Inicialização ──
onMounted(async () => {
  // Carrega FAQs para triagem IA
  try {
    const res = await publicApi.getFaq()
    faqData.value = res.data || []
  } catch {}

  // Verifica se já existe conversa aberta → vai direto pro chat
  try {
    const res = await chatApi.listarConversas()
    const conversas = res.data || []
    const aberta = conversas.find(c => c.status === 'ABERTA')
    if (aberta) {
      conversaId.value = aberta.id
      fase.value = 'chat'
      await carregarMensagens()
      startPolling()
      return
    }
  } catch {}

  // Senão, mostra triagem
  addBotMessage('Olá! 👋 Sou o assistente virtual da TradeLink.\n\nComo posso ajudar? Escolha uma opção ou escreva sua dúvida:')
  quickOptions.value = [...categorias]
})

onUnmounted(() => {
  if (pollInterval) clearInterval(pollInterval)
})

// ── Triagem: adicionar mensagem do bot ──
function addBotMessage(text) {
  botTyping.value = true
  setTimeout(() => {
    botTyping.value = false
    triagemMsgs.value.push({ from: 'bot', text })
    nextTick(() => scrollTriagem())
  }, 600 + Math.random() * 400)
}

function addUserMessage(text) {
  triagemMsgs.value.push({ from: 'user', text })
  nextTick(() => scrollTriagem())
}

function scrollTriagem() {
  if (triagemBody.value) triagemBody.value.scrollTop = triagemBody.value.scrollHeight
}

// ── Triagem: buscar FAQ mais relevante ──
function buscarFaqRelevante(texto) {
  const q = texto.toLowerCase()
  const words = q.split(/\s+/).filter(w => w.length > 2)

  let melhor = null
  let melhorScore = 0

  for (const faq of faqData.value) {
    const pergunta = faq.pergunta.toLowerCase()
    const resposta = faq.resposta.toLowerCase()
    let score = 0

    for (const w of words) {
      if (pergunta.includes(w)) score += 3
      if (resposta.includes(w)) score += 1
    }

    // Bonus por match exato de frase
    if (pergunta.includes(q)) score += 10
    if (resposta.includes(q)) score += 5

    if (score > melhorScore) {
      melhorScore = score
      melhor = faq
    }
  }

  return melhorScore >= 3 ? melhor : null
}

function buscarFaqsPorCategoria(catId) {
  const categoriasMap = {
    'conta': ['Conta', 'Cadastro', 'Geral', 'Acesso'],
    'carteira': ['Carteiras', 'Alocação', 'Rebalanceamento'],
    'recomendacao': ['Recomendações', 'Operações'],
    'pagamento': ['Pagamentos', 'Planos', 'Faturamento'],
    'portfolio': ['Portfolio', 'Ativos', 'Cotações']
  }

  const cats = categoriasMap[catId] || []
  const results = faqData.value.filter(f => {
    const faqCat = (f.categoria || '').toLowerCase()
    return cats.some(c => faqCat.includes(c.toLowerCase()))
  })

  return results.slice(0, 5)
}

// ── Triagem: opção rápida ──
function handleQuickOption(opt) {
  quickOptions.value = []

  if (opt.id === 'outro') {
    addUserMessage('Quero falar com um atendente humano')
    iniciarFila()
    return
  }

  addUserMessage(opt.label)

  const faqs = buscarFaqsPorCategoria(opt.id)
  if (faqs.length > 0) {
    const opsList = faqs.map(f => `<strong>•</strong> ${f.pergunta}`).join('\n')
    addBotMessage(`Encontrei estas perguntas sobre o tema:\n\n${opsList}\n\n<em>Clique em uma pergunta abaixo ou escreva sua dúvida:</em>`)

    setTimeout(() => {
      quickOptions.value = [
        ...faqs.map(f => ({ id: 'faq_' + f.id, label: f.pergunta, faq: f })),
        { id: 'voltar', label: '← Voltar às categorias' },
        { id: 'outro', label: '💬 Falar com atendente humano' }
      ]
    }, 1100)
  } else {
    addBotMessage('Não encontrei perguntas específicas nessa categoria.\n\nVocê pode descrever sua dúvida ou falar com um atendente.')
    setTimeout(() => {
      quickOptions.value = [
        { id: 'voltar', label: '← Voltar às categorias' },
        { id: 'outro', label: '💬 Falar com atendente humano' }
      ]
    }, 1100)
  }
}

// ── Triagem: clicou em FAQ específica ou voltar ──
watch(quickOptions, () => {}, { deep: true })

function handleQuickOptionGeneric(opt) {
  quickOptions.value = []

  if (opt.id === 'voltar') {
    addBotMessage('Sem problema! Escolha uma categoria:')
    setTimeout(() => { quickOptions.value = [...categorias] }, 700)
    return
  }

  if (opt.id === 'outro') {
    addUserMessage('Quero falar com um atendente humano')
    iniciarFila()
    return
  }

  if (opt.faq) {
    addUserMessage(opt.faq.pergunta)
    perguntasFeitas.value++
    addBotMessage(`${opt.faq.resposta}\n\n<em>Isso respondeu sua dúvida?</em>`)

    setTimeout(() => {
      quickOptions.value = [
        { id: 'sim_resolveu', label: '✅ Sim, obrigado!' },
        { id: 'voltar', label: '🔄 Tenho outra dúvida' },
        { id: 'outro', label: '💬 Falar com atendente humano' }
      ]
    }, 1100)
    return
  }

  if (opt.id === 'sim_resolveu') {
    addUserMessage('Sim, obrigado!')
    addBotMessage('Fico feliz em ajudar! 😊\n\nSe tiver mais alguma dúvida, é só voltar aqui.')
    setTimeout(() => {
      quickOptions.value = [
        { id: 'voltar', label: '🔄 Tenho outra dúvida' }
      ]
    }, 1100)
    return
  }
}

// Override do handleQuickOption para tratar todas as opções
const originalHandle = handleQuickOption
watch(() => quickOptions.value, () => {}, { deep: true })

// Unifica handler
function onClickOption(opt) {
  // Categorias principais
  if (categorias.some(c => c.id === opt.id)) {
    handleQuickOption(opt)
  } else {
    handleQuickOptionGeneric(opt)
  }
}

// Substitui o handler no template
// (vamos usar onClickOption via um wrapper)

// ── Triagem: enviar texto livre ──
function enviarTriagem() {
  const texto = triagemInput.value.trim()
  if (!texto) return

  addUserMessage(texto)
  triagemInput.value = ''
  quickOptions.value = []
  perguntasFeitas.value++

  const faq = buscarFaqRelevante(texto)
  if (faq) {
    addBotMessage(`${faq.resposta}\n\n<em>Isso respondeu sua dúvida?</em>`)
    setTimeout(() => {
      quickOptions.value = [
        { id: 'sim_resolveu', label: '✅ Sim, obrigado!' },
        { id: 'voltar', label: '🔄 Tenho outra dúvida' },
        { id: 'outro', label: '💬 Falar com atendente humano' }
      ]
    }, 1100)
  } else {
    addBotMessage('Não encontrei uma resposta exata para essa dúvida. 🤔\n\nPosso transferir você para um atendente humano que pode ajudar melhor.')
    setTimeout(() => {
      quickOptions.value = [
        { id: 'voltar', label: '🔄 Tentar outra pergunta' },
        { id: 'outro', label: '💬 Falar com atendente humano' }
      ]
    }, 1100)
  }
}

// ── Fila de espera ──
function iniciarFila() {
  fase.value = 'fila'
  posicaoFila.value = Math.floor(Math.random() * 3) + 1
  const min = posicaoFila.value * 2
  const max = min + 3
  tempoEspera.value = `${min}-${max} min`

  // Simula saída da fila e conecta ao chat
  setTimeout(async () => {
    await conectarChatHumano()
  }, 3000)
}

function voltarTriagem() {
  fase.value = 'triagem'
  addBotMessage('Ok, voltamos! Como posso ajudar?')
  setTimeout(() => { quickOptions.value = [...categorias] }, 700)
}

// ── Chat humano ──
async function conectarChatHumano() {
  fase.value = 'chat'
  loading.value = true
  try {
    const res = await chatApi.listarConversas()
    const conversas = res.data || []
    const aberta = conversas.find(c => c.status === 'ABERTA')
    if (aberta) {
      conversaId.value = aberta.id
      await carregarMensagens()
    } else {
      // Monta resumo da triagem
      const userMsgs = triagemMsgs.value.filter(m => m.from === 'user').map(m => m.text.replace(/<[^>]*>/g, ''))
      const assunto = userMsgs.length > 0 ? userMsgs[userMsgs.length - 1].substring(0, 100) : 'Suporte'
      const nova = await chatApi.iniciarConversa(assunto)
      conversaId.value = nova.data.id

      // Envia contexto da triagem como primeira mensagem
      if (userMsgs.length > 0) {
        const contexto = `[Triagem IA] O usuário perguntou sobre:\n${userMsgs.join('\n')}`
        await chatApi.enviarMensagem(conversaId.value, contexto)
        await carregarMensagens()
      }
    }
  } catch {
    try {
      const nova = await chatApi.iniciarConversa('Suporte')
      conversaId.value = nova.data.id
    } catch {}
  }
  loading.value = false
  startPolling()
  await nextTick()
  scrollToBottom()
}

async function carregarMensagens() {
  if (!conversaId.value) return
  try {
    const res = await chatApi.listarMensagens(conversaId.value)
    const newMsgs = res.data || []
    if (newMsgs.length !== mensagens.value.length) {
      mensagens.value = newMsgs
      await nextTick()
      scrollToBottom()
      emit('update-badge')
    }
  } catch {}
}

async function enviar() {
  if (!novaMsg.value.trim()) return
  if (!conversaId.value) {
    try {
      const nova = await chatApi.iniciarConversa('Suporte')
      conversaId.value = nova.data.id
    } catch { return }
  }
  enviando.value = true
  try {
    const res = await chatApi.enviarMensagem(conversaId.value, novaMsg.value.trim())
    mensagens.value.push(res.data)
    novaMsg.value = ''
    await nextTick()
    scrollToBottom()
  } catch {}
  enviando.value = false
}

function startPolling() {
  if (pollInterval) clearInterval(pollInterval)
  pollInterval = setInterval(carregarMensagens, 5000)
}

function scrollToBottom() {
  if (chatBody.value) chatBody.value.scrollTop = chatBody.value.scrollHeight
}

function formatTime(dt) {
  if (!dt) return ''
  const d = new Date(dt)
  return d.toLocaleString('pt-BR', { hour: '2-digit', minute: '2-digit' })
}
</script>

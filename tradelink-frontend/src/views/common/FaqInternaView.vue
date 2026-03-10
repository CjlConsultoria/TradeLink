<template>
  <div>
    <div class="flex items-center justify-between mb-6">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Perguntas Frequentes</h1>
        <p class="text-sm text-gray-500 mt-1">Encontre respostas para suas dúvidas</p>
      </div>
    </div>

    <!-- Busca -->
    <div class="relative mb-6">
      <svg class="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.3-4.3"/></svg>
      <input v-model="search" type="text" placeholder="Buscar perguntas..."
        class="w-full pl-10 pr-4 py-2.5 border border-gray-300 rounded-xl text-sm focus:outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500/20" />
    </div>

    <div v-if="loading" class="text-center py-12 text-gray-400 text-sm">Carregando...</div>

    <div v-else-if="filteredFaqs.length === 0" class="text-center py-12">
      <p class="text-gray-400 text-sm">Nenhuma pergunta encontrada.</p>
    </div>

    <div v-else class="space-y-4">
      <div v-for="(group, cat) in groupedFaqs" :key="cat">
        <h3 v-if="cat !== 'null'" class="text-xs font-semibold text-indigo-600 uppercase tracking-wider mb-3 mt-4">{{ cat }}</h3>
        <div class="space-y-2">
          <div v-for="faq in group" :key="faq.id" class="bg-white rounded-xl border border-gray-200 overflow-hidden shadow-sm">
            <button type="button" @click="toggle(faq.id)"
              class="w-full px-5 py-3.5 text-left flex items-center justify-between gap-3 hover:bg-gray-50 transition-colors">
              <span class="font-medium text-sm text-gray-800">{{ faq.pergunta }}</span>
              <svg class="w-4 h-4 text-gray-400 flex-shrink-0 transition-transform" :class="{ 'rotate-180': opened.has(faq.id) }" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="6 9 12 15 18 9"/></svg>
            </button>
            <div v-if="opened.has(faq.id)" class="px-5 pb-4 text-sm text-gray-600 leading-relaxed border-t border-gray-100 pt-3">
              {{ faq.resposta }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="mt-8 text-center">
      <p class="text-sm text-gray-500 mb-3">Não encontrou sua resposta?</p>
      <p class="text-sm text-gray-400">Use o <strong>Chat de Suporte</strong> clicando no botão 💬 no canto inferior direito.</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import publicApi from '../../api/publicApi'

const loading = ref(true)
const faqs = ref([])
const search = ref('')
const opened = ref(new Set())

const filteredFaqs = computed(() => {
  if (!search.value) return faqs.value
  const q = search.value.toLowerCase()
  return faqs.value.filter(f => f.pergunta.toLowerCase().includes(q) || f.resposta.toLowerCase().includes(q))
})

const groupedFaqs = computed(() => {
  const groups = {}
  filteredFaqs.value.forEach(f => {
    const cat = f.categoria || 'Geral'
    if (!groups[cat]) groups[cat] = []
    groups[cat].push(f)
  })
  return groups
})

function toggle(id) {
  const s = new Set(opened.value)
  s.has(id) ? s.delete(id) : s.add(id)
  opened.value = s
}

onMounted(async () => {
  try { const res = await publicApi.getFaq(); faqs.value = res.data || [] }
  catch {} finally { loading.value = false }
})
</script>

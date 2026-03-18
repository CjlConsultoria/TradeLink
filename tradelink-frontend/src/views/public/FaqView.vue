<template>
  <div class="faq-page">
    <div class="faq-page__bg">
      <div class="faq-page__shape faq-page__shape--1"></div>
      <div class="faq-page__shape faq-page__shape--2"></div>
    </div>

    <div class="faq-page__content">
      <div class="faq-header">
        <div class="faq-header__logo">
          <div class="faq-header__logo-icon">&#9670;</div>
          <h1 class="faq-header__brand">TradeLink</h1>
        </div>
        <h2 class="faq-header__title">Perguntas Frequentes</h2>
        <p class="faq-header__subtitle">Encontre respostas para suas duvidas</p>
      </div>

      <div class="faq-search">
        <svg class="faq-search__icon" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.3-4.3"/></svg>
        <input v-model="search" type="text" placeholder="Buscar perguntas..." class="faq-search__input" />
      </div>

      <div v-if="loading" class="faq-empty">
        <div class="faq-spinner"></div>
        <span>Carregando...</span>
      </div>

      <div v-else-if="filteredFaqs.length === 0" class="faq-empty">
        <p>Nenhuma pergunta encontrada.</p>
      </div>

      <div v-else class="faq-list">
        <div v-for="(group, cat) in groupedFaqs" :key="cat" class="faq-group">
          <h3 v-if="cat !== 'null'" class="faq-group__title">{{ cat }}</h3>
          <div v-for="faq in group" :key="faq.id" class="faq-item">
            <button type="button" @click="toggle(faq.id)" class="faq-item__header">
              <span class="faq-item__question">{{ faq.pergunta }}</span>
              <svg class="faq-item__chevron" :class="opened.has(faq.id) ? 'faq-item__chevron--open' : ''" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="6 9 12 15 18 9"/></svg>
            </button>
            <div v-if="opened.has(faq.id)" class="faq-item__answer">{{ faq.resposta }}</div>
          </div>
        </div>
      </div>

      <div class="faq-footer">
        <p class="faq-footer__text">Nao encontrou sua resposta?</p>
        <router-link to="/login" class="faq-footer__btn">Fale com o Suporte</router-link>
        <div class="faq-footer__links">
          <router-link to="/login">Login</router-link>
          <router-link to="/cadastro">Cadastre-se</router-link>
          <router-link to="/precos">Precos</router-link>
        </div>
      </div>
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

<style scoped>
.faq-page { min-height: 100vh; position: relative; overflow: hidden; }
.faq-page__bg { position: absolute; inset: 0; background: linear-gradient(135deg, rgb(99 102 241) 0%, rgb(139 92 246) 50%, rgb(79 70 229) 100%); }
.faq-page__shape { position: absolute; border-radius: 50%; filter: blur(80px); opacity: 0.4; }
.faq-page__shape--1 { width: 400px; height: 400px; background: rgb(255 255 255); top: -100px; right: -100px; animation: ffloat 15s ease-in-out infinite; }
.faq-page__shape--2 { width: 300px; height: 300px; background: rgb(196 181 253); bottom: -50px; left: -50px; animation: ffloat 18s ease-in-out infinite reverse; }
@keyframes ffloat { 0%,100% { transform: translate(0,0); } 50% { transform: translate(20px,-20px); } }

.faq-page__content { position: relative; z-index: 1; max-width: 800px; margin: 0 auto; padding: 2rem 1rem; }
.faq-header { text-align: center; margin-bottom: 2rem; }
.faq-header__logo { display: inline-flex; align-items: center; gap: 0.75rem; margin-bottom: 1rem; }
.faq-header__logo-icon { width: 40px; height: 40px; background: linear-gradient(135deg,rgb(99 102 241),rgb(139 92 246)); border-radius: 10px; display: flex; align-items: center; justify-content: center; color: white; font-size: 1.25rem; font-weight: 700; box-shadow: 0 4px 12px rgba(99,102,241,0.3); }
.faq-header__brand { font-size: 1.5rem; font-weight: 700; color: white; margin: 0; }
.faq-header__title { font-size: 1.25rem; font-weight: 600; color: rgba(255,255,255,0.95); margin: 0 0 0.25rem; }
.faq-header__subtitle { font-size: 0.875rem; color: rgba(255,255,255,0.7); margin: 0; }

.faq-search { position: relative; margin-bottom: 1.5rem; }
.faq-search__icon { position: absolute; left: 1rem; top: 50%; transform: translateY(-50%); color: rgb(156 163 175); }
.faq-search__input { width: 100%; padding: 0.75rem 1rem 0.75rem 2.75rem; background: rgba(255,255,255,0.95); border: 1px solid rgba(255,255,255,0.3); border-radius: 12px; font-size: 0.875rem; color: rgb(17 24 39); box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
.faq-search__input:focus { outline: none; border-color: rgb(99 102 241); box-shadow: 0 4px 12px rgba(0,0,0,0.1), 0 0 0 3px rgba(99,102,241,0.2); }
.faq-search__input::placeholder { color: rgb(156 163 175); }

.faq-empty { text-align: center; padding: 2rem; color: rgba(255,255,255,0.7); display: flex; align-items: center; justify-content: center; gap: 0.5rem; }
.faq-spinner { width: 1.25rem; height: 1.25rem; border: 2px solid rgba(255,255,255,0.3); border-top-color: white; border-radius: 50%; animation: fspin 0.7s linear infinite; }
@keyframes fspin { to { transform: rotate(360deg); } }

.faq-list { display: flex; flex-direction: column; gap: 0.75rem; }
.faq-group__title { font-size: 0.75rem; font-weight: 600; color: rgba(255,255,255,0.8); text-transform: uppercase; letter-spacing: 0.05em; margin: 1rem 0 0.5rem; }
.faq-item { background: rgba(255,255,255,0.95); border-radius: 12px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.faq-item__header { width: 100%; padding: 1rem 1.25rem; text-align: left; display: flex; align-items: center; justify-content: space-between; gap: 0.75rem; background: none; border: none; cursor: pointer; transition: background 0.15s; }
.faq-item__header:hover { background: rgb(249 250 251); }
.faq-item__question { font-size: 0.9375rem; font-weight: 500; color: rgb(17 24 39); }
.faq-item__chevron { color: rgb(156 163 175); flex-shrink: 0; transition: transform 0.2s; }
.faq-item__chevron--open { transform: rotate(180deg); }
.faq-item__answer { padding: 0 1.25rem 1rem; font-size: 0.875rem; color: rgb(107 114 128); line-height: 1.7; border-top: 1px solid rgb(243 244 246); padding-top: 0.75rem; }

.faq-footer { text-align: center; margin-top: 2.5rem; }
.faq-footer__text { font-size: 0.875rem; color: rgba(255,255,255,0.7); margin: 0 0 0.75rem; }
.faq-footer__btn { display: inline-block; padding: 0.625rem 1.5rem; background: rgba(255,255,255,0.95); color: rgb(99 102 241); font-weight: 600; font-size: 0.875rem; border-radius: 8px; text-decoration: none; transition: background 0.2s, transform 0.1s; box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
.faq-footer__btn:hover { background: white; transform: translateY(-1px); }
.faq-footer__links { display: flex; justify-content: center; gap: 1.5rem; margin-top: 1rem; }
.faq-footer__links a { font-size: 0.8125rem; color: rgba(255,255,255,0.6); text-decoration: none; transition: color 0.2s; }
.faq-footer__links a:hover { color: white; }
</style>

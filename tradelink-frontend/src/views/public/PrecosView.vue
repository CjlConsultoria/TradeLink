<template>
  <div class="precos-page">
    <div class="precos-page__bg">
      <div class="precos-page__shape precos-page__shape--1"></div>
      <div class="precos-page__shape precos-page__shape--2"></div>
    </div>

    <div class="precos-page__content">
      <div class="precos-header">
        <div class="precos-header__logo">
          <div class="precos-header__logo-icon">&#9670;</div>
          <h1 class="precos-header__brand">TradeLink</h1>
        </div>
        <h2 class="precos-header__title">Escolha o plano ideal para voce</h2>
        <p class="precos-header__subtitle">Comece com 5 dias gratuitos. Cancele quando quiser.</p>
      </div>

      <div v-if="loading" class="text-center py-12">
        <div class="loading-spinner__dot" style="width:2.5rem;height:2.5rem;border:3px solid rgba(255,255,255,0.3);border-top-color:#fff;border-radius:50%;animation:spin 0.8s linear infinite;margin:0 auto;"></div>
        <p class="text-white/70 text-sm mt-3">Carregando planos...</p>
      </div>

      <div v-else class="precos-grid">
        <!-- Plano Auto-Gestão (dinâmico) -->
        <div v-if="planoAutoGestao" class="precos-card precos-card--featured">
          <div class="precos-card__badge">Popular</div>
          <h3 class="precos-card__name">{{ planoAutoGestao.nome }}</h3>
          <p class="precos-card__desc">Para investidores individuais</p>
          <div class="precos-card__price">R$ {{ formatPreco(planoAutoGestao.preco) }}<span class="precos-card__period">/mes</span></div>
          <ul class="precos-card__features">
            <li><span class="precos-check">&#10003;</span> Portfolio completo</li>
            <li><span class="precos-check">&#10003;</span> Cotacoes em tempo real</li>
            <li><span class="precos-check">&#10003;</span> Relatorios detalhados</li>
            <li><span class="precos-check">&#10003;</span> Notificacoes inteligentes</li>
            <li><span class="precos-check">&#10003;</span> Suporte via chat</li>
          </ul>
          <router-link to="/cadastro" class="precos-card__btn precos-card__btn--primary">Comecar Gratis</router-link>
        </div>

        <!-- Planos Consultor (dinâmico) -->
        <div v-for="plano in planosConsultor" :key="plano.id" class="precos-card">
          <h3 class="precos-card__name">{{ plano.nome }}</h3>
          <p class="precos-card__desc">Ate {{ plano.maxUsuarios }} cliente{{ plano.maxUsuarios > 1 ? 's' : '' }}</p>
          <div class="precos-card__price">R$ {{ formatPreco(plano.preco) }}<span class="precos-card__period">/mes</span></div>
          <ul class="precos-card__features">
            <li><span class="precos-check">&#10003;</span> Tudo do Auto-Gestao</li>
            <li><span class="precos-check">&#10003;</span> Gestao de carteiras</li>
            <li><span class="precos-check">&#10003;</span> Recomendacoes para clientes</li>
            <li><span class="precos-check">&#10003;</span> Rebalanceamento automatico</li>
            <li><span class="precos-check">&#10003;</span> {{ plano.maxUsuarios }} licenca{{ plano.maxUsuarios > 1 ? 's' : '' }}</li>
          </ul>
          <router-link to="/cadastro" class="precos-card__btn">Comecar Gratis</router-link>
        </div>
      </div>

      <div class="precos-footer">
        <router-link to="/login">Login</router-link>
        <router-link to="/faq">FAQ</router-link>
        <router-link to="/cadastro">Cadastre-se</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import publicApi from '../../api/publicApi'

const loading = ref(true)
const todosPlanos = ref([])
const planoAutoGestao = computed(() => todosPlanos.value.find(p => p.tipo === 'AUTO_GESTAO'))
const planosConsultor = computed(() => todosPlanos.value.filter(p => p.tipo !== 'AUTO_GESTAO'))
function formatPreco(v) { return Number(v).toFixed(2).replace('.', ',') }

onMounted(async () => {
  try { const res = await publicApi.getPlanos(); todosPlanos.value = res.data || [] } catch {}
  finally { loading.value = false }
})
</script>

<style scoped>
.precos-page { min-height: 100vh; position: relative; overflow: hidden; }
.precos-page__bg { position: absolute; inset: 0; background: linear-gradient(135deg, rgb(99 102 241) 0%, rgb(139 92 246) 50%, rgb(79 70 229) 100%); }
.precos-page__shape { position: absolute; border-radius: 50%; filter: blur(80px); opacity: 0.4; }
.precos-page__shape--1 { width: 400px; height: 400px; background: rgb(255 255 255); top: -100px; right: -100px; animation: pfloat 15s ease-in-out infinite; }
.precos-page__shape--2 { width: 300px; height: 300px; background: rgb(196 181 253); bottom: -50px; left: -50px; animation: pfloat 18s ease-in-out infinite reverse; }
@keyframes pfloat { 0%,100% { transform: translate(0,0); } 50% { transform: translate(20px,-20px); } }

.precos-page__content { position: relative; z-index: 1; max-width: 1000px; margin: 0 auto; padding: 2rem 1rem; }

.precos-header { text-align: center; margin-bottom: 2.5rem; }
.precos-header__logo { display: inline-flex; align-items: center; gap: 0.75rem; margin-bottom: 1rem; }
.precos-header__logo-icon { width: 40px; height: 40px; background: linear-gradient(135deg,rgb(99 102 241),rgb(139 92 246)); border-radius: 10px; display: flex; align-items: center; justify-content: center; color: white; font-size: 1.25rem; font-weight: 700; box-shadow: 0 4px 12px rgba(99,102,241,0.3); }
.precos-header__brand { font-size: 1.5rem; font-weight: 700; color: white; margin: 0; }
.precos-header__title { font-size: 1.25rem; font-weight: 600; color: rgba(255,255,255,0.95); margin: 0 0 0.25rem; }
.precos-header__subtitle { font-size: 0.875rem; color: rgba(255,255,255,0.7); margin: 0; }

.precos-grid { display: grid; grid-template-columns: 1fr; gap: 1.5rem; margin-bottom: 2rem; }
@media (min-width: 640px) { .precos-grid { grid-template-columns: repeat(2, 1fr); } }
@media (min-width: 900px) { .precos-grid { grid-template-columns: repeat(3, 1fr); } }

.precos-card { background: rgba(255,255,255,0.95); border-radius: 16px; padding: 1.75rem; position: relative; box-shadow: 0 4px 16px rgba(0,0,0,0.08); border: 2px solid transparent; transition: transform 0.2s, box-shadow 0.2s; }
.precos-card:hover { transform: translateY(-4px); box-shadow: 0 8px 24px rgba(0,0,0,0.12); }
.precos-card--featured { border-color: rgb(99 102 241); background: white; }
.precos-card__badge { position: absolute; top: -12px; left: 1rem; padding: 0.25rem 0.75rem; background: rgb(99 102 241); color: white; font-size: 0.75rem; font-weight: 600; border-radius: 9999px; }
.precos-card__name { font-size: 1.125rem; font-weight: 700; color: rgb(17 24 39); margin: 0 0 0.25rem; }
.precos-card__desc { font-size: 0.8125rem; color: rgb(107 114 128); margin: 0 0 1rem; }
.precos-card__price { font-size: 2rem; font-weight: 700; color: rgb(17 24 39); margin: 0 0 0.25rem; }
.precos-card__period { font-size: 0.875rem; font-weight: 400; color: rgb(107 114 128); }
.precos-card__features { list-style: none; padding: 0; margin: 1rem 0 1.5rem; display: flex; flex-direction: column; gap: 0.5rem; }
.precos-card__features li { font-size: 0.875rem; color: rgb(55 65 81); display: flex; align-items: center; gap: 0.5rem; }
.precos-check { color: rgb(34 197 94); font-weight: 700; }

.precos-card__btn { display: block; width: 100%; padding: 0.75rem; text-align: center; border-radius: 8px; font-size: 0.875rem; font-weight: 600; text-decoration: none; transition: background 0.2s; background: rgb(243 244 246); color: rgb(55 65 81); }
.precos-card__btn:hover { background: rgb(229 231 235); }
.precos-card__btn--primary { background: rgb(99 102 241); color: white; }
.precos-card__btn--primary:hover { background: rgb(79 70 229); }

.precos-footer { display: flex; justify-content: center; gap: 1.5rem; }
.precos-footer a { font-size: 0.8125rem; color: rgba(255,255,255,0.6); text-decoration: none; transition: color 0.2s; }
.precos-footer a:hover { color: white; }
</style>

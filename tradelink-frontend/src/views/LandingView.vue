<template>
  <div class="landing">
    <!-- Navbar -->
    <nav class="landing-nav">
      <div class="landing-container landing-nav__inner">
        <div class="landing-nav__brand">
          <div class="landing-nav__logo" aria-hidden="true">
            <span class="landing-nav__logo-icon">&#9672;</span>
          </div>
          <span class="landing-nav__name">TradeLink</span>
        </div>
        <div class="landing-nav__links">
          <a href="#funcionalidades" class="landing-nav__link">Funcionalidades</a>
          <a href="#planos" class="landing-nav__link">Planos</a>
          <a href="#como-funciona" class="landing-nav__link landing-nav__link--desktop">Como funciona</a>
          <router-link to="/login" class="landing-nav__btn landing-nav__btn--ghost">Entrar</router-link>
          <router-link to="/login" class="landing-nav__btn landing-nav__btn--solid">Cadastrar</router-link>
        </div>
      </div>
    </nav>

    <!-- Hero -->
    <section class="hero">
      <div class="hero__bg">
        <div class="hero__shape hero__shape--1"></div>
        <div class="hero__shape hero__shape--2"></div>
        <div class="hero__shape hero__shape--3"></div>
      </div>
      <div class="landing-container hero__content">
        <div class="hero__badge">Plataforma de Consultoria de Trading</div>
        <h1 class="hero__title">
          Gerencie suas <span class="hero__highlight">recomendacoes de trading</span> em um so lugar
        </h1>
        <p class="hero__subtitle">
          Conecte consultores e clientes com carteiras inteligentes,
          recomendacoes em tempo real e acompanhamento completo de operacoes.
        </p>
        <div class="hero__actions">
          <router-link to="/login" class="hero__btn hero__btn--primary">
            Comece agora
            <svg class="hero__btn-arrow" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M10.293 3.293a1 1 0 011.414 0l6 6a1 1 0 010 1.414l-6 6a1 1 0 01-1.414-1.414L14.586 11H3a1 1 0 110-2h11.586l-4.293-4.293a1 1 0 010-1.414z" clip-rule="evenodd" /></svg>
          </router-link>
          <a href="#funcionalidades" class="hero__btn hero__btn--outline">Saiba mais</a>
        </div>
        <div class="hero__stats">
          <div class="hero__stat">
            <span class="hero__stat-value">Tempo Real</span>
            <span class="hero__stat-label">Cotacoes ao vivo</span>
          </div>
          <div class="hero__stat-divider"></div>
          <div class="hero__stat">
            <span class="hero__stat-value">Multi-Ativos</span>
            <span class="hero__stat-label">Forex, Crypto, Acoes</span>
          </div>
          <div class="hero__stat-divider"></div>
          <div class="hero__stat">
            <span class="hero__stat-value">100%</span>
            <span class="hero__stat-label">Seguro e confiavel</span>
          </div>
        </div>
      </div>
    </section>

    <!-- Features -->
    <section id="funcionalidades" class="features">
      <div class="landing-container">
        <div class="section-header">
          <span class="section-header__tag">Funcionalidades</span>
          <h2 class="section-header__title">Tudo que voce precisa para sua consultoria</h2>
          <p class="section-header__desc">
            Uma plataforma completa para consultores e clientes gerenciarem suas operacoes de trading.
          </p>
        </div>
        <div class="features__grid">
          <div v-for="f in features" :key="f.title" class="feature-card">
            <div class="feature-card__icon" :style="{ background: f.color }">
              <svg v-html="f.icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="feature-card__svg"></svg>
            </div>
            <h3 class="feature-card__title">{{ f.title }}</h3>
            <p class="feature-card__desc">{{ f.desc }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- Como funciona -->
    <section id="como-funciona" class="how-it-works">
      <div class="landing-container">
        <div class="section-header">
          <span class="section-header__tag">Como funciona</span>
          <h2 class="section-header__title">Simples e eficiente</h2>
          <p class="section-header__desc">
            Em poucos passos, voce ja esta operando com sua equipe.
          </p>
        </div>
        <div class="steps">
          <div v-for="(step, i) in steps" :key="i" class="step">
            <div class="step__number">{{ i + 1 }}</div>
            <h3 class="step__title">{{ step.title }}</h3>
            <p class="step__desc">{{ step.desc }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- Plans -->
    <section id="planos" class="plans">
      <div class="landing-container">
        <div class="section-header">
          <span class="section-header__tag">Planos</span>
          <h2 class="section-header__title">Escolha o plano ideal para sua empresa</h2>
          <p class="section-header__desc">
            Planos flexiveis que crescem com o seu negocio.
          </p>
        </div>
        <div v-if="loadingPlanos" class="plans__loading">
          <div class="plans__spinner"></div>
          <span>Carregando planos...</span>
        </div>
        <div v-else-if="planos.length" class="plans__grid">
          <div
            v-for="(plano, i) in planos"
            :key="plano.id"
            class="plan-card"
            :class="{ 'plan-card--featured': i === 1 }"
          >
            <div v-if="i === 1" class="plan-card__badge">Mais popular</div>
            <h3 class="plan-card__name">{{ plano.nome }}</h3>
            <div class="plan-card__price">
              <span class="plan-card__currency">R$</span>
              <span class="plan-card__value">{{ formatPrice(plano.preco) }}</span>
              <span class="plan-card__period">/mes</span>
            </div>
            <ul class="plan-card__features">
              <li class="plan-card__feature">
                <svg class="plan-card__check" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd" /></svg>
                Ate {{ plano.maxUsuarios }} usuarios
              </li>
              <li class="plan-card__feature">
                <svg class="plan-card__check" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd" /></svg>
                Carteiras ilimitadas
              </li>
              <li class="plan-card__feature">
                <svg class="plan-card__check" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd" /></svg>
                Cotacoes em tempo real
              </li>
              <li class="plan-card__feature">
                <svg class="plan-card__check" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd" /></svg>
                Notificacoes (Email, Telegram, WhatsApp)
              </li>
              <li class="plan-card__feature">
                <svg class="plan-card__check" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd" /></svg>
                Relatorios e exportacao
              </li>
            </ul>
            <router-link
              to="/login"
              class="plan-card__btn"
              :class="i === 1 ? 'plan-card__btn--primary' : 'plan-card__btn--outline'"
            >
              Comecar agora
            </router-link>
          </div>
        </div>
        <!-- Fallback static plans if API is unavailable -->
        <div v-else class="plans__grid">
          <div v-for="(sp, i) in staticPlans" :key="i" class="plan-card" :class="{ 'plan-card--featured': i === 1 }">
            <div v-if="i === 1" class="plan-card__badge">Mais popular</div>
            <h3 class="plan-card__name">{{ sp.nome }}</h3>
            <div class="plan-card__price">
              <span class="plan-card__currency">R$</span>
              <span class="plan-card__value">{{ sp.preco }}</span>
              <span class="plan-card__period">/mes</span>
            </div>
            <ul class="plan-card__features">
              <li v-for="feat in sp.features" :key="feat" class="plan-card__feature">
                <svg class="plan-card__check" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd" /></svg>
                {{ feat }}
              </li>
            </ul>
            <router-link to="/login" class="plan-card__btn" :class="i === 1 ? 'plan-card__btn--primary' : 'plan-card__btn--outline'">
              Comecar agora
            </router-link>
          </div>
        </div>
      </div>
    </section>

    <!-- CTA -->
    <section class="cta">
      <div class="cta__bg">
        <div class="cta__shape cta__shape--1"></div>
        <div class="cta__shape cta__shape--2"></div>
      </div>
      <div class="landing-container cta__content">
        <h2 class="cta__title">Pronto para transformar sua consultoria?</h2>
        <p class="cta__desc">
          Junte-se a consultores que ja usam o TradeLink para gerenciar carteiras e recomendacoes.
        </p>
        <div class="cta__actions">
          <router-link to="/login" class="cta__btn cta__btn--white">
            Criar conta gratuita
          </router-link>
          <router-link to="/login" class="cta__btn cta__btn--ghost">
            Ja tenho conta
          </router-link>
        </div>
      </div>
    </section>

    <!-- Footer -->
    <footer class="landing-footer">
      <div class="landing-container landing-footer__inner">
        <div class="landing-footer__brand">
          <div class="landing-nav__logo" aria-hidden="true" style="width: 32px; height: 32px; border-radius: 8px;">
            <span style="font-size: 1.125rem;">&#9672;</span>
          </div>
          <span class="landing-footer__name">TradeLink</span>
        </div>
        <p class="landing-footer__copy">&copy; {{ new Date().getFullYear() }} TradeLink. Todos os direitos reservados.</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../api/axiosInstance'

const planos = ref([])
const loadingPlanos = ref(true)

const features = [
  {
    title: 'Carteiras Inteligentes',
    desc: 'Crie e gerencie carteiras com recomendacoes detalhadas de entrada, alvo e stop-loss para seus clientes.',
    icon: '<rect x="2" y="3" width="20" height="14" rx="2"/><line x1="8" y1="21" x2="16" y2="21"/><line x1="12" y1="17" x2="12" y2="21"/>',
    color: 'rgba(99, 102, 241, 0.1)'
  },
  {
    title: 'Cotacoes em Tempo Real',
    desc: 'Acompanhe precos de Forex, Criptomoedas e Acoes com dados atualizados de multiplas fontes confiaveis.',
    icon: '<polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/>',
    color: 'rgba(34, 197, 94, 0.1)'
  },
  {
    title: 'Recomendacoes Automatizadas',
    desc: 'Envie recomendacoes de compra e venda diretamente para seus clientes com notificacoes instantaneas.',
    icon: '<path d="M22 2L11 13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/>',
    color: 'rgba(139, 92, 246, 0.1)'
  },
  {
    title: 'Notificacoes Multi-Canal',
    desc: 'Notifique clientes por Email, Telegram e WhatsApp automaticamente quando novas recomendacoes forem criadas.',
    icon: '<path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/>',
    color: 'rgba(234, 179, 8, 0.1)'
  },
  {
    title: 'Relatorios Detalhados',
    desc: 'Gere relatorios de performance com graficos e metricas para acompanhar a evolucao das carteiras.',
    icon: '<line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/>',
    color: 'rgba(239, 68, 68, 0.1)'
  },
  {
    title: 'Gestao de Clientes',
    desc: 'Gerencie seus clientes, controle acessos e acompanhe o engajamento de cada um na plataforma.',
    icon: '<path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/>',
    color: 'rgba(6, 182, 212, 0.1)'
  }
]

const steps = [
  { title: 'Cadastre sua empresa', desc: 'Crie sua conta, configure os dados da empresa e escolha o plano ideal para seu negocio.' },
  { title: 'Adicione seus clientes', desc: 'Convide seus clientes para a plataforma e organize-os em carteiras personalizadas.' },
  { title: 'Envie recomendacoes', desc: 'Crie recomendacoes de trading com entrada, alvo e stop-loss. Seus clientes recebem na hora.' },
  { title: 'Acompanhe resultados', desc: 'Monitore a performance das carteiras, gere relatorios e tome decisoes baseadas em dados.' }
]

const staticPlans = [
  {
    nome: 'Starter',
    preco: '49,90',
    features: ['Ate 5 usuarios', 'Carteiras ilimitadas', 'Cotacoes em tempo real', 'Notificacoes por email', 'Relatorios basicos']
  },
  {
    nome: 'Profissional',
    preco: '99,90',
    features: ['Ate 20 usuarios', 'Carteiras ilimitadas', 'Cotacoes em tempo real', 'Notificacoes multi-canal', 'Relatorios completos']
  },
  {
    nome: 'Enterprise',
    preco: '199,90',
    features: ['Usuarios ilimitados', 'Carteiras ilimitadas', 'Cotacoes em tempo real', 'Notificacoes multi-canal', 'Suporte prioritario']
  }
]

function formatPrice(value) {
  return Number(value).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

onMounted(async () => {
  try {
    const res = await api.get('/auth/planos')
    planos.value = res.data || []
  } catch {
    planos.value = []
  } finally {
    loadingPlanos.value = false
  }
})
</script>

<style scoped>
/* ---- Layout ---- */
.landing {
  background: rgb(var(--tl-surface));
  color: rgb(var(--tl-text));
  overflow-x: hidden;
}

.landing-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1.5rem;
}

/* ---- Navbar ---- */
.landing-nav {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 50;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(var(--tl-border), 0.5);
}

.landing-nav__inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
}

.landing-nav__brand {
  display: flex;
  align-items: center;
  gap: 0.625rem;
}

.landing-nav__logo {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, rgb(99 102 241), rgb(139 92 246));
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
}

.landing-nav__logo-icon {
  font-size: 1.25rem;
  color: white;
  font-weight: 700;
  line-height: 1;
}

.landing-nav__name {
  font-size: 1.25rem;
  font-weight: 700;
  color: rgb(var(--tl-text));
  letter-spacing: -0.02em;
}

.landing-nav__links {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.landing-nav__link {
  padding: 0.5rem 0.75rem;
  font-size: 0.875rem;
  font-weight: 500;
  color: rgb(var(--tl-text-muted));
  text-decoration: none;
  border-radius: 8px;
  transition: color 0.2s, background 0.2s;
}

.landing-nav__link:hover {
  color: rgb(var(--tl-text));
  background: rgb(var(--tl-surface-alt));
}

.landing-nav__btn {
  padding: 0.5rem 1rem;
  font-size: 0.875rem;
  font-weight: 600;
  border-radius: 8px;
  text-decoration: none;
  transition: all 0.2s;
}

.landing-nav__btn--ghost {
  color: rgb(var(--tl-text));
}

.landing-nav__btn--ghost:hover {
  background: rgb(var(--tl-surface-alt));
}

.landing-nav__btn--solid {
  background: rgb(var(--tl-primary));
  color: white;
}

.landing-nav__btn--solid:hover {
  background: rgb(var(--tl-primary-hover));
}

/* ---- Hero ---- */
.hero {
  position: relative;
  padding: 8rem 0 5rem;
  text-align: center;
  overflow: hidden;
}

.hero__bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgb(238 242 255) 0%, rgb(245 243 255) 50%, rgb(254 249 195 / 0.2) 100%);
}

.hero__shape {
  position: absolute;
  border-radius: 50%;
  filter: blur(100px);
  opacity: 0.5;
}

.hero__shape--1 {
  width: 500px;
  height: 500px;
  background: rgba(99, 102, 241, 0.15);
  top: -150px;
  right: -100px;
  animation: float 20s ease-in-out infinite;
}

.hero__shape--2 {
  width: 400px;
  height: 400px;
  background: rgba(139, 92, 246, 0.12);
  bottom: -100px;
  left: -100px;
  animation: float 25s ease-in-out infinite reverse;
}

.hero__shape--3 {
  width: 250px;
  height: 250px;
  background: rgba(234, 179, 8, 0.1);
  top: 40%;
  left: 60%;
  animation: float 15s ease-in-out infinite;
  animation-delay: -5s;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(30px, -30px); }
}

.hero__content {
  position: relative;
  z-index: 1;
}

.hero__badge {
  display: inline-block;
  padding: 0.375rem 1rem;
  font-size: 0.8125rem;
  font-weight: 600;
  color: rgb(var(--tl-primary));
  background: rgba(99, 102, 241, 0.1);
  border: 1px solid rgba(99, 102, 241, 0.2);
  border-radius: 999px;
  margin-bottom: 1.5rem;
  letter-spacing: 0.02em;
}

.hero__title {
  font-size: clamp(2rem, 5vw, 3.5rem);
  font-weight: 800;
  line-height: 1.15;
  color: rgb(var(--tl-text));
  letter-spacing: -0.03em;
  margin: 0 auto 1.25rem;
  max-width: 800px;
}

.hero__highlight {
  background: linear-gradient(135deg, rgb(99 102 241), rgb(139 92 246));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero__subtitle {
  font-size: 1.125rem;
  color: rgb(var(--tl-text-muted));
  line-height: 1.7;
  max-width: 600px;
  margin: 0 auto 2.5rem;
}

.hero__actions {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  flex-wrap: wrap;
  margin-bottom: 3.5rem;
}

.hero__btn {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.875rem 2rem;
  font-size: 1rem;
  font-weight: 600;
  border-radius: 12px;
  text-decoration: none;
  transition: all 0.2s;
}

.hero__btn--primary {
  background: rgb(var(--tl-primary));
  color: white;
  box-shadow: 0 4px 16px rgba(99, 102, 241, 0.35);
}

.hero__btn--primary:hover {
  background: rgb(var(--tl-primary-hover));
  box-shadow: 0 8px 24px rgba(99, 102, 241, 0.4);
  transform: translateY(-1px);
}

.hero__btn-arrow {
  width: 18px;
  height: 18px;
}

.hero__btn--outline {
  color: rgb(var(--tl-text));
  border: 1px solid rgb(var(--tl-border));
  background: rgb(var(--tl-surface));
}

.hero__btn--outline:hover {
  border-color: rgb(var(--tl-primary));
  color: rgb(var(--tl-primary));
}

.hero__stats {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 2rem;
  flex-wrap: wrap;
}

.hero__stat {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.hero__stat-value {
  font-size: 1.125rem;
  font-weight: 700;
  color: rgb(var(--tl-text));
}

.hero__stat-label {
  font-size: 0.8125rem;
  color: rgb(var(--tl-text-muted));
}

.hero__stat-divider {
  width: 1px;
  height: 36px;
  background: rgb(var(--tl-border));
}

/* ---- Section Header ---- */
.section-header {
  text-align: center;
  margin-bottom: 3.5rem;
}

.section-header__tag {
  display: inline-block;
  padding: 0.25rem 0.75rem;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: rgb(var(--tl-primary));
  background: rgba(99, 102, 241, 0.08);
  border-radius: 999px;
  margin-bottom: 1rem;
}

.section-header__title {
  font-size: clamp(1.5rem, 3vw, 2.25rem);
  font-weight: 800;
  color: rgb(var(--tl-text));
  letter-spacing: -0.02em;
  margin: 0 0 0.75rem;
}

.section-header__desc {
  font-size: 1.0625rem;
  color: rgb(var(--tl-text-muted));
  max-width: 550px;
  margin: 0 auto;
  line-height: 1.6;
}

/* ---- Features ---- */
.features {
  padding: 6rem 0;
}

.features__grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 1.5rem;
}

.feature-card {
  padding: 2rem;
  border-radius: var(--tl-radius);
  border: 1px solid rgb(var(--tl-border));
  background: rgb(var(--tl-surface));
  transition: box-shadow 0.3s, border-color 0.3s, transform 0.3s;
}

.feature-card:hover {
  box-shadow: var(--tl-shadow-md);
  border-color: rgba(99, 102, 241, 0.25);
  transform: translateY(-2px);
}

.feature-card__icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 1.25rem;
}

.feature-card__svg {
  width: 24px;
  height: 24px;
  color: rgb(var(--tl-primary));
}

.feature-card__title {
  font-size: 1.0625rem;
  font-weight: 700;
  color: rgb(var(--tl-text));
  margin: 0 0 0.5rem;
}

.feature-card__desc {
  font-size: 0.9375rem;
  color: rgb(var(--tl-text-muted));
  line-height: 1.6;
  margin: 0;
}

/* ---- How it works ---- */
.how-it-works {
  padding: 6rem 0;
  background: rgb(var(--tl-surface-alt));
}

.steps {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 2rem;
}

.step {
  text-align: center;
  padding: 1.5rem;
}

.step__number {
  width: 48px;
  height: 48px;
  margin: 0 auto 1.25rem;
  border-radius: 50%;
  background: linear-gradient(135deg, rgb(99 102 241), rgb(139 92 246));
  color: white;
  font-size: 1.25rem;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 16px rgba(99, 102, 241, 0.3);
}

.step__title {
  font-size: 1.0625rem;
  font-weight: 700;
  color: rgb(var(--tl-text));
  margin: 0 0 0.5rem;
}

.step__desc {
  font-size: 0.9375rem;
  color: rgb(var(--tl-text-muted));
  line-height: 1.6;
  margin: 0;
}

/* ---- Plans ---- */
.plans {
  padding: 6rem 0;
}

.plans__loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  padding: 3rem;
  color: rgb(var(--tl-text-muted));
  font-size: 0.9375rem;
}

.plans__spinner {
  width: 24px;
  height: 24px;
  border: 3px solid rgb(var(--tl-border));
  border-top-color: rgb(var(--tl-primary));
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.plans__grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1.5rem;
  max-width: 1000px;
  margin: 0 auto;
}

.plan-card {
  position: relative;
  padding: 2.5rem 2rem;
  border-radius: 16px;
  border: 1px solid rgb(var(--tl-border));
  background: rgb(var(--tl-surface));
  text-align: center;
  transition: box-shadow 0.3s, border-color 0.3s, transform 0.3s;
}

.plan-card:hover {
  box-shadow: var(--tl-shadow-md);
  transform: translateY(-2px);
}

.plan-card--featured {
  border-color: rgb(var(--tl-primary));
  box-shadow: 0 8px 32px rgba(99, 102, 241, 0.15);
}

.plan-card--featured:hover {
  box-shadow: 0 12px 40px rgba(99, 102, 241, 0.2);
}

.plan-card__badge {
  position: absolute;
  top: -12px;
  left: 50%;
  transform: translateX(-50%);
  padding: 0.25rem 1rem;
  font-size: 0.75rem;
  font-weight: 600;
  color: white;
  background: linear-gradient(135deg, rgb(99 102 241), rgb(139 92 246));
  border-radius: 999px;
  white-space: nowrap;
}

.plan-card__name {
  font-size: 1.25rem;
  font-weight: 700;
  color: rgb(var(--tl-text));
  margin: 0 0 1rem;
}

.plan-card__price {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 0.125rem;
  margin-bottom: 2rem;
}

.plan-card__currency {
  font-size: 1.125rem;
  font-weight: 600;
  color: rgb(var(--tl-text-muted));
}

.plan-card__value {
  font-size: 2.5rem;
  font-weight: 800;
  color: rgb(var(--tl-text));
  letter-spacing: -0.03em;
  line-height: 1;
}

.plan-card__period {
  font-size: 0.9375rem;
  color: rgb(var(--tl-text-muted));
  font-weight: 500;
}

.plan-card__features {
  list-style: none;
  padding: 0;
  margin: 0 0 2rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.plan-card__feature {
  display: flex;
  align-items: center;
  gap: 0.625rem;
  font-size: 0.9375rem;
  color: rgb(var(--tl-text-muted));
  text-align: left;
}

.plan-card__check {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
  color: rgb(var(--tl-success));
}

.plan-card__btn {
  display: block;
  width: 100%;
  padding: 0.875rem 1.5rem;
  font-size: 0.9375rem;
  font-weight: 600;
  border-radius: 10px;
  text-decoration: none;
  text-align: center;
  transition: all 0.2s;
}

.plan-card__btn--primary {
  background: rgb(var(--tl-primary));
  color: white;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
}

.plan-card__btn--primary:hover {
  background: rgb(var(--tl-primary-hover));
  box-shadow: 0 6px 20px rgba(99, 102, 241, 0.35);
}

.plan-card__btn--outline {
  color: rgb(var(--tl-primary));
  border: 1px solid rgba(99, 102, 241, 0.3);
  background: transparent;
}

.plan-card__btn--outline:hover {
  background: rgba(99, 102, 241, 0.06);
  border-color: rgb(var(--tl-primary));
}

/* ---- CTA ---- */
.cta {
  position: relative;
  padding: 6rem 0;
  overflow: hidden;
}

.cta__bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgb(99 102 241) 0%, rgb(139 92 246) 50%, rgb(79 70 229) 100%);
}

.cta__shape {
  position: absolute;
  border-radius: 50%;
  filter: blur(100px);
  opacity: 0.3;
}

.cta__shape--1 {
  width: 400px;
  height: 400px;
  background: white;
  top: -150px;
  right: -100px;
}

.cta__shape--2 {
  width: 300px;
  height: 300px;
  background: rgba(196, 181, 253, 0.5);
  bottom: -100px;
  left: -50px;
}

.cta__content {
  position: relative;
  z-index: 1;
  text-align: center;
}

.cta__title {
  font-size: clamp(1.5rem, 3vw, 2.25rem);
  font-weight: 800;
  color: white;
  letter-spacing: -0.02em;
  margin: 0 0 1rem;
}

.cta__desc {
  font-size: 1.0625rem;
  color: rgba(255, 255, 255, 0.8);
  max-width: 500px;
  margin: 0 auto 2.5rem;
  line-height: 1.6;
}

.cta__actions {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  flex-wrap: wrap;
}

.cta__btn {
  padding: 0.875rem 2rem;
  font-size: 1rem;
  font-weight: 600;
  border-radius: 12px;
  text-decoration: none;
  transition: all 0.2s;
}

.cta__btn--white {
  background: white;
  color: rgb(var(--tl-primary));
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.cta__btn--white:hover {
  transform: translateY(-1px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
}

.cta__btn--ghost {
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.35);
}

.cta__btn--ghost:hover {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.6);
}

/* ---- Footer ---- */
.landing-footer {
  padding: 2rem 0;
  border-top: 1px solid rgb(var(--tl-border));
}

.landing-footer__inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 1rem;
}

.landing-footer__brand {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.landing-footer__name {
  font-size: 1rem;
  font-weight: 700;
  color: rgb(var(--tl-text));
}

.landing-footer__copy {
  font-size: 0.8125rem;
  color: rgb(var(--tl-text-muted));
  margin: 0;
}

/* ---- Responsive ---- */
@media (max-width: 768px) {
  .landing-nav__link {
    display: none;
  }

  .landing-nav__link--desktop {
    display: none;
  }

  .hero {
    padding: 7rem 0 4rem;
  }

  .hero__stats {
    gap: 1.25rem;
  }

  .hero__stat-divider {
    display: none;
  }

  .features__grid {
    grid-template-columns: 1fr;
  }

  .plans__grid {
    grid-template-columns: 1fr;
    max-width: 400px;
  }

  .steps {
    grid-template-columns: 1fr;
    max-width: 400px;
    margin: 0 auto;
  }
}

@media (max-width: 480px) {
  .hero__actions {
    flex-direction: column;
  }

  .hero__btn {
    width: 100%;
    justify-content: center;
  }

  .cta__actions {
    flex-direction: column;
  }

  .cta__btn {
    width: 100%;
    text-align: center;
  }

  .landing-footer__inner {
    flex-direction: column;
    text-align: center;
  }
}
</style>

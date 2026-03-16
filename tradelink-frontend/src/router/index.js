import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

import LoginView from '../views/auth/LoginView.vue'
import CadastroView from '../views/auth/CadastroView.vue'
import AcessoBloqueadoView from '../views/auth/AcessoBloqueadoView.vue'
import AppLayout from '../components/layout/AppLayout.vue'
import FaqView from '../views/public/FaqView.vue'
import PrecosView from '../views/public/PrecosView.vue'

import DashboardAdminMax from '../views/admin-max/DashboardAdminMax.vue'
import EmpresasView from '../views/admin-max/EmpresasView.vue'
import EmpresaDetailView from '../views/admin-max/EmpresaDetailView.vue'
import PlanosView from '../views/admin-max/PlanosView.vue'
import UsuariosView from '../views/admin-max/UsuariosView.vue'

import DashboardConsultor from '../views/consultor/DashboardConsultor.vue'
import CarteirasView from '../views/consultor/CarteirasView.vue'
import CarteiraDetailView from '../views/consultor/CarteiraDetailView.vue'
import ClientesView from '../views/consultor/ClientesView.vue'
import CotacoesView from '../views/consultor/CotacoesView.vue'
import RelatoriosView from '../views/consultor/RelatoriosView.vue'
import PainelRebalanceamentoView from '../views/consultor/PainelRebalanceamentoView.vue'

import DashboardCliente from '../views/cliente/DashboardCliente.vue'
import CarteirasClienteView from '../views/cliente/CarteirasClienteView.vue'
import CarteiraClienteDetailView from '../views/cliente/CarteiraClienteDetailView.vue'
import CotacoesClienteView from '../views/cliente/CotacoesClienteView.vue'
import RelatoriosClienteView from '../views/cliente/RelatoriosClienteView.vue'
import ConfiguracoesNotificacaoView from '../views/ConfiguracoesNotificacaoView.vue'
import CotacaoDetailView from '../views/CotacaoDetailView.vue'

const routes = [
  { path: '/login', name: 'Login', component: LoginView, meta: { public: true } },
  { path: '/cadastro', name: 'Cadastro', component: CadastroView, meta: { public: true } },
  { path: '/faq', name: 'FaqPublica', component: FaqView, meta: { public: true } },
  { path: '/precos', name: 'Precos', component: PrecosView, meta: { public: true } },
  { path: '/acesso-bloqueado', name: 'AcessoBloqueado', component: AcessoBloqueadoView, meta: { requiresAuth: true } },
  { path: '/verify-otp', name: 'VerifyOtp', component: () => import('../views/auth/VerifyOtpView.vue'), meta: { public: true } },
  { path: '/forgot-password', name: 'ForgotPassword', component: () => import('../views/auth/ForgotPasswordView.vue'), meta: { public: true } },
  { path: '/reset-password', name: 'ResetPassword', component: () => import('../views/auth/ResetPasswordView.vue'), meta: { public: true } },
  { path: '/', redirect: '/login' },
  {
    path: '/admin-max',
    component: AppLayout,
    meta: { requiresAuth: true, role: 'AdminMax' },
    children: [
      { path: '', name: 'AdminMaxDashboard', component: DashboardAdminMax },
      { path: 'empresas', name: 'Empresas', component: EmpresasView },
      { path: 'empresas/:id', name: 'EmpresaDetail', component: EmpresaDetailView },
      { path: 'usuarios', name: 'UsuariosAdminMax', component: UsuariosView },
      { path: 'planos', name: 'Planos', component: PlanosView },
      { path: 'financeiro', name: 'FinanceiroAdminMax', component: () => import('../views/admin-max/FinanceiroView.vue') },
      { path: 'carteiras', name: 'CarteirasAdminMax', component: () => import('../views/admin-max/CarteirasAdminView.vue') },
      { path: 'faq', name: 'FaqAdmin', component: () => import('../views/admin-max/FaqAdminView.vue') },
      { path: 'chat', name: 'ChatAdmin', component: () => import('../views/admin-max/ChatAdminView.vue') },
      { path: 'chamados', name: 'ChamadosAdmin', component: () => import('../views/admin-max/ChamadosAdminView.vue') },
      { path: 'emails-apresentacao', name: 'EmailsApresentacao', component: () => import('../views/admin-max/EmailsApresentacaoView.vue') },
      { path: 'configuracoes', name: 'ConfiguracoesAdminMax', component: ConfiguracoesNotificacaoView }
    ]
  },
  {
    path: '/consultor',
    component: AppLayout,
    meta: { requiresAuth: true, role: 'Admin' },
    children: [
      { path: '', name: 'ConsultorDashboard', component: DashboardConsultor },
      { path: 'carteiras', name: 'Carteiras', component: CarteirasView },
      { path: 'carteiras/:id', name: 'CarteiraDetail', component: CarteiraDetailView },
      { path: 'rebalanceamento', name: 'PainelRebalanceamento', component: PainelRebalanceamentoView },
      { path: 'clientes', name: 'Clientes', component: ClientesView },
      { path: 'cotacoes', name: 'CotacoesConsultor', component: CotacoesView },
      { path: 'cotacoes/:moeda/:parMoeda', name: 'CotacaoDetailConsultor', component: CotacaoDetailView },
      { path: 'relatorios', name: 'RelatoriosConsultor', component: RelatoriosView },
      { path: 'kanban', name: 'KanbanConsultor', component: () => import('../views/consultor/KanbanRecomendacoesView.vue') },
      { path: 'copy-trading', name: 'CopyTrading', component: () => import('../views/consultor/CopyTradingView.vue') },
      { path: 'alertas-preco', name: 'AlertasPrecoConsultor', component: () => import('../views/cliente/AlertasPrecoView.vue') },
      { path: 'heatmap', name: 'HeatMapConsultor', component: () => import('../views/common/HeatMapCotacoesView.vue') },
      { path: 'comparador', name: 'ComparadorConsultor', component: () => import('../views/common/ComparadorMoedasView.vue') },
      { path: 'simulador', name: 'SimuladorConsultor', component: () => import('../views/common/SimuladorInvestimentoView.vue') },
      { path: 'watchlist', name: 'WatchlistConsultor', component: () => import('../views/common/WatchlistView.vue') },
      { path: 'atividades', name: 'AtividadesConsultor', component: () => import('../views/common/AtividadeTimelineView.vue') },
      { path: 'faturas', name: 'FaturasConsultor', component: () => import('../views/consultor/FaturasView.vue') },
      { path: 'chamados', name: 'ChamadosConsultor', component: () => import('../views/consultor/ChamadosConsultorView.vue') },
      { path: 'faq', name: 'FaqConsultor', component: () => import('../views/common/FaqInternaView.vue') },
      { path: 'configuracoes', name: 'ConfiguracoesConsultor', component: ConfiguracoesNotificacaoView }
    ]
  },
  {
    path: '/cliente',
    component: AppLayout,
    meta: { requiresAuth: true, role: 'Cliente' },
    children: [
      { path: '', name: 'ClienteDashboard', component: DashboardCliente },
      { path: 'portfolio', name: 'PortfolioCliente', component: () => import('../views/cliente/PortfolioView.vue') },
      { path: 'carteiras', name: 'CarteirasCliente', component: CarteirasClienteView },
      { path: 'carteiras/:id', name: 'CarteiraClienteDetail', component: CarteiraClienteDetailView },
      { path: 'cotacoes', name: 'CotacoesCliente', component: CotacoesClienteView },
      { path: 'cotacoes/:moeda/:parMoeda', name: 'CotacaoDetailCliente', component: CotacaoDetailView },
      { path: 'relatorios', name: 'RelatoriosCliente', component: RelatoriosClienteView },
      { path: 'performance', name: 'PerformanceCliente', component: () => import('../views/cliente/PerformanceView.vue') },
      { path: 'metas', name: 'MetasCliente', component: () => import('../views/cliente/MetasView.vue') },
      { path: 'alertas-preco', name: 'AlertasPrecoCliente', component: () => import('../views/cliente/AlertasPrecoView.vue') },
      { path: 'heatmap', name: 'HeatMapCliente', component: () => import('../views/common/HeatMapCotacoesView.vue') },
      { path: 'comparador', name: 'ComparadorCliente', component: () => import('../views/common/ComparadorMoedasView.vue') },
      { path: 'simulador', name: 'SimuladorCliente', component: () => import('../views/common/SimuladorInvestimentoView.vue') },
      { path: 'watchlist', name: 'WatchlistCliente', component: () => import('../views/common/WatchlistView.vue') },
      { path: 'atividades', name: 'AtividadesCliente', component: () => import('../views/common/AtividadeTimelineView.vue') },
      { path: 'faturas', name: 'FaturasCliente', component: () => import('../views/cliente/FaturasClienteView.vue') },
      { path: 'chamados', name: 'ChamadosCliente', component: () => import('../views/cliente/ChamadosClienteView.vue') },
      { path: 'faq', name: 'FaqCliente', component: () => import('../views/common/FaqInternaView.vue') },
      { path: 'configuracoes', name: 'ConfiguracoesCliente', component: ConfiguracoesNotificacaoView }
    ]
  },
  {
    path: '/cliente/pos-exclusao',
    name: 'PosExclusao',
    component: () => import('../views/cliente/PosExclusaoView.vue'),
    meta: { requiresAuth: true, role: 'Cliente' }
  },
  {
    path: '/ativar-conta/:token',
    name: 'AtivarConta',
    component: () => import('../views/auth/AtivarContaView.vue'),
    meta: { public: true }
  },
  { path: '/:pathMatch(.*)*', redirect: '/login' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  if (to.meta.public) return next()
  if (to.meta.requiresAuth && !authStore.isAuthenticated) return next('/login')
  if (to.name === 'AcessoBloqueado') return next()
  if (to.name === 'PosExclusao') return next()
  // Redirecionar cliente excluído sem auto-gestão para tela pós-exclusão
  if (authStore.user?.role === 'Cliente' && authStore.user?.clienteExcluido && !authStore.user?.autoGestaoAtiva) {
    if (to.name !== 'PosExclusao') return next('/cliente/pos-exclusao')
  }
  // Consultor com trial expirado e sem plano → faturas para escolher plano
  if (authStore.user?.role === 'Admin' && authStore.user?.precisaEscolherPlano) {
    if (to.name !== 'FaturasConsultor' && to.name !== 'AcessoBloqueado') return next('/consultor/faturas')
  }
  // Cliente auto-cadastro com trial expirado e sem plano → pós-exclusão
  if (authStore.user?.role === 'Cliente' && authStore.user?.autoCadastro && authStore.user?.precisaEscolherPlano) {
    if (to.name !== 'PosExclusao') return next('/cliente/pos-exclusao')
  }
  if (to.meta.role && authStore.user?.role !== to.meta.role) return next(authStore.dashboardRoute)
  next()
})

export default router
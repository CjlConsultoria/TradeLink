import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

import LoginView from '../views/auth/LoginView.vue'
import AppLayout from '../components/layout/AppLayout.vue'

import DashboardAdminMax from '../views/admin-max/DashboardAdminMax.vue'
import EmpresasView from '../views/admin-max/EmpresasView.vue'
import EmpresaDetailView from '../views/admin-max/EmpresaDetailView.vue'
import PlanosView from '../views/admin-max/PlanosView.vue'

import DashboardConsultor from '../views/consultor/DashboardConsultor.vue'
import CarteirasView from '../views/consultor/CarteirasView.vue'
import CarteiraDetailView from '../views/consultor/CarteiraDetailView.vue'
import ClientesView from '../views/consultor/ClientesView.vue'
import CotacoesView from '../views/consultor/CotacoesView.vue'
import RelatoriosView from '../views/consultor/RelatoriosView.vue'

import DashboardCliente from '../views/cliente/DashboardCliente.vue'
import CarteirasClienteView from '../views/cliente/CarteirasClienteView.vue'
import CarteiraClienteDetailView from '../views/cliente/CarteiraClienteDetailView.vue'
import CotacoesClienteView from '../views/cliente/CotacoesClienteView.vue'
import RelatoriosClienteView from '../views/cliente/RelatoriosClienteView.vue'
import ConfiguracoesNotificacaoView from '../views/ConfiguracoesNotificacaoView.vue'
import CotacaoDetailView from '../views/CotacaoDetailView.vue'

const routes = [
  { path: '/login', name: 'Login', component: LoginView, meta: { public: true } },
  { path: '/', redirect: '/login' },
  {
    path: '/admin-max',
    component: AppLayout,
    meta: { requiresAuth: true, role: 'AdminMax' },
    children: [
      { path: '', name: 'AdminMaxDashboard', component: DashboardAdminMax },
      { path: 'empresas', name: 'Empresas', component: EmpresasView },
      { path: 'empresas/:id', name: 'EmpresaDetail', component: EmpresaDetailView },
      { path: 'planos', name: 'Planos', component: PlanosView },
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
      { path: 'clientes', name: 'Clientes', component: ClientesView },
      { path: 'cotacoes', name: 'CotacoesConsultor', component: CotacoesView },
      { path: 'cotacoes/:moeda/:parMoeda', name: 'CotacaoDetailConsultor', component: CotacaoDetailView },
      { path: 'relatorios', name: 'RelatoriosConsultor', component: RelatoriosView },
      { path: 'faturas', name: 'FaturasConsultor', component: () => import('../views/consultor/FaturasView.vue') },
      { path: 'configuracoes', name: 'ConfiguracoesConsultor', component: ConfiguracoesNotificacaoView }
    ]
  },
  {
    path: '/cliente',
    component: AppLayout,
    meta: { requiresAuth: true, role: 'Cliente' },
    children: [
      { path: '', name: 'ClienteDashboard', component: DashboardCliente },
      { path: 'carteiras', name: 'CarteirasCliente', component: CarteirasClienteView },
      { path: 'carteiras/:id', name: 'CarteiraClienteDetail', component: CarteiraClienteDetailView },
      { path: 'cotacoes', name: 'CotacoesCliente', component: CotacoesClienteView },
      { path: 'cotacoes/:moeda/:parMoeda', name: 'CotacaoDetailCliente', component: CotacaoDetailView },
      { path: 'relatorios', name: 'RelatoriosCliente', component: RelatoriosClienteView },
      { path: 'faturas', name: 'FaturasCliente', component: () => import('../views/cliente/FaturasClienteView.vue') },
      { path: 'configuracoes', name: 'ConfiguracoesCliente', component: ConfiguracoesNotificacaoView }
    ]
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
  if (to.meta.role && authStore.user?.role !== to.meta.role) return next(authStore.dashboardRoute)
  next()
})

export default router
import { createRouter, createWebHistory } from 'vue-router'

// Importa suas telas da pasta views
import Login from '../views/Login.vue'
import Cadastro from '../views/Cadastro.vue'

const routes = [
  {
    path: '/',
    name: 'Login',
    component: Login
  },
  {
    path: '/cadastro',
    name: 'Cadastro',
    component: Cadastro
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router

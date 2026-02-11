import api from './axiosInstance'

export default {
  listarTodos() {
    return api.get('/admin-max/usuarios')
  },
  getById(id) {
    return api.get(`/admin-max/usuarios/${id}`)
  },
  atualizarAdmin(id, data) {
    return api.put(`/admin-max/usuarios/${id}`, data)
  },
  ativar(id) {
    return api.post(`/admin-max/usuarios/${id}/ativar`)
  },
  alterarSenhaAdmin(id, data) {
    return api.put(`/admin-max/usuarios/${id}/senha`, data)
  },
  desativar(id) {
    return api.delete(`/admin-max/usuarios/${id}`)
  },
  listarClientes() {
    return api.get('/consultor/clientes')
  },
  criarCliente(data) {
    return api.post('/consultor/clientes', data)
  },
  inativarCliente(id) {
    return api.put(`/consultor/clientes/${id}/inativar`)
  },
  ativarCliente(id) {
    return api.put(`/consultor/clientes/${id}/ativar`)
  },
  excluirCliente(id) {
    return api.delete(`/consultor/clientes/${id}`)
  },
  dashboard() {
    return api.get('/cliente/dashboard')
  },

  // Perfil e notificações (qualquer usuário autenticado)
  me() {
    return api.get('/me')
  },
  configNotificacao() {
    return api.get('/me/config-notificacao')
  },
  empresaNotificacoes() {
    return api.get('/me/empresa-notificacoes')
  },
  atualizarTelegramChatId(telegramChatId) {
    return api.put('/me/telegram-chat-id', { telegramChatId })
  },
  registrarPushSubscription(subscription) {
    return api.post('/me/push-subscription', subscription)
  },
  removerPushSubscription(endpoint) {
    return api.delete('/me/push-subscription', { params: { endpoint } })
  },
  trocarSenha(senhaAtual, novaSenha) {
    return api.put('/me/senha', { senhaAtual, novaSenha })
  }
}
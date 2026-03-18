import api from './axiosInstance'

/**
 * API de chat. Detecta o role do usuário para usar o prefix correto.
 */
function getPrefix() {
  try {
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    if (user.role === 'AdminMax') return '/admin-max'
    if (user.role === 'Admin') return '/consultor'
    return '/cliente'
  } catch {
    return '/cliente'
  }
}

export default {
  // Cliente/Consultor
  listarConversas() {
    return api.get(`${getPrefix()}/chat/conversas`)
  },
  iniciarConversa(assunto) {
    return api.post(`${getPrefix()}/chat/conversas`, assunto ? { assunto } : {})
  },
  listarMensagens(conversaId) {
    return api.get(`${getPrefix()}/chat/conversas/${conversaId}/mensagens`)
  },
  enviarMensagem(conversaId, conteudo) {
    return api.post(`${getPrefix()}/chat/conversas/${conversaId}/mensagens`, { conteudo })
  },
  contarNaoLidas() {
    return api.get(`${getPrefix()}/chat/nao-lidas`)
  },

  // AdminMax
  listarTodasConversas() {
    return api.get('/admin-max/chat/conversas')
  },
  listarMensagensAdmin(conversaId) {
    return api.get(`/admin-max/chat/conversas/${conversaId}/mensagens`)
  },
  enviarMensagemAdmin(conversaId, conteudo) {
    return api.post(`/admin-max/chat/conversas/${conversaId}/mensagens`, { conteudo })
  },
  fecharConversa(conversaId) {
    return api.put(`/admin-max/chat/conversas/${conversaId}/fechar`)
  },
  contarNaoLidasAdmin() {
    return api.get('/admin-max/chat/nao-lidas')
  }
}

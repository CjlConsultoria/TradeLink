import api from './axiosInstance'

export default {
  listar() {
    return api.get('/consultor/carteiras')
  },
  buscar(id) {
    return api.get(`/consultor/carteiras/${id}`)
  },
  criar(data) {
    return api.post('/consultor/carteiras', data)
  },
  atualizar(id, data) {
    return api.put(`/consultor/carteiras/${id}`, data)
  },
  desativar(id) {
    return api.delete(`/consultor/carteiras/${id}`)
  },
  excluir(id) {
    return api.delete(`/consultor/carteiras/${id}`, { params: { excluir: true } })
  },
  listarClientes(carteiraId) {
    return api.get(`/consultor/carteiras/${carteiraId}/clientes`)
  },
  atribuirCliente(carteiraId, clienteId) {
    return api.post(`/consultor/carteiras/${carteiraId}/clientes`, { clienteId })
  },
  removerCliente(carteiraId, clienteId) {
    return api.delete(`/consultor/carteiras/${carteiraId}/clientes/${clienteId}`)
  },
  listarComoCliente() {
    return api.get('/cliente/carteiras')
  },
  buscarComoCliente(id) {
    return api.get(`/cliente/carteiras/${id}`)
  }
}
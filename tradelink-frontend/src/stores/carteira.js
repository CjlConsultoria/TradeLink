import { defineStore } from 'pinia'
import carteiraApi from '../api/carteiraApi'
import { ref } from 'vue'

export const useCarteiraStore = defineStore('carteira', () => {
  const carteiras = ref([])
  const carteiraAtual = ref(null)
  const loading = ref(false)

  async function listar() {
    loading.value = true
    try {
      const res = await carteiraApi.listar()
      carteiras.value = res.data
    } finally {
      loading.value = false
    }
  }

  async function buscar(id) {
    loading.value = true
    try {
      const res = await carteiraApi.buscar(id)
      carteiraAtual.value = res.data
    } finally {
      loading.value = false
    }
  }

  async function listarComoCliente() {
    loading.value = true
    try {
      const res = await carteiraApi.listarComoCliente()
      carteiras.value = res.data
    } finally {
      loading.value = false
    }
  }

  return { carteiras, carteiraAtual, loading, listar, buscar, listarComoCliente }
})
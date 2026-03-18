<template>
  <div>
    <div class="flex items-center justify-between mb-6">
      <h2 class="page-title mb-0">Alertas de Preco</h2>
      <button type="button" class="btn-primary flex items-center gap-2" @click="abrirModal()">
        <span class="text-lg leading-none">+</span> Novo Alerta
      </button>
    </div>

    <p class="text-sm text-gray-500 mb-4">
      Receba notificacoes quando um ativo atingir o preco desejado. Maximo de 20 alertas ativos.
    </p>

    <!-- Loading -->
    <div v-if="loading" class="flex items-center justify-center py-16">
      <div class="flex items-center gap-3 text-gray-500">
        <svg class="animate-spin h-5 w-5" viewBox="0 0 24 24" fill="none">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
        </svg>
        Carregando...
      </div>
    </div>

    <!-- Lista de alertas -->
    <div v-else-if="alertas.length > 0" class="space-y-3">
      <div v-for="a in alertas" :key="a.id" class="card p-4"
           :class="{ 'opacity-60': !a.ativo }">
        <div class="flex items-start justify-between gap-4">
          <div class="flex-1 min-w-0">
            <div class="flex items-center gap-2 mb-1">
              <span class="font-semibold text-gray-900">{{ a.moeda }}/{{ a.parMoeda }}</span>
              <span class="px-2 py-0.5 rounded-full text-xs font-medium"
                    :class="a.tipoAlerta === 'ACIMA' ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'">
                {{ a.tipoAlerta === 'ACIMA' ? 'Acima' : 'Abaixo' }}
              </span>
              <span v-if="a.disparado" class="px-2 py-0.5 rounded-full text-xs font-medium bg-amber-100 text-amber-700">
                Disparado
              </span>
              <span v-else-if="!a.ativo" class="px-2 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-500">
                Inativo
              </span>
              <span v-else class="px-2 py-0.5 rounded-full text-xs font-medium bg-indigo-100 text-indigo-700">
                Ativo
              </span>
            </div>
            <p class="text-sm text-gray-600">
              Alertar quando {{ a.tipoAlerta === 'ACIMA' ? 'acima de' : 'abaixo de' }}
              <strong class="text-gray-900">{{ formatPreco(a.precoAlerta) }}</strong>
            </p>
            <p v-if="a.observacao" class="text-xs text-gray-400 mt-1">{{ a.observacao }}</p>
            <p v-if="a.disparado && a.dataDisparo" class="text-xs text-amber-600 mt-1">
              Disparado em {{ a.dataDisparo }}
            </p>
            <p v-if="a.createdAt" class="text-xs text-gray-400 mt-0.5">Criado em {{ a.createdAt }}</p>
          </div>
          <div class="flex items-center gap-1.5 shrink-0">
            <button type="button" @click="toggleAtivo(a)" class="p-2 rounded-lg hover:bg-gray-100 transition-colors"
                    :title="a.ativo ? 'Desativar' : 'Ativar'">
              <svg v-if="a.ativo" xmlns="http://www.w3.org/2000/svg" class="w-5 h-5 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                <path stroke-linecap="round" stroke-linejoin="round" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
              </svg>
              <svg v-else xmlns="http://www.w3.org/2000/svg" class="w-5 h-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.878 9.878L3 3m6.878 6.878L21 21" />
              </svg>
            </button>
            <button type="button" @click="abrirModal(a)" class="p-2 rounded-lg hover:bg-gray-100 transition-colors" title="Editar">
              <svg xmlns="http://www.w3.org/2000/svg" class="w-5 h-5 text-gray-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
              </svg>
            </button>
            <button type="button" @click="confirmarExcluir(a)" class="p-2 rounded-lg hover:bg-red-50 transition-colors" title="Excluir">
              <svg xmlns="http://www.w3.org/2000/svg" class="w-5 h-5 text-red-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
              </svg>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Estado vazio -->
    <div v-else class="card p-12 text-center">
      <p class="text-gray-500 mb-4">Voce ainda nao tem alertas de preco configurados.</p>
      <button type="button" class="btn-primary" @click="abrirModal()">Criar Primeiro Alerta</button>
    </div>

    <!-- Modal criar/editar -->
    <div v-if="showModal" class="fixed inset-0 z-50 flex items-center justify-center p-4" @click.self="fecharModal">
      <div class="fixed inset-0 bg-black/40" @click="fecharModal"></div>
      <div class="relative bg-white rounded-2xl shadow-xl w-full max-w-md p-6 z-10">
        <h3 class="text-lg font-semibold text-gray-900 mb-4">
          {{ editando ? 'Editar Alerta' : 'Novo Alerta de Preco' }}
        </h3>

        <form @submit.prevent="salvar" class="space-y-4">
          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="text-sm font-medium text-gray-700 mb-1 block">Moeda</label>
              <input v-model="form.moeda" type="text" required class="input-base w-full"
                     placeholder="BTC" :disabled="salvando" />
            </div>
            <div>
              <label class="text-sm font-medium text-gray-700 mb-1 block">Par</label>
              <input v-model="form.parMoeda" type="text" required class="input-base w-full"
                     placeholder="BRL" :disabled="salvando" />
            </div>
          </div>

          <div>
            <label class="text-sm font-medium text-gray-700 mb-1 block">Tipo de Alerta</label>
            <select v-model="form.tipoAlerta" required class="input-base w-full" :disabled="salvando">
              <option value="ACIMA">Preco Acima de</option>
              <option value="ABAIXO">Preco Abaixo de</option>
            </select>
          </div>

          <div>
            <label class="text-sm font-medium text-gray-700 mb-1 block">Preco Alvo</label>
            <input v-model="form.precoAlerta" type="number" step="any" required class="input-base w-full"
                   placeholder="0.00" :disabled="salvando" />
          </div>

          <div>
            <label class="text-sm font-medium text-gray-700 mb-1 block">Observacao (opcional)</label>
            <input v-model="form.observacao" type="text" class="input-base w-full"
                   placeholder="Ex: Meta de venda" :disabled="salvando" />
          </div>

          <p v-if="modalError" class="text-sm text-red-600">{{ modalError }}</p>

          <div class="flex gap-3 pt-2">
            <button type="button" class="btn-secondary flex-1" @click="fecharModal" :disabled="salvando">Cancelar</button>
            <button type="submit" class="btn-primary flex-1" :disabled="salvando">
              {{ salvando ? 'Salvando...' : (editando ? 'Salvar' : 'Criar Alerta') }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import alertaPrecoApi from '../../api/alertaPrecoApi'
import { useToast } from '../../composables/useToast'

const toast = useToast()

const alertas = ref([])
const loading = ref(true)
const showModal = ref(false)
const editando = ref(null)
const salvando = ref(false)
const modalError = ref('')

const formDefault = { moeda: '', parMoeda: '', tipoAlerta: 'ACIMA', precoAlerta: '', observacao: '' }
const form = ref({ ...formDefault })

function formatPreco(val) {
  if (val == null) return '-'
  const n = Number(val)
  if (isNaN(n)) return val
  return n.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 8 })
}

async function carregarAlertas() {
  loading.value = true
  try {
    const res = await alertaPrecoApi.listar()
    alertas.value = res.data || []
  } catch (e) {
    console.error('Erro ao carregar alertas:', e)
    toast.error('Erro ao carregar alertas de preco')
  } finally {
    loading.value = false
  }
}

function abrirModal(alerta = null) {
  modalError.value = ''
  if (alerta) {
    editando.value = alerta.id
    form.value = {
      moeda: alerta.moeda,
      parMoeda: alerta.parMoeda,
      tipoAlerta: alerta.tipoAlerta,
      precoAlerta: alerta.precoAlerta,
      observacao: alerta.observacao || ''
    }
  } else {
    editando.value = null
    form.value = { ...formDefault }
  }
  showModal.value = true
}

function fecharModal() {
  showModal.value = false
  editando.value = null
}

async function salvar() {
  modalError.value = ''
  salvando.value = true
  try {
    const payload = {
      moeda: form.value.moeda.toUpperCase().trim(),
      parMoeda: form.value.parMoeda.toUpperCase().trim(),
      tipoAlerta: form.value.tipoAlerta,
      precoAlerta: Number(form.value.precoAlerta),
      observacao: form.value.observacao || null
    }

    if (editando.value) {
      await alertaPrecoApi.atualizar(editando.value, payload)
      toast.success('Alerta atualizado com sucesso')
    } else {
      await alertaPrecoApi.criar(payload)
      toast.success('Alerta criado com sucesso')
    }

    fecharModal()
    await carregarAlertas()
  } catch (e) {
    const msg = e.response?.data?.message || e.response?.data?.mensagem || 'Erro ao salvar alerta'
    modalError.value = msg
  } finally {
    salvando.value = false
  }
}

async function toggleAtivo(alerta) {
  try {
    await alertaPrecoApi.toggle(alerta.id)
    toast.success(alerta.ativo ? 'Alerta desativado' : 'Alerta reativado')
    await carregarAlertas()
  } catch (e) {
    toast.error('Erro ao alterar status do alerta')
  }
}

async function confirmarExcluir(alerta) {
  if (!confirm(`Excluir alerta de ${alerta.moeda}/${alerta.parMoeda}?`)) return
  try {
    await alertaPrecoApi.excluir(alerta.id)
    toast.success('Alerta excluido')
    await carregarAlertas()
  } catch (e) {
    toast.error('Erro ao excluir alerta')
  }
}

onMounted(() => {
  carregarAlertas()
})
</script>

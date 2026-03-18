<template>
  <div>
    <h2 class="page-title">Metas de Investimento</h2>
    <p class="text-sm text-gray-500 mb-6">Defina metas financeiras e acompanhe seu progresso.</p>

    <!-- Botão criar -->
    <div class="flex justify-end mb-4">
      <button type="button" @click="abrirModal()" class="px-4 py-2 bg-indigo-600 text-white rounded-lg text-sm font-medium hover:bg-indigo-700">
        + Nova Meta
      </button>
    </div>

    <LoadingSpinner v-if="loading" text="Carregando metas..." />

    <div v-else-if="metas.length === 0" class="card p-12 text-center">
      <p class="text-4xl mb-3">🎯</p>
      <p class="text-gray-500 text-lg">Nenhuma meta criada ainda.</p>
      <p class="text-gray-400 text-sm mt-1">Crie sua primeira meta para acompanhar seu progresso!</p>
    </div>

    <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-4">
      <div v-for="m in metas" :key="m.id" class="card p-5 relative"
        :class="m.concluida ? 'border-emerald-300 bg-emerald-50/50' : ''">
        <!-- Badge concluída -->
        <span v-if="m.concluida" class="absolute top-3 right-3 px-2 py-0.5 bg-emerald-100 text-emerald-700 text-xs font-bold rounded-full">
          ✓ Concluída
        </span>

        <h3 class="font-bold text-gray-900 text-lg pr-20">{{ m.titulo }}</h3>
        <p v-if="m.descricao" class="text-sm text-gray-500 mt-1">{{ m.descricao }}</p>

        <!-- Barra de progresso -->
        <div class="mt-4">
          <div class="flex items-center justify-between text-sm mb-1">
            <span class="text-gray-500">Progresso</span>
            <span class="font-bold" :class="percentual(m) >= 100 ? 'text-emerald-600' : 'text-indigo-600'">
              {{ percentual(m).toFixed(0) }}%
            </span>
          </div>
          <div class="w-full bg-gray-200 rounded-full h-3 overflow-hidden">
            <div class="h-3 rounded-full transition-all duration-500"
              :class="percentual(m) >= 100 ? 'bg-emerald-500' : 'bg-indigo-500'"
              :style="{ width: Math.min(percentual(m), 100) + '%' }"></div>
          </div>
          <div class="flex justify-between text-xs text-gray-400 mt-1">
            <span>{{ formatCurrency(m.valorAtual || 0) }}</span>
            <span>{{ formatCurrency(m.valorAlvo) }}</span>
          </div>
        </div>

        <!-- Data limite -->
        <div v-if="m.dataLimite" class="mt-3 text-xs text-gray-400">
          📅 Prazo: {{ formatDate(m.dataLimite) }}
          <span v-if="diasRestantes(m) !== null" :class="diasRestantes(m) < 0 ? 'text-red-500' : diasRestantes(m) < 7 ? 'text-amber-500' : ''">
            ({{ diasRestantes(m) < 0 ? 'Vencida' : diasRestantes(m) + ' dias restantes' }})
          </span>
        </div>

        <!-- Ações -->
        <div class="flex gap-2 mt-4">
          <button type="button" @click="abrirModalValor(m)" v-if="!m.concluida"
            class="px-3 py-1.5 bg-indigo-50 text-indigo-600 rounded-lg text-xs font-medium hover:bg-indigo-100">
            Atualizar valor
          </button>
          <button type="button" @click="abrirModal(m)"
            class="px-3 py-1.5 bg-gray-100 text-gray-600 rounded-lg text-xs font-medium hover:bg-gray-200">
            Editar
          </button>
          <button v-if="!m.concluida" type="button" @click="concluir(m)"
            class="px-3 py-1.5 bg-emerald-50 text-emerald-600 rounded-lg text-xs font-medium hover:bg-emerald-100">
            ✓ Concluir
          </button>
          <button type="button" @click="excluir(m)"
            class="px-3 py-1.5 bg-red-50 text-red-500 rounded-lg text-xs font-medium hover:bg-red-100">
            Excluir
          </button>
        </div>
      </div>
    </div>

    <!-- Modal Criar/Editar -->
    <Teleport to="body">
      <div v-if="modal.show" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40" @click.self="modal.show = false">
        <div class="bg-white rounded-xl shadow-2xl w-full max-w-md p-5 sm:p-6">
          <h3 class="text-lg font-bold mb-4">{{ modal.meta ? 'Editar Meta' : 'Nova Meta' }}</h3>
          <form @submit.prevent="salvar">
            <div class="space-y-3">
              <div>
                <label class="text-xs font-medium text-gray-500">Título *</label>
                <input v-model="modal.form.titulo" required class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm" />
              </div>
              <div>
                <label class="text-xs font-medium text-gray-500">Descrição</label>
                <input v-model="modal.form.descricao" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm" />
              </div>
              <div class="grid grid-cols-2 gap-3">
                <div>
                  <label class="text-xs font-medium text-gray-500">Valor Alvo (R$) *</label>
                  <input v-model.number="modal.form.valorAlvo" type="number" min="1" step="0.01" required class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm" />
                </div>
                <div>
                  <label class="text-xs font-medium text-gray-500">Valor Atual (R$)</label>
                  <input v-model.number="modal.form.valorAtual" type="number" min="0" step="0.01" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm" />
                </div>
              </div>
              <div>
                <label class="text-xs font-medium text-gray-500">Data Limite</label>
                <input v-model="modal.form.dataLimite" type="date" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm" />
              </div>
            </div>
            <div class="flex justify-end gap-2 mt-5">
              <button type="button" @click="modal.show = false" class="px-4 py-2 bg-gray-100 rounded-lg text-sm">Cancelar</button>
              <button type="submit" class="px-4 py-2 bg-indigo-600 text-white rounded-lg text-sm hover:bg-indigo-700">Salvar</button>
            </div>
          </form>
        </div>
      </div>
    </Teleport>

    <!-- Modal Atualizar Valor -->
    <Teleport to="body">
      <div v-if="modalValor.show" class="fixed inset-0 z-50 flex items-center justify-center bg-black/40" @click.self="modalValor.show = false">
        <div class="bg-white rounded-xl shadow-2xl w-full max-w-sm p-6">
          <h3 class="text-lg font-bold mb-4">Atualizar Valor</h3>
          <p class="text-sm text-gray-500 mb-3">{{ modalValor.meta?.titulo }}</p>
          <input v-model.number="modalValor.valor" type="number" min="0" step="0.01"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm mb-4" placeholder="Novo valor atual (R$)" />
          <div class="flex justify-end gap-2">
            <button type="button" @click="modalValor.show = false" class="px-4 py-2 bg-gray-100 rounded-lg text-sm">Cancelar</button>
            <button type="button" @click="atualizarValor" class="px-4 py-2 bg-indigo-600 text-white rounded-lg text-sm hover:bg-indigo-700">Atualizar</button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import metaApi from '../../api/metaApi'
import { useToast } from '../../composables/useToast'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'

const toast = useToast()
const metas = ref([])
const loading = ref(true)
const modal = ref({ show: false, meta: null, form: {} })
const modalValor = ref({ show: false, meta: null, valor: 0 })

onMounted(async () => {
  try { metas.value = (await metaApi.listar()).data || [] }
  catch (e) { console.error(e) }
  finally { loading.value = false }
})

function percentual(m) {
  if (!m.valorAlvo || m.valorAlvo === 0) return 0
  return ((m.valorAtual || 0) / m.valorAlvo) * 100
}

function diasRestantes(m) {
  if (!m.dataLimite) return null
  const hoje = new Date()
  const limite = new Date(m.dataLimite)
  return Math.ceil((limite - hoje) / (1000 * 60 * 60 * 24))
}

function formatCurrency(v) {
  return 'R$ ' + Number(v).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function formatDate(d) {
  if (!d) return '-'
  return new Date(d + 'T00:00:00').toLocaleDateString('pt-BR')
}

function abrirModal(meta = null) {
  modal.value = {
    show: true,
    meta,
    form: meta ? { titulo: meta.titulo, descricao: meta.descricao, valorAlvo: meta.valorAlvo, valorAtual: meta.valorAtual, dataLimite: meta.dataLimite } : { titulo: '', descricao: '', valorAlvo: null, valorAtual: 0, dataLimite: '' }
  }
}

function abrirModalValor(meta) {
  modalValor.value = { show: true, meta, valor: meta.valorAtual || 0 }
}

async function salvar() {
  try {
    if (modal.value.meta) {
      const res = await metaApi.atualizar(modal.value.meta.id, modal.value.form)
      const idx = metas.value.findIndex(m => m.id === modal.value.meta.id)
      if (idx >= 0) metas.value[idx] = res.data
      toast.success('Meta atualizada')
    } else {
      const res = await metaApi.criar(modal.value.form)
      metas.value.unshift(res.data)
      toast.success('Meta criada')
    }
    modal.value.show = false
  } catch (e) { toast.error(e.response?.data?.message || 'Erro ao salvar') }
}

async function atualizarValor() {
  try {
    const res = await metaApi.atualizar(modalValor.value.meta.id, { valorAtual: modalValor.value.valor })
    const idx = metas.value.findIndex(m => m.id === modalValor.value.meta.id)
    if (idx >= 0) metas.value[idx] = res.data
    modalValor.value.show = false
    toast.success('Valor atualizado')
  } catch (e) { toast.error('Erro ao atualizar') }
}

async function concluir(m) {
  try {
    const res = await metaApi.atualizar(m.id, { concluida: true, valorAtual: m.valorAlvo })
    const idx = metas.value.findIndex(x => x.id === m.id)
    if (idx >= 0) metas.value[idx] = res.data
    toast.success('🎉 Meta concluída! Parabéns!')
  } catch (e) { toast.error('Erro') }
}

async function excluir(m) {
  if (!confirm('Excluir meta "' + m.titulo + '"?')) return
  try {
    await metaApi.excluir(m.id)
    metas.value = metas.value.filter(x => x.id !== m.id)
    toast.info('Meta excluída')
  } catch (e) { toast.error('Erro ao excluir') }
}
</script>

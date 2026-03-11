<template>
  <div>
    <h2 class="page-title">Kanban · Recomendações</h2>
    <p class="text-sm text-gray-500 mb-6">Arraste os cards entre colunas para alterar o status. Reordene dentro de cada coluna.</p>

    <!-- Filtro por carteira -->
    <div class="flex flex-wrap gap-3 mb-6">
      <select v-model="carteiraId" class="input-base w-64" @change="carregar">
        <option value="">Todas as carteiras</option>
        <option v-for="c in carteiras" :key="c.id" :value="c.id">{{ c.nome }}</option>
      </select>
      <button type="button" @click="carregar" class="btn-primary text-sm px-4 py-2">Atualizar</button>
    </div>

    <LoadingSpinner v-if="loading" />

    <div v-else class="grid grid-cols-1 lg:grid-cols-3 gap-4">
      <!-- Coluna ATIVA -->
      <div class="kanban-col">
        <div class="kanban-header bg-indigo-50 border-indigo-200 text-indigo-700">
          <span class="kanban-dot bg-indigo-500"></span>
          Ativas
          <span class="kanban-count">{{ ativas.length }}</span>
        </div>
        <draggable
          v-model="ativas"
          group="kanban"
          item-key="id"
          class="kanban-body"
          ghost-class="kanban-ghost"
          drag-class="kanban-drag"
          :animation="200"
          @change="(e) => onDragChange(e, 'ATIVA')"
        >
          <template #item="{ element: r }">
            <div class="kanban-card border-l-indigo-500" :data-id="r.id">
              <div class="kanban-card__handle" title="Arraste para mover">⠿</div>
              <div class="flex items-center justify-between">
                <span class="font-bold text-sm">{{ r.moeda }}/{{ r.parMoeda }}</span>
                <span class="px-2 py-0.5 rounded text-xs font-semibold"
                  :class="r.tipo === 'COMPRA' ? 'bg-emerald-100 text-emerald-700' : 'bg-red-100 text-red-700'">
                  {{ r.tipo }}
                </span>
              </div>
              <div class="mt-2 grid grid-cols-2 gap-1 text-xs text-gray-500">
                <span>Entrada: {{ formatCurrency(r.precoEntrada) }}</span>
                <span>Alvo: {{ formatCurrency(r.precoAlvo) }}</span>
                <span>Stop: {{ formatCurrency(r.stopLoss) }}</span>
                <span v-if="r.quantidade">Qtd: {{ r.quantidade }}</span>
              </div>
              <p v-if="r.observacao" class="text-xs text-gray-400 mt-2 line-clamp-2">{{ r.observacao }}</p>
              <div class="flex gap-1 mt-3">
                <button type="button" @click="executar(r)" class="px-2 py-1 bg-emerald-50 text-emerald-600 rounded text-xs font-medium hover:bg-emerald-100">
                  ✓ Executar
                </button>
                <button type="button" @click="cancelar(r)" class="px-2 py-1 bg-red-50 text-red-500 rounded text-xs font-medium hover:bg-red-100">
                  ✕ Cancelar
                </button>
                <button type="button" @click="abrirCopy(r)" class="px-2 py-1 bg-blue-50 text-blue-600 rounded text-xs font-medium hover:bg-blue-100">
                  📋 Copiar
                </button>
              </div>
            </div>
          </template>
          <template #footer>
            <div v-if="ativas.length === 0" class="text-center py-8 text-gray-400 text-sm">Nenhuma recomendação ativa</div>
          </template>
        </draggable>
      </div>

      <!-- Coluna EXECUTADA -->
      <div class="kanban-col">
        <div class="kanban-header bg-emerald-50 border-emerald-200 text-emerald-700">
          <span class="kanban-dot bg-emerald-500"></span>
          Executadas
          <span class="kanban-count">{{ executadas.length }}</span>
        </div>
        <draggable
          v-model="executadas"
          group="kanban"
          item-key="id"
          class="kanban-body"
          ghost-class="kanban-ghost"
          drag-class="kanban-drag"
          :animation="200"
          @change="(e) => onDragChange(e, 'EXECUTADA')"
        >
          <template #item="{ element: r }">
            <div class="kanban-card border-l-emerald-500" :data-id="r.id">
              <div class="kanban-card__handle" title="Arraste para mover">⠿</div>
              <div class="flex items-center justify-between">
                <span class="font-bold text-sm">{{ r.moeda }}/{{ r.parMoeda }}</span>
                <span class="px-2 py-0.5 rounded text-xs font-semibold"
                  :class="r.tipo === 'COMPRA' ? 'bg-emerald-100 text-emerald-700' : 'bg-red-100 text-red-700'">
                  {{ r.tipo }}
                </span>
              </div>
              <div class="mt-2 grid grid-cols-2 gap-1 text-xs text-gray-500">
                <span>Entrada: {{ formatCurrency(r.precoEntrada) }}</span>
                <span>Alvo: {{ formatCurrency(r.precoAlvo) }}</span>
              </div>
              <p v-if="r.observacao" class="text-xs text-gray-400 mt-2 line-clamp-2">{{ r.observacao }}</p>
            </div>
          </template>
          <template #footer>
            <div v-if="executadas.length === 0" class="text-center py-8 text-gray-400 text-sm">Nenhuma recomendação executada</div>
          </template>
        </draggable>
      </div>

      <!-- Coluna CANCELADA -->
      <div class="kanban-col">
        <div class="kanban-header bg-red-50 border-red-200 text-red-700">
          <span class="kanban-dot bg-red-500"></span>
          Canceladas
          <span class="kanban-count">{{ canceladas.length }}</span>
        </div>
        <draggable
          v-model="canceladas"
          group="kanban"
          item-key="id"
          class="kanban-body"
          ghost-class="kanban-ghost"
          drag-class="kanban-drag"
          :animation="200"
          @change="(e) => onDragChange(e, 'CANCELADA')"
        >
          <template #item="{ element: r }">
            <div class="kanban-card border-l-red-500 opacity-75" :data-id="r.id">
              <div class="kanban-card__handle" title="Arraste para mover">⠿</div>
              <div class="flex items-center justify-between">
                <span class="font-bold text-sm">{{ r.moeda }}/{{ r.parMoeda }}</span>
                <span class="px-2 py-0.5 rounded text-xs font-semibold"
                  :class="r.tipo === 'COMPRA' ? 'bg-emerald-100 text-emerald-700' : 'bg-red-100 text-red-700'">
                  {{ r.tipo }}
                </span>
              </div>
              <div class="mt-2 grid grid-cols-2 gap-1 text-xs text-gray-500">
                <span>Entrada: {{ formatCurrency(r.precoEntrada) }}</span>
                <span>Alvo: {{ formatCurrency(r.precoAlvo) }}</span>
              </div>
              <p v-if="r.observacao" class="text-xs text-gray-400 mt-2 line-clamp-2">{{ r.observacao }}</p>
            </div>
          </template>
          <template #footer>
            <div v-if="canceladas.length === 0" class="text-center py-8 text-gray-400 text-sm">Nenhuma recomendação cancelada</div>
          </template>
        </draggable>
      </div>
    </div>

    <!-- Modal Copy Trading -->
    <Teleport to="body">
      <div v-if="copyModal.show" class="fixed inset-0 z-50 flex items-center justify-center bg-black/40" @click.self="copyModal.show = false">
        <div class="bg-white rounded-xl shadow-2xl w-full max-w-md p-6">
          <h3 class="text-lg font-bold mb-2">Copiar Recomendação</h3>
          <p class="text-sm text-gray-500 mb-4">
            {{ copyModal.rec?.moeda }}/{{ copyModal.rec?.parMoeda }} · {{ copyModal.rec?.tipo }}
          </p>
          <p class="text-xs text-gray-400 mb-3">Selecione as carteiras de destino:</p>
          <div class="space-y-2 max-h-60 overflow-y-auto">
            <label v-for="c in carteirasDisponiveis" :key="c.id" class="flex items-center gap-2 p-2 rounded hover:bg-gray-50 cursor-pointer">
              <input type="checkbox" v-model="copyModal.selectedIds" :value="c.id" class="rounded border-gray-300" />
              <span class="text-sm">{{ c.nome }}</span>
            </label>
          </div>
          <div v-if="carteirasDisponiveis.length === 0" class="text-center py-4 text-gray-400 text-sm">Nenhuma outra carteira disponível</div>
          <div class="flex justify-end gap-2 mt-5">
            <button type="button" @click="copyModal.show = false" class="px-4 py-2 bg-gray-100 rounded-lg text-sm">Cancelar</button>
            <button type="button" @click="executarCopy" :disabled="copyModal.selectedIds.length === 0"
              class="px-4 py-2 bg-indigo-600 text-white rounded-lg text-sm hover:bg-indigo-700 disabled:opacity-50">
              Copiar para {{ copyModal.selectedIds.length }} carteira(s)
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import draggable from 'vuedraggable'
import carteiraApi from '../../api/carteiraApi'
import recomendacaoApi from '../../api/recomendacaoApi'
import api from '../../api/axiosInstance'
import { useToast } from '../../composables/useToast'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'

const toast = useToast()
const loading = ref(true)
const carteiras = ref([])
const carteiraId = ref('')
const copyModal = ref({ show: false, rec: null, selectedIds: [] })

// Separate reactive arrays for each column (vuedraggable v-model)
const ativas = ref([])
const executadas = ref([])
const canceladas = ref([])

const carteirasDisponiveis = computed(() => {
  if (!copyModal.value.rec) return carteiras.value
  return carteiras.value.filter(c => c.id !== copyModal.value.rec.carteiraId)
})

onMounted(async () => {
  try {
    const res = await carteiraApi.listar()
    carteiras.value = res.data || []
    await carregar()
  } catch (e) { console.error(e) }
  finally { loading.value = false }
})

async function carregar() {
  loading.value = true
  try {
    let all = []
    if (carteiraId.value) {
      const res = await recomendacaoApi.listarPorCarteira(carteiraId.value)
      all = res.data || []
    } else {
      const promises = carteiras.value.map(c => recomendacaoApi.listarPorCarteira(c.id))
      const results = await Promise.all(promises)
      for (const res of results) {
        all.push(...(res.data || []))
      }
    }
    ativas.value = all.filter(r => r.status === 'ATIVA')
    executadas.value = all.filter(r => r.status === 'EXECUTADA')
    canceladas.value = all.filter(r => r.status === 'CANCELADA')
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

async function onDragChange(evt, targetStatus) {
  // When an item is added to this column, update its status on the backend
  if (evt.added) {
    const rec = evt.added.element
    const oldStatus = rec.status
    rec.status = targetStatus // optimistic update

    try {
      if (targetStatus === 'EXECUTADA') {
        await recomendacaoApi.executar(rec.id)
        toast.success(`${rec.moeda}/${rec.parMoeda} executada`)
      } else if (targetStatus === 'CANCELADA') {
        await recomendacaoApi.cancelar(rec.id)
        toast.success(`${rec.moeda}/${rec.parMoeda} cancelada`)
      } else if (targetStatus === 'ATIVA') {
        // Reativar — try backend, fallback to reload
        try {
          await recomendacaoApi.reativar(rec.id)
          toast.success(`${rec.moeda}/${rec.parMoeda} reativada`)
        } catch {
          toast.warning('Reativação não suportada — recarregando')
          await carregar()
        }
      }
    } catch (e) {
      toast.error('Erro ao mover card — recarregando')
      rec.status = oldStatus
      await carregar()
    }
  }
}

async function executar(r) {
  try {
    await recomendacaoApi.executar(r.id)
    r.status = 'EXECUTADA'
    ativas.value = ativas.value.filter(x => x.id !== r.id)
    executadas.value.unshift(r)
    toast.success('Recomendação executada')
  } catch (e) { toast.error('Erro ao executar') }
}

async function cancelar(r) {
  try {
    await recomendacaoApi.cancelar(r.id)
    r.status = 'CANCELADA'
    ativas.value = ativas.value.filter(x => x.id !== r.id)
    canceladas.value.unshift(r)
    toast.success('Recomendação cancelada')
  } catch (e) { toast.error('Erro ao cancelar') }
}

function abrirCopy(r) {
  copyModal.value = { show: true, rec: r, selectedIds: [] }
}

async function executarCopy() {
  try {
    await api.post(`/consultor/recomendacoes/${copyModal.value.rec.id}/copy`, {
      carteiraIds: copyModal.value.selectedIds
    })
    toast.success(`Recomendação copiada para ${copyModal.value.selectedIds.length} carteira(s)`)
    copyModal.value.show = false
    await carregar()
  } catch (e) { toast.error('Erro ao copiar') }
}

function formatCurrency(v) {
  if (!v && v !== 0) return '-'
  return 'R$ ' + Number(v).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}
</script>

<style scoped>
.kanban-col {
  display: flex;
  flex-direction: column;
  min-height: 400px;
}
.kanban-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  border-radius: 0.75rem 0.75rem 0 0;
  border: 1px solid;
  border-bottom: none;
  font-size: 0.875rem;
  font-weight: 700;
}
.kanban-dot {
  width: 0.5rem;
  height: 0.5rem;
  border-radius: 9999px;
}
.kanban-count {
  margin-left: auto;
  background: rgba(0,0,0,0.08);
  padding: 0.125rem 0.5rem;
  border-radius: 9999px;
  font-size: 0.75rem;
}
.kanban-body {
  flex: 1;
  padding: 0.75rem;
  background: rgb(var(--tl-surface));
  border: 1px solid rgb(var(--tl-border));
  border-top: none;
  border-radius: 0 0 0.75rem 0.75rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  overflow-y: auto;
  max-height: 600px;
  min-height: 120px;
}
.kanban-card {
  padding: 0.75rem;
  background: rgb(var(--tl-surface));
  border: 1px solid rgb(var(--tl-border));
  border-left: 3px solid;
  border-radius: 0.5rem;
  transition: box-shadow 0.2s, transform 0.15s;
  cursor: grab;
  position: relative;
}
.kanban-card:hover {
  box-shadow: var(--tl-shadow-md);
}
.kanban-card:active {
  cursor: grabbing;
}
.kanban-card__handle {
  position: absolute;
  top: 0.5rem;
  right: 0.5rem;
  font-size: 1rem;
  color: #cbd5e1;
  cursor: grab;
  line-height: 1;
  user-select: none;
  opacity: 0.5;
  transition: opacity 0.2s;
}
.kanban-card:hover .kanban-card__handle {
  opacity: 1;
  color: #94a3b8;
}
/* Ghost = placeholder in the drop zone */
.kanban-ghost {
  opacity: 0.4;
  background: #e0e7ff !important;
  border: 2px dashed #6366f1 !important;
  border-radius: 0.5rem;
}
/* Drag = the card being dragged */
.kanban-drag {
  transform: rotate(2deg);
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.15);
}
</style>

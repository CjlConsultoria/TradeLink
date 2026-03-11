<template>
  <div>
    <h2 class="page-title">Copy Trading</h2>
    <p class="text-sm text-gray-500 mb-6">Replique recomendações de uma carteira para outras, com um clique.</p>

    <div v-if="loading" class="text-center py-12 text-gray-400">Carregando...</div>

    <template v-else>
      <!-- Step 1: Selecionar carteira origem -->
      <div class="card p-6 mb-6">
        <h3 class="section-title">1. Selecione a carteira origem</h3>
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3 mt-3">
          <button v-for="c in carteiras" :key="c.id" type="button"
            @click="selecionarOrigem(c)"
            class="p-4 border rounded-lg text-left transition-colors"
            :class="carteiraOrigem?.id === c.id ? 'border-indigo-500 bg-indigo-50' : 'border-gray-200 hover:border-indigo-300 hover:bg-indigo-50/50'">
            <h4 class="font-semibold text-gray-900 text-sm">{{ c.nome }}</h4>
            <p class="text-xs text-gray-500 mt-1">{{ c.totalRecomendacoes || 0 }} recomendações · {{ c.totalClientes || 0 }} clientes</p>
          </button>
        </div>
      </div>

      <!-- Step 2: Selecionar recomendação -->
      <div v-if="carteiraOrigem" class="card p-6 mb-6">
        <h3 class="section-title">2. Selecione a recomendação para copiar</h3>
        <div v-if="recomendacoes.length === 0" class="text-center py-6 text-gray-400 text-sm">
          Nenhuma recomendação ativa nesta carteira.
        </div>
        <div v-else class="space-y-2 mt-3">
          <button v-for="r in recomendacoes" :key="r.id" type="button"
            @click="recSelecionada = r"
            class="w-full flex items-center justify-between p-3 border rounded-lg text-left transition-colors"
            :class="recSelecionada?.id === r.id ? 'border-indigo-500 bg-indigo-50' : 'border-gray-200 hover:border-indigo-300'">
            <div class="flex items-center gap-3">
              <span class="px-2 py-0.5 rounded text-xs font-semibold"
                :class="r.tipo === 'COMPRA' ? 'bg-emerald-100 text-emerald-700' : 'bg-red-100 text-red-700'">
                {{ r.tipo }}
              </span>
              <span class="font-semibold text-sm">{{ r.moeda }}/{{ r.parMoeda }}</span>
            </div>
            <div class="text-right text-xs text-gray-500">
              <span>Entrada: {{ formatCurrency(r.precoEntrada) }}</span>
              <span class="ml-2">Alvo: {{ formatCurrency(r.precoAlvo) }}</span>
            </div>
          </button>
        </div>
      </div>

      <!-- Step 3: Selecionar carteiras destino -->
      <div v-if="recSelecionada" class="card p-6 mb-6">
        <h3 class="section-title">3. Selecione as carteiras destino</h3>
        <div class="flex items-center gap-3 mb-3">
          <button type="button" @click="selecionarTodas"
            class="text-xs text-indigo-600 hover:underline">Selecionar todas</button>
          <button type="button" @click="carteirasDestino = []"
            class="text-xs text-gray-500 hover:underline">Limpar</button>
        </div>
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-2">
          <label v-for="c in carteirasParaCopy" :key="c.id"
            class="flex items-center gap-2 p-3 border rounded-lg cursor-pointer transition-colors"
            :class="carteirasDestino.includes(c.id) ? 'border-indigo-400 bg-indigo-50' : 'border-gray-200 hover:border-indigo-200'">
            <input type="checkbox" v-model="carteirasDestino" :value="c.id" class="rounded border-gray-300 text-indigo-600" />
            <div>
              <span class="text-sm font-medium text-gray-900">{{ c.nome }}</span>
              <p class="text-xs text-gray-500">{{ c.totalClientes || 0 }} clientes</p>
            </div>
          </label>
        </div>
      </div>

      <!-- Step 4: Confirmar -->
      <div v-if="recSelecionada && carteirasDestino.length > 0" class="card p-6">
        <h3 class="section-title">4. Confirmar Copy Trading</h3>
        <div class="bg-indigo-50 rounded-lg p-4 mb-4">
          <div class="grid grid-cols-2 gap-2 text-sm">
            <div>
              <span class="text-gray-500">Origem:</span>
              <span class="font-medium ml-1">{{ carteiraOrigem.nome }}</span>
            </div>
            <div>
              <span class="text-gray-500">Recomendação:</span>
              <span class="font-medium ml-1">{{ recSelecionada.tipo }} {{ recSelecionada.moeda }}/{{ recSelecionada.parMoeda }}</span>
            </div>
            <div>
              <span class="text-gray-500">Destinos:</span>
              <span class="font-medium ml-1">{{ carteirasDestino.length }} carteira(s)</span>
            </div>
            <div>
              <span class="text-gray-500">Preço entrada:</span>
              <span class="font-medium ml-1">{{ formatCurrency(recSelecionada.precoEntrada) }}</span>
            </div>
          </div>
        </div>
        <div class="flex justify-end">
          <button type="button" @click="confirmarCopy" :disabled="copiando"
            class="px-6 py-2.5 bg-indigo-600 text-white rounded-lg text-sm font-medium hover:bg-indigo-700 disabled:opacity-50">
            {{ copiando ? 'Copiando...' : '📋 Replicar Recomendação' }}
          </button>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import carteiraApi from '../../api/carteiraApi'
import recomendacaoApi from '../../api/recomendacaoApi'
import api from '../../api/axiosInstance'
import { useToast } from '../../composables/useToast'

const toast = useToast()
const loading = ref(true)
const carteiras = ref([])
const carteiraOrigem = ref(null)
const recomendacoes = ref([])
const recSelecionada = ref(null)
const carteirasDestino = ref([])
const copiando = ref(false)

const carteirasParaCopy = computed(() =>
  carteiras.value.filter(c => c.id !== carteiraOrigem.value?.id)
)

onMounted(async () => {
  try {
    const res = await carteiraApi.listar()
    carteiras.value = res.data || []
  } catch (e) { console.error(e) }
  finally { loading.value = false }
})

async function selecionarOrigem(c) {
  carteiraOrigem.value = c
  recSelecionada.value = null
  carteirasDestino.value = []
  try {
    const res = await recomendacaoApi.listarPorCarteira(c.id)
    recomendacoes.value = (res.data || []).filter(r => r.status === 'ATIVA')
  } catch (e) { console.error(e); toast.error('Erro ao carregar recomendações') }
}

function selecionarTodas() {
  carteirasDestino.value = carteirasParaCopy.value.map(c => c.id)
}

async function confirmarCopy() {
  copiando.value = true
  try {
    await api.post(`/consultor/recomendacoes/${recSelecionada.value.id}/copy`, {
      carteiraIds: carteirasDestino.value
    })
    toast.success(`Recomendação replicada para ${carteirasDestino.value.length} carteira(s)!`)
    recSelecionada.value = null
    carteirasDestino.value = []
    // Recarregar recomendações da origem
    await selecionarOrigem(carteiraOrigem.value)
  } catch (e) {
    toast.error(e.response?.data?.message || 'Erro ao copiar recomendação')
  } finally { copiando.value = false }
}

function formatCurrency(v) {
  if (!v && v !== 0) return '-'
  return 'R$ ' + Number(v).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}
</script>

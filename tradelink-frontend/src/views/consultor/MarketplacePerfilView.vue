<template>
  <div class="space-y-6">
    <div>
      <h1 class="text-2xl font-bold text-white">Marketplace</h1>
      <p class="text-slate-400 text-sm mt-1">Gerencie seu perfil no marketplace de consultores</p>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="flex justify-center py-12">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-400"></div>
    </div>

    <template v-else>
      <!-- Toggle + Status -->
      <div class="bg-slate-800/50 rounded-xl border border-slate-700/50 p-5">
        <div class="flex items-center justify-between">
          <div>
            <h2 class="text-white font-semibold">Visibilidade no Marketplace</h2>
            <p class="text-slate-400 text-xs mt-1">Quando ativo, clientes podem encontrar voce e solicitar mentoria</p>
          </div>
          <button
            @click="toggleVisibilidade"
            :class="perfil.marketplaceVisivel ? 'bg-green-600' : 'bg-slate-600'"
            class="relative inline-flex h-6 w-11 items-center rounded-full transition-colors"
          >
            <span
              :class="perfil.marketplaceVisivel ? 'translate-x-6' : 'translate-x-1'"
              class="inline-block h-4 w-4 transform rounded-full bg-white transition-transform"
            ></span>
          </button>
        </div>
      </div>

      <!-- Perfil Form -->
      <div class="bg-slate-800/50 rounded-xl border border-slate-700/50 p-5">
        <h2 class="text-white font-semibold mb-4">Seu Perfil</h2>
        <div class="space-y-4">
          <div>
            <label class="text-slate-300 text-xs font-medium mb-1 block">Descricao</label>
            <textarea
              v-model="perfil.marketplaceDescricao"
              rows="3"
              placeholder="Descreva seus servicos e diferenciais..."
              class="w-full bg-slate-700/50 text-white rounded-lg p-3 text-sm border border-slate-600/50 focus:border-indigo-500 focus:outline-none"
            ></textarea>
          </div>
          <div class="grid sm:grid-cols-2 gap-4">
            <div>
              <label class="text-slate-300 text-xs font-medium mb-1 block">Especializacao</label>
              <input
                v-model="perfil.marketplaceEspecializacao"
                type="text"
                placeholder="Ex: Criptomoedas, Renda Variavel..."
                class="w-full bg-slate-700/50 text-white rounded-lg p-3 text-sm border border-slate-600/50 focus:border-indigo-500 focus:outline-none"
              />
            </div>
            <div>
              <label class="text-slate-300 text-xs font-medium mb-1 block">Experiencia</label>
              <input
                v-model="perfil.marketplaceExperiencia"
                type="text"
                placeholder="Ex: 5 anos no mercado..."
                class="w-full bg-slate-700/50 text-white rounded-lg p-3 text-sm border border-slate-600/50 focus:border-indigo-500 focus:outline-none"
              />
            </div>
          </div>
          <div class="grid sm:grid-cols-2 gap-4">
            <div>
              <label class="text-slate-300 text-xs font-medium mb-1 block">URL da Foto</label>
              <input
                v-model="perfil.marketplaceFotoUrl"
                type="text"
                placeholder="https://..."
                class="w-full bg-slate-700/50 text-white rounded-lg p-3 text-sm border border-slate-600/50 focus:border-indigo-500 focus:outline-none"
              />
            </div>
            <div>
              <label class="text-slate-300 text-xs font-medium mb-1 block">Preco Base (R$/mes por cliente)</label>
              <input
                v-model.number="perfil.marketplacePrecoBase"
                type="number"
                step="0.01"
                min="0"
                placeholder="99.90"
                class="w-full bg-slate-700/50 text-white rounded-lg p-3 text-sm border border-slate-600/50 focus:border-indigo-500 focus:outline-none"
              />
            </div>
          </div>

          <!-- Info Taxa -->
          <div v-if="perfil.marketplacePrecoBase && perfil.taxaPlataforma" class="bg-indigo-500/10 rounded-lg p-4 border border-indigo-500/20">
            <p class="text-indigo-300 text-sm">
              <strong>Preco para o cliente:</strong> R$ {{ formatPreco(perfil.marketplacePrecoBase) }}
              <br />
              <strong>Taxa da plataforma ({{ perfil.taxaPlataforma }}%):</strong> R$ {{ formatPreco(calcTaxa) }}
              <br />
              <strong>Voce recebe:</strong> R$ {{ formatPreco(calcRecebe) }}
            </p>
          </div>

          <button
            @click="salvar"
            :disabled="saving"
            class="bg-indigo-600 text-white px-6 py-2.5 rounded-lg text-sm font-medium hover:bg-indigo-700 disabled:opacity-50 transition-colors"
          >
            {{ saving ? 'Salvando...' : 'Salvar Perfil' }}
          </button>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getPerfilMarketplace, atualizarPerfilMarketplace } from '../../api/marketplaceApi'
import { useToast } from '../../composables/useToast'

const toast = useToast()
const loading = ref(true)
const saving = ref(false)
const perfil = ref({})

onMounted(async () => {
  try {
    const res = await getPerfilMarketplace()
    perfil.value = res.data
  } catch (e) {
    toast.error('Erro ao carregar perfil')
  } finally {
    loading.value = false
  }
})

const calcTaxa = computed(() => {
  if (!perfil.value.marketplacePrecoBase || !perfil.value.taxaPlataforma) return 0
  return (perfil.value.marketplacePrecoBase * perfil.value.taxaPlataforma / 100)
})

const calcRecebe = computed(() => {
  if (!perfil.value.marketplacePrecoBase) return 0
  return perfil.value.marketplacePrecoBase - calcTaxa.value
})

async function toggleVisibilidade() {
  perfil.value.marketplaceVisivel = !perfil.value.marketplaceVisivel
  await salvar()
}

async function salvar() {
  saving.value = true
  try {
    const res = await atualizarPerfilMarketplace({
      marketplaceVisivel: perfil.value.marketplaceVisivel,
      marketplaceDescricao: perfil.value.marketplaceDescricao,
      marketplaceEspecializacao: perfil.value.marketplaceEspecializacao,
      marketplaceExperiencia: perfil.value.marketplaceExperiencia,
      marketplaceFotoUrl: perfil.value.marketplaceFotoUrl,
      marketplacePrecoBase: perfil.value.marketplacePrecoBase
    })
    perfil.value = res.data
    toast.success('Perfil atualizado!')
  } catch (e) {
    toast.error(e.response?.data?.message || 'Erro ao salvar')
  } finally {
    saving.value = false
  }
}

function formatPreco(v) {
  if (!v && v !== 0) return '0,00'
  return Number(v).toFixed(2).replace('.', ',')
}
</script>

<template>
  <div>
    <h2 class="page-title">Watchlist · Favoritos</h2>
    <p class="text-sm text-gray-500 mb-6">Acompanhe suas moedas favoritas em tempo real.</p>

    <!-- Adicionar moeda -->
    <div class="card p-4 mb-6">
      <div class="flex flex-wrap gap-3 items-end">
        <div class="flex-1 min-w-[200px]">
          <label class="text-xs font-medium text-gray-500 mb-1 block">Adicionar moeda</label>
          <select v-model="novaMoeda" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm">
            <option value="">Selecione...</option>
            <option v-for="c in moedasDisponiveis" :key="c.key" :value="c.key"
              :disabled="favoritosSet.has(c.key)">
              {{ c.moeda }}/{{ c.parMoeda }} {{ favoritosSet.has(c.key) ? '★' : '' }}
            </option>
          </select>
        </div>
        <button type="button" @click="adicionar" :disabled="!novaMoeda"
          class="px-4 py-2 bg-indigo-600 text-white rounded-lg text-sm font-medium hover:bg-indigo-700 disabled:opacity-50">
          Adicionar ★
        </button>
      </div>
    </div>

    <!-- Lista de favoritos -->
    <div v-if="loading" class="text-center py-12 text-gray-400">Carregando favoritos...</div>

    <div v-else-if="favoritosComCotacao.length === 0" class="card p-12 text-center">
      <p class="text-gray-400 text-lg mb-2">Nenhuma moeda favorita ainda</p>
      <p class="text-gray-400 text-sm">Adicione moedas acima para acompanhá-las aqui.</p>
    </div>

    <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
      <div v-for="f in favoritosComCotacao" :key="f.key"
        class="card p-5 relative group cursor-pointer hover:border-indigo-300"
        @click="irParaDetalhe(f)">
        <button type="button" @click.stop="remover(f)"
          class="absolute top-3 right-3 w-7 h-7 rounded-full bg-red-50 text-red-400 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity hover:bg-red-100 hover:text-red-600">
          ✕
        </button>
        <div class="flex items-center gap-2 mb-2">
          <span class="text-lg font-bold text-gray-900">{{ f.moeda }}</span>
          <span class="text-sm text-gray-400">/{{ f.parMoeda }}</span>
        </div>
        <div class="text-2xl font-bold" :class="(f.variacao || 0) >= 0 ? 'text-emerald-600' : 'text-red-600'">
          {{ formatPreco(f.preco) }}
        </div>
        <div class="flex items-center gap-1 mt-1">
          <span class="text-sm font-semibold" :class="(f.variacao || 0) >= 0 ? 'text-emerald-600' : 'text-red-600'">
            {{ (f.variacao || 0) >= 0 ? '+' : '' }}{{ (f.variacao || 0).toFixed(2) }}%
          </span>
          <span class="text-xs text-gray-400">{{ f.fonte || '' }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import { useToast } from '../../composables/useToast'
import favoritoApi from '../../api/favoritoApi'
import cotacaoApi from '../../api/cotacaoApi'

const router = useRouter()
const authStore = useAuthStore()
const toast = useToast()
const loading = ref(true)
const favoritos = ref([])
const todasCotacoes = ref([])
const novaMoeda = ref('')

const moedasDisponiveis = computed(() =>
  todasCotacoes.value.map(c => ({ key: `${c.moeda}/${c.parMoeda}`, moeda: c.moeda, parMoeda: c.parMoeda }))
)
const favoritosSet = computed(() => new Set(favoritos.value.map(f => `${f.moeda}/${f.parMoeda}`)))

const favoritosComCotacao = computed(() =>
  favoritos.value.map(f => {
    const cot = todasCotacoes.value.find(c => c.moeda === f.moeda && c.parMoeda === f.parMoeda)
    return {
      ...f,
      key: `${f.moeda}/${f.parMoeda}`,
      preco: cot?.precoCompra || cot?.precoVenda || 0,
      variacao: cot?.variacao || 0,
      fonte: cot?.fonte || ''
    }
  })
)

onMounted(async () => {
  try {
    const [favRes, cotRes] = await Promise.all([favoritoApi.listar(), cotacaoApi.listarUltimas()])
    favoritos.value = favRes.data || []
    todasCotacoes.value = cotRes.data || []
  } catch (e) { console.error(e) }
  finally { loading.value = false }
})

async function adicionar() {
  if (!novaMoeda.value) return
  const [moeda, parMoeda] = novaMoeda.value.split('/')
  try {
    await favoritoApi.adicionar(moeda, parMoeda)
    favoritos.value.unshift({ moeda, parMoeda })
    novaMoeda.value = ''
    toast.success('Moeda adicionada aos favoritos')
  } catch (e) {
    toast.error(e.response?.data?.message || 'Erro ao adicionar favorito')
  }
}

async function remover(f) {
  try {
    await favoritoApi.remover(f.moeda, f.parMoeda)
    favoritos.value = favoritos.value.filter(x => !(x.moeda === f.moeda && x.parMoeda === f.parMoeda))
    toast.info('Favorito removido')
  } catch (e) { toast.error('Erro ao remover') }
}

function irParaDetalhe(f) {
  const prefix = authStore.user?.role === 'Admin' ? '/consultor' : '/cliente'
  router.push(`${prefix}/cotacoes/${f.moeda}/${f.parMoeda}`)
}

function formatPreco(p) {
  if (p >= 1000) return 'R$ ' + Number(p).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
  if (p >= 1) return Number(p).toLocaleString('pt-BR', { minimumFractionDigits: 4, maximumFractionDigits: 4 })
  return Number(p).toLocaleString('pt-BR', { minimumFractionDigits: 6, maximumFractionDigits: 8 })
}
</script>

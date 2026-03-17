<template>
  <div>
    <h2 class="page-title">Marketplace</h2>

    <!-- Loading -->
    <div v-if="loading" class="flex justify-center py-16">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-500"></div>
    </div>

    <template v-else>
      <!-- Visibilidade -->
      <div class="card p-5 sm:p-6 mb-6">
        <div class="flex items-start sm:items-center justify-between gap-4">
          <div class="flex items-start sm:items-center gap-3 sm:gap-4">
            <div class="w-10 h-10 sm:w-11 sm:h-11 rounded-xl flex items-center justify-center shrink-0"
              :class="perfil.marketplaceVisivel ? 'bg-green-100' : 'bg-gray-100'">
              <svg v-if="perfil.marketplaceVisivel" class="w-5 h-5 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/>
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/>
              </svg>
              <svg v-else class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.878 9.878L3 3m6.878 6.878L21 21"/>
              </svg>
            </div>
            <div>
              <h3 class="font-semibold" style="color: rgb(var(--tl-text));">Visibilidade no Marketplace</h3>
              <p class="text-sm mt-0.5" style="color: rgb(var(--tl-text-muted));">
                {{ perfil.marketplaceVisivel
                  ? 'Seu perfil esta ativo. Clientes podem encontra-lo e solicitar mentoria.'
                  : 'Seu perfil esta oculto. Ative para aparecer nas buscas de clientes.' }}
              </p>
            </div>
          </div>
          <button
            @click="toggleVisibilidade"
            :class="perfil.marketplaceVisivel ? 'bg-green-500' : 'bg-gray-300'"
            class="relative inline-flex h-7 w-12 items-center rounded-full transition-colors shrink-0"
          >
            <span
              :class="perfil.marketplaceVisivel ? 'translate-x-6' : 'translate-x-1'"
              class="inline-block h-5 w-5 transform rounded-full bg-white shadow transition-transform"
            ></span>
          </button>
        </div>
      </div>

      <!-- Stats -->
      <div class="grid grid-cols-2 md:grid-cols-4 gap-3 sm:gap-4 mb-6">
        <div class="card p-4 text-center">
          <p class="text-xs font-medium mb-1" style="color: rgb(var(--tl-text-muted));">Preco Base</p>
          <p class="text-lg sm:text-xl font-bold" style="color: rgb(var(--tl-text));">R$ {{ formatPreco(perfil.marketplacePrecoBase) }}</p>
        </div>
        <div class="card p-4 text-center">
          <p class="text-xs font-medium mb-1" style="color: rgb(var(--tl-text-muted));">Voce Recebe</p>
          <p class="text-lg sm:text-xl font-bold text-green-600">R$ {{ formatPreco(calcRecebe) }}</p>
        </div>
        <div class="card p-4 text-center">
          <p class="text-xs font-medium mb-1" style="color: rgb(var(--tl-text-muted));">Taxa Plataforma</p>
          <p class="text-lg sm:text-xl font-bold text-indigo-600">{{ perfil.taxaPlataforma || 15 }}%</p>
        </div>
        <div class="card p-4 text-center">
          <p class="text-xs font-medium mb-1" style="color: rgb(var(--tl-text-muted));">Desconto Taxa</p>
          <p class="text-lg sm:text-xl font-bold text-red-500">R$ {{ formatPreco(calcTaxa) }}</p>
        </div>
      </div>

      <!-- Formulario -->
      <div class="card p-5 sm:p-6 mb-6">
        <h3 class="section-title">Seu Perfil</h3>
        <p class="text-sm -mt-3 mb-5" style="color: rgb(var(--tl-text-muted));">Informacoes exibidas para clientes no marketplace</p>

        <div class="space-y-5">
          <!-- Descricao -->
          <div>
            <label class="block text-sm font-semibold mb-1.5" style="color: rgb(var(--tl-text));">Descricao</label>
            <textarea
              v-model="perfil.marketplaceDescricao"
              rows="4"
              maxlength="500"
              placeholder="Descreva seus servicos e diferenciais. Ex: Especialista em criptomoedas com foco em gestao de risco..."
              class="input-base resize-none"
            ></textarea>
            <p class="text-xs mt-1" style="color: rgb(var(--tl-text-muted));">{{ (perfil.marketplaceDescricao || '').length }}/500 caracteres</p>
          </div>

          <!-- Especializacao + Experiencia -->
          <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-semibold mb-1.5" style="color: rgb(var(--tl-text));">Especializacao</label>
              <input
                v-model="perfil.marketplaceEspecializacao"
                type="text"
                placeholder="Ex: Criptomoedas, Renda Variavel, DeFi"
                class="input-base"
              />
            </div>
            <div>
              <label class="block text-sm font-semibold mb-1.5" style="color: rgb(var(--tl-text));">Experiencia</label>
              <input
                v-model="perfil.marketplaceExperiencia"
                type="text"
                placeholder="Ex: 5 anos no mercado cripto"
                class="input-base"
              />
            </div>
          </div>

          <!-- Rede Social -->
          <div>
            <label class="block text-sm font-semibold mb-1.5" style="color: rgb(var(--tl-text));">Rede Social</label>
            <input
              v-model="perfil.marketplaceRedeSocial"
              type="url"
              placeholder="https://instagram.com/seu_perfil"
              class="input-base"
            />
            <p class="text-xs mt-1" style="color: rgb(var(--tl-text-muted));">Instagram, LinkedIn, Twitter ou site pessoal</p>
          </div>

          <!-- Preco -->
          <div>
            <label class="block text-sm font-semibold mb-1.5" style="color: rgb(var(--tl-text));">Preco Mensal por Cliente (R$)</label>
            <div class="max-w-xs">
              <input
                v-model.number="perfil.marketplacePrecoBase"
                type="number"
                step="0.01"
                min="0"
                placeholder="99.90"
                class="input-base"
              />
            </div>
          </div>

          <!-- Simulacao de receita -->
          <div v-if="perfil.marketplacePrecoBase > 0" class="rounded-xl p-4 bg-indigo-50 border border-indigo-200">
            <p class="text-xs font-bold text-indigo-700 uppercase tracking-wider mb-3">Simulacao de Receita por Cliente</p>
            <div class="grid grid-cols-3 gap-2 sm:gap-3">
              <div class="text-center p-2 sm:p-3 bg-white rounded-lg">
                <p class="text-sm font-bold" style="color: rgb(var(--tl-text));">R$ {{ formatPreco(perfil.marketplacePrecoBase) }}</p>
                <p class="text-xs mt-0.5" style="color: rgb(var(--tl-text-muted));">Cliente paga</p>
              </div>
              <div class="text-center p-2 sm:p-3 bg-white rounded-lg">
                <p class="text-sm font-bold text-red-600">- R$ {{ formatPreco(calcTaxa) }}</p>
                <p class="text-xs mt-0.5" style="color: rgb(var(--tl-text-muted));">Taxa {{ perfil.taxaPlataforma || 15 }}%</p>
              </div>
              <div class="text-center p-2 sm:p-3 bg-white rounded-lg">
                <p class="text-sm font-bold text-green-600">R$ {{ formatPreco(calcRecebe) }}</p>
                <p class="text-xs mt-0.5" style="color: rgb(var(--tl-text-muted));">Voce recebe</p>
              </div>
            </div>
          </div>

          <!-- Botao salvar -->
          <div class="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 pt-4 border-t" style="border-color: rgb(var(--tl-border));">
            <p class="text-xs" style="color: rgb(var(--tl-text-muted));">Alteracoes visiveis imediatamente apos salvar</p>
            <button
              @click="salvar"
              :disabled="saving"
              class="btn-primary flex items-center gap-2 w-full sm:w-auto justify-center"
            >
              <svg v-if="!saving" class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/>
              </svg>
              <span v-else class="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin"></span>
              {{ saving ? 'Salvando...' : 'Salvar Perfil' }}
            </button>
          </div>
        </div>
      </div>

      <!-- Pre-visualizacao -->
      <div class="card p-5 sm:p-6">
        <h3 class="section-title">Pre-visualizacao</h3>
        <p class="text-sm -mt-3 mb-5" style="color: rgb(var(--tl-text-muted));">Assim os clientes veem seu perfil no marketplace</p>

        <div class="max-w-sm mx-auto">
          <div class="card p-5">
            <div class="flex items-center gap-3 mb-3">
              <div class="w-12 h-12 bg-indigo-100 rounded-xl flex items-center justify-center shrink-0">
                <span class="text-lg font-bold text-indigo-600">{{ getInitials() }}</span>
              </div>
              <div class="min-w-0">
                <h4 class="font-semibold text-sm truncate" style="color: rgb(var(--tl-text));">{{ perfil.nome || 'Sua Consultoria' }}</h4>
                <p v-if="perfil.marketplaceEspecializacao" class="text-indigo-600 text-xs truncate">{{ perfil.marketplaceEspecializacao }}</p>
                <p v-else class="text-xs italic" style="color: rgb(var(--tl-text-muted));">Sem especializacao</p>
              </div>
            </div>

            <p v-if="perfil.marketplaceDescricao" class="text-xs mb-3 line-clamp-3" style="color: rgb(var(--tl-text-muted));">{{ perfil.marketplaceDescricao }}</p>
            <p v-else class="text-xs mb-3 italic" style="color: rgb(var(--tl-text-muted));">Sem descricao...</p>

            <div class="space-y-1 mb-3">
              <div v-if="perfil.marketplaceExperiencia" class="flex items-center gap-1.5 text-xs" style="color: rgb(var(--tl-text-muted));">
                <svg class="w-3.5 h-3.5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 13.255A23.931 23.931 0 0112 15c-3.183 0-6.22-.62-9-1.745M16 6V4a2 2 0 00-2-2h-4a2 2 0 00-2 2v2m4 6h.01M5 20h14a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"/>
                </svg>
                {{ perfil.marketplaceExperiencia }}
              </div>
              <div v-if="perfil.marketplaceRedeSocial" class="flex items-center gap-1.5 text-indigo-600 text-xs">
                <svg class="w-3.5 h-3.5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1"/>
                </svg>
                <span class="truncate">{{ formatRedeSocial(perfil.marketplaceRedeSocial) }}</span>
              </div>
            </div>

            <div class="pt-3 flex items-center justify-between" style="border-top: 1px solid rgb(var(--tl-border));">
              <div>
                <span class="text-lg font-bold" style="color: rgb(var(--tl-text));">R$ {{ formatPreco(perfil.marketplacePrecoBase) }}</span>
                <span class="text-xs" style="color: rgb(var(--tl-text-muted));">/mes</span>
              </div>
              <span class="px-4 py-1.5 rounded-lg text-xs font-medium bg-indigo-100 text-indigo-600 border border-indigo-200">
                Solicitar
              </span>
            </div>
          </div>
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
    toast.error('Erro ao carregar perfil do marketplace')
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

function getInitials() {
  const nome = perfil.value.nome || ''
  return nome.split(' ').map(w => w[0]).filter(Boolean).slice(0, 2).join('').toUpperCase() || '?'
}

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
      marketplaceRedeSocial: perfil.value.marketplaceRedeSocial,
      marketplacePrecoBase: perfil.value.marketplacePrecoBase
    })
    perfil.value = res.data
    toast.success('Perfil atualizado com sucesso!')
  } catch (e) {
    toast.error(e.response?.data?.message || 'Erro ao salvar perfil')
  } finally {
    saving.value = false
  }
}

function formatPreco(v) {
  if (!v && v !== 0) return '0,00'
  return Number(v).toFixed(2).replace('.', ',')
}

function formatRedeSocial(url) {
  if (!url) return ''
  try {
    const u = new URL(url)
    return u.hostname.replace('www.', '') + u.pathname.replace(/\/$/, '')
  } catch {
    return url
  }
}
</script>

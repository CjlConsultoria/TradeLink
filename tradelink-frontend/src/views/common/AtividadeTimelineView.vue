<template>
  <div>
    <h2 class="page-title">Atividades Recentes</h2>
    <p class="text-sm text-gray-500 mb-6">Timeline com todas as suas ações na plataforma.</p>

    <LoadingSpinner v-if="loading" text="Carregando atividades..." />

    <div v-else-if="atividades.length === 0" class="card p-12 text-center">
      <p class="text-gray-400 text-lg">Nenhuma atividade registrada ainda.</p>
    </div>

    <div v-else class="relative">
      <!-- Linha vertical -->
      <div class="absolute left-6 top-0 bottom-0 w-0.5 bg-gray-200"></div>

      <div v-for="(a, idx) in atividades" :key="a.id || idx" class="relative flex gap-4 mb-6">
        <!-- Ícone -->
        <div class="relative z-10 flex-shrink-0 w-12 h-12 rounded-full flex items-center justify-center text-white text-lg"
          :class="iconClass(a.tipo)">
          {{ iconEmoji(a.tipo) }}
        </div>

        <!-- Conteúdo -->
        <div class="card p-4 flex-1">
          <div class="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-1 sm:gap-2">
            <div>
              <span class="inline-block px-2 py-0.5 rounded text-xs font-semibold mb-1"
                :class="badgeClass(a.tipo)">
                {{ tipoLabel(a.tipo) }}
              </span>
              <p class="text-sm text-gray-800">{{ a.descricao }}</p>
            </div>
            <span class="text-xs text-gray-400 flex-shrink-0">{{ formatDate(a.createdAt) }}</span>
          </div>
          <router-link v-if="a.link" :to="a.link" class="text-xs text-indigo-600 hover:underline mt-1 inline-block">
            Ver detalhes →
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import atividadeApi from '../../api/atividadeApi'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'

const atividades = ref([])
const loading = ref(true)

const tipoMap = {
  LOGIN: { label: 'Login', emoji: '🔑', icon: 'bg-blue-500', badge: 'bg-blue-100 text-blue-700' },
  OPERACAO: { label: 'Operação', emoji: '💹', icon: 'bg-emerald-500', badge: 'bg-emerald-100 text-emerald-700' },
  ALERTA_DISPARADO: { label: 'Alerta', emoji: '🔔', icon: 'bg-amber-500', badge: 'bg-amber-100 text-amber-700' },
  RECOMENDACAO_RECEBIDA: { label: 'Recomendação', emoji: '📩', icon: 'bg-indigo-500', badge: 'bg-indigo-100 text-indigo-700' },
  RECOMENDACAO_RESOLVIDA: { label: 'Resolvida', emoji: '✅', icon: 'bg-green-500', badge: 'bg-green-100 text-green-700' },
  FATURA_PAGA: { label: 'Pagamento', emoji: '💳', icon: 'bg-purple-500', badge: 'bg-purple-100 text-purple-700' },
  PERFIL_ATUALIZADO: { label: 'Perfil', emoji: '👤', icon: 'bg-gray-500', badge: 'bg-gray-100 text-gray-700' }
}

function tipoLabel(t) { return tipoMap[t]?.label || t }
function iconEmoji(t) { return tipoMap[t]?.emoji || '📌' }
function iconClass(t) { return tipoMap[t]?.icon || 'bg-gray-400' }
function badgeClass(t) { return tipoMap[t]?.badge || 'bg-gray-100 text-gray-600' }

function formatDate(dt) {
  if (!dt) return ''
  const d = new Date(dt)
  const now = new Date()
  const diffMs = now - d
  const diffMin = Math.floor(diffMs / 60000)
  if (diffMin < 1) return 'agora'
  if (diffMin < 60) return `há ${diffMin}min`
  const diffH = Math.floor(diffMin / 60)
  if (diffH < 24) return `há ${diffH}h`
  const diffD = Math.floor(diffH / 24)
  if (diffD < 7) return `há ${diffD}d`
  return d.toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit', year: '2-digit' })
}

onMounted(async () => {
  try {
    const res = await atividadeApi.listar(50)
    atividades.value = res.data || []
  } catch (e) { console.error(e) }
  finally { loading.value = false }
})
</script>

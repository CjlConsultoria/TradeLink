<template>
  <div>
    <h2 class="page-title">Gerenciar FAQ</h2>

    <!-- Formulario -->
    <div class="card p-6 mb-6">
      <h3 class="section-title">{{ editingId ? 'Editar FAQ' : 'Nova FAQ' }}</h3>
      <form @submit.prevent="salvar" class="space-y-4">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div class="md:col-span-2">
            <label class="label">Pergunta *</label>
            <input v-model="form.pergunta" type="text" required class="input" placeholder="Ex: Como funciona o trial?" />
          </div>
          <div class="md:col-span-2">
            <label class="label">Resposta *</label>
            <textarea v-model="form.resposta" required rows="4" class="input" placeholder="Resposta detalhada..."></textarea>
          </div>
          <div>
            <label class="label">Categoria</label>
            <input v-model="form.categoria" type="text" class="input" placeholder="Geral, Pagamento, etc." />
          </div>
          <div>
            <label class="label">Ordem</label>
            <input v-model.number="form.ordem" type="number" min="0" class="input" />
          </div>
        </div>
        <div class="flex gap-3">
          <button type="submit" :disabled="saving" class="btn btn-primary">
            {{ saving ? 'Salvando...' : (editingId ? 'Atualizar' : 'Criar') }}
          </button>
          <button v-if="editingId" type="button" @click="cancelEdit" class="btn btn-secondary">Cancelar</button>
        </div>
      </form>
    </div>

    <!-- Lista -->
    <div class="card p-6">
      <h3 class="section-title">FAQs cadastradas</h3>
      <LoadingSpinner v-if="loading" size="sm" />
      <div v-else-if="faqs.length === 0" class="text-center py-6 text-gray-500">Nenhuma FAQ cadastrada.</div>
      <div v-else class="space-y-3">
        <div v-for="faq in faqs" :key="faq.id"
          class="p-4 rounded-lg border transition-colors"
          :class="faq.ativo ? 'border-gray-200 bg-white' : 'border-gray-100 bg-gray-50 opacity-60'">
          <div class="flex items-start justify-between gap-3">
            <div class="flex-1">
              <div class="flex items-center gap-2 mb-1">
                <span class="text-xs px-2 py-0.5 rounded-full bg-indigo-100 text-indigo-700">{{ faq.categoria || 'Geral' }}</span>
                <span class="text-xs text-gray-400">#{{ faq.ordem }}</span>
                <span v-if="!faq.ativo" class="text-xs px-2 py-0.5 rounded-full bg-red-100 text-red-700">Inativo</span>
              </div>
              <p class="font-medium text-gray-800">{{ faq.pergunta }}</p>
              <p class="text-sm text-gray-500 mt-1 line-clamp-2">{{ faq.resposta }}</p>
            </div>
            <div class="flex gap-2 flex-shrink-0">
              <button @click="editar(faq)" class="text-sm text-indigo-600 hover:text-indigo-800">Editar</button>
              <button v-if="faq.ativo" @click="excluir(faq.id)" class="text-sm text-red-500 hover:text-red-700">Desativar</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../../api/axiosInstance'
import { useToast } from '../../composables/useToast'
import { useConfirm } from '../../composables/useConfirm'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'

const toast = useToast()
const { confirm } = useConfirm()
const loading = ref(true)
const saving = ref(false)
const faqs = ref([])
const editingId = ref(null)
const form = ref({ pergunta: '', resposta: '', categoria: '', ordem: 0 })

async function carregar() {
  loading.value = true
  try {
    const res = await api.get('/admin-max/faq')
    faqs.value = res.data || []
  } catch { toast.error('Erro ao carregar FAQs.') }
  finally { loading.value = false }
}

async function salvar() {
  saving.value = true
  try {
    if (editingId.value) {
      await api.put(`/admin-max/faq/${editingId.value}`, form.value)
      toast.success('FAQ atualizada!')
    } else {
      await api.post('/admin-max/faq', form.value)
      toast.success('FAQ criada!')
    }
    resetForm()
    await carregar()
  } catch (e) { toast.error(e.response?.data?.mensagem || 'Erro ao salvar.') }
  finally { saving.value = false }
}

function editar(faq) {
  editingId.value = faq.id
  form.value = { pergunta: faq.pergunta, resposta: faq.resposta, categoria: faq.categoria || '', ordem: faq.ordem || 0 }
}

function cancelEdit() {
  editingId.value = null
  resetForm()
}

async function excluir(id) {
  const ok = await confirm({ title: 'Desativar FAQ', message: 'Desativar esta FAQ? Ela nao sera mais exibida publicamente.', confirmText: 'Desativar', variant: 'warning' })
  if (!ok) return
  try {
    await api.delete(`/admin-max/faq/${id}`)
    toast.success('FAQ desativada.')
    await carregar()
  } catch { toast.error('Erro ao desativar.') }
}

function resetForm() {
  editingId.value = null
  form.value = { pergunta: '', resposta: '', categoria: '', ordem: 0 }
}

onMounted(carregar)
</script>

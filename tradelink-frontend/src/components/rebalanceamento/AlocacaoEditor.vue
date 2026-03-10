<template>
  <div v-if="show" class="fixed inset-0 z-50 flex items-center justify-center">
    <div class="fixed inset-0 bg-black/50" @click="$emit('close')"></div>
    <div class="bg-white rounded-xl shadow-xl max-w-2xl w-full mx-4 max-h-[90vh] overflow-hidden flex flex-col relative z-10">
      <div class="p-4 border-b flex justify-between items-center">
        <h3 class="text-lg font-semibold">Alocacao Ideal</h3>
        <button type="button" @click="$emit('close')" class="text-gray-400 hover:text-gray-600">&#x2715;</button>
      </div>
      <div class="p-4 overflow-y-auto flex-1">
        <!-- Config -->
        <div class="grid grid-cols-2 gap-3 mb-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Margem de Erro (%)</label>
            <input v-model.number="margemErro" type="number" step="0.5" min="0" max="50" class="w-full px-3 py-2 border rounded-lg text-sm" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Moeda Referencia</label>
            <select v-model="moedaReferencia" class="w-full px-3 py-2 border rounded-lg text-sm">
              <option value="USD">USD (Dolar)</option>
              <option value="BRL">BRL (Real)</option>
            </select>
          </div>
        </div>

        <!-- Tabela de alocacoes -->
        <div class="border rounded-lg overflow-hidden mb-4">
          <table class="w-full text-sm">
            <thead class="bg-gray-50">
              <tr>
                <th class="text-left px-3 py-2 font-medium text-gray-600">Ativo</th>
                <th class="text-left px-3 py-2 font-medium text-gray-600">Nome</th>
                <th class="text-right px-3 py-2 font-medium text-gray-600">% Alvo</th>
                <th class="w-10"></th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(row, idx) in rows" :key="idx" class="border-t">
                <td class="px-3 py-2">
                  <input v-model="row.simbolo" type="text" placeholder="BTC" list="moedas-list"
                    @input="onSimboloChange(idx)" class="w-full px-2 py-1 border rounded text-sm uppercase" />
                </td>
                <td class="px-3 py-2">
                  <input v-model="row.nome" type="text" placeholder="Bitcoin" class="w-full px-2 py-1 border rounded text-sm" />
                </td>
                <td class="px-3 py-2">
                  <input v-model.number="row.percentualAlvo" type="number" step="0.5" min="0" max="100"
                    class="w-full px-2 py-1 border rounded text-sm text-right" />
                </td>
                <td class="px-2 py-2">
                  <button @click="removeRow(idx)" class="text-red-400 hover:text-red-600" title="Remover">&#x2715;</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <datalist id="moedas-list">
          <option v-for="m in MOEDAS" :key="m.value" :value="m.value">{{ m.label }}</option>
        </datalist>

        <div class="flex items-center justify-between mb-4">
          <button @click="addRow" class="text-sm text-indigo-600 hover:underline">+ Adicionar ativo</button>
          <div class="text-sm font-medium" :class="totalOk ? 'text-emerald-600' : 'text-red-600'">
            Total: {{ totalPercent.toFixed(1) }}%
            <span v-if="!totalOk" class="ml-1">(deve ser 100%)</span>
          </div>
        </div>

        <p v-if="erro" class="text-red-500 text-sm mb-3">{{ erro }}</p>

        <div class="flex justify-end gap-3">
          <button @click="$emit('close')" class="px-4 py-2 text-sm text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200">Cancelar</button>
          <button @click="salvar" :disabled="saving || !totalOk" class="px-4 py-2 text-sm text-white bg-indigo-600 rounded-lg hover:bg-indigo-700 disabled:opacity-50">
            {{ saving ? 'Salvando...' : 'Salvar' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { MOEDAS } from '../../utils/constants'
import alocacaoApi from '../../api/alocacaoApi'

const props = defineProps({
  show: Boolean,
  carteiraId: [Number, String],
  alocacoesAtuais: { type: Array, default: () => [] },
  margemErroAtual: { type: Number, default: 5 },
  moedaReferenciaAtual: { type: String, default: 'USD' }
})

const emit = defineEmits(['close', 'saved'])

const rows = ref([])
const margemErro = ref(5)
const moedaReferencia = ref('USD')
const saving = ref(false)
const erro = ref('')

const totalPercent = computed(() => rows.value.reduce((sum, r) => sum + (r.percentualAlvo || 0), 0))
const totalOk = computed(() => Math.abs(totalPercent.value - 100) < 0.01)

watch(() => props.show, (v) => {
  if (v) {
    erro.value = ''
    margemErro.value = props.margemErroAtual || 5
    moedaReferencia.value = props.moedaReferenciaAtual || 'USD'
    if (props.alocacoesAtuais?.length) {
      rows.value = props.alocacoesAtuais.map(a => ({
        simbolo: a.simbolo,
        nome: a.nome,
        percentualAlvo: Number(a.percentualAlvo),
        parMoedaReferencia: a.parMoedaReferencia || 'USD'
      }))
    } else {
      rows.value = [
        { simbolo: 'BTC', nome: 'Bitcoin', percentualAlvo: 40, parMoedaReferencia: 'USD' },
        { simbolo: 'USD', nome: 'Dolar', percentualAlvo: 60, parMoedaReferencia: 'USD' }
      ]
    }
  }
})

function addRow() {
  rows.value.push({ simbolo: '', nome: '', percentualAlvo: 0, parMoedaReferencia: moedaReferencia.value })
}

function removeRow(idx) {
  if (rows.value.length > 1) rows.value.splice(idx, 1)
}

function onSimboloChange(idx) {
  const row = rows.value[idx]
  const sim = (row.simbolo || '').toUpperCase()
  const found = MOEDAS.find(m => m.value === sim)
  if (found && !row.nome) {
    row.nome = found.label.split(' (')[0] || found.label
  }
}

async function salvar() {
  erro.value = ''
  if (!totalOk.value) { erro.value = 'A soma dos percentuais deve ser 100%.'; return }
  const simbolos = rows.value.map(r => r.simbolo.toUpperCase())
  if (new Set(simbolos).size !== simbolos.length) { erro.value = 'Ativos duplicados encontrados.'; return }
  if (rows.value.some(r => !r.simbolo || !r.nome)) { erro.value = 'Preencha todos os campos.'; return }

  saving.value = true
  try {
    const payload = {
      alocacoes: rows.value.map(r => ({
        simbolo: r.simbolo.toUpperCase(),
        nome: r.nome,
        percentualAlvo: r.percentualAlvo,
        parMoedaReferencia: r.parMoedaReferencia || moedaReferencia.value
      })),
      margemErro: margemErro.value,
      moedaReferencia: moedaReferencia.value
    }
    const res = await alocacaoApi.salvarAlocacoes(props.carteiraId, payload)
    emit('saved', res.data)
    emit('close')
  } catch (e) {
    erro.value = e.response?.data?.erro || e.response?.data?.mensagem || 'Erro ao salvar alocacoes.'
  } finally {
    saving.value = false
  }
}
</script>

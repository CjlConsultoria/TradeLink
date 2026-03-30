<template>
  <div>
    <!-- Header -->
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 mb-6">
      <div>
        <h2 class="page-title mb-1">Email Marketing</h2>
        <p class="text-sm text-gray-500 dark:text-gray-400">Envie emails de apresentação para leads externos</p>
      </div>
      <div class="flex gap-2">
        <button
          v-for="tab in tabs" :key="tab.key"
          @click="abaAtiva = tab.key"
          class="px-4 py-2 rounded-lg text-sm font-medium transition-colors"
          :class="abaAtiva === tab.key
            ? 'bg-indigo-600 text-white shadow-sm'
            : 'bg-gray-100 dark:bg-slate-700 text-gray-600 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-slate-600'"
        >
          {{ tab.label }}
        </button>
      </div>
    </div>

    <!-- Stats Cards -->
    <div class="grid grid-cols-2 lg:grid-cols-4 gap-3 sm:gap-4 mb-6">
      <div class="card p-3 sm:p-4">
        <p class="text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wide">Total Contatos</p>
        <p class="text-xl sm:text-2xl font-bold text-gray-900 dark:text-white mt-1">{{ contatos.length }}</p>
      </div>
      <div class="card p-3 sm:p-4">
        <p class="text-xs font-medium text-emerald-600 uppercase tracking-wide">Com Email</p>
        <p class="text-xl sm:text-2xl font-bold text-gray-900 dark:text-white mt-1">{{ contatos.filter(c => c.email).length }}</p>
      </div>
      <div class="card p-3 sm:p-4">
        <p class="text-xs font-medium text-blue-600 uppercase tracking-wide">Selecionados</p>
        <p class="text-xl sm:text-2xl font-bold text-gray-900 dark:text-white mt-1">{{ selecionados.length }}</p>
      </div>
      <div class="card p-3 sm:p-4">
        <p class="text-xs font-medium text-amber-600 uppercase tracking-wide">Limite Diário</p>
        <p class="text-xl sm:text-2xl font-bold text-gray-900 dark:text-white mt-1">100</p>
      </div>
    </div>

    <!-- TAB: CONTATOS / ENVIAR -->
    <div v-if="abaAtiva === 'contatos'">
      <!-- Importar / Adicionar -->
      <div class="card p-4 sm:p-5 mb-4">
        <div class="flex flex-col gap-4">
          <!-- Importar CSV -->
          <div>
            <h3 class="text-sm font-semibold text-gray-700 dark:text-gray-300 mb-2">📥 Importar CSV/Excel</h3>
            <div class="flex flex-col sm:flex-row gap-3 items-start sm:items-end">
              <div class="flex-1 w-full">
                <label class="block text-xs text-gray-500 dark:text-gray-400 mb-1">Arquivo CSV (colunas: nome, email)</label>
                <input
                  ref="fileInput"
                  type="file"
                  accept=".csv,.txt"
                  @change="importarCSV"
                  class="block w-full text-sm text-gray-500 dark:text-gray-400
                    file:mr-3 file:py-2 file:px-4 file:rounded-lg file:border-0
                    file:text-sm file:font-medium file:bg-indigo-50 dark:file:bg-indigo-900/30
                    file:text-indigo-700 dark:file:text-indigo-300 hover:file:bg-indigo-100
                    dark:hover:file:bg-indigo-900/50 cursor-pointer"
                />
              </div>
              <p class="text-xs text-gray-400">Separador: <code class="bg-gray-100 dark:bg-slate-700 px-1 rounded">;</code> ou <code class="bg-gray-100 dark:bg-slate-700 px-1 rounded">,</code></p>
            </div>
          </div>

          <div class="border-t border-gray-200 dark:border-slate-600"></div>

          <!-- Adicionar manualmente -->
          <div>
            <h3 class="text-sm font-semibold text-gray-700 dark:text-gray-300 mb-2">➕ Adicionar manualmente</h3>
            <form @submit.prevent="adicionarContato" class="flex flex-col sm:flex-row gap-2">
              <input v-model="novoContato.nome" type="text" placeholder="Nome (opcional)" class="input flex-1" />
              <input v-model="novoContato.email" type="email" placeholder="Email *" class="input flex-1" required />
              <button type="submit" class="btn btn-primary whitespace-nowrap px-4 py-2 text-sm">
                Adicionar
              </button>
            </form>
          </div>
        </div>
      </div>

      <!-- Configuração do envio -->
      <div class="card p-4 sm:p-5 mb-4">
        <h3 class="text-sm font-semibold text-gray-700 dark:text-gray-300 mb-3">⚙️ Configuração do Envio</h3>
        <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
          <div>
            <label class="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1">Assunto do Email</label>
            <input v-model="configEnvio.assunto" type="text" class="input w-full" placeholder="Conheça o TradeLink" />
          </div>
          <div>
            <label class="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1">Campanha (tag)</label>
            <input v-model="configEnvio.campanha" type="text" class="input w-full" placeholder="leads-consultores-mar2026" />
          </div>
        </div>
      </div>

      <!-- Ações em lote -->
      <div class="card p-3 sm:p-4 mb-4">
        <div class="flex flex-col sm:flex-row gap-3 sm:items-center">
          <div class="relative flex-1 min-w-0">
            <input v-model="busca" type="text" placeholder="Buscar por nome ou email..." class="input w-full pl-9" />
            <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/></svg>
          </div>
          <div class="flex items-center gap-2 sm:gap-3 justify-between sm:justify-end">
            <span class="text-xs sm:text-sm text-gray-500 dark:text-gray-400 whitespace-nowrap">{{ selecionados.length }} selecionado(s)</span>
            <button @click="selecionarTodos" class="btn btn-secondary whitespace-nowrap text-xs sm:text-sm px-2.5 sm:px-3 py-1.5">
              {{ todosVisiveisSelecionados ? 'Desmarcar' : 'Selecionar' }} todos
            </button>
            <button @click="removerSelecionados" v-if="selecionados.length > 0" class="btn whitespace-nowrap text-xs sm:text-sm px-2.5 sm:px-3 py-1.5 bg-red-100 text-red-700 hover:bg-red-200 dark:bg-red-900/30 dark:text-red-400">
              Remover
            </button>
            <button @click="abrirPreview(null)" class="btn btn-secondary whitespace-nowrap text-xs sm:text-sm px-2.5 sm:px-3 py-1.5">
              Preview
            </button>
            <button
              @click="enviarEmLote"
              :disabled="selecionados.length === 0 || enviando"
              class="btn btn-primary whitespace-nowrap text-xs sm:text-sm px-3 sm:px-4 py-1.5 sm:py-2"
            >
              <template v-if="enviando">
                <svg class="animate-spin -ml-1 mr-1.5 h-3.5 w-3.5 text-white inline" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/></svg>
                Enviando...
              </template>
              <template v-else>🚀 Enviar {{ selecionados.length > 0 ? `(${selecionados.length})` : '' }}</template>
            </button>
          </div>
        </div>
      </div>

      <!-- Lista de contatos -->
      <div class="card overflow-hidden">
        <div v-if="contatos.length === 0" class="text-center py-12 text-gray-500 dark:text-gray-400">
          <svg class="w-12 h-12 mx-auto text-gray-300 dark:text-gray-600 mb-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"/></svg>
          <p class="font-medium">Nenhum contato adicionado</p>
          <p class="text-sm mt-1">Importe um CSV ou adicione contatos manualmente</p>
        </div>
        <template v-else>
          <!-- Desktop Table Header -->
          <div class="hidden md:grid grid-cols-[40px_1fr_1fr_80px] gap-4 px-5 py-3 bg-gray-50 dark:bg-slate-800 border-b border-gray-200 dark:border-slate-600 text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase tracking-wider">
            <div></div>
            <div>Nome</div>
            <div>Email</div>
            <div class="text-right">Ações</div>
          </div>

          <!-- Desktop Rows -->
          <div
            v-for="(c, idx) in contatosPaginados" :key="idx"
            class="hidden md:grid grid-cols-[40px_1fr_1fr_80px] gap-4 px-5 py-3 items-center border-b border-gray-100 dark:border-slate-700 transition-colors cursor-pointer"
            :class="selecionados.includes(idx + paginaInicio) ? 'bg-indigo-50/60 dark:bg-indigo-900/20' : 'hover:bg-gray-50 dark:hover:bg-slate-800'"
            @click="toggleSelecionado(idx + paginaInicio)"
          >
            <div>
              <input type="checkbox" :checked="selecionados.includes(idx + paginaInicio)" @click.stop @change="toggleSelecionado(idx + paginaInicio)" class="rounded border-gray-300 text-indigo-600 focus:ring-indigo-500" />
            </div>
            <div class="min-w-0">
              <p class="text-sm font-medium text-gray-900 dark:text-white truncate">{{ c.nome || '(sem nome)' }}</p>
            </div>
            <div class="min-w-0">
              <p class="text-sm text-gray-600 dark:text-gray-300 truncate">{{ c.email }}</p>
            </div>
            <div class="text-right flex items-center justify-end gap-2">
              <button @click.stop="abrirPreview(c)" class="text-xs text-indigo-600 hover:text-indigo-800 dark:text-indigo-400 font-semibold">Preview</button>
              <button @click.stop="removerContato(idx + paginaInicio)" class="text-xs text-red-500 hover:text-red-700 dark:text-red-400 font-semibold">✕</button>
            </div>
          </div>

          <!-- Mobile Cards -->
          <div class="md:hidden divide-y divide-gray-100 dark:divide-slate-700">
            <div
              v-for="(c, idx) in contatosPaginados" :key="'m-' + idx"
              class="flex items-start gap-3 p-3 transition-colors cursor-pointer"
              :class="selecionados.includes(idx + paginaInicio) ? 'bg-indigo-50/60 dark:bg-indigo-900/20' : 'hover:bg-gray-50 dark:hover:bg-slate-800'"
              @click="toggleSelecionado(idx + paginaInicio)"
            >
              <input type="checkbox" :checked="selecionados.includes(idx + paginaInicio)" @click.stop @change="toggleSelecionado(idx + paginaInicio)" class="rounded border-gray-300 text-indigo-600 focus:ring-indigo-500 mt-1 flex-shrink-0" />
              <div class="flex-1 min-w-0">
                <p class="text-sm font-medium text-gray-900 dark:text-white truncate">{{ c.nome || '(sem nome)' }}</p>
                <p class="text-xs text-gray-500 dark:text-gray-400 truncate">{{ c.email }}</p>
              </div>
              <div class="flex gap-2 flex-shrink-0">
                <button @click.stop="abrirPreview(c)" class="text-xs text-indigo-600 dark:text-indigo-400 font-semibold">Preview</button>
                <button @click.stop="removerContato(idx + paginaInicio)" class="text-xs text-red-500 dark:text-red-400">✕</button>
              </div>
            </div>
          </div>

          <!-- Paginação -->
          <div class="flex flex-col sm:flex-row items-center justify-between gap-2 px-4 sm:px-5 py-3 bg-gray-50 dark:bg-slate-800 border-t border-gray-200 dark:border-slate-600">
            <p class="text-xs text-gray-500 dark:text-gray-400">
              {{ paginaInicio + 1 }}–{{ Math.min(paginaFim, contatosFiltrados.length) }} de {{ contatosFiltrados.length }}
            </p>
            <div class="flex items-center gap-1">
              <button @click="pagina = pagina - 1" :disabled="pagina <= 1" class="px-2.5 sm:px-3 py-1.5 text-xs font-medium rounded-md border border-gray-300 dark:border-slate-600 bg-white dark:bg-slate-700 text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-slate-600 disabled:opacity-40 disabled:cursor-not-allowed">
                Anterior
              </button>
              <template v-for="p in paginasVisiveis" :key="p">
                <button v-if="p === '...'" disabled class="px-2 py-1.5 text-xs text-gray-400">...</button>
                <button v-else @click="pagina = p" class="px-2.5 sm:px-3 py-1.5 text-xs font-medium rounded-md border transition-colors" :class="pagina === p ? 'bg-indigo-600 text-white border-indigo-600' : 'border-gray-300 dark:border-slate-600 bg-white dark:bg-slate-700 text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-slate-600'">
                  {{ p }}
                </button>
              </template>
              <button @click="pagina = pagina + 1" :disabled="pagina >= totalPaginas" class="px-2.5 sm:px-3 py-1.5 text-xs font-medium rounded-md border border-gray-300 dark:border-slate-600 bg-white dark:bg-slate-700 text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-slate-600 disabled:opacity-40 disabled:cursor-not-allowed">
                Próxima
              </button>
            </div>
          </div>
        </template>
      </div>
    </div>

    <!-- TAB: HISTÓRICO -->
    <div v-if="abaAtiva === 'historico'">
      <div class="card overflow-hidden">
        <LoadingSpinner v-if="loadingHistorico" size="sm" class="py-12" />
        <div v-else-if="historico.length === 0" class="text-center py-12 text-gray-500 dark:text-gray-400">
          <svg class="w-12 h-12 mx-auto text-gray-300 dark:text-gray-600 mb-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"/></svg>
          <p class="font-medium">Nenhum email marketing enviado ainda</p>
        </div>
        <template v-else>
          <!-- Desktop -->
          <div class="hidden md:grid grid-cols-[1fr_1fr_120px_140px_90px] gap-4 px-5 py-3 bg-gray-50 dark:bg-slate-800 border-b border-gray-200 dark:border-slate-600 text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase tracking-wider">
            <div>Nome</div>
            <div>Email</div>
            <div>Campanha</div>
            <div>Enviado</div>
            <div class="text-right">Status</div>
          </div>
          <div
            v-for="h in historicoPaginado" :key="h.id"
            class="hidden md:grid grid-cols-[1fr_1fr_120px_140px_90px] gap-4 px-5 py-3 items-center border-b border-gray-100 dark:border-slate-700 hover:bg-gray-50 dark:hover:bg-slate-800 transition-colors"
          >
            <div class="min-w-0">
              <p class="text-sm font-medium text-gray-900 dark:text-white truncate">{{ h.nome || '(sem nome)' }}</p>
            </div>
            <div class="min-w-0">
              <p class="text-sm text-gray-600 dark:text-gray-300 truncate">{{ h.email }}</p>
            </div>
            <div>
              <span v-if="h.campanha" class="text-xs px-2 py-0.5 rounded-full bg-indigo-100 dark:bg-indigo-900/30 text-indigo-700 dark:text-indigo-300 font-medium">{{ h.campanha }}</span>
            </div>
            <div>
              <p class="text-xs text-gray-700 dark:text-gray-300">{{ formatData(h.enviadoEm) }}</p>
              <p class="text-xs text-gray-400">por {{ h.enviadoPor }}</p>
            </div>
            <div class="text-right">
              <span class="text-xs px-2 py-0.5 rounded-full font-medium inline-flex items-center gap-1" :class="h.status === 'ENVIADO' ? 'bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-400' : 'bg-red-100 dark:bg-red-900/30 text-red-700 dark:text-red-400'">
                <span class="w-1.5 h-1.5 rounded-full" :class="h.status === 'ENVIADO' ? 'bg-green-500' : 'bg-red-500'"></span>
                {{ h.status === 'ENVIADO' ? 'Enviado' : 'Falha' }}
              </span>
              <p v-if="h.erro" class="text-xs text-red-400 mt-0.5 truncate max-w-[120px]" :title="h.erro">{{ h.erro }}</p>
            </div>
          </div>

          <!-- Mobile -->
          <div class="md:hidden divide-y divide-gray-100 dark:divide-slate-700">
            <div v-for="h in historicoPaginado" :key="'mh-' + h.id" class="p-3">
              <div class="flex items-start justify-between gap-2 mb-1.5">
                <div class="min-w-0 flex-1">
                  <p class="text-sm font-medium text-gray-900 dark:text-white truncate">{{ h.nome || '(sem nome)' }}</p>
                  <p class="text-xs text-gray-500 dark:text-gray-400 truncate">{{ h.email }}</p>
                </div>
                <span class="text-[11px] px-2 py-0.5 rounded-full font-medium inline-flex items-center gap-1 flex-shrink-0" :class="h.status === 'ENVIADO' ? 'bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-400' : 'bg-red-100 dark:bg-red-900/30 text-red-700 dark:text-red-400'">
                  <span class="w-1.5 h-1.5 rounded-full" :class="h.status === 'ENVIADO' ? 'bg-green-500' : 'bg-red-500'"></span>
                  {{ h.status === 'ENVIADO' ? 'Enviado' : 'Falha' }}
                </span>
              </div>
              <div class="flex items-center gap-2 flex-wrap">
                <span v-if="h.campanha" class="text-[11px] px-2 py-0.5 rounded-full bg-indigo-100 dark:bg-indigo-900/30 text-indigo-700 dark:text-indigo-300 font-medium">{{ h.campanha }}</span>
                <span class="text-[11px] text-gray-400">{{ formatData(h.enviadoEm) }} por {{ h.enviadoPor }}</span>
              </div>
            </div>
          </div>

          <!-- Paginação -->
          <div class="flex flex-col sm:flex-row items-center justify-between gap-2 px-4 sm:px-5 py-3 bg-gray-50 dark:bg-slate-800 border-t border-gray-200 dark:border-slate-600">
            <p class="text-xs text-gray-500 dark:text-gray-400">
              {{ histPaginaInicio + 1 }}–{{ Math.min(histPaginaFim, historico.length) }} de {{ historico.length }}
            </p>
            <div class="flex items-center gap-1">
              <button @click="histPagina = histPagina - 1" :disabled="histPagina <= 1" class="px-2.5 sm:px-3 py-1.5 text-xs font-medium rounded-md border border-gray-300 dark:border-slate-600 bg-white dark:bg-slate-700 text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-slate-600 disabled:opacity-40 disabled:cursor-not-allowed">Anterior</button>
              <template v-for="p in histPaginasVisiveis" :key="p">
                <button v-if="p === '...'" disabled class="px-2 py-1.5 text-xs text-gray-400">...</button>
                <button v-else @click="histPagina = p" class="px-2.5 sm:px-3 py-1.5 text-xs font-medium rounded-md border transition-colors" :class="histPagina === p ? 'bg-indigo-600 text-white border-indigo-600' : 'border-gray-300 dark:border-slate-600 bg-white dark:bg-slate-700 text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-slate-600'">{{ p }}</button>
              </template>
              <button @click="histPagina = histPagina + 1" :disabled="histPagina >= histTotalPaginas" class="px-2.5 sm:px-3 py-1.5 text-xs font-medium rounded-md border border-gray-300 dark:border-slate-600 bg-white dark:bg-slate-700 text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-slate-600 disabled:opacity-40 disabled:cursor-not-allowed">Próxima</button>
            </div>
          </div>
        </template>
      </div>
    </div>

    <!-- Modal Preview -->
    <Teleport to="body">
      <div v-if="preview.visivel" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-2 sm:p-4" @click.self="fecharPreview">
        <div class="bg-white dark:bg-slate-800 rounded-xl shadow-2xl w-full max-w-4xl max-h-[95vh] sm:max-h-[90vh] flex flex-col">
          <div class="flex items-center justify-between px-4 sm:px-5 py-3 sm:py-4 border-b border-gray-200 dark:border-slate-600">
            <div>
              <h3 class="text-base sm:text-lg font-semibold text-gray-900 dark:text-white">Preview do Email</h3>
              <p v-if="preview.contato" class="text-xs text-gray-500 dark:text-gray-400 mt-0.5">Para: {{ preview.contato.nome || '' }} &lt;{{ preview.contato.email }}&gt;</p>
            </div>
            <button @click="fecharPreview" class="w-8 h-8 rounded-lg flex items-center justify-center text-gray-400 hover:bg-gray-100 dark:hover:bg-slate-700 hover:text-gray-600 transition-colors">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/></svg>
            </button>
          </div>
          <div class="flex-1 overflow-auto p-1.5 sm:p-2 bg-gray-100 dark:bg-slate-900">
            <LoadingSpinner v-if="preview.loading" size="sm" class="py-12" />
            <iframe v-else :srcdoc="preview.html" class="w-full bg-white rounded-lg shadow-sm border-0" style="min-height: 400px; height: 70vh;"></iframe>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- Modal Progresso de Envio -->
    <Teleport to="body">
      <div v-if="progressoEnvio.visivel" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
        <div class="bg-white dark:bg-slate-800 rounded-xl shadow-2xl w-full max-w-md p-6">
          <h3 class="text-lg font-semibold text-gray-900 dark:text-white mb-4 text-center">📧 Enviando Emails</h3>
          <div class="mb-4">
            <div class="flex justify-between text-sm text-gray-600 dark:text-gray-300 mb-2">
              <span>{{ progressoEnvio.enviados + progressoEnvio.falhas }} de {{ progressoEnvio.total }}</span>
              <span>{{ Math.round(((progressoEnvio.enviados + progressoEnvio.falhas) / progressoEnvio.total) * 100) }}%</span>
            </div>
            <div class="w-full bg-gray-200 dark:bg-slate-700 rounded-full h-3">
              <div class="h-3 rounded-full transition-all duration-300 bg-gradient-to-r from-indigo-500 to-purple-500"
                :style="{ width: ((progressoEnvio.enviados + progressoEnvio.falhas) / progressoEnvio.total * 100) + '%' }">
              </div>
            </div>
          </div>
          <div class="grid grid-cols-2 gap-3 mb-4">
            <div class="text-center p-3 bg-green-50 dark:bg-green-900/20 rounded-lg">
              <p class="text-2xl font-bold text-green-600">{{ progressoEnvio.enviados }}</p>
              <p class="text-xs text-green-700 dark:text-green-400">Enviados</p>
            </div>
            <div class="text-center p-3 bg-red-50 dark:bg-red-900/20 rounded-lg">
              <p class="text-2xl font-bold text-red-600">{{ progressoEnvio.falhas }}</p>
              <p class="text-xs text-red-700 dark:text-red-400">Falhas</p>
            </div>
          </div>
          <p v-if="progressoEnvio.emailAtual" class="text-xs text-gray-500 dark:text-gray-400 text-center truncate">
            Enviando para: {{ progressoEnvio.emailAtual }}
          </p>
          <button v-if="!enviando" @click="progressoEnvio.visivel = false" class="btn btn-primary w-full mt-4">
            Fechar
          </button>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import emailMarketingApi from '../../api/emailMarketingApi'
import { useToast } from '../../composables/useToast'
import { useConfirm } from '../../composables/useConfirm'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'

const toast = useToast()
const { confirm } = useConfirm()
const POR_PAGINA = 15

const tabs = [
  { key: 'contatos', label: '📋 Contatos' },
  { key: 'historico', label: '📊 Histórico' }
]

const abaAtiva = ref('contatos')
const enviando = ref(false)
const loadingHistorico = ref(false)
const contatos = ref([])
const historico = ref([])
const selecionados = ref([])
const busca = ref('')
const pagina = ref(1)
const histPagina = ref(1)
const fileInput = ref(null)

const novoContato = ref({ nome: '', email: '' })
const configEnvio = ref({
  assunto: 'Conheça o TradeLink - Plataforma de Gestão de Investimentos',
  campanha: 'leads-consultores-mar2026'
})

const preview = ref({ visivel: false, loading: false, html: '', contato: null })
const progressoEnvio = ref({ visivel: false, total: 0, enviados: 0, falhas: 0, emailAtual: '' })

// --- Filtro ---
const contatosFiltrados = computed(() => {
  if (!busca.value) return contatos.value
  const q = busca.value.toLowerCase()
  return contatos.value.filter(c => (c.nome || '').toLowerCase().includes(q) || (c.email || '').toLowerCase().includes(q))
})

const todosVisiveisSelecionados = computed(() => {
  if (contatosFiltrados.value.length === 0) return false
  return contatosFiltrados.value.every((_, idx) => {
    const realIdx = contatos.value.indexOf(contatosFiltrados.value[idx])
    return selecionados.value.includes(realIdx)
  })
})

// --- Paginação Contatos ---
const totalPaginas = computed(() => Math.max(1, Math.ceil(contatosFiltrados.value.length / POR_PAGINA)))
const paginaInicio = computed(() => (pagina.value - 1) * POR_PAGINA)
const paginaFim = computed(() => paginaInicio.value + POR_PAGINA)
const contatosPaginados = computed(() => contatosFiltrados.value.slice(paginaInicio.value, paginaFim.value))
const paginasVisiveis = computed(() => gerarPaginas(pagina.value, totalPaginas.value))

// --- Paginação Histórico ---
const histTotalPaginas = computed(() => Math.max(1, Math.ceil(historico.value.length / POR_PAGINA)))
const histPaginaInicio = computed(() => (histPagina.value - 1) * POR_PAGINA)
const histPaginaFim = computed(() => histPaginaInicio.value + POR_PAGINA)
const historicoPaginado = computed(() => historico.value.slice(histPaginaInicio.value, histPaginaFim.value))
const histPaginasVisiveis = computed(() => gerarPaginas(histPagina.value, histTotalPaginas.value))

function gerarPaginas(atual, total) {
  if (total <= 7) return Array.from({ length: total }, (_, i) => i + 1)
  const pages = [1]
  if (atual > 3) pages.push('...')
  for (let i = Math.max(2, atual - 1); i <= Math.min(total - 1, atual + 1); i++) pages.push(i)
  if (atual < total - 2) pages.push('...')
  pages.push(total)
  return pages
}

watch(busca, () => { pagina.value = 1 })

// --- Importar CSV ---
function importarCSV(event) {
  const file = event.target.files[0]
  if (!file) return

  const reader = new FileReader()
  reader.onload = (e) => {
    const text = e.target.result
    const lines = text.split(/\r?\n/).filter(l => l.trim())
    if (lines.length === 0) return

    // Detectar separador
    const sep = lines[0].includes(';') ? ';' : ','

    // Detectar se primeira linha é header
    const firstLine = lines[0].toLowerCase()
    const startIdx = (firstLine.includes('nome') || firstLine.includes('email') || firstLine.includes('empresa')) ? 1 : 0

    let importados = 0
    const emailsExistentes = new Set(contatos.value.map(c => c.email.toLowerCase()))

    for (let i = startIdx; i < lines.length; i++) {
      const cols = lines[i].split(sep).map(c => c.trim().replace(/^["']|["']$/g, ''))
      if (cols.length < 1) continue

      // Tentar encontrar coluna de email
      let email = '', nome = ''
      for (const col of cols) {
        if (col.includes('@') && col.includes('.')) {
          email = col.toLowerCase()
          break
        }
      }

      if (!email) continue

      // Nome é a primeira coluna que não é email
      for (const col of cols) {
        if (!col.includes('@') && col.length > 1) {
          nome = col
          break
        }
      }

      if (!emailsExistentes.has(email)) {
        contatos.value.push({ nome, email })
        emailsExistentes.add(email)
        importados++
      }
    }

    toast.success(`${importados} contato(s) importado(s)!`)
    if (fileInput.value) fileInput.value.value = ''
  }
  reader.readAsText(file, 'UTF-8')
}

// --- Adicionar manualmente ---
function adicionarContato() {
  const email = novoContato.value.email.trim().toLowerCase()
  if (!email) return

  if (contatos.value.some(c => c.email.toLowerCase() === email)) {
    toast.warning('Este email já está na lista.')
    return
  }

  contatos.value.push({
    nome: novoContato.value.nome.trim(),
    email
  })
  novoContato.value = { nome: '', email: '' }
  toast.success('Contato adicionado!')
}

// --- Seleção ---
function toggleSelecionado(idx) {
  const i = selecionados.value.indexOf(idx)
  if (i === -1) selecionados.value.push(idx)
  else selecionados.value.splice(i, 1)
}

function selecionarTodos() {
  if (todosVisiveisSelecionados.value) {
    const indices = contatosFiltrados.value.map((_, idx) => contatos.value.indexOf(contatosFiltrados.value[idx]))
    selecionados.value = selecionados.value.filter(i => !indices.includes(i))
  } else {
    const set = new Set(selecionados.value)
    contatosFiltrados.value.forEach((_, idx) => set.add(contatos.value.indexOf(contatosFiltrados.value[idx])))
    selecionados.value = [...set]
  }
}

function removerContato(idx) {
  contatos.value.splice(idx, 1)
  selecionados.value = selecionados.value.filter(i => i !== idx).map(i => i > idx ? i - 1 : i)
}

function removerSelecionados() {
  const sorted = [...selecionados.value].sort((a, b) => b - a)
  sorted.forEach(idx => contatos.value.splice(idx, 1))
  selecionados.value = []
  toast.info('Contatos removidos.')
}

// --- Preview ---
async function abrirPreview(contato) {
  preview.value = { visivel: true, loading: true, html: '', contato }
  try {
    const res = await emailMarketingApi.preview()
    preview.value.html = res.data.html
  } catch (e) {
    toast.error('Erro ao carregar preview.')
    preview.value.visivel = false
  } finally {
    preview.value.loading = false
  }
}

function fecharPreview() {
  preview.value.visivel = false
}

// --- Enviar ---
async function enviarEmLote() {
  if (selecionados.value.length === 0) return

  const contatosSelecionados = selecionados.value.map(idx => contatos.value[idx]).filter(c => c && c.email)

  if (contatosSelecionados.length > 100) {
    toast.error('Máximo de 100 emails por lote. Selecione menos contatos.')
    return
  }

  const ok = await confirm({
    title: 'Confirmar envio',
    message: `Enviar email de apresentação para ${contatosSelecionados.length} contato(s)?\n\nAssunto: ${configEnvio.value.assunto}\nCampanha: ${configEnvio.value.campanha || '(sem tag)'}`,
    confirmText: 'Enviar',
    variant: 'info'
  })
  if (!ok) return

  enviando.value = true
  progressoEnvio.value = {
    visivel: true,
    total: contatosSelecionados.length,
    enviados: 0,
    falhas: 0,
    emailAtual: contatosSelecionados[0]?.email || ''
  }

  try {
    const payload = contatosSelecionados.map(c => ({ email: c.email, nome: c.nome || '' }))
    const res = await emailMarketingApi.enviar(payload, configEnvio.value.assunto, configEnvio.value.campanha)
    const resultados = res.data || []

    const enviados = resultados.filter(r => r.status === 'ENVIADO').length
    const falhas = resultados.filter(r => r.status === 'FALHA').length

    progressoEnvio.value.enviados = enviados
    progressoEnvio.value.falhas = falhas
    progressoEnvio.value.emailAtual = ''

    if (enviados > 0) toast.success(`${enviados} email(s) enviado(s) com sucesso!`)
    if (falhas > 0) toast.error(`${falhas} email(s) falharam.`)

    selecionados.value = []
    await carregarHistorico()
  } catch (e) {
    toast.error(e.response?.data?.mensagem || e.response?.data?.message || 'Erro ao enviar emails.')
    progressoEnvio.value.visivel = false
  } finally {
    enviando.value = false
  }
}

// --- Histórico ---
async function carregarHistorico() {
  loadingHistorico.value = true
  try {
    const res = await emailMarketingApi.historico()
    historico.value = res.data || []
  } catch (e) {
    toast.error('Erro ao carregar histórico.')
  } finally {
    loadingHistorico.value = false
  }
}

function formatData(dt) {
  if (!dt) return '-'
  const d = new Date(dt)
  return d.toLocaleDateString('pt-BR') + ' ' + d.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })
}

watch(abaAtiva, (tab) => {
  if (tab === 'historico' && historico.value.length === 0) carregarHistorico()
})
</script>

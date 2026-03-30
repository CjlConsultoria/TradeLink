<template>
  <div>
    <!-- Header -->
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 mb-6">
      <div>
        <h2 class="page-title mb-1">Email Marketing</h2>
        <p class="text-sm" style="color: rgb(var(--tl-text-muted));">Envie emails de apresentacao para leads externos via Resend</p>
      </div>
      <div class="flex gap-2">
        <button
          v-for="tab in tabs" :key="tab.key"
          @click="abaAtiva = tab.key"
          class="px-4 py-2.5 rounded-lg text-sm font-semibold transition-all"
          :class="abaAtiva === tab.key
            ? 'text-white shadow-md'
            : 'border hover:opacity-80'"
          :style="abaAtiva === tab.key
            ? 'background: rgb(var(--tl-primary));'
            : 'background: rgb(var(--tl-surface)); border-color: rgb(var(--tl-border)); color: rgb(var(--tl-text));'"
        >
          {{ tab.label }}
        </button>
      </div>
    </div>

    <!-- Stats Cards -->
    <div class="grid grid-cols-2 lg:grid-cols-4 gap-3 sm:gap-4 mb-6">
      <div class="card p-4">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 rounded-xl flex items-center justify-center text-lg" style="background: rgba(var(--tl-primary), 0.1);">📋</div>
          <div>
            <p class="text-xs font-semibold uppercase tracking-wide" style="color: rgb(var(--tl-text-muted));">Total</p>
            <p class="text-2xl font-bold" style="color: rgb(var(--tl-text));">{{ contatos.length }}</p>
          </div>
        </div>
      </div>
      <div class="card p-4">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 rounded-xl flex items-center justify-center text-lg bg-emerald-100 dark:bg-emerald-900/30">✅</div>
          <div>
            <p class="text-xs font-semibold text-emerald-600 uppercase tracking-wide">Com Email</p>
            <p class="text-2xl font-bold" style="color: rgb(var(--tl-text));">{{ contatos.filter(c => c.email).length }}</p>
          </div>
        </div>
      </div>
      <div class="card p-4">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 rounded-xl flex items-center justify-center text-lg bg-blue-100 dark:bg-blue-900/30">☑️</div>
          <div>
            <p class="text-xs font-semibold text-blue-600 uppercase tracking-wide">Selecionados</p>
            <p class="text-2xl font-bold" style="color: rgb(var(--tl-text));">{{ selecionados.length }}</p>
          </div>
        </div>
      </div>
      <div class="card p-4">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 rounded-xl flex items-center justify-center text-lg bg-amber-100 dark:bg-amber-900/30">⚡</div>
          <div>
            <p class="text-xs font-semibold text-amber-600 uppercase tracking-wide">Limite/Dia</p>
            <p class="text-2xl font-bold" style="color: rgb(var(--tl-text));">100</p>
          </div>
        </div>
      </div>
    </div>

    <!-- TAB: CONTATOS -->
    <div v-if="abaAtiva === 'contatos'">
      <!-- Importar / Adicionar -->
      <div class="card p-5 mb-4">
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-5">
          <!-- Importar CSV -->
          <div class="p-4 rounded-xl" style="background: rgba(var(--tl-primary), 0.04); border: 1px dashed rgba(var(--tl-primary), 0.3);">
            <h3 class="text-sm font-bold mb-3 flex items-center gap-2" style="color: rgb(var(--tl-text));">
              <span class="w-7 h-7 rounded-lg flex items-center justify-center text-xs text-white" style="background: rgb(var(--tl-primary));">📥</span>
              Importar CSV
            </h3>
            <label class="block text-xs font-medium mb-2" style="color: rgb(var(--tl-text-muted));">Arquivo com colunas: nome, email (separador ; ou ,)</label>
            <input
              ref="fileInput"
              type="file"
              accept=".csv,.txt"
              @change="importarCSV"
              class="block w-full text-sm cursor-pointer file:mr-3 file:py-2.5 file:px-5 file:rounded-lg file:border-0 file:text-sm file:font-bold file:cursor-pointer"
              style="color: rgb(var(--tl-text-muted));"
              :style="{ '--file-bg': 'rgb(var(--tl-primary))', '--file-color': 'white' }"
            />
          </div>

          <!-- Adicionar manualmente -->
          <div class="p-4 rounded-xl" style="background: rgba(16, 185, 129, 0.04); border: 1px dashed rgba(16, 185, 129, 0.3);">
            <h3 class="text-sm font-bold mb-3 flex items-center gap-2" style="color: rgb(var(--tl-text));">
              <span class="w-7 h-7 rounded-lg bg-emerald-500 flex items-center justify-center text-xs text-white">➕</span>
              Adicionar Manualmente
            </h3>
            <form @submit.prevent="adicionarContato" class="flex flex-col sm:flex-row gap-2">
              <input v-model="novoContato.nome" type="text" placeholder="Nome (opcional)" class="input-base flex-1" />
              <input v-model="novoContato.email" type="email" placeholder="Email *" class="input-base flex-1" required />
              <button type="submit" class="btn-primary px-5 py-2.5 rounded-lg text-sm font-bold whitespace-nowrap">
                + Adicionar
              </button>
            </form>
          </div>
        </div>
      </div>

      <!-- Configuracao do envio -->
      <div class="card p-5 mb-4">
        <h3 class="text-sm font-bold mb-3 flex items-center gap-2" style="color: rgb(var(--tl-text));">
          <span class="w-7 h-7 rounded-lg flex items-center justify-center text-xs text-white" style="background: rgb(var(--tl-primary));">⚙️</span>
          Configuracao do Envio
        </h3>
        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <label class="block text-xs font-bold uppercase tracking-wide mb-1.5" style="color: rgb(var(--tl-text-muted));">Assunto do Email</label>
            <input v-model="configEnvio.assunto" type="text" class="input-base w-full" placeholder="Conheca o TradeLink" />
          </div>
          <div>
            <label class="block text-xs font-bold uppercase tracking-wide mb-1.5" style="color: rgb(var(--tl-text-muted));">Campanha (tag)</label>
            <input v-model="configEnvio.campanha" type="text" class="input-base w-full" placeholder="leads-consultores-mar2026" />
          </div>
        </div>
      </div>

      <!-- Toolbar -->
      <div class="card p-3 sm:p-4 mb-4">
        <div class="flex flex-col sm:flex-row gap-3 sm:items-center">
          <div class="relative flex-1 min-w-0">
            <input v-model="busca" type="text" placeholder="Buscar por nome ou email..." class="input-base w-full pl-10" />
            <svg class="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4" style="color: rgb(var(--tl-text-muted));" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/></svg>
          </div>
          <div class="flex items-center gap-2 flex-wrap justify-between sm:justify-end">
            <span class="text-xs font-bold px-2.5 py-1 rounded-full" style="background: rgba(var(--tl-primary), 0.1); color: rgb(var(--tl-primary));">{{ selecionados.length }} selecionado(s)</span>
            <button @click="selecionarTodos" class="px-3 py-2 rounded-lg text-xs font-bold border transition-colors" style="border-color: rgb(var(--tl-border)); color: rgb(var(--tl-text)); background: rgb(var(--tl-surface));">
              {{ todosVisiveisSelecionados ? '✕ Desmarcar' : '☑ Selecionar' }} todos
            </button>
            <button v-if="selecionados.length > 0" @click="removerSelecionados" class="px-3 py-2 rounded-lg text-xs font-bold bg-red-500 text-white hover:bg-red-600 transition-colors">
              🗑 Remover
            </button>
            <button @click="abrirPreview(null)" class="px-3 py-2 rounded-lg text-xs font-bold border transition-colors" style="border-color: rgb(var(--tl-border)); color: rgb(var(--tl-text)); background: rgb(var(--tl-surface));">
              👁 Preview
            </button>
            <button
              @click="enviarEmLote"
              :disabled="selecionados.length === 0 || enviando"
              class="btn-primary px-4 py-2.5 rounded-lg text-sm font-bold whitespace-nowrap disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <template v-if="enviando">
                <svg class="animate-spin -ml-1 mr-1.5 h-4 w-4 text-white inline" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/></svg>
                Enviando...
              </template>
              <template v-else>🚀 Enviar {{ selecionados.length > 0 ? `(${selecionados.length})` : '' }}</template>
            </button>
          </div>
        </div>
      </div>

      <!-- Lista de contatos -->
      <div class="card overflow-hidden">
        <div v-if="contatos.length === 0" class="text-center py-16">
          <div class="w-16 h-16 mx-auto mb-4 rounded-2xl flex items-center justify-center text-3xl" style="background: rgba(var(--tl-primary), 0.08);">📧</div>
          <p class="text-base font-bold mb-1" style="color: rgb(var(--tl-text));">Nenhum contato adicionado</p>
          <p class="text-sm" style="color: rgb(var(--tl-text-muted));">Importe um CSV ou adicione contatos manualmente acima</p>
        </div>
        <template v-else>
          <!-- Desktop Table -->
          <div class="hidden md:grid grid-cols-[40px_2fr_2fr_100px] gap-3 px-5 py-3 text-xs font-bold uppercase tracking-wider" style="background: rgba(var(--tl-primary), 0.05); border-bottom: 2px solid rgba(var(--tl-primary), 0.15); color: rgb(var(--tl-text-muted));">
            <div></div>
            <div>Nome / Empresa</div>
            <div>Email</div>
            <div class="text-right">Acoes</div>
          </div>
          <div
            v-for="(c, idx) in contatosPaginados" :key="idx"
            class="hidden md:grid grid-cols-[40px_2fr_2fr_100px] gap-3 px-5 py-3.5 items-center transition-all cursor-pointer"
            :class="isSelected(idx) ? 'border-l-4' : 'border-l-4 border-l-transparent hover:border-l-gray-200'"
            :style="isSelected(idx)
              ? 'background: rgba(var(--tl-primary), 0.06); border-left-color: rgb(var(--tl-primary)); border-bottom: 1px solid rgba(var(--tl-primary), 0.1);'
              : 'border-bottom: 1px solid rgb(var(--tl-border));'"
            @click="toggleSelecionado(getRealIdx(idx))"
          >
            <div>
              <input type="checkbox" :checked="isSelected(idx)" @click.stop @change="toggleSelecionado(getRealIdx(idx))" class="w-4 h-4 rounded border-gray-300 text-indigo-600 focus:ring-indigo-500 cursor-pointer" />
            </div>
            <div class="min-w-0">
              <p class="text-sm font-semibold truncate" style="color: rgb(var(--tl-text));">{{ c.nome || '—' }}</p>
            </div>
            <div class="min-w-0">
              <p class="text-sm truncate" style="color: rgb(var(--tl-text-muted));">{{ c.email }}</p>
            </div>
            <div class="flex items-center justify-end gap-3">
              <button @click.stop="abrirPreview(c)" class="text-xs font-bold px-2 py-1 rounded-md transition-colors hover:underline" style="color: rgb(var(--tl-primary));">👁</button>
              <button @click.stop="removerContato(getRealIdx(idx))" class="text-xs font-bold px-2 py-1 rounded-md text-red-500 hover:bg-red-50 dark:hover:bg-red-900/20 transition-colors">✕</button>
            </div>
          </div>

          <!-- Mobile Cards -->
          <div class="md:hidden">
            <div
              v-for="(c, idx) in contatosPaginados" :key="'m-' + idx"
              class="flex items-center gap-3 px-4 py-3.5 transition-all cursor-pointer"
              :style="isSelected(idx)
                ? 'background: rgba(var(--tl-primary), 0.06); border-bottom: 1px solid rgba(var(--tl-primary), 0.1);'
                : 'border-bottom: 1px solid rgb(var(--tl-border));'"
              @click="toggleSelecionado(getRealIdx(idx))"
            >
              <input type="checkbox" :checked="isSelected(idx)" @click.stop @change="toggleSelecionado(getRealIdx(idx))" class="w-4 h-4 rounded border-gray-300 text-indigo-600 focus:ring-indigo-500 flex-shrink-0" />
              <div class="flex-1 min-w-0">
                <p class="text-sm font-semibold truncate" style="color: rgb(var(--tl-text));">{{ c.nome || '—' }}</p>
                <p class="text-xs truncate" style="color: rgb(var(--tl-text-muted));">{{ c.email }}</p>
              </div>
              <div class="flex gap-1 flex-shrink-0">
                <button @click.stop="abrirPreview(c)" class="text-xs p-1.5 rounded-md" style="color: rgb(var(--tl-primary));">👁</button>
                <button @click.stop="removerContato(getRealIdx(idx))" class="text-xs p-1.5 rounded-md text-red-500">✕</button>
              </div>
            </div>
          </div>

          <!-- Paginacao -->
          <div class="flex flex-col sm:flex-row items-center justify-between gap-2 px-5 py-3" style="background: rgba(var(--tl-primary), 0.03); border-top: 1px solid rgb(var(--tl-border));">
            <p class="text-xs font-medium" style="color: rgb(var(--tl-text-muted));">
              {{ paginaInicio + 1 }}–{{ Math.min(paginaFim, contatosFiltrados.length) }} de {{ contatosFiltrados.length }}
            </p>
            <div class="flex items-center gap-1">
              <button @click="pagina = pagina - 1" :disabled="pagina <= 1" class="px-3 py-1.5 text-xs font-bold rounded-md border disabled:opacity-30 disabled:cursor-not-allowed" style="border-color: rgb(var(--tl-border)); color: rgb(var(--tl-text)); background: rgb(var(--tl-surface));">Anterior</button>
              <template v-for="p in paginasVisiveis" :key="p">
                <button v-if="p === '...'" disabled class="px-2 py-1.5 text-xs" style="color: rgb(var(--tl-text-muted));">...</button>
                <button v-else @click="pagina = p" class="px-3 py-1.5 text-xs font-bold rounded-md border transition-colors" :style="pagina === p ? 'background: rgb(var(--tl-primary)); color: white; border-color: rgb(var(--tl-primary));' : 'border-color: rgb(var(--tl-border)); color: rgb(var(--tl-text)); background: rgb(var(--tl-surface));'">{{ p }}</button>
              </template>
              <button @click="pagina = pagina + 1" :disabled="pagina >= totalPaginas" class="px-3 py-1.5 text-xs font-bold rounded-md border disabled:opacity-30 disabled:cursor-not-allowed" style="border-color: rgb(var(--tl-border)); color: rgb(var(--tl-text)); background: rgb(var(--tl-surface));">Proxima</button>
            </div>
          </div>
        </template>
      </div>
    </div>

    <!-- TAB: HISTORICO -->
    <div v-if="abaAtiva === 'historico'">
      <div class="card overflow-hidden">
        <LoadingSpinner v-if="loadingHistorico" size="sm" class="py-12" />
        <div v-else-if="historico.length === 0" class="text-center py-16">
          <div class="w-16 h-16 mx-auto mb-4 rounded-2xl flex items-center justify-center text-3xl" style="background: rgba(var(--tl-primary), 0.08);">📊</div>
          <p class="text-base font-bold mb-1" style="color: rgb(var(--tl-text));">Nenhum email marketing enviado</p>
          <p class="text-sm" style="color: rgb(var(--tl-text-muted));">Envie emails na aba Contatos</p>
        </div>
        <template v-else>
          <div class="hidden md:grid grid-cols-[1.5fr_2fr_120px_140px_90px] gap-3 px-5 py-3 text-xs font-bold uppercase tracking-wider" style="background: rgba(var(--tl-primary), 0.05); border-bottom: 2px solid rgba(var(--tl-primary), 0.15); color: rgb(var(--tl-text-muted));">
            <div>Nome</div>
            <div>Email</div>
            <div>Campanha</div>
            <div>Enviado</div>
            <div class="text-right">Status</div>
          </div>
          <div
            v-for="h in historicoPaginado" :key="h.id"
            class="hidden md:grid grid-cols-[1.5fr_2fr_120px_140px_90px] gap-3 px-5 py-3.5 items-center transition-colors"
            style="border-bottom: 1px solid rgb(var(--tl-border));"
          >
            <div class="min-w-0">
              <p class="text-sm font-semibold truncate" style="color: rgb(var(--tl-text));">{{ h.nome || '—' }}</p>
            </div>
            <div class="min-w-0">
              <p class="text-sm truncate" style="color: rgb(var(--tl-text-muted));">{{ h.email }}</p>
            </div>
            <div>
              <span v-if="h.campanha" class="text-[11px] px-2.5 py-1 rounded-full font-bold" style="background: rgba(var(--tl-primary), 0.1); color: rgb(var(--tl-primary));">{{ h.campanha }}</span>
            </div>
            <div>
              <p class="text-xs font-medium" style="color: rgb(var(--tl-text));">{{ formatData(h.enviadoEm) }}</p>
              <p class="text-[11px]" style="color: rgb(var(--tl-text-muted));">por {{ h.enviadoPor }}</p>
            </div>
            <div class="text-right">
              <span class="text-[11px] px-2.5 py-1 rounded-full font-bold inline-flex items-center gap-1.5" :class="h.status === 'ENVIADO' ? 'bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-400' : 'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400'">
                <span class="w-2 h-2 rounded-full" :class="h.status === 'ENVIADO' ? 'bg-emerald-500' : 'bg-red-500'"></span>
                {{ h.status === 'ENVIADO' ? 'Enviado' : 'Falha' }}
              </span>
            </div>
          </div>

          <!-- Mobile -->
          <div class="md:hidden">
            <div v-for="h in historicoPaginado" :key="'mh-' + h.id" class="px-4 py-3.5" style="border-bottom: 1px solid rgb(var(--tl-border));">
              <div class="flex items-start justify-between gap-2 mb-1.5">
                <div class="min-w-0 flex-1">
                  <p class="text-sm font-semibold truncate" style="color: rgb(var(--tl-text));">{{ h.nome || '—' }}</p>
                  <p class="text-xs truncate" style="color: rgb(var(--tl-text-muted));">{{ h.email }}</p>
                </div>
                <span class="text-[11px] px-2 py-0.5 rounded-full font-bold inline-flex items-center gap-1 flex-shrink-0" :class="h.status === 'ENVIADO' ? 'bg-emerald-100 text-emerald-700' : 'bg-red-100 text-red-700'">
                  <span class="w-1.5 h-1.5 rounded-full" :class="h.status === 'ENVIADO' ? 'bg-emerald-500' : 'bg-red-500'"></span>
                  {{ h.status === 'ENVIADO' ? 'Enviado' : 'Falha' }}
                </span>
              </div>
              <div class="flex items-center gap-2 flex-wrap">
                <span v-if="h.campanha" class="text-[11px] px-2 py-0.5 rounded-full font-bold" style="background: rgba(var(--tl-primary), 0.1); color: rgb(var(--tl-primary));">{{ h.campanha }}</span>
                <span class="text-[11px]" style="color: rgb(var(--tl-text-muted));">{{ formatData(h.enviadoEm) }} · {{ h.enviadoPor }}</span>
              </div>
            </div>
          </div>

          <!-- Paginacao -->
          <div class="flex flex-col sm:flex-row items-center justify-between gap-2 px-5 py-3" style="background: rgba(var(--tl-primary), 0.03); border-top: 1px solid rgb(var(--tl-border));">
            <p class="text-xs font-medium" style="color: rgb(var(--tl-text-muted));">{{ histPaginaInicio + 1 }}–{{ Math.min(histPaginaFim, historico.length) }} de {{ historico.length }}</p>
            <div class="flex items-center gap-1">
              <button @click="histPagina = histPagina - 1" :disabled="histPagina <= 1" class="px-3 py-1.5 text-xs font-bold rounded-md border disabled:opacity-30" style="border-color: rgb(var(--tl-border)); color: rgb(var(--tl-text)); background: rgb(var(--tl-surface));">Anterior</button>
              <template v-for="p in histPaginasVisiveis" :key="p">
                <button v-if="p === '...'" disabled class="px-2 py-1.5 text-xs" style="color: rgb(var(--tl-text-muted));">...</button>
                <button v-else @click="histPagina = p" class="px-3 py-1.5 text-xs font-bold rounded-md border" :style="histPagina === p ? 'background: rgb(var(--tl-primary)); color: white; border-color: rgb(var(--tl-primary));' : 'border-color: rgb(var(--tl-border)); color: rgb(var(--tl-text)); background: rgb(var(--tl-surface));'">{{ p }}</button>
              </template>
              <button @click="histPagina = histPagina + 1" :disabled="histPagina >= histTotalPaginas" class="px-3 py-1.5 text-xs font-bold rounded-md border disabled:opacity-30" style="border-color: rgb(var(--tl-border)); color: rgb(var(--tl-text)); background: rgb(var(--tl-surface));">Proxima</button>
            </div>
          </div>
        </template>
      </div>
    </div>

    <!-- Modal Preview -->
    <Teleport to="body">
      <div v-if="preview.visivel" class="fixed inset-0 z-50 flex items-center justify-center p-2 sm:p-4" style="background: rgba(0,0,0,0.6);" @click.self="fecharPreview">
        <div class="rounded-xl shadow-2xl w-full max-w-4xl max-h-[95vh] sm:max-h-[90vh] flex flex-col" style="background: rgb(var(--tl-surface));">
          <div class="flex items-center justify-between px-5 py-4" style="border-bottom: 1px solid rgb(var(--tl-border));">
            <div>
              <h3 class="text-lg font-bold" style="color: rgb(var(--tl-text));">Preview do Email</h3>
              <p v-if="preview.contato" class="text-xs mt-0.5" style="color: rgb(var(--tl-text-muted));">Para: {{ preview.contato.nome || '' }} &lt;{{ preview.contato.email }}&gt;</p>
            </div>
            <button @click="fecharPreview" class="w-9 h-9 rounded-lg flex items-center justify-center transition-colors hover:opacity-70" style="color: rgb(var(--tl-text-muted));">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/></svg>
            </button>
          </div>
          <div class="flex-1 overflow-auto p-2" style="background: rgb(var(--tl-surface-alt));">
            <LoadingSpinner v-if="preview.loading" size="sm" class="py-12" />
            <iframe v-else :srcdoc="preview.html" class="w-full rounded-lg shadow-sm border-0" style="min-height: 400px; height: 70vh; background: white;"></iframe>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- Modal Progresso -->
    <Teleport to="body">
      <div v-if="progressoEnvio.visivel" class="fixed inset-0 z-50 flex items-center justify-center p-4" style="background: rgba(0,0,0,0.6);">
        <div class="rounded-xl shadow-2xl w-full max-w-md p-6" style="background: rgb(var(--tl-surface));">
          <h3 class="text-lg font-bold mb-4 text-center" style="color: rgb(var(--tl-text));">📧 Enviando Emails</h3>
          <div class="mb-4">
            <div class="flex justify-between text-sm font-medium mb-2" style="color: rgb(var(--tl-text));">
              <span>{{ progressoEnvio.enviados + progressoEnvio.falhas }} de {{ progressoEnvio.total }}</span>
              <span>{{ Math.round(((progressoEnvio.enviados + progressoEnvio.falhas) / Math.max(progressoEnvio.total, 1)) * 100) }}%</span>
            </div>
            <div class="w-full rounded-full h-3" style="background: rgb(var(--tl-border));">
              <div class="h-3 rounded-full transition-all duration-300" style="background: linear-gradient(135deg, rgb(var(--tl-primary)), #8b5cf6);"
                :style="{ width: ((progressoEnvio.enviados + progressoEnvio.falhas) / Math.max(progressoEnvio.total, 1) * 100) + '%' }">
              </div>
            </div>
          </div>
          <div class="grid grid-cols-2 gap-3 mb-4">
            <div class="text-center p-3 bg-emerald-50 dark:bg-emerald-900/20 rounded-xl">
              <p class="text-2xl font-bold text-emerald-600">{{ progressoEnvio.enviados }}</p>
              <p class="text-xs font-bold text-emerald-700 dark:text-emerald-400">Enviados</p>
            </div>
            <div class="text-center p-3 bg-red-50 dark:bg-red-900/20 rounded-xl">
              <p class="text-2xl font-bold text-red-600">{{ progressoEnvio.falhas }}</p>
              <p class="text-xs font-bold text-red-700 dark:text-red-400">Falhas</p>
            </div>
          </div>
          <p v-if="progressoEnvio.emailAtual" class="text-xs text-center truncate mb-3" style="color: rgb(var(--tl-text-muted));">
            Enviando para: {{ progressoEnvio.emailAtual }}
          </p>
          <button v-if="!enviando" @click="progressoEnvio.visivel = false" class="btn-primary w-full py-2.5 rounded-lg text-sm font-bold">
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
  { key: 'historico', label: '📊 Historico' }
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
  assunto: 'Conheca o TradeLink - Plataforma de Gestao de Investimentos',
  campanha: 'leads-consultores-mar2026'
})

const preview = ref({ visivel: false, loading: false, html: '', contato: null })
const progressoEnvio = ref({ visivel: false, total: 0, enviados: 0, falhas: 0, emailAtual: '' })

// --- Helpers ---
function getRealIdx(paginatedIdx) {
  const item = contatosFiltrados.value[paginatedIdx + paginaInicio.value]
  return contatos.value.indexOf(item)
}
function isSelected(paginatedIdx) {
  return selecionados.value.includes(getRealIdx(paginatedIdx))
}

// --- Filtro ---
const contatosFiltrados = computed(() => {
  if (!busca.value) return contatos.value
  const q = busca.value.toLowerCase()
  return contatos.value.filter(c => (c.nome || '').toLowerCase().includes(q) || (c.email || '').toLowerCase().includes(q))
})

const todosVisiveisSelecionados = computed(() => {
  if (contatosFiltrados.value.length === 0) return false
  return contatosFiltrados.value.every(c => selecionados.value.includes(contatos.value.indexOf(c)))
})

// --- Paginacao Contatos ---
const totalPaginas = computed(() => Math.max(1, Math.ceil(contatosFiltrados.value.length / POR_PAGINA)))
const paginaInicio = computed(() => (pagina.value - 1) * POR_PAGINA)
const paginaFim = computed(() => paginaInicio.value + POR_PAGINA)
const contatosPaginados = computed(() => contatosFiltrados.value.slice(paginaInicio.value, paginaFim.value))
const paginasVisiveis = computed(() => gerarPaginas(pagina.value, totalPaginas.value))

// --- Paginacao Historico ---
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
    const sep = lines[0].includes(';') ? ';' : ','
    const firstLine = lines[0].toLowerCase()
    const startIdx = (firstLine.includes('nome') || firstLine.includes('email') || firstLine.includes('empresa')) ? 1 : 0
    let importados = 0
    const emailsExistentes = new Set(contatos.value.map(c => c.email.toLowerCase()))
    for (let i = startIdx; i < lines.length; i++) {
      const cols = lines[i].split(sep).map(c => c.trim().replace(/^["']|["']$/g, ''))
      if (cols.length < 1) continue
      let email = '', nome = ''
      for (const col of cols) {
        if (col.includes('@') && col.includes('.')) { email = col.toLowerCase(); break }
      }
      if (!email) continue
      for (const col of cols) {
        if (!col.includes('@') && col.length > 1) { nome = col; break }
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
  if (contatos.value.some(c => c.email.toLowerCase() === email)) { toast.warning('Email ja esta na lista.'); return }
  contatos.value.push({ nome: novoContato.value.nome.trim(), email })
  novoContato.value = { nome: '', email: '' }
  toast.success('Contato adicionado!')
}

// --- Selecao ---
function toggleSelecionado(idx) {
  const i = selecionados.value.indexOf(idx)
  if (i === -1) selecionados.value.push(idx)
  else selecionados.value.splice(i, 1)
}

function selecionarTodos() {
  if (todosVisiveisSelecionados.value) {
    const indices = contatosFiltrados.value.map(c => contatos.value.indexOf(c))
    selecionados.value = selecionados.value.filter(i => !indices.includes(i))
  } else {
    const set = new Set(selecionados.value)
    contatosFiltrados.value.forEach(c => set.add(contatos.value.indexOf(c)))
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
function fecharPreview() { preview.value.visivel = false }

// --- Enviar ---
async function enviarEmLote() {
  if (selecionados.value.length === 0) return
  const contatosSelecionados = selecionados.value.map(idx => contatos.value[idx]).filter(c => c && c.email)
  if (contatosSelecionados.length > 100) { toast.error('Maximo 100 emails por lote.'); return }
  const ok = await confirm({
    title: 'Confirmar envio',
    message: `Enviar email para ${contatosSelecionados.length} contato(s)?\n\nAssunto: ${configEnvio.value.assunto}\nCampanha: ${configEnvio.value.campanha || '(sem tag)'}`,
    confirmText: 'Enviar',
    variant: 'info'
  })
  if (!ok) return
  enviando.value = true
  progressoEnvio.value = { visivel: true, total: contatosSelecionados.length, enviados: 0, falhas: 0, emailAtual: contatosSelecionados[0]?.email || '' }
  try {
    const payload = contatosSelecionados.map(c => ({ email: c.email, nome: c.nome || '' }))
    const res = await emailMarketingApi.enviar(payload, configEnvio.value.assunto, configEnvio.value.campanha)
    const resultados = res.data || []
    const enviados = resultados.filter(r => r.status === 'ENVIADO').length
    const falhas = resultados.filter(r => r.status === 'FALHA').length
    progressoEnvio.value.enviados = enviados
    progressoEnvio.value.falhas = falhas
    progressoEnvio.value.emailAtual = ''
    if (enviados > 0) toast.success(`${enviados} email(s) enviado(s)!`)
    if (falhas > 0) toast.error(`${falhas} email(s) falharam.`)
    selecionados.value = []
    await carregarHistorico()
  } catch (e) {
    toast.error(e.response?.data?.mensagem || e.response?.data?.message || 'Erro ao enviar.')
    progressoEnvio.value.visivel = false
  } finally {
    enviando.value = false
  }
}

// --- Historico ---
async function carregarHistorico() {
  loadingHistorico.value = true
  try {
    const res = await emailMarketingApi.historico()
    historico.value = res.data || []
  } catch (e) {
    toast.error('Erro ao carregar historico.')
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

<style scoped>
input[type="file"]::file-selector-button {
  background: rgb(var(--tl-primary));
  color: white;
  border: none;
  padding: 0.625rem 1.25rem;
  border-radius: 0.5rem;
  font-weight: 700;
  font-size: 0.875rem;
  cursor: pointer;
  transition: opacity 0.2s;
}
input[type="file"]::file-selector-button:hover {
  opacity: 0.85;
}
</style>

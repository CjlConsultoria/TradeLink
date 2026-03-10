# Plano: Adicionar botão "Suporte" no menu lateral

## O que já existe (não precisa mudar)
- ✅ **Triagem IA** no ChatPanel.vue (3 fases: triagem → fila → chat humano)
- ✅ **~40 FAQs pré-populadas** no DbMigrationRunner.java (8 categorias)
- ✅ **FAQ no menu** do Consultor e Cliente (sidebar já tem ❓ FAQ)
- ✅ **FloatingChatButton** funcional no canto inferior direito
- ✅ **Rotas** /consultor/faq e /cliente/faq já existem

## O que falta (escopo real)
Apenas 1 coisa: **Botão "Suporte 💬"** no menu lateral do Consultor e Cliente que abre o chat.

### Mudanças necessárias:

1. **Criar composable `useChatPanel.js`** (novo arquivo)
   - Estado compartilhado `chatOpen` entre sidebar e FloatingChatButton
   - Funções `openChat()` e `closeChat()`

2. **Modificar `FloatingChatButton.vue`**
   - Usar o composable compartilhado em vez do `ref` local

3. **Modificar `AppSidebar.vue`**
   - Adicionar item `{ label: 'Suporte', icon: '💬', action: 'openChat' }` nos menus Consultor e Cliente
   - Tratar itens com `action` como `<button>` em vez de `<router-link>`
   - Importar `useChatPanel` para abrir o chat ao clicar

### Arquivos:
- **CRIAR**: `tradelink-frontend/src/composables/useChatPanel.js`
- **MODIFICAR**: `tradelink-frontend/src/components/chat/FloatingChatButton.vue`
- **MODIFICAR**: `tradelink-frontend/src/components/layout/AppSidebar.vue`

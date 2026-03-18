# Notificações: Consultor ↔ Cliente (cenários de teste)

Resumo dos **canais** e **fluxos** de notificação entre consultor e cliente. **E-mail**, **Telegram**, **Web Push** e **WhatsApp** estão implementados. SMS existe como opção na empresa mas **não está implementado** no código.

---

## Canais disponíveis (implementados)

| Canal      | Onde ativa | O que o usuário precisa                    | Configuração no sistema |
|-----------|------------|--------------------------------------------|--------------------------|
| **E-mail** | Por empresa | E-mail cadastrado no usuário               | Resend ou Gmail (ver CONFIGURAR-EMAIL-GMAIL.md) |
| **Telegram** | Por empresa | `telegram_chat_id` preenchido no usuário   | Bot Token no ambiente (`app.notificacao.telegram.bot-token`) |
| **Web Push** | Por empresa | Inscrição no navegador (Configurações do app) | VAPID no ambiente (`VAPID_PRIVATE` no Render) |
| **WhatsApp** | Por empresa | **Telefone** cadastrado no usuário (ex.: 11999998888) | Twilio: `TWILIO_ACCOUNT_SID`, `TWILIO_AUTH_TOKEN`, `TWILIO_WHATSAPP_FROM` (ver CONFIGURAR-WHATSAPP-TWILIO.md) |

Os canais são ligados/desligados por **empresa** (notificacao_email, notificacao_telegram, notificacao_push). Se o canal estiver ativo na empresa, cada usuário recebe por todos os canais para os quais estiver preparado (ex.: tem e-mail e chat_id do Telegram).

---

## 1. Consultor → Cliente

**Quando:** consultor **cria uma nova recomendação** em uma carteira.

**Quem recebe:** todos os **clientes** vinculados àquela carteira (e ativos).

**Canais usados (para cada cliente):**
- **E-mail** – se empresa tem `notificacao_email=true` e o cliente tem e-mail → envia HTML com detalhes da recomendação.
- **Telegram** – se empresa tem `notificacao_telegram=true` e o cliente tem `telegram_chat_id` → envia título + resumo em texto.
- **Web Push** – se empresa tem `notificacao_push=true` e o cliente tem pelo menos uma inscrição (push) no navegador → envia título + resumo.
- **WhatsApp** – se empresa tem `notificacao_whatsapp=true` e o cliente tem **telefone** cadastrado → envia título + resumo via Twilio.

**Como testar (cenário de teste):**
1. Empresa com **notificacao_email = true** (e, se quiser, telegram e push também).
2. Carteira com **pelo menos um cliente** que tenha:
   - **E-mail** preenchido (para receber e-mail).
   - (Opcional) **Telegram Chat ID** preenchido no usuário (para Telegram).
   - (Opcional) **Web Push** ativado nas Configurações do app no navegador (para push).
3. Consultor **cria uma nova recomendação** nessa carteira.
4. Verificar:
   - E-mail na caixa (e spam) do cliente.
   - Mensagem no Telegram do cliente (se configurado).
   - Notificação no navegador do cliente (se inscrito em push).

---

## 2. Cliente → Consultor

**Quando:** cliente **marca uma recomendação como resolvida** (operou/alvo atingido/etc.).

**Quem recebe:** o **consultor** dono da carteira daquela recomendação.

**Canais usados (para o consultor):**
- **E-mail** – se empresa tem `notificacao_email=true` e o consultor tem e-mail → envia HTML “Cliente X resolveu a recomendação Y/Z (carteira Z)”.
- **Telegram** – se empresa tem `notificacao_telegram=true` e o consultor tem `telegram_chat_id`.
- **Web Push** – se empresa tem `notificacao_push=true` e o consultor tem inscrição push no navegador.
- **WhatsApp** – se empresa tem `notificacao_whatsapp=true` e o consultor tem **telefone** cadastrado.

**Como testar (cenário de teste):**
1. Mesma empresa com notificações ativas (e-mail e, se quiser, telegram/push).
2. Consultor com **e-mail** (e, opcionalmente, telegram_chat_id e push ativado).
3. Cliente **abre a recomendação** e **marca como resolvida** (botão/ação “Resolver” ou equivalente no app).
4. Verificar:
   - E-mail no consultor com assunto tipo “Cliente resolveu recomendação”.
   - Telegram e/ou push no consultor, se configurados.

---

## Resumo rápido para teste

| Direção              | Gatilho                         | Quem recebe   | Canais (se ativos na empresa + dados do usuário) |
|----------------------|----------------------------------|---------------|---------------------------------------------------|
| **Consultor → Cliente** | Nova recomendação criada         | Clientes da carteira | E-mail, Telegram, Web Push, WhatsApp |
| **Cliente → Consultor** | Recomendação marcada como resolvida | Consultor da carteira | E-mail, Telegram, Web Push, WhatsApp |

---

## Checklist mínimo para teste completo

**Empresa (admin-max):**
- Notificação E-mail = sim.
- (Opcional) Notificação Telegram = sim e bot configurado.
- (Opcional) Notificação Push = sim e VAPID no ambiente.

**Consultor:**
- E-mail cadastrado.
- (Opcional) Telegram: chat_id preenchido no perfil.
- (Opcional) Push: acessar app como consultor → Configurações → ativar notificações no navegador.
- (Opcional) WhatsApp: telefone cadastrado; em teste com Twilio Sandbox, enviar "join \<código\>" para o número da sandbox.

**Cliente:**
- E-mail cadastrado.
- (Opcional) Telegram: chat_id preenchido.
- (Opcional) Push: acessar app como cliente → Configurações → ativar notificações no navegador.
- (Opcional) WhatsApp: telefone cadastrado; em teste com Twilio Sandbox, enviar "join \<código\>" para o número da sandbox.

**Teste Consultor → Cliente:** consultor cria recomendação na carteira onde o cliente está → cliente recebe por e-mail (e por telegram/push se configurados).

**Teste Cliente → Consultor:** cliente marca recomendação como resolvida → consultor recebe por e-mail (e por telegram/push se configurados).

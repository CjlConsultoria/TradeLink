# Configurar WhatsApp com Twilio (notificações TradeLink)

O TradeLink usa a **Twilio** para enviar mensagens WhatsApp (notificações de nova recomendação e de cliente resolvendo recomendação). A Twilio cobra **cerca de US$ 0,005 por mensagem** mais taxas da Meta por tipo de mensagem — uma das opções mais baratas entre as plataformas oficiais.

**Não é necessário usar seu número pessoal.** Você usa um número da Twilio (sandbox para teste ou número aprovado para produção).

---

## O que você precisa fazer

### 1. Criar conta na Twilio

1. Acesse [twilio.com](https://www.twilio.com) e clique em **Sign up**.
2. Preencha e-mail, senha e confirme o e-mail.
3. No primeiro acesso, a Twilio pode pedir verificação de celular (SMS) — use um número que você tenha acesso.

### 2. Ativar o WhatsApp (Sandbox para teste — mais rápido)

Para **testar sem aprovação de número próprio**:

1. No console da Twilio: **Messaging** → **Try it out** → **Send a WhatsApp message** (ou **Explore** → **WhatsApp**).
2. Abra a **Sandbox for WhatsApp**.
3. Você verá um número tipo **+1 415 523 8886** e um código para vincular (ex.: "join \<palavra-código\>").
4. No seu WhatsApp (celular), envie essa mensagem **join \<código\>** para o número da sandbox. Assim você autoriza receber mensagens da sandbox.
5. Para **cada número que for receber notificação** (clientes/consultores), essa pessoa também precisa enviar **join \<código\>** para o número da sandbox **uma vez**. Depois disso o sistema pode enviar mensagens para eles.
6. Anote:
   - **Account SID** (painel inicial: "Account SID", começa com `AC...`).
   - **Auth Token** (clique em "Show" ao lado de Auth Token).
   - **Número From** no formato do WhatsApp: `whatsapp:+14155238886` (use o número exato que a sandbox mostrar, com o código do país, sem espaços).

### 3. Configurar no Render (ou no seu ambiente)

No **Render** → seu serviço **CJLInvestimentos** → **Environment** → **Add Environment Variable**:

| Key | Value |
|-----|--------|
| `TWILIO_ACCOUNT_SID` | Seu Account SID (ex.: `ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx`) |
| `TWILIO_AUTH_TOKEN` | Seu Auth Token |
| `TWILIO_WHATSAPP_FROM` | O número From no formato `whatsapp:+14155238886` (sandbox) ou `whatsapp:+5511999999999` (número próprio aprovado) |

Salve. O Render reinicia o serviço automaticamente.

**Local / outros ambientes:** defina as mesmas variáveis (`TWILIO_ACCOUNT_SID`, `TWILIO_AUTH_TOKEN`, `TWILIO_WHATSAPP_FROM`) ou use em `application-local.properties`:

```properties
app.notificacao.whatsapp.account-sid=ACxxxxxxxx...
app.notificacao.whatsapp.auth-token=seu_token
app.notificacao.whatsapp.from=whatsapp:+14155238886
```

### 4. No TradeLink: empresa e usuários

1. **Empresa:** no painel admin-max, na empresa desejada, ative **Notificação WhatsApp** (ou "Notificações por WhatsApp").
2. **Usuários (clientes/consultores):** cada um precisa ter o **Telefone** preenchido no perfil (ex.: `11999998888` ou `11999998888`). O sistema normaliza para E.164 (+5511999998888) para Brasil.

### 5. Testar

- **Consultor → Cliente:** crie uma recomendação na carteira em que o cliente está. O cliente deve receber WhatsApp (além de e-mail/push, se configurados) **se** tiver telefone cadastrado e **se** tiver enviado `join <código>` para o número da sandbox (em teste).
- **Cliente → Consultor:** cliente marca recomendação como resolvida. O consultor recebe WhatsApp se tiver telefone e tiver feito o join na sandbox (em teste).

Nos logs da aplicação, ao subir o serviço, deve aparecer algo como:  
`Notificação WhatsApp: CONFIGURADO (Twilio). From: whatsapp:+14155238886 ...`

---

## Produção: número próprio (opcional)

Para **produção** sem sandbox:

1. Na Twilio: **Messaging** → **WhatsApp** → solicitar/registrar um **número próprio** para WhatsApp Business.
2. Siga o fluxo da Meta/Twilio para aprovação do número e do negócio (documentação, uso de templates fora da janela de 24h, etc.).
3. Depois de aprovado, use esse número em `TWILIO_WHATSAPP_FROM` no formato `whatsapp:+5511999999999`.

Enquanto estiver só testando, o **Sandbox** basta: basta cada destinatário fazer o **join** uma vez no número da sandbox.

---

## Resumo

| Onde | O que fazer |
|------|--------------|
| **Twilio** | Criar conta → WhatsApp Sandbox → anotar Account SID, Auth Token e número From (`whatsapp:+14...`) |
| **Celular (cada destinatário de teste)** | Enviar "join \<código\>" para o número da sandbox no WhatsApp |
| **Render** | Environment: `TWILIO_ACCOUNT_SID`, `TWILIO_AUTH_TOKEN`, `TWILIO_WHATSAPP_FROM` |
| **TradeLink** | Empresa com "Notificação WhatsApp" ativa; usuários com **Telefone** preenchido |

Custo aproximado: **~US$ 0,005 por mensagem** (Twilio) + taxas Meta conforme tipo/país. Para notificações utilitárias em janela de conversa, as taxas Meta costumam ser baixas.

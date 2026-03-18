# Configurar envio de e-mail com Gmail

O TradeLink usa o endereço **cjlconsultoria.consultor@gmail.com** configurado em `application-render.properties` / `application-production.properties`. Para o envio funcionar, é obrigatório configurar uma **Senha de app** no Gmail e definir a variável **MAIL_PASSWORD** no ambiente (Render, Rancher, etc.).

---

## ⚠️ Aplicação no Render: timeout ao conectar no Gmail

Se nos logs aparecer **"Mail server connection failed ... Couldn't connect to host smtp.gmail.com, port 587; timeout"** ou **"Operation timed out"**, o **Render bloqueia a porta SMTP 587**. O envio direto por Gmail **não funciona** nesse ambiente, mesmo com `MAIL_PASSWORD` correto.

**O que fazer:** use **Resend** (já integrado no TradeLink, plano gratuito). Veja a seção **Configurar e-mail com Resend (Render)** abaixo.

O restante deste documento vale para ambientes onde SMTP está liberado (local, VPS, Rancher, etc.) com Gmail.

---

## Configurar e-mail com Resend (Render) — gratuito

O **Resend** envia e-mail por API HTTPS (não usa porta 587), então **funciona no Render**. Plano gratuito: **100 e-mails por dia**.

### Passo a passo

1. **Criar conta no Resend**
   - Acesse [resend.com](https://resend.com) e crie uma conta (grátis).

2. **Obter a API Key**
   - No dashboard do Resend: **API Keys** → **Create API Key**.
   - Dê um nome (ex.: `TradeLink Render`) e copie a chave (começa com `re_`).
   - Guarde a chave; ela não é mostrada de novo.

3. **Configurar no Render**
   - No seu serviço no Render: **Environment** → **Add Environment Variable**.
   - **Key:** `RESEND_API_KEY`
   - **Value:** a API key que você copiou (ex.: `re_xxxxxxxxxxxx`).
   - Salve. O Render reinicia o serviço automaticamente.

4. **Remetente (from)**  
   - Com **onboarding@resend.dev** você **só pode enviar para o e-mail da sua conta Resend**.  
   - Para enviar para **qualquer cliente** (ex.: ticleyton@gmail.com), é obrigatório **verificar um domínio**. Veja o item 5 abaixo.

5. **Enviar para qualquer destinatário: verificar um domínio**
   - Se aparecer no log: **"403 Forbidden"** ou **"You can only send testing emails to your own email address"**, o Resend está bloqueando porque o remetente é de teste.
   - **Solução:** no [Resend Dashboard](https://resend.com/domains) → **Domains** → **Add Domain**.
   - Digite seu domínio (ex.: `seudominio.com` ou um subdomínio como `mail.seudominio.com`).
   - O Resend mostra os **registros DNS** que você deve criar no seu provedor (Registro.br, Cloudflare, GoDaddy, etc.):
     - **TXT** (para verificação e DKIM)
     - Às vezes **MX** (se for usar recebimento no mesmo domínio; para só enviar, só TXT basta)
   - No painel do seu provedor de domínio, crie os registros exatamente como o Resend indicar. Aguarde a propagação (minutos a algumas horas).
   - No Resend, clique em **Verify** no domínio. Quando aparecer **Verified**, use um e-mail desse domínio como remetente:
   - No Render: **Environment** → adicione (ou edite):
     - **Key:** `APP_NOTIFICACAO_EMAIL_FROM`  
     - **Value:** `TradeLink <noreply@seudominio.com>`  
     (substitua `noreply@seudominio.com` pelo endereço do domínio que você verificou.)
   - Se não usar variável de ambiente, edite em `application-render.properties`:  
     `app.notificacao.email.from=TradeLink <noreply@seudominio.com>`  
   - Reinicie o serviço no Render. Depois disso você poderá enviar para qualquer e-mail (clientes, etc.).

6. **Conferir**
   - Nos logs da aplicação deve aparecer:  
     `Notificação e-mail: CONFIGURADO (Resend). Remetente: ...`
   - Ao criar uma recomendação ou cadastrar usuário, o e-mail deve ser enviado. Se não chegar, veja a pasta de **spam**.

### Resumo Resend

| Onde | O que fazer |
|------|-------------|
| **Resend** | Criar conta em [resend.com](https://resend.com) → API Keys → Create API Key → copiar a chave `re_...` |
| **Render** | Environment → `RESEND_API_KEY` = chave copiada → salvar (serviço reinicia) |
| **Remetente** | Já configurado como `onboarding@resend.dev`; opcional: trocar por domínio verificado no Resend |

---

## Por que não funciona com a senha normal?

O Gmail não permite mais que aplicativos usem a senha da sua conta para SMTP. É obrigatório usar **Senha de app** (App Password), e para criá-la a conta precisa ter **Verificação em duas etapas** ativada.

## Passo a passo no Gmail

### 1. Ativar Verificação em duas etapas

1. Acesse [Conta Google → Segurança](https://myaccount.google.com/security).
2. Em **Como fazer login no Google**, clique em **Verificação em duas etapas**.
3. Ative e conclua o fluxo (celular, etc.).

### 2. Criar uma Senha de app

1. Acesse [Senhas de app](https://myaccount.google.com/apppasswords).
   - Se o link não aparecer, volte em **Segurança** e procure por "Senhas de app" ou "App passwords".
2. Em **Selecionar app**, escolha **Outro (nome personalizado)** e digite por exemplo: `TradeLink`.
3. Clique em **Gerar**.
4. O Gmail mostra uma **senha de 16 caracteres** (ex.: `abcd efgh ijkl mnop`). **Copie essa senha** (sem os espaços, se o sistema pedir).
5. Você não poderá ver essa senha de novo depois de sair da tela.

### 3. Definir MAIL_PASSWORD no ambiente

- **Render**: no Dashboard do serviço, vá em **Environment** e adicione:
  - **Key:** `MAIL_PASSWORD`
  - **Value:** a senha de 16 caracteres (pode colar com ou sem espaços; o Spring remove espaços ao usar).
- **Rancher / Kubernetes**: defina a variável de ambiente (ou Secret) `MAIL_PASSWORD` com o mesmo valor.
- **Local**: em `application-local.properties` ou variável de ambiente:
  - `MAIL_PASSWORD=suasenha16caracteres`

Depois de salvar, **reinicie a aplicação** para carregar a nova variável.

## Conferir se está configurado

**Ao subir a aplicação**, os logs devem mostrar uma destas mensagens:

- **Configurado:** `Notificação e-mail: CONFIGURADO (remetente: cjlconsultoria.consultor@gmail.com). Os e-mails serão enviados.`
- **Não configurado:** `Notificação e-mail: NÃO CONFIGURADO. Defina MAIL_PASSWORD no ambiente...`

Ao **tentar enviar** um e-mail (ex.: novo usuário, nova recomendação):

- Se **MAIL_PASSWORD** estiver vazio: `E-mail não configurado ... ignorando envio para xxx@...`
- Se a **senha estiver errada** (ou não for Senha de app): `Falha ao enviar e-mail HTML para xxx@...: AuthenticationFailedException - ...`
- Se **enviou com sucesso**: `E-mail HTML enviado para xxx@... (assunto: ...)`

**Se o log mostra "E-mail HTML enviado" mas o e-mail não chega na caixa de entrada:** verifique a **pasta de spam/lixo eletrônico** e se o endereço do destinatário está correto. O remetente aparece como "TradeLink" para ajudar na entrega.

Nesses casos, confira:

1. Verificação em duas etapas ativada na conta Google.
2. Senha de app gerada para **cjlconsultoria.consultor@gmail.com**.
3. Variável **MAIL_PASSWORD** definida no ambiente e aplicação reiniciada.

## Resumo

| Onde           | O que fazer |
|----------------|-------------|
| **Gmail**      | Ativar verificação em 2 etapas e criar **Senha de app** em https://myaccount.google.com/apppasswords |
| **Render**     | Environment → `MAIL_PASSWORD` = senha de 16 caracteres → reiniciar o serviço |
| **Rancher**   | Definir `MAIL_PASSWORD` no deployment/Secret e reiniciar o pod |

Não é necessário habilitar "Acesso a apps menos seguros" — isso foi descontinuado pelo Google; use sempre **Senha de app**.

---

## E-mail no Render: timeout ao conectar no Gmail

Se nos logs aparecer **"Mail server connection failed ... Operation timed out"** ao enviar e-mail, o Render está **bloqueando a porta SMTP 587**. Use **Resend** (seção **Configurar e-mail com Resend (Render)** acima); o TradeLink já está preparado para isso.

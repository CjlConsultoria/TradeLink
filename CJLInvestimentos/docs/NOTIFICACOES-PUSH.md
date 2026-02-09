# Notificações Push no navegador

O sistema já envia **push no navegador** quando o consultor cria uma nova recomendação: os clientes da carteira recebem a notificação na hora (consultor na casa dele, cliente na dele, ambos logados ou não — o push chega mesmo com a aba fechada, desde que o cliente tenha ativado).

## Como funciona

1. O **consultor** cria uma recomendação (carteira X).
2. O **backend** identifica os clientes da carteira X e, para cada um, envia push para todas as inscrições (push subscriptions) salvas.
3. O **navegador do cliente** recebe a mensagem (via Service Worker) e exibe a notificação do sistema (ex.: "Nova recomendação – BTC/BRL – Entrada: ...").

## O que você precisa fazer para ativar

### 1. Gerar chaves VAPID (uma vez)

No seu computador, na pasta do projeto (ou qualquer pasta com Node):

```bash
npx web-push generate-vapid-keys
```

Serão exibidas duas chaves: **public** e **private**.

### 2. Configurar no backend (Render)

No **Render** → serviço do **backend** → **Environment**:

| Variável        | Valor                          |
|-----------------|--------------------------------|
| `VAPID_PUBLIC`  | a chave **public** gerada      |
| `VAPID_PRIVATE` | a chave **private** gerada     |

Faça um novo deploy do backend após salvar.

### 3. Habilitar Push na empresa

- **AdminMax** → **Empresas** → selecionar a empresa → garantir que **Push** (notificações no navegador) está **habilitado** (já vem habilitado por padrão).

### 4. Cliente ativar no navegador

- O **cliente** entra no sistema → **Configurações** → seção **"Notificações no navegador"** → clica em **"Ativar notificações no navegador"**.
- O navegador pede permissão; ao aceitar, a inscrição é salva no backend.

Depois disso, sempre que o consultor criar uma recomendação em uma carteira em que o cliente está, o cliente recebe o push na hora (no desktop ou no celular, se estiver usando o mesmo navegador e tiver ativado as notificações).

## Observações

- **HTTPS**: Push só funciona em HTTPS (ou localhost). No Render já é HTTPS.
- **Chave pública no frontend**: O frontend já usa a chave pública retornada por `GET /api/me/config-notificacao` (vapidPublicKey) para registrar a inscrição; não é preciso configurar nada extra no front.
- **E-mail e Telegram**: O mesmo fluxo de “nova recomendação” pode enviar e-mail e Telegram se a empresa tiver esses canais configurados.

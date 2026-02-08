# Integração de pagamentos (Stripe) – TradeLink

As empresas podem pagar o plano mensal via **Stripe**: **cartão** (Checkout) ou **PIX**. O fluxo usa **Stripe Checkout** para cartão e **PaymentIntent PIX** para PIX; **webhooks** atualizam o status e o histórico de faturas.

- **PIX**: consultor ou cliente acessa **Faturas** → "Pagar com PIX" → backend gera um PaymentIntent PIX → o frontend pode exibir o QR Code (Stripe.js) ou o usuário paga pelo app do banco; ao confirmar, o webhook `payment_intent.succeeded` registra a fatura e libera o acesso.
- **Cartão**: AdminMax em **Empresas** → "Pagar plano" → redireciona ao Stripe Checkout (assinatura recorrente).
- **Outra forma**: AdminMax em **Detalhe da empresa** → **Faturas** → "Marcar como pago (outra forma)" → registra pagamento manual e avança o período em 30 dias.

**Regra de bloqueio**: se a empresa não pagar até **5 dias após o vencimento**, o acesso de **consultores e clientes** dessa empresa é bloqueado até a regularização. AdminMax continua com acesso total.

## Visão geral

1. **AdminMax** cadastra planos e opcionalmente informa o **Stripe Price ID** (preço recorrente mensal no Stripe).
2. **AdminMax** atribui um plano à empresa e pode clicar em **"Pagar plano"** na listagem de empresas.
3. O usuário é redirecionado ao **Stripe Checkout** para informar cartão e concluir a assinatura.
4. Após o pagamento, o **Stripe** envia eventos para o **webhook** do backend, que atualiza a empresa (status da assinatura, fim do período, etc.).

## Configuração no backend

### 1. Variáveis em `application.properties`

```properties
# Chave secreta do Stripe (Dashboard → Developers → API keys). Use sk_test_... em desenvolvimento.
stripe.api-key=sk_test_...

# Segredo do webhook (Dashboard → Developers → Webhooks → Add endpoint → Signing secret)
stripe.webhook-secret=whsec_...

# URLs para onde o Stripe redireciona após sucesso ou cancelamento
stripe.success-url=http://localhost:5173/admin-max/empresas
stripe.cancel-url=http://localhost:5173/admin-max/empresas
```

Em produção, use `sk_live_...` e URLs reais (ex: `https://seu-dominio.com/admin-max/empresas`).

### 2. Criar produto e preço no Stripe

1. Acesse [Stripe Dashboard → Products](https://dashboard.stripe.com/products).
2. Crie um **Product** (ex: "TradeLink - Plano Básico").
3. Adicione um **Price** ao produto:
   - **Recurring**: Monthly.
   - Valor em BRL ou USD conforme sua conta.
4. Copie o **Price ID** (começa com `price_...`).
5. No TradeLink, em **Planos** → Editar o plano → cole o **Stripe Price ID** e salve.

### 3. Configurar o webhook no Stripe

1. [Stripe Dashboard → Webhooks](https://dashboard.stripe.com/webhooks) → **Add endpoint**.
2. **Endpoint URL**: `https://seu-dominio.com/api/webhooks/stripe`  
   (em local: use [Stripe CLI](https://stripe.com/docs/stripe-cli) para encaminhar: `stripe listen --forward-to localhost:8080/api/webhooks/stripe`).
3. **Eventos** a assinar (obrigatórios para gerar fatura ao pagar):
   - `checkout.session.completed` (cartão/boleto via Checkout – gera fatura quando pago)
   - `payment_intent.succeeded` (PIX e boleto pago depois – gera fatura)
   - `customer.subscription.updated`
   - `customer.subscription.deleted`
4. Após criar, copie o **Signing secret** (`whsec_...`) e configure em `stripe.webhook-secret`.  
   **Sem `stripe.webhook-secret` o webhook é ignorado e nenhuma fatura é criada automaticamente.**

## Endpoints da API

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/admin-max/empresas/{id}/checkout` | Cria sessão de checkout; body: `{ "planoId": 1 }`. Retorna `{ "checkoutUrl": "https://checkout.stripe.com/..." }`. |
| GET | `/api/admin-max/pagamentos/configurado` | Retorna `{ "configurado": true/false }` conforme `stripe.api-key`. |
| POST | `/api/webhooks/stripe` | Webhook do Stripe (não requer autenticação; validado pela assinatura). |

## Status da assinatura (empresa)

- **NONE**: Sem assinatura ativa (plano pode ter sido atribuído manualmente).
- **ACTIVE**: Assinatura paga e ativa.
- **PAST_DUE**: Pagamento atrasado ou falhou.
- **CANCELLED**: Assinatura cancelada.
- **TRIAL**: Período de trial.

O frontend exibe o status e a data de próxima cobrança (`currentPeriodEnd`) na listagem e no detalhe da empresa.

## Alternativas: Mercado Pago e outros

Para aceitar **PIX**, **boleto** ou métodos locais no Brasil, você pode integrar em paralelo o **Mercado Pago**:

1. **Assinaturas Mercado Pago**: criar plano recorrente na API do MP e, no backend, ter um serviço similar a `StripePaymentService` (ex: `MercadoPagoPaymentService`) que cria preferência de assinatura e processa notificações IPN/webhook.
2. **Campos na empresa**: pode reutilizar os mesmos campos de status (`subscriptionStatus`, `currentPeriodEnd`) e adicionar, por exemplo, `mpSubscriptionId` para referência ao MP.
3. **Frontend**: botão "Pagar com PIX/Boleto" que chama o endpoint de checkout do MP e redireciona ou exibe QR Code.

A estrutura atual (status na empresa, webhook, checkout por plano) foi pensada para permitir adicionar outro gateway sem quebrar o fluxo existente.

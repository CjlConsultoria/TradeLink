# TradeLink – Backend (CJL Investimentos)

API Spring Boot para a plataforma TradeLink (empresas, planos, consultores, clientes, recomendações, faturas, pagamentos Stripe).

## Executar em desenvolvimento

- PostgreSQL em `localhost:5432`, banco `cjlinvestimentos`, usuário/senha em `application.properties`.
- `./mvnw spring-boot:run`
- Na primeira subida, se não existir usuário AdminMax, é criado `admin@tradelink.local` com senha gerada (veja o log).

## Docker e deploy

- **Build:** `docker build -t tradelink-backend .`
- **Deploy no Rancher:** ver [docs/DEPLOY-RANCHER.md](docs/DEPLOY-RANCHER.md) (PostgreSQL, variáveis de ambiente, primeiro admin).

## Documentação

- [docs/DEPLOY-RANCHER.md](docs/DEPLOY-RANCHER.md) – Deploy no Rancher, variáveis de ambiente, primeiro acesso admin.
- [docs/PAGAMENTOS-STRIPE.md](docs/PAGAMENTOS-STRIPE.md) – Stripe (Checkout, PIX, webhooks).

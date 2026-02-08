# Deploy no Rancher (TradeLink Backend)

## Visão geral

- O **PostgreSQL** você sobe no Rancher (ou usa um serviço gerenciado).
- A **aplicação** cria as tabelas automaticamente na primeira subida (`spring.jpa.hibernate.ddl-auto=update`).
- Na **primeira subida**, se não existir nenhum usuário AdminMax, é criado um admin:
  - Se você definir **ADMIN_INITIAL_EMAIL** e **ADMIN_INITIAL_PASSWORD** no ambiente, esse usuário é criado com essa senha.
  - Se **não** definir, é criado `admin@tradelink.local` com uma **senha aleatória** (aparece **uma vez** no log da aplicação – procure por "PRIMEIRO ACESSO ADMIN").

## 1. Banco PostgreSQL no Rancher

Crie um deployment/serviço PostgreSQL (ou use um banco externo) e anote:

- Host e porta (ex.: `postgres.default.svc.cluster.local:5432`)
- Nome do banco (ex.: `tradelink`)
- Usuário e senha

Exemplo de URL JDBC:

```text
jdbc:postgresql://postgres.default.svc.cluster.local:5432/tradelink
```

## 2. Build da imagem Docker

No repositório do backend (CJLInvestimentos):

```bash
docker build -t seu-registry/tradelink-backend:latest .
docker push seu-registry/tradelink-backend:latest
```

(Substitua `seu-registry` pelo seu registry configurado no Rancher.)

## 3. Variáveis de ambiente no Rancher

No deployment do **backend** no Rancher, configure as variáveis de ambiente:

| Variável | Obrigatório | Exemplo | Descrição |
|----------|-------------|---------|-----------|
| `SPRING_PROFILES_ACTIVE` | Sim (prod) | `production` | Usa `application-production.properties`. |
| `SPRING_DATASOURCE_URL` | Sim | `jdbc:postgresql://postgres:5432/tradelink` | URL do PostgreSQL. |
| `SPRING_DATASOURCE_USERNAME` | Sim | `tradelink` | Usuário do banco. |
| `SPRING_DATASOURCE_PASSWORD` | Sim | (secret) | Senha do banco. |
| `ADMIN_INITIAL_EMAIL` | Recomendado | `admin@seudominio.com` | E-mail do primeiro admin. |
| `ADMIN_INITIAL_PASSWORD` | Recomendado | (senha forte) | Senha do primeiro admin (evita senha no log). |
| `APP_CORS_ALLOWED_ORIGINS` | Sim (se front separado) | `https://app.seudominio.com` | Origens CORS do frontend. |

**Primeiro acesso admin:**

- Se **ADMIN_INITIAL_EMAIL** e **ADMIN_INITIAL_PASSWORD** estiverem definidos: use esse e-mail e senha para fazer login como Super Admin.
- Se **não** estiverem definidos: na primeira subida, procure no log da aplicação a mensagem **"PRIMEIRO ACESSO ADMIN"** e use o e-mail e a senha exibidos (troque a senha após o primeiro login).

## 4. Porta e health

- A aplicação expõe a porta **8080**.
- Configure no Rancher o uso da porta 8080 para o serviço e, se quiser, um probe de readiness em `/api/auth/...` ou em um endpoint de health (se existir).

## 5. Resumo do fluxo

1. Subir PostgreSQL no Rancher (ou apontar para um banco existente).
2. Criar no Rancher um Deployment usando a imagem `tradelink-backend`.
3. Definir as variáveis de ambiente acima (incluindo `SPRING_PROFILES_ACTIVE=production` e as do datasource).
4. Na primeira subida, a aplicação:
   - Conecta no banco e **cria/atualiza as tabelas**.
   - Cria o **primeiro usuário AdminMax** (com o e-mail/senha definidos ou com os valores logados).
5. Acessar o frontend e fazer login com o usuário admin criado.

## 6. Stripe (opcional)

Para pagamentos em produção, configure no ambiente (ou em um ConfigMap/Secret) as variáveis Stripe, por exemplo:

- `STRIPE_API_KEY`
- `STRIPE_WEBHOOK_SECRET`
- `STRIPE_SUCCESS_URL`, `STRIPE_CANCEL_URL`, etc.

Consulte `PAGAMENTOS-STRIPE.md` para detalhes.

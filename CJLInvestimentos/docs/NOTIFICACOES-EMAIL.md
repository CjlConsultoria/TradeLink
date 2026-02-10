# Notificações por e-mail

O sistema envia e-mails em **HTML responsivo** (layout único para todos os clientes de e-mail) nos seguintes eventos:

| Evento | Quem recebe | Assunto |
|--------|-------------|---------|
| **Novo usuário** | O próprio usuário (registro ou criação por consultor) | Bem-vindo ao TradeLink |
| **Senha alterada** | O usuário que alterou a senha | Senha alterada - TradeLink |
| **Nova recomendação** | Clientes da carteira (se a empresa tiver e-mail habilitado) | Nova recomendação |
| **Cliente resolveu** | Consultor da carteira (se a empresa tiver e-mail habilitado) | Cliente resolveu recomendação |

## Configuração

### Render (produção)

O remetente e o SMTP já estão em `application-render.properties` (Gmail).  
Defina no **Render** → Backend → **Environment**:

- **`MAIL_PASSWORD`** = senha do e-mail `cjlconsultoria.consultor@gmail.com`  
  - Se a conta tiver **verificação em duas etapas**, use uma **senha de app**:  
    [Google – Senhas de app](https://myaccount.google.com/apppasswords)

### Local

Copie o trecho de e-mail do `application-local.properties.example` para `application-local.properties` e preencha:

- `spring.mail.password` (ou use variável `MAIL_PASSWORD` no ambiente)

## Templates

Os templates são montados em `EmailTemplateService` (layout base + conteúdo por tipo).  
O layout é responsivo (table-based para compatibilidade com clientes de e-mail) e usa:

- Cabeçalho com gradiente (roxo/indigo)
- Corpo em branco com boa legibilidade
- Botões de ação (login, dashboard)
- Rodapé discreto

Nenhuma senha ou dado sensível fica nos arquivos do repositório; a senha do e-mail deve ser definida apenas em variável de ambiente (ex.: `MAIL_PASSWORD` no Render).

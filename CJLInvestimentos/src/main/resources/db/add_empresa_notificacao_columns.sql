-- Colunas de notificação na tabela tb_empresas (execute no PostgreSQL se as colunas não existirem)
-- Pode rodar: psql -U postgres -d cjlinvestimentos -f add_empresa_notificacao_columns.sql

ALTER TABLE tb_empresas ADD COLUMN IF NOT EXISTS notificacao_email boolean NOT NULL DEFAULT true;
ALTER TABLE tb_empresas ADD COLUMN IF NOT EXISTS notificacao_telegram boolean NOT NULL DEFAULT true;
ALTER TABLE tb_empresas ADD COLUMN IF NOT EXISTS notificacao_push boolean NOT NULL DEFAULT true;
ALTER TABLE tb_empresas ADD COLUMN IF NOT EXISTS notificacao_whatsapp boolean NOT NULL DEFAULT false;
ALTER TABLE tb_empresas ADD COLUMN IF NOT EXISTS notificacao_sms boolean NOT NULL DEFAULT false;

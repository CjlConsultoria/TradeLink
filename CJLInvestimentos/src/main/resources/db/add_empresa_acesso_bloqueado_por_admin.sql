-- Coluna para bloqueio de acesso pela plataforma (AdminMax). Execute se a coluna não existir.
-- Com spring.jpa.hibernate.ddl-auto=update a coluna é criada automaticamente.

ALTER TABLE tb_empresas ADD COLUMN IF NOT EXISTS acesso_bloqueado_por_admin boolean NOT NULL DEFAULT false;

package com.example.CJLInvestimentos.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Executa migrações SQL ao iniciar a aplicação (ex.: colunas novas no Render onde o ddl-auto pode não ter rodado).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
@Slf4j
public class DbMigrationRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        try {
            jdbcTemplate.execute(
                "ALTER TABLE tb_empresas ADD COLUMN IF NOT EXISTS acesso_bloqueado_por_admin boolean NOT NULL DEFAULT false"
            );
            log.debug("Migração: coluna acesso_bloqueado_por_admin verificada/criada.");
        } catch (Exception e) {
            log.warn("Migração acesso_bloqueado_por_admin: {} (pode já existir ou ser outro banco)", e.getMessage());
        }

        try {
            jdbcTemplate.execute("ALTER TABLE tb_carteiras ADD COLUMN IF NOT EXISTS margem_erro numeric(5,2)");
            jdbcTemplate.execute("ALTER TABLE tb_carteiras ADD COLUMN IF NOT EXISTS moeda_referencia_rebalance varchar(10)");
            jdbcTemplate.execute("ALTER TABLE tb_carteiras ADD COLUMN IF NOT EXISTS rebalance_ativo boolean");
            log.debug("Migração: colunas de rebalanceamento verificadas/criadas.");
        } catch (Exception e) {
            log.warn("Migração rebalanceamento: {}", e.getMessage());
        }

        // Sistema de convite / ativação de conta
        try {
            jdbcTemplate.execute("ALTER TABLE tb_usuarios ADD COLUMN IF NOT EXISTS cpf varchar(14) UNIQUE");
            jdbcTemplate.execute("ALTER TABLE tb_usuarios ADD COLUMN IF NOT EXISTS whatsapp varchar(30)");
            jdbcTemplate.execute("ALTER TABLE tb_usuarios ADD COLUMN IF NOT EXISTS cep varchar(10)");
            jdbcTemplate.execute("ALTER TABLE tb_usuarios ADD COLUMN IF NOT EXISTS logradouro varchar(200)");
            jdbcTemplate.execute("ALTER TABLE tb_usuarios ADD COLUMN IF NOT EXISTS numero varchar(20)");
            jdbcTemplate.execute("ALTER TABLE tb_usuarios ADD COLUMN IF NOT EXISTS complemento varchar(100)");
            jdbcTemplate.execute("ALTER TABLE tb_usuarios ADD COLUMN IF NOT EXISTS bairro varchar(100)");
            jdbcTemplate.execute("ALTER TABLE tb_usuarios ADD COLUMN IF NOT EXISTS cidade varchar(100)");
            jdbcTemplate.execute("ALTER TABLE tb_usuarios ADD COLUMN IF NOT EXISTS estado varchar(2)");
            jdbcTemplate.execute("ALTER TABLE tb_usuarios ADD COLUMN IF NOT EXISTS termo_aceito boolean");
            jdbcTemplate.execute("ALTER TABLE tb_usuarios ADD COLUMN IF NOT EXISTS termo_aceito_em timestamp");
            jdbcTemplate.execute("ALTER TABLE tb_usuarios ADD COLUMN IF NOT EXISTS token_convite varchar(128) UNIQUE");
            jdbcTemplate.execute("ALTER TABLE tb_usuarios ADD COLUMN IF NOT EXISTS token_convite_expiracao timestamp");
            log.debug("Migração: colunas de convite/ativação verificadas/criadas.");
        } catch (Exception e) {
            log.warn("Migração convite/ativação: {}", e.getMessage());
        }

        // Sistema de exclusão / auto-gestão
        try {
            jdbcTemplate.execute("ALTER TABLE tb_usuarios ADD COLUMN IF NOT EXISTS auto_gestao boolean NOT NULL DEFAULT false");
            jdbcTemplate.execute("ALTER TABLE tb_usuarios ADD COLUMN IF NOT EXISTS relatorio_compl_baixado boolean NOT NULL DEFAULT false");
            jdbcTemplate.execute("ALTER TABLE tb_usuarios ADD COLUMN IF NOT EXISTS relatorio_compl_baixado_em timestamp");
            jdbcTemplate.execute("ALTER TABLE tb_usuarios ADD COLUMN IF NOT EXISTS data_exclusao timestamp");
            jdbcTemplate.execute("ALTER TABLE tb_usuarios ADD COLUMN IF NOT EXISTS stripe_customer_id varchar(128)");
            jdbcTemplate.execute("ALTER TABLE tb_usuarios ADD COLUMN IF NOT EXISTS current_period_end timestamp with time zone");
            jdbcTemplate.execute("ALTER TABLE tb_usuarios ADD COLUMN IF NOT EXISTS subscription_status varchar(20) DEFAULT 'NONE'");
            jdbcTemplate.execute("ALTER TABLE tb_faturas ALTER COLUMN empresa_id DROP NOT NULL");
            jdbcTemplate.execute("ALTER TABLE tb_faturas ADD COLUMN IF NOT EXISTS user_id bigint REFERENCES tb_usuarios(id)");
            log.debug("Migração: colunas de auto-gestão/exclusão verificadas/criadas.");
        } catch (Exception e) {
            log.warn("Migração auto-gestão/exclusão: {}", e.getMessage());
        }

        // Sistema de auto-cadastro / trial
        try {
            jdbcTemplate.execute("ALTER TABLE tb_usuarios ADD COLUMN IF NOT EXISTS trial_inicio timestamp");
            jdbcTemplate.execute("ALTER TABLE tb_usuarios ADD COLUMN IF NOT EXISTS trial_fim timestamp");
            jdbcTemplate.execute("ALTER TABLE tb_usuarios ADD COLUMN IF NOT EXISTS auto_cadastro boolean NOT NULL DEFAULT false");
            jdbcTemplate.execute("ALTER TABLE tb_usuarios ADD COLUMN IF NOT EXISTS cnpj varchar(18)");
            jdbcTemplate.execute("ALTER TABLE tb_empresas ADD COLUMN IF NOT EXISTS trial_inicio timestamp");
            jdbcTemplate.execute("ALTER TABLE tb_empresas ADD COLUMN IF NOT EXISTS trial_fim timestamp");
            jdbcTemplate.execute("ALTER TABLE tb_empresas ADD COLUMN IF NOT EXISTS auto_cadastro boolean NOT NULL DEFAULT false");
            log.debug("Migração: colunas de auto-cadastro/trial verificadas/criadas.");
        } catch (Exception e) {
            log.warn("Migração auto-cadastro/trial: {}", e.getMessage());
        }

        // FAQ
        try {
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS tb_faqs (
                    id bigserial PRIMARY KEY,
                    pergunta text NOT NULL,
                    resposta text NOT NULL,
                    categoria varchar(100),
                    ordem integer NOT NULL DEFAULT 0,
                    ativo boolean NOT NULL DEFAULT true,
                    created_at timestamp NOT NULL DEFAULT now(),
                    updated_at timestamp NOT NULL DEFAULT now()
                )
            """);
            log.debug("Migração: tabela tb_faqs verificada/criada.");
        } catch (Exception e) {
            log.warn("Migração FAQ: {}", e.getMessage());
        }

        // Chat
        try {
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS tb_chat_conversas (
                    id bigserial PRIMARY KEY,
                    user_id bigint NOT NULL REFERENCES tb_usuarios(id),
                    status varchar(20) NOT NULL DEFAULT 'ABERTA',
                    assunto varchar(200),
                    created_at timestamp NOT NULL DEFAULT now(),
                    updated_at timestamp NOT NULL DEFAULT now()
                )
            """);
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS tb_chat_mensagens (
                    id bigserial PRIMARY KEY,
                    conversa_id bigint NOT NULL REFERENCES tb_chat_conversas(id),
                    remetente_id bigint NOT NULL REFERENCES tb_usuarios(id),
                    conteudo text NOT NULL,
                    lida boolean NOT NULL DEFAULT false,
                    created_at timestamp NOT NULL DEFAULT now()
                )
            """);
            log.debug("Migração: tabelas de chat verificadas/criadas.");
        } catch (Exception e) {
            log.warn("Migração chat: {}", e.getMessage());
        }

        // Plano tipo (CONSULTOR / AUTO_GESTAO) + seed plano auto-gestão
        try {
            jdbcTemplate.execute("ALTER TABLE tb_planos ADD COLUMN IF NOT EXISTS tipo varchar(20) NOT NULL DEFAULT 'CONSULTOR'");
            // Cria plano Auto-Gestão se não existir
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tb_planos WHERE tipo = 'AUTO_GESTAO'", Integer.class
            );
            if (count == null || count == 0) {
                jdbcTemplate.execute("""
                    INSERT INTO tb_planos (nome, tipo, max_usuarios, preco, ativo, created_at)
                    VALUES ('Auto-Gestão', 'AUTO_GESTAO', 1, 9.99, true, now())
                """);
                log.info("Migração: plano Auto-Gestão criado com preço R$9,99.");
            }
            log.debug("Migração: coluna tipo em tb_planos verificada/criada.");
        } catch (Exception e) {
            log.warn("Migração plano tipo: {}", e.getMessage());
        }
    }
}

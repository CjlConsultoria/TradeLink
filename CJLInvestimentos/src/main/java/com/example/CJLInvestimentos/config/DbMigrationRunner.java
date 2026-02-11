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
    }
}

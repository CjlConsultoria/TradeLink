package com.example.CJLInvestimentos.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteExcluidoStatusResponse {
    /** true se empresa_id é null e ativo=true. */
    private boolean excluido;
    /** true se autoGestao=true e subscription ativa. */
    private boolean autoGestaoAtiva;
    /** true se já baixou o relatório gratuito. */
    private boolean relatorioGratisBaixado;
    private LocalDateTime dataExclusao;
    private String nomeCliente;
    private String cpf;
    /** Preço mensal auto-gestão (ex.: 9.99). */
    private BigDecimal precoAutoGestao;
    /** Preço para re-download do relatório (ex.: 19.90). */
    private BigDecimal precoRelatorio;
}

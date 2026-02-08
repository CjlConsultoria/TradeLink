package com.example.CJLInvestimentos.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProximaFaturaResponse {
    /** Data de vencimento (30 dias após última cobrança ou assinatura). */
    private Instant dataVencimento;
    private BigDecimal valor;
    private String planoNome;
    /** Se já existe assinatura (tem período vigente). */
    private boolean temAssinatura;
}

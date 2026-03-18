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
public class FaturaResponse {
    private Long id;
    private Long empresaId;
    private Instant dataVencimento;
    private Instant dataPagamento;
    private BigDecimal valor;
    private String status;
    private String formaPagamento;
    private String descricaoServico;
    private String observacao;
    /** ID do usuário individual (para faturas de auto-gestão, quando empresaId é null). */
    private Long userId;
}

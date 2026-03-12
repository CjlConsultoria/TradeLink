package com.example.CJLInvestimentos.dtos.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FaturaAdminResponse {
    private Long id;
    private Long empresaId;
    private String empresaNome;
    private Instant dataVencimento;
    private Instant dataPagamento;
    private BigDecimal valor;
    private String status;
    private String formaPagamento;
    private String descricaoServico;
    private String observacao;
}

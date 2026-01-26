package com.example.CJLInvestimentos.dtos.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class AcaoFinanceiraResponse {

    private Long id;
    private String ativo;
    private BigDecimal valor;
    private Integer quantidadeAcoes;
    private LocalDate data;
    private BigDecimal lucroPrejuizo;
}

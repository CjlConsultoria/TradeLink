package com.example.CJLInvestimentos.dtos.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AcaoFinanceiraRequest {

    private String ativo;
    private BigDecimal valor;
    private Integer quantidadeAcoes;
    private LocalDate data;
    private BigDecimal lucroPrejuizo;
}

package com.example.CJLInvestimentos.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MovimentacaoRequest {
    @NotBlank
    private String tipo; // APORTE ou SAQUE

    @NotNull
    @Positive
    private BigDecimal valor;

    private String moeda; // default USD

    @NotBlank
    private String dataMovimentacao; // yyyy-MM-dd

    private String observacao;
}

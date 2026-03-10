package com.example.CJLInvestimentos.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AlocacaoAlvoRequest {
    @NotBlank
    private String simbolo;
    @NotBlank
    private String nome;
    @NotNull
    private BigDecimal percentualAlvo;
    private String parMoedaReferencia;
}

package com.example.CJLInvestimentos.dtos.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SalvarAlocacoesRequest {
    @NotNull
    private List<AlocacaoAlvoRequest> alocacoes;
    private BigDecimal margemErro;
    private String moedaReferencia;
}

package com.example.CJLInvestimentos.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioResumoResponse {
    private Long clienteId;
    private String clienteNome;
    private BigDecimal valorTotalPortfolio;
    private String moedaReferencia;
    private List<AtivoClienteResponse> ativos;
}

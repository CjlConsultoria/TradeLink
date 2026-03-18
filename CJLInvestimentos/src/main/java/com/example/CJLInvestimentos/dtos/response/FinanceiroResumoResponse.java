package com.example.CJLInvestimentos.dtos.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinanceiroResumoResponse {

    private BigDecimal receitaTotal;
    private BigDecimal receitaMesAtual;

    private long faturasPendentesCount;
    private BigDecimal faturasPendentesValor;

    private long faturasVencidasCount;
    private BigDecimal faturasVencidasValor;

    /** Monthly Recurring Revenue: soma dos preços dos planos de empresas com assinatura ativa. */
    private BigDecimal mrr;

    /** Receita mensal dos últimos 12 meses. */
    private List<ReceitaMensal> receitaMensal;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReceitaMensal {
        private String mes; // "2026-01", "2026-02", etc.
        private BigDecimal valor;
        private long count;
    }
}

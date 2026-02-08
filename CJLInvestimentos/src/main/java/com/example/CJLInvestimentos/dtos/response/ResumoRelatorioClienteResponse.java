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
public class ResumoRelatorioClienteResponse {
    private Long totalOperacoes;
    private Long totalCompras;
    private Long totalVendas;
    /** Soma do valor (preço × quantidade) de todas as compras. */
    private BigDecimal valorTotalCompras;
    /** Soma do valor (preço × quantidade) de todas as vendas. */
    private BigDecimal valorTotalVendas;
    /** resultado = valorTotalVendas - valorTotalCompras (ganho se positivo, perda se negativo). */
    private BigDecimal resultado;
    private List<PerdaGanhoPorMoeda> perdasGanhosPorMoeda;
    private List<OperacoesPorPeriodo> operacoesPorPeriodo;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerdaGanhoPorMoeda {
        private String moedaPar;
        private BigDecimal valorTotalCompras;
        private BigDecimal valorTotalVendas;
        private BigDecimal resultado;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OperacoesPorPeriodo {
        private String periodo;
        private Long total;
    }
}

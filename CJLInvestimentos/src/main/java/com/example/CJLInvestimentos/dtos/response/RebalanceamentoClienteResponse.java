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
public class RebalanceamentoClienteResponse {
    private Long clienteId;
    private String clienteNome;
    private BigDecimal valorTotalPortfolio;
    private String moedaReferencia;
    private String statusSaude;
    private List<AtivoRebalanceamento> ativos;
    private List<AcaoRebalanceamento> acoesSugeridas;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AtivoRebalanceamento {
        private String simbolo;
        private String nome;
        private BigDecimal quantidade;
        private BigDecimal precoAtual;
        private BigDecimal valorUsd;
        private BigDecimal percentualAtual;
        private BigDecimal percentualAlvo;
        private BigDecimal diferencaPercentual;
        private Boolean comprar;
        private Boolean vender;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AcaoRebalanceamento {
        private String tipo;
        private String simbolo;
        private BigDecimal quantidade;
        private BigDecimal valorUsd;
        private String descricao;
    }
}

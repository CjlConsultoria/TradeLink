package com.example.CJLInvestimentos.dtos.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecomendacaoImpactoResponse {

    private Long recomendacaoId;
    private String tipo;
    private String moeda;
    private String parMoeda;
    private BigDecimal precoEntrada;
    private BigDecimal precoAlvo;
    private BigDecimal quantidade;
    private BigDecimal percentualAlvo;
    private String observacao;
    private String carteiraNome;
    private BigDecimal margemErro;
    private String moedaReferencia;
    private List<ClienteImpacto> clientes;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClienteImpacto {
        private Long clienteId;
        private String clienteNome;
        private BigDecimal valorTotalPortfolio;

        /** Status atual (OK / ATENCAO / CRITICO) */
        private String statusAntes;
        /** Status projetado apos executar a recomendacao */
        private String statusDepois;

        /** Ativos atuais com percentual atual vs ideal */
        private List<AtivoImpacto> ativosAntes;
        /** Ativos projetados apos executar */
        private List<AtivoImpacto> ativosDepois;

        /** Quantidade especifica que este cliente deveria executar */
        private BigDecimal quantidadeCliente;
        private BigDecimal valorEstimado;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AtivoImpacto {
        private String simbolo;
        private BigDecimal quantidade;
        private BigDecimal valorUsd;
        private BigDecimal percentualAtual;
        private BigDecimal percentualAlvo;
        private BigDecimal diferencaPercentual;
        private Boolean comprar;
        private Boolean vender;
    }
}

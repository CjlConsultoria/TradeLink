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
public class AnaliseConsolidadaResponse {
    private List<ClienteConsolidado> clientes;
    private Resumo resumo;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClienteConsolidado {
        private Long clienteId;
        private String clienteNome;
        private Long carteiraId;
        private String carteiraNome;
        private BigDecimal valorTotalPortfolio;
        private String moedaReferencia;
        private String statusSaude;
        private List<RebalanceamentoClienteResponse.AtivoRebalanceamento> ativos;
        private List<RebalanceamentoClienteResponse.AcaoRebalanceamento> acoesSugeridas;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Resumo {
        private int totalClientes;
        private int totalDesbalanceados;
        private int totalCriticos;
        private BigDecimal valorTotalGeral;
    }
}

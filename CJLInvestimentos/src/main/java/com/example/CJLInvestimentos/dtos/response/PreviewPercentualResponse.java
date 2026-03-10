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
public class PreviewPercentualResponse {

    private String moeda;
    private String parMoeda;
    private BigDecimal percentual;
    private List<ClientePreview> clientes;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClientePreview {
        private Long clienteId;
        private String clienteNome;
        private BigDecimal quantidadeAtivo;
        private BigDecimal quantidadeCalculada;
        private BigDecimal precoAtual;
        private BigDecimal valorEstimado;
    }
}

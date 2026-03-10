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
public class SaudeGeralResponse {
    private List<CarteiraHealth> carteiras;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CarteiraHealth {
        private Long carteiraId;
        private String carteiraNome;
        private boolean temAlocacoes;
        private List<ClienteHealth> clientes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClienteHealth {
        private Long clienteId;
        private String clienteNome;
        private BigDecimal valorTotal;
        private String status;
        private int totalDesbalanceados;
        private BigDecimal maiorDesvio;
    }
}

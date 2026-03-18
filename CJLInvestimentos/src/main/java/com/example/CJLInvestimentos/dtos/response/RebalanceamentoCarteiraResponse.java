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
public class RebalanceamentoCarteiraResponse {
    private Long carteiraId;
    private String carteiraNome;
    private BigDecimal margemErro;
    private String moedaReferencia;
    private List<AlocacaoAlvoResponse> alocacoesAlvo;
    private List<RebalanceamentoClienteResponse> clientes;
}

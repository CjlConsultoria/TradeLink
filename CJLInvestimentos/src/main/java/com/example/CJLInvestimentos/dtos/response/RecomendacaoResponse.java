package com.example.CJLInvestimentos.dtos.response;

import com.example.CJLInvestimentos.entities.enums.StatusRecomendacao;
import com.example.CJLInvestimentos.entities.enums.TipoRecomendacao;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecomendacaoResponse {
    private Long id;
    private Long carteiraId;
    private String carteiraNome;
    private TipoRecomendacao tipo;
    private String moeda;
    private String parMoeda;
    private BigDecimal precoEntrada;
    private BigDecimal precoAlvo;
    private BigDecimal stopLoss;
    private BigDecimal quantidade;
    private BigDecimal percentual;
    private Boolean modoPercentual;
    private BigDecimal quantidadeCalculadaCliente;
    private BigDecimal valorEstimadoCliente;
    private StatusRecomendacao status;
    private String observacao;
    private BigDecimal cotacaoAtual;
    private Integer totalOperacoesClientes;
    /** Se o cliente logado marcou esta recomendação como resolvida (apenas quando listada para cliente). */
    private Boolean resolvido;
    private LocalDateTime resolvidoEm;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

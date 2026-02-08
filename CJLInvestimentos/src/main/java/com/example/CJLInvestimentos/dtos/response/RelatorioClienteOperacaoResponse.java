package com.example.CJLInvestimentos.dtos.response;

import com.example.CJLInvestimentos.entities.enums.TipoOperacao;
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
public class RelatorioClienteOperacaoResponse {
    private Long id;
    private Long recomendacaoId;
    private String recomendacaoMoedaPar;
    private Long carteiraId;
    private String carteiraNome;
    private TipoOperacao tipo;
    private BigDecimal precoExecutado;
    private BigDecimal quantidade;
    private LocalDateTime dataExecucao;
    private String observacao;
    private LocalDateTime createdAt;
    private BigDecimal precoEntradaRecomendacao;
    private BigDecimal valorOperacao;
}

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
public class RelatorioConsultorResponse {
    private Long id;
    private Long recomendacaoId;
    private String recomendacaoMoedaPar;
    private Long carteiraId;
    private String carteiraNome;
    private Long clienteId;
    private String clienteNome;
    private TipoOperacao tipo;
    private BigDecimal precoExecutado;
    private BigDecimal quantidade;
    private LocalDateTime dataExecucao;
    private String observacao;
    private LocalDateTime createdAt;
    /** Preço de entrada da recomendação (para comparação). */
    private BigDecimal precoEntradaRecomendacao;
    /** Valor da operação: precoExecutado * quantidade. */
    private BigDecimal valorOperacao;
    /** Se o cliente marcou esta recomendação como resolvida. */
    private Boolean recomendacaoResolvidaPeloCliente;
    private LocalDateTime recomendacaoResolvidoEm;
}

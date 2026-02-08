package com.example.CJLInvestimentos.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponse {
    private Integer totalCarteiras;
    private Integer totalRecomendacoesAtivas;
    /** Recomendações que o cliente ainda não marcou como resolvidas. */
    private Integer totalPendentes;
    /** Recomendações que o cliente marcou como resolvidas. */
    private Integer totalResolvidas;
    private List<CotacaoResponse> cotacoesRecentes;
    private List<RecomendacaoResponse> ultimasRecomendacoes;
}

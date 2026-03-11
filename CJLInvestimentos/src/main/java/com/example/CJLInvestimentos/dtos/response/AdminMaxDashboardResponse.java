package com.example.CJLInvestimentos.dtos.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminMaxDashboardResponse {

    // --- Totais gerais ---
    private long totalEmpresas;
    private long empresasAtivas;
    private long empresasInativas;
    private long totalConsultores;
    private long totalClientes;
    private long totalCarteiras;

    // --- Distribuição por status de assinatura ---
    private Map<String, Long> empresasPorSubscription;

    // --- Distribuição por plano ---
    private List<PlanoDistribuicao> empresasPorPlano;

    // --- Receita ---
    private BigDecimal receitaTotal;
    private BigDecimal receitaMesAtual;
    private long faturasPendentes;
    private long faturasVencidas;

    // --- Crescimento mensal (últimos 6 meses) ---
    private List<CrescimentoMensal> crescimentoMensal;

    // --- Empresas recentes ---
    private List<EmpresaResumo> empresasRecentes;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PlanoDistribuicao {
        private String nome;
        private long quantidade;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CrescimentoMensal {
        private String mes; // "2026-01", "2026-02", etc.
        private long empresas;
        private long usuarios;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EmpresaResumo {
        private Long id;
        private String nome;
        private String planoNome;
        private boolean ativa;
        private String subscriptionStatus;
        private long totalUsuarios;
        private String createdAt;
    }
}

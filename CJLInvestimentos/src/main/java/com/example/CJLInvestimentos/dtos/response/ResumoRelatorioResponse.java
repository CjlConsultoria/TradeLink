package com.example.CJLInvestimentos.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumoRelatorioResponse {
    private Long totalOperacoes;
    private Long totalCompras;
    private Long totalVendas;
    private List<ResumoPorCarteira> porCarteira;
    private List<ResumoPorCliente> porCliente;
    private List<ResumoPorMoeda> porMoeda;
    private List<OperacoesPorPeriodo> operacoesPorPeriodo;
    /** Recomendações marcadas como resolvidas pelos clientes (histórico). */
    private List<RecomendacaoResolvidaItem> recomendacoesResolvidas;
    /** Perdas e ganhos por cliente e moeda (valor vendido - valor comprado). */
    private List<PerdaGanhoClienteMoeda> perdasGanhosPorClienteMoeda;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResumoPorCarteira {
        private Long carteiraId;
        private String carteiraNome;
        private Long total;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResumoPorCliente {
        private Long clienteId;
        private String clienteNome;
        private Long total;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResumoPorMoeda {
        private String moedaPar;
        private Long total;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OperacoesPorPeriodo {
        private String periodo;
        private Long total;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecomendacaoResolvidaItem {
        private Long clienteId;
        private String clienteNome;
        private Long recomendacaoId;
        private String recomendacaoMoedaPar;
        private Long carteiraId;
        private String carteiraNome;
        private LocalDateTime resolvidoEm;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerdaGanhoClienteMoeda {
        private Long clienteId;
        private String clienteNome;
        private String moedaPar;
        private BigDecimal valorTotalCompras;
        private BigDecimal valorTotalVendas;
        private BigDecimal resultado; // valorTotalVendas - valorTotalCompras
    }
}

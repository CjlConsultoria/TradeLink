package com.example.CJLInvestimentos.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_cotacoes_historico",
       indexes = {
           @Index(name = "idx_hist_moeda_par_intervalo_dt", columnList = "moeda, par_moeda, intervalo, data_hora"),
           @Index(name = "idx_hist_data_hora", columnList = "data_hora")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_hist_moeda_par_intervalo_dt",
                             columnNames = {"moeda", "par_moeda", "intervalo", "data_hora"})
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CotacaoHistorico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String moeda;

    @Column(name = "par_moeda", nullable = false, length = 10)
    private String parMoeda;

    @Column(name = "preco_open", precision = 18, scale = 8, nullable = false)
    private BigDecimal open;

    @Column(name = "preco_high", precision = 18, scale = 8, nullable = false)
    private BigDecimal high;

    @Column(name = "preco_low", precision = 18, scale = 8, nullable = false)
    private BigDecimal low;

    @Column(name = "preco_close", precision = 18, scale = 8, nullable = false)
    private BigDecimal close;

    @Column(precision = 24, scale = 8)
    private BigDecimal volume;

    @Column(nullable = false, length = 10)
    private String intervalo;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String fonte = "COINGECKO";
}

package com.example.CJLInvestimentos.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_cotacoes",
       indexes = {
           @Index(name = "idx_cotacao_moeda_par", columnList = "moeda, par_moeda"),
           @Index(name = "idx_cotacao_data_hora", columnList = "data_hora")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cotacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String moeda;

    @Column(name = "par_moeda", nullable = false, length = 10)
    private String parMoeda;

    @Column(name = "preco_compra", precision = 18, scale = 8, nullable = false)
    private BigDecimal precoCompra;

    @Column(name = "preco_venda", precision = 18, scale = 8, nullable = false)
    private BigDecimal precoVenda;

    @Column(precision = 10, scale = 4)
    private BigDecimal variacao;

    @Column(precision = 18, scale = 8)
    private BigDecimal maximo;

    @Column(precision = 18, scale = 8)
    private BigDecimal minimo;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Column(nullable = false, length = 30)
    private String fonte;
}

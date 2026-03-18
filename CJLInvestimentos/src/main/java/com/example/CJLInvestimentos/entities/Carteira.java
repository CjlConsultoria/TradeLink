package com.example.CJLInvestimentos.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_carteiras")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Carteira {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(length = 500)
    private String descricao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultor_id", nullable = false)
    private User consultor;

    @Column(nullable = false)
    @Builder.Default
    private Boolean ativa = true;

    @Column(name = "margem_erro", precision = 5, scale = 2)
    private BigDecimal margemErro;

    @Column(name = "moeda_referencia_rebalance", length = 10)
    private String moedaReferenciaRebalance;

    @Column(name = "rebalance_ativo")
    private Boolean rebalanceAtivo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}

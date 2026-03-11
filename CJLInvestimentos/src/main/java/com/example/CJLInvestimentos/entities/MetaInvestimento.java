package com.example.CJLInvestimentos.entities;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_metas_investimento", indexes = @Index(name = "idx_meta_user", columnList = "user_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MetaInvestimento {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String titulo;

    @Column(length = 300)
    private String descricao;

    @Column(name = "valor_alvo", nullable = false, precision = 18, scale = 2)
    private BigDecimal valorAlvo;

    @Column(name = "valor_atual", precision = 18, scale = 2)
    private BigDecimal valorAtual;

    @Column(name = "data_limite")
    private LocalDate dataLimite;

    @Column(nullable = false)
    @Builder.Default
    private Boolean concluida = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() { if (createdAt == null) createdAt = LocalDateTime.now(); if (valorAtual == null) valorAtual = BigDecimal.ZERO; }
}

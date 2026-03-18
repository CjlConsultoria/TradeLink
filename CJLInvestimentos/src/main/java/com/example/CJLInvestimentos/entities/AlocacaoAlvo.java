package com.example.CJLInvestimentos.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_alocacoes_alvo",
       uniqueConstraints = @UniqueConstraint(columnNames = {"carteira_id", "simbolo"}),
       indexes = @Index(name = "idx_alocacao_carteira", columnList = "carteira_id"))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlocacaoAlvo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carteira_id", nullable = false)
    private Carteira carteira;

    @Column(nullable = false, length = 50)
    private String simbolo;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(name = "percentual_alvo", nullable = false, precision = 5, scale = 2)
    private BigDecimal percentualAlvo;

    @Column(name = "par_moeda_referencia", length = 10)
    @Builder.Default
    private String parMoedaReferencia = "USD";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}

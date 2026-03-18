package com.example.CJLInvestimentos.entities;

import com.example.CJLInvestimentos.entities.enums.StatusRecomendacao;
import com.example.CJLInvestimentos.entities.enums.TipoRecomendacao;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_recomendacoes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recomendacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carteira_id", nullable = false)
    private Carteira carteira;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoRecomendacao tipo;

    @Column(nullable = false, length = 10)
    private String moeda;

    @Column(name = "par_moeda", nullable = false, length = 10)
    private String parMoeda;

    @Column(name = "preco_entrada", precision = 18, scale = 8)
    private BigDecimal precoEntrada;

    @Column(name = "preco_alvo", precision = 18, scale = 8)
    private BigDecimal precoAlvo;

    @Column(name = "stop_loss", precision = 18, scale = 8)
    private BigDecimal stopLoss;

    @Column(precision = 18, scale = 8)
    private BigDecimal quantidade;

    @Column(name = "percentual", precision = 5, scale = 2)
    private BigDecimal percentual;

    @Column(name = "modo_percentual")
    @Builder.Default
    private Boolean modoPercentual = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatusRecomendacao status = StatusRecomendacao.ATIVA;

    @Column(length = 1000)
    private String observacao;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

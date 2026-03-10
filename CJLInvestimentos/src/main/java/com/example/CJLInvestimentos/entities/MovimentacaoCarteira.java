package com.example.CJLInvestimentos.entities;

import com.example.CJLInvestimentos.entities.enums.TipoMovimentacao;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_movimentacoes_carteira",
       indexes = {
           @Index(name = "idx_mov_cliente", columnList = "cliente_id"),
           @Index(name = "idx_mov_carteira", columnList = "carteira_id"),
           @Index(name = "idx_mov_data", columnList = "data_movimentacao")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimentacaoCarteira {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private User cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carteira_id")
    private Carteira carteira;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoMovimentacao tipo;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal valor;

    @Column(length = 10, nullable = false)
    @Builder.Default
    private String moeda = "USD";

    @Column(name = "data_movimentacao", nullable = false)
    private LocalDate dataMovimentacao;

    @Column(length = 500)
    private String observacao;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}

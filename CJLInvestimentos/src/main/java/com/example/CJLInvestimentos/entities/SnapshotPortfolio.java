package com.example.CJLInvestimentos.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_snapshots_portfolio",
       indexes = {
           @Index(name = "idx_snapshot_cliente_data", columnList = "cliente_id, data_snapshot"),
           @Index(name = "idx_snapshot_carteira", columnList = "carteira_id")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SnapshotPortfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private User cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carteira_id")
    private Carteira carteira;

    @Column(name = "data_snapshot", nullable = false)
    private LocalDate dataSnapshot;

    @Column(name = "valor_total_portfolio", precision = 18, scale = 2)
    private BigDecimal valorTotalPortfolio;

    @Column(name = "valor_btc_hold", precision = 18, scale = 2)
    private BigDecimal valorBtcHold;

    @Column(name = "detalhes_json", columnDefinition = "TEXT")
    private String detalhesJson;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}

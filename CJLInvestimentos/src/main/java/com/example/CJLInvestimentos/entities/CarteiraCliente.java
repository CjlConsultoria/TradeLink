package com.example.CJLInvestimentos.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_carteira_clientes",
       uniqueConstraints = @UniqueConstraint(columnNames = {"carteira_id", "cliente_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarteiraCliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carteira_id", nullable = false)
    private Carteira carteira;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private User cliente;

    @Column(name = "assigned_at", nullable = false, updatable = false)
    private LocalDateTime assignedAt;

    @PrePersist
    void prePersist() {
        this.assignedAt = LocalDateTime.now();
    }
}

package com.example.CJLInvestimentos.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_recomendacao_resolvida_cliente",
       uniqueConstraints = @UniqueConstraint(columnNames = {"recomendacao_id", "cliente_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecomendacaoResolvidaCliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recomendacao_id", nullable = false)
    private Recomendacao recomendacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private User cliente;

    @Column(name = "resolvido_em", nullable = false, updatable = false)
    private LocalDateTime resolvidoEm;

    @PrePersist
    void prePersist() {
        if (this.resolvidoEm == null) {
            this.resolvidoEm = LocalDateTime.now();
        }
    }
}

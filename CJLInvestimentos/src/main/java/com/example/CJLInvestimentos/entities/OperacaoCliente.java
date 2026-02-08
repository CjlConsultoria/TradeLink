package com.example.CJLInvestimentos.entities;

import com.example.CJLInvestimentos.entities.enums.TipoOperacao;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_operacoes_clientes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperacaoCliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recomendacao_id", nullable = false)
    private Recomendacao recomendacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private User cliente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoOperacao tipo;

    @Column(name = "preco_executado", nullable = false, precision = 18, scale = 8)
    private BigDecimal precoExecutado;

    @Column(nullable = false, precision = 18, scale = 8)
    private BigDecimal quantidade;

    @Column(name = "data_execucao", nullable = false)
    private LocalDateTime dataExecucao;

    @Column(length = 500)
    private String observacao;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}

package com.example.CJLInvestimentos.entities;

import com.example.CJLInvestimentos.entities.enums.CategoriaAtivo;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_ativos_clientes",
       uniqueConstraints = @UniqueConstraint(columnNames = {"cliente_id", "simbolo"}),
       indexes = {
           @Index(name = "idx_ativo_cliente_id", columnList = "cliente_id"),
           @Index(name = "idx_ativo_simbolo", columnList = "simbolo")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtivoCliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private User cliente;

    @Column(nullable = false, length = 50)
    private String simbolo;

    @Column(nullable = false, length = 100)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CategoriaAtivo categoria;

    @Column(nullable = false, precision = 18, scale = 8)
    private BigDecimal quantidade;

    @Column(name = "preco_manual", precision = 18, scale = 8)
    private BigDecimal precoManual;

    @Column(name = "par_moeda_referencia", length = 10)
    @Builder.Default
    private String parMoedaReferencia = "BRL";

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

package com.example.CJLInvestimentos.entities;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity
@Table(name = "tb_planos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plano {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String nome;

    /** Tipo do plano: CONSULTOR (para empresas) ou AUTO_GESTAO (para clientes individuais). */
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String tipo = "CONSULTOR";

    @Column(name = "max_usuarios", nullable = false)
    private Integer maxUsuarios;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;
    @Column(nullable = false)
    @Builder.Default
    private Boolean ativo = true;

    /** ID do preço no Stripe (ex: price_xxx) para cobrança recorrente. Configurar no Stripe Dashboard. */
    @Column(name = "stripe_price_id", length = 128)
    private String stripePriceId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
package com.example.CJLInvestimentos.entities;

import com.example.CJLInvestimentos.entities.enums.TipoAlerta;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_alertas_preco", indexes = {
    @Index(name = "idx_alerta_user", columnList = "user_id"),
    @Index(name = "idx_alerta_moeda_par", columnList = "moeda, par_moeda")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertaPreco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 10)
    private String moeda;

    @Column(name = "par_moeda", nullable = false, length = 10)
    private String parMoeda;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_alerta", nullable = false, length = 10)
    private TipoAlerta tipoAlerta;

    @Column(name = "preco_alerta", nullable = false, precision = 18, scale = 8)
    private BigDecimal precoAlerta;

    @Column(nullable = false)
    @Builder.Default
    private Boolean ativo = true;

    /** True quando o alerta ja foi disparado (para evitar notificacoes repetidas). */
    @Column(nullable = false)
    @Builder.Default
    private Boolean disparado = false;

    @Column(length = 200)
    private String observacao;

    @Column(name = "data_disparo")
    private LocalDateTime dataDisparo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}

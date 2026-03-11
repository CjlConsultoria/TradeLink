package com.example.CJLInvestimentos.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_notificacoes_in_app", indexes = {
    @Index(name = "idx_notif_user", columnList = "user_id"),
    @Index(name = "idx_notif_lida", columnList = "user_id, lida")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificacaoInApp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(nullable = false, length = 500)
    private String mensagem;

    /** Tipo para icone/cor no frontend: RECOMENDACAO, ALERTA, SISTEMA, OPERACAO, etc. */
    @Column(nullable = false, length = 30)
    @Builder.Default
    private String tipo = "SISTEMA";

    /** Link para onde a notificacao deve redirecionar (opcional). */
    @Column(length = 300)
    private String link;

    @Column(nullable = false)
    @Builder.Default
    private Boolean lida = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}

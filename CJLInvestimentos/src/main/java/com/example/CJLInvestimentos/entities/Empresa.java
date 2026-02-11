package com.example.CJLInvestimentos.entities;

import com.example.CJLInvestimentos.entities.enums.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_empresas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true, length = 18)
    private String cnpj;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plano_id")
    private Plano plano;

    @Column(nullable = false)
    @Builder.Default
    private Boolean ativo = true;

    /** Canais de notificação (por empresa): habilitar/desabilitar. */
    @Column(name = "notificacao_email", nullable = false)
    @Builder.Default
    private Boolean notificacaoEmail = true;

    @Column(name = "notificacao_telegram", nullable = false)
    @Builder.Default
    private Boolean notificacaoTelegram = true;

    @Column(name = "notificacao_push", nullable = false)
    @Builder.Default
    private Boolean notificacaoPush = true;

    @Column(name = "notificacao_whatsapp", nullable = false)
    @Builder.Default
    private Boolean notificacaoWhatsApp = false;

    @Column(name = "notificacao_sms", nullable = false)
    @Builder.Default
    private Boolean notificacaoSms = false;

    /** ID do cliente no Stripe (para cobrança). */
    @Column(name = "stripe_customer_id", length = 128)
    private String stripeCustomerId;

    /** ID da assinatura no Stripe (subscription). */
    @Column(name = "stripe_subscription_id", length = 128)
    private String stripeSubscriptionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_status", length = 20)
    @Builder.Default
    private SubscriptionStatus subscriptionStatus = SubscriptionStatus.NONE;

    /** Fim do período atual de cobrança (renovação). */
    @Column(name = "current_period_end")
    private Instant currentPeriodEnd;

    /** Bloqueio manual pelo AdminMax: pagamento não validado ou outra diretriz. Bloqueia todos os consultores e clientes da empresa. */
    @Column(name = "acesso_bloqueado_por_admin", nullable = false)
    @Builder.Default
    private Boolean acessoBloqueadoPorAdmin = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}

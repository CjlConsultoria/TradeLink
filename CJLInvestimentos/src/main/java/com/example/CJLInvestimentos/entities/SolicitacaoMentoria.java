package com.example.CJLInvestimentos.entities;

import com.example.CJLInvestimentos.entities.enums.StatusSolicitacaoMentoria;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_solicitacoes_mentoria")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitacaoMentoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Cliente que solicita a mentoria. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private User cliente;

    /** Empresa/consultor que recebe a solicitação. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatusSolicitacaoMentoria status = StatusSolicitacaoMentoria.PENDENTE;

    /** Preço proposto pelo consultor (base do marketplace). */
    @Column(name = "preco_proposto", precision = 10, scale = 2)
    private BigDecimal precoProposto;

    /** Preço final negociado (pode ser ajustado pelo consultor ao aceitar). */
    @Column(name = "preco_final", precision = 10, scale = 2)
    private BigDecimal precoFinal;

    /** Mensagem do cliente ao solicitar. */
    @Column(name = "mensagem_cliente", columnDefinition = "text")
    private String mensagemCliente;

    /** Mensagem do consultor ao responder (aceitar/recusar). */
    @Column(name = "mensagem_consultor", columnDefinition = "text")
    private String mensagemConsultor;

    /** ID da sessão de checkout do Stripe (para pagamento). */
    @Column(name = "stripe_checkout_session_id", length = 256)
    private String stripeCheckoutSessionId;

    /** ID da subscription criada no Stripe após pagamento. */
    @Column(name = "stripe_subscription_id", length = 128)
    private String stripeSubscriptionId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

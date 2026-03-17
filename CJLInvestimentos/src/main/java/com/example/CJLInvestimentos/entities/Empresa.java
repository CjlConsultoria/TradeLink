package com.example.CJLInvestimentos.entities;

import com.example.CJLInvestimentos.entities.enums.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
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

    /** Endereço */
    @Column(length = 10)
    private String cep;
    @Column(length = 200)
    private String logradouro;
    @Column(length = 20)
    private String numero;
    @Column(length = 100)
    private String complemento;
    @Column(length = 100)
    private String bairro;
    @Column(length = 100)
    private String cidade;
    @Column(length = 2)
    private String uf;

    /** Responsável legal / contato principal */
    @Column(name = "nome_responsavel", length = 150)
    private String nomeResponsavel;
    @Column(name = "cpf_responsavel", length = 14)
    private String cpfResponsavel;
    @Column(name = "email_alternativo", length = 150)
    private String emailAlternativo;
    @Column(length = 30)
    private String telefone;

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
    @Column(name = "acesso_bloqueado_por_admin", nullable = false, columnDefinition = "boolean not null default false")
    @Builder.Default
    private Boolean acessoBloqueadoPorAdmin = false;

    // --- Campos de auto-cadastro / trial ---

    @Column(name = "trial_inicio")
    private LocalDateTime trialInicio;

    @Column(name = "trial_fim")
    private LocalDateTime trialFim;

    @Column(name = "auto_cadastro", nullable = false)
    @Builder.Default
    private Boolean autoCadastro = false;

    // --- Campos do Marketplace ---

    @Column(name = "marketplace_visivel")
    @Builder.Default
    private Boolean marketplaceVisivel = false;

    @Column(name = "marketplace_descricao", columnDefinition = "text")
    private String marketplaceDescricao;

    @Column(name = "marketplace_especializacao", length = 200)
    private String marketplaceEspecializacao;

    @Column(name = "marketplace_experiencia", length = 200)
    private String marketplaceExperiencia;

    @Column(name = "marketplace_foto_url", length = 500)
    private String marketplaceFotoUrl;

    @Column(name = "marketplace_preco_base", precision = 10, scale = 2)
    private BigDecimal marketplacePrecoBase;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}

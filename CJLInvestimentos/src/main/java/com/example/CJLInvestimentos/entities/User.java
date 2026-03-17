package com.example.CJLInvestimentos.entities;

import com.example.CJLInvestimentos.entities.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tb_usuarios")
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    @Column(unique = true)
    private String email;

    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    /** Telefone para WhatsApp (formato livre, ex: 11999999999). */
    @Column(length = 30)
    private String telefone;

    /** Chat ID do Telegram para notificações (usuário vincula no app). */
    @Column(name = "telegram_chat_id", length = 50)
    private String telegramChatId;

    @Column(nullable = false)
    @Builder.Default
    private Boolean ativo = true;

    // --- Campos do sistema de convite / ativação de conta ---

    @Column(length = 14, unique = true)
    private String cpf;

    @Column(length = 30)
    private String whatsapp;

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
    private String estado;

    @Column(name = "termo_aceito")
    private Boolean termoAceito;

    @Column(name = "termo_aceito_em")
    private LocalDateTime termoAceitoEm;

    @Column(name = "token_convite", length = 128, unique = true)
    private String tokenConvite;

    @Column(name = "token_convite_expiracao")
    private LocalDateTime tokenConviteExpiracao;

    // --- Campos do sistema de exclusão / auto-gestão ---

    @Column(name = "auto_gestao", nullable = false)
    @Builder.Default
    private Boolean autoGestao = false;

    @Column(name = "relatorio_compl_baixado", nullable = false)
    @Builder.Default
    private Boolean relatorioComplBaixado = false;

    @Column(name = "relatorio_compl_baixado_em")
    private LocalDateTime relatorioComplBaixadoEm;

    @Column(name = "data_exclusao")
    private LocalDateTime dataExclusao;

    /** Stripe customer ID para cobrança individual (quando empresa_id é null). */
    @Column(name = "stripe_customer_id", length = 128)
    private String stripeCustomerId;

    /** Fim do período da assinatura auto-gestão. */
    @Column(name = "current_period_end")
    private Instant currentPeriodEnd;

    /** Status da subscription individual: NONE, ACTIVE, EXPIRED, TRIAL. */
    @Column(name = "subscription_status", length = 20)
    @Builder.Default
    private String subscriptionStatus = "NONE";

    // --- Campos do sistema de auto-cadastro / trial ---

    @Column(name = "trial_inicio")
    private LocalDateTime trialInicio;

    @Column(name = "trial_fim")
    private LocalDateTime trialFim;

    @Column(name = "auto_cadastro", nullable = false)
    @Builder.Default
    private Boolean autoCadastro = false;

    @Column(length = 18)
    private String cnpj;

    // --- Campos do Marketplace ---

    @Column(name = "origem_vinculo", length = 20)
    private String origemVinculo;

    @Column(name = "marketplace_preco_cliente", precision = 10, scale = 2)
    private BigDecimal marketplacePrecoCliente;

    @Column(name = "marketplace_subscription_id", length = 128)
    private String marketplaceSubscriptionId;

    @Column(name = "marketplace_current_period_end")
    private Instant marketplaceCurrentPeriodEnd;

    // --- Campos de recuperação de senha ---

    @Column(name = "token_reset_senha", length = 128, unique = true)
    private String tokenResetSenha;

    @Column(name = "token_reset_senha_expiracao")
    private LocalDateTime tokenResetSenhaExpiracao;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(() -> "ROLE_" + role.name());
    }

    @Override
    public String getPassword() {
        return senha;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return ativo != null ? ativo : true;
    }
}

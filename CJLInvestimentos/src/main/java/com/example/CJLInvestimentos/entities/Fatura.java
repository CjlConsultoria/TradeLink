package com.example.CJLInvestimentos.entities;

import com.example.CJLInvestimentos.entities.enums.FormaPagamento;
import com.example.CJLInvestimentos.entities.enums.StatusFatura;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_faturas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fatura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    /** Usuário individual (para faturas de auto-gestão — quando empresa_id é null). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /** Data de vencimento (30 dias após assinatura ou após período anterior). */
    @Column(name = "data_vencimento", nullable = false)
    private Instant dataVencimento;

    /** Data em que foi paga (null se pendente/vencida). */
    @Column(name = "data_pagamento")
    private Instant dataPagamento;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatusFatura status = StatusFatura.PENDENTE;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento", length = 20)
    private FormaPagamento formaPagamento;

    /** Referência externa (Stripe payment_intent id, etc.). */
    @Column(name = "referencia_externa", length = 128)
    private String referenciaExterna;

    /** Descrição do serviço na fatura (ex.: "Assinatura mensal - Plano Premium"). */
    @Column(name = "descricao_servico", length = 500)
    private String descricaoServico;

    /** Observação para pagamento manual (AdminMax). */
    @Column(length = 500)
    private String observacao;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}

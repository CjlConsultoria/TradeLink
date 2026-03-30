package com.example.CJLInvestimentos.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_email_marketing", indexes = {
    @Index(name = "idx_email_mkt_campanha", columnList = "campanha"),
    @Index(name = "idx_email_mkt_enviado_por", columnList = "enviado_por_id"),
    @Index(name = "idx_email_mkt_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailMarketing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email_destinatario", nullable = false)
    private String emailDestinatario;

    @Column(name = "nome_destinatario")
    private String nomeDestinatario;

    @Column(nullable = false)
    private String assunto;

    @Column(length = 100)
    private String campanha;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "ENVIADO";

    @Column(length = 500)
    private String erro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enviado_por_id")
    private User enviadoPor;

    @Column(name = "enviado_em", nullable = false, updatable = false)
    private LocalDateTime enviadoEm;

    @PrePersist
    void prePersist() {
        this.enviadoEm = LocalDateTime.now();
    }
}

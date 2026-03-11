package com.example.CJLInvestimentos.entities;

import com.example.CJLInvestimentos.entities.enums.TipoTemplateApresentacao;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_emails_apresentacao", indexes = {
    @Index(name = "idx_email_apres_user", columnList = "user_id"),
    @Index(name = "idx_email_apres_enviado_por", columnList = "enviado_por_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailApresentacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enviado_por_id", nullable = false)
    private User enviadoPor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoTemplateApresentacao tipoTemplate;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "ENVIADO";

    @Column(length = 500)
    private String erro;

    @Column(name = "enviado_em", nullable = false, updatable = false)
    private LocalDateTime enviadoEm;

    @PrePersist
    void prePersist() {
        this.enviadoEm = LocalDateTime.now();
    }
}

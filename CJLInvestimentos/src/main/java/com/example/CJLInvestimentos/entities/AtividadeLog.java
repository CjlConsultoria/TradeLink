package com.example.CJLInvestimentos.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_atividade_log",
       indexes = {@Index(name = "idx_atividade_user", columnList = "user_id"),
                  @Index(name = "idx_atividade_data", columnList = "created_at")})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AtividadeLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String tipo; // LOGIN, OPERACAO, ALERTA_DISPARADO, RECOMENDACAO_RECEBIDA, RECOMENDACAO_RESOLVIDA, FATURA_PAGA, PERFIL_ATUALIZADO

    @Column(length = 500)
    private String descricao;

    @Column(length = 200)
    private String link; // optional route to navigate to

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() { if (createdAt == null) createdAt = LocalDateTime.now(); }
}

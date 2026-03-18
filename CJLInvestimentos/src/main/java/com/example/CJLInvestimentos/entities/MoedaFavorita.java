package com.example.CJLInvestimentos.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_moedas_favoritas",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "moeda", "par_moeda"}),
       indexes = @Index(name = "idx_fav_user", columnList = "user_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MoedaFavorita {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 20)
    private String moeda;

    @Column(name = "par_moeda", nullable = false, length = 20)
    private String parMoeda;

    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() { if (createdAt == null) createdAt = LocalDateTime.now(); }
}

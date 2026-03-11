package com.example.CJLInvestimentos.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tb_login_logs")
@Builder
public class LoginLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(length = 255)
    private String email;

    @Column(length = 45)
    private String ip;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "data_hora", nullable = false)
    @Builder.Default
    private LocalDateTime dataHora = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private Boolean sucesso = false;

    @Column(name = "motivo_falha", length = 200)
    private String motivoFalha;
}

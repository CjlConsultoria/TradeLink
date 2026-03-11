package com.example.CJLInvestimentos.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tb_configuracao_sistema")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracaoSistema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Se true, exige OTP por e-mail para login (exceto AdminMax). */
    @Column(nullable = false)
    @Builder.Default
    private Boolean doisFatoresAtivo = true;
}

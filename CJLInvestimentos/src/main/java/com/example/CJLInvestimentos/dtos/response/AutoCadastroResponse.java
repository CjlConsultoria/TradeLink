package com.example.CJLInvestimentos.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutoCadastroResponse {
    private String token;
    private String role;
    private Long userId;
    private String nome;
    private Long empresaId;
    private LocalDateTime trialFim;
    private String mensagem;
}

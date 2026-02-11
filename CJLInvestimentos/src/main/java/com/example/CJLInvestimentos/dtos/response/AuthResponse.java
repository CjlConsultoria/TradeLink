package com.example.CJLInvestimentos.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String token;
    private String mensagem;
    private String role;
    private Long empresaId;
    private Long userId;
    private String nome;
    private Boolean bloqueado;
    private String motivoBloqueio;
}

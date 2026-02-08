package com.example.CJLInvestimentos.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String nome;
    private String email;
    private String role;
    private Long empresaId;
    private String empresaNome;
    private Boolean ativo;
    /** Chat ID do Telegram (se vinculado). */
    private String telegramChatId;
    /** Se o usuário tem pelo menos uma inscrição de push ativa. */
    private Boolean pushInscrito;
}

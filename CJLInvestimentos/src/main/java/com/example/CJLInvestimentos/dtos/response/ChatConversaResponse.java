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
public class ChatConversaResponse {
    private Long id;
    private Long userId;
    private String nomeUsuario;
    private String roleUsuario;
    private String status;
    private String assunto;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String ultimaMensagem;
    private int naoLidas;
}

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
public class ChamadoResponse {

    private Long id;
    private String numero;
    private String assunto;
    private String descricao;
    private String categoria;
    private String prioridade;
    private String status;
    private Long userId;
    private String nomeUsuario;
    private String roleUsuario;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String ultimaResposta;
    private int totalRespostas;
    private int naoLidas;
}

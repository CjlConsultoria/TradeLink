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
public class ChamadoRespostaResponse {

    private Long id;
    private Long chamadoId;
    private Long remetenteId;
    private String nomeRemetente;
    private String conteudo;
    private Boolean lida;
    private LocalDateTime createdAt;
    private Boolean isAdmin;
}

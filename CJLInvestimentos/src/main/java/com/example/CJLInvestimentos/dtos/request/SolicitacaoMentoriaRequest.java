package com.example.CJLInvestimentos.dtos.request;

import lombok.Data;

@Data
public class SolicitacaoMentoriaRequest {
    private Long empresaId;
    private String mensagemCliente;
}

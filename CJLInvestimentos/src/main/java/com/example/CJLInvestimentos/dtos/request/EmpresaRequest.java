package com.example.CJLInvestimentos.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmpresaRequest {
    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    @NotBlank(message = "CNPJ é obrigatório")
    private String cnpj;

    private Long planoId;

    /** Canais de notificação: habilitar/desabilitar por empresa. */
    private Boolean notificacaoEmail;
    private Boolean notificacaoTelegram;
    private Boolean notificacaoPush;
    private Boolean notificacaoWhatsApp;
    private Boolean notificacaoSms;
}
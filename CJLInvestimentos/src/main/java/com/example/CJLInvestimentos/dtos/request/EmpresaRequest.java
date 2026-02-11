package com.example.CJLInvestimentos.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EmpresaRequest {
    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 255)
    private String nome;

    @NotBlank(message = "CNPJ é obrigatório")
    private String cnpj;

    /** Endereço */
    @Size(max = 10)
    private String cep;
    @Size(max = 200)
    private String logradouro;
    @Size(max = 20)
    private String numero;
    @Size(max = 100)
    private String complemento;
    @Size(max = 100)
    private String bairro;
    @Size(max = 100)
    private String cidade;
    @Size(max = 2)
    private String uf;

    /** Responsável e contato */
    @Size(max = 150)
    private String nomeResponsavel;
    @Size(max = 14)
    private String cpfResponsavel;
    @Size(max = 150)
    private String emailAlternativo;
    @Size(max = 30)
    private String telefone;

    private Long planoId;

    /** Canais de notificação: habilitar/desabilitar por empresa. */
    private Boolean notificacaoEmail;
    private Boolean notificacaoTelegram;
    private Boolean notificacaoPush;
    private Boolean notificacaoWhatsApp;
    private Boolean notificacaoSms;
}
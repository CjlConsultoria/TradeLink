package com.example.CJLInvestimentos.dtos.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class EnviarEmailMarketingRequest {

    @NotEmpty(message = "Informe pelo menos um contato")
    @Valid
    private List<ContatoExterno> contatos;

    @NotBlank(message = "Informe o assunto do email")
    private String assunto;

    private String campanha;

    @Data
    public static class ContatoExterno {
        @NotBlank(message = "Email e obrigatorio")
        @Email(message = "Email invalido")
        private String email;

        private String nome;
    }
}

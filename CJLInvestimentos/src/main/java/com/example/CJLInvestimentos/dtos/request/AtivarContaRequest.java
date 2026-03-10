package com.example.CJLInvestimentos.dtos.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AtivarContaRequest {

    @NotBlank(message = "Token é obrigatório")
    private String token;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 150)
    private String nome;

    @NotBlank(message = "CPF é obrigatório")
    @Size(min = 11, max = 14)
    private String cpf;

    @NotBlank(message = "WhatsApp é obrigatório")
    @Size(max = 30)
    private String whatsapp;

    @NotBlank(message = "CEP é obrigatório")
    @Size(min = 8, max = 10)
    private String cep;

    @NotBlank(message = "Logradouro é obrigatório")
    @Size(max = 200)
    private String logradouro;

    @NotBlank(message = "Número é obrigatório")
    @Size(max = 20)
    private String numero;

    @Size(max = 100)
    private String complemento;

    @NotBlank(message = "Bairro é obrigatório")
    @Size(max = 100)
    private String bairro;

    @NotBlank(message = "Cidade é obrigatória")
    @Size(max = 100)
    private String cidade;

    @NotBlank(message = "Estado é obrigatório")
    @Size(min = 2, max = 2)
    private String estado;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
    private String senha;

    @NotBlank(message = "Confirmação de senha é obrigatória")
    private String confirmarSenha;

    @NotNull(message = "Aceite dos termos é obrigatório")
    @AssertTrue(message = "Você deve aceitar os termos de uso")
    private Boolean termoAceito;
}

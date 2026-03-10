package com.example.CJLInvestimentos.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AutoCadastroRequest {

    /** "CLIENTE" ou "CONSULTOR" */
    @NotBlank
    private String tipo;

    @NotBlank
    @Size(min = 3, max = 150)
    private String nome;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 6, max = 100)
    private String senha;

    @NotBlank
    @Size(min = 11, max = 14)
    private String cpf;

    @Size(max = 30)
    private String whatsapp;

    // Endereço
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
    private String estado;

    @NotNull
    private Boolean termoAceito;

    // Campos condicionais para consultor
    /** CNPJ se PJ, ou CPF se PF */
    @Size(max = 18)
    private String cnpj;

    @Size(max = 150)
    private String nomeEmpresa;

    @Size(max = 150)
    private String nomeResponsavel;

    @Size(max = 14)
    private String cpfResponsavel;
}

package com.example.CJLInvestimentos.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CarteiraRequest {
    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    private String descricao;
}
package com.example.CJLInvestimentos.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FaqRequest {
    @NotBlank
    private String pergunta;

    @NotBlank
    private String resposta;

    private String categoria;

    private Integer ordem;
}

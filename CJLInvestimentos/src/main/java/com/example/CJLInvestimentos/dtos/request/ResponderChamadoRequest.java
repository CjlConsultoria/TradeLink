package com.example.CJLInvestimentos.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResponderChamadoRequest {

    @NotBlank(message = "O conteúdo da resposta é obrigatório")
    private String conteudo;
}

package com.example.CJLInvestimentos.dtos.request;

import com.example.CJLInvestimentos.entities.enums.CategoriaChamado;
import com.example.CJLInvestimentos.entities.enums.PrioridadeChamado;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CriarChamadoRequest {

    @NotBlank(message = "O assunto é obrigatório")
    private String assunto;

    @NotBlank(message = "A descrição é obrigatória")
    private String descricao;

    @NotNull(message = "A categoria é obrigatória")
    private CategoriaChamado categoria;

    @NotNull(message = "A prioridade é obrigatória")
    private PrioridadeChamado prioridade;
}

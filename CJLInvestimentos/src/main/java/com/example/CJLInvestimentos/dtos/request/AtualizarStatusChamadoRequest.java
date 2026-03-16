package com.example.CJLInvestimentos.dtos.request;

import com.example.CJLInvestimentos.entities.enums.StatusChamado;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AtualizarStatusChamadoRequest {

    @NotNull(message = "O status é obrigatório")
    private StatusChamado status;
}

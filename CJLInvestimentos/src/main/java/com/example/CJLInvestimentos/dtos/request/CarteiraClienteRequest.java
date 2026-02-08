package com.example.CJLInvestimentos.dtos.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CarteiraClienteRequest {
    @NotNull(message = "ID do cliente é obrigatório")
    private Long clienteId;
}

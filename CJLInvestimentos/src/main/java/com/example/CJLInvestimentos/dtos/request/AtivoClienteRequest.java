package com.example.CJLInvestimentos.dtos.request;

import com.example.CJLInvestimentos.entities.enums.CategoriaAtivo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AtivoClienteRequest {
    @NotBlank(message = "Símbolo é obrigatório")
    private String simbolo;

    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    @NotNull(message = "Categoria é obrigatória")
    private CategoriaAtivo categoria;

    @NotNull(message = "Quantidade é obrigatória")
    private BigDecimal quantidade;

    private BigDecimal precoManual;

    private String parMoedaReferencia;
}

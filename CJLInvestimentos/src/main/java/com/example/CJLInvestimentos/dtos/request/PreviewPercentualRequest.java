package com.example.CJLInvestimentos.dtos.request;

import com.example.CJLInvestimentos.entities.enums.TipoRecomendacao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PreviewPercentualRequest {
    @NotNull(message = "Carteira é obrigatória")
    private Long carteiraId;

    @NotNull(message = "Tipo é obrigatório")
    private TipoRecomendacao tipo;

    @NotBlank(message = "Moeda é obrigatória")
    private String moeda;

    @NotBlank(message = "Par de moeda é obrigatório")
    private String parMoeda;

    @NotNull(message = "Percentual é obrigatório")
    private BigDecimal percentual;
}

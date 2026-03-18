package com.example.CJLInvestimentos.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AlertaPrecoRequest {

    @NotBlank(message = "Moeda e obrigatoria")
    private String moeda;

    @NotBlank(message = "Par de moeda e obrigatorio")
    private String parMoeda;

    @NotBlank(message = "Tipo de alerta e obrigatorio (ACIMA ou ABAIXO)")
    private String tipoAlerta;

    @NotNull(message = "Preco de alerta e obrigatorio")
    private BigDecimal precoAlerta;

    private String observacao;
}

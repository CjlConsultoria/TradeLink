package com.example.CJLInvestimentos.dtos.request;

import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class AtualizarFaturaRequest {

    private Instant dataVencimento;

    @DecimalMin(value = "0.01", message = "Valor deve ser positivo")
    private BigDecimal valor;

    private String descricaoServico;
    private String observacao;
}

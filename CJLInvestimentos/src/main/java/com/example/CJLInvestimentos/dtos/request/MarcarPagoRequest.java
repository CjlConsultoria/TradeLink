package com.example.CJLInvestimentos.dtos.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MarcarPagoRequest {
    private BigDecimal valor;
    private String observacao;
}

package com.example.CJLInvestimentos.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CotacaoResponse {
    private Long id;
    private String moeda;
    private String parMoeda;
    private BigDecimal precoCompra;
    private BigDecimal precoVenda;
    private BigDecimal variacao;
    private BigDecimal maximo;
    private BigDecimal minimo;
    private LocalDateTime dataHora;
    private String fonte;
}

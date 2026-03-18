package com.example.CJLInvestimentos.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaldoResponse {
    private BigDecimal totalAportado;
    private BigDecimal totalSacado;
    private BigDecimal saldoDisponivel;   // valor dos ativos cash (USD/BRL)
    private BigDecimal saldoInvestido;    // valor dos ativos nao-cash (crypto etc)
    private BigDecimal saldoTotal;        // disponivel + investido
    private BigDecimal lucroPerda;        // saldoTotal - (totalAportado - totalSacado)
    private String moedaReferencia;
}

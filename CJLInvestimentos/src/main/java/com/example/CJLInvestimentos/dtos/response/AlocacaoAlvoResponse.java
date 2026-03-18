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
public class AlocacaoAlvoResponse {
    private Long id;
    private String simbolo;
    private String nome;
    private BigDecimal percentualAlvo;
    private String parMoedaReferencia;
}

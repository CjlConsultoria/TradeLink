package com.example.CJLInvestimentos.dtos.response;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertaPrecoResponse {
    private Long id;
    private String moeda;
    private String parMoeda;
    private String tipoAlerta;
    private BigDecimal precoAlerta;
    private boolean ativo;
    private boolean disparado;
    private String observacao;
    private String dataDisparo;
    private String createdAt;
}

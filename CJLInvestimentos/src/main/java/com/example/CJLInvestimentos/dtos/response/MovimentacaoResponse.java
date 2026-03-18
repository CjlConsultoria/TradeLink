package com.example.CJLInvestimentos.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimentacaoResponse {
    private Long id;
    private String tipo;
    private BigDecimal valor;
    private String moeda;
    private LocalDate dataMovimentacao;
    private String observacao;
    private Long clienteId;
    private String clienteNome;
    private LocalDateTime createdAt;
}

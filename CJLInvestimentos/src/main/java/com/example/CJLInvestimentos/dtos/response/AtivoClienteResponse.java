package com.example.CJLInvestimentos.dtos.response;

import com.example.CJLInvestimentos.entities.enums.CategoriaAtivo;
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
public class AtivoClienteResponse {
    private Long id;
    private Long clienteId;
    private String simbolo;
    private String nome;
    private CategoriaAtivo categoria;
    private BigDecimal quantidade;
    private BigDecimal precoManual;
    private String parMoedaReferencia;
    private BigDecimal precoAtual;
    private BigDecimal valorTotal;
    private BigDecimal percentualAlocacao;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

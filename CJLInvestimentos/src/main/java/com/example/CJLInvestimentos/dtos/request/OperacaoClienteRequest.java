package com.example.CJLInvestimentos.dtos.request;

import com.example.CJLInvestimentos.entities.enums.TipoOperacao;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OperacaoClienteRequest {
    @NotNull(message = "Tipo é obrigatório")
    private TipoOperacao tipo;

    @NotNull(message = "Preço executado é obrigatório")
    private BigDecimal precoExecutado;

    @NotNull(message = "Quantidade é obrigatória")
    private BigDecimal quantidade;

    @NotNull(message = "Data de execução é obrigatória")
    private LocalDateTime dataExecucao;

    private String observacao;
}

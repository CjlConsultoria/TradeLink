package com.example.CJLInvestimentos.dtos.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class CriarFaturaRequest {

    @NotNull(message = "Data de vencimento é obrigatória")
    private Instant dataVencimento;

    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor deve ser positivo")
    private BigDecimal valor;

    /** Descrição do serviço na fatura (ex.: "Assinatura mensal - Plano Premium"). */
    private String descricaoServico;

    private String observacao;
}

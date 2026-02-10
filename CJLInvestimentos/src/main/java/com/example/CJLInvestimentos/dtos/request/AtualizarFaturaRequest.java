package com.example.CJLInvestimentos.dtos.request;

import com.example.CJLInvestimentos.entities.enums.FormaPagamento;
import com.example.CJLInvestimentos.entities.enums.StatusFatura;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class AtualizarFaturaRequest {

    private Instant dataVencimento;

    /** Data em que a fatura foi paga (quando status = PAGA). */
    private Instant dataPagamento;

    @DecimalMin(value = "0.01", message = "Valor deve ser positivo")
    private BigDecimal valor;

    private StatusFatura status;
    private FormaPagamento formaPagamento;

    private String descricaoServico;
    private String observacao;
}

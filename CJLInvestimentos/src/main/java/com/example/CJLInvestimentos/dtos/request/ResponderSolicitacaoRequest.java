package com.example.CJLInvestimentos.dtos.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ResponderSolicitacaoRequest {
    /** true = aceitar, false = recusar */
    private Boolean aceitar;
    /** Preço ajustado (opcional, usa o base se null). */
    private BigDecimal precoFinal;
    /** Mensagem opcional do consultor. */
    private String mensagemConsultor;
}

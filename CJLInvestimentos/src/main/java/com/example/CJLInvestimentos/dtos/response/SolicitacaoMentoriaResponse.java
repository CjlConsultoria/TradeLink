package com.example.CJLInvestimentos.dtos.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class SolicitacaoMentoriaResponse {
    private Long id;
    private Long clienteId;
    private String clienteNome;
    private String clienteEmail;
    private Long empresaId;
    private String empresaNome;
    private String status;
    private BigDecimal precoProposto;
    private BigDecimal precoFinal;
    private String mensagemCliente;
    private String mensagemConsultor;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

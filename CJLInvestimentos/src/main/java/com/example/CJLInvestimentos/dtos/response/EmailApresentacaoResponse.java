package com.example.CJLInvestimentos.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailApresentacaoResponse {
    private Long id;
    private String nomeDestinatario;
    private String emailDestinatario;
    private String roleDestinatario;
    private String tipoTemplate;
    private String nomeEnviadoPor;
    private LocalDateTime enviadoEm;
    private String status;
    private String erro;
}

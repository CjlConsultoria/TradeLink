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
public class EmailMarketingResponse {
    private Long id;
    private String email;
    private String nome;
    private String assunto;
    private String status;
    private String erro;
    private String campanha;
    private String enviadoPor;
    private LocalDateTime enviadoEm;
}

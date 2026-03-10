package com.example.CJLInvestimentos.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FaqResponse {
    private Long id;
    private String pergunta;
    private String resposta;
    private String categoria;
    private Integer ordem;
    private Boolean ativo;
}

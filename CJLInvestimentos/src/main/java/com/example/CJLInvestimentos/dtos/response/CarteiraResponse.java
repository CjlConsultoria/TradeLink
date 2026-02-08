package com.example.CJLInvestimentos.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarteiraResponse {
    private Long id;
    private String nome;
    private String descricao;
    private Long empresaId;
    private String empresaNome;
    private Long consultorId;
    private String consultorNome;
    private Boolean ativa;
    private LocalDateTime createdAt;
    private Integer totalClientes;
    private Integer totalRecomendacoes;
}

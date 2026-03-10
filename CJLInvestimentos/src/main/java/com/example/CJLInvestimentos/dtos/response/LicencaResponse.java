package com.example.CJLInvestimentos.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LicencaResponse {
    private Integer maxUsuarios;
    private Long totalUsuarios;
    private Long totalClientes;
    private Boolean podeConvidar;
    private String planoNome;
}

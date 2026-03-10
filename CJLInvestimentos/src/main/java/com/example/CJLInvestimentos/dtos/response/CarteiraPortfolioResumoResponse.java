package com.example.CJLInvestimentos.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarteiraPortfolioResumoResponse {
    private Long carteiraId;
    private String carteiraNome;
    private List<PortfolioResumoResponse> clientes;
}

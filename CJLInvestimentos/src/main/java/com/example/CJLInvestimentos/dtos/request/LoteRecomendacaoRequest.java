package com.example.CJLInvestimentos.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoteRecomendacaoRequest {
    private List<LoteRecomendacaoItem> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoteRecomendacaoItem {
        private Long carteiraId;
        private Long clienteId;
    }
}

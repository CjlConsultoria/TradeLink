package com.example.CJLInvestimentos.dtos.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class MarketplaceConsultorResponse {
    private Long empresaId;
    private String nome;
    private String marketplaceDescricao;
    private String marketplaceEspecializacao;
    private String marketplaceExperiencia;
    private String marketplaceFotoUrl;
    private BigDecimal marketplacePrecoBase;
}

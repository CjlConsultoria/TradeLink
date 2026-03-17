package com.example.CJLInvestimentos.dtos.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class MarketplacePerfilResponse {
    private Long empresaId;
    private String nome;
    private Boolean marketplaceVisivel;
    private String marketplaceDescricao;
    private String marketplaceEspecializacao;
    private String marketplaceExperiencia;
    private String marketplaceFotoUrl;
    private BigDecimal marketplacePrecoBase;
    /** Informativo: taxa da plataforma em percentual. */
    private BigDecimal taxaPlataforma;
}

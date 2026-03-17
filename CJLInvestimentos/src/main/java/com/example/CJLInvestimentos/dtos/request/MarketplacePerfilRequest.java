package com.example.CJLInvestimentos.dtos.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class MarketplacePerfilRequest {
    private Boolean marketplaceVisivel;
    private String marketplaceDescricao;
    private String marketplaceEspecializacao;
    private String marketplaceExperiencia;
    private String marketplaceRedeSocial;
    private BigDecimal marketplacePrecoBase;
}

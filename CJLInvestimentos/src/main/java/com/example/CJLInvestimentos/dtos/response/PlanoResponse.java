package com.example.CJLInvestimentos.dtos.response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanoResponse {
    private Long id;
    private String nome;
    /** Tipo do plano: CONSULTOR ou AUTO_GESTAO */
    private String tipo;
    private Integer maxUsuarios;
    private BigDecimal preco;
    private Boolean ativo;
    private LocalDateTime createdAt;
    /** ID do preço no Stripe (price_xxx) para checkout. */
    private String stripePriceId;
}
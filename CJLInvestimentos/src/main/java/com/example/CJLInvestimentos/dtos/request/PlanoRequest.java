package com.example.CJLInvestimentos.dtos.request;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
@Data
public class PlanoRequest {
    @NotBlank(message = "Nome é obrigatório")
    private String nome;
    @NotNull(message = "Máximo de usuários é obrigatório")
    @Min(value = 1, message = "Mínimo de 1 usuário")
    private Integer maxUsuarios;
    @NotNull(message = "Preço é obrigatório")
    private BigDecimal preco;
    /** ID do preço no Stripe (price_xxx) para cobrança recorrente mensal. */
    private String stripePriceId;
}
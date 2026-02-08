package com.example.CJLInvestimentos.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutSessionResponse {
    /** URL para redirecionar o usuário ao Stripe Checkout. */
    private String checkoutUrl;
}

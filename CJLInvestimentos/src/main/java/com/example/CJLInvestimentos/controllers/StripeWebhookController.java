package com.example.CJLInvestimentos.controllers;

import com.example.CJLInvestimentos.services.StripePaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookController {

    private final StripePaymentService stripePaymentService;

    /**
     * Webhook do Stripe para eventos de assinatura (checkout concluído, assinatura atualizada/cancelada).
     * Configurar no Stripe Dashboard: https://dashboard.stripe.com/webhooks
     * URL: https://seu-dominio.com/api/webhooks/stripe
     * Eventos: checkout.session.completed, customer.subscription.updated, customer.subscription.deleted
     */
    @PostMapping("/stripe")
    public ResponseEntity<Void> stripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature) {
        try {
            stripePaymentService.handleWebhook(payload, signature);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Erro ao processar webhook Stripe", e);
            return ResponseEntity.status(400).build();
        }
    }
}

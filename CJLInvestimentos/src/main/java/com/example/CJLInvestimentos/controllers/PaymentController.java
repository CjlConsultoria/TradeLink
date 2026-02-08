package com.example.CJLInvestimentos.controllers;

import com.example.CJLInvestimentos.dtos.request.MarcarPagoRequest;
import com.example.CJLInvestimentos.dtos.response.CheckoutSessionResponse;
import com.example.CJLInvestimentos.dtos.response.FaturaResponse;
import com.example.CJLInvestimentos.services.FaturaService;
import com.example.CJLInvestimentos.services.StripePaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin-max")
@RequiredArgsConstructor
public class PaymentController {

    private final StripePaymentService stripePaymentService;
    private final FaturaService faturaService;

    /**
     * Cria uma sessão de checkout Stripe para a empresa assinar o plano (pagamento recorrente).
     * Retorna a URL para redirecionar o usuário ao Stripe Checkout.
     */
    @PostMapping("/empresas/{empresaId}/checkout")
    public ResponseEntity<CheckoutSessionResponse> createCheckout(
            @PathVariable Long empresaId,
            @RequestBody Map<String, Long> body) {
        Long planoId = body != null ? body.get("planoId") : null;
        if (planoId == null) {
            return ResponseEntity.badRequest().build();
        }
        String url = stripePaymentService.createCheckoutSession(empresaId, planoId);
        return ResponseEntity.ok(CheckoutSessionResponse.builder().checkoutUrl(url).build());
    }

    @GetMapping("/pagamentos/configurado")
    public ResponseEntity<Map<String, Boolean>> pagamentoConfigurado() {
        return ResponseEntity.ok(Map.of("configurado", stripePaymentService.isConfigured()));
    }

    @PostMapping("/empresas/{empresaId}/faturas/marcar-pago")
    public ResponseEntity<FaturaResponse> marcarComoPago(
            @PathVariable Long empresaId,
            @RequestBody(required = false) MarcarPagoRequest request) {
        if (request == null) request = new MarcarPagoRequest();
        return ResponseEntity.ok(faturaService.marcarComoPagoManual(empresaId, request));
    }
}

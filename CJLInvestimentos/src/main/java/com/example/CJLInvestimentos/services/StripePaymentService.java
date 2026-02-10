package com.example.CJLInvestimentos.services;

import com.example.CJLInvestimentos.entities.Empresa;
import com.example.CJLInvestimentos.entities.Plano;
import com.example.CJLInvestimentos.entities.enums.FormaPagamento;
import com.example.CJLInvestimentos.entities.enums.SubscriptionStatus;
import com.example.CJLInvestimentos.entities.User;
import com.example.CJLInvestimentos.exceptions.BusinessException;
import com.example.CJLInvestimentos.repositories.EmpresaRepository;
import com.example.CJLInvestimentos.repositories.PlanoRepository;
import com.example.CJLInvestimentos.repositories.UserRepository;
import com.stripe.Stripe;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.model.Subscription;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class StripePaymentService {

    @Value("${stripe.api-key:}")
    private String stripeApiKey;

    @Value("${stripe.publishable-key:}")
    private String stripePublishableKey;

    @Value("${stripe.webhook-secret:}")
    private String webhookSecret;

    @Value("${stripe.success-url:http://localhost:5173/admin-max/empresas}")
    private String successUrl;

    @Value("${stripe.cancel-url:http://localhost:5173/admin-max/empresas}")
    private String cancelUrl;

    @Value("${stripe.success-url-pagamento-consultor:http://localhost:5173/consultor/faturas?pagamento=ok}")
    private String successUrlPagamentoConsultor;

    @Value("${stripe.cancel-url-pagamento-consultor:http://localhost:5173/consultor/faturas}")
    private String cancelUrlPagamentoConsultor;

    @Value("${stripe.success-url-pagamento-cliente:http://localhost:5173/cliente/faturas?pagamento=ok}")
    private String successUrlPagamentoCliente;

    @Value("${stripe.cancel-url-pagamento-cliente:http://localhost:5173/cliente/faturas}")
    private String cancelUrlPagamentoCliente;

    private final EmpresaRepository empresaRepository;
    private final PlanoRepository planoRepository;
    private final FaturaService faturaService;
    private final UserRepository userRepository;

    public StripePaymentService(EmpresaRepository empresaRepository, PlanoRepository planoRepository, FaturaService faturaService, UserRepository userRepository) {
        this.empresaRepository = empresaRepository;
        this.planoRepository = planoRepository;
        this.faturaService = faturaService;
        this.userRepository = userRepository;
    }

    @PostConstruct
    void init() {
        if (stripeApiKey != null && !stripeApiKey.isBlank()) {
            Stripe.apiKey = stripeApiKey;
        }
    }

    public boolean isConfigured() {
        return stripeApiKey != null && !stripeApiKey.isBlank();
    }

    public String getPublishableKey() {
        return stripePublishableKey != null ? stripePublishableKey : "";
    }

    public String getSuccessUrlPagamentoConsultor() { return successUrlPagamentoConsultor; }
    public String getCancelUrlPagamentoConsultor() { return cancelUrlPagamentoConsultor; }
    public String getSuccessUrlPagamentoCliente() { return successUrlPagamentoCliente; }
    public String getCancelUrlPagamentoCliente() { return cancelUrlPagamentoCliente; }

    /**
     * Confirma o pagamento a partir do session_id (retorno do Stripe Checkout).
     * Usado quando o webhook não foi recebido (ex.: dev local). Idempotente.
     * @param sessionId ID da sessão (cs_xxx)
     * @param empresaIdDoUsuario empresa do usuário logado (deve coincidir com a da sessão)
     * @return true se a fatura foi registrada ou já existia
     */
    public boolean confirmarPagamentoPorSessionId(String sessionId, Long empresaIdDoUsuario) {
        if (!isConfigured() || sessionId == null || sessionId.isBlank()) return false;
        try {
            Session session = Session.retrieve(sessionId);
            String empresaIdStr = session.getClientReferenceId();
            if (empresaIdStr == null && session.getMetadata() != null) empresaIdStr = session.getMetadata().get("empresa_id");
            if (empresaIdStr == null) {
                log.warn("confirmarPagamentoPorSessionId: sessão sem empresa_id");
                return false;
            }
            Long empresaId = Long.parseLong(empresaIdStr);
            if (!empresaId.equals(empresaIdDoUsuario)) {
                log.warn("confirmarPagamentoPorSessionId: empresa da sessão não coincide com o usuário");
                return false;
            }
            if (!"payment".equals(session.getMode())) return false;
            if (!"paid".equals(session.getPaymentStatus())) return false;
            String piId = getPaymentIntentIdFromSession(session);
            Long amountTotal = session.getAmountTotal();
            if (piId == null || piId.isBlank() || amountTotal == null || amountTotal <= 0) return false;
            BigDecimal amount = BigDecimal.valueOf(amountTotal).divide(BigDecimal.valueOf(100));
            FormaPagamento forma = FormaPagamento.CARTAO;
            faturaService.registrarPagamentoExterno(empresaId, amount, forma, piId);
            log.info("Fatura registrada via confirmar-retorno (session_id) para empresa {}", empresaId);
            return true;
        } catch (Exception e) {
            log.error("Erro ao confirmar pagamento por session_id", e);
            return false;
        }
    }

    /**
     * Cria uma sessão de Checkout do Stripe para assinatura mensal da empresa ao plano.
     * Redirecione o usuário para checkoutUrl.
     */
    public String createCheckoutSession(Long empresaId, Long planoId) {
        if (!isConfigured()) {
            throw new BusinessException("Pagamentos não estão configurados. Defina stripe.api-key.");
        }

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new BusinessException("Empresa não encontrada"));
        Plano plano = planoRepository.findById(planoId)
                .orElseThrow(() -> new BusinessException("Plano não encontrado"));

        if (plano.getStripePriceId() == null || plano.getStripePriceId().isBlank()) {
            throw new BusinessException("Plano não possui preço configurado no Stripe. Informe o Stripe Price ID no plano.");
        }

        try {
            SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                    .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}&empresa_id=" + empresaId)
                    .setCancelUrl(cancelUrl)
                    .setClientReferenceId(empresaId.toString())
                    .putMetadata("empresa_id", empresaId.toString())
                    .putMetadata("plano_id", planoId.toString())
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setPrice(plano.getStripePriceId())
                                    .setQuantity(1L)
                                    .build()
                    );

            if (empresa.getStripeCustomerId() != null && !empresa.getStripeCustomerId().isBlank()) {
                paramsBuilder.setCustomer(empresa.getStripeCustomerId());
            }

            Session session = Session.create(paramsBuilder.build());
            return session.getUrl();
        } catch (Exception e) {
            log.error("Erro ao criar sessão Stripe", e);
            throw new BusinessException("Erro ao iniciar checkout: " + e.getMessage());
        }
    }

    /**
     * Cria uma sessão de Checkout para pagamento único (cartão ou boleto) da próxima fatura da empresa.
     * successUrl e cancelUrl variam conforme consultor ou cliente.
     */
    public String createCheckoutSessionPagamentoUnicoPorUsuarioId(Long usuarioId, String successUrl, String cancelUrl) {
        User user = userRepository.findByIdWithEmpresa(usuarioId).orElseThrow(() -> new BusinessException("Usuário não encontrado"));
        if (user.getEmpresa() == null) throw new BusinessException("Usuário sem empresa vinculada.");
        return createCheckoutSessionPagamentoUnico(user.getEmpresa().getId(), successUrl, cancelUrl);
    }

    /**
     * Cria sessão Stripe Checkout para pagamento único em BRL (cartão + boleto).
     */
    public String createCheckoutSessionPagamentoUnico(Long empresaId, String successUrl, String cancelUrl) {
        if (!isConfigured()) {
            throw new BusinessException("Pagamentos não estão configurados. Defina stripe.api-key.");
        }
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new BusinessException("Empresa não encontrada"));
        Plano plano = empresa.getPlano();
        if (plano == null) throw new BusinessException("Empresa não possui plano atribuído.");
        long amountCentavos = plano.getPreco().multiply(BigDecimal.valueOf(100)).longValue();
        if (amountCentavos < 100) amountCentavos = 100;
        try {
            SessionCreateParams.LineItem.PriceData.ProductData productData =
                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                            .setName("Assinatura - " + (plano.getNome() != null ? plano.getNome() : "Mensalidade"))
                            .build();
            SessionCreateParams.LineItem.PriceData priceData = SessionCreateParams.LineItem.PriceData.builder()
                    .setCurrency("brl")
                    .setUnitAmount(amountCentavos)
                    .setProductData(productData)
                    .build();
            SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                    .setPriceData(priceData)
                    .setQuantity(1L)
                    .build();
            SessionCreateParams.PaymentIntentData paymentIntentData = SessionCreateParams.PaymentIntentData.builder()
                    .putMetadata("empresa_id", empresaId.toString())
                    .putMetadata("plano_id", plano.getId().toString())
                    .build();
            String successWithSessionId = successUrl + (successUrl.contains("?") ? "&" : "?") + "session_id={CHECKOUT_SESSION_ID}";
            SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successWithSessionId)
                    .setCancelUrl(cancelUrl)
                    .setClientReferenceId(empresaId.toString())
                    .putMetadata("empresa_id", empresaId.toString())
                    .addLineItem(lineItem)
                    .setPaymentIntentData(paymentIntentData)
                    .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                    .addPaymentMethodType(SessionCreateParams.PaymentMethodType.BOLETO);
            if (empresa.getStripeCustomerId() != null && !empresa.getStripeCustomerId().isBlank()) {
                paramsBuilder.setCustomer(empresa.getStripeCustomerId());
            }
            Session session = Session.create(paramsBuilder.build());
            return session.getUrl();
        } catch (Exception e) {
            log.error("Erro ao criar Checkout pagamento único", e);
            throw new BusinessException("Erro ao iniciar pagamento: " + e.getMessage());
        }
    }

    /**
     * Cria um PaymentIntent para pagamento embutido (cartão + PIX + boleto) na tela do sistema.
     * Retorna clientSecret e paymentIntentId para o frontend montar o Payment Element do Stripe.
     */
    public Map<String, String> createPaymentIntentPagamentoUnicoPorUsuarioId(Long usuarioId) {
        User user = userRepository.findByIdWithEmpresa(usuarioId).orElseThrow(() -> new BusinessException("Usuário não encontrado"));
        if (user.getEmpresa() == null) {
            throw new BusinessException("Usuário sem empresa vinculada.");
        }
        return createPaymentIntentPagamentoUnico(user.getEmpresa().getId());
    }

    /**
     * Cria um PaymentIntent com cartão, PIX e boleto para a empresa (pagamento único, exibido no Payment Element).
     */
    public Map<String, String> createPaymentIntentPagamentoUnico(Long empresaId) {
        if (!isConfigured()) {
            throw new BusinessException("Pagamentos não estão configurados. Defina stripe.api-key.");
        }
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new BusinessException("Empresa não encontrada"));
        Plano plano = empresa.getPlano();
        if (plano == null) {
            throw new BusinessException("Empresa não possui plano atribuído.");
        }
        long amountCentavos = plano.getPreco().multiply(BigDecimal.valueOf(100)).longValue();
        if (amountCentavos < 100) amountCentavos = 100;
        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountCentavos)
                    .setCurrency("brl")
                    .putMetadata("empresa_id", empresaId.toString())
                    .putMetadata("plano_id", plano.getId().toString())
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build())
                    .build();
            PaymentIntent pi = PaymentIntent.create(params);
            Map<String, String> result = new HashMap<>();
            result.put("clientSecret", pi.getClientSecret());
            result.put("paymentIntentId", pi.getId());
            result.put("publishableKey", getPublishableKey());
            return result;
        } catch (Exception e) {
            log.error("Erro ao criar PaymentIntent (pagamento embutido)", e);
            throw new BusinessException("Erro ao iniciar pagamento: " + e.getMessage());
        }
    }

    /**
     * Cria um PaymentIntent PIX para o usuário (consultor/cliente) pagar a mensalidade da sua empresa.
     * Carrega a empresa dentro da transação para evitar LazyInitializationException.
     */
    public Map<String, String> createPaymentIntentPixPorUsuarioId(Long usuarioId) {
        User user = userRepository.findByIdWithEmpresa(usuarioId).orElseThrow(() -> new BusinessException("Usuário não encontrado"));
        if (user.getEmpresa() == null) {
            throw new BusinessException("Usuário sem empresa vinculada.");
        }
        return createPaymentIntentPix(user.getEmpresa().getId());
    }

    /**
     * Cria um PaymentIntent PIX para a empresa pagar a mensalidade atual.
     * Retorna clientSecret e paymentIntentId para o frontend exibir QR Code PIX.
     */
    public Map<String, String> createPaymentIntentPix(Long empresaId) {
        if (!isConfigured()) {
            throw new BusinessException("Pagamentos não estão configurados. Defina stripe.api-key.");
        }
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new BusinessException("Empresa não encontrada"));
        Plano plano = empresa.getPlano();
        if (plano == null) {
            throw new BusinessException("Empresa não possui plano atribuído.");
        }
        long amountCentavos = plano.getPreco().multiply(BigDecimal.valueOf(100)).longValue();
        if (amountCentavos < 100) amountCentavos = 100;
        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountCentavos)
                    .setCurrency("brl")
                    .addPaymentMethodType("pix")
                    .putMetadata("empresa_id", empresaId.toString())
                    .putMetadata("plano_id", plano.getId().toString())
                    .build();
            PaymentIntent pi = PaymentIntent.create(params);
            Map<String, String> result = new HashMap<>();
            result.put("clientSecret", pi.getClientSecret());
            result.put("paymentIntentId", pi.getId());
            return result;
        } catch (Exception e) {
            log.error("Erro ao criar PaymentIntent PIX", e);
            throw new BusinessException("Erro ao gerar PIX: " + e.getMessage());
        }
    }

    /**
     * Processa evento do webhook Stripe (assinatura criada/atualizada/cancelada, fatura paga, PIX pago).
     */
    public void handleWebhook(String payload, String signature) {
        if (webhookSecret == null || webhookSecret.isBlank()) {
            log.warn("Webhook Stripe ignorado: configure stripe.webhook-secret no application.properties para gerar faturas ao receber pagamento.");
            return;
        }
        Event event;
        try {
            event = Webhook.constructEvent(payload, signature, webhookSecret);
        } catch (Exception e) {
            log.error("Assinatura do webhook Stripe inválida", e);
            throw new BusinessException("Webhook inválido");
        }

        String eventType = event.getType();
        log.info("Webhook Stripe: processando evento {}", eventType);
        switch (eventType) {
            case "checkout.session.completed" -> handleCheckoutSessionCompleted(event);
            case "customer.subscription.updated" -> handleSubscriptionUpdated(event);
            case "customer.subscription.deleted" -> handleSubscriptionDeleted(event);
            case "invoice.paid" -> handleInvoicePaid(event);
            case "payment_intent.succeeded" -> handlePaymentIntentSucceeded(event);
            default -> log.debug("Evento Stripe ignorado: {}", eventType);
        }
    }

    private void handlePaymentIntentSucceeded(Event event) {
        PaymentIntent pi = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
        if (pi == null) {
            log.warn("payment_intent.succeeded: objeto PaymentIntent nulo");
            return;
        }
        Map<String, String> meta = pi.getMetadata();
        if (meta == null || !meta.containsKey("empresa_id")) {
            log.warn("payment_intent.succeeded: PaymentIntent {} sem metadata empresa_id (não é pagamento de assinatura?)", pi.getId());
            return;
        }
        Long empresaId = Long.parseLong(meta.get("empresa_id"));
        BigDecimal amount = BigDecimal.valueOf(pi.getAmount()).divide(BigDecimal.valueOf(100));
        FormaPagamento forma = FormaPagamento.CARTAO;
        if (pi.getPaymentMethodTypes() != null) {
            if (pi.getPaymentMethodTypes().stream().anyMatch(t -> "boleto".equalsIgnoreCase(t))) forma = FormaPagamento.BOLETO;
            else if (pi.getPaymentMethodTypes().stream().anyMatch(t -> "pix".equalsIgnoreCase(t))) forma = FormaPagamento.PIX;
        }
        faturaService.registrarPagamentoExterno(empresaId, amount, forma, pi.getId());
        log.info("Fatura registrada via payment_intent.succeeded: empresa={}, forma={}, valor={}", empresaId, forma, amount);
    }

    private void handleCheckoutSessionCompleted(Event event) {
        Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
        if (session == null) return;

        String empresaIdStr = session.getClientReferenceId();
        if (empresaIdStr == null) empresaIdStr = session.getMetadata() != null ? session.getMetadata().get("empresa_id") : null;
        if (empresaIdStr == null) {
            log.warn("checkout.session.completed sem empresa_id");
            return;
        }

        Long empresaId = Long.parseLong(empresaIdStr);
        Empresa empresa = empresaRepository.findById(empresaId).orElse(null);
        if (empresa == null) return;

        if (session.getCustomer() != null) {
            empresa.setStripeCustomerId(session.getCustomer());
        }
        String subId = session.getSubscription();
        if (subId != null) {
            empresa.setStripeSubscriptionId(subId);
            empresa.setSubscriptionStatus(SubscriptionStatus.ACTIVE);
            try {
                Subscription sub = Subscription.retrieve(subId);
                if (sub.getCurrentPeriodEnd() != null) {
                    empresa.setCurrentPeriodEnd(Instant.ofEpochSecond(sub.getCurrentPeriodEnd()));
                }
            } catch (Exception e) {
                log.warn("Erro ao obter período da assinatura {}", subId, e);
            }
            empresaRepository.save(empresa);
            log.info("Empresa {} assinatura ativada (Stripe).", empresaId);
            return;
        }
        // Pagamento único (cartão/boleto): quando pago na hora (cartão) ou quando boleto já consta como pago
        if ("payment".equals(session.getMode())) {
            String paymentStatus = session.getPaymentStatus();
            String piId = getPaymentIntentIdFromSession(session);
            Long amountTotal = session.getAmountTotal();
            if (amountTotal != null && amountTotal > 0 && piId != null && !piId.isBlank()) {
                BigDecimal amount = BigDecimal.valueOf(amountTotal).divide(BigDecimal.valueOf(100));
                FormaPagamento forma = FormaPagamento.CARTAO;
                if ("paid".equals(paymentStatus)) {
                    faturaService.registrarPagamentoExterno(empresaId, amount, forma, piId);
                    log.info("Fatura registrada via checkout.session.completed para empresa {} (payment_intent={})", empresaId, piId);
                }
            } else if ("paid".equals(paymentStatus) && (piId == null || piId.isBlank())) {
                log.warn("checkout.session.completed paid mas payment_intent ausente para empresa {}", empresaId);
            }
        }
        empresaRepository.save(empresa);
    }

    /** Extrai o ID do PaymentIntent da Session (Stripe SDK pode retornar String ou ExpandableField). */
    private String getPaymentIntentIdFromSession(Session session) {
        Object piRef = session.getPaymentIntent();
        if (piRef == null) return null;
        if (piRef instanceof String) return (String) piRef;
        try {
            java.lang.reflect.Method getId = piRef.getClass().getMethod("getId");
            Object id = getId.invoke(piRef);
            return id != null ? id.toString() : null;
        } catch (Exception e) {
            log.debug("Não foi possível obter getId do payment_intent: {}", e.getMessage());
            return null;
        }
    }

    private void handleSubscriptionUpdated(Event event) {
        Subscription sub = (Subscription) event.getDataObjectDeserializer().getObject().orElse(null);
        if (sub == null) return;

        empresaRepository.findByStripeSubscriptionId(sub.getId()).ifPresent(empresa -> {
            empresa.setCurrentPeriodEnd(sub.getCurrentPeriodEnd() != null ? Instant.ofEpochSecond(sub.getCurrentPeriodEnd()) : null);
            String status = sub.getStatus();
            if ("active".equals(status)) {
                empresa.setSubscriptionStatus(SubscriptionStatus.ACTIVE);
            } else if ("past_due".equals(status) || "unpaid".equals(status)) {
                empresa.setSubscriptionStatus(SubscriptionStatus.PAST_DUE);
            } else if ("canceled".equals(status) || "incomplete_expired".equals(status)) {
                empresa.setSubscriptionStatus(SubscriptionStatus.CANCELLED);
            }
            empresaRepository.save(empresa);
        });
    }

    private void handleSubscriptionDeleted(Event event) {
        Subscription sub = (Subscription) event.getDataObjectDeserializer().getObject().orElse(null);
        if (sub == null) return;

        empresaRepository.findByStripeSubscriptionId(sub.getId()).ifPresent(empresa -> {
            empresa.setSubscriptionStatus(SubscriptionStatus.CANCELLED);
            empresa.setStripeSubscriptionId(null);
            empresa.setCurrentPeriodEnd(null);
            empresaRepository.save(empresa);
            log.info("Assinatura cancelada para empresa {}", empresa.getId());
        });
    }

    private void handleInvoicePaid(Event event) {
        // Opcional: atualizar currentPeriodEnd a partir da invoice se necessário
    }
}

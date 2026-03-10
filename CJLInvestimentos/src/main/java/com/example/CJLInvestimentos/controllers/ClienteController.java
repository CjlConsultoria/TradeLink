package com.example.CJLInvestimentos.controllers;

import com.example.CJLInvestimentos.dtos.response.ChatConversaResponse;
import com.example.CJLInvestimentos.dtos.response.ChatMensagemResponse;
import com.example.CJLInvestimentos.dtos.request.AtivoClienteRequest;
import com.example.CJLInvestimentos.dtos.request.MarcarResolvidoRequest;
import com.example.CJLInvestimentos.dtos.request.OperacaoClienteRequest;
import com.example.CJLInvestimentos.dtos.response.AtivoClienteResponse;
import com.example.CJLInvestimentos.dtos.response.CarteiraResponse;
import com.example.CJLInvestimentos.dtos.response.CotacaoResponse;
import com.example.CJLInvestimentos.dtos.response.DashboardResponse;
import com.example.CJLInvestimentos.dtos.response.OperacaoClienteResponse;
import com.example.CJLInvestimentos.dtos.response.FaturasComProximaResponse;
import com.example.CJLInvestimentos.dtos.response.PageResponse;
import com.example.CJLInvestimentos.dtos.response.PortfolioResumoResponse;
import com.example.CJLInvestimentos.dtos.response.RebalanceamentoClienteResponse;
import com.example.CJLInvestimentos.dtos.response.RecomendacaoResponse;
import com.example.CJLInvestimentos.dtos.response.RelatorioClienteOperacaoResponse;
import com.example.CJLInvestimentos.dtos.response.ResumoRelatorioClienteResponse;
import com.example.CJLInvestimentos.entities.User;
import com.example.CJLInvestimentos.exceptions.BusinessException;
import com.example.CJLInvestimentos.entities.enums.StatusRecomendacao;
import com.example.CJLInvestimentos.entities.enums.TipoRecomendacao;
import com.example.CJLInvestimentos.repositories.UserRepository;
import com.example.CJLInvestimentos.services.CarteiraService;
import com.example.CJLInvestimentos.services.CotacaoService;
import com.example.CJLInvestimentos.services.FaturaService;
import com.example.CJLInvestimentos.services.OperacaoClienteService;
import com.example.CJLInvestimentos.services.RecomendacaoService;
import com.example.CJLInvestimentos.services.RelatorioClienteService;
import com.example.CJLInvestimentos.services.RelatorioPdfService;
import com.example.CJLInvestimentos.services.PortfolioService;
import com.example.CJLInvestimentos.services.RebalanceamentoService;
import com.example.CJLInvestimentos.services.MovimentacaoService;
import com.example.CJLInvestimentos.services.NotificationAsyncRunner;
import com.example.CJLInvestimentos.services.SnapshotService;
import com.example.CJLInvestimentos.services.AutoGestaoService;
import com.example.CJLInvestimentos.services.ChatService;
import com.example.CJLInvestimentos.services.StripePaymentService;
import com.example.CJLInvestimentos.dtos.response.ClienteExcluidoStatusResponse;
import com.example.CJLInvestimentos.dtos.request.MovimentacaoRequest;
import com.example.CJLInvestimentos.dtos.response.MovimentacaoResponse;
import com.example.CJLInvestimentos.dtos.response.SaldoResponse;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/cliente")
@RequiredArgsConstructor
public class ClienteController {

    private final CarteiraService carteiraService;
    private final RecomendacaoService recomendacaoService;
    private final CotacaoService cotacaoService;
    private final OperacaoClienteService operacaoClienteService;
    private final UserRepository userRepository;
    private final FaturaService faturaService;
    private final StripePaymentService stripePaymentService;
    private final RelatorioClienteService relatorioClienteService;
    private final RelatorioPdfService relatorioPdfService;
    private final PortfolioService portfolioService;
    private final RebalanceamentoService rebalanceamentoService;
    private final SnapshotService snapshotService;
    private final MovimentacaoService movimentacaoService;
    private final NotificationAsyncRunner notificationAsyncRunner;
    private final AutoGestaoService autoGestaoService;
    private final ChatService chatService;

    private User getUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    @GetMapping("/carteiras")
    public ResponseEntity<List<CarteiraResponse>> listarCarteiras(
            @AuthenticationPrincipal UserDetails userDetails) {
        User cliente = getUser(userDetails);
        return ResponseEntity.ok(carteiraService.listarPorCliente(cliente));
    }

    @GetMapping("/carteiras/{id}")
    public ResponseEntity<CarteiraResponse> buscarCarteira(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        User cliente = getUser(userDetails);
        return ResponseEntity.ok(carteiraService.buscarPorId(id, cliente));
    }

    @GetMapping("/carteiras/{carteiraId}/recomendacoes")
    public ResponseEntity<List<RecomendacaoResponse>> listarRecomendacoes(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long carteiraId) {
        User cliente = getUser(userDetails);
        return ResponseEntity.ok(recomendacaoService.listarPorCarteira(carteiraId, cliente));
    }

    @GetMapping("/recomendacoes")
    public ResponseEntity<PageResponse<RecomendacaoResponse>> listarRecomendacoesFiltrado(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Long carteiraId,
            @RequestParam(required = false) TipoRecomendacao tipo,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Boolean resolvido,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        User cliente = getUser(userDetails);
        return ResponseEntity.ok(recomendacaoService.listarParaClienteFiltrado(cliente, carteiraId, tipo, nome, resolvido, page, size));
    }

    @PostMapping("/recomendacoes/{id}/operacoes")
    public ResponseEntity<OperacaoClienteResponse> registrarOperacao(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody OperacaoClienteRequest request) {
        User cliente = getUser(userDetails);
        OperacaoClienteResponse resp = operacaoClienteService.registrar(id, request, cliente);
        // Notifica consultor sobre operacao registrada
        try {
            String tipo = request.getTipo() != null ? request.getTipo().name() : "-";
            String qtd = request.getQuantidade() != null ? request.getQuantidade().toPlainString() : "-";
            String preco = request.getPrecoExecutado() != null ? request.getPrecoExecutado().toPlainString() : "-";
            notificationAsyncRunner.notificarOperacaoRegistradaAsync(id, cliente.getId(), tipo, "ativo", qtd, preco);
        } catch (Exception ignored) {}
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(resp);
    }

    @PutMapping("/recomendacoes/{id}/resolvido")
    public ResponseEntity<RecomendacaoResponse> marcarResolvido(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestBody MarcarResolvidoRequest request) {
        User cliente = getUser(userDetails);
        return ResponseEntity.ok(recomendacaoService.marcarResolvido(id, cliente, request.isResolvido()));
    }

    @GetMapping("/recomendacoes/{id}/operacoes")
    public ResponseEntity<java.util.List<OperacaoClienteResponse>> listarOperacoesRecomendacao(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        User cliente = getUser(userDetails);
        return ResponseEntity.ok(operacaoClienteService.listarPorRecomendacao(id, cliente));
    }

    @PutMapping("/operacoes/{id}")
    public ResponseEntity<OperacaoClienteResponse> atualizarOperacao(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody OperacaoClienteRequest request) {
        User cliente = getUser(userDetails);
        return ResponseEntity.ok(operacaoClienteService.atualizar(id, request, cliente));
    }

    @DeleteMapping("/operacoes/{id}")
    public ResponseEntity<Void> excluirOperacao(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        User cliente = getUser(userDetails);
        operacaoClienteService.excluir(id, cliente);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/operacoes")
    public ResponseEntity<java.util.List<OperacaoClienteResponse>> listarMinhasOperacoes(
            @AuthenticationPrincipal UserDetails userDetails) {
        User cliente = getUser(userDetails);
        return ResponseEntity.ok(operacaoClienteService.listarPorCliente(cliente));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> dashboard(
            @AuthenticationPrincipal UserDetails userDetails) {
        User cliente = getUser(userDetails);

        List<CarteiraResponse> carteiras = carteiraService.listarPorCliente(cliente);

        List<RecomendacaoResponse> todasRecomendacoes = new ArrayList<>();
        int totalAtivas = 0;
        int totalResolvidas = 0;
        for (CarteiraResponse c : carteiras) {
            List<RecomendacaoResponse> recs = recomendacaoService.listarPorCarteira(c.getId(), cliente);
            todasRecomendacoes.addAll(recs);
            totalAtivas += (int) recs.stream()
                    .filter(r -> r.getStatus() == StatusRecomendacao.ATIVA).count();
            totalResolvidas += (int) recs.stream().filter(r -> Boolean.TRUE.equals(r.getResolvido())).count();
        }
        int totalPendentes = todasRecomendacoes.size() - totalResolvidas;

        List<CotacaoResponse> cotacoes = cotacaoService.listarUltimasCotacoes();

        List<RecomendacaoResponse> ultimas = todasRecomendacoes.stream()
                .sorted((a, b) -> {
                    if (b.getCreatedAt() == null) return -1;
                    if (a.getCreatedAt() == null) return 1;
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                })
                .limit(10)
                .toList();

        return ResponseEntity.ok(DashboardResponse.builder()
                .totalCarteiras(carteiras.size())
                .totalRecomendacoesAtivas(totalAtivas)
                .totalPendentes(totalPendentes)
                .totalResolvidas(totalResolvidas)
                .cotacoesRecentes(cotacoes)
                .ultimasRecomendacoes(ultimas)
                .build());
    }

    @GetMapping("/faturas")
    public ResponseEntity<FaturasComProximaResponse> listarFaturas(@AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        // Cliente em auto-gestão (sem empresa): retorna faturas individuais
        if (autoGestaoService.isClienteExcluido(user)) {
            return ResponseEntity.ok(faturaService.getFaturasParaClienteIndividual(user.getId()));
        }
        return ResponseEntity.ok(faturaService.getFaturasComProximaParaUsuario(user.getId()));
    }

    @PostMapping("/checkout-pix")
    public ResponseEntity<Map<String, String>> criarCheckoutPix(@AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        return ResponseEntity.ok(stripePaymentService.createPaymentIntentPixPorUsuarioId(user.getId()));
    }

    @PostMapping("/checkout-cartao-boleto")
    public ResponseEntity<Map<String, String>> criarCheckoutCartaoBoleto(@AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        String url = stripePaymentService.createCheckoutSessionPagamentoUnicoPorUsuarioId(
                user.getId(),
                stripePaymentService.getSuccessUrlPagamentoCliente(),
                stripePaymentService.getCancelUrlPagamentoCliente());
        return ResponseEntity.ok(Map.of("checkoutUrl", url));
    }

    /** Pagamento embutido (cartão, PIX, boleto) na própria tela, sem redirecionar ao Stripe. */
    @PostMapping("/checkout-embedded")
    public ResponseEntity<Map<String, String>> criarCheckoutEmbedded(@AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        return ResponseEntity.ok(stripePaymentService.createPaymentIntentPagamentoUnicoPorUsuarioId(user.getId()));
    }

    @PostMapping("/faturas/confirmar-stripe")
    public ResponseEntity<Map<String, Object>> confirmarPagamentoStripe(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> body) {
        User user = userRepository.findByIdWithEmpresa(getUser(userDetails).getId())
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));
        if (user.getEmpresa() == null) {
            return ResponseEntity.badRequest().body(Map.of("confirmado", false, "erro", "Usuário sem empresa vinculada."));
        }
        String sessionId = body != null ? body.get("sessionId") : null;
        if (sessionId == null || sessionId.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("confirmado", false, "erro", "sessionId obrigatório."));
        }
        boolean ok = stripePaymentService.confirmarPagamentoPorSessionId(sessionId.trim(), user.getEmpresa().getId());
        return ResponseEntity.ok(Map.of("confirmado", ok));
    }

    @PostMapping("/faturas/confirmar-pagamento-embutido")
    public ResponseEntity<Map<String, Object>> confirmarPagamentoEmbutido(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> body) {
        User user = userRepository.findByIdWithEmpresa(getUser(userDetails).getId())
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));
        if (user.getEmpresa() == null) {
            return ResponseEntity.badRequest().body(Map.of("confirmado", false, "erro", "Usuário sem empresa vinculada."));
        }
        String paymentIntentId = body != null ? body.get("paymentIntentId") : null;
        if (paymentIntentId == null || paymentIntentId.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("confirmado", false, "erro", "paymentIntentId obrigatório."));
        }
        boolean ok = stripePaymentService.confirmarPagamentoPorPaymentIntentId(paymentIntentId.trim(), user.getEmpresa().getId());
        return ResponseEntity.ok(Map.of("confirmado", ok));
    }

    @GetMapping("/relatorios/operacoes")
    public ResponseEntity<List<RelatorioClienteOperacaoResponse>> relatorioOperacoes(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String dataDe,
            @RequestParam(required = false) String dataAte) {
        User cliente = getUser(userDetails);
        LocalDate de = dataDe != null && !dataDe.isBlank() ? LocalDate.parse(dataDe) : null;
        LocalDate ate = dataAte != null && !dataAte.isBlank() ? LocalDate.parse(dataAte) : null;
        return ResponseEntity.ok(relatorioClienteService.listarOperacoes(cliente.getId(), de, ate));
    }

    @GetMapping("/relatorios/resumo")
    public ResponseEntity<ResumoRelatorioClienteResponse> relatorioResumo(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String dataDe,
            @RequestParam(required = false) String dataAte) {
        User cliente = getUser(userDetails);
        LocalDate de = dataDe != null && !dataDe.isBlank() ? LocalDate.parse(dataDe) : null;
        LocalDate ate = dataAte != null && !dataAte.isBlank() ? LocalDate.parse(dataAte) : null;
        return ResponseEntity.ok(relatorioClienteService.resumo(cliente.getId(), de, ate));
    }

    @GetMapping(value = "/relatorios/operacoes/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> relatorioOperacoesPdf(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String dataDe,
            @RequestParam(required = false) String dataAte) {
        User cliente = getUser(userDetails);
        LocalDate de = dataDe != null && !dataDe.isBlank() ? LocalDate.parse(dataDe) : null;
        LocalDate ate = dataAte != null && !dataAte.isBlank() ? LocalDate.parse(dataAte) : null;
        List<RelatorioClienteOperacaoResponse> ops = relatorioClienteService.listarOperacoes(cliente.getId(), de, ate);
        byte[] pdf = relatorioPdfService.gerarPdfOperacoesCliente(ops, dataDe, dataAte);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", "relatorio-operacoes-cliente.pdf");
        return ResponseEntity.ok().headers(headers).body(pdf);
    }

    @GetMapping(value = "/relatorios/resumo/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> relatorioResumoPdf(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String dataDe,
            @RequestParam(required = false) String dataAte) {
        User cliente = getUser(userDetails);
        LocalDate de = dataDe != null && !dataDe.isBlank() ? LocalDate.parse(dataDe) : null;
        LocalDate ate = dataAte != null && !dataAte.isBlank() ? LocalDate.parse(dataAte) : null;
        ResumoRelatorioClienteResponse resumo = relatorioClienteService.resumo(cliente.getId(), de, ate);
        byte[] pdf = relatorioPdfService.gerarPdfResumoCliente(resumo, dataDe, dataAte);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", "relatorio-resumo-cliente.pdf");
        return ResponseEntity.ok().headers(headers).body(pdf);
    }

    // ── Alocacao / Rebalanceamento ──

    @GetMapping("/carteiras/{carteiraId}/minha-alocacao")
    public ResponseEntity<RebalanceamentoClienteResponse> minhaAlocacao(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long carteiraId) {
        User cliente = getUser(userDetails);
        RebalanceamentoClienteResponse resp = rebalanceamentoService.analisarClienteParaCliente(carteiraId, cliente);
        if (resp == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/carteiras/{carteiraId}/performance")
    public ResponseEntity<List<Map<String, Object>>> performance(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long carteiraId) {
        User cliente = getUser(userDetails);
        return ResponseEntity.ok(snapshotService.dadosPerformance(carteiraId, cliente.getId()));
    }

    // ── Movimentacoes (Aporte/Saque) ──

    @PostMapping("/carteiras/{carteiraId}/movimentacoes")
    public ResponseEntity<MovimentacaoResponse> registrarMovimentacao(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long carteiraId,
            @Valid @RequestBody MovimentacaoRequest request) {
        User cliente = getUser(userDetails);
        MovimentacaoResponse resp = movimentacaoService.registrar(carteiraId, cliente, request);
        // Notifica consultor sobre nova movimentacao
        try {
            String valor = request.getValor() != null ? "$" + request.getValor().toPlainString() : "-";
            String data = request.getDataMovimentacao() != null ? request.getDataMovimentacao() : "-";
            String tipo = request.getTipo() != null ? request.getTipo() : "-";
            notificationAsyncRunner.notificarNovaMovimentacaoAsync(carteiraId, cliente.getId(), tipo, valor, data);
        } catch (Exception ignored) {}
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/carteiras/{carteiraId}/movimentacoes")
    public ResponseEntity<List<MovimentacaoResponse>> listarMovimentacoes(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long carteiraId) {
        User cliente = getUser(userDetails);
        return ResponseEntity.ok(movimentacaoService.listarPorClienteCarteira(cliente.getId(), carteiraId));
    }

    @GetMapping("/carteiras/{carteiraId}/saldo")
    public ResponseEntity<SaldoResponse> meuSaldo(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long carteiraId) {
        User cliente = getUser(userDetails);
        return ResponseEntity.ok(movimentacaoService.calcularSaldo(carteiraId, cliente.getId()));
    }

    // ── Portfolio ──

    @GetMapping("/portfolio")
    public ResponseEntity<List<AtivoClienteResponse>> listarPortfolio(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(portfolioService.listarAtivos(getUser(userDetails)));
    }

    @GetMapping("/portfolio/resumo")
    public ResponseEntity<PortfolioResumoResponse> resumoPortfolio(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(portfolioService.resumoPortfolio(getUser(userDetails)));
    }

    @PostMapping("/portfolio")
    public ResponseEntity<AtivoClienteResponse> adicionarAtivo(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AtivoClienteRequest request) {
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
                .body(portfolioService.adicionarAtivo(request, getUser(userDetails)));
    }

    @PutMapping("/portfolio/{id}")
    public ResponseEntity<AtivoClienteResponse> atualizarAtivo(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody AtivoClienteRequest request) {
        return ResponseEntity.ok(portfolioService.atualizarAtivo(id, request, getUser(userDetails)));
    }

    @DeleteMapping("/portfolio/{id}")
    public ResponseEntity<Void> removerAtivo(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        portfolioService.removerAtivo(id, getUser(userDetails));
        return ResponseEntity.noContent().build();
    }

    // === POS-EXCLUSAO / AUTO-GESTAO ===

    @GetMapping("/exclusao/status")
    public ResponseEntity<ClienteExcluidoStatusResponse> statusExclusao(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(autoGestaoService.getStatus(getUser(userDetails)));
    }

    @GetMapping(value = "/exclusao/relatorio-completo", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> baixarRelatorioCompleto(
            @AuthenticationPrincipal UserDetails userDetails) {
        User cliente = getUser(userDetails);
        byte[] pdf = autoGestaoService.gerarRelatorioGratuito(cliente);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", "relatorio-completo-tradelink.pdf");
        return ResponseEntity.ok().headers(headers).body(pdf);
    }

    @PostMapping("/exclusao/checkout-autogestao")
    public ResponseEntity<Map<String, String>> checkoutAutoGestao(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody(required = false) Map<String, String> body) {
        String returnUrl = (body != null) ? body.get("returnUrl") : null;
        return ResponseEntity.ok(stripePaymentService.createCheckoutSessionAutoGestao(getUser(userDetails), returnUrl));
    }

    @PostMapping("/exclusao/checkout-relatorio")
    public ResponseEntity<Map<String, String>> checkoutRelatorio(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(stripePaymentService.createCheckoutSessionRelatorio(getUser(userDetails)));
    }

    @GetMapping(value = "/exclusao/relatorio-pago", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> baixarRelatorioPago(
            @AuthenticationPrincipal UserDetails userDetails) {
        User cliente = getUser(userDetails);
        byte[] pdf = autoGestaoService.gerarRelatorioPago(cliente);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", "relatorio-completo-tradelink.pdf");
        return ResponseEntity.ok().headers(headers).body(pdf);
    }

    @PostMapping("/exclusao/confirmar-pagamento")
    public ResponseEntity<Map<String, Object>> confirmarPagamentoIndividual(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> body) {
        User cliente = getUser(userDetails);
        String piId = body.get("paymentIntentId");
        String sessionId = body.get("sessionId");
        boolean ok = false;
        // Tenta confirmar por PaymentIntent ID
        if (piId != null && !piId.isBlank()) {
            ok = stripePaymentService.confirmarPagamentoIndividualPorPaymentIntentId(piId, cliente.getId());
        }
        // Tenta confirmar por Session ID (fallback)
        if (!ok && sessionId != null && !sessionId.isBlank()) {
            ok = stripePaymentService.confirmarPagamentoIndividualPorSessionId(sessionId, cliente.getId());
        }
        return ResponseEntity.ok(Map.of("sucesso", ok));
    }

    // === CHAT ===

    @GetMapping("/chat/conversas")
    public ResponseEntity<List<ChatConversaResponse>> listarConversasChat(@AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        return ResponseEntity.ok(chatService.listarConversasUsuario(user.getId()));
    }

    @PostMapping("/chat/conversas")
    public ResponseEntity<ChatConversaResponse> iniciarConversa(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody(required = false) Map<String, String> body) {
        User user = getUser(userDetails);
        String assunto = body != null ? body.get("assunto") : null;
        return ResponseEntity.ok(chatService.iniciarConversa(user, assunto));
    }

    @GetMapping("/chat/conversas/{id}/mensagens")
    public ResponseEntity<List<ChatMensagemResponse>> listarMensagensChat(
            @PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        return ResponseEntity.ok(chatService.listarMensagens(id, user));
    }

    @PostMapping("/chat/conversas/{id}/mensagens")
    public ResponseEntity<ChatMensagemResponse> enviarMensagemChat(
            @PathVariable Long id, @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        return ResponseEntity.ok(chatService.enviarMensagem(id, user, body.get("conteudo")));
    }

    @GetMapping("/chat/nao-lidas")
    public ResponseEntity<Map<String, Long>> contarNaoLidasChat(@AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        return ResponseEntity.ok(Map.of("total", chatService.contarNaoLidas(user.getId())));
    }
}

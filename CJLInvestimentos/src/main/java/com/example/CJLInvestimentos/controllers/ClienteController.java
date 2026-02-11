package com.example.CJLInvestimentos.controllers;

import com.example.CJLInvestimentos.dtos.request.MarcarResolvidoRequest;
import com.example.CJLInvestimentos.dtos.request.OperacaoClienteRequest;
import com.example.CJLInvestimentos.dtos.response.CarteiraResponse;
import com.example.CJLInvestimentos.dtos.response.CotacaoResponse;
import com.example.CJLInvestimentos.dtos.response.DashboardResponse;
import com.example.CJLInvestimentos.dtos.response.OperacaoClienteResponse;
import com.example.CJLInvestimentos.dtos.response.FaturasComProximaResponse;
import com.example.CJLInvestimentos.dtos.response.PageResponse;
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
import com.example.CJLInvestimentos.services.StripePaymentService;
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
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
                .body(operacaoClienteService.registrar(id, request, cliente));
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
}

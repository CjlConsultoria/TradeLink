package com.example.CJLInvestimentos.controllers;

import com.example.CJLInvestimentos.dtos.request.CarteiraClienteRequest;
import com.example.CJLInvestimentos.dtos.request.CarteiraRequest;
import com.example.CJLInvestimentos.dtos.request.RecomendacaoRequest;
import com.example.CJLInvestimentos.dtos.request.RegisterRequest;
import com.example.CJLInvestimentos.dtos.response.CarteiraResponse;
import com.example.CJLInvestimentos.dtos.response.OperacaoClienteResponse;
import com.example.CJLInvestimentos.dtos.response.RecomendacaoResponse;
import com.example.CJLInvestimentos.dtos.response.RelatorioConsultorResponse;
import com.example.CJLInvestimentos.dtos.response.ResumoRelatorioResponse;
import com.example.CJLInvestimentos.dtos.response.FaturasComProximaResponse;
import com.example.CJLInvestimentos.dtos.response.UserResponse;
import com.example.CJLInvestimentos.entities.User;
import com.example.CJLInvestimentos.exceptions.BusinessException;
import jakarta.validation.Valid;
import com.example.CJLInvestimentos.exceptions.ResourceNotFoundException;
import com.example.CJLInvestimentos.repositories.UserRepository;
import com.example.CJLInvestimentos.services.CarteiraService;
import com.example.CJLInvestimentos.services.FaturaPdfService;
import com.example.CJLInvestimentos.services.FaturaService;
import com.example.CJLInvestimentos.services.OperacaoClienteService;
import com.example.CJLInvestimentos.services.RelatorioConsultorService;
import com.example.CJLInvestimentos.services.RelatorioPdfService;
import com.example.CJLInvestimentos.services.RecomendacaoService;
import com.example.CJLInvestimentos.services.StripePaymentService;
import com.example.CJLInvestimentos.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/consultor")
@RequiredArgsConstructor
public class ConsultorController {

    private final CarteiraService carteiraService;
    private final RecomendacaoService recomendacaoService;
    private final OperacaoClienteService operacaoClienteService;
    private final RelatorioConsultorService relatorioConsultorService;
    private final RelatorioPdfService relatorioPdfService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final FaturaService faturaService;
    private final FaturaPdfService faturaPdfService;
    private final StripePaymentService stripePaymentService;

    private User getUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    // === CLIENTES ===

    @PostMapping("/clientes")
    public ResponseEntity<UserResponse> criarCliente(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody RegisterRequest request) {
        User consultor = getUser(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.criarCliente(request, consultor.getEmpresa().getId()));
    }

    @GetMapping("/clientes")
    public ResponseEntity<List<UserResponse>> listarClientes(
            @AuthenticationPrincipal UserDetails userDetails) {
        User consultor = getUser(userDetails);
        return ResponseEntity.ok(userService.listarClientesDaEmpresa(consultor.getEmpresa().getId()));
    }

    @PutMapping("/clientes/{id}/inativar")
    public ResponseEntity<Void> inativarCliente(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        User consultor = getUser(userDetails);
        userService.inativarClientePorConsultor(id, consultor);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/clientes/{id}/ativar")
    public ResponseEntity<Void> ativarCliente(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        User consultor = getUser(userDetails);
        userService.ativarClientePorConsultor(id, consultor);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/clientes/{id}")
    public ResponseEntity<Void> excluirCliente(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        User consultor = getUser(userDetails);
        List<CarteiraResponse> carteiras = carteiraService.listarPorConsultor(consultor);
        for (CarteiraResponse c : carteiras) {
            try {
                carteiraService.removerCliente(c.getId(), id, consultor);
            } catch (ResourceNotFoundException ignored) {
                // Cliente não estava nesta carteira
            }
        }
        userService.inativarClientePorConsultor(id, consultor);
        return ResponseEntity.noContent().build();
    }

    // === CARTEIRAS ===

    @PostMapping("/carteiras")
    public ResponseEntity<CarteiraResponse> criarCarteira(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CarteiraRequest request) {
        User consultor = getUser(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(carteiraService.criar(request, consultor));
    }

    @GetMapping("/carteiras")
    public ResponseEntity<List<CarteiraResponse>> listarCarteiras(
            @AuthenticationPrincipal UserDetails userDetails) {
        User consultor = getUser(userDetails);
        return ResponseEntity.ok(carteiraService.listarPorConsultor(consultor));
    }

    @GetMapping("/carteiras/{id}")
    public ResponseEntity<CarteiraResponse> buscarCarteira(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        User consultor = getUser(userDetails);
        return ResponseEntity.ok(carteiraService.buscarPorId(id, consultor));
    }

    @PutMapping("/carteiras/{id}")
    public ResponseEntity<CarteiraResponse> atualizarCarteira(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id, @Valid @RequestBody CarteiraRequest request) {
        User consultor = getUser(userDetails);
        return ResponseEntity.ok(carteiraService.atualizar(id, request, consultor));
    }

    @DeleteMapping("/carteiras/{id}")
    public ResponseEntity<Void> excluirOuDesativarCarteira(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "false") boolean excluir) {
        User consultor = getUser(userDetails);
        if (excluir) {
            carteiraService.excluir(id, consultor);
        } else {
            carteiraService.desativar(id, consultor);
        }
        return ResponseEntity.noContent().build();
    }

    // === CLIENTES DA CARTEIRA ===

    @PostMapping("/carteiras/{carteiraId}/clientes")
    public ResponseEntity<Void> atribuirCliente(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long carteiraId,
            @Valid @RequestBody CarteiraClienteRequest request) {
        User consultor = getUser(userDetails);
        carteiraService.atribuirCliente(carteiraId, request.getClienteId(), consultor);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/carteiras/{carteiraId}/clientes/{clienteId}")
    public ResponseEntity<Void> removerCliente(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long carteiraId, @PathVariable Long clienteId) {
        User consultor = getUser(userDetails);
        carteiraService.removerCliente(carteiraId, clienteId, consultor);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/carteiras/{carteiraId}/clientes")
    public ResponseEntity<List<UserResponse>> listarClientesDaCarteira(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long carteiraId) {
        User consultor = getUser(userDetails);
        return ResponseEntity.ok(carteiraService.listarClientesDaCarteira(carteiraId, consultor));
    }

    // === RECOMENDAÇÕES ===

    @PostMapping("/carteiras/{carteiraId}/recomendacoes")
    public ResponseEntity<RecomendacaoResponse> criarRecomendacao(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long carteiraId,
            @Valid @RequestBody RecomendacaoRequest request) {
        User consultor = getUser(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(recomendacaoService.criar(carteiraId, request, consultor));
    }

    @GetMapping("/carteiras/{carteiraId}/recomendacoes")
    public ResponseEntity<List<RecomendacaoResponse>> listarRecomendacoes(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long carteiraId) {
        User consultor = getUser(userDetails);
        return ResponseEntity.ok(recomendacaoService.listarPorCarteira(carteiraId, consultor));
    }

    @PutMapping("/recomendacoes/{id}")
    public ResponseEntity<RecomendacaoResponse> atualizarRecomendacao(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody RecomendacaoRequest request) {
        User consultor = getUser(userDetails);
        return ResponseEntity.ok(recomendacaoService.atualizar(id, request, consultor));
    }

    @PatchMapping("/recomendacoes/{id}/cancelar")
    public ResponseEntity<RecomendacaoResponse> cancelarRecomendacao(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        User consultor = getUser(userDetails);
        return ResponseEntity.ok(recomendacaoService.cancelar(id, consultor));
    }

    @PatchMapping("/recomendacoes/{id}/executar")
    public ResponseEntity<RecomendacaoResponse> executarRecomendacao(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        User consultor = getUser(userDetails);
        return ResponseEntity.ok(recomendacaoService.executar(id, consultor));
    }

    @GetMapping("/recomendacoes/{id}/operacoes")
    public ResponseEntity<List<OperacaoClienteResponse>> listarOperacoesRecomendacao(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        User consultor = getUser(userDetails);
        return ResponseEntity.ok(operacaoClienteService.listarPorRecomendacaoComoConsultor(id, consultor));
    }

    // === RELATÓRIOS E HISTÓRICO ===

    @GetMapping("/relatorios/operacoes")
    public ResponseEntity<List<RelatorioConsultorResponse>> relatorioOperacoes(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Long carteiraId,
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) String dataDe,
            @RequestParam(required = false) String dataAte) {
        User consultor = getUser(userDetails);
        LocalDate de = parseLocalDate(dataDe);
        LocalDate ate = parseLocalDate(dataAte);
        return ResponseEntity.ok(relatorioConsultorService.listarOperacoes(
                consultor.getId(), carteiraId, clienteId, de, ate));
    }

    @GetMapping("/relatorios/resumo")
    public ResponseEntity<ResumoRelatorioResponse> relatorioResumo(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String dataDe,
            @RequestParam(required = false) String dataAte) {
        User consultor = getUser(userDetails);
        LocalDate de = parseLocalDate(dataDe);
        LocalDate ate = parseLocalDate(dataAte);
        return ResponseEntity.ok(relatorioConsultorService.resumoCompleto(consultor.getId(), de, ate));
    }

    @GetMapping(value = "/relatorios/operacoes/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> relatorioOperacoesPdf(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Long carteiraId,
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) String dataDe,
            @RequestParam(required = false) String dataAte) {
        User consultor = getUser(userDetails);
        LocalDate de = parseLocalDate(dataDe);
        LocalDate ate = parseLocalDate(dataAte);
        List<RelatorioConsultorResponse> ops = relatorioConsultorService.listarOperacoes(
                consultor.getId(), carteiraId, clienteId, de, ate);
        byte[] pdf = relatorioPdfService.gerarPdfOperacoesConsultor(ops, dataDe, dataAte);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", "relatorio-operacoes-consultor.pdf");
        return ResponseEntity.ok().headers(headers).body(pdf);
    }

    @GetMapping(value = "/relatorios/resumo/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> relatorioResumoPdf(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String dataDe,
            @RequestParam(required = false) String dataAte) {
        User consultor = getUser(userDetails);
        LocalDate de = parseLocalDate(dataDe);
        LocalDate ate = parseLocalDate(dataAte);
        ResumoRelatorioResponse resumo = relatorioConsultorService.resumoCompleto(consultor.getId(), de, ate);
        byte[] pdf = relatorioPdfService.gerarPdfResumoConsultor(resumo, dataDe, dataAte);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", "relatorio-resumo-consultor.pdf");
        return ResponseEntity.ok().headers(headers).body(pdf);
    }

    @GetMapping("/faturas")
    public ResponseEntity<FaturasComProximaResponse> listarFaturas(@AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        return ResponseEntity.ok(faturaService.getFaturasComProximaParaUsuario(user.getId()));
    }

    @GetMapping(value = "/faturas/{faturaId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> baixarFaturaPdf(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long faturaId) {
        User user = getUser(userDetails);
        if (user.getEmpresa() == null) {
            throw new BusinessException("Usuário sem empresa vinculada.");
        }
        byte[] pdf = faturaPdfService.gerarPdf(user.getEmpresa().getId(), faturaId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", "fatura-" + faturaId + ".pdf");
        return ResponseEntity.ok().headers(headers).body(pdf);
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
                stripePaymentService.getSuccessUrlPagamentoConsultor(),
                stripePaymentService.getCancelUrlPagamentoConsultor());
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

    /** Confirma pagamento feito na tela (Payment Element) por payment_intent_id. Registra fatura e avança período. */
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

    private static LocalDate parseLocalDate(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return LocalDate.parse(value.trim());
        } catch (Exception e) {
            return null;
        }
    }
}

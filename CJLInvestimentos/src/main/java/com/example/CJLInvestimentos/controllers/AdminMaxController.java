package com.example.CJLInvestimentos.controllers;

import com.example.CJLInvestimentos.dtos.request.AlterarSenhaAdminRequest;
import com.example.CJLInvestimentos.dtos.request.AtualizarFaturaRequest;
import com.example.CJLInvestimentos.dtos.request.AtualizarUsuarioAdminRequest;
import com.example.CJLInvestimentos.dtos.request.CriarFaturaRequest;
import com.example.CJLInvestimentos.dtos.request.EmpresaRequest;
import com.example.CJLInvestimentos.dtos.request.PlanoRequest;
import com.example.CJLInvestimentos.dtos.request.RegisterRequest;
import com.example.CJLInvestimentos.dtos.response.AdminMaxDashboardResponse;
import com.example.CJLInvestimentos.dtos.response.EmpresaResponse;
import com.example.CJLInvestimentos.dtos.response.FaturaResponse;
import com.example.CJLInvestimentos.dtos.response.FaturasComProximaResponse;
import com.example.CJLInvestimentos.dtos.response.PlanoResponse;
import com.example.CJLInvestimentos.dtos.response.ProximaFaturaResponse;
import com.example.CJLInvestimentos.dtos.response.UserResponse;
import com.example.CJLInvestimentos.dtos.request.FaqRequest;
import com.example.CJLInvestimentos.dtos.request.EnviarEmailApresentacaoRequest;
import com.example.CJLInvestimentos.dtos.response.ChatConversaResponse;
import com.example.CJLInvestimentos.dtos.response.ChatMensagemResponse;
import com.example.CJLInvestimentos.dtos.response.EmailApresentacaoResponse;
import com.example.CJLInvestimentos.dtos.response.FaqResponse;
import com.example.CJLInvestimentos.entities.User;
import com.example.CJLInvestimentos.repositories.UserRepository;
import com.example.CJLInvestimentos.services.AdminMaxDashboardService;
import com.example.CJLInvestimentos.services.ChatService;
import com.example.CJLInvestimentos.services.ConfiguracaoSistemaService;
import com.example.CJLInvestimentos.services.CotacaoService;
import com.example.CJLInvestimentos.services.EmailApresentacaoService;
import com.example.CJLInvestimentos.services.EmpresaService;
import com.example.CJLInvestimentos.services.FaqService;
import com.example.CJLInvestimentos.services.FaturaPdfService;
import com.example.CJLInvestimentos.services.FaturaService;
import com.example.CJLInvestimentos.services.PlanoService;
import com.example.CJLInvestimentos.services.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin-max")
@RequiredArgsConstructor
public class AdminMaxController {

    private final EmpresaService empresaService;
    private final UserService userService;
    private final PlanoService planoService;
    private final CotacaoService cotacaoService;
    private final FaturaService faturaService;
    private final FaturaPdfService faturaPdfService;
    private final FaqService faqService;
    private final ChatService chatService;
    private final UserRepository userRepository;
    private final AdminMaxDashboardService adminMaxDashboardService;
    private final ConfiguracaoSistemaService configuracaoSistemaService;
    private final EmailApresentacaoService emailApresentacaoService;

    // === CONFIGURACAO DO SISTEMA ===

    @GetMapping("/config-sistema")
    public ResponseEntity<Map<String, Object>> getConfigSistema() {
        var config = configuracaoSistemaService.get();
        return ResponseEntity.ok(Map.of("doisFatoresAtivo", Boolean.TRUE.equals(config.getDoisFatoresAtivo())));
    }

    @PutMapping("/config-sistema/2fa")
    public ResponseEntity<Map<String, Object>> toggle2FA(@RequestBody Map<String, Boolean> body) {
        boolean ativo = body != null && Boolean.TRUE.equals(body.get("ativo"));
        configuracaoSistemaService.setDoisFatoresAtivo(ativo);
        return ResponseEntity.ok(Map.of("doisFatoresAtivo", ativo));
    }

    // === DASHBOARD ===

    @GetMapping("/dashboard-stats")
    public ResponseEntity<AdminMaxDashboardResponse> getDashboardStats() {
        return ResponseEntity.ok(adminMaxDashboardService.getDashboardStats());
    }

    // === PLANOS ===

    @PostMapping("/planos")
    public ResponseEntity<PlanoResponse> criarPlano(@Valid @RequestBody PlanoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(planoService.criar(request));
    }

    @GetMapping("/planos")
    public ResponseEntity<List<PlanoResponse>> listarPlanos() {
        return ResponseEntity.ok(planoService.listarTodos());
    }

    @GetMapping("/planos/ativos")
    public ResponseEntity<List<PlanoResponse>> listarPlanosAtivos() {
        return ResponseEntity.ok(planoService.listarAtivos());
    }

    @GetMapping("/planos/{id}")
    public ResponseEntity<PlanoResponse> buscarPlano(@PathVariable Long id) {
        return ResponseEntity.ok(planoService.buscarPorId(id));
    }

    @PutMapping("/planos/{id}")
    public ResponseEntity<PlanoResponse> atualizarPlano(
            @PathVariable Long id, @Valid @RequestBody PlanoRequest request) {
        return ResponseEntity.ok(planoService.atualizar(id, request));
    }

    @DeleteMapping("/planos/{id}")
    public ResponseEntity<Void> desativarPlano(@PathVariable Long id) {
        planoService.desativar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/planos/{id}/ativar")
    public ResponseEntity<Void> ativarPlano(@PathVariable Long id) {
        planoService.ativar(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/empresas/{id}/plano")
    public ResponseEntity<EmpresaResponse> atribuirPlano(
            @PathVariable Long id, @RequestBody Map<String, Long> body) {
        Long planoId = body != null ? body.get("planoId") : null;
        return ResponseEntity.ok(empresaService.atribuirPlano(id, planoId));
    }

    // === EMPRESAS ===

    @PostMapping("/empresas")
    public ResponseEntity<EmpresaResponse> criarEmpresa(@Valid @RequestBody EmpresaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(empresaService.criar(request));
    }

    @GetMapping("/empresas")
    public ResponseEntity<List<EmpresaResponse>> listarEmpresas() {
        return ResponseEntity.ok(empresaService.listarTodas());
    }

    @GetMapping("/empresas/{id}")
    public ResponseEntity<EmpresaResponse> buscarEmpresa(@PathVariable Long id) {
        return ResponseEntity.ok(empresaService.buscarPorId(id));
    }

    @PutMapping("/empresas/{id}")
    public ResponseEntity<EmpresaResponse> atualizarEmpresa(
            @PathVariable Long id, @Valid @RequestBody EmpresaRequest request) {
        return ResponseEntity.ok(empresaService.atualizar(id, request));
    }

    @DeleteMapping("/empresas/{id}")
    public ResponseEntity<Void> desativarEmpresa(@PathVariable Long id) {
        empresaService.desativar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/empresas/{id}/ativar")
    public ResponseEntity<Void> ativarEmpresa(@PathVariable Long id) {
        empresaService.ativar(id);
        return ResponseEntity.noContent().build();
    }

    /** Bloqueia ou desbloqueia o acesso à plataforma da empresa (todos os consultores e clientes). Body: { "bloqueado": true/false }. */
    @PutMapping("/empresas/{id}/bloquear-acesso")
    public ResponseEntity<EmpresaResponse> bloquearAcessoEmpresa(
            @PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        boolean bloqueado = body != null && Boolean.TRUE.equals(body.get("bloqueado"));
        return ResponseEntity.ok(empresaService.bloquearAcesso(id, bloqueado));
    }

    @PostMapping("/empresas/{empresaId}/consultores")
    public ResponseEntity<UserResponse> criarConsultor(
            @PathVariable Long empresaId, @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.criarConsultor(request, empresaId));
    }

    @GetMapping("/empresas/{empresaId}/usuarios")
    public ResponseEntity<List<UserResponse>> listarUsuariosDaEmpresa(@PathVariable Long empresaId) {
        return ResponseEntity.ok(userService.listarPorEmpresa(empresaId));
    }

    // === FATURAS (Admin) ===

    @GetMapping("/empresas/{empresaId}/faturas")
    public ResponseEntity<FaturasComProximaResponse> listarFaturasDaEmpresa(@PathVariable Long empresaId) {
        List<FaturaResponse> faturas = faturaService.listarPorEmpresa(empresaId);
        ProximaFaturaResponse proxima = faturaService.getProximaFatura(empresaId);
        boolean acesso = faturaService.acessoPermitidoPorEmpresaId(empresaId);
        return ResponseEntity.ok(FaturasComProximaResponse.builder()
                .faturas(faturas)
                .proxima(proxima)
                .acessoPermitido(acesso)
                .build());
    }

    @PostMapping("/empresas/{empresaId}/faturas")
    public ResponseEntity<FaturaResponse> criarFatura(
            @PathVariable Long empresaId, @Valid @RequestBody CriarFaturaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(faturaService.criarFaturaManual(empresaId, request));
    }

    @PutMapping("/empresas/{empresaId}/faturas/{faturaId}")
    public ResponseEntity<FaturaResponse> atualizarFatura(
            @PathVariable Long empresaId, @PathVariable Long faturaId,
            @Valid @RequestBody AtualizarFaturaRequest request) {
        return ResponseEntity.ok(faturaService.atualizarFatura(empresaId, faturaId, request));
    }

    @DeleteMapping("/empresas/{empresaId}/faturas/{faturaId}")
    public ResponseEntity<Void> excluirFatura(
            @PathVariable Long empresaId, @PathVariable Long faturaId) {
        faturaService.excluirFatura(empresaId, faturaId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/empresas/{empresaId}/faturas/{faturaId}/pdf")
    public ResponseEntity<byte[]> imprimirFaturaPdf(
            @PathVariable Long empresaId, @PathVariable Long faturaId) {
        byte[] pdf = faturaPdfService.gerarPdf(empresaId, faturaId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("inline", "fatura-" + faturaId + ".pdf");
        return ResponseEntity.ok().headers(headers).body(pdf);
    }

    @GetMapping("/usuarios")
    public ResponseEntity<List<UserResponse>> listarTodosUsuarios() {
        return ResponseEntity.ok(userService.listarTodos());
    }

    @GetMapping("/usuarios/{id}")
    public ResponseEntity<UserResponse> buscarUsuario(@PathVariable Long id) {
        return ResponseEntity.ok(userService.buscarPorId(id));
    }

    @PutMapping("/usuarios/{id}")
    public ResponseEntity<UserResponse> atualizarUsuario(
            @PathVariable Long id, @RequestBody AtualizarUsuarioAdminRequest request) {
        return ResponseEntity.ok(userService.atualizarPorAdminMax(id, request));
    }

    @PostMapping("/usuarios/{id}/ativar")
    public ResponseEntity<Void> ativarUsuario(@PathVariable Long id) {
        userService.ativar(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/usuarios/{id}/senha")
    public ResponseEntity<Void> alterarSenhaUsuario(
            @PathVariable Long id, @Valid @RequestBody AlterarSenhaAdminRequest request) {
        userService.alterarSenhaPorAdminMax(id, request.getNovaSenha());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<Void> desativarUsuario(@PathVariable Long id) {
        userService.desativar(id);
        return ResponseEntity.noContent().build();
    }

    /** Zera todas as cotações e recarrega apenas as atuais (AwesomeAPI + CoinGecko), sem duplicar. */
    @PostMapping("/cotacoes/zerar")
    public ResponseEntity<Void> zerarCotacoes() {
        cotacaoService.zerarERecarregar();
        return ResponseEntity.noContent().build();
    }

    // === FAQ ===

    @GetMapping("/faq")
    public ResponseEntity<List<FaqResponse>> listarFaq() {
        return ResponseEntity.ok(faqService.listarTodas());
    }

    @PostMapping("/faq")
    public ResponseEntity<FaqResponse> criarFaq(@Valid @RequestBody FaqRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(faqService.criar(request));
    }

    @PutMapping("/faq/{id}")
    public ResponseEntity<FaqResponse> atualizarFaq(@PathVariable Long id, @Valid @RequestBody FaqRequest request) {
        return ResponseEntity.ok(faqService.atualizar(id, request));
    }

    @DeleteMapping("/faq/{id}")
    public ResponseEntity<Void> excluirFaq(@PathVariable Long id) {
        faqService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/faq/reordenar")
    public ResponseEntity<Void> reordenarFaq(@RequestBody List<Long> ids) {
        faqService.reordenar(ids);
        return ResponseEntity.noContent().build();
    }

    // === CHAT (AdminMax como suporte) ===

    @GetMapping("/chat/conversas")
    public ResponseEntity<List<ChatConversaResponse>> listarConversasChat(@AuthenticationPrincipal UserDetails userDetails) {
        User admin = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        return ResponseEntity.ok(chatService.listarTodasConversas(admin.getId()));
    }

    @GetMapping("/chat/conversas/{id}/mensagens")
    public ResponseEntity<List<ChatMensagemResponse>> listarMensagensChat(
            @PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User admin = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        return ResponseEntity.ok(chatService.listarMensagens(id, admin));
    }

    @PostMapping("/chat/conversas/{id}/mensagens")
    public ResponseEntity<ChatMensagemResponse> enviarMensagemChat(
            @PathVariable Long id, @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        User admin = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        return ResponseEntity.ok(chatService.enviarMensagem(id, admin, body.get("conteudo")));
    }

    @PutMapping("/chat/conversas/{id}/fechar")
    public ResponseEntity<Void> fecharConversa(@PathVariable Long id) {
        chatService.fecharConversa(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/chat/nao-lidas")
    public ResponseEntity<Map<String, Long>> contarNaoLidasChat(@AuthenticationPrincipal UserDetails userDetails) {
        User admin = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        return ResponseEntity.ok(Map.of("total", chatService.contarNaoLidasAdmin(admin.getId())));
    }

    // === EMAILS DE APRESENTAÇÃO ===

    @PostMapping("/emails-apresentacao/enviar")
    public ResponseEntity<List<EmailApresentacaoResponse>> enviarEmailApresentacao(
            @Valid @RequestBody EnviarEmailApresentacaoRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User admin = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        return ResponseEntity.ok(emailApresentacaoService.enviar(request.getUserIds(), admin));
    }

    @GetMapping("/emails-apresentacao/historico")
    public ResponseEntity<List<EmailApresentacaoResponse>> historicoEmailApresentacao() {
        return ResponseEntity.ok(emailApresentacaoService.historico());
    }

    @GetMapping("/emails-apresentacao/preview/{userId}")
    public ResponseEntity<Map<String, String>> previewEmailApresentacao(@PathVariable Long userId) {
        String html = emailApresentacaoService.preview(userId);
        return ResponseEntity.ok(Map.of("html", html));
    }
}

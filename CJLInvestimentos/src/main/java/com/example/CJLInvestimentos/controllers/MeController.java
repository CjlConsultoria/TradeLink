package com.example.CJLInvestimentos.controllers;

import com.example.CJLInvestimentos.dtos.request.PushSubscriptionRequest;
import com.example.CJLInvestimentos.dtos.request.TrocarSenhaRequest;
import com.example.CJLInvestimentos.dtos.response.EmpresaResponse;
import com.example.CJLInvestimentos.dtos.response.UserResponse;
import com.example.CJLInvestimentos.entities.MoedaFavorita;
import com.example.CJLInvestimentos.entities.User;
import com.example.CJLInvestimentos.exceptions.BusinessException;
import com.example.CJLInvestimentos.repositories.MoedaFavoritaRepository;
import com.example.CJLInvestimentos.repositories.UserRepository;
import com.example.CJLInvestimentos.services.AtividadeLogService;
import com.example.CJLInvestimentos.services.EmpresaService;
import com.example.CJLInvestimentos.services.FaturaService;
import com.example.CJLInvestimentos.services.LoginLogService;
import com.example.CJLInvestimentos.services.NotificacaoInAppService;
import com.example.CJLInvestimentos.services.NotificationAsyncRunner;
import com.example.CJLInvestimentos.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class MeController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final EmpresaService empresaService;
    private final FaturaService faturaService;
    private final PasswordEncoder passwordEncoder;
    private final NotificationAsyncRunner notificationAsyncRunner;
    private final LoginLogService loginLogService;
    private final NotificacaoInAppService notificacaoInAppService;
    private final MoedaFavoritaRepository moedaFavoritaRepository;
    private final AtividadeLogService atividadeLogService;

    @Value("${app.notificacao.push.vapid-public:}")
    private String vapidPublicKey;

    private User getUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    @GetMapping
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        UserResponse res = userService.toResponse(user);
        if (user.getEmpresa() != null && !faturaService.acessoPermitidoPorUsuarioId(user.getId())) {
            res.setBloqueado(true);
            res.setMotivoBloqueio(faturaService.getMotivoBloqueioPorUsuarioId(user.getId()));
            if (res.getMotivoBloqueio() == null) res.setMotivoBloqueio("Acesso bloqueado.");
        }
        return ResponseEntity.ok(res);
    }

    /** Retorna as configurações de notificação da empresa do usuário (para exibir no front quais canais estão habilitados). */
    @GetMapping("/empresa-notificacoes")
    public ResponseEntity<EmpresaResponse> empresaNotificacoes(@AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        if (user.getEmpresa() == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(empresaService.buscarPorId(user.getEmpresa().getId()));
    }

    /** Altera a senha do usuário logado (ex.: AdminMax após primeiro acesso). */
    @PutMapping("/senha")
    public ResponseEntity<Void> trocarSenha(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody TrocarSenhaRequest request) {
        User user = getUser(userDetails);
        if (!passwordEncoder.matches(request.getSenhaAtual(), user.getSenha())) {
            throw new BusinessException("Senha atual incorreta");
        }
        user.setSenha(passwordEncoder.encode(request.getNovaSenha()));
        userRepository.save(user);
        notificationAsyncRunner.enviarEmailSenhaAlteradaAsync(user.getEmail(), user.getNome());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/telegram-chat-id")
    public ResponseEntity<UserResponse> atualizarTelegramChatId(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> body) {
        User user = getUser(userDetails);
        String chatId = body != null ? body.get("telegramChatId") : null;
        return ResponseEntity.ok(userService.atualizarTelegramChatId(user, chatId));
    }

    @PostMapping("/push-subscription")
    public ResponseEntity<UserResponse> registrarPushSubscription(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody PushSubscriptionRequest request) {
        User user = getUser(userDetails);
        return ResponseEntity.ok(userService.registrarPushSubscription(user, request));
    }

    @DeleteMapping("/push-subscription")
    public ResponseEntity<Void> removerPushSubscription(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String endpoint) {
        User user = getUser(userDetails);
        userService.removerPushSubscription(user, endpoint);
        return ResponseEntity.noContent().build();
    }

    /** Retorna o histórico de acessos (login) do usuário autenticado. */
    @GetMapping("/login-history")
    public ResponseEntity<?> loginHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "20") int limit) {
        User user = getUser(userDetails);
        return ResponseEntity.ok(loginLogService.listarPorUsuario(user.getId(), Math.min(limit, 50)));
    }

    /** Chave pública VAPID para o frontend registrar push (navegador). */
    @GetMapping("/config-notificacao")
    public ResponseEntity<Map<String, String>> configNotificacao() {
        return ResponseEntity.ok(Map.of(
                "vapidPublicKey", vapidPublicKey != null ? vapidPublicKey : "",
                "telegramInstrucoes", "Para receber notificações no Telegram: 1) Abra @userinfobot no Telegram. 2) Envie /start. 3) Copie seu Id e cole abaixo."
        ));
    }

    // === NOTIFICACOES IN-APP ===

    @GetMapping("/notificacoes")
    public ResponseEntity<List<Map<String, Object>>> listarNotificacoes(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "30") int limit) {
        User user = getUser(userDetails);
        return ResponseEntity.ok(notificacaoInAppService.listar(user.getId(), limit));
    }

    @GetMapping("/notificacoes/count")
    public ResponseEntity<Map<String, Long>> contarNaoLidas(@AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        return ResponseEntity.ok(Map.of("count", notificacaoInAppService.contarNaoLidas(user.getId())));
    }

    @PutMapping("/notificacoes/{id}/lida")
    public ResponseEntity<Void> marcarComoLida(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        User user = getUser(userDetails);
        notificacaoInAppService.marcarComoLida(user.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/notificacoes/lidas")
    public ResponseEntity<Map<String, Integer>> marcarTodasComoLidas(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        int total = notificacaoInAppService.marcarTodasComoLidas(user.getId());
        return ResponseEntity.ok(Map.of("marcadas", total));
    }

    // === FAVORITOS (WATCHLIST) ===

    @GetMapping("/favoritos")
    public ResponseEntity<List<Map<String, String>>> listarFavoritos(@AuthenticationPrincipal UserDetails ud) {
        User user = userRepository.findByEmail(ud.getUsername()).orElseThrow();
        return ResponseEntity.ok(moedaFavoritaRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
            .stream().map(f -> Map.of("moeda", f.getMoeda(), "parMoeda", f.getParMoeda())).toList());
    }

    @PostMapping("/favoritos")
    public ResponseEntity<?> adicionarFavorito(@AuthenticationPrincipal UserDetails ud, @RequestBody Map<String, String> body) {
        User user = userRepository.findByEmail(ud.getUsername()).orElseThrow();
        String moeda = body.get("moeda"); String par = body.get("parMoeda");
        if (moeda == null || par == null) return ResponseEntity.badRequest().build();
        if (moedaFavoritaRepository.existsByUserIdAndMoedaAndParMoeda(user.getId(), moeda, par))
            return ResponseEntity.ok().build();
        if (moedaFavoritaRepository.countByUserId(user.getId()) >= 30)
            return ResponseEntity.badRequest().body(Map.of("message", "Máximo de 30 favoritos"));
        moedaFavoritaRepository.save(MoedaFavorita.builder().user(user).moeda(moeda).parMoeda(par).build());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/favoritos/{moeda}/{parMoeda}")
    @Transactional
    public ResponseEntity<?> removerFavorito(@AuthenticationPrincipal UserDetails ud, @PathVariable String moeda, @PathVariable String parMoeda) {
        User user = userRepository.findByEmail(ud.getUsername()).orElseThrow();
        moedaFavoritaRepository.deleteByUserIdAndMoedaAndParMoeda(user.getId(), moeda, parMoeda);
        return ResponseEntity.ok().build();
    }

    // === ATIVIDADES (TIMELINE) ===

    @GetMapping("/atividades")
    public ResponseEntity<?> listarAtividades(@AuthenticationPrincipal UserDetails ud,
                                               @RequestParam(defaultValue = "30") int limit) {
        User user = userRepository.findByEmail(ud.getUsername()).orElseThrow();
        var atividades = atividadeLogService.listar(user.getId(), Math.min(limit, 100));
        return ResponseEntity.ok(atividades.stream().map(a -> Map.of(
            "id", a.getId(),
            "tipo", a.getTipo(),
            "descricao", a.getDescricao() != null ? a.getDescricao() : "",
            "link", a.getLink() != null ? a.getLink() : "",
            "createdAt", a.getCreatedAt().toString()
        )).toList());
    }
}

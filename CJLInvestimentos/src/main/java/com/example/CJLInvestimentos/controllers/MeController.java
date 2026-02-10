package com.example.CJLInvestimentos.controllers;

import com.example.CJLInvestimentos.dtos.request.PushSubscriptionRequest;
import com.example.CJLInvestimentos.dtos.request.TrocarSenhaRequest;
import com.example.CJLInvestimentos.dtos.response.EmpresaResponse;
import com.example.CJLInvestimentos.dtos.response.UserResponse;
import com.example.CJLInvestimentos.entities.User;
import com.example.CJLInvestimentos.exceptions.BusinessException;
import com.example.CJLInvestimentos.repositories.UserRepository;
import com.example.CJLInvestimentos.services.EmpresaService;
import com.example.CJLInvestimentos.services.NotificationAsyncRunner;
import com.example.CJLInvestimentos.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class MeController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final EmpresaService empresaService;
    private final PasswordEncoder passwordEncoder;
    private final NotificationAsyncRunner notificationAsyncRunner;

    @Value("${app.notificacao.push.vapid-public:}")
    private String vapidPublicKey;

    private User getUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    @GetMapping
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        return ResponseEntity.ok(userService.toResponse(user));
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

    /** Chave pública VAPID para o frontend registrar push (navegador). */
    @GetMapping("/config-notificacao")
    public ResponseEntity<Map<String, String>> configNotificacao() {
        return ResponseEntity.ok(Map.of(
                "vapidPublicKey", vapidPublicKey != null ? vapidPublicKey : "",
                "telegramInstrucoes", "Para receber notificações no Telegram: 1) Abra @userinfobot no Telegram. 2) Envie /start. 3) Copie seu Id e cole abaixo."
        ));
    }
}

package com.example.CJLInvestimentos.controllers;

import com.example.CJLInvestimentos.dtos.request.AtivarContaRequest;
import com.example.CJLInvestimentos.dtos.request.AutoCadastroRequest;
import com.example.CJLInvestimentos.dtos.request.ForgotPasswordRequest;
import com.example.CJLInvestimentos.dtos.request.LoginRequest;
import com.example.CJLInvestimentos.dtos.request.RegisterRequest;
import com.example.CJLInvestimentos.dtos.request.ResetPasswordRequest;
import com.example.CJLInvestimentos.dtos.request.VerifyOtpRequest;
import com.example.CJLInvestimentos.dtos.response.AuthResponse;
import com.example.CJLInvestimentos.dtos.response.AutoCadastroResponse;
import com.example.CJLInvestimentos.dtos.response.PlanoResponse;
import com.example.CJLInvestimentos.dtos.response.ValidarConviteResponse;
import com.example.CJLInvestimentos.entities.Empresa;
import com.example.CJLInvestimentos.entities.User;
import com.example.CJLInvestimentos.entities.enums.Role;
import com.example.CJLInvestimentos.repositories.EmpresaRepository;
import com.example.CJLInvestimentos.repositories.UserRepository;
import com.example.CJLInvestimentos.security.LoginRateLimiter;
import com.example.CJLInvestimentos.services.AutoCadastroService;
import com.example.CJLInvestimentos.services.AutoGestaoService;
import com.example.CJLInvestimentos.services.ConfiguracaoSistemaService;
import com.example.CJLInvestimentos.services.FaturaService;
import com.example.CJLInvestimentos.services.JwtService;
import com.example.CJLInvestimentos.services.LoginLogService;
import com.example.CJLInvestimentos.services.NotificationAsyncRunner;
import com.example.CJLInvestimentos.services.OtpService;
import com.example.CJLInvestimentos.services.PlanoService;
import com.example.CJLInvestimentos.services.TrialService;
import com.example.CJLInvestimentos.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.List;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final EmpresaRepository empresaRepository;
    private final FaturaService faturaService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final NotificationAsyncRunner notificationAsyncRunner;
    private final UserService userService;
    private final AutoGestaoService autoGestaoService;
    private final AutoCadastroService autoCadastroService;
    private final TrialService trialService;
    private final OtpService otpService;
    private final LoginRateLimiter rateLimiter;
    private final LoginLogService loginLogService;
    private final ConfiguracaoSistemaService configuracaoSistemaService;
    private final PlanoService planoService;

    @GetMapping("/planos")
    public ResponseEntity<List<PlanoResponse>> listarPlanosPublicos() {
        return ResponseEntity.ok(planoService.listarAtivos());
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest()
                    .body(AuthResponse.builder().mensagem("E-mail já registrado").build());
        }

        Role role = (request.getRole() != null) ? request.getRole() : Role.Cliente;

        User.UserBuilder userBuilder = User.builder()
                .nome(request.getNome())
                .email(request.getEmail())
                .senha(passwordEncoder.encode(request.getSenha()))
                .role(role);

        if (request.getEmpresaId() != null) {
            Empresa empresa = empresaRepository.findById(request.getEmpresaId()).orElse(null);
            if (empresa != null) {
                userBuilder.empresa(empresa);
            }
        }

        User user = userRepository.save(userBuilder.build());
        notificationAsyncRunner.enviarEmailNovoUsuarioAsync(user.getEmail(), user.getNome(), user.getRole());
        String token = jwtService.generateToken(user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AuthResponse.builder()
                        .token(token)
                        .role(user.getRole().name())
                        .userId(user.getId())
                        .nome(user.getNome())
                        .empresaId(user.getEmpresa() != null ? user.getEmpresa().getId() : null)
                        .build());
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String ip = extractIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        String email = request.getEmail();

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            rateLimiter.registerAttempt(email);
            loginLogService.registrar(null, email, ip, userAgent, false, "Usuario nao encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(AuthResponse.builder().mensagem("Usuário não encontrado").build());
        }

        // Conta pendente de ativação (convidado, sem senha definida)
        if (user.getSenha() == null || user.getSenha().isBlank()) {
            loginLogService.registrar(user.getId(), email, ip, userAgent, false, "Conta pendente de ativacao");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthResponse.builder().mensagem("Conta pendente de ativação. Verifique seu e-mail para ativar.").build());
        }

        if (!passwordEncoder.matches(request.getSenha(), user.getSenha())) {
            rateLimiter.registerAttempt(email);
            loginLogService.registrar(user.getId(), email, ip, userAgent, false, "Senha invalida");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthResponse.builder().mensagem("Senha inválida").build());
        }
        if (!Boolean.TRUE.equals(user.getAtivo())) {
            loginLogService.registrar(user.getId(), email, ip, userAgent, false, "Conta inativa");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthResponse.builder().mensagem("Conta inativa. Entre em contato com o administrador.").build());
        }

        // Login bem-sucedido: resetar rate limiter
        rateLimiter.resetAttempts(email);

        // 2FA: AdminMax faz login direto; demais precisam verificar OTP (se 2FA global estiver ativo)
        if (user.getRole() != Role.AdminMax && configuracaoSistemaService.isDoisFatoresAtivo()) {
            otpService.generateAndSend(user);
            loginLogService.registrar(user.getId(), email, ip, userAgent, true, "2FA enviado");
            return ResponseEntity.ok(AuthResponse.builder()
                    .requires2FA(true)
                    .userId(user.getId())
                    .mensagem("Código de verificação enviado para seu e-mail.")
                    .build());
        }

        loginLogService.registrar(user.getId(), email, ip, userAgent, true, null);
        return ResponseEntity.ok(buildFullAuthResponse(user).build());
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponse> verifyOtp(@RequestBody VerifyOtpRequest request, HttpServletRequest httpRequest) {
        String ip = extractIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        if (request.getUserId() == null || request.getCode() == null || request.getCode().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(AuthResponse.builder().mensagem("Código e usuário são obrigatórios.").build());
        }

        User user = userRepository.findById(request.getUserId()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(AuthResponse.builder().mensagem("Usuário não encontrado.").build());
        }

        if (!otpService.validate(user.getId(), request.getCode())) {
            String key = "otp:" + request.getUserId();
            rateLimiter.registerAttempt(key);
            loginLogService.registrar(user.getId(), user.getEmail(), ip, userAgent, false, "OTP invalido");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthResponse.builder().mensagem("Código inválido ou expirado.").build());
        }

        rateLimiter.resetAttempts("otp:" + request.getUserId());
        loginLogService.registrar(user.getId(), user.getEmail(), ip, userAgent, true, null);
        return ResponseEntity.ok(buildFullAuthResponse(user).build());
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<AuthResponse> resendOtp(@RequestBody VerifyOtpRequest request) {
        if (request.getUserId() == null) {
            return ResponseEntity.badRequest()
                    .body(AuthResponse.builder().mensagem("Usuário é obrigatório.").build());
        }

        User user = userRepository.findById(request.getUserId()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(AuthResponse.builder().mensagem("Usuário não encontrado.").build());
        }

        otpService.generateAndSend(user);
        return ResponseEntity.ok(AuthResponse.builder()
                .mensagem("Novo código de verificação enviado para seu e-mail.")
                .build());
    }

    // === RECUPERAÇÃO DE SENHA ===

    @PostMapping("/forgot-password")
    public ResponseEntity<AuthResponse> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        // Sempre retorna sucesso para não expor quais e-mails existem
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user != null && Boolean.TRUE.equals(user.getAtivo())) {
            String token = UUID.randomUUID().toString();
            user.setTokenResetSenha(token);
            user.setTokenResetSenhaExpiracao(LocalDateTime.now().plusMinutes(30));
            userRepository.save(user);
            notificationAsyncRunner.enviarEmailResetSenhaAsync(user.getEmail(), user.getNome(), token);
        }
        return ResponseEntity.ok(AuthResponse.builder()
                .mensagem("Se o e-mail estiver cadastrado, você receberá as instruções para redefinir sua senha.")
                .build());
    }

    @PostMapping("/reset-password")
    public ResponseEntity<AuthResponse> resetPassword(@RequestBody ResetPasswordRequest request) {
        if (request.getToken() == null || request.getToken().isBlank()
                || request.getNovaSenha() == null || request.getNovaSenha().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(AuthResponse.builder().mensagem("Token e nova senha são obrigatórios.").build());
        }

        if (request.getNovaSenha().length() < 6) {
            return ResponseEntity.badRequest()
                    .body(AuthResponse.builder().mensagem("A senha deve ter pelo menos 6 caracteres.").build());
        }

        User user = userRepository.findByTokenResetSenha(request.getToken()).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(AuthResponse.builder().mensagem("Token inválido ou expirado.").build());
        }

        if (user.getTokenResetSenhaExpiracao() == null || user.getTokenResetSenhaExpiracao().isBefore(LocalDateTime.now())) {
            user.setTokenResetSenha(null);
            user.setTokenResetSenhaExpiracao(null);
            userRepository.save(user);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(AuthResponse.builder().mensagem("Token expirado. Solicite uma nova redefinição.").build());
        }

        user.setSenha(passwordEncoder.encode(request.getNovaSenha()));
        user.setTokenResetSenha(null);
        user.setTokenResetSenhaExpiracao(null);
        userRepository.save(user);

        notificationAsyncRunner.enviarEmailSenhaAlteradaAsync(user.getEmail(), user.getNome());

        return ResponseEntity.ok(AuthResponse.builder()
                .mensagem("Senha redefinida com sucesso! Faça login com sua nova senha.")
                .build());
    }

    // === REFRESH TOKEN ===

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(AuthResponse.builder().mensagem("Refresh token é obrigatório.").build());
        }

        try {
            if (!jwtService.isRefreshToken(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(AuthResponse.builder().mensagem("Token inválido.").build());
            }

            String email = jwtService.extractEmail(refreshToken);
            User user = userRepository.findByEmail(email).orElse(null);

            if (user == null || !Boolean.TRUE.equals(user.getAtivo())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(AuthResponse.builder().mensagem("Usuário não encontrado ou inativo.").build());
            }

            if (!jwtService.isTokenValid(refreshToken, user)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(AuthResponse.builder().mensagem("Refresh token expirado.").build());
            }

            return ResponseEntity.ok(buildFullAuthResponse(user).build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthResponse.builder().mensagem("Token inválido ou expirado.").build());
        }
    }

    /** Monta a resposta completa de autenticação com token JWT, refresh token e flags de bloqueio/trial. */
    private AuthResponse.AuthResponseBuilder buildFullAuthResponse(User user) {
        String token = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        AuthResponse.AuthResponseBuilder response = AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .role(user.getRole().name())
                .userId(user.getId())
                .nome(user.getNome())
                .empresaId(user.getEmpresa() != null ? user.getEmpresa().getId() : null)
                .autoCadastro(Boolean.TRUE.equals(user.getAutoCadastro()))
                .origemVinculo(user.getOrigemVinculo());

        // Bloqueio por admin, inativação ou pagamento (empresa)
        if (user.getEmpresa() != null && !faturaService.acessoPermitidoPorUsuarioId(user.getId())) {
            boolean porAdmin = faturaService.isBloqueadoPorAdmin(user.getId());
            if (porAdmin) {
                response.bloqueado(true)
                        .bloqueadoPorAdmin(true)
                        .motivoBloqueio(faturaService.getMotivoBloqueioPorUsuarioId(user.getId()));
            } else if (trialService.isTrialAtivoEmpresa(user.getEmpresa())) {
                response.trialAtivo(true);
                response.trialFim(user.getEmpresa().getTrialFim() != null ? user.getEmpresa().getTrialFim().toString() : null);
            } else if (trialService.isTrialExpiradoEmpresa(user.getEmpresa())) {
                response.bloqueado(true).motivoBloqueio("Seu período de teste expirou. Escolha um plano para continuar acessando.");
                response.precisaEscolherPlano(true);
            } else {
                response.bloqueado(true).motivoBloqueio(faturaService.getMotivoBloqueioPorUsuarioId(user.getId()));
            }
        }

        // Consultor auto-cadastro com trial
        if (user.getRole() == Role.Admin && Boolean.TRUE.equals(user.getAutoCadastro()) && user.getEmpresa() != null) {
            if (trialService.isTrialAtivoEmpresa(user.getEmpresa())) {
                response.trialAtivo(true);
                response.trialFim(user.getEmpresa().getTrialFim() != null ? user.getEmpresa().getTrialFim().toString() : null);
            } else if (trialService.isTrialExpiradoEmpresa(user.getEmpresa())) {
                response.bloqueado(true).motivoBloqueio("Seu período de teste expirou. Escolha um plano para continuar acessando.");
                response.precisaEscolherPlano(true);
            }
        }

        // Marketplace com pagamento em atraso
        if ("MARKETPLACE".equals(user.getOrigemVinculo()) && "PAST_DUE".equals(user.getMarketplaceStatus())) {
            response.marketplaceBloqueado(true);
            response.bloqueado(true);
            response.motivoBloqueio("Pagamento da mentoria em atraso.");
        }

        // Cliente excluído ou auto-cadastro (sem empresa, ativo)
        if (autoGestaoService.isClienteExcluido(user)) {
            response.clienteExcluido(true);
            boolean hasActive = autoGestaoService.hasActiveSubscription(user);
            boolean trialAtivo = trialService.isTrialAtivo(user);
            response.autoGestaoAtiva(hasActive || trialAtivo);

            if (trialAtivo) {
                response.trialAtivo(true);
                response.trialFim(user.getTrialFim() != null ? user.getTrialFim().toString() : null);
            }
        }

        return response;
    }

    // === AUTO-CADASTRO ===

    @PostMapping("/auto-cadastro")
    public ResponseEntity<AutoCadastroResponse> autoCadastro(@Valid @RequestBody AutoCadastroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(autoCadastroService.cadastrar(request));
    }

    // === CONVITE / ATIVAÇÃO DE CONTA ===

    @GetMapping("/validar-convite/{token}")
    public ResponseEntity<ValidarConviteResponse> validarConvite(@PathVariable String token) {
        return ResponseEntity.ok(userService.validarConvite(token));
    }

    @PostMapping("/ativar-conta")
    public ResponseEntity<AuthResponse> ativarConta(@Valid @RequestBody AtivarContaRequest request) {
        return ResponseEntity.ok(userService.ativarConta(request));
    }

    // === HELPERS ===

    private String extractIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

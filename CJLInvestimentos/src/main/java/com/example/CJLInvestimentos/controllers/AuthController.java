package com.example.CJLInvestimentos.controllers;

import com.example.CJLInvestimentos.dtos.request.AtivarContaRequest;
import com.example.CJLInvestimentos.dtos.request.AutoCadastroRequest;
import com.example.CJLInvestimentos.dtos.request.LoginRequest;
import com.example.CJLInvestimentos.dtos.request.RegisterRequest;
import com.example.CJLInvestimentos.dtos.response.AuthResponse;
import com.example.CJLInvestimentos.dtos.response.AutoCadastroResponse;
import com.example.CJLInvestimentos.dtos.response.ValidarConviteResponse;
import com.example.CJLInvestimentos.entities.Empresa;
import com.example.CJLInvestimentos.entities.User;
import com.example.CJLInvestimentos.entities.enums.Role;
import com.example.CJLInvestimentos.entities.enums.SubscriptionStatus;
import com.example.CJLInvestimentos.repositories.EmpresaRepository;
import com.example.CJLInvestimentos.repositories.UserRepository;
import com.example.CJLInvestimentos.services.AutoCadastroService;
import com.example.CJLInvestimentos.services.AutoGestaoService;
import com.example.CJLInvestimentos.services.FaturaService;
import com.example.CJLInvestimentos.services.JwtService;
import com.example.CJLInvestimentos.services.NotificationAsyncRunner;
import com.example.CJLInvestimentos.services.TrialService;
import com.example.CJLInvestimentos.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;


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
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(AuthResponse.builder().mensagem("Usuário não encontrado").build());
        }

        // Conta pendente de ativação (convidado, sem senha definida)
        if (user.getSenha() == null || user.getSenha().isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthResponse.builder().mensagem("Conta pendente de ativação. Verifique seu e-mail para ativar.").build());
        }

        if (!passwordEncoder.matches(request.getSenha(), user.getSenha())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthResponse.builder().mensagem("Senha inválida").build());
        }
        if (!Boolean.TRUE.equals(user.getAtivo())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthResponse.builder().mensagem("Conta inativa. Entre em contato com o administrador.").build());
        }

        String token = jwtService.generateToken(user);

        AuthResponse.AuthResponseBuilder response = AuthResponse.builder()
                .token(token)
                .role(user.getRole().name())
                .userId(user.getId())
                .nome(user.getNome())
                .empresaId(user.getEmpresa() != null ? user.getEmpresa().getId() : null)
                .autoCadastro(Boolean.TRUE.equals(user.getAutoCadastro()));

        // Bloqueio por admin, inativação ou pagamento (empresa)
        if (user.getEmpresa() != null && !faturaService.acessoPermitidoPorUsuarioId(user.getId())) {
            boolean porAdmin = faturaService.isBloqueadoPorAdmin(user.getId());
            if (porAdmin) {
                // Bloqueio por admin ou empresa inativa: acesso totalmente restrito
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

        return ResponseEntity.ok(response.build());
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
}

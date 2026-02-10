package com.example.CJLInvestimentos.controllers;

import com.example.CJLInvestimentos.dtos.request.LoginRequest;
import com.example.CJLInvestimentos.dtos.request.RegisterRequest;
import com.example.CJLInvestimentos.dtos.response.AuthResponse;
import com.example.CJLInvestimentos.entities.Empresa;
import com.example.CJLInvestimentos.entities.User;
import com.example.CJLInvestimentos.entities.enums.Role;
import com.example.CJLInvestimentos.repositories.EmpresaRepository;
import com.example.CJLInvestimentos.repositories.UserRepository;
import com.example.CJLInvestimentos.services.JwtService;
import com.example.CJLInvestimentos.services.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final EmpresaRepository empresaRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;
    private final NotificationService notificationService;

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
        try {
            notificationService.enviarEmailNovoUsuario(user.getEmail(), user.getNome(), user.getRole());
        } catch (Exception ignored) { }
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

        if (!passwordEncoder.matches(request.getSenha(), user.getSenha())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthResponse.builder().mensagem("Senha inválida").build());
        }
        if (!Boolean.TRUE.equals(user.getAtivo())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthResponse.builder().mensagem("Conta inativa. Entre em contato com o administrador.").build());
        }

        String token = jwtService.generateToken(user);

        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .role(user.getRole().name())
                .userId(user.getId())
                .nome(user.getNome())
                .empresaId(user.getEmpresa() != null ? user.getEmpresa().getId() : null)
                .build());
    }
}

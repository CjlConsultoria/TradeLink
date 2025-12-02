package com.example.CJLInvestimentos.controllers;

import com.example.CJLInvestimentos.dtos.request.LoginRequest;
import com.example.CJLInvestimentos.dtos.request.RegisterRequest;
import com.example.CJLInvestimentos.dtos.response.AuthResponse;
import com.example.CJLInvestimentos.entities.User;
import com.example.CJLInvestimentos.entities.enums.Role;
import com.example.CJLInvestimentos.repositories.UserRepository;
import com.example.CJLInvestimentos.services.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;


    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return new ResponseEntity<>(new AuthResponse(null, "E-mail já registrado"), HttpStatus.BAD_REQUEST);
        }

        Role role = (request.getRole() != null) ? request.getRole() : Role.Cliente;

        User user = User.builder()
                .nome(request.getNome())
                .email(request.getEmail())
                .senha(passwordEncoder.encode(request.getSenha()))
                .role(role)
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user);
        return new ResponseEntity<>(new AuthResponse(token, null), HttpStatus.CREATED);
    }

    //LOGIN
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user == null) {
            return new ResponseEntity<>(new AuthResponse(null, "Usuário não encontrado"), HttpStatus.NOT_FOUND);
        }

        if (!passwordEncoder.matches(request.getSenha(), user.getSenha())) {
            return new ResponseEntity<>(new AuthResponse(null, "Senha inválida"), HttpStatus.UNAUTHORIZED);
        }

        String token = jwtService.generateToken(user);
        return new ResponseEntity<>(new AuthResponse(token, null), HttpStatus.OK);
    }
}
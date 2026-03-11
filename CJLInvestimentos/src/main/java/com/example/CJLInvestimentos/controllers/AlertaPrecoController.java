package com.example.CJLInvestimentos.controllers;

import com.example.CJLInvestimentos.dtos.request.AlertaPrecoRequest;
import com.example.CJLInvestimentos.dtos.response.AlertaPrecoResponse;
import com.example.CJLInvestimentos.entities.User;
import com.example.CJLInvestimentos.repositories.UserRepository;
import com.example.CJLInvestimentos.services.AlertaPrecoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/me/alertas-preco")
@RequiredArgsConstructor
public class AlertaPrecoController {

    private final AlertaPrecoService alertaPrecoService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<AlertaPrecoResponse>> listar(@AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        return ResponseEntity.ok(alertaPrecoService.listarPorUsuario(user.getId()));
    }

    @PostMapping
    public ResponseEntity<AlertaPrecoResponse> criar(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AlertaPrecoRequest request) {
        User user = getUser(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(alertaPrecoService.criar(user.getId(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AlertaPrecoResponse> atualizar(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody AlertaPrecoRequest request) {
        User user = getUser(userDetails);
        return ResponseEntity.ok(alertaPrecoService.atualizar(user.getId(), id, request));
    }

    @PutMapping("/{id}/toggle")
    public ResponseEntity<Void> alternarAtivo(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        User user = getUser(userDetails);
        alertaPrecoService.alternarAtivo(user.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        User user = getUser(userDetails);
        alertaPrecoService.excluir(user.getId(), id);
        return ResponseEntity.noContent().build();
    }

    private User getUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
    }
}

package com.example.CJLInvestimentos.controllers;

import com.example.CJLInvestimentos.dtos.request.CriarChamadoRequest;
import com.example.CJLInvestimentos.dtos.request.ResponderChamadoRequest;
import com.example.CJLInvestimentos.dtos.response.ChamadoRespostaResponse;
import com.example.CJLInvestimentos.dtos.response.ChamadoResponse;
import com.example.CJLInvestimentos.entities.User;
import com.example.CJLInvestimentos.repositories.UserRepository;
import com.example.CJLInvestimentos.services.ChamadoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints de chamados para Cliente e Consultor.
 * Security: /api/cliente/** permite Cliente, Admin e AdminMax.
 */
@RestController
@RequestMapping("/api/cliente/chamados")
@RequiredArgsConstructor
public class ChamadoController {

    private final ChamadoService chamadoService;
    private final UserRepository userRepository;

    private User getUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    @PostMapping
    public ResponseEntity<ChamadoResponse> criar(
            @Valid @RequestBody CriarChamadoRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(chamadoService.criarChamado(user, request));
    }

    @GetMapping
    public ResponseEntity<List<ChamadoResponse>> listarMeus(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        return ResponseEntity.ok(chamadoService.listarChamadosUsuario(user.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChamadoResponse> buscar(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        return ResponseEntity.ok(chamadoService.buscarChamado(id, user));
    }

    @GetMapping("/{id}/respostas")
    public ResponseEntity<List<ChamadoRespostaResponse>> listarRespostas(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        return ResponseEntity.ok(chamadoService.listarRespostas(id, user));
    }

    @PostMapping("/{id}/respostas")
    public ResponseEntity<ChamadoRespostaResponse> responder(
            @PathVariable Long id,
            @Valid @RequestBody ResponderChamadoRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(chamadoService.responderChamado(id, user, request));
    }
}

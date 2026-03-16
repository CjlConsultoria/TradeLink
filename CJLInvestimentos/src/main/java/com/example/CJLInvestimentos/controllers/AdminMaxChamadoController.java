package com.example.CJLInvestimentos.controllers;

import com.example.CJLInvestimentos.dtos.request.AtualizarStatusChamadoRequest;
import com.example.CJLInvestimentos.dtos.request.ResponderChamadoRequest;
import com.example.CJLInvestimentos.dtos.response.ChamadoRespostaResponse;
import com.example.CJLInvestimentos.dtos.response.ChamadoResponse;
import com.example.CJLInvestimentos.entities.User;
import com.example.CJLInvestimentos.entities.enums.StatusChamado;
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
import java.util.Map;

/**
 * Endpoints de gerenciamento de chamados para AdminMax.
 */
@RestController
@RequestMapping("/api/admin-max/chamados")
@RequiredArgsConstructor
public class AdminMaxChamadoController {

    private final ChamadoService chamadoService;
    private final UserRepository userRepository;

    private User getUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    @GetMapping
    public ResponseEntity<List<ChamadoResponse>> listarTodos(
            @RequestParam(required = false) StatusChamado status,
            @AuthenticationPrincipal UserDetails userDetails) {
        User admin = getUser(userDetails);
        if (status != null) {
            return ResponseEntity.ok(chamadoService.listarChamadosPorStatus(status, admin.getId()));
        }
        return ResponseEntity.ok(chamadoService.listarTodosChamados(admin.getId()));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> stats() {
        return ResponseEntity.ok(chamadoService.contarPorStatus());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChamadoResponse> buscar(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User admin = getUser(userDetails);
        return ResponseEntity.ok(chamadoService.buscarChamado(id, admin));
    }

    @GetMapping("/{id}/respostas")
    public ResponseEntity<List<ChamadoRespostaResponse>> listarRespostas(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User admin = getUser(userDetails);
        return ResponseEntity.ok(chamadoService.listarRespostas(id, admin));
    }

    @PostMapping("/{id}/respostas")
    public ResponseEntity<ChamadoRespostaResponse> responder(
            @PathVariable Long id,
            @Valid @RequestBody ResponderChamadoRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User admin = getUser(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(chamadoService.responderChamado(id, admin, request));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ChamadoResponse> atualizarStatus(
            @PathVariable Long id,
            @Valid @RequestBody AtualizarStatusChamadoRequest request) {
        return ResponseEntity.ok(chamadoService.atualizarStatus(id, request));
    }
}

package com.example.CJLInvestimentos.controllers;

import com.example.CJLInvestimentos.dtos.response.FaqResponse;
import com.example.CJLInvestimentos.entities.Plano;
import com.example.CJLInvestimentos.repositories.PlanoRepository;
import com.example.CJLInvestimentos.services.FaqService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Endpoints públicos (sem autenticação) para FAQ e listagem de planos.
 */
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {

    private final FaqService faqService;
    private final PlanoRepository planoRepository;

    @GetMapping("/faq")
    public ResponseEntity<List<FaqResponse>> listarFaq() {
        return ResponseEntity.ok(faqService.listarPublico());
    }

    @GetMapping("/planos")
    public ResponseEntity<List<Map<String, Object>>> listarPlanos() {
        List<Map<String, Object>> planos = planoRepository.findByAtivoTrue().stream()
                .map(p -> Map.<String, Object>of(
                        "id", p.getId(),
                        "nome", p.getNome(),
                        "tipo", p.getTipo() != null ? p.getTipo() : "CONSULTOR",
                        "preco", p.getPreco(),
                        "maxUsuarios", p.getMaxUsuarios()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(planos);
    }
}

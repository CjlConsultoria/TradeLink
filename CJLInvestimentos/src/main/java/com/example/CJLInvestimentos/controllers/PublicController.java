package com.example.CJLInvestimentos.controllers;

import com.example.CJLInvestimentos.dtos.response.FaqResponse;
import com.example.CJLInvestimentos.dtos.response.MarketplaceConsultorResponse;
import com.example.CJLInvestimentos.entities.Plano;
import com.example.CJLInvestimentos.repositories.PlanoRepository;
import com.example.CJLInvestimentos.services.FaqService;
import com.example.CJLInvestimentos.services.MarketplaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    private final MarketplaceService marketplaceService;

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

    // ─── Marketplace público ─────────────────────────────────────

    @GetMapping("/marketplace")
    public ResponseEntity<List<MarketplaceConsultorResponse>> listarConsultoresMarketplace(
            @RequestParam(required = false) String termo) {
        return ResponseEntity.ok(marketplaceService.listarConsultores(termo));
    }

    @GetMapping("/marketplace/{empresaId}")
    public ResponseEntity<MarketplaceConsultorResponse> getConsultorMarketplace(@PathVariable Long empresaId) {
        return ResponseEntity.ok(marketplaceService.getConsultorDetalhe(empresaId));
    }
}

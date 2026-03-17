package com.example.CJLInvestimentos.controllers;

import com.example.CJLInvestimentos.dtos.request.MarketplacePerfilRequest;
import com.example.CJLInvestimentos.dtos.response.MarketplacePerfilResponse;
import com.example.CJLInvestimentos.dtos.response.SolicitacaoMentoriaResponse;
import com.example.CJLInvestimentos.services.ConfiguracaoPlataformaService;
import com.example.CJLInvestimentos.services.MarketplaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin-max/marketplace")
@RequiredArgsConstructor
public class AdminMaxMarketplaceController {

    private final MarketplaceService marketplaceService;
    private final ConfiguracaoPlataformaService configService;

    /** Listar todos os consultores com perfil marketplace (visíveis ou não). */
    @GetMapping("/consultores")
    public ResponseEntity<List<MarketplacePerfilResponse>> listarConsultores() {
        return ResponseEntity.ok(marketplaceService.listarTodosConsultoresMarketplace());
    }

    /** Forçar visibilidade de um consultor no marketplace. */
    @PutMapping("/consultores/{empresaId}/visibilidade")
    public ResponseEntity<?> alterarVisibilidade(
            @PathVariable Long empresaId,
            @RequestBody Map<String, Boolean> body) {
        marketplaceService.adminAlterarVisibilidade(empresaId, body.getOrDefault("visivel", false));
        return ResponseEntity.ok(Map.of("message", "Visibilidade atualizada"));
    }

    /** Editar perfil marketplace de qualquer consultor. */
    @PutMapping("/consultores/{empresaId}/perfil")
    public ResponseEntity<?> atualizarPerfil(
            @PathVariable Long empresaId,
            @RequestBody MarketplacePerfilRequest request) {
        marketplaceService.adminAtualizarPerfil(empresaId, request);
        return ResponseEntity.ok(Map.of("message", "Perfil atualizado"));
    }

    /** Remover consultor do marketplace completamente. */
    @DeleteMapping("/consultores/{empresaId}")
    public ResponseEntity<?> removerDoMarketplace(@PathVariable Long empresaId) {
        marketplaceService.adminRemoverDoMarketplace(empresaId);
        return ResponseEntity.ok(Map.of("message", "Consultor removido do marketplace"));
    }

    /** Listar todas as solicitações de mentoria (filtro por status). */
    @GetMapping("/solicitacoes")
    public ResponseEntity<List<SolicitacaoMentoriaResponse>> listarSolicitacoes(
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(marketplaceService.adminListarSolicitacoes(status));
    }

    /** Forçar status de uma solicitação. */
    @PutMapping("/solicitacoes/{id}/status")
    public ResponseEntity<?> alterarStatusSolicitacao(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        marketplaceService.adminAlterarStatusSolicitacao(id, body.get("status"));
        return ResponseEntity.ok(Map.of("message", "Status atualizado"));
    }

    /** Dashboard: stats do marketplace. */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(marketplaceService.getStatsMarketplace());
    }

    /** Obter taxa da plataforma. */
    @GetMapping("/taxa")
    public ResponseEntity<Map<String, BigDecimal>> getTaxa() {
        return ResponseEntity.ok(Map.of("taxa", configService.getTaxaMarketplace()));
    }

    /** Alterar taxa da plataforma. */
    @PutMapping("/taxa")
    public ResponseEntity<?> alterarTaxa(@RequestBody Map<String, BigDecimal> body) {
        BigDecimal novaTaxa = body.get("taxa");
        if (novaTaxa == null || novaTaxa.compareTo(BigDecimal.ZERO) < 0 || novaTaxa.compareTo(new BigDecimal("100")) > 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "Taxa deve estar entre 0 e 100"));
        }
        configService.setTaxaMarketplace(novaTaxa);
        return ResponseEntity.ok(Map.of("message", "Taxa atualizada", "taxa", novaTaxa));
    }
}

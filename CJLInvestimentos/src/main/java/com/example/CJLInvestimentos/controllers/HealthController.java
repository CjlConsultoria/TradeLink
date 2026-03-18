package com.example.CJLInvestimentos.controllers;

import com.example.CJLInvestimentos.services.ConfiguracaoSistemaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class HealthController {

    private final ConfiguracaoSistemaService configuracaoSistemaService;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "timestamp", Instant.now().toString(),
                "service", "TradeLink API"
        ));
    }

    /** Retorna configuracoes publicas do sistema (ex: se 2FA esta ativo). */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> config() {
        return ResponseEntity.ok(Map.of(
                "doisFatoresAtivo", configuracaoSistemaService.isDoisFatoresAtivo()
        ));
    }
}

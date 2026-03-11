package com.example.CJLInvestimentos.controllers;

import com.example.CJLInvestimentos.dtos.response.CotacaoHistoricoResponse;
import com.example.CJLInvestimentos.dtos.response.CotacaoResponse;
import com.example.CJLInvestimentos.dtos.response.PageResponse;
import com.example.CJLInvestimentos.services.CotacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cotacoes")
@RequiredArgsConstructor
public class CotacaoController {

    private final CotacaoService cotacaoService;

    @GetMapping
    public ResponseEntity<List<CotacaoResponse>> listarUltimas() {
        return ResponseEntity.ok(cotacaoService.listarUltimasCotacoes());
    }

    @GetMapping("/paginado")
    public ResponseEntity<PageResponse<CotacaoResponse>> listarPaginado(
            @RequestParam(required = false) String moeda,
            @RequestParam(required = false) String parMoeda,
            @RequestParam(required = false) String fonte,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataDe,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataAte,
            @RequestParam(required = false) BigDecimal precoCompraMin,
            @RequestParam(required = false) BigDecimal precoCompraMax,
            @RequestParam(required = false) BigDecimal precoVendaMin,
            @RequestParam(required = false) BigDecimal precoVendaMax,
            @RequestParam(required = false) BigDecimal variacaoMin,
            @RequestParam(required = false) BigDecimal variacaoMax,
            @RequestParam(required = false) BigDecimal maximoMin,
            @RequestParam(required = false) BigDecimal maximoMax,
            @RequestParam(required = false) BigDecimal minimoMin,
            @RequestParam(required = false) BigDecimal minimoMax,
            @RequestParam(required = false) String ordenarPor,
            @RequestParam(required = false) String direcao,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(cotacaoService.listarPaginado(
                moeda, parMoeda, fonte,
                dataDe, dataAte,
                precoCompraMin, precoCompraMax,
                precoVendaMin, precoVendaMax,
                variacaoMin, variacaoMax,
                maximoMin, maximoMax,
                minimoMin, minimoMax,
                ordenarPor, direcao,
                page, size));
    }

    @GetMapping("/{moeda}/{parMoeda}")
    public ResponseEntity<CotacaoResponse> buscarUltima(
            @PathVariable String moeda, @PathVariable String parMoeda) {
        CotacaoResponse response = cotacaoService.buscarUltimaCotacao(moeda, parMoeda);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{moeda}/{parMoeda}/historico")
    public ResponseEntity<List<CotacaoResponse>> historico(
            @PathVariable String moeda, @PathVariable String parMoeda,
            @RequestParam(defaultValue = "24") int horas) {
        return ResponseEntity.ok(cotacaoService.historico(moeda, parMoeda, horas));
    }

    /**
     * Retorna dados históricos OHLCV (candlestick) para um par.
     * Intervalos suportados: 1min, 5min, 15min, 30min, 1h, 4h, 1day, 1week, 1month
     */
    @GetMapping("/{moeda}/{parMoeda}/ohlcv")
    public ResponseEntity<List<CotacaoHistoricoResponse>> historicoOHLCV(
            @PathVariable String moeda,
            @PathVariable String parMoeda,
            @RequestParam(defaultValue = "1day") String intervalo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime ate) {
        return ResponseEntity.ok(cotacaoService.historicoOHLCV(moeda, parMoeda, intervalo, de, ate));
    }

    /**
     * Lista todos os pares monitorados (forex + crypto).
     */
    @GetMapping("/pares-disponiveis")
    public ResponseEntity<List<Map<String, String>>> paresDisponiveis() {
        return ResponseEntity.ok(cotacaoService.paresDisponiveis());
    }

    @PostMapping("/refresh")
    public ResponseEntity<List<CotacaoResponse>> refreshTodas() {
        cotacaoService.forceRefresh();
        return ResponseEntity.ok(cotacaoService.listarUltimasCotacoes());
    }

    @GetMapping("/refresh/{moeda}/{parMoeda}")
    public ResponseEntity<CotacaoResponse> refreshUma(
            @PathVariable String moeda, @PathVariable String parMoeda) {
        CotacaoResponse response = cotacaoService.fetchSingleQuote(moeda, parMoeda);
        return ResponseEntity.ok(response);
    }
}

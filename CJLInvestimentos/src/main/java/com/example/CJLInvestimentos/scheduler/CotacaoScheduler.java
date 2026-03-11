package com.example.CJLInvestimentos.scheduler;

import com.example.CJLInvestimentos.services.CotacaoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler de cotações — APIs gratuitas.
 *
 * Três tarefas:
 *   1. Forex (AwesomeAPI) a cada 10 minutos
 *   2. Crypto (CoinGecko) a cada 10 minutos (com delay escalonado)
 *   3. Histórico OHLC (CoinGecko) a cada 1 hora
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CotacaoScheduler {

    private final CotacaoService cotacaoService;

    /**
     * Busca cotações forex via AwesomeAPI.
     * Intervalo: 10 minutos. Delay inicial: 5 segundos.
     */
    @Scheduled(initialDelayString = "${app.cotacao.initial-delay:5000}",
               fixedDelayString = "${app.cotacao.interval:600000}")
    public void fetchForex() {
        log.info("Buscando cotações forex via AwesomeAPI...");
        try {
            cotacaoService.fetchAndSaveForex();
        } catch (Exception e) {
            log.error("Erro no scheduler forex: {}", e.getMessage());
        }
    }

    /**
     * Busca cotações crypto via CoinGecko.
     * Intervalo: 10 minutos. Delay inicial: 30 segundos (escalonado para não bater rate limit).
     */
    @Scheduled(initialDelayString = "${app.cotacao.initial-delay-crypto:30000}",
               fixedDelayString = "${app.cotacao.interval:600000}")
    public void fetchCrypto() {
        log.info("Buscando cotações crypto via CoinGecko...");
        try {
            cotacaoService.fetchAndSaveCrypto();
        } catch (Exception e) {
            log.error("Erro no scheduler crypto: {}", e.getMessage());
        }
    }

    /**
     * Atualiza histórico OHLC dos cryptos principais via CoinGecko.
     * Intervalo: 1 hora. Delay inicial: 2 minutos.
     */
    @Scheduled(initialDelayString = "${app.cotacao.historico-initial-delay:120000}",
               fixedDelayString = "${app.cotacao.historico-interval:3600000}")
    public void atualizarHistoricoOHLC() {
        log.info("Atualizando histórico OHLC via CoinGecko...");
        try {
            cotacaoService.atualizarHistoricoPrincipal();
        } catch (Exception e) {
            log.error("Erro no scheduler de histórico OHLC: {}", e.getMessage());
        }
    }
}

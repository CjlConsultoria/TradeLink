package com.example.CJLInvestimentos.scheduler;

import com.example.CJLInvestimentos.services.CotacaoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CotacaoScheduler {

    private final CotacaoService cotacaoService;

    @Scheduled(initialDelayString = "${app.cotacao.initial-delay:0}", fixedDelayString = "${app.cotacao.awesome-api.interval}")
    public void fetchAwesomeApi() {
        log.info("Buscando cotações da AwesomeAPI...");
        cotacaoService.fetchAndSaveAwesomeApi();
    }

    @Scheduled(initialDelayString = "${app.cotacao.initial-delay:0}", fixedDelayString = "${app.cotacao.coingecko.interval}")
    public void fetchCoinGecko() {
        log.info("Buscando cotações do CoinGecko...");
        cotacaoService.fetchAndSaveCoinGecko();
    }
}

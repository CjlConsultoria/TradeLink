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

    @Scheduled(initialDelayString = "${app.cotacao.initial-delay-coingecko:30000}", fixedDelayString = "${app.cotacao.coingecko.interval}")
    public void fetchCoinGecko() {
        log.info("Buscando cotações do CoinGecko...");
        cotacaoService.fetchAndSaveCoinGecko();
    }

    @Scheduled(initialDelayString = "${app.cotacao.initial-delay-binance:60000}", fixedDelayString = "${app.cotacao.binance.interval:600000}")
    public void fetchBinance() {
        log.info("Buscando cotações da Binance...");
        cotacaoService.fetchAndSaveBinance();
    }

    @Scheduled(initialDelayString = "${app.cotacao.initial-delay-coincap:90000}", fixedDelayString = "${app.cotacao.coincap.interval:600000}")
    public void fetchCoinCap() {
        log.info("Buscando cotações do CoinCap...");
        cotacaoService.fetchAndSaveCoinCap();
    }

    @Scheduled(initialDelayString = "${app.cotacao.initial-delay-frankfurter:240000}", fixedDelayString = "${app.cotacao.frankfurter.interval:600000}")
    public void fetchFrankfurter() {
        log.info("Buscando cotações do Frankfurter...");
        cotacaoService.fetchAndSaveFrankfurter();
    }

    @Scheduled(initialDelayString = "${app.cotacao.initial-delay-kraken:270000}", fixedDelayString = "${app.cotacao.kraken.interval:600000}")
    public void fetchKraken() {
        log.info("Buscando cotações do Kraken...");
        cotacaoService.fetchAndSaveKraken();
    }

    @Scheduled(initialDelayString = "${app.cotacao.initial-delay-kucoin:300000}", fixedDelayString = "${app.cotacao.kucoin.interval:600000}")
    public void fetchKuCoin() {
        log.info("Buscando cotações do KuCoin...");
        cotacaoService.fetchAndSaveKuCoin();
    }

    @Scheduled(initialDelayString = "${app.cotacao.initial-delay-bybit:330000}", fixedDelayString = "${app.cotacao.bybit.interval:600000}")
    public void fetchBybit() {
        log.info("Buscando cotações do Bybit...");
        cotacaoService.fetchAndSaveBybit();
    }
}

package com.example.CJLInvestimentos.services;

import com.example.CJLInvestimentos.dtos.response.CotacaoResponse;
import com.example.CJLInvestimentos.dtos.response.PageResponse;
import com.example.CJLInvestimentos.entities.Cotacao;
import com.example.CJLInvestimentos.exceptions.BusinessException;
import com.example.CJLInvestimentos.repositories.CotacaoRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Map.entry;

@Service
@RequiredArgsConstructor
@Slf4j
public class CotacaoService {

    private final CotacaoRepository cotacaoRepository;
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${app.cotacao.awesome-api.url}")
    private String awesomeApiUrl;

    @Value("${app.cotacao.coingecko.url}")
    private String coinGeckoUrl;

    @Value("${app.cotacao.binance.url:https://api.binance.com/api/v3/ticker/price}")
    private String binanceUrl;

    @Value("${app.cotacao.coincap.assets.url:https://api.coincap.io/v2/assets?limit=80}")
    private String coinCapAssetsUrl;

    @Value("${app.cotacao.coincap.rate-brl.url:https://api.coincap.io/v2/rates/brazilian-real}")
    private String coinCapRateBrlUrl;

    @Value("${app.cotacao.frankfurter.url:https://api.frankfurter.dev/v1/latest?base=USD&symbols=BRL,EUR,GBP,CHF,JPY,CAD,AUD}")
    private String frankfurterUrl;

    @Value("${app.cotacao.kraken.url:https://api.kraken.com/0/public/Ticker?pair=XBTUSD,ETHUSD,LTCUSD,XRPUSD,ADAUSD,SOLUSD,DOTUSD,AVAXUSD,MATICUSD,LINKUSD,UNIUSD,BNBUSD,DOGEUSD}")
    private String krakenUrl;

    @Value("${app.cotacao.kucoin.url:https://api.kucoin.com/api/v1/market/allTickers}")
    private String kucoinUrl;

    @Value("${app.cotacao.bybit.url:https://api.bybit.com/v5/market/tickers?category=spot}")
    private String bybitUrl;

    /** Mapeamento Kraken: par interno -> símbolo (XBT = Bitcoin). */
    private static final Map<String, String> KRAKEN_PAIR_TO_MOEDA = Map.ofEntries(
            entry("XXBTZUSD", "BTC"),
            entry("XETHZUSD", "ETH"),
            entry("XLTCZUSD", "LTC"),
            entry("XXRPZUSD", "XRP"),
            entry("ADAUSD", "ADA"),
            entry("SOLUSD", "SOL"),
            entry("DOTUSD", "DOT"),
            entry("AVAXUSD", "AVAX"),
            entry("MATICUSD", "MATIC"),
            entry("LINKUSD", "LINK"),
            entry("UNIUSD", "UNI"),
            entry("BNBUSD", "BNB"),
            entry("XDGUSD", "DOGE")
    );

    private static final Map<String, String> CRYPTO_MAP = Map.ofEntries(
            entry("bitcoin", "BTC"),
            entry("ethereum", "ETH"),
            entry("binancecoin", "BNB"),
            entry("ripple", "XRP"),
            entry("solana", "SOL"),
            entry("cardano", "ADA"),
            entry("dogecoin", "DOGE"),
            entry("polkadot", "DOT"),
            entry("avalanche-2", "AVAX"),
            entry("matic-network", "MATIC"),
            entry("chainlink", "LINK"),
            entry("uniswap", "UNI"),
            entry("litecoin", "LTC")
    );

    @Transactional
    public void fetchAndSaveAwesomeApi() {
        try {
            String response = webClientBuilder.build()
                    .get()
                    .uri(awesomeApiUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = objectMapper.readTree(response);
            List<Cotacao> cotacoes = new ArrayList<>();

            Iterator<Map.Entry<String, JsonNode>> fields = root.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                JsonNode node = entry.getValue();

                String moeda = node.get("code").asText();
                String parMoeda = node.get("codein").asText();

                LocalDateTime dataHora;
                try {
                    long timestamp = node.get("timestamp").asLong();
                    dataHora = LocalDateTime.ofInstant(
                            Instant.ofEpochSecond(timestamp),
                            ZoneId.of("America/Sao_Paulo")
                    );
                } catch (Exception e) {
                    dataHora = LocalDateTime.now();
                }

                cotacoes.add(Cotacao.builder()
                        .moeda(moeda)
                        .parMoeda(parMoeda)
                        .precoCompra(new BigDecimal(node.get("bid").asText()))
                        .precoVenda(new BigDecimal(node.get("ask").asText()))
                        .variacao(parseBigDecimal(node.get("pctChange").asText()))
                        .maximo(parseBigDecimal(node.get("high").asText()))
                        .minimo(parseBigDecimal(node.get("low").asText()))
                        .dataHora(dataHora)
                        .fonte("AWESOME_API")
                        .build());
            }

            cotacaoRepository.deleteByFonte("AWESOME_API");
            cotacaoRepository.saveAll(cotacoes);
            log.info("AwesomeAPI: {} cotações salvas (substituídas)", cotacoes.size());
        } catch (WebClientResponseException e) {
            if (e.getStatusCode() != null && e.getStatusCode().value() == 429) {
                log.warn("AwesomeAPI retornou 429 (limite de requisições). Próxima tentativa no próximo ciclo. Aumente app.cotacao.awesome-api.interval se persistir.");
            } else {
                log.error("Erro ao buscar cotações da AwesomeAPI: {}", e.getMessage());
            }
        } catch (Exception e) {
            log.error("Erro ao buscar cotações da AwesomeAPI: {}", e.getMessage());
        }
    }

    @Transactional
    public void fetchAndSaveCoinGecko() {
        try {
            String response = webClientBuilder.build()
                    .get()
                    .uri(coinGeckoUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = objectMapper.readTree(response);
            List<Cotacao> cotacoes = new ArrayList<>();
            LocalDateTime now = LocalDateTime.now();

            Iterator<Map.Entry<String, JsonNode>> cryptos = root.fields();
            while (cryptos.hasNext()) {
                Map.Entry<String, JsonNode> crypto = cryptos.next();
                String cryptoId = crypto.getKey();
                String moeda = CRYPTO_MAP.getOrDefault(cryptoId, cryptoId.toUpperCase());
                JsonNode prices = crypto.getValue();

                Iterator<Map.Entry<String, JsonNode>> currencies = prices.fields();
                while (currencies.hasNext()) {
                    Map.Entry<String, JsonNode> currency = currencies.next();
                    String parMoeda = currency.getKey().toUpperCase();
                    BigDecimal price = new BigDecimal(currency.getValue().asText());

                    cotacoes.add(Cotacao.builder()
                            .moeda(moeda)
                            .parMoeda(parMoeda)
                            .precoCompra(price)
                            .precoVenda(price)
                            .dataHora(now)
                            .fonte("COINGECKO")
                            .build());
                }
            }

            cotacaoRepository.deleteByFonte("COINGECKO");
            cotacaoRepository.saveAll(cotacoes);
            log.info("CoinGecko: {} cotações salvas (substituídas)", cotacoes.size());
        } catch (WebClientResponseException e) {
            if (e.getStatusCode() != null && e.getStatusCode().value() == 429) {
                log.warn("CoinGecko retornou 429 (limite de requisições). Próxima tentativa no próximo ciclo. Aumente app.cotacao.coingecko.interval se persistir.");
            } else {
                log.error("Erro ao buscar cotações do CoinGecko: {}", e.getMessage());
            }
        } catch (Exception e) {
            log.error("Erro ao buscar cotações do CoinGecko: {}", e.getMessage());
        }
    }

    @Transactional
    public void fetchAndSaveBinance() {
        try {
            String response = webClientBuilder.build()
                    .get()
                    .uri(binanceUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            JsonNode root = objectMapper.readTree(response);
            if (!root.isArray()) return;
            List<Cotacao> cotacoes = new ArrayList<>();
            LocalDateTime now = LocalDateTime.now();
            for (JsonNode item : root) {
                String symbol = item.has("symbol") ? item.get("symbol").asText() : "";
                if (symbol.length() < 4) continue;
                String par = symbol.substring(symbol.length() - 3);
                if (!"BRL".equals(par) && !"USDT".equals(par)) continue;
                String moeda = symbol.substring(0, symbol.length() - 3);
                BigDecimal price = parseBigDecimal(item.has("price") ? item.get("price").asText() : null);
                if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) continue;
                cotacoes.add(Cotacao.builder()
                        .moeda(moeda)
                        .parMoeda(par)
                        .precoCompra(price)
                        .precoVenda(price)
                        .dataHora(now)
                        .fonte("BINANCE")
                        .build());
            }
            if (!cotacoes.isEmpty()) {
                cotacaoRepository.deleteByFonte("BINANCE");
                cotacaoRepository.saveAll(cotacoes);
                log.info("Binance: {} cotações salvas (substituídas)", cotacoes.size());
            }
        } catch (WebClientResponseException e) {
            int code = e.getStatusCode() != null ? e.getStatusCode().value() : 0;
            if (code == 429) {
                log.warn("Binance retornou 429 (rate limit). Próxima tentativa no próximo ciclo.");
            } else if (code == 451) {
                log.warn("Binance retornou 451 (indisponível por restrição legal/região). Cotações da Binance não serão atualizadas.");
            } else {
                log.error("Erro ao buscar cotações da Binance: {}", e.getMessage());
            }
        } catch (Exception e) {
            log.error("Erro ao buscar cotações da Binance: {}", e.getMessage());
        }
    }

    @Transactional
    public void fetchAndSaveCoinCap() {
        try {
            String rateResponse = webClientBuilder.build()
                    .get()
                    .uri(coinCapRateBrlUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            JsonNode rateNode = objectMapper.readTree(rateResponse);
            BigDecimal usdPerBrl = BigDecimal.ONE;
            if (rateNode.has("data") && rateNode.get("data").has("rateUsd")) {
                try {
                    usdPerBrl = new BigDecimal(rateNode.get("data").get("rateUsd").asText());
                } catch (Exception ignored) {}
            }
            String assetsResponse = webClientBuilder.build()
                    .get()
                    .uri(coinCapAssetsUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            JsonNode assetsRoot = objectMapper.readTree(assetsResponse);
            JsonNode data = assetsRoot.has("data") ? assetsRoot.get("data") : null;
            if (data == null || !data.isArray()) return;
            List<Cotacao> cotacoes = new ArrayList<>();
            LocalDateTime now = LocalDateTime.now();
            for (JsonNode asset : data) {
                String symbol = asset.has("symbol") ? asset.get("symbol").asText().toUpperCase() : "";
                if (symbol.isEmpty()) continue;
                String priceUsdStr = asset.has("priceUsd") ? asset.get("priceUsd").asText() : null;
                BigDecimal priceUsd = parseBigDecimal(priceUsdStr);
                if (priceUsd == null || priceUsd.compareTo(BigDecimal.ZERO) <= 0) continue;
                BigDecimal priceBrl = priceUsd.divide(usdPerBrl, 8, java.math.RoundingMode.HALF_UP);
                cotacoes.add(Cotacao.builder()
                        .moeda(symbol)
                        .parMoeda("BRL")
                        .precoCompra(priceBrl)
                        .precoVenda(priceBrl)
                        .dataHora(now)
                        .fonte("COINCAP")
                        .build());
                cotacoes.add(Cotacao.builder()
                        .moeda(symbol)
                        .parMoeda("USD")
                        .precoCompra(priceUsd)
                        .precoVenda(priceUsd)
                        .dataHora(now)
                        .fonte("COINCAP")
                        .build());
            }
            if (!cotacoes.isEmpty()) {
                cotacaoRepository.deleteByFonte("COINCAP");
                cotacaoRepository.saveAll(cotacoes);
                log.info("CoinCap: {} cotações salvas (substituídas)", cotacoes.size());
            }
        } catch (WebClientResponseException e) {
            if (e.getStatusCode() != null && e.getStatusCode().value() == 429) {
                log.warn("CoinCap retornou 429. Próxima tentativa no próximo ciclo.");
            } else {
                log.error("Erro ao buscar cotações do CoinCap: {}", e.getMessage());
            }
        } catch (Exception e) {
            log.error("Erro ao buscar cotações do CoinCap: {}", e.getMessage());
        }
    }

    @Transactional
    public void fetchAndSaveFrankfurter() {
        try {
            String response = webClientBuilder.build()
                    .get()
                    .uri(frankfurterUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            JsonNode root = objectMapper.readTree(response);
            String base = root.has("base") ? root.get("base").asText().toUpperCase() : "USD";
            JsonNode rates = root.get("rates");
            if (rates == null || !rates.isObject()) return;
            List<Cotacao> cotacoes = new ArrayList<>();
            LocalDateTime now = LocalDateTime.now();
            Iterator<Map.Entry<String, JsonNode>> it = rates.fields();
            while (it.hasNext()) {
                Map.Entry<String, JsonNode> e = it.next();
                String par = e.getKey().toUpperCase();
                BigDecimal price = parseBigDecimal(e.getValue().asText());
                if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) continue;
                cotacoes.add(Cotacao.builder()
                        .moeda(base)
                        .parMoeda(par)
                        .precoCompra(price)
                        .precoVenda(price)
                        .dataHora(now)
                        .fonte("FRANKFURTER")
                        .build());
            }
            if (!cotacoes.isEmpty()) {
                cotacaoRepository.deleteByFonte("FRANKFURTER");
                cotacaoRepository.saveAll(cotacoes);
                log.info("Frankfurter: {} cotações salvas (substituídas)", cotacoes.size());
            }
        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode() != null && ex.getStatusCode().value() == 429) {
                log.warn("Frankfurter retornou 429. Próxima tentativa no próximo ciclo.");
            } else {
                log.error("Erro ao buscar cotações do Frankfurter: {}", ex.getMessage());
            }
        } catch (Exception e) {
            log.error("Erro ao buscar cotações do Frankfurter: {}", e.getMessage());
        }
    }

    @Transactional
    public void fetchAndSaveKraken() {
        try {
            String response = webClientBuilder.build()
                    .get()
                    .uri(krakenUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            JsonNode root = objectMapper.readTree(response);
            if (root.has("error") && root.get("error").isArray() && root.get("error").size() > 0) return;
            JsonNode result = root.get("result");
            if (result == null || !result.isObject()) return;
            List<Cotacao> cotacoes = new ArrayList<>();
            LocalDateTime now = LocalDateTime.now();
            Iterator<Map.Entry<String, JsonNode>> it = result.fields();
            while (it.hasNext()) {
                Map.Entry<String, JsonNode> e = it.next();
                String pairKey = e.getKey();
                String moeda = KRAKEN_PAIR_TO_MOEDA.get(pairKey);
                if (moeda == null) {
                    if (pairKey.endsWith("ZUSD")) moeda = pairKey.replace("ZUSD", "").replace("X", "").replace("XXBT", "BTC").replace("XETH", "ETH").replace("XLTC", "LTC").replace("XXRP", "XRP");
                    else if (pairKey.endsWith("USD")) moeda = pairKey.replace("USD", "");
                    else continue;
                }
                JsonNode ticker = e.getValue();
                JsonNode c = ticker.get("c");
                if (c == null || !c.isArray()) continue;
                String priceStr = c.get(0).asText();
                BigDecimal price = parseBigDecimal(priceStr);
                if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) continue;
                cotacoes.add(Cotacao.builder()
                        .moeda(moeda)
                        .parMoeda("USD")
                        .precoCompra(price)
                        .precoVenda(price)
                        .dataHora(now)
                        .fonte("KRAKEN")
                        .build());
            }
            if (!cotacoes.isEmpty()) {
                cotacaoRepository.deleteByFonte("KRAKEN");
                cotacaoRepository.saveAll(cotacoes);
                log.info("Kraken: {} cotações salvas (substituídas)", cotacoes.size());
            }
        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode() != null && ex.getStatusCode().value() == 429) {
                log.warn("Kraken retornou 429. Próxima tentativa no próximo ciclo.");
            } else {
                log.error("Erro ao buscar cotações do Kraken: {}", ex.getMessage());
            }
        } catch (Exception e) {
            log.error("Erro ao buscar cotações do Kraken: {}", e.getMessage());
        }
    }

    @Transactional
    public void fetchAndSaveKuCoin() {
        try {
            String response = webClientBuilder.build()
                    .get()
                    .uri(kucoinUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            JsonNode root = objectMapper.readTree(response);
            JsonNode data = root.has("data") ? root.get("data") : null;
            if (data == null) return;
            JsonNode tickers = data.get("ticker");
            if (tickers == null || !tickers.isArray()) return;
            List<Cotacao> cotacoes = new ArrayList<>();
            LocalDateTime now = LocalDateTime.now();
            Set<String> allowedPairs = Set.of("BTC-USDT", "ETH-USDT", "BNB-USDT", "XRP-USDT", "SOL-USDT", "ADA-USDT", "DOGE-USDT", "DOT-USDT", "AVAX-USDT", "MATIC-USDT", "LINK-USDT", "UNI-USDT", "LTC-USDT");
            for (JsonNode t : tickers) {
                String symbol = t.has("symbol") ? t.get("symbol").asText() : "";
                if (!allowedPairs.contains(symbol)) continue;
                String[] parts = symbol.split("-");
                if (parts.length != 2) continue;
                String moeda = parts[0].toUpperCase();
                String par = parts[1].toUpperCase();
                String priceStr = t.has("last") ? t.get("last").asText() : (t.has("sell") ? t.get("sell").asText() : null);
                BigDecimal price = parseBigDecimal(priceStr);
                if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) continue;
                cotacoes.add(Cotacao.builder()
                        .moeda(moeda)
                        .parMoeda(par)
                        .precoCompra(price)
                        .precoVenda(price)
                        .dataHora(now)
                        .fonte("KUCOIN")
                        .build());
            }
            if (!cotacoes.isEmpty()) {
                cotacaoRepository.deleteByFonte("KUCOIN");
                cotacaoRepository.saveAll(cotacoes);
                log.info("KuCoin: {} cotações salvas (substituídas)", cotacoes.size());
            }
        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode() != null && ex.getStatusCode().value() == 429) {
                log.warn("KuCoin retornou 429. Próxima tentativa no próximo ciclo.");
            } else {
                log.error("Erro ao buscar cotações do KuCoin: {}", ex.getMessage());
            }
        } catch (Exception e) {
            log.error("Erro ao buscar cotações do KuCoin: {}", e.getMessage());
        }
    }

    @Transactional
    public void fetchAndSaveBybit() {
        try {
            String response = webClientBuilder.build()
                    .get()
                    .uri(bybitUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            JsonNode root = objectMapper.readTree(response);
            if (root.has("retCode") && root.get("retCode").asInt() != 0) return;
            JsonNode result = root.get("result");
            if (result == null) return;
            JsonNode list = result.get("list");
            if (list == null || !list.isArray()) return;
            List<Cotacao> cotacoes = new ArrayList<>();
            LocalDateTime now = LocalDateTime.now();
            Set<String> want = Set.of("BTCUSDT", "ETHUSDT", "BNBUSDT", "XRPUSDT", "SOLUSDT", "ADAUSDT", "DOGEUSDT", "DOTUSDT", "AVAXUSDT", "MATICUSDT", "LINKUSDT", "UNIUSDT", "LTCUSDT");
            for (JsonNode item : list) {
                String symbol = item.has("symbol") ? item.get("symbol").asText() : "";
                if (!want.contains(symbol)) continue;
                String lastPrice = item.has("lastPrice") ? item.get("lastPrice").asText() : null;
                BigDecimal price = parseBigDecimal(lastPrice);
                if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) continue;
                String moeda = symbol.replace("USDT", "").replace("USDC", "").toUpperCase();
                if (moeda.isEmpty()) continue;
                cotacoes.add(Cotacao.builder()
                        .moeda(moeda)
                        .parMoeda("USDT")
                        .precoCompra(price)
                        .precoVenda(price)
                        .dataHora(now)
                        .fonte("BYBIT")
                        .build());
            }
            if (!cotacoes.isEmpty()) {
                cotacaoRepository.deleteByFonte("BYBIT");
                cotacaoRepository.saveAll(cotacoes);
                log.info("Bybit: {} cotações salvas (substituídas)", cotacoes.size());
            }
        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode() != null && ex.getStatusCode().value() == 429) {
                log.warn("Bybit retornou 429. Próxima tentativa no próximo ciclo.");
            } else {
                log.error("Erro ao buscar cotações do Bybit: {}", ex.getMessage());
            }
        } catch (Exception e) {
            log.error("Erro ao buscar cotações do Bybit: {}", e.getMessage());
        }
    }

    /** Atualiza todas as cotações chamando todas as fontes (AwesomeAPI, CoinGecko, Binance, CoinCap, Frankfurter, Kraken, KuCoin, Bybit). */
    public void forceRefresh() {
        fetchAndSaveAwesomeApi();
        fetchAndSaveCoinGecko();
        fetchAndSaveBinance();
        fetchAndSaveCoinCap();
        fetchAndSaveFrankfurter();
        fetchAndSaveKraken();
        fetchAndSaveKuCoin();
        fetchAndSaveBybit();
    }

    /** Remove todas as cotações do banco (zerar). */
    @Transactional
    public void limparTodas() {
        cotacaoRepository.deleteAll();
        log.info("Todas as cotações foram removidas.");
    }

    /** Zera a base e recarrega apenas as cotações atuais das fontes (sem duplicar). */
    @Transactional
    public void zerarERecarregar() {
        limparTodas();
        forceRefresh();
    }

    /**
     * Busca e salva cotação individual (útil para refresh sob demanda).
     * Suporta pares forex (ex: USD-BRL) e cryptos (ex: BTC-BRL via CoinGecko).
     */
    public CotacaoResponse fetchSingleQuote(String moeda, String parMoeda) {
        String moedaUpper = moeda.toUpperCase();
        String parUpper = parMoeda.toUpperCase();
        String coingeckoId = CRYPTO_ID_BY_SYMBOL.get(moedaUpper);
        if (coingeckoId != null) {
            fetchAndSaveSingleCrypto(coingeckoId, moedaUpper, parUpper);
        } else {
            fetchAndSaveSingleAwesome(moedaUpper, parUpper);
        }
        return buscarUltimaCotacao(moedaUpper, parUpper);
    }

    private static final Map<String, String> CRYPTO_ID_BY_SYMBOL = Map.ofEntries(
            entry("BTC", "bitcoin"),
            entry("ETH", "ethereum"),
            entry("BNB", "binancecoin"),
            entry("XRP", "ripple"),
            entry("SOL", "solana"),
            entry("ADA", "cardano"),
            entry("DOGE", "dogecoin"),
            entry("DOT", "polkadot"),
            entry("AVAX", "avalanche-2"),
            entry("MATIC", "matic-network"),
            entry("LINK", "chainlink"),
            entry("UNI", "uniswap"),
            entry("LTC", "litecoin")
    );

    private void fetchAndSaveSingleAwesome(String moeda, String parMoeda) {
        try {
            String url = "https://economia.awesomeapi.com.br/json/last/" + moeda + "-" + parMoeda;
            String response = webClientBuilder.build().get().uri(url).retrieve().bodyToMono(String.class).block();
            JsonNode root = objectMapper.readTree(response);
            JsonNode node = root.fields().next().getValue();
            String code = node.get("code").asText();
            String codein = node.get("codein").asText();
            LocalDateTime dataHora;
            try {
                long timestamp = node.get("timestamp").asLong();
                dataHora = LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.of("America/Sao_Paulo"));
            } catch (Exception e) {
                dataHora = LocalDateTime.now();
            }
            Cotacao c = Cotacao.builder()
                    .moeda(code)
                    .parMoeda(codein)
                    .precoCompra(new BigDecimal(node.get("bid").asText()))
                    .precoVenda(new BigDecimal(node.get("ask").asText()))
                    .variacao(parseBigDecimal(node.has("pctChange") ? node.get("pctChange").asText() : null))
                    .maximo(parseBigDecimal(node.has("high") ? node.get("high").asText() : null))
                    .minimo(parseBigDecimal(node.has("low") ? node.get("low").asText() : null))
                    .dataHora(dataHora)
                    .fonte("AWESOME_API")
                    .build();
            cotacaoRepository.save(c);
        } catch (Exception e) {
            log.error("Erro ao buscar cotação única AwesomeAPI {}-{}", moeda, parMoeda, e);
            throw new BusinessException("Não foi possível atualizar a cotação");
        }
    }

    private void fetchAndSaveSingleCrypto(String coingeckoId, String moeda, String parMoeda) {
        try {
            String currency = parMoeda.equals("BRL") ? "brl" : "usd";
            String url = "https://api.coingecko.com/api/v3/simple/price?ids=" + coingeckoId + "&vs_currencies=" + currency;
            String response = webClientBuilder.build().get().uri(url).retrieve().bodyToMono(String.class).block();
            JsonNode root = objectMapper.readTree(response);
            JsonNode prices = root.get(coingeckoId);
            if (prices == null) return;
            BigDecimal price = new BigDecimal(prices.get(currency).asText());
            LocalDateTime now = LocalDateTime.now();
            cotacaoRepository.save(Cotacao.builder()
                    .moeda(moeda)
                    .parMoeda(parMoeda)
                    .precoCompra(price)
                    .precoVenda(price)
                    .dataHora(now)
                    .fonte("COINGECKO")
                    .build());
        } catch (Exception e) {
            log.error("Erro ao buscar cotação única CoinGecko {}", coingeckoId, e);
            throw new BusinessException("Não foi possível atualizar a cotação");
        }
    }

    /**
     * Lista cotações com paginação e filtros.
     * Filtros: moeda, parMoeda, fonte, dataDe, dataAte, precoCompraMin/Max, precoVendaMin/Max,
     * variacaoMin/Max, maximoMin/Max, minimoMin/Max.
     * Ordenação: ordenarPor (dataHora, moeda, parMoeda, precoCompra, precoVenda, variacao, maximo, minimo, fonte), direcao (ASC/DESC).
     */
    public PageResponse<CotacaoResponse> listarPaginado(
            String moeda, String parMoeda, String fonte,
            LocalDate dataDe, LocalDate dataAte,
            BigDecimal precoCompraMin, BigDecimal precoCompraMax,
            BigDecimal precoVendaMin, BigDecimal precoVendaMax,
            BigDecimal variacaoMin, BigDecimal variacaoMax,
            BigDecimal maximoMin, BigDecimal maximoMax,
            BigDecimal minimoMin, BigDecimal minimoMax,
            String ordenarPor, String direcao,
            int page, int size) {
        Specification<Cotacao> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (moeda != null && !moeda.isBlank()) {
                predicates.add(cb.like(cb.upper(root.get("moeda")), "%" + moeda.toUpperCase().trim() + "%"));
            }
            if (parMoeda != null && !parMoeda.isBlank()) {
                predicates.add(cb.like(cb.upper(root.get("parMoeda")), "%" + parMoeda.toUpperCase().trim() + "%"));
            }
            if (fonte != null && !fonte.isBlank()) {
                predicates.add(cb.equal(cb.upper(root.get("fonte")), fonte.toUpperCase().trim()));
            }
            if (dataDe != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("dataHora"), dataDe.atStartOfDay()));
            }
            if (dataAte != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("dataHora"), dataAte.atTime(LocalTime.MAX)));
            }
            if (precoCompraMin != null) predicates.add(cb.greaterThanOrEqualTo(root.get("precoCompra"), precoCompraMin));
            if (precoCompraMax != null) predicates.add(cb.lessThanOrEqualTo(root.get("precoCompra"), precoCompraMax));
            if (precoVendaMin != null) predicates.add(cb.greaterThanOrEqualTo(root.get("precoVenda"), precoVendaMin));
            if (precoVendaMax != null) predicates.add(cb.lessThanOrEqualTo(root.get("precoVenda"), precoVendaMax));
            if (variacaoMin != null) predicates.add(cb.greaterThanOrEqualTo(root.get("variacao"), variacaoMin));
            if (variacaoMax != null) predicates.add(cb.lessThanOrEqualTo(root.get("variacao"), variacaoMax));
            if (maximoMin != null) predicates.add(cb.greaterThanOrEqualTo(root.get("maximo"), maximoMin));
            if (maximoMax != null) predicates.add(cb.lessThanOrEqualTo(root.get("maximo"), maximoMax));
            if (minimoMin != null) predicates.add(cb.greaterThanOrEqualTo(root.get("minimo"), minimoMin));
            if (minimoMax != null) predicates.add(cb.lessThanOrEqualTo(root.get("minimo"), minimoMax));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        String orderBy = ordenarPor != null && !ordenarPor.isBlank() ? ordenarPor.trim() : "dataHora";
        Sort.Direction dir = "ASC".equalsIgnoreCase(direcao) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)), Sort.by(dir, orderBy));
        var pageResult = cotacaoRepository.findAll(spec, pageable);
        return PageResponse.<CotacaoResponse>builder()
                .content(pageResult.getContent().stream().map(this::toResponse).collect(Collectors.toList()))
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .number(pageResult.getNumber())
                .size(pageResult.getSize())
                .first(pageResult.isFirst())
                .last(pageResult.isLast())
                .build();
    }

    public List<CotacaoResponse> listarUltimasCotacoes() {
        return cotacaoRepository.findLatestQuotes().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public CotacaoResponse buscarUltimaCotacao(String moeda, String parMoeda) {
        Cotacao cotacao = cotacaoRepository
                .findTopByMoedaAndParMoedaOrderByDataHoraDesc(moeda.toUpperCase(), parMoeda.toUpperCase())
                .orElse(null);
        return cotacao != null ? toResponse(cotacao) : null;
    }

    public List<CotacaoResponse> historico(String moeda, String parMoeda, int horas) {
        LocalDateTime after = LocalDateTime.now().minusHours(horas);
        return cotacaoRepository
                .findByMoedaAndParMoedaAndDataHoraAfterOrderByDataHoraAsc(
                        moeda.toUpperCase(), parMoeda.toUpperCase(), after)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private CotacaoResponse toResponse(Cotacao c) {
        return CotacaoResponse.builder()
                .id(c.getId())
                .moeda(c.getMoeda())
                .parMoeda(c.getParMoeda())
                .precoCompra(c.getPrecoCompra())
                .precoVenda(c.getPrecoVenda())
                .variacao(c.getVariacao())
                .maximo(c.getMaximo())
                .minimo(c.getMinimo())
                .dataHora(c.getDataHora())
                .fonte(c.getFonte())
                .build();
    }

    private BigDecimal parseBigDecimal(String value) {
        try {
            return value != null ? new BigDecimal(value) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

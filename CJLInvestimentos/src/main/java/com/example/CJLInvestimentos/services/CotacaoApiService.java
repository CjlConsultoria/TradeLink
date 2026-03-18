package com.example.CJLInvestimentos.services;

import com.example.CJLInvestimentos.entities.Cotacao;
import com.example.CJLInvestimentos.entities.CotacaoHistorico;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * Serviço de cotações usando APIs gratuitas:
 *   - AwesomeAPI: forex (USD/BRL, EUR/BRL, etc.) — 100% grátis, 100k requests
 *   - CoinGecko: crypto (BTC, ETH, SOL, etc.) + OHLC histórico — grátis, 10k/mês
 *
 * Substitui as 8 APIs anteriores por uma integração mais limpa e organizada.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CotacaoApiService {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${app.cotacao.coingecko.api-key:}")
    private String coinGeckoApiKey;

    private static final ZoneId SAO_PAULO = ZoneId.of("America/Sao_Paulo");

    // ─── Mapeamentos ─────────────────────────────────────────────────────

    /** Mapa CoinGecko ID → símbolo ticker (ex: "bitcoin" → "BTC") */
    private static final Map<String, String> COINGECKO_TO_SYMBOL = Map.ofEntries(
            Map.entry("bitcoin", "BTC"),
            Map.entry("ethereum", "ETH"),
            Map.entry("binancecoin", "BNB"),
            Map.entry("ripple", "XRP"),
            Map.entry("solana", "SOL"),
            Map.entry("cardano", "ADA"),
            Map.entry("dogecoin", "DOGE"),
            Map.entry("polkadot", "DOT"),
            Map.entry("avalanche-2", "AVAX"),
            Map.entry("matic-network", "MATIC"),
            Map.entry("chainlink", "LINK"),
            Map.entry("uniswap", "UNI"),
            Map.entry("litecoin", "LTC")
    );

    /** Mapa inverso: símbolo → CoinGecko ID */
    private static final Map<String, String> SYMBOL_TO_COINGECKO = new HashMap<>();
    static {
        COINGECKO_TO_SYMBOL.forEach((id, sym) -> SYMBOL_TO_COINGECKO.put(sym, id));
    }

    // ════════════════════════════════════════════════════════════════════════
    //  AWESOME API — Forex
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Busca cotações forex da AwesomeAPI.
     * URL: https://economia.awesomeapi.com.br/json/last/USD-BRL,EUR-BRL,...
     *
     * @param pares Lista de pares no formato "USD-BRL", "EUR-BRL", etc.
     * @return Lista de Cotacao entities
     */
    public List<Cotacao> fetchForexAwesomeApi(List<String> pares) {
        if (pares == null || pares.isEmpty()) return Collections.emptyList();

        String paresParam = String.join(",", pares);
        String url = "https://economia.awesomeapi.com.br/json/last/" + paresParam;

        try {
            String response = webClientBuilder.build()
                    .get().uri(url).retrieve()
                    .bodyToMono(String.class).block();

            JsonNode root = objectMapper.readTree(response);
            List<Cotacao> result = new ArrayList<>();

            Iterator<Map.Entry<String, JsonNode>> fields = root.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                JsonNode node = entry.getValue();

                String moeda = node.get("code").asText();
                String parMoeda = node.get("codein").asText();

                LocalDateTime dataHora;
                try {
                    long timestamp = node.get("timestamp").asLong();
                    dataHora = LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), SAO_PAULO);
                } catch (Exception e) {
                    dataHora = LocalDateTime.now();
                }

                result.add(Cotacao.builder()
                        .moeda(moeda)
                        .parMoeda(parMoeda)
                        .precoCompra(parseBd(node.get("bid").asText()))
                        .precoVenda(parseBd(node.get("ask").asText()))
                        .variacao(parseBd(node.has("pctChange") ? node.get("pctChange").asText() : null))
                        .maximo(parseBd(node.has("high") ? node.get("high").asText() : null))
                        .minimo(parseBd(node.has("low") ? node.get("low").asText() : null))
                        .dataHora(dataHora)
                        .fonte("AWESOME_API")
                        .build());
            }

            log.info("AwesomeAPI: {} cotações forex obtidas", result.size());
            return result;

        } catch (WebClientResponseException e) {
            if (e.getStatusCode() != null && e.getStatusCode().value() == 429) {
                log.warn("AwesomeAPI retornou 429 (rate limit). Aguardando próximo ciclo.");
            } else {
                log.error("Erro HTTP {} da AwesomeAPI: {}", e.getStatusCode(), e.getMessage());
            }
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Erro ao buscar forex da AwesomeAPI: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Busca cotação forex individual sob demanda.
     */
    public Cotacao fetchSingleForex(String moeda, String parMoeda) {
        List<Cotacao> result = fetchForexAwesomeApi(List.of(moeda + "-" + parMoeda));
        return result.isEmpty() ? null : result.get(0);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  COINGECKO — Crypto (preços + OHLC histórico)
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Busca preços crypto em tempo real via CoinGecko /simple/price.
     *
     * @param coinIds  IDs CoinGecko (ex: ["bitcoin","ethereum","solana"])
     * @param currencies Moedas alvo (ex: ["usd","brl"])
     * @return Lista de Cotacao entities
     */
    public List<Cotacao> fetchCryptoCoingecko(List<String> coinIds, List<String> currencies) {
        if (coinIds == null || coinIds.isEmpty()) return Collections.emptyList();

        String ids = String.join(",", coinIds);
        String vs = String.join(",", currencies);
        String url = "https://api.coingecko.com/api/v3/simple/price?ids=" + ids
                + "&vs_currencies=" + vs
                + "&include_24hr_change=true&include_24hr_vol=true&include_high_24h=true&include_low_24h=true";

        try {
            WebClient.RequestHeadersSpec<?> request = webClientBuilder.build()
                    .get().uri(url);
            // Adicionar API key se disponível
            if (coinGeckoApiKey != null && !coinGeckoApiKey.isBlank()) {
                request = request.header("x-cg-demo-api-key", coinGeckoApiKey);
            }

            String response = ((WebClient.RequestHeadersSpec<?>) request)
                    .retrieve().bodyToMono(String.class).block();

            JsonNode root = objectMapper.readTree(response);
            List<Cotacao> result = new ArrayList<>();
            LocalDateTime now = LocalDateTime.now();

            for (String coinId : coinIds) {
                JsonNode coin = root.get(coinId);
                if (coin == null) continue;

                String symbol = COINGECKO_TO_SYMBOL.getOrDefault(coinId, coinId.toUpperCase());

                for (String currency : currencies) {
                    String cur = currency.toLowerCase();
                    String curUpper = currency.toUpperCase();

                    JsonNode priceNode = coin.get(cur);
                    if (priceNode == null) continue;

                    BigDecimal price = parseBd(priceNode.asText());
                    if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) continue;

                    BigDecimal variacao = parseBd(coin.has(cur + "_24h_change") ? coin.get(cur + "_24h_change").asText() : null);
                    BigDecimal high = parseBd(coin.has(cur + "_24h_high") ? coin.get(cur + "_24h_high").asText() : null);
                    BigDecimal low = parseBd(coin.has(cur + "_24h_low") ? coin.get(cur + "_24h_low").asText() : null);

                    result.add(Cotacao.builder()
                            .moeda(symbol)
                            .parMoeda(curUpper)
                            .precoCompra(price)
                            .precoVenda(price)
                            .variacao(variacao)
                            .maximo(high)
                            .minimo(low)
                            .dataHora(now)
                            .fonte("COINGECKO")
                            .build());
                }
            }

            log.info("CoinGecko: {} cotações crypto obtidas", result.size());
            return result;

        } catch (WebClientResponseException e) {
            if (e.getStatusCode() != null && e.getStatusCode().value() == 429) {
                log.warn("CoinGecko retornou 429 (rate limit). Aguardando próximo ciclo.");
            } else {
                log.error("Erro HTTP {} do CoinGecko: {}", e.getStatusCode(), e.getMessage());
            }
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Erro ao buscar crypto do CoinGecko: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Busca cotação crypto individual sob demanda.
     */
    public Cotacao fetchSingleCrypto(String moeda, String parMoeda) {
        String coinId = SYMBOL_TO_COINGECKO.get(moeda.toUpperCase());
        if (coinId == null) return null;

        List<Cotacao> result = fetchCryptoCoingecko(List.of(coinId), List.of(parMoeda.toLowerCase()));
        return result.isEmpty() ? null : result.get(0);
    }

    // ─── OHLC Histórico (CoinGecko) ──────────────────────────────────────

    /**
     * Busca dados OHLC históricos do CoinGecko.
     * Endpoint: /coins/{id}/ohlc?vs_currency=usd&days=30
     *
     * Granularidade automática:
     *   - 1-2 dias: 30 minutos
     *   - 3-30 dias: 4 horas
     *   - 31+ dias: 4 dias
     *
     * @param coinId     ID CoinGecko (ex: "bitcoin")
     * @param vsCurrency Moeda alvo (ex: "usd", "brl")
     * @param days       Período: 1, 7, 14, 30, 90, 180, 365, "max"
     * @return Lista de CotacaoHistorico (sem volume — CoinGecko OHLC não retorna volume)
     */
    public List<CotacaoHistorico> fetchOhlcCoingecko(String coinId, String vsCurrency, String days) {
        String url = "https://api.coingecko.com/api/v3/coins/" + coinId
                + "/ohlc?vs_currency=" + vsCurrency + "&days=" + days;

        try {
            WebClient.RequestHeadersSpec<?> request = webClientBuilder.build()
                    .get().uri(url);
            if (coinGeckoApiKey != null && !coinGeckoApiKey.isBlank()) {
                request = request.header("x-cg-demo-api-key", coinGeckoApiKey);
            }

            String response = ((WebClient.RequestHeadersSpec<?>) request)
                    .retrieve().bodyToMono(String.class).block();

            JsonNode root = objectMapper.readTree(response);
            if (!root.isArray()) {
                log.warn("CoinGecko OHLC: resposta inesperada para {} ({})", coinId, days);
                return Collections.emptyList();
            }

            String symbol = COINGECKO_TO_SYMBOL.getOrDefault(coinId, coinId.toUpperCase());
            String parMoeda = vsCurrency.toUpperCase();

            // Determinar intervalo baseado nos dias
            String intervalo = determinarIntervaloOhlc(days);

            List<CotacaoHistorico> result = new ArrayList<>();
            for (JsonNode candle : root) {
                if (!candle.isArray() || candle.size() < 5) continue;

                long timestamp = candle.get(0).asLong();
                BigDecimal open = parseBd(candle.get(1).asText());
                BigDecimal high = parseBd(candle.get(2).asText());
                BigDecimal low = parseBd(candle.get(3).asText());
                BigDecimal close = parseBd(candle.get(4).asText());

                if (open == null || close == null) continue;

                LocalDateTime dateTime = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(timestamp), SAO_PAULO);

                result.add(CotacaoHistorico.builder()
                        .moeda(symbol)
                        .parMoeda(parMoeda)
                        .open(open)
                        .high(high != null ? high : open)
                        .low(low != null ? low : open)
                        .close(close)
                        .volume(null) // CoinGecko OHLC não retorna volume
                        .intervalo(intervalo)
                        .dataHora(dateTime)
                        .fonte("COINGECKO")
                        .build());
            }

            log.info("CoinGecko OHLC: {} candles para {} ({} dias, intervalo={})",
                    result.size(), coinId, days, intervalo);
            return result;

        } catch (WebClientResponseException e) {
            if (e.getStatusCode() != null && e.getStatusCode().value() == 429) {
                log.warn("CoinGecko OHLC retornou 429 (rate limit). Aguardando próximo ciclo.");
            } else {
                log.error("Erro HTTP {} do CoinGecko OHLC: {}", e.getStatusCode(), e.getMessage());
            }
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Erro ao buscar OHLC do CoinGecko para {}: {}", coinId, e.getMessage());
            return Collections.emptyList();
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  AWESOME API — Histórico Forex (OHLC diário)
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Busca histórico diário de forex via AwesomeAPI.
     * Endpoint: https://economia.awesomeapi.com.br/json/daily/USD-BRL/30
     *
     * Máximo: 360 dias.
     * Resposta: array de objetos com bid, ask, high, low, pctChange, timestamp.
     *
     * Mapeia para CotacaoHistorico:
     *   open = bid (preço de compra na abertura do dia)
     *   close = ask (preço de venda no fechamento)
     *   high/low = high/low do dia
     *   volume = null (AwesomeAPI não fornece volume forex)
     *
     * @param par  Par no formato "USD-BRL"
     * @param dias Número de dias (máx 360)
     * @return Lista de CotacaoHistorico com intervalo "1day"
     */
    public List<CotacaoHistorico> fetchForexHistorico(String par, int dias) {
        if (dias <= 0) dias = 30;
        if (dias > 360) dias = 360;

        String url = "https://economia.awesomeapi.com.br/json/daily/" + par + "/" + dias;

        try {
            String response = webClientBuilder.build()
                    .get().uri(url).retrieve()
                    .bodyToMono(String.class).block();

            JsonNode root = objectMapper.readTree(response);
            if (!root.isArray()) {
                log.warn("AwesomeAPI daily: resposta inesperada para {} ({} dias)", par, dias);
                return Collections.emptyList();
            }

            String[] partes = par.split("-");
            String moeda = partes[0];
            String parMoeda = partes.length > 1 ? partes[1] : "BRL";

            List<CotacaoHistorico> result = new ArrayList<>();
            for (JsonNode node : root) {
                long timestamp = node.get("timestamp").asLong();
                LocalDateTime dateTime = LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(timestamp), SAO_PAULO);

                BigDecimal bid = parseBd(node.get("bid").asText());
                BigDecimal ask = parseBd(node.get("ask").asText());
                BigDecimal high = parseBd(node.has("high") ? node.get("high").asText() : null);
                BigDecimal low = parseBd(node.has("low") ? node.get("low").asText() : null);

                if (bid == null && ask == null) continue;
                BigDecimal open = bid != null ? bid : ask;
                BigDecimal close = ask != null ? ask : bid;

                result.add(CotacaoHistorico.builder()
                        .moeda(moeda)
                        .parMoeda(parMoeda)
                        .open(open)
                        .high(high != null ? high : open)
                        .low(low != null ? low : open)
                        .close(close)
                        .volume(null)
                        .intervalo("1day")
                        .dataHora(dateTime)
                        .fonte("AWESOME_API")
                        .build());
            }

            // AwesomeAPI retorna do mais recente ao mais antigo — inverter
            Collections.reverse(result);

            log.info("AwesomeAPI daily: {} candles para {} ({} dias)", result.size(), par, dias);
            return result;

        } catch (WebClientResponseException e) {
            if (e.getStatusCode() != null && e.getStatusCode().value() == 429) {
                log.warn("AwesomeAPI daily retornou 429 (rate limit).");
            } else {
                log.error("Erro HTTP {} da AwesomeAPI daily: {}", e.getStatusCode(), e.getMessage());
            }
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Erro ao buscar histórico forex da AwesomeAPI para {}: {}", par, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Busca histórico diário de forex por moeda/parMoeda.
     */
    public List<CotacaoHistorico> fetchForexHistoricoBySymbol(String moeda, String parMoeda, int dias) {
        return fetchForexHistorico(moeda.toUpperCase() + "-" + parMoeda.toUpperCase(), dias);
    }

    // ─── OHLC Histórico (CoinGecko) ──────────────────────────────────────

    /**
     * Busca OHLC por símbolo (ex: "BTC") em vez de coinId.
     */
    public List<CotacaoHistorico> fetchOhlcBySymbol(String symbol, String parMoeda, String days) {
        String coinId = SYMBOL_TO_COINGECKO.get(symbol.toUpperCase());
        if (coinId == null) {
            log.warn("Símbolo {} não encontrado no mapeamento CoinGecko", symbol);
            return Collections.emptyList();
        }
        return fetchOhlcCoingecko(coinId, parMoeda.toLowerCase(), days);
    }

    /**
     * Verifica se um símbolo é crypto (presente no mapeamento CoinGecko).
     */
    public boolean isCrypto(String symbol) {
        return SYMBOL_TO_COINGECKO.containsKey(symbol.toUpperCase());
    }

    // ─── Helpers ─────────────────────────────────────────────────────────

    private String determinarIntervaloOhlc(String days) {
        try {
            int d = "max".equalsIgnoreCase(days) ? 9999 : Integer.parseInt(days);
            if (d <= 2) return "30min";
            if (d <= 30) return "4h";
            return "4day";
        } catch (NumberFormatException e) {
            return "4day";
        }
    }

    private BigDecimal parseBd(String value) {
        if (value == null || value.isBlank() || "null".equals(value)) return null;
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

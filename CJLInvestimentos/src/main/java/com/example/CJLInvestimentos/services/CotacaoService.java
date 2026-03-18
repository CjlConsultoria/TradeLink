package com.example.CJLInvestimentos.services;

import com.example.CJLInvestimentos.dtos.response.CotacaoHistoricoResponse;
import com.example.CJLInvestimentos.dtos.response.CotacaoResponse;
import com.example.CJLInvestimentos.dtos.response.PageResponse;
import com.example.CJLInvestimentos.entities.Cotacao;
import com.example.CJLInvestimentos.entities.CotacaoHistorico;
import com.example.CJLInvestimentos.exceptions.BusinessException;
import com.example.CJLInvestimentos.repositories.CotacaoHistoricoRepository;
import com.example.CJLInvestimentos.repositories.CotacaoRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Serviço principal de cotações.
 * Fontes gratuitas:
 *   - AwesomeAPI: forex (USD/BRL, EUR/BRL, etc.)
 *   - CoinGecko: crypto (BTC, ETH, SOL, etc.) + OHLC histórico
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CotacaoService {

    private final CotacaoRepository cotacaoRepository;
    private final CotacaoHistoricoRepository cotacaoHistoricoRepository;
    private final CotacaoApiService apiService; // AwesomeAPI + CoinGecko

    // ─── Pares monitorados ───────────────────────────────────────────────

    /** Pares forex (formato AwesomeAPI: MOEDA-PAR) */
    private static final List<String> FOREX_PAIRS = List.of(
            "USD-BRL", "EUR-BRL", "GBP-BRL", "JPY-BRL", "CHF-BRL",
            "CAD-BRL", "AUD-BRL", "NZD-BRL", "CNY-BRL", "ARS-BRL",
            "MXN-BRL", "BTC-BRL", "ETH-BRL"
    );

    /** IDs CoinGecko para crypto */
    private static final List<String> CRYPTO_IDS = List.of(
            "bitcoin", "ethereum", "binancecoin", "ripple", "solana",
            "cardano", "dogecoin", "polkadot", "avalanche-2", "matic-network",
            "chainlink", "uniswap", "litecoin"
    );

    /** Moedas alvo para crypto (CoinGecko) */
    private static final List<String> CRYPTO_CURRENCIES = List.of("usd", "brl");

    /** Cryptos principais para histórico OHLC */
    private static final List<String> OHLC_CRYPTO_IDS = List.of(
            "bitcoin", "ethereum", "binancecoin", "ripple", "solana",
            "cardano", "dogecoin", "litecoin"
    );

    /** Pares forex principais para histórico diário (AwesomeAPI daily) */
    private static final List<String> OHLC_FOREX_PAIRS = List.of(
            "USD-BRL", "EUR-BRL", "GBP-BRL", "JPY-BRL", "CHF-BRL",
            "CAD-BRL", "AUD-BRL", "EUR-USD", "GBP-USD"
    );

    // ─── Fetch das fontes ────────────────────────────────────────────────

    /**
     * Busca cotações forex via AwesomeAPI.
     * Chamado pelo scheduler a cada 10 minutos.
     */
    @Transactional
    public void fetchAndSaveForex() {
        List<Cotacao> cotacoes = apiService.fetchForexAwesomeApi(FOREX_PAIRS);
        if (!cotacoes.isEmpty()) {
            cotacaoRepository.deleteByFonte("AWESOME_API");
            cotacaoRepository.saveAll(cotacoes);
            log.info("AwesomeAPI: {} cotações forex salvas", cotacoes.size());
        }
    }

    /**
     * Busca cotações crypto via CoinGecko.
     * Chamado pelo scheduler a cada 10 minutos (com delay para não bater rate limit).
     */
    @Transactional
    public void fetchAndSaveCrypto() {
        List<Cotacao> cotacoes = apiService.fetchCryptoCoingecko(CRYPTO_IDS, CRYPTO_CURRENCIES);
        if (!cotacoes.isEmpty()) {
            cotacaoRepository.deleteByFonte("COINGECKO");
            cotacaoRepository.saveAll(cotacoes);
            log.info("CoinGecko: {} cotações crypto salvas", cotacoes.size());
        }
    }

    /**
     * Busca e salva dados OHLC históricos para um crypto específico.
     */
    @Transactional
    public void fetchAndSaveOhlc(String coinId, String vsCurrency, String days) {
        List<CotacaoHistorico> dados = apiService.fetchOhlcCoingecko(coinId, vsCurrency, days);
        if (dados.isEmpty()) return;

        String moeda = dados.get(0).getMoeda();
        String parMoeda = dados.get(0).getParMoeda();
        String intervalo = dados.get(0).getIntervalo();

        cotacaoHistoricoRepository.deleteByMoedaAndParMoedaAndIntervalo(moeda, parMoeda, intervalo);
        cotacaoHistoricoRepository.saveAll(dados);
        log.info("OHLC salvo: {} candles para {}/{} ({})", dados.size(), moeda, parMoeda, intervalo);
    }

    /**
     * Atualiza o histórico OHLC de cryptos e forex.
     * Chamado pelo scheduler a cada 1 hora.
     *
     * Crypto: 90 dias via CoinGecko (granularidade 4 dias) em USD e BRL.
     * Forex: 90 dias via AwesomeAPI daily (granularidade 1 dia).
     */
    public void atualizarHistoricoPrincipal() {
        // 1. Crypto via CoinGecko
        for (String coinId : OHLC_CRYPTO_IDS) {
            try {
                fetchAndSaveOhlc(coinId, "usd", "90");
                Thread.sleep(2500); // Respeitar rate limit CoinGecko (30/min)
                fetchAndSaveOhlc(coinId, "brl", "90");
                Thread.sleep(2500);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                log.warn("Falha ao atualizar OHLC crypto de {}: {}", coinId, e.getMessage());
            }
        }

        // 2. Forex via AwesomeAPI daily
        for (String par : OHLC_FOREX_PAIRS) {
            try {
                fetchAndSaveForexOhlc(par, 90);
                Thread.sleep(1000); // AwesomeAPI tem rate limit generoso, 1s basta
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                log.warn("Falha ao atualizar OHLC forex de {}: {}", par, e.getMessage());
            }
        }
    }

    /**
     * Busca e salva dados OHLC históricos de forex via AwesomeAPI daily.
     */
    @Transactional
    public void fetchAndSaveForexOhlc(String par, int dias) {
        List<CotacaoHistorico> dados = apiService.fetchForexHistorico(par, dias);
        if (dados.isEmpty()) return;

        String moeda = dados.get(0).getMoeda();
        String parMoeda = dados.get(0).getParMoeda();
        String intervalo = dados.get(0).getIntervalo();

        cotacaoHistoricoRepository.deleteByMoedaAndParMoedaAndIntervalo(moeda, parMoeda, intervalo);
        cotacaoHistoricoRepository.saveAll(dados);
        log.info("OHLC forex salvo: {} candles para {}/{} ({})", dados.size(), moeda, parMoeda, intervalo);
    }

    // ─── Busca sob demanda ───────────────────────────────────────────────

    /**
     * Busca e salva cotação individual sob demanda.
     */
    public CotacaoResponse fetchSingleQuote(String moeda, String parMoeda) {
        String moedaUpper = moeda.toUpperCase();
        String parUpper = parMoeda.toUpperCase();

        Cotacao c;
        if (apiService.isCrypto(moedaUpper)) {
            c = apiService.fetchSingleCrypto(moedaUpper, parUpper);
        } else {
            c = apiService.fetchSingleForex(moedaUpper, parUpper);
        }

        if (c != null) {
            cotacaoRepository.save(c);
        } else {
            throw new BusinessException("Não foi possível atualizar a cotação " + moedaUpper + "/" + parUpper);
        }
        return buscarUltimaCotacao(moedaUpper, parUpper);
    }

    // ─── Force refresh ───────────────────────────────────────────────────

    public void forceRefresh() {
        fetchAndSaveForex();
        fetchAndSaveCrypto();
    }

    @Transactional
    public void limparTodas() {
        cotacaoRepository.deleteAll();
        log.info("Todas as cotações foram removidas.");
    }

    @Transactional
    public void zerarERecarregar() {
        limparTodas();
        forceRefresh();
    }

    // ─── Queries de cotação ──────────────────────────────────────────────

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

    // ─── Queries de histórico OHLCV ──────────────────────────────────────

    /**
     * Retorna dados históricos OHLC do banco de dados, parametrizado por dias.
     * Se não existem, busca sob demanda (CoinGecko para crypto, AwesomeAPI para forex).
     *
     * @param dias 0 = máximo disponível; 7/30/90/180/365 = período em dias
     */
    public List<CotacaoHistoricoResponse> historicoOHLCV(String moeda, String parMoeda,
                                                          int dias,
                                                          LocalDateTime de, LocalDateTime ate) {
        String moedaUpper = moeda.toUpperCase();
        String parUpper = parMoeda.toUpperCase();
        boolean isCrypto = apiService.isCrypto(moedaUpper);

        // Calcular cutoff de data se dias > 0
        LocalDateTime cutoff = (dias > 0) ? LocalDateTime.now().minusDays(dias) : null;

        // Buscar do banco SEM filtrar por intervalo — retorna qualquer granularidade disponível.
        // Isso resolve o problema de mismatch: scheduler salva "4day" (90d fetch),
        // mas query para 30 dias buscava "4h" → encontrava vazio.
        List<CotacaoHistorico> dados;
        if (de != null && ate != null) {
            dados = cotacaoHistoricoRepository
                    .findByMoedaAndParMoedaAndDataHoraBetweenOrderByDataHoraAsc(
                            moedaUpper, parUpper, de, ate);
        } else if (cutoff != null) {
            dados = cotacaoHistoricoRepository
                    .findByMoedaAndParMoedaAndDataHoraBetweenOrderByDataHoraAsc(
                            moedaUpper, parUpper, cutoff, LocalDateTime.now());
        } else {
            dados = cotacaoHistoricoRepository
                    .findByMoedaAndParMoedaOrderByDataHoraAsc(
                            moedaUpper, parUpper);
        }

        // Verificar se os dados cobrem o período solicitado.
        // Se vazios ou se cobertura < 70%, buscar sob demanda.
        boolean precisaBuscar = dados.isEmpty();
        if (!precisaBuscar && cutoff != null) {
            LocalDateTime dadoMaisAntigo = dados.get(0).getDataHora();
            long diasCobertos = java.time.Duration.between(dadoMaisAntigo, LocalDateTime.now()).toDays();
            precisaBuscar = diasCobertos < (dias * 0.7);
        }

        if (precisaBuscar) {
            try {
                if (isCrypto) {
                    // Crypto: CoinGecko OHLC — buscar os dias solicitados
                    String daysParam = (dias <= 0) ? "max" : String.valueOf(Math.min(dias, 365));
                    apiService.fetchOhlcBySymbol(moedaUpper, parUpper, daysParam)
                            .forEach(h -> {
                                try {
                                    cotacaoHistoricoRepository.save(h);
                                } catch (Exception ignored) {} // ignora duplicatas
                            });
                } else {
                    // Forex: AwesomeAPI daily (max 360 dias)
                    int diasForex = (dias <= 0) ? 360 : Math.min(dias, 360);
                    List<CotacaoHistorico> fetched = apiService.fetchForexHistoricoBySymbol(moedaUpper, parUpper, diasForex);
                    if (!fetched.isEmpty()) {
                        cotacaoHistoricoRepository.deleteByMoedaAndParMoedaAndIntervalo(moedaUpper, parUpper, "1day");
                        cotacaoHistoricoRepository.saveAll(fetched);
                    }
                }

                // Re-query após fetch — sem filtro de intervalo
                if (cutoff != null) {
                    dados = cotacaoHistoricoRepository
                            .findByMoedaAndParMoedaAndDataHoraBetweenOrderByDataHoraAsc(
                                    moedaUpper, parUpper, cutoff, LocalDateTime.now());
                } else {
                    dados = cotacaoHistoricoRepository
                            .findByMoedaAndParMoedaOrderByDataHoraAsc(
                                    moedaUpper, parUpper);
                }
            } catch (Exception e) {
                log.warn("Falha ao buscar OHLC sob demanda para {}/{}: {}", moedaUpper, parUpper, e.getMessage());
            }
        }

        return dados.stream().map(this::toHistoricoResponse).collect(Collectors.toList());
    }

    /**
     * Retorna a lista de todos os pares monitorados.
     */
    public List<Map<String, String>> paresDisponiveis() {
        List<Map<String, String>> pares = new ArrayList<>();
        for (String s : FOREX_PAIRS) {
            String[] p = s.split("-");
            pares.add(Map.of("moeda", p[0], "parMoeda", p[1], "tipo", "forex", "symbol", s, "fonte", "AwesomeAPI"));
        }
        // Adicionar crypto
        Map<String, String> symbolMap = Map.ofEntries(
            Map.entry("bitcoin", "BTC"), Map.entry("ethereum", "ETH"), Map.entry("binancecoin", "BNB"),
            Map.entry("ripple", "XRP"), Map.entry("solana", "SOL"), Map.entry("cardano", "ADA"),
            Map.entry("dogecoin", "DOGE"), Map.entry("polkadot", "DOT"), Map.entry("avalanche-2", "AVAX"),
            Map.entry("matic-network", "MATIC"), Map.entry("chainlink", "LINK"),
            Map.entry("uniswap", "UNI"), Map.entry("litecoin", "LTC")
        );
        for (String coinId : CRYPTO_IDS) {
            String symbol = symbolMap.getOrDefault(coinId, coinId.toUpperCase());
            for (String cur : CRYPTO_CURRENCIES) {
                pares.add(Map.of("moeda", symbol, "parMoeda", cur.toUpperCase(), "tipo", "crypto",
                        "symbol", symbol + "/" + cur.toUpperCase(), "fonte", "CoinGecko"));
            }
        }
        return pares;
    }

    // ─── Paginação com filtros ───────────────────────────────────────────

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

    // ─── Mappers ─────────────────────────────────────────────────────────

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

    private CotacaoHistoricoResponse toHistoricoResponse(CotacaoHistorico h) {
        return CotacaoHistoricoResponse.builder()
                .id(h.getId())
                .moeda(h.getMoeda())
                .parMoeda(h.getParMoeda())
                .open(h.getOpen())
                .high(h.getHigh())
                .low(h.getLow())
                .close(h.getClose())
                .volume(h.getVolume())
                .intervalo(h.getIntervalo())
                .dataHora(h.getDataHora())
                .build();
    }

    // ─── Helpers ─────────────────────────────────────────────────────────

    /**
     * Determina qual intervalo o CoinGecko usa baseado no número de dias.
     * CoinGecko granularidade: 1-2d→30min, 3-30d→4h, 31+d→4day
     */
    private String determinarIntervaloParaDias(String days) {
        try {
            int d = "max".equalsIgnoreCase(days) ? 9999 : Integer.parseInt(days);
            if (d <= 2) return "30min";
            if (d <= 30) return "4h";
            return "4day";
        } catch (NumberFormatException e) {
            return "4h";
        }
    }
}

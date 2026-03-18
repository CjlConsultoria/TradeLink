package com.example.CJLInvestimentos.services;

import com.example.CJLInvestimentos.entities.*;
import com.example.CJLInvestimentos.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SnapshotService {

    private final SnapshotPortfolioRepository snapshotRepository;
    private final CarteiraClienteRepository carteiraClienteRepository;
    private final AtivoClienteRepository ativoClienteRepository;
    private final CotacaoRepository cotacaoRepository;
    private final CarteiraRepository carteiraRepository;

    @Transactional
    public void criarSnapshotCarteira(Long carteiraId) {
        Carteira carteira = carteiraRepository.findById(carteiraId)
                .orElseThrow(() -> new RuntimeException("Carteira não encontrada"));

        List<CarteiraCliente> vinculos = carteiraClienteRepository.findByCarteiraId(carteiraId);
        LocalDate hoje = LocalDate.now();

        for (CarteiraCliente vinculo : vinculos) {
            Long clienteId = vinculo.getCliente().getId();
            if (snapshotRepository.existsByClienteIdAndCarteiraIdAndDataSnapshot(clienteId, carteiraId, hoje)) {
                continue;
            }

            List<AtivoCliente> ativos = ativoClienteRepository.findByClienteIdOrderBySimboloAsc(clienteId);
            BigDecimal valorTotal = calcularValorTotal(ativos);
            BigDecimal valorBtcHold = calcularValorBtcHold(ativos);

            SnapshotPortfolio snapshot = SnapshotPortfolio.builder()
                    .cliente(vinculo.getCliente())
                    .carteira(carteira)
                    .dataSnapshot(hoje)
                    .valorTotalPortfolio(valorTotal)
                    .valorBtcHold(valorBtcHold)
                    .build();

            snapshotRepository.save(snapshot);
        }
    }

    @Transactional
    public void criarSnapshotsGeral() {
        List<Carteira> carteiras = carteiraRepository.findAll();
        for (Carteira c : carteiras) {
            if (Boolean.TRUE.equals(c.getAtiva())) {
                try {
                    criarSnapshotCarteira(c.getId());
                } catch (Exception e) {
                    log.error("Erro ao criar snapshot para carteira {}: {}", c.getId(), e.getMessage());
                }
            }
        }
    }

    public List<Map<String, Object>> dadosPerformance(Long carteiraId, Long clienteId) {
        List<SnapshotPortfolio> snapshots;
        if (clienteId != null) {
            snapshots = snapshotRepository.findByClienteIdAndCarteiraIdOrderByDataSnapshotAsc(clienteId, carteiraId);
        } else {
            snapshots = snapshotRepository.findByCarteiraIdOrderByDataSnapshotAsc(carteiraId);
        }

        if (clienteId != null) {
            return snapshots.stream().map(s -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("data", s.getDataSnapshot().toString());
                m.put("valorPortfolio", s.getValorTotalPortfolio());
                m.put("valorBtcHold", s.getValorBtcHold());
                return m;
            }).collect(Collectors.toList());
        }

        // Agrupar por data e somar
        Map<LocalDate, BigDecimal[]> agrupado = new TreeMap<>();
        for (SnapshotPortfolio s : snapshots) {
            agrupado.computeIfAbsent(s.getDataSnapshot(), k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            BigDecimal[] vals = agrupado.get(s.getDataSnapshot());
            if (s.getValorTotalPortfolio() != null) vals[0] = vals[0].add(s.getValorTotalPortfolio());
            if (s.getValorBtcHold() != null) vals[1] = vals[1].add(s.getValorBtcHold());
        }

        return agrupado.entrySet().stream().map(e -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("data", e.getKey().toString());
            m.put("valorPortfolio", e.getValue()[0]);
            m.put("valorBtcHold", e.getValue()[1]);
            return m;
        }).collect(Collectors.toList());
    }

    private BigDecimal calcularValorTotal(List<AtivoCliente> ativos) {
        BigDecimal total = BigDecimal.ZERO;
        for (AtivoCliente a : ativos) {
            BigDecimal preco = resolverPreco(a.getSimbolo(), a.getParMoedaReferencia(), a.getPrecoManual());
            if (preco != null && a.getQuantidade() != null) {
                total = total.add(a.getQuantidade().multiply(preco));
            }
        }
        return total;
    }

    private BigDecimal calcularValorBtcHold(List<AtivoCliente> ativos) {
        BigDecimal totalUsd = calcularValorTotal(ativos);
        BigDecimal btcPrice = resolverPreco("BTC", "USD", null);
        if (btcPrice == null || btcPrice.compareTo(BigDecimal.ZERO) == 0) return totalUsd;
        return totalUsd;
    }

    private BigDecimal resolverPreco(String simbolo, String par, BigDecimal precoManual) {
        return cotacaoRepository.findTopByMoedaAndParMoedaOrderByDataHoraDesc(simbolo, par)
                .map(Cotacao::getPrecoCompra)
                .orElse(precoManual);
    }
}

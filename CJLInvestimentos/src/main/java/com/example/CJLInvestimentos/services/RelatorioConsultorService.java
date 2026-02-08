package com.example.CJLInvestimentos.services;

import com.example.CJLInvestimentos.dtos.response.RelatorioConsultorResponse;
import com.example.CJLInvestimentos.dtos.response.ResumoRelatorioResponse;
import com.example.CJLInvestimentos.entities.OperacaoCliente;
import com.example.CJLInvestimentos.entities.enums.TipoOperacao;
import com.example.CJLInvestimentos.repositories.CarteiraRepository;
import com.example.CJLInvestimentos.repositories.OperacaoClienteRepository;
import com.example.CJLInvestimentos.repositories.RecomendacaoResolvidaClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RelatorioConsultorService {

    private final OperacaoClienteRepository operacaoClienteRepository;
    private final RecomendacaoResolvidaClienteRepository resolvidaClienteRepository;
    private final CarteiraRepository carteiraRepository;

    public List<RelatorioConsultorResponse> listarOperacoes(Long consultorId, Long carteiraId, Long clienteId, LocalDate dataDe, LocalDate dataAte) {
        List<Long> carteiraIds = carteiraRepository.findByConsultorId(consultorId).stream()
                .map(c -> c.getId()).collect(Collectors.toList());
        if (carteiraIds.isEmpty()) {
            return Collections.emptyList();
        }
        if (carteiraId != null && carteiraIds.contains(carteiraId)) {
            carteiraIds = List.of(carteiraId);
        } else if (carteiraId != null) {
            return Collections.emptyList();
        }
        LocalDateTime de = dataDe != null ? dataDe.atStartOfDay() : LocalDateTime.of(1970, 1, 1, 0, 0);
        LocalDateTime ate = dataAte != null ? dataAte.atTime(23, 59, 59) : LocalDateTime.of(9999, 12, 31, 23, 59, 59);
        List<OperacaoCliente> list = operacaoClienteRepository.findByCarteiraIdsAndFiltros(carteiraIds, clienteId, de, ate);
        return list.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public ResumoRelatorioResponse resumoCompleto(Long consultorId, LocalDate dataDe, LocalDate dataAte) {
        List<Long> carteiraIds = carteiraRepository.findByConsultorId(consultorId).stream()
                .map(c -> c.getId()).collect(Collectors.toList());
        LocalDateTime de = dataDe != null ? dataDe.atStartOfDay() : LocalDateTime.of(1970, 1, 1, 0, 0);
        LocalDateTime ate = dataAte != null ? dataAte.atTime(23, 59, 59) : LocalDateTime.of(9999, 12, 31, 23, 59, 59);
        List<OperacaoCliente> todas = carteiraIds.isEmpty() ? Collections.emptyList()
                : operacaoClienteRepository.findByCarteiraIdsAndFiltros(carteiraIds, null, de, ate);
        List<OperacaoCliente> filtradas = todas;

        long totalCompras = filtradas.stream().filter(op -> op.getTipo() == TipoOperacao.COMPRA).count();
        long totalVendas = filtradas.stream().filter(op -> op.getTipo() == TipoOperacao.VENDA).count();

        Map<Long, String> carteiraNomes = new HashMap<>();
        Map<Long, String> clienteNomes = new HashMap<>();
        filtradas.forEach(op -> {
            carteiraNomes.put(op.getRecomendacao().getCarteira().getId(), op.getRecomendacao().getCarteira().getNome());
            clienteNomes.put(op.getCliente().getId(), op.getCliente().getNome());
        });

        List<ResumoRelatorioResponse.ResumoPorCarteira> porCarteira = filtradas.stream()
                .collect(Collectors.groupingBy(op -> op.getRecomendacao().getCarteira().getId()))
                .entrySet().stream()
                .map(e -> new ResumoRelatorioResponse.ResumoPorCarteira(
                        e.getKey(), carteiraNomes.get(e.getKey()), (long) e.getValue().size()))
                .collect(Collectors.toList());

        List<ResumoRelatorioResponse.ResumoPorCliente> porCliente = filtradas.stream()
                .collect(Collectors.groupingBy(op -> op.getCliente().getId()))
                .entrySet().stream()
                .map(e -> new ResumoRelatorioResponse.ResumoPorCliente(
                        e.getKey(), clienteNomes.get(e.getKey()), (long) e.getValue().size()))
                .collect(Collectors.toList());

        List<ResumoRelatorioResponse.ResumoPorMoeda> porMoeda = filtradas.stream()
                .map(op -> op.getRecomendacao().getMoeda() + "/" + op.getRecomendacao().getParMoeda())
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()))
                .entrySet().stream()
                .map(e -> new ResumoRelatorioResponse.ResumoPorMoeda(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<ResumoRelatorioResponse.OperacoesPorPeriodo> porPeriodo = filtradas.stream()
                .map(op -> op.getDataExecucao().toLocalDate().format(fmt))
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new ResumoRelatorioResponse.OperacoesPorPeriodo(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        List<ResumoRelatorioResponse.RecomendacaoResolvidaItem> recomendacoesResolvidas = resolvidaClienteRepository
                .findByConsultorId(consultorId, de, ate).stream()
                .map(rrc -> new ResumoRelatorioResponse.RecomendacaoResolvidaItem(
                        rrc.getCliente().getId(),
                        rrc.getCliente().getNome(),
                        rrc.getRecomendacao().getId(),
                        rrc.getRecomendacao().getMoeda() + "/" + rrc.getRecomendacao().getParMoeda(),
                        rrc.getRecomendacao().getCarteira().getId(),
                        rrc.getRecomendacao().getCarteira().getNome(),
                        rrc.getResolvidoEm()))
                .collect(Collectors.toList());

        Map<String, List<OperacaoCliente>> porClienteMoeda = filtradas.stream()
                .collect(Collectors.groupingBy(op -> op.getCliente().getId() + "_" + op.getRecomendacao().getMoeda() + "/" + op.getRecomendacao().getParMoeda()));
        List<ResumoRelatorioResponse.PerdaGanhoClienteMoeda> perdasGanhos = new ArrayList<>();
        for (Map.Entry<String, List<OperacaoCliente>> e : porClienteMoeda.entrySet()) {
            List<OperacaoCliente> ops = e.getValue();
            OperacaoCliente first = ops.get(0);
            Long cid = first.getCliente().getId();
            String cNome = first.getCliente().getNome();
            String moedaPar = first.getRecomendacao().getMoeda() + "/" + first.getRecomendacao().getParMoeda();
            BigDecimal compras = BigDecimal.ZERO;
            BigDecimal vendas = BigDecimal.ZERO;
            for (OperacaoCliente op : ops) {
                BigDecimal valor = op.getPrecoExecutado().multiply(op.getQuantidade());
                if (op.getTipo() == TipoOperacao.COMPRA) compras = compras.add(valor);
                else vendas = vendas.add(valor);
            }
            perdasGanhos.add(new ResumoRelatorioResponse.PerdaGanhoClienteMoeda(cid, cNome, moedaPar, compras, vendas, vendas.subtract(compras)));
        }
        perdasGanhos.sort(Comparator.comparing(ResumoRelatorioResponse.PerdaGanhoClienteMoeda::getClienteNome)
                .thenComparing(ResumoRelatorioResponse.PerdaGanhoClienteMoeda::getMoedaPar));

        return ResumoRelatorioResponse.builder()
                .totalOperacoes((long) filtradas.size())
                .totalCompras(totalCompras)
                .totalVendas(totalVendas)
                .porCarteira(porCarteira)
                .porCliente(porCliente)
                .porMoeda(porMoeda)
                .operacoesPorPeriodo(porPeriodo)
                .recomendacoesResolvidas(recomendacoesResolvidas)
                .perdasGanhosPorClienteMoeda(perdasGanhos)
                .build();
    }

    private RelatorioConsultorResponse toResponse(OperacaoCliente op) {
        BigDecimal valorOp = op.getPrecoExecutado().multiply(op.getQuantidade());
        var rec = op.getRecomendacao();
        var optResolvida = resolvidaClienteRepository.findByRecomendacaoIdAndClienteId(rec.getId(), op.getCliente().getId());
        return RelatorioConsultorResponse.builder()
                .id(op.getId())
                .recomendacaoId(rec.getId())
                .recomendacaoMoedaPar(rec.getMoeda() + "/" + rec.getParMoeda())
                .carteiraId(rec.getCarteira().getId())
                .carteiraNome(rec.getCarteira().getNome())
                .clienteId(op.getCliente().getId())
                .clienteNome(op.getCliente().getNome())
                .tipo(op.getTipo())
                .precoExecutado(op.getPrecoExecutado())
                .quantidade(op.getQuantidade())
                .dataExecucao(op.getDataExecucao())
                .observacao(op.getObservacao())
                .createdAt(op.getCreatedAt())
                .precoEntradaRecomendacao(rec.getPrecoEntrada())
                .valorOperacao(valorOp)
                .recomendacaoResolvidaPeloCliente(optResolvida.isPresent())
                .recomendacaoResolvidoEm(optResolvida.map(com.example.CJLInvestimentos.entities.RecomendacaoResolvidaCliente::getResolvidoEm).orElse(null))
                .build();
    }
}

package com.example.CJLInvestimentos.services;

import com.example.CJLInvestimentos.dtos.response.RelatorioClienteOperacaoResponse;
import com.example.CJLInvestimentos.dtos.response.ResumoRelatorioClienteResponse;
import com.example.CJLInvestimentos.entities.OperacaoCliente;
import com.example.CJLInvestimentos.entities.enums.TipoOperacao;
import com.example.CJLInvestimentos.repositories.OperacaoClienteRepository;
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
public class RelatorioClienteService {

    private final OperacaoClienteRepository operacaoClienteRepository;

    public List<RelatorioClienteOperacaoResponse> listarOperacoes(Long clienteId, LocalDate dataDe, LocalDate dataAte) {
        LocalDateTime de = dataDe != null ? dataDe.atStartOfDay() : LocalDateTime.of(1970, 1, 1, 0, 0);
        LocalDateTime ate = dataAte != null ? dataAte.atTime(23, 59, 59) : LocalDateTime.of(9999, 12, 31, 23, 59, 59);
        List<OperacaoCliente> list = operacaoClienteRepository.findByClienteIdAndDataExecucaoBetween(clienteId, de, ate);
        return list.stream().map(this::toOperacaoResponse).collect(Collectors.toList());
    }

    public ResumoRelatorioClienteResponse resumo(Long clienteId, LocalDate dataDe, LocalDate dataAte) {
        LocalDateTime de = dataDe != null ? dataDe.atStartOfDay() : LocalDateTime.of(1970, 1, 1, 0, 0);
        LocalDateTime ate = dataAte != null ? dataAte.atTime(23, 59, 59) : LocalDateTime.of(9999, 12, 31, 23, 59, 59);
        List<OperacaoCliente> filtradas = operacaoClienteRepository.findByClienteIdAndDataExecucaoBetween(clienteId, de, ate);

        long totalCompras = filtradas.stream().filter(op -> op.getTipo() == TipoOperacao.COMPRA).count();
        long totalVendas = filtradas.stream().filter(op -> op.getTipo() == TipoOperacao.VENDA).count();

        BigDecimal valorTotalCompras = BigDecimal.ZERO;
        BigDecimal valorTotalVendas = BigDecimal.ZERO;
        for (OperacaoCliente op : filtradas) {
            BigDecimal valor = op.getPrecoExecutado().multiply(op.getQuantidade());
            if (op.getTipo() == TipoOperacao.COMPRA) {
                valorTotalCompras = valorTotalCompras.add(valor);
            } else {
                valorTotalVendas = valorTotalVendas.add(valor);
            }
        }
        BigDecimal resultado = valorTotalVendas.subtract(valorTotalCompras);

        Map<String, List<OperacaoCliente>> porMoeda = filtradas.stream()
                .collect(Collectors.groupingBy(op -> op.getRecomendacao().getMoeda() + "/" + op.getRecomendacao().getParMoeda()));
        List<ResumoRelatorioClienteResponse.PerdaGanhoPorMoeda> perdasGanhosPorMoeda = new ArrayList<>();
        for (Map.Entry<String, List<OperacaoCliente>> e : porMoeda.entrySet()) {
            BigDecimal compras = BigDecimal.ZERO;
            BigDecimal vendas = BigDecimal.ZERO;
            for (OperacaoCliente op : e.getValue()) {
                BigDecimal valor = op.getPrecoExecutado().multiply(op.getQuantidade());
                if (op.getTipo() == TipoOperacao.COMPRA) compras = compras.add(valor);
                else vendas = vendas.add(valor);
            }
            perdasGanhosPorMoeda.add(new ResumoRelatorioClienteResponse.PerdaGanhoPorMoeda(
                    e.getKey(), compras, vendas, vendas.subtract(compras)));
        }
        perdasGanhosPorMoeda.sort(Comparator.comparing(ResumoRelatorioClienteResponse.PerdaGanhoPorMoeda::getMoedaPar));

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<ResumoRelatorioClienteResponse.OperacoesPorPeriodo> operacoesPorPeriodo = filtradas.stream()
                .map(op -> op.getDataExecucao().toLocalDate().format(fmt))
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new ResumoRelatorioClienteResponse.OperacoesPorPeriodo(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        return ResumoRelatorioClienteResponse.builder()
                .totalOperacoes((long) filtradas.size())
                .totalCompras(totalCompras)
                .totalVendas(totalVendas)
                .valorTotalCompras(valorTotalCompras)
                .valorTotalVendas(valorTotalVendas)
                .resultado(resultado)
                .perdasGanhosPorMoeda(perdasGanhosPorMoeda)
                .operacoesPorPeriodo(operacoesPorPeriodo)
                .build();
    }

    private RelatorioClienteOperacaoResponse toOperacaoResponse(OperacaoCliente op) {
        BigDecimal valorOp = op.getPrecoExecutado().multiply(op.getQuantidade());
        var rec = op.getRecomendacao();
        return RelatorioClienteOperacaoResponse.builder()
                .id(op.getId())
                .recomendacaoId(rec.getId())
                .recomendacaoMoedaPar(rec.getMoeda() + "/" + rec.getParMoeda())
                .carteiraId(rec.getCarteira().getId())
                .carteiraNome(rec.getCarteira().getNome())
                .tipo(op.getTipo())
                .precoExecutado(op.getPrecoExecutado())
                .quantidade(op.getQuantidade())
                .dataExecucao(op.getDataExecucao())
                .observacao(op.getObservacao())
                .createdAt(op.getCreatedAt())
                .precoEntradaRecomendacao(rec.getPrecoEntrada())
                .valorOperacao(valorOp)
                .build();
    }
}

package com.example.CJLInvestimentos.services;

import com.example.CJLInvestimentos.dtos.request.MovimentacaoRequest;
import com.example.CJLInvestimentos.dtos.response.MovimentacaoResponse;
import com.example.CJLInvestimentos.dtos.response.SaldoResponse;
import com.example.CJLInvestimentos.entities.AtivoCliente;
import com.example.CJLInvestimentos.entities.Carteira;
import com.example.CJLInvestimentos.entities.MovimentacaoCarteira;
import com.example.CJLInvestimentos.entities.User;
import com.example.CJLInvestimentos.entities.enums.CategoriaAtivo;
import com.example.CJLInvestimentos.entities.enums.TipoMovimentacao;
import com.example.CJLInvestimentos.exceptions.BusinessException;
import com.example.CJLInvestimentos.repositories.AtivoClienteRepository;
import com.example.CJLInvestimentos.repositories.CarteiraRepository;
import com.example.CJLInvestimentos.repositories.CotacaoRepository;
import com.example.CJLInvestimentos.repositories.MovimentacaoCarteiraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovimentacaoService {

    private final MovimentacaoCarteiraRepository movimentacaoRepository;
    private final AtivoClienteRepository ativoClienteRepository;
    private final CarteiraRepository carteiraRepository;
    private final CotacaoRepository cotacaoRepository;

    private static final Set<String> MOEDAS_CASH = Set.of("USD", "BRL", "EUR", "GBP", "ARS", "MXN", "CAD", "AUD", "CHF", "JPY", "CNY", "NZD");

    @Transactional
    public MovimentacaoResponse registrar(Long carteiraId, User cliente, MovimentacaoRequest request) {
        TipoMovimentacao tipo;
        try {
            tipo = TipoMovimentacao.valueOf(request.getTipo().toUpperCase());
        } catch (Exception e) {
            throw new BusinessException("Tipo de movimentacao invalido. Selecione APORTE (deposito) ou SAQUE (retirada).");
        }

        String moeda = request.getMoeda() != null ? request.getMoeda().toUpperCase() : "USD";
        LocalDate data;
        try {
            data = LocalDate.parse(request.getDataMovimentacao());
        } catch (Exception e) {
            throw new BusinessException("Data invalida. Informe no formato correto (ex: 2025-01-15).");
        }

        Carteira carteira = null;
        if (carteiraId != null) {
            carteira = carteiraRepository.findById(carteiraId)
                    .orElseThrow(() -> new BusinessException("Carteira não encontrada."));
        }

        // Ajustar o ativo cash do cliente
        ajustarAtivoCash(cliente, moeda, request.getValor(), tipo);

        MovimentacaoCarteira mov = MovimentacaoCarteira.builder()
                .cliente(cliente)
                .carteira(carteira)
                .tipo(tipo)
                .valor(request.getValor())
                .moeda(moeda)
                .dataMovimentacao(data)
                .observacao(request.getObservacao())
                .build();

        mov = movimentacaoRepository.save(mov);
        return toResponse(mov);
    }

    public List<MovimentacaoResponse> listarPorClienteCarteira(Long clienteId, Long carteiraId) {
        return movimentacaoRepository
                .findByClienteIdAndCarteiraIdOrderByDataMovimentacaoDescCreatedAtDesc(clienteId, carteiraId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<MovimentacaoResponse> listarPorCarteira(Long carteiraId) {
        return movimentacaoRepository
                .findByCarteiraIdOrderByDataMovimentacaoDescCreatedAtDesc(carteiraId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<MovimentacaoResponse> listarPorCliente(Long clienteId) {
        return movimentacaoRepository
                .findByClienteIdOrderByDataMovimentacaoDescCreatedAtDesc(clienteId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public SaldoResponse calcularSaldo(Long carteiraId, Long clienteId) {
        BigDecimal totalAportado = movimentacaoRepository.somarAportes(clienteId, carteiraId);
        BigDecimal totalSacado = movimentacaoRepository.somarSaques(clienteId, carteiraId);

        List<AtivoCliente> ativos = ativoClienteRepository.findByClienteIdOrderBySimboloAsc(clienteId);

        BigDecimal saldoDisponivel = BigDecimal.ZERO;
        BigDecimal saldoInvestido = BigDecimal.ZERO;

        for (AtivoCliente a : ativos) {
            BigDecimal preco = resolverPreco(a);
            if (preco == null) continue;
            BigDecimal valor = a.getQuantidade().multiply(preco);

            if (MOEDAS_CASH.contains(a.getSimbolo().toUpperCase())) {
                saldoDisponivel = saldoDisponivel.add(valor);
            } else {
                saldoInvestido = saldoInvestido.add(valor);
            }
        }

        BigDecimal saldoTotal = saldoDisponivel.add(saldoInvestido);
        BigDecimal capitalLiquido = totalAportado.subtract(totalSacado);
        BigDecimal lucroPerda = capitalLiquido.compareTo(BigDecimal.ZERO) > 0
                ? saldoTotal.subtract(capitalLiquido)
                : BigDecimal.ZERO;

        return SaldoResponse.builder()
                .totalAportado(totalAportado)
                .totalSacado(totalSacado)
                .saldoDisponivel(saldoDisponivel)
                .saldoInvestido(saldoInvestido)
                .saldoTotal(saldoTotal)
                .lucroPerda(lucroPerda)
                .moedaReferencia("USD")
                .build();
    }

    private void ajustarAtivoCash(User cliente, String moeda, BigDecimal valor, TipoMovimentacao tipo) {
        Optional<AtivoCliente> opt = ativoClienteRepository.findByClienteAndSimbolo(cliente.getId(), moeda);

        if (tipo == TipoMovimentacao.APORTE) {
            if (opt.isPresent()) {
                AtivoCliente ativo = opt.get();
                ativo.setQuantidade(ativo.getQuantidade().add(valor));
                ativoClienteRepository.save(ativo);
            } else {
                AtivoCliente novo = AtivoCliente.builder()
                        .cliente(cliente)
                        .simbolo(moeda)
                        .nome(nomeParaMoeda(moeda))
                        .categoria(CategoriaAtivo.FOREX)
                        .quantidade(valor)
                        .parMoedaReferencia("USD")
                        .build();
                ativoClienteRepository.save(novo);
            }
        } else {
            if (opt.isEmpty()) {
                throw new BusinessException("Saque nao permitido. Voce ainda nao possui saldo de " + moeda + " nesta carteira.");
            }
            AtivoCliente ativo = opt.get();
            if (ativo.getQuantidade().compareTo(valor) < 0) {
                throw new BusinessException("Saldo insuficiente de " + moeda + " para este saque. Saldo disponivel: " + ativo.getQuantidade().stripTrailingZeros().toPlainString() + " " + moeda + ".");
            }
            ativo.setQuantidade(ativo.getQuantidade().subtract(valor));
            ativoClienteRepository.save(ativo);
        }
    }

    private String nomeParaMoeda(String moeda) {
        return switch (moeda) {
            case "USD" -> "Dólar";
            case "BRL" -> "Real";
            case "EUR" -> "Euro";
            case "GBP" -> "Libra";
            default -> moeda;
        };
    }

    private BigDecimal resolverPreco(AtivoCliente ativo) {
        if (MOEDAS_CASH.contains(ativo.getSimbolo().toUpperCase()) && "USD".equals(ativo.getParMoedaReferencia())) {
            return BigDecimal.ONE;
        }
        return cotacaoRepository.findTopByMoedaAndParMoedaOrderByDataHoraDesc(
                        ativo.getSimbolo(), ativo.getParMoedaReferencia())
                .map(c -> c.getPrecoCompra())
                .orElse(ativo.getPrecoManual());
    }

    private MovimentacaoResponse toResponse(MovimentacaoCarteira m) {
        return MovimentacaoResponse.builder()
                .id(m.getId())
                .tipo(m.getTipo().name())
                .valor(m.getValor())
                .moeda(m.getMoeda())
                .dataMovimentacao(m.getDataMovimentacao())
                .observacao(m.getObservacao())
                .clienteId(m.getCliente().getId())
                .clienteNome(m.getCliente().getNome())
                .createdAt(m.getCreatedAt())
                .build();
    }
}

package com.example.CJLInvestimentos.services;

import com.example.CJLInvestimentos.dtos.request.AtivoClienteRequest;
import com.example.CJLInvestimentos.dtos.response.AtivoClienteResponse;
import com.example.CJLInvestimentos.dtos.response.PortfolioResumoResponse;
import com.example.CJLInvestimentos.entities.AtivoCliente;
import com.example.CJLInvestimentos.entities.Cotacao;
import com.example.CJLInvestimentos.entities.OperacaoCliente;
import com.example.CJLInvestimentos.entities.User;
import com.example.CJLInvestimentos.entities.enums.TipoOperacao;
import com.example.CJLInvestimentos.exceptions.BusinessException;
import com.example.CJLInvestimentos.repositories.AtivoClienteRepository;
import com.example.CJLInvestimentos.repositories.CotacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final AtivoClienteRepository ativoClienteRepository;
    private final CotacaoRepository cotacaoRepository;

    @Transactional
    public AtivoClienteResponse adicionarAtivo(AtivoClienteRequest request, User cliente) {
        String simbolo = request.getSimbolo().trim().toUpperCase();
        if (ativoClienteRepository.existsByClienteIdAndSimboloIgnoreCase(cliente.getId(), simbolo)) {
            throw new BusinessException("Ativo '" + simbolo + "' já existe no seu portfolio.");
        }
        AtivoCliente ativo = AtivoCliente.builder()
                .cliente(cliente)
                .simbolo(simbolo)
                .nome(request.getNome().trim())
                .categoria(request.getCategoria())
                .quantidade(request.getQuantidade())
                .precoManual(request.getPrecoManual())
                .parMoedaReferencia(request.getParMoedaReferencia() != null ? request.getParMoedaReferencia() : "BRL")
                .build();
        ativo = ativoClienteRepository.save(ativo);
        return toResponse(ativo, null);
    }

    @Transactional
    public AtivoClienteResponse atualizarAtivo(Long ativoId, AtivoClienteRequest request, User cliente) {
        AtivoCliente ativo = ativoClienteRepository.findById(ativoId)
                .orElseThrow(() -> new BusinessException("Ativo não encontrado."));
        if (!ativo.getCliente().getId().equals(cliente.getId())) {
            throw new BusinessException("Acesso negado.");
        }
        ativo.setNome(request.getNome().trim());
        ativo.setCategoria(request.getCategoria());
        ativo.setQuantidade(request.getQuantidade());
        ativo.setPrecoManual(request.getPrecoManual());
        if (request.getParMoedaReferencia() != null) {
            ativo.setParMoedaReferencia(request.getParMoedaReferencia());
        }
        ativo = ativoClienteRepository.save(ativo);
        return toResponse(ativo, null);
    }

    @Transactional
    public void removerAtivo(Long ativoId, User cliente) {
        AtivoCliente ativo = ativoClienteRepository.findById(ativoId)
                .orElseThrow(() -> new BusinessException("Ativo não encontrado."));
        if (!ativo.getCliente().getId().equals(cliente.getId())) {
            throw new BusinessException("Acesso negado.");
        }
        ativoClienteRepository.delete(ativo);
    }

    public List<AtivoClienteResponse> listarAtivos(User cliente) {
        List<AtivoCliente> ativos = ativoClienteRepository.findByClienteIdOrderBySimboloAsc(cliente.getId());
        BigDecimal total = calcularValorTotal(ativos);
        return ativos.stream().map(a -> toResponse(a, total)).toList();
    }

    public PortfolioResumoResponse resumoPortfolio(User cliente) {
        List<AtivoCliente> ativos = ativoClienteRepository.findByClienteIdOrderBySimboloAsc(cliente.getId());
        BigDecimal total = calcularValorTotal(ativos);
        List<AtivoClienteResponse> ativosResp = ativos.stream().map(a -> toResponse(a, total)).toList();
        return PortfolioResumoResponse.builder()
                .clienteId(cliente.getId())
                .clienteNome(cliente.getNome())
                .valorTotalPortfolio(total)
                .moedaReferencia("BRL")
                .ativos(ativosResp)
                .build();
    }

    public PortfolioResumoResponse resumoPortfolioByClienteId(Long clienteId) {
        List<AtivoCliente> ativos = ativoClienteRepository.findByClienteIdOrderBySimboloAsc(clienteId);
        BigDecimal total = calcularValorTotal(ativos);
        List<AtivoClienteResponse> ativosResp = ativos.stream().map(a -> toResponse(a, total)).toList();
        String clienteNome = ativos.isEmpty() ? "" : ativos.get(0).getCliente().getNome();
        return PortfolioResumoResponse.builder()
                .clienteId(clienteId)
                .clienteNome(clienteNome)
                .valorTotalPortfolio(total)
                .moedaReferencia("BRL")
                .ativos(ativosResp)
                .build();
    }

    @Transactional
    public void atualizarPortfolioAposOperacao(OperacaoCliente operacao) {
        Long clienteId = operacao.getCliente().getId();
        String moedaBase = operacao.getMoedaBase();
        String moedaContra = operacao.getMoedaContra();
        BigDecimal quantidade = operacao.getQuantidade();
        BigDecimal valorTotal = operacao.getValorTotal();

        if (moedaBase == null || moedaContra == null || quantidade == null || valorTotal == null) {
            return;
        }

        if (operacao.getTipo() == TipoOperacao.VENDA) {
            ajustarAtivo(clienteId, moedaBase, quantidade.negate());
            ajustarAtivo(clienteId, moedaContra, valorTotal);
        } else {
            ajustarAtivo(clienteId, moedaBase, quantidade);
            ajustarAtivo(clienteId, moedaContra, valorTotal.negate());
        }
    }

    @Transactional
    public void reverterPortfolioOperacao(OperacaoCliente operacao) {
        Long clienteId = operacao.getCliente().getId();
        String moedaBase = operacao.getMoedaBase();
        String moedaContra = operacao.getMoedaContra();
        BigDecimal quantidade = operacao.getQuantidade();
        BigDecimal valorTotal = operacao.getValorTotal();

        if (moedaBase == null || moedaContra == null || quantidade == null || valorTotal == null) {
            return;
        }

        if (operacao.getTipo() == TipoOperacao.VENDA) {
            ajustarAtivo(clienteId, moedaBase, quantidade);
            ajustarAtivo(clienteId, moedaContra, valorTotal.negate());
        } else {
            ajustarAtivo(clienteId, moedaBase, quantidade.negate());
            ajustarAtivo(clienteId, moedaContra, valorTotal);
        }
    }

    public BigDecimal getQuantidadeAtivo(Long clienteId, String simbolo) {
        return ativoClienteRepository.findByClienteAndSimbolo(clienteId, simbolo)
                .map(AtivoCliente::getQuantidade)
                .orElse(BigDecimal.ZERO);
    }

    // --- helpers ---

    private void ajustarAtivo(Long clienteId, String simbolo, BigDecimal delta) {
        Optional<AtivoCliente> opt = ativoClienteRepository.findByClienteAndSimbolo(clienteId, simbolo);
        if (opt.isPresent()) {
            AtivoCliente ativo = opt.get();
            ativo.setQuantidade(ativo.getQuantidade().add(delta));
            ativoClienteRepository.save(ativo);
        } else if (delta.compareTo(BigDecimal.ZERO) > 0) {
            AtivoCliente novo = AtivoCliente.builder()
                    .cliente(User.builder().id(clienteId).build())
                    .simbolo(simbolo.toUpperCase())
                    .nome(simbolo.toUpperCase())
                    .categoria(com.example.CJLInvestimentos.entities.enums.CategoriaAtivo.OUTRO)
                    .quantidade(delta)
                    .parMoedaReferencia("BRL")
                    .build();
            ativoClienteRepository.save(novo);
        }
    }

    private BigDecimal resolverPrecoAtual(AtivoCliente ativo) {
        Optional<Cotacao> cotacao = cotacaoRepository.findTopByMoedaAndParMoedaOrderByDataHoraDesc(
                ativo.getSimbolo(), ativo.getParMoedaReferencia());
        if (cotacao.isPresent()) {
            return cotacao.get().getPrecoCompra();
        }
        return ativo.getPrecoManual();
    }

    private BigDecimal calcularValorTotal(List<AtivoCliente> ativos) {
        BigDecimal total = BigDecimal.ZERO;
        for (AtivoCliente a : ativos) {
            BigDecimal preco = resolverPrecoAtual(a);
            if (preco != null) {
                total = total.add(a.getQuantidade().multiply(preco));
            }
        }
        return total;
    }

    private AtivoClienteResponse toResponse(AtivoCliente ativo, BigDecimal totalPortfolio) {
        BigDecimal preco = resolverPrecoAtual(ativo);
        BigDecimal valor = preco != null ? ativo.getQuantidade().multiply(preco) : null;
        BigDecimal pctAlocacao = null;
        if (valor != null && totalPortfolio != null && totalPortfolio.compareTo(BigDecimal.ZERO) > 0) {
            pctAlocacao = valor.multiply(BigDecimal.valueOf(100)).divide(totalPortfolio, 2, RoundingMode.HALF_UP);
        }
        return AtivoClienteResponse.builder()
                .id(ativo.getId())
                .clienteId(ativo.getCliente().getId())
                .simbolo(ativo.getSimbolo())
                .nome(ativo.getNome())
                .categoria(ativo.getCategoria())
                .quantidade(ativo.getQuantidade())
                .precoManual(ativo.getPrecoManual())
                .parMoedaReferencia(ativo.getParMoedaReferencia())
                .precoAtual(preco)
                .valorTotal(valor)
                .percentualAlocacao(pctAlocacao)
                .createdAt(ativo.getCreatedAt())
                .updatedAt(ativo.getUpdatedAt())
                .build();
    }
}

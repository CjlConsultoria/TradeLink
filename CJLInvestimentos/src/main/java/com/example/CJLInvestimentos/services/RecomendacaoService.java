package com.example.CJLInvestimentos.services;

import com.example.CJLInvestimentos.dtos.request.RecomendacaoRequest;
import com.example.CJLInvestimentos.dtos.response.RecomendacaoResponse;
import com.example.CJLInvestimentos.entities.Carteira;
import com.example.CJLInvestimentos.entities.Cotacao;
import com.example.CJLInvestimentos.entities.Recomendacao;
import com.example.CJLInvestimentos.entities.User;
import com.example.CJLInvestimentos.dtos.response.PageResponse;
import com.example.CJLInvestimentos.entities.enums.Role;
import com.example.CJLInvestimentos.entities.enums.TipoRecomendacao;
import com.example.CJLInvestimentos.entities.enums.StatusRecomendacao;
import com.example.CJLInvestimentos.exceptions.ResourceNotFoundException;
import com.example.CJLInvestimentos.entities.RecomendacaoResolvidaCliente;
import com.example.CJLInvestimentos.repositories.CarteiraClienteRepository;
import com.example.CJLInvestimentos.repositories.CarteiraRepository;
import com.example.CJLInvestimentos.repositories.CotacaoRepository;
import com.example.CJLInvestimentos.repositories.OperacaoClienteRepository;
import com.example.CJLInvestimentos.repositories.RecomendacaoResolvidaClienteRepository;
import com.example.CJLInvestimentos.repositories.RecomendacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.example.CJLInvestimentos.entities.Empresa;

@Service
@RequiredArgsConstructor
public class RecomendacaoService {

    private final RecomendacaoRepository recomendacaoRepository;
    private final CarteiraRepository carteiraRepository;
    private final CarteiraClienteRepository carteiraClienteRepository;
    private final CotacaoRepository cotacaoRepository;
    private final OperacaoClienteRepository operacaoClienteRepository;
    private final RecomendacaoResolvidaClienteRepository resolvidaClienteRepository;
    private final NotificationAsyncRunner notificationAsyncRunner;

    public RecomendacaoResponse criar(Long carteiraId, RecomendacaoRequest request, User consultor) {
        Carteira carteira = getCarteiraDoConsultor(carteiraId, consultor);

        Recomendacao recomendacao = recomendacaoRepository.save(
                Recomendacao.builder()
                        .carteira(carteira)
                        .tipo(request.getTipo())
                        .moeda(request.getMoeda().toUpperCase())
                        .parMoeda(request.getParMoeda().toUpperCase())
                        .precoEntrada(request.getPrecoEntrada())
                        .precoAlvo(request.getPrecoAlvo())
                        .stopLoss(request.getStopLoss())
                        .quantidade(request.getQuantidade())
                        .observacao(request.getObservacao())
                        .build()
        );

        notificationAsyncRunner.notificarClientesNovaRecomendacaoAsync(recomendacao.getId());
        return toResponse(recomendacao);
    }

    public RecomendacaoResponse atualizar(Long id, RecomendacaoRequest request, User consultor) {
        Recomendacao recomendacao = recomendacaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recomendação não encontrada"));

        getCarteiraDoConsultor(recomendacao.getCarteira().getId(), consultor);

        recomendacao.setTipo(request.getTipo());
        recomendacao.setMoeda(request.getMoeda().toUpperCase());
        recomendacao.setParMoeda(request.getParMoeda().toUpperCase());
        recomendacao.setPrecoEntrada(request.getPrecoEntrada());
        recomendacao.setPrecoAlvo(request.getPrecoAlvo());
        recomendacao.setStopLoss(request.getStopLoss());
        recomendacao.setQuantidade(request.getQuantidade());
        recomendacao.setObservacao(request.getObservacao());
        recomendacaoRepository.save(recomendacao);

        return toResponse(recomendacao);
    }

    public RecomendacaoResponse cancelar(Long id, User consultor) {
        Recomendacao recomendacao = recomendacaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recomendação não encontrada"));
        getCarteiraDoConsultor(recomendacao.getCarteira().getId(), consultor);
        recomendacao.setStatus(StatusRecomendacao.CANCELADA);
        recomendacaoRepository.save(recomendacao);
        return toResponse(recomendacao);
    }

    public RecomendacaoResponse executar(Long id, User consultor) {
        Recomendacao recomendacao = recomendacaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recomendação não encontrada"));
        getCarteiraDoConsultor(recomendacao.getCarteira().getId(), consultor);
        recomendacao.setStatus(StatusRecomendacao.EXECUTADA);
        recomendacaoRepository.save(recomendacao);
        return toResponse(recomendacao);
    }

    public List<RecomendacaoResponse> listarPorCarteira(Long carteiraId, User user) {
        verificarAcesso(carteiraId, user);
        Long clienteId = user.getRole() == Role.Cliente ? user.getId() : null;
        return recomendacaoRepository.findByCarteiraId(carteiraId).stream()
                .map(r -> toResponse(r, clienteId))
                .collect(Collectors.toList());
    }

    public List<RecomendacaoResponse> listarAtivasPorCarteira(Long carteiraId, User user) {
        verificarAcesso(carteiraId, user);
        return recomendacaoRepository.findByCarteiraIdAndStatus(carteiraId, StatusRecomendacao.ATIVA).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /** Lista recomendações do cliente com filtros (carteira, tipo, nome, resolvido) e paginação. */
    public PageResponse<RecomendacaoResponse> listarParaClienteFiltrado(User cliente, Long carteiraId, TipoRecomendacao tipo, String nome, Boolean resolvido, int page, int size) {
        List<Long> carteiraIds = carteiraClienteRepository.findByClienteId(cliente.getId()).stream()
                .map(cc -> cc.getCarteira().getId())
                .collect(Collectors.toList());
        if (carteiraIds.isEmpty()) {
            return PageResponse.<RecomendacaoResponse>builder()
                    .content(new ArrayList<>()).totalElements(0).totalPages(0).number(0).size(size).first(true).last(true).build();
        }
        if (carteiraId != null && carteiraIds.contains(carteiraId)) {
            carteiraIds = List.of(carteiraId);
        } else if (carteiraId != null) {
            return PageResponse.<RecomendacaoResponse>builder()
                    .content(new ArrayList<>()).totalElements(0).totalPages(0).number(0).size(size).first(true).last(true).build();
        }
        List<Recomendacao> todas = recomendacaoRepository.findByCarteiraIdIn(carteiraIds);
        String nomeLower = (nome != null && !nome.isBlank()) ? nome.trim().toLowerCase() : null;
        List<Recomendacao> filtradas = todas.stream()
                .filter(r -> tipo == null || r.getTipo() == tipo)
                .filter(r -> nomeLower == null || (r.getMoeda() != null && r.getMoeda().toLowerCase().contains(nomeLower))
                        || (r.getParMoeda() != null && r.getParMoeda().toLowerCase().contains(nomeLower))
                        || (r.getObservacao() != null && r.getObservacao().toLowerCase().contains(nomeLower)))
                .filter(r -> resolvido == null || resolvidaClienteRepository.existsByRecomendacaoIdAndClienteId(r.getId(), cliente.getId()) == resolvido)
                .sorted(java.util.Comparator.comparing(Recomendacao::getCreatedAt, java.util.Comparator.nullsLast(java.time.LocalDateTime::compareTo)).reversed())
                .collect(Collectors.toList());
        int total = filtradas.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) total / size));
        int from = Math.min(page * size, total);
        int to = Math.min(from + size, total);
        List<RecomendacaoResponse> content = filtradas.subList(from, to).stream()
                .map(r -> toResponse(r, cliente.getId()))
                .collect(Collectors.toList());
        return PageResponse.<RecomendacaoResponse>builder()
                .content(content)
                .totalElements(total)
                .totalPages(totalPages)
                .number(page)
                .size(size)
                .first(page == 0)
                .last(page >= totalPages - 1)
                .build();
    }

    private void verificarAcesso(Long carteiraId, User user) {
        Carteira carteira = carteiraRepository.findById(carteiraId)
                .orElseThrow(() -> new ResourceNotFoundException("Carteira não encontrada"));

        if (user.getRole() == Role.Admin) {
            if (!carteira.getConsultor().getId().equals(user.getId())) {
                throw new AccessDeniedException("Sem acesso a esta carteira");
            }
        } else if (user.getRole() == Role.Cliente) {
            if (!carteiraClienteRepository.existsByCarteiraIdAndClienteId(carteiraId, user.getId())) {
                throw new AccessDeniedException("Sem acesso a esta carteira");
            }
        }
    }

    private Carteira getCarteiraDoConsultor(Long carteiraId, User consultor) {
        Carteira carteira = carteiraRepository.findById(carteiraId)
                .orElseThrow(() -> new ResourceNotFoundException("Carteira não encontrada"));

        if (!carteira.getConsultor().getId().equals(consultor.getId())) {
            throw new AccessDeniedException("Sem acesso a esta carteira");
        }

        return carteira;
    }

    /** Cliente marca ou desmarca a recomendação como resolvida. */
    @Transactional
    public RecomendacaoResponse marcarResolvido(Long recomendacaoId, User cliente, boolean resolvido) {
        Recomendacao rec = recomendacaoRepository.findById(recomendacaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Recomendação não encontrada"));
        verificarAcesso(rec.getCarteira().getId(), cliente);

        if (resolvido) {
            if (!resolvidaClienteRepository.existsByRecomendacaoIdAndClienteId(recomendacaoId, cliente.getId())) {
                resolvidaClienteRepository.save(RecomendacaoResolvidaCliente.builder()
                        .recomendacao(rec)
                        .cliente(cliente)
                        .build());
                notificationAsyncRunner.notificarConsultorClienteResolveuAsync(rec.getId(), cliente.getId());
            }
        } else {
            resolvidaClienteRepository.deleteByRecomendacaoIdAndClienteId(recomendacaoId, cliente.getId());
        }
        return toResponse(rec, cliente.getId());
    }

    private RecomendacaoResponse toResponse(Recomendacao r) {
        return toResponse(r, null);
    }

    private RecomendacaoResponse toResponse(Recomendacao r, Long clienteId) {
        BigDecimal cotacaoAtual = cotacaoRepository
                .findTopByMoedaAndParMoedaOrderByDataHoraDesc(r.getMoeda(), r.getParMoeda())
                .map(Cotacao::getPrecoCompra)
                .orElse(null);

        int totalOperacoesClientes = (int) operacaoClienteRepository.countByRecomendacaoId(r.getId());

        Boolean resolvido = null;
        java.time.LocalDateTime resolvidoEm = null;
        if (clienteId != null) {
            var opt = resolvidaClienteRepository.findByRecomendacaoIdAndClienteId(r.getId(), clienteId);
            resolvido = opt.isPresent();
            resolvidoEm = opt.map(RecomendacaoResolvidaCliente::getResolvidoEm).orElse(null);
        }

        return RecomendacaoResponse.builder()
                .id(r.getId())
                .carteiraId(r.getCarteira().getId())
                .carteiraNome(r.getCarteira().getNome())
                .tipo(r.getTipo())
                .moeda(r.getMoeda())
                .parMoeda(r.getParMoeda())
                .precoEntrada(r.getPrecoEntrada())
                .precoAlvo(r.getPrecoAlvo())
                .stopLoss(r.getStopLoss())
                .quantidade(r.getQuantidade())
                .status(r.getStatus())
                .observacao(r.getObservacao())
                .cotacaoAtual(cotacaoAtual)
                .totalOperacoesClientes(totalOperacoesClientes)
                .resolvido(resolvido)
                .resolvidoEm(resolvidoEm)
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}

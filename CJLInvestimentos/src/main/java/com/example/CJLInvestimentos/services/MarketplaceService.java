package com.example.CJLInvestimentos.services;

import com.example.CJLInvestimentos.dtos.request.MarketplacePerfilRequest;
import com.example.CJLInvestimentos.dtos.request.ResponderSolicitacaoRequest;
import com.example.CJLInvestimentos.dtos.request.SolicitacaoMentoriaRequest;
import com.example.CJLInvestimentos.dtos.response.MarketplaceConsultorResponse;
import com.example.CJLInvestimentos.dtos.response.MarketplacePerfilResponse;
import com.example.CJLInvestimentos.dtos.response.SolicitacaoMentoriaResponse;
import com.example.CJLInvestimentos.entities.Empresa;
import com.example.CJLInvestimentos.entities.SolicitacaoMentoria;
import com.example.CJLInvestimentos.entities.User;
import com.example.CJLInvestimentos.entities.enums.StatusSolicitacaoMentoria;
import com.example.CJLInvestimentos.exceptions.BusinessException;
import com.example.CJLInvestimentos.exceptions.ResourceNotFoundException;
import com.example.CJLInvestimentos.repositories.EmpresaRepository;
import com.example.CJLInvestimentos.repositories.SolicitacaoMentoriaRepository;
import com.example.CJLInvestimentos.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketplaceService {

    private final EmpresaRepository empresaRepository;
    private final SolicitacaoMentoriaRepository solicitacaoRepository;
    private final UserRepository userRepository;
    private final ConfiguracaoPlataformaService configService;
    private final StripePaymentService stripePaymentService;
    private final NotificationAsyncRunner notificationAsyncRunner;
    private final FaturaService faturaService;

    // ─── Listagem pública ────────────────────────────────────────

    public List<MarketplaceConsultorResponse> listarConsultores(String termo) {
        List<Empresa> empresas;
        if (termo != null && !termo.isBlank()) {
            empresas = empresaRepository.buscarNoMarketplace(termo.trim());
        } else {
            empresas = empresaRepository.findByMarketplaceVisivelTrueAndAtivoTrueOrderByNomeAsc();
        }
        return empresas.stream().map(this::toConsultorResponse).collect(Collectors.toList());
    }

    public MarketplaceConsultorResponse getConsultorDetalhe(Long empresaId) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Consultor não encontrado"));
        if (!Boolean.TRUE.equals(empresa.getMarketplaceVisivel()) || !Boolean.TRUE.equals(empresa.getAtivo())) {
            throw new BusinessException("Consultor não disponível no marketplace.");
        }
        return toConsultorResponse(empresa);
    }

    // ─── Perfil do consultor ─────────────────────────────────────

    public MarketplacePerfilResponse getPerfilMarketplace(User consultor) {
        Empresa empresa = getEmpresaDoConsultor(consultor);
        BigDecimal taxa = configService.getTaxaMarketplace();
        return MarketplacePerfilResponse.builder()
                .empresaId(empresa.getId())
                .nome(empresa.getNome())
                .marketplaceVisivel(empresa.getMarketplaceVisivel())
                .marketplaceDescricao(empresa.getMarketplaceDescricao())
                .marketplaceEspecializacao(empresa.getMarketplaceEspecializacao())
                .marketplaceExperiencia(empresa.getMarketplaceExperiencia())
                .marketplaceRedeSocial(empresa.getMarketplaceRedeSocial())
                .marketplacePrecoBase(empresa.getMarketplacePrecoBase())
                .taxaPlataforma(taxa)
                .build();
    }

    @Transactional
    public MarketplacePerfilResponse atualizarPerfilMarketplace(User consultor, MarketplacePerfilRequest request) {
        Empresa empresa = getEmpresaDoConsultor(consultor);
        if (request.getMarketplaceVisivel() != null) empresa.setMarketplaceVisivel(request.getMarketplaceVisivel());
        if (request.getMarketplaceDescricao() != null) empresa.setMarketplaceDescricao(request.getMarketplaceDescricao());
        if (request.getMarketplaceEspecializacao() != null) empresa.setMarketplaceEspecializacao(request.getMarketplaceEspecializacao());
        if (request.getMarketplaceExperiencia() != null) empresa.setMarketplaceExperiencia(request.getMarketplaceExperiencia());
        if (request.getMarketplaceRedeSocial() != null) empresa.setMarketplaceRedeSocial(request.getMarketplaceRedeSocial());
        if (request.getMarketplacePrecoBase() != null) empresa.setMarketplacePrecoBase(request.getMarketplacePrecoBase());
        empresaRepository.save(empresa);
        return getPerfilMarketplace(consultor);
    }

    // ─── Solicitação de mentoria (cliente) ───────────────────────

    @Transactional
    public SolicitacaoMentoriaResponse solicitarMentoria(User cliente, SolicitacaoMentoriaRequest request) {
        Empresa empresa = empresaRepository.findById(request.getEmpresaId())
                .orElseThrow(() -> new ResourceNotFoundException("Consultor não encontrado"));

        if (!Boolean.TRUE.equals(empresa.getMarketplaceVisivel()) || !Boolean.TRUE.equals(empresa.getAtivo())) {
            throw new BusinessException("Consultor não disponível no marketplace.");
        }

        // Verificar se já tem solicitação pendente ou aceita
        List<SolicitacaoMentoria> existentes = solicitacaoRepository
                .findByClienteIdAndEmpresaIdAndStatusIn(cliente.getId(), empresa.getId(),
                        List.of(StatusSolicitacaoMentoria.PENDENTE, StatusSolicitacaoMentoria.ACEITA));
        if (!existentes.isEmpty()) {
            throw new BusinessException("Você já possui uma solicitação pendente ou aceita com este consultor.");
        }

        SolicitacaoMentoria solicitacao = SolicitacaoMentoria.builder()
                .cliente(cliente)
                .empresa(empresa)
                .status(StatusSolicitacaoMentoria.PENDENTE)
                .precoProposto(empresa.getMarketplacePrecoBase())
                .mensagemCliente(request.getMensagemCliente())
                .build();
        solicitacaoRepository.save(solicitacao);

        // Email para o consultor
        notificationAsyncRunner.enviarEmailMarketplaceNovaSolicitacaoAsync(
                empresa, cliente.getNome() != null ? cliente.getNome() : cliente.getEmail(),
                cliente.getEmail(), request.getMensagemCliente());

        return toSolicitacaoResponse(solicitacao);
    }

    public List<SolicitacaoMentoriaResponse> listarSolicitacoesCliente(Long clienteId) {
        return solicitacaoRepository.findByClienteIdOrderByCreatedAtDesc(clienteId)
                .stream().map(this::toSolicitacaoResponse).collect(Collectors.toList());
    }

    @Transactional
    public void cancelarSolicitacao(Long solicitacaoId, User cliente) {
        SolicitacaoMentoria sol = solicitacaoRepository.findById(solicitacaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitação não encontrada"));
        if (!sol.getCliente().getId().equals(cliente.getId())) {
            throw new BusinessException("Solicitação não pertence a você.");
        }
        if (sol.getStatus() == StatusSolicitacaoMentoria.PAGA) {
            throw new BusinessException("Para cancelar uma mentoria ativa, use a opção de desvinculação.");
        }
        sol.setStatus(StatusSolicitacaoMentoria.CANCELADA);
        solicitacaoRepository.save(sol);
    }

    // ─── Resposta do consultor ───────────────────────────────────

    public List<SolicitacaoMentoriaResponse> listarSolicitacoesConsultor(Long empresaId) {
        return solicitacaoRepository.findByEmpresaIdOrderByCreatedAtDesc(empresaId)
                .stream().map(this::toSolicitacaoResponse).collect(Collectors.toList());
    }

    @Transactional
    public SolicitacaoMentoriaResponse responderSolicitacao(Long solicitacaoId, User consultor, ResponderSolicitacaoRequest request) {
        Empresa empresa = getEmpresaDoConsultor(consultor);
        SolicitacaoMentoria sol = solicitacaoRepository.findById(solicitacaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitação não encontrada"));

        if (!sol.getEmpresa().getId().equals(empresa.getId())) {
            throw new BusinessException("Solicitação não pertence à sua empresa.");
        }
        if (sol.getStatus() != StatusSolicitacaoMentoria.PENDENTE) {
            throw new BusinessException("Só é possível responder solicitações pendentes.");
        }

        if (Boolean.TRUE.equals(request.getAceitar())) {
            sol.setStatus(StatusSolicitacaoMentoria.ACEITA);
            BigDecimal precoFinal = request.getPrecoFinal() != null ? request.getPrecoFinal() : sol.getPrecoProposto();
            sol.setPrecoFinal(precoFinal);
            sol.setMensagemConsultor(request.getMensagemConsultor());
            solicitacaoRepository.save(sol);

            // Email para o cliente: aceita com preço
            notificationAsyncRunner.enviarEmailMarketplaceSolicitacaoAceitaAsync(
                    sol.getCliente().getEmail(),
                    sol.getCliente().getNome() != null ? sol.getCliente().getNome() : sol.getCliente().getEmail(),
                    empresa.getNome(), precoFinal);
        } else {
            sol.setStatus(StatusSolicitacaoMentoria.RECUSADA);
            sol.setMensagemConsultor(request.getMensagemConsultor());
            solicitacaoRepository.save(sol);

            // Email para o cliente: recusada
            notificationAsyncRunner.enviarEmailMarketplaceSolicitacaoRecusadaAsync(
                    sol.getCliente().getEmail(),
                    sol.getCliente().getNome() != null ? sol.getCliente().getNome() : sol.getCliente().getEmail(),
                    empresa.getNome());
        }

        return toSolicitacaoResponse(sol);
    }

    // ─── Checkout / Pagamento ────────────────────────────────────

    @Transactional
    public Map<String, String> criarCheckoutMarketplace(User cliente, Long solicitacaoId) {
        SolicitacaoMentoria sol = solicitacaoRepository.findById(solicitacaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitação não encontrada"));
        if (!sol.getCliente().getId().equals(cliente.getId())) {
            throw new BusinessException("Solicitação não pertence a você.");
        }
        if (sol.getStatus() != StatusSolicitacaoMentoria.ACEITA) {
            throw new BusinessException("Só é possível pagar solicitações aceitas.");
        }
        if (sol.getPrecoFinal() == null || sol.getPrecoFinal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Preço não definido para esta solicitação.");
        }

        Map<String, String> result = stripePaymentService.createCheckoutSessionMarketplace(cliente, sol);
        // Salvar o session ID que foi setado no objeto sol pelo Stripe service
        solicitacaoRepository.save(sol);
        return result;
    }

    /**
     * Chamado após pagamento confirmado (webhook ou fallback).
     * Vincula o cliente ao consultor com origemVinculo=MARKETPLACE.
     */
    @Transactional
    public void processarPagamentoMarketplace(Long solicitacaoId) {
        SolicitacaoMentoria sol = solicitacaoRepository.findById(solicitacaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitação não encontrada"));

        if (sol.getStatus() == StatusSolicitacaoMentoria.PAGA) {
            log.info("Solicitação {} já processada (PAGA). Ignorando.", solicitacaoId);
            return;
        }

        sol.setStatus(StatusSolicitacaoMentoria.PAGA);
        solicitacaoRepository.save(sol);

        User cliente = sol.getCliente();
        Empresa empresa = sol.getEmpresa();

        // Vincular cliente ao consultor
        cliente.setEmpresa(empresa);
        cliente.setOrigemVinculo("MARKETPLACE");
        cliente.setMarketplacePrecoCliente(sol.getPrecoFinal());
        cliente.setMarketplaceSubscriptionId(sol.getStripeSubscriptionId());
        cliente.setAutoGestao(false);
        cliente.setSubscriptionStatus("NONE");
        cliente.setCurrentPeriodEnd(null);
        cliente.setDataExclusao(null);
        userRepository.save(cliente);

        // Registrar fatura marketplace
        faturaService.registrarFaturaMarketplace(cliente, empresa, sol.getPrecoFinal(), sol.getStripeSubscriptionId());

        // Email para ambos: pagamento confirmado
        notificationAsyncRunner.enviarEmailMarketplacePagamentoConfirmadoAsync(
                cliente.getEmail(),
                cliente.getNome() != null ? cliente.getNome() : cliente.getEmail(),
                empresa.getNome(), sol.getPrecoFinal());

        log.info("Cliente {} vinculado ao consultor {} via marketplace. Preço: {}",
                cliente.getId(), empresa.getId(), sol.getPrecoFinal());
    }

    /**
     * Confirma pagamento marketplace por session_id (fallback quando webhook não chega).
     */
    @Transactional
    public boolean confirmarPagamentoMarketplacePorSessionId(String sessionId, User cliente) {
        SolicitacaoMentoria sol = solicitacaoRepository.findByStripeCheckoutSessionId(sessionId).orElse(null);
        if (sol == null) return false;
        if (!sol.getCliente().getId().equals(cliente.getId())) return false;
        if (sol.getStatus() == StatusSolicitacaoMentoria.PAGA) return true; // já processado
        processarPagamentoMarketplace(sol.getId());
        return true;
    }

    /**
     * Confirmação manual de pagamento pelo consultor (para dev local sem webhook).
     */
    @Transactional
    public void confirmarPagamentoManual(Long solicitacaoId, User consultor) {
        SolicitacaoMentoria sol = solicitacaoRepository.findById(solicitacaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitação não encontrada"));
        if (!sol.getEmpresa().getId().equals(consultor.getEmpresa().getId())) {
            throw new BusinessException("Solicitação não pertence à sua empresa.");
        }
        if (sol.getStatus() == StatusSolicitacaoMentoria.PAGA) {
            throw new BusinessException("Solicitação já está paga.");
        }
        if (sol.getStatus() != StatusSolicitacaoMentoria.ACEITA) {
            throw new BusinessException("Só é possível confirmar pagamento de solicitações aceitas.");
        }
        processarPagamentoMarketplace(sol.getId());
    }

    /**
     * Chamado pelo webhook checkout.session.completed para subscriptions marketplace.
     * Vincula o cliente e salva o subscriptionId.
     */
    @Transactional
    public void processarPagamentoMarketplaceComSubscription(Long solicitacaoId, String subscriptionId) {
        SolicitacaoMentoria sol = solicitacaoRepository.findById(solicitacaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitacao nao encontrada"));

        if (sol.getStatus() == StatusSolicitacaoMentoria.PAGA) {
            log.info("Solicitacao {} ja processada (PAGA). Ignorando.", solicitacaoId);
            return;
        }

        sol.setStatus(StatusSolicitacaoMentoria.PAGA);
        sol.setStripeSubscriptionId(subscriptionId);
        solicitacaoRepository.save(sol);

        User cliente = sol.getCliente();
        Empresa empresa = sol.getEmpresa();

        // Vincular cliente ao consultor
        cliente.setEmpresa(empresa);
        cliente.setOrigemVinculo("MARKETPLACE");
        cliente.setMarketplacePrecoCliente(sol.getPrecoFinal());
        cliente.setMarketplaceSubscriptionId(subscriptionId);
        cliente.setAutoGestao(false);
        cliente.setSubscriptionStatus("NONE");
        cliente.setCurrentPeriodEnd(null);
        cliente.setDataExclusao(null);

        // Obter periodo da subscription
        if (subscriptionId != null) {
            try {
                com.stripe.model.Subscription sub = com.stripe.model.Subscription.retrieve(subscriptionId);
                if (sub.getCurrentPeriodEnd() != null) {
                    cliente.setMarketplaceCurrentPeriodEnd(java.time.Instant.ofEpochSecond(sub.getCurrentPeriodEnd()));
                }
            } catch (Exception e) {
                log.warn("Erro ao obter periodo da subscription marketplace {}: {}", subscriptionId, e.getMessage());
            }
        }
        userRepository.save(cliente);

        // Registrar fatura marketplace
        faturaService.registrarFaturaMarketplace(cliente, empresa, sol.getPrecoFinal(), subscriptionId);

        // Email para ambos
        notificationAsyncRunner.enviarEmailMarketplacePagamentoConfirmadoAsync(
                cliente.getEmail(),
                cliente.getNome() != null ? cliente.getNome() : cliente.getEmail(),
                empresa.getNome(), sol.getPrecoFinal());

        log.info("Cliente {} vinculado ao consultor {} via marketplace subscription {}. Preco: {}",
                cliente.getId(), empresa.getId(), subscriptionId, sol.getPrecoFinal());
    }

    /**
     * Chamado pelo webhook subscription.deleted para desvincular cliente do marketplace.
     */
    @Transactional
    public void desvincularClientePorSubscriptionCancelada(User cliente) {
        String empresaNome = cliente.getEmpresa() != null ? cliente.getEmpresa().getNome() : "";
        cliente.setEmpresa(null);
        cliente.setOrigemVinculo(null);
        cliente.setMarketplacePrecoCliente(null);
        cliente.setMarketplaceSubscriptionId(null);
        cliente.setMarketplaceCurrentPeriodEnd(null);
        cliente.setDataExclusao(java.time.LocalDateTime.now());
        userRepository.save(cliente);

        notificationAsyncRunner.enviarEmailMarketplaceDesvinculacaoAsync(
                cliente.getEmail(),
                cliente.getNome() != null ? cliente.getNome() : cliente.getEmail(),
                empresaNome);

        log.info("Cliente {} desvinculado por cancelamento de subscription marketplace", cliente.getId());
    }

    // ─── Desvinculação marketplace ───────────────────────────────

    @Transactional
    public void desvincularClienteMarketplace(Long clienteId, User consultor) {
        Empresa empresa = getEmpresaDoConsultor(consultor);
        User cliente = userRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado"));

        if (cliente.getEmpresa() == null || !cliente.getEmpresa().getId().equals(empresa.getId())) {
            throw new BusinessException("Cliente não pertence à sua empresa.");
        }

        desvincularClienteMarketplaceInterno(cliente);
    }

    @Transactional
    public void desvincularClienteMarketplaceInterno(User cliente) {
        // Cancelar subscription no Stripe se existir
        if (cliente.getMarketplaceSubscriptionId() != null && !cliente.getMarketplaceSubscriptionId().isBlank()) {
            try {
                com.stripe.model.Subscription sub = com.stripe.model.Subscription.retrieve(cliente.getMarketplaceSubscriptionId());
                sub.cancel();
                log.info("Subscription marketplace {} cancelada para cliente {}", cliente.getMarketplaceSubscriptionId(), cliente.getId());
            } catch (Exception e) {
                log.warn("Erro ao cancelar subscription marketplace {}: {}", cliente.getMarketplaceSubscriptionId(), e.getMessage());
            }
        }

        String empresaNome = cliente.getEmpresa() != null ? cliente.getEmpresa().getNome() : "";
        cliente.setEmpresa(null);
        cliente.setOrigemVinculo(null);
        cliente.setMarketplacePrecoCliente(null);
        cliente.setMarketplaceSubscriptionId(null);
        cliente.setMarketplaceCurrentPeriodEnd(null);
        cliente.setDataExclusao(java.time.LocalDateTime.now());
        userRepository.save(cliente);

        // Email para o cliente
        notificationAsyncRunner.enviarEmailMarketplaceDesvinculacaoAsync(
                cliente.getEmail(),
                cliente.getNome() != null ? cliente.getNome() : cliente.getEmail(),
                empresaNome);
    }

    // ─── Consultor: clientes vindos do marketplace ───────────────

    public List<SolicitacaoMentoriaResponse> listarClientesMarketplace(Long empresaId) {
        return solicitacaoRepository.findByEmpresaIdOrderByCreatedAtDesc(empresaId).stream()
                .filter(s -> s.getStatus() == StatusSolicitacaoMentoria.PAGA)
                .map(this::toSolicitacaoResponse)
                .collect(Collectors.toList());
    }

    // ─── Admin Max ───────────────────────────────────────────────

    public List<MarketplacePerfilResponse> listarTodosConsultoresMarketplace() {
        BigDecimal taxa = configService.getTaxaMarketplace();
        return empresaRepository.findAll().stream()
                .filter(e -> e.getMarketplaceVisivel() != null)
                .map(e -> MarketplacePerfilResponse.builder()
                        .empresaId(e.getId())
                        .nome(e.getNome())
                        .marketplaceVisivel(e.getMarketplaceVisivel())
                        .marketplaceDescricao(e.getMarketplaceDescricao())
                        .marketplaceEspecializacao(e.getMarketplaceEspecializacao())
                        .marketplaceExperiencia(e.getMarketplaceExperiencia())
                        .marketplaceRedeSocial(e.getMarketplaceRedeSocial())
                        .marketplacePrecoBase(e.getMarketplacePrecoBase())
                        .taxaPlataforma(taxa)
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void adminAlterarVisibilidade(Long empresaId, Boolean visivel) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada"));
        empresa.setMarketplaceVisivel(visivel);
        empresaRepository.save(empresa);
    }

    @Transactional
    public void adminAtualizarPerfil(Long empresaId, MarketplacePerfilRequest request) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada"));
        if (request.getMarketplaceVisivel() != null) empresa.setMarketplaceVisivel(request.getMarketplaceVisivel());
        if (request.getMarketplaceDescricao() != null) empresa.setMarketplaceDescricao(request.getMarketplaceDescricao());
        if (request.getMarketplaceEspecializacao() != null) empresa.setMarketplaceEspecializacao(request.getMarketplaceEspecializacao());
        if (request.getMarketplaceExperiencia() != null) empresa.setMarketplaceExperiencia(request.getMarketplaceExperiencia());
        if (request.getMarketplaceRedeSocial() != null) empresa.setMarketplaceRedeSocial(request.getMarketplaceRedeSocial());
        if (request.getMarketplacePrecoBase() != null) empresa.setMarketplacePrecoBase(request.getMarketplacePrecoBase());
        empresaRepository.save(empresa);
    }

    @Transactional
    public void adminRemoverDoMarketplace(Long empresaId) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada"));
        empresa.setMarketplaceVisivel(false);
        empresa.setMarketplaceDescricao(null);
        empresa.setMarketplaceEspecializacao(null);
        empresa.setMarketplaceExperiencia(null);
        empresa.setMarketplaceRedeSocial(null);
        empresa.setMarketplacePrecoBase(null);
        empresaRepository.save(empresa);
    }

    public List<SolicitacaoMentoriaResponse> adminListarSolicitacoes(String statusFilter) {
        List<SolicitacaoMentoria> lista;
        if (statusFilter != null && !statusFilter.isBlank()) {
            try {
                StatusSolicitacaoMentoria status = StatusSolicitacaoMentoria.valueOf(statusFilter.toUpperCase());
                lista = solicitacaoRepository.findByStatus(status);
            } catch (IllegalArgumentException e) {
                lista = solicitacaoRepository.findAllByOrderByCreatedAtDesc();
            }
        } else {
            lista = solicitacaoRepository.findAllByOrderByCreatedAtDesc();
        }
        return lista.stream().map(this::toSolicitacaoResponse).collect(Collectors.toList());
    }

    @Transactional
    public void adminAlterarStatusSolicitacao(Long solicitacaoId, String novoStatus) {
        SolicitacaoMentoria sol = solicitacaoRepository.findById(solicitacaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitação não encontrada"));
        try {
            sol.setStatus(StatusSolicitacaoMentoria.valueOf(novoStatus.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Status inválido: " + novoStatus);
        }
        solicitacaoRepository.save(sol);
    }

    public Map<String, Object> getStatsMarketplace() {
        long totalConsultores = empresaRepository.findByMarketplaceVisivelTrueAndAtivoTrueOrderByNomeAsc().size();
        long solicitacoesPendentes = solicitacaoRepository.countByStatus(StatusSolicitacaoMentoria.PENDENTE);
        long solicitacoesPagas = solicitacaoRepository.countByStatus(StatusSolicitacaoMentoria.PAGA);
        BigDecimal taxa = configService.getTaxaMarketplace();

        // Calcular receita marketplace (soma dos preços finais das solicitações pagas)
        BigDecimal receitaBruta = solicitacaoRepository.findByStatus(StatusSolicitacaoMentoria.PAGA).stream()
                .map(SolicitacaoMentoria::getPrecoFinal)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal taxaAcumulada = receitaBruta.multiply(taxa).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);

        return Map.of(
                "totalConsultores", totalConsultores,
                "solicitacoesPendentes", solicitacoesPendentes,
                "solicitacoesPagas", solicitacoesPagas,
                "receitaBruta", receitaBruta,
                "taxaPercentual", taxa,
                "taxaAcumulada", taxaAcumulada
        );
    }

    // ─── Helpers ─────────────────────────────────────────────────

    private Empresa getEmpresaDoConsultor(User consultor) {
        if (consultor.getEmpresa() == null) {
            throw new BusinessException("Consultor sem empresa vinculada.");
        }
        return empresaRepository.findById(consultor.getEmpresa().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada"));
    }

    private MarketplaceConsultorResponse toConsultorResponse(Empresa e) {
        return MarketplaceConsultorResponse.builder()
                .empresaId(e.getId())
                .nome(e.getNome())
                .marketplaceDescricao(e.getMarketplaceDescricao())
                .marketplaceEspecializacao(e.getMarketplaceEspecializacao())
                .marketplaceExperiencia(e.getMarketplaceExperiencia())
                .marketplaceRedeSocial(e.getMarketplaceRedeSocial())
                .marketplacePrecoBase(e.getMarketplacePrecoBase())
                .build();
    }

    private SolicitacaoMentoriaResponse toSolicitacaoResponse(SolicitacaoMentoria s) {
        return SolicitacaoMentoriaResponse.builder()
                .id(s.getId())
                .clienteId(s.getCliente().getId())
                .clienteNome(s.getCliente().getNome())
                .clienteEmail(s.getCliente().getEmail())
                .empresaId(s.getEmpresa().getId())
                .empresaNome(s.getEmpresa().getNome())
                .status(s.getStatus().name())
                .precoProposto(s.getPrecoProposto())
                .precoFinal(s.getPrecoFinal())
                .mensagemCliente(s.getMensagemCliente())
                .mensagemConsultor(s.getMensagemConsultor())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}

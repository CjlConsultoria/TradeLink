package com.example.CJLInvestimentos.services;

import com.example.CJLInvestimentos.dtos.request.AlocacaoAlvoRequest;
import com.example.CJLInvestimentos.dtos.request.SalvarAlocacoesRequest;
import com.example.CJLInvestimentos.dtos.request.LoteRecomendacaoRequest;
import com.example.CJLInvestimentos.dtos.request.LoteRecomendacaoRequest.LoteRecomendacaoItem;
import com.example.CJLInvestimentos.dtos.response.*;
import com.example.CJLInvestimentos.dtos.response.AnaliseConsolidadaResponse.ClienteConsolidado;
import com.example.CJLInvestimentos.dtos.response.AnaliseConsolidadaResponse.Resumo;
import com.example.CJLInvestimentos.dtos.response.RebalanceamentoClienteResponse.AcaoRebalanceamento;
import com.example.CJLInvestimentos.dtos.response.RebalanceamentoClienteResponse.AtivoRebalanceamento;
import com.example.CJLInvestimentos.dtos.response.SaudeGeralResponse.CarteiraHealth;
import com.example.CJLInvestimentos.dtos.response.SaudeGeralResponse.ClienteHealth;
import com.example.CJLInvestimentos.entities.*;
import com.example.CJLInvestimentos.entities.enums.StatusRecomendacao;
import com.example.CJLInvestimentos.entities.enums.TipoRecomendacao;
import com.example.CJLInvestimentos.exceptions.BusinessException;
import com.example.CJLInvestimentos.exceptions.ResourceNotFoundException;
import com.example.CJLInvestimentos.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RebalanceamentoService {

    private final AlocacaoAlvoRepository alocacaoAlvoRepository;
    private final CarteiraRepository carteiraRepository;
    private final CarteiraClienteRepository carteiraClienteRepository;
    private final AtivoClienteRepository ativoClienteRepository;
    private final CotacaoRepository cotacaoRepository;
    private final RecomendacaoRepository recomendacaoRepository;
    private final NotificationAsyncRunner notificationAsyncRunner;

    private static final BigDecimal CEM = BigDecimal.valueOf(100);
    private static final BigDecimal MARGEM_PADRAO = BigDecimal.valueOf(5);

    @Transactional
    public List<AlocacaoAlvoResponse> salvarAlocacoes(Long carteiraId, SalvarAlocacoesRequest request, User consultor) {
        Carteira carteira = getCarteiraDoConsultor(carteiraId, consultor);

        BigDecimal total = request.getAlocacoes().stream()
                .map(AlocacaoAlvoRequest::getPercentualAlvo)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (total.compareTo(CEM) != 0) {
            throw new BusinessException("A soma dos percentuais deve ser exatamente 100%. Atual: " + total + "%");
        }

        alocacaoAlvoRepository.deleteByCarteiraId(carteiraId);

        if (request.getMargemErro() != null) {
            carteira.setMargemErro(request.getMargemErro());
        }
        if (request.getMoedaReferencia() != null) {
            carteira.setMoedaReferenciaRebalance(request.getMoedaReferencia());
        }
        carteira.setRebalanceAtivo(true);
        carteiraRepository.save(carteira);

        List<AlocacaoAlvo> alocacoes = new ArrayList<>();
        for (AlocacaoAlvoRequest req : request.getAlocacoes()) {
            AlocacaoAlvo alvo = AlocacaoAlvo.builder()
                    .carteira(carteira)
                    .simbolo(req.getSimbolo().trim().toUpperCase())
                    .nome(req.getNome().trim())
                    .percentualAlvo(req.getPercentualAlvo())
                    .parMoedaReferencia(req.getParMoedaReferencia() != null ? req.getParMoedaReferencia() : "USD")
                    .build();
            alocacoes.add(alvo);
        }
        alocacaoAlvoRepository.saveAll(alocacoes);

        return alocacoes.stream().map(this::toAlocacaoResponse).toList();
    }

    public List<AlocacaoAlvoResponse> listarAlocacoes(Long carteiraId, User consultor) {
        getCarteiraDoConsultor(carteiraId, consultor);
        return alocacaoAlvoRepository.findByCarteiraIdOrderByPercentualAlvoDesc(carteiraId)
                .stream().map(this::toAlocacaoResponse).toList();
    }

    public RebalanceamentoCarteiraResponse analisarCarteira(Long carteiraId, User consultor) {
        Carteira carteira = getCarteiraDoConsultor(carteiraId, consultor);
        List<AlocacaoAlvo> alocacoes = alocacaoAlvoRepository.findByCarteiraIdOrderByPercentualAlvoDesc(carteiraId);
        if (alocacoes.isEmpty()) {
            throw new BusinessException("Esta carteira ainda nao possui alocacao ideal definida. Va em 'Alocacao Ideal' e defina os percentuais antes de analisar.");
        }

        BigDecimal margem = carteira.getMargemErro() != null ? carteira.getMargemErro() : MARGEM_PADRAO;
        String moedaRef = carteira.getMoedaReferenciaRebalance() != null ? carteira.getMoedaReferenciaRebalance() : "USD";

        List<CarteiraCliente> carteiraClientes = carteiraClienteRepository.findByCarteiraId(carteiraId);
        List<RebalanceamentoClienteResponse> clientesResp = carteiraClientes.stream()
                .map(cc -> analisarClienteInterno(cc.getCliente(), alocacoes, margem, moedaRef))
                .toList();

        return RebalanceamentoCarteiraResponse.builder()
                .carteiraId(carteiraId)
                .carteiraNome(carteira.getNome())
                .margemErro(margem)
                .moedaReferencia(moedaRef)
                .alocacoesAlvo(alocacoes.stream().map(this::toAlocacaoResponse).toList())
                .clientes(clientesResp)
                .build();
    }

    public RebalanceamentoClienteResponse analisarCliente(Long carteiraId, Long clienteId, User consultor) {
        Carteira carteira = getCarteiraDoConsultor(carteiraId, consultor);
        List<AlocacaoAlvo> alocacoes = alocacaoAlvoRepository.findByCarteiraIdOrderByPercentualAlvoDesc(carteiraId);
        if (alocacoes.isEmpty()) {
            throw new BusinessException("Esta carteira ainda nao possui alocacao ideal definida. Defina os percentuais alvo antes de analisar o cliente.");
        }

        CarteiraCliente cc = carteiraClienteRepository.findByCarteiraIdAndClienteId(carteiraId, clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Este cliente nao esta vinculado a esta carteira. Verifique se ele foi adicionado corretamente."));

        BigDecimal margem = carteira.getMargemErro() != null ? carteira.getMargemErro() : MARGEM_PADRAO;
        String moedaRef = carteira.getMoedaReferenciaRebalance() != null ? carteira.getMoedaReferenciaRebalance() : "USD";

        return analisarClienteInterno(cc.getCliente(), alocacoes, margem, moedaRef);
    }

    @Transactional
    public List<RecomendacaoResponse> gerarRecomendacoes(Long carteiraId, User consultor) {
        Carteira carteira = getCarteiraDoConsultor(carteiraId, consultor);
        List<AlocacaoAlvo> alocacoes = alocacaoAlvoRepository.findByCarteiraIdOrderByPercentualAlvoDesc(carteiraId);
        if (alocacoes.isEmpty()) {
            throw new BusinessException("Nao e possivel gerar recomendacoes sem alocacao ideal. Defina os percentuais alvo na carteira primeiro.");
        }

        BigDecimal margem = carteira.getMargemErro() != null ? carteira.getMargemErro() : MARGEM_PADRAO;
        String moedaRef = carteira.getMoedaReferenciaRebalance() != null ? carteira.getMoedaReferenciaRebalance() : "USD";

        // Analisar um cliente representativo ou a carteira em geral
        // Gerar recomendacoes baseado nos ativos fora da faixa
        Set<String> jaGerados = new HashSet<>();
        List<Recomendacao> recomendacoesCriadas = new ArrayList<>();

        List<CarteiraCliente> clientes = carteiraClienteRepository.findByCarteiraId(carteiraId);
        for (CarteiraCliente cc : clientes) {
            RebalanceamentoClienteResponse analise = analisarClienteInterno(cc.getCliente(), alocacoes, margem, moedaRef);
            for (AcaoRebalanceamento acao : analise.getAcoesSugeridas()) {
                String chave = acao.getTipo() + "_" + acao.getSimbolo();
                if (jaGerados.contains(chave)) continue;
                jaGerados.add(chave);

                TipoRecomendacao tipo = "COMPRA".equals(acao.getTipo()) ? TipoRecomendacao.COMPRA : TipoRecomendacao.VENDA;

                // Verificar se ja existe recomendacao ATIVA para este ativo/tipo/carteira
                if (recomendacaoRepository.existsByCarteiraIdAndMoedaAndTipoAndStatus(
                        carteiraId, acao.getSimbolo(), tipo, StatusRecomendacao.ATIVA)) {
                    continue;
                }

                BigDecimal preco = resolverPreco(acao.getSimbolo(), moedaRef);

                // Buscar percentual alvo do ativo na alocacao ideal
                BigDecimal percentualAlvo = alocacoes.stream()
                        .filter(a -> a.getSimbolo().equalsIgnoreCase(acao.getSimbolo()))
                        .map(AlocacaoAlvo::getPercentualAlvo)
                        .findFirst().orElse(null);

                Recomendacao rec = Recomendacao.builder()
                        .carteira(carteira)
                        .tipo(tipo)
                        .moeda(acao.getSimbolo())
                        .parMoeda(moedaRef)
                        .precoEntrada(preco)
                        .precoAlvo(preco)
                        .quantidade(acao.getQuantidade())
                        .percentual(percentualAlvo)
                        .modoPercentual(false)
                        .observacao("Rebalanceamento automatico - " + acao.getDescricao())
                        .build();
                recomendacoesCriadas.add(rec);
            }
        }

        if (recomendacoesCriadas.isEmpty()) {
            throw new BusinessException("Nenhuma recomendacao nova foi criada. Todos os clientes ja estao balanceados ou ja possuem recomendacoes ativas para os ativos necessarios.");
        }

        recomendacaoRepository.saveAll(recomendacoesCriadas);

        // Notificar consultor sobre recomendacoes geradas
        try {
            int totalClientes = clientes.size();
            notificationAsyncRunner.notificarRecomendacoesGeradasAsync(
                    carteiraId, recomendacoesCriadas.size(), totalClientes);
        } catch (Exception ignored) {}

        return recomendacoesCriadas.stream()
                .map(r -> RecomendacaoResponse.builder()
                        .id(r.getId())
                        .carteiraId(carteiraId)
                        .carteiraNome(carteira.getNome())
                        .tipo(r.getTipo())
                        .moeda(r.getMoeda())
                        .parMoeda(r.getParMoeda())
                        .precoEntrada(r.getPrecoEntrada())
                        .precoAlvo(r.getPrecoAlvo())
                        .quantidade(r.getQuantidade())
                        .percentual(r.getPercentual())
                        .modoPercentual(r.getModoPercentual())
                        .observacao(r.getObservacao())
                        .status(r.getStatus())
                        .createdAt(r.getCreatedAt())
                        .build())
                .toList();
    }

    public SaudeGeralResponse saudeGeral(User consultor) {
        List<Carteira> carteiras = carteiraRepository.findByConsultorId(consultor.getId());
        List<CarteiraHealth> carteiraHealths = new ArrayList<>();

        for (Carteira carteira : carteiras) {
            List<AlocacaoAlvo> alocacoes = alocacaoAlvoRepository.findByCarteiraIdOrderByPercentualAlvoDesc(carteira.getId());
            boolean temAlocacoes = !alocacoes.isEmpty();

            List<ClienteHealth> clienteHealths = new ArrayList<>();
            if (temAlocacoes) {
                BigDecimal margem = carteira.getMargemErro() != null ? carteira.getMargemErro() : MARGEM_PADRAO;
                String moedaRef = carteira.getMoedaReferenciaRebalance() != null ? carteira.getMoedaReferenciaRebalance() : "USD";

                List<CarteiraCliente> clientes = carteiraClienteRepository.findByCarteiraId(carteira.getId());
                for (CarteiraCliente cc : clientes) {
                    RebalanceamentoClienteResponse analise = analisarClienteInterno(cc.getCliente(), alocacoes, margem, moedaRef);
                    int desbalanceados = (int) analise.getAtivos().stream()
                            .filter(a -> Boolean.TRUE.equals(a.getComprar()) || Boolean.TRUE.equals(a.getVender()))
                            .count();
                    BigDecimal maiorDesvio = analise.getAtivos().stream()
                            .map(a -> a.getDiferencaPercentual() != null ? a.getDiferencaPercentual().abs() : BigDecimal.ZERO)
                            .max(Comparator.naturalOrder())
                            .orElse(BigDecimal.ZERO);

                    clienteHealths.add(ClienteHealth.builder()
                            .clienteId(analise.getClienteId())
                            .clienteNome(analise.getClienteNome())
                            .valorTotal(analise.getValorTotalPortfolio())
                            .status(analise.getStatusSaude())
                            .totalDesbalanceados(desbalanceados)
                            .maiorDesvio(maiorDesvio)
                            .build());
                }
            }

            carteiraHealths.add(CarteiraHealth.builder()
                    .carteiraId(carteira.getId())
                    .carteiraNome(carteira.getNome())
                    .temAlocacoes(temAlocacoes)
                    .clientes(clienteHealths)
                    .build());
        }

        return SaudeGeralResponse.builder().carteiras(carteiraHealths).build();
    }

    // === Analise Consolidada (todas as carteiras) ===

    public AnaliseConsolidadaResponse analiseConsolidada(User consultor) {
        List<Carteira> carteiras = carteiraRepository.findByConsultorId(consultor.getId());
        List<ClienteConsolidado> todosClientes = new ArrayList<>();

        int totalDesbalanceados = 0;
        int totalCriticos = 0;
        BigDecimal valorTotalGeral = BigDecimal.ZERO;

        for (Carteira carteira : carteiras) {
            if (!Boolean.TRUE.equals(carteira.getAtiva())) continue;
            List<AlocacaoAlvo> alocacoes = alocacaoAlvoRepository.findByCarteiraIdOrderByPercentualAlvoDesc(carteira.getId());
            if (alocacoes.isEmpty()) continue;

            BigDecimal margem = carteira.getMargemErro() != null ? carteira.getMargemErro() : MARGEM_PADRAO;
            String moedaRef = carteira.getMoedaReferenciaRebalance() != null ? carteira.getMoedaReferenciaRebalance() : "USD";

            List<CarteiraCliente> clientes = carteiraClienteRepository.findByCarteiraId(carteira.getId());
            for (CarteiraCliente cc : clientes) {
                RebalanceamentoClienteResponse analise = analisarClienteInterno(cc.getCliente(), alocacoes, margem, moedaRef);

                todosClientes.add(ClienteConsolidado.builder()
                        .clienteId(analise.getClienteId())
                        .clienteNome(analise.getClienteNome())
                        .carteiraId(carteira.getId())
                        .carteiraNome(carteira.getNome())
                        .valorTotalPortfolio(analise.getValorTotalPortfolio())
                        .moedaReferencia(moedaRef)
                        .statusSaude(analise.getStatusSaude())
                        .ativos(analise.getAtivos())
                        .acoesSugeridas(analise.getAcoesSugeridas())
                        .build());

                valorTotalGeral = valorTotalGeral.add(analise.getValorTotalPortfolio() != null ? analise.getValorTotalPortfolio() : BigDecimal.ZERO);
                if (!"OK".equals(analise.getStatusSaude())) totalDesbalanceados++;
                if ("CRITICO".equals(analise.getStatusSaude())) {
                    totalCriticos++;
                    // Notificar consultor sobre portfolio critico
                    try {
                        long desvios = analise.getAtivos().stream()
                                .filter(a -> Boolean.TRUE.equals(a.getComprar()) || Boolean.TRUE.equals(a.getVender()))
                                .count();
                        String maiorDesvio = analise.getAtivos().stream()
                                .filter(a -> a.getDiferencaPercentual() != null)
                                .max(java.util.Comparator.comparing(a -> a.getDiferencaPercentual().abs()))
                                .map(a -> a.getSimbolo() + " (" + a.getDiferencaPercentual() + "%)")
                                .orElse("-");
                        java.util.List<String[]> ativosList = analise.getAtivos().stream()
                                .filter(a -> Boolean.TRUE.equals(a.getComprar()) || Boolean.TRUE.equals(a.getVender()))
                                .map(a -> new String[]{a.getSimbolo(),
                                        a.getPercentualAtual() + "%",
                                        a.getPercentualAlvo() + "%",
                                        a.getDiferencaPercentual() + "%"})
                                .collect(Collectors.toList());
                        notificationAsyncRunner.notificarPortfolioCriticoAsync(
                                carteira.getId(), cc.getCliente().getId(),
                                analise.getClienteNome(), (int) desvios, maiorDesvio, ativosList);
                    } catch (Exception ignored) {}
                }
            }
        }

        return AnaliseConsolidadaResponse.builder()
                .clientes(todosClientes)
                .resumo(Resumo.builder()
                        .totalClientes(todosClientes.size())
                        .totalDesbalanceados(totalDesbalanceados)
                        .totalCriticos(totalCriticos)
                        .valorTotalGeral(valorTotalGeral)
                        .build())
                .build();
    }

    @Transactional
    public Map<String, Object> gerarRecomendacoesLote(LoteRecomendacaoRequest request, User consultor) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BusinessException("Selecione pelo menos um cliente para gerar recomendacoes.");
        }

        // Agrupar por carteira
        Map<Long, List<Long>> porCarteira = new HashMap<>();
        for (LoteRecomendacaoItem item : request.getItems()) {
            porCarteira.computeIfAbsent(item.getCarteiraId(), k -> new ArrayList<>()).add(item.getClienteId());
        }

        List<Recomendacao> recomendacoesCriadas = new ArrayList<>();

        for (Map.Entry<Long, List<Long>> entry : porCarteira.entrySet()) {
            Long carteiraId = entry.getKey();
            List<Long> clienteIds = entry.getValue();

            Carteira carteira = getCarteiraDoConsultor(carteiraId, consultor);
            List<AlocacaoAlvo> alocacoes = alocacaoAlvoRepository.findByCarteiraIdOrderByPercentualAlvoDesc(carteiraId);
            if (alocacoes.isEmpty()) continue;

            BigDecimal margem = carteira.getMargemErro() != null ? carteira.getMargemErro() : MARGEM_PADRAO;
            String moedaRef = carteira.getMoedaReferenciaRebalance() != null ? carteira.getMoedaReferenciaRebalance() : "USD";

            Set<String> jaGerados = new HashSet<>();

            for (Long clienteId : clienteIds) {
                Optional<CarteiraCliente> ccOpt = carteiraClienteRepository.findByCarteiraIdAndClienteId(carteiraId, clienteId);
                if (ccOpt.isEmpty()) continue;

                RebalanceamentoClienteResponse analise = analisarClienteInterno(ccOpt.get().getCliente(), alocacoes, margem, moedaRef);
                for (AcaoRebalanceamento acao : analise.getAcoesSugeridas()) {
                    String chave = acao.getTipo() + "_" + acao.getSimbolo();
                    if (jaGerados.contains(chave)) continue;
                    jaGerados.add(chave);

                    TipoRecomendacao tipo = "COMPRA".equals(acao.getTipo()) ? TipoRecomendacao.COMPRA : TipoRecomendacao.VENDA;

                    // Verificar se ja existe recomendacao ATIVA para este ativo/tipo/carteira
                    if (recomendacaoRepository.existsByCarteiraIdAndMoedaAndTipoAndStatus(
                            carteiraId, acao.getSimbolo(), tipo, StatusRecomendacao.ATIVA)) {
                        continue;
                    }

                    BigDecimal preco = resolverPreco(acao.getSimbolo(), moedaRef);

                    // Buscar percentual alvo do ativo na alocacao ideal
                    BigDecimal percentualAlvo = alocacoes.stream()
                            .filter(a -> a.getSimbolo().equalsIgnoreCase(acao.getSimbolo()))
                            .map(AlocacaoAlvo::getPercentualAlvo)
                            .findFirst().orElse(null);

                    Recomendacao rec = Recomendacao.builder()
                            .carteira(carteira)
                            .tipo(tipo)
                            .moeda(acao.getSimbolo())
                            .parMoeda(moedaRef)
                            .precoEntrada(preco)
                            .precoAlvo(preco)
                            .quantidade(acao.getQuantidade())
                            .percentual(percentualAlvo)
                            .modoPercentual(false)
                            .observacao("Rebalanceamento em lote - " + acao.getDescricao())
                            .build();
                    recomendacoesCriadas.add(rec);
                }
            }
        }

        if (recomendacoesCriadas.isEmpty()) {
            throw new BusinessException("Nenhuma recomendacao nova foi criada. Todos os clientes ja estao balanceados ou ja possuem recomendacoes ativas para os ativos necessarios.");
        }

        recomendacaoRepository.saveAll(recomendacoesCriadas);

        // Notificar consultor sobre recomendacoes geradas em lote
        try {
            int totalClientesLote = request.getItems().size();
            Long primeiraCarteiraId = request.getItems().get(0).getCarteiraId();
            notificationAsyncRunner.notificarRecomendacoesGeradasAsync(
                    primeiraCarteiraId, recomendacoesCriadas.size(), totalClientesLote);
        } catch (Exception ignored) {}

        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("totalRecomendacoes", recomendacoesCriadas.size());
        resultado.put("mensagem", recomendacoesCriadas.size() + " recomendacoes geradas com sucesso.");
        return resultado;
    }

    // === Analise do cliente para o ClienteController ===

    public RebalanceamentoClienteResponse analisarClienteParaCliente(Long carteiraId, User cliente) {
        Carteira carteira = carteiraRepository.findById(carteiraId)
                .orElseThrow(() -> new ResourceNotFoundException("Carteira nao encontrada. Ela pode ter sido excluida."));
        if (!carteiraClienteRepository.existsByCarteiraIdAndClienteId(carteiraId, cliente.getId())) {
            throw new AccessDeniedException("Sem acesso a esta carteira.");
        }
        List<AlocacaoAlvo> alocacoes = alocacaoAlvoRepository.findByCarteiraIdOrderByPercentualAlvoDesc(carteiraId);
        if (alocacoes.isEmpty()) {
            return null;
        }
        BigDecimal margem = carteira.getMargemErro() != null ? carteira.getMargemErro() : MARGEM_PADRAO;
        String moedaRef = carteira.getMoedaReferenciaRebalance() != null ? carteira.getMoedaReferenciaRebalance() : "USD";
        return analisarClienteInterno(cliente, alocacoes, margem, moedaRef);
    }

    // === Helpers ===

    private RebalanceamentoClienteResponse analisarClienteInterno(User cliente, List<AlocacaoAlvo> alocacoes, BigDecimal margem, String moedaRef) {
        List<AtivoCliente> ativos = ativoClienteRepository.findByClienteIdOrderBySimboloAsc(cliente.getId());

        // Mapear ativos do cliente por simbolo
        Map<String, AtivoCliente> ativoMap = ativos.stream()
                .collect(Collectors.toMap(a -> a.getSimbolo().toUpperCase(), a -> a, (a, b) -> a));

        // Calcular valor total do portfolio na moeda de referencia
        BigDecimal totalPortfolio = BigDecimal.ZERO;
        Map<String, BigDecimal> valorPorAtivo = new HashMap<>();
        Map<String, BigDecimal> precoPorAtivo = new HashMap<>();

        for (AlocacaoAlvo alvo : alocacoes) {
            String sim = alvo.getSimbolo().toUpperCase();
            AtivoCliente ativo = ativoMap.get(sim);
            BigDecimal quantidade = ativo != null ? ativo.getQuantidade() : BigDecimal.ZERO;
            BigDecimal preco = resolverPrecoParaAtivo(ativo, sim, moedaRef);
            precoPorAtivo.put(sim, preco);

            BigDecimal valor = BigDecimal.ZERO;
            if (preco != null && quantidade.compareTo(BigDecimal.ZERO) > 0) {
                valor = quantidade.multiply(preco);
            }
            valorPorAtivo.put(sim, valor);
            totalPortfolio = totalPortfolio.add(valor);
        }

        // Incluir ativos do cliente que nao estao nas alocacoes (extras)
        for (AtivoCliente ativo : ativos) {
            String sim = ativo.getSimbolo().toUpperCase();
            if (!valorPorAtivo.containsKey(sim)) {
                BigDecimal preco = resolverPrecoParaAtivo(ativo, sim, moedaRef);
                precoPorAtivo.put(sim, preco);
                BigDecimal valor = BigDecimal.ZERO;
                if (preco != null && ativo.getQuantidade().compareTo(BigDecimal.ZERO) > 0) {
                    valor = ativo.getQuantidade().multiply(preco);
                }
                valorPorAtivo.put(sim, valor);
                totalPortfolio = totalPortfolio.add(valor);
            }
        }

        // Construir lista de ativos com analise
        List<AtivoRebalanceamento> ativosRebal = new ArrayList<>();
        List<AcaoRebalanceamento> acoes = new ArrayList<>();
        BigDecimal margemFator = margem.divide(CEM, 4, RoundingMode.HALF_UP);

        for (AlocacaoAlvo alvo : alocacoes) {
            String sim = alvo.getSimbolo().toUpperCase();
            AtivoCliente ativo = ativoMap.get(sim);
            BigDecimal quantidade = ativo != null ? ativo.getQuantidade() : BigDecimal.ZERO;
            BigDecimal preco = precoPorAtivo.get(sim);
            BigDecimal valor = valorPorAtivo.get(sim);
            BigDecimal pctAtual = totalPortfolio.compareTo(BigDecimal.ZERO) > 0
                    ? valor.multiply(CEM).divide(totalPortfolio, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            BigDecimal pctAlvo = alvo.getPercentualAlvo();
            BigDecimal diff = pctAtual.subtract(pctAlvo);

            boolean comprar = pctAtual.compareTo(pctAlvo.multiply(BigDecimal.ONE.subtract(margemFator))) < 0;
            boolean vender = pctAtual.compareTo(pctAlvo.multiply(BigDecimal.ONE.add(margemFator))) > 0;

            ativosRebal.add(AtivoRebalanceamento.builder()
                    .simbolo(sim)
                    .nome(alvo.getNome())
                    .quantidade(quantidade)
                    .precoAtual(preco)
                    .valorUsd(valor)
                    .percentualAtual(pctAtual)
                    .percentualAlvo(pctAlvo)
                    .diferencaPercentual(diff)
                    .comprar(comprar)
                    .vender(vender)
                    .build());

            if ((comprar || vender) && totalPortfolio.compareTo(BigDecimal.ZERO) > 0 && preco != null && preco.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal diferencaUsd = totalPortfolio.multiply(pctAlvo.subtract(pctAtual)).divide(CEM, 8, RoundingMode.HALF_UP);
                BigDecimal qtdAcao = diferencaUsd.divide(preco, 8, RoundingMode.HALF_UP).abs();
                String tipo = comprar ? "COMPRA" : "VENDA";
                String desc = (comprar ? "Comprar " : "Vender ") + formatQtd(qtdAcao) + " " + sim;

                acoes.add(AcaoRebalanceamento.builder()
                        .tipo(tipo)
                        .simbolo(sim)
                        .quantidade(qtdAcao)
                        .valorUsd(diferencaUsd.abs())
                        .descricao(desc)
                        .build());
            }
        }

        String status = calcularStatusSaude(ativosRebal);

        return RebalanceamentoClienteResponse.builder()
                .clienteId(cliente.getId())
                .clienteNome(cliente.getNome())
                .valorTotalPortfolio(totalPortfolio)
                .moedaReferencia(moedaRef)
                .statusSaude(status)
                .ativos(ativosRebal)
                .acoesSugeridas(acoes)
                .build();
    }

    private String calcularStatusSaude(List<AtivoRebalanceamento> ativos) {
        long foraFaixa = ativos.stream()
                .filter(a -> Boolean.TRUE.equals(a.getComprar()) || Boolean.TRUE.equals(a.getVender()))
                .count();
        boolean desvioGrande = ativos.stream()
                .anyMatch(a -> a.getDiferencaPercentual() != null && a.getDiferencaPercentual().abs().compareTo(BigDecimal.valueOf(15)) > 0);

        if (foraFaixa == 0) return "OK";
        if (foraFaixa >= 3 || desvioGrande) return "CRITICO";
        return "ATENCAO";
    }

    private BigDecimal resolverPrecoParaAtivo(AtivoCliente ativo, String simbolo, String moedaRef) {
        // Para USD em carteira USD, preco = 1
        if (simbolo.equalsIgnoreCase(moedaRef)) {
            return BigDecimal.ONE;
        }
        // Tentar cotacao
        Optional<Cotacao> cotacao = cotacaoRepository.findTopByMoedaAndParMoedaOrderByDataHoraDesc(simbolo, moedaRef);
        if (cotacao.isPresent()) {
            return cotacao.get().getPrecoCompra();
        }
        // Fallback para preco manual do ativo
        if (ativo != null && ativo.getPrecoManual() != null) {
            return ativo.getPrecoManual();
        }
        return null;
    }

    private BigDecimal resolverPreco(String simbolo, String moedaRef) {
        if (simbolo.equalsIgnoreCase(moedaRef)) return BigDecimal.ONE;
        return cotacaoRepository.findTopByMoedaAndParMoedaOrderByDataHoraDesc(simbolo, moedaRef)
                .map(Cotacao::getPrecoCompra)
                .orElse(null);
    }

    private Carteira getCarteiraDoConsultor(Long carteiraId, User consultor) {
        Carteira carteira = carteiraRepository.findById(carteiraId)
                .orElseThrow(() -> new ResourceNotFoundException("Carteira nao encontrada. Ela pode ter sido excluida."));
        if (!carteira.getConsultor().getId().equals(consultor.getId())) {
            throw new AccessDeniedException("Sem acesso a esta carteira.");
        }
        return carteira;
    }

    private AlocacaoAlvoResponse toAlocacaoResponse(AlocacaoAlvo alvo) {
        return AlocacaoAlvoResponse.builder()
                .id(alvo.getId())
                .simbolo(alvo.getSimbolo())
                .nome(alvo.getNome())
                .percentualAlvo(alvo.getPercentualAlvo())
                .parMoedaReferencia(alvo.getParMoedaReferencia())
                .build();
    }

    private String formatQtd(BigDecimal v) {
        if (v.compareTo(BigDecimal.ONE) < 0) {
            return v.stripTrailingZeros().toPlainString();
        }
        return v.setScale(4, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }

    // === Impacto de recomendacao por cliente ===

    public RecomendacaoImpactoResponse impactoRecomendacao(Long recomendacaoId, User consultor) {
        Recomendacao rec = recomendacaoRepository.findById(recomendacaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Recomendacao nao encontrada."));
        Carteira carteira = rec.getCarteira();
        if (!carteira.getConsultor().getId().equals(consultor.getId())) {
            throw new AccessDeniedException("Sem acesso a esta recomendacao.");
        }

        List<AlocacaoAlvo> alocacoes = alocacaoAlvoRepository.findByCarteiraIdOrderByPercentualAlvoDesc(carteira.getId());
        BigDecimal margem = carteira.getMargemErro() != null ? carteira.getMargemErro() : MARGEM_PADRAO;
        String moedaRef = carteira.getMoedaReferenciaRebalance() != null ? carteira.getMoedaReferenciaRebalance() : "USD";

        List<CarteiraCliente> clientesDaCarteira = carteiraClienteRepository.findByCarteiraId(carteira.getId());

        BigDecimal precoAtivo = resolverPreco(rec.getMoeda(), moedaRef);

        List<RecomendacaoImpactoResponse.ClienteImpacto> impactos = new ArrayList<>();

        for (CarteiraCliente cc : clientesDaCarteira) {
            User cliente = cc.getCliente();
            if (cliente == null || !Boolean.TRUE.equals(cliente.getAtivo())) continue;

            // ---- Estado ANTES ----
            RebalanceamentoClienteResponse analiseAntes = null;
            if (!alocacoes.isEmpty()) {
                analiseAntes = analisarClienteInterno(cliente, alocacoes, margem, moedaRef);
            }

            // Determinar quantidade que este cliente especificamente deveria executar
            BigDecimal qtdCliente = BigDecimal.ZERO;
            if (analiseAntes != null) {
                for (AcaoRebalanceamento acao : analiseAntes.getAcoesSugeridas()) {
                    if (acao.getSimbolo().equalsIgnoreCase(rec.getMoeda())
                            && acao.getTipo().equalsIgnoreCase(rec.getTipo().name())) {
                        qtdCliente = acao.getQuantidade() != null ? acao.getQuantidade() : BigDecimal.ZERO;
                        break;
                    }
                }
            }

            BigDecimal valorEstimado = (precoAtivo != null && qtdCliente.compareTo(BigDecimal.ZERO) > 0)
                    ? qtdCliente.multiply(precoAtivo).setScale(2, RoundingMode.HALF_UP)
                    : null;

            // ---- Simular estado DEPOIS ----
            // Para simular, ajustar os ativos do cliente como se a operacao tivesse sido executada
            List<RecomendacaoImpactoResponse.AtivoImpacto> ativosAntes = new ArrayList<>();
            List<RecomendacaoImpactoResponse.AtivoImpacto> ativosDepois = new ArrayList<>();
            String statusAntes = analiseAntes != null ? analiseAntes.getStatusSaude() : "OK";
            String statusDepois = statusAntes;

            if (analiseAntes != null && !analiseAntes.getAtivos().isEmpty()) {
                BigDecimal totalPortfolio = analiseAntes.getValorTotalPortfolio() != null ? analiseAntes.getValorTotalPortfolio() : BigDecimal.ZERO;

                // Calcular ajuste de valor na simulacao
                BigDecimal ajusteValor = BigDecimal.ZERO;
                if (precoAtivo != null && qtdCliente.compareTo(BigDecimal.ZERO) > 0) {
                    ajusteValor = qtdCliente.multiply(precoAtivo);
                    if ("VENDA".equals(rec.getTipo().name())) {
                        ajusteValor = ajusteValor.negate();
                    }
                }

                BigDecimal totalPortfolioDepois = totalPortfolio; // Compra/venda nao muda total (troca entre ativos)

                List<AtivoRebalanceamento> simAtivos = new ArrayList<>();

                for (AtivoRebalanceamento at : analiseAntes.getAtivos()) {
                    ativosAntes.add(RecomendacaoImpactoResponse.AtivoImpacto.builder()
                            .simbolo(at.getSimbolo())
                            .quantidade(at.getQuantidade())
                            .valorUsd(at.getValorUsd())
                            .percentualAtual(at.getPercentualAtual())
                            .percentualAlvo(at.getPercentualAlvo())
                            .diferencaPercentual(at.getDiferencaPercentual())
                            .comprar(at.getComprar())
                            .vender(at.getVender())
                            .build());

                    // Simular
                    BigDecimal simQtd = at.getQuantidade();
                    BigDecimal simValor = at.getValorUsd();
                    if (at.getSimbolo().equalsIgnoreCase(rec.getMoeda())) {
                        if ("COMPRA".equals(rec.getTipo().name())) {
                            simQtd = simQtd.add(qtdCliente);
                            if (precoAtivo != null) simValor = simValor.add(qtdCliente.multiply(precoAtivo));
                        } else {
                            simQtd = simQtd.subtract(qtdCliente);
                            if (precoAtivo != null) simValor = simValor.subtract(qtdCliente.multiply(precoAtivo));
                            if (simQtd.compareTo(BigDecimal.ZERO) < 0) simQtd = BigDecimal.ZERO;
                            if (simValor.compareTo(BigDecimal.ZERO) < 0) simValor = BigDecimal.ZERO;
                        }
                    }
                    // Contrapartida: moeda de referencia (ex: USD) ajusta inversamente
                    if (at.getSimbolo().equalsIgnoreCase(moedaRef) && !rec.getMoeda().equalsIgnoreCase(moedaRef)) {
                        if ("COMPRA".equals(rec.getTipo().name()) && precoAtivo != null) {
                            simValor = simValor.subtract(qtdCliente.multiply(precoAtivo));
                            simQtd = simValor; // Para moeda ref, qtd = valor
                        } else if ("VENDA".equals(rec.getTipo().name()) && precoAtivo != null) {
                            simValor = simValor.add(qtdCliente.multiply(precoAtivo));
                            simQtd = simValor;
                        }
                        if (simQtd.compareTo(BigDecimal.ZERO) < 0) simQtd = BigDecimal.ZERO;
                        if (simValor.compareTo(BigDecimal.ZERO) < 0) simValor = BigDecimal.ZERO;
                    }

                    BigDecimal simPctAtual = totalPortfolioDepois.compareTo(BigDecimal.ZERO) > 0
                            ? simValor.multiply(CEM).divide(totalPortfolioDepois, 2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;
                    BigDecimal pctAlvo = at.getPercentualAlvo() != null ? at.getPercentualAlvo() : BigDecimal.ZERO;
                    BigDecimal simDiff = simPctAtual.subtract(pctAlvo);
                    BigDecimal limInf = pctAlvo.multiply(CEM.subtract(margem)).divide(CEM, 2, RoundingMode.HALF_UP);
                    BigDecimal limSup = pctAlvo.multiply(CEM.add(margem)).divide(CEM, 2, RoundingMode.HALF_UP);
                    boolean simComprar = simPctAtual.compareTo(limInf) < 0;
                    boolean simVender = simPctAtual.compareTo(limSup) > 0;

                    ativosDepois.add(RecomendacaoImpactoResponse.AtivoImpacto.builder()
                            .simbolo(at.getSimbolo())
                            .quantidade(simQtd.setScale(8, RoundingMode.HALF_UP))
                            .valorUsd(simValor.setScale(2, RoundingMode.HALF_UP))
                            .percentualAtual(simPctAtual)
                            .percentualAlvo(pctAlvo)
                            .diferencaPercentual(simDiff.setScale(2, RoundingMode.HALF_UP))
                            .comprar(simComprar)
                            .vender(simVender)
                            .build());

                    simAtivos.add(AtivoRebalanceamento.builder()
                            .simbolo(at.getSimbolo())
                            .percentualAtual(simPctAtual)
                            .percentualAlvo(pctAlvo)
                            .diferencaPercentual(simDiff)
                            .comprar(simComprar)
                            .vender(simVender)
                            .build());
                }

                statusDepois = calcularStatusSaude(simAtivos);
            }

            impactos.add(RecomendacaoImpactoResponse.ClienteImpacto.builder()
                    .clienteId(cliente.getId())
                    .clienteNome(cliente.getNome() != null ? cliente.getNome() : cliente.getEmail())
                    .valorTotalPortfolio(analiseAntes != null ? analiseAntes.getValorTotalPortfolio() : BigDecimal.ZERO)
                    .statusAntes(statusAntes)
                    .statusDepois(statusDepois)
                    .ativosAntes(ativosAntes)
                    .ativosDepois(ativosDepois)
                    .quantidadeCliente(qtdCliente)
                    .valorEstimado(valorEstimado)
                    .build());
        }

        // Buscar percentual alvo para o ativo da recomendacao
        BigDecimal pctAlvo = alocacoes.stream()
                .filter(a -> a.getSimbolo().equalsIgnoreCase(rec.getMoeda()))
                .map(AlocacaoAlvo::getPercentualAlvo)
                .findFirst().orElse(rec.getPercentual());

        return RecomendacaoImpactoResponse.builder()
                .recomendacaoId(rec.getId())
                .tipo(rec.getTipo().name())
                .moeda(rec.getMoeda())
                .parMoeda(rec.getParMoeda())
                .precoEntrada(rec.getPrecoEntrada())
                .precoAlvo(rec.getPrecoAlvo())
                .quantidade(rec.getQuantidade())
                .percentualAlvo(pctAlvo)
                .observacao(rec.getObservacao())
                .carteiraNome(carteira.getNome())
                .margemErro(margem)
                .moedaReferencia(moedaRef)
                .clientes(impactos)
                .build();
    }
}

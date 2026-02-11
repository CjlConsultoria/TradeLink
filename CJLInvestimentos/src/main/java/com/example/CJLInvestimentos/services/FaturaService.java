package com.example.CJLInvestimentos.services;

import com.example.CJLInvestimentos.dtos.request.AtualizarFaturaRequest;
import com.example.CJLInvestimentos.dtos.request.CriarFaturaRequest;
import com.example.CJLInvestimentos.dtos.request.MarcarPagoRequest;
import com.example.CJLInvestimentos.dtos.response.FaturaResponse;
import com.example.CJLInvestimentos.dtos.response.FaturasComProximaResponse;
import com.example.CJLInvestimentos.dtos.response.ProximaFaturaResponse;
import com.example.CJLInvestimentos.entities.Empresa;
import com.example.CJLInvestimentos.entities.Fatura;
import com.example.CJLInvestimentos.entities.Plano;
import com.example.CJLInvestimentos.entities.enums.FormaPagamento;
import com.example.CJLInvestimentos.entities.enums.StatusFatura;
import com.example.CJLInvestimentos.entities.enums.SubscriptionStatus;
import com.example.CJLInvestimentos.exceptions.BusinessException;
import com.example.CJLInvestimentos.exceptions.ResourceNotFoundException;
import com.example.CJLInvestimentos.entities.User;
import com.example.CJLInvestimentos.repositories.EmpresaRepository;
import com.example.CJLInvestimentos.repositories.FaturaRepository;
import com.example.CJLInvestimentos.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FaturaService {

    private static final int DIAS_TOLERANCIA_VENCIMENTO = 5;
    private static final int DIAS_PERIODO = 30;

    private final FaturaRepository faturaRepository;
    private final EmpresaRepository empresaRepository;
    private final UserRepository userRepository;

    /**
     * Verifica se a empresa está em dia: não bloqueada por admin, tem período vigente e não passou de 5 dias após o vencimento.
     * AdminMax não usa empresa; para eles sempre true (checado no filtro).
     * Bloqueio por admin (acessoBloqueadoPorAdmin) nega acesso a todos os consultores e clientes da empresa.
     */
    public boolean acessoPermitido(Empresa empresa) {
        if (empresa == null) return true;
        if (Boolean.TRUE.equals(empresa.getAcessoBloqueadoPorAdmin())) return false;
        if (empresa.getCurrentPeriodEnd() == null) return true; // nova empresa ou sem assinatura: libera uso
        Instant limite = empresa.getCurrentPeriodEnd().plus(DIAS_TOLERANCIA_VENCIMENTO, ChronoUnit.DAYS);
        return Instant.now().isBefore(limite) || Instant.now().equals(limite);
    }

    public boolean acessoPermitidoPorEmpresaId(Long empresaId) {
        Empresa empresa = empresaRepository.findById(empresaId).orElse(null);
        return acessoPermitido(empresa);
    }

    /**
     * Verifica se o usuário (consultor/cliente) tem acesso permitido pela empresa.
     * Deve ser chamado dentro de transação; carrega a empresa do usuário.
     */
    @Transactional(readOnly = true)
    public boolean acessoPermitidoPorUsuarioId(Long usuarioId) {
        User user = userRepository.findByIdWithEmpresa(usuarioId).orElse(null);
        if (user == null || user.getEmpresa() == null) return true;
        return acessoPermitido(user.getEmpresa());
    }

    /** Retorna o motivo do bloqueio quando o acesso não é permitido (para mensagem ao usuário). */
    @Transactional(readOnly = true)
    public String getMotivoBloqueioPorUsuarioId(Long usuarioId) {
        User user = userRepository.findByIdWithEmpresa(usuarioId).orElse(null);
        if (user == null || user.getEmpresa() == null) return null;
        Empresa empresa = user.getEmpresa();
        if (Boolean.TRUE.equals(empresa.getAcessoBloqueadoPorAdmin())) {
            // Consultor: contato com responsável do sistema. Cliente: contato com a empresa (consultor).
            if (user.getRole() == com.example.CJLInvestimentos.entities.enums.Role.Cliente) {
                return "Acesso bloqueado. Entre em contato com sua empresa (consultor).";
            }
            return "Acesso bloqueado. Entre em contato com o responsável pelo sistema.";
        }
        if (empresa.getCurrentPeriodEnd() == null) return null;
        Instant limite = empresa.getCurrentPeriodEnd().plus(DIAS_TOLERANCIA_VENCIMENTO, ChronoUnit.DAYS);
        if (Instant.now().isAfter(limite)) {
            return "Assinatura vencida. Regularize o pagamento para continuar acessando.";
        }
        return null;
    }

    /** Indica se o bloqueio é por decisão do admin (não por pagamento). Usado para restringir totalmente o acesso. */
    @Transactional(readOnly = true)
    public boolean isBloqueadoPorAdmin(Long usuarioId) {
        User user = userRepository.findByIdWithEmpresa(usuarioId).orElse(null);
        if (user == null || user.getEmpresa() == null) return false;
        return Boolean.TRUE.equals(user.getEmpresa().getAcessoBloqueadoPorAdmin());
    }

    /**
     * Retorna faturas + próxima + acessoPermitido para o usuário (consultor/cliente).
     * Carrega empresa dentro da transação; evita LazyInitializationException no controller.
     */
    @Transactional(readOnly = true)
    public FaturasComProximaResponse getFaturasComProximaParaUsuario(Long usuarioId) {
        User user = userRepository.findByIdWithEmpresa(usuarioId).orElse(null);
        if (user == null || user.getEmpresa() == null) {
            return FaturasComProximaResponse.builder()
                    .faturas(List.of())
                    .proxima(ProximaFaturaResponse.builder().temAssinatura(false).valor(BigDecimal.ZERO).planoNome(null).build())
                    .acessoPermitido(true)
                    .build();
        }
        Long empresaId = user.getEmpresa().getId();
        return FaturasComProximaResponse.builder()
                .faturas(listarPorEmpresa(empresaId))
                .proxima(getProximaFatura(empresaId))
                .acessoPermitido(acessoPermitido(user.getEmpresa()))
                .build();
    }

    @Transactional
    public FaturaResponse criarFaturaManual(Long empresaId, CriarFaturaRequest request) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada"));
        Fatura fatura = faturaRepository.save(Fatura.builder()
                .empresa(empresa)
                .dataVencimento(request.getDataVencimento())
                .valor(request.getValor())
                .descricaoServico(request.getDescricaoServico())
                .observacao(request.getObservacao())
                .status(StatusFatura.PENDENTE)
                .build());
        return toResponse(fatura);
    }

    @Transactional
    public FaturaResponse atualizarFatura(Long empresaId, Long faturaId, AtualizarFaturaRequest request) {
        Fatura fatura = faturaRepository.findById(faturaId)
                .orElseThrow(() -> new ResourceNotFoundException("Fatura não encontrada"));
        if (!fatura.getEmpresa().getId().equals(empresaId)) {
            throw new ResourceNotFoundException("Fatura não pertence a esta empresa");
        }
        if (request.getDataVencimento() != null) fatura.setDataVencimento(request.getDataVencimento());
        if (request.getValor() != null) fatura.setValor(request.getValor());
        if (request.getStatus() != null) {
            fatura.setStatus(request.getStatus());
            if (request.getStatus() == com.example.CJLInvestimentos.entities.enums.StatusFatura.PAGA) {
                if (request.getDataPagamento() != null) fatura.setDataPagamento(request.getDataPagamento());
                else if (fatura.getDataPagamento() == null) fatura.setDataPagamento(java.time.Instant.now());
                if (request.getFormaPagamento() != null) fatura.setFormaPagamento(request.getFormaPagamento());
            } else {
                fatura.setDataPagamento(null);
                fatura.setFormaPagamento(null);
            }
        } else {
            if (request.getDataPagamento() != null) fatura.setDataPagamento(request.getDataPagamento());
            if (request.getFormaPagamento() != null) fatura.setFormaPagamento(request.getFormaPagamento());
        }
        if (request.getDescricaoServico() != null) fatura.setDescricaoServico(request.getDescricaoServico());
        if (request.getObservacao() != null) fatura.setObservacao(request.getObservacao());
        return toResponse(faturaRepository.save(fatura));
    }

    @Transactional
    public void excluirFatura(Long empresaId, Long faturaId) {
        Fatura fatura = faturaRepository.findById(faturaId)
                .orElseThrow(() -> new ResourceNotFoundException("Fatura não encontrada"));
        if (!fatura.getEmpresa().getId().equals(empresaId)) {
            throw new ResourceNotFoundException("Fatura não pertence a esta empresa");
        }
        faturaRepository.delete(fatura);
    }

    public List<FaturaResponse> listarPorEmpresa(Long empresaId) {
        return faturaRepository.findByEmpresaIdOrderByDataVencimentoDesc(empresaId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ProximaFaturaResponse getProximaFatura(Long empresaId) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada"));
        Plano plano = empresa.getPlano();
        if (plano == null) {
            return ProximaFaturaResponse.builder()
                    .temAssinatura(false)
                    .valor(BigDecimal.ZERO)
                    .planoNome(null)
                    .build();
        }
        Instant dataVencimento;
        if (empresa.getCurrentPeriodEnd() == null) {
            dataVencimento = Instant.now().plus(DIAS_PERIODO, ChronoUnit.DAYS);
        } else {
            dataVencimento = empresa.getCurrentPeriodEnd();
        }
        return ProximaFaturaResponse.builder()
                .temAssinatura(empresa.getCurrentPeriodEnd() != null)
                .dataVencimento(dataVencimento)
                .valor(plano.getPreco())
                .planoNome(plano.getNome())
                .build();
    }

    @Transactional
    public FaturaResponse marcarComoPagoManual(Long empresaId, MarcarPagoRequest request) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada"));
        Plano plano = empresa.getPlano();
        if (plano == null) {
            throw new BusinessException("Empresa não possui plano atribuído.");
        }
        BigDecimal valor = request.getValor() != null ? request.getValor() : plano.getPreco();
        Instant dataVencimento;
        if (empresa.getCurrentPeriodEnd() == null) {
            dataVencimento = Instant.now().plus(DIAS_PERIODO, ChronoUnit.DAYS);
        } else {
            dataVencimento = empresa.getCurrentPeriodEnd();
        }
        Instant novoFimPeriodo = dataVencimento.plus(DIAS_PERIODO, ChronoUnit.DAYS);

        Fatura fatura = faturaRepository.save(Fatura.builder()
                .empresa(empresa)
                .dataVencimento(dataVencimento)
                .dataPagamento(Instant.now())
                .valor(valor)
                .status(StatusFatura.PAGA)
                .formaPagamento(FormaPagamento.MANUAL)
                .observacao(request.getObservacao())
                .build());

        empresa.setCurrentPeriodEnd(novoFimPeriodo);
        empresa.setSubscriptionStatus(SubscriptionStatus.ACTIVE);
        empresaRepository.save(empresa);

        return toResponse(fatura);
    }

    /**
     * Chamado quando um pagamento PIX (ou outro) é confirmado via webhook.
     * Cria fatura PAGA e avança o período da empresa.
     */
    @Transactional
    public void registrarPagamentoExterno(Long empresaId, BigDecimal valor, FormaPagamento forma, String referenciaExterna) {
        if (faturaRepository.existsByEmpresaIdAndReferenciaExterna(empresaId, referenciaExterna)) {
            return;
        }
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada"));
        Plano plano = empresa.getPlano();
        if (plano == null) return;

        Instant dataVencimento;
        if (empresa.getCurrentPeriodEnd() == null) {
            dataVencimento = Instant.now().plus(DIAS_PERIODO, ChronoUnit.DAYS);
        } else {
            dataVencimento = empresa.getCurrentPeriodEnd();
        }
        Instant novoFimPeriodo = dataVencimento.plus(DIAS_PERIODO, ChronoUnit.DAYS);

        String descricao = "Pagamento Stripe - " + (plano.getNome() != null ? plano.getNome() : "Assinatura");
        faturaRepository.save(Fatura.builder()
                .empresa(empresa)
                .dataVencimento(dataVencimento)
                .dataPagamento(Instant.now())
                .valor(valor)
                .status(StatusFatura.PAGA)
                .formaPagamento(forma)
                .referenciaExterna(referenciaExterna)
                .descricaoServico(descricao)
                .build());

        empresa.setCurrentPeriodEnd(novoFimPeriodo);
        empresa.setSubscriptionStatus(SubscriptionStatus.ACTIVE);
        empresaRepository.save(empresa);
    }

    private FaturaResponse toResponse(Fatura f) {
        return FaturaResponse.builder()
                .id(f.getId())
                .empresaId(f.getEmpresa().getId())
                .dataVencimento(f.getDataVencimento())
                .dataPagamento(f.getDataPagamento())
                .valor(f.getValor())
                .status(f.getStatus().name())
                .formaPagamento(f.getFormaPagamento() != null ? f.getFormaPagamento().name() : null)
                .descricaoServico(f.getDescricaoServico())
                .observacao(f.getObservacao())
                .build();
    }
}

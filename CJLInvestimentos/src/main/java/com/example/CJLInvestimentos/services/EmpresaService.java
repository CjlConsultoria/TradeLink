package com.example.CJLInvestimentos.services;

import com.example.CJLInvestimentos.dtos.request.EmpresaRequest;
import com.example.CJLInvestimentos.dtos.response.EmpresaResponse;
import com.example.CJLInvestimentos.entities.Empresa;
import com.example.CJLInvestimentos.entities.Plano;
import com.example.CJLInvestimentos.entities.enums.Role;
import com.example.CJLInvestimentos.exceptions.BusinessException;
import com.example.CJLInvestimentos.exceptions.ResourceNotFoundException;
import com.example.CJLInvestimentos.repositories.EmpresaRepository;
import com.example.CJLInvestimentos.repositories.PlanoRepository;
import com.example.CJLInvestimentos.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmpresaService {

    private static final int DIAS_TOLERANCIA = 5;

    private final EmpresaRepository empresaRepository;
    private final UserRepository userRepository;
    private final PlanoRepository planoRepository;
    private final FaturaService faturaService;

    public EmpresaResponse criar(EmpresaRequest request) {
        if (empresaRepository.findByCnpj(request.getCnpj()).isPresent()) {
            throw new BusinessException("CNPJ já cadastrado");
        }

        Plano plano = request.getPlanoId() != null
                ? planoRepository.findById(request.getPlanoId())
                    .orElse(null)
                : null;

        Empresa empresa = empresaRepository.save(
                Empresa.builder()
                        .nome(request.getNome())
                        .cnpj(request.getCnpj())
                        .plano(plano)
                        .notificacaoEmail(request.getNotificacaoEmail() != null ? request.getNotificacaoEmail() : true)
                        .notificacaoTelegram(request.getNotificacaoTelegram() != null ? request.getNotificacaoTelegram() : true)
                        .notificacaoPush(request.getNotificacaoPush() != null ? request.getNotificacaoPush() : true)
                        .notificacaoWhatsApp(request.getNotificacaoWhatsApp() != null ? request.getNotificacaoWhatsApp() : false)
                        .notificacaoSms(request.getNotificacaoSms() != null ? request.getNotificacaoSms() : false)
                        .build()
        );

        return toResponse(empresa);
    }

    public EmpresaResponse atualizar(Long id, EmpresaRequest request) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada"));

        empresaRepository.findByCnpj(request.getCnpj())
                .filter(e -> !e.getId().equals(id))
                .ifPresent(e -> { throw new BusinessException("CNPJ já cadastrado por outra empresa"); });

        empresa.setNome(request.getNome());
        empresa.setCnpj(request.getCnpj());
        if (request.getPlanoId() != null) {
            empresa.setPlano(planoRepository.findById(request.getPlanoId())
                    .orElse(null));
        } else {
            empresa.setPlano(null);
        }
        if (request.getNotificacaoEmail() != null) empresa.setNotificacaoEmail(request.getNotificacaoEmail());
        if (request.getNotificacaoTelegram() != null) empresa.setNotificacaoTelegram(request.getNotificacaoTelegram());
        if (request.getNotificacaoPush() != null) empresa.setNotificacaoPush(request.getNotificacaoPush());
        if (request.getNotificacaoWhatsApp() != null) empresa.setNotificacaoWhatsApp(request.getNotificacaoWhatsApp());
        if (request.getNotificacaoSms() != null) empresa.setNotificacaoSms(request.getNotificacaoSms());
        empresaRepository.save(empresa);

        return toResponse(empresa);
    }

    public void desativar(Long id) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada"));
        empresa.setAtivo(false);
        empresaRepository.save(empresa);
    }

    @Transactional(readOnly = true)
    public List<EmpresaResponse> listarTodas() {
        return empresaRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EmpresaResponse buscarPorId(Long id) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada"));
        return toResponse(empresa);
    }

    public EmpresaResponse atribuirPlano(Long empresaId, Long planoId) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada"));
        Plano plano = planoId != null
                ? planoRepository.findById(planoId).orElseThrow(() -> new ResourceNotFoundException("Plano não encontrado"))
                : null;
        empresa.setPlano(plano);
        empresaRepository.save(empresa);
        return toResponse(empresa);
    }

    /** Bloqueia ou desbloqueia o acesso à plataforma para todos os consultores e clientes da empresa (diretriz admin). */
    public EmpresaResponse bloquearAcesso(Long id, boolean bloqueado) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada"));
        empresa.setAcessoBloqueadoPorAdmin(bloqueado);
        empresaRepository.save(empresa);
        return toResponse(empresa);
    }

    private EmpresaResponse toResponse(Empresa empresa) {
        int totalUsuarios = (int) userRepository.countByEmpresaId(empresa.getId());
        int totalConsultores = (int) userRepository.countByEmpresaIdAndRole(empresa.getId(), Role.Admin);
        int totalClientes = (int) userRepository.countByEmpresaIdAndRole(empresa.getId(), Role.Cliente);

        return EmpresaResponse.builder()
                .id(empresa.getId())
                .nome(empresa.getNome())
                .cnpj(empresa.getCnpj())
                .ativo(empresa.getAtivo())
                .createdAt(empresa.getCreatedAt())
                .planoId(empresa.getPlano() != null ? empresa.getPlano().getId() : null)
                .planoNome(empresa.getPlano() != null ? empresa.getPlano().getNome() : null)
                .maxUsuarios(empresa.getPlano() != null ? empresa.getPlano().getMaxUsuarios() : null)
                .totalUsuarios(totalUsuarios)
                .totalConsultores(totalConsultores)
                .totalClientes(totalClientes)
                .notificacaoEmail(empresa.getNotificacaoEmail())
                .notificacaoTelegram(empresa.getNotificacaoTelegram())
                .notificacaoPush(empresa.getNotificacaoPush())
                .notificacaoWhatsApp(empresa.getNotificacaoWhatsApp())
                .notificacaoSms(empresa.getNotificacaoSms())
                .subscriptionStatus(empresa.getSubscriptionStatus() != null ? empresa.getSubscriptionStatus().name() : null)
                .currentPeriodEnd(empresa.getCurrentPeriodEnd())
                .acessoPermitido(faturaService.acessoPermitido(empresa))
                .acessoBloqueadoPorAdmin(Boolean.TRUE.equals(empresa.getAcessoBloqueadoPorAdmin()))
                .statusPagamento(calcularStatusPagamento(empresa))
                .build();
    }

    private String calcularStatusPagamento(Empresa empresa) {
        if (empresa.getCurrentPeriodEnd() == null) return "SEM_ASSINATURA";
        Instant now = Instant.now();
        if (!now.isAfter(empresa.getCurrentPeriodEnd())) return "EM_DIA";
        Instant limite = empresa.getCurrentPeriodEnd().plus(DIAS_TOLERANCIA, ChronoUnit.DAYS);
        if (!now.isAfter(limite)) return "VENCIDO";
        return "EM_ATRASO";
    }
}

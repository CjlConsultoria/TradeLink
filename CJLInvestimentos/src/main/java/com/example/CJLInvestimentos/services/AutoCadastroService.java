package com.example.CJLInvestimentos.services;

import com.example.CJLInvestimentos.dtos.request.AutoCadastroRequest;
import com.example.CJLInvestimentos.dtos.response.AutoCadastroResponse;
import com.example.CJLInvestimentos.entities.Empresa;
import com.example.CJLInvestimentos.entities.User;
import com.example.CJLInvestimentos.entities.enums.Role;
import com.example.CJLInvestimentos.entities.enums.SubscriptionStatus;
import com.example.CJLInvestimentos.exceptions.BusinessException;
import com.example.CJLInvestimentos.repositories.EmpresaRepository;
import com.example.CJLInvestimentos.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Serviço de auto-cadastro para clientes e consultores.
 * Cria conta com 5 dias de trial gratuito.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AutoCadastroService {

    private static final int DIAS_TRIAL = 5;

    private final UserRepository userRepository;
    private final EmpresaRepository empresaRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final NotificationAsyncRunner notificationAsyncRunner;

    @Transactional
    public AutoCadastroResponse cadastrar(AutoCadastroRequest request) {
        if ("CONSULTOR".equalsIgnoreCase(request.getTipo())) {
            return cadastrarConsultor(request);
        }
        return cadastrarCliente(request);
    }

    private AutoCadastroResponse cadastrarCliente(AutoCadastroRequest request) {
        validarDadosComuns(request);

        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime trialFim = agora.plusDays(DIAS_TRIAL);

        User user = User.builder()
                .nome(request.getNome())
                .email(request.getEmail())
                .senha(passwordEncoder.encode(request.getSenha()))
                .role(Role.Cliente)
                .cpf(limparDocumento(request.getCpf()))
                .whatsapp(request.getWhatsapp())
                .cep(request.getCep())
                .logradouro(request.getLogradouro())
                .numero(request.getNumero())
                .complemento(request.getComplemento())
                .bairro(request.getBairro())
                .cidade(request.getCidade())
                .estado(request.getEstado())
                .termoAceito(true)
                .termoAceitoEm(agora)
                .autoGestao(true)
                .autoCadastro(true)
                .trialInicio(agora)
                .trialFim(trialFim)
                .subscriptionStatus("TRIAL")
                .currentPeriodEnd(trialFim.atZone(ZoneId.systemDefault()).toInstant())
                .build();

        user = userRepository.save(user);

        String token = jwtService.generateToken(user);

        // Email de boas-vindas async
        try {
            notificationAsyncRunner.enviarBoasVindasAutoCadastroAsync(
                    user.getEmail(), user.getNome(), "CLIENTE", trialFim);
        } catch (Exception e) {
            log.warn("Falha ao enviar email de boas-vindas (auto-cadastro cliente): {}", e.getMessage());
        }

        log.info("Auto-cadastro cliente: {} ({})", user.getEmail(), user.getId());

        return AutoCadastroResponse.builder()
                .token(token)
                .role(Role.Cliente.name())
                .userId(user.getId())
                .nome(user.getNome())
                .trialFim(trialFim)
                .mensagem("Conta criada com sucesso! Você tem " + DIAS_TRIAL + " dias gratuitos.")
                .build();
    }

    private AutoCadastroResponse cadastrarConsultor(AutoCadastroRequest request) {
        validarDadosComuns(request);

        String cnpj = request.getCnpj() != null ? limparDocumento(request.getCnpj()) : limparDocumento(request.getCpf());
        String nomeEmpresa = request.getNomeEmpresa() != null && !request.getNomeEmpresa().isBlank()
                ? request.getNomeEmpresa()
                : request.getNome();

        // Verificar CNPJ único
        if (empresaRepository.findByCnpj(cnpj).isPresent()) {
            throw new BusinessException("CNPJ/CPF já cadastrado como empresa.");
        }

        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime trialFim = agora.plusDays(DIAS_TRIAL);

        // Criar empresa
        Empresa empresa = Empresa.builder()
                .nome(nomeEmpresa)
                .cnpj(cnpj)
                .cep(request.getCep())
                .logradouro(request.getLogradouro())
                .numero(request.getNumero())
                .complemento(request.getComplemento())
                .bairro(request.getBairro())
                .cidade(request.getCidade())
                .uf(request.getEstado())
                .nomeResponsavel(request.getNomeResponsavel() != null ? request.getNomeResponsavel() : request.getNome())
                .cpfResponsavel(limparDocumento(request.getCpf()))
                .telefone(request.getWhatsapp())
                .autoCadastro(true)
                .trialInicio(agora)
                .trialFim(trialFim)
                .subscriptionStatus(SubscriptionStatus.TRIAL)
                .currentPeriodEnd(trialFim.atZone(ZoneId.systemDefault()).toInstant())
                .build();

        empresa = empresaRepository.save(empresa);

        // Criar usuário consultor
        User user = User.builder()
                .nome(request.getNome())
                .email(request.getEmail())
                .senha(passwordEncoder.encode(request.getSenha()))
                .role(Role.Admin)
                .empresa(empresa)
                .cpf(limparDocumento(request.getCpf()))
                .whatsapp(request.getWhatsapp())
                .cep(request.getCep())
                .logradouro(request.getLogradouro())
                .numero(request.getNumero())
                .complemento(request.getComplemento())
                .bairro(request.getBairro())
                .cidade(request.getCidade())
                .estado(request.getEstado())
                .termoAceito(true)
                .termoAceitoEm(agora)
                .autoCadastro(true)
                .trialInicio(agora)
                .trialFim(trialFim)
                .build();

        user = userRepository.save(user);

        String token = jwtService.generateToken(user);

        // Email de boas-vindas async
        try {
            notificationAsyncRunner.enviarBoasVindasAutoCadastroAsync(
                    user.getEmail(), user.getNome(), "CONSULTOR", trialFim);
        } catch (Exception e) {
            log.warn("Falha ao enviar email de boas-vindas (auto-cadastro consultor): {}", e.getMessage());
        }

        log.info("Auto-cadastro consultor: {} ({}) empresa: {} ({})",
                user.getEmail(), user.getId(), empresa.getNome(), empresa.getId());

        return AutoCadastroResponse.builder()
                .token(token)
                .role(Role.Admin.name())
                .userId(user.getId())
                .nome(user.getNome())
                .empresaId(empresa.getId())
                .trialFim(trialFim)
                .mensagem("Conta criada com sucesso! Você tem " + DIAS_TRIAL + " dias gratuitos.")
                .build();
    }

    private void validarDadosComuns(AutoCadastroRequest request) {
        if (!Boolean.TRUE.equals(request.getTermoAceito())) {
            throw new BusinessException("É necessário aceitar os termos de uso.");
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BusinessException("E-mail já cadastrado.");
        }

        String cpfLimpo = limparDocumento(request.getCpf());
        if (cpfLimpo != null && userRepository.findByCpf(cpfLimpo).isPresent()) {
            throw new BusinessException("CPF já cadastrado.");
        }
    }

    private String limparDocumento(String doc) {
        if (doc == null) return null;
        return doc.replaceAll("[^0-9]", "");
    }
}

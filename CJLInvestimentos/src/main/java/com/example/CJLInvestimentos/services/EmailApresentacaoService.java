package com.example.CJLInvestimentos.services;

import com.example.CJLInvestimentos.dtos.response.EmailApresentacaoResponse;
import com.example.CJLInvestimentos.entities.CarteiraCliente;
import com.example.CJLInvestimentos.entities.EmailApresentacao;
import com.example.CJLInvestimentos.entities.User;
import com.example.CJLInvestimentos.entities.enums.Role;
import com.example.CJLInvestimentos.entities.enums.TipoTemplateApresentacao;
import com.example.CJLInvestimentos.repositories.CarteiraClienteRepository;
import com.example.CJLInvestimentos.repositories.EmailApresentacaoRepository;
import com.example.CJLInvestimentos.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailApresentacaoService {

    private final EmailApresentacaoRepository emailApresentacaoRepository;
    private final UserRepository userRepository;
    private final CarteiraClienteRepository carteiraClienteRepository;
    private final NotificationService notificationService;
    private final EmailTemplateService emailTemplateService;

    @Value("${app.apresentacao.link-ppt:}")
    private String linkPpt;

    @Value("${app.apresentacao.link-html:}")
    private String linkHtml;

    public TipoTemplateApresentacao detectarTipoTemplate(User user) {
        if (user.getRole() == Role.Admin || user.getRole() == Role.AdminMax) {
            return TipoTemplateApresentacao.CONSULTOR;
        }
        if (Boolean.TRUE.equals(user.getAutoGestao())) {
            return TipoTemplateApresentacao.CLIENTE_AUTO_GESTAO;
        }
        return TipoTemplateApresentacao.CLIENTE;
    }

    public List<EmailApresentacaoResponse> enviar(List<Long> userIds, User enviadoPor) {
        List<EmailApresentacaoResponse> resultados = new ArrayList<>();

        for (Long userId : userIds) {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) continue;

            TipoTemplateApresentacao tipo = detectarTipoTemplate(user);
            String nome = user.getNome() != null ? user.getNome() : user.getEmail();
            String html;

            switch (tipo) {
                case CONSULTOR:
                    html = emailTemplateService.buildApresentacaoConsultor(nome, linkPpt, linkHtml);
                    break;
                case CLIENTE:
                    html = emailTemplateService.buildApresentacaoCliente(nome, resolverConsultorNome(user), linkPpt, linkHtml);
                    break;
                case CLIENTE_AUTO_GESTAO:
                    html = emailTemplateService.buildApresentacaoClienteAutoGestao(nome, linkPpt, linkHtml);
                    break;
                default:
                    html = emailTemplateService.buildApresentacaoConsultor(nome, linkPpt, linkHtml);
            }

            String assunto = switch (tipo) {
                case CONSULTOR -> "Guia do Consultor - TradeLink";
                case CLIENTE -> "Guia do Cliente - TradeLink";
                case CLIENTE_AUTO_GESTAO -> "Guia do Investidor - TradeLink";
            };

            EmailApresentacao registro = EmailApresentacao.builder()
                    .user(user)
                    .enviadoPor(enviadoPor)
                    .tipoTemplate(tipo)
                    .build();

            try {
                notificationService.enviarEmailHtml(user.getEmail(), assunto, html);
                registro.setStatus("ENVIADO");
                log.info("Email de apresentacao ({}) enviado para {} (id={})", tipo, user.getEmail(), user.getId());
            } catch (Exception e) {
                registro.setStatus("FALHA");
                registro.setErro(e.getMessage() != null ? e.getMessage().substring(0, Math.min(e.getMessage().length(), 500)) : "Erro desconhecido");
                log.error("Falha ao enviar email de apresentacao para {}: {}", user.getEmail(), e.getMessage());
            }

            emailApresentacaoRepository.save(registro);
            resultados.add(toResponse(registro));
        }

        return resultados;
    }

    public String preview(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado"));

        TipoTemplateApresentacao tipo = detectarTipoTemplate(user);
        String nome = user.getNome() != null ? user.getNome() : user.getEmail();

        return switch (tipo) {
            case CONSULTOR -> emailTemplateService.buildApresentacaoConsultor(nome, linkPpt, linkHtml);
            case CLIENTE -> emailTemplateService.buildApresentacaoCliente(nome, resolverConsultorNome(user), linkPpt, linkHtml);
            case CLIENTE_AUTO_GESTAO -> emailTemplateService.buildApresentacaoClienteAutoGestao(nome, linkPpt, linkHtml);
        };
    }

    /**
     * Envia automaticamente o email de apresentação como boas-vindas.
     * Chamado após ativação de conta (convite, criação direta, auto-cadastro).
     * Não envia duplicado se já foi enviado automaticamente para o mesmo user.
     */
    public void enviarBoasVindas(Long userId) {
        // Evitar duplicidade
        if (emailApresentacaoRepository.existsByUserIdAndAutomaticoTrue(userId)) {
            log.info("Email de boas-vindas já enviado para userId={}, ignorando", userId);
            return;
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        TipoTemplateApresentacao tipo = detectarTipoTemplate(user);
        String nome = user.getNome() != null ? user.getNome() : user.getEmail();
        String html;

        switch (tipo) {
            case CONSULTOR:
                html = emailTemplateService.buildBoasVindasConsultor(nome, linkPpt, linkHtml);
                break;
            case CLIENTE:
                html = emailTemplateService.buildBoasVindasCliente(nome, resolverConsultorNome(user), linkPpt, linkHtml);
                break;
            case CLIENTE_AUTO_GESTAO:
                html = emailTemplateService.buildBoasVindasClienteAutoGestao(nome, linkPpt, linkHtml);
                break;
            default:
                html = emailTemplateService.buildBoasVindasConsultor(nome, linkPpt, linkHtml);
        }

        String assunto = "Bem-vindo ao TradeLink! Conheça a plataforma";

        EmailApresentacao registro = EmailApresentacao.builder()
                .user(user)
                .enviadoPor(null)
                .tipoTemplate(tipo)
                .automatico(true)
                .build();

        try {
            notificationService.enviarEmailHtml(user.getEmail(), assunto, html);
            registro.setStatus("ENVIADO");
            log.info("Email de boas-vindas automático ({}) enviado para {} (id={})", tipo, user.getEmail(), user.getId());
        } catch (Exception e) {
            registro.setStatus("FALHA");
            registro.setErro(e.getMessage() != null ? e.getMessage().substring(0, Math.min(e.getMessage().length(), 500)) : "Erro desconhecido");
            log.error("Falha ao enviar email de boas-vindas automático para {}: {}", user.getEmail(), e.getMessage());
        }

        emailApresentacaoRepository.save(registro);
    }

    public List<EmailApresentacaoResponse> historico() {
        return emailApresentacaoRepository.findAllWithUsersOrderByEnviadoEmDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Resolve o(s) nome(s) do(s) consultor(es) vinculado(s) ao cliente.
     * Retorna null se nenhum consultor for encontrado.
     */
    private String resolverConsultorNome(User clienteUser) {
        // 1) Buscar consultores reais via CarteiraCliente → Carteira → consultor
        try {
            List<CarteiraCliente> vinculos = carteiraClienteRepository.findByClienteIdWithCarteiraConsultor(clienteUser.getId());
            List<String> nomes = vinculos.stream()
                    .map(cc -> cc.getCarteira().getConsultor())
                    .filter(Objects::nonNull)
                    .map(User::getNome)
                    .filter(n -> n != null && !n.isBlank())
                    .distinct()
                    .toList();
            if (!nomes.isEmpty()) return String.join(", ", nomes);
        } catch (Exception e) {
            log.debug("Erro ao buscar consultores via carteira para userId={}: {}", clienteUser.getId(), e.getMessage());
        }

        // 2) Fallback: empresa.nomeResponsavel
        if (clienteUser.getEmpresa() != null && clienteUser.getEmpresa().getNomeResponsavel() != null
                && !clienteUser.getEmpresa().getNomeResponsavel().isBlank()) {
            return clienteUser.getEmpresa().getNomeResponsavel();
        }

        // 3) Fallback: buscar qualquer consultor da mesma empresa
        if (clienteUser.getEmpresa() != null) {
            List<User> consultores = userRepository.findByEmpresaIdAndRole(
                    clienteUser.getEmpresa().getId(), Role.Admin);
            if (!consultores.isEmpty()) {
                String joined = consultores.stream()
                        .map(User::getNome)
                        .filter(n -> n != null && !n.isBlank())
                        .collect(Collectors.joining(", "));
                if (!joined.isBlank()) return joined;
            }
        }

        return null;
    }

    private EmailApresentacaoResponse toResponse(EmailApresentacao e) {
        return EmailApresentacaoResponse.builder()
                .id(e.getId())
                .nomeDestinatario(e.getUser().getNome())
                .emailDestinatario(e.getUser().getEmail())
                .roleDestinatario(e.getUser().getRole().name())
                .tipoTemplate(e.getTipoTemplate().name())
                .nomeEnviadoPor(e.getEnviadoPor() != null ? e.getEnviadoPor().getNome() : "Sistema (automático)")
                .enviadoEm(e.getEnviadoEm())
                .status(e.getStatus())
                .erro(e.getErro())
                .build();
    }
}

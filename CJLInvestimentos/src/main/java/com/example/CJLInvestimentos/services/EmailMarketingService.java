package com.example.CJLInvestimentos.services;

import com.example.CJLInvestimentos.dtos.request.EnviarEmailMarketingRequest;
import com.example.CJLInvestimentos.dtos.response.EmailMarketingResponse;
import com.example.CJLInvestimentos.entities.EmailMarketing;
import com.example.CJLInvestimentos.entities.User;
import com.example.CJLInvestimentos.repositories.EmailMarketingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailMarketingService {

    private final EmailMarketingRepository emailMarketingRepository;
    private final NotificationService notificationService;
    private final EmailTemplateService emailTemplateService;

    private static final int MAX_EMAILS_POR_LOTE = 100;
    private static final long DELAY_ENTRE_ENVIOS_MS = 1200;

    public List<EmailMarketingResponse> enviar(EnviarEmailMarketingRequest request, User enviadoPor) {
        List<EnviarEmailMarketingRequest.ContatoExterno> contatos = request.getContatos();

        if (contatos.size() > MAX_EMAILS_POR_LOTE) {
            throw new IllegalArgumentException(
                    "Maximo de " + MAX_EMAILS_POR_LOTE + " emails por lote. Recebido: " + contatos.size()
                            + ". Divida em lotes menores.");
        }

        List<EmailMarketingResponse> resultados = new ArrayList<>();
        String campanha = request.getCampanha();
        String assunto = request.getAssunto();

        log.info("Iniciando envio de email marketing: {} contatos, campanha='{}', assunto='{}', por={}",
                contatos.size(), campanha, assunto,
                enviadoPor != null ? enviadoPor.getEmail() : "sistema");

        for (int i = 0; i < contatos.size(); i++) {
            EnviarEmailMarketingRequest.ContatoExterno contato = contatos.get(i);
            String email = contato.getEmail().trim().toLowerCase();
            String nome = contato.getNome();

            String html = emailTemplateService.buildEmailMarketing(nome);

            EmailMarketing registro = EmailMarketing.builder()
                    .emailDestinatario(email)
                    .nomeDestinatario(nome)
                    .assunto(assunto)
                    .campanha(campanha)
                    .enviadoPor(enviadoPor)
                    .build();

            try {
                notificationService.enviarEmailHtml(email, assunto, html);
                registro.setStatus("ENVIADO");
                log.info("[{}/{}] Email marketing enviado para {} ({})",
                        i + 1, contatos.size(), email, nome != null ? nome : "-");
            } catch (Exception e) {
                registro.setStatus("FALHA");
                registro.setErro(e.getMessage() != null
                        ? e.getMessage().substring(0, Math.min(e.getMessage().length(), 500))
                        : "Erro desconhecido");
                log.error("[{}/{}] Falha ao enviar email marketing para {}: {}",
                        i + 1, contatos.size(), email, e.getMessage());
            }

            emailMarketingRepository.save(registro);
            resultados.add(toResponse(registro));

            // Delay entre envios para respeitar rate limit
            if (i < contatos.size() - 1) {
                try {
                    Thread.sleep(DELAY_ENTRE_ENVIOS_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("Envio interrompido no email {}/{}", i + 1, contatos.size());
                    break;
                }
            }
        }

        long enviados = resultados.stream().filter(r -> "ENVIADO".equals(r.getStatus())).count();
        long falhas = resultados.stream().filter(r -> "FALHA".equals(r.getStatus())).count();
        log.info("Email marketing finalizado: {} enviados, {} falhas, campanha='{}'",
                enviados, falhas, campanha);

        return resultados;
    }

    public String preview() {
        return emailTemplateService.buildEmailMarketing("Exemplo de Lead");
    }

    public List<EmailMarketingResponse> historico() {
        return emailMarketingRepository.findAllWithEnviadoPorOrderByEnviadoEmDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<EmailMarketingResponse> historicoPorCampanha(String campanha) {
        return emailMarketingRepository.findByCampanhaOrderByEnviadoEmDesc(campanha)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private EmailMarketingResponse toResponse(EmailMarketing e) {
        return EmailMarketingResponse.builder()
                .id(e.getId())
                .email(e.getEmailDestinatario())
                .nome(e.getNomeDestinatario())
                .assunto(e.getAssunto())
                .status(e.getStatus())
                .erro(e.getErro())
                .campanha(e.getCampanha())
                .enviadoPor(e.getEnviadoPor() != null ? e.getEnviadoPor().getNome() : "Sistema")
                .enviadoEm(e.getEnviadoEm())
                .build();
    }
}

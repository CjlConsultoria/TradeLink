package com.example.CJLInvestimentos.services;

import com.example.CJLInvestimentos.entities.Empresa;
import com.example.CJLInvestimentos.entities.PushSubscription;
import com.example.CJLInvestimentos.entities.Recomendacao;
import com.example.CJLInvestimentos.entities.User;
import com.example.CJLInvestimentos.entities.enums.Role;
import com.example.CJLInvestimentos.repositories.PushSubscriptionRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.security.Security;
import java.util.List;

@Slf4j
@Service
public class NotificationService {

    private final JavaMailSender mailSender;
    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final EmailTemplateService emailTemplateService;

    public NotificationService(
            @org.springframework.beans.factory.annotation.Autowired(required = false) JavaMailSender mailSender,
            PushSubscriptionRepository pushSubscriptionRepository,
            EmailTemplateService emailTemplateService) {
        this.mailSender = mailSender;
        this.pushSubscriptionRepository = pushSubscriptionRepository;
        this.emailTemplateService = emailTemplateService;
    }

    @Value("${app.notificacao.telegram.bot-token:}")
    private String telegramBotToken;

    @Value("${app.notificacao.push.vapid-public:}")
    private String vapidPublicKey;

    @Value("${app.notificacao.push.vapid-private:}")
    private String vapidPrivateKey;

    @Value("${app.notificacao.email.from:}")
    private String emailFrom;

    private static final String TELEGRAM_API = "https://api.telegram.org/bot";

    /** Envia e-mail em texto simples (se configurado). */
    public void enviarEmail(String para, String assunto, String corpo) {
        if (mailSender == null || emailFrom == null || emailFrom.isBlank()) {
            log.info("E-mail não configurado (MAIL_PASSWORD ou app.notificacao.email.from vazio), ignorando envio.");
            return;
        }
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(emailFrom);
            msg.setTo(para);
            msg.setSubject(assunto);
            msg.setText(corpo);
            mailSender.send(msg);
            log.info("E-mail enviado para {}", para);
        } catch (Exception e) {
            log.warn("Falha ao enviar e-mail para {}: {} - {}", para, e.getClass().getSimpleName(), e.getMessage());
            if (log.isDebugEnabled()) {
                log.debug("Detalhe da exceção de e-mail", e);
            }
        }
    }

    /** Envia e-mail em HTML (template responsivo). */
    public void enviarEmailHtml(String para, String assunto, String htmlBody) {
        if (mailSender == null || emailFrom == null || emailFrom.isBlank()) {
            log.info("E-mail não configurado (MAIL_PASSWORD ou app.notificacao.email.from vazio), ignorando envio.");
            return;
        }
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            msg.setFrom(emailFrom);
            msg.setRecipients(jakarta.mail.Message.RecipientType.TO, para);
            msg.setSubject(assunto);
            msg.setContent(htmlBody, "text/html; charset=UTF-8");
            mailSender.send(msg);
            log.info("E-mail HTML enviado para {}", para);
        } catch (Exception e) {
            log.warn("Falha ao enviar e-mail HTML para {}: {} - {}", para, e.getClass().getSimpleName(), e.getMessage());
            if (log.isDebugEnabled()) {
                log.debug("Detalhe da exceção de e-mail", e);
            }
        }
    }

    /** E-mail de boas-vindas (novo usuário). */
    public void enviarEmailNovoUsuario(String para, String nome, Role role) {
        String roleDisplay = role == Role.Admin || role == Role.AdminMax ? "Consultor/Admin" : role == Role.Cliente ? "Cliente" : role.name();
        String html = emailTemplateService.buildNovoUsuario(nome != null ? nome : para, para, roleDisplay);
        enviarEmailHtml(para, "Bem-vindo ao TradeLink", html);
    }

    /** E-mail de confirmação de alteração de senha. */
    public void enviarEmailSenhaAlterada(String para, String nome) {
        String html = emailTemplateService.buildSenhaAlterada(nome != null ? nome : para);
        enviarEmailHtml(para, "Senha alterada - TradeLink", html);
    }

    /** Envia mensagem pelo Telegram (se token configurado). */
    public void enviarTelegram(String chatId, String texto) {
        if (telegramBotToken == null || telegramBotToken.isBlank() || chatId == null || chatId.isBlank()) {
            log.debug("Telegram não configurado ou chatId vazio, ignorando envio.");
            return;
        }
        try {
            String url = TELEGRAM_API + telegramBotToken + "/sendMessage?chat_id=" + chatId + "&text=" + java.net.URLEncoder.encode(texto, java.nio.charset.StandardCharsets.UTF_8);
            RestTemplate rest = new RestTemplate();
            rest.getForObject(url, String.class);
            log.debug("Telegram enviado para chat_id {}", chatId);
        } catch (Exception e) {
            log.warn("Falha ao enviar Telegram para {}: {}", chatId, e.getMessage());
        }
    }

    /** Envia push para uma inscrição (se VAPID configurado). */
    public void enviarPush(PushSubscription sub, String titulo, String corpo) {
        if (vapidPublicKey == null || vapidPublicKey.isBlank() || vapidPrivateKey == null || vapidPrivateKey.isBlank()) {
            log.info("Web Push: VAPID não configurado (defina VAPID_PRIVATE no Render). Push não enviado.");
            return;
        }
        try {
            ensureBouncyCastle();
            PushService pushService = new PushService(vapidPublicKey, vapidPrivateKey);
            String payload = "{\"title\":\"" + escapeJson(titulo) + "\",\"body\":\"" + escapeJson(corpo) + "\"}";
            Notification notification = new Notification(sub.getEndpoint(), sub.getP256dhKey(), sub.getAuthKey(), payload);
            pushService.send(notification);
            log.info("Web Push enviado: {} | endpoint: {}", titulo, sub.getEndpoint() != null ? sub.getEndpoint().substring(0, Math.min(60, sub.getEndpoint().length())) + "..." : "?");
        } catch (Exception e) {
            log.warn("Falha ao enviar Push para endpoint {}: {}", sub.getEndpoint(), e.getMessage());
        }
    }

    private static void ensureBouncyCastle() {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        }
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    /** Notifica um usuário conforme canais habilitados na empresa. Se htmlBody não for nulo, e-mail é enviado em HTML. */
    public void notificarUsuario(Empresa empresa, User usuario, String titulo, String corpo, String htmlBody) {
        if (empresa == null || usuario == null) return;

        if (Boolean.TRUE.equals(empresa.getNotificacaoEmail()) && usuario.getEmail() != null && !usuario.getEmail().isBlank()) {
            if (htmlBody != null && !htmlBody.isBlank()) {
                enviarEmailHtml(usuario.getEmail(), titulo, htmlBody);
            } else {
                enviarEmail(usuario.getEmail(), titulo, corpo);
            }
        } else if (Boolean.TRUE.equals(empresa.getNotificacaoEmail()) && (usuario.getEmail() == null || usuario.getEmail().isBlank())) {
            log.info("E-mail não enviado para usuário id={}: cliente sem e-mail cadastrado.", usuario.getId());
        } else if (!Boolean.TRUE.equals(empresa.getNotificacaoEmail())) {
            log.info("E-mail não enviado: notificação por e-mail desabilitada na empresa (id={}).", empresa.getId());
        }
        if (Boolean.TRUE.equals(empresa.getNotificacaoTelegram()) && usuario.getTelegramChatId() != null && !usuario.getTelegramChatId().isBlank()) {
            enviarTelegram(usuario.getTelegramChatId(), titulo + "\n\n" + corpo);
        }
        if (Boolean.TRUE.equals(empresa.getNotificacaoPush())) {
            List<PushSubscription> subs = pushSubscriptionRepository.findByUserId(usuario.getId());
            if (subs.isEmpty()) {
                log.info("Web Push: usuário {} (id={}) não tem inscrição no navegador. Peça para ativar em Configurações.", usuario.getEmail(), usuario.getId());
            } else {
                for (PushSubscription sub : subs) {
                    enviarPush(sub, titulo, corpo);
                }
            }
        }
    }

    /** Notifica um usuário (e-mail em texto simples). */
    public void notificarUsuario(Empresa empresa, User usuario, String titulo, String corpo) {
        notificarUsuario(empresa, usuario, titulo, corpo, null);
    }
    /** Chamado quando uma nova recomendação é criada: notificar clientes da carteira (e-mail em HTML). */
    public void notificarNovaRecomendacaoParaClientes(Empresa empresa, List<User> clientes, Recomendacao rec) {
        String titulo = "Nova recomendação";
        String corpo = String.format("%s/%s - %s | Entrada: %s | Alvo: %s. Acesse o dashboard para ver detalhes.",
                rec.getMoeda(), rec.getParMoeda(), rec.getTipo(),
                rec.getPrecoEntrada() != null ? rec.getPrecoEntrada().toPlainString() : "-",
                rec.getPrecoAlvo() != null ? rec.getPrecoAlvo().toPlainString() : "-");
        String html = emailTemplateService.buildNovaRecomendacao(rec);
        for (User cliente : clientes) {
            notificarUsuario(empresa, cliente, titulo, corpo, html);
        }
    }

    /** Chamado quando o cliente marca recomendação como resolvida: notificar o consultor (e-mail em HTML). */
    public void notificarClienteResolveuParaConsultor(Empresa empresa, User consultor, User cliente, Recomendacao rec) {
        String titulo = "Cliente resolveu recomendação";
        String corpo = String.format("%s marcou como resolvida a recomendação %s/%s (carteira %s).",
                cliente.getNome() != null ? cliente.getNome() : cliente.getEmail(),
                rec.getMoeda(), rec.getParMoeda(),
                rec.getCarteira() != null ? rec.getCarteira().getNome() : "-");
        String html = emailTemplateService.buildClienteResolveu(
                cliente.getNome() != null ? cliente.getNome() : cliente.getEmail(),
                rec.getMoeda() != null ? rec.getMoeda() : "-",
                rec.getParMoeda() != null ? rec.getParMoeda() : "-",
                rec.getCarteira() != null && rec.getCarteira().getNome() != null ? rec.getCarteira().getNome() : "-");
        notificarUsuario(empresa, consultor, titulo, corpo, html);
    }
}

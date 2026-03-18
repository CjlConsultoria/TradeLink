package com.example.CJLInvestimentos.services;

import com.example.CJLInvestimentos.entities.Empresa;
import com.example.CJLInvestimentos.entities.PushSubscription;
import com.example.CJLInvestimentos.entities.Recomendacao;
import com.example.CJLInvestimentos.entities.User;
import com.example.CJLInvestimentos.entities.enums.Role;
import com.example.CJLInvestimentos.repositories.PushSubscriptionRepository;
import jakarta.annotation.PostConstruct;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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

    @Value("${app.notificacao.email.resend-api-key:${RESEND_API_KEY:}}")
    private String resendApiKey;

    @Value("${app.notificacao.whatsapp.account-sid:${TWILIO_ACCOUNT_SID:}}")
    private String twilioAccountSid;

    @Value("${app.notificacao.whatsapp.auth-token:${TWILIO_AUTH_TOKEN:}}")
    private String twilioAuthToken;

    @Value("${app.notificacao.whatsapp.from:${TWILIO_WHATSAPP_FROM:}}")
    private String twilioWhatsappFrom;

    private static final String TELEGRAM_API = "https://api.telegram.org/bot";
    private static final String RESEND_API = "https://api.resend.com/emails";
    private static final String TWILIO_API = "https://api.twilio.com/2010-04-01/Accounts/%s/Messages.json";
    private static final String FROM_DISPLAY_NAME = "TradeLink";
    private static final Pattern ONLY_DIGITS = Pattern.compile("\\D+");

    @PostConstruct
    public void logEmailConfig() {
        if (resendApiKey != null && !resendApiKey.isBlank()) {
            log.info("Notificação e-mail: CONFIGURADO (Resend). Remetente: {}. Os e-mails serão enviados via API.", emailFrom != null && !emailFrom.isBlank() ? emailFrom : "defina app.notificacao.email.from");
        } else if (mailSender != null && emailFrom != null && !emailFrom.isBlank()) {
            log.info("Notificação e-mail: CONFIGURADO (SMTP). Remetente: {}. Os e-mails serão enviados.", emailFrom);
        } else {
            log.warn("Notificação e-mail: NÃO CONFIGURADO. Para Render: defina RESEND_API_KEY e app.notificacao.email.from (ex: onboarding@resend.dev). Veja docs/CONFIGURAR-EMAIL-GMAIL.md");
        }
        if (twilioAccountSid != null && !twilioAccountSid.isBlank() && twilioAuthToken != null && !twilioAuthToken.isBlank() && twilioWhatsappFrom != null && !twilioWhatsappFrom.isBlank()) {
            log.info("Notificação WhatsApp: CONFIGURADO (Twilio). From: {}. Será usado quando empresa tiver notificacao_whatsapp=true e usuário tiver telefone.", twilioWhatsappFrom);
        } else {
            log.debug("Notificação WhatsApp: não configurado (TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN, TWILIO_WHATSAPP_FROM). Veja docs/CONFIGURAR-WHATSAPP-TWILIO.md");
        }
    }

    /** Envia e-mail em texto simples (se configurado). */
    public void enviarEmail(String para, String assunto, String corpo) {
        if (resendApiKey != null && !resendApiKey.isBlank()) {
            enviarViaResend(para, assunto, null, corpo);
            return;
        }
        if (mailSender == null || emailFrom == null || emailFrom.isBlank()) {
            log.info("E-mail não configurado (RESEND_API_KEY ou MAIL_PASSWORD / app.notificacao.email.from vazio), ignorando envio.");
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

    /** Envia e-mail em HTML (template responsivo). Usa remetente com nome para reduzir chance de ir para spam. */
    public void enviarEmailHtml(String para, String assunto, String htmlBody) {
        if (resendApiKey != null && !resendApiKey.isBlank()) {
            enviarViaResend(para, assunto, htmlBody, null);
            return;
        }
        if (mailSender == null || emailFrom == null || emailFrom.isBlank()) {
            log.info("E-mail não configurado (RESEND_API_KEY ou MAIL_PASSWORD / app.notificacao.email.from vazio), ignorando envio para {}", para);
            return;
        }
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(new InternetAddress(emailFrom, FROM_DISPLAY_NAME, "UTF-8"));
            helper.setTo(para);
            helper.setSubject(assunto);
            helper.setText(htmlBody, true);
            mailSender.send(msg);
            log.info("E-mail HTML enviado para {} (assunto: {})", para, assunto);
        } catch (Exception e) {
            log.warn("Falha ao enviar e-mail HTML para {}: {} - {}", para, e.getClass().getSimpleName(), e.getMessage());
            if (log.isDebugEnabled()) {
                log.debug("Detalhe da exceção de e-mail", e);
            }
        }
    }

    /** Envia e-mail via API Resend (HTTPS). Usado quando RESEND_API_KEY está definido (ex: no Render). */
    private void enviarViaResend(String para, String assunto, String html, String texto) {
        if (emailFrom == null || emailFrom.isBlank()) {
            log.warn("Resend configurado mas app.notificacao.email.from vazio. Defina o remetente (ex: onboarding@resend.dev).");
            return;
        }
        try {
            String from = emailFrom.contains("<") ? emailFrom : (FROM_DISPLAY_NAME + " <" + emailFrom.trim() + ">");
            Map<String, Object> body = new java.util.HashMap<>();
            body.put("from", from);
            body.put("to", List.of(para));
            body.put("subject", assunto);
            if (html != null && !html.isBlank()) {
                body.put("html", html);
            } else {
                body.put("text", texto != null ? texto : "");
            }
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(resendApiKey.trim());
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            RestTemplate rest = new RestTemplate();
            rest.postForEntity(RESEND_API, request, String.class);
            log.info("E-mail enviado para {} via Resend (assunto: {})", para, assunto);
        } catch (Exception e) {
            log.warn("Falha ao enviar e-mail via Resend para {}: {} - {}", para, e.getClass().getSimpleName(), e.getMessage());
            if (log.isDebugEnabled()) {
                log.debug("Detalhe da exceção Resend", e);
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

    /** Envia push para uma inscrição (se VAPID configurado). Cada dispositivo inscrito recebe a notificação. */
    public void enviarPush(PushSubscription sub, String titulo, String corpo) {
        if (vapidPublicKey == null || vapidPublicKey.isBlank() || vapidPrivateKey == null || vapidPrivateKey.isBlank()) {
            log.info("Web Push: VAPID não configurado (defina VAPID_PRIVATE no Render). Push não enviado.");
            return;
        }
        if (sub.getEndpoint() == null || sub.getEndpoint().isBlank()) {
            log.warn("Web Push: inscrição sem endpoint, ignorando.");
            return;
        }
        try {
            ensureBouncyCastle();
            PushService pushService = new PushService(vapidPublicKey, vapidPrivateKey);
            String payload = "{\"title\":\"" + escapeJson(titulo) + "\",\"body\":\"" + escapeJson(corpo) + "\"}";
            Notification notification = new Notification(sub.getEndpoint(), sub.getP256dhKey(), sub.getAuthKey(), payload);
            pushService.send(notification);
            log.info("Web Push enviado: {} | endpoint: {}", titulo, sub.getEndpoint().length() > 60 ? sub.getEndpoint().substring(0, 60) + "..." : sub.getEndpoint());
        } catch (Exception e) {
            log.warn("Falha ao enviar Push para um dispositivo (endpoint pode estar expirado): {}", e.getMessage());
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

    /** Envia mensagem WhatsApp via Twilio. Número em formato livre (ex: 11999998888) é normalizado para E.164 (+5511999998888). */
    private void enviarWhatsApp(String telefone, String texto) {
        if (twilioAccountSid == null || twilioAccountSid.isBlank() || twilioAuthToken == null || twilioAuthToken.isBlank() || twilioWhatsappFrom == null || twilioWhatsappFrom.isBlank()) {
            log.debug("WhatsApp não configurado (Twilio), ignorando envio.");
            return;
        }
        String toE164 = normalizarTelefoneWhatsApp(telefone);
        if (toE164 == null || toE164.isBlank()) {
            log.warn("WhatsApp: número inválido ou não suportado: {}", telefone);
            return;
        }
        try {
            String url = String.format(TWILIO_API, twilioAccountSid.trim());
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("To", "whatsapp:" + toE164);
            body.add("From", twilioWhatsappFrom.trim().startsWith("whatsapp:") ? twilioWhatsappFrom.trim() : "whatsapp:" + twilioWhatsappFrom.trim());
            body.add("Body", texto != null ? texto : "");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            String auth = twilioAccountSid.trim() + ":" + twilioAuthToken.trim();
            headers.set("Authorization", "Basic " + Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8)));
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
            RestTemplate rest = new RestTemplate();
            rest.postForObject(url, request, String.class);
            log.info("WhatsApp enviado para {}", toE164);
        } catch (Exception e) {
            log.warn("Falha ao enviar WhatsApp para {}: {} - {}", telefone, e.getClass().getSimpleName(), e.getMessage());
            if (log.isDebugEnabled()) {
                log.debug("Detalhe da exceção WhatsApp", e);
            }
        }
    }

    /** Normaliza telefone para E.164 (Brasil: +55 + DDD + número). Apenas dígitos; se 10 ou 11 dígitos, adiciona +55. */
    private static String normalizarTelefoneWhatsApp(String telefone) {
        if (telefone == null || telefone.isBlank()) return null;
        String digits = ONLY_DIGITS.matcher(telefone).replaceAll("");
        if (digits.startsWith("55") && (digits.length() == 12 || digits.length() == 13)) {
            return "+" + digits;
        }
        if (digits.length() == 10 || digits.length() == 11) {
            return "+55" + digits;
        }
        return null;
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
                log.info("Web Push: enviando '{}' para {} (id={}), {} dispositivo(s) inscrito(s).", titulo, usuario.getEmail(), usuario.getId(), subs.size());
                for (PushSubscription sub : subs) {
                    enviarPush(sub, titulo, corpo);
                }
            }
        }
        if (Boolean.TRUE.equals(empresa.getNotificacaoWhatsApp()) && usuario.getTelefone() != null && !usuario.getTelefone().isBlank()) {
            enviarWhatsApp(usuario.getTelefone(), titulo + "\n\n" + corpo);
        } else if (Boolean.TRUE.equals(empresa.getNotificacaoWhatsApp()) && (usuario.getTelefone() == null || usuario.getTelefone().isBlank())) {
            log.debug("WhatsApp não enviado para usuário id={}: telefone não cadastrado.", usuario.getId());
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

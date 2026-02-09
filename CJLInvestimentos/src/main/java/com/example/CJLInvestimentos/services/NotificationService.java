package com.example.CJLInvestimentos.services;

import com.example.CJLInvestimentos.entities.Empresa;
import com.example.CJLInvestimentos.entities.PushSubscription;
import com.example.CJLInvestimentos.entities.Recomendacao;
import com.example.CJLInvestimentos.entities.User;
import com.example.CJLInvestimentos.repositories.PushSubscriptionRepository;
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

    public NotificationService(
            @org.springframework.beans.factory.annotation.Autowired(required = false) JavaMailSender mailSender,
            PushSubscriptionRepository pushSubscriptionRepository) {
        this.mailSender = mailSender;
        this.pushSubscriptionRepository = pushSubscriptionRepository;
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

    /** Envia e-mail (se configurado). */
    public void enviarEmail(String para, String assunto, String corpo) {
        if (mailSender == null || emailFrom == null || emailFrom.isBlank()) {
            log.debug("E-mail não configurado, ignorando envio.");
            return;
        }
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(emailFrom);
            msg.setTo(para);
            msg.setSubject(assunto);
            msg.setText(corpo);
            mailSender.send(msg);
            log.debug("E-mail enviado para {}", para);
        } catch (Exception e) {
            log.warn("Falha ao enviar e-mail para {}: {}", para, e.getMessage());
        }
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

    /** Notifica um usuário conforme canais habilitados na empresa. */
    public void notificarUsuario(Empresa empresa, User usuario, String titulo, String corpo) {
        if (empresa == null || usuario == null) return;

        if (Boolean.TRUE.equals(empresa.getNotificacaoEmail()) && usuario.getEmail() != null && !usuario.getEmail().isBlank()) {
            enviarEmail(usuario.getEmail(), titulo, corpo);
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
        // WhatsApp: futuro (notificacaoWhatsApp)
    }

    /** Chamado quando uma nova recomendação é criada: notificar clientes da carteira. */
    public void notificarNovaRecomendacaoParaClientes(Empresa empresa, List<User> clientes, Recomendacao rec) {
        String titulo = "Nova recomendação";
        String corpo = String.format("%s/%s - %s | Entrada: %s | Alvo: %s. Acesse o dashboard para ver detalhes.",
                rec.getMoeda(), rec.getParMoeda(), rec.getTipo(),
                rec.getPrecoEntrada() != null ? rec.getPrecoEntrada().toPlainString() : "-",
                rec.getPrecoAlvo() != null ? rec.getPrecoAlvo().toPlainString() : "-");
        for (User cliente : clientes) {
            notificarUsuario(empresa, cliente, titulo, corpo);
        }
    }

    /** Chamado quando o cliente marca recomendação como resolvida: notificar o consultor. */
    public void notificarClienteResolveuParaConsultor(Empresa empresa, User consultor, User cliente, Recomendacao rec) {
        String titulo = "Cliente resolveu recomendação";
        String corpo = String.format("%s marcou como resolvida a recomendação %s/%s (carteira %s).",
                cliente.getNome() != null ? cliente.getNome() : cliente.getEmail(),
                rec.getMoeda(), rec.getParMoeda(),
                rec.getCarteira() != null ? rec.getCarteira().getNome() : "-");
        notificarUsuario(empresa, consultor, titulo, corpo);
    }
}

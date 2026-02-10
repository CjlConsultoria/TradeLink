package com.example.CJLInvestimentos.services;

import com.example.CJLInvestimentos.entities.Recomendacao;
import org.springframework.stereotype.Service;

/**
 * Monta o HTML dos e-mails (layout responsivo único + conteúdo por tipo).
 */
@Service
public class EmailTemplateService {

    private static final String APP_NAME = "TradeLink";
    private static final String FOOTER_TEXT = "Este é um e-mail automático. Não responda diretamente.";

    /** Layout base responsivo (table-based para clientes de e-mail). */
    public String wrapInLayout(String title, String preheader, String bodyContent) {
        return """
            <!DOCTYPE html>
            <html lang="pt-BR">
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
              <meta http-equiv="X-UA-Compatible" content="IE=edge">
              <title>%s</title>
              <!--[if mso]>
              <noscript><xml><o:OfficeDocumentSettings><o:PixelsPerInch>96</o:PixelsPerInch></o:OfficeDocumentSettings></xml></noscript>
              <![endif]-->
              <style>
                body { margin: 0; padding: 0; -webkit-text-size-adjust: 100%%; -ms-text-size-adjust: 100%%; }
                table { border-collapse: collapse; mso-table-lspace: 0; mso-table-rspace: 0; }
                img { border: 0; height: auto; line-height: 100%%; outline: none; text-decoration: none; -ms-interpolation-mode: bicubic; }
                .preheader { display: none !important; visibility: hidden; max-height: 0; font-size: 1px; line-height: 1px; }
                @media only screen and (max-width: 600px) {
                  .wrapper { width: 100%% !important; max-width: 100%% !important; }
                  .content { padding: 16px !important; }
                  .btn { display: block !important; width: 100%% !important; }
                }
              </style>
            </head>
            <body style="margin:0; padding:0; background-color:#f4f4f5; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;">
              <span class="preheader">%s</span>
              <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background-color:#f4f4f5;">
                <tr><td align="center" style="padding: 24px 16px;">
                  <table class="wrapper" role="presentation" width="600" cellspacing="0" cellpadding="0" style="max-width:600px; width:100%%; background-color:#ffffff; border-radius:12px; box-shadow: 0 4px 6px rgba(0,0,0,0.07);">
                    <tr>
                      <td style="background: linear-gradient(135deg, #4f46e5 0%%, #7c3aed 100%%); padding: 28px 32px; border-radius: 12px 12px 0 0;">
                        <table width="100%%" cellspacing="0" cellpadding="0"><tr>
                          <td><h1 style="margin:0; color:#ffffff; font-size:24px; font-weight:700; letter-spacing:-0.5px;">%s</h1></td>
                        </tr></table>
                      </td>
                    </tr>
                    <tr>
                      <td class="content" style="padding: 32px; color:#374151; font-size:16px; line-height:1.6;">
                        %s
                      </td>
                    </tr>
                    <tr>
                      <td style="padding: 20px 32px; border-top: 1px solid #e5e7eb; color:#9ca3af; font-size:12px; border-radius: 0 0 12px 12px;">
                        %s
                      </td>
                    </tr>
                  </table>
                </td></tr>
              </table>
            </body>
            </html>
            """.formatted(
                escape(title),
                escape(preheader),
                escape(APP_NAME),
                bodyContent,
                FOOTER_TEXT
            );
    }

    public String buildNovoUsuario(String nome, String email, String roleDisplay) {
        String body = """
            <p style="margin:0 0 16px;">Olá, <strong>%s</strong>!</p>
            <p style="margin:0 0 20px;">Sua conta no <strong>TradeLink</strong> foi criada. Você está cadastrado como <strong>%s</strong>.</p>
            <p style="margin:0 0 24px;">Use o e-mail <strong>%s</strong> e a senha que você definiu para acessar o sistema.</p>
            <p style="margin:0 0 8px;">Acesse:</p>
            <p style="margin:0 0 24px;"><a href="https://tradelink-grun.onrender.com/login" style="display:inline-block; padding:12px 24px; background: linear-gradient(135deg, #4f46e5, #7c3aed); color:#ffffff !important; text-decoration:none; border-radius:8px; font-weight:600;">Fazer login</a></p>
            <p style="margin:0; color:#6b7280; font-size:14px;">Se você não solicitou este cadastro, ignore este e-mail.</p>
            """.formatted(escape(nome), escape(roleDisplay), escape(email));
        return wrapInLayout("Bem-vindo ao TradeLink", "Sua conta foi criada.", body);
    }

    public String buildSenhaAlterada(String nome) {
        String body = """
            <p style="margin:0 0 16px;">Olá, <strong>%s</strong>!</p>
            <p style="margin:0 0 20px;">A senha da sua conta no <strong>TradeLink</strong> foi alterada com sucesso.</p>
            <p style="margin:0 0 24px;">Se foi você quem fez a alteração, não é necessário fazer nada.</p>
            <p style="margin:0; color:#dc2626; font-size:14px;">Se você não alterou a senha, entre em contato com o administrador do sistema imediatamente.</p>
            """.formatted(escape(nome));
        return wrapInLayout("Senha alterada", "Sua senha foi alterada.", body);
    }

    public String buildNovaRecomendacao(Recomendacao rec) {
        String moeda = rec.getMoeda() != null ? rec.getMoeda() : "-";
        String par = rec.getParMoeda() != null ? rec.getParMoeda() : "-";
        String tipo = rec.getTipo() != null ? rec.getTipo().name() : "-";
        String entrada = rec.getPrecoEntrada() != null ? rec.getPrecoEntrada().toPlainString() : "-";
        String alvo = rec.getPrecoAlvo() != null ? rec.getPrecoAlvo().toPlainString() : "-";
        String body = """
            <p style="margin:0 0 16px;">Nova recomendação disponível na sua carteira.</p>
            <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="margin:20px 0; border:1px solid #e5e7eb; border-radius:8px; overflow:hidden;">
              <tr style="background:#f9fafb;"><td style="padding:12px 16px; font-weight:600; color:#6b7280;">Ativo</td><td style="padding:12px 16px;"><strong>%s / %s</strong></td></tr>
              <tr><td style="padding:12px 16px; color:#6b7280;">Tipo</td><td style="padding:12px 16px;">%s</td></tr>
              <tr style="background:#f9fafb;"><td style="padding:12px 16px; color:#6b7280;">Entrada</td><td style="padding:12px 16px;">%s</td></tr>
              <tr><td style="padding:12px 16px; color:#6b7280;">Alvo</td><td style="padding:12px 16px;">%s</td></tr>
            </table>
            <p style="margin:0 0 24px;"><a href="https://tradelink-grun.onrender.com/cliente" style="display:inline-block; padding:12px 24px; background: linear-gradient(135deg, #4f46e5, #7c3aed); color:#ffffff !important; text-decoration:none; border-radius:8px; font-weight:600;">Ver no dashboard</a></p>
            """.formatted(escape(moeda), escape(par), escape(tipo), escape(entrada), escape(alvo));
        return wrapInLayout("Nova recomendação", "Nova recomendação: " + moeda + "/" + par, body);
    }

    public String buildClienteResolveu(String nomeCliente, String moeda, String par, String carteiraNome) {
        String body = """
            <p style="margin:0 0 16px;"><strong>%s</strong> marcou como resolvida uma recomendação.</p>
            <p style="margin:0 0 12px;">Recomendação: <strong>%s / %s</strong></p>
            <p style="margin:0 0 24px;">Carteira: %s</p>
            <p style="margin:0;"><a href="https://tradelink-grun.onrender.com/consultor" style="display:inline-block; padding:12px 24px; background: linear-gradient(135deg, #4f46e5, #7c3aed); color:#ffffff !important; text-decoration:none; border-radius:8px; font-weight:600;">Abrir painel do consultor</a></p>
            """.formatted(escape(nomeCliente), escape(moeda), escape(par), escape(carteiraNome));
        return wrapInLayout("Cliente resolveu recomendação", "Cliente resolveu: " + moeda + "/" + par, body);
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}

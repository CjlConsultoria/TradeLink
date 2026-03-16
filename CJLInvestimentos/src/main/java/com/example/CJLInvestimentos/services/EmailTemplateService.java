package com.example.CJLInvestimentos.services;

import com.example.CJLInvestimentos.entities.Recomendacao;
import com.example.CJLInvestimentos.entities.User;
import com.example.CJLInvestimentos.dtos.response.ResumoRelatorioClienteResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Monta o HTML dos e-mails (layout responsivo com design premium — tema CLARO).
 * Paleta: background #f1f5f9, card #ffffff, texto #1e293b, accent #6366f1.
 */
@Service
public class EmailTemplateService {

    private static final String APP_NAME = "TradeLink";
    private static final String APP_URL = "https://tradelinkinvest.com.br";

    /** Layout base responsivo (table-based para clientes de e-mail) — TEMA CLARO. */
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
                  .content { padding: 20px !important; }
                  .btn { display: block !important; width: 100%% !important; text-align: center !important; }
                  .metric-cell { display: block !important; width: 100%% !important; padding: 8px 0 !important; }
                }
              </style>
            </head>
            <body style="margin:0; padding:0; background-color:#f1f5f9; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;">
              <span class="preheader">%s</span>
              <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background-color:#f1f5f9;">
                <tr><td align="center" style="padding: 32px 16px;">
                  <!-- Logo -->
                  <table role="presentation" width="600" cellspacing="0" cellpadding="0" style="max-width:600px; width:100%%;">
                    <tr><td align="center" style="padding-bottom:24px;">
                      <table role="presentation" cellspacing="0" cellpadding="0"><tr>
                        <td style="background:linear-gradient(135deg, #6366f1, #8b5cf6); width:40px; height:40px; border-radius:10px; text-align:center; vertical-align:middle; font-size:20px; color:#fff;">&#9651;</td>
                        <td style="padding-left:12px; color:#1e293b; font-size:22px; font-weight:700; letter-spacing:-0.5px;">%s</td>
                      </tr></table>
                    </td></tr>
                  </table>
                  <!-- Card -->
                  <table class="wrapper" role="presentation" width="600" cellspacing="0" cellpadding="0" style="max-width:600px; width:100%%; background-color:#ffffff; border-radius:16px; border:1px solid #e2e8f0; box-shadow:0 1px 3px rgba(0,0,0,0.06);">
                    <tr>
                      <td class="content" style="padding: 36px 32px; color:#334155; font-size:16px; line-height:1.7;">
                        %s
                      </td>
                    </tr>
                    <tr>
                      <td style="padding: 20px 32px; border-top: 1px solid #e2e8f0; color:#94a3b8; font-size:12px; border-radius: 0 0 16px 16px; line-height:1.6;">
                        Este e-mail foi enviado automaticamente pelo TradeLink.<br>
                        Se voce nao esperava este e-mail, pode ignora-lo com seguranca.
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
                bodyContent
            );
    }

    // ─── Helpers visuais (tema claro) ───

    private String buildButton(String text, String url, String bgColor) {
        return """
            <table role="presentation" cellspacing="0" cellpadding="0" style="margin:24px 0 8px;"><tr>
              <td class="btn" style="border-radius:10px; background:%s;">
                <a href="%s" target="_blank" style="display:inline-block; padding:14px 32px; color:#ffffff !important; text-decoration:none; font-weight:600; font-size:15px; letter-spacing:0.3px;">%s</a>
              </td>
            </tr></table>
            """.formatted(bgColor, url, text);
    }

    private String buildButton(String text, String url) {
        return buildButton(text, url, "linear-gradient(135deg, #6366f1, #8b5cf6)");
    }

    private String buildInfoRow(String label, String value, boolean highlighted) {
        String bg = highlighted ? "background-color:#f8fafc;" : "";
        return """
            <tr style="%s"><td style="padding:12px 16px; color:#64748b; font-size:14px; font-weight:500; width:40%%;">%s</td><td style="padding:12px 16px; color:#1e293b; font-size:14px; font-weight:600;">%s</td></tr>
            """.formatted(bg, label, value);
    }

    private String buildInfoTable(String[][] rows) {
        StringBuilder sb = new StringBuilder();
        sb.append("<table role=\"presentation\" width=\"100%%\" cellspacing=\"0\" cellpadding=\"0\" style=\"margin:16px 0; border:1px solid #e2e8f0; border-radius:10px; overflow:hidden;\">");
        for (int i = 0; i < rows.length; i++) {
            sb.append(buildInfoRow(rows[i][0], rows[i][1], i % 2 == 0));
        }
        sb.append("</table>");
        return sb.toString();
    }

    private String buildBadge(String text, String bgColor, String textColor) {
        return """
            <span style="display:inline-block; padding:4px 14px; border-radius:20px; font-size:13px; font-weight:600; background:%s; color:%s;">%s</span>
            """.formatted(bgColor, textColor, text);
    }

    private String buildAlertBox(String icon, String message, String bgColor, String borderColor) {
        return """
            <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="margin:16px 0;">
              <tr><td style="background:%s; border:1px solid %s; border-radius:10px; padding:16px 20px;">
                <table role="presentation" cellspacing="0" cellpadding="0"><tr>
                  <td style="font-size:20px; vertical-align:top; padding-right:12px;">%s</td>
                  <td style="color:#334155; font-size:14px; line-height:1.6;">%s</td>
                </tr></table>
              </td></tr>
            </table>
            """.formatted(bgColor, borderColor, icon, message);
    }

    // ─── Templates (tema claro) ───

    public String buildNovoUsuario(String nome, String email, String roleDisplay) {
        String roleBadge;
        if (roleDisplay.toLowerCase().contains("cliente")) {
            roleBadge = buildBadge("Cliente", "rgba(34,197,94,0.12)", "#16a34a");
        } else {
            roleBadge = buildBadge("Consultor", "rgba(99,102,241,0.12)", "#6366f1");
        }

        String body = """
            <p style="margin:0 0 8px; font-size:14px; color:#94a3b8;">Bem-vindo(a)</p>
            <h2 style="margin:0 0 20px; color:#1e293b; font-size:24px; font-weight:700;">Ola, %s!</h2>
            <p style="margin:0 0 20px; color:#475569;">Sua conta no <strong style="color:#6366f1;">TradeLink</strong> foi criada com sucesso. Voce esta cadastrado como:</p>
            <p style="margin:0 0 24px;">%s</p>
            """.formatted(escape(nome), roleBadge);

        body += buildInfoTable(new String[][]{
            {"E-mail", escape(email)},
            {"Perfil", escape(roleDisplay)}
        });

        body += buildButton("Acessar o TradeLink", APP_URL + "/login");

        body += """
            <p style="margin:16px 0 0; color:#94a3b8; font-size:13px;">Se voce nao solicitou este cadastro, ignore este e-mail.</p>
            """;

        return wrapInLayout("Bem-vindo ao TradeLink", "Sua conta foi criada com sucesso.", body);
    }

    public String buildSenhaAlterada(String nome) {
        String body = """
            <h2 style="margin:0 0 20px; color:#1e293b; font-size:22px; font-weight:700;">Senha Alterada</h2>
            <p style="margin:0 0 16px; color:#475569;">Ola, <strong style="color:#1e293b;">%s</strong>!</p>
            <p style="margin:0 0 20px; color:#475569;">A senha da sua conta no TradeLink foi alterada com sucesso.</p>
            """.formatted(escape(nome));

        body += buildAlertBox("&#9888;&#65039;",
                "<strong style='color:#d97706;'>Nao foi voce?</strong><br>Se voce nao alterou a senha, entre em contato com o administrador do sistema imediatamente.",
                "rgba(245,158,11,0.06)", "rgba(245,158,11,0.25)");

        body += buildButton("Acessar o TradeLink", APP_URL + "/login");

        return wrapInLayout("Senha alterada", "Sua senha foi alterada com sucesso.", body);
    }

    public String buildNovaRecomendacao(Recomendacao rec) {
        String moeda = rec.getMoeda() != null ? rec.getMoeda() : "-";
        String par = rec.getParMoeda() != null ? rec.getParMoeda() : "-";
        String tipoStr = rec.getTipo() != null ? rec.getTipo().name() : "-";
        String entrada = rec.getPrecoEntrada() != null ? "$" + rec.getPrecoEntrada().toPlainString() : "-";
        String alvo = rec.getPrecoAlvo() != null ? "$" + rec.getPrecoAlvo().toPlainString() : "-";

        boolean isCompra = "COMPRA".equalsIgnoreCase(tipoStr);
        String tipoBadge = isCompra
                ? buildBadge("COMPRA", "rgba(34,197,94,0.12)", "#16a34a")
                : buildBadge("VENDA", "rgba(239,68,68,0.12)", "#dc2626");

        String body = """
            <p style="margin:0 0 8px; font-size:14px; color:#94a3b8;">Nova recomendacao</p>
            <h2 style="margin:0 0 8px; color:#1e293b; font-size:24px; font-weight:700;">%s / %s</h2>
            <p style="margin:0 0 20px;">%s</p>
            <p style="margin:0 0 16px; color:#475569;">Seu consultor criou uma nova recomendacao para a sua carteira. Confira os detalhes abaixo e execute quando estiver pronto.</p>
            """.formatted(escape(moeda), escape(par), tipoBadge);

        body += buildInfoTable(new String[][]{
            {"Ativo", escape(moeda) + " / " + escape(par)},
            {"Tipo", escape(tipoStr)},
            {"Preco de Entrada", escape(entrada)},
            {"Preco Alvo", escape(alvo)}
        });

        body += buildAlertBox("&#128161;",
                "Acesse o sistema para ver todos os detalhes, incluindo quantidade sugerida e observacoes do consultor.",
                "rgba(99,102,241,0.05)", "rgba(99,102,241,0.15)");

        body += buildButton("Ver Recomendacao", APP_URL + "/cliente");

        return wrapInLayout("Nova Recomendacao: " + moeda + "/" + par,
                "Nova recomendacao de " + tipoStr + ": " + moeda + "/" + par, body);
    }

    public String buildClienteResolveu(String nomeCliente, String moeda, String par, String carteiraNome) {
        String body = """
            <p style="margin:0 0 8px; font-size:14px; color:#94a3b8;">Recomendacao resolvida</p>
            <h2 style="margin:0 0 20px; color:#1e293b; font-size:22px; font-weight:700;">%s resolveu uma recomendacao</h2>
            """.formatted(escape(nomeCliente));

        body += buildInfoTable(new String[][]{
            {"Cliente", escape(nomeCliente)},
            {"Ativo", escape(moeda) + " / " + escape(par)},
            {"Carteira", escape(carteiraNome)},
            {"Status", "Resolvida &#10003;"}
        });

        body += buildButton("Abrir Painel do Consultor", APP_URL + "/consultor");

        return wrapInLayout("Cliente resolveu: " + moeda + "/" + par,
                nomeCliente + " resolveu: " + moeda + "/" + par, body);
    }

    // ─── Novos templates ───

    /** Notificacao de portfolio critico (consultor) */
    public String buildPortfolioCritico(String nomeCliente, String carteiraNome,
                                         int totalDesvios, String maiorDesvio,
                                         List<String[]> ativos) {
        String body = """
            <p style="margin:0 0 8px; font-size:14px; color:#94a3b8;">Alerta de Portfolio</p>
            <h2 style="margin:0 0 8px; color:#dc2626; font-size:24px; font-weight:700;">&#9888; Portfolio Critico</h2>
            <p style="margin:0 0 16px; color:#475569;">O portfolio de <strong style="color:#1e293b;">%s</strong> na carteira <strong style="color:#6366f1;">%s</strong> esta em estado <strong style="color:#dc2626;">critico</strong> e necessita de atencao imediata.</p>
            """.formatted(escape(nomeCliente), escape(carteiraNome));

        body += buildAlertBox("&#128680;",
                "<strong style='color:#dc2626;'>" + totalDesvios + " ativo(s) fora da margem</strong><br>Maior desvio: <strong>" + escape(maiorDesvio) + "</strong>",
                "rgba(239,68,68,0.06)", "rgba(239,68,68,0.2)");

        if (ativos != null && !ativos.isEmpty()) {
            body += "<table role=\"presentation\" width=\"100%%\" cellspacing=\"0\" cellpadding=\"0\" style=\"margin:16px 0; border:1px solid #e2e8f0; border-radius:10px; overflow:hidden;\">";
            body += "<tr style=\"background:#f8fafc;\"><td style=\"padding:10px 16px; color:#64748b; font-size:13px; font-weight:600;\">Ativo</td><td style=\"padding:10px 16px; color:#64748b; font-size:13px; font-weight:600;\">Atual</td><td style=\"padding:10px 16px; color:#64748b; font-size:13px; font-weight:600;\">Ideal</td><td style=\"padding:10px 16px; color:#64748b; font-size:13px; font-weight:600;\">Acao</td></tr>";
            for (String[] row : ativos) {
                String acaoBadge = "COMPRAR".equalsIgnoreCase(row[3])
                        ? buildBadge("COMPRAR", "rgba(34,197,94,0.12)", "#16a34a")
                        : "VENDER".equalsIgnoreCase(row[3])
                            ? buildBadge("VENDER", "rgba(239,68,68,0.12)", "#dc2626")
                            : buildBadge("OK", "rgba(148,163,184,0.1)", "#94a3b8");
                body += "<tr><td style=\"padding:10px 16px; color:#1e293b; font-size:14px; font-weight:600;\">" + escape(row[0]) +
                        "</td><td style=\"padding:10px 16px; color:#1e293b; font-size:14px;\">" + escape(row[1]) +
                        "</td><td style=\"padding:10px 16px; color:#64748b; font-size:14px;\">" + escape(row[2]) +
                        "</td><td style=\"padding:10px 16px;\">" + acaoBadge + "</td></tr>";
            }
            body += "</table>";
        }

        body += buildButton("Abrir Painel de Rebalanceamento", APP_URL + "/consultor/rebalanceamento", "linear-gradient(135deg, #dc2626, #ef4444)");

        return wrapInLayout("Portfolio Critico: " + nomeCliente,
                "ALERTA: Portfolio de " + nomeCliente + " esta critico", body);
    }

    /** Notificacao de recomendacoes geradas em lote (consultor) */
    public String buildRecomendacoesGeradas(int totalRecomendacoes, int totalClientes, String carteiraNome) {
        String body = """
            <p style="margin:0 0 8px; font-size:14px; color:#94a3b8;">Rebalanceamento</p>
            <h2 style="margin:0 0 20px; color:#1e293b; font-size:22px; font-weight:700;">Recomendacoes Geradas com Sucesso</h2>
            <p style="margin:0 0 20px; color:#475569;">O rebalanceamento foi executado e as recomendacoes foram criadas automaticamente.</p>
            """;

        // Metricas em linha
        body += """
            <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="margin:16px 0;">
              <tr>
                <td class="metric-cell" style="width:50%%; padding-right:8px;">
                  <table role="presentation" width="100%%" cellspacing="0" cellpadding="0">
                    <tr><td style="background:rgba(99,102,241,0.06); border:1px solid rgba(99,102,241,0.15); border-radius:10px; padding:20px; text-align:center;">
                      <span style="display:block; font-size:28px; font-weight:800; color:#6366f1;">%d</span>
                      <span style="display:block; font-size:13px; color:#64748b; margin-top:4px;">Recomendacoes</span>
                    </td></tr>
                  </table>
                </td>
                <td class="metric-cell" style="width:50%%; padding-left:8px;">
                  <table role="presentation" width="100%%" cellspacing="0" cellpadding="0">
                    <tr><td style="background:rgba(34,197,94,0.06); border:1px solid rgba(34,197,94,0.15); border-radius:10px; padding:20px; text-align:center;">
                      <span style="display:block; font-size:28px; font-weight:800; color:#16a34a;">%d</span>
                      <span style="display:block; font-size:13px; color:#64748b; margin-top:4px;">Clientes Impactados</span>
                    </td></tr>
                  </table>
                </td>
              </tr>
            </table>
            """.formatted(totalRecomendacoes, totalClientes);

        if (carteiraNome != null) {
            body += "<p style=\"margin:0 0 16px; color:#64748b; font-size:14px;\">Carteira: <strong style=\"color:#1e293b;\">" + escape(carteiraNome) + "</strong></p>";
        }

        body += buildAlertBox("&#128276;",
                "Os clientes serao notificados automaticamente sobre as novas recomendacoes (se as notificacoes estiverem ativadas).",
                "rgba(99,102,241,0.05)", "rgba(99,102,241,0.15)");

        body += buildButton("Ver Recomendacoes", APP_URL + "/consultor");

        return wrapInLayout("Recomendacoes Geradas",
                totalRecomendacoes + " recomendacoes geradas para " + totalClientes + " clientes", body);
    }

    /** Notificacao de novo aporte/saque (consultor) */
    public String buildNovaMovimentacao(String nomeCliente, String carteiraNome,
                                         String tipo, String valor, String data) {
        boolean isAporte = "APORTE".equalsIgnoreCase(tipo);
        String icon = isAporte ? "&#128176;" : "&#128184;";
        String tipoBadge = isAporte
                ? buildBadge("APORTE", "rgba(34,197,94,0.12)", "#16a34a")
                : buildBadge("SAQUE", "rgba(239,68,68,0.12)", "#dc2626");

        String body = """
            <p style="margin:0 0 8px; font-size:14px; color:#94a3b8;">Movimentacao</p>
            <h2 style="margin:0 0 8px; color:#1e293b; font-size:22px; font-weight:700;">%s Nova Movimentacao</h2>
            <p style="margin:0 0 16px;">%s</p>
            <p style="margin:0 0 20px; color:#475569;">O cliente <strong style="color:#1e293b;">%s</strong> registrou uma movimentacao na carteira <strong style="color:#6366f1;">%s</strong>.</p>
            """.formatted(icon, tipoBadge, escape(nomeCliente), escape(carteiraNome));

        body += buildInfoTable(new String[][]{
            {"Cliente", escape(nomeCliente)},
            {"Carteira", escape(carteiraNome)},
            {"Tipo", escape(tipo)},
            {"Valor", escape(valor)},
            {"Data", escape(data)}
        });

        if (isAporte) {
            body += buildAlertBox("&#128161;",
                    "Apos o aporte, a alocacao do cliente pode ter mudado. Considere executar um novo rebalanceamento.",
                    "rgba(99,102,241,0.05)", "rgba(99,102,241,0.15)");
        }

        body += buildButton("Ver Carteira", APP_URL + "/consultor");

        return wrapInLayout("Movimentacao: " + tipo + " de " + valor,
                nomeCliente + " fez um " + tipo + " de " + valor, body);
    }

    /** Notificacao de operacao registrada (consultor) */
    public String buildOperacaoRegistrada(String nomeCliente, String carteiraNome,
                                           String tipoOp, String ativo, String quantidade,
                                           String precoExecutado) {
        boolean isCompra = "COMPRA".equalsIgnoreCase(tipoOp);
        String tipoBadge = isCompra
                ? buildBadge("COMPRA", "rgba(34,197,94,0.12)", "#16a34a")
                : buildBadge("VENDA", "rgba(239,68,68,0.12)", "#dc2626");

        String body = """
            <p style="margin:0 0 8px; font-size:14px; color:#94a3b8;">Operacao Executada</p>
            <h2 style="margin:0 0 8px; color:#1e293b; font-size:22px; font-weight:700;">%s Executou uma Operacao</h2>
            <p style="margin:0 0 16px;">%s</p>
            """.formatted(escape(nomeCliente), tipoBadge);

        body += buildInfoTable(new String[][]{
            {"Cliente", escape(nomeCliente)},
            {"Carteira", escape(carteiraNome)},
            {"Ativo", escape(ativo)},
            {"Quantidade", escape(quantidade)},
            {"Preco Executado", "$" + escape(precoExecutado)}
        });

        body += buildButton("Ver Detalhes", APP_URL + "/consultor");

        return wrapInLayout("Operacao: " + nomeCliente + " " + tipoOp + " " + ativo,
                nomeCliente + " executou " + tipoOp + " de " + ativo, body);
    }

    /** Resumo semanal do consultor */
    public String buildResumoSemanal(String nomeConsultor, int totalClientes,
                                      int clientesOk, int clientesAtencao, int clientesCritico,
                                      String valorTotal, int recomendacoesAtivas,
                                      int operacoesSemanais) {
        String body = """
            <p style="margin:0 0 8px; font-size:14px; color:#94a3b8;">Resumo Semanal</p>
            <h2 style="margin:0 0 8px; color:#1e293b; font-size:22px; font-weight:700;">Ola, %s!</h2>
            <p style="margin:0 0 20px; color:#475569;">Aqui esta o resumo semanal dos seus clientes no TradeLink.</p>
            """.formatted(escape(nomeConsultor));

        // Metricas 2x2
        body += """
            <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="margin:16px 0;">
              <tr>
                <td class="metric-cell" style="width:50%%; padding:4px 4px 4px 0;">
                  <table role="presentation" width="100%%" cellspacing="0" cellpadding="0">
                    <tr><td style="background:rgba(99,102,241,0.06); border:1px solid rgba(99,102,241,0.12); border-radius:10px; padding:16px; text-align:center;">
                      <span style="display:block; font-size:24px; font-weight:800; color:#6366f1;">%d</span>
                      <span style="display:block; font-size:12px; color:#64748b; margin-top:2px;">Total Clientes</span>
                    </td></tr>
                  </table>
                </td>
                <td class="metric-cell" style="width:50%%; padding:4px 0 4px 4px;">
                  <table role="presentation" width="100%%" cellspacing="0" cellpadding="0">
                    <tr><td style="background:rgba(34,197,94,0.06); border:1px solid rgba(34,197,94,0.12); border-radius:10px; padding:16px; text-align:center;">
                      <span style="display:block; font-size:24px; font-weight:800; color:#16a34a;">%s</span>
                      <span style="display:block; font-size:12px; color:#64748b; margin-top:2px;">Valor Total</span>
                    </td></tr>
                  </table>
                </td>
              </tr>
              <tr>
                <td class="metric-cell" style="width:50%%; padding:4px 4px 4px 0;">
                  <table role="presentation" width="100%%" cellspacing="0" cellpadding="0">
                    <tr><td style="background:rgba(245,158,11,0.06); border:1px solid rgba(245,158,11,0.12); border-radius:10px; padding:16px; text-align:center;">
                      <span style="display:block; font-size:24px; font-weight:800; color:#d97706;">%d</span>
                      <span style="display:block; font-size:12px; color:#64748b; margin-top:2px;">Recomendacoes Ativas</span>
                    </td></tr>
                  </table>
                </td>
                <td class="metric-cell" style="width:50%%; padding:4px 0 4px 4px;">
                  <table role="presentation" width="100%%" cellspacing="0" cellpadding="0">
                    <tr><td style="background:rgba(59,130,246,0.06); border:1px solid rgba(59,130,246,0.12); border-radius:10px; padding:16px; text-align:center;">
                      <span style="display:block; font-size:24px; font-weight:800; color:#2563eb;">%d</span>
                      <span style="display:block; font-size:12px; color:#64748b; margin-top:2px;">Operacoes na Semana</span>
                    </td></tr>
                  </table>
                </td>
              </tr>
            </table>
            """.formatted(totalClientes, escape(valorTotal), recomendacoesAtivas, operacoesSemanais);

        // Status dos clientes
        body += """
            <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="margin:12px 0;">
              <tr>
                <td style="padding:8px 16px; border-radius:8px 8px 0 0; background:#f8fafc;">
                  <table role="presentation" width="100%%" cellspacing="0" cellpadding="0"><tr>
                    <td style="color:#64748b; font-size:13px; font-weight:600;">Saude dos Portfolios</td>
                  </tr></table>
                </td>
              </tr>
              <tr>
                <td style="padding:12px 16px; border:1px solid #e2e8f0; border-radius:0 0 8px 8px;">
                  <table role="presentation" width="100%%" cellspacing="0" cellpadding="0"><tr>
                    <td style="text-align:center;">
                      <span style="color:#16a34a; font-weight:700; font-size:16px;">%d</span>
                      <span style="color:#64748b; font-size:12px;"> OK</span>
                    </td>
                    <td style="text-align:center; border-left:1px solid #e2e8f0; border-right:1px solid #e2e8f0;">
                      <span style="color:#d97706; font-weight:700; font-size:16px;">%d</span>
                      <span style="color:#64748b; font-size:12px;"> Atencao</span>
                    </td>
                    <td style="text-align:center;">
                      <span style="color:#dc2626; font-weight:700; font-size:16px;">%d</span>
                      <span style="color:#64748b; font-size:12px;"> Critico</span>
                    </td>
                  </tr></table>
                </td>
              </tr>
            </table>
            """.formatted(clientesOk, clientesAtencao, clientesCritico);

        if (clientesCritico > 0) {
            body += buildAlertBox("&#128680;",
                    "<strong style='color:#dc2626;'>" + clientesCritico + " cliente(s) em estado critico!</strong><br>Acesse o painel de rebalanceamento para analisar e gerar recomendacoes.",
                    "rgba(239,68,68,0.06)", "rgba(239,68,68,0.2)");
        }

        body += buildButton("Acessar Dashboard", APP_URL + "/consultor");

        return wrapInLayout("Resumo Semanal - TradeLink",
                "Resumo: " + totalClientes + " clientes, " + clientesCritico + " critico(s)", body);
    }

    /** E-mail de convite para novo cliente ativar conta */
    public String buildConviteCliente(String email, String token, String empresaNome) {
        String activationUrl = APP_URL + "/ativar-conta/" + token;

        String body = """
            <p style="margin:0 0 8px; font-size:14px; color:#94a3b8;">Convite</p>
            <h2 style="margin:0 0 20px; color:#1e293b; font-size:24px; font-weight:700;">Voce foi convidado!</h2>
            <p style="margin:0 0 20px; color:#475569;">
                A empresa <strong style="color:#6366f1;">%s</strong> convidou voce para
                acessar o <strong style="color:#6366f1;">TradeLink</strong>, a plataforma
                de acompanhamento e controle de investimentos.</p>
            <p style="margin:0 0 24px; color:#475569;">
                Clique no botao abaixo para ativar sua conta e completar seu cadastro:</p>
            """.formatted(escape(empresaNome));

        body += buildButton("Ativar Minha Conta", activationUrl);

        body += buildAlertBox("&#9200;",
                "Este convite expira em <strong style='color:#d97706;'>48 horas</strong>. "
                + "Caso o link expire, solicite um novo convite ao seu consultor.",
                "rgba(245,158,11,0.06)", "rgba(245,158,11,0.25)");

        body += """
            <p style="margin:16px 0 0; color:#94a3b8; font-size:13px;">
                Se voce nao esperava este convite, ignore este e-mail com seguranca.</p>
            """;

        return wrapInLayout("Convite TradeLink",
                "Voce foi convidado para o TradeLink por " + empresaNome, body);
    }

    /** E-mail informando o cliente que foi desvinculado pelo consultor */
    public String buildExclusaoCliente(String nome) {
        String nomeDisplay = (nome != null && !nome.isBlank()) ? escape(nome) : "Cliente";
        String loginUrl = APP_URL + "/login";

        String body = """
            <h1 style="margin:0 0 8px; font-size:22px; color:#1e293b;">
                Alteracao na sua conta</h1>
            <p style="margin:0 0 24px; color:#64748b; font-size:15px;">
                Ola, <strong style="color:#1e293b;">%s</strong></p>

            <p style="margin:0 0 16px; color:#475569; font-size:14px; line-height:1.6;">
                Informamos que seu consultor desvinculou sua conta do grupo de atendimento.
                Seus dados e historico de investimentos estao preservados.</p>

            <p style="margin:0 0 24px; color:#475569; font-size:14px; line-height:1.6;">
                Ao fazer login, voce encontrara opcoes para continuar gerenciando seu portfolio
                de forma independente ou baixar um relatorio completo com todo seu historico.</p>
            """.formatted(nomeDisplay);

        body += buildButton("Acessar Minha Conta", loginUrl);

        body += buildAlertBox("&#128274;",
                "Seus dados estao seguros. Nenhuma informacao foi perdida neste processo.",
                "rgba(99,102,241,0.05)", "rgba(99,102,241,0.15)");

        body += """
            <p style="margin:16px 0 0; color:#94a3b8; font-size:13px;">
                Em caso de duvidas, entre em contato com o suporte do TradeLink.</p>
            """;

        return wrapInLayout("Alteracao na sua conta TradeLink",
                "Seu consultor desvinculou sua conta. Acesse para ver suas opcoes.", body);
    }

    /** E-mail informando o cliente que foi vinculado a um novo consultor */
    public String buildVinculacaoCliente(String nome, String empresaNome) {
        String nomeDisplay = (nome != null && !nome.isBlank()) ? escape(nome) : "Cliente";
        String empresaDisplay = (empresaNome != null && !empresaNome.isBlank()) ? escape(empresaNome) : "um novo consultor";
        String loginUrl = APP_URL + "/login";

        String body = """
            <h1 style="margin:0 0 8px; font-size:22px; color:#1e293b;">
                Bem-vindo de volta!</h1>
            <p style="margin:0 0 24px; color:#64748b; font-size:15px;">
                Ola, <strong style="color:#1e293b;">%s</strong></p>

            <p style="margin:0 0 16px; color:#475569; font-size:14px; line-height:1.6;">
                Boa noticia! Sua conta foi vinculada ao grupo de atendimento de
                <strong style="color:#1e293b;">%s</strong>.</p>

            <p style="margin:0 0 24px; color:#475569; font-size:14px; line-height:1.6;">
                Voce agora tem acesso completo a todas as funcionalidades da plataforma,
                incluindo carteiras, recomendacoes e acompanhamento profissional do seu portfolio.</p>
            """.formatted(nomeDisplay, empresaDisplay);

        body += buildButton("Acessar Minha Conta", loginUrl);

        body += buildAlertBox("&#127881;",
                "Seu historico e dados foram preservados. Tudo continua como antes.",
                "rgba(16,185,129,0.06)", "rgba(16,185,129,0.2)");

        return wrapInLayout("Bem-vindo de volta ao TradeLink",
                "Sua conta foi vinculada a " + empresaDisplay + ". Acesse a plataforma.", body);
    }

    /** E-mail de boas-vindas para auto-cadastro (cliente ou consultor). */
    public String buildBoasVindasAutoCadastro(String nome, String tipo, java.time.LocalDateTime trialFim) {
        String perfil = "CONSULTOR".equalsIgnoreCase(tipo) ? "Consultor/Assessor" : "Investidor";
        String trialFimStr = trialFim != null ? trialFim.toLocalDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";

        String body = """
            <h2 style="margin:0 0 16px 0; color:#1e293b; font-size:22px; font-weight:700;">Bem-vindo ao TradeLink!</h2>
            <p style="margin:0 0 12px 0; color:#475569;">Ola, <strong style="color:#1e293b;">%s</strong>!</p>
            <p style="margin:0 0 20px 0; color:#475569;">Sua conta como <strong>%s</strong> foi criada com sucesso.</p>

            <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="margin-bottom:24px;">
              <tr><td style="background:linear-gradient(135deg, #6366f1, #8b5cf6); border-radius:12px; padding:20px;">
                <p style="margin:0 0 8px 0; color:#fff; font-size:18px; font-weight:700;">5 Dias Gratuitos</p>
                <p style="margin:0; color:rgba(255,255,255,0.9); font-size:14px;">
                  Seu periodo de teste gratuito esta ativo ate <strong>%s</strong>.
                  Explore todas as funcionalidades sem compromisso!
                </p>
              </td></tr>
            </table>

            <p style="margin:0 0 8px 0; font-weight:600; color:#1e293b;">O que voce pode fazer:</p>
            <ul style="margin:0 0 24px 0; padding-left:20px; color:#475569;">
              <li style="margin-bottom:6px;">Gerenciar seu portfolio de investimentos</li>
              <li style="margin-bottom:6px;">Acompanhar cotacoes em tempo real</li>
              <li style="margin-bottom:6px;">Gerar relatorios detalhados</li>
              <li style="margin-bottom:6px;">Receber notificacoes inteligentes</li>
            </ul>

            <table role="presentation" cellspacing="0" cellpadding="0" style="margin-bottom:16px;">
              <tr><td style="background:linear-gradient(135deg, #6366f1, #8b5cf6); border-radius:8px;">
                <a href="%s/login" style="display:inline-block; padding:14px 32px; color:#fff; text-decoration:none; font-weight:600; font-size:16px;">Acessar Minha Conta</a>
              </td></tr>
            </table>

            <p style="margin:0; color:#94a3b8; font-size:13px;">Apos o periodo de teste, escolha o plano ideal para voce.</p>
            """.formatted(
                escape(nome),
                escape(perfil),
                trialFimStr,
                APP_URL
        );

        return wrapInLayout("Bem-vindo ao TradeLink", "Sua conta foi criada! 5 dias gratuitos.", body);
    }

    /** E-mail com codigo OTP para autenticacao de dois fatores. */
    public String buildOtp(String nome, String code) {
        String nomeDisplay = (nome != null && !nome.isBlank()) ? escape(nome) : "Usuario";

        // Cada digito em uma celula separada para visual premium
        StringBuilder digits = new StringBuilder();
        digits.append("<table role=\"presentation\" cellspacing=\"0\" cellpadding=\"0\" style=\"margin:24px auto;\"><tr>");
        for (char c : code.toCharArray()) {
            digits.append("<td style=\"width:48px; height:56px; background:#f8fafc; border:2px solid #6366f1; border-radius:10px; text-align:center; vertical-align:middle; margin:0 4px; font-size:28px; font-weight:800; color:#6366f1; letter-spacing:2px;\">")
                  .append(c)
                  .append("</td><td style=\"width:8px;\"></td>");
        }
        digits.append("</tr></table>");

        String body = """
            <p style="margin:0 0 8px; font-size:14px; color:#94a3b8;">Verificacao de seguranca</p>
            <h2 style="margin:0 0 20px; color:#1e293b; font-size:24px; font-weight:700;">Codigo de Verificacao</h2>
            <p style="margin:0 0 8px; color:#475569;">Ola, <strong style="color:#1e293b;">%s</strong>!</p>
            <p style="margin:0 0 24px; color:#475569;">Use o codigo abaixo para completar seu login no TradeLink:</p>
            %s
            """.formatted(nomeDisplay, digits.toString());

        body += buildAlertBox("&#9200;",
                "Este codigo expira em <strong style='color:#d97706;'>5 minutos</strong>. "
                + "Se voce nao solicitou este codigo, ignore este e-mail.",
                "rgba(245,158,11,0.06)", "rgba(245,158,11,0.25)");

        body += """
            <p style="margin:16px 0 0; color:#94a3b8; font-size:13px;">
                Nao compartilhe este codigo com ninguem. A equipe do TradeLink nunca pedira seu codigo.</p>
            """;

        return wrapInLayout("Codigo de Verificacao - TradeLink",
                "Seu codigo de verificacao TradeLink: " + code, body);
    }

    /** E-mail de recuperação de senha com link para redefinição. */
    public String buildResetSenha(String nome, String token) {
        String nomeDisplay = (nome != null && !nome.isBlank()) ? escape(nome) : "Usuario";
        String resetUrl = APP_URL + "/reset-password?token=" + token;

        String body = """
            <p style="margin:0 0 8px; font-size:14px; color:#94a3b8;">Recuperacao de senha</p>
            <h2 style="margin:0 0 20px; color:#1e293b; font-size:24px; font-weight:700;">Redefinir Senha</h2>
            <p style="margin:0 0 8px; color:#475569;">Ola, <strong style="color:#1e293b;">%s</strong>!</p>
            <p style="margin:0 0 24px; color:#475569;">Recebemos uma solicitacao para redefinir a senha da sua conta no TradeLink. Clique no botao abaixo para criar uma nova senha:</p>
            """.formatted(nomeDisplay);

        body += buildButton("Redefinir Minha Senha", resetUrl);

        body += buildAlertBox("&#9200;",
                "Este link expira em <strong style='color:#d97706;'>30 minutos</strong>. "
                + "Se voce nao solicitou a redefinicao de senha, ignore este e-mail.",
                "rgba(245,158,11,0.06)", "rgba(245,158,11,0.25)");

        body += """
            <p style="margin:16px 0 0; color:#94a3b8; font-size:13px;">
                Se o botao nao funcionar, copie e cole o link abaixo no seu navegador:<br>
                <a href="%s" style="color:#6366f1; word-break:break-all;">%s</a></p>
            """.formatted(resetUrl, resetUrl);

        return wrapInLayout("Redefinir Senha - TradeLink",
                "Solicitacao de redefinicao de senha no TradeLink", body);
    }

    public String buildAlertaPreco(String nome, String par, String tipoAlerta,
                                     String precoAlerta, String precoAtual) {
        String nomeDisplay = (nome != null && !nome.isBlank()) ? escape(nome) : "Usuario";
        String descricao = tipoAlerta.equals("ACIMA")
                ? "atingiu ou ultrapassou o valor definido"
                : "caiu abaixo do valor definido";

        String body = """
            <p style="margin:0 0 8px; font-size:14px; color:#94a3b8;">Alerta de Preco</p>
            <h2 style="margin:0 0 20px; color:#1e293b; font-size:24px; font-weight:700;">%s</h2>
            <p style="margin:0 0 8px; color:#475569;">Ola, <strong style="color:#1e293b;">%s</strong>!</p>
            <p style="margin:0 0 24px; color:#475569;">O par <strong style="color:#1e293b;">%s</strong> %s.</p>
            """.formatted(escape(par), nomeDisplay, escape(par), descricao);

        body += buildInfoTable(new String[][] {
                { "Par", par },
                { "Tipo de Alerta", tipoAlerta.equals("ACIMA") ? "Preco Acima" : "Preco Abaixo" },
                { "Preco Alvo", precoAlerta },
                { "Preco Atual", precoAtual }
        });

        body += buildAlertBox("&#128200;",
                "Este alerta foi desativado automaticamente. Acesse a plataforma para reativa-lo ou criar novos alertas.",
                "rgba(99,102,241,0.06)", "rgba(99,102,241,0.25)");

        body += buildButton("Ver Meus Alertas", APP_URL + "/cliente/alertas-preco");

        return wrapInLayout("Alerta de Preco - TradeLink",
                "Alerta de preco disparado: " + par, body);
    }

    /** Resumo semanal enviado ao cliente toda segunda-feira. */
    public String buildRelatorioSemanal(User cliente, ResumoRelatorioClienteResponse resumo) {
        String nome = (cliente.getNome() != null && !cliente.getNome().isBlank()) ? escape(cliente.getNome()) : "Investidor";
        boolean lucro = resumo.getResultado() != null && resumo.getResultado().compareTo(BigDecimal.ZERO) >= 0;
        String resultadoFormatado = resumo.getResultado() != null ? "$" + resumo.getResultado().toPlainString() : "$0";
        String resultadoBadge = lucro
                ? buildBadge("+" + resultadoFormatado, "rgba(34,197,94,0.12)", "#16a34a")
                : buildBadge(resultadoFormatado, "rgba(239,68,68,0.12)", "#dc2626");

        String body = """
            <p style="margin:0 0 8px; font-size:14px; color:#94a3b8;">Resumo Semanal</p>
            <h2 style="margin:0 0 8px; color:#1e293b; font-size:22px; font-weight:700;">Ola, %s!</h2>
            <p style="margin:0 0 20px; color:#475569;">Aqui esta o resumo das suas operacoes no TradeLink.</p>
            """.formatted(nome);

        // Metricas 2x2
        body += """
            <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="margin:16px 0;">
              <tr>
                <td class="metric-cell" style="width:50%%; padding:4px 4px 4px 0;">
                  <table role="presentation" width="100%%" cellspacing="0" cellpadding="0">
                    <tr><td style="background:rgba(99,102,241,0.06); border:1px solid rgba(99,102,241,0.12); border-radius:10px; padding:16px; text-align:center;">
                      <span style="display:block; font-size:24px; font-weight:800; color:#6366f1;">%d</span>
                      <span style="display:block; font-size:12px; color:#64748b; margin-top:2px;">Total Operacoes</span>
                    </td></tr>
                  </table>
                </td>
                <td class="metric-cell" style="width:50%%; padding:4px 0 4px 4px;">
                  <table role="presentation" width="100%%" cellspacing="0" cellpadding="0">
                    <tr><td style="background:rgba(34,197,94,0.06); border:1px solid rgba(34,197,94,0.12); border-radius:10px; padding:16px; text-align:center;">
                      <span style="display:block; font-size:24px; font-weight:800; color:#16a34a;">%d</span>
                      <span style="display:block; font-size:12px; color:#64748b; margin-top:2px;">Compras</span>
                    </td></tr>
                  </table>
                </td>
              </tr>
              <tr>
                <td class="metric-cell" style="width:50%%; padding:4px 4px 4px 0;">
                  <table role="presentation" width="100%%" cellspacing="0" cellpadding="0">
                    <tr><td style="background:rgba(239,68,68,0.06); border:1px solid rgba(239,68,68,0.12); border-radius:10px; padding:16px; text-align:center;">
                      <span style="display:block; font-size:24px; font-weight:800; color:#dc2626;">%d</span>
                      <span style="display:block; font-size:12px; color:#64748b; margin-top:2px;">Vendas</span>
                    </td></tr>
                  </table>
                </td>
                <td class="metric-cell" style="width:50%%; padding:4px 0 4px 4px;">
                  <table role="presentation" width="100%%" cellspacing="0" cellpadding="0">
                    <tr><td style="background:%s; border:1px solid %s; border-radius:10px; padding:16px; text-align:center;">
                      <span style="display:block; font-size:24px; font-weight:800; color:%s;">%s</span>
                      <span style="display:block; font-size:12px; color:#64748b; margin-top:2px;">Resultado</span>
                    </td></tr>
                  </table>
                </td>
              </tr>
            </table>
            """.formatted(
                resumo.getTotalOperacoes(),
                resumo.getTotalCompras(),
                resumo.getTotalVendas(),
                lucro ? "rgba(34,197,94,0.06)" : "rgba(239,68,68,0.06)",
                lucro ? "rgba(34,197,94,0.12)" : "rgba(239,68,68,0.12)",
                lucro ? "#16a34a" : "#dc2626",
                escape(resultadoFormatado)
            );

        // Tabela de totais
        body += buildInfoTable(new String[][]{
            {"Total Compras", "$" + (resumo.getValorTotalCompras() != null ? resumo.getValorTotalCompras().toPlainString() : "0")},
            {"Total Vendas", "$" + (resumo.getValorTotalVendas() != null ? resumo.getValorTotalVendas().toPlainString() : "0")},
            {"Resultado Geral", resultadoFormatado}
        });

        // Resultado badge
        body += "<p style=\"margin:16px 0; text-align:center;\">" + resultadoBadge + "</p>";

        body += buildAlertBox("&#128200;",
                "Acesse o TradeLink para ver o detalhamento completo das suas operacoes, graficos de performance e recomendacoes do seu consultor.",
                "rgba(99,102,241,0.05)", "rgba(99,102,241,0.15)");

        body += buildButton("Ver Relatorio Completo", APP_URL + "/cliente");

        return wrapInLayout("Resumo Semanal - TradeLink",
                "Seu resumo semanal: " + resumo.getTotalOperacoes() + " operacoes, resultado " + resultadoFormatado, body);
    }

    // ─── Templates de Apresentação / Onboarding ───

    private String buildFeatureItem(String emoji, String titulo, String descricao) {
        return """
            <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="margin:6px 0;">
              <tr>
                <td style="width:36px; vertical-align:top; padding-top:2px; font-size:18px;">%s</td>
                <td>
                  <span style="font-weight:700; color:#1e293b; font-size:14px;">%s</span><br>
                  <span style="color:#64748b; font-size:13px;">%s</span>
                </td>
              </tr>
            </table>
            """.formatted(emoji, titulo, descricao);
    }

    private String buildSectionHeader(String texto) {
        return """
            <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="margin:24px 0 12px;">
              <tr>
                <td style="border-bottom:2px solid #6366f1; padding-bottom:6px;">
                  <span style="font-size:17px; font-weight:700; color:#1e293b;">%s</span>
                </td>
              </tr>
            </table>
            """.formatted(texto);
    }

    private String buildDownloadButtons(String linkPpt, String linkHtml) {
        return """
            <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="margin:20px 0;">
              <tr>
                <td align="center">
                  <table role="presentation" cellspacing="0" cellpadding="0"><tr>
                    <td class="btn" style="border-radius:10px; background:linear-gradient(135deg, #6366f1, #8b5cf6);">
                      <a href="%s" target="_blank" style="display:inline-block; padding:12px 24px; color:#ffffff !important; text-decoration:none; font-weight:600; font-size:14px;">&#128202; Abrir Apresentacao</a>
                    </td>
                    <td style="width:12px;"></td>
                    <td class="btn" style="border-radius:10px; background:linear-gradient(135deg, #0891B2, #0e7490);">
                      <a href="%s" target="_blank" style="display:inline-block; padding:12px 24px; color:#ffffff !important; text-decoration:none; font-weight:600; font-size:14px;">&#127760; Baixar Guia Interativo</a>
                    </td>
                  </tr></table>
                </td>
              </tr>
            </table>
            """.formatted(linkPpt, linkHtml);
    }

    /** Template de apresentacao para CONSULTOR. */
    public String buildApresentacaoConsultor(String nome, String linkPpt, String linkHtml) {
        String body = "<h1 style=\"font-size:22px; color:#1e293b; margin:0 0 8px;\">Ola, " + escape(nome) + "! &#128075;</h1>"
            + "<p style=\"color:#64748b; font-size:15px; margin:0 0 20px;\">Preparamos um guia completo para voce dominar o TradeLink e fazer a transicao da planilha para o sistema.</p>";

        body += buildAlertBox("&#128640;",
            "O TradeLink substitui suas planilhas por uma plataforma completa, segura e colaborativa. Tudo o que voce fazia em abas e formulas, agora esta automatizado.",
            "rgba(99,102,241,0.06)", "rgba(99,102,241,0.15)");

        body += buildSectionHeader("&#128203; Da Planilha para o Sistema");
        body += buildInfoTable(new String[][]{
            {"Resumo de cotacoes (aba Cotacoes)", "Dashboard com atualizacao em tempo real"},
            {"Lista de clientes (aba Clientes)", "Gestao com convite por email e licencas"},
            {"Saldo por carteira (aba Carteiras)", "Calculo automatico com graficos de alocacao"},
            {"Operacoes (linhas manuais)", "Formulario com validacao e badges visuais"},
            {"Notas e anotacoes", "Kanban de Recomendacoes com drag-and-drop"}
        });

        body += buildSectionHeader("&#9889; Suas Novas Ferramentas");
        body += buildFeatureItem("&#128203;", "Kanban de Recomendacoes", "Arraste cards entre colunas ATIVA, EXECUTADA e CANCELADA para gerenciar recomendacoes visualmente.");
        body += buildFeatureItem("&#128203;", "Copy Trading", "Replique recomendacoes em multiplas carteiras de uma vez, economizando horas de trabalho repetitivo.");
        body += buildFeatureItem("&#9878;&#65039;", "Rebalanceamento", "Monitore a saude das carteiras com badges visuais (OK, ATENCAO, CRITICO) e edite alocacoes ideais.");
        body += buildFeatureItem("&#128276;", "Alertas de Preco", "Configure alertas ACIMA ou ABAIXO e receba notificacao automatica quando o preco atingir.");
        body += buildFeatureItem("&#127777;&#65039;", "Heat Map", "Veja todas as variacoes do mercado em uma grade colorida: verde (alta), vermelho (baixa).");
        body += buildFeatureItem("&#9878;&#65039;", "Comparador de Moedas", "Compare ate 3 moedas em graficos sobrepostos por periodos de 7d, 30d, 90d ou 1 ano.");
        body += buildFeatureItem("&#129518;", "Simulador", "Teste cenarios de investimento antes de recomendar ao cliente com analises what-if.");
        body += buildFeatureItem("&#11088;", "Watchlist", "Favorite pares de moedas para acompanhamento rapido sem buscar na lista completa.");
        body += buildFeatureItem("&#128172;", "Chat de Suporte", "Comunique-se com seus clientes e com o suporte diretamente dentro do sistema.");
        body += buildFeatureItem("&#127769;", "Dark Mode", "Alterne entre modo claro e escuro com um clique. Preferencia salva automaticamente.");

        body += buildSectionHeader("&#128274; Seguranca");
        body += "<p style=\"color:#475569; font-size:14px;\">O sistema conta com autenticacao JWT, verificacao 2FA por email (OTP), controle de acesso por perfil e ativacao de conta por token. Seus dados e os de seus clientes estao protegidos.</p>";

        body += buildDownloadButtons(linkPpt, linkHtml);

        body += buildButton("Acessar o TradeLink", APP_URL + "/consultor");

        return wrapInLayout("Guia do Consultor - TradeLink",
            "Seu guia completo para dominar o TradeLink", body);
    }

    /** Template de apresentacao para CLIENTE (com consultor). */
    public String buildApresentacaoCliente(String nome, String consultorNome, String linkPpt, String linkHtml) {
        boolean plural = consultorNome != null && consultorNome.contains(",");
        String introTexto;
        if (consultorNome == null) {
            introTexto = "Preparamos este guia para voce conhecer o TradeLink e acompanhar seus investimentos.";
        } else if (plural) {
            introTexto = "Seus consultores <strong>" + escape(consultorNome) + "</strong> prepararam este guia para voce conhecer o TradeLink e acompanhar seus investimentos.";
        } else {
            introTexto = "Seu consultor <strong>" + escape(consultorNome) + "</strong> preparou este guia para voce conhecer o TradeLink e acompanhar seus investimentos.";
        }

        String body = "<h1 style=\"font-size:22px; color:#1e293b; margin:0 0 8px;\">Ola, " + escape(nome) + "! &#128075;</h1>"
            + "<p style=\"color:#64748b; font-size:15px; margin:0 0 20px;\">" + introTexto + "</p>";

        body += buildAlertBox("&#128176;",
            "O TradeLink e sua plataforma pessoal para acompanhar portfolio, receber recomendacoes e monitorar o mercado — tudo em um so lugar.",
            "rgba(16,185,129,0.06)", "rgba(16,185,129,0.15)");

        body += buildSectionHeader("&#128202; Seu Dashboard");
        body += "<p style=\"color:#475569; font-size:14px; margin:0 0 12px;\">Ao fazer login, voce vera um painel com saldo total, rentabilidade, recomendacoes pendentes do consultor e alertas ativos. Tudo consolidado em uma unica tela.</p>";

        String consultorLabel = plural ? "seus consultores gerenciam" : "seu consultor gerencia";
        body += buildSectionHeader("&#128188; O que voce pode fazer");
        body += buildFeatureItem("&#128188;", "Ver Carteiras", "Acompanhe as carteiras que " + consultorLabel + " para voce, com saldos e operacoes detalhadas.");
        body += buildFeatureItem("&#128203;", "Recomendacoes", "Receba recomendacoes do consultor e aceite ou recuse diretamente no sistema.");
        body += buildFeatureItem("&#128193;", "Meu Portfolio", "Visualize todos os seus ativos, alocacao percentual e valor total investido.");
        body += buildFeatureItem("&#128200;", "Performance", "Acompanhe rentabilidade acumulada, ROI por periodo e performance por moeda.");
        body += buildFeatureItem("&#127919;", "Metas", "Defina objetivos financeiros com valor-alvo e prazo, e acompanhe o progresso com barras visuais.");

        body += buildSectionHeader("&#128295; Ferramentas de Mercado");
        body += buildFeatureItem("&#128276;", "Alertas de Preco", "Configure alertas para ser notificado quando uma moeda atingir o preco desejado.");
        body += buildFeatureItem("&#127777;&#65039;", "Heat Map", "Veja as variacoes do mercado em grade colorida.");
        body += buildFeatureItem("&#9878;&#65039;", "Comparador", "Compare ate 3 moedas lado a lado.");
        body += buildFeatureItem("&#129518;", "Simulador", "Teste cenarios de investimento.");
        body += buildFeatureItem("&#11088;", "Watchlist", "Favorite moedas para acompanhamento rapido.");

        body += buildDownloadButtons(linkPpt, linkHtml);

        body += buildButton("Acessar o TradeLink", APP_URL + "/cliente");

        return wrapInLayout("Guia do Cliente - TradeLink",
            "Conheca o TradeLink: seu portal de investimentos", body);
    }

    /** Template de apresentacao para CLIENTE AUTO-GESTAO. */
    public String buildApresentacaoClienteAutoGestao(String nome, String linkPpt, String linkHtml) {
        String body = "<h1 style=\"font-size:22px; color:#1e293b; margin:0 0 8px;\">Ola, " + escape(nome) + "! &#128075;</h1>"
            + "<p style=\"color:#64748b; font-size:15px; margin:0 0 20px;\">Bem-vindo ao TradeLink! Como investidor auto-gestao, voce tem acesso completo a todas as ferramentas para gerenciar seus proprios investimentos.</p>";

        body += buildAlertBox("&#128640;",
            "Voce tem total autonomia: crie carteiras, gerencie seu portfolio, defina metas e utilize todas as ferramentas de mercado sem depender de um consultor.",
            "rgba(16,185,129,0.06)", "rgba(16,185,129,0.15)");

        body += buildSectionHeader("&#128202; Seu Dashboard");
        body += "<p style=\"color:#475569; font-size:14px; margin:0 0 12px;\">Ao fazer login, voce vera um painel com saldo total, rentabilidade e alertas ativos. Menu simplificado e direto para gerenciar tudo.</p>";

        body += buildSectionHeader("&#128188; Gerenciamento Completo");
        body += buildFeatureItem("&#128193;", "Meu Portfolio", "Adicione e remova ativos, visualize alocacao percentual e valor total investido.");
        body += buildFeatureItem("&#128188;", "Carteiras", "Crie e gerencie suas proprias carteiras com calculos automaticos de saldo e graficos.");
        body += buildFeatureItem("&#128200;", "Performance", "Acompanhe ROI, rentabilidade acumulada e resultado por par de moeda.");
        body += buildFeatureItem("&#127919;", "Metas", "Defina objetivos financeiros com prazo e acompanhe o progresso visualmente.");

        body += buildSectionHeader("&#128295; Ferramentas de Mercado");
        body += buildFeatureItem("&#128276;", "Alertas de Preco", "Configure ate 20 alertas para ser notificado automaticamente.");
        body += buildFeatureItem("&#127777;&#65039;", "Heat Map", "Visao panoramica do mercado com grade colorida.");
        body += buildFeatureItem("&#9878;&#65039;", "Comparador", "Compare ate 3 moedas em graficos sobrepostos.");
        body += buildFeatureItem("&#129518;", "Simulador", "Teste cenarios de investimento antes de aplicar.");
        body += buildFeatureItem("&#11088;", "Watchlist", "Favorite moedas para acompanhamento rapido.");

        body += buildSectionHeader("&#128176; Assinatura & Faturas");
        body += "<p style=\"color:#475569; font-size:14px; margin:0 0 12px;\">Gerencie sua assinatura diretamente no sistema. Visualize faturas pendentes e pagas, faca pagamentos via Stripe e acompanhe o status em tempo real.</p>";

        body += buildDownloadButtons(linkPpt, linkHtml);

        body += buildButton("Acessar o TradeLink", APP_URL + "/cliente");

        return wrapInLayout("Guia do Investidor - TradeLink",
            "Conheca o TradeLink: sua plataforma de investimentos auto-gestao", body);
    }

    // ─── Templates de Boas-Vindas Automáticas (pós-ativação) ───

    private String buildBoasVindasBanner() {
        return """
            <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="margin:0 0 20px;">
              <tr>
                <td style="background:linear-gradient(135deg,#6366f1,#8b5cf6); border-radius:12px; padding:16px 20px; text-align:center;">
                  <span style="font-size:28px;">&#127881;</span><br>
                  <span style="color:#ffffff; font-size:18px; font-weight:700;">Bem-vindo ao TradeLink!</span><br>
                  <span style="color:#e0e7ff; font-size:13px;">Este e um email automatico de boas-vindas enviado apos a ativacao da sua conta.</span>
                </td>
              </tr>
            </table>
            """;
    }

    private String buildBoasVindasFooterNote() {
        return """
            <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="margin:20px 0 0;">
              <tr>
                <td style="background:#f8fafc; border-radius:8px; padding:14px 18px; border:1px solid #e2e8f0;">
                  <span style="font-size:12px; color:#94a3b8;">&#128172; Este email foi enviado automaticamente pelo TradeLink apos a ativacao da sua conta. Voce nao precisa responde-lo. Se tiver duvidas, utilize o chat de suporte dentro da plataforma.</span>
                </td>
              </tr>
            </table>
            """;
    }

    /** Template de BOAS-VINDAS automatico para CONSULTOR. */
    public String buildBoasVindasConsultor(String nome, String linkPpt, String linkHtml) {
        String body = buildBoasVindasBanner();

        body += "<h1 style=\"font-size:22px; color:#1e293b; margin:0 0 8px;\">Ola, " + escape(nome) + "! &#128075;</h1>"
            + "<p style=\"color:#64748b; font-size:15px; margin:0 0 20px;\">Sua conta de consultor foi ativada com sucesso! Preparamos um guia completo para voce conhecer o sistema e migrar suas rotinas da planilha.</p>";

        body += buildSectionHeader("&#128640; Primeiros Passos");
        body += buildFeatureItem("1&#65039;&#8419;", "Cadastre seus clientes", "Envie convites por email ou cadastre manualmente. Eles recebem acesso automatico.");
        body += buildFeatureItem("2&#65039;&#8419;", "Crie carteiras", "Monte carteiras modelo e vincule clientes. O sistema calcula saldos e graficos.");
        body += buildFeatureItem("3&#65039;&#8419;", "Envie recomendacoes", "Use o Kanban para gerenciar suas sugestoes de compra e venda.");

        body += buildSectionHeader("&#9889; Funcionalidades Disponiveis");
        body += buildFeatureItem("&#128203;", "Kanban de Recomendacoes", "Gerencie sugestoes visualmente com drag-and-drop entre colunas.");
        body += buildFeatureItem("&#128203;", "Copy Trading", "Replique operacoes em multiplas carteiras simultaneamente.");
        body += buildFeatureItem("&#9878;&#65039;", "Rebalanceamento", "Monitore desvios de alocacao com badges visuais.");
        body += buildFeatureItem("&#128276;", "Alertas de Preco", "Receba notificacao quando um ativo atingir o preco desejado.");
        body += buildFeatureItem("&#127777;&#65039;", "Heat Map e Ferramentas", "Cotacoes em tempo real, comparador, simulador e watchlist.");

        body += buildDownloadButtons(linkPpt, linkHtml);

        body += buildAlertBox("&#128218;",
            "Baixe a apresentacao completa e o guia interativo para conhecer todas as telas e funcionalidades em detalhes.",
            "rgba(99,102,241,0.06)", "rgba(99,102,241,0.15)");

        body += buildButton("Acessar o TradeLink", APP_URL + "/consultor");

        body += buildBoasVindasFooterNote();

        return wrapInLayout("Bem-vindo ao TradeLink!",
            "Sua conta de consultor foi ativada. Conheca a plataforma!", body);
    }

    /** Template de BOAS-VINDAS automatico para CLIENTE (com consultor). */
    public String buildBoasVindasCliente(String nome, String consultorNome, String linkPpt, String linkHtml) {
        String body = buildBoasVindasBanner();

        boolean plural = consultorNome != null && consultorNome.contains(",");
        String introTexto;
        if (consultorNome == null) {
            introTexto = "Sua conta foi ativada com sucesso! O TradeLink e utilizado para gerenciar seus investimentos. Veja o que voce pode acompanhar:";
        } else if (plural) {
            introTexto = "Sua conta foi ativada com sucesso! Seus consultores <strong>" + escape(consultorNome) + "</strong> utilizam o TradeLink para gerenciar seus investimentos. Veja o que voce pode acompanhar:";
        } else {
            introTexto = "Sua conta foi ativada com sucesso! Seu consultor <strong>" + escape(consultorNome) + "</strong> utiliza o TradeLink para gerenciar seus investimentos. Veja o que voce pode acompanhar:";
        }

        body += "<h1 style=\"font-size:22px; color:#1e293b; margin:0 0 8px;\">Ola, " + escape(nome) + "! &#128075;</h1>"
            + "<p style=\"color:#64748b; font-size:15px; margin:0 0 20px;\">" + introTexto + "</p>";

        body += buildSectionHeader("&#128202; O que voce encontra no TradeLink");
        body += buildFeatureItem("&#128193;", "Meu Portfolio", "Veja todos os seus ativos, alocacao e valor total investido.");
        body += buildFeatureItem("&#128203;", "Recomendacoes", "Receba e aceite/recuse sugestoes do seu consultor.");
        body += buildFeatureItem("&#128200;", "Performance", "Acompanhe rentabilidade acumulada e resultado por ativo.");
        body += buildFeatureItem("&#127919;", "Metas", "Defina e acompanhe seus objetivos financeiros visualmente.");
        body += buildFeatureItem("&#128276;", "Alertas e Ferramentas", "Heat Map, comparador, simulador e alertas de preco.");

        body += buildDownloadButtons(linkPpt, linkHtml);

        body += buildButton("Acessar o TradeLink", APP_URL + "/cliente");

        body += buildBoasVindasFooterNote();

        return wrapInLayout("Bem-vindo ao TradeLink!",
            "Sua conta foi ativada. Acompanhe seus investimentos!", body);
    }

    /** Template de BOAS-VINDAS automatico para CLIENTE AUTO-GESTAO. */
    public String buildBoasVindasClienteAutoGestao(String nome, String linkPpt, String linkHtml) {
        String body = buildBoasVindasBanner();

        body += "<h1 style=\"font-size:22px; color:#1e293b; margin:0 0 8px;\">Ola, " + escape(nome) + "! &#128075;</h1>"
            + "<p style=\"color:#64748b; font-size:15px; margin:0 0 20px;\">Sua conta de investidor auto-gestao foi ativada! Voce tem acesso completo a todas as ferramentas para gerenciar seus proprios investimentos.</p>";

        body += buildSectionHeader("&#128640; Comece Agora");
        body += buildFeatureItem("1&#65039;&#8419;", "Monte seu portfolio", "Adicione seus ativos e acompanhe a evolucao em tempo real.");
        body += buildFeatureItem("2&#65039;&#8419;", "Defina suas metas", "Crie objetivos financeiros e acompanhe o progresso.");
        body += buildFeatureItem("3&#65039;&#8419;", "Explore o mercado", "Use Heat Map, comparador e simulador para tomar decisoes.");

        body += buildSectionHeader("&#128295; Tudo que voce precisa");
        body += buildFeatureItem("&#128193;", "Portfolio Completo", "Gerencie ativos, carteiras e alocacoes.");
        body += buildFeatureItem("&#128200;", "Performance", "ROI, rentabilidade acumulada e graficos detalhados.");
        body += buildFeatureItem("&#128276;", "Alertas de Preco", "Notificacao automatica quando um ativo atingir seu alvo.");
        body += buildFeatureItem("&#127777;&#65039;", "Ferramentas de Mercado", "Heat Map, comparador, simulador e watchlist.");

        body += buildDownloadButtons(linkPpt, linkHtml);

        body += buildButton("Acessar o TradeLink", APP_URL + "/cliente");

        body += buildBoasVindasFooterNote();

        return wrapInLayout("Bem-vindo ao TradeLink!",
            "Sua conta auto-gestao foi ativada. Gerencie seus investimentos!", body);
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}

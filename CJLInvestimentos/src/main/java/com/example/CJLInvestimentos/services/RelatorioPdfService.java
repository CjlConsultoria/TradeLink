package com.example.CJLInvestimentos.services;

import com.example.CJLInvestimentos.dtos.response.*;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@Slf4j
public class RelatorioPdfService {

    // ── Design System TradeLink ──────────────────────────────────────
    private static final Color INDIGO       = new Color(99, 102, 241);   // #6366f1
    private static final Color INDIGO_DARK  = new Color(79, 70, 229);    // #4f46e5
    private static final Color INDIGO_LIGHT = new Color(238, 242, 255);  // #eef2ff
    private static final Color SLATE_900    = new Color(30, 41, 59);     // #1e293b
    private static final Color SLATE_700    = new Color(51, 65, 85);     // #334155
    private static final Color SLATE_500    = new Color(100, 116, 139);  // #64748b
    private static final Color SLATE_200    = new Color(226, 232, 240);  // #e2e8f0
    private static final Color SLATE_50     = new Color(248, 250, 252);  // #f8fafc

    private static final Locale PT_BR = new Locale("pt", "BR");
    private static final DateTimeFormatter DATA_HORA_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", PT_BR);
    private static final DateTimeFormatter DATA_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy", PT_BR);

    // ── Fonts ────────────────────────────────────────────────────────
    private static final Font FONT_BRAND     = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.WHITE);
    private static final Font FONT_BRAND_SUB = FontFactory.getFont(FontFactory.HELVETICA, 9, new Color(199, 210, 254));
    private static final Font FONT_TITULO    = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, SLATE_900);
    private static final Font FONT_SUBTITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, SLATE_900);
    private static final Font FONT_NORMAL    = FontFactory.getFont(FontFactory.HELVETICA, 9, SLATE_700);
    private static final Font FONT_HEADER    = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE);
    private static final Font FONT_CELL      = FontFactory.getFont(FontFactory.HELVETICA, 8, SLATE_700);
    private static final Font FONT_SMALL     = FontFactory.getFont(FontFactory.HELVETICA, 8, SLATE_500);

    private static String fmt(BigDecimal v) {
        if (v == null) return "-";
        return String.format(PT_BR, "R$ %,.2f", v);
    }

    private static String fmt(LocalDateTime dt) {
        if (dt == null) return "-";
        return dt.format(DATA_HORA_FMT);
    }

    // ── Header TradeLink (indigo bar) ────────────────────────────────
    private static void addTradelinkHeader(Document doc) throws DocumentException {
        PdfPTable bar = new PdfPTable(1);
        bar.setWidthPercentage(100f);
        bar.setSpacingAfter(16f);
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(INDIGO);
        cell.setPadding(14f);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.addElement(new Paragraph("TradeLink", FONT_BRAND));
        cell.addElement(new Paragraph("Plataforma de investimentos e consultoria", FONT_BRAND_SUB));
        bar.addCell(cell);
        doc.add(bar);
    }

    // ── Rodapé padrão ────────────────────────────────────────────────
    private static void addRodape(Document doc) throws DocumentException {
        PdfPTable footerBar = new PdfPTable(1);
        footerBar.setWidthPercentage(100f);
        footerBar.setSpacingBefore(20f);
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.TOP);
        cell.setBorderColor(SLATE_200);
        cell.setBorderWidth(0.5f);
        cell.setPaddingTop(6f);
        cell.addElement(new Paragraph("Documento gerado automaticamente pelo TradeLink. Este relatório não constitui recomendação de investimento.", FONT_SMALL));
        footerBar.addCell(cell);
        doc.add(footerBar);
    }

    /** PDF: Relatório de operações do cliente (histórico). */
    public byte[] gerarPdfOperacoesCliente(List<RelatorioClienteOperacaoResponse> operacoes,
                                            String dataDe, String dataAte) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            Document doc = new Document(PageSize.A4.rotate(), 24, 24, 24, 24);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            addTradelinkHeader(doc);

            doc.add(new Paragraph("Relatório de Operações — Cliente", FONT_TITULO));
            doc.add(new Paragraph("Período: " + (dataDe != null ? dataDe : "início") + " a " + (dataAte != null ? dataAte : "hoje"), FONT_NORMAL));
            doc.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100f);
            table.setWidths(new float[]{1.2f, 1.2f, 1.2f, 0.8f, 1f, 0.8f, 1.2f});
            addHeader(table, "Data", "Carteira", "Par", "Tipo", "Preço", "Qtd", "Valor");

            int row = 0;
            for (RelatorioClienteOperacaoResponse op : operacoes) {
                Color bg = (row++ % 2 == 0) ? Color.WHITE : SLATE_50;
                addCell(table, fmt(op.getDataExecucao()), bg);
                addCell(table, op.getCarteiraNome() != null ? op.getCarteiraNome() : "-", bg);
                addCell(table, op.getRecomendacaoMoedaPar() != null ? op.getRecomendacaoMoedaPar() : "-", bg);
                addCell(table, op.getTipo() != null ? op.getTipo().name() : "-", bg);
                addCell(table, fmt(op.getPrecoExecutado()), bg);
                addCell(table, op.getQuantidade() != null ? op.getQuantidade().toPlainString() : "-", bg);
                addCell(table, fmt(op.getValorOperacao()), bg);
            }
            doc.add(table);
            doc.add(new Paragraph("Total de operações: " + operacoes.size(), FONT_SMALL));

            addRodape(doc);
            doc.close();
            return baos.toByteArray();
        } catch (DocumentException e) {
            log.error("Erro ao gerar PDF operações cliente", e);
            throw new RuntimeException("Erro ao gerar PDF", e);
        }
    }

    /** PDF: Resumo (ganho/perda) do cliente. */
    public byte[] gerarPdfResumoCliente(ResumoRelatorioClienteResponse resumo,
                                        String dataDe, String dataAte) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            addTradelinkHeader(doc);

            doc.add(new Paragraph("Resumo de Ganhos e Perdas — Cliente", FONT_TITULO));
            doc.add(new Paragraph("Período: " + (dataDe != null ? dataDe : "início") + " a " + (dataAte != null ? dataAte : "hoje"), FONT_NORMAL));
            doc.add(new Paragraph(" "));

            doc.add(new Paragraph("Totais", FONT_SUBTITULO));
            PdfPTable totais = new PdfPTable(2);
            totais.setWidthPercentage(60f);
            addInfoRow(totais, "Total investido (compras):", fmt(resumo != null ? resumo.getValorTotalCompras() : null));
            addInfoRow(totais, "Total vendido:", fmt(resumo != null ? resumo.getValorTotalVendas() : null));
            addInfoRow(totais, "Resultado (ganho/perda):", fmt(resumo != null ? resumo.getResultado() : null));
            addInfoRow(totais, "Operações (compras/vendas):", (resumo != null ? resumo.getTotalCompras() : 0) + " / " + (resumo != null ? resumo.getTotalVendas() : 0));
            doc.add(totais);
            doc.add(new Paragraph(" "));

            List<ResumoRelatorioClienteResponse.PerdaGanhoPorMoeda> porMoeda = resumo != null && resumo.getPerdasGanhosPorMoeda() != null
                    ? resumo.getPerdasGanhosPorMoeda() : List.of();
            doc.add(new Paragraph("Resultado por Moeda/Par", FONT_SUBTITULO));
            doc.add(new Paragraph(" "));
            PdfPTable tableMoeda = new PdfPTable(4);
            tableMoeda.setWidthPercentage(100f);
            addHeader(tableMoeda, "Par", "Total compras", "Total vendas", "Resultado");

            int row = 0;
            for (ResumoRelatorioClienteResponse.PerdaGanhoPorMoeda item : porMoeda) {
                Color bg = (row++ % 2 == 0) ? Color.WHITE : SLATE_50;
                addCell(tableMoeda, item.getMoedaPar() != null ? item.getMoedaPar() : "-", bg);
                addCell(tableMoeda, fmt(item.getValorTotalCompras()), bg);
                addCell(tableMoeda, fmt(item.getValorTotalVendas()), bg);
                addCell(tableMoeda, fmt(item.getResultado()), bg);
            }
            doc.add(tableMoeda);
            if (porMoeda.isEmpty()) {
                doc.add(new Paragraph("Nenhum dado por moeda no período.", FONT_NORMAL));
            }

            addRodape(doc);
            doc.close();
            return baos.toByteArray();
        } catch (DocumentException e) {
            log.error("Erro ao gerar PDF resumo cliente", e);
            throw new RuntimeException("Erro ao gerar PDF", e);
        }
    }

    /** PDF: Relatório de operações do consultor (histórico de todos os clientes). */
    public byte[] gerarPdfOperacoesConsultor(List<RelatorioConsultorResponse> operacoes,
                                             String dataDe, String dataAte) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            Document doc = new Document(PageSize.A4.rotate(), 24, 24, 24, 24);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            addTradelinkHeader(doc);

            doc.add(new Paragraph("Relatório de Operações — Consultor", FONT_TITULO));
            doc.add(new Paragraph("Período: " + (dataDe != null ? dataDe : "início") + " a " + (dataAte != null ? dataAte : "hoje"), FONT_NORMAL));
            doc.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(9);
            table.setWidthPercentage(100f);
            table.setWidths(new float[]{1.2f, 1f, 1f, 1f, 0.7f, 0.9f, 0.6f, 1f, 0.5f});
            addHeader(table, "Data", "Cliente", "Carteira", "Par", "Tipo", "Preço", "Qtd", "Valor", "Res.");

            int row = 0;
            for (RelatorioConsultorResponse op : operacoes) {
                Color bg = (row++ % 2 == 0) ? Color.WHITE : SLATE_50;
                addCell(table, fmt(op.getDataExecucao()), bg);
                addCell(table, op.getClienteNome() != null ? op.getClienteNome() : "-", bg);
                addCell(table, op.getCarteiraNome() != null ? op.getCarteiraNome() : "-", bg);
                addCell(table, op.getRecomendacaoMoedaPar() != null ? op.getRecomendacaoMoedaPar() : "-", bg);
                addCell(table, op.getTipo() != null ? op.getTipo().name() : "-", bg);
                addCell(table, fmt(op.getPrecoExecutado()), bg);
                addCell(table, op.getQuantidade() != null ? op.getQuantidade().toPlainString() : "-", bg);
                addCell(table, fmt(op.getValorOperacao()), bg);
                addCell(table, Boolean.TRUE.equals(op.getRecomendacaoResolvidaPeloCliente()) ? "Sim" : "-", bg);
            }
            doc.add(table);
            doc.add(new Paragraph("Total de operações: " + operacoes.size(), FONT_SMALL));

            addRodape(doc);
            doc.close();
            return baos.toByteArray();
        } catch (DocumentException e) {
            log.error("Erro ao gerar PDF operações consultor", e);
            throw new RuntimeException("Erro ao gerar PDF", e);
        }
    }

    /** PDF: Resumo completo do consultor (totais, por carteira, por cliente, por moeda, resolvidas, perdas/ganhos). */
    public byte[] gerarPdfResumoConsultor(ResumoRelatorioResponse resumo,
                                          String dataDe, String dataAte) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            addTradelinkHeader(doc);

            doc.add(new Paragraph("Resumo Completo — Consultor", FONT_TITULO));
            doc.add(new Paragraph("Período: " + (dataDe != null ? dataDe : "início") + " a " + (dataAte != null ? dataAte : "hoje"), FONT_NORMAL));
            doc.add(new Paragraph(" "));

            long totalOp = resumo != null && resumo.getTotalOperacoes() != null ? resumo.getTotalOperacoes() : 0;
            long totalC = resumo != null && resumo.getTotalCompras() != null ? resumo.getTotalCompras() : 0;
            long totalV = resumo != null && resumo.getTotalVendas() != null ? resumo.getTotalVendas() : 0;

            // Summary highlight box
            PdfPTable sumBox = new PdfPTable(1);
            sumBox.setWidthPercentage(100f);
            sumBox.setSpacingAfter(16f);
            PdfPCell sumCell = new PdfPCell(new Phrase(
                    totalOp + " operações  ·  " + totalC + " compras  ·  " + totalV + " vendas",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, INDIGO_DARK)));
            sumCell.setPadding(10f);
            sumCell.setBackgroundColor(INDIGO_LIGHT);
            sumCell.setBorderWidth(0.5f);
            sumCell.setBorderColor(INDIGO);
            sumBox.addCell(sumCell);
            doc.add(sumBox);

            // Por carteira
            if (resumo != null && resumo.getPorCarteira() != null && !resumo.getPorCarteira().isEmpty()) {
                doc.add(new Paragraph("Por Carteira", FONT_SUBTITULO));
                doc.add(new Paragraph(" "));
                PdfPTable t = new PdfPTable(2);
                t.setWidthPercentage(80f);
                addHeader(t, "Carteira", "Operações");
                int row = 0;
                for (ResumoRelatorioResponse.ResumoPorCarteira r : resumo.getPorCarteira()) {
                    Color bg = (row++ % 2 == 0) ? Color.WHITE : SLATE_50;
                    addCell(t, r.getCarteiraNome() != null ? r.getCarteiraNome() : "-", bg);
                    addCell(t, String.valueOf(r.getTotal() != null ? r.getTotal() : 0), bg);
                }
                doc.add(t);
                doc.add(new Paragraph(" "));
            }

            // Por cliente
            if (resumo != null && resumo.getPorCliente() != null && !resumo.getPorCliente().isEmpty()) {
                doc.add(new Paragraph("Por Cliente", FONT_SUBTITULO));
                doc.add(new Paragraph(" "));
                PdfPTable t = new PdfPTable(2);
                t.setWidthPercentage(80f);
                addHeader(t, "Cliente", "Operações");
                int row = 0;
                for (ResumoRelatorioResponse.ResumoPorCliente r : resumo.getPorCliente()) {
                    Color bg = (row++ % 2 == 0) ? Color.WHITE : SLATE_50;
                    addCell(t, r.getClienteNome() != null ? r.getClienteNome() : "-", bg);
                    addCell(t, String.valueOf(r.getTotal() != null ? r.getTotal() : 0), bg);
                }
                doc.add(t);
                doc.add(new Paragraph(" "));
            }

            // Por moeda
            if (resumo != null && resumo.getPorMoeda() != null && !resumo.getPorMoeda().isEmpty()) {
                doc.add(new Paragraph("Por Moeda/Par", FONT_SUBTITULO));
                doc.add(new Paragraph(" "));
                PdfPTable t = new PdfPTable(2);
                t.setWidthPercentage(60f);
                addHeader(t, "Par", "Operações");
                int row = 0;
                for (ResumoRelatorioResponse.ResumoPorMoeda r : resumo.getPorMoeda()) {
                    Color bg = (row++ % 2 == 0) ? Color.WHITE : SLATE_50;
                    addCell(t, r.getMoedaPar() != null ? r.getMoedaPar() : "-", bg);
                    addCell(t, String.valueOf(r.getTotal() != null ? r.getTotal() : 0), bg);
                }
                doc.add(t);
                doc.add(new Paragraph(" "));
            }

            // Recomendações resolvidas
            if (resumo != null && resumo.getRecomendacoesResolvidas() != null && !resumo.getRecomendacoesResolvidas().isEmpty()) {
                doc.add(new Paragraph("Recomendações Resolvidas pelos Clientes", FONT_SUBTITULO));
                doc.add(new Paragraph(" "));
                PdfPTable t = new PdfPTable(4);
                t.setWidthPercentage(100f);
                addHeader(t, "Data", "Cliente", "Carteira", "Par");
                int row = 0;
                for (ResumoRelatorioResponse.RecomendacaoResolvidaItem r : resumo.getRecomendacoesResolvidas()) {
                    Color bg = (row++ % 2 == 0) ? Color.WHITE : SLATE_50;
                    addCell(t, r.getResolvidoEm() != null ? r.getResolvidoEm().format(DATA_HORA_FMT) : "-", bg);
                    addCell(t, r.getClienteNome() != null ? r.getClienteNome() : "-", bg);
                    addCell(t, r.getCarteiraNome() != null ? r.getCarteiraNome() : "-", bg);
                    addCell(t, r.getRecomendacaoMoedaPar() != null ? r.getRecomendacaoMoedaPar() : "-", bg);
                }
                doc.add(t);
                doc.add(new Paragraph(" "));
            }

            // Perdas e ganhos
            if (resumo != null && resumo.getPerdasGanhosPorClienteMoeda() != null && !resumo.getPerdasGanhosPorClienteMoeda().isEmpty()) {
                doc.add(new Paragraph("Perdas e Ganhos por Cliente e Moeda", FONT_SUBTITULO));
                doc.add(new Paragraph(" "));
                PdfPTable t = new PdfPTable(5);
                t.setWidthPercentage(100f);
                addHeader(t, "Cliente", "Par", "Compras", "Vendas", "Resultado");
                int row = 0;
                for (ResumoRelatorioResponse.PerdaGanhoClienteMoeda r : resumo.getPerdasGanhosPorClienteMoeda()) {
                    Color bg = (row++ % 2 == 0) ? Color.WHITE : SLATE_50;
                    addCell(t, r.getClienteNome() != null ? r.getClienteNome() : "-", bg);
                    addCell(t, r.getMoedaPar() != null ? r.getMoedaPar() : "-", bg);
                    addCell(t, fmt(r.getValorTotalCompras()), bg);
                    addCell(t, fmt(r.getValorTotalVendas()), bg);
                    addCell(t, fmt(r.getResultado()), bg);
                }
                doc.add(t);
            }

            addRodape(doc);
            doc.close();
            return baos.toByteArray();
        } catch (DocumentException e) {
            log.error("Erro ao gerar PDF resumo consultor", e);
            throw new RuntimeException("Erro ao gerar PDF", e);
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────

    private static void addHeader(PdfPTable table, String... headers) {
        for (String h : headers) {
            PdfPCell c = new PdfPCell(new Phrase(h, FONT_HEADER));
            c.setBackgroundColor(INDIGO);
            c.setPadding(5);
            c.setHorizontalAlignment(Element.ALIGN_CENTER);
            c.setBorderWidth(0.5f);
            c.setBorderColor(INDIGO_DARK);
            table.addCell(c);
        }
    }

    private static void addInfoRow(PdfPTable table, String label, String value) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, SLATE_700)));
        c1.setPadding(5);
        c1.setBorderWidth(0.5f);
        c1.setBorderColor(SLATE_200);
        table.addCell(c1);

        PdfPCell c2 = new PdfPCell(new Phrase(value != null ? value : "-", FontFactory.getFont(FontFactory.HELVETICA, 10, SLATE_900)));
        c2.setPadding(5);
        c2.setBorderWidth(0.5f);
        c2.setBorderColor(SLATE_200);
        table.addCell(c2);
    }

    private static void addCell(PdfPTable table, String text, Color bg) {
        PdfPCell c = new PdfPCell(new Phrase(text != null ? text : "-", FONT_CELL));
        c.setPadding(4);
        c.setBorderWidth(0.5f);
        c.setBorderColor(SLATE_200);
        c.setBackgroundColor(bg);
        table.addCell(c);
    }
}

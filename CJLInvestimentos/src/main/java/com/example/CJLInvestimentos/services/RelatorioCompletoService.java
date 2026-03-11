package com.example.CJLInvestimentos.services;

import com.example.CJLInvestimentos.dtos.response.RelatorioClienteOperacaoResponse;
import com.example.CJLInvestimentos.dtos.response.ResumoRelatorioClienteResponse;
import com.example.CJLInvestimentos.entities.User;
import com.example.CJLInvestimentos.repositories.UserRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Gera um PDF consolidado com todo o histórico do cliente
 * (operações, resumo de ganhos/perdas, recomendações, etc.).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RelatorioCompletoService {

    private final RelatorioClienteService relatorioClienteService;
    private final UserRepository userRepository;

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
    private static final DateTimeFormatter DATA_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy", PT_BR);
    private static final DateTimeFormatter DATA_HORA_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", PT_BR);

    // ── Fonts ────────────────────────────────────────────────────────
    private static final Font FONT_BRAND       = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 28, Color.WHITE);
    private static final Font FONT_BRAND_SUB   = FontFactory.getFont(FontFactory.HELVETICA, 12, new Color(199, 210, 254));
    private static final Font FONT_TITULO      = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, SLATE_900);
    private static final Font FONT_SUBTITULO   = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, SLATE_900);
    private static final Font FONT_NORMAL      = FontFactory.getFont(FontFactory.HELVETICA, 9, SLATE_700);
    private static final Font FONT_HEADER      = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
    private static final Font FONT_SMALL       = FontFactory.getFont(FontFactory.HELVETICA, 8, SLATE_500);
    private static final Font FONT_COVER_LABEL = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, SLATE_700);
    private static final Font FONT_COVER_VALUE = FontFactory.getFont(FontFactory.HELVETICA, 10, SLATE_900);

    private static String fmt(BigDecimal v) {
        if (v == null) return "-";
        return String.format(PT_BR, "R$ %,.2f", v);
    }

    private static String fmt(LocalDateTime dt) {
        if (dt == null) return "-";
        return dt.format(DATA_HORA_FMT);
    }

    @Transactional(readOnly = true)
    public byte[] gerarRelatorioCompleto(Long clienteId) {
        User cliente = userRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        List<RelatorioClienteOperacaoResponse> operacoes =
                relatorioClienteService.listarOperacoes(clienteId, null, null);
        ResumoRelatorioClienteResponse resumo =
                relatorioClienteService.resumo(clienteId, null, null);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            // ══════════════════════════════════════════════════════════
            // CAPA PREMIUM
            // ══════════════════════════════════════════════════════════
            doc.add(new Paragraph(" "));

            // Indigo header block on cover
            PdfPTable coverHeader = new PdfPTable(1);
            coverHeader.setWidthPercentage(100f);
            coverHeader.setSpacingAfter(30f);
            PdfPCell coverHeaderCell = new PdfPCell();
            coverHeaderCell.setBackgroundColor(INDIGO);
            coverHeaderCell.setPadding(24f);
            coverHeaderCell.setBorder(Rectangle.NO_BORDER);
            coverHeaderCell.setHorizontalAlignment(Element.ALIGN_CENTER);

            Paragraph brandP = new Paragraph("TradeLink", FONT_BRAND);
            brandP.setAlignment(Element.ALIGN_CENTER);
            coverHeaderCell.addElement(brandP);

            Paragraph subtitleP = new Paragraph("Relatório Completo de Investimentos", FONT_BRAND_SUB);
            subtitleP.setAlignment(Element.ALIGN_CENTER);
            coverHeaderCell.addElement(subtitleP);
            coverHeader.addCell(coverHeaderCell);
            doc.add(coverHeader);

            // Client info table
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(60f);
            infoTable.setHorizontalAlignment(Element.ALIGN_CENTER);
            addInfoRow(infoTable, "Cliente:", cliente.getNome() != null ? cliente.getNome() : "-");
            addInfoRow(infoTable, "CPF:", cliente.getCpf() != null ? formatCpf(cliente.getCpf()) : "-");
            addInfoRow(infoTable, "E-mail:", cliente.getEmail());
            addInfoRow(infoTable, "Gerado em:", LocalDate.now().format(DATA_FMT));
            doc.add(infoTable);

            doc.add(new Paragraph(" "));
            doc.add(new Paragraph(" "));

            Paragraph disclaimer = new Paragraph(
                    "Este relatório possui caráter exclusivamente informativo e não constitui recomendação de investimento.",
                    FONT_SMALL);
            disclaimer.setAlignment(Element.ALIGN_CENTER);
            doc.add(disclaimer);

            // ══════════════════════════════════════════════════════════
            // SEÇÃO 1: Resumo Financeiro
            // ══════════════════════════════════════════════════════════
            doc.newPage();
            addSectionHeader(doc);

            doc.add(new Paragraph("1. Resumo Financeiro", FONT_TITULO));
            doc.add(new Paragraph(" "));

            if (resumo != null) {
                PdfPTable resumoTable = new PdfPTable(2);
                resumoTable.setWidthPercentage(70f);
                addInfoRow(resumoTable, "Total de operações:", String.valueOf(resumo.getTotalOperacoes()));
                addInfoRow(resumoTable, "Compras:", String.valueOf(resumo.getTotalCompras()));
                addInfoRow(resumoTable, "Vendas:", String.valueOf(resumo.getTotalVendas()));
                addInfoRow(resumoTable, "Valor total compras:", fmt(resumo.getValorTotalCompras()));
                addInfoRow(resumoTable, "Valor total vendas:", fmt(resumo.getValorTotalVendas()));
                addInfoRow(resumoTable, "Resultado líquido:", fmt(resumo.getResultado()));
                doc.add(resumoTable);

                // Ganhos/perdas por moeda
                if (resumo.getPerdasGanhosPorMoeda() != null && !resumo.getPerdasGanhosPorMoeda().isEmpty()) {
                    doc.add(new Paragraph(" "));
                    doc.add(new Paragraph("Resultado por Par de Moeda", FONT_SUBTITULO));
                    doc.add(new Paragraph(" "));

                    PdfPTable moedaTable = new PdfPTable(4);
                    moedaTable.setWidthPercentage(100f);
                    moedaTable.setWidths(new float[]{1.5f, 1.2f, 1.2f, 1.2f});
                    addHeaderCell(moedaTable, "Par");
                    addHeaderCell(moedaTable, "Compras");
                    addHeaderCell(moedaTable, "Vendas");
                    addHeaderCell(moedaTable, "Resultado");

                    int row = 0;
                    for (ResumoRelatorioClienteResponse.PerdaGanhoPorMoeda pg : resumo.getPerdasGanhosPorMoeda()) {
                        Color bg = (row++ % 2 == 0) ? Color.WHITE : SLATE_50;
                        addCell(moedaTable, pg.getMoedaPar(), FONT_NORMAL, bg);
                        addCell(moedaTable, fmt(pg.getValorTotalCompras()), FONT_NORMAL, bg);
                        addCell(moedaTable, fmt(pg.getValorTotalVendas()), FONT_NORMAL, bg);
                        addCell(moedaTable, fmt(pg.getResultado()), FONT_NORMAL, bg);
                    }
                    doc.add(moedaTable);
                }
            } else {
                doc.add(new Paragraph("Nenhum dado financeiro disponível.", FONT_NORMAL));
            }

            // ══════════════════════════════════════════════════════════
            // SEÇÃO 2: Histórico de Operações
            // ══════════════════════════════════════════════════════════
            doc.newPage();
            addSectionHeader(doc);

            doc.add(new Paragraph("2. Histórico de Operações", FONT_TITULO));
            doc.add(new Paragraph(" "));

            if (!operacoes.isEmpty()) {
                PdfPTable opTable = new PdfPTable(7);
                opTable.setWidthPercentage(100f);
                opTable.setWidths(new float[]{1.3f, 1.2f, 1.2f, 0.7f, 1f, 0.8f, 1.1f});
                addHeaderCell(opTable, "Data");
                addHeaderCell(opTable, "Carteira");
                addHeaderCell(opTable, "Par");
                addHeaderCell(opTable, "Tipo");
                addHeaderCell(opTable, "Preço");
                addHeaderCell(opTable, "Qtd");
                addHeaderCell(opTable, "Valor");

                int row = 0;
                for (RelatorioClienteOperacaoResponse op : operacoes) {
                    Color bg = (row++ % 2 == 0) ? Color.WHITE : SLATE_50;
                    addCell(opTable, fmt(op.getDataExecucao()), FONT_NORMAL, bg);
                    addCell(opTable, op.getCarteiraNome() != null ? op.getCarteiraNome() : "-", FONT_NORMAL, bg);
                    addCell(opTable, op.getRecomendacaoMoedaPar() != null ? op.getRecomendacaoMoedaPar() : "-", FONT_NORMAL, bg);
                    addCell(opTable, op.getTipo() != null ? op.getTipo().name() : "-", FONT_NORMAL, bg);
                    addCell(opTable, fmt(op.getPrecoExecutado()), FONT_NORMAL, bg);
                    addCell(opTable, op.getQuantidade() != null ? op.getQuantidade().toPlainString() : "-", FONT_NORMAL, bg);
                    addCell(opTable, fmt(op.getValorOperacao()), FONT_NORMAL, bg);
                }
                doc.add(opTable);
                doc.add(new Paragraph("Total: " + operacoes.size() + " operação(ões)", FONT_SMALL));
            } else {
                doc.add(new Paragraph("Nenhuma operação registrada.", FONT_NORMAL));
            }

            // ══════════════════════════════════════════════════════════
            // RODAPÉ
            // ══════════════════════════════════════════════════════════
            doc.add(new Paragraph(" "));
            PdfPTable footerBar = new PdfPTable(1);
            footerBar.setWidthPercentage(100f);
            footerBar.setSpacingBefore(20f);
            PdfPCell footerCell = new PdfPCell();
            footerCell.setBorder(Rectangle.TOP);
            footerCell.setBorderColor(SLATE_200);
            footerCell.setBorderWidth(0.5f);
            footerCell.setPaddingTop(8f);
            footerCell.addElement(new Paragraph(
                    "Documento gerado automaticamente pelo TradeLink em " + LocalDateTime.now().format(DATA_HORA_FMT) + ".",
                    FONT_SMALL));
            footerCell.addElement(new Paragraph(
                    "Este relatório não constitui recomendação de investimento regulada pela CVM.",
                    FONT_SMALL));
            footerCell.addElement(new Paragraph(
                    "Os dados são de responsabilidade do cliente e/ou seu consultor.",
                    FONT_SMALL));
            footerBar.addCell(footerCell);
            doc.add(footerBar);

            doc.close();
            return baos.toByteArray();
        } catch (DocumentException e) {
            log.error("Erro ao gerar relatório completo para cliente {}", clienteId, e);
            throw new RuntimeException("Erro ao gerar relatório PDF", e);
        }
    }

    // ─── Helpers ────────────────────────────────────────────────────

    /** Thin indigo accent bar at top of each content page. */
    private void addSectionHeader(Document doc) throws DocumentException {
        PdfPTable bar = new PdfPTable(1);
        bar.setWidthPercentage(100f);
        bar.setSpacingAfter(16f);
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(INDIGO);
        cell.setFixedHeight(6f);
        cell.setBorder(Rectangle.NO_BORDER);
        bar.addCell(cell);
        doc.add(bar);
    }

    private void addInfoRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, FONT_COVER_LABEL));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(5);
        labelCell.setBackgroundColor(SLATE_50);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, FONT_COVER_VALUE));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(5);
        valueCell.setBackgroundColor(SLATE_50);
        table.addCell(valueCell);
    }

    private void addHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FONT_HEADER));
        cell.setBackgroundColor(INDIGO);
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorderWidth(0.5f);
        cell.setBorderColor(INDIGO_DARK);
        table.addCell(cell);
    }

    private void addCell(PdfPTable table, String text, Font font, Color bg) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setPadding(4);
        cell.setBorderWidth(0.5f);
        cell.setBorderColor(SLATE_200);
        table.addCell(cell);
    }

    private String formatCpf(String cpf) {
        if (cpf == null || cpf.length() != 11) return cpf != null ? cpf : "-";
        return cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "." + cpf.substring(6, 9) + "-" + cpf.substring(9);
    }
}

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

    private static final Locale PT_BR = new Locale("pt", "BR");
    private static final DateTimeFormatter DATA_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy", PT_BR);
    private static final DateTimeFormatter DATA_HORA_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", PT_BR);

    private static final Font TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Color.DARK_GRAY);
    private static final Font SUBTITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.DARK_GRAY);
    private static final Font NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 9);
    private static final Font HEADER = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
    private static final Font SMALL = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY);
    private static final Color HEADER_BG = new Color(55, 65, 81);
    private static final Color ZEBRA = new Color(249, 250, 251);

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

        // Buscar todos os dados (sem filtro de data = todo o histórico)
        List<RelatorioClienteOperacaoResponse> operacoes =
                relatorioClienteService.listarOperacoes(clienteId, null, null);
        ResumoRelatorioClienteResponse resumo =
                relatorioClienteService.resumo(clienteId, null, null);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            // === Capa ===
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph(" "));
            Paragraph titleP = new Paragraph("TradeLink", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 28, new Color(79, 70, 229)));
            titleP.setAlignment(Element.ALIGN_CENTER);
            doc.add(titleP);

            Paragraph subtitleP = new Paragraph("Relatório Completo de Investimentos", FontFactory.getFont(FontFactory.HELVETICA, 16, Color.DARK_GRAY));
            subtitleP.setAlignment(Element.ALIGN_CENTER);
            doc.add(subtitleP);
            doc.add(new Paragraph(" "));

            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(60f);
            infoTable.setHorizontalAlignment(Element.ALIGN_CENTER);
            addInfoRow(infoTable, "Cliente:", cliente.getNome() != null ? cliente.getNome() : "-");
            addInfoRow(infoTable, "CPF:", cliente.getCpf() != null ? formatCpf(cliente.getCpf()) : "-");
            addInfoRow(infoTable, "E-mail:", cliente.getEmail());
            addInfoRow(infoTable, "Gerado em:", LocalDate.now().format(DATA_FMT));
            doc.add(infoTable);

            doc.add(new Paragraph(" "));
            Paragraph disclaimer = new Paragraph(
                    "Este relatório possui caráter exclusivamente informativo e não constitui recomendação de investimento.",
                    SMALL);
            disclaimer.setAlignment(Element.ALIGN_CENTER);
            doc.add(disclaimer);

            // === Seção 1: Resumo Financeiro ===
            doc.newPage();
            doc.add(new Paragraph("1. Resumo Financeiro", TITULO));
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
                    doc.add(new Paragraph("Resultado por Par de Moeda", SUBTITULO));
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
                        Color bg = (row++ % 2 == 0) ? Color.WHITE : ZEBRA;
                        addCell(moedaTable, pg.getMoedaPar(), NORMAL, bg);
                        addCell(moedaTable, fmt(pg.getValorTotalCompras()), NORMAL, bg);
                        addCell(moedaTable, fmt(pg.getValorTotalVendas()), NORMAL, bg);
                        addCell(moedaTable, fmt(pg.getResultado()), NORMAL, bg);
                    }
                    doc.add(moedaTable);
                }
            } else {
                doc.add(new Paragraph("Nenhum dado financeiro disponível.", NORMAL));
            }

            // === Seção 2: Histórico de Operações ===
            doc.newPage();
            doc.add(new Paragraph("2. Histórico de Operações", TITULO));
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
                    Color bg = (row++ % 2 == 0) ? Color.WHITE : ZEBRA;
                    addCell(opTable, fmt(op.getDataExecucao()), NORMAL, bg);
                    addCell(opTable, op.getCarteiraNome() != null ? op.getCarteiraNome() : "-", NORMAL, bg);
                    addCell(opTable, op.getRecomendacaoMoedaPar() != null ? op.getRecomendacaoMoedaPar() : "-", NORMAL, bg);
                    addCell(opTable, op.getTipo() != null ? op.getTipo().name() : "-", NORMAL, bg);
                    addCell(opTable, fmt(op.getPrecoExecutado()), NORMAL, bg);
                    addCell(opTable, op.getQuantidade() != null ? op.getQuantidade().toPlainString() : "-", NORMAL, bg);
                    addCell(opTable, fmt(op.getValorOperacao()), NORMAL, bg);
                }
                doc.add(opTable);
                doc.add(new Paragraph("Total: " + operacoes.size() + " operação(ões)", SMALL));
            } else {
                doc.add(new Paragraph("Nenhuma operação registrada.", NORMAL));
            }

            // === Rodapé ===
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph(" "));
            Paragraph footer = new Paragraph(
                    "Documento gerado automaticamente pelo TradeLink em " + LocalDateTime.now().format(DATA_HORA_FMT) + ".\n"
                            + "Este relatório não constitui recomendação de investimento regulada pela CVM.\n"
                            + "Os dados são de responsabilidade do cliente e/ou seu consultor.",
                    SMALL);
            footer.setAlignment(Element.ALIGN_CENTER);
            doc.add(footer);

            doc.close();
            return baos.toByteArray();
        } catch (DocumentException e) {
            log.error("Erro ao gerar relatório completo para cliente {}", clienteId, e);
            throw new RuntimeException("Erro ao gerar relatório PDF", e);
        }
    }

    // ─── Helpers ────────────────────────────────────────────────────

    private void addInfoRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(4);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, FontFactory.getFont(FontFactory.HELVETICA, 10)));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(4);
        table.addCell(valueCell);
    }

    private void addHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, HEADER));
        cell.setBackgroundColor(HEADER_BG);
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private void addCell(PdfPTable table, String text, Font font, Color bg) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setPadding(4);
        table.addCell(cell);
    }

    private String formatCpf(String cpf) {
        if (cpf == null || cpf.length() != 11) return cpf != null ? cpf : "-";
        return cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "." + cpf.substring(6, 9) + "-" + cpf.substring(9);
    }
}

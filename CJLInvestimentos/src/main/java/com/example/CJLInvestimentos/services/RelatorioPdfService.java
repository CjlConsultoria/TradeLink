package com.example.CJLInvestimentos.services;

import com.example.CJLInvestimentos.dtos.response.*;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@Slf4j
public class RelatorioPdfService {

    private static final DateTimeFormatter DATA_HORA_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", new Locale("pt", "BR"));
    private static final DateTimeFormatter DATA_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy", new Locale("pt", "BR"));

    private static String fmt(BigDecimal v) {
        if (v == null) return "-";
        return String.format(new Locale("pt", "BR"), "R$ %,.2f", v);
    }

    private static String fmt(LocalDateTime dt) {
        if (dt == null) return "-";
        return dt.format(DATA_HORA_FMT);
    }

    /** PDF: Relatório de operações do cliente (histórico). */
    public byte[] gerarPdfOperacoesCliente(List<RelatorioClienteOperacaoResponse> operacoes,
                                            String dataDe, String dataAte) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            Document doc = new Document(PageSize.A4.rotate(), 24, 24, 24, 24);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Font normal = FontFactory.getFont(FontFactory.HELVETICA, 8);

            doc.add(new Paragraph("Relatório de operações — Cliente", titulo));
            doc.add(new Paragraph("Período: " + (dataDe != null ? dataDe : "início") + " a " + (dataAte != null ? dataAte : "hoje"), normal));
            doc.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100f);
            table.setWidths(new float[]{1.2f, 1.2f, 1.2f, 0.8f, 1f, 0.8f, 1.2f});
            addHeader(table, normal, "Data", "Carteira", "Par", "Tipo", "Preço", "Qtd", "Valor");
            for (RelatorioClienteOperacaoResponse op : operacoes) {
                addCell(table, fmt(op.getDataExecucao()), normal);
                addCell(table, op.getCarteiraNome() != null ? op.getCarteiraNome() : "-", normal);
                addCell(table, op.getRecomendacaoMoedaPar() != null ? op.getRecomendacaoMoedaPar() : "-", normal);
                addCell(table, op.getTipo() != null ? op.getTipo().name() : "-", normal);
                addCell(table, fmt(op.getPrecoExecutado()), normal);
                addCell(table, op.getQuantidade() != null ? op.getQuantidade().toPlainString() : "-", normal);
                addCell(table, fmt(op.getValorOperacao()), normal);
            }
            doc.add(table);
            doc.add(new Paragraph("Total de operações: " + operacoes.size(), normal));
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

            Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Font subtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
            Font normal = FontFactory.getFont(FontFactory.HELVETICA, 10);

            doc.add(new Paragraph("Resumo de ganhos e perdas — Cliente", titulo));
            doc.add(new Paragraph("Período: " + (dataDe != null ? dataDe : "início") + " a " + (dataAte != null ? dataAte : "hoje"), normal));
            doc.add(new Paragraph(" "));

            doc.add(new Paragraph("Totais", subtitulo));
            PdfPTable totais = new PdfPTable(2);
            totais.setWidthPercentage(60f);
            addRow(totais, "Total investido (compras):", fmt(resumo != null ? resumo.getValorTotalCompras() : null), normal);
            addRow(totais, "Total vendido:", fmt(resumo != null ? resumo.getValorTotalVendas() : null), normal);
            addRow(totais, "Resultado (ganho/perda):", fmt(resumo != null ? resumo.getResultado() : null), normal);
            addRow(totais, "Operações (compras/vendas):", (resumo != null ? resumo.getTotalCompras() : 0) + " / " + (resumo != null ? resumo.getTotalVendas() : 0), normal);
            doc.add(totais);
            doc.add(new Paragraph(" "));

            List<ResumoRelatorioClienteResponse.PerdaGanhoPorMoeda> porMoeda = resumo != null && resumo.getPerdasGanhosPorMoeda() != null
                    ? resumo.getPerdasGanhosPorMoeda() : List.of();
            doc.add(new Paragraph("Resultado por moeda/par", subtitulo));
            PdfPTable tableMoeda = new PdfPTable(4);
            tableMoeda.setWidthPercentage(100f);
            addHeader(tableMoeda, normal, "Par", "Total compras", "Total vendas", "Resultado");
            for (ResumoRelatorioClienteResponse.PerdaGanhoPorMoeda item : porMoeda) {
                addCell(tableMoeda, item.getMoedaPar() != null ? item.getMoedaPar() : "-", normal);
                addCell(tableMoeda, fmt(item.getValorTotalCompras()), normal);
                addCell(tableMoeda, fmt(item.getValorTotalVendas()), normal);
                addCell(tableMoeda, fmt(item.getResultado()), normal);
            }
            doc.add(tableMoeda);
            if (porMoeda.isEmpty()) {
                doc.add(new Paragraph("Nenhum dado por moeda no período.", normal));
            }
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

            Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Font normal = FontFactory.getFont(FontFactory.HELVETICA, 7);

            doc.add(new Paragraph("Relatório de operações — Consultor", titulo));
            doc.add(new Paragraph("Período: " + (dataDe != null ? dataDe : "início") + " a " + (dataAte != null ? dataAte : "hoje"), normal));
            doc.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(9);
            table.setWidthPercentage(100f);
            table.setWidths(new float[]{1.2f, 1f, 1f, 1f, 0.7f, 0.9f, 0.6f, 1f, 0.5f});
            addHeader(table, normal, "Data", "Cliente", "Carteira", "Par", "Tipo", "Preço", "Qtd", "Valor", "Res.");
            for (RelatorioConsultorResponse op : operacoes) {
                addCell(table, fmt(op.getDataExecucao()), normal);
                addCell(table, op.getClienteNome() != null ? op.getClienteNome() : "-", normal);
                addCell(table, op.getCarteiraNome() != null ? op.getCarteiraNome() : "-", normal);
                addCell(table, op.getRecomendacaoMoedaPar() != null ? op.getRecomendacaoMoedaPar() : "-", normal);
                addCell(table, op.getTipo() != null ? op.getTipo().name() : "-", normal);
                addCell(table, fmt(op.getPrecoExecutado()), normal);
                addCell(table, op.getQuantidade() != null ? op.getQuantidade().toPlainString() : "-", normal);
                addCell(table, fmt(op.getValorOperacao()), normal);
                addCell(table, Boolean.TRUE.equals(op.getRecomendacaoResolvidaPeloCliente()) ? "Sim" : "-", normal);
            }
            doc.add(table);
            doc.add(new Paragraph("Total de operações: " + operacoes.size(), normal));
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

            Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Font subtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
            Font normal = FontFactory.getFont(FontFactory.HELVETICA, 9);

            doc.add(new Paragraph("Resumo completo — Consultor", titulo));
            doc.add(new Paragraph("Período: " + (dataDe != null ? dataDe : "início") + " a " + (dataAte != null ? dataAte : "hoje"), normal));
            doc.add(new Paragraph(" "));

            long totalOp = resumo != null && resumo.getTotalOperacoes() != null ? resumo.getTotalOperacoes() : 0;
            long totalC = resumo != null && resumo.getTotalCompras() != null ? resumo.getTotalCompras() : 0;
            long totalV = resumo != null && resumo.getTotalVendas() != null ? resumo.getTotalVendas() : 0;
            doc.add(new Paragraph("Totais: " + totalOp + " operações (" + totalC + " compras, " + totalV + " vendas)", subtitulo));
            doc.add(new Paragraph(" "));

            if (resumo != null && resumo.getPorCarteira() != null && !resumo.getPorCarteira().isEmpty()) {
                doc.add(new Paragraph("Por carteira", subtitulo));
                PdfPTable t = new PdfPTable(2);
                t.setWidthPercentage(80f);
                addHeader(t, normal, "Carteira", "Operações");
                for (ResumoRelatorioResponse.ResumoPorCarteira r : resumo.getPorCarteira()) {
                    addCell(t, r.getCarteiraNome() != null ? r.getCarteiraNome() : "-", normal);
                    addCell(t, String.valueOf(r.getTotal() != null ? r.getTotal() : 0), normal);
                }
                doc.add(t);
                doc.add(new Paragraph(" "));
            }

            if (resumo != null && resumo.getPorCliente() != null && !resumo.getPorCliente().isEmpty()) {
                doc.add(new Paragraph("Por cliente", subtitulo));
                PdfPTable t = new PdfPTable(2);
                t.setWidthPercentage(80f);
                addHeader(t, normal, "Cliente", "Operações");
                for (ResumoRelatorioResponse.ResumoPorCliente r : resumo.getPorCliente()) {
                    addCell(t, r.getClienteNome() != null ? r.getClienteNome() : "-", normal);
                    addCell(t, String.valueOf(r.getTotal() != null ? r.getTotal() : 0), normal);
                }
                doc.add(t);
                doc.add(new Paragraph(" "));
            }

            if (resumo != null && resumo.getPorMoeda() != null && !resumo.getPorMoeda().isEmpty()) {
                doc.add(new Paragraph("Por moeda/par", subtitulo));
                PdfPTable t = new PdfPTable(2);
                t.setWidthPercentage(60f);
                addHeader(t, normal, "Par", "Operações");
                for (ResumoRelatorioResponse.ResumoPorMoeda r : resumo.getPorMoeda()) {
                    addCell(t, r.getMoedaPar() != null ? r.getMoedaPar() : "-", normal);
                    addCell(t, String.valueOf(r.getTotal() != null ? r.getTotal() : 0), normal);
                }
                doc.add(t);
                doc.add(new Paragraph(" "));
            }

            if (resumo != null && resumo.getRecomendacoesResolvidas() != null && !resumo.getRecomendacoesResolvidas().isEmpty()) {
                doc.add(new Paragraph("Recomendações marcadas como resolvidas pelos clientes", subtitulo));
                PdfPTable t = new PdfPTable(4);
                t.setWidthPercentage(100f);
                addHeader(t, normal, "Data", "Cliente", "Carteira", "Par");
                for (ResumoRelatorioResponse.RecomendacaoResolvidaItem r : resumo.getRecomendacoesResolvidas()) {
                    addCell(t, r.getResolvidoEm() != null ? r.getResolvidoEm().format(DATA_HORA_FMT) : "-", normal);
                    addCell(t, r.getClienteNome() != null ? r.getClienteNome() : "-", normal);
                    addCell(t, r.getCarteiraNome() != null ? r.getCarteiraNome() : "-", normal);
                    addCell(t, r.getRecomendacaoMoedaPar() != null ? r.getRecomendacaoMoedaPar() : "-", normal);
                }
                doc.add(t);
                doc.add(new Paragraph(" "));
            }

            if (resumo != null && resumo.getPerdasGanhosPorClienteMoeda() != null && !resumo.getPerdasGanhosPorClienteMoeda().isEmpty()) {
                doc.add(new Paragraph("Perdas e ganhos por cliente e moeda (vendas − compras)", subtitulo));
                PdfPTable t = new PdfPTable(5);
                t.setWidthPercentage(100f);
                addHeader(t, normal, "Cliente", "Par", "Compras", "Vendas", "Resultado");
                for (ResumoRelatorioResponse.PerdaGanhoClienteMoeda r : resumo.getPerdasGanhosPorClienteMoeda()) {
                    addCell(t, r.getClienteNome() != null ? r.getClienteNome() : "-", normal);
                    addCell(t, r.getMoedaPar() != null ? r.getMoedaPar() : "-", normal);
                    addCell(t, fmt(r.getValorTotalCompras()), normal);
                    addCell(t, fmt(r.getValorTotalVendas()), normal);
                    addCell(t, fmt(r.getResultado()), normal);
                }
                doc.add(t);
            }

            doc.close();
            return baos.toByteArray();
        } catch (DocumentException e) {
            log.error("Erro ao gerar PDF resumo consultor", e);
            throw new RuntimeException("Erro ao gerar PDF", e);
        }
    }

    private static void addHeader(PdfPTable table, Font font, String... headers) {
        for (String h : headers) {
            PdfPCell c = new PdfPCell(new Phrase(h, font));
            c.setBackgroundColor(new java.awt.Color(0xE5, 0xE7, 0xEB));
            table.addCell(c);
        }
    }

    private static void addRow(PdfPTable table, String label, String value, Font font) {
        table.addCell(new PdfPCell(new Phrase(label != null ? label : "-", font)));
        table.addCell(new PdfPCell(new Phrase(value != null ? value : "-", font)));
    }

    private static void addCell(PdfPTable table, String text, Font font) {
        table.addCell(new PdfPCell(new Phrase(text != null ? text : "-", font)));
    }
}

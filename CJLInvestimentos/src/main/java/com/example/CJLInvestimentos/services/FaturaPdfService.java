package com.example.CJLInvestimentos.services;

import com.example.CJLInvestimentos.entities.Fatura;
import com.example.CJLInvestimentos.exceptions.ResourceNotFoundException;
import com.example.CJLInvestimentos.repositories.FaturaRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class FaturaPdfService {

    private static final String NOME_EMISSOR = "TradeLink";
    private static final String SUBTITULO_EMISSOR = "Plataforma de investimentos e consultoria";
    private static final DateTimeFormatter DATA_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.forLanguageTag("pt-BR"));
    private static final DateTimeFormatter DATA_HORA_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.forLanguageTag("pt-BR"));
    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");

    // ── Design System TradeLink ──────────────────────────────────────
    private static final Color INDIGO       = new Color(99, 102, 241);   // #6366f1
    private static final Color INDIGO_DARK  = new Color(79, 70, 229);    // #4f46e5
    private static final Color INDIGO_LIGHT = new Color(238, 242, 255);  // #eef2ff
    private static final Color SLATE_900    = new Color(30, 41, 59);     // #1e293b
    private static final Color SLATE_700    = new Color(51, 65, 85);     // #334155
    private static final Color SLATE_500    = new Color(100, 116, 139);  // #64748b
    private static final Color SLATE_200    = new Color(226, 232, 240);  // #e2e8f0
    private static final Color SLATE_50     = new Color(248, 250, 252);  // #f8fafc

    private final FaturaRepository faturaRepository;

    @Transactional(readOnly = true)
    public byte[] gerarPdf(Long empresaId, Long faturaId) {
        Fatura fatura = faturaRepository.findById(faturaId)
                .orElseThrow(() -> new ResourceNotFoundException("Fatura não encontrada"));
        if (!fatura.getEmpresa().getId().equals(empresaId)) {
            throw new ResourceNotFoundException("Fatura não pertence a esta empresa");
        }
        return buildPdf(fatura);
    }

    @Transactional(readOnly = true)
    public byte[] gerarPdfParaUsuario(Long userId, Long faturaId) {
        Fatura fatura = faturaRepository.findById(faturaId)
                .orElseThrow(() -> new ResourceNotFoundException("Fatura não encontrada"));
        if (fatura.getUser() == null || !fatura.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Fatura não pertence a este usuário");
        }
        return buildPdf(fatura);
    }

    private byte[] buildPdf(Fatura fatura) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            Document doc = new Document(PageSize.A4, 40, 40, 45, 40);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            // ── Fonts ────────────────────────────────────────────────
            Font fontBrand      = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, Color.WHITE);
            Font fontBrandSub   = FontFactory.getFont(FontFactory.HELVETICA, 9, new Color(199, 210, 254)); // indigo-200
            Font fontSecao      = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, SLATE_900);
            Font fontLabel      = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, SLATE_700);
            Font fontNormal     = FontFactory.getFont(FontFactory.HELVETICA, 10, SLATE_700);
            Font fontValorGrande = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, INDIGO_DARK);
            Font fontRodape     = FontFactory.getFont(FontFactory.HELVETICA, 8, SLATE_500);
            Font fontBadge      = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);

            // ── Dados da fatura ──────────────────────────────────────
            String dataEmissao = fatura.getCreatedAt() != null
                    ? fatura.getCreatedAt().atZone(ZONE).format(DATA_HORA_FMT) : "-";
            String dataVencimento = fatura.getDataVencimento() != null
                    ? DATA_FMT.format(fatura.getDataVencimento().atZone(ZONE)) : "-";
            String dataPagamento = fatura.getDataPagamento() != null
                    ? DATA_FMT.format(fatura.getDataPagamento().atZone(ZONE)) : "-";
            String descricao = fatura.getDescricaoServico() != null && !fatura.getDescricaoServico().isBlank()
                    ? fatura.getDescricaoServico() : "Assinatura e uso da plataforma";
            String valorStr = String.format(Locale.forLanguageTag("pt-BR"), "R$ %.2f", fatura.getValor());
            String status = formatStatus(fatura.getStatus().name());
            String formaPagamento = fatura.getFormaPagamento() != null
                    ? fatura.getFormaPagamento().name() : "-";
            String nomeCliente = fatura.getEmpresa().getNome();
            String cnpjCliente = fatura.getEmpresa().getCnpj() != null ? fatura.getEmpresa().getCnpj() : "-";
            String planoNome = fatura.getEmpresa().getPlano() != null
                    ? fatura.getEmpresa().getPlano().getNome() : "-";
            String observacao = fatura.getObservacao() != null && !fatura.getObservacao().isBlank()
                    ? fatura.getObservacao() : "";
            String referencia = fatura.getReferenciaExterna() != null && !fatura.getReferenciaExterna().isBlank()
                    ? fatura.getReferenciaExterna() : "";

            // ══════════════════════════════════════════════════════════
            // HEADER — barra indigo full-width
            // ══════════════════════════════════════════════════════════
            PdfPTable headerBar = new PdfPTable(1);
            headerBar.setWidthPercentage(100f);
            headerBar.setSpacingAfter(20f);

            PdfPCell headerCell = new PdfPCell();
            headerCell.setBackgroundColor(INDIGO);
            headerCell.setPadding(16f);
            headerCell.setBorder(Rectangle.NO_BORDER);
            headerCell.addElement(new Paragraph(NOME_EMISSOR, fontBrand));
            headerCell.addElement(new Paragraph(SUBTITULO_EMISSOR, fontBrandSub));
            headerBar.addCell(headerCell);
            doc.add(headerBar);

            // ══════════════════════════════════════════════════════════
            // Número da fatura + data de emissão
            // ══════════════════════════════════════════════════════════
            PdfPTable tblNumero = new PdfPTable(2);
            tblNumero.setWidthPercentage(100f);
            tblNumero.setWidths(new float[]{1f, 1f});
            tblNumero.setSpacingAfter(16f);
            addPairCell(tblNumero, "FATURA Nº", String.valueOf(fatura.getId()), fontLabel, fontNormal, SLATE_50);
            addPairCell(tblNumero, "DATA DE EMISSÃO", dataEmissao, fontLabel, fontNormal, SLATE_50);
            doc.add(tblNumero);

            // ══════════════════════════════════════════════════════════
            // Emissor | Cliente
            // ══════════════════════════════════════════════════════════
            PdfPTable tblPartes = new PdfPTable(2);
            tblPartes.setWidthPercentage(100f);
            tblPartes.setWidths(new float[]{1f, 1f});
            tblPartes.setSpacingAfter(20f);

            PdfPCell cellEmissor = new PdfPCell();
            cellEmissor.setPadding(12f);
            cellEmissor.setBorderWidth(0.5f);
            cellEmissor.setBorderColor(SLATE_200);
            cellEmissor.setBackgroundColor(SLATE_50);
            cellEmissor.addElement(new Paragraph("EMISSOR", fontLabel));
            cellEmissor.addElement(new Paragraph(NOME_EMISSOR, fontNormal));
            cellEmissor.addElement(new Paragraph(SUBTITULO_EMISSOR, FontFactory.getFont(FontFactory.HELVETICA, 9, SLATE_500)));
            tblPartes.addCell(cellEmissor);

            PdfPCell cellCliente = new PdfPCell();
            cellCliente.setPadding(12f);
            cellCliente.setBorderWidth(0.5f);
            cellCliente.setBorderColor(SLATE_200);
            cellCliente.setBackgroundColor(SLATE_50);
            cellCliente.addElement(new Paragraph("CLIENTE / PAGADOR", fontLabel));
            cellCliente.addElement(new Paragraph(nomeCliente, fontNormal));
            cellCliente.addElement(new Paragraph("CNPJ: " + cnpjCliente, fontNormal));
            if (!"-".equals(planoNome)) {
                cellCliente.addElement(new Paragraph("Plano: " + planoNome, fontNormal));
            }
            tblPartes.addCell(cellCliente);
            doc.add(tblPartes);

            // ══════════════════════════════════════════════════════════
            // Detalhes da fatura
            // ══════════════════════════════════════════════════════════
            doc.add(new Paragraph("DETALHES DA FATURA", fontSecao));
            doc.add(Chunk.NEWLINE);

            PdfPTable tblDetalhes = new PdfPTable(2);
            tblDetalhes.setWidthPercentage(100f);
            tblDetalhes.setWidths(new float[]{35f, 65f});
            tblDetalhes.setSpacingAfter(8f);

            boolean zebra = false;
            zebra = addDetailRow(tblDetalhes, "Descrição do serviço", descricao, fontLabel, fontNormal, zebra);
            zebra = addDetailRow(tblDetalhes, "Data de vencimento", dataVencimento, fontLabel, fontNormal, zebra);
            zebra = addDetailRow(tblDetalhes, "Status", status, fontLabel, fontNormal, zebra);
            zebra = addDetailRow(tblDetalhes, "Data de pagamento", dataPagamento, fontLabel, fontNormal, zebra);
            zebra = addDetailRow(tblDetalhes, "Forma de pagamento", formaPagamento, fontLabel, fontNormal, zebra);
            if (!referencia.isEmpty()) {
                addDetailRow(tblDetalhes, "Referência (ID pagamento)", referencia, fontLabel, fontNormal, zebra);
            }
            doc.add(tblDetalhes);

            // ══════════════════════════════════════════════════════════
            // Valor total em destaque (indigo)
            // ══════════════════════════════════════════════════════════
            PdfPTable tblValor = new PdfPTable(1);
            tblValor.setWidthPercentage(100f);
            tblValor.setSpacingBefore(12f);
            tblValor.setSpacingAfter(16f);
            tblValor.setHorizontalAlignment(Element.ALIGN_RIGHT);
            PdfPCell cellValor = new PdfPCell(new Phrase("VALOR TOTAL: " + valorStr, fontValorGrande));
            cellValor.setPadding(12f);
            cellValor.setBorderWidth(1f);
            cellValor.setBorderColor(INDIGO);
            cellValor.setBackgroundColor(INDIGO_LIGHT);
            cellValor.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tblValor.addCell(cellValor);
            doc.add(tblValor);

            // ── Observações ──────────────────────────────────────────
            if (!observacao.isEmpty()) {
                doc.add(new Paragraph("Observações", fontSecao));
                doc.add(new Paragraph(observacao, fontNormal));
                doc.add(Chunk.NEWLINE);
            }

            // ══════════════════════════════════════════════════════════
            // Rodapé
            // ══════════════════════════════════════════════════════════
            PdfPTable footerBar = new PdfPTable(1);
            footerBar.setWidthPercentage(100f);
            footerBar.setSpacingBefore(24f);
            PdfPCell footerCell = new PdfPCell();
            footerCell.setBorder(Rectangle.TOP);
            footerCell.setBorderColor(SLATE_200);
            footerCell.setBorderWidth(0.5f);
            footerCell.setPaddingTop(8f);
            footerCell.addElement(new Paragraph("Documento gerado eletronicamente pelo TradeLink. Em caso de dúvidas, entre em contato com o suporte.", fontRodape));
            footerCell.addElement(new Paragraph("Este PDF é uma cópia para seus registros.", fontRodape));
            footerBar.addCell(footerCell);
            doc.add(footerBar);

            doc.close();
            return baos.toByteArray();
        } catch (DocumentException e) {
            throw new RuntimeException("Erro ao gerar PDF da fatura", e);
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────

    private static String formatStatus(String status) {
        if (status == null) return "-";
        switch (status.toUpperCase()) {
            case "PAGA": return "Paga";
            case "PENDENTE": return "Pendente";
            case "VENCIDA": return "Vencida";
            default: return status;
        }
    }

    private static void addPairCell(PdfPTable table, String label, String value,
                                     Font fontLabel, Font fontValue, Color bg) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(8f);
        cell.setBorderWidth(0.5f);
        cell.setBorderColor(SLATE_200);
        if (bg != null) cell.setBackgroundColor(bg);
        cell.addElement(new Paragraph(label, fontLabel));
        cell.addElement(new Paragraph(value != null ? value : "-", fontValue));
        table.addCell(cell);
    }

    private static boolean addDetailRow(PdfPTable table, String label, String value,
                                         Font fontLabel, Font fontValue, boolean zebra) {
        Color bg = zebra ? SLATE_50 : Color.WHITE;
        PdfPCell c1 = new PdfPCell(new Phrase(label, fontLabel));
        c1.setPadding(7f);
        c1.setBorderWidth(0.5f);
        c1.setBorderColor(SLATE_200);
        c1.setBackgroundColor(bg);
        table.addCell(c1);

        PdfPCell c2 = new PdfPCell(new Phrase(value != null ? value : "-", fontValue));
        c2.setPadding(7f);
        c2.setBorderWidth(0.5f);
        c2.setBorderColor(SLATE_200);
        c2.setBackgroundColor(bg);
        table.addCell(c2);
        return !zebra;
    }
}

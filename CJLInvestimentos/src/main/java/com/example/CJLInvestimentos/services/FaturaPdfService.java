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

    private static final String NOME_EMISSOR = "CJL Investimentos";
    private static final String SUBTITULO_EMISSOR = "Plataforma de investimentos e consultoria";
    private static final DateTimeFormatter DATA_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.forLanguageTag("pt-BR"));
    private static final DateTimeFormatter DATA_HORA_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.forLanguageTag("pt-BR"));
    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");

    private static final Color COR_TITULO = new Color(37, 99, 235);
    private static final Color COR_CABECALHO_TABELA = new Color(55, 65, 81);
    private static final Color COR_BORDA = new Color(229, 231, 235);
    private static final Color COR_FUNDO_LEVE = new Color(249, 250, 251);

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

    private byte[] buildPdf(Fatura fatura) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            Document doc = new Document(PageSize.A4, 40, 40, 45, 40);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, COR_TITULO);
            Font fontSubtitulo = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GRAY);
            Font fontSecao = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, COR_CABECALHO_TABELA);
            Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY);
            Font fontValor = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, COR_TITULO);
            Font fontRodape = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY);

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

            // ---- Cabeçalho ----
            Paragraph pTitulo = new Paragraph(NOME_EMISSOR, fontTitulo);
            pTitulo.setSpacingAfter(2f);
            doc.add(pTitulo);
            doc.add(new Paragraph(SUBTITULO_EMISSOR, fontSubtitulo));
            doc.add(Chunk.NEWLINE);

            // ---- Número e data da fatura ----
            PdfPTable tblNumero = new PdfPTable(2);
            tblNumero.setWidthPercentage(100f);
            tblNumero.setWidths(new float[]{1f, 1f});
            tblNumero.setSpacingAfter(16f);
            addCell(tblNumero, "FATURA Nº", String.valueOf(fatura.getId()), fontSecao, fontNormal, COR_FUNDO_LEVE);
            addCell(tblNumero, "Data de emissão", dataEmissao, fontSecao, fontNormal, COR_FUNDO_LEVE);
            doc.add(tblNumero);

            // ---- Dois blocos: Emissor | Cliente ----
            PdfPTable tblPartes = new PdfPTable(2);
            tblPartes.setWidthPercentage(100f);
            tblPartes.setWidths(new float[]{1f, 1f});
            tblPartes.setSpacingAfter(20f);

            PdfPCell cellEmissor = new PdfPCell();
            cellEmissor.setPadding(12f);
            cellEmissor.setBorderWidth(0.5f);
            cellEmissor.setBorderColor(COR_BORDA);
            cellEmissor.setBackgroundColor(COR_FUNDO_LEVE);
            cellEmissor.addElement(new Paragraph("EMISSOR", fontSecao));
            cellEmissor.addElement(new Paragraph(NOME_EMISSOR, fontNormal));
            cellEmissor.addElement(new Paragraph(SUBTITULO_EMISSOR, fontSubtitulo));
            tblPartes.addCell(cellEmissor);

            PdfPCell cellCliente = new PdfPCell();
            cellCliente.setPadding(12f);
            cellCliente.setBorderWidth(0.5f);
            cellCliente.setBorderColor(COR_BORDA);
            cellCliente.setBackgroundColor(COR_FUNDO_LEVE);
            cellCliente.addElement(new Paragraph("CLIENTE / PAGADOR", fontSecao));
            cellCliente.addElement(new Paragraph(nomeCliente, fontNormal));
            cellCliente.addElement(new Paragraph("CNPJ: " + cnpjCliente, fontNormal));
            if (!"-".equals(planoNome)) {
                cellCliente.addElement(new Paragraph("Plano: " + planoNome, fontNormal));
            }
            tblPartes.addCell(cellCliente);
            doc.add(tblPartes);

            // ---- Tabela de itens/detalhes da fatura ----
            doc.add(new Paragraph("DETALHES DA FATURA", fontSecao));
            doc.add(Chunk.NEWLINE);

            PdfPTable tblDetalhes = new PdfPTable(2);
            tblDetalhes.setWidthPercentage(100f);
            tblDetalhes.setWidths(new float[]{35f, 65f});
            tblDetalhes.setSpacingAfter(8f);
            addCell(tblDetalhes, "Descrição do serviço", descricao, fontSecao, fontNormal, null);
            addCell(tblDetalhes, "Data de vencimento", dataVencimento, fontSecao, fontNormal, null);
            addCell(tblDetalhes, "Status", status, fontSecao, fontNormal, null);
            addCell(tblDetalhes, "Data de pagamento", dataPagamento, fontSecao, fontNormal, null);
            addCell(tblDetalhes, "Forma de pagamento", formaPagamento, fontSecao, fontNormal, null);
            if (!referencia.isEmpty()) {
                addCell(tblDetalhes, "Referência (ID pagamento)", referencia, fontSecao, fontNormal, null);
            }
            doc.add(tblDetalhes);

            // ---- Valor total em destaque ----
            PdfPTable tblValor = new PdfPTable(1);
            tblValor.setWidthPercentage(100f);
            tblValor.setSpacingBefore(12f);
            tblValor.setSpacingAfter(16f);
            tblValor.setHorizontalAlignment(Element.ALIGN_RIGHT);
            PdfPCell cellValor = new PdfPCell(new Phrase("VALOR TOTAL: " + valorStr, fontValor));
            cellValor.setPadding(10f);
            cellValor.setBorderWidth(0.5f);
            cellValor.setBorderColor(COR_TITULO);
            cellValor.setBackgroundColor(new Color(239, 246, 255));
            cellValor.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tblValor.addCell(cellValor);
            doc.add(tblValor);

            if (!observacao.isEmpty()) {
                doc.add(new Paragraph("Observações", fontSecao));
                doc.add(new Paragraph(observacao, fontNormal));
                doc.add(Chunk.NEWLINE);
            }

            // ---- Rodapé ----
            doc.add(new Paragraph("Documento gerado eletronicamente. Em caso de dúvidas, entre em contato com o suporte.",
                    fontRodape));
            doc.add(new Paragraph("Este PDF é uma cópia para seus registros.", fontRodape));

            doc.close();
            return baos.toByteArray();
        } catch (DocumentException e) {
            throw new RuntimeException("Erro ao gerar PDF da fatura", e);
        }
    }

    private static String formatStatus(String status) {
        if (status == null) return "-";
        switch (status.toUpperCase()) {
            case "PAGA": return "Paga";
            case "PENDENTE": return "Pendente";
            case "VENCIDA": return "Vencida";
            default: return status;
        }
    }

    private static void addCell(PdfPTable table, String label, String value, Font fontLabel, Font fontValue, Color bg) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, fontLabel));
        c1.setPadding(6f);
        c1.setBorderWidth(0.5f);
        c1.setBorderColor(COR_BORDA);
        if (bg != null) c1.setBackgroundColor(bg);
        table.addCell(c1);
        PdfPCell c2 = new PdfPCell(new Phrase(value != null ? value : "-", fontValue));
        c2.setPadding(6f);
        c2.setBorderWidth(0.5f);
        c2.setBorderColor(COR_BORDA);
        if (bg != null) c2.setBackgroundColor(bg);
        table.addCell(c2);
    }
}

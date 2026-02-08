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

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class FaturaPdfService {

    private static final String NOME_EMISSOR = "CJL Investimentos";
    private static final DateTimeFormatter DATA_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy", new Locale("pt", "BR"));
    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");

    private final FaturaRepository faturaRepository;

    @Transactional(readOnly = true)
    public byte[] gerarPdf(Long empresaId, Long faturaId) {
        Fatura fatura = faturaRepository.findById(faturaId)
                .orElseThrow(() -> new ResourceNotFoundException("Fatura não encontrada"));
        if (!fatura.getEmpresa().getId().equals(empresaId)) {
            throw new ResourceNotFoundException("Fatura não pertence a esta empresa");
        }
        String nomeCliente = fatura.getEmpresa().getNome();
        String cnpjCliente = fatura.getEmpresa().getCnpj();
        String dataVencimento = fatura.getDataVencimento() == null
                ? "-" : DATA_FMT.format(fatura.getDataVencimento().atZone(ZONE));
        String descricao = fatura.getDescricaoServico() != null && !fatura.getDescricaoServico().isBlank()
                ? fatura.getDescricaoServico() : "Serviços de assinatura";
        BigDecimal valor = fatura.getValor();
        String valorStr = String.format(new Locale("pt", "BR"), "R$ %.2f", valor);
        String status = fatura.getStatus().name();
        return buildPdf(faturaId, nomeCliente, cnpjCliente, dataVencimento, descricao, valorStr, status);
    }

    private byte[] buildPdf(Long faturaId, String nomeCliente, String cnpjCliente,
                            String dataVencimento, String descricao, String valorStr, String status) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font subtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font normal = FontFactory.getFont(FontFactory.HELVETICA, 11);

            doc.add(new Paragraph(NOME_EMISSOR, titulo));
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("FATURA #" + faturaId, subtitulo));
            doc.add(new Paragraph(" "));

            PdfPTable tabela = new PdfPTable(2);
            tabela.setWidthPercentage(100f);
            tabela.setSpacingBefore(10f);
            tabela.setSpacingAfter(10f);
            addCell(tabela, "Cliente:", nomeCliente, normal);
            addCell(tabela, "CNPJ:", cnpjCliente, normal);
            addCell(tabela, "Vencimento:", dataVencimento, normal);
            addCell(tabela, "Status:", status, normal);
            doc.add(tabela);

            doc.add(new Paragraph("Descrição do serviço", subtitulo));
            doc.add(new Paragraph(descricao, normal));
            doc.add(new Paragraph(" "));

            doc.add(new Paragraph("Valor total: " + valorStr, subtitulo));
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Documento gerado eletronicamente. Em caso de dúvidas, entre em contato.", FontFactory.getFont(FontFactory.HELVETICA, 9)));

            doc.close();
            return baos.toByteArray();
        } catch (DocumentException e) {
            throw new RuntimeException("Erro ao gerar PDF da fatura", e);
        }
    }

    private static void addCell(PdfPTable table, String label, String value, Font font) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, font));
        c1.setBorderWidth(0f);
        table.addCell(c1);
        PdfPCell c2 = new PdfPCell(new Phrase(value != null ? value : "-", font));
        c2.setBorderWidth(0f);
        table.addCell(c2);
    }
}

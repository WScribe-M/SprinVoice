package fr.manu.sprinvoice.services;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import fr.manu.sprinvoice.models.Customer;
import fr.manu.sprinvoice.models.Invoice;
import fr.manu.sprinvoice.models.InvoiceRow;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class InvoicePdfService {

    private static final Color DARK_BLUE  = new Color(0x1d, 0x35, 0x57);
    private static final Color MID_BLUE   = new Color(0x45, 0x7b, 0x9d);
    private static final Color ORANGE     = new Color(0xf4, 0xa2, 0x61);
    private static final Color LIGHT_GREY = new Color(0xf0, 0xf4, 0xf8);
    private static final Color WHITE      = Color.WHITE;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final Font FONT_LOGO      = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, WHITE);
    private static final Font FONT_LOGO_SPAN = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, ORANGE);
    private static final Font FONT_TITLE     = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, WHITE);
    private static final Font FONT_SECTION   = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, DARK_BLUE);
    private static final Font FONT_LABEL     = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9,  new Color(0x55, 0x55, 0x55));
    private static final Font FONT_VALUE     = FontFactory.getFont(FontFactory.HELVETICA,      9,  new Color(0x33, 0x33, 0x33));
    private static final Font FONT_TH        = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9,  WHITE);
    private static final Font FONT_TD        = FontFactory.getFont(FontFactory.HELVETICA,      9,  new Color(0x1a, 0x1a, 0x1a));
    private static final Font FONT_TOTAL_LBL = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(0x55, 0x55, 0x55));
    private static final Font FONT_TOTAL_VAL = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, DARK_BLUE);
    private static final Font FONT_FOOTER    = FontFactory.getFont(FontFactory.HELVETICA,      8,  new Color(0xaa, 0xaa, 0xaa));

    public byte[] generate(Invoice invoice, List<InvoiceRow> rows) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 40, 40, 40, 50);
        PdfWriter writer = PdfWriter.getInstance(doc, out);

        writer.setPageEvent(new PdfPageEventHelper() {
            @Override
            public void onEndPage(PdfWriter w, Document d) {
                PdfContentByte cb = w.getDirectContent();
                Phrase footer = new Phrase("SprinVoice – Gestion de facturation  ·  Page " + w.getPageNumber(), FONT_FOOTER);
                ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, footer,
                        (d.right() + d.left()) / 2, d.bottom() - 20, 0);
            }
        });

        doc.open();

        // ── En-tête ──────────────────────────────────────────────
        addHeader(doc, invoice);

        doc.add(Chunk.NEWLINE);

        // ── Bloc info client + dates ─────────────────────────────
        addInfoBlock(doc, invoice);

        doc.add(Chunk.NEWLINE);

        // ── Tableau des lignes ───────────────────────────────────
        addRowsTable(doc, rows);

        doc.add(Chunk.NEWLINE);

        // ── Total ────────────────────────────────────────────────
        addTotal(doc, invoice);

        doc.close();
        return out.toByteArray();
    }

    // ── En-tête bleu foncé ───────────────────────────────────────
    private void addHeader(Document doc, Invoice invoice) throws DocumentException {
        PdfPTable header = new PdfPTable(2);
        header.setWidthPercentage(100);
        header.setWidths(new float[]{2f, 1f});

        // Cellule gauche : logo
        Paragraph logo = new Paragraph();
        logo.add(new Chunk("Sprin", FONT_LOGO));
        logo.add(new Chunk("Voice", FONT_LOGO_SPAN));
        PdfPCell logoCell = new PdfPCell(logo);
        logoCell.setBackgroundColor(DARK_BLUE);
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setPadding(16);
        header.addCell(logoCell);

        // Cellule droite : "FACTURE" + numéro
        Paragraph title = new Paragraph();
        title.add(new Chunk("FACTURE", FONT_TITLE));
        title.add(Chunk.NEWLINE);
        Phrase num = new Phrase("N° " + invoice.getId(), FontFactory.getFont(FontFactory.HELVETICA, 11, ORANGE));
        title.add(num);
        title.setAlignment(Element.ALIGN_RIGHT);
        PdfPCell titleCell = new PdfPCell(title);
        titleCell.setBackgroundColor(DARK_BLUE);
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setPadding(16);
        titleCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        header.addCell(titleCell);

        // Bande orange sous le header
        PdfPCell band = new PdfPCell(new Phrase(" "));
        band.setBackgroundColor(ORANGE);
        band.setFixedHeight(4f);
        band.setBorder(Rectangle.NO_BORDER);
        band.setColspan(2);
        header.addCell(band);

        doc.add(header);

        // Désignation
        Paragraph desig = new Paragraph(invoice.getDesignation(),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, DARK_BLUE));
        desig.setSpacingBefore(12);
        desig.setSpacingAfter(4);
        doc.add(desig);
    }

    // ── Bloc 2 colonnes : client | dates ─────────────────────────
    private void addInfoBlock(Document doc, Invoice invoice) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1f, 1f});

        table.addCell(buildInfoCell("Client", buildClientContent(invoice.getCustomer())));
        table.addCell(buildInfoCell("Dates", buildDatesContent(invoice)));

        doc.add(table);
    }

    private PdfPCell buildInfoCell(String title, Paragraph content) {
        PdfPTable inner = new PdfPTable(1);
        inner.setWidthPercentage(100);

        // Titre de section
        PdfPCell titleCell = new PdfPCell(new Phrase(title, FONT_SECTION));
        titleCell.setBackgroundColor(LIGHT_GREY);
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setPadding(7);
        titleCell.setPaddingBottom(5);
        inner.addCell(titleCell);

        // Contenu
        PdfPCell contentCell = new PdfPCell(content);
        contentCell.setBorder(Rectangle.NO_BORDER);
        contentCell.setPadding(8);
        contentCell.setPaddingTop(4);
        inner.addCell(contentCell);

        PdfPCell wrapper = new PdfPCell(inner);
        wrapper.setBorder(Rectangle.BOX);
        wrapper.setBorderColor(new Color(0xdd, 0xe3, 0xea));
        wrapper.setPadding(4);
        return wrapper;
    }

    private Paragraph buildClientContent(Customer c) {
        Paragraph p = new Paragraph();
        if (c == null) {
            p.add(new Chunk("–", FONT_VALUE));
            return p;
        }
        if (c.getName() != null)          addLabelValue(p, "Nom",      c.getName());
        if (c.getCorporateName() != null) addLabelValue(p, "Société",  c.getCorporateName());
        if (c.getAddress() != null)       addLabelValue(p, "Adresse",  c.getAddress());
        String cp = (c.getZipcode() != null ? c.getZipcode() : "")
                  + (c.getCity()    != null ? " " + c.getCity() : "");
        if (!cp.isBlank())                addLabelValue(p, "",         cp);
        addLabelValue(p, "Délai", c.getDelay() + " jours");
        return p;
    }

    private Paragraph buildDatesContent(Invoice invoice) {
        Paragraph p = new Paragraph();
        addLabelValue(p, "Créée le",    fmt(invoice.getCreatedAt()  != null ? invoice.getCreatedAt().format(FMT)  : null));
        addLabelValue(p, "Facturée le", fmt(invoice.getInvoicedAt() != null ? invoice.getInvoicedAt().format(FMT) : null));
        if (invoice.getInvoicedAt() != null && invoice.getCustomer() != null) {
            String echeance = invoice.getInvoicedAt()
                    .plusDays(invoice.getCustomer().getDelay())
                    .format(FMT);
            addLabelValue(p, "Échéance", echeance);
        }
        addLabelValue(p, "Payée le", fmt(invoice.getPaidAt() != null ? invoice.getPaidAt().format(FMT) : "En attente"));
        return p;
    }

    // ── Tableau des lignes ───────────────────────────────────────
    private void addRowsTable(Document doc, List<InvoiceRow> rows) throws DocumentException {
        Paragraph sectionTitle = new Paragraph("Lignes de facturation", FONT_SECTION);
        sectionTitle.setSpacingBefore(4);
        sectionTitle.setSpacingAfter(8);
        doc.add(sectionTitle);

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3f, 2f, 1.8f, 1f, 1f, 1.8f});

        addTh(table, "Produit");
        addTh(table, "Catégorie");
        addTh(table, "Prix unitaire HT");
        addTh(table, "TVA");
        addTh(table, "Qté");
        addTh(table, "Montant HT");

        if (rows == null || rows.isEmpty()) {
            PdfPCell empty = new PdfPCell(new Phrase("Aucune ligne de facturation.", FONT_VALUE));
            empty.setColspan(6);
            empty.setHorizontalAlignment(Element.ALIGN_CENTER);
            empty.setBorder(Rectangle.BOX);
            empty.setBorderColor(new Color(0xdd, 0xe3, 0xea));
            empty.setPadding(12);
            table.addCell(empty);
        } else {
            boolean alt = false;
            for (InvoiceRow row : rows) {
                Color bg = alt ? LIGHT_GREY : WHITE;
                addTd(table, row.getProduct() != null ? row.getProduct().getDesignation() : "–", bg, Element.ALIGN_LEFT);
                addTd(table, row.getProduct() != null ? nvl(row.getProduct().getCategory()) : "–", bg, Element.ALIGN_LEFT);
                addTd(table, row.getProduct() != null ? String.format("%.2f €", row.getProduct().getUnitPrice()) : "–", bg, Element.ALIGN_RIGHT);
                addTd(table, row.getProduct() != null ? row.getProduct().getTvaRate() + " %" : "–", bg, Element.ALIGN_CENTER);
                addTd(table, row.getQuantity() != null ? String.valueOf(row.getQuantity()) : "–", bg, Element.ALIGN_CENTER);
                addTd(table, String.format("%.2f €", row.amount()), bg, Element.ALIGN_RIGHT);
                alt = !alt;
            }
        }

        doc.add(table);
    }

    // ── Total ────────────────────────────────────────────────────
    private void addTotal(Document doc, Invoice invoice) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(40);
        table.setHorizontalAlignment(Element.ALIGN_RIGHT);

        addTotalRow(table, "Total HT",  String.format("%.2f €", invoice.total()),    false);
        addTotalRow(table, "TVA",       String.format("%.2f €", invoice.totalTva()), false);
        addTotalRow(table, "Total TTC", String.format("%.2f €", invoice.totalTtc()), true);

        doc.add(table);
    }

    private void addTotalRow(PdfPTable table, String label, String value, boolean highlight) {
        Font lblFont = highlight ? FONT_TOTAL_VAL  : FONT_TOTAL_LBL;
        Font valFont = highlight ? FONT_TOTAL_VAL  : FONT_TOTAL_VAL;
        Color valBg  = highlight ? new Color(0xea, 0xf2, 0xfb) : LIGHT_GREY;

        PdfPCell lbl = new PdfPCell(new Phrase(label, lblFont));
        lbl.setBorder(highlight ? Rectangle.TOP : Rectangle.NO_BORDER);
        lbl.setBorderColor(DARK_BLUE);
        lbl.setBorderWidth(highlight ? 2f : 0f);
        lbl.setPadding(8);
        lbl.setHorizontalAlignment(Element.ALIGN_RIGHT);

        PdfPCell val = new PdfPCell(new Phrase(value, valFont));
        val.setBorder(highlight ? Rectangle.TOP : Rectangle.NO_BORDER);
        val.setBorderColor(DARK_BLUE);
        val.setBorderWidth(highlight ? 2f : 0f);
        val.setPadding(8);
        val.setHorizontalAlignment(Element.ALIGN_RIGHT);
        val.setBackgroundColor(valBg);

        table.addCell(lbl);
        table.addCell(val);
    }

    // ── Utilitaires ──────────────────────────────────────────────
    private void addTh(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FONT_TH));
        cell.setBackgroundColor(DARK_BLUE);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(8);
        table.addCell(cell);
    }

    private void addTd(PdfPTable table, String text, Color bg, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FONT_TD));
        cell.setBackgroundColor(bg);
        cell.setBorderColor(new Color(0xdd, 0xe3, 0xea));
        cell.setBorderWidth(0.5f);
        cell.setPadding(7);
        cell.setHorizontalAlignment(align);
        table.addCell(cell);
    }

    private void addLabelValue(Paragraph p, String label, String value) {
        if (!label.isBlank()) {
            p.add(new Chunk(label + " : ", FONT_LABEL));
        }
        p.add(new Chunk(value != null ? value : "–", FONT_VALUE));
        p.add(Chunk.NEWLINE);
    }

    private String fmt(String value) {
        return value != null ? value : "–";
    }

    private String nvl(String s) {
        return s != null ? s : "–";
    }
}

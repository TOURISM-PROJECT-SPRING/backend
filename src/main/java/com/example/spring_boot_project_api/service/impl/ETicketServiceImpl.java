package com.example.spring_boot_project_api.service.impl;

import java.io.ByteArrayOutputStream;
import java.math.RoundingMode;

import org.springframework.stereotype.Service;

import com.example.spring_boot_project_api.dto.response.TicketBookingResponse;
import com.example.spring_boot_project_api.service.ETicketService;
import com.example.spring_boot_project_api.util.QrCodeUtil;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

@Service
public class ETicketServiceImpl implements ETicketService {

    private static final String OPEN_SANS = "Helvetica";

    @Override
    public byte[] generatePdf(TicketBookingResponse booking) {
        return generatePdfStream(booking).toByteArray();
    }

    @Override
    public ByteArrayOutputStream generatePdfStream(TicketBookingResponse booking) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, out);
            document.open();
            addTitle(document);
            addDetails(document, booking);
            addQrCode(document, booking.getQrCode());
            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate e-ticket PDF: " + e.getMessage(), e);
        }
        return out;
    }

    private void addTitle(Document document) {
        Font titleFont = FontFactory.getFont(OPEN_SANS, 20, Font.BOLD);
        Paragraph title = new Paragraph("E-Ticket", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Font subFont = FontFactory.getFont(OPEN_SANS, 11, Font.NORMAL);
        Paragraph sub = new Paragraph("Tourism Management System", subFont);
        sub.setAlignment(Element.ALIGN_CENTER);
        document.add(sub);
        document.add(new Paragraph("\n"));
    }

    private void addDetails(Document document, TicketBookingResponse booking) {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(90);
        table.setSpacingBefore(10);
        table.setSpacingAfter(10);

        Font labelFont = FontFactory.getFont(OPEN_SANS, 11, Font.BOLD);
        Font valueFont = FontFactory.getFont(OPEN_SANS, 11, Font.NORMAL);

        addRow(table, "Booking ID", String.valueOf(booking.getId()), labelFont, valueFont);
        addRow(table, "Customer", nvl(booking.getUserName()), labelFont, valueFont);
        addRow(table, "Destination", nvl(booking.getTourismPlaceName()), labelFont, valueFont);
        addRow(table, "Ticket", nvl(booking.getTicketName()), labelFont, valueFont);
        addRow(table, "Quantity", String.valueOf(booking.getQuantity()), labelFont, valueFont);
        addRow(table, "Unit Price",
                "$ " + nvl(booking.getUnitPrice()), labelFont, valueFont);
        addRow(table, "Total Paid",
                "$ " + nvl(booking.getTotalPrice()), labelFont, valueFont);
        addRow(table, "Visit Date", String.valueOf(booking.getVisitDate()), labelFont, valueFont);
        addRow(table, "Status", nvl(booking.getStatus()), labelFont, valueFont);
        addRow(table, "QR Reference", nvl(booking.getQrCode()), labelFont, valueFont);

        document.add(table);
    }

    private void addQrCode(Document document, String qrCode) throws Exception {
        if (qrCode == null || qrCode.isBlank()) {
            return;
        }
        byte[] qrBytes = QrCodeUtil.generatePngBytes(qrCode);
        Image qrImage = Image.getInstance(qrBytes);
        qrImage.scaleToFit(150, 150);
        qrImage.setAlignment(Element.ALIGN_CENTER);
        document.add(qrImage);

        Font noteFont = FontFactory.getFont(OPEN_SANS, 9, Font.NORMAL);
        Paragraph note = new Paragraph("Present this QR code at the entrance for check-in.",
                noteFont);
        note.setAlignment(Element.ALIGN_CENTER);
        document.add(note);
    }

    private void addRow(PdfPTable table, String label, String value,
                        Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(0);
        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(0);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private String nvl(Object value) {
        if (value == null) return "-";
        if (value instanceof java.math.BigDecimal bd) {
            return bd.setScale(2, RoundingMode.HALF_UP).toPlainString();
        }
        return String.valueOf(value);
    }
}

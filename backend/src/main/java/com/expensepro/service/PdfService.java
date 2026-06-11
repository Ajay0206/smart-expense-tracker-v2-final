package com.expensepro.service;

import com.expensepro.model.Expense;
import com.expensepro.repository.ExpenseRepository;
import com.expensepro.repository.UserRepository;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class PdfService {

    @Autowired ExpenseRepository expRepo;
    @Autowired UserRepository    userRepo;

    private static final BaseColor INDIGO  = new BaseColor(99, 102, 241);
    private static final BaseColor INDIGO2 = new BaseColor(67, 56, 202);
    private static final BaseColor GRAY50  = new BaseColor(249, 250, 251);
    private static final BaseColor GRAY200 = new BaseColor(229, 231, 235);
    private static final BaseColor GRAY600 = new BaseColor(75, 85, 99);
    private static final BaseColor GRAY900 = new BaseColor(17, 24, 39);
    private static final BaseColor RED500  = new BaseColor(239, 68, 68);

    private static final Font F_HERO  = font(22, Font.BOLD,   BaseColor.WHITE);
    private static final Font F_H2    = font(13, Font.BOLD,   GRAY900);
    private static final Font F_BODY  = font(9,  Font.NORMAL, GRAY600);
    private static final Font F_BOLD  = font(9,  Font.BOLD,   GRAY900);
    private static final Font F_RED   = font(9,  Font.BOLD,   RED500);
    private static final Font F_WHITE = font(9,  Font.BOLD,   BaseColor.WHITE);

    private static Font font(float sz, int style, BaseColor color) {
        return new Font(Font.FontFamily.HELVETICA, sz, style, color);
    }

    public byte[] monthlyReport(Long userId, int year, int month) throws Exception {
        var user = userRepo.findById(userId).orElseThrow();
        LocalDate from = LocalDate.of(year, month, 1);
        LocalDate to   = from.withDayOfMonth(from.lengthOfMonth());
        List<Expense> expenses = expRepo
                .findByUserIdAndExpenseDateBetweenOrderByExpenseDateDesc(userId, from, to);

        Document doc = new Document(PageSize.A4, 40, 40, 40, 40);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(doc, out);

        writer.setPageEvent(new PdfPageEventHelper() {
            @Override public void onEndPage(PdfWriter w, Document d) {
                PdfContentByte cb = w.getDirectContent();
                cb.saveState();
                String footer = "ExpensePro  ·  Page " + w.getPageNumber() + "  ·  Confidential";
                ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                        new Phrase(footer, font(8, Font.NORMAL, GRAY600)),
                        d.getPageSize().getWidth() / 2, 25, 0);
                cb.restoreState();
            }
        });

        doc.open();

        PdfPTable banner = new PdfPTable(1);
        banner.setWidthPercentage(100);
        PdfPCell heroCell = new PdfPCell();
        heroCell.setBackgroundColor(INDIGO);
        heroCell.setPadding(28);
        heroCell.setBorder(Rectangle.NO_BORDER);
        Paragraph heroText = new Paragraph();
        heroText.add(new Chunk("ExpensePro — Monthly Report\n", F_HERO));
        heroText.add(new Chunk(
            Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + year,
            font(12, Font.NORMAL, new BaseColor(199, 210, 254))));
        heroText.add(Chunk.NEWLINE);
        heroText.add(new Chunk(user.getFullName() + "  ·  " + user.getEmail(),
            font(9, Font.ITALIC, new BaseColor(165, 180, 252))));
        heroCell.addElement(heroText);
        banner.addCell(heroCell);
        banner.setSpacingAfter(20);
        doc.add(banner);

        double total = expenses.stream().mapToDouble(e -> e.getAmount().doubleValue()).sum();
        double avg   = expenses.isEmpty() ? 0 : total / expenses.size();

        Map<String, Long> catCount = new LinkedHashMap<>();
        for (Expense e : expenses)
            catCount.merge(e.getCategory().getName(), 1L, Long::sum);
        String topCat = catCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse("—");

        PdfPTable summary = new PdfPTable(4);
        summary.setWidthPercentage(100);
        summary.setWidths(new float[]{1, 1, 1, 1});
        summary.setSpacingAfter(24);
        addSummaryCard(summary, "Total Spent",   user.getCurrency() + " " + fmt(total), INDIGO);
        addSummaryCard(summary, "Transactions",  String.valueOf(expenses.size()),        new BaseColor(16, 185, 129));
        addSummaryCard(summary, "Average / Txn", user.getCurrency() + " " + fmt(avg),   new BaseColor(245, 158, 11));
        addSummaryCard(summary, "Top Category",  topCat,                                 new BaseColor(139, 92, 246));
        doc.add(summary);

        Paragraph tableTitle = new Paragraph("Transaction Details", F_H2);
        tableTitle.setSpacingAfter(10);
        doc.add(tableTitle);

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2.8f, 1.8f, 1.4f, 1.4f, 1.4f});
        table.setSpacingAfter(20);

        for (String h : new String[]{"Title", "Category", "Date", "Mode", "Amount"})
            addTH(table, h);

        boolean alt = false;
        for (Expense e : expenses) {
            BaseColor bg = alt ? GRAY50 : BaseColor.WHITE;
            addTD(table, e.getTitle(), bg, Element.ALIGN_LEFT, F_BOLD);
            addTD(table, e.getCategory().getIcon() + " " + e.getCategory().getName(), bg, Element.ALIGN_LEFT, F_BODY);
            addTD(table, e.getExpenseDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")), bg, Element.ALIGN_CENTER, F_BODY);
            addTD(table, e.getPaymentMode().toString().replace("_", " "), bg, Element.ALIGN_CENTER, F_BODY);
            addTD(table, user.getCurrency() + " " + fmt(e.getAmount().doubleValue()), bg, Element.ALIGN_RIGHT, F_RED);
            alt = !alt;
        }

        PdfPCell totalLabel = new PdfPCell(new Phrase("TOTAL", F_WHITE));
        totalLabel.setColspan(4);
        totalLabel.setBackgroundColor(INDIGO2);
        totalLabel.setPadding(8);
        totalLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalLabel.setBorderColor(GRAY200);
        table.addCell(totalLabel);

        PdfPCell totalVal = new PdfPCell(new Phrase(user.getCurrency() + " " + fmt(total), F_WHITE));
        totalVal.setBackgroundColor(INDIGO2);
        totalVal.setPadding(8);
        totalVal.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalVal.setBorderColor(GRAY200);
        table.addCell(totalVal);

        doc.add(table);

        Paragraph foot = new Paragraph(
            "Generated on " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")) +
            "  ·  ExpensePro System Report",
            font(8, Font.ITALIC, GRAY600));
        foot.setAlignment(Element.ALIGN_CENTER);
        doc.add(foot);

        doc.close();
        return out.toByteArray();
    }

    private void addSummaryCard(PdfPTable t, String label, String value, BaseColor accent) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(14);
        cell.setBorderColor(GRAY200);
        Paragraph p = new Paragraph();
        p.add(new Chunk(label + "\n", font(8, Font.NORMAL, GRAY600)));
        p.add(new Chunk(value, font(13, Font.BOLD, accent)));
        cell.addElement(p);
        t.addCell(cell);
    }

    private void addTH(PdfPTable t, String text) {
        PdfPCell c = new PdfPCell(new Phrase(text, F_WHITE));
        c.setBackgroundColor(INDIGO);
        c.setPadding(9);
        c.setBorderColor(INDIGO2);
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        t.addCell(c);
    }

    private void addTD(PdfPTable t, String text, BaseColor bg, int align, Font f) {
        PdfPCell c = new PdfPCell(new Phrase(text, f));
        c.setBackgroundColor(bg);
        c.setPadding(7);
        c.setHorizontalAlignment(align);
        c.setBorderColor(GRAY200);
        t.addCell(c);
    }

    private String fmt(double v) {
        return String.format("%,.2f", v);
    }
}

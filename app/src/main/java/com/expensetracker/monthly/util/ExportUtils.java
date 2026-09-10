package com.expensetracker.monthly.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;

import androidx.core.content.FileProvider;

import com.expensetracker.monthly.data.model.CategorySpendSummary;
import com.expensetracker.monthly.data.model.ExpenseWithDetails;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExportUtils {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    /**
     * Generates a CSV file containing the list of expenses.
     */
    public static File generateCsvFile(Context context, List<ExpenseWithDetails> expenses, String filename) throws IOException {
        File exportDir = new File(context.getCacheDir(), "exports");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        File csvFile = new File(exportDir, filename);
        try (FileWriter writer = new FileWriter(csvFile)) {
            // CSV Header
            writer.append("Date,Title,Category,Subcategory,Amount,Notes\n");

            if (expenses != null) {
                for (ExpenseWithDetails item : expenses) {
                    if (item.expense == null) continue;

                    String dateStr = DATE_FORMAT.format(new Date(item.expense.getDateMillis()));
                    String title = escapeCsv(item.expense.getTitle());
                    String category = item.category != null ? escapeCsv(item.category.getName()) : "Uncategorized";
                    String subcategory = item.subcategory != null ? escapeCsv(item.subcategory.getName()) : "";
                    String amount = String.format(Locale.US, "%.2f", item.expense.getAmount());
                    String notes = item.expense.getNotes() != null ? escapeCsv(item.expense.getNotes()) : "";

                    writer.append(dateStr).append(",")
                            .append(title).append(",")
                            .append(category).append(",")
                            .append(subcategory).append(",")
                            .append(amount).append(",")
                            .append(notes).append("\n");
                }
            }
            writer.flush();
        }

        return csvFile;
    }

    /**
     * Generates a clean, branded PDF Statement using native PdfDocument.
     */
    public static File generatePdfStatement(
            Context context,
            String statementPeriod,
            List<ExpenseWithDetails> expenses,
            List<CategorySpendSummary> categorySummaries,
            double totalSpend,
            String currencySymbol,
            String filename
    ) throws IOException {
        File exportDir = new File(context.getCacheDir(), "exports");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        File pdfFile = new File(exportDir, filename);
        PdfDocument document = new PdfDocument();

        int pageWidth = 595; // A4 standard width in points
        int pageHeight = 842; // A4 standard height in points
        int pageNumber = 1;

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        // Header Background
        paint.setColor(Color.parseColor("#1E88E5"));
        canvas.drawRect(0, 0, pageWidth, 90, paint);

        // App Title
        paint.setColor(Color.WHITE);
        paint.setTextSize(20);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("MONTHLY EXPENSE TRACKER", 36, 42, paint);

        // Subtitle / Period
        paint.setTextSize(13);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        canvas.drawText("Statement Period: " + statementPeriod, 36, 64, paint);

        // Generation timestamp
        paint.setTextSize(9);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("Generated: " + TIME_FORMAT.format(new Date()), pageWidth - 36, 64, paint);
        paint.setTextAlign(Paint.Align.LEFT);

        // Metrics Summary Cards
        int y = 120;
        paint.setColor(Color.parseColor("#F1F3F5"));
        canvas.drawRoundRect(36, y, pageWidth - 36, y + 60, 10, 10, paint);

        // Metrics text
        paint.setColor(Color.parseColor("#757575"));
        paint.setTextSize(10);
        paint.setTypeface(Typeface.DEFAULT);
        canvas.drawText("TOTAL SPEND", 56, y + 24, paint);
        canvas.drawText("TOTAL TRANSACTIONS", 230, y + 24, paint);
        canvas.drawText("CATEGORIES ACTIVE", 410, y + 24, paint);

        paint.setColor(Color.parseColor("#212121"));
        paint.setTextSize(16);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText(CurrencyUtils.formatAmount(totalSpend, currencySymbol), 56, y + 46, paint);
        int txCount = expenses != null ? expenses.size() : 0;
        canvas.drawText(String.valueOf(txCount), 230, y + 46, paint);
        int catCount = categorySummaries != null ? categorySummaries.size() : 0;
        canvas.drawText(String.valueOf(catCount), 410, y + 46, paint);

        y += 85;

        // Category Breakdown Section
        paint.setColor(Color.parseColor("#212121"));
        paint.setTextSize(13);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("Category Breakdown", 36, y, paint);

        y += 16;
        if (categorySummaries != null && !categorySummaries.isEmpty()) {
            paint.setTextSize(10);
            paint.setTypeface(Typeface.DEFAULT);

            int maxCategories = Math.min(5, categorySummaries.size());
            for (int i = 0; i < maxCategories; i++) {
                CategorySpendSummary cat = categorySummaries.get(i);

                // Category dot
                try {
                    paint.setColor(Color.parseColor(cat.colorHex));
                } catch (Exception e) {
                    paint.setColor(Color.parseColor("#1E88E5"));
                }
                canvas.drawCircle(44, y - 3, 4, paint);

                paint.setColor(Color.parseColor("#212121"));
                canvas.drawText(cat.categoryName, 56, y, paint);

                float pct = totalSpend > 0 ? (float) ((cat.totalAmount / totalSpend) * 100.0) : 0f;
                String pctStr = String.format(Locale.getDefault(), "%.1f%%", pct);
                canvas.drawText(pctStr, 250, y, paint);

                paint.setTextAlign(Paint.Align.RIGHT);
                canvas.drawText(CurrencyUtils.formatAmount(cat.totalAmount, currencySymbol), pageWidth - 36, y, paint);
                paint.setTextAlign(Paint.Align.LEFT);

                y += 16;
            }
        }

        y += 15;
        // Table Divider
        paint.setColor(Color.parseColor("#E0E0E0"));
        paint.setStrokeWidth(1);
        canvas.drawLine(36, y, pageWidth - 36, y, paint);

        y += 20;
        // Transaction List Header
        paint.setColor(Color.parseColor("#212121"));
        paint.setTextSize(13);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("Transactions Ledger", 36, y, paint);

        y += 18;
        paint.setColor(Color.parseColor("#F8F9FA"));
        canvas.drawRect(36, y - 12, pageWidth - 36, y + 6, paint);

        paint.setColor(Color.parseColor("#757575"));
        paint.setTextSize(9);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("DATE", 42, y, paint);
        canvas.drawText("TITLE", 110, y, paint);
        canvas.drawText("CATEGORY", 280, y, paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("AMOUNT", pageWidth - 42, y, paint);
        paint.setTextAlign(Paint.Align.LEFT);

        y += 18;
        paint.setTypeface(Typeface.DEFAULT);
        paint.setColor(Color.parseColor("#212121"));

        if (expenses != null) {
            for (ExpenseWithDetails item : expenses) {
                if (item.expense == null) continue;

                // Check if page full
                if (y > pageHeight - 40) {
                    document.finishPage(page);
                    pageNumber++;
                    pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create();
                    page = document.startPage(pageInfo);
                    canvas = page.getCanvas();
                    y = 40;

                    // Table Header on subsequent pages
                    paint.setColor(Color.parseColor("#757575"));
                    paint.setTextSize(9);
                    paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    canvas.drawText("DATE", 42, y, paint);
                    canvas.drawText("TITLE", 110, y, paint);
                    canvas.drawText("CATEGORY", 280, y, paint);
                    paint.setTextAlign(Paint.Align.RIGHT);
                    canvas.drawText("AMOUNT", pageWidth - 42, y, paint);
                    paint.setTextAlign(Paint.Align.LEFT);
                    y += 18;
                    paint.setTypeface(Typeface.DEFAULT);
                    paint.setColor(Color.parseColor("#212121"));
                }

                String dStr = DATE_FORMAT.format(new Date(item.expense.getDateMillis()));
                String tStr = item.expense.getTitle();
                if (tStr.length() > 28) tStr = tStr.substring(0, 25) + "...";
                String cStr = item.category != null ? item.category.getName() : "-";
                if (cStr.length() > 20) cStr = cStr.substring(0, 17) + "...";

                canvas.drawText(dStr, 42, y, paint);
                canvas.drawText(tStr, 110, y, paint);
                canvas.drawText(cStr, 280, y, paint);

                paint.setTextAlign(Paint.Align.RIGHT);
                canvas.drawText(CurrencyUtils.formatAmount(item.expense.getAmount(), currencySymbol), pageWidth - 42, y, paint);
                paint.setTextAlign(Paint.Align.LEFT);

                y += 16;
            }
        }

        document.finishPage(page);

        try (FileOutputStream fos = new FileOutputStream(pdfFile)) {
            document.writeTo(fos);
        } finally {
            document.close();
        }

        return pdfFile;
    }

    /**
     * Triggers the Android Share Sheet via FileProvider.
     */
    public static void shareExportedFile(Context context, File file, String mimeType, String chooserTitle) {
        if (context == null || file == null || !file.exists()) return;

        Uri contentUri = FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".fileprovider",
                file
        );

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType(mimeType);
        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        Intent chooser = Intent.createChooser(shareIntent, chooserTitle);
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(chooser);
    }

    public static String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}

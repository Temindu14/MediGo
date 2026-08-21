package com.styro.medigo.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.styro.medigo.R;
import com.styro.medigo.util.Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportsActivity extends AppCompatActivity {

    private ChipGroup chipGroupReportType, chipGroupDateRange;
    private TextInputEditText etFromDate, etToDate;
    private MaterialCardView cardDateRange, cardResults;
    private MaterialButton btnGenerateReport, btnExportPdf;
    private ProgressBar progressBar;
    private TextView tvReportTitle, tvReportSubtitle;
    private LinearLayout layoutSummaryStats, layoutTableRows;
    private TextView tvTotalOrders, tvTotalRevenue;
    private TextView tvCol1Header, tvCol2Header, tvCol3Header;

    private String currentReportType = "sales";
    private long fromTimestamp = 0, toTimestamp = Long.MAX_VALUE;
    private final Calendar calFrom = Calendar.getInstance();
    private final Calendar calTo = Calendar.getInstance();
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    private final SimpleDateFormat sdfFull = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    private final DecimalFormat df = new DecimalFormat("#,##0.00");

    private String lastReportTitle = "", lastReportSubtitle = "";
    private String lastCol1 = "", lastCol2 = "", lastCol3 = "";
    private final List<String[]> lastRows = new ArrayList<>();
    private int lastTotalOrders = 0;
    private double lastTotalRevenue = 0;
    private boolean isSalesReport = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        bindViews();

        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        View toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) toolbar.setOnClickListener(v -> onBackPressed());

        setDefaultDateRange();
        setupChips();
        setupDatePickers();
        btnGenerateReport.setOnClickListener(v -> generateReport());
        btnExportPdf.setOnClickListener(v -> exportPdf());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void bindViews() {
        chipGroupReportType = findViewById(R.id.chipGroupReportType);
        chipGroupDateRange = findViewById(R.id.chipGroupDateRange);
        etFromDate = findViewById(R.id.etFromDate);
        etToDate = findViewById(R.id.etToDate);
        cardDateRange = findViewById(R.id.cardDateRange);
        cardResults = findViewById(R.id.cardResults);
        btnGenerateReport = findViewById(R.id.btnGenerateReport);
        btnExportPdf = findViewById(R.id.btnExportPdf);
        progressBar = findViewById(R.id.progressBar);
        tvReportTitle = findViewById(R.id.tvReportTitle);
        tvReportSubtitle = findViewById(R.id.tvReportSubtitle);
        layoutSummaryStats = findViewById(R.id.layoutSummaryStats);
        layoutTableRows = findViewById(R.id.layoutTableRows);
        tvTotalOrders = findViewById(R.id.tvTotalOrders);
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
        tvCol1Header = findViewById(R.id.tvCol1Header);
        tvCol2Header = findViewById(R.id.tvCol2Header);
        tvCol3Header = findViewById(R.id.tvCol3Header);
    }

    private void setDefaultDateRange() {
        calFrom.set(Calendar.DAY_OF_MONTH, 1);
        calFrom.set(Calendar.HOUR_OF_DAY, 0);
        calFrom.set(Calendar.MINUTE, 0);
        calFrom.set(Calendar.SECOND, 0);
        calTo.setTimeInMillis(System.currentTimeMillis());
        updateDateFields();
    }

    private void updateDateFields() {
        etFromDate.setText(sdf.format(calFrom.getTime()));
        etToDate.setText(sdf.format(calTo.getTime()));
        fromTimestamp = calFrom.getTimeInMillis();
        calTo.set(Calendar.HOUR_OF_DAY, 23);
        calTo.set(Calendar.MINUTE, 59);
        calTo.set(Calendar.SECOND, 59);
        toTimestamp = calTo.getTimeInMillis();
    }

    private void setupChips() {
        chipGroupReportType.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chipSales) {
                currentReportType = "sales";
                cardDateRange.setVisibility(View.VISIBLE);
            } else if (id == R.id.chipTopMedicines) {
                currentReportType = "top";
                cardDateRange.setVisibility(View.VISIBLE);
            } else if (id == R.id.chipStock) {
                currentReportType = "stock";
                cardDateRange.setVisibility(View.GONE);
            }
            cardResults.setVisibility(View.GONE);
            btnExportPdf.setVisibility(View.GONE);
        });

        chipGroupDateRange.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            Calendar now = Calendar.getInstance();
            calFrom.setTimeInMillis(now.getTimeInMillis());
            calTo.setTimeInMillis(now.getTimeInMillis());
            if (id == R.id.chipToday) {
                calFrom.set(Calendar.HOUR_OF_DAY, 0);
                calFrom.set(Calendar.MINUTE, 0);
            } else if (id == R.id.chipThisWeek) {
                calFrom.set(Calendar.DAY_OF_WEEK, calFrom.getFirstDayOfWeek());
                calFrom.set(Calendar.HOUR_OF_DAY, 0);
                calFrom.set(Calendar.MINUTE, 0);
            } else if (id == R.id.chipThisMonth) {
                calFrom.set(Calendar.DAY_OF_MONTH, 1);
                calFrom.set(Calendar.HOUR_OF_DAY, 0);
                calFrom.set(Calendar.MINUTE, 0);
            } else if (id == R.id.chipAllTime) {
                calFrom.setTimeInMillis(0);
                calTo.setTimeInMillis(Long.MAX_VALUE / 2);
            }
            updateDateFields();
        });
    }

    private void setupDatePickers() {
        etFromDate.setOnClickListener(v -> new DatePickerDialog(this,
                (view, y, m, d) -> { calFrom.set(y, m, d); updateDateFields(); },
                calFrom.get(Calendar.YEAR), calFrom.get(Calendar.MONTH), calFrom.get(Calendar.DAY_OF_MONTH)).show());
        etToDate.setOnClickListener(v -> new DatePickerDialog(this,
                (view, y, m, d) -> { calTo.set(y, m, d); updateDateFields(); },
                calTo.get(Calendar.YEAR), calTo.get(Calendar.MONTH), calTo.get(Calendar.DAY_OF_MONTH)).show());
    }

    private void generateReport() {
        showLoading(true);
        cardResults.setVisibility(View.GONE);
        btnExportPdf.setVisibility(View.GONE);
        switch (currentReportType) {
            case "sales": loadSalesReport(); break;
            case "top": loadTopMedicinesReport(); break;
            case "stock": loadStockReport(); break;
        }
    }

    private void loadSalesReport() {
        FirebaseDatabase.getInstance().getReference(Constants.NODE_ORDERS)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        int totalOrders = 0;
                        double totalRevenue = 0;
                        List<String[]> rows = new ArrayList<>();
                        for (DataSnapshot order : snapshot.getChildren()) {
                            long ts = getLong(order, "timestamp");
                            if (ts == 0) ts = getLong(order, "createdAt");
                            if (ts < fromTimestamp || ts > toTimestamp) continue;
                            totalOrders++;
                            double amount = getDouble(order, "total");
                            if (amount == 0) amount = getDouble(order, "totalAmount");
                            totalRevenue += amount;
                            String orderId = getString(order, "orderId");
                            if (orderId.length() > 13) orderId = "..." + orderId.substring(orderId.length() - 10);
                            rows.add(new String[]{ orderId, capitalise(getString(order, "status")), "Rs. " + df.format(amount) });
                        }
                        String dl = fromTimestamp == 0 ? "All Time" : sdf.format(new Date(fromTimestamp)) + " - " + sdf.format(new Date(toTimestamp));
                        int finalTotalOrders = totalOrders;
                        double finalTotalRevenue = totalRevenue;
                        runOnUiThread(() -> renderReport("Sales Report", dl, "Order ID", "Status", "Total", rows, finalTotalOrders, finalTotalRevenue, true));
                    }

                    @Override
                    public void onCancelled(DatabaseError e) {
                        showLoading(false);
                    }
                });
    }

    private void loadTopMedicinesReport() {
        FirebaseDatabase.getInstance().getReference(Constants.NODE_ORDERS)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        Map<String, Integer> qtyMap = new HashMap<>();
                        Map<String, Double> revMap = new HashMap<>();
                        for (DataSnapshot order : snapshot.getChildren()) {
                            long ts = getLong(order, "timestamp");
                            if (ts == 0) ts = getLong(order, "createdAt");
                            if (ts < fromTimestamp || ts > toTimestamp) continue;
                            for (DataSnapshot item : order.child("items").getChildren()) {
                                String name = getString(item, "medicineName");
                                if (name.isEmpty()) name = getString(item, "name");
                                if (name.isEmpty()) continue;
                                int qty = (int) getLong(item, "quantity");
                                double itemTotal = getDouble(item, "itemTotal");
                                if (itemTotal == 0) itemTotal = getDouble(item, "price") * qty;
                                qtyMap.merge(name, qty, Integer::sum);
                                revMap.merge(name, itemTotal, Double::sum);
                            }
                        }
                        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(qtyMap.entrySet());
                        sorted.sort((a, b) -> b.getValue() - a.getValue());
                        List<String[]> rows = new ArrayList<>();
                        int rank = 1;
                        for (Map.Entry<String, Integer> e : sorted) {
                            String n = e.getKey().length() > 24 ? e.getKey().substring(0, 22) + "..." : e.getKey();
                            rows.add(new String[]{ rank++ + ". " + n, e.getValue() + " units", "Rs. " + df.format(revMap.getOrDefault(e.getKey(), 0.0)) });
                        }
                        String dl = fromTimestamp == 0 ? "All Time" : sdf.format(new Date(fromTimestamp)) + " - " + sdf.format(new Date(toTimestamp));
                        runOnUiThread(() -> renderReport("Top Selling Medicines", dl, "Medicine", "Sold", "Revenue", rows, 0, 0, false));
                    }

                    @Override
                    public void onCancelled(DatabaseError e) {
                        showLoading(false);
                    }
                });
    }

    private void loadStockReport() {
        FirebaseDatabase.getInstance().getReference("medicines")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<String[]> rows = new ArrayList<>();
                        for (DataSnapshot med : snapshot.getChildren()) {
                            String name = getString(med, "name");
                            long stock = getLong(med, "stockQuantity");
                            String status = stock == 0 ? "Out of Stock" : stock <= 10 ? "Low Stock" : "In Stock";
                            String display = name.length() > 24 ? name.substring(0, 22) + "..." : name;
                            rows.add(new String[]{ display, stock + " units", status });
                        }
                        rows.sort((a, b) -> stockRank(a[2]) - stockRank(b[2]));
                        runOnUiThread(() -> renderReport("Stock Report", "Generated: " + sdf.format(new Date()), "Medicine", "Stock", "Status", rows, 0, 0, false));
                    }

                    @Override
                    public void onCancelled(DatabaseError e) {
                        showLoading(false);
                    }
                });
    }

    private int stockRank(String s) {
        if (s.equals("Out of Stock")) return 0;
        if (s.equals("Low Stock")) return 1;
        return 2;
    }

    private void renderReport(String title, String subtitle, String col1, String col2, String col3,
                              List<String[]> rows, int totalOrders, double totalRevenue, boolean showStats) {
        showLoading(false);
        lastReportTitle = title;
        lastReportSubtitle = subtitle;
        lastCol1 = col1;
        lastCol2 = col2;
        lastCol3 = col3;
        lastRows.clear();
        lastRows.addAll(rows);
        lastTotalOrders = totalOrders;
        lastTotalRevenue = totalRevenue;
        isSalesReport = showStats;

        tvReportTitle.setText(title);
        tvReportSubtitle.setText(subtitle);
        layoutSummaryStats.setVisibility(showStats ? View.VISIBLE : View.GONE);
        if (showStats) {
            tvTotalOrders.setText(String.valueOf(totalOrders));
            tvTotalRevenue.setText(df.format(totalRevenue));
        }
        tvCol1Header.setText(col1);
        tvCol2Header.setText(col2);
        tvCol3Header.setText(col3);

        layoutTableRows.removeAllViews();
        if (rows.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No data found for this period.");
            empty.setTextColor(Color.GRAY);
            empty.setPadding(8, 24, 8, 8);
            layoutTableRows.addView(empty);
        } else {
            for (int i = 0; i < rows.size(); i++) {
                String[] row = rows.get(i);
                LinearLayout rowLayout = new LinearLayout(this);
                rowLayout.setOrientation(LinearLayout.HORIZONTAL);
                rowLayout.setPadding(0, 12, 0, 12);
                rowLayout.setBackgroundColor(i % 2 == 0 ? Color.parseColor("#F9F9F9") : Color.WHITE);
                rowLayout.addView(makeCell(row[0], 2, false, Color.BLACK));
                rowLayout.addView(makeCell(row[1], 1, true, statusColor(row[1])));
                rowLayout.addView(makeCell(row[2], 1, false, Color.BLACK));
                layoutTableRows.addView(rowLayout);
            }
        }
        cardResults.setVisibility(View.VISIBLE);
        btnExportPdf.setVisibility(View.VISIBLE);
    }

    private TextView makeCell(String text, int weight, boolean center, int color) {
        TextView tv = new TextView(this);
        tv.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weight));
        tv.setText(text);
        tv.setTextSize(12f);
        tv.setTextColor(color);
        tv.setPadding(4, 0, 4, 0);
        if (center) tv.setGravity(android.view.Gravity.CENTER);
        return tv;
    }

    private int statusColor(String t) {
        String tl = t.toLowerCase();
        if (tl.contains("out of stock") || tl.contains("cancel")) return Color.RED;
        if (tl.contains("low stock")) return Color.parseColor("#E65100");
        if (tl.contains("in stock") || tl.contains("deliver")) return Color.parseColor("#2E7D32");
        return Color.BLACK;
    }

    private void exportPdf() {
        PdfDocument doc = new PdfDocument();
        final int W = 595, H = 842, M = 40;

        Paint pHdrBg = fp("#1A5276");
        Paint pWhtB = tp(Color.WHITE, 20, true);
        Paint pWhtSm = tp(Color.WHITE, 11, false);
        Paint pColBg = fp("#2E4057");
        Paint pColTxt = tp(Color.WHITE, 11, true);
        Paint pEven = fp("#F5F5F5");
        Paint pOdd = fp("#FFFFFF");
        Paint pCell = tp(Color.BLACK, 11, false);
        Paint pRed = tp(Color.RED, 11, false);
        Paint pOrg = tp(Color.parseColor("#E65100"), 11, false);
        Paint pGrn = tp(Color.parseColor("#2E7D32"), 11, false);
        Paint pLine = new Paint();
        pLine.setColor(Color.LTGRAY);
        pLine.setStrokeWidth(0.5f);
        Paint pStatBg = fp("#EBF5FB");
        Paint pStatN = tp(Color.parseColor("#1A5276"), 22, true);
        Paint pStatL = tp(Color.GRAY, 10, false);
        Paint pFoot = tp(Color.GRAY, 9, false);
        Paint pBrand = tp(Color.parseColor("#E74C3C"), 11, true);

        int rowsPerPage = 24;
        int totalPages = Math.max(1, (int) Math.ceil((double) lastRows.size() / rowsPerPage));

        for (int p = 0; p < totalPages; p++) {
            PdfDocument.Page page = doc.startPage(new PdfDocument.PageInfo.Builder(W, H, p + 1).create());
            Canvas c = page.getCanvas();
            int y;

            c.drawRect(0, 0, W, 80, pHdrBg);
            c.drawText("MediHealth Pharmacies", M, 28, pWhtB);
            c.drawText("MediGo Admin  |  " + lastReportTitle, M, 48, pWhtSm);
            c.drawText(lastReportSubtitle, M, 64, pWhtSm);
            c.drawText("STYRO", W - M - 38, 30, pBrand);
            c.drawText("Generated: " + sdfFull.format(new Date()), W - M - 115, 64, pFoot);
            y = 92;

            if (isSalesReport && p == 0) {
                int bw = (W - 2 * M - 10) / 2;
                c.drawRect(M, y, M + bw, y + 52, pStatBg);
                c.drawText(String.valueOf(lastTotalOrders), M + 10, y + 32, pStatN);
                c.drawText("Total Orders", M + 10, y + 47, pStatL);
                c.drawRect(M + bw + 10, y, M + 2 * bw + 10, y + 52, pStatBg);
                c.drawText("Rs. " + df.format(lastTotalRevenue), M + bw + 20, y + 32, pStatN);
                c.drawText("Total Revenue", M+bw+20, y+47, pStatL);
                y += 62;
            }

            int cw1 = (int)((W - 2 * M) * 0.45), cw2 = (int)((W - 2 * M) * 0.25), cw3 = (W - 2 * M) - cw1 - cw2;
            c.drawRect(M, y, W - M, y + 22, pColBg);
            c.drawText(lastCol1, M + 4, y + 15, pColTxt);
            c.drawText(lastCol2, M + cw1 + 4, y + 15, pColTxt);
            c.drawText(lastCol3, M + cw1 + cw2 + 4, y + 15, pColTxt);
            y += 22;

            int start = p * rowsPerPage, end = Math.min(start + rowsPerPage, lastRows.size()), rh = 20;
            for (int i = start; i < end; i++) {
                String[] row = lastRows.get(i);
                c.drawRect(M, y, W - M, y + rh, i % 2 == 0 ? pEven : pOdd);
                String tl = row[1].toLowerCase();
                Paint cp = (tl.contains("out of stock") || tl.contains("cancel")) ? pRed : tl.contains("low stock") ? pOrg : (tl.contains("in stock") || tl.contains("deliver")) ? pGrn : pCell;
                c.drawText(tr(row[0], 40), M + 4, y + 14, pCell);
                c.drawText(tr(row[1], 22), M + cw1 + 4, y + 14, cp);
                c.drawText(tr(row[2], 20), M + cw1 + cw2 + 4, y + 14, pCell);
                c.drawLine(M, y + rh, W - M, y + rh, pLine);
                y += rh;
            }

            c.drawLine(M, H - 30, W - M, H - 30, pLine);
            c.drawText("MediGo by STYRO  |  MediHealth Pharmacies  |  Confidential", M, H - 16, pFoot);
            c.drawText("Page " + (p + 1) + " of " + totalPages, W - M - 52, H - 16, pFoot);
            doc.finishPage(page);
        }

        try {
            String fn = "MediGo_" + lastReportTitle.replace(" ", "_") + "_" + new SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(new Date()) + ".pdf";
            File dir = new File(getCacheDir(), "reports");
            if (!dir.exists()) dir.mkdirs();
            File file = new File(dir, fn);
            doc.writeTo(new FileOutputStream(file));
            doc.close();
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Open PDF with..."));
            Toast.makeText(this, "PDF ready: " + fn, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            doc.close();
        }
    }

    private Paint fp(String hex) {
        Paint p = new Paint();
        p.setColor(Color.parseColor(hex));
        p.setStyle(Paint.Style.FILL);
        return p;
    }

    private Paint tp(int color, int size, boolean bold) {
        Paint p = new Paint();
        p.setColor(color);
        p.setTextSize(size);
        if (bold) p.setTypeface(Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD));
        return p;
    }

    private String tr(String s, int max) {
        return s.length() > max ? s.substring(0, max - 1) + "..." : s;
    }

    private String getString(DataSnapshot snap, String key) {
        Object v = snap.child(key).getValue();
        return v == null ? "" : String.valueOf(v).trim();
    }

    private long getLong(DataSnapshot snap, String key) {
        Object v = snap.child(key).getValue();
        if (v == null) return 0L;
        try {
            return Long.parseLong(String.valueOf(v));
        } catch (Exception e) {
            return 0L;
        }
    }

    private double getDouble(DataSnapshot snap, String key) {
        Object v = snap.child(key).getValue();
        if (v == null) return 0.0;
        try {
            return Double.parseDouble(String.valueOf(v));
        } catch (Exception e) {
            return 0.0;
        }
    }

    private String capitalise(String s) {
        if (s == null || s.isEmpty()) return "";
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).replace("_", " ");
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnGenerateReport.setEnabled(!show);
    }
}
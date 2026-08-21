package com.restaurant.pos.ui.admin;

import com.restaurant.pos.AppContext;
import com.restaurant.pos.model.Order;
import com.restaurant.pos.service.CashierSalesReport;
import com.restaurant.pos.service.SalesReport;
import com.restaurant.pos.service.TopSellingItem;
import com.restaurant.pos.ui.components.DatePickerButton;
import com.restaurant.pos.ui.components.KpiCard;
import com.restaurant.pos.ui.components.StripedTableCellRenderer;
import com.restaurant.pos.ui.format.MoneyFormatter;
import com.restaurant.pos.ui.theme.AppTheme;
import com.restaurant.pos.ui.theme.Icons;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

public final class ReportsPanel extends JPanel {

    private static final int ROW_HEIGHT = 38;
    private static final DateTimeFormatter DATETIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    private final AppContext context;

    private final DatePickerButton fromPicker = new DatePickerButton(LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()));
    private final DatePickerButton toPicker   = new DatePickerButton(LocalDate.now());

    private final KpiCard grossCard  = new KpiCard("Gross Revenue",  null, Icons.reports(AppTheme.ACCENT, 16));
    private final KpiCard netCard    = new KpiCard("Net Revenue",    null, Icons.trendingUp(AppTheme.SUCCESS, 16));
    private final KpiCard vatCard    = new KpiCard("VAT Collected",  null, Icons.percent(AppTheme.TEXT_SECONDARY, 16));
    private final KpiCard ordersCard = new KpiCard("Total Orders",   null, Icons.orders(AppTheme.TEXT_SECONDARY, 16));

    private final TopSellingItemsTableModel topItemsModel = new TopSellingItemsTableModel();
    private final CashierSalesTableModel cashierSalesModel = new CashierSalesTableModel();
    private final VoidReportTableModel voidReportModel = new VoidReportTableModel();

    private SalesReport currentSalesReport;
    private List<CashierSalesReport> currentCashierReport = new ArrayList<>();
    private List<Order> currentVoidReport = new ArrayList<>();

    public ReportsPanel(AppContext context) {
        super(new BorderLayout(0, 16));
        this.context = context;

        setBackground(AppTheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(buildHeaderSection(), BorderLayout.NORTH);
        add(buildMainSection(), BorderLayout.CENTER);

        runReport();
    }

    private JPanel buildHeaderSection() {
        JPanel header = new JPanel(new MigLayout("insets 0, fillx, wrap 1", "[grow, fill]", "[]12[]"));
        header.setOpaque(false);

        JPanel titleRow = new JPanel(new MigLayout("insets 0, fillx", "[grow][]"));
        titleRow.setOpaque(false);

        JPanel titleBox = new JPanel(new MigLayout("insets 0, wrap 1, gapy 2"));
        titleBox.setOpaque(false);

        JLabel title = new JLabel("Reports & Analytics");
        title.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_PAGE_TITLE));
        title.setForeground(AppTheme.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Analyze revenue performance, top selling items, and cashier sales breakdowns");
        subtitle.setFont(AppTheme.captionFont());
        subtitle.setForeground(AppTheme.TEXT_SECONDARY);

        titleBox.add(title);
        titleBox.add(subtitle);
        titleRow.add(titleBox, "growx");

        JPanel exportGroup = new JPanel(new MigLayout("insets 0", "[]8[]8[]"));
        exportGroup.setOpaque(false);

        JButton exportCsvBtn = createExportButton("Export CSV", Icons.fileText(AppTheme.TEXT_PRIMARY, 14));
        exportCsvBtn.addActionListener(e -> exportCsv());

        JButton exportExcelBtn = createExportButton("Export Excel", Icons.download(AppTheme.TEXT_PRIMARY, 14));
        exportExcelBtn.addActionListener(e -> exportExcel());

        JButton exportPdfBtn = createExportButton("Export PDF", Icons.download(AppTheme.TEXT_PRIMARY, 14));
        exportPdfBtn.addActionListener(e -> exportPdf());

        exportGroup.add(exportCsvBtn, "h 38!");
        exportGroup.add(exportExcelBtn, "h 38!");
        exportGroup.add(exportPdfBtn, "h 38!");
        titleRow.add(exportGroup, "align right");

        header.add(titleRow, "growx");

        JPanel filterBar = new JPanel(new MigLayout("insets 12 16 12 16, fillx", "[]8[]16[]8[]16[]6[]6[]16[]push"));
        filterBar.setBackground(AppTheme.CARD);
        filterBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));

        JLabel fromLabel = new JLabel("From:");
        fromLabel.setFont(AppTheme.bodyFont());
        fromLabel.setForeground(AppTheme.TEXT_SECONDARY);

        JLabel toLabel = new JLabel("To:");
        toLabel.setFont(AppTheme.bodyFont());
        toLabel.setForeground(AppTheme.TEXT_SECONDARY);

        JButton todayBtn = createPresetButton("Today");
        todayBtn.addActionListener(e -> {
            LocalDate today = LocalDate.now();
            fromPicker.setSelectedDate(today);
            toPicker.setSelectedDate(today);
            runReport();
        });

        JButton weekBtn = createPresetButton("This Week");
        weekBtn.addActionListener(e -> {
            LocalDate today = LocalDate.now();
            fromPicker.setSelectedDate(today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)));
            toPicker.setSelectedDate(today);
            runReport();
        });

        JButton monthBtn = createPresetButton("This Month");
        monthBtn.addActionListener(e -> {
            LocalDate today = LocalDate.now();
            fromPicker.setSelectedDate(today.with(TemporalAdjusters.firstDayOfMonth()));
            toPicker.setSelectedDate(today);
            runReport();
        });

        JButton runBtn = new JButton("Run Report");
        runBtn.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_BODY));
        runBtn.setIcon(Icons.refresh(Color.WHITE, 14));
        runBtn.setIconTextGap(8);
        runBtn.setBackground(AppTheme.PRIMARY);
        runBtn.setForeground(Color.WHITE);
        runBtn.setFocusPainted(false);
        runBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        runBtn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        runBtn.addActionListener(e -> runReport());

        filterBar.add(fromLabel);
        filterBar.add(fromPicker, "w 160!, h 36!");
        filterBar.add(toLabel);
        filterBar.add(toPicker, "w 160!, h 36!");
        filterBar.add(todayBtn, "h 36!");
        filterBar.add(weekBtn, "h 36!");
        filterBar.add(monthBtn, "h 36!");
        filterBar.add(runBtn, "h 36!");

        header.add(filterBar, "growx");

        return header;
    }

    private JPanel buildMainSection() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);

        JPanel kpiGrid = new JPanel(new MigLayout("insets 0, fillx", "[grow,fill]12[grow,fill]12[grow,fill]12[grow,fill]", "[]"));
        kpiGrid.setOpaque(false);
        kpiGrid.add(grossCard, "h 110!");
        kpiGrid.add(netCard, "h 110!");
        kpiGrid.add(vatCard, "h 110!");
        kpiGrid.add(ordersCard, "h 110!");
        panel.add(kpiGrid, BorderLayout.NORTH);

        panel.add(buildTabsPane(), BorderLayout.CENTER);
        return panel;
    }

    private JButton createPresetButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(AppTheme.bodyFont());
        btn.setBackground(AppTheme.CARD);
        btn.setForeground(AppTheme.TEXT_PRIMARY);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createExportButton(String text, javax.swing.Icon icon) {
        JButton btn = new JButton(text);
        if (icon != null) {
            btn.setIcon(icon);
            btn.setIconTextGap(8);
        }
        btn.setFont(AppTheme.bodyFont());
        btn.setBackground(AppTheme.CARD);
        btn.setForeground(AppTheme.TEXT_PRIMARY);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER),
                BorderFactory.createEmptyBorder(7, 16, 7, 16)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JTabbedPane buildTabsPane() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_BODY));

        tabs.addTab("Sales Summary", Icons.reports(AppTheme.TEXT_SECONDARY, 15), buildSalesSummaryTab());
        tabs.addTab("Sales by Cashier", Icons.users(AppTheme.TEXT_SECONDARY, 15), buildSalesByCashierTab());
        tabs.addTab("Voids & Cancellations", Icons.xCircle(AppTheme.TEXT_SECONDARY, 15), buildVoidReportTab());

        return tabs;
    }

    private JPanel buildSalesSummaryTab() {
        JPanel tableSection = new JPanel(new BorderLayout(0, 10));
        tableSection.setBackground(AppTheme.CARD);
        tableSection.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)));

        JLabel tableTitle = new JLabel("Top Selling Items");
        tableTitle.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_SECTION_HEADER));
        tableTitle.setForeground(AppTheme.TEXT_PRIMARY);
        tableSection.add(tableTitle, BorderLayout.NORTH);

        JTable topTable = new JTable(topItemsModel);
        topTable.setRowHeight(ROW_HEIGHT);
        topTable.getTableHeader().setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_TABLE_HEADER));
        topTable.setFont(AppTheme.bodyFont());
        StripedTableCellRenderer.apply(topTable);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < 3; i++) {
            topTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        topTable.getColumnModel().getColumn(0).setPreferredWidth(300);
        topTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        topTable.getColumnModel().getColumn(2).setPreferredWidth(160);

        JScrollPane scrollPane = new JScrollPane(topTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER));
        tableSection.add(scrollPane, BorderLayout.CENTER);

        return tableSection;
    }

    private JPanel buildSalesByCashierTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(AppTheme.CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)));

        JTable cashierTable = new JTable(cashierSalesModel);
        cashierTable.setRowHeight(ROW_HEIGHT);
        cashierTable.getTableHeader().setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_TABLE_HEADER));
        cashierTable.setFont(AppTheme.bodyFont());
        StripedTableCellRenderer.apply(cashierTable);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < 3; i++) {
            cashierTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        cashierTable.getColumnModel().getColumn(0).setPreferredWidth(260);
        cashierTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        cashierTable.getColumnModel().getColumn(2).setPreferredWidth(160);

        JScrollPane scrollPane = new JScrollPane(cashierTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER));
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildVoidReportTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(AppTheme.CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)));

        JTable voidTable = new JTable(voidReportModel);
        voidTable.setRowHeight(ROW_HEIGHT);
        voidTable.getTableHeader().setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_TABLE_HEADER));
        voidTable.setFont(AppTheme.bodyFont());
        StripedTableCellRenderer.apply(voidTable);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < 5; i++) {
            voidTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        voidTable.getColumnModel().getColumn(0).setPreferredWidth(90);
        voidTable.getColumnModel().getColumn(1).setPreferredWidth(160);
        voidTable.getColumnModel().getColumn(2).setPreferredWidth(140);
        voidTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        voidTable.getColumnModel().getColumn(4).setPreferredWidth(220);

        JScrollPane scrollPane = new JScrollPane(voidTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER));
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void runReport() {
        LocalDate fromDate = fromPicker.getSelectedDate();
        LocalDate toDate   = toPicker.getSelectedDate();

        Instant from = fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant to   = toDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        currentSalesReport  = context.reportService().salesReport(from, to);
        currentCashierReport = context.reportService().salesByCashier(from, to);
        currentVoidReport    = context.reportService().voidReport(from, to);

        grossCard.setValue(MoneyFormatter.format(currentSalesReport.grossRevenue()));
        netCard.setValue(MoneyFormatter.format(currentSalesReport.netRevenue()));
        vatCard.setValue(MoneyFormatter.format(currentSalesReport.vatCollected()));
        ordersCard.setValue(String.valueOf(currentSalesReport.orderCount()));

        topItemsModel.setItems(currentSalesReport.topSellingItems());
        cashierSalesModel.setItems(currentCashierReport);
        voidReportModel.setOrders(currentVoidReport);
    }

    private void exportCsv() {
        if (currentSalesReport == null) {
            runReport();
        }
        if (currentSalesReport == null) return;
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("sales_report_" + LocalDate.now() + ".csv"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".csv")) {
                file = new File(file.getAbsolutePath() + ".csv");
            }
            try (FileWriter out = new FileWriter(file);
                 CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT)) {

                printer.printRecord(com.restaurant.pos.validation.CsvSanitizer.sanitizeArray("SALES SUMMARY REPORT", "From: " + fromPicker.getSelectedDate(), "To: " + toPicker.getSelectedDate()));
                printer.printRecord(com.restaurant.pos.validation.CsvSanitizer.sanitizeArray("Gross Revenue", MoneyFormatter.format(currentSalesReport.grossRevenue())));
                printer.printRecord(com.restaurant.pos.validation.CsvSanitizer.sanitizeArray("Net Revenue", MoneyFormatter.format(currentSalesReport.netRevenue())));
                printer.printRecord(com.restaurant.pos.validation.CsvSanitizer.sanitizeArray("VAT Collected", MoneyFormatter.format(currentSalesReport.vatCollected())));
                printer.printRecord(com.restaurant.pos.validation.CsvSanitizer.sanitizeArray("Total Orders", currentSalesReport.orderCount()));
                printer.println();

                printer.printRecord(com.restaurant.pos.validation.CsvSanitizer.sanitizeArray("TOP SELLING ITEMS"));
                printer.printRecord(com.restaurant.pos.validation.CsvSanitizer.sanitizeArray("Item Name", "Quantity Sold", "Total Revenue"));
                for (TopSellingItem item : currentSalesReport.topSellingItems()) {
                    printer.printRecord(com.restaurant.pos.validation.CsvSanitizer.sanitizeArray(item.itemName(), item.quantitySold(), MoneyFormatter.format(item.revenue())));
                }
                printer.println();

                printer.printRecord(com.restaurant.pos.validation.CsvSanitizer.sanitizeArray("SALES BY CASHIER"));
                printer.printRecord(com.restaurant.pos.validation.CsvSanitizer.sanitizeArray("Cashier Username", "Total Orders", "Total Revenue"));
                if (currentCashierReport != null) {
                    for (CashierSalesReport c : currentCashierReport) {
                        printer.printRecord(com.restaurant.pos.validation.CsvSanitizer.sanitizeArray(c.cashierName(), c.orderCount(), MoneyFormatter.format(c.totalSales())));
                    }
                }

                JOptionPane.showMessageDialog(this, "CSV exported successfully to " + file.getName(), "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error exporting CSV: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportExcel() {
        if (currentSalesReport == null) {
            runReport();
        }
        if (currentSalesReport == null) return;
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("sales_report_" + LocalDate.now() + ".xlsx"));
        chooser.setFileFilter(new FileNameExtensionFilter("Excel Spreadsheet (*.xlsx)", "xlsx"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".xlsx")) {
                file = new File(file.getAbsolutePath() + ".xlsx");
            }
            try (XSSFWorkbook workbook = new XSSFWorkbook();
                 FileOutputStream out = new FileOutputStream(file)) {

                Sheet sheet1 = workbook.createSheet("Sales & Top Items");
                Row r0 = sheet1.createRow(0);
                r0.createCell(0).setCellValue("Gross Revenue");
                r0.createCell(1).setCellValue(MoneyFormatter.format(currentSalesReport.grossRevenue()));
                Row r1 = sheet1.createRow(1);
                r1.createCell(0).setCellValue("Net Revenue");
                r1.createCell(1).setCellValue(MoneyFormatter.format(currentSalesReport.netRevenue()));
                Row r2 = sheet1.createRow(2);
                r2.createCell(0).setCellValue("VAT Collected");
                r2.createCell(1).setCellValue(MoneyFormatter.format(currentSalesReport.vatCollected()));
                Row r3 = sheet1.createRow(3);
                r3.createCell(0).setCellValue("Total Orders");
                r3.createCell(1).setCellValue(currentSalesReport.orderCount());

                Row header1 = sheet1.createRow(5);
                header1.createCell(0).setCellValue("Item Name");
                header1.createCell(1).setCellValue("Quantity Sold");
                header1.createCell(2).setCellValue("Total Revenue");

                int rowIdx = 6;
                for (TopSellingItem item : currentSalesReport.topSellingItems()) {
                    Row row = sheet1.createRow(rowIdx++);
                    row.createCell(0).setCellValue(item.itemName());
                    row.createCell(1).setCellValue(item.quantitySold());
                    row.createCell(2).setCellValue(MoneyFormatter.format(item.revenue()));
                }

                Sheet sheet2 = workbook.createSheet("Sales by Cashier");
                Row header2 = sheet2.createRow(0);
                header2.createCell(0).setCellValue("Cashier Username");
                header2.createCell(1).setCellValue("Total Orders");
                header2.createCell(2).setCellValue("Total Revenue");
                int rowIdx2 = 1;
                if (currentCashierReport != null) {
                    for (CashierSalesReport c : currentCashierReport) {
                        Row row = sheet2.createRow(rowIdx2++);
                        row.createCell(0).setCellValue(c.cashierName());
                        row.createCell(1).setCellValue(c.orderCount());
                        row.createCell(2).setCellValue(MoneyFormatter.format(c.totalSales()));
                    }
                }

                workbook.write(out);
                JOptionPane.showMessageDialog(this, "Excel exported successfully to " + file.getName(), "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error exporting Excel: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportPdf() {
        if (currentSalesReport == null) {
            runReport();
        }
        if (currentSalesReport == null) return;
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("sales_report_" + LocalDate.now() + ".pdf"));
        chooser.setFileFilter(new FileNameExtensionFilter("PDF Document (*.pdf)", "pdf"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".pdf")) {
                file = new File(file.getAbsolutePath() + ".pdf");
            }
            try (PDDocument document = new PDDocument()) {
                PDPage page = new PDPage(PDRectangle.LETTER);
                document.addPage(page);

                try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                    stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
                    stream.beginText();
                    stream.newLineAtOffset(50, 750);
                    stream.showText("Sales Summary Report (" + fromPicker.getSelectedDate() + " to " + toPicker.getSelectedDate() + ")");
                    stream.endText();

                    stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);
                    stream.setLeading(16f);
                    stream.beginText();
                    stream.newLineAtOffset(50, 720);
                    stream.showText("Gross Revenue: " + sanitizeForPdf(MoneyFormatter.format(currentSalesReport.grossRevenue())));
                    stream.newLine();
                    stream.showText("Net Revenue: " + sanitizeForPdf(MoneyFormatter.format(currentSalesReport.netRevenue())));
                    stream.newLine();
                    stream.showText("VAT Collected: " + sanitizeForPdf(MoneyFormatter.format(currentSalesReport.vatCollected())));
                    stream.newLine();
                    stream.showText("Total Orders: " + currentSalesReport.orderCount());
                    stream.newLine();
                    stream.newLine();
                    stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                    stream.showText("Top Selling Items:");
                    stream.newLine();
                    stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                    for (TopSellingItem item : currentSalesReport.topSellingItems()) {
                        String line = String.format("- %s | Qty: %d | Total: %s",
                                item.itemName(), item.quantitySold(), sanitizeForPdf(MoneyFormatter.format(item.revenue())));
                        stream.showText(line);
                        stream.newLine();
                    }
                    stream.endText();
                }

                document.save(file);
                JOptionPane.showMessageDialog(this, "PDF exported successfully to " + file.getName(), "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error exporting PDF: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static String sanitizeForPdf(String text) {
        if (text == null) return "";
        return text.replace("\u20B1", "PHP ").replace("₱", "PHP ");
    }

    private static final class TopSellingItemsTableModel extends AbstractTableModel {
        private static final String[] COLUMNS = {"Item Name", "Quantity Sold", "Total Revenue"};
        private List<TopSellingItem> items = new ArrayList<>();

        void setItems(List<TopSellingItem> items) { this.items = new ArrayList<>(items); fireTableDataChanged(); }

        @Override public int getRowCount()    { return items.size(); }
        @Override public int getColumnCount() { return COLUMNS.length; }
        @Override public String getColumnName(int col) { return COLUMNS[col]; }

        @Override
        public Object getValueAt(int row, int col) {
            TopSellingItem item = items.get(row);
            return switch (col) {
                case 0 -> item.itemName();
                case 1 -> item.quantitySold();
                case 2 -> MoneyFormatter.format(item.revenue());
                default -> "";
            };
        }
    }

    private static final class CashierSalesTableModel extends AbstractTableModel {
        private static final String[] COLUMNS = {"Cashier Name", "Orders Count", "Total Sales"};
        private List<CashierSalesReport> items = new ArrayList<>();

        void setItems(List<CashierSalesReport> items) { this.items = new ArrayList<>(items); fireTableDataChanged(); }

        @Override public int getRowCount()    { return items.size(); }
        @Override public int getColumnCount() { return COLUMNS.length; }
        @Override public String getColumnName(int col) { return COLUMNS[col]; }

        @Override
        public Object getValueAt(int row, int col) {
            CashierSalesReport c = items.get(row);
            return switch (col) {
                case 0 -> c.cashierName();
                case 1 -> c.orderCount();
                case 2 -> MoneyFormatter.format(c.totalSales());
                default -> "";
            };
        }
    }

    private static final class VoidReportTableModel extends AbstractTableModel {
        private static final String[] COLUMNS = {"Order #", "Voided At", "Cashier", "Total Due", "Reason"};
        private List<Order> orders = new ArrayList<>();

        void setOrders(List<Order> orders) { this.orders = new ArrayList<>(orders); fireTableDataChanged(); }

        @Override public int getRowCount()    { return orders.size(); }
        @Override public int getColumnCount() { return COLUMNS.length; }
        @Override public String getColumnName(int col) { return COLUMNS[col]; }

        @Override
        public Object getValueAt(int row, int col) {
            Order o = orders.get(row);
            return switch (col) {
                case 0 -> o.orderNumber();
                case 1 -> o.voidedAt() != null ? DATETIME_FORMAT.format(o.voidedAt()) : "-";
                case 2 -> o.cashierName();
                case 3 -> MoneyFormatter.format(o.totalDue());
                case 4 -> o.voidReason() != null ? o.voidReason() : "";
                default -> "";
            };
        }
    }
}

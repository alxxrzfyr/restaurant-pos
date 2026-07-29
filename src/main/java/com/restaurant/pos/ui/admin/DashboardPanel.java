package com.restaurant.pos.ui.admin;

import com.restaurant.pos.AppContext;
import com.restaurant.pos.model.Money;
import com.restaurant.pos.model.Order;
import com.restaurant.pos.model.OrderStatus;
import com.restaurant.pos.model.OrderType;
import com.restaurant.pos.model.User;
import com.restaurant.pos.service.SalesReport;
import com.restaurant.pos.service.TopSellingItem;
import com.restaurant.pos.ui.components.KpiCard;
import com.restaurant.pos.ui.components.StripedTableCellRenderer;
import com.restaurant.pos.ui.format.MoneyFormatter;
import com.restaurant.pos.ui.theme.AppTheme;
import com.restaurant.pos.ui.theme.Icons;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class DashboardPanel extends JPanel {

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("hh:mm:ss a").withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy").withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter ORDER_TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault());

    private static final Color PURPLE = Color.decode("#8B5CF6");
    private static final Color TEAL   = Color.decode("#0D9488");
    private static final Color AMBER  = Color.decode("#D97706");

    private final AppContext context;
    private final User currentUser;

    private final JLabel dateLabel = new JLabel();
    private final JLabel timeLabel = new JLabel();
    private final JLabel lastUpdatedLabel = new JLabel("Updated: Just now");
    private final Timer clockTimer;

    private final KpiCard salesCard        = new KpiCard("Today's Sales",   AppTheme.PRIMARY, Icons.reports(AppTheme.TEXT_SECONDARY, 18));
    private final KpiCard revenueCard      = new KpiCard("Net Revenue",      AppTheme.SUCCESS, Icons.fileText(AppTheme.TEXT_SECONDARY, 18));
    private final KpiCard transactionsCard  = new KpiCard("Transactions",    AppTheme.WARNING, Icons.orders(AppTheme.TEXT_SECONDARY, 18));
    private final KpiCard itemsSoldCard    = new KpiCard("Items Sold",       PURPLE,           Icons.menu(AppTheme.TEXT_SECONDARY, 18));
    private final KpiCard aovCard          = new KpiCard("Avg Order Value", TEAL,             Icons.user(AppTheme.TEXT_SECONDARY, 18));
    private final KpiCard topItemCard      = new KpiCard("Top Selling Item", AMBER,            Icons.checkCircle(AppTheme.TEXT_SECONDARY, 18));

    private final DailySalesChart salesChart = new DailySalesChart();
    private DailySalesChart.ChartMode activeChartMode = DailySalesChart.ChartMode.DAILY;
    private final JButton btnDaily   = new JButton("Daily");
    private final JButton btnWeekly  = new JButton("Weekly");
    private final JButton btnMonthly = new JButton("Monthly");

    private final RecentOrdersTableModel recentOrdersModel = new RecentOrdersTableModel();

    private final TopSellingTableModel topSellingModel = new TopSellingTableModel();

    private final JLabel summaryVatVal       = new JLabel("₱0.00");
    private final JLabel summaryDiscountVal  = new JLabel("₱0.00");
    private final JLabel summaryVoidVal      = new JLabel("0");
    private final JLabel summaryAvgItemsVal  = new JLabel("0.0");
    private final JLabel summaryChannelVal   = new JLabel("0 Dine-In / 0 Take-Out");

    public DashboardPanel(AppContext context, User currentUser) {
        super(new MigLayout("insets 16 16 16 16, fill, hidemode 3", "[grow]", "[][][grow 55, fill][grow 45, fill]"));
        this.context = context;
        this.currentUser = currentUser;

        setBackground(AppTheme.BACKGROUND);

        add(buildHeader(), "growx, wrap, gapbottom 12");
        add(buildKpiRow(), "growx, wrap, gapbottom 12");
        add(buildRow2(), "grow, push, wrap, gapbottom 12, h 260::");
        add(buildRow3(), "grow, push, h 220::");

        clockTimer = new Timer(1000, e -> updateClock());
        clockTimer.start();
        updateClock();

        refresh();
    }

    public DashboardPanel(AppContext context) {
        this(context, null);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new MigLayout("insets 0, fillx", "[grow][]"));
        header.setOpaque(false);

        JPanel titleBox = new JPanel(new MigLayout("insets 0, wrap 1"));
        titleBox.setOpaque(false);

        JLabel title = new JLabel("Executive Dashboard");
        title.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_DASHBOARD_TITLE));
        title.setForeground(AppTheme.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Real-time operational summary & today's sales metrics");
        subtitle.setFont(AppTheme.captionFont());
        subtitle.setForeground(AppTheme.TEXT_SECONDARY);

        titleBox.add(title);
        titleBox.add(subtitle);
        header.add(titleBox, "growx");

        JPanel rightBox = new JPanel(new MigLayout("insets 0, aligny center", "[]16[]16[]12[]"));
        rightBox.setOpaque(false);

        dateLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        dateLabel.setForeground(AppTheme.TEXT_PRIMARY);

        timeLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        timeLabel.setForeground(AppTheme.PRIMARY);

        lastUpdatedLabel.setFont(AppTheme.captionFont());
        lastUpdatedLabel.setForeground(AppTheme.TEXT_SECONDARY);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        refreshBtn.setIcon(Icons.refresh(Color.WHITE, 14));
        refreshBtn.setBackground(AppTheme.PRIMARY);
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> refresh());

        rightBox.add(dateLabel);
        rightBox.add(timeLabel);
        rightBox.add(lastUpdatedLabel);
        rightBox.add(refreshBtn, "h 34!, w 100!");

        header.add(rightBox, "align right");
        return header;
    }

    private JPanel buildKpiRow() {
        JPanel kpiRow = new JPanel(new MigLayout("insets 0, fillx", "[grow, fill]8[grow, fill]8[grow, fill]8[grow, fill]8[grow, fill]8[grow, fill]", "[fill]"));
        kpiRow.setOpaque(false);

        kpiRow.add(salesCard);
        kpiRow.add(revenueCard);
        kpiRow.add(transactionsCard);
        kpiRow.add(itemsSoldCard);
        kpiRow.add(aovCard);
        kpiRow.add(topItemCard);

        return kpiRow;
    }

    private JPanel buildRow2() {
        JPanel row = new JPanel(new MigLayout("insets 0, fill", "[grow 60, fill]12[grow 40, fill]", "[fill]"));
        row.setOpaque(false);

        row.add(buildChartSection(), "grow");
        row.add(buildRecentTransactionsSection(), "grow");

        return row;
    }

    private JPanel buildChartSection() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(AppTheme.CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)));

        JPanel headerBar = new JPanel(new BorderLayout());
        headerBar.setOpaque(false);

        JLabel title = new JLabel("Sales Performance");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        title.setForeground(AppTheme.TEXT_PRIMARY);
        headerBar.add(title, BorderLayout.WEST);

        JPanel btnGroupPanel = new JPanel(new MigLayout("insets 0", "[]4[]4[]"));
        btnGroupPanel.setOpaque(false);

        stylePeriodButton(btnDaily, DailySalesChart.ChartMode.DAILY);
        stylePeriodButton(btnWeekly, DailySalesChart.ChartMode.WEEKLY);
        stylePeriodButton(btnMonthly, DailySalesChart.ChartMode.MONTHLY);

        btnGroupPanel.add(btnDaily, "h 28!");
        btnGroupPanel.add(btnWeekly, "h 28!");
        btnGroupPanel.add(btnMonthly, "h 28!");

        headerBar.add(btnGroupPanel, BorderLayout.EAST);
        panel.add(headerBar, BorderLayout.NORTH);

        panel.add(salesChart, BorderLayout.CENTER);
        return panel;
    }

    private void stylePeriodButton(JButton button, DailySalesChart.ChartMode mode) {
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        updatePeriodButtonStyle(button, mode == activeChartMode);

        button.addActionListener(e -> {
            activeChartMode = mode;
            updatePeriodButtonStyle(btnDaily, activeChartMode == DailySalesChart.ChartMode.DAILY);
            updatePeriodButtonStyle(btnWeekly, activeChartMode == DailySalesChart.ChartMode.WEEKLY);
            updatePeriodButtonStyle(btnMonthly, activeChartMode == DailySalesChart.ChartMode.MONTHLY);
            loadChartData();
        });
    }

    private void updatePeriodButtonStyle(JButton button, boolean active) {
        if (active) {
            button.setBackground(AppTheme.PRIMARY);
            button.setForeground(Color.WHITE);
        } else {
            button.setBackground(new Color(241, 245, 249));
            button.setForeground(AppTheme.TEXT_PRIMARY);
        }
    }

    private JPanel buildRecentTransactionsSection() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(AppTheme.CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Recent Transactions");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        title.setForeground(AppTheme.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Newest First");
        subtitle.setFont(AppTheme.captionFont());
        subtitle.setForeground(AppTheme.TEXT_SECONDARY);

        header.add(title, BorderLayout.WEST);
        header.add(subtitle, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        JTable table = new JTable(recentOrdersModel);
        table.setRowHeight(32);
        table.getTableHeader().setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_TABLE_HEADER));
        table.setFont(AppTheme.bodyFont());
        table.setFillsViewportHeight(true);

        StripedTableCellRenderer.apply(table);
        setColumnAlignment(table, 0, SwingConstants.CENTER);
        setColumnAlignment(table, 1, SwingConstants.CENTER);
        setColumnAlignment(table, 3, SwingConstants.CENTER);
        setColumnAlignment(table, 4, SwingConstants.RIGHT);
        table.getColumnModel().getColumn(5).setCellRenderer(new StatusPillRenderer());

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildRow3() {
        JPanel row = new JPanel(new MigLayout("insets 0, fill", "[grow 65, fill]12[grow 35, fill]", "[fill]"));
        row.setOpaque(false);

        row.add(buildTopSellingSection(), "grow");
        row.add(buildTodaysSummarySection(), "grow");

        return row;
    }

    private JPanel buildTopSellingSection() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(AppTheme.CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Top Selling Items");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        title.setForeground(AppTheme.TEXT_PRIMARY);

        JLabel sub = new JLabel("Ranked by Quantity");
        sub.setFont(AppTheme.captionFont());
        sub.setForeground(AppTheme.TEXT_SECONDARY);

        header.add(title, BorderLayout.WEST);
        header.add(sub, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        JTable table = new JTable(topSellingModel);
        table.setRowHeight(32);
        table.getTableHeader().setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_TABLE_HEADER));
        table.setFont(AppTheme.bodyFont());
        table.setFillsViewportHeight(true);

        StripedTableCellRenderer.apply(table);
        setColumnAlignment(table, 0, SwingConstants.CENTER);
        setColumnAlignment(table, 2, SwingConstants.CENTER);
        setColumnAlignment(table, 3, SwingConstants.RIGHT);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildTodaysSummarySection() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(AppTheme.CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Today's Summary");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        title.setForeground(AppTheme.TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);

        panel.add(header, BorderLayout.NORTH);

        JPanel listPanel = new JPanel(new MigLayout("insets 12 4 12 4, fillx, wrap 2", "[grow][right]", "[]12[]12[]12[]12[]"));
        listPanel.setOpaque(false);

        addSummaryRow(listPanel, "VAT Collected", summaryVatVal);
        addSummaryRow(listPanel, "Discounts Given", summaryDiscountVal);
        addSummaryRow(listPanel, "Voided Orders", summaryVoidVal);
        addSummaryRow(listPanel, "Average Items / Txn", summaryAvgItemsVal);
        addSummaryRow(listPanel, "Order Channels", summaryChannelVal);

        panel.add(listPanel, BorderLayout.CENTER);
        return panel;
    }

    private void addSummaryRow(JPanel parent, String labelText, JLabel valueLabel) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        label.setForeground(AppTheme.TEXT_SECONDARY);

        valueLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        valueLabel.setForeground(AppTheme.TEXT_PRIMARY);

        parent.add(label);
        parent.add(valueLabel, "growx");
    }

    private void updateClock() {
        Instant now = Instant.now();
        dateLabel.setText(DATE_FORMATTER.format(now));
        timeLabel.setText(TIME_FORMATTER.format(now));
    }

    private void refresh() {
        LocalDate today = LocalDate.now();
        ZoneId zone = ZoneId.systemDefault();
        Instant startOfToday = today.atStartOfDay(zone).toInstant();
        Instant now = Instant.now();

        SalesReport todayReport = context.reportService().salesReport(startOfToday, now);

        LocalTime nowTime = LocalTime.now();
        LocalDate lastWeekDate = today.minusWeeks(1);
        Instant lastWeekStart = lastWeekDate.atStartOfDay(zone).toInstant();
        Instant lastWeekEnd = lastWeekDate.atTime(nowTime).atZone(zone).toInstant();
        SalesReport baselineReport = context.reportService().salesReport(lastWeekStart, lastWeekEnd);

        String dayName = lastWeekDate.getDayOfWeek().name();
        String baselineLabel = "vs last " + dayName.substring(0, 1).toUpperCase() + dayName.substring(1, 3).toLowerCase();

        if (baselineReport.orderCount() == 0) {
            LocalDate yesterdayDate = today.minusDays(1);
            Instant yesterdayStart = yesterdayDate.atStartOfDay(zone).toInstant();
            Instant yesterdayEnd = yesterdayDate.atTime(nowTime).atZone(zone).toInstant();
            SalesReport yesterdayReport = context.reportService().salesReport(yesterdayStart, yesterdayEnd);
            if (yesterdayReport.orderCount() > 0) {
                baselineReport = yesterdayReport;
                baselineLabel = "vs yesterday";
            }
        }

        salesCard.setValue(MoneyFormatter.format(todayReport.grossRevenue()));
        revenueCard.setValue(MoneyFormatter.format(todayReport.netRevenue()));
        transactionsCard.setValue(String.valueOf(todayReport.orderCount()));
        itemsSoldCard.setValue(String.valueOf(todayReport.itemsSold()));

        Money aovToday = todayReport.orderCount() > 0 ?
                Money.ofMinorUnits(todayReport.grossRevenue().toMinorUnits() / todayReport.orderCount()) : Money.ZERO;
        aovCard.setValue(MoneyFormatter.format(aovToday));

        if (baselineReport != null && baselineReport.orderCount() > 0) {
            double salesPct = calcPctChange(todayReport.grossRevenue().toMinorUnits(), baselineReport.grossRevenue().toMinorUnits());
            salesCard.setChangeText(formatPctBadge(salesPct) + " " + baselineLabel, salesPct >= 0);

            double netPct = calcPctChange(todayReport.netRevenue().toMinorUnits(), baselineReport.netRevenue().toMinorUnits());
            revenueCard.setChangeText(formatPctBadge(netPct) + " " + baselineLabel, netPct >= 0);

            int countDiff = todayReport.orderCount() - baselineReport.orderCount();
            String countText = (countDiff >= 0 ? "+" + countDiff : String.valueOf(countDiff)) + " " + baselineLabel;
            transactionsCard.setChangeText(countText, countDiff >= 0);

            int itemsDiff = todayReport.itemsSold() - baselineReport.itemsSold();
            String itemsText = (itemsDiff >= 0 ? "+" + itemsDiff : String.valueOf(itemsDiff)) + " " + baselineLabel;
            itemsSoldCard.setChangeText(itemsText, itemsDiff >= 0);

            Money aovBaseline = baselineReport.orderCount() > 0 ?
                    Money.ofMinorUnits(baselineReport.grossRevenue().toMinorUnits() / baselineReport.orderCount()) : Money.ZERO;
            double aovPct = calcPctChange(aovToday.toMinorUnits(), aovBaseline.toMinorUnits());
            aovCard.setChangeText(formatPctBadge(aovPct) + " " + baselineLabel, aovPct >= 0);
        } else {

            salesCard.setChangeText("", AppTheme.TEXT_SECONDARY);
            revenueCard.setChangeText("", AppTheme.TEXT_SECONDARY);
            transactionsCard.setChangeText("", AppTheme.TEXT_SECONDARY);
            itemsSoldCard.setChangeText("", AppTheme.TEXT_SECONDARY);
            aovCard.setChangeText("", AppTheme.TEXT_SECONDARY);
        }

        List<TopSellingItem> topItems = todayReport.topSellingItems();
        if (topItems.isEmpty()) {
            Instant thirtyDaysAgo = today.minusDays(30).atStartOfDay(zone).toInstant();
            topItems = context.reportService().salesReport(thirtyDaysAgo, now).topSellingItems();
        }
        if (!topItems.isEmpty()) {
            TopSellingItem top1 = topItems.get(0);
            topItemCard.setValue(top1.itemName());
            topItemCard.setChangeText(top1.quantitySold() + " units sold (" + MoneyFormatter.format(top1.revenue()) + ")", AppTheme.TEXT_SECONDARY);
        } else {
            topItemCard.setValue("N/A");
            topItemCard.setChangeText("No sales recorded", AppTheme.TEXT_SECONDARY);
        }

        topSellingModel.setItems(topItems);

        Instant thirtyDaysAgo = today.minusDays(30).atStartOfDay(zone).toInstant();
        List<Order> recentOrders = context.orderService().findOrderHistory(thirtyDaysAgo, now);
        int orderLimit = Math.min(15, recentOrders.size());
        recentOrdersModel.setOrders(recentOrders.subList(0, orderLimit));

        summaryVatVal.setText(MoneyFormatter.format(todayReport.vatCollected()));

        List<Order> todayOrders = context.orderService().findOrderHistory(startOfToday, now);
        Money todayDiscounts = Money.ZERO;
        long dineInCount = 0;
        long takeOutCount = 0;

        for (Order o : todayOrders) {
            if (o.status() == OrderStatus.PAID) {
                todayDiscounts = todayDiscounts.add(o.discountAmount());
                if (o.orderType() == OrderType.DINE_IN) dineInCount++;
                else if (o.orderType() == OrderType.TAKE_OUT) takeOutCount++;
            }
        }
        summaryDiscountVal.setText(MoneyFormatter.format(todayDiscounts));

        List<Order> voidedOrders = context.reportService().voidReport(startOfToday, now);
        summaryVoidVal.setText(String.valueOf(voidedOrders.size()));

        double avgItems = todayReport.orderCount() > 0 ? (double) todayReport.itemsSold() / todayReport.orderCount() : 0.0;
        summaryAvgItemsVal.setText(String.format("%.1f", avgItems));

        summaryChannelVal.setText(dineInCount + " Dine-In / " + takeOutCount + " Take-Out");

        loadChartData();

        lastUpdatedLabel.setText("Updated: " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }

    private void loadChartData() {
        LocalDate today = LocalDate.now();
        ZoneId sysZone = ZoneId.systemDefault();
        List<DailySalesChart.DayTotal> chartData = new java.util.ArrayList<>();

        if (activeChartMode == DailySalesChart.ChartMode.DAILY) {
            DateTimeFormatter dayLabel = DateTimeFormatter.ofPattern("MM/dd");
            for (int i = 6; i >= 0; i--) {
                LocalDate day = today.minusDays(i);
                Instant from = day.atStartOfDay(sysZone).toInstant();
                Instant to   = day.plusDays(1).atStartOfDay(sysZone).toInstant();
                SalesReport dayReport = context.reportService().salesReport(from, to);
                chartData.add(new DailySalesChart.DayTotal(day.format(dayLabel), dayReport.grossRevenue()));
            }
        } else if (activeChartMode == DailySalesChart.ChartMode.WEEKLY) {
            DateTimeFormatter weekLabelFormatter = DateTimeFormatter.ofPattern("MM/dd");
            for (int i = 7; i >= 0; i--) {
                LocalDate weekStart = today.minusWeeks(i).with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
                LocalDate weekEnd = weekStart.plusDays(7);
                Instant from = weekStart.atStartOfDay(sysZone).toInstant();
                Instant to   = weekEnd.atStartOfDay(sysZone).toInstant();
                SalesReport weekReport = context.reportService().salesReport(from, to);
                String label = weekStart.format(weekLabelFormatter);
                chartData.add(new DailySalesChart.DayTotal("W-" + label, weekReport.grossRevenue()));
            }
        } else if (activeChartMode == DailySalesChart.ChartMode.MONTHLY) {
            DateTimeFormatter monthLabelFormatter = DateTimeFormatter.ofPattern("MMM");
            for (int i = 5; i >= 0; i--) {
                LocalDate monthStart = today.minusMonths(i).withDayOfMonth(1);
                LocalDate monthEnd = monthStart.plusMonths(1);
                Instant from = monthStart.atStartOfDay(sysZone).toInstant();
                Instant to   = monthEnd.atStartOfDay(sysZone).toInstant();
                SalesReport monthReport = context.reportService().salesReport(from, to);
                chartData.add(new DailySalesChart.DayTotal(monthStart.format(monthLabelFormatter), monthReport.grossRevenue()));
            }
        }

        salesChart.setData(activeChartMode, chartData);
    }

    private static double calcPctChange(long current, long previous) {
        if (previous == 0) {
            return current > 0 ? 100.0 : 0.0;
        }
        return ((double) (current - previous) / previous) * 100.0;
    }

    private static String formatPctBadge(double pct) {
        if (pct > 0) {
            return String.format("+%.1f%%", pct);
        } else if (pct < 0) {
            return String.format("%.1f%%", pct);
        } else {
            return "0.0%";
        }
    }

    private static void setColumnAlignment(JTable table, int column, int alignment) {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(alignment);
        table.getColumnModel().getColumn(column).setCellRenderer(renderer);
    }

    private static final class TopSellingTableModel extends AbstractTableModel {
        private static final String[] COLUMNS = {"#", "Item", "Qty Sold", "Revenue"};
        private List<TopSellingItem> items = List.of();

        void setItems(List<TopSellingItem> items) { this.items = items; fireTableDataChanged(); }

        @Override public int getRowCount()    { return items.size(); }
        @Override public int getColumnCount() { return COLUMNS.length; }
        @Override public String getColumnName(int col) { return COLUMNS[col]; }

        @Override
        public Object getValueAt(int row, int col) {
            TopSellingItem item = items.get(row);
            return switch (col) {
                case 0 -> String.valueOf(row + 1);
                case 1 -> item.itemName();
                case 2 -> item.quantitySold();
                case 3 -> MoneyFormatter.format(item.revenue());
                default -> "";
            };
        }
    }

    private static final class RecentOrdersTableModel extends AbstractTableModel {
        private static final String[] COLUMNS = {"Order #", "Time", "Cashier", "Type", "Total", "Status"};
        private List<Order> orders = List.of();

        void setOrders(List<Order> orders) { this.orders = orders; fireTableDataChanged(); }

        @Override public int getRowCount()    { return orders.size(); }
        @Override public int getColumnCount() { return COLUMNS.length; }
        @Override public String getColumnName(int col) { return COLUMNS[col]; }

        @Override
        public Object getValueAt(int row, int col) {
            Order o = orders.get(row);
            return switch (col) {
                case 0 -> o.orderNumber();
                case 1 -> ORDER_TIME_FORMAT.format(o.placedAt());
                case 2 -> o.cashierName();
                case 3 -> o.orderType().displayName();
                case 4 -> MoneyFormatter.format(o.totalDue());
                case 5 -> o.status().name();
                default -> "";
            };
        }
    }

    private static final class StatusPillRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
            label.setOpaque(true);

            String text = value != null ? value.toString() : "";
            if ("PAID".equalsIgnoreCase(text)) {
                label.setBackground(isSelected ? new Color(187, 247, 208) : new Color(220, 252, 231));
                label.setForeground(new Color(22, 101, 52));
            } else if ("VOIDED".equalsIgnoreCase(text)) {
                label.setBackground(isSelected ? new Color(254, 202, 202) : new Color(254, 226, 226));
                label.setForeground(new Color(153, 27, 27));
            } else {
                label.setBackground(isSelected ? new Color(253, 230, 138) : new Color(254, 243, 199));
                label.setForeground(new Color(146, 64, 14));
            }
            return label;
        }
    }
}

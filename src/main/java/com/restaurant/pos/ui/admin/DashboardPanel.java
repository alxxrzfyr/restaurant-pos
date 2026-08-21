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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
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
            DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy").withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter ORDER_TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault());

    private final AppContext context;
    private final User currentUser;

    private final JLabel dateLabel = new JLabel();
    private final JLabel timeLabel = new JLabel();
    private final JLabel lastUpdatedLabel = new JLabel("Updated: Just now");
    private final Timer clockTimer;

    private final KpiCard salesCard        = new KpiCard("Today's Sales",   null, Icons.reports(AppTheme.ACCENT, 15));
    private final KpiCard revenueCard      = new KpiCard("Net Revenue",      null, Icons.trendingUp(AppTheme.SUCCESS, 15));
    private final KpiCard transactionsCard  = new KpiCard("Transactions",    null, Icons.orders(AppTheme.TEXT_SECONDARY, 15));
    private final KpiCard itemsSoldCard    = new KpiCard("Items Sold",       null, Icons.shoppingBag(AppTheme.TEXT_SECONDARY, 15));
    private final KpiCard aovCard          = new KpiCard("Avg Order Value", null, Icons.percent(AppTheme.TEXT_SECONDARY, 15));
    private final KpiCard topItemCard      = new KpiCard("Top Selling Item", null, Icons.star(AppTheme.TEXT_SECONDARY, 15));

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
        super(new MigLayout("insets 16 20 16 20, fill, hidemode 3", "[grow, fill]", "[][][grow 56, fill][grow 44, fill]"));
        this.context = context;
        this.currentUser = currentUser;

        setBackground(AppTheme.BACKGROUND);

        add(buildHeader(), "growx, wrap, gapbottom 12");
        add(buildKpiGrid(), "growx, wrap, gapbottom 14");
        add(buildRow2(), "grow, push, wrap, gapbottom 14");
        add(buildRow3(), "grow, push");

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

        JPanel titleBox = new JPanel(new MigLayout("insets 0, wrap 1, gapy 2"));
        titleBox.setOpaque(false);

        JLabel title = new JLabel("Executive Dashboard");
        title.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_DASHBOARD_TITLE));
        title.setForeground(AppTheme.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Real-time operational summary and performance metrics");
        subtitle.setFont(AppTheme.captionFont());
        subtitle.setForeground(AppTheme.TEXT_SECONDARY);

        titleBox.add(title);
        titleBox.add(subtitle);
        header.add(titleBox, "growx");

        JPanel rightBox = new JPanel(new MigLayout("insets 0, aligny center", "[]18[]18[]14[]"));
        rightBox.setOpaque(false);

        dateLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        dateLabel.setForeground(AppTheme.TEXT_PRIMARY);

        timeLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        timeLabel.setForeground(AppTheme.ACCENT);

        lastUpdatedLabel.setFont(AppTheme.captionFont());
        lastUpdatedLabel.setForeground(AppTheme.TEXT_MUTED);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        refreshBtn.setIcon(Icons.refresh(Color.WHITE, 14));
        refreshBtn.setIconTextGap(8);
        refreshBtn.setBackground(AppTheme.PRIMARY);
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        refreshBtn.setBorder(BorderFactory.createEmptyBorder(7, 16, 7, 16));
        refreshBtn.addActionListener(e -> refresh());

        rightBox.add(dateLabel);
        rightBox.add(timeLabel);
        rightBox.add(lastUpdatedLabel);
        rightBox.add(refreshBtn, "h 36!");

        header.add(rightBox, "align right");
        return header;
    }

    private JPanel buildKpiGrid() {
        JPanel kpiGrid = new JPanel(new MigLayout("insets 0, fillx", "[grow, fill]12[grow, fill]12[grow, fill]12[grow, fill]12[grow, fill]12[grow, fill]", "[]"));
        kpiGrid.setOpaque(false);

        kpiGrid.add(salesCard, "h 112!");
        kpiGrid.add(revenueCard, "h 112!");
        kpiGrid.add(transactionsCard, "h 112!");
        kpiGrid.add(itemsSoldCard, "h 112!");
        kpiGrid.add(aovCard, "h 112!");
        kpiGrid.add(topItemCard, "h 112!");

        return kpiGrid;
    }

    private JPanel buildRow2() {
        JPanel row = new JPanel(new MigLayout("insets 0, fill", "[grow 58, fill]14[grow 42, fill]", "[fill]"));
        row.setOpaque(false);

        row.add(buildChartSection(), "grow");
        row.add(buildRecentTransactionsSection(), "grow");

        return row;
    }

    private JPanel buildChartSection() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(AppTheme.CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));

        JPanel headerBar = new JPanel(new BorderLayout());
        headerBar.setOpaque(false);

        JPanel titleArea = new JPanel(new MigLayout("insets 0, wrap 1, gapy 1"));
        titleArea.setOpaque(false);
        JLabel title = new JLabel("Sales Performance");
        title.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_SECTION_HEADER));
        title.setForeground(AppTheme.TEXT_PRIMARY);
        JLabel sub = new JLabel("Revenue trends across selected interval");
        sub.setFont(AppTheme.captionFont());
        sub.setForeground(AppTheme.TEXT_MUTED);
        titleArea.add(title);
        titleArea.add(sub);
        headerBar.add(titleArea, BorderLayout.WEST);

        JPanel btnGroupPanel = new JPanel(new MigLayout("insets 0", "[]6[]6[]"));
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
            button.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        } else {
            button.setBackground(AppTheme.BORDER_SUBTLE);
            button.setForeground(AppTheme.TEXT_SECONDARY);
            button.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        }
    }

    private JPanel buildRecentTransactionsSection() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(AppTheme.CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Recent Transactions");
        title.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_SECTION_HEADER));
        title.setForeground(AppTheme.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Latest 15 Orders");
        subtitle.setFont(AppTheme.captionFont());
        subtitle.setForeground(AppTheme.TEXT_MUTED);

        header.add(title, BorderLayout.WEST);
        header.add(subtitle, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        JTable table = new JTable(recentOrdersModel);
        table.setRowHeight(36);
        table.getTableHeader().setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_TABLE_HEADER));
        table.setFont(AppTheme.bodyFont());
        table.setFillsViewportHeight(true);

        StripedTableCellRenderer.apply(table);
        for (int i = 0; i < 5; i++) {
            setColumnAlignment(table, i, SwingConstants.CENTER);
        }
        table.getColumnModel().getColumn(5).setCellRenderer(new RoundedPillStatusRenderer());

        table.getColumnModel().getColumn(0).setPreferredWidth(75);
        table.getColumnModel().getColumn(1).setPreferredWidth(55);
        table.getColumnModel().getColumn(2).setPreferredWidth(95);
        table.getColumnModel().getColumn(3).setPreferredWidth(75);
        table.getColumnModel().getColumn(4).setPreferredWidth(85);
        table.getColumnModel().getColumn(5).setPreferredWidth(85);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildRow3() {
        JPanel row = new JPanel(new MigLayout("insets 0, fill", "[grow 58, fill]14[grow 42, fill]", "[fill]"));
        row.setOpaque(false);

        row.add(buildTopSellingSection(), "grow");
        row.add(buildTodaysSummarySection(), "grow");

        return row;
    }

    private JPanel buildTopSellingSection() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(AppTheme.CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Top Selling Items");
        title.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_SECTION_HEADER));
        title.setForeground(AppTheme.TEXT_PRIMARY);

        JLabel sub = new JLabel("Ranked by Quantity");
        sub.setFont(AppTheme.captionFont());
        sub.setForeground(AppTheme.TEXT_MUTED);

        header.add(title, BorderLayout.WEST);
        header.add(sub, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        JTable table = new JTable(topSellingModel);
        table.setRowHeight(36);
        table.getTableHeader().setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_TABLE_HEADER));
        table.setFont(AppTheme.bodyFont());
        table.setFillsViewportHeight(true);

        StripedTableCellRenderer.apply(table);
        for (int i = 0; i < 4; i++) {
            setColumnAlignment(table, i, SwingConstants.CENTER);
        }

        table.getColumnModel().getColumn(0).setPreferredWidth(35);
        table.getColumnModel().getColumn(1).setPreferredWidth(190);
        table.getColumnModel().getColumn(2).setPreferredWidth(70);
        table.getColumnModel().getColumn(3).setPreferredWidth(95);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildTodaysSummarySection() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(AppTheme.CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Today's Summary");
        title.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_SECTION_HEADER));
        title.setForeground(AppTheme.TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);

        panel.add(header, BorderLayout.NORTH);

        JPanel listPanel = new JPanel(new MigLayout("insets 6 2 6 2, fillx, wrap 1", "[grow, fill]", "[]6[]6[]6[]6[]"));
        listPanel.setOpaque(false);

        listPanel.add(createSummaryItem("VAT Collected", summaryVatVal, Icons.percent(AppTheme.TEXT_SECONDARY, 14)));
        listPanel.add(createSummaryItem("Discounts Given", summaryDiscountVal, Icons.shoppingBag(AppTheme.TEXT_SECONDARY, 14)));
        listPanel.add(createSummaryItem("Voided Orders", summaryVoidVal, Icons.xCircle(AppTheme.DANGER, 14)));
        listPanel.add(createSummaryItem("Average Items / Order", summaryAvgItemsVal, Icons.utensils(AppTheme.TEXT_SECONDARY, 14)));
        listPanel.add(createSummaryItem("Order Channels", summaryChannelVal, Icons.store(AppTheme.TEXT_SECONDARY, 14)));

        panel.add(listPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createSummaryItem(String labelText, JLabel valueLabel, javax.swing.Icon icon) {
        JPanel row = new JPanel(new MigLayout("insets 6 10 6 10, fillx", "[]8[grow][right]")) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.decode("#F8FAFC"));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        row.setOpaque(false);

        if (icon != null) {
            row.add(new JLabel(icon));
        }

        JLabel label = new JLabel(labelText);
        label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        label.setForeground(AppTheme.TEXT_SECONDARY);
        row.add(label, "growx");

        valueLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        valueLabel.setForeground(AppTheme.TEXT_PRIMARY);
        row.add(valueLabel);

        return row;
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
            salesCard.setChangeText("Standard Day Target", AppTheme.TEXT_MUTED);
            revenueCard.setChangeText("After VAT and Discounts", AppTheme.TEXT_MUTED);
            transactionsCard.setChangeText("Completed Orders", AppTheme.TEXT_MUTED);
            itemsSoldCard.setChangeText("Total Units Sold", AppTheme.TEXT_MUTED);
            aovCard.setChangeText("Average per Order", AppTheme.TEXT_MUTED);
        }

        List<TopSellingItem> topItems = todayReport.topSellingItems();
        if (topItems.isEmpty()) {
            Instant thirtyDaysAgo = today.minusDays(30).atStartOfDay(zone).toInstant();
            topItems = context.reportService().salesReport(thirtyDaysAgo, now).topSellingItems();
        }
        if (!topItems.isEmpty()) {
            TopSellingItem top1 = topItems.get(0);
            topItemCard.setValue(top1.itemName());
            topItemCard.setChangeText(top1.quantitySold() + " units (" + MoneyFormatter.format(top1.revenue()) + ")", AppTheme.TEXT_SECONDARY);
        } else {
            topItemCard.setValue("N/A");
            topItemCard.setChangeText("No sales recorded", AppTheme.TEXT_MUTED);
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

    private static final class RoundedPillStatusRenderer extends DefaultTableCellRenderer {
        private String currentStatus = "";
        private boolean isRowSelected = false;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            this.currentStatus = value != null ? value.toString() : "";
            this.isRowSelected = isSelected;
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
            setOpaque(false);

            if ("PAID".equalsIgnoreCase(currentStatus)) {
                setForeground(AppTheme.SUCCESS);
            } else if ("VOIDED".equalsIgnoreCase(currentStatus)) {
                setForeground(AppTheme.DANGER);
            } else {
                setForeground(AppTheme.WARNING);
            }
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int pillW = 60;
            int pillH = 22;
            int x = (w - pillW) / 2;
            int y = (h - pillH) / 2;

            if ("PAID".equalsIgnoreCase(currentStatus)) {
                g2.setColor(isRowSelected ? Color.decode("#BBF7D0") : AppTheme.SUCCESS_BG);
                g2.fillRoundRect(x, y, pillW, pillH, 12, 12);
                g2.setColor(AppTheme.SUCCESS_BORDER);
                g2.drawRoundRect(x, y, pillW, pillH, 12, 12);
            } else if ("VOIDED".equalsIgnoreCase(currentStatus)) {
                g2.setColor(isRowSelected ? Color.decode("#FECACA") : AppTheme.DANGER_BG);
                g2.fillRoundRect(x, y, pillW, pillH, 12, 12);
                g2.setColor(AppTheme.DANGER_BORDER);
                g2.drawRoundRect(x, y, pillW, pillH, 12, 12);
            } else {
                g2.setColor(isRowSelected ? Color.decode("#FDE68A") : AppTheme.WARNING_BG);
                g2.fillRoundRect(x, y, pillW, pillH, 12, 12);
                g2.setColor(AppTheme.WARNING_BORDER);
                g2.drawRoundRect(x, y, pillW, pillH, 12, 12);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }
}

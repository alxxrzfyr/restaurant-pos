package com.restaurant.pos.ui.admin;

import com.restaurant.pos.model.Money;
import com.restaurant.pos.ui.format.MoneyFormatter;
import com.restaurant.pos.ui.theme.AppTheme;

import javax.swing.JComponent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

final class DailySalesChart extends JComponent {

    public enum ChartMode {
        DAILY("Daily Sales Performance"),
        WEEKLY("Weekly Sales Performance"),
        MONTHLY("Monthly Sales Performance");

        private final String title;
        ChartMode(String title) { this.title = title; }
        public String title() { return title; }
    }

    record DayTotal(String label, Money amount) {}

    private static final int LEFT_PAD   = 65;
    private static final int RIGHT_PAD  = 20;
    private static final int TOP_PAD    = 24;
    private static final int BOTTOM_PAD = 32;
    private static final int MAX_BAR_WIDTH = 38;
    private static final int GRID_LINES = 4;

    private static final Color BAR_TOP_COLOR     = Color.decode("#3B82F6");
    private static final Color BAR_BOTTOM_COLOR  = Color.decode("#1D4ED8");
    private static final Color BAR_BG_TRACK      = Color.decode("#F1F5F9");
    private static final Color GRID_COLOR        = Color.decode("#E2E8F0");
    private static final Color AXIS_COLOR        = Color.decode("#CBD5E1");

    private List<DayTotal> data = List.of();
    private ChartMode currentMode = ChartMode.DAILY;
    private final List<Rectangle> barBounds = new ArrayList<>();

    public DailySalesChart() {
    }

    void setData(List<DayTotal> data) {
        setData(ChartMode.DAILY, data);
    }

    void setData(ChartMode mode, List<DayTotal> data) {
        this.currentMode = mode != null ? mode : ChartMode.DAILY;
        this.data = data != null ? data : List.of();
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(500, 240);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(280, 160);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int width  = getWidth();
        int height = getHeight();
        barBounds.clear();

        g2.setColor(AppTheme.CARD);
        g2.fillRect(0, 0, width, height);

        int chartLeft   = LEFT_PAD;
        int chartRight  = width - RIGHT_PAD;
        int chartTop    = TOP_PAD;
        int chartBottom = height - BOTTOM_PAD;
        int chartH      = chartBottom - chartTop;
        int chartW      = chartRight - chartLeft;

        if (data.isEmpty() || chartH <= 10 || chartW <= 10) {
            drawEmptyState(g2, width, height);
            g2.dispose();
            return;
        }

        long maxMinor = data.stream().mapToLong(d -> d.amount().toMinorUnits()).max().orElse(100);
        if (maxMinor == 0) maxMinor = 100;
        maxMinor = calculateNiceMax(maxMinor);

        g2.setStroke(new BasicStroke(1f));
        for (int i = 0; i <= GRID_LINES; i++) {
            double fraction = (double) i / GRID_LINES;
            int y = (int) (chartBottom - chartH * fraction);

            if (i == 0) {
                g2.setColor(AXIS_COLOR);
                g2.setStroke(new BasicStroke(1f));
                g2.drawLine(chartLeft, y, chartRight, y);
            } else {
                g2.setColor(GRID_COLOR);
                float[] dash = {3f, 3f};
                g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, dash, 0f));
                g2.drawLine(chartLeft, y, chartRight, y);
                g2.setStroke(new BasicStroke(1f));
            }

            long val = (long) (maxMinor * fraction);
            String label = formatAxisValue(val);
            g2.setColor(AppTheme.TEXT_SECONDARY);
            g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(label, chartLeft - fm.stringWidth(label) - 8, y + fm.getAscent() / 2 - 2);
        }

        int barCount   = data.size();
        int slotWidth  = chartW / barCount;
        int barWidth   = Math.min(MAX_BAR_WIDTH, Math.max(16, slotWidth - 16));

        for (int i = 0; i < barCount; i++) {
            DayTotal day = data.get(i);
            int slotCenterX = chartLeft + (i * slotWidth) + (slotWidth / 2);
            int x = slotCenterX - (barWidth / 2);

            double ratio = (double) day.amount().toMinorUnits() / maxMinor;
            int barH = (int) (chartH * ratio);

            Rectangle boundRect = new Rectangle(x - 4, chartTop, barWidth + 8, chartH + 24);
            barBounds.add(boundRect);

            g2.setColor(BAR_BG_TRACK);
            g2.fillRoundRect(x, chartTop, barWidth, chartH, 6, 6);

            if (barH > 0) {
                int solidH = Math.max(4, barH);
                int barY = chartBottom - solidH;

                GradientPaint gp = new GradientPaint(
                        x, barY, BAR_TOP_COLOR,
                        x, chartBottom, BAR_BOTTOM_COLOR);
                g2.setPaint(gp);
                g2.fillRoundRect(x, barY, barWidth, solidH, 6, 6);

                if (!day.amount().isZero() && barY > chartTop + 16) {
                    String valStr = formatCompactValue(day.amount().toMinorUnits());
                    g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
                    FontMetrics fm = g2.getFontMetrics();
                    g2.setColor(AppTheme.TEXT_SECONDARY);
                    int labelX = x + (barWidth - fm.stringWidth(valStr)) / 2;
                    int labelY = barY - 4;
                    g2.drawString(valStr, labelX, labelY);
                }
            }

            g2.setColor(AppTheme.TEXT_SECONDARY);
            g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
            FontMetrics fm = g2.getFontMetrics();
            String dayLabel = day.label();
            int labelX = x + (barWidth - fm.stringWidth(dayLabel)) / 2;
            g2.drawString(dayLabel, labelX, chartBottom + 16);
        }

        g2.dispose();
    }

    private static void drawEmptyState(Graphics2D g2, int width, int height) {
        g2.setColor(AppTheme.TEXT_SECONDARY);
        g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        String msg = "No sales data recorded for this period";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(msg, (width - fm.stringWidth(msg)) / 2, height / 2);
    }

    private static long calculateNiceMax(long maxMinor) {
        if (maxMinor <= 100000) return Math.max(50000, ((maxMinor + 19999) / 20000) * 20000);
        if (maxMinor <= 1000000) return ((maxMinor + 199999) / 200000) * 200000;
        return ((maxMinor + 999999) / 1000000) * 1000000;
    }

    private static String formatAxisValue(long minorUnits) {
        if (minorUnits == 0) return "₱0";
        long pesos = minorUnits / 100;
        if (pesos >= 1_000_000) return String.format("₱%.1fM", pesos / 1_000_000.0);
        if (pesos >= 1_000)     return "₱" + (pesos / 1_000) + "k";
        return "₱" + pesos;
    }

    private static String formatCompactValue(long minorUnits) {
        if (minorUnits == 0) return "₱0";
        long pesos = minorUnits / 100;
        if (pesos >= 1_000_000) return String.format("₱%.1fM", pesos / 1_000_000.0);
        if (pesos >= 1_000)     return String.format("₱%.1fk", pesos / 1_000.0);
        return "₱" + pesos;
    }
}

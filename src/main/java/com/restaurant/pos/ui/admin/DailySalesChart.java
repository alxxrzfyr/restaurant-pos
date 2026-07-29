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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
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

    private static final int LEFT_PAD   = 70;
    private static final int RIGHT_PAD  = 20;
    private static final int TOP_PAD    = 35;
    private static final int BOTTOM_PAD = 40;
    private static final int BAR_GAP    = 12;
    private static final int GRID_LINES = 4;

    private static final Color BAR_FILL        = AppTheme.PRIMARY;
    private static final Color BAR_FILL_HOVER  = new Color(29, 78, 216);
    private static final Color BAR_FILL_LIGHT  = new Color(219, 234, 254);
    private static final Color GRID_COLOR      = new Color(229, 231, 235, 180);
    private static final Color AXIS_COLOR      = new Color(203, 213, 225);
    private static final Color VALUE_COLOR     = Color.decode("#1E40AF");

    private List<DayTotal> data = List.of();
    private ChartMode currentMode = ChartMode.DAILY;
    private int hoveredIndex = -1;
    private final List<Rectangle> barBounds = new ArrayList<>();

    public DailySalesChart() {
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                Point p = e.getPoint();
                int oldHover = hoveredIndex;
                hoveredIndex = -1;
                for (int i = 0; i < barBounds.size(); i++) {
                    if (barBounds.get(i).contains(p)) {
                        hoveredIndex = i;
                        break;
                    }
                }
                if (oldHover != hoveredIndex) {
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (hoveredIndex != -1) {
                    hoveredIndex = -1;
                    repaint();
                }
            }
        };
        addMouseMotionListener(mouseAdapter);
        addMouseListener(mouseAdapter);
    }

    void setData(List<DayTotal> data) {
        setData(ChartMode.DAILY, data);
    }

    void setData(ChartMode mode, List<DayTotal> data) {
        this.currentMode = mode != null ? mode : ChartMode.DAILY;
        this.data = data != null ? data : List.of();
        this.hoveredIndex = -1;
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(600, 320);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(350, 220);
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

        if (data.isEmpty()) {
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
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawLine(chartLeft, y, chartRight, y);
                g2.setStroke(new BasicStroke(1f));
            } else {
                g2.setColor(GRID_COLOR);
                float[] dash = {4f, 4f};
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
        int gap        = data.size() > 10 ? Math.max(4, BAR_GAP / 2) : BAR_GAP;
        int totalGapW  = (barCount - 1) * gap;
        int barWidth   = Math.max(12, (chartW - totalGapW - 12) / barCount);

        for (int i = 0; i < barCount; i++) {
            DayTotal day = data.get(i);
            int x       = chartLeft + 6 + i * (barWidth + gap);
            double ratio = (double) day.amount().toMinorUnits() / maxMinor;
            int barH    = (int) (chartH * ratio);
            boolean isHovered = (i == hoveredIndex);

            Rectangle boundRect = new Rectangle(x, chartTop, barWidth, chartH + 20);
            barBounds.add(boundRect);

            if (barH > 0) {

                g2.setColor(BAR_FILL_LIGHT);
                g2.fillRect(x, chartBottom - barH, barWidth, barH);

                g2.setColor(isHovered ? BAR_FILL_HOVER : BAR_FILL);
                int solidH = Math.max(3, barH);
                g2.fillRoundRect(x, chartBottom - solidH, barWidth, solidH, 4, 4);
                if (solidH > 4) {
                    g2.fillRect(x, chartBottom - 4, barWidth, 4);
                }

                if (!day.amount().isZero() && barWidth >= 24) {
                    g2.setColor(isHovered ? AppTheme.PRIMARY : VALUE_COLOR);
                    g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
                    String valStr = MoneyFormatter.format(day.amount());
                    FontMetrics fm = g2.getFontMetrics();
                    int labelX = x + (barWidth - fm.stringWidth(valStr)) / 2;
                    int labelY = chartBottom - barH - 6;
                    if (labelY < chartTop + 12) labelY = chartTop + 12;
                    g2.drawString(valStr, labelX, labelY);
                }
            }

            g2.setColor(isHovered ? AppTheme.TEXT_PRIMARY : AppTheme.TEXT_SECONDARY);
            g2.setFont(new Font(Font.SANS_SERIF, isHovered ? Font.BOLD : Font.PLAIN, 10));
            FontMetrics fm = g2.getFontMetrics();
            String dayLabel = day.label();
            int labelX = x + (barWidth - fm.stringWidth(dayLabel)) / 2;
            g2.drawString(dayLabel, labelX, chartBottom + 18);
        }

        paintLegend(g2, width);

        if (hoveredIndex >= 0 && hoveredIndex < data.size()) {
            drawTooltip(g2, hoveredIndex, barWidth, chartBottom);
        }

        g2.dispose();
    }

    private void paintLegend(Graphics2D g2, int width) {
        int boxSize = 10;
        String legendText = currentMode.title() + " (₱)";
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        FontMetrics fm = g2.getFontMetrics();
        int totalW = boxSize + 6 + fm.stringWidth(legendText);
        int startX = (width - totalW) / 2;

        g2.setColor(BAR_FILL);
        g2.fillRoundRect(startX, TOP_PAD - 18, boxSize, boxSize, 3, 3);
        g2.setColor(AppTheme.TEXT_PRIMARY);
        g2.drawString(legendText, startX + boxSize + 6, TOP_PAD - 9);
    }

    private void drawTooltip(Graphics2D g2, int index, int barWidth, int chartBottom) {
        DayTotal day = data.get(index);
        Rectangle b = barBounds.get(index);

        String line1 = day.label();
        String line2 = "Revenue: " + MoneyFormatter.format(day.amount());

        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        FontMetrics fm = g2.getFontMetrics();
        int tooltipW = Math.max(fm.stringWidth(line1), fm.stringWidth(line2)) + 20;
        int tooltipH = 44;

        int tooltipX = b.x + (barWidth - tooltipW) / 2;
        if (tooltipX < LEFT_PAD) tooltipX = LEFT_PAD;
        if (tooltipX + tooltipW > getWidth() - RIGHT_PAD) tooltipX = getWidth() - RIGHT_PAD - tooltipW;
        int tooltipY = TOP_PAD + 10;

        g2.setColor(new Color(0, 0, 0, 30));
        g2.fill(new RoundRectangle2D.Float(tooltipX + 2, tooltipY + 2, tooltipW, tooltipH, 8, 8));

        g2.setColor(new Color(17, 24, 39, 230));
        g2.fill(new RoundRectangle2D.Float(tooltipX, tooltipY, tooltipW, tooltipH, 8, 8));

        g2.setColor(Color.WHITE);
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        g2.drawString(line1, tooltipX + 10, tooltipY + 16);

        g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        g2.setColor(new Color(147, 197, 253));
        g2.drawString(line2, tooltipX + 10, tooltipY + 32);
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
}

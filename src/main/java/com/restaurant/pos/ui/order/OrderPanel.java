package com.restaurant.pos.ui.order;

import com.restaurant.pos.AppContext;
import com.restaurant.pos.model.Cart;
import com.restaurant.pos.model.CartLine;
import com.restaurant.pos.model.Category;
import com.restaurant.pos.model.CheckoutResult;
import com.restaurant.pos.model.MenuItem;
import com.restaurant.pos.model.Money;
import com.restaurant.pos.model.OrderType;
import com.restaurant.pos.service.OrderTotals;
import com.restaurant.pos.ui.format.MoneyFormatter;
import com.restaurant.pos.ui.theme.AppTheme;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class OrderPanel extends JPanel {

    private static final int TILE_WIDTH = 180;
    private static final int TILE_HEIGHT = 175;
    private static final int GRID_COLUMNS = 3;
    private static final int CART_WIDTH = 340;
    private static final int ROW_HEIGHT = 36;
    private static final int BUTTON_HEIGHT = 48;

    private final AppContext context;
    private final long cashierId;
    private final String cashierName;
    private final Cart cart = new Cart();

    private final CartTableModel cartTableModel = new CartTableModel();
    private final JLabel subtotalLabel = new JLabel();
    private final JLabel vatLabel = new JLabel();
    private final JLabel totalLabel = new JLabel();
    private final JRadioButton dineInRadio = new JRadioButton(OrderType.DINE_IN.displayName(), true);
    private final JRadioButton takeOutRadio = new JRadioButton(OrderType.TAKE_OUT.displayName());
    private final JTextField tableNumberField = new JTextField(8);
    private final JTextField notesField = new JTextField(20);

    public OrderPanel(AppContext context, long cashierId, String cashierName) {
        super(new BorderLayout(12, 0));
        this.context = context;
        this.cashierId = cashierId;
        this.cashierName = cashierName;
        setBackground(AppTheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        cart.setOrderType(OrderType.DINE_IN);

        add(buildCartPanel(), BorderLayout.WEST);
        add(buildMenuPanel(), BorderLayout.CENTER);
        refreshTotals();
    }

    private JPanel buildMenuPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(AppTheme.BACKGROUND);

        List<Category> categories = context.categoryService().findAllOrdered();
        List<MenuItem> allItems = context.menuService().findAllAvailable();
        Map<Long, List<MenuItem>> itemsByCategory = allItems.stream()
                .collect(Collectors.groupingBy(MenuItem::categoryId));

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_TABLE_HEADER));
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        for (Category category : categories) {
            List<MenuItem> items = itemsByCategory.getOrDefault(category.id(), List.of());
            tabs.addTab(category.name(), buildMenuGrid(items));
        }

        panel.add(tabs, BorderLayout.CENTER);
        return panel;
    }

    private JScrollPane buildMenuGrid(List<MenuItem> items) {
        int rows = Math.max(1, (items.size() + GRID_COLUMNS - 1) / GRID_COLUMNS);
        JPanel grid = new JPanel(new GridLayout(rows, GRID_COLUMNS, 12, 12));
        grid.setBackground(AppTheme.BACKGROUND);
        grid.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        for (MenuItem item : items) {
            grid.add(createMenuTile(item));
        }

        int emptyCells = (rows * GRID_COLUMNS) - items.size();
        for (int i = 0; i < emptyCells; i++) {
            JPanel spacer = new JPanel();
            spacer.setOpaque(false);
            grid.add(spacer);
        }

        JScrollPane scrollPane = new JScrollPane(grid);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        return scrollPane;
    }

    private static final Map<String, java.awt.image.BufferedImage> IMAGE_CACHE = new java.util.concurrent.ConcurrentHashMap<>();

    private static java.awt.image.BufferedImage loadImage(String imagePath) {
        if (imagePath == null || imagePath.isBlank()) return null;
        return IMAGE_CACHE.computeIfAbsent(imagePath, path -> {
            java.io.File file = new java.io.File(path);
            if (!file.exists()) return null;
            try {
                java.awt.image.BufferedImage raw = javax.imageio.ImageIO.read(file);
                return makeWhiteBackgroundTransparent(raw);
            } catch (Exception e) {
                return null;
            }
        });
    }

    private static java.awt.image.BufferedImage makeWhiteBackgroundTransparent(java.awt.image.BufferedImage src) {
        if (src == null) return null;
        int w = src.getWidth();
        int h = src.getHeight();
        java.awt.image.BufferedImage dst = new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_ARGB);

        int[] pixels = src.getRGB(0, 0, w, h, null, 0, w);
        boolean[] visited = new boolean[pixels.length];
        java.util.ArrayDeque<Integer> queue = new java.util.ArrayDeque<>();

        java.util.function.IntPredicate isNearWhite = index -> {
            int c = pixels[index];
            int a = (c >> 24) & 0xFF;
            if (a < 10) return true;
            int r = (c >> 16) & 0xFF;
            int g = (c >> 8) & 0xFF;
            int b = c & 0xFF;
            return r >= 235 && g >= 235 && b >= 235;
        };

        for (int x = 0; x < w; x++) {
            int idxTop = x;
            int idxBottom = (h - 1) * w + x;
            if (isNearWhite.test(idxTop)) { visited[idxTop] = true; queue.add(idxTop); }
            if (isNearWhite.test(idxBottom)) { visited[idxBottom] = true; queue.add(idxBottom); }
        }
        for (int y = 0; y < h; y++) {
            int idxLeft = y * w;
            int idxRight = y * w + (w - 1);
            if (!visited[idxLeft] && isNearWhite.test(idxLeft)) { visited[idxLeft] = true; queue.add(idxLeft); }
            if (!visited[idxRight] && isNearWhite.test(idxRight)) { visited[idxRight] = true; queue.add(idxRight); }
        }

        while (!queue.isEmpty()) {
            int curr = queue.poll();
            pixels[curr] = 0x00FFFFFF;

            int cx = curr % w;
            int cy = curr / w;

            if (cx > 0) {
                int n = curr - 1;
                if (!visited[n] && isNearWhite.test(n)) { visited[n] = true; queue.add(n); }
            }
            if (cx < w - 1) {
                int n = curr + 1;
                if (!visited[n] && isNearWhite.test(n)) { visited[n] = true; queue.add(n); }
            }
            if (cy > 0) {
                int n = curr - w;
                if (!visited[n] && isNearWhite.test(n)) { visited[n] = true; queue.add(n); }
            }
            if (cy < h - 1) {
                int n = curr + w;
                if (!visited[n] && isNearWhite.test(n)) { visited[n] = true; queue.add(n); }
            }
        }

        dst.setRGB(0, 0, w, h, pixels, 0, w);
        return dst;
    }

    private JPanel createMenuTile(MenuItem item) {

        JPanel tile = new JPanel(new MigLayout("insets 6, fill, wrap 1", "[grow, fill]", "[grow 70, fill][grow 30, center]"));
        tile.setBackground(AppTheme.CARD);
        tile.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER, 1));
        tile.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        tile.setPreferredSize(new Dimension(TILE_WIDTH, TILE_HEIGHT));
        tile.setMinimumSize(new Dimension(130, 150));

        java.awt.image.BufferedImage img = loadImage(item.imagePath());
        ScaledImagePanel imgContainer = new ScaledImagePanel(img);
        tile.add(imgContainer, "grow");

        JPanel infoPanel = new JPanel(new MigLayout("insets 2 4 4 4, wrap 1, fillx", "[center]", "[]2[]"));
        infoPanel.setOpaque(false);

        JLabel nameLabel = new JLabel(item.name());
        nameLabel.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_TABLE_HEADER));
        nameLabel.setForeground(AppTheme.TEXT_PRIMARY);
        nameLabel.setHorizontalAlignment(JLabel.CENTER);

        JLabel priceLabel = new JLabel(MoneyFormatter.format(item.price()));
        priceLabel.setFont(AppTheme.bodyFont());
        priceLabel.setForeground(AppTheme.TEXT_SECONDARY);
        priceLabel.setHorizontalAlignment(JLabel.CENTER);

        infoPanel.add(nameLabel, "growx");
        infoPanel.add(priceLabel, "growx");

        tile.add(infoPanel, "growx, align center");

        tile.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                cart.addItem(item);
                refreshCart();
            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                tile.setBackground(AppTheme.BACKGROUND);
                tile.setBorder(BorderFactory.createLineBorder(AppTheme.PRIMARY, 2));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                tile.setBackground(AppTheme.CARD);
                tile.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER, 1));
            }
        });

        return tile;
    }

    private static final class ScaledImagePanel extends JPanel {
        private final java.awt.image.BufferedImage image;

        ScaledImagePanel(java.awt.image.BufferedImage image) {
            this.image = image;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(java.awt.Graphics g) {
            super.paintComponent(g);
            int panelW = getWidth();
            int panelH = getHeight();
            if (panelW <= 0 || panelH <= 0) return;

            if (image == null) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new java.awt.Color(241, 245, 249));
                g2.fillRoundRect(2, 2, panelW - 4, panelH - 4, 6, 6);
                g2.setColor(AppTheme.TEXT_SECONDARY);
                g2.setFont(new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.PLAIN, 11));
                java.awt.FontMetrics fm = g2.getFontMetrics();
                String text = "No Image";
                g2.drawString(text, (panelW - fm.stringWidth(text)) / 2, (panelH + fm.getAscent()) / 2 - 2);
                g2.dispose();
                return;
            }

            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

            int imgW = image.getWidth();
            int imgH = image.getHeight();

            int padding = 4;
            int availW = Math.max(1, panelW - (padding * 2));
            int availH = Math.max(1, panelH - (padding * 2));

            double scale = Math.min((double) availW / imgW, (double) availH / imgH);
            int drawW = Math.max(1, (int) Math.round(imgW * scale));
            int drawH = Math.max(1, (int) Math.round(imgH * scale));

            int drawX = (panelW - drawW) / 2;
            int drawY = (panelH - drawH) / 2;

            g2.drawImage(image, drawX, drawY, drawW, drawH, null);
            g2.dispose();
        }
    }

    private JPanel buildCartPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 16, wrap 1, fill", "[" + CART_WIDTH + "!]"));
        panel.setBackground(AppTheme.CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)));

        JLabel title = new JLabel("Current Order");
        title.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_PAGE_TITLE));
        panel.add(title, "gapbottom 12");

        JPanel orderTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        orderTypePanel.setOpaque(false);
        ButtonGroup typeGroup = new ButtonGroup();
        typeGroup.add(dineInRadio);
        typeGroup.add(takeOutRadio);
        dineInRadio.setOpaque(false);
        takeOutRadio.setOpaque(false);
        dineInRadio.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_TABLE_HEADER));
        takeOutRadio.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_TABLE_HEADER));
        dineInRadio.addActionListener(e -> cart.setOrderType(OrderType.DINE_IN));
        takeOutRadio.addActionListener(e -> cart.setOrderType(OrderType.TAKE_OUT));
        orderTypePanel.add(dineInRadio);
        orderTypePanel.add(takeOutRadio);
        panel.add(orderTypePanel, "gapbottom 8");

        JPanel fieldsPanel = new JPanel(new MigLayout("insets 0, wrap 2", "[][grow, fill]"));
        fieldsPanel.setOpaque(false);
        JLabel tableLabel = new JLabel("Table #:");
        tableLabel.setFont(AppTheme.bodyFont());
        JLabel notesLabel = new JLabel("Notes:");
        notesLabel.setFont(AppTheme.bodyFont());
        tableNumberField.setFont(AppTheme.bodyFont());
        notesField.setFont(AppTheme.bodyFont());
        fieldsPanel.add(tableLabel);
        fieldsPanel.add(tableNumberField, "h 32!");
        fieldsPanel.add(notesLabel);
        fieldsPanel.add(notesField, "h 32!");
        panel.add(fieldsPanel, "growx, gapbottom 12");

        JTable cartTable = new JTable(cartTableModel);
        cartTable.setRowHeight(ROW_HEIGHT);
        cartTable.getTableHeader().setFont(AppTheme.bodyFont());
        cartTable.setFont(AppTheme.bodyFont());
        cartTable.getColumnModel().getColumn(0).setPreferredWidth(140);
        cartTable.getColumnModel().getColumn(1).setPreferredWidth(50);
        cartTable.getColumnModel().getColumn(2).setPreferredWidth(100);

        JScrollPane cartScroll = new JScrollPane(cartTable);
        panel.add(cartScroll, "grow, push, gapbottom 8");

        JPanel qtyPanel = new JPanel(new MigLayout("insets 0", "[grow]8[grow]8[grow]"));
        qtyPanel.setOpaque(false);
        JButton removeBtn = createTouchButton("-1", AppTheme.WARNING);
        JButton addBtn = createTouchButton("+1", AppTheme.SUCCESS);
        JButton deleteBtn = createTouchButton("Remove", AppTheme.DANGER);
        removeBtn.addActionListener(e -> adjustSelectedLine(cartTable, -1));
        addBtn.addActionListener(e -> adjustSelectedLine(cartTable, 1));
        deleteBtn.addActionListener(e -> removeSelectedLine(cartTable));
        qtyPanel.add(removeBtn, "growx, h " + BUTTON_HEIGHT + "!");
        qtyPanel.add(addBtn, "growx, h " + BUTTON_HEIGHT + "!");
        qtyPanel.add(deleteBtn, "growx, h " + BUTTON_HEIGHT + "!");
        panel.add(qtyPanel, "growx, gapbottom 12");

        JPanel totalsPanel = new JPanel(new MigLayout("insets 0, wrap 2", "[grow][right]"));
        totalsPanel.setOpaque(false);
        totalsPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, AppTheme.BORDER));
        addTotalRow(totalsPanel, "Subtotal:", subtotalLabel, false);
        addTotalRow(totalsPanel, "VAT:", vatLabel, false);
        addTotalRow(totalsPanel, "Total Due:", totalLabel, true);
        panel.add(totalsPanel, "growx, gapbottom 16");

        JButton placeOrderButton = createTouchButton("Place Order", AppTheme.PRIMARY);
        placeOrderButton.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_SECTION_HEADER));
        placeOrderButton.addActionListener(e -> openPaymentDialog());
        panel.add(placeOrderButton, "growx, h 56!");

        JButton clearBtn = createTouchButton("Clear Order", AppTheme.BORDER);
        clearBtn.setForeground(AppTheme.TEXT_PRIMARY);
        clearBtn.addActionListener(e -> {
            cart.clear();
            cart.setOrderType(OrderType.DINE_IN);
            dineInRadio.setSelected(true);
            tableNumberField.setText("");
            notesField.setText("");
            refreshCart();
        });
        panel.add(clearBtn, "growx, h " + BUTTON_HEIGHT + "!, gaptop 8");

        return panel;
    }

    private void addTotalRow(JPanel panel, String label, JLabel valueLabel, boolean bold) {
        JLabel caption = new JLabel(label);
        Font font = bold
                ? AppTheme.titleFont(AppTheme.FONT_SIZE_SECTION_HEADER)
                : AppTheme.titleFont(AppTheme.FONT_SIZE_TABLE_HEADER);
        caption.setFont(font);
        valueLabel.setFont(font);
        panel.add(caption, "gaptop 6");
        panel.add(valueLabel, "gaptop 6");
    }

    private JButton createTouchButton(String text, Color background) {
        JButton button = new JButton(text);
        button.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_TABLE_HEADER));
        button.setBackground(background);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void adjustSelectedLine(JTable table, int delta) {
        int row = table.getSelectedRow();
        if (row < 0) return;
        CartLine line = cart.lines().get(row);
        if (delta > 0) {
            cart.addItem(line.menuItem());
        } else {
            cart.removeOneUnit(line.menuItem());
        }
        refreshCart();
    }

    private void removeSelectedLine(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) return;
        CartLine line = cart.lines().get(row);
        cart.removeLine(line.menuItem());
        refreshCart();
    }

    private void refreshCart() {
        cartTableModel.fireTableDataChanged();
        refreshTotals();
    }

    private void refreshTotals() {
        if (cart.isEmpty()) {
            subtotalLabel.setText(MoneyFormatter.format(Money.ZERO));
            vatLabel.setText(MoneyFormatter.format(Money.ZERO));
            totalLabel.setText(MoneyFormatter.format(Money.ZERO));
            return;
        }
        OrderTotals totals = context.orderService().calculateTotals(cart);
        subtotalLabel.setText(MoneyFormatter.format(totals.subtotal()));
        vatLabel.setText(MoneyFormatter.format(totals.vat()));
        totalLabel.setText(MoneyFormatter.format(totals.totalDue()));
    }

    private void openPaymentDialog() {
        if (cart.isEmpty()) return;

        cart.setTableNumber(tableNumberField.getText().trim().isEmpty() ? null : tableNumberField.getText().trim());
        cart.setNotes(notesField.getText().trim().isEmpty() ? null : notesField.getText().trim());

        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        OrderTotals totals = context.orderService().calculateTotals(cart);

        PaymentDialog dialog = new PaymentDialog(owner, context, cart, totals, cashierId, cashierName);
        dialog.setVisible(true);

        CheckoutResult result = dialog.getResult();
        if (result != null) {
            ReceiptDialog receiptDialog = new ReceiptDialog(owner, context, result);
            receiptDialog.setVisible(true);

            cart.clear();
            cart.setOrderType(OrderType.DINE_IN);
            dineInRadio.setSelected(true);
            tableNumberField.setText("");
            notesField.setText("");
            refreshCart();
        }
    }

    private final class CartTableModel extends AbstractTableModel {
        private static final String[] COLUMNS = {"Item", "Qty", "Total"};

        @Override
        public int getRowCount() {
            return cart.lines().size();
        }

        @Override
        public int getColumnCount() {
            return COLUMNS.length;
        }

        @Override
        public String getColumnName(int column) {
            return COLUMNS[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            CartLine line = cart.lines().get(rowIndex);
            return switch (columnIndex) {
                case 0 -> line.menuItem().name();
                case 1 -> line.quantity();
                case 2 -> MoneyFormatter.format(line.lineTotal());
                default -> "";
            };
        }
    }
}

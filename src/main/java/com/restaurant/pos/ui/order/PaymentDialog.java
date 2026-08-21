package com.restaurant.pos.ui.order;

import com.restaurant.pos.AppContext;
import com.restaurant.pos.exception.InsufficientPaymentException;
import com.restaurant.pos.model.Cart;
import com.restaurant.pos.model.CheckoutResult;
import com.restaurant.pos.model.Money;
import com.restaurant.pos.model.PaymentMethod;
import com.restaurant.pos.service.OrderTotals;
import com.restaurant.pos.ui.components.PrimaryButton;
import com.restaurant.pos.ui.components.SecondaryButton;
import com.restaurant.pos.ui.format.MoneyFormatter;
import com.restaurant.pos.ui.theme.AppTheme;
import com.restaurant.pos.ui.theme.Icons;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

final class PaymentDialog extends JDialog {

    private static final int BUTTON_HEIGHT = 48;
    private static final int INPUT_HEIGHT = 48;

    private final AppContext context;
    private final Cart cart;
    private final OrderTotals totals;
    private final long cashierId;
    private final String cashierName;

    private PaymentMethod selectedMethod = PaymentMethod.CASH;
    private final List<MethodCardButton> methodButtons = new ArrayList<>();

    private final JTextField tenderedField = new JTextField(14);
    private final JLabel changeLabel = new JLabel("Enter amount or choose quick preset");
    private final JLabel errorLabel = new JLabel(" ");
    private final JPanel changeCard = new JPanel(new BorderLayout(8, 0));

    private final StringBuilder digits = new StringBuilder();
    private CheckoutResult result;

    PaymentDialog(Frame owner, AppContext context, Cart cart, OrderTotals totals,
                  long cashierId, String cashierName) {
        super(owner, "Process Payment", true);
        this.context = context;
        this.cart = cart;
        this.totals = totals;
        this.cashierId = cashierId;
        this.cashierName = cashierName;

        setContentPane(buildContent());
        pack();
        setLocationRelativeTo(owner);
        setResizable(false);

        setupShortcuts();
        updateDisplayFromDigits();
    }

    CheckoutResult getResult() {
        return result;
    }

    private void setupShortcuts() {
        tenderedField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    dispose();
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    attemptCheckout();
                } else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                    onBackspace();
                } else if (e.getKeyChar() >= '0' && e.getKeyChar() <= '9') {
                    appendDigit(e.getKeyChar());
                }
            }
        });
    }

    private JPanel buildContent() {
        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(AppTheme.CARD);
        root.setBorder(BorderFactory.createEmptyBorder(22, 24, 22, 24));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel title = new JLabel("Payment Checkout");
        title.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_PAGE_TITLE));
        title.setForeground(AppTheme.TEXT_PRIMARY);

        JLabel cashierInfo = new JLabel("Cashier: " + cashierName);
        cashierInfo.setFont(AppTheme.captionFont());
        cashierInfo.setForeground(AppTheme.TEXT_MUTED);

        headerPanel.add(title, BorderLayout.WEST);
        headerPanel.add(cashierInfo, BorderLayout.EAST);
        root.add(headerPanel, BorderLayout.NORTH);

        JPanel splitPane = new JPanel(new MigLayout("insets 0, fill", "[250!]18[390!]", "[grow, fill]"));
        splitPane.setOpaque(false);

        splitPane.add(buildLeftSummaryPane(), "grow");
        splitPane.add(buildRightTypingPane(), "grow");
        root.add(splitPane, BorderLayout.CENTER);

        JPanel footerBar = new JPanel(new MigLayout("insets 14 0 0 0, fillx", "[250!]18[390!]"));
        footerBar.setOpaque(false);

        SecondaryButton cancelBtn = new SecondaryButton("Cancel (Esc)");
        cancelBtn.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_BODY));
        cancelBtn.addActionListener(e -> dispose());

        PrimaryButton confirmBtn = new PrimaryButton("Complete Payment (Enter)", Icons.check(Color.WHITE, 18));
        confirmBtn.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_BODY));
        confirmBtn.addActionListener(e -> attemptCheckout());

        footerBar.add(cancelBtn, "growx, h " + BUTTON_HEIGHT + "!");
        footerBar.add(confirmBtn, "growx, h " + BUTTON_HEIGHT + "!");
        root.add(footerBar, BorderLayout.SOUTH);

        return root;
    }

    private JPanel buildLeftSummaryPane() {
        JPanel panel = new JPanel(new MigLayout("insets 0, fillx, wrap 1"));
        panel.setOpaque(false);

        JPanel orderCard = new JPanel(new MigLayout("insets 14 14 14 14, fillx, wrap 2", "[grow][right]"));
        orderCard.setBackground(AppTheme.BACKGROUND);
        orderCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));

        JLabel summaryTitle = new JLabel("ORDER SUMMARY");
        summaryTitle.setFont(AppTheme.titleFont(11));
        summaryTitle.setForeground(AppTheme.TEXT_MUTED);
        orderCard.add(summaryTitle, "span 2, gapbottom 8");

        int totalItems = cart.lines().stream().mapToInt(com.restaurant.pos.model.CartLine::quantity).sum();
        addSummaryRow(orderCard, "Items Count:", totalItems + " (" + cart.lineCount() + " lines)");
        addSummaryRow(orderCard, "Subtotal:", MoneyFormatter.format(totals.subtotal()));
        addSummaryRow(orderCard, "VAT (12%):", MoneyFormatter.format(totals.vat()));

        if (totals.discount().isPositive()) {
            addSummaryRow(orderCard, "Discount:", "- " + MoneyFormatter.format(totals.discount()));
        }

        JPanel totalBadge = new JPanel(new MigLayout("insets 10 12 10 12, fillx", "[grow][right]"));
        totalBadge.setBackground(AppTheme.PRIMARY);

        JLabel totalTextLabel = new JLabel("TOTAL DUE");
        totalTextLabel.setFont(AppTheme.titleFont(10));
        totalTextLabel.setForeground(Color.decode("#94A3B8"));

        JLabel totalValLabel = new JLabel(MoneyFormatter.format(totals.totalDue()));
        totalValLabel.setFont(AppTheme.titleFont(16));
        totalValLabel.setForeground(Color.WHITE);

        totalBadge.add(totalTextLabel, "align left");
        totalBadge.add(totalValLabel, "align right");

        orderCard.add(totalBadge, "span 2, growx, gaptop 10");
        panel.add(orderCard, "growx, gapbottom 14");

        JLabel methodLabel = new JLabel("Payment Method");
        methodLabel.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_TABLE_HEADER));
        methodLabel.setForeground(AppTheme.TEXT_PRIMARY);
        panel.add(methodLabel, "gapbottom 8");

        MethodCardButton cashBtn = new MethodCardButton("Cash", "Currency & Coins", Icons.banknote(AppTheme.TEXT_PRIMARY, 16), PaymentMethod.CASH);
        MethodCardButton debitBtn = new MethodCardButton("Debit Card", "Swipe / Chip / POS", Icons.creditCard(AppTheme.TEXT_PRIMARY, 16), PaymentMethod.DEBIT_CARD);
        MethodCardButton creditBtn = new MethodCardButton("Credit Card", "Visa, Mastercard", Icons.creditCard(AppTheme.TEXT_PRIMARY, 16), PaymentMethod.CREDIT_CARD);

        methodButtons.add(cashBtn);
        methodButtons.add(debitBtn);
        methodButtons.add(creditBtn);

        cashBtn.setSelectedMethod(true);

        panel.add(cashBtn, "growx, h 44!, gapbottom 6");
        panel.add(debitBtn, "growx, h 44!, gapbottom 6");
        panel.add(creditBtn, "growx, h 44!");

        return panel;
    }

    private void addSummaryRow(JPanel panel, String label, String value) {
        JLabel l = new JLabel(label);
        l.setFont(AppTheme.bodyFont());
        l.setForeground(AppTheme.TEXT_SECONDARY);

        JLabel v = new JLabel(value);
        v.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_BODY));
        v.setForeground(AppTheme.TEXT_PRIMARY);

        panel.add(l, "gapbottom 5");
        panel.add(v, "gapbottom 5");
    }

    private JPanel buildRightTypingPane() {
        JPanel panel = new JPanel(new MigLayout("insets 0, fillx, wrap 1"));
        panel.setOpaque(false);

        JLabel tenderedLabel = new JLabel("Amount Tendered (₱)");
        tenderedLabel.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_TABLE_HEADER));
        tenderedLabel.setForeground(AppTheme.TEXT_PRIMARY);
        panel.add(tenderedLabel, "gapbottom 4");

        tenderedField.setFont(AppTheme.titleFont(24));
        tenderedField.setHorizontalAlignment(SwingConstants.RIGHT);
        tenderedField.setEditable(false);
        tenderedField.setBackground(AppTheme.BACKGROUND);
        tenderedField.setForeground(AppTheme.TEXT_PRIMARY);
        tenderedField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1),
                BorderFactory.createEmptyBorder(4, 16, 4, 16)));
        tenderedField.setFocusable(true);
        panel.add(tenderedField, "growx, h " + INPUT_HEIGHT + "!, gapbottom 10");

        JPanel presetPanel = new JPanel(new MigLayout("insets 0", "[grow]8[grow]8[grow]"));
        presetPanel.setOpaque(false);

        JButton exactBtn = createPresetBtn("Exact");
        exactBtn.addActionListener(e -> setAmount(totals.totalDue()));

        JButton p500Btn = createPresetBtn("₱ 500");
        p500Btn.addActionListener(e -> setAmount(Money.of(500)));

        JButton p1000Btn = createPresetBtn("₱ 1,000");
        p1000Btn.addActionListener(e -> setAmount(Money.of(1000)));

        presetPanel.add(exactBtn, "growx, h 36!");
        presetPanel.add(p500Btn, "growx, h 36!");
        presetPanel.add(p1000Btn, "growx, h 36!");
        panel.add(presetPanel, "growx, gapbottom 10");

        panel.add(buildNumpad(), "growx, gapbottom 10");

        changeCard.setOpaque(true);
        changeCard.setBackground(AppTheme.BACKGROUND);
        changeCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));

        changeLabel.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_BODY));
        changeLabel.setForeground(AppTheme.TEXT_MUTED);
        changeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        changeCard.add(changeLabel, BorderLayout.CENTER);
        panel.add(changeCard, "growx, gapbottom 4");

        errorLabel.setForeground(AppTheme.DANGER);
        errorLabel.setFont(AppTheme.captionFont());
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(errorLabel, "growx");

        return panel;
    }

    private JButton createPresetBtn(String label) {
        JButton btn = new JButton(label) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppTheme.CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.setColor(AppTheme.BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_BODY));
        btn.setForeground(AppTheme.TEXT_PRIMARY);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private PaymentMethod selectedMethod() {
        return selectedMethod;
    }

    private JPanel buildNumpad() {
        JPanel pad = new JPanel(new MigLayout("insets 0, gap 8 8, wrap 3", "[grow,fill][grow,fill][grow,fill]"));
        pad.setOpaque(false);

        int padBtnHeight = 44;
        Font padFont = AppTheme.titleFont(18);

        String[] keys = {"7", "8", "9", "4", "5", "6", "1", "2", "3", "C", "0", "BKSP"};

        for (String key : keys) {
            JButton btn = new JButton() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(AppTheme.CARD);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.setColor(AppTheme.BORDER);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            btn.setFont(padFont);
            btn.setFocusPainted(false);
            btn.setContentAreaFilled(false);
            btn.setOpaque(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            if ("C".equals(key)) {
                btn.setText("C");
                btn.setForeground(AppTheme.DANGER);
                btn.addActionListener(e -> onClearAll());
            } else if ("BKSP".equals(key)) {
                btn.setIcon(Icons.backspace(AppTheme.TEXT_SECONDARY, 18));
                btn.addActionListener(e -> onBackspace());
            } else {
                btn.setText(key);
                btn.setForeground(AppTheme.TEXT_PRIMARY);
                btn.addActionListener(e -> appendDigit(key.charAt(0)));
            }

            pad.add(btn, "h " + padBtnHeight + "!");
        }

        return pad;
    }

    private void appendDigit(char digit) {
        if (digits.length() == 0 && digit == '0') {
            return;
        }
        if (digits.length() >= 9) {
            return;
        }
        digits.append(digit);
        updateDisplayFromDigits();
    }

    private void onBackspace() {
        if (digits.length() > 0) {
            digits.deleteCharAt(digits.length() - 1);
            updateDisplayFromDigits();
        }
    }

    private void onClearAll() {
        digits.setLength(0);
        updateDisplayFromDigits();
    }

    private void setAmount(Money money) {
        digits.setLength(0);
        long minorUnits = money.toMinorUnits();
        if (minorUnits > 0) {
            digits.append(minorUnits);
        }
        updateDisplayFromDigits();
    }

    private void updateDisplayFromDigits() {
        errorLabel.setText(" ");

        Money currentMoney;
        if (digits.length() == 0) {
            currentMoney = Money.ZERO;
        } else {
            long minorUnits = Long.parseLong(digits.toString());
            currentMoney = Money.ofMinorUnits(minorUnits);
        }

        tenderedField.setText(MoneyFormatter.formatPlain(currentMoney));

        if (!currentMoney.isZero() && currentMoney.compareTo(totals.totalDue()) >= 0) {
            Money change = currentMoney.subtract(totals.totalDue());
            changeLabel.setText("Change Due: " + MoneyFormatter.format(change));
            changeLabel.setForeground(AppTheme.SUCCESS);
            changeCard.setBackground(AppTheme.SUCCESS_BG);
            changeCard.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(AppTheme.SUCCESS_BORDER, 1),
                    BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        } else {
            changeLabel.setText("Enter amount or choose quick preset");
            changeLabel.setForeground(AppTheme.TEXT_MUTED);
            changeCard.setBackground(AppTheme.BACKGROUND);
            changeCard.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(AppTheme.BORDER, 1),
                    BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        }
    }

    private Money getTenderedAmount() {
        if (digits.length() == 0) return Money.ZERO;
        long minorUnits = Long.parseLong(digits.toString());
        return Money.ofMinorUnits(minorUnits);
    }

    private void attemptCheckout() {
        errorLabel.setText(" ");

        Money amountTendered = getTenderedAmount();

        if (amountTendered.isZero() || amountTendered.isNegative()) {
            errorLabel.setText("Enter the amount tendered.");
            return;
        }

        try {
            Money change = context.orderService().calculateChange(totals.totalDue(), amountTendered);
            changeLabel.setText("Change Due: " + MoneyFormatter.format(change));
        } catch (InsufficientPaymentException ex) {
            errorLabel.setText("Insufficient. Minimum: " + MoneyFormatter.format(ex.amountDue()));
            return;
        }

        result = context.orderService().checkout(cart, cashierId, cashierName, selectedMethod(), amountTendered);
        dispose();
    }

    private final class MethodCardButton extends JButton {
        private final String titleText;
        private final String subtitleText;
        private final javax.swing.Icon cardIcon;
        private final PaymentMethod method;
        private boolean isSelectedMethod = false;

        MethodCardButton(String title, String subtitle, javax.swing.Icon icon, PaymentMethod method) {
            super();
            this.titleText = title;
            this.subtitleText = subtitle;
            this.cardIcon = icon;
            this.method = method;

            setLayout(new BorderLayout(10, 0));
            setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setContentAreaFilled(false);
            setOpaque(false);

            JLabel iconLabel = new JLabel(icon);
            add(iconLabel, BorderLayout.WEST);

            JPanel textPanel = new JPanel(new MigLayout("insets 0, wrap 1, gapy 0"));
            textPanel.setOpaque(false);

            JLabel titleLbl = new JLabel(title);
            titleLbl.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_BODY));
            titleLbl.setForeground(AppTheme.TEXT_PRIMARY);

            JLabel subLbl = new JLabel(subtitle);
            subLbl.setFont(AppTheme.captionFont());
            subLbl.setForeground(AppTheme.TEXT_MUTED);

            textPanel.add(titleLbl);
            textPanel.add(subLbl);
            add(textPanel, BorderLayout.CENTER);

            addActionListener(e -> {
                for (MethodCardButton b : methodButtons) {
                    b.setSelectedMethod(b == this);
                }
                selectedMethod = method;
                if (method != PaymentMethod.CASH) {
                    setAmount(totals.totalDue());
                }
            });
        }

        void setSelectedMethod(boolean selected) {
            this.isSelectedMethod = selected;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            if (isSelectedMethod) {
                g2.setColor(AppTheme.ACCENT_SUBTLE);
                g2.fillRoundRect(0, 0, w, h, 6, 6);
                g2.setColor(AppTheme.ACCENT);
                g2.drawRoundRect(0, 0, w - 1, h - 1, 6, 6);
            } else {
                g2.setColor(AppTheme.CARD);
                g2.fillRoundRect(0, 0, w, h, 6, 6);
                g2.setColor(AppTheme.BORDER);
                g2.drawRoundRect(0, 0, w - 1, h - 1, 6, 6);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }
}

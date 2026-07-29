package com.restaurant.pos.ui.order;

import com.restaurant.pos.AppContext;
import com.restaurant.pos.exception.InsufficientPaymentException;
import com.restaurant.pos.model.Cart;
import com.restaurant.pos.model.CheckoutResult;
import com.restaurant.pos.model.Money;
import com.restaurant.pos.model.PaymentMethod;
import com.restaurant.pos.service.OrderTotals;
import com.restaurant.pos.ui.format.MoneyFormatter;
import com.restaurant.pos.ui.theme.AppTheme;
import com.restaurant.pos.ui.theme.Icons;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

final class PaymentDialog extends JDialog {

    private static final int BUTTON_HEIGHT = 44;
    private static final int INPUT_HEIGHT = 44;

    private final AppContext context;
    private final Cart cart;
    private final OrderTotals totals;
    private final long cashierId;
    private final String cashierName;

    private final JRadioButton cashRadio = new JRadioButton(PaymentMethod.CASH.displayName(), true);
    private final JRadioButton debitRadio = new JRadioButton(PaymentMethod.DEBIT_CARD.displayName());
    private final JRadioButton creditRadio = new JRadioButton(PaymentMethod.CREDIT_CARD.displayName());
    private final JTextField tenderedField = new JTextField(14);
    private final JLabel changeLabel = new JLabel(" ");
    private final JLabel errorLabel = new JLabel(" ");

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

        updateDisplayFromDigits();
    }

    CheckoutResult getResult() {
        return result;
    }

    private JPanel buildContent() {
        JPanel panel = new JPanel(new MigLayout("insets 24, wrap 1", "[380!]"));
        panel.setBackground(AppTheme.CARD);

        JLabel title = new JLabel("Process Payment");
        title.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_DASHBOARD_TITLE));
        title.setForeground(AppTheme.TEXT_PRIMARY);
        panel.add(title, "gapbottom 8");

        JLabel totalCaption = new JLabel("Total Due: " + MoneyFormatter.format(totals.totalDue()));
        totalCaption.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_SECTION_HEADER));
        totalCaption.setForeground(AppTheme.PRIMARY);
        panel.add(totalCaption, "gapbottom 16");

        JLabel methodLabel = new JLabel("Payment Method:");
        methodLabel.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_TABLE_HEADER));
        panel.add(methodLabel, "gapbottom 6");

        JPanel methodPanel = new JPanel(new MigLayout("insets 0", "[]16[]16[]"));
        methodPanel.setOpaque(false);
        ButtonGroup methodGroup = new ButtonGroup();
        methodGroup.add(cashRadio);
        methodGroup.add(debitRadio);
        methodGroup.add(creditRadio);
        Font radioFont = AppTheme.titleFont(AppTheme.FONT_SIZE_BODY);
        cashRadio.setFont(radioFont);
        cashRadio.setOpaque(false);
        debitRadio.setFont(radioFont);
        debitRadio.setOpaque(false);
        creditRadio.setFont(radioFont);
        creditRadio.setOpaque(false);
        methodPanel.add(cashRadio);
        methodPanel.add(debitRadio);
        methodPanel.add(creditRadio);
        panel.add(methodPanel, "gapbottom 12");

        JLabel tenderedLabel = new JLabel("Amount Tendered (\u20B1):");
        tenderedLabel.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_TABLE_HEADER));
        panel.add(tenderedLabel, "gapbottom 4");

        tenderedField.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_DASHBOARD_TITLE));
        tenderedField.setHorizontalAlignment(SwingConstants.RIGHT);
        tenderedField.setEditable(false);
        tenderedField.setBackground(Color.WHITE);
        tenderedField.setFocusable(true);
        tenderedField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                    onBackspace();
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    attemptCheckout();
                } else if (e.getKeyChar() >= '0' && e.getKeyChar() <= '9') {
                    appendDigit(e.getKeyChar());
                }
            }
        });
        panel.add(tenderedField, "growx, h " + INPUT_HEIGHT + "!, gapbottom 10");

        JPanel presetPanel = new JPanel(new MigLayout("insets 0", "[grow]6[grow]6[grow]"));
        presetPanel.setOpaque(false);
        JButton exactBtn = createPresetBtn("Exact");
        exactBtn.addActionListener(e -> setAmount(totals.totalDue()));

        JButton p500Btn = createPresetBtn("\u20B1 500");
        p500Btn.addActionListener(e -> setAmount(Money.of(500)));

        JButton p1000Btn = createPresetBtn("\u20B1 1,000");
        p1000Btn.addActionListener(e -> setAmount(Money.of(1000)));

        presetPanel.add(exactBtn, "growx, h 34!");
        presetPanel.add(p500Btn, "growx, h 34!");
        presetPanel.add(p1000Btn, "growx, h 34!");
        panel.add(presetPanel, "growx, gapbottom 12");

        panel.add(buildNumpad(), "growx, gapbottom 12");

        changeLabel.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_SECTION_HEADER));
        changeLabel.setForeground(AppTheme.SUCCESS);
        panel.add(changeLabel, "gapbottom 4");

        errorLabel.setForeground(AppTheme.DANGER);
        errorLabel.setFont(AppTheme.bodyFont());
        panel.add(errorLabel, "gapbottom 16");

        JPanel buttonPanel = new JPanel(new MigLayout("insets 0", "[grow]12[grow]"));
        buttonPanel.setOpaque(false);

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_BODY));
        cancelBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cancelBtn.addActionListener(e -> dispose());

        JButton confirmBtn = new JButton("Confirm Payment");
        confirmBtn.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_BODY));
        confirmBtn.setBackground(AppTheme.PRIMARY);
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        confirmBtn.addActionListener(e -> attemptCheckout());

        buttonPanel.add(cancelBtn, "growx, h " + BUTTON_HEIGHT + "!");
        buttonPanel.add(confirmBtn, "growx, h " + BUTTON_HEIGHT + "!");
        panel.add(buttonPanel, "growx");

        return panel;
    }

    private JButton createPresetBtn(String label) {
        JButton btn = new JButton(label);
        btn.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_CAPTION));
        btn.setBackground(AppTheme.BACKGROUND);
        btn.setForeground(AppTheme.PRIMARY);
        btn.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private PaymentMethod selectedMethod() {
        if (debitRadio.isSelected()) return PaymentMethod.DEBIT_CARD;
        if (creditRadio.isSelected()) return PaymentMethod.CREDIT_CARD;
        return PaymentMethod.CASH;
    }

    private JPanel buildNumpad() {
        JPanel pad = new JPanel(new MigLayout("insets 0, gap 6 6, wrap 3", "[grow,fill][grow,fill][grow,fill]"));
        pad.setOpaque(false);

        int padBtnHeight = 40;
        Font padFont = AppTheme.titleFont(AppTheme.FONT_SIZE_SECTION_HEADER);

        String[] keys = {"7", "8", "9", "4", "5", "6", "1", "2", "3", "C", "0", "BKSP"};

        for (String key : keys) {
            JButton btn = new JButton();
            btn.setFont(padFont);
            btn.setFocusPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            if ("C".equals(key)) {
                btn.setText("C");
                btn.setBackground(new Color(254, 243, 199));
                btn.setForeground(AppTheme.WARNING);
                btn.setBorder(BorderFactory.createLineBorder(new Color(253, 230, 138)));
                btn.addActionListener(e -> onClearAll());
            } else if ("BKSP".equals(key)) {
                btn.setIcon(Icons.backspace(AppTheme.DANGER, 18));
                btn.setBackground(new Color(254, 226, 226));
                btn.setForeground(AppTheme.DANGER);
                btn.setBorder(BorderFactory.createLineBorder(new Color(252, 165, 165)));
                btn.addActionListener(e -> onBackspace());
            } else {
                btn.setText(key);
                btn.setBackground(AppTheme.BACKGROUND);
                btn.setForeground(AppTheme.TEXT_PRIMARY);
                btn.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER));
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
        changeLabel.setText(" ");

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
            changeLabel.setText("Change: " + MoneyFormatter.format(change));
        }
    }

    private Money getTenderedAmount() {
        if (digits.length() == 0) return Money.ZERO;
        long minorUnits = Long.parseLong(digits.toString());
        return Money.ofMinorUnits(minorUnits);
    }

    private void attemptCheckout() {
        errorLabel.setText(" ");
        changeLabel.setText(" ");

        Money amountTendered = getTenderedAmount();

        if (amountTendered.isZero() || amountTendered.isNegative()) {
            errorLabel.setText("Enter the amount tendered.");
            return;
        }

        try {
            Money change = context.orderService().calculateChange(totals.totalDue(), amountTendered);
            changeLabel.setText("Change: " + MoneyFormatter.format(change));
        } catch (InsufficientPaymentException ex) {
            errorLabel.setText("Insufficient. Minimum: " + MoneyFormatter.format(ex.amountDue()));
            return;
        }

        result = context.orderService().checkout(cart, cashierId, cashierName, selectedMethod(), amountTendered);
        dispose();
    }
}

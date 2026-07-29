package com.restaurant.pos.service;

import com.restaurant.pos.model.BusinessSettings;
import com.restaurant.pos.model.Order;
import com.restaurant.pos.model.OrderLineItem;
import com.restaurant.pos.model.Payment;
import com.restaurant.pos.ui.format.MoneyFormatter;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class ReceiptFormatter {

    public static final int RECEIPT_WIDTH = 42;
    private static final String SINGLE_DIVIDER = "------------------------------------------";
    private static final String DOUBLE_DIVIDER = "==========================================";

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    private ReceiptFormatter() {
    }

    public static String formatReceipt(Order order, Payment payment, BusinessSettings settings) {
        StringBuilder sb = new StringBuilder();

        appendHeader(sb, settings);

        appendTransactionInfo(sb, order);

        appendItemsTable(sb, order);

        appendSummary(sb, order);

        appendPaymentDetails(sb, payment);

        appendFooter(sb, settings);

        return sb.toString();
    }

    private static void appendHeader(StringBuilder sb, BusinessSettings settings) {
        sb.append(SINGLE_DIVIDER).append("\n");

        if (!settings.businessName().isEmpty()) {
            for (String line : wrapText(settings.businessName().toUpperCase(), RECEIPT_WIDTH)) {
                sb.append(center(line, RECEIPT_WIDTH)).append("\n");
            }
        }

        if (!settings.branchName().isEmpty()) {
            for (String line : wrapText("Branch: " + settings.branchName(), RECEIPT_WIDTH)) {
                sb.append(center(line, RECEIPT_WIDTH)).append("\n");
            }
        }

        if (!settings.address().isEmpty()) {
            for (String line : wrapText(settings.address(), RECEIPT_WIDTH)) {
                sb.append(center(line, RECEIPT_WIDTH)).append("\n");
            }
        }

        if (!settings.phone().isEmpty()) {
            sb.append(center("Tel: " + settings.phone(), RECEIPT_WIDTH)).append("\n");
        }

        if (!settings.tin().isEmpty()) {
            sb.append(center("TIN: " + settings.tin(), RECEIPT_WIDTH)).append("\n");
        }

        if (!settings.vatRegNo().isEmpty()) {
            sb.append(center("VAT REG: " + settings.vatRegNo(), RECEIPT_WIDTH)).append("\n");
        }

        if (!settings.birPermitNo().isEmpty()) {
            sb.append(center("BIR PERMIT #: " + settings.birPermitNo(), RECEIPT_WIDTH)).append("\n");
        }

        if (!settings.posSerialNo().isEmpty()) {
            sb.append(center("SERIAL #: " + settings.posSerialNo(), RECEIPT_WIDTH)).append("\n");
        }

        if (!settings.machineNo().isEmpty()) {
            sb.append(center("MIN: " + settings.machineNo(), RECEIPT_WIDTH)).append("\n");
        }

        sb.append(SINGLE_DIVIDER).append("\n");
    }

    private static void appendTransactionInfo(StringBuilder sb, Order order) {
        sb.append(formatPair("Receipt #:", "OR-" + order.orderNumber())).append("\n");
        sb.append(formatPair("Order #:", order.orderNumber())).append("\n");
        sb.append(formatPair("Date/Time:", DATE_TIME_FORMATTER.format(order.placedAt()))).append("\n");
        sb.append(formatPair("Cashier:", order.cashierName())).append("\n");
        sb.append(formatPair("Type:", order.orderType().displayName())).append("\n");

        if (order.tableNumber() != null && !order.tableNumber().trim().isEmpty()) {
            sb.append(formatPair("Table #:", order.tableNumber().trim())).append("\n");
        }

        sb.append(SINGLE_DIVIDER).append("\n");
    }

    private static void appendItemsTable(StringBuilder sb, Order order) {

        sb.append(String.format("%-3s %-26s %10s%n", "Qty", "Item Description", "Amount"));
        sb.append(SINGLE_DIVIDER).append("\n");

        for (OrderLineItem item : order.lineItems()) {
            String qtyStr = String.valueOf(item.quantity());
            String amountStr = MoneyFormatter.formatPlain(item.lineTotal());
            List<String> descLines = wrapText(item.itemName(), 26);

            for (int i = 0; i < descLines.size(); i++) {
                String currentQty = (i == 0) ? qtyStr : "";
                String currentAmount = (i == 0) ? amountStr : "";
                sb.append(String.format("%-3s %-26s %10s%n", currentQty, descLines.get(i), currentAmount));
            }
        }

        sb.append(SINGLE_DIVIDER).append("\n");
    }

    private static void appendSummary(StringBuilder sb, Order order) {
        sb.append(formatPair("Subtotal", MoneyFormatter.formatPlain(order.subtotal()))).append("\n");

        String vatLabel = "VAT (" + order.vatRatePercent().stripTrailingZeros().toPlainString() + "%)";
        sb.append(formatPair(vatLabel, MoneyFormatter.formatPlain(order.vatAmount()))).append("\n");

        if (order.discountAmount().isPositive()) {
            sb.append(formatPair("Discount", "-" + MoneyFormatter.formatPlain(order.discountAmount()))).append("\n");
        }

        sb.append(DOUBLE_DIVIDER).append("\n");
        sb.append(formatPair("TOTAL", "PHP " + MoneyFormatter.formatPlain(order.totalDue()))).append("\n");
        sb.append(DOUBLE_DIVIDER).append("\n");
    }

    private static void appendPaymentDetails(StringBuilder sb, Payment payment) {
        sb.append(formatPair("Payment Method", payment.method().displayName())).append("\n");
        sb.append(formatPair("Cash Tendered", "PHP " + MoneyFormatter.formatPlain(payment.amountTendered()))).append("\n");
        sb.append(formatPair("Change", "PHP " + MoneyFormatter.formatPlain(payment.changeGiven()))).append("\n");
        sb.append(SINGLE_DIVIDER).append("\n");
    }

    private static void appendFooter(StringBuilder sb, BusinessSettings settings) {
        sb.append(center("Thank you for dining with us!", RECEIPT_WIDTH)).append("\n");
        sb.append(center("Please come again.", RECEIPT_WIDTH)).append("\n");
        sb.append(center("This serves as your customer receipt.", RECEIPT_WIDTH)).append("\n");
    }

    public static String formatPair(String label, String value) {
        if (label == null) label = "";
        if (value == null) value = "";

        int totalLen = label.length() + value.length() + 1;
        if (totalLen <= RECEIPT_WIDTH) {
            int spaces = RECEIPT_WIDTH - label.length() - value.length();
            return label + " ".repeat(spaces) + value;
        }

        int maxLabelLen = RECEIPT_WIDTH - value.length() - 1;
        if (maxLabelLen > 0 && label.length() > maxLabelLen) {
            label = label.substring(0, maxLabelLen);
        }
        int spaces = Math.max(1, RECEIPT_WIDTH - label.length() - value.length());
        return label + " ".repeat(spaces) + value;
    }

    public static String center(String text, int width) {
        if (text == null) return "";
        text = text.trim();
        if (text.length() >= width) {
            return text.substring(0, width);
        }
        int padding = (width - text.length()) / 2;
        return " ".repeat(padding) + text;
    }

    public static List<String> wrapText(String text, int maxLen) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.trim().isEmpty()) {
            return lines;
        }

        String[] originalLines = text.split("\r?\n");
        for (String orig : originalLines) {
            orig = orig.trim();
            if (orig.isEmpty()) continue;

            while (orig.length() > maxLen) {
                int spaceIdx = orig.lastIndexOf(' ', maxLen);
                if (spaceIdx <= 0) {

                    lines.add(orig.substring(0, maxLen));
                    orig = orig.substring(maxLen).trim();
                } else {
                    lines.add(orig.substring(0, spaceIdx));
                    orig = orig.substring(spaceIdx + 1).trim();
                }
            }
            if (!orig.isEmpty()) {
                lines.add(orig);
            }
        }
        return lines;
    }
}

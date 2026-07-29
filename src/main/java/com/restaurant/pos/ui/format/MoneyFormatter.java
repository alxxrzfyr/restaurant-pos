package com.restaurant.pos.ui.format;

import com.restaurant.pos.model.Money;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class MoneyFormatter {

    private static final String CURRENCY_SYMBOL = "\u20B1";
    private static final DecimalFormat FORMAT;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        FORMAT = new DecimalFormat("#,##0.00", symbols);
    }

    private MoneyFormatter() {
    }

    public static String format(Money amount) {
        return CURRENCY_SYMBOL + " " + FORMAT.format(amount.toBigDecimal());
    }

    public static String formatPlain(Money amount) {
        return FORMAT.format(amount.toBigDecimal());
    }
}

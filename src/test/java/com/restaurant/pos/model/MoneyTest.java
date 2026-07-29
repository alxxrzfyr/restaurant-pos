package com.restaurant.pos.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoneyTest {

    @Test
    void roundsHalfUpToTwoDecimalPlaces() {
        Money money = Money.of(new BigDecimal("10.005"));
        assertEquals("10.01", money.toPlainString());
    }

    @Test
    void addSubtractMultiplyProduceExactResults() {
        Money burger = Money.of(new BigDecimal("30.00"));
        Money total = burger.multiply(3).add(Money.of(new BigDecimal("15.50"))).subtract(Money.of(new BigDecimal("5.00")));
        assertEquals("100.50", total.toPlainString());
    }

    @Test
    void percentageOfComputesTwelvePercentVat() {
        Money subtotal = Money.of(new BigDecimal("100.00"));
        Money vat = subtotal.percentageOf(new BigDecimal("12"));
        assertEquals("12.00", vat.toPlainString());
    }

    @Test
    void percentageOfRoundsHalfUp() {
        Money subtotal = Money.of(new BigDecimal("30.00"));
        Money vat = subtotal.percentageOf(new BigDecimal("12"));
        assertEquals("3.60", vat.toPlainString());
    }

    @Test
    void minorUnitsRoundTripPreservesValue() {
        Money original = Money.of(new BigDecimal("135.50"));
        long minorUnits = original.toMinorUnits();
        assertEquals(13550L, minorUnits);
        assertEquals(original, Money.ofMinorUnits(minorUnits));
    }

    @Test
    void zeroIsNeitherPositiveNorNegative() {
        assertTrue(Money.ZERO.isZero());
        assertFalse(Money.ZERO.isPositive());
        assertFalse(Money.ZERO.isNegative());
    }

    @Test
    void equalsIsValueBasedNotScaleSensitive() {
        assertEquals(Money.of(new BigDecimal("30")), Money.of(new BigDecimal("30.00")));
    }

    @Test
    void parseRejectsInvalidText() {
        assertThrows(NumberFormatException.class, () -> Money.parse("not a number"));
    }
}

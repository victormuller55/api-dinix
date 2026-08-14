package br.net.convertix.dinix.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoneyUtilsTest {

    @Test
    void splitInstallmentsKeepsTotal() {
        BigDecimal[] parts = MoneyUtils.splitInstallments(new BigDecimal("4800.00"), 12);
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal part : parts) {
            sum = sum.add(part);
        }
        assertEquals(12, parts.length);
        assertEquals(new BigDecimal("4800.00"), sum);
        assertEquals(new BigDecimal("400.00"), parts[1]);
    }

    @Test
    void percentageHandlesZeroTotal() {
        assertEquals(new BigDecimal("0.00"), MoneyUtils.percentage(new BigDecimal("10"), BigDecimal.ZERO));
    }

    @Test
    void firstInstallmentAbsorbsRemainder() {
        BigDecimal[] parts = MoneyUtils.splitInstallments(new BigDecimal("100.00"), 3);
        assertTrue(parts[0].compareTo(parts[1]) >= 0);
        assertEquals(new BigDecimal("100.00"), parts[0].add(parts[1]).add(parts[2]));
    }
}

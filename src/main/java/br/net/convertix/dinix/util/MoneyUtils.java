package br.net.convertix.dinix.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;

public final class MoneyUtils {

    public static final int SCALE = 2;
    public static final RoundingMode ROUNDING = RoundingMode.HALF_EVEN;

    private MoneyUtils() {
    }

    public static BigDecimal zero() {
        return BigDecimal.ZERO.setScale(SCALE, ROUNDING);
    }

    public static BigDecimal of(BigDecimal value) {
        if (value == null) {
            return zero();
        }
        return value.setScale(SCALE, ROUNDING);
    }

    public static BigDecimal[] splitInstallments(BigDecimal total, int installments) {
        BigDecimal normalized = of(total);
        BigDecimal base = normalized.divide(BigDecimal.valueOf(installments), SCALE, ROUNDING);
        BigDecimal[] parts = new BigDecimal[installments];
        BigDecimal allocated = zero();
        for (int i = 1; i < installments; i++) {
            parts[i] = base;
            allocated = allocated.add(base);
        }
        parts[0] = normalized.subtract(allocated);
        return parts;
    }

    public static BigDecimal percentage(BigDecimal part, BigDecimal total) {
        if (total == null || total.compareTo(BigDecimal.ZERO) == 0) {
            return zero();
        }
        return of(part).multiply(BigDecimal.valueOf(100)).divide(of(total), SCALE, ROUNDING);
    }

    public static LocalDate atDayOfMonth(YearMonth yearMonth, int day) {
        int safeDay = Math.min(day, yearMonth.lengthOfMonth());
        return yearMonth.atDay(safeDay);
    }
}

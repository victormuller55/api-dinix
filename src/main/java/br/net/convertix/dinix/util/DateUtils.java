package br.net.convertix.dinix.util;

import java.time.LocalDate;
import java.time.YearMonth;

public final class DateUtils {

    private DateUtils() {
    }

    public static LocalDate startOfMonth(int month, int year) {
        return YearMonth.of(year, month).atDay(1);
    }

    public static LocalDate endOfMonth(int month, int year) {
        return YearMonth.of(year, month).atEndOfMonth();
    }

    public static YearMonth previous(int month, int year) {
        return YearMonth.of(year, month).minusMonths(1);
    }

    public static boolean isInPeriod(LocalDate date, LocalDate start, LocalDate end) {
        return (date.isEqual(start) || date.isAfter(start)) && (date.isEqual(end) || date.isBefore(end));
    }
}

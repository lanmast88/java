package ru.mirea.insurance.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Форматирование денег и дат: суммы с двумя знаками и разделителем разрядов,
 * даты в виде ДД.ММ.ГГГГ. Лежит в util, потому что нужно и меню, и текстам
 * бизнес-исключений.
 */
public final class Formats {
    public static final String CURRENCY = "₽";
    public static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.uuuu");

    private Formats() {
    }

    public static String money(BigDecimal amount) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(' ');
        symbols.setDecimalSeparator(',');
        symbols.setMinusSign('-');
        DecimalFormat format = new DecimalFormat("#,##0.00", symbols);
        format.setRoundingMode(RoundingMode.HALF_UP);
        return format.format(amount);
    }

    public static String rub(BigDecimal amount) {
        return money(amount) + " " + CURRENCY;
    }

    public static String date(LocalDate date) {
        return date.format(DATE);
    }

    public static String period(LocalDate start, LocalDate end) {
        return date(start) + " – " + date(end);
    }

    public static String shortName(String lastName, String firstName) {
        return lastName + " " + firstName.charAt(0) + ".";
    }

    public static String fullName(String lastName, String firstName) {
        return lastName + " " + firstName;
    }
}

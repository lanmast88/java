package ru.mirea.insurance.ui.console;

import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Scanner;
import java.util.regex.Pattern;

public class ConsoleReader {
    private static final DateTimeFormatter DATE = Formats.DATE.withResolverStyle(ResolverStyle.STRICT);
    private static final Pattern DATE_SHAPE = Pattern.compile("\\d{2}\\.\\d{2}\\.\\d{4}");
    private static final Pattern MONEY_SHAPE = Pattern.compile("-?\\d+(\\.\\d{1,2})?");

    private final Scanner scanner;
    private final ConsolePrinter printer;

    public ConsoleReader(InputStream in, ConsolePrinter printer) {
        this.scanner = new Scanner(in, StandardCharsets.UTF_8);
        this.printer = printer;
    }

    public String readLine(String prompt) {
        printer.prompt(prompt);
        if (!scanner.hasNextLine()) {
            throw new InputClosedException();
        }
        return scanner.nextLine().trim();
    }

    public int readId(String prompt) {
        while (true) {
            String input = readLine(prompt);
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                printer.error("ID должен быть целым числом");
            }
        }
    }

    public int readChoice(String prompt, int max) {
        while (true) {
            String input = readLine(prompt);
            if (input.matches("\\d{1,9}")) {
                int choice = Integer.parseInt(input);
                if (choice >= 1 && choice <= max) {
                    return choice;
                }
            }
            printer.error("введите число от 1 до " + max);
        }
    }

    public String readRequired(String prompt, String emptyError) {
        while (true) {
            String input = readLine(prompt);
            if (!input.isEmpty()) {
                return input;
            }
            printer.error(emptyError);
        }
    }

    public String readOptional(String label, String current) {
        String input = readLine(label + " [" + current + "]: ");
        return input.isEmpty() ? current : input;
    }

    public LocalDate readDate(String prompt) {
        while (true) {
            String input = readLine(prompt);
            if (!DATE_SHAPE.matcher(input).matches()) {
                printer.error("неверный формат даты «" + input + "», нужно ДД.ММ.ГГГГ, например 01.10.2026");
                continue;
            }
            try {
                return LocalDate.parse(input, DATE);
            } catch (DateTimeParseException e) {
                printer.error("даты " + input + " не существует");
            }
        }
    }

    public BigDecimal readMoney(String prompt, String notPositiveError) {
        while (true) {
            String input = readLine(prompt);
            String normalized = input.replaceAll("[\\s\\u00A0]", "").replace(',', '.');
            if (!MONEY_SHAPE.matcher(normalized).matches()) {
                printer.error("неверное число «" + input + "», пример: 800000 или 800000,50");
                continue;
            }
            BigDecimal amount = new BigDecimal(normalized);
            if (amount.signum() > 0) {
                return amount;
            }
            printer.error(notPositiveError);
        }
    }

    public boolean confirm(String question) {
        while (true) {
            String input = readLine(Tone.BOLD.paint(question) + " (да/нет): ").toLowerCase();
            if (input.equals("да")) {
                return true;
            }
            if (input.equals("нет")) {
                return false;
            }
            printer.error("ответьте «да» или «нет»");
        }
    }

    public void pause() {
        readLine(Tone.DIM.paint("Нажмите Enter, чтобы продолжить…"));
    }
}

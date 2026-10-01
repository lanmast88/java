package ru.mirea.insurance.ui.console;

import java.util.ArrayList;
import java.util.List;

public class TablePrinter {
    private static final String EMPTY_TEXT = "Ничего не найдено";

    private final ConsolePrinter printer;

    public TablePrinter(ConsolePrinter printer) {
        this.printer = printer;
    }

    public void print(List<Column> columns, List<List<String>> rows) {
        print(columns, rows, "");
    }

    public void print(List<Column> columns, List<List<String>> rows, String indent) {
        rule(columns, indent, "┌", "┬", "┐");
        row(columns, columns.stream().map(Column::header).toList(), indent, true);
        rule(columns, indent, "├", "┼", "┤");
        if (rows.isEmpty()) {
            int inner = columns.stream().mapToInt(c -> c.width() + 3).sum() - 3;
            int left = (inner - EMPTY_TEXT.length()) / 2;
            printer.line(indent + Tone.DIM.paint("│ ") + " ".repeat(left) + Tone.YELLOW.paint(EMPTY_TEXT)
                    + " ".repeat(inner - left - EMPTY_TEXT.length()) + Tone.DIM.paint(" │"));
        }
        for (List<String> cells : rows) {
            row(columns, cells, indent, false);
        }
        rule(columns, indent, "└", "┴", "┘");
        printer.line(indent + "Найдено: " + rows.size());
    }

    private void rule(List<Column> columns, String indent, String left, String middle, String right) {
        List<String> parts = columns.stream().map(c -> "─".repeat(c.width() + 2)).toList();
        printer.line(indent + Tone.DIM.paint(left + String.join(middle, parts) + right));
    }

    private void row(List<Column> columns, List<String> cells, String indent, boolean header) {
        StringBuilder result = new StringBuilder(indent).append(Tone.DIM.paint("│ "));
        for (int i = 0; i < columns.size(); i++) {
            Column column = columns.get(i);
            String cell = cells.get(i);
            if (!Text.hasAnsi(cell)) {
                cell = Text.truncate(cell, column.width());
            }
            String padded = column.alignRight() ? Text.padLeft(cell, column.width()) : Text.padRight(cell, column.width());
            result.append(header ? Tone.BOLD.paint(padded) : padded);
            result.append(Tone.DIM.paint(i < columns.size() - 1 ? " │ " : " │"));
        }
        printer.line(result.toString());
    }

    public void dump(String table, List<String> headers, List<Boolean> numeric, List<List<String>> rows) {
        int[] widths = new int[headers.size()];
        for (int i = 0; i < widths.length; i++) {
            widths[i] = headers.get(i).length();
            for (List<String> cells : rows) {
                widths[i] = Math.max(widths[i], cells.get(i).length());
            }
        }
        int excess = totalWidth(widths) - ConsolePrinter.MAX_WIDTH;
        if (excess > 0) {
            int widest = widestTextColumn(widths, numeric);
            if (widest >= 0) {
                widths[widest] = Math.max(headers.get(widest).length(), widths[widest] - excess);
                printer.warning("таблица шире " + ConsolePrinter.MAX_WIDTH + " символов, колонка "
                        + headers.get(widest) + " обрезана до " + widths[widest] + " символов");
            }
        }
        printer.line(Tone.BOLD.paint("Таблица " + table));
        dumpRow(headers, widths, numeric);
        List<String> dashes = new ArrayList<>();
        for (int width : widths) {
            dashes.add("-".repeat(width));
        }
        printer.line(Tone.DIM.paint(String.join("-+-", dashes)));
        for (List<String> cells : rows) {
            dumpRow(cells, widths, numeric);
        }
        printer.line("Строк: " + rows.size());
    }

    private void dumpRow(List<String> cells, int[] widths, List<Boolean> numeric) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < widths.length; i++) {
            if (i > 0) {
                result.append(Tone.DIM.paint(" | "));
            }
            String cell = Text.truncate(cells.get(i), widths[i]);
            result.append(numeric.get(i) ? Text.padLeft(cell, widths[i]) : Text.padRight(cell, widths[i]));
        }
        printer.line(result.toString());
    }

    private static int totalWidth(int[] widths) {
        int total = 3 * (widths.length - 1);
        for (int width : widths) {
            total += width;
        }
        return total;
    }

    private static int widestTextColumn(int[] widths, List<Boolean> numeric) {
        int widest = -1;
        for (int i = 0; i < widths.length; i++) {
            if (!numeric.get(i) && (widest < 0 || widths[i] > widths[widest])) {
                widest = i;
            }
        }
        return widest;
    }
}

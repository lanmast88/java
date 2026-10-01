package ru.mirea.insurance.ui.console;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ConsolePrinter {
    public static final int HEADER_WIDTH = 60;
    public static final int MAX_WIDTH = 110;

    private static final int TITLE_WIDTH = 44;
    private static final int CARD_WIDTH = 84;
    private static final int BAR_WIDTH = 24;

    private final PrintStream out;
    private boolean atBlankLine = true;

    public ConsolePrinter(PrintStream out) {
        this.out = out;
    }

    public static ConsolePrinter system() {
        return new ConsolePrinter(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));
    }

    public void line(String text) {
        out.println(text);
        atBlankLine = text.isEmpty();
    }

    public void blank() {
        line("");
    }

    public void gap() {
        if (!atBlankLine) {
            blank();
        }
    }

    public void prompt(String text) {
        out.print(text);
        out.flush();
        atBlankLine = false;
    }

    public void title(String name) {
        gap();
        int left = (TITLE_WIDTH - name.length()) / 2;
        int right = TITLE_WIDTH - left - name.length();
        line(Tone.CYAN.paint("╔" + "═".repeat(TITLE_WIDTH) + "╗"));
        line(Tone.CYAN.paint("║") + " ".repeat(left) + Tone.BOLD.paint(name) + " ".repeat(right) + Tone.CYAN.paint("║"));
        line(Tone.CYAN.paint("╚" + "═".repeat(TITLE_WIDTH) + "╝"));
    }

    public void header(String... crumbs) {
        gap();
        StringBuilder result = new StringBuilder(Tone.DIM.paint("── "));
        for (int i = 0; i < crumbs.length; i++) {
            if (i > 0) {
                result.append(Tone.DIM.paint(" › "));
            }
            boolean last = i == crumbs.length - 1;
            result.append(last ? Tone.paint(crumbs[i], Tone.BOLD, Tone.CYAN) : crumbs[i]);
        }
        int used = Text.visibleLength(result.toString()) + 1;
        result.append(Tone.DIM.paint(" " + "─".repeat(Math.max(3, HEADER_WIDTH - used))));
        line(result.toString());
    }

    public void menuItems(List<MenuItem> items) {
        for (MenuItem item : items) {
            boolean zero = item.number() == 0;
            Tone tone = zero ? Tone.DIM : Tone.BOLD;
            String label = zero ? Tone.DIM.paint(item.label()) : item.label();
            line("  " + tone.paint(item.number() + ".") + " " + label);
        }
    }

    public void error(String text, String... more) {
        message("Ошибка: ", Tone.RED, Tone.RED.paint(text), more);
    }

    public void databaseError(String text, String... more) {
        message("Ошибка базы данных: ", Tone.RED, Tone.RED.paint(text), more);
    }

    public void success(String text, String... more) {
        message("Готово: ", Tone.GREEN, Tone.GREEN.paint(text), more);
    }

    public void warning(String text, String... more) {
        message("Внимание: ", Tone.YELLOW, Tone.YELLOW.paint(text), more);
    }

    public void info(String text, String... more) {
        message("› ", Tone.CYAN, text, more);
    }

    private void message(String prefix, Tone tone, String body, String... more) {
        line(Tone.paint(prefix, Tone.BOLD, tone) + body);
        for (String extra : more) {
            line(" ".repeat(prefix.length()) + extra);
        }
    }

    public void ref(String text) {
        line("  " + Tone.DIM.paint("→ ") + text);
    }

    public void card(List<Field> fields) {
        String indent = "  ";
        int keyWidth = fields.stream().mapToInt(f -> f.key().length()).max().orElse(0) + 1;
        int valueWidth = CARD_WIDTH - indent.length() - keyWidth - 1;
        for (Field field : fields) {
            String key = Tone.DIM.paint(Text.padRight(field.key() + ":", keyWidth));
            String value = field.value();
            if (Text.hasAnsi(value) || value.length() <= valueWidth) {
                line(indent + key + " " + value);
                continue;
            }
            List<String> lines = Text.wrap(value, valueWidth);
            for (int i = 0; i < lines.size(); i++) {
                String left = i == 0 ? key : " ".repeat(keyWidth);
                line(indent + left + " " + lines.get(i));
            }
        }
    }

    public void histogram(List<Bar> bars) {
        long max = bars.stream().mapToLong(Bar::value).max().orElse(0);
        int labelWidth = bars.stream().mapToInt(b -> b.label().length()).max().orElse(0);
        for (Bar bar : bars) {
            int length = bar.value() == 0 ? 0 : (int) Math.max(1, Math.round((double) bar.value() / max * BAR_WIDTH));
            line("  " + Text.padRight(bar.label(), labelWidth) + " "
                    + Tone.CYAN.paint("█".repeat(length)) + " ".repeat(BAR_WIDTH - length + 1)
                    + Tone.BOLD.paint(String.valueOf(bar.value())));
        }
    }
}

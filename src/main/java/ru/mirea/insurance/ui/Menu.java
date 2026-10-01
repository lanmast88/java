package ru.mirea.insurance.ui;

import java.util.List;

import ru.mirea.insurance.exception.BusinessRuleException;
import ru.mirea.insurance.exception.DataAccessException;
import ru.mirea.insurance.exception.EntityNotFoundException;
import ru.mirea.insurance.ui.console.ConsolePrinter;
import ru.mirea.insurance.ui.console.ConsoleReader;
import ru.mirea.insurance.ui.console.MenuItem;

/**
 * Общий каркас любого меню: цикл, печать пунктов и перехват исключений вокруг
 * всего блока — поэтому ни одна ошибка не роняет программу, а возвращает в меню.
 * Конкретные меню переопределяют три метода: это шаблонный метод и полиморфизм
 * в чистом виде — ConsoleApp вызывает show(), не зная, какое меню перед ним.
 */
public abstract class Menu {
    protected final ConsoleReader reader;
    protected final ConsolePrinter printer;

    protected Menu(ConsoleReader reader, ConsolePrinter printer) {
        this.reader = reader;
        this.printer = printer;
    }

    /** Заголовок раздела в «хлебных крошках». */
    protected abstract String title();

    /** Пункты меню; пункт 0 — выход или возврат назад. */
    protected abstract List<MenuItem> items();

    /** @return false, если пользователь выбрал выход из этого меню */
    protected abstract boolean handle(int choice);

    public void show() {
        boolean running = true;
        while (running) {
            printer.header("СТРАХОВАЯ КОМПАНИЯ", title());
            printer.menuItems(items());
            int choice = reader.readMenu("Пункт меню: ", maxNumber());
            try {
                running = handle(choice);
            } catch (EntityNotFoundException | BusinessRuleException e) {
                printer.error(e.getMessage());
            } catch (DataAccessException e) {
                printer.databaseError(e.getMessage());
            } catch (IllegalArgumentException e) {
                printer.error(e.getMessage());
            }
        }
    }

    private int maxNumber() {
        return items().stream().mapToInt(MenuItem::number).max().orElse(0);
    }
}

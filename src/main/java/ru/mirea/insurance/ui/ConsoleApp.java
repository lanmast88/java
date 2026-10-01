package ru.mirea.insurance.ui;

import java.util.List;

import org.springframework.stereotype.Component;

import ru.mirea.insurance.ui.console.ConsolePrinter;
import ru.mirea.insurance.ui.console.ConsoleReader;
import ru.mirea.insurance.ui.console.InputClosedException;
import ru.mirea.insurance.ui.console.MenuItem;

/** Главный цикл: пока не выбран пункт 0, после каждой операции снова видно меню. */
@Component
public class ConsoleApp extends Menu {
    private final ClientMenu clientMenu;
    private final PolicyMenu policyMenu;
    private final ClaimMenu claimMenu;
    private final ReportMenu reportMenu;

    public ConsoleApp(ConsoleReader reader, ConsolePrinter printer, ClientMenu clientMenu,
                      PolicyMenu policyMenu, ClaimMenu claimMenu, ReportMenu reportMenu) {
        super(reader, printer);
        this.clientMenu = clientMenu;
        this.policyMenu = policyMenu;
        this.claimMenu = claimMenu;
        this.reportMenu = reportMenu;
    }

    public void run(String database) {
        printer.title("СТРАХОВАЯ КОМПАНИЯ");
        printer.info("База данных: " + database);
        try {
            show();
        } catch (InputClosedException e) {
            printer.gap();
            printer.warning("ввод закрыт");
        }
        printer.gap();
        printer.line("Работа завершена.");
    }

    @Override
    protected String title() {
        return "Главное меню";
    }

    @Override
    protected List<MenuItem> items() {
        return List.of(
                MenuItem.of(1, "Клиенты"),
                MenuItem.of(2, "Полисы"),
                MenuItem.of(3, "Страховые случаи"),
                MenuItem.of(4, "Статистика"),
                MenuItem.of(5, "Экспорт полисов в Excel"),
                MenuItem.of(6, "Вывести таблицы базы данных"),
                MenuItem.of(7, "Демонстрация бизнес-правил"),
                MenuItem.of(0, "Выход"));
    }

    @Override
    protected boolean handle(int choice) {
        switch (choice) {
            case 1 -> clientMenu.show();
            case 2 -> policyMenu.show();
            case 3 -> claimMenu.show();
            case 4 -> reportMenu.printStatistics();
            case 5 -> reportMenu.exportPolicies();
            case 6 -> reportMenu.printDatabaseTables();
            case 7 -> reportMenu.printRulesDemo();
            case 0 -> {
                return false;
            }
            default -> printer.error("нет такого пункта меню");
        }
        return true;
    }
}

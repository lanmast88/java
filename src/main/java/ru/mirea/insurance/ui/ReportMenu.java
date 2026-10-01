package ru.mirea.insurance.ui;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import ru.mirea.insurance.model.PolicyType;
import ru.mirea.insurance.repository.TableSnapshot;
import ru.mirea.insurance.service.DatabaseService;
import ru.mirea.insurance.service.ExportService;
import ru.mirea.insurance.service.RuleCheck;
import ru.mirea.insurance.service.RulesDemoService;
import ru.mirea.insurance.service.Statistics;
import ru.mirea.insurance.service.StatisticsService;
import ru.mirea.insurance.ui.console.Bar;
import ru.mirea.insurance.ui.console.ConsolePrinter;
import ru.mirea.insurance.ui.console.ConsoleReader;
import ru.mirea.insurance.ui.console.Field;
import ru.mirea.insurance.util.Formats;
import ru.mirea.insurance.ui.console.TablePrinter;
import ru.mirea.insurance.ui.console.Tone;

/** Отчётные пункты главного меню: статистика, экспорт, таблицы БД, проверка правил. */
@Component
public class ReportMenu {
    private final ConsoleReader reader;
    private final ConsolePrinter printer;
    private final TablePrinter tablePrinter;
    private final StatisticsService statisticsService;
    private final ExportService exportService;
    private final DatabaseService databaseService;
    private final RulesDemoService rulesDemoService;

    public ReportMenu(ConsoleReader reader, ConsolePrinter printer, TablePrinter tablePrinter,
                      StatisticsService statisticsService, ExportService exportService,
                      DatabaseService databaseService, RulesDemoService rulesDemoService) {
        this.reader = reader;
        this.printer = printer;
        this.tablePrinter = tablePrinter;
        this.statisticsService = statisticsService;
        this.exportService = exportService;
        this.databaseService = databaseService;
        this.rulesDemoService = rulesDemoService;
    }

    /** Шесть показателей, посчитанных сервисом на Stream API. */
    public void printStatistics() {
        Statistics statistics = statisticsService.collect();
        printer.header("СТРАХОВАЯ КОМПАНИЯ", "Статистика");
        printer.card(List.of(
                Field.of("Клиентов", String.valueOf(statistics.clients())),
                Field.of("Полисов", String.valueOf(statistics.policies())),
                Field.of("Действующих полисов", String.valueOf(statistics.activePolicies())),
                Field.of("Собрано премий", Formats.rub(statistics.totalPremium())),
                Field.of("Выплачено по убыткам", Formats.rub(statistics.totalPayout())),
                Field.of("Средняя выплата", Formats.rub(statistics.averagePayout()))));
        printer.gap();
        printer.info("Полисов по видам страхования:");
        Map<PolicyType, Long> byType = statistics.policiesByType();
        printer.histogram(List.of(PolicyType.values()).stream()
                .map(type -> Bar.of(type.getTitle(), byType.getOrDefault(type, 0L)))
                .toList());
    }

    public void exportPolicies() {
        printer.header("СТРАХОВАЯ КОМПАНИЯ", "Экспорт в Excel");
        String fileName = reader.readOptional("Имя файла", ExportService.DEFAULT_FILE);
        Path file = exportService.exportPolicies(fileName);
        printer.success("полисы выгружены в " + file);
    }

    /** Пункт задания «Вывести таблицы базы данных». */
    public void printDatabaseTables() {
        printer.header("СТРАХОВАЯ КОМПАНИЯ", "Таблицы базы данных");
        List<String> tables = databaseService.listTables();
        if (tables.isEmpty()) {
            printer.warning("в схеме public нет таблиц");
            return;
        }
        for (int i = 0; i < tables.size(); i++) {
            printer.line("  " + Tone.BOLD.paint((i + 1) + ".") + " " + tables.get(i));
        }
        String table = tables.get(reader.readChoice("Номер таблицы: ", tables.size()) - 1);
        TableSnapshot snapshot = databaseService.dumpTable(table);
        printer.gap();
        tablePrinter.dump(snapshot.name(), snapshot.headers(), snapshot.numeric(), snapshot.rows());
    }

    /** Пять заведомо неверных вызовов подряд: сценарий демонстрации на защите. */
    public void printRulesDemo() {
        printer.header("СТРАХОВАЯ КОМПАНИЯ", "Демонстрация бизнес-правил");
        printer.info("Пять заведомо неверных вызовов. Ничего не записывается, "
                + "программа продолжает работать после каждого.");
        for (RuleCheck check : rulesDemoService.runAll()) {
            printer.gap();
            printer.line(Tone.BOLD.paint("Правило " + check.number() + ". ") + check.rule());
            printer.ref("вызов: " + check.call());
            if (check.rejected()) {
                printer.error(check.outcome());
            } else {
                printer.warning(check.outcome());
            }
        }
    }
}

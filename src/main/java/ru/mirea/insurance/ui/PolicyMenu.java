package ru.mirea.insurance.ui;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import ru.mirea.insurance.model.Client;
import ru.mirea.insurance.model.Policy;
import ru.mirea.insurance.model.PolicyStatus;
import ru.mirea.insurance.model.PolicyType;
import ru.mirea.insurance.service.ClientService;
import ru.mirea.insurance.service.PolicyService;
import ru.mirea.insurance.ui.console.Column;
import ru.mirea.insurance.ui.console.ConsolePrinter;
import ru.mirea.insurance.ui.console.ConsoleReader;
import ru.mirea.insurance.ui.console.Field;
import ru.mirea.insurance.util.Formats;
import ru.mirea.insurance.ui.console.MenuItem;
import ru.mirea.insurance.ui.console.TablePrinter;

/** Раздел «Полисы» — основная сущность системы: CRUD, поиск, фильтры, сортировки, статусы. */
@Component
public class PolicyMenu extends Menu {
    private static final List<Column> COLUMNS = List.of(
            Column.right("ID", 4),
            Column.left("Номер", 19),
            Column.left("Страхователь", 17),
            Column.left("Вид", 10),
            Column.left("Статус", 11),
            Column.right("Страховая сумма", 15),
            Column.left("Окончание", 10));

    private final PolicyService policyService;
    private final ClientService clientService;
    private final TablePrinter tablePrinter;

    public PolicyMenu(ConsoleReader reader, ConsolePrinter printer, PolicyService policyService,
                      ClientService clientService, TablePrinter tablePrinter) {
        super(reader, printer);
        this.policyService = policyService;
        this.clientService = clientService;
        this.tablePrinter = tablePrinter;
    }

    @Override
    protected String title() {
        return "Полисы";
    }

    @Override
    protected List<MenuItem> items() {
        return List.of(
                MenuItem.of(1, "Оформить полис (премия считается по тарифу)"),
                MenuItem.of(2, "Список полисов"),
                MenuItem.of(3, "Найти по ID"),
                MenuItem.of(4, "Поиск по номеру полиса"),
                MenuItem.of(5, "Полисы клиента"),
                MenuItem.of(6, "Изменить условия"),
                MenuItem.of(7, "Удалить полис"),
                MenuItem.of(8, "Фильтр по виду страхования"),
                MenuItem.of(9, "Фильтр по статусу"),
                MenuItem.of(10, "Сортировка по дате окончания"),
                MenuItem.of(11, "Сортировка по страховой сумме"),
                MenuItem.of(12, "Ввести полис в действие (черновик → действует)"),
                MenuItem.of(13, "Расторгнуть полис"),
                MenuItem.of(14, "Отметить полис истёкшим"),
                MenuItem.of(0, "Назад"));
    }

    @Override
    protected boolean handle(int choice) {
        switch (choice) {
            case 1 -> issue();
            case 2 -> printTable(policyService.findAll());
            case 3 -> printCard(policyService.findById(reader.readId("ID полиса: ")));
            case 4 -> printCard(policyService.findByNumber(
                    reader.readRequired("Номер полиса: ", "номер полиса обязателен")));
            case 5 -> printTable(policyService.findByClientId(reader.readId("ID клиента: ")));
            case 6 -> change();
            case 7 -> delete();
            case 8 -> printTable(policyService.filterByType(readType()));
            case 9 -> printTable(policyService.filterByStatus(readStatus()));
            case 10 -> printTable(policyService.sortedByEndDate());
            case 11 -> printTable(policyService.sortedByInsuredSum());
            case 12 -> printCard(policyService.activate(reader.readId("ID полиса: ")),
                    "полис введён в действие");
            case 13 -> printCard(policyService.cancel(reader.readId("ID полиса: ")),
                    "полис расторгнут");
            case 14 -> printCard(policyService.expire(reader.readId("ID полиса: ")),
                    "полис отмечен истёкшим");
            case 0 -> {
                return false;
            }
            default -> printer.error("нет такого пункта меню");
        }
        return true;
    }

    private void issue() {
        int clientId = reader.readId("ID страхователя: ");
        Client client = clientService.findById(clientId);
        printer.info("Страхователь: " + client.getFullName());
        PolicyType type = readType();
        BigDecimal insuredSum = reader.readMoney("Страховая сумма, руб.: ",
                "страховая сумма должна быть больше нуля");
        printer.info("Премия по тарифу " + type.getTitle() + ": "
                + Formats.rub(type.calculatePremium(insuredSum)));
        LocalDate start = reader.readDate("Начало действия (ДД.ММ.ГГГГ): ");
        LocalDate end = reader.readDate("Окончание действия (ДД.ММ.ГГГГ): ");
        printCard(policyService.issue(clientId, type, insuredSum, start, end), "полис оформлен");
    }

    private void change() {
        Policy policy = policyService.findById(reader.readId("ID полиса: "));
        printCard(policy);
        BigDecimal insuredSum = reader.readMoney("Новая страховая сумма, руб.: ",
                "страховая сумма должна быть больше нуля");
        LocalDate start = reader.readDate("Новое начало действия (ДД.ММ.ГГГГ): ");
        LocalDate end = reader.readDate("Новое окончание действия (ДД.ММ.ГГГГ): ");
        printCard(policyService.change(policy.getId(), insuredSum, start, end),
                "условия изменены, премия пересчитана");
    }

    private void delete() {
        Policy policy = policyService.findById(reader.readId("ID полиса: "));
        printCard(policy);
        if (!reader.confirm("Удалить полис " + policy.getNumber() + " вместе с его убытками?")) {
            printer.info("удаление отменено");
            return;
        }
        policyService.delete(policy.getId());
        printer.success("полис удалён");
    }

    private PolicyType readType() {
        return reader.readEnum("Вид страхования:", Arrays.asList(PolicyType.values()),
                type -> type.getTitle() + " — тариф " + type.getBaseRate());
    }

    private PolicyStatus readStatus() {
        return reader.readEnum("Статус полиса:", Arrays.asList(PolicyStatus.values()),
                PolicyStatus::getTitle);
    }

    private void printTable(List<Policy> policies) {
        Map<Integer, String> names = clientNames();
        printer.gap();
        tablePrinter.print(COLUMNS, policies.stream()
                .map(policy -> List.of(
                        String.valueOf(policy.getId()),
                        policy.getNumber(),
                        names.getOrDefault(policy.getClientId(), "—"),
                        policy.getType().getTitle(),
                        policy.getStatus().getTitle(),
                        Formats.money(policy.getInsuredSum()),
                        Formats.date(policy.getEndDate())))
                .toList());
    }

    private void printCard(Policy policy, String message) {
        printer.success(message);
        printCard(policy);
    }

    private void printCard(Policy policy) {
        PolicyType type = policy.getType();
        printer.gap();
        printer.card(List.of(
                Field.of("ID", String.valueOf(policy.getId())),
                Field.of("Номер", policy.getNumber()),
                Field.of("Страхователь", clientNames()
                        .getOrDefault(policy.getClientId(), "id=" + policy.getClientId())),
                Field.of("Вид страхования", type.getTitle() + " (тариф " + type.getBaseRate() + ")"),
                Field.of("Статус", policy.getStatus().getTitle()),
                Field.of("Страховая сумма", Formats.rub(policy.getInsuredSum())),
                Field.of("Премия", Formats.rub(policy.getPremium())),
                Field.of("Франшиза", Formats.rub(type.calculateDeductible(policy.getInsuredSum()))),
                Field.of("Лимит выплаты", Formats.rub(type.maxPayout(policy.getInsuredSum()))),
                Field.of("Период", Formats.period(policy.getStartDate(), policy.getEndDate()))));
    }

    private Map<Integer, String> clientNames() {
        Map<Integer, String> names = new HashMap<>();
        for (Client client : clientService.findAll()) {
            names.put(client.getId(), client.getFullName());
        }
        return names;
    }
}

package ru.mirea.insurance.ui;

import java.util.List;

import org.springframework.stereotype.Component;

import ru.mirea.insurance.model.Client;
import ru.mirea.insurance.service.ClientService;
import ru.mirea.insurance.ui.console.Column;
import ru.mirea.insurance.ui.console.ConsolePrinter;
import ru.mirea.insurance.ui.console.ConsoleReader;
import ru.mirea.insurance.ui.console.Field;
import ru.mirea.insurance.ui.console.MenuItem;
import ru.mirea.insurance.ui.console.TablePrinter;

/** Раздел «Клиенты». Знает только о ClientService — ни Connection, ни SQL здесь нет. */
@Component
public class ClientMenu extends Menu {
    private static final List<Column> COLUMNS = List.of(
            Column.right("ID", 4),
            Column.left("Фамилия", 18),
            Column.left("Имя", 16),
            Column.left("Телефон", 22));

    private final ClientService clientService;
    private final TablePrinter tablePrinter;

    public ClientMenu(ConsoleReader reader, ConsolePrinter printer, ClientService clientService,
                      TablePrinter tablePrinter) {
        super(reader, printer);
        this.clientService = clientService;
        this.tablePrinter = tablePrinter;
    }

    @Override
    protected String title() {
        return "Клиенты";
    }

    @Override
    protected List<MenuItem> items() {
        return List.of(
                MenuItem.of(1, "Добавить клиента"),
                MenuItem.of(2, "Список клиентов"),
                MenuItem.of(3, "Найти по ID"),
                MenuItem.of(4, "Поиск по ФИО"),
                MenuItem.of(5, "Изменить клиента"),
                MenuItem.of(6, "Удалить клиента"),
                MenuItem.of(0, "Назад"));
    }

    @Override
    protected boolean handle(int choice) {
        switch (choice) {
            case 1 -> add();
            case 2 -> printTable(clientService.findAll());
            case 3 -> printCard(clientService.findById(reader.readId("ID клиента: ")));
            case 4 -> search();
            case 5 -> edit();
            case 6 -> delete();
            case 0 -> {
                return false;
            }
            default -> printer.error("нет такого пункта меню");
        }
        return true;
    }

    private void add() {
        String lastName = reader.readRequired("Фамилия: ", "фамилия страхователя обязательна");
        String firstName = reader.readRequired("Имя: ", "имя страхователя обязательно");
        String phone = reader.readRequired("Телефон: ", "телефон страхователя обязателен");
        Client client = clientService.create(lastName, firstName, phone);
        printer.success("клиент добавлен, id=" + client.getId());
        printCard(client);
    }

    private void search() {
        String part = reader.readRequired("Часть фамилии или имени: ", "нужна хотя бы одна буква");
        printTable(clientService.searchByName(part));
    }

    private void edit() {
        Client client = clientService.findById(reader.readId("ID клиента: "));
        printCard(client);
        printer.info("Enter оставляет прежнее значение");
        String lastName = reader.readOptional("Фамилия", client.getLastName());
        String firstName = reader.readOptional("Имя", client.getFirstName());
        String phone = reader.readOptional("Телефон", client.getPhone());
        Client updated = clientService.update(client.getId(), lastName, firstName, phone);
        printer.success("данные клиента изменены");
        printCard(updated);
    }

    private void delete() {
        Client client = clientService.findById(reader.readId("ID клиента: "));
        printCard(client);
        if (!reader.confirm("Удалить клиента " + client.getFullName()
                + " вместе с его закрытыми полисами?")) {
            printer.info("удаление отменено");
            return;
        }
        int policies = clientService.delete(client.getId());
        printer.success("клиент удалён" + (policies > 0
                ? ", вместе с ним удалено закрытых полисов: " + policies : ""));
    }

    private void printTable(List<Client> clients) {
        printer.gap();
        tablePrinter.print(COLUMNS, clients.stream()
                .map(client -> List.of(
                        String.valueOf(client.getId()),
                        client.getLastName(),
                        client.getFirstName(),
                        client.getPhone()))
                .toList());
    }

    private void printCard(Client client) {
        printer.gap();
        printer.card(List.of(
                Field.of("ID", String.valueOf(client.getId())),
                Field.of("Страхователь", client.getFullName()),
                Field.of("Телефон", client.getPhone())));
    }
}

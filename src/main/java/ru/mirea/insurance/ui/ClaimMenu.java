package ru.mirea.insurance.ui;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import ru.mirea.insurance.model.Claim;
import ru.mirea.insurance.model.Policy;
import ru.mirea.insurance.service.ClaimService;
import ru.mirea.insurance.service.PolicyService;
import ru.mirea.insurance.ui.console.Column;
import ru.mirea.insurance.ui.console.ConsolePrinter;
import ru.mirea.insurance.ui.console.ConsoleReader;
import ru.mirea.insurance.ui.console.Field;
import ru.mirea.insurance.util.Formats;
import ru.mirea.insurance.ui.console.MenuItem;
import ru.mirea.insurance.ui.console.TablePrinter;

/** Раздел «Страховые случаи»: весь жизненный цикл SUBMITTED → APPROVED → PAID. */
@Component
public class ClaimMenu extends Menu {
    private static final List<Column> COLUMNS = List.of(
            Column.right("ID", 4),
            Column.left("Полис", 19),
            Column.left("Событие", 10),
            Column.right("Заявлено", 13),
            Column.right("Выплачено", 13),
            Column.left("Статус", 10),
            Column.left("Описание", 20));

    private final ClaimService claimService;
    private final PolicyService policyService;
    private final TablePrinter tablePrinter;

    public ClaimMenu(ConsoleReader reader, ConsolePrinter printer, ClaimService claimService,
                     PolicyService policyService, TablePrinter tablePrinter) {
        super(reader, printer);
        this.claimService = claimService;
        this.policyService = policyService;
        this.tablePrinter = tablePrinter;
    }

    @Override
    protected String title() {
        return "Страховые случаи";
    }

    @Override
    protected List<MenuItem> items() {
        return List.of(
                MenuItem.of(1, "Заявить убыток"),
                MenuItem.of(2, "Убытки по полису"),
                MenuItem.of(3, "Все убытки"),
                MenuItem.of(4, "Найти по ID"),
                MenuItem.of(5, "Одобрить убыток"),
                MenuItem.of(6, "Отклонить убыток"),
                MenuItem.of(7, "Выплатить по убытку"),
                MenuItem.of(0, "Назад"));
    }

    @Override
    protected boolean handle(int choice) {
        switch (choice) {
            case 1 -> submit();
            case 2 -> printTable(claimService.findByPolicyId(reader.readId("ID полиса: ")));
            case 3 -> printTable(claimService.findAll());
            case 4 -> printCard(claimService.findById(reader.readId("ID убытка: ")));
            case 5 -> printCard(claimService.approve(reader.readId("ID убытка: ")), "убыток одобрен");
            case 6 -> printCard(claimService.reject(reader.readId("ID убытка: ")), "убыток отклонён");
            case 7 -> pay();
            case 0 -> {
                return false;
            }
            default -> printer.error("нет такого пункта меню");
        }
        return true;
    }

    private void submit() {
        Policy policy = policyService.findById(reader.readId("ID полиса: "));
        printer.info("Полис " + policy.getNumber() + ", период "
                + Formats.period(policy.getStartDate(), policy.getEndDate())
                + ", статус: " + policy.getStatus().getTitle());
        LocalDate eventDate = reader.readDate("Дата события (ДД.ММ.ГГГГ): ");
        String description = reader.readRequired("Описание случая: ", "описание обязательно");
        BigDecimal claimedAmount = reader.readMoney("Заявленная сумма убытка, руб.: ",
                "сумма убытка должна быть больше нуля");
        printCard(claimService.submit(policy.getId(), eventDate, description, claimedAmount),
                "убыток заявлен");
    }

    private void pay() {
        Claim claim = claimService.requirePayable(reader.readId("ID убытка: "));
        printCard(claim);
        Policy policy = policyService.findById(claim.getPolicyId());
        printer.info("Лимит выплаты по полису " + policy.getNumber() + ": "
                + Formats.rub(policy.getType().maxPayout(policy.getInsuredSum())));
        BigDecimal payout = reader.readMoney("Сумма выплаты, руб.: ",
                "сумма выплаты должна быть больше нуля");
        printCard(claimService.pay(claim.getId(), payout), "выплата проведена");
    }

    private void printTable(List<Claim> claims) {
        Map<Integer, String> numbers = policyNumbers();
        printer.gap();
        tablePrinter.print(COLUMNS, claims.stream()
                .map(claim -> List.of(
                        String.valueOf(claim.getId()),
                        numbers.getOrDefault(claim.getPolicyId(), "id=" + claim.getPolicyId()),
                        Formats.date(claim.getEventDate()),
                        Formats.money(claim.getClaimedAmount()),
                        Formats.money(claim.getPayout()),
                        claim.getStatus().getTitle(),
                        claim.getDescription()))
                .toList());
    }

    private void printCard(Claim claim, String message) {
        printer.success(message);
        printCard(claim);
    }

    private void printCard(Claim claim) {
        printer.gap();
        printer.card(List.of(
                Field.of("ID", String.valueOf(claim.getId())),
                Field.of("Полис", policyNumbers()
                        .getOrDefault(claim.getPolicyId(), "id=" + claim.getPolicyId())),
                Field.of("Дата события", Formats.date(claim.getEventDate())),
                Field.of("Заявлен", Formats.date(claim.getSubmittedAt())),
                Field.of("Описание", claim.getDescription()),
                Field.of("Заявленная сумма", Formats.rub(claim.getClaimedAmount())),
                Field.of("Выплата", Formats.rub(claim.getPayout())),
                Field.of("Статус", claim.getStatus().getTitle())));
    }

    private Map<Integer, String> policyNumbers() {
        Map<Integer, String> numbers = new HashMap<>();
        for (Policy policy : policyService.findAll()) {
            numbers.put(policy.getId(), policy.getNumber());
        }
        return numbers;
    }
}

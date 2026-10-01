package ru.mirea.insurance;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import ru.mirea.insurance.ui.ConsoleApp;
import ru.mirea.insurance.util.DatabaseManager;

/**
 * Точка сборки: Spring создал репозитории, передал их в сервисы, сервисы — в меню,
 * здесь остаётся только запустить главный цикл. Поэтому Main такой короткий.
 */
@Component
@Profile("!test")
public class StartupRunner implements CommandLineRunner {
    private final DatabaseManager databaseManager;
    private final ConsoleApp consoleApp;

    public StartupRunner(DatabaseManager databaseManager, ConsoleApp consoleApp) {
        this.databaseManager = databaseManager;
        this.consoleApp = consoleApp;
    }

    @Override
    public void run(String... args) {
        consoleApp.run(databaseManager.describeConnection());
    }
}

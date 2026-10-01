package ru.mirea.insurance;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import ru.mirea.insurance.util.DatabaseManager;

@Component
@Profile("!test")
public class StartupRunner implements CommandLineRunner {
    private final DatabaseManager databaseManager;

    public StartupRunner(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public void run(String... args) {
        System.out.println("Информационная система «Страховая компания»");
        System.out.println("БД: " + databaseManager.describeConnection());
    }
}

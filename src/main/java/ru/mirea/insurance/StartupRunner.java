package ru.mirea.insurance;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import ru.mirea.insurance.model.Client;
import ru.mirea.insurance.repository.ClientRepository;
import ru.mirea.insurance.util.DatabaseManager;

@Component
@Profile("!test")
public class StartupRunner implements CommandLineRunner {
    private final DatabaseManager databaseManager;
    private final ClientRepository clientRepository;

    public StartupRunner(DatabaseManager databaseManager, ClientRepository clientRepository) {
        this.databaseManager = databaseManager;
        this.clientRepository = clientRepository;
    }

    @Override
    public void run(String... args) {
        System.out.println("Информационная система «Страховая компания»");
        System.out.println("БД: " + databaseManager.describeConnection());
        System.out.println("Клиенты:");
        for (Client client : clientRepository.findAll()) {
            System.out.println("  " + client);
        }
    }
}

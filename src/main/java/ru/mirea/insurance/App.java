package ru.mirea.insurance;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import ru.mirea.insurance.util.DatabaseManager;

/**
 * Точка входа. Spring Boot поднимает контекст, создаёт пул соединений к БД
 * и передаёт управление в консольный слой ui/.
 */
@SpringBootApplication
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    /**
     * CommandLineRunner выполняется один раз после старта контекста —
     * это замена ручной сборки объектов в main(). Сюда подставится ConsoleApp,
     * когда появится меню.
     */
    @Bean
    @Profile("!test")
    CommandLineRunner consoleRunner(DatabaseManager databaseManager) {
        return args -> {
            System.out.println("Информационная система «Страховая компания»");
            System.out.println("БД: " + databaseManager.describeConnection());
        };
    }
}

package ru.mirea.insurance;

import java.sql.Connection;
import java.sql.SQLException;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import ru.mirea.insurance.util.DatabaseManager;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    @Profile("!test")
    CommandLineRunner consoleRunner() {
        return args -> {
            System.out.println("Страховая компания");
            try (Connection connection = DatabaseManager.getConnection()) {
                System.out.println("Подключение OK");
            } catch (SQLException e) {
                System.out.println("Ошибка подключения: " + e.getMessage());
            }
        };
    }
}

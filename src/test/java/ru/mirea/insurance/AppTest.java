package ru.mirea.insurance;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import ru.mirea.insurance.util.DatabaseManager;

/**
 * Проверяет, что контекст поднимается и настройки БД читаются
 * (реального подключения к PostgreSQL тест не требует).
 */
@SpringBootTest
@ActiveProfiles("test")
class AppTest {

    @Autowired
    private DatabaseManager databaseManager;

    @Autowired
    private DataSource dataSource;

    @Test
    void contextLoads() {
        assertThat(databaseManager).isNotNull();
        assertThat(dataSource).isNotNull();
    }
}

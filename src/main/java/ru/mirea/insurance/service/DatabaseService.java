package ru.mirea.insurance.service;

import java.util.List;

import org.springframework.stereotype.Service;

import ru.mirea.insurance.repository.SchemaRepository;
import ru.mirea.insurance.repository.TableSnapshot;

/**
 * Пункт «Вывести таблицы базы данных» на уровне сервиса: меню обращается сюда,
 * а не к репозиторию, поэтому в ui/ нет ни Connection, ни SQL.
 */
@Service
public class DatabaseService {
    private final SchemaRepository schemaRepository;

    public DatabaseService(SchemaRepository schemaRepository) {
        this.schemaRepository = schemaRepository;
    }

    public List<String> listTables() {
        return schemaRepository.listTables();
    }

    public TableSnapshot dumpTable(String table) {
        return schemaRepository.dumpTable(table);
    }
}

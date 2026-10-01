package ru.mirea.insurance.repository;

import java.util.List;

/**
 * Содержимое произвольной таблицы в виде строк текста: репозиторий отдаёт данные,
 * печатает их слой ui. Поэтому пункт «вывести таблицы БД» не тащит java.sql в меню.
 *
 * @param name    имя таблицы
 * @param headers имена колонок из ResultSetMetaData
 * @param numeric для каждой колонки — числовая ли она (нужно для выравнивания вправо)
 * @param rows    значения, уже приведённые к строкам
 */
public record TableSnapshot(String name, List<String> headers, List<Boolean> numeric,
                            List<List<String>> rows) {
}

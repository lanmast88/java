package ru.mirea.insurance.util;

import java.sql.Connection;
import java.sql.SQLException;

public interface ConnectionSource {

    Connection getConnection() throws SQLException;
}

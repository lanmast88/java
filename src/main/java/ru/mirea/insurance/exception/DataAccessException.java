package ru.mirea.insurance.exception;

/** Ошибка слоя доступа к данным: обёртка над SQLException, чтобы верхние слои не зависели от java.sql. */
public class DataAccessException extends RuntimeException {
    public DataAccessException(String message) {
        super(message);
    }

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}

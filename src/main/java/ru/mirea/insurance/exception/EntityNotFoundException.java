package ru.mirea.insurance.exception;

/** Запись с указанным идентификатором отсутствует в базе. */
public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String message) {
        super(message);
    }

    public EntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

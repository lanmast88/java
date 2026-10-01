package ru.mirea.insurance.model;

/** Страхователь: человек, с которым заключается договор страхования. */
public class Client {
    private final int id;
    private String lastName;
    private String firstName;
    private String phone;

    public Client(int id, String lastName, String firstName, String phone) {
        if (id < 0) {
            throw new IllegalArgumentException("Идентификатор клиента не может быть отрицательным");
        }
        this.id = id;
        this.lastName = requireText(lastName, "Фамилия страхователя обязательна");
        this.firstName = requireText(firstName, "Имя страхователя обязательно");
        this.phone = requireText(phone, "Телефон страхователя обязателен");
    }

    public Client(String lastName, String firstName, String phone) {
        this(0, lastName, firstName, phone);
    }

    public int getId() {
        return id;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = requireText(lastName, "Фамилия страхователя обязательна");
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = requireText(firstName, "Имя страхователя обязательно");
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = requireText(phone, "Телефон страхователя обязателен");
    }

    public String getFullName() {
        return lastName + " " + firstName;
    }

    /** Проверка стоит и в конструкторе, и в сеттерах, поэтому невалидного клиента не существует. */
    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    @Override
    public String toString() {
        return String.format("#%d %s, тел. %s", id, getFullName(), phone);
    }
}

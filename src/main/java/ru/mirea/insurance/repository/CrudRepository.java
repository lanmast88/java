package ru.mirea.insurance.repository;

import java.util.List;
import java.util.Optional;

public interface CrudRepository<T> {

    int save(T entity);

    Optional<T> findById(int id);

    List<T> findAll();

    boolean update(T entity);

    boolean delete(int id);
}
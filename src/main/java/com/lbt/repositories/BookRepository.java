package com.lbt.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lbt.entities.Book;

import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, String> {

    default Book findByIsbn(String isbn) {
        return findById(isbn).orElse(null);
    }

    Optional<Book> findByTitle(String title);

    default boolean existsByIsbn(String isbn) {
        return existsById(isbn);
    }

    default boolean delete(String isbn) {
        if (existsById(isbn)) {
            deleteById(isbn);
            return true;
        }
        return false;
    }
}
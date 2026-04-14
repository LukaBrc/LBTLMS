package com.lbt.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.lbt.entities.Book;

import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    Book findByIsbn(String isbn);

    Optional<Book> findByTitle(String title);

    boolean existsByIsbn(String isbn);

    @Transactional
    void deleteByIsbn(String isbn);

    default boolean delete(String isbn) {
        if (existsByIsbn(isbn)) {
            deleteByIsbn(isbn);
            return true;
        }
        return false;
    }
}

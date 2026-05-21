package com.lbt.repositories;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.lbt.entities.Book;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    Book findByIsbn(String isbn);

    Book findByIsbnAndDeletedFalse(String isbn);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Book b WHERE b.isbn = :isbn AND b.deleted = false")
    Book findByIsbnAndDeletedFalseForUpdate(@Param("isbn") String isbn);

    Optional<Book> findByTitle(String title);

    boolean existsByIsbn(String isbn);

    boolean existsByIsbnAndDeletedFalse(String isbn);

    List<Book> findAllByDeletedFalse();

    @Transactional
    void deleteByIsbn(String isbn);
}

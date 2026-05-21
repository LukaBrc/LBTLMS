package com.lbt.services;

import com.lbt.entities.Book;
import com.lbt.repositories.BookRepository;
import com.lbt.services.cache.AbstractEntityCache;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BookCache extends AbstractEntityCache<Book, String> {

    private final BookRepository bookRepository;

    public BookCache(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    protected List<Book> loadAll() {
        return bookRepository.findAll();
    }

    @Override
    protected String extractKey(Book book) {
        return book.getIsbn();
    }

    @Override
    @Scheduled(fixedRateString = "${book.cache.refresh-interval-ms:300000}")
    public void refresh() {
        super.refresh();
    }
}

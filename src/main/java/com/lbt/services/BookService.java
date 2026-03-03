package com.lbt.services;

import org.springframework.stereotype.Service;

import com.lbt.entities.Book;
import com.lbt.repositories.BookRepository;

import java.util.List;

@Service
public class BookService {

    private final BookRepository bookRepository;

	private final ValidationHandler validationHandler;
	
    public BookService(BookRepository bookRepository, ValidationHandler validationHandler) {
        this.bookRepository = bookRepository;
        this.validationHandler = validationHandler;
    }

    public void addBook(Book book) {
    	validationHandler.validate(book);
    	bookRepository.save(book);
       
    }

    public Book findByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn);
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public void removeBook(String isbn) {
        bookRepository.delete(isbn);
    }
}
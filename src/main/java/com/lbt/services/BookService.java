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

    public Book updateBook(String isbn, String title, String author, String genre, int totalCopies) {
        Book book = bookRepository.findByIsbn(isbn);
        if (book == null) {
            throw new IllegalArgumentException("Book not found with ISBN: " + isbn);
        }
        book.setTitle(title);
        book.setAuthor(author);
        book.setGenre(genre);
        book.setTotalCopies(totalCopies);
        validationHandler.validate(book);
        return bookRepository.save(book);
    }

    public void removeBook(String isbn) {
        bookRepository.delete(isbn);
    }
}
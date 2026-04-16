package com.lbt.services;

import org.springframework.stereotype.Service;

import com.lbt.entities.Author;
import com.lbt.entities.Book;
import com.lbt.repositories.BookRepository;

import java.util.List;

@Service
public class BookService {

    private final BookRepository bookRepository;

	private final ValidationHandler validationHandler;

	private final AuthorService authorService;
	
    public BookService(BookRepository bookRepository, ValidationHandler validationHandler, AuthorService authorService) {
        this.bookRepository = bookRepository;
        this.validationHandler = validationHandler;
        this.authorService = authorService;
    }

    public void addBook(Book book) {
    	validationHandler.validate(book);
    	if (bookRepository.existsByIsbn(book.getIsbn())) {
    	    throw new IllegalArgumentException("ISBN already exists");
    	}
    	bookRepository.save(book);
       
    }

    public Book findByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn);
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Book updateBook(String isbn, String title, Long authorId, String genre, int totalCopies) {
        Book book = bookRepository.findByIsbn(isbn);
        if (book == null) {
            throw new IllegalArgumentException("Book not found with ISBN: " + isbn);
        }
        Author author = authorService.getAuthorById(authorId);
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
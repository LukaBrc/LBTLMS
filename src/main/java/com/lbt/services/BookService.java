package com.lbt.services;

import org.springframework.stereotype.Service;

import com.lbt.entities.Author;
import com.lbt.entities.Book;
import com.lbt.repositories.BookRepository;
import com.lbt.validation.ValidationError;

import java.util.List;

@Service
public class BookService {

    private final BookRepository bookRepository;

    private final AuthorService authorService;

    private final BookCache bookCache;

    public BookService(BookRepository bookRepository, AuthorService authorService, BookCache bookCache) {
        this.bookRepository = bookRepository;
        this.authorService = authorService;
        this.bookCache = bookCache;
    }

    private void validateBook(Book book) {
        if (book == null) {
            throw new IllegalArgumentException("Book must not be null.");
        }
        List<ValidationError> errors = book.getValidationErrors();
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(errors.get(0).message());
        }
    }

    public void addBook(Book book) {
        validateBook(book);
        if (bookRepository.existsByIsbn(book.getIsbn())) {
            throw new IllegalArgumentException("ISBN already exists");
        }
        Book savedBook = bookRepository.save(book);
        bookCache.put(savedBook);
    }

    public Book findByIsbn(String isbn) {
        return bookCache.getById(isbn).orElse(null);
    }

    public List<Book> getAllBooks() {
        return bookCache.getAll();
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
        validateBook(book);
        Book savedBook = bookRepository.save(book);
        bookCache.put(savedBook);
        return savedBook;
    }

    public void removeBook(String isbn) {
        bookRepository.delete(isbn);
        bookCache.evict(isbn);
    }
}
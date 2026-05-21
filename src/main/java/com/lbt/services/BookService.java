package com.lbt.services;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.lbt.entities.Author;
import com.lbt.entities.Book;
import com.lbt.exceptions.ResourceConflictException;
import com.lbt.exceptions.ResourceNotFoundException;
import com.lbt.repositories.BookRepository;
import com.lbt.repositories.BorrowTransactionRepository;
import com.lbt.validation.ValidationHandler;
import com.lbt.validation.ValidationHandlerResolver;
import com.lbt.validation.ValidationError;

import java.util.List;

@Service
public class BookService {

    private final BookRepository bookRepository;

    private final AuthorService authorService;

    private final BookCache bookCache;

    private final BorrowTransactionRepository borrowTransactionRepository;

    private final ValidationHandler validationHandler;

    public BookService(BookRepository bookRepository,
                       AuthorService authorService,
                       BookCache bookCache,
                       BorrowTransactionRepository borrowTransactionRepository) {
        this(bookRepository, authorService, bookCache, borrowTransactionRepository, ValidationHandlerResolver.get());
    }

    @Autowired
    public BookService(BookRepository bookRepository,
                       AuthorService authorService,
                       BookCache bookCache,
                       BorrowTransactionRepository borrowTransactionRepository,
                       ValidationHandler validationHandler) {
        this.bookRepository = bookRepository;
        this.authorService = authorService;
        this.bookCache = bookCache;
        this.borrowTransactionRepository = borrowTransactionRepository;
        this.validationHandler = validationHandler;
    }

    private void validateBook(Book book) {
        if (book == null) {
            throw new IllegalArgumentException("Book must not be null.");
        }
        List<ValidationError> errors = validationHandler.getValidationErrors(book);
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
        Book book = bookRepository.findByIsbnAndDeletedFalse(isbn);
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

    @Transactional
    public void removeBook(String isbn) {
        Book book = bookRepository.findByIsbnAndDeletedFalseForUpdate(isbn);
        if (book == null) {
            throw new ResourceNotFoundException("Book not found with ISBN: " + isbn);
        }
        if (borrowTransactionRepository.existsByBookIsbnAndReturnDateIsNull(isbn)) {
            throw new ResourceConflictException("Book with ISBN " + isbn + " cannot be deleted while it has active borrows.");
        }
        book.setDeleted(true);
        bookRepository.save(book);
        bookCache.evict(isbn);
    }
}
package com.lbt.controllers;

import com.lbt.dto.BookRequest;
import com.lbt.dto.BookResponse;
import com.lbt.entities.Author;
import com.lbt.entities.Book;
import com.lbt.services.AuthorService;
import com.lbt.services.BookService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/books")
@CrossOrigin(origins = "*")
public class BookController {

    private final BookService bookService;
    private final AuthorService authorService;

    public BookController(BookService bookService, AuthorService authorService) {
        this.bookService = bookService;
        this.authorService = authorService;
    }

    @PostMapping
    public ResponseEntity<Void> addBook(@Valid @RequestBody BookRequest request) {
        Author author = authorService.getAuthorById(request.getAuthorId());
        var book = Book.builder()
                .title(request.getTitle())
                .author(author)
                .genre(request.getGenre())
                .isbn(request.getIsbn())
                .totalCopies(request.getTotalCopies())
                .build();

        bookService.addBook(book);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<List<BookResponse>> getAllBooks() {
        List<Book> books = bookService.getAllBooks();
        List<BookResponse> response = books.stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{isbn}")
    public ResponseEntity<BookResponse> getBook(@PathVariable String isbn) {
        Book book = bookService.findByIsbn(isbn);
        return book != null 
                ? ResponseEntity.ok(toResponse(book)) 
                : ResponseEntity.notFound().build();
    }

    @PutMapping("/{isbn}")
    public ResponseEntity<BookResponse> updateBook(@PathVariable String isbn,
                                                   @Valid @RequestBody BookRequest request) {
        Book updatedBook = bookService.updateBook(isbn, request.getTitle(), request.getAuthorId(),
                request.getGenre(), request.getTotalCopies());
        return updatedBook != null
                ? ResponseEntity.ok(toResponse(updatedBook))
                : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{isbn}")
    public ResponseEntity<Void> removeBook(@PathVariable String isbn) {
        bookService.removeBook(isbn);
        return ResponseEntity.noContent().build();
    }

    private BookResponse toResponse(Book book) {
        BookResponse r = new BookResponse();
        r.setIsbn(book.getIsbn());
        r.setTitle(book.getTitle());
        r.setAuthorId(book.getAuthor().getId());
        r.setAuthorName(book.getAuthor().getName());
        r.setGenre(book.getGenre());
        r.setTotalCopies(book.getTotalCopies());
        r.setAvailableCopies(book.getAvailableCopies());
        return r;
    }
}
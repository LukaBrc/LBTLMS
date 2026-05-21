package com.lbt;

import com.lbt.entities.Author;
import com.lbt.entities.Book;
import com.lbt.exceptions.ResourceConflictException;
import com.lbt.exceptions.ResourceNotFoundException;
import com.lbt.repositories.BookRepository;
import com.lbt.repositories.BorrowTransactionRepository;
import com.lbt.services.AuthorService;
import com.lbt.services.BookCache;
import com.lbt.services.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuthorService authorService;

    @Mock
    private BorrowTransactionRepository borrowTransactionRepository;

    private BookCache bookCache;

    private BookService bookService;

    private Author sampleAuthor;
    private Book sampleBook;

    @BeforeEach
    void setUp() {
        BookRepository cacheRepo = mock(BookRepository.class);
        when(cacheRepo.findAllByDeletedFalse()).thenReturn(Collections.emptyList());
        bookCache = new BookCache(cacheRepo);
        bookCache.init();
        bookService = new BookService(bookRepository, authorService, bookCache, borrowTransactionRepository);
        sampleAuthor = Author.builder().id(1L).name("Joshua Bloch").build();
        sampleBook = Book.builder()
                .isbn("978-0-13-468599-1")
                .title("Effective Java")
                .author(sampleAuthor)
                .genre("Programming")
                .totalCopies(5)
                .availableCopies(5)
                .build();
    }

    @Test
    void addBook_savesBookAfterValidation() {
        when(bookRepository.save(sampleBook)).thenReturn(sampleBook);
        bookService.addBook(sampleBook);
        verify(bookRepository).save(sampleBook);
    }

    @Test
    void addBook_throwsWhenBookIsNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                bookService.addBook(null));
        assertEquals("Book must not be null.", ex.getMessage());
    }

    @Test
    void addBook_throwsWhenTitleIsBlank() {
        sampleBook.setTitle("   ");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                bookService.addBook(sampleBook));
        assertEquals("Book title must not be empty.", ex.getMessage());
    }

    @Test
    void updateBook_updatesFieldsAndSaves() {
        Author newAuthor = Author.builder().id(2L).name("New Author").build();
        when(bookRepository.findByIsbnAndDeletedFalse("978-0-13-468599-1")).thenReturn(sampleBook);
        when(bookRepository.save(any(Book.class))).thenReturn(sampleBook);
        when(authorService.getAuthorById(2L)).thenReturn(newAuthor);

        Book result = bookService.updateBook("978-0-13-468599-1", "New Title", 2L, "New Genre", 10);

        assertNotNull(result);
        assertEquals("New Title", sampleBook.getTitle());
        assertEquals(newAuthor, sampleBook.getAuthor());
        assertEquals("New Genre", sampleBook.getGenre());
        verify(bookRepository).save(sampleBook);
    }

    @Test
    void updateBook_preservesBorrowedCountWhenTotalCopiesChanges() {
        Author newAuthor = Author.builder().id(2L).name("New Author").build();
        sampleBook.setAvailableCopies(2); // 3 borrowed from totalCopies=5
        when(bookRepository.findByIsbnAndDeletedFalse("978-0-13-468599-1")).thenReturn(sampleBook);
        when(bookRepository.save(any(Book.class))).thenReturn(sampleBook);
        when(authorService.getAuthorById(2L)).thenReturn(newAuthor);

        bookService.updateBook("978-0-13-468599-1", "New Title", 2L, "New Genre", 10);

        assertEquals(10, sampleBook.getTotalCopies());
        assertEquals(7, sampleBook.getAvailableCopies());
    }

    @Test
    void updateBook_throwsWhenBookNotFound() {
        when(bookRepository.findByIsbnAndDeletedFalse("UNKNOWN")).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () ->
                bookService.updateBook("UNKNOWN", "T", 1L, "G", 1));
    }

    @Test
    void removeBook_deletesByIsbnWhenBookExists() {
        when(bookRepository.findByIsbnAndDeletedFalseForUpdate("978-0-13-468599-1")).thenReturn(sampleBook);
        when(bookRepository.save(any(Book.class))).thenReturn(sampleBook);
        when(borrowTransactionRepository.existsByBookIsbnAndReturnDateIsNull("978-0-13-468599-1")).thenReturn(false);

        bookService.removeBook("978-0-13-468599-1");

        assertTrue(sampleBook.isDeleted());
        verify(bookRepository).save(sampleBook);
    }

    @Test
    void removeBook_throwsWhenBookNotFound() {
        when(bookRepository.findByIsbnAndDeletedFalseForUpdate("UNKNOWN")).thenReturn(null);

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () ->
                bookService.removeBook("UNKNOWN"));

        assertEquals("Book not found with ISBN: UNKNOWN", ex.getMessage());
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void removeBook_throwsConflictWhenBookHasActiveBorrows() {
        when(bookRepository.findByIsbnAndDeletedFalseForUpdate("978-0-13-468599-1")).thenReturn(sampleBook);
        when(borrowTransactionRepository.existsByBookIsbnAndReturnDateIsNull("978-0-13-468599-1")).thenReturn(true);

        ResourceConflictException ex = assertThrows(ResourceConflictException.class, () ->
                bookService.removeBook("978-0-13-468599-1"));

        assertEquals("Book with ISBN 978-0-13-468599-1 cannot be deleted while it has active borrows.", ex.getMessage());
        verify(bookRepository, never()).save(any(Book.class));
    }
}

package com.lbt;

import com.lbt.entities.Author;
import com.lbt.entities.Book;
import com.lbt.repositories.BookRepository;
import com.lbt.services.AuthorService;
import com.lbt.services.BookService;
import com.lbt.services.ValidationHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private ValidationHandler validationHandler;

    @Mock
    private AuthorService authorService;

    private BookService bookService;

    private Author sampleAuthor;
    private Book sampleBook;

    @BeforeEach
    void setUp() {
        bookService = new BookService(bookRepository, validationHandler, authorService);
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
        bookService.addBook(sampleBook);
        verify(validationHandler).validate(sampleBook);
        verify(bookRepository).save(sampleBook);
    }

    @Test
    void updateBook_updatesFieldsAndSaves() {
        Author newAuthor = Author.builder().id(2L).name("New Author").build();
        when(bookRepository.findByIsbn("978-0-13-468599-1")).thenReturn(sampleBook);
        when(bookRepository.save(any(Book.class))).thenReturn(sampleBook);
        when(authorService.getAuthorById(2L)).thenReturn(newAuthor);

        Book result = bookService.updateBook("978-0-13-468599-1", "New Title", 2L, "New Genre", 10);

        assertNotNull(result);
        assertEquals("New Title", sampleBook.getTitle());
        assertEquals(newAuthor, sampleBook.getAuthor());
        assertEquals("New Genre", sampleBook.getGenre());
        verify(validationHandler).validate(sampleBook);
        verify(bookRepository).save(sampleBook);
    }

    @Test
    void updateBook_throwsWhenBookNotFound() {
        when(bookRepository.findByIsbn("UNKNOWN")).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () ->
                bookService.updateBook("UNKNOWN", "T", 1L, "G", 1));
    }

    @Test
    void removeBook_delegatesToRepository() {
        bookService.removeBook("978-0-13-468599-1");
        verify(bookRepository).delete("978-0-13-468599-1");
    }
}

package com.lbt;

import com.lbt.dto.BookRequest;
import com.lbt.dto.BookResponse;
import com.lbt.entities.Author;
import com.lbt.entities.Book;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for modified Book components after Author FK migration.
 * Validates: Requirements 2.3, 2.4
 */
class ModifiedBookComponentsTest {

    // --- Book entity provides access to Author object (Req 2.3) ---

    @Test
    void bookProvidesAccessToAssociatedAuthorObject() {
        Author author = Author.builder().id(42L).name("Joshua Bloch").build();
        Book book = Book.builder()
                .id(1L)
                .isbn("978-0134685991")
                .title("Effective Java")
                .author(author)
                .genre("Programming")
                .totalCopies(5)
                .availableCopies(5)
                .build();

        assertNotNull(book.getAuthor());
        assertEquals(42L, book.getAuthor().getId());
        assertEquals("Joshua Bloch", book.getAuthor().getName());
    }

    // --- BookResponse includes authorId and authorName (Req 2.4) ---

    @Test
    void bookResponseIncludesAuthorIdAndAuthorName() {
        BookResponse response = new BookResponse();
        response.setAuthorId(7L);
        response.setAuthorName("Martin Fowler");
        response.setIsbn("978-0201633610");
        response.setTitle("Refactoring");
        response.setGenre("Software Engineering");
        response.setTotalCopies(3);
        response.setAvailableCopies(3);

        assertEquals(7L, response.getAuthorId());
        assertEquals("Martin Fowler", response.getAuthorName());
    }

    // --- BookRequest uses authorId instead of author string (Req 2.1) ---

    @Test
    void bookRequestUsesAuthorIdInsteadOfAuthorString() {
        BookRequest request = new BookRequest();
        request.setAuthorId(10L);

        assertEquals(10L, request.getAuthorId());

        // Verify there is no getAuthor() method returning String
        boolean hasStringGetAuthor = false;
        for (Method method : BookRequest.class.getDeclaredMethods()) {
            if (method.getName().equals("getAuthor") && method.getReturnType() == String.class) {
                hasStringGetAuthor = true;
                break;
            }
        }
        assertFalse(hasStringGetAuthor, "BookRequest should not have a getAuthor() method returning String");
    }
}

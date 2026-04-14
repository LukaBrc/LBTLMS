package com.lbt;

import com.lbt.controllers.BookController;
import com.lbt.controllers.GlobalExceptionHandler;
import com.lbt.entities.Book;
import com.lbt.services.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({BookController.class, GlobalExceptionHandler.class})
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    @Test
    void postBook_returns201() throws Exception {
        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"title":"Effective Java","author":"Bloch","isbn":"978-1","genre":"Programming","totalCopies":5}
                            """))
                .andExpect(status().isCreated());
        verify(bookService).addBook(any(Book.class));
    }

    @Test
    void postBook_returns400WhenTitleMissing() throws Exception {
        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"author":"Bloch","isbn":"978-1","genre":"Programming","totalCopies":5}
                            """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllBooks_returns200WithList() throws Exception {
        Book book = Book.builder().isbn("978-1").title("T").author("A").genre("G").totalCopies(3).availableCopies(3).build();
        when(bookService.getAllBooks()).thenReturn(List.of(book));

        mockMvc.perform(get("/api/v1/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].isbn").value("978-1"));
    }

    @Test
    void getBookByIsbn_returns200() throws Exception {
        Book book = Book.builder().isbn("978-1").title("T").author("A").genre("G").totalCopies(3).availableCopies(3).build();
        when(bookService.findByIsbn("978-1")).thenReturn(book);

        mockMvc.perform(get("/api/v1/books/978-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("T"));
    }

    @Test
    void getBookByIsbn_returns404WhenNotFound() throws Exception {
        when(bookService.findByIsbn("UNKNOWN")).thenReturn(null);

        mockMvc.perform(get("/api/v1/books/UNKNOWN"))
                .andExpect(status().isNotFound());
    }

    @Test
    void putBook_returns200WithUpdatedBook() throws Exception {
        Book updated = Book.builder().isbn("978-1").title("New").author("New A").genre("New G").totalCopies(10).availableCopies(10).build();
        when(bookService.updateBook(eq("978-1"), eq("New"), eq("New A"), eq("New G"), eq(10))).thenReturn(updated);

        mockMvc.perform(put("/api/v1/books/978-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"title":"New","author":"New A","isbn":"978-1","genre":"New G","totalCopies":10}
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New"));
    }

    @Test
    void putBook_returns400WhenBookNotFound() throws Exception {
        when(bookService.updateBook(eq("UNKNOWN"), any(), any(), any(), anyInt()))
                .thenThrow(new IllegalArgumentException("Book not found with ISBN: UNKNOWN"));

        mockMvc.perform(put("/api/v1/books/UNKNOWN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"title":"T","author":"A","isbn":"UNKNOWN","genre":"G","totalCopies":1}
                            """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteBook_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/books/978-1"))
                .andExpect(status().isNoContent());
        verify(bookService).removeBook("978-1");
    }
}

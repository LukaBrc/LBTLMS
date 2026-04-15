package com.lbt;

import com.lbt.controllers.AuthorController;
import com.lbt.controllers.GlobalExceptionHandler;
import com.lbt.entities.Author;
import com.lbt.services.AuthorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({AuthorController.class, GlobalExceptionHandler.class})
class AuthorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthorService authorService;

    @Test
    void postAuthor_returns201WithValidRequest() throws Exception {
        Author author = Author.builder().id(1L).name("Joshua Bloch").build();
        when(authorService.createAuthor("Joshua Bloch")).thenReturn(author);

        mockMvc.perform(post("/api/v1/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"name":"Joshua Bloch"}
                            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Joshua Bloch"));
    }

    @Test
    void postAuthor_returns400WithBlankName() throws Exception {
        mockMvc.perform(post("/api/v1/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"name":""}
                            """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllAuthors_returns200WithList() throws Exception {
        Author a1 = Author.builder().id(1L).name("Author A").build();
        Author a2 = Author.builder().id(2L).name("Author B").build();
        when(authorService.getAllAuthors()).thenReturn(List.of(a1, a2));

        mockMvc.perform(get("/api/v1/authors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Author A"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Author B"));
    }

    @Test
    void getAuthorById_returns200WhenFound() throws Exception {
        Author author = Author.builder().id(1L).name("Author A").build();
        when(authorService.getAuthorById(1L)).thenReturn(author);

        mockMvc.perform(get("/api/v1/authors/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Author A"));
    }

    @Test
    void getAuthorById_returns404WhenNotFound() throws Exception {
        when(authorService.getAuthorById(999L))
                .thenThrow(new IllegalArgumentException("Author not found with id: 999"));

        mockMvc.perform(get("/api/v1/authors/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void putAuthor_returns200WithValidRequest() throws Exception {
        Author updated = Author.builder().id(1L).name("Updated Name").build();
        when(authorService.updateAuthor(eq(1L), eq("Updated Name"))).thenReturn(updated);

        mockMvc.perform(put("/api/v1/authors/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"name":"Updated Name"}
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    void putAuthor_returns404WhenNotFound() throws Exception {
        when(authorService.updateAuthor(eq(999L), eq("Any Name")))
                .thenThrow(new IllegalArgumentException("Author not found with id: 999"));

        mockMvc.perform(put("/api/v1/authors/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"name":"Any Name"}
                            """))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteAuthor_returns204WhenFound() throws Exception {
        doNothing().when(authorService).deleteAuthor(1L);

        mockMvc.perform(delete("/api/v1/authors/1"))
                .andExpect(status().isNoContent());
        verify(authorService).deleteAuthor(1L);
    }

    @Test
    void deleteAuthor_returns404WhenNotFound() throws Exception {
        doThrow(new IllegalArgumentException("Author not found with id: 999"))
                .when(authorService).deleteAuthor(999L);

        mockMvc.perform(delete("/api/v1/authors/999"))
                .andExpect(status().isNotFound());
    }
}

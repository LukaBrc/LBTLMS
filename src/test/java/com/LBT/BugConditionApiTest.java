package com.lbt;

import com.lbt.controllers.BookController;
import com.lbt.controllers.BorrowController;
import com.lbt.controllers.MemberController;
import com.lbt.controllers.GlobalExceptionHandler;
import com.lbt.entities.Author;
import com.lbt.entities.Book;
import com.lbt.entities.Member;
import com.lbt.services.AuthorService;
import com.lbt.services.BookService;
import com.lbt.services.BorrowTransactionService;
import com.lbt.services.MemberService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Bug Condition Exploration Tests — API endpoint layer (Tests 5-8).
 *
 * Uses @WebMvcTest with mocked services to test controller routing bugs
 * without loading the full Spring context (avoids Bug 5 blocking).
 *
 * These tests encode the EXPECTED (correct) behavior.
 * On UNFIXED code, they are EXPECTED TO FAIL — failure confirms the bugs exist.
 *
 * Validates: Requirements 1.6, 1.7, 1.8, 1.9
 */
@WebMvcTest({MemberController.class, BorrowController.class,
             BookController.class, GlobalExceptionHandler.class})
@ActiveProfiles("test")
@DisplayName("Bug Condition Exploration — API Endpoints")
class BugConditionApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberService memberService;

    @MockitoBean
    private BorrowTransactionService borrowTransactionService;

    @MockitoBean
    private BookService bookService;

    @MockitoBean
    private AuthorService authorService;

    @Test
    @DisplayName("Test 5 — Bug 6: GET /api/v1/members and /api/v1/borrows should return 200")
    void consistentApiVersioning() throws Exception {
        // Validates: Requirements 1.6
        // Bug 6: MemberController uses /api/members, BorrowController uses /api/borrows
        // instead of /api/v1/members and /api/v1/borrows

        when(memberService.getAllMembers()).thenReturn(List.of());
        when(borrowTransactionService.getOverdueBooks()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/members"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/borrows/overdue"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Test 6 — Bug 7: PUT /api/v1/members/{memberId} should return 200")
    void memberControllerPutEndpoint() throws Exception {
        // Validates: Requirements 1.7
        // Bug 7: MemberController has no PUT endpoint
        // On unfixed code, returns 405 Method Not Allowed

        Member mockMember = new Member();
        mockMember.setMemberId("M001");
        mockMember.setName("Updated Name");
        mockMember.setContact("updated@example.com");
        when(memberService.updateMember(anyString(), anyString(), anyString())).thenReturn(mockMember);

        String requestBody = """
                {
                    "name": "Updated Name",
                    "memberId": "M001",
                    "contact": "updated@example.com"
                }
                """;

        mockMvc.perform(put("/api/v1/members/M001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Test 7 — Bug 8: DELETE /api/v1/members/{memberId} should return 204")
    void memberControllerDeleteEndpoint() throws Exception {
        // Validates: Requirements 1.8
        // Bug 8: MemberController has no DELETE endpoint
        // On unfixed code, returns 405 Method Not Allowed

        mockMvc.perform(delete("/api/v1/members/M001"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Test 8 — Bug 9: PUT /api/v1/books/{isbn} should return 200")
    void bookControllerPutEndpoint() throws Exception {
        // Validates: Requirements 1.9
        // Bug 9: BookController has no PUT endpoint
        // On unfixed code, returns 405 Method Not Allowed

        Author mockAuthor = Author.builder().id(1L).name("Updated Author").build();
        Book mockBook = Book.builder()
                .isbn("978-0-13-468599-1")
                .title("Updated Title")
                .author(mockAuthor)
                .genre("Updated Genre")
                .totalCopies(10)
                .build();
        when(bookService.updateBook(anyString(), anyString(), anyLong(), anyString(), anyInt())).thenReturn(mockBook);

        String requestBody = """
                {
                    "isbn": "978-0-13-468599-1",
                    "title": "Updated Title",
                    "authorId": 1,
                    "genre": "Updated Genre",
                    "totalCopies": 10
                }
                """;

        mockMvc.perform(put("/api/v1/books/978-0-13-468599-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());
    }
}

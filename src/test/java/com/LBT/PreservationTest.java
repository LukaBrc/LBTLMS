package com.lbt;

import com.lbt.controllers.BookController;
import com.lbt.controllers.BorrowController;
import com.lbt.controllers.GlobalExceptionHandler;
import com.lbt.controllers.MemberController;
import com.lbt.dto.BookResponse;
import com.lbt.dto.MemberResponse;
import com.lbt.entities.Book;
import com.lbt.entities.BorrowTransaction;
import com.lbt.entities.Member;
import com.lbt.services.BookService;
import com.lbt.services.BorrowTransactionService;
import com.lbt.services.MemberService;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Preservation Tests — Capture existing working behavior on UNFIXED code.
 *
 * These tests MUST PASS on the current unfixed code to establish a baseline.
 * After the bugfix, these tests ensure no regressions are introduced.
 *
 * Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 3.8, 3.9, 3.10, 3.11
 */
class PreservationTest {

    /**
     * Test 1 — BorrowTransaction entity preservation (Req 3.1)
     * Verify BorrowTransaction continues to use @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
     * with its existing long id field. Uses reflection to verify annotations.
     */
    @Nested
    @DisplayName("Test 1 — BorrowTransaction entity preservation (Req 3.1)")
    class BorrowTransactionEntityPreservation {

        @Test
        @DisplayName("BorrowTransaction should have @Id @GeneratedValue(IDENTITY) on id field")
        void borrowTransactionIdAnnotations() throws NoSuchFieldException {
            // Validates: Requirements 3.1
            Field idField = BorrowTransaction.class.getDeclaredField("id");

            assertThat(idField.getType())
                    .as("BorrowTransaction.id should be of type long")
                    .isEqualTo(long.class);

            Id idAnnotation = idField.getAnnotation(Id.class);
            assertThat(idAnnotation)
                    .as("BorrowTransaction.id should have @Id annotation")
                    .isNotNull();

            GeneratedValue genAnnotation = idField.getAnnotation(GeneratedValue.class);
            assertThat(genAnnotation)
                    .as("BorrowTransaction.id should have @GeneratedValue annotation")
                    .isNotNull();
            assertThat(genAnnotation.strategy())
                    .as("BorrowTransaction.id should use GenerationType.IDENTITY")
                    .isEqualTo(GenerationType.IDENTITY);
        }

        @Test
        @DisplayName("BorrowTransaction should set borrowDate and auto-calculate dueDate")
        void borrowTransactionDateLogic() {
            // Validates: Requirements 3.1
            BorrowTransaction tx = new BorrowTransaction();
            LocalDate borrowDate = LocalDate.of(2024, 1, 15);
            tx.setBorrowDate(borrowDate);

            assertThat(tx.getBorrowDate()).isEqualTo(borrowDate);
            assertThat(tx.getDueDate()).isEqualTo(borrowDate.plusDays(14));
            assertThat(tx.isActive()).isTrue();
        }
    }

    /**
     * Tests 2-5: Book controller endpoint preservation
     * Tests 6-7: Member controller endpoint preservation
     * Tests 8-9: Borrow controller endpoint preservation
     * Test 10: GlobalExceptionHandler preservation
     *
     * Uses @WebMvcTest with mocked services.
     * All controllers now use consistent /api/v1/ versioned paths (Bug 6 fixed).
     */
    @Nested
    @WebMvcTest({BookController.class, MemberController.class,
                 BorrowController.class, GlobalExceptionHandler.class})
    @ActiveProfiles("test")
    @DisplayName("Controller endpoint preservation tests")
    class ControllerPreservation {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private BookService bookService;

        @MockitoBean
        private MemberService memberService;

        @MockitoBean
        private BorrowTransactionService borrowTransactionService;

        // --- Test 2: Book POST preservation (Req 3.2) ---

        @Test
        @DisplayName("Test 2a — POST /api/v1/books with valid request returns 201 Created")
        void bookPostValid() throws Exception {
            // Validates: Requirements 3.2
            String validBookJson = """
                    {
                        "title": "Clean Code",
                        "author": "Robert C. Martin",
                        "isbn": "978-0-13-235088-4",
                        "genre": "Software",
                        "totalCopies": 5
                    }
                    """;

            mockMvc.perform(post("/api/v1/books")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validBookJson))
                    .andExpect(status().isCreated());

            verify(bookService).addBook(any(Book.class));
        }

        @Test
        @DisplayName("Test 2b — POST /api/v1/books with missing fields returns 400")
        void bookPostInvalid() throws Exception {
            // Validates: Requirements 3.2
            String invalidBookJson = """
                    {
                        "title": "",
                        "author": "",
                        "isbn": "",
                        "genre": "",
                        "totalCopies": 0
                    }
                    """;

            mockMvc.perform(post("/api/v1/books")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidBookJson))
                    .andExpect(status().isBadRequest());
        }

        // --- Test 3: Book GET all preservation (Req 3.3) ---

        @Test
        @DisplayName("Test 3 — GET /api/v1/books returns 200 with list of books")
        void bookGetAll() throws Exception {
            // Validates: Requirements 3.3
            Book book = Book.builder()
                    .isbn("978-0-13-235088-4")
                    .title("Clean Code")
                    .author("Robert C. Martin")
                    .genre("Software")
                    .totalCopies(5)
                    .availableCopies(3)
                    .build();

            when(bookService.getAllBooks()).thenReturn(List.of(book));

            mockMvc.perform(get("/api/v1/books"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].isbn").value("978-0-13-235088-4"))
                    .andExpect(jsonPath("$[0].title").value("Clean Code"))
                    .andExpect(jsonPath("$[0].author").value("Robert C. Martin"))
                    .andExpect(jsonPath("$[0].genre").value("Software"))
                    .andExpect(jsonPath("$[0].totalCopies").value(5))
                    .andExpect(jsonPath("$[0].availableCopies").value(3));
        }

        // --- Test 4: Book GET by ISBN preservation (Req 3.4) ---

        @Test
        @DisplayName("Test 4a — GET /api/v1/books/{isbn} for existing book returns 200")
        void bookGetByIsbnFound() throws Exception {
            // Validates: Requirements 3.4
            Book book = Book.builder()
                    .isbn("978-0-13-235088-4")
                    .title("Clean Code")
                    .author("Robert C. Martin")
                    .genre("Software")
                    .totalCopies(5)
                    .availableCopies(5)
                    .build();

            when(bookService.findByIsbn("978-0-13-235088-4")).thenReturn(book);

            mockMvc.perform(get("/api/v1/books/978-0-13-235088-4"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isbn").value("978-0-13-235088-4"))
                    .andExpect(jsonPath("$.title").value("Clean Code"));
        }

        @Test
        @DisplayName("Test 4b — GET /api/v1/books/{isbn} for non-existent ISBN returns 404")
        void bookGetByIsbnNotFound() throws Exception {
            // Validates: Requirements 3.4
            when(bookService.findByIsbn("999-0-00-000000-0")).thenReturn(null);

            mockMvc.perform(get("/api/v1/books/999-0-00-000000-0"))
                    .andExpect(status().isNotFound());
        }

        // --- Test 5: Book DELETE preservation (Req 3.5) ---

        @Test
        @DisplayName("Test 5 — DELETE /api/v1/books/{isbn} returns 204 No Content")
        void bookDelete() throws Exception {
            // Validates: Requirements 3.5
            mockMvc.perform(delete("/api/v1/books/978-0-13-235088-4"))
                    .andExpect(status().isNoContent());

            verify(bookService).removeBook("978-0-13-235088-4");
        }

        // --- Test 6: Member POST preservation (Req 3.6) ---
        // Updated to use fixed versioned path /api/v1/members (Bug 6 fix)

        @Test
        @DisplayName("Test 6 — POST /api/v1/members with valid request returns 201 Created")
        void memberPostValid() throws Exception {
            // Validates: Requirements 3.6
            String validMemberJson = """
                    {
                        "name": "John Doe",
                        "memberId": "M001",
                        "contact": "john@example.com"
                    }
                    """;

            mockMvc.perform(post("/api/v1/members")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validMemberJson))
                    .andExpect(status().isCreated());

            verify(memberService).registerMember("John Doe", "M001", "john@example.com");
        }

        // --- Test 7: Member GET all preservation (Req 3.7) ---
        // Updated to use fixed versioned path /api/v1/members (Bug 6 fix)

        @Test
        @DisplayName("Test 7 — GET /api/v1/members returns 200 with list of members")
        void memberGetAll() throws Exception {
            // Validates: Requirements 3.7
            Member member = new Member();
            member.setMemberId("M001");
            member.setName("John Doe");
            member.setContact("john@example.com");
            member.setBorrowedIsbns(List.of("978-0-13-235088-4"));

            when(memberService.getAllMembers()).thenReturn(List.of(member));

            mockMvc.perform(get("/api/v1/members"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].memberId").value("M001"))
                    .andExpect(jsonPath("$[0].name").value("John Doe"))
                    .andExpect(jsonPath("$[0].contact").value("john@example.com"))
                    .andExpect(jsonPath("$[0].borrowedIsbns[0]").value("978-0-13-235088-4"));
        }

        // --- Test 8: Borrow/Return flow preservation (Req 3.8, 3.9) ---
        // Updated to use fixed versioned path /api/v1/borrows (Bug 6 fix)

        @Test
        @DisplayName("Test 8a — POST /api/v1/borrows creates borrow transaction")
        void borrowBookFlow() throws Exception {
            // Validates: Requirements 3.8
            when(borrowTransactionService.borrowBook("978-0-13-235088-4", "M001")).thenReturn(true);

            String borrowJson = """
                    {
                        "isbn": "978-0-13-235088-4",
                        "memberId": "M001"
                    }
                    """;

            mockMvc.perform(post("/api/v1/borrows")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(borrowJson))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Book borrowed successfully"));

            verify(borrowTransactionService).borrowBook("978-0-13-235088-4", "M001");
        }

        @Test
        @DisplayName("Test 8b — POST /api/v1/borrows/return closes borrow transaction")
        void returnBookFlow() throws Exception {
            // Validates: Requirements 3.9
            when(borrowTransactionService.returnBook("978-0-13-235088-4", "M001")).thenReturn(true);

            String returnJson = """
                    {
                        "isbn": "978-0-13-235088-4",
                        "memberId": "M001"
                    }
                    """;

            mockMvc.perform(post("/api/v1/borrows/return")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(returnJson))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Book returned successfully"));

            verify(borrowTransactionService).returnBook("978-0-13-235088-4", "M001");
        }

        // --- Test 9: Overdue query preservation (Req 3.10) ---
        // Updated to use fixed versioned path /api/v1/borrows (Bug 6 fix)

        @Test
        @DisplayName("Test 9 — GET /api/v1/borrows/overdue returns 200 with overdue transactions")
        void overdueQuery() throws Exception {
            // Validates: Requirements 3.10
            BorrowTransaction tx = new BorrowTransaction();
            tx.setBookIsbn("978-0-13-235088-4");
            tx.setMemberId("M001");
            tx.setBorrowDate(LocalDate.of(2024, 1, 1));

            when(borrowTransactionService.getOverdueBooks()).thenReturn(List.of(tx));

            mockMvc.perform(get("/api/v1/borrows/overdue"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].bookIsbn").value("978-0-13-235088-4"))
                    .andExpect(jsonPath("$[0].memberId").value("M001"));
        }

        // --- Test 10: GlobalExceptionHandler preservation (Req 3.11) ---

        @Test
        @DisplayName("Test 10a — IllegalArgumentException returns 400 with message")
        void globalExceptionHandlerIllegalArgument() throws Exception {
            // Validates: Requirements 3.11
            doThrow(new IllegalArgumentException("Book ISBN must not be empty."))
                    .when(bookService).addBook(any(Book.class));

            String bookJson = """
                    {
                        "title": "Test",
                        "author": "Author",
                        "isbn": "123",
                        "genre": "Genre",
                        "totalCopies": 1
                    }
                    """;

            mockMvc.perform(post("/api/v1/books")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(bookJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Book ISBN must not be empty."));
        }

        @Test
        @DisplayName("Test 10b — MethodArgumentNotValidException returns 400 with field errors")
        void globalExceptionHandlerValidation() throws Exception {
            // Validates: Requirements 3.11
            // Send a request with empty required fields to trigger validation
            String invalidJson = """
                    {
                        "title": "",
                        "author": "",
                        "isbn": "",
                        "genre": "",
                        "totalCopies": 0
                    }
                    """;

            mockMvc.perform(post("/api/v1/books")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value("Title is required"))
                    .andExpect(jsonPath("$.author").value("Author is required"))
                    .andExpect(jsonPath("$.isbn").value("ISBN is required"))
                    .andExpect(jsonPath("$.genre").value("Genre is required"));
        }
    }

    /**
     * Test 11 — Business logic preservation (Req 3.8, 3.9)
     * Plain unit tests without Spring context.
     */
    @Nested
    @DisplayName("Test 11 — Business logic preservation (Req 3.8, 3.9)")
    class BusinessLogicPreservation {

        @Test
        @DisplayName("Book.borrowCopy() decrements availableCopies")
        void bookBorrowCopy() {
            // Validates: Requirements 3.8
            Book book = Book.builder()
                    .isbn("978-0-13-235088-4")
                    .title("Clean Code")
                    .author("Robert C. Martin")
                    .genre("Software")
                    .totalCopies(3)
                    .availableCopies(3)
                    .build();

            boolean result = book.borrowCopy();

            assertThat(result).isTrue();
            assertThat(book.getAvailableCopies()).isEqualTo(2);
        }

        @Test
        @DisplayName("Book.borrowCopy() returns false when no copies available")
        void bookBorrowCopyNoneAvailable() {
            // Validates: Requirements 3.8
            Book book = Book.builder()
                    .isbn("978-0-13-235088-4")
                    .title("Clean Code")
                    .author("Robert C. Martin")
                    .genre("Software")
                    .totalCopies(1)
                    .availableCopies(0)
                    .build();

            boolean result = book.borrowCopy();

            assertThat(result).isFalse();
            assertThat(book.getAvailableCopies()).isEqualTo(0);
        }

        @Test
        @DisplayName("Book.returnCopy() increments availableCopies")
        void bookReturnCopy() {
            // Validates: Requirements 3.9
            Book book = Book.builder()
                    .isbn("978-0-13-235088-4")
                    .title("Clean Code")
                    .author("Robert C. Martin")
                    .genre("Software")
                    .totalCopies(3)
                    .availableCopies(2)
                    .build();

            book.returnCopy();

            assertThat(book.getAvailableCopies()).isEqualTo(3);
        }

        @Test
        @DisplayName("Book.returnCopy() does not exceed totalCopies")
        void bookReturnCopyAtMax() {
            // Validates: Requirements 3.9
            Book book = Book.builder()
                    .isbn("978-0-13-235088-4")
                    .title("Clean Code")
                    .author("Robert C. Martin")
                    .genre("Software")
                    .totalCopies(3)
                    .availableCopies(3)
                    .build();

            book.returnCopy();

            assertThat(book.getAvailableCopies()).isEqualTo(3);
        }

        @Test
        @DisplayName("Member.canBorrow() returns false at MAX_BORROW=5")
        void memberCanBorrowAtLimit() {
            // Validates: Requirements 3.8
            Member member = new Member();
            member.setMemberId("M001");
            member.setName("Test");
            member.setContact("test@example.com");

            // Borrow 5 books (MAX_BORROW)
            member.borrowBook("ISBN-1");
            member.borrowBook("ISBN-2");
            member.borrowBook("ISBN-3");
            member.borrowBook("ISBN-4");
            member.borrowBook("ISBN-5");

            assertThat(member.canBorrow()).isFalse();
        }

        @Test
        @DisplayName("Member.canBorrow() returns true below MAX_BORROW")
        void memberCanBorrowBelowLimit() {
            // Validates: Requirements 3.8
            Member member = new Member();
            member.setMemberId("M001");
            member.setName("Test");
            member.setContact("test@example.com");

            member.borrowBook("ISBN-1");

            assertThat(member.canBorrow()).isTrue();
        }

        @Test
        @DisplayName("Member.borrowBook() adds ISBN to borrowedIsbns")
        void memberBorrowBook() {
            // Validates: Requirements 3.8
            Member member = new Member();
            member.setMemberId("M001");
            member.setName("Test");
            member.setContact("test@example.com");

            member.borrowBook("978-0-13-235088-4");

            assertThat(member.getBorrowedIsbns()).contains("978-0-13-235088-4");
        }

        @Test
        @DisplayName("Member.borrowBook() does not add duplicate ISBN")
        void memberBorrowBookDuplicate() {
            // Validates: Requirements 3.8
            Member member = new Member();
            member.setMemberId("M001");
            member.setName("Test");
            member.setContact("test@example.com");

            member.borrowBook("978-0-13-235088-4");
            member.borrowBook("978-0-13-235088-4");

            assertThat(member.getBorrowedIsbns()).hasSize(1);
        }

        @Test
        @DisplayName("Member.returnBook() removes ISBN from borrowedIsbns")
        void memberReturnBook() {
            // Validates: Requirements 3.9
            Member member = new Member();
            member.setMemberId("M001");
            member.setName("Test");
            member.setContact("test@example.com");

            member.borrowBook("978-0-13-235088-4");
            member.returnBook("978-0-13-235088-4");

            assertThat(member.getBorrowedIsbns()).doesNotContain("978-0-13-235088-4");
        }
    }
}

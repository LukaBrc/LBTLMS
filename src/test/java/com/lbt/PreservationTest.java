package com.lbt;

import com.lbt.controllers.BookController;
import com.lbt.controllers.BorrowController;
import com.lbt.controllers.GlobalExceptionHandler;
import com.lbt.controllers.MemberController;
import com.lbt.dto.BookResponse;
import com.lbt.dto.MemberResponse;
import com.lbt.entities.Author;
import com.lbt.entities.Book;
import com.lbt.entities.BorrowTransaction;
import com.lbt.entities.Member;
import com.lbt.services.AuthorService;
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

class PreservationTest {

    @Nested
    @DisplayName("Test 1 Ã¢â‚¬â€ BorrowTransaction entity preservation (Req 3.1)")
    class BorrowTransactionEntityPreservation {

        @Test
        @DisplayName("BorrowTransaction should have @Id @GeneratedValue(IDENTITY) on id field")
        void borrowTransactionIdAnnotations() throws NoSuchFieldException {
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
            BorrowTransaction tx = new BorrowTransaction();
            LocalDate borrowDate = LocalDate.of(2024, 1, 15);
            tx.setBorrowDate(borrowDate);

            assertThat(tx.getBorrowDate()).isEqualTo(borrowDate);
            assertThat(tx.getDueDate()).isEqualTo(borrowDate.plusDays(14));
            assertThat(tx.isActive()).isTrue();
        }
    }

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
        private AuthorService authorService;

        @MockitoBean
        private MemberService memberService;

        @MockitoBean
        private BorrowTransactionService borrowTransactionService;


        @Test
        @DisplayName("Test 2a Ã¢â‚¬â€ POST /api/v1/books with valid request returns 201 Created")
        void bookPostValid() throws Exception {
            when(authorService.getAuthorById(1L)).thenReturn(Author.builder().id(1L).name("Robert C. Martin").build());

            String validBookJson = """
                    {
                        "title": "Clean Code",
                        "authorId": 1,
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
        @DisplayName("Test 2b Ã¢â‚¬â€ POST /api/v1/books with missing fields returns 400")
        void bookPostInvalid() throws Exception {
            String invalidBookJson = """
                    {
                        "title": "",
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


        @Test
        @DisplayName("Test 3 Ã¢â‚¬â€ GET /api/v1/books returns 200 with list of books")
        void bookGetAll() throws Exception {
            Author author = Author.builder().id(1L).name("Robert C. Martin").build();
            Book book = Book.builder()
                    .isbn("978-0-13-235088-4")
                    .title("Clean Code")
                    .author(author)
                    .genre("Software")
                    .totalCopies(5)
                    .availableCopies(3)
                    .build();

            when(bookService.getAllBooks()).thenReturn(List.of(book));

            mockMvc.perform(get("/api/v1/books"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].isbn").value("978-0-13-235088-4"))
                    .andExpect(jsonPath("$[0].title").value("Clean Code"))
                    .andExpect(jsonPath("$[0].authorId").value(1))
                    .andExpect(jsonPath("$[0].authorName").value("Robert C. Martin"))
                    .andExpect(jsonPath("$[0].genre").value("Software"))
                    .andExpect(jsonPath("$[0].totalCopies").value(5))
                    .andExpect(jsonPath("$[0].availableCopies").value(3));
        }


        @Test
        @DisplayName("Test 4a Ã¢â‚¬â€ GET /api/v1/books/{isbn} for existing book returns 200")
        void bookGetByIsbnFound() throws Exception {
            Author author = Author.builder().id(1L).name("Robert C. Martin").build();
            Book book = Book.builder()
                    .isbn("978-0-13-235088-4")
                    .title("Clean Code")
                    .author(author)
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
        @DisplayName("Test 4b Ã¢â‚¬â€ GET /api/v1/books/{isbn} for non-existent ISBN returns 404")
        void bookGetByIsbnNotFound() throws Exception {
            when(bookService.findByIsbn("999-0-00-000000-0")).thenReturn(null);

            mockMvc.perform(get("/api/v1/books/999-0-00-000000-0"))
                    .andExpect(status().isNotFound());
        }


        @Test
        @DisplayName("Test 5 Ã¢â‚¬â€ DELETE /api/v1/books/{isbn} returns 204 No Content")
        void bookDelete() throws Exception {
            mockMvc.perform(delete("/api/v1/books/978-0-13-235088-4"))
                    .andExpect(status().isNoContent());

            verify(bookService).removeBook("978-0-13-235088-4");
        }


        @Test
        @DisplayName("Test 6 Ã¢â‚¬â€ POST /api/v1/members with valid request returns 201 Created")
        void memberPostValid() throws Exception {
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


        @Test
        @DisplayName("Test 7 Ã¢â‚¬â€ GET /api/v1/members returns 200 with list of members")
        void memberGetAll() throws Exception {
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


        @Test
        @DisplayName("Test 8a Ã¢â‚¬â€ POST /api/v1/borrows creates borrow transaction")
        void borrowBookFlow() throws Exception {
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
        @DisplayName("Test 8b Ã¢â‚¬â€ POST /api/v1/borrows/return closes borrow transaction")
        void returnBookFlow() throws Exception {
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


        @Test
        @DisplayName("Test 9 Ã¢â‚¬â€ GET /api/v1/borrows/overdue returns 200 with overdue transactions")
        void overdueQuery() throws Exception {
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


        @Test
        @DisplayName("Test 10a Ã¢â‚¬â€ IllegalArgumentException returns 400 with message")
        void globalExceptionHandlerIllegalArgument() throws Exception {
            doThrow(new IllegalArgumentException("Book ISBN must not be empty."))
                    .when(bookService).addBook(any(Book.class));

            String bookJson = """
                    {
                        "title": "Test",
                        "authorId": 1,
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
        @DisplayName("Test 10b Ã¢â‚¬â€ MethodArgumentNotValidException returns 400 with field errors")
        void globalExceptionHandlerValidation() throws Exception {
            String invalidJson = """
                    {
                        "title": "",
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
                    .andExpect(jsonPath("$.isbn").value("ISBN is required"))
                    .andExpect(jsonPath("$.genre").value("Genre is required"));
        }
    }

    @Nested
    @DisplayName("Test 11 Ã¢â‚¬â€ Business logic preservation (Req 3.8, 3.9)")
    class BusinessLogicPreservation {

        @Test
        @DisplayName("Book.borrowCopy() decrements availableCopies")
        void bookBorrowCopy() {
            Author author = Author.builder().id(1L).name("Robert C. Martin").build();
            Book book = Book.builder()
                    .isbn("978-0-13-235088-4")
                    .title("Clean Code")
                    .author(author)
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
            Author author = Author.builder().id(1L).name("Robert C. Martin").build();
            Book book = Book.builder()
                    .isbn("978-0-13-235088-4")
                    .title("Clean Code")
                    .author(author)
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
            Author author = Author.builder().id(1L).name("Robert C. Martin").build();
            Book book = Book.builder()
                    .isbn("978-0-13-235088-4")
                    .title("Clean Code")
                    .author(author)
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
            Author author = Author.builder().id(1L).name("Robert C. Martin").build();
            Book book = Book.builder()
                    .isbn("978-0-13-235088-4")
                    .title("Clean Code")
                    .author(author)
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
            Member member = new Member();
            member.setMemberId("M001");
            member.setName("Test");
            member.setContact("test@example.com");

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
            Member member = new Member();
            member.setMemberId("M001");
            member.setName("Test");
            member.setContact("test@example.com");

            member.borrowBook("978-0-13-235088-4");

            assertThat(member.getBorrowedIsbns()).contains("978-0-13-235088-4");
        }

        @Test
        @DisplayName("Member.borrowBook() allows duplicate ISBN for multiple copies")
        void memberBorrowBookDuplicate() {
            Member member = new Member();
            member.setMemberId("M001");
            member.setName("Test");
            member.setContact("test@example.com");

            member.borrowBook("978-0-13-235088-4");
            member.borrowBook("978-0-13-235088-4");

            assertThat(member.getBorrowedIsbns()).hasSize(2);
        }

        @Test
        @DisplayName("Member.returnBook() removes ISBN from borrowedIsbns")
        void memberReturnBook() {
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

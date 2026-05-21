package com.lbt;

import com.lbt.entities.Author;
import com.lbt.entities.Book;
import com.lbt.entities.Member;
import com.lbt.repositories.AuthorRepository;
import com.lbt.repositories.BookRepository;
import com.lbt.repositories.BorrowTransactionRepository;
import com.lbt.repositories.MemberRepository;
import com.lbt.services.BookCache;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BorrowBookAvailabilityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BorrowTransactionRepository borrowTransactionRepository;

    @Autowired
    private BookCache bookCache;

    @Test
    void borrowThenGetBookByIsbn_reflectsDecrementedAvailableCopies() throws Exception {
        String isbn = "ISBN-IT-" + System.nanoTime();
        String memberId = "M-IT-" + System.nanoTime();

        borrowTransactionRepository.deleteAll();
        memberRepository.deleteAll();
        bookRepository.deleteAll();
        authorRepository.deleteAll();

        Author author = authorRepository.save(Author.builder().name("Integration Author").build());
        Book book = bookRepository.save(Book.builder()
                .isbn(isbn)
                .title("Integration Book")
                .author(author)
                .genre("Integration")
                .totalCopies(3)
                .availableCopies(3)
                .build());

        Member member = new Member();
        member.setName("Integration Member");
        member.setMemberId(memberId);
        member.setContact("integration@example.com");
        memberRepository.save(member);

        bookCache.put(book);

        mockMvc.perform(get("/api/v1/books/{isbn}", isbn))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableCopies").value(3));

        mockMvc.perform(post("/api/v1/borrows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isbn\":\"" + isbn + "\",\"memberId\":\"" + memberId + "\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/books/{isbn}", isbn))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableCopies").value(2))
                .andExpect(jsonPath("$.totalCopies").value(3));
    }

    @Test
    void borrowReturnThenGetBookByIsbn_reflectsIncrementedAvailableCopies() throws Exception {
        String isbn = "ISBN-IT-" + System.nanoTime();
        String memberId = "M-IT-" + System.nanoTime();

        borrowTransactionRepository.deleteAll();
        memberRepository.deleteAll();
        bookRepository.deleteAll();
        authorRepository.deleteAll();

        Author author = authorRepository.save(Author.builder().name("Integration Author").build());
        Book book = bookRepository.save(Book.builder()
                .isbn(isbn)
                .title("Integration Book")
                .author(author)
                .genre("Integration")
                .totalCopies(3)
                .availableCopies(3)
                .build());

        Member member = new Member();
        member.setName("Integration Member");
        member.setMemberId(memberId);
        member.setContact("integration@example.com");
        memberRepository.save(member);

        bookCache.put(book);

        mockMvc.perform(post("/api/v1/borrows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isbn\":\"" + isbn + "\",\"memberId\":\"" + memberId + "\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/borrows/return")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isbn\":\"" + isbn + "\",\"memberId\":\"" + memberId + "\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/books/{isbn}", isbn))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableCopies").value(3))
                .andExpect(jsonPath("$.totalCopies").value(3));
    }

    @Test
    void borrowTwiceReturnOnce_thenGetBookByIsbn_reflectsSingleIncrement() throws Exception {
        String isbn = "ISBN-IT-" + System.nanoTime();
        String memberId = "M-IT-" + System.nanoTime();

        borrowTransactionRepository.deleteAll();
        memberRepository.deleteAll();
        bookRepository.deleteAll();
        authorRepository.deleteAll();

        Author author = authorRepository.save(Author.builder().name("Integration Author").build());
        Book book = bookRepository.save(Book.builder()
                .isbn(isbn)
                .title("Integration Book")
                .author(author)
                .genre("Integration")
                .totalCopies(3)
                .availableCopies(3)
                .build());

        Member member = new Member();
        member.setName("Integration Member");
        member.setMemberId(memberId);
        member.setContact("integration@example.com");
        memberRepository.save(member);

        bookCache.put(book);

        mockMvc.perform(post("/api/v1/borrows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isbn\":\"" + isbn + "\",\"memberId\":\"" + memberId + "\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/borrows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isbn\":\"" + isbn + "\",\"memberId\":\"" + memberId + "\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/books/{isbn}", isbn))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableCopies").value(1))
                .andExpect(jsonPath("$.totalCopies").value(3));

        mockMvc.perform(post("/api/v1/borrows/return")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isbn\":\"" + isbn + "\",\"memberId\":\"" + memberId + "\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/books/{isbn}", isbn))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableCopies").value(2))
                .andExpect(jsonPath("$.totalCopies").value(3));
    }
}


package com.lbt;

import com.lbt.entities.Book;
import com.lbt.entities.BorrowTransaction;
import com.lbt.entities.Member;
import com.lbt.repositories.BookRepository;
import com.lbt.repositories.BorrowTransactionRepository;
import com.lbt.repositories.MemberRepository;
import com.lbt.services.BorrowTransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BorrowTransactionServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private BorrowTransactionRepository transactionRepository;

    @InjectMocks
    private BorrowTransactionService borrowService;

    private Book sampleBook;
    private Member sampleMember;

    @BeforeEach
    void setUp() {
        sampleBook = Book.builder()
                .isbn("ISBN-1")
                .title("T")
                .author("A")
                .genre("G")
                .totalCopies(3)
                .availableCopies(3)
                .build();

        sampleMember = new Member();
        sampleMember.setMemberId("M001");
        sampleMember.setName("Alice");
        sampleMember.setContact("alice@test.com");
    }

    @Test
    void borrowBook_successfulBorrow() {
        when(bookRepository.findByIsbn("ISBN-1")).thenReturn(sampleBook);
        when(memberRepository.findByMemberId("M001")).thenReturn(sampleMember);

        boolean result = borrowService.borrowBook("ISBN-1", "M001");

        assertTrue(result);
        assertEquals(2, sampleBook.getAvailableCopies());
        verify(transactionRepository).save(any(BorrowTransaction.class));
        verify(bookRepository).save(sampleBook);
    }

    @Test
    void borrowBook_returnsFalseWhenBookNotFound() {
        when(bookRepository.findByIsbn("UNKNOWN")).thenReturn(null);
        assertFalse(borrowService.borrowBook("UNKNOWN", "M001"));
    }

    @Test
    void borrowBook_returnsFalseWhenMemberNotFound() {
        when(bookRepository.findByIsbn("ISBN-1")).thenReturn(sampleBook);
        when(memberRepository.findByMemberId("UNKNOWN")).thenReturn(null);
        assertFalse(borrowService.borrowBook("ISBN-1", "UNKNOWN"));
    }

    @Test
    void borrowBook_returnsFalseWhenMemberAtBorrowLimit() {
        for (int i = 0; i < 5; i++) {
            sampleMember.borrowBook("ISBN-" + i);
        }
        when(bookRepository.findByIsbn("ISBN-1")).thenReturn(sampleBook);
        when(memberRepository.findByMemberId("M001")).thenReturn(sampleMember);

        assertFalse(borrowService.borrowBook("ISBN-1", "M001"));
    }

    @Test
    void borrowBook_returnsFalseWhenNoCopiesAvailable() {
        sampleBook = Book.builder()
                .isbn("ISBN-1").title("T").author("A").genre("G")
                .totalCopies(1).availableCopies(0).build();
        when(bookRepository.findByIsbn("ISBN-1")).thenReturn(sampleBook);
        when(memberRepository.findByMemberId("M001")).thenReturn(sampleMember);

        assertFalse(borrowService.borrowBook("ISBN-1", "M001"));
    }

    @Test
    void returnBook_successfulReturn() {
        BorrowTransaction tx = new BorrowTransaction();
        tx.setBookIsbn("ISBN-1");
        tx.setMemberId("M001");

        sampleBook = Book.builder()
                .isbn("ISBN-1").title("T").author("A").genre("G")
                .totalCopies(3).availableCopies(2).build();
        sampleMember.borrowBook("ISBN-1");

        when(bookRepository.findByIsbn("ISBN-1")).thenReturn(sampleBook);
        when(memberRepository.findByMemberId("M001")).thenReturn(sampleMember);
        when(transactionRepository.findByBookIsbnAndMemberIdAndReturnDateIsNull("ISBN-1", "M001"))
                .thenReturn(Optional.of(tx));

        boolean result = borrowService.returnBook("ISBN-1", "M001");

        assertTrue(result);
        assertEquals(3, sampleBook.getAvailableCopies());
        assertNotNull(tx.getReturnDate());
        verify(transactionRepository).save(tx);
    }

    @Test
    void returnBook_returnsFalseWhenNoActiveTransaction() {
        when(bookRepository.findByIsbn("ISBN-1")).thenReturn(sampleBook);
        when(memberRepository.findByMemberId("M001")).thenReturn(sampleMember);
        when(transactionRepository.findByBookIsbnAndMemberIdAndReturnDateIsNull("ISBN-1", "M001"))
                .thenReturn(Optional.empty());

        assertFalse(borrowService.returnBook("ISBN-1", "M001"));
    }
}

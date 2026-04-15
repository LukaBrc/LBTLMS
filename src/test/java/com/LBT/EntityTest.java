package com.lbt;

import com.lbt.entities.Author;
import com.lbt.entities.Book;
import com.lbt.entities.Member;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EntityTest {

    private Author sampleAuthor() {
        return Author.builder().id(1L).name("A").build();
    }

    // --- Book.borrowCopy() ---

    @Test
    void borrowCopy_decrementsAvailableCopies() {
        Book book = Book.builder().isbn("ISBN-1").title("T").author(sampleAuthor()).genre("G").totalCopies(3).availableCopies(3).build();
        assertTrue(book.borrowCopy());
        assertEquals(2, book.getAvailableCopies());
    }

    @Test
    void borrowCopy_returnsFalseWhenNoCopiesAvailable() {
        Book book = Book.builder().isbn("ISBN-1").title("T").author(sampleAuthor()).genre("G").totalCopies(1).availableCopies(0).build();
        assertFalse(book.borrowCopy());
        assertEquals(0, book.getAvailableCopies());
    }

    // --- Book.returnCopy() ---

    @Test
    void returnCopy_incrementsAvailableCopies() {
        Book book = Book.builder().isbn("ISBN-1").title("T").author(sampleAuthor()).genre("G").totalCopies(3).availableCopies(2).build();
        book.returnCopy();
        assertEquals(3, book.getAvailableCopies());
    }

    @Test
    void returnCopy_doesNotExceedTotalCopies() {
        Book book = Book.builder().isbn("ISBN-1").title("T").author(sampleAuthor()).genre("G").totalCopies(3).availableCopies(3).build();
        book.returnCopy();
        assertEquals(3, book.getAvailableCopies());
    }

    // --- Member.canBorrow() ---

    @Test
    void canBorrow_returnsTrueWhenUnderLimit() {
        Member member = new Member();
        member.setMemberId("M001");
        member.setName("Alice");
        member.setContact("alice@test.com");
        assertTrue(member.canBorrow());
    }

    @Test
    void canBorrow_returnsFalseAtMaxBorrow() {
        Member member = new Member();
        member.setMemberId("M001");
        member.setName("Alice");
        member.setContact("alice@test.com");
        for (int i = 0; i < 5; i++) {
            member.borrowBook("ISBN-" + i);
        }
        assertFalse(member.canBorrow());
    }

    // --- Member.borrowBook() ---

    @Test
    void borrowBook_addsIsbnToList() {
        Member member = new Member();
        member.setMemberId("M001");
        member.setName("Alice");
        member.setContact("alice@test.com");
        member.borrowBook("ISBN-1");
        assertTrue(member.getBorrowedIsbns().contains("ISBN-1"));
    }

    @Test
    void borrowBook_doesNotAddDuplicateIsbn() {
        Member member = new Member();
        member.setMemberId("M001");
        member.setName("Alice");
        member.setContact("alice@test.com");
        member.borrowBook("ISBN-1");
        member.borrowBook("ISBN-1");
        assertEquals(1, member.getBorrowedIsbns().size());
    }

    // --- Member.returnBook() ---

    @Test
    void returnBook_removesIsbnFromList() {
        Member member = new Member();
        member.setMemberId("M001");
        member.setName("Alice");
        member.setContact("alice@test.com");
        member.borrowBook("ISBN-1");
        member.returnBook("ISBN-1");
        assertFalse(member.getBorrowedIsbns().contains("ISBN-1"));
    }

    @Test
    void returnBook_noOpIfIsbnNotPresent() {
        Member member = new Member();
        member.setMemberId("M001");
        member.setName("Alice");
        member.setContact("alice@test.com");
        member.returnBook("ISBN-NONE");
        assertTrue(member.getBorrowedIsbns().isEmpty());
    }
}

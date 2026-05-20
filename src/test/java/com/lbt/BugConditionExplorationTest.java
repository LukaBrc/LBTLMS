package com.lbt;

import com.lbt.entities.Book;
import com.lbt.entities.Member;
import com.lbt.repositories.BookRepository;
import com.lbt.repositories.MemberRepository;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Bug Condition Exploration Ã¢â‚¬â€ Entity/Repository Layer")
class BugConditionExplorationTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("Test 1 Ã¢â‚¬â€ Bug 1 & 2: Book and Member entities should have auto-generated Long id")
    void entityIdAutoGeneration() {

        Field bookIdField = null;
        try {
            bookIdField = Book.class.getDeclaredField("id");
        } catch (NoSuchFieldException e) {
            fail("Book entity should have an 'id' field of type Long for auto-generation. " +
                 "Currently isbn is used as @Id which is a business key, not an auto-generated ID.");
        }
        assertThat(bookIdField.getType())
                .as("Book.id should be of type Long")
                .isEqualTo(Long.class);

        Field memberIdField = null;
        try {
            memberIdField = Member.class.getDeclaredField("id");
        } catch (NoSuchFieldException e) {
            fail("Member entity should have an 'id' field of type Long for auto-generation. " +
                 "Currently memberId is used as @Id which is a business key, not an auto-generated ID.");
        }
        assertThat(memberIdField.getType())
                .as("Member.id should be of type Long")
                .isEqualTo(Long.class);
    }

    @Test
    @DisplayName("Test 2 Ã¢â‚¬â€ Bug 3: Member borrowedIsbns should persist to database")
    void memberBorrowedIsbnsPersistence() {

        Member member = new Member();
        member.setMemberId("M002");
        member.setName("Borrow Test Member");
        member.setContact("borrow@example.com");
        member.borrowBook("978-0-13-468599-1");

        memberRepository.save(member);
        entityManager.flush();
        entityManager.clear();

        Member reloaded = memberRepository.findByMemberId("M002");
        assertNotNull(reloaded, "Member should be found in database");
        assertThat(reloaded.getBorrowedIsbns())
                .as("borrowedIsbns should be persisted and contain the borrowed ISBN after reload. " +
                    "Currently the list is silently dropped because it has no JPA mapping.")
                .contains("978-0-13-468599-1");
    }

    @Test
    @DisplayName("Test 3 Ã¢â‚¬â€ Bug 4: MemberRepository.existsById should not cause StackOverflowError")
    void memberRepositoryExistsByIdNonRecursion() {

        Member member = new Member();
        member.setMemberId("M003");
        member.setName("Exists Test Member");
        member.setContact("exists@example.com");
        memberRepository.save(member);
        entityManager.flush();

        assertDoesNotThrow(() -> {
            boolean exists = memberRepository.existsByMemberId("M003");
            assertTrue(exists, "existsByMemberId should return true for existing member");
        }, "existsByMemberId should not throw StackOverflowError Ã¢â‚¬â€ " +
           "the default method recursively calls itself instead of delegating to JPA");
    }
}

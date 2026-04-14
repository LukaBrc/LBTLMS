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

/**
 * Bug Condition Exploration Tests — Entity and Repository layer (Tests 1-3).
 *
 * Uses @DataJpaTest with H2 to test JPA entity and repository bugs
 * without loading the full Spring context (avoids Bug 5 blocking).
 *
 * These tests encode the EXPECTED (correct) behavior.
 * On UNFIXED code, they are EXPECTED TO FAIL — failure confirms the bugs exist.
 *
 * Validates: Requirements 1.1, 1.2, 1.3, 1.4
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Bug Condition Exploration — Entity/Repository Layer")
class BugConditionExplorationTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("Test 1 — Bug 1 & 2: Book and Member entities should have auto-generated Long id")
    void entityIdAutoGeneration() {
        // Validates: Requirements 1.1, 1.2
        // Bug 1: Book uses isbn as @Id with no auto-generation
        // Bug 2: Member uses memberId as @Id with no auto-generation

        // Verify Book has an auto-generated Long id field distinct from isbn
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

        // Verify Member has an auto-generated Long id field distinct from memberId
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
    @DisplayName("Test 2 — Bug 3: Member borrowedIsbns should persist to database")
    void memberBorrowedIsbnsPersistence() {
        // Validates: Requirements 1.3
        // Bug 3: borrowedIsbns is a plain ArrayList with no JPA mapping

        Member member = new Member();
        member.setMemberId("M002");
        member.setName("Borrow Test Member");
        member.setContact("borrow@example.com");
        member.borrowBook("978-0-13-468599-1");

        memberRepository.save(member);
        entityManager.flush();
        entityManager.clear();

        // Reload from database
        Member reloaded = memberRepository.findByMemberId("M002");
        assertNotNull(reloaded, "Member should be found in database");
        assertThat(reloaded.getBorrowedIsbns())
                .as("borrowedIsbns should be persisted and contain the borrowed ISBN after reload. " +
                    "Currently the list is silently dropped because it has no JPA mapping.")
                .contains("978-0-13-468599-1");
    }

    @Test
    @DisplayName("Test 3 — Bug 4: MemberRepository.existsById should not cause StackOverflowError")
    void memberRepositoryExistsByIdNonRecursion() {
        // Validates: Requirements 1.4
        // Bug 4: default existsById(String) calls itself recursively causing StackOverflowError

        Member member = new Member();
        member.setMemberId("M003");
        member.setName("Exists Test Member");
        member.setContact("exists@example.com");
        memberRepository.save(member);
        entityManager.flush();

        // On unfixed code, the default existsById method calls itself,
        // causing StackOverflowError instead of delegating to JPA
        assertDoesNotThrow(() -> {
            boolean exists = memberRepository.existsByMemberId("M003");
            assertTrue(exists, "existsByMemberId should return true for existing member");
        }, "existsByMemberId should not throw StackOverflowError — " +
           "the default method recursively calls itself instead of delegating to JPA");
    }
}

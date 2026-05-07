package com.lbt;

import com.lbt.entities.Member;
import com.lbt.repositories.BorrowTransactionRepository;
import com.lbt.repositories.MemberRepository;
import com.lbt.services.MemberService;
import com.lbt.validation.ValidationError;

import net.jqwik.api.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Feature: entity-validation-abstraction, Property 7: Service error message contains all validation errors joined by semicolon
/**
 * Property 7: Service error message contains all validation errors joined by semicolon
 *
 * For any invalid Member passed to MemberService.registerMember(), the service shall throw
 * an IllegalArgumentException whose message equals all ValidationError messages from
 * getValidationErrors() joined by the delimiter "; ".
 *
 * Validates: Requirements 6.2
 */
@Label("Feature: entity-validation-abstraction, Property 7: Service error message contains all validation errors joined by semicolon")
class SemicolonJoinedMessagePropertyTest {

    /**
     * Generate invalid Member data (various combinations of null/blank name, memberId, contact),
     * call MemberService.registerMember(), and verify the thrown IllegalArgumentException message
     * equals all validation error messages joined by "; ".
     *
     * **Validates: Requirements 6.2**
     */
    @Property(tries = 100)
    @Label("MemberService throws IllegalArgumentException with all errors joined by semicolon")
    void memberServiceThrowsWithAllErrorsJoinedBySemicolon(
            @ForAll("invalidMemberName") String name,
            @ForAll("invalidMemberId") String memberId,
            @ForAll("invalidMemberContact") String contact) {

        // Set up mocks
        MemberRepository memberRepository = mock(MemberRepository.class);
        BorrowTransactionRepository transactionRepository = mock(BorrowTransactionRepository.class);
        MemberRepository cacheRepository = mock(MemberRepository.class);
        when(cacheRepository.findAll()).thenReturn(java.util.Collections.emptyList());
        com.lbt.services.MemberCache memberCache = new com.lbt.services.MemberCache(cacheRepository);
        memberCache.init();
        MemberService memberService = new MemberService(memberRepository, transactionRepository, memberCache);

        // Build a Member with the same values to compute expected errors
        Member member = new Member();
        member.setName(name);
        member.setMemberId(memberId);
        member.setContact(contact);

        List<ValidationError> errors = member.getValidationErrors();

        // Only test when there are actual validation errors (at least one invalid field)
        Assume.that(!errors.isEmpty());

        // Compute expected message: all error messages joined by "; "
        String expectedMessage = errors.stream()
                .map(ValidationError::message)
                .collect(Collectors.joining("; "));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> memberService.registerMember(name, memberId, contact),
                "Expected IllegalArgumentException for invalid member data"
        );

        assertEquals(expectedMessage, exception.getMessage(),
                "Exception message should equal all validation error messages joined by \"; \"");
    }

    // --- Generators ---

    @Provide
    Arbitrary<String> invalidMemberName() {
        return Arbitraries.oneOf(
                Arbitraries.just(null),
                Arbitraries.just(""),
                Arbitraries.just("   "),
                Arbitraries.strings()
                        .withChars(' ', '\t', '\n', '\r')
                        .ofMinLength(1)
                        .ofMaxLength(10),
                // Name exceeding 150 characters
                Arbitraries.strings()
                        .alpha()
                        .ofMinLength(151)
                        .ofMaxLength(200)
        );
    }

    @Provide
    Arbitrary<String> invalidMemberId() {
        return Arbitraries.oneOf(
                Arbitraries.just(null),
                Arbitraries.just(""),
                Arbitraries.just("  "),
                Arbitraries.strings()
                        .withChars(' ', '\t', '\n', '\r')
                        .ofMinLength(1)
                        .ofMaxLength(10),
                // MemberId exceeding 50 characters
                Arbitraries.strings()
                        .alpha()
                        .ofMinLength(51)
                        .ofMaxLength(80)
        );
    }

    @Provide
    Arbitrary<String> invalidMemberContact() {
        return Arbitraries.oneOf(
                Arbitraries.just(null),
                Arbitraries.just(""),
                Arbitraries.just("  "),
                Arbitraries.strings()
                        .withChars(' ', '\t', '\n', '\r')
                        .ofMinLength(1)
                        .ofMaxLength(10),
                // Contact exceeding 200 characters
                Arbitraries.strings()
                        .alpha()
                        .ofMinLength(201)
                        .ofMaxLength(250)
        );
    }
}

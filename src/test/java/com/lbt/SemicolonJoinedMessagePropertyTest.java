package com.lbt;

import com.lbt.entities.Member;
import com.lbt.repositories.BorrowTransactionRepository;
import com.lbt.repositories.MemberRepository;
import com.lbt.services.MemberService;
import com.lbt.validation.ValidationError;
import com.lbt.validation.ValidationHandlerResolver;

import net.jqwik.api.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("unused")
class SemicolonJoinedMessagePropertyTest {

    @Property(tries = 100)
    void memberServiceThrowsWithAllErrorsJoinedBySemicolon(
            @ForAll("invalidMemberName") String name,
            @ForAll("invalidMemberId") String memberId,
            @ForAll("invalidMemberContact") String contact) {

        MemberRepository memberRepository = mock(MemberRepository.class);
        BorrowTransactionRepository transactionRepository = mock(BorrowTransactionRepository.class);
        MemberRepository cacheRepository = mock(MemberRepository.class);
        when(cacheRepository.findAll()).thenReturn(java.util.Collections.emptyList());
        com.lbt.services.MemberCache memberCache = new com.lbt.services.MemberCache(cacheRepository);
        memberCache.init();
        MemberService memberService = new MemberService(memberRepository, transactionRepository, memberCache);

        Member member = new Member();
        member.setName(name);
        member.setMemberId(memberId);
        member.setContact(contact);

        List<ValidationError> errors = validationErrors(member);

        Assume.that(!errors.isEmpty());

        String expectedMessage = errors.stream()
                .map(ValidationError::message)
                .collect(Collectors.joining("; "));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> memberService.registerMember(name, memberId, contact),
                "Expected IllegalArgumentException for invalid member data"
        );

        assertEquals(expectedMessage, exception.getMessage(),
                "Exception message should equal all validation error messages joined by \"; \"");
    }


    @Provide
    public Arbitrary<String> invalidMemberName() {
        return Arbitraries.oneOf(
                Arbitraries.just(null),
                Arbitraries.just(""),
                Arbitraries.just("   "),
                Arbitraries.strings()
                        .withChars(' ', '\t', '\n', '\r')
                        .ofMinLength(1)
                        .ofMaxLength(10),
                Arbitraries.strings()
                        .alpha()
                        .ofMinLength(151)
                        .ofMaxLength(200)
        );
    }

    @Provide
    public Arbitrary<String> invalidMemberId() {
        return Arbitraries.oneOf(
                Arbitraries.just(null),
                Arbitraries.just(""),
                Arbitraries.just("  "),
                Arbitraries.strings()
                        .withChars(' ', '\t', '\n', '\r')
                        .ofMinLength(1)
                        .ofMaxLength(10),
                Arbitraries.strings()
                        .alpha()
                        .ofMinLength(51)
                        .ofMaxLength(80)
        );
    }

    @Provide
    public Arbitrary<String> invalidMemberContact() {
        return Arbitraries.oneOf(
                Arbitraries.just(null),
                Arbitraries.just(""),
                Arbitraries.just("  "),
                Arbitraries.strings()
                        .withChars(' ', '\t', '\n', '\r')
                        .ofMinLength(1)
                        .ofMaxLength(10),
                Arbitraries.strings()
                        .alpha()
                        .ofMinLength(201)
                        .ofMaxLength(250)
        );
    }

    private List<ValidationError> validationErrors(Object entity) {
        return ValidationHandlerResolver.get().getValidationErrors(entity);
    }
}

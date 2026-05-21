package com.lbt;

import com.lbt.entities.Member;
import com.lbt.repositories.MemberRepository;
import com.lbt.services.MemberCache;

import net.jqwik.api.*;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MemberCacheExtractKeyPropertyTest {

    private final MemberCache memberCache;

    MemberCacheExtractKeyPropertyTest() {
        MemberRepository mockRepository = mock(MemberRepository.class);
        when(mockRepository.findAll()).thenReturn(Collections.emptyList());
        this.memberCache = new MemberCache(mockRepository);
    }

    @Property(tries = 200)
    void extractKeyReturnsMemberId(@ForAll("membersWithNonNullMemberId") Member member) {
        String expectedKey = member.getMemberId();

        memberCache.put(member);

        assertTrue(memberCache.getById(expectedKey).isPresent(),
                "getById(member.getMemberId()) should find the member after put - confirms extractKey returns getMemberId()");
        assertEquals(member, memberCache.getById(expectedKey).get(),
                "The retrieved member should be the same instance that was put");

        memberCache.evict(expectedKey);
    }


    @Provide
    Arbitrary<Member> membersWithNonNullMemberId() {
        Arbitrary<Long> ids = Arbitraries.longs().between(1L, Long.MAX_VALUE);
        Arbitrary<String> memberIds = Arbitraries.strings().alpha().numeric().ofMinLength(1).ofMaxLength(50);
        Arbitrary<String> names = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(50);
        Arbitrary<String> contacts = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(50);

        return Combinators.combine(ids, memberIds, names, contacts).as((id, memberId, name, contact) -> {
            Member member = new Member();
            member.setId(id);
            member.setMemberId(memberId);
            member.setName(name);
            member.setContact(contact);
            return member;
        });
    }
}

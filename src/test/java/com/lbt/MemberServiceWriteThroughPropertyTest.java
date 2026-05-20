package com.lbt;

import com.lbt.entities.Member;
import com.lbt.repositories.BorrowTransactionRepository;
import com.lbt.repositories.MemberRepository;
import com.lbt.services.MemberCache;
import com.lbt.services.MemberService;

import net.jqwik.api.*;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MemberServiceWriteThroughPropertyTest {

    private final MemberRepository memberRepository;
    private final BorrowTransactionRepository transactionRepository;
    private final MemberCache memberCache;
    private final MemberService memberService;

    MemberServiceWriteThroughPropertyTest() {
        this.memberRepository = mock(MemberRepository.class);
        this.transactionRepository = mock(BorrowTransactionRepository.class);

        MemberRepository cacheRepository = mock(MemberRepository.class);
        when(cacheRepository.findAll()).thenReturn(Collections.emptyList());
        this.memberCache = new MemberCache(cacheRepository);
        this.memberCache.init();

        this.memberService = new MemberService(memberRepository, transactionRepository, memberCache);
    }

    @Property(tries = 100)
    void afterRegisterMemberCacheContainsMember(
            @ForAll("validNames") String name,
            @ForAll("validMemberIds") String memberId,
            @ForAll("validContacts") String contact) {

        when(memberRepository.existsByMemberId(memberId)).thenReturn(false);
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.findActiveBookIsbnsByMemberId(memberId)).thenReturn(Collections.emptyList());

        memberService.registerMember(name, memberId, contact);

        Member cached = memberService.findById(memberId);
        assertNotNull(cached, "Member should be retrievable from cache after registerMember");
        assertEquals(memberId, cached.getMemberId(), "Cached member ID should match");
        assertEquals(name, cached.getName(), "Cached member name should match");
        assertEquals(contact, cached.getContact(), "Cached member contact should match");

        memberCache.evict(memberId);
    }

    @Property(tries = 100)
    void afterUpdateMemberCacheReflectsUpdate(
            @ForAll("validNames") String originalName,
            @ForAll("validMemberIds") String memberId,
            @ForAll("validContacts") String originalContact,
            @ForAll("validNames") String newName,
            @ForAll("validContacts") String newContact) {

        Member existingMember = new Member();
        existingMember.setMemberId(memberId);
        existingMember.setName(originalName);
        existingMember.setContact(originalContact);

        when(memberRepository.findByMemberId(memberId)).thenReturn(existingMember);
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.findActiveBookIsbnsByMemberId(memberId)).thenReturn(Collections.emptyList());

        memberService.updateMember(memberId, newName, newContact);

        Member cached = memberService.findById(memberId);
        assertNotNull(cached, "Updated member should be retrievable from cache after updateMember");
        assertEquals(newName, cached.getName(), "Cached member name should reflect the update");
        assertEquals(newContact, cached.getContact(), "Cached member contact should reflect the update");
        assertEquals(memberId, cached.getMemberId(), "Cached member ID should remain the same");

        memberCache.evict(memberId);
    }

    @Property(tries = 100)
    void afterDeleteMemberCacheDoesNotContainMember(
            @ForAll("validNames") String name,
            @ForAll("validMemberIds") String memberId,
            @ForAll("validContacts") String contact) {

        Member member = new Member();
        member.setMemberId(memberId);
        member.setName(name);
        member.setContact(contact);
        memberCache.put(member);

        when(transactionRepository.findActiveBookIsbnsByMemberId(memberId)).thenReturn(Collections.emptyList());
        assertNotNull(memberService.findById(memberId), "Precondition: member should be in cache before deletion");

        when(memberRepository.findByMemberId(memberId)).thenReturn(member);
        doNothing().when(memberRepository).delete(member);

        memberService.deleteMember(memberId);

        Member cached = memberService.findById(memberId);
        assertNull(cached, "Member should not be retrievable from cache after deleteMember");
    }


    @Provide
    Arbitrary<String> validNames() {
        return Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(50);
    }

    @Provide
    Arbitrary<String> validMemberIds() {
        return Arbitraries.strings().alpha().numeric().ofMinLength(1).ofMaxLength(20);
    }

    @Provide
    Arbitrary<String> validContacts() {
        return Arbitraries.strings().alpha().numeric().ofMinLength(1).ofMaxLength(50);
    }
}

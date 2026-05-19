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

/**
 * Property-based test for MemberService write-through cache consistency.
 *
 * Validates: Requirements 9.3, 9.4, 9.5
 */
@Label("Feature: entity-cache-abstraction, Property 8: Service write-through keeps cache consistent (MemberService)")
class MemberServiceWriteThroughPropertyTest {

    private final MemberRepository memberRepository;
    private final BorrowTransactionRepository transactionRepository;
    private final MemberCache memberCache;
    private final MemberService memberService;

    MemberServiceWriteThroughPropertyTest() {
        this.memberRepository = mock(MemberRepository.class);
        this.transactionRepository = mock(BorrowTransactionRepository.class);

        // Use a real MemberCache backed by a mocked repository for loadAll
        MemberRepository cacheRepository = mock(MemberRepository.class);
        when(cacheRepository.findAll()).thenReturn(Collections.emptyList());
        this.memberCache = new MemberCache(cacheRepository);
        this.memberCache.init();

        this.memberService = new MemberService(memberRepository, transactionRepository, memberCache);
    }

    /**
     * Property 8: After registerMember, the member is retrievable from the cache.
     *
     * For any valid member, after registerMember completes successfully, the cache contains
     * the member and it is retrievable by memberId.
     *
     * Validates: Requirements 9.3
     */
    @Property(tries = 100)
    @Label("Property 8: After registerMember, member is retrievable from cache")
    void afterRegisterMemberCacheContainsMember(
            @ForAll("validNames") String name,
            @ForAll("validMemberIds") String memberId,
            @ForAll("validContacts") String contact) {

        // Setup: memberId does not already exist
        when(memberRepository.existsByMemberId(memberId)).thenReturn(false);
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.findActiveBookIsbnsByMemberId(memberId)).thenReturn(Collections.emptyList());

        // Act
        memberService.registerMember(name, memberId, contact);

        // Assert: member is in cache
        Member cached = memberService.findById(memberId);
        assertNotNull(cached, "Member should be retrievable from cache after registerMember");
        assertEquals(memberId, cached.getMemberId(), "Cached member ID should match");
        assertEquals(name, cached.getName(), "Cached member name should match");
        assertEquals(contact, cached.getContact(), "Cached member contact should match");

        // Cleanup for next iteration
        memberCache.evict(memberId);
    }

    /**
     * Property 8: After updateMember, the updated member is retrievable from the cache.
     *
     * For any valid member that exists in the repository, after updateMember completes,
     * the cache reflects the updated values.
     *
     * Validates: Requirements 9.4
     */
    @Property(tries = 100)
    @Label("Property 8: After updateMember, updated member is retrievable from cache")
    void afterUpdateMemberCacheReflectsUpdate(
            @ForAll("validNames") String originalName,
            @ForAll("validMemberIds") String memberId,
            @ForAll("validContacts") String originalContact,
            @ForAll("validNames") String newName,
            @ForAll("validContacts") String newContact) {

        // Setup: member exists in repository
        Member existingMember = new Member();
        existingMember.setMemberId(memberId);
        existingMember.setName(originalName);
        existingMember.setContact(originalContact);

        when(memberRepository.findByMemberId(memberId)).thenReturn(existingMember);
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.findActiveBookIsbnsByMemberId(memberId)).thenReturn(Collections.emptyList());

        // Act
        memberService.updateMember(memberId, newName, newContact);

        // Assert: updated member is in cache
        Member cached = memberService.findById(memberId);
        assertNotNull(cached, "Updated member should be retrievable from cache after updateMember");
        assertEquals(newName, cached.getName(), "Cached member name should reflect the update");
        assertEquals(newContact, cached.getContact(), "Cached member contact should reflect the update");
        assertEquals(memberId, cached.getMemberId(), "Cached member ID should remain the same");

        // Cleanup for next iteration
        memberCache.evict(memberId);
    }

    /**
     * Property 8: After deleteMember, the member is no longer in the cache.
     *
     * For any valid member that was previously added to the cache, after deleteMember
     * completes, the member is not retrievable from the cache.
     *
     * Validates: Requirements 9.5
     */
    @Property(tries = 100)
    @Label("Property 8: After deleteMember, member is no longer in cache")
    void afterDeleteMemberCacheDoesNotContainMember(
            @ForAll("validNames") String name,
            @ForAll("validMemberIds") String memberId,
            @ForAll("validContacts") String contact) {

        // Setup: put member in cache first
        Member member = new Member();
        member.setMemberId(memberId);
        member.setName(name);
        member.setContact(contact);
        memberCache.put(member);

        when(transactionRepository.findActiveBookIsbnsByMemberId(memberId)).thenReturn(Collections.emptyList());
        assertNotNull(memberService.findById(memberId), "Precondition: member should be in cache before deletion");

        // Mock repository findByMemberId to return the member (needed for deleteMember)
        when(memberRepository.findByMemberId(memberId)).thenReturn(member);
        doNothing().when(memberRepository).delete(member);

        // Act
        memberService.deleteMember(memberId);

        // Assert: member is no longer in cache
        Member cached = memberService.findById(memberId);
        assertNull(cached, "Member should not be retrievable from cache after deleteMember");
    }

    // --- Generators ---

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

package com.lbt;

import com.lbt.entities.Member;
import com.lbt.repositories.BorrowTransactionRepository;
import com.lbt.repositories.MemberRepository;
import com.lbt.services.MemberCache;
import com.lbt.services.MemberService;

import net.jqwik.api.*;
import net.jqwik.api.lifecycle.BeforeProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Property-based test for MemberService enrichment from transaction repository.
 *
 * Property 9: For any member retrieved via findById(memberId) or getAllMembers(),
 * the returned member's borrowedIsbns list SHALL equal the active ISBNs from
 * BorrowTransactionRepository for that member — not the cached entity's stale
 * borrowedIsbns field.
 *
 * Validates: Requirements 9.1, 9.2
 */
@Label("Feature: entity-cache-abstraction, Property 9: MemberService enrichment from transaction repository")
class MemberServiceEnrichmentPropertyTest {

    private MemberRepository memberRepository;
    private BorrowTransactionRepository transactionRepository;
    private MemberCache memberCache;
    private MemberService memberService;

    @BeforeProperty
    void setUp() {
        memberRepository = mock(MemberRepository.class);
        transactionRepository = mock(BorrowTransactionRepository.class);

        // Use a real MemberCache backed by a mocked MemberRepository for loadAll
        MemberRepository cacheRepository = mock(MemberRepository.class);
        when(cacheRepository.findAll()).thenReturn(Collections.emptyList());
        memberCache = new MemberCache(cacheRepository);
        memberCache.init();

        memberService = new MemberService(memberRepository, transactionRepository, memberCache);
    }

    /**
     * Property 9: findById enriches member with active ISBNs from transaction repository.
     *
     * For any member in the cache with stale borrowedIsbns, when findById is called,
     * the returned member's borrowedIsbns SHALL equal the active ISBNs from
     * BorrowTransactionRepository — not the cached stale values.
     *
     * Validates: Requirements 9.2
     */
    @Property(tries = 100)
    @Label("Property 9: findById enriches member with active ISBNs from transaction repository")
    void findByIdEnrichesMemberWithActiveIsbns(
            @ForAll("validMembers") Member member,
            @ForAll("isbnLists") List<String> staleIsbns,
            @ForAll("isbnLists") List<String> activeIsbns) {

        // Setup: put member in cache with stale borrowedIsbns
        member.setBorrowedIsbns(staleIsbns);
        memberCache.put(member);

        // Mock transaction repository to return different active ISBNs
        when(transactionRepository.findActiveBookIsbnsByMemberId(member.getMemberId()))
                .thenReturn(activeIsbns);

        // Act
        Member result = memberService.findById(member.getMemberId());

        // Assert: returned member has ISBNs from transaction repo, not stale cache
        assertNotNull(result, "findById should return the member from cache");
        assertEquals(activeIsbns, result.getBorrowedIsbns(),
                "borrowedIsbns should equal active ISBNs from transaction repository, not stale cached values");

        // Cleanup for next iteration
        memberCache.evict(member.getMemberId());
    }

    /**
     * Property 9: getAllMembers enriches each member with active ISBNs from transaction repository.
     *
     * For any set of members in the cache, when getAllMembers is called,
     * each returned member's borrowedIsbns SHALL equal the active ISBNs from
     * BorrowTransactionRepository for that member.
     *
     * Validates: Requirements 9.1
     */
    @Property(tries = 100)
    @Label("Property 9: getAllMembers enriches each member with active ISBNs from transaction repository")
    void getAllMembersEnrichesEachMemberWithActiveIsbns(
            @ForAll("validMemberLists") List<Member> members,
            @ForAll("isbnLists") List<String> staleIsbns) {

        // Setup: put each member in cache with stale borrowedIsbns
        // and configure transaction repo to return unique active ISBNs per member
        List<List<String>> expectedIsbnsByMember = new ArrayList<>();
        for (int i = 0; i < members.size(); i++) {
            Member member = members.get(i);
            member.setBorrowedIsbns(staleIsbns);
            memberCache.put(member);

            // Each member gets a distinct active ISBN list based on their index
            List<String> activeIsbns = List.of("ISBN-ACTIVE-" + member.getMemberId());
            expectedIsbnsByMember.add(activeIsbns);
            when(transactionRepository.findActiveBookIsbnsByMemberId(member.getMemberId()))
                    .thenReturn(activeIsbns);
        }

        // Act
        List<Member> result = memberService.getAllMembers();

        // Assert: each returned member has ISBNs from transaction repo
        assertEquals(members.size(), result.size(),
                "getAllMembers should return all cached members");

        for (Member returnedMember : result) {
            String memberId = returnedMember.getMemberId();
            List<String> expectedIsbns = List.of("ISBN-ACTIVE-" + memberId);
            assertEquals(expectedIsbns, returnedMember.getBorrowedIsbns(),
                    "Member " + memberId + " borrowedIsbns should equal active ISBNs from transaction repository");
        }

        // Cleanup for next iteration
        for (Member member : members) {
            memberCache.evict(member.getMemberId());
        }
    }

    // --- Generators ---

    @Provide
    Arbitrary<Member> validMembers() {
        Arbitrary<String> memberIds = Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(20);
        Arbitrary<String> names = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(50);
        Arbitrary<String> contacts = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(50);

        return Combinators.combine(memberIds, names, contacts)
                .as((memberId, name, contact) -> {
                    Member member = new Member();
                    member.setMemberId(memberId);
                    member.setName(name);
                    member.setContact(contact);
                    member.setBorrowedIsbns(new ArrayList<>());
                    return member;
                });
    }

    @Provide
    Arbitrary<List<Member>> validMemberLists() {
        return validMembers()
                .list()
                .ofMinSize(1)
                .ofMaxSize(5)
                .uniqueElements(Member::getMemberId);
    }

    @Provide
    Arbitrary<List<String>> isbnLists() {
        return Arbitraries.strings().alpha().numeric().ofMinLength(5).ofMaxLength(15)
                .list()
                .ofMinSize(0)
                .ofMaxSize(4)
                .uniqueElements();
    }
}

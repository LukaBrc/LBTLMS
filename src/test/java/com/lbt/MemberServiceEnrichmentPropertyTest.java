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

class MemberServiceEnrichmentPropertyTest {

    private MemberRepository memberRepository;
    private BorrowTransactionRepository transactionRepository;
    private MemberCache memberCache;
    private MemberService memberService;

    @BeforeProperty
    void setUp() {
        memberRepository = mock(MemberRepository.class);
        transactionRepository = mock(BorrowTransactionRepository.class);

        MemberRepository cacheRepository = mock(MemberRepository.class);
        when(cacheRepository.findAll()).thenReturn(Collections.emptyList());
        memberCache = new MemberCache(cacheRepository);
        memberCache.init();

        memberService = new MemberService(memberRepository, transactionRepository, memberCache);
    }

    @Property(tries = 100)
    void findByIdEnrichesMemberWithActiveIsbns(
            @ForAll("validMembers") Member member,
            @ForAll("isbnLists") List<String> staleIsbns,
            @ForAll("isbnLists") List<String> activeIsbns) {

        member.setBorrowedIsbns(staleIsbns);
        memberCache.put(member);

        when(transactionRepository.findActiveBookIsbnsByMemberId(member.getMemberId()))
                .thenReturn(activeIsbns);

        Member result = memberService.findById(member.getMemberId());

        assertNotNull(result, "findById should return the member from cache");
        assertEquals(activeIsbns, result.getBorrowedIsbns(),
                "borrowedIsbns should equal active ISBNs from transaction repository, not stale cached values");

        memberCache.evict(member.getMemberId());
    }

    @Property(tries = 100)
    void getAllMembersEnrichesEachMemberWithActiveIsbns(
            @ForAll("validMemberLists") List<Member> members,
            @ForAll("isbnLists") List<String> staleIsbns) {

        List<List<String>> expectedIsbnsByMember = new ArrayList<>();
        for (int i = 0; i < members.size(); i++) {
            Member member = members.get(i);
            member.setBorrowedIsbns(staleIsbns);
            memberCache.put(member);

            List<String> activeIsbns = List.of("ISBN-ACTIVE-" + member.getMemberId());
            expectedIsbnsByMember.add(activeIsbns);
            when(transactionRepository.findActiveBookIsbnsByMemberId(member.getMemberId()))
                    .thenReturn(activeIsbns);
        }

        List<Member> result = memberService.getAllMembers();

        assertEquals(members.size(), result.size(),
                "getAllMembers should return all cached members");

        for (Member returnedMember : result) {
            String memberId = returnedMember.getMemberId();
            List<String> expectedIsbns = List.of("ISBN-ACTIVE-" + memberId);
            assertEquals(expectedIsbns, returnedMember.getBorrowedIsbns(),
                    "Member " + memberId + " borrowedIsbns should equal active ISBNs from transaction repository");
        }

        for (Member member : members) {
            memberCache.evict(member.getMemberId());
        }
    }


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

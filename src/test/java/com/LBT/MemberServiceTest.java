package com.lbt;

import com.lbt.entities.Member;
import com.lbt.repositories.BorrowTransactionRepository;
import com.lbt.repositories.MemberRepository;
import com.lbt.services.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private BorrowTransactionRepository transactionRepository;

    @InjectMocks
    private MemberService memberService;

    private Member sampleMember;

    @BeforeEach
    void setUp() {
        sampleMember = new Member();
        sampleMember.setMemberId("M001");
        sampleMember.setName("Alice");
        sampleMember.setContact("alice@test.com");
    }

    @Test
    void registerMember_savesNewMember() {
        when(memberRepository.existsByMemberId("M001")).thenReturn(false);
        memberService.registerMember("Alice", "M001", "alice@test.com");
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    void registerMember_throwsOnDuplicateMemberId() {
        when(memberRepository.existsByMemberId("M001")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () ->
                memberService.registerMember("Alice", "M001", "alice@test.com"));
    }

    @Test
    void registerMember_throwsOnNullFields() {
        assertThrows(IllegalArgumentException.class, () ->
                memberService.registerMember(null, "M001", "contact"));
    }

    @Test
    void updateMember_updatesNameAndContact() {
        when(memberRepository.findByMemberId("M001")).thenReturn(sampleMember);
        when(memberRepository.save(any(Member.class))).thenReturn(sampleMember);

        Member result = memberService.updateMember("M001", "Bob", "bob@test.com");

        assertNotNull(result);
        assertEquals("Bob", sampleMember.getName());
        assertEquals("bob@test.com", sampleMember.getContact());
        verify(memberRepository).save(sampleMember);
    }

    @Test
    void updateMember_throwsWhenMemberNotFound() {
        when(memberRepository.findByMemberId("UNKNOWN")).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () ->
                memberService.updateMember("UNKNOWN", "Name", "Contact"));
    }

    @Test
    void deleteMember_deletesMember() {
        when(memberRepository.findByMemberId("M001")).thenReturn(sampleMember);
        memberService.deleteMember("M001");
        verify(memberRepository).delete(sampleMember);
    }

    @Test
    void deleteMember_throwsWhenMemberNotFound() {
        when(memberRepository.findByMemberId("UNKNOWN")).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () ->
                memberService.deleteMember("UNKNOWN"));
    }
}

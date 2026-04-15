package com.lbt.services;

import org.springframework.stereotype.Service;

import com.lbt.entities.Member;
import com.lbt.repositories.BorrowTransactionRepository;
import com.lbt.repositories.MemberRepository;

import java.util.List;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final BorrowTransactionRepository transactionRepository;

    public MemberService(MemberRepository memberRepository,
                         BorrowTransactionRepository transactionRepository) {
        this.memberRepository = memberRepository;
        this.transactionRepository = transactionRepository;
    }

    public void registerMember(String name, String memberId, String contact) {
        if (name == null || memberId == null || contact == null) {
            throw new IllegalArgumentException("Name, ID and contact are required");
        }
        if (memberRepository.existsByMemberId(memberId)) {
            throw new IllegalArgumentException("Member ID " + memberId + " is already registered");
        }

        Member member = new Member();
        member.setMemberId(memberId);
        member.setName(name);
        member.setContact(contact);

        memberRepository.save(member);
    }

    public Member findById(String memberId) {
        Member member = memberRepository.findByMemberId(memberId);
        if (member != null) {
            List<String> activeIsbns = transactionRepository.findActiveBookIsbnsByMemberId(memberId);
            member.setBorrowedIsbns(activeIsbns);
        }
        return member;
    }

    public List<Member> getAllMembers() {
        List<Member> members = memberRepository.findAll();
        for (Member m : members) {
            List<String> activeIsbns = transactionRepository.findActiveBookIsbnsByMemberId(m.getMemberId());
            m.setBorrowedIsbns(activeIsbns);
        }
        return members;
    }

    public Member updateMember(String memberId, String name, String contact) {
        Member member = memberRepository.findByMemberId(memberId);
        if (member == null) {
            throw new IllegalArgumentException("Member not found: " + memberId);
        }
        member.setName(name);
        member.setContact(contact);
        return memberRepository.save(member);
    }

    public void deleteMember(String memberId) {
        Member member = memberRepository.findByMemberId(memberId);
        if (member == null) {
            throw new IllegalArgumentException("Member not found: " + memberId);
        }
        memberRepository.delete(member);
    }
}
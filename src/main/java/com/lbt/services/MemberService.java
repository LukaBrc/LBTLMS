package com.lbt.services;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.lbt.entities.Member;
import com.lbt.exceptions.ResourceConflictException;
import com.lbt.repositories.BorrowTransactionRepository;
import com.lbt.repositories.MemberRepository;
import com.lbt.validation.ValidationHandler;
import com.lbt.validation.ValidationHandlerResolver;
import com.lbt.validation.ValidationError;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final BorrowTransactionRepository transactionRepository;
    private final MemberCache memberCache;
    private final ValidationHandler validationHandler;

    public MemberService(MemberRepository memberRepository,
                         BorrowTransactionRepository transactionRepository,
                         MemberCache memberCache) {
        this(memberRepository, transactionRepository, memberCache, ValidationHandlerResolver.get());
    }

    @Autowired
    public MemberService(MemberRepository memberRepository,
                         BorrowTransactionRepository transactionRepository,
                         MemberCache memberCache,
                         ValidationHandler validationHandler) {
        this.memberRepository = memberRepository;
        this.transactionRepository = transactionRepository;
        this.memberCache = memberCache;
        this.validationHandler = validationHandler;
    }

    public void registerMember(String name, String memberId, String contact) {
        Member member = new Member();
        member.setMemberId(memberId);
        member.setName(name);
        member.setContact(contact);

        validateEntity(member, "Member");

        if (memberRepository.existsByMemberId(memberId)) {
            throw new IllegalArgumentException("Member ID " + memberId + " is already registered");
        }

        Member savedMember = memberRepository.save(member);
        memberCache.put(savedMember);
    }

    public Member findById(String memberId) {
        Optional<Member> optionalMember = memberCache.getById(memberId);
        Member member = optionalMember.orElse(null);
        if (member != null) {
            List<String> activeIsbns = transactionRepository.findActiveBookIsbnsByMemberId(memberId);
            member.setBorrowedIsbns(activeIsbns);
        }
        return member;
    }

    public List<Member> getAllMembers() {
        List<Member> members = memberCache.getAll();
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
        validateEntity(member, "Member");
        Member savedMember = memberRepository.save(member);
        List<String> activeIsbns = transactionRepository.findActiveBookIsbnsByMemberId(memberId);
        savedMember.setBorrowedIsbns(activeIsbns);
        memberCache.put(savedMember);
        return savedMember;
    }

    @Transactional
    public void deleteMember(String memberId) {
        Member member = memberRepository.findByMemberIdForUpdate(memberId);
        if (member == null) {
            throw new IllegalArgumentException("Member not found: " + memberId);
        }
        if (transactionRepository.existsByMemberIdAndReturnDateIsNull(memberId)) {
            throw new ResourceConflictException("Member " + memberId + " cannot be deleted while they have active borrows.");
        }
        memberRepository.delete(member);
        memberCache.evict(memberId);
    }

    private void validateEntity(Object entity, String entityName) {
        if (entity == null) {
            throw new IllegalArgumentException(entityName + " must not be null.");
        }
        List<ValidationError> errors = validationHandler.getValidationErrors(entity);
        if (!errors.isEmpty()) {
            String message = errors.stream()
                .map(ValidationError::message)
                .collect(Collectors.joining("; "));
            throw new IllegalArgumentException(message);
        }
    }
}
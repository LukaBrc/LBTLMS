package com.lbt.services;

import com.lbt.entities.Member;
import com.lbt.repositories.MemberRepository;
import com.lbt.services.cache.AbstractEntityCache;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MemberCache extends AbstractEntityCache<Member, String> {

    private final MemberRepository memberRepository;

    public MemberCache(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    protected List<Member> loadAll() {
        return memberRepository.findAll();
    }

    @Override
    protected String extractKey(Member member) {
        return member.getMemberId();
    }

    @Override
    @Scheduled(fixedRateString = "${member.cache.refresh-interval-ms:300000}")
    public void refresh() {
        super.refresh();
    }
}

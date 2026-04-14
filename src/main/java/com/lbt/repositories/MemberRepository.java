package com.lbt.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.lbt.entities.Member;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByName(String name);

    Member findByMemberId(String memberId);

    boolean existsByMemberId(String memberId);

    @Transactional
    void deleteByMemberId(String memberId);

    default boolean delete(String memberId) {
        if (existsByMemberId(memberId)) {
            deleteByMemberId(memberId);
            return true;
        }
        return false;
    }
}

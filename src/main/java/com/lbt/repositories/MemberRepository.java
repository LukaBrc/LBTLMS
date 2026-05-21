package com.lbt.repositories;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.lbt.entities.Member;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByName(String name);

    Member findByMemberId(String memberId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM Member m WHERE m.memberId = :memberId")
    Member findByMemberIdForUpdate(@Param("memberId") String memberId);

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

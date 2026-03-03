package com.lbt.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lbt.entities.Member;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, String> {
	
	Optional<Member> findByName(String name);	

	default Member findMemberById(String memberId) {
        return findById(memberId).orElse(null);
    }

    default boolean existsById(String memberId) {
        return existsById(memberId);
    }

    default boolean delete(String memberId) {
        if (existsById(memberId)) {
            deleteById(memberId);
            return true;
        }
        return false;
    }
}
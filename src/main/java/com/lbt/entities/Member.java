package com.lbt.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "members")
public class Member {

    @Id
    @Column(name = "member_id", length = 50)
    private String memberId;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "contact", nullable = false, length = 200)
    private String contact;

    private final List<String> borrowedIsbns = new ArrayList<>();

    private static final int MAX_BORROW = 5;

    public boolean canBorrow() {
        return borrowedIsbns.size() < MAX_BORROW;
    }

    public void borrowBook(String isbn) {
        if (!borrowedIsbns.contains(isbn)) borrowedIsbns.add(isbn);
    }

    public void returnBook(String isbn) {
        borrowedIsbns.remove(isbn);
    }

    public void setBorrowedIsbns(List<String> isbns) {
        borrowedIsbns.clear();
        borrowedIsbns.addAll(isbns);
    }

    @Override
    public String toString() {
        return "Member: " + name + " (ID:" + memberId + ") Borrowed: " + borrowedIsbns.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Member member = (Member) o;
        return Objects.equals(memberId, member.memberId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(memberId);
    }
}
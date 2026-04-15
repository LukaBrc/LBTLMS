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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", length = 50, unique = true, nullable = false)
    private String memberId;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "contact", nullable = false, length = 200)
    private String contact;

    @ElementCollection
    @CollectionTable(name = "member_borrowed_isbns", joinColumns = @JoinColumn(name = "member_id"))
    @Column(name = "isbn")
    private List<String> borrowedIsbns = new ArrayList<>();

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

    public Long getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Member member = (Member) o;
        return Objects.equals(id, member.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
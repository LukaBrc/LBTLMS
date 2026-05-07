package com.lbt.entities;

import com.lbt.validation.Validatable;
import com.lbt.validation.ValidationError;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "authors")
public class Author implements Validatable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Builder.Default
    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @Override
    public List<ValidationError> getValidationErrors() {
        List<ValidationError> errors = new ArrayList<>();
        if (name == null || name.trim().isEmpty()) {
            errors.add(new ValidationError("name", "Author name must not be empty."));
        } else if (name.length() > 150) {
            errors.add(new ValidationError("name", "Author name must not exceed 150 characters."));
        }
        return errors;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Author author = (Author) o;
        return Objects.equals(id, author.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Author{id=%d, name='%s', deleted=%s}", id, name, deleted);
    }
}

# 📚 Library Management System (LMS)

A Java Spring Boot application providing RESTful APIs to manage a library's books, authors, members, and borrow transactions.

---

## ✨ Features

- **Author Management:** Create, update, soft-delete, and list authors with in-memory caching.
- **Book Management:** Add, update, delete, and view books linked to authors.
- **Member Management:** Register, update, delete, and view library members.
- **Borrowing System:** Borrow and return books with transaction tracking, 14-day due dates, overdue detection, and a 5-book limit per member.
- **Author Cache:** `ConcurrentHashMap`-backed cache with scheduled refresh (configurable interval, default 5 minutes).
- **Validation:** Custom `ValidationHandler` for entity-level rules plus Jakarta Bean Validation on DTOs.
- **Global Exception Handling:** Centralized error responses via `@RestControllerAdvice`.
- **Layered Architecture:** Controllers → Services → Repositories → Entities, with separate DTOs for request/response.

---

## 📁 Project Structure

```
src/
 ├── main/
 │    ├── java/com/lbt/
 │    │    ├── controllers/
 │    │    │    ├── AuthorController.java
 │    │    │    ├── BookController.java
 │    │    │    ├── BorrowController.java
 │    │    │    ├── GlobalExceptionHandler.java
 │    │    │    └── MemberController.java
 │    │    ├── dto/
 │    │    │    ├── AuthorRequest.java
 │    │    │    ├── AuthorResponse.java
 │    │    │    ├── BookRequest.java
 │    │    │    ├── BookResponse.java
 │    │    │    ├── BorrowRequest.java
 │    │    │    ├── MemberRequest.java
 │    │    │    └── MemberResponse.java
 │    │    ├── entities/
 │    │    │    ├── Author.java
 │    │    │    ├── Book.java
 │    │    │    ├── BorrowTransaction.java
 │    │    │    └── Member.java
 │    │    ├── repositories/
 │    │    │    ├── AuthorRepository.java
 │    │    │    ├── BookRepository.java
 │    │    │    ├── BorrowTransactionRepository.java
 │    │    │    └── MemberRepository.java
 │    │    ├── services/
 │    │    │    ├── AuthorCache.java
 │    │    │    ├── AuthorService.java
 │    │    │    ├── BookService.java
 │    │    │    ├── BorrowTransactionService.java
 │    │    │    ├── MemberService.java
 │    │    │    └── ValidationHandler.java
 │    │    └── lms.java
 │    └── resources/
 │         └── application.properties
 └── test/
      ├── java/com/lbt/
      │    ├── AuthorCacheTest.java
      │    ├── AuthorControllerTest.java
      │    ├── AuthorEntityToDtoMappingPropertyTest.java
      │    ├── AuthorNameLengthValidationPropertyTest.java
      │    ├── BookControllerTest.java
      │    ├── BookResponseAuthorInfoPropertyTest.java
      │    ├── BookServiceTest.java
      │    ├── BorrowControllerTest.java
      │    ├── BorrowTransactionServiceTest.java
      │    ├── BugConditionApiTest.java
      │    ├── BugConditionExplorationTest.java
      │    ├── BugConditionValidationHandlerTest.java
      │    ├── CacheReflectsWritesPropertyTest.java
      │    ├── CreateAuthorRoundTripPropertyTest.java
      │    ├── EntityTest.java
      │    ├── InvalidNameRejectionPropertyTest.java
      │    ├── LMSTests.java
      │    ├── MemberControllerTest.java
      │    ├── MemberServiceTest.java
      │    ├── ModifiedBookComponentsTest.java
      │    ├── PreservationTest.java
      │    ├── SoftDeleteSetsFlagPropertyTest.java
      │    ├── SoftDeleteVisibilityPropertyTest.java
      │    └── UpdatePersistsNewValuesPropertyTest.java
      └── resources/
           └── application-test.properties
```

---

## 🚀 Technologies

- Java 17
- Spring Boot 4.0.2
- Spring Data JPA / Hibernate
- Spring Validation (Jakarta Bean Validation)
- Spring Scheduling (cache refresh)
- MySQL (production) / H2 (testing)
- Lombok
- jqwik (property-based testing)

---

## 🌐 API Endpoints

All endpoints are prefixed with `/api/v1`.

### Authors (`/api/v1/authors`)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/authors` | Create an author |
| GET | `/api/v1/authors` | List all authors |
| GET | `/api/v1/authors/{id}` | Get author by ID |
| PUT | `/api/v1/authors/{id}` | Update an author |
| DELETE | `/api/v1/authors/{id}` | Soft-delete an author |

### Books (`/api/v1/books`)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/books` | Add a book |
| GET | `/api/v1/books` | List all books |
| GET | `/api/v1/books/{isbn}` | Get book by ISBN |
| PUT | `/api/v1/books/{isbn}` | Update a book |
| DELETE | `/api/v1/books/{isbn}` | Remove a book |

### Members (`/api/v1/members`)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/members` | Register a member |
| GET | `/api/v1/members` | List all members |
| GET | `/api/v1/members/{memberId}` | Get member by ID |
| PUT | `/api/v1/members/{memberId}` | Update a member |
| DELETE | `/api/v1/members/{memberId}` | Delete a member |

### Borrows (`/api/v1/borrows`)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/borrows` | Borrow a book |
| POST | `/api/v1/borrows/return` | Return a book |
| GET | `/api/v1/borrows/overdue` | List overdue transactions |

---

## 🧪 Testing

Tests are in `src/test/java/com/lbt/` and use H2 as an in-memory database (`application-test.properties`).

The suite includes:
- Unit tests for controllers, services, entities, and the validation handler.
- Property-based tests using jqwik covering author name validation, entity-to-DTO mapping, soft-delete behavior, cache consistency, and round-trip persistence.

Run all tests:
```bash
./mvnw test
```

---

## ⚙️ Configuration

Configuration lives in `src/main/resources/application.properties`.

| Property | Default | Description |
|----------|---------|-------------|
| `server.port` | `8080` | HTTP server port |
| `spring.datasource.url` | `jdbc:mysql://localhost:3306/library` | MySQL connection URL |
| `spring.datasource.username` | `lbt_user` | Database username |
| `spring.datasource.password` | `${DB_PASSWORD}` | Database password (set via environment variable) |
| `spring.jpa.hibernate.ddl-auto` | `validate` | Schema management strategy |
| `author.cache.refresh-interval-ms` | `300000` | Author cache refresh interval in milliseconds |

Set the `DB_PASSWORD` environment variable before running the application.

# 📚 Library Management System (LBTLMS)

A Java Spring Boot application providing RESTful APIs to manage a library's books, authors, members, and borrow transactions.

---

## ✨ Features

- **Author Management:** Create, update, soft-delete, and list active authors.
- **Book Management:** Add, update, delete, and view books linked to authors.
- **Member Management:** Register, update, delete, and view members.
- **Borrowing System:** Borrow and return one copy per request with transaction tracking, overdue detection, and 5-active-borrow limit per member.
- **Duplicate Copy Support:** Same member can hold multiple active borrows of the same ISBN; return closes the oldest active transaction.
- **Copy Accounting:** `totalCopies` updates preserve borrowed count and recompute `availableCopies` safely.
- **Caching:** Caffeine-backed `Author`, `Book`, and `Member` caches with startup snapshot load, write-through updates, scheduled refresh, and stale-data retention on refresh failure.
- **Validation:** Centralized `ValidationHandler` for entity rules plus Jakarta Bean Validation on DTOs.
- **Global Error Handling:** Centralized exception responses via `GlobalExceptionHandler`.

---

## 📁 Project Structure

```text
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
 │    │    │    ├── cache/
 │    │    │    │    └── AbstractEntityCache.java
 │    │    │    ├── AuthorCache.java
 │    │    │    ├── AuthorService.java
 │    │    │    ├── BookCache.java
 │    │    │    ├── BookService.java
 │    │    │    ├── BorrowTransactionService.java
 │    │    │    ├── MemberCache.java
 │    │    │    └── MemberService.java
 │    │    ├── validation/
 │    │    │    ├── Validatable.java
 │    │    │    ├── ValidationError.java
 │    │    │    ├── ValidationHandler.java
 │    │    │    └── ValidationHandlerResolver.java
 │    │    └── lms.java
 │    └── resources/
 │         ├── application.properties
 │         ├── application-local.properties
 │         └── application-docker.properties
 └── test/
      ├── java/com/lbt/
      │    └── ... unit, property-based, and integration tests
      └── resources/
           └── application-test.properties
```

---

## 🚀 Technologies

- Java 17
- Spring Boot 4.0.2
- Spring Data JPA / Hibernate
- Spring Validation (Jakarta Bean Validation)
- Spring Scheduling
- Caffeine
- MySQL (runtime) / H2 (testing)
- Lombok
- jqwik (property-based testing)

---

## 🌐 API Endpoints

All endpoints are prefixed with `/api/v1`.

### Authors (`/api/v1/authors`)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/authors` | Create an author |
| GET | `/api/v1/authors` | List active authors (optional `name` filter) |
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
| POST | `/api/v1/borrows` | Borrow one copy |
| POST | `/api/v1/borrows/return` | Return one copy |
| GET | `/api/v1/borrows/overdue` | List overdue transactions |

---

## ⚙️ Configuration

Configuration files:

- `src/main/resources/application.properties`
- `src/main/resources/application-local.properties`
- `src/main/resources/application-docker.properties`

`application-local.properties` imports `.env` from project root:

- `spring.config.import=optional:file:.env[.properties]`

| Property | Default | Description |
|----------|---------|-------------|
| `server.port` | `${SERVER_PORT:8081}` | HTTP server port |
| `spring.datasource.url` (local) | `jdbc:mysql://localhost:3306/library?...` | MySQL URL when app runs on host |
| `spring.datasource.url` (docker) | `jdbc:mysql://mysql:3306/library?...` | MySQL URL when app runs in compose network |
| `spring.datasource.username` | `lbt_user` | Database username |
| `spring.datasource.password` | `${SPRING_DATASOURCE_PASSWORD:${DB_PASSWORD:}}` | Database password |
| `author.cache.refresh-interval-ms` | `300000` | Author cache refresh interval |
| `book.cache.refresh-interval-ms` | `300000` | Book cache refresh interval |
| `member.cache.refresh-interval-ms` | `300000` | Member cache refresh interval |

---

## ▶️ Running the Application (Windows / PowerShell)

Run commands from the project root.

Start only MySQL (useful for IntelliJ local debug):

```powershell
docker compose up -d mysql
```

Run app locally with the `local` profile:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

Run full stack in Docker:

```powershell
docker compose up -d
```

Package jar:

```powershell
.\mvnw.cmd clean package
```

Default API base URL:

- `http://localhost:8081`

---

## 🧪 Testing

Tests are in `src/test/java/com/lbt/` and use H2 (`src/test/resources/application-test.properties`).

Run all tests:

```powershell
.\mvnw.cmd test
```

Run a focused class:

```powershell
.\mvnw.cmd "-Dtest=BorrowBookAvailabilityIntegrationTest" test
```


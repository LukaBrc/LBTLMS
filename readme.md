# Library Management System (LBTLMS)

Spring Boot REST API for managing authors, books, members, and borrow/return transactions.

## Highlights

- Layered architecture: `controllers -> services -> repositories -> entities`.
- Centralized validation via `ValidationHandler` and `Validatable` default methods.
- Caffeine-backed caches for `Author`, `Book`, and `Member` with scheduled refresh.
- Soft-delete for authors (`deleted=true`) while keeping active queries clean.
- Borrow policy: max 5 active borrows per member.
- Duplicate active borrows of the same ISBN are supported.
- Return flow closes one active borrow transaction (oldest first) per `(isbn, memberId)`.
- Copy accounting preserves borrowed count when `totalCopies` changes.

## Tech stack

- Java 17
- Spring Boot 4.0.2
- Spring Data JPA + Hibernate
- Spring Validation (Jakarta Bean Validation)
- Spring Scheduling
- Caffeine cache
- MySQL (runtime) and H2 (tests)
- Lombok
- jqwik (property-based testing)

## Project layout

Key packages under `src/main/java/com/lbt`:

- `controllers` - REST endpoints
- `dto` - request/response payloads
- `entities` - JPA models
- `repositories` - Spring Data repositories
- `services` - business logic and caches
- `services/cache` - generic cache base class
- `validation` - centralized validation contracts/handler

### Project structure

```text
src/
  main/
	java/com/lbt/
	  controllers/
		AuthorController.java
		BookController.java
		BorrowController.java
		GlobalExceptionHandler.java
		MemberController.java
	  dto/
		AuthorRequest.java
		AuthorResponse.java
		BookRequest.java
		BookResponse.java
		BorrowRequest.java
		MemberRequest.java
		MemberResponse.java
	  entities/
		Author.java
		Book.java
		BorrowTransaction.java
		Member.java
	  repositories/
		AuthorRepository.java
		BookRepository.java
		BorrowTransactionRepository.java
		MemberRepository.java
	  services/
		cache/
		  AbstractEntityCache.java
		AuthorCache.java
		AuthorService.java
		BookCache.java
		BookService.java
		BorrowTransactionService.java
		MemberCache.java
		MemberService.java
	  validation/
		Validatable.java
		ValidationError.java
		ValidationHandler.java
		ValidationHandlerResolver.java
	  lms.java
	resources/
	  application.properties
	  application-local.properties
	  application-docker.properties
  test/
	java/com/lbt/
	  ... unit, property-based, and integration tests
	resources/
	  application-test.properties
```

## API endpoints

All endpoints are prefixed with `/api/v1`.

### Authors

| Method | Path | Description |
|---|---|---|
| POST | `/api/v1/authors` | Create author |
| GET | `/api/v1/authors` | List active authors (optional `name` filter) |
| GET | `/api/v1/authors/{id}` | Get author by id |
| PUT | `/api/v1/authors/{id}` | Update author |
| DELETE | `/api/v1/authors/{id}` | Soft-delete author |

### Books

| Method | Path | Description |
|---|---|---|
| POST | `/api/v1/books` | Create book |
| GET | `/api/v1/books` | List books |
| GET | `/api/v1/books/{isbn}` | Get book by ISBN |
| PUT | `/api/v1/books/{isbn}` | Update book metadata/copies |
| DELETE | `/api/v1/books/{isbn}` | Delete book |

### Members

| Method | Path | Description |
|---|---|---|
| POST | `/api/v1/members` | Register member |
| GET | `/api/v1/members` | List members |
| GET | `/api/v1/members/{memberId}` | Get member by memberId |
| PUT | `/api/v1/members/{memberId}` | Update member |
| DELETE | `/api/v1/members/{memberId}` | Delete member |

### Borrows

| Method | Path | Description |
|---|---|---|
| POST | `/api/v1/borrows` | Borrow one copy |
| POST | `/api/v1/borrows/return` | Return one copy |
| GET | `/api/v1/borrows/overdue` | List overdue active borrows |

## Profiles and configuration

Configuration files:

- `src/main/resources/application.properties`
- `src/main/resources/application-local.properties`
- `src/main/resources/application-docker.properties`

`application-local.properties` imports `.env` from project root:

- `spring.config.import=optional:file:.env[.properties]`

Datasource settings are Spring-standard env based:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

Server port defaults to `8081`:

- `server.port=${SERVER_PORT:8081}`

Cache refresh intervals (default 5 minutes):

- `author.cache.refresh-interval-ms`
- `book.cache.refresh-interval-ms`
- `member.cache.refresh-interval-ms`

## Running locally (Windows / PowerShell)

Run commands from the project root directory.

Start MySQL container only:

```powershell
docker compose up -d mysql
```

Run app with local profile:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

The API is available at:

- `http://localhost:8081`

Run app + MySQL in Docker:

```powershell
docker compose up -d
```

Package jar:

```powershell
.\mvnw.cmd clean package
```

## Running tests

Run full suite:

```powershell
.\mvnw.cmd test
```

Run a focused class:

```powershell
.\mvnw.cmd "-Dtest=BorrowBookAvailabilityIntegrationTest" test
```

Tests use H2 via `src/test/resources/application-test.properties`.

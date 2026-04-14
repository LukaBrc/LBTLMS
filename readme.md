# 📚 Library Management System (LMS)

A **Java Spring Boot** application providing RESTful APIs to manage a library's books, members, and borrow transactions.

---

## ✨ Features

- **Book Management:** Add, update, delete, and view books.
- **Member Management:** Register, update, delete, and view library members.
- **Borrowing System:** Borrow and return books with transaction tracking.
- **Robust Architecture:** Layered structure with controllers, services, repositories, DTOs, and entities.
- **Validation & Exception Handling:** Ensures reliable and user-friendly API responses.
- **Configuration:** Managed via `application.properties`.
- **Unit Testing:** Comprehensive tests for code quality and reliability.

---

## 📁 Project Structure

```
src/
 ├── main/
 │    ├── java/
 │    │    └── com/lbt/
 │    │         ├── controllers/
 │    │         ├── dto/
 │    │         ├── entities/
 │    │         ├── repositories/
 │    │         ├── services/
 │    │         └── lms.java
 │    └── resources/
 │         └── application.properties
 └── test/
      └── java/com/lbt/
           ├── BookControllerTest.java
           ├── BookServiceTest.java
           ├── BorrowControllerTest.java
           ├── BorrowTransactionServiceTest.java
           ├── BugConditionApiTest.java
           ├── BugConditionExplorationTest.java
           ├── BugConditionValidationHandlerTest.java
           ├── EntityTest.java
           ├── LMSTests.java
           ├── MemberControllerTest.java
           ├── MemberServiceTest.java
           └── PreservationTest.java
```

---

## 🚀 Technologies

- Java 17
- Spring Boot 4.0.2
- JPA/Hibernate
- MySQL (production)
- H2 (testing)
- Lombok
- Spring Validation

---

## 🧪 Testing

Unit tests are located in `src/test/java/com/lbt/`. Test files cover controllers, services, entities, and bug condition validation.

---

## ⚙️ Configuration

All configuration is managed in `src/main/resources/application.properties`.

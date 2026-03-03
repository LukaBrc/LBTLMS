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
 │    │    └── com/lbt/lms/
 │    │         ├── controllers/
 │    │         ├── dto/
 │    │         ├── entities/
 │    │         ├── repositories/
 │    │         └── services/
 │    └── resources/
 │         └── application.properties
 └── test/
      └── java/com/LBT/LMS/LMSTests.java
```

---

## 🚀 Technologies

- Java
- Spring Boot
- JPA/Hibernate

---

## 🧪 Testing

Unit tests are located in `src/test/java/com/LBT/LMS/LMSTests.java`.

---

## ⚙️ Configuration

All configuration is managed in `src/main/resources/application.properties`.

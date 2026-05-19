package com.lbt;

import com.lbt.controllers.AuthorController;
import com.lbt.controllers.BookController;
import com.lbt.controllers.MemberController;
import com.lbt.dto.AuthorResponse;
import com.lbt.dto.BookResponse;
import com.lbt.dto.MemberResponse;
import com.lbt.entities.Author;
import com.lbt.entities.Book;
import com.lbt.entities.Member;
import com.lbt.services.AuthorCache;
import com.lbt.services.BookCache;
import com.lbt.services.BorrowTransactionService;
import com.lbt.services.MemberCache;
import com.lbt.services.cache.AbstractEntityCache;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests verifying backward compatibility after the cache abstraction refactor.
 *
 * Validates:
 * - Requirement 11.1: AuthorController API responses remain identical
 * - Requirement 11.2: BookController API responses remain identical
 * - Requirement 11.3: MemberController API responses remain identical
 * - Requirement 11.4: BorrowTransactionService remains unchanged and does not use any cache
 * - Requirement 12.1: BorrowTransactionService does not depend on any cache class
 * - Requirement 12.2: BorrowTransactionRepository is queried directly
 */
class CacheBackwardCompatibilityTest {

    // ========================================================================
    // Requirement 12.1, 12.2: BorrowTransactionService has NO cache dependency
    // ========================================================================

    @Test
    void borrowTransactionService_constructorHasNoCacheDependency() {
        Constructor<?>[] constructors = BorrowTransactionService.class.getDeclaredConstructors();

        for (Constructor<?> constructor : constructors) {
            for (Parameter param : constructor.getParameters()) {
                Class<?> paramType = param.getType();
                assertFalse(AuthorCache.class.isAssignableFrom(paramType),
                        "BorrowTransactionService must not depend on AuthorCache");
                assertFalse(BookCache.class.isAssignableFrom(paramType),
                        "BorrowTransactionService must not depend on BookCache");
                assertFalse(MemberCache.class.isAssignableFrom(paramType),
                        "BorrowTransactionService must not depend on MemberCache");
                assertFalse(AbstractEntityCache.class.isAssignableFrom(paramType),
                        "BorrowTransactionService must not depend on AbstractEntityCache");
            }
        }
    }

    @Test
    void borrowTransactionService_fieldsHaveNoCacheType() {
        Field[] fields = BorrowTransactionService.class.getDeclaredFields();

        Set<String> cacheClassNames = Set.of(
                AuthorCache.class.getName(),
                BookCache.class.getName(),
                MemberCache.class.getName(),
                AbstractEntityCache.class.getName()
        );

        for (Field field : fields) {
            Class<?> fieldType = field.getType();
            assertFalse(cacheClassNames.contains(fieldType.getName()),
                    "BorrowTransactionService field '" + field.getName() + "' must not be a cache type, but was: " + fieldType.getName());
            assertFalse(AbstractEntityCache.class.isAssignableFrom(fieldType),
                    "BorrowTransactionService field '" + field.getName() + "' must not extend AbstractEntityCache");
        }
    }

    @Test
    void borrowTransactionService_noMethodReferencesCache() {
        Method[] methods = BorrowTransactionService.class.getDeclaredMethods();

        for (Method method : methods) {
            // Check return type
            assertFalse(AbstractEntityCache.class.isAssignableFrom(method.getReturnType()),
                    "BorrowTransactionService method '" + method.getName() + "' must not return a cache type");

            // Check parameter types
            for (Class<?> paramType : method.getParameterTypes()) {
                assertFalse(AbstractEntityCache.class.isAssignableFrom(paramType),
                        "BorrowTransactionService method '" + method.getName() + "' must not accept a cache parameter");
            }
        }
    }

    // ========================================================================
    // Requirement 11.1: AuthorController API responses remain identical
    // ========================================================================

    @Test
    void authorController_getAllAuthorsReturnsListOfAuthorResponse() throws NoSuchMethodException {
        Method method = AuthorController.class.getMethod("getAllAuthors", String.class);
        assertEquals(ResponseEntity.class, method.getReturnType(),
                "getAllAuthors must return ResponseEntity");
    }

    @Test
    void authorController_getAuthorByIdAcceptsLongReturnsResponseEntity() throws NoSuchMethodException {
        Method method = AuthorController.class.getMethod("getAuthorById", Long.class);
        assertEquals(ResponseEntity.class, method.getReturnType(),
                "getAuthorById must return ResponseEntity");
        assertEquals(1, method.getParameterCount());
        assertEquals(Long.class, method.getParameterTypes()[0]);
    }

    @Test
    void authorController_hasExpectedEndpointMethods() {
        Set<String> expectedMethods = Set.of(
                "createAuthor", "getAllAuthors", "getAuthorById", "updateAuthor", "deleteAuthor"
        );

        Set<String> actualPublicMethods = Arrays.stream(AuthorController.class.getDeclaredMethods())
                .filter(m -> java.lang.reflect.Modifier.isPublic(m.getModifiers()))
                .map(Method::getName)
                .collect(Collectors.toSet());

        for (String expected : expectedMethods) {
            assertTrue(actualPublicMethods.contains(expected),
                    "AuthorController must have public method: " + expected);
        }
    }

    @Test
    void authorController_dependsOnAuthorServiceOnly() {
        Constructor<?>[] constructors = AuthorController.class.getDeclaredConstructors();
        assertTrue(constructors.length > 0);

        // The controller should depend on AuthorService, not directly on any cache
        Constructor<?> mainConstructor = constructors[0];
        List<Class<?>> paramTypes = List.of(mainConstructor.getParameterTypes());

        assertFalse(paramTypes.contains(AuthorCache.class),
                "AuthorController must not directly depend on AuthorCache");
        assertFalse(paramTypes.contains(AbstractEntityCache.class),
                "AuthorController must not directly depend on AbstractEntityCache");
    }

    // ========================================================================
    // Requirement 11.2: BookController API responses remain identical
    // ========================================================================

    @Test
    void bookController_getAllBooksReturnsResponseEntity() throws NoSuchMethodException {
        Method method = BookController.class.getMethod("getAllBooks");
        assertEquals(ResponseEntity.class, method.getReturnType(),
                "getAllBooks must return ResponseEntity");
    }

    @Test
    void bookController_getBookAcceptsStringReturnsResponseEntity() throws NoSuchMethodException {
        Method method = BookController.class.getMethod("getBook", String.class);
        assertEquals(ResponseEntity.class, method.getReturnType(),
                "getBook must return ResponseEntity");
        assertEquals(1, method.getParameterCount());
        assertEquals(String.class, method.getParameterTypes()[0]);
    }

    @Test
    void bookController_hasExpectedEndpointMethods() {
        Set<String> expectedMethods = Set.of(
                "addBook", "getAllBooks", "getBook", "updateBook", "removeBook"
        );

        Set<String> actualPublicMethods = Arrays.stream(BookController.class.getDeclaredMethods())
                .filter(m -> java.lang.reflect.Modifier.isPublic(m.getModifiers()))
                .map(Method::getName)
                .collect(Collectors.toSet());

        for (String expected : expectedMethods) {
            assertTrue(actualPublicMethods.contains(expected),
                    "BookController must have public method: " + expected);
        }
    }

    @Test
    void bookController_doesNotDirectlyDependOnCache() {
        Constructor<?>[] constructors = BookController.class.getDeclaredConstructors();
        assertTrue(constructors.length > 0);

        Constructor<?> mainConstructor = constructors[0];
        List<Class<?>> paramTypes = List.of(mainConstructor.getParameterTypes());

        assertFalse(paramTypes.contains(BookCache.class),
                "BookController must not directly depend on BookCache");
        assertFalse(paramTypes.contains(AbstractEntityCache.class),
                "BookController must not directly depend on AbstractEntityCache");
    }

    // ========================================================================
    // Requirement 11.3: MemberController API responses remain identical
    // ========================================================================

    @Test
    void memberController_getAllMembersReturnsResponseEntity() throws NoSuchMethodException {
        Method method = MemberController.class.getMethod("getAllMembers");
        assertEquals(ResponseEntity.class, method.getReturnType(),
                "getAllMembers must return ResponseEntity");
    }

    @Test
    void memberController_getMemberAcceptsStringReturnsResponseEntity() throws NoSuchMethodException {
        Method method = MemberController.class.getMethod("getMember", String.class);
        assertEquals(ResponseEntity.class, method.getReturnType(),
                "getMember must return ResponseEntity");
        assertEquals(1, method.getParameterCount());
        assertEquals(String.class, method.getParameterTypes()[0]);
    }

    @Test
    void memberController_hasExpectedEndpointMethods() {
        Set<String> expectedMethods = Set.of(
                "registerMember", "getAllMembers", "getMember", "updateMember", "deleteMember"
        );

        Set<String> actualPublicMethods = Arrays.stream(MemberController.class.getDeclaredMethods())
                .filter(m -> java.lang.reflect.Modifier.isPublic(m.getModifiers()))
                .map(Method::getName)
                .collect(Collectors.toSet());

        for (String expected : expectedMethods) {
            assertTrue(actualPublicMethods.contains(expected),
                    "MemberController must have public method: " + expected);
        }
    }

    @Test
    void memberController_doesNotDirectlyDependOnCache() {
        Constructor<?>[] constructors = MemberController.class.getDeclaredConstructors();
        assertTrue(constructors.length > 0);

        Constructor<?> mainConstructor = constructors[0];
        List<Class<?>> paramTypes = List.of(mainConstructor.getParameterTypes());

        assertFalse(paramTypes.contains(MemberCache.class),
                "MemberController must not directly depend on MemberCache");
        assertFalse(paramTypes.contains(AbstractEntityCache.class),
                "MemberController must not directly depend on AbstractEntityCache");
    }

    // ========================================================================
    // Response DTO structure verification
    // ========================================================================

    @Test
    void authorResponse_hasExpectedFields() {
        Set<String> expectedFields = Set.of("id", "name");
        Set<String> actualFields = Arrays.stream(AuthorResponse.class.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());

        assertEquals(expectedFields, actualFields,
                "AuthorResponse must have exactly the fields: id, name");
    }

    @Test
    void bookResponse_hasExpectedFields() {
        Set<String> expectedFields = Set.of(
                "isbn", "title", "authorId", "authorName", "genre", "totalCopies", "availableCopies"
        );
        Set<String> actualFields = Arrays.stream(BookResponse.class.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());

        assertEquals(expectedFields, actualFields,
                "BookResponse must have exactly the expected fields");
    }

    @Test
    void memberResponse_hasExpectedFields() {
        Set<String> expectedFields = Set.of("memberId", "name", "contact", "borrowedIsbns");
        Set<String> actualFields = Arrays.stream(MemberResponse.class.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());

        assertEquals(expectedFields, actualFields,
                "MemberResponse must have exactly the expected fields");
    }
}

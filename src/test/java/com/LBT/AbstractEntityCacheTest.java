package com.lbt;

import com.lbt.services.cache.AbstractEntityCache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AbstractEntityCache.
 * Validates: Requirements 4.3, 3.3
 */
class AbstractEntityCacheTest {

    /**
     * Concrete test subclass that caches String entities keyed by String.
     * The key is the string itself (identity function).
     */
    static class TestStringCache extends AbstractEntityCache<String, String> {

        List<String> entities = Collections.emptyList();

        @Override
        protected List<String> loadAll() {
            return new ArrayList<>(entities);
        }

        @Override
        protected String extractKey(String entity) {
            return entity;
        }
    }

    private TestStringCache cache;

    @BeforeEach
    void setUp() {
        cache = new TestStringCache();
        cache.entities = Collections.emptyList();
        cache.init();
    }

    // --- init() with empty list (Requirement 3.3) ---

    @Test
    @DisplayName("init() with empty list results in empty but functional cache")
    void initWithEmptyListResultsInEmptyButFunctionalCache() {
        // Cache was initialized with empty list in setUp
        assertTrue(cache.getAll().isEmpty(), "Cache should be empty after init with empty list");
        assertTrue(cache.getById("anything").isEmpty(), "getById should return empty for any key");

        // Verify cache is still functional — can put and retrieve
        cache.put("hello");
        assertTrue(cache.getById("hello").isPresent(), "Cache should be functional after empty init");
        assertEquals("hello", cache.getById("hello").get());
        assertEquals(1, cache.getAll().size());
    }

    // --- evict() non-existent key (Requirement 4.3) ---

    @Test
    @DisplayName("evict() on non-existent key completes without throwing")
    void evictNonExistentKeyDoesNotThrow() {
        assertDoesNotThrow(() -> cache.evict("non-existent-key"));
    }

    @Test
    @DisplayName("evict() on non-existent key does not affect existing entries")
    void evictNonExistentKeyDoesNotAffectExistingEntries() {
        cache.put("alpha");
        cache.put("beta");

        cache.evict("gamma");

        assertEquals(2, cache.getAll().size());
        assertTrue(cache.getById("alpha").isPresent());
        assertTrue(cache.getById("beta").isPresent());
    }

    // --- put() overwrites existing entry (Requirement 4.3) ---

    @Test
    @DisplayName("put() overwrites existing entry with same key")
    void putOverwritesExistingEntryWithSameKey() {
        cache.put("entity-a");

        // Put again with the same key (since extractKey is identity, same string = same key)
        cache.put("entity-a");

        assertEquals(1, cache.getAll().size(), "Cache size should remain 1 after overwrite");
        assertTrue(cache.getById("entity-a").isPresent());
        assertEquals("entity-a", cache.getById("entity-a").get());
    }

    @Test
    @DisplayName("put() overwrites value for existing key — verified with distinguishable entities")
    void putOverwritesValueForExistingKeyDistinguishable() {
        // Use a cache with a different key strategy to demonstrate overwrite with different values
        AbstractEntityCache<String[], String> keyedCache = new AbstractEntityCache<>() {
            @Override
            protected List<String[]> loadAll() {
                return Collections.emptyList();
            }

            @Override
            protected String extractKey(String[] entity) {
                return entity[0]; // First element is the key
            }
        };
        keyedCache.init();

        String[] original = {"key1", "original-value"};
        keyedCache.put(original);
        assertEquals("original-value", keyedCache.getById("key1").get()[1]);

        String[] updated = {"key1", "updated-value"};
        keyedCache.put(updated);

        assertEquals(1, keyedCache.getAll().size(), "Cache size should remain 1 after overwrite");
        assertEquals("updated-value", keyedCache.getById("key1").get()[1],
                "Value should be updated after put with same key");
    }
}

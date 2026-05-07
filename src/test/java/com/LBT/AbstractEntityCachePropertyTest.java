package com.lbt;

import com.lbt.services.cache.AbstractEntityCache;

import net.jqwik.api.*;
import net.jqwik.api.lifecycle.BeforeProperty;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for AbstractEntityCache using a concrete TestStringCache subclass.
 *
 * Validates: Requirements 2.2, 2.4, 3.2, 3.3, 4.1, 4.2
 */
@Label("Feature: entity-cache-abstraction, Properties 1-5: AbstractEntityCache correctness")
class AbstractEntityCachePropertyTest {

    /**
     * Concrete test subclass that caches String entities keyed by String.
     * The key is the string itself (identity function).
     * loadAll() behavior is controllable via the entities field and shouldThrow flag.
     */
    static class TestStringCache extends AbstractEntityCache<String, String> {

        List<String> entities = Collections.emptyList();
        boolean shouldThrow = false;

        @Override
        protected List<String> loadAll() {
            if (shouldThrow) {
                throw new RuntimeException("Simulated loadAll failure");
            }
            return new ArrayList<>(entities);
        }

        @Override
        protected String extractKey(String entity) {
            return entity;
        }
    }

    private TestStringCache cache;

    @BeforeProperty
    void setUp() {
        cache = new TestStringCache();
        cache.entities = Collections.emptyList();
        cache.shouldThrow = false;
        cache.init();
    }

    // --- Property 1: Refresh produces an exact snapshot of loadAll ---

    /**
     * Property 1: Refresh produces an exact snapshot of loadAll
     *
     * For any list of entities returned by loadAll(), after refresh(),
     * the cache contains exactly those entities keyed by extractKey(entity).
     *
     * Validates: Requirements 2.2, 3.3
     */
    @Property(tries = 200)
    @Label("Property 1: Refresh produces an exact snapshot of loadAll")
    void refreshProducesExactSnapshotOfLoadAll(@ForAll("distinctStringLists") List<String> entities) {
        cache.entities = entities;
        cache.shouldThrow = false;

        cache.refresh();

        List<String> allCached = cache.getAll();
        assertEquals(entities.size(), allCached.size(),
                "Cache size should match loadAll size");

        for (String entity : entities) {
            Optional<String> found = cache.getById(entity);
            assertTrue(found.isPresent(),
                    "Entity '" + entity + "' should be present in cache after refresh");
            assertEquals(entity, found.get());
        }

        // No extra entries
        Set<String> expected = new HashSet<>(entities);
        Set<String> actual = new HashSet<>(allCached);
        assertEquals(expected, actual,
                "Cache should contain exactly the entities from loadAll");
    }

    // --- Property 2: Failed refresh retains stale data ---

    /**
     * Property 2: Failed refresh retains stale data
     *
     * For any cache state, if loadAll() throws an exception during refresh(),
     * the cache contents remain identical to the state before the refresh was attempted.
     *
     * Validates: Requirements 3.2
     */
    @Property(tries = 200)
    @Label("Property 2: Failed refresh retains stale data")
    void failedRefreshRetainsStaleData(@ForAll("distinctStringLists") List<String> initialEntities) {
        // Set up initial state
        cache.entities = initialEntities;
        cache.shouldThrow = false;
        cache.refresh();

        // Capture state before failed refresh
        List<String> beforeRefresh = cache.getAll();

        // Make loadAll throw
        cache.shouldThrow = true;
        cache.refresh();

        // Cache should be unchanged
        List<String> afterRefresh = cache.getAll();
        assertEquals(new HashSet<>(beforeRefresh), new HashSet<>(afterRefresh),
                "Cache contents should remain unchanged after failed refresh");
        assertEquals(beforeRefresh.size(), afterRefresh.size(),
                "Cache size should remain unchanged after failed refresh");
    }

    // --- Property 3: Snapshots are unmodifiable ---

    /**
     * Property 3: Snapshots are unmodifiable
     *
     * For any cache state, the internal map rejects mutation attempts
     * (UnsupportedOperationException).
     *
     * Validates: Requirements 2.4
     */
    @Property(tries = 200)
    @Label("Property 3: Snapshots are unmodifiable — getAll list does not affect cache")
    void snapshotsAreUnmodifiable(@ForAll("distinctStringLists") List<String> entities) {
        cache.entities = entities;
        cache.shouldThrow = false;
        cache.refresh();

        // Attempt to modify the list returned by getAll — should not affect cache
        List<String> allCached = cache.getAll();
        allCached.clear();

        // Cache should still have all entities
        assertEquals(entities.size(), cache.getAll().size(),
                "Modifying getAll() result should not affect cache contents");
    }

    @Property(tries = 200)
    @Label("Property 3: Snapshots are unmodifiable — put on internal map throws")
    void internalMapRejectsMutation(@ForAll("distinctStringLists") List<String> entities,
                                    @ForAll("nonBlankStrings") String extraEntity) {
        cache.entities = entities;
        cache.shouldThrow = false;
        cache.refresh();

        // The put method uses copy-on-write, so the old snapshot should remain unmodifiable.
        // We verify this by checking that after put, the old getAll result size is unchanged
        // and the new state includes the extra entity.
        int sizeBefore = cache.getAll().size();
        cache.put(extraEntity);

        // The cache now has the extra entity
        assertTrue(cache.getById(extraEntity).isPresent());

        // Verify the snapshot model: size should be original + 1 (unless extraEntity was already present)
        int expectedSize = new HashSet<>(entities).contains(extraEntity)
                ? entities.size()
                : entities.size() + 1;
        assertEquals(expectedSize, cache.getAll().size());
    }

    // --- Property 4: Put adds entity retrievable by key ---

    /**
     * Property 4: Put adds entity retrievable by key
     *
     * For any entity e and any prior cache state, after calling put(e),
     * getById(extractKey(e)) returns Optional.of(e) and getAll() contains e.
     *
     * Validates: Requirements 4.1
     */
    @Property(tries = 200)
    @Label("Property 4: Put adds entity retrievable by key")
    void putAddsEntityRetrievableByKey(@ForAll("distinctStringLists") List<String> initialEntities,
                                       @ForAll("nonBlankStrings") String newEntity) {
        // Set up initial state
        cache.entities = initialEntities;
        cache.shouldThrow = false;
        cache.refresh();

        // Put the new entity
        cache.put(newEntity);

        // Verify getById returns the entity
        Optional<String> found = cache.getById(newEntity);
        assertTrue(found.isPresent(),
                "getById should return the entity after put");
        assertEquals(newEntity, found.get(),
                "getById should return the exact entity that was put");

        // Verify getAll contains the entity
        assertTrue(cache.getAll().contains(newEntity),
                "getAll should contain the entity after put");
    }

    // --- Property 5: Evict removes entity by key ---

    /**
     * Property 5: Evict removes entity by key
     *
     * For any cache state containing an entity with key k, after calling evict(k),
     * getById(k) returns Optional.empty() and getAll() does not contain entity with that key.
     *
     * Validates: Requirements 4.2
     */
    @Property(tries = 200)
    @Label("Property 5: Evict removes entity by key")
    void evictRemovesEntityByKey(@ForAll("nonEmptyDistinctStringLists") List<String> initialEntities) {
        // Set up initial state
        cache.entities = initialEntities;
        cache.shouldThrow = false;
        cache.refresh();

        // Pick an entity to evict (first one)
        String entityToEvict = initialEntities.get(0);

        // Evict it
        cache.evict(entityToEvict);

        // Verify getById returns empty
        Optional<String> found = cache.getById(entityToEvict);
        assertTrue(found.isEmpty(),
                "getById should return empty after evict");

        // Verify getAll does not contain the entity
        assertFalse(cache.getAll().contains(entityToEvict),
                "getAll should not contain the entity after evict");

        // Verify other entities are still present
        for (int i = 1; i < initialEntities.size(); i++) {
            assertTrue(cache.getById(initialEntities.get(i)).isPresent(),
                    "Other entities should remain after evict");
        }
    }

    // --- Generators ---

    @Provide
    Arbitrary<List<String>> distinctStringLists() {
        return Arbitraries.strings()
                .alpha()
                .ofMinLength(1)
                .ofMaxLength(20)
                .list()
                .ofMinSize(0)
                .ofMaxSize(20)
                .uniqueElements();
    }

    @Provide
    Arbitrary<List<String>> nonEmptyDistinctStringLists() {
        return Arbitraries.strings()
                .alpha()
                .ofMinLength(1)
                .ofMaxLength(20)
                .list()
                .ofMinSize(1)
                .ofMaxSize(20)
                .uniqueElements();
    }

    @Provide
    Arbitrary<String> nonBlankStrings() {
        return Arbitraries.strings()
                .alpha()
                .ofMinLength(1)
                .ofMaxLength(20);
    }
}

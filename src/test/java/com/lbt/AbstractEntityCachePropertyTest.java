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

        Set<String> expected = new HashSet<>(entities);
        Set<String> actual = new HashSet<>(allCached);
        assertEquals(expected, actual,
                "Cache should contain exactly the entities from loadAll");
    }

    @Property(tries = 200)
    @Label("Property 2: Failed refresh retains stale data")
    void failedRefreshRetainsStaleData(@ForAll("distinctStringLists") List<String> initialEntities) {
        cache.entities = initialEntities;
        cache.shouldThrow = false;
        cache.refresh();

        List<String> beforeRefresh = cache.getAll();

        cache.shouldThrow = true;
        cache.refresh();

        List<String> afterRefresh = cache.getAll();
        assertEquals(new HashSet<>(beforeRefresh), new HashSet<>(afterRefresh),
                "Cache contents should remain unchanged after failed refresh");
        assertEquals(beforeRefresh.size(), afterRefresh.size(),
                "Cache size should remain unchanged after failed refresh");
    }

    @Property(tries = 200)
    @Label("Property 3: Snapshots are unmodifiable — getAll list does not affect cache")
    void snapshotsAreUnmodifiable(@ForAll("distinctStringLists") List<String> entities) {
        cache.entities = entities;
        cache.shouldThrow = false;
        cache.refresh();

        List<String> allCached = cache.getAll();
        allCached.clear();

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

        cache.put(extraEntity);

        assertTrue(cache.getById(extraEntity).isPresent());

        int expectedSize = new HashSet<>(entities).contains(extraEntity)
                ? entities.size()
                : entities.size() + 1;
        assertEquals(expectedSize, cache.getAll().size());
    }

    @Property(tries = 200)
    @Label("Property 4: Put adds entity retrievable by key")
    void putAddsEntityRetrievableByKey(@ForAll("distinctStringLists") List<String> initialEntities,
                                       @ForAll("nonBlankStrings") String newEntity) {
        cache.entities = initialEntities;
        cache.shouldThrow = false;
        cache.refresh();

        cache.put(newEntity);

        Optional<String> found = cache.getById(newEntity);
        assertTrue(found.isPresent(),
                "getById should return the entity after put");
        assertEquals(newEntity, found.get(),
                "getById should return the exact entity that was put");

        assertTrue(cache.getAll().contains(newEntity),
                "getAll should contain the entity after put");
    }

    @Property(tries = 200)
    @Label("Property 5: Evict removes entity by key")
    void evictRemovesEntityByKey(@ForAll("nonEmptyDistinctStringLists") List<String> initialEntities) {
        cache.entities = initialEntities;
        cache.shouldThrow = false;
        cache.refresh();

        String entityToEvict = initialEntities.get(0);

        cache.evict(entityToEvict);

        Optional<String> found = cache.getById(entityToEvict);
        assertTrue(found.isEmpty(),
                "getById should return empty after evict");

        assertFalse(cache.getAll().contains(entityToEvict),
                "getAll should not contain the entity after evict");

        for (int i = 1; i < initialEntities.size(); i++) {
            assertTrue(cache.getById(initialEntities.get(i)).isPresent(),
                    "Other entities should remain after evict");
        }
    }


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

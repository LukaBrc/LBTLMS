package com.lbt.services.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Generic abstract cache base class that provides thread-safe caching
 * backed by Caffeine with atomic snapshot replacement on init/refresh.
 *
 * @param <T> the entity type
 * @param <K> the cache key type
 */
public abstract class AbstractEntityCache<T, K> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private volatile Cache<K, T> cache = Caffeine.newBuilder().build();

    /**
     * Loads all entities from the underlying data source.
     *
     * @return list of all entities
     */
    protected abstract List<T> loadAll();

    /**
     * Extracts the cache key from the given entity.
     *
     * @param entity the entity to extract the key from
     * @return the cache key
     */
    protected abstract K extractKey(T entity);

    /**
     * Initializes the cache on application startup by loading all entities
     * and building an immutable snapshot.
     */
    @PostConstruct
    public void init() {
        this.cache = buildSnapshot(loadAll());
    }

    /**
     * Refreshes the cache by reloading all entities from the data source.
     * On failure, retains the previous snapshot and logs a warning.
     */
    public void refresh() {
        try {
            this.cache = buildSnapshot(loadAll());
        } catch (Exception e) {
            logger.warn("Cache refresh failed, retaining stale data", e);
        }
    }

    /**
     * Returns all cached entities as a new list.
     *
     * @return list of all cached entities
     */
    public List<T> getAll() {
        return new ArrayList<>(cache.asMap().values());
    }

    /**
     * Retrieves a cached entity by its key.
     *
     * @param key the cache key
     * @return an Optional containing the entity, or empty if not found
     */
    public Optional<T> getById(K key) {
        return Optional.ofNullable(cache.getIfPresent(key));
    }

    /**
     * Adds or updates an entity in the cache.
     *
     * @param entity the entity to add or update
     */
    public void put(T entity) {
        cache.put(extractKey(entity), entity);
    }

    /**
     * Removes an entity from the cache by key.
     * If the key is not present, completes without throwing an exception.
     *
     * @param key the key of the entity to remove
     */
    public void evict(K key) {
        cache.invalidate(key);
    }

    private Cache<K, T> buildSnapshot(List<T> entities) {
        Cache<K, T> newSnapshot = Caffeine.newBuilder().build();
        for (T entity : entities) {
            newSnapshot.put(extractKey(entity), entity);
        }
        return newSnapshot;
    }
}

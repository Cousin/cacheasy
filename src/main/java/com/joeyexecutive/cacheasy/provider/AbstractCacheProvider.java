package com.joeyexecutive.cacheasy.provider;

import com.joeyexecutive.cacheasy.annotation.Cached;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Base class for cache backends. A provider adapts one caching technology — Caffeine, Guava,
 * Ehcache, Redis, or a plain {@link java.util.concurrent.ConcurrentHashMap} — to the minimal
 * surface {@code CacheAspect} needs: build a cache from a {@link Cached}, and
 * put/get/remove/clear/size entries in it.
 *
 * <p>A single provider instance is shared by every {@code @Cached} method that targets its backend.
 * It holds one cache per distinct {@link Cached} annotation (see {@link #getCache}); the cache
 * object is handed back into each operation, so a provider stays stateless beyond that map.
 *
 * @param <C> the backend's native cache type (e.g. Caffeine's {@code Cache<String, Object>}).
 *            Keys are always {@code String}; values are always {@code Object}.
 */
@Getter
@RequiredArgsConstructor
public abstract class AbstractCacheProvider<C> {

    /** Human-readable backend name, e.g. {@code "Caffeine"}; exposed via {@code getName()}. */
    private final String name;

    // One cache per distinct @Cached. Populated lazily from caller threads (via the aspect),
    // so it must be concurrent.
    private final Map<Cached, C> cachedMap = new ConcurrentHashMap<>();

    /** Creates a new backing cache configured from the annotation (expiry settings, etc.). */
    public abstract C createCache(Cached cached);

    /** Stores {@code value} under {@code key}, overwriting any existing entry. */
    public abstract void put(C cache, String key, Object value);

    /** Returns the value cached under {@code key}, or {@code null} if absent or expired. */
    public abstract Object get(C cache, String key);

    /** Removes the entry for {@code key}, if present. */
    public abstract void remove(C cache, String key);

    /** Removes every entry from the given cache. */
    public abstract void clear(C cache);

    /**
     * Default membership check: present iff a non-expired value is cached. Backends with a cheaper
     * or more precise native check may override.
     */
    public boolean containsKey(C cache, String key) {
        return get(cache, key) != null;
    }

    /** Approximate number of live entries in the cache. */
    public abstract long size(C cache);

    /** Forgets every cache this provider has created (the underlying caches are left for GC). */
    public void clear() {
        cachedMap.clear();
    }

    /**
     * Returns the cache associated with the given annotation, creating it on first use. Two
     * {@code @Cached} annotations with identical attributes share a cache (annotations are value
     * objects — see {@link Object#equals}); the lookup is thread-safe.
     */
    public final C getCache(Cached cached) {
        return cachedMap.computeIfAbsent(cached, this::createCache);
    }

}

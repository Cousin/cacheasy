package com.joeyexecutive.cacheasy.provider;

import com.joeyexecutive.cacheasy.annotation.CacheExpiryType;
import com.joeyexecutive.cacheasy.annotation.Cached;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Dependency-free backend built on {@link ConcurrentHashMap}. Entries carry an expiry
 * timestamp and are evicted lazily on access (there is no background sweeper), so this is a
 * good default when you don't want to pull in a cache library. Supports both write- and
 * access-based expiry.
 */
public class ConcurrentMapCacheProvider extends AbstractCacheProvider<ConcurrentMapCacheProvider.TtlMap> {

    public ConcurrentMapCacheProvider() {
        super("ConcurrentMap");
    }

    @Override
    public TtlMap createCache(Cached cached) {
        long ttlNanos = cached.expiresTimeUnit().toNanos(cached.expiresAfter());
        boolean refreshOnAccess = cached.expiryType() == CacheExpiryType.EXPIRES_AFTER_ACCESS;
        return new TtlMap(ttlNanos, refreshOnAccess);
    }

    @Override
    public void put(TtlMap cache, String key, Object value) {
        cache.map.put(key, new Entry(value, System.nanoTime() + cache.ttlNanos));
    }

    @Override
    public Object get(TtlMap cache, String key) {
        Entry entry = cache.map.get(key);
        if (entry == null) {
            return null;
        }
        long now = System.nanoTime();
        if (isExpired(entry, now)) {
            cache.map.remove(key, entry);
            return null;
        }
        if (cache.refreshOnAccess) {
            entry.expiresAtNanos = now + cache.ttlNanos;
        }
        return entry.value;
    }

    @Override
    public void remove(TtlMap cache, String key) {
        cache.map.remove(key);
    }

    @Override
    public void clear(TtlMap cache) {
        cache.map.clear();
    }

    @Override
    public long size(TtlMap cache) {
        long now = System.nanoTime();
        cache.map.values().removeIf(entry -> isExpired(entry, now));
        return cache.map.size();
    }

    // Overflow-safe comparison: see System.nanoTime() contract on wrap-around.
    private static boolean isExpired(Entry entry, long now) {
        return now - entry.expiresAtNanos >= 0;
    }

    /** A map plus the expiry settings derived from one {@code @Cached}. */
    public static final class TtlMap {
        final ConcurrentMap<String, Entry> map = new ConcurrentHashMap<>();
        final long ttlNanos;
        final boolean refreshOnAccess;

        TtlMap(long ttlNanos, boolean refreshOnAccess) {
            this.ttlNanos = ttlNanos;
            this.refreshOnAccess = refreshOnAccess;
        }
    }

    private static final class Entry {
        final Object value;
        volatile long expiresAtNanos;

        Entry(Object value, long expiresAtNanos) {
            this.value = value;
            this.expiresAtNanos = expiresAtNanos;
        }
    }

}

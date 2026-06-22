package com.joeyexecutive.cacheasy.provider;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.joeyexecutive.cacheasy.annotation.CacheExpiryType;
import com.joeyexecutive.cacheasy.annotation.Cached;

/**
 * <a href="https://github.com/ben-manes/caffeine">Caffeine</a> backend — a high-performance,
 * near-optimal local cache. Maps Cacheasy's write/access expiry onto Caffeine's
 * {@code expireAfterWrite}/{@code expireAfterAccess}.
 */
public class CaffeineCacheProvider extends AbstractCacheProvider<Cache<String, Object>> {

    public CaffeineCacheProvider() {
        super("Caffeine");
    }

    @Override
    public Cache<String, Object> createCache(Cached cached) {
        Caffeine<Object, Object> builder = Caffeine.newBuilder();

        if (cached.expiryType() == CacheExpiryType.EXPIRES_AFTER_ACCESS) {
            builder.expireAfterAccess(cached.expiresAfter(), cached.expiresTimeUnit());
        } else {
            builder.expireAfterWrite(cached.expiresAfter(), cached.expiresTimeUnit());
        }

        return builder.build();
    }

    @Override
    public void put(Cache<String, Object> cache, String key, Object value) {
        cache.put(key, value);
    }

    @Override
    public Object get(Cache<String, Object> cache, String key) {
        return cache.getIfPresent(key);
    }

    @Override
    public void remove(Cache<String, Object> cache, String key) {
        cache.invalidate(key);
    }

    @Override
    public void clear(Cache<String, Object> cache) {
        cache.invalidateAll();
    }

    @Override
    public long size(Cache<String, Object> cache) {
        // Caffeine applies writes asynchronously, so this is an estimate (hence not unit-tested
        // for an exact value in the provider contract test).
        return cache.estimatedSize();
    }

}

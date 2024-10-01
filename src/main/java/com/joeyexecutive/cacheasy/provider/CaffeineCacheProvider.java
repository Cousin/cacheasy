package com.joeyexecutive.cacheasy.provider;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.joeyexecutive.cacheasy.annotation.CacheExpiryType;
import com.joeyexecutive.cacheasy.annotation.Cached;

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
    public void put(Cached cache, String key, Object value) {
        getCache(cache).put(key, value);
    }

    @Override
    public Object get(Cached cache, String key) {
        return getCache(cache).getIfPresent(key);
    }

    @Override
    public void remove(Cached cache, String key) {
        getCache(cache).invalidate(key);
    }

    @Override
    public void clear(Cached cache) {
        getCache(cache).invalidateAll();
    }

    @Override
    public boolean containsKey(Cached cache, String key) {
        return getCache(cache).getIfPresent(key) != null;
    }

    @Override
    public long size(Cached cache) {
        return getCache(cache).estimatedSize();
    }

}

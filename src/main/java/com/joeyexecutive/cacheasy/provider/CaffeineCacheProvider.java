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
    public void put(Cached cached, String key, Object value) {
        getCache(cached).put(key, value);
    }

    @Override
    public Object get(Cached cached, String key) {
        return getCache(cached).getIfPresent(key);
    }

    @Override
    public void remove(Cached cached, String key) {
        getCache(cached).invalidate(key);
    }

    @Override
    public void clear(Cached cached) {
        getCache(cached).invalidateAll();
    }

    @Override
    public boolean containsKey(Cached cached, String key) {
        return getCache(cached).getIfPresent(key) != null;
    }

    @Override
    public long size(Cached cached) {
        return getCache(cached).estimatedSize();
    }

}

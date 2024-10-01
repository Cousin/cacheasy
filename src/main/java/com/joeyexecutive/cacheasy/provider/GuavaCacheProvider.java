package com.joeyexecutive.cacheasy.provider;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.joeyexecutive.cacheasy.annotation.CacheExpiryType;
import com.joeyexecutive.cacheasy.annotation.Cached;

public class GuavaCacheProvider extends AbstractCacheProvider<Cache<String, Object>> {

    public GuavaCacheProvider() {
        super("Guava");
    }

    @Override
    public Cache<String, Object> createCache(Cached ms) {
        CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder();

        if (ms.expiryType() == CacheExpiryType.EXPIRES_AFTER_ACCESS) {
            cacheBuilder.expireAfterAccess(ms.expiresAfter(), ms.expiresTimeUnit());
        } else {
            cacheBuilder.expireAfterWrite(ms.expiresAfter(), ms.expiresTimeUnit());
        }

        return cacheBuilder.build();
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
        return getCache(cache).size();
    }
}

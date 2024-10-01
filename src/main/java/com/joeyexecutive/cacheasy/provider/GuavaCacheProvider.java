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
    public boolean containsKey(Cache<String, Object> cache, String key) {
        return cache.getIfPresent(key) != null;
    }

    @Override
    public long size(Cache<String, Object> cache) {
        return cache.size();
    }

}

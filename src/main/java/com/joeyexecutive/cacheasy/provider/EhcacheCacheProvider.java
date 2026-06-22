package com.joeyexecutive.cacheasy.provider;

import com.joeyexecutive.cacheasy.annotation.CacheExpiryType;
import com.joeyexecutive.cacheasy.annotation.Cached;
import org.ehcache.Cache;
import org.ehcache.UserManagedCache;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.UserManagedCacheBuilder;
import org.ehcache.expiry.ExpiryPolicy;

import java.time.Duration;

/**
 * Ehcache 3 backend. Each {@code @Cached} gets a standalone on-heap {@link UserManagedCache}.
 * Write-expiry maps to time-to-live, access-expiry to time-to-idle.
 */
public class EhcacheCacheProvider extends AbstractCacheProvider<Cache<String, Object>> {

    public EhcacheCacheProvider() {
        super("Ehcache");
    }

    @Override
    public Cache<String, Object> createCache(Cached cached) {
        Duration duration = Duration.ofNanos(cached.expiresTimeUnit().toNanos(cached.expiresAfter()));
        ExpiryPolicy<Object, Object> expiry = cached.expiryType() == CacheExpiryType.EXPIRES_AFTER_ACCESS
                ? ExpiryPolicyBuilder.timeToIdleExpiration(duration)
                : ExpiryPolicyBuilder.timeToLiveExpiration(duration);

        return UserManagedCacheBuilder
                .newUserManagedCacheBuilder(String.class, Object.class)
                .withExpiry(expiry)
                .build(true);
    }

    @Override
    public void put(Cache<String, Object> cache, String key, Object value) {
        cache.put(key, value);
    }

    @Override
    public Object get(Cache<String, Object> cache, String key) {
        return cache.get(key);
    }

    @Override
    public void remove(Cache<String, Object> cache, String key) {
        cache.remove(key);
    }

    @Override
    public void clear(Cache<String, Object> cache) {
        cache.clear();
    }

    @Override
    public long size(Cache<String, Object> cache) {
        // org.ehcache.Cache has no size(); count via its iterator.
        long count = 0;
        for (Cache.Entry<String, Object> ignored : cache) {
            count++;
        }
        return count;
    }

}

package com.joeyexecutive.cacheasy;

import com.github.benmanes.caffeine.cache.Cache;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Registry resolution. Individual provider behavior is covered by the provider-package tests.
 */
class CacheasyTest {

    @Test
    void resolvesBuiltInCaffeineProvider() {
        assertEquals("Caffeine", Cacheasy.getCacheProvider(Cache.class).getName());
    }

    @Test
    void resolvesZeroDependencyConcurrentMapProvider() {
        assertEquals("ConcurrentMap", Cacheasy.getCacheProvider(ConcurrentHashMap.class).getName());
    }

    @Test
    void unregisteredProviderThrows() {
        assertThrows(IllegalStateException.class, () -> Cacheasy.getCacheProvider(String.class));
    }

}

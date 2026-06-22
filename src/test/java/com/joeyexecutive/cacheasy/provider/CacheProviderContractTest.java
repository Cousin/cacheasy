package com.joeyexecutive.cacheasy.provider;

import com.joeyexecutive.cacheasy.annotation.Cached;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * One contract exercised against every in-process backend. Redis is excluded because it needs a
 * live server; it is only compile-checked. Caffeine is excluded from the exact-size check since
 * its {@code estimatedSize()} is intentionally approximate.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
class CacheProviderContractTest {

    // createCache only reads the expiry settings, so the cacheProvider() value is irrelevant here.
    @Cached(cacheProvider = ConcurrentHashMap.class, expiresAfter = 1, expiresTimeUnit = TimeUnit.HOURS)
    void longLived() {
    }

    @Cached(cacheProvider = ConcurrentHashMap.class, expiresAfter = 30, expiresTimeUnit = TimeUnit.MILLISECONDS)
    void shortLived() {
    }

    private static Cached annotation(String method) throws NoSuchMethodException {
        return CacheProviderContractTest.class.getDeclaredMethod(method).getAnnotation(Cached.class);
    }

    static Stream<AbstractCacheProvider> allProviders() {
        return Stream.of(
                new ConcurrentMapCacheProvider(),
                new CaffeineCacheProvider(),
                new GuavaCacheProvider(),
                new EhcacheCacheProvider());
    }

    static Stream<AbstractCacheProvider> exactSizeProviders() {
        return Stream.of(
                new ConcurrentMapCacheProvider(),
                new GuavaCacheProvider(),
                new EhcacheCacheProvider());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("allProviders")
    void putGetRemoveClear(AbstractCacheProvider provider) throws Exception {
        Object cache = provider.getCache(annotation("longLived"));

        assertNull(provider.get(cache, "missing"));
        assertFalse(provider.containsKey(cache, "missing"));

        provider.put(cache, "k", "v");
        assertEquals("v", provider.get(cache, "k"));
        assertTrue(provider.containsKey(cache, "k"));

        provider.remove(cache, "k");
        assertNull(provider.get(cache, "k"));
        assertFalse(provider.containsKey(cache, "k"));

        provider.put(cache, "a", "1");
        provider.put(cache, "b", "2");
        provider.clear(cache);
        assertNull(provider.get(cache, "a"));
        assertNull(provider.get(cache, "b"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("exactSizeProviders")
    void reportsExactSize(AbstractCacheProvider provider) throws Exception {
        Object cache = provider.getCache(annotation("longLived"));

        provider.put(cache, "a", "1");
        provider.put(cache, "b", "2");

        assertEquals(2, provider.size(cache));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("allProviders")
    void valuesExpireAfterTtl(AbstractCacheProvider provider) throws Exception {
        Object cache = provider.getCache(annotation("shortLived"));

        provider.put(cache, "k", "v");
        assertEquals("v", provider.get(cache, "k"));

        Thread.sleep(80); // comfortably past the 30ms TTL

        assertNull(provider.get(cache, "k"), provider.getName() + " should drop expired entries");
    }

}

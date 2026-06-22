package com.joeyexecutive.cacheasy;

import com.github.benmanes.caffeine.cache.Cache;
import com.joeyexecutive.cacheasy.annotation.Cached;
import com.joeyexecutive.cacheasy.provider.CaffeineCacheProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the registry and a provider, exercised directly (no weaving). End-to-end
 * weaving is verified by the example module's tests, since weaving is a consumer concern.
 */
class CacheasyTest {

    // Present only so a real @Cached instance can be read reflectively below.
    @Cached(cacheProvider = Cache.class, expiresAfter = 1)
    void annotatedSample() {
    }

    private static Cached sampleAnnotation() throws NoSuchMethodException {
        return CacheasyTest.class.getDeclaredMethod("annotatedSample").getAnnotation(Cached.class);
    }

    @Test
    void resolvesBuiltInCaffeineProvider() {
        assertEquals("Caffeine", Cacheasy.getCacheProvider(Cache.class).getName());
    }

    @Test
    void unregisteredProviderThrows() {
        assertThrows(IllegalStateException.class, () -> Cacheasy.getCacheProvider(String.class));
    }

    @Test
    void providerPutThenGetRoundTrips() throws Exception {
        CaffeineCacheProvider provider = new CaffeineCacheProvider();
        Cache<String, Object> cache = provider.getCache(sampleAnnotation());

        provider.put(cache, "key", "value");

        assertEquals("value", provider.get(cache, "key"));
        assertTrue(provider.containsKey(cache, "key"));
        assertEquals(1, provider.size(cache));
    }

}

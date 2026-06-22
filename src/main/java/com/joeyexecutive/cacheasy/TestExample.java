package com.joeyexecutive.cacheasy;

import com.github.benmanes.caffeine.cache.Cache;
import com.joeyexecutive.cacheasy.annotation.Cached;

import java.util.concurrent.TimeUnit;

/**
 * Demonstrates {@link Cached}. Once the aspect is woven in, every call with the same
 * arguments returns the first computed value until it expires — even though the body
 * itself is non-deterministic. No call-site changes are needed.
 */
public class TestExample {

    @Cached(cacheProvider = Cache.class, expiresAfter = 5, expiresTimeUnit = TimeUnit.MINUTES)
    public String getExpensiveValue(String param1, Object param2) {
        // Non-deterministic on purpose: if two calls with the same args return the same
        // thing, caching is working. nanoTime() differs on every real invocation.
        return param1 + ":" + System.nanoTime();
    }

}

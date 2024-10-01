package com.joeyexecutive.cacheasy;

import com.github.benmanes.caffeine.cache.Cache;
import com.joeyexecutive.cacheasy.annotation.Cached;
import com.joeyexecutive.cacheasy.provider.AbstractCacheProvider;

import java.util.Random;

/**
 * Example to demonstrate the expected source code, then the generated source code.
 */
public class TestExample {

    @Cached(cacheProvider = Cache.class, expiresAfter = 5)
    public String getTestRaw(String param1, Object param2) {
        final Random random = new Random();

        if (random.nextBoolean() && param1.equals("Bob")) {
            return "TestTrue";
        }

        if (random.nextInt() % 2 == 0) {
            return "TestInt";
        }

        return "TestNone";
    }

    public String getTestGenerated(String param1, Object param2) {
        Cached __cachedAnnotation = null; // SOMEHOW, USE THE CACHED INSTANCE FROM THE METHOD? prob do this some other way

        AbstractCacheProvider<Cache> __abstractCacheProvider = Cacheasy.getCacheProvider(Cache.class);

        Cache<String, Object> __cache = __abstractCacheProvider.getCache(__cachedAnnotation);

        String __cacheKey = "getTestGenerated;" + param1.hashCode() + ";" + param2.hashCode();

        Object __cached = __abstractCacheProvider.get(__cache, __cacheKey);

        if (__cached != null) {
            return (String) __cached;
        }

        final Random random = new Random();

        if (random.nextBoolean()) {
            String __temp1 = "TestTrue";
            __abstractCacheProvider.put(__cache, __cacheKey, __temp1);
            return __temp1;
        }

        if (random.nextInt() % 2 == 0) {
            String __temp2 = "TestInt";
            __abstractCacheProvider.put(__cache, __cacheKey, __temp2);
            return __temp2;
        }

        String __temp3 = "TestNone";
        __abstractCacheProvider.put(__cache, __cacheKey, __temp3);
        return __temp3;
    }

}

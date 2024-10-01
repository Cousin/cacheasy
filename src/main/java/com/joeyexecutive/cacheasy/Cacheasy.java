package com.joeyexecutive.cacheasy;

import com.joeyexecutive.cacheasy.provider.AbstractCacheProvider;
import com.joeyexecutive.cacheasy.provider.CaffeineCacheProvider;

import java.util.Map;

public class Cacheasy {

    private static final Map<Class<?>, AbstractCacheProvider<?>> cacheProviders = Map.of(
            com.github.benmanes.caffeine.cache.Cache.class, new CaffeineCacheProvider(),
            com.google.common.cache.Cache.class, new CaffeineCacheProvider()
    );

    public static <C> AbstractCacheProvider<C> getCacheProvider(Class<C> cacheProvider) {
        return (AbstractCacheProvider<C>) cacheProviders.get(cacheProvider);
    }

    public static void registerCacheProvider(Class<?> cacheProvider, AbstractCacheProvider<?> provider) {
        cacheProviders.put(cacheProvider, provider);
    }

}

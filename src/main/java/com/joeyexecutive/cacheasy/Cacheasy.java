package com.joeyexecutive.cacheasy;

import com.joeyexecutive.cacheasy.provider.AbstractCacheProvider;
import com.joeyexecutive.cacheasy.provider.CaffeineCacheProvider;
import com.joeyexecutive.cacheasy.provider.GuavaCacheProvider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Registry mapping a cache-backend type (e.g. Caffeine's or Guava's {@code Cache})
 * to the {@link AbstractCacheProvider} that knows how to drive it. The {@code CacheAspect}
 * looks providers up here at runtime using the {@code cacheProvider()} of each {@code @Cached}.
 */
public class Cacheasy {

    private static final Map<Class<?>, AbstractCacheProvider<?>> cacheProviders = new ConcurrentHashMap<>();

    static {
        // Register the built-in backends, but only the ones actually on the classpath.
        // Consumers typically depend on one of these, not both.
        register("com.github.benmanes.caffeine.cache.Cache", CaffeineCacheProvider::new);
        register("com.google.common.cache.Cache", GuavaCacheProvider::new);
    }

    @SuppressWarnings("unchecked")
    public static <C> AbstractCacheProvider<C> getCacheProvider(Class<C> cacheProvider) {
        AbstractCacheProvider<C> provider = (AbstractCacheProvider<C>) cacheProviders.get(cacheProvider);
        if (provider == null) {
            throw new IllegalStateException("No cache provider registered for " + cacheProvider.getName()
                    + ". Is the backend on the classpath, or did you forget Cacheasy.registerCacheProvider(...)?");
        }
        return provider;
    }

    public static void registerCacheProvider(Class<?> cacheProvider, AbstractCacheProvider<?> provider) {
        cacheProviders.put(cacheProvider, provider);
    }

    /**
     * Registers a built-in provider only if its backing cache class can be loaded, so that a
     * consumer who pulls in just one backend doesn't hit a {@link NoClassDefFoundError} for the other.
     */
    private static void register(String cacheClassName, Supplier<AbstractCacheProvider<?>> providerFactory) {
        try {
            Class<?> cacheClass = Class.forName(cacheClassName);
            cacheProviders.put(cacheClass, providerFactory.get());
        } catch (ClassNotFoundException | NoClassDefFoundError ignored) {
            // Backend not present on the classpath — nothing to register.
        }
    }

}

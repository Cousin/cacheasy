package com.joeyexecutive.cacheasy;

import com.joeyexecutive.cacheasy.provider.AbstractCacheProvider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry mapping a cache-backend type (e.g. Caffeine's or Guava's {@code Cache})
 * to the {@link AbstractCacheProvider} that knows how to drive it. The {@code CacheAspect}
 * looks providers up here at runtime using the {@code cacheProvider()} of each {@code @Cached}.
 */
public class Cacheasy {

    private static final Map<Class<?>, AbstractCacheProvider<?>> cacheProviders = new ConcurrentHashMap<>();

    private static final String PROVIDER_PACKAGE = "com.joeyexecutive.cacheasy.provider.";

    static {
        // Register the built-in backends, but only the ones actually on the classpath —
        // consumers typically depend on just one. ConcurrentHashMap is always available (JDK),
        // so it acts as the zero-dependency default. The first argument (the class a consumer
        // names in @Cached(cacheProvider = ...)) is the map key.
        register("java.util.concurrent.ConcurrentHashMap", "ConcurrentMapCacheProvider");
        register("com.github.benmanes.caffeine.cache.Cache", "CaffeineCacheProvider");
        register("com.google.common.cache.Cache", "GuavaCacheProvider");
        register("org.ehcache.Cache", "EhcacheCacheProvider");
        register("redis.clients.jedis.Jedis", "RedisCacheProvider");
    }

    /**
     * Returns the provider registered for the given backend type (the class named in
     * {@code @Cached(cacheProvider = ...)}).
     *
     * @throws IllegalStateException if no provider is registered — typically because the backend
     *                               is not on the classpath, or it needs manual registration.
     */
    @SuppressWarnings("unchecked")
    public static <C> AbstractCacheProvider<C> getCacheProvider(Class<C> cacheProvider) {
        AbstractCacheProvider<C> provider = (AbstractCacheProvider<C>) cacheProviders.get(cacheProvider);
        if (provider == null) {
            throw new IllegalStateException("No cache provider registered for " + cacheProvider.getName()
                    + ". Is the backend on the classpath, or did you forget Cacheasy.registerCacheProvider(...)?");
        }
        return provider;
    }

    /**
     * Registers (or replaces) the provider for a backend type. Use this for custom providers or to
     * supply a configured built-in — for example a Redis provider pointing at a specific host:
     * {@code Cacheasy.registerCacheProvider(Jedis.class, new RedisCacheProvider("cache.internal", 6379))}.
     */
    public static void registerCacheProvider(Class<?> cacheProvider, AbstractCacheProvider<?> provider) {
        cacheProviders.put(cacheProvider, provider);
    }

    /**
     * Registers a built-in provider only if its backing cache class is present. The provider is
     * instantiated reflectively (by simple class name) so that merely loading {@code Cacheasy}
     * never references a provider class directly — otherwise the JVM would try to load that
     * provider's backend types, defeating the point for consumers who pulled in just one backend.
     */
    private static void register(String cacheClassName, String providerSimpleName) {
        try {
            Class<?> cacheClass = Class.forName(cacheClassName);
            AbstractCacheProvider<?> provider = (AbstractCacheProvider<?>) Class
                    .forName(PROVIDER_PACKAGE + providerSimpleName)
                    .getDeclaredConstructor()
                    .newInstance();
            cacheProviders.put(cacheClass, provider);
        } catch (ReflectiveOperationException | NoClassDefFoundError ignored) {
            // Backend (or its provider) not loadable on this classpath — nothing to register.
        }
    }

}

package com.joeyexecutive.cacheasy.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * Marks a method whose return value should be cached.
 *
 * <p>Annotate any method and Cacheasy will, at <em>build time</em>, weave caching logic around it
 * (via AspectJ): the first call with a given set of arguments runs the body and stores the result;
 * later calls with the same arguments return the stored value until it expires. Call sites need no
 * changes — they invoke the method exactly as before.
 *
 * <pre>{@code
 * @Cached(cacheProvider = ConcurrentHashMap.class, expiresAfter = 30)
 * public Report buildReport(String accountId) {
 *     // expensive work runs only on a cache miss
 * }
 * }</pre>
 *
 * <p>The annotation is read at runtime by {@code CacheAspect}, which resolves a provider for
 * {@link #cacheProvider()} from the {@code Cacheasy} registry and applies the expiry settings below.
 *
 * @see com.joeyexecutive.cacheasy.aspect.CacheAspect
 * @see com.joeyexecutive.cacheasy.Cacheasy
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Cached {

    /**
     * The cache-backend type that selects which provider drives this cache — for example
     * {@code com.github.benmanes.caffeine.cache.Cache.class} for Caffeine, or
     * {@code java.util.concurrent.ConcurrentHashMap.class} for the dependency-free default. The
     * backend must be registered in {@code Cacheasy}; the built-in ones self-register when present
     * on the classpath.
     */
    Class<?> cacheProvider();

    /**
     * How long a cached entry lives, measured in {@link #expiresTimeUnit()}. Interpreted as a
     * time-to-live or time-to-idle depending on {@link #expiryType()}.
     */
    long expiresAfter();

    /** The unit for {@link #expiresAfter()}. Defaults to {@link TimeUnit#SECONDS}. */
    TimeUnit expiresTimeUnit() default TimeUnit.SECONDS;

    /**
     * Whether {@link #expiresAfter()} is measured from the last write or the last access.
     * Defaults to {@link CacheExpiryType#EXPIRES_AFTER_WRITE}.
     */
    CacheExpiryType expiryType() default CacheExpiryType.EXPIRES_AFTER_WRITE;

}

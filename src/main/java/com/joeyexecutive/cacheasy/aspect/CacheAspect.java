package com.joeyexecutive.cacheasy.aspect;

import com.joeyexecutive.cacheasy.Cacheasy;
import com.joeyexecutive.cacheasy.annotation.Cached;
import com.joeyexecutive.cacheasy.provider.AbstractCacheProvider;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * The heart of Cacheasy: an AspectJ aspect that wraps every {@code @Cached} method.
 *
 * <p>This advice is <em>woven into the compiled bytecode at build time</em> by the
 * post-compile-weaving plugin. After weaving, a call to a {@code @Cached} method no longer runs the
 * original body directly — it runs {@link #aroundCached} instead, which:
 * <ol>
 *   <li>resolves the {@link AbstractCacheProvider} for the backend named in the annotation,</li>
 *   <li>builds a cache key from the method and its arguments,</li>
 *   <li>returns the cached value on a hit, or</li>
 *   <li>invokes the real method once, stores the result, and returns it.</li>
 * </ol>
 *
 * <p>Because all of this lives in bytecode, call sites are untouched and need no knowledge of
 * Cacheasy.
 */
@Aspect
public class CacheAspect {

    /**
     * Around-advice matching the execution of any method annotated with {@link Cached}. The
     * {@code @annotation(cached)} clause binds the annotation instance so its settings are available
     * here.
     *
     * <p>Raw types are used deliberately: the concrete cache type {@code C} differs per backend and
     * is unknown at this generic join point.
     */
    @Around("execution(@com.joeyexecutive.cacheasy.annotation.Cached * *(..)) && @annotation(cached)")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Object aroundCached(ProceedingJoinPoint joinPoint, Cached cached) throws Throwable {
        // Pick the provider for the backend declared in @Cached(cacheProvider = ...), then get
        // (or lazily create) the single cache instance tied to this annotation.
        AbstractCacheProvider provider = Cacheasy.getCacheProvider(cached.cacheProvider());
        Object cache = provider.getCache(cached);

        String key = buildKey(joinPoint);

        Object hit = provider.get(cache, key);
        if (hit != null) {
            return hit; // cache hit — the original method body never runs
        }

        // Cache miss: run the real method body exactly once, then remember its result.
        Object result = joinPoint.proceed();
        if (result != null) {
            // Most backends reject null values, so treat a null result as simply "not cached".
            provider.put(cache, key, result);
        }
        return result;
    }

    /**
     * Builds a key of the form {@code com.acme.Service#method;<argHash>;<argHash>...}.
     *
     * <p>Argument hash codes keep keys compact and avoid retaining references to the arguments, at
     * the cost of a theoretical collision if two distinct argument sets hash alike — an accepted
     * trade-off for this library. {@code null} arguments are encoded as the literal {@code "null"}.
     */
    private static String buildKey(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        StringBuilder key = new StringBuilder(signature.getDeclaringTypeName())
                .append('#')
                .append(signature.getName());
        for (Object arg : joinPoint.getArgs()) {
            key.append(';').append(arg == null ? "null" : arg.hashCode());
        }
        return key.toString();
    }

}

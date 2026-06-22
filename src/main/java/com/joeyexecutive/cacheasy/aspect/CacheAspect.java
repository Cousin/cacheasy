package com.joeyexecutive.cacheasy.aspect;

import com.joeyexecutive.cacheasy.Cacheasy;
import com.joeyexecutive.cacheasy.annotation.Cached;
import com.joeyexecutive.cacheasy.provider.AbstractCacheProvider;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * Wraps every {@code @Cached} method. Woven into the bytecode at build time, so callers
 * use the annotated class normally and caching happens transparently around each call.
 */
@Aspect
public class CacheAspect {

    @Around("execution(@com.joeyexecutive.cacheasy.annotation.Cached * *(..)) && @annotation(cached)")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Object aroundCached(ProceedingJoinPoint joinPoint, Cached cached) throws Throwable {
        AbstractCacheProvider provider = Cacheasy.getCacheProvider(cached.cacheProvider());
        Object cache = provider.getCache(cached);

        String key = buildKey(joinPoint);

        Object hit = provider.get(cache, key);
        if (hit != null) {
            return hit;
        }

        Object result = joinPoint.proceed();
        if (result != null) {
            // Most backends reject null values; treat a null result as simply "not cached".
            provider.put(cache, key, result);
        }
        return result;
    }

    /**
     * Builds a cache key from the fully-qualified method plus the hash of each argument.
     * Hash-based keys can collide in theory; that is an accepted trade-off for this library.
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

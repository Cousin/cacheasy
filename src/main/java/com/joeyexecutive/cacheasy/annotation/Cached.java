package com.joeyexecutive.cacheasy.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface Cached {

    Class<?> cacheProvider();

    long expiresAfter();

    TimeUnit expiresTimeUnit() default TimeUnit.SECONDS;

    CacheExpiryType expiryType() default CacheExpiryType.EXPIRES_AFTER_WRITE;

}

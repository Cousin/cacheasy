package com.joeyexecutive.cacheasy.annotation;

import java.util.concurrent.TimeUnit;

public @interface Cached {

    Class<?> cacheProvider();

    long expiresAfter();

    TimeUnit expiresTimeUnit() default TimeUnit.SECONDS;

    CacheExpiryType expiryType() default CacheExpiryType.EXPIRES_AFTER_WRITE;

}

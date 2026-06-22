package com.joeyexecutive.cacheasy.annotation;

/**
 * Controls when a cached entry's expiry clock resets — the classic "time-to-live" versus
 * "time-to-idle" distinction found in most cache libraries.
 *
 * @see Cached#expiryType()
 */
public enum CacheExpiryType {

    /** Entry expires a fixed duration after it was written, regardless of reads (time-to-live). */
    EXPIRES_AFTER_WRITE,

    /** Entry expires only after a period of inactivity; each read resets the clock (time-to-idle). */
    EXPIRES_AFTER_ACCESS;

}

package com.example.app;

import com.github.benmanes.caffeine.cache.Cache;
import com.joeyexecutive.cacheasy.annotation.Cached;

import java.util.concurrent.TimeUnit;

/**
 * A plain service in a downstream project. The only thing tying it to Cacheasy is the
 * {@link Cached} annotation — no caching code is written here. The aspect is woven in at
 * build time by this module's post-compile-weaving plugin.
 */
public class GreetingService {

    @Cached(cacheProvider = Cache.class, expiresAfter = 10, expiresTimeUnit = TimeUnit.MINUTES)
    public String expensiveGreeting(String name) {
        // Prints only when the body actually runs. If caching works, you see this once per
        // distinct name, never twice for the same one.
        System.out.println("  [computing greeting for \"" + name + "\" — expensive work running]");
        sleep(500); // pretend this is slow

        return "Hello, " + name + "! (token=" + System.nanoTime() + ")";
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}

package com.joeyexecutive.cacheasy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Exercises the woven {@code @Cached} method. If weaving were absent, the non-deterministic
 * body would return a different value on every call and these assertions would fail.
 */
class CachingTest {

    @Test
    void returnsCachedValueForSameArguments() {
        TestExample example = new TestExample();

        String first = example.getExpensiveValue("a", "b");
        String second = example.getExpensiveValue("a", "b");

        assertEquals(first, second, "Same args should return the cached value, not recompute");
    }

    @Test
    void differentArgumentsAreCachedSeparately() {
        TestExample example = new TestExample();

        String forArgsAB = example.getExpensiveValue("a", "b");
        String forArgsXY = example.getExpensiveValue("x", "y");

        assertNotEquals(forArgsAB, forArgsXY, "Different args should have distinct cache keys");
        // And the distinct entry is itself stable across calls.
        assertEquals(forArgsXY, example.getExpensiveValue("x", "y"));
    }

}

package com.example.app;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Verifies that the @Cached method in this consumer module was actually woven. Without
 * weaving the non-deterministic body returns a different token each call and these fail.
 */
class GreetingServiceTest {

    @Test
    void sameNameIsCached() {
        GreetingService service = new GreetingService();
        assertEquals(service.expensiveGreeting("Alice"), service.expensiveGreeting("Alice"));
    }

    @Test
    void differentNamesAreDistinct() {
        GreetingService service = new GreetingService();
        assertNotEquals(service.expensiveGreeting("Alice"), service.expensiveGreeting("Bob"));
    }

}

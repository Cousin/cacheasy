package com.example.app;

public class Main {

    public static void main(String[] args) {
        GreetingService service = new GreetingService();

        System.out.println("1) First call for \"Alice\" (cache miss — should be slow):");
        String alice1 = timed(() -> service.expensiveGreeting("Alice"));

        System.out.println("\n2) Second call for \"Alice\" (cache hit — should be instant, no compute log):");
        String alice2 = timed(() -> service.expensiveGreeting("Alice"));

        System.out.println("\n3) First call for \"Bob\" (different key — cache miss, recomputed):");
        String bob1 = timed(() -> service.expensiveGreeting("Bob"));

        System.out.println("\n--- results ---");
        System.out.println("Alice call #1: " + alice1);
        System.out.println("Alice call #2: " + alice2);
        System.out.println("Bob   call #1: " + bob1);
        System.out.println();
        System.out.println("Alice #1 == Alice #2 (cached)?  " + alice1.equals(alice2));
        System.out.println("Alice    != Bob     (distinct)? " + !alice1.equals(bob1));
    }

    private static String timed(java.util.function.Supplier<String> call) {
        long start = System.nanoTime();
        String result = call.get();
        long millis = (System.nanoTime() - start) / 1_000_000;
        System.out.println("  -> returned in " + millis + " ms");
        return result;
    }

}

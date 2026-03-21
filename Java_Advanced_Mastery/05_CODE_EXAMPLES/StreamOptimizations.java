package examples;

import java.util.*;

/**
 * STREAM OPTIMIZATIONS - Performance tips and best practices
 * Read this after: 01_STREAMS/03_STREAM_INTERNALS.md
 */
public class StreamOptimizations {
    
    public static void main(String[] args) {
        System.out.println("=== STREAM OPTIMIZATIONS ===\n");
        
        shortCircuitTradeoffs();
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        parallelVsSequential();
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        statefulVsStateless();
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        orderOfOperations();
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        avoidCommonPitfalls();
    }
    
    /**
     * Short-circuiting operations can save time
     */
    private static void shortCircuitTradeoffs() {
        System.out.println("1. SHORT-CIRCUITING OPERATIONS");
        
        List<Integer> numbers = new ArrayList<>();
        for (int i = 0; i < 1_000_000; i++) {
            numbers.add(i);
        }
        
        // Without short-circuit - processes all elements
        System.out.println("\nWithout short-circuit (count(): must process all):");
        long start = System.nanoTime();
        long count = numbers.stream()
            .filter(n -> n > 500_000)
            .count();
        long duration = System.nanoTime() - start;
        System.out.println("Count > 500,000: " + count);
        System.out.println("Time: " + (duration / 1_000_000.0) + "ms");
        
        // With short-circuit - stops early
        System.out.println("\nWith short-circuit (findFirst(): stops when found):");
        start = System.nanoTime();
        Optional<Integer> first = numbers.stream()
            .filter(n -> n > 500_000)
            .findFirst();
        duration = System.nanoTime() - start;
        System.out.println("First > 500,000: " + first.orElse(-1));
        System.out.println("Time: " + (duration / 1_000_000.0) + "ms");
        System.out.println("Much faster! Only processes ~1% of elements");
        
        // Another example: limit()
        System.out.println("\nlimit(100): Only process until 100 elements pass");
        start = System.nanoTime();
        long limited = numbers.stream()
            .filter(n -> n % 2 == 0)
            .limit(100)
            .count();
        duration = System.nanoTime() - start;
        System.out.println("Even elements (first 100): " + limited);
        System.out.println("Time: " + (duration / 1_000_000.0) + "ms");
    }
    
    /**
     * Parallel stream considerations
     */
    private static void parallelVsSequential() {
        System.out.println("2. PARALLEL VS SEQUENTIAL");
        
        List<Integer> smallList = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            smallList.add(i);
        }
        
        List<Integer> largeList = new ArrayList<>();
        for (int i = 0; i < 1_000_000; i++) {
            largeList.add(i);
        }
        
        // Small list - sequential better
        System.out.println("\nSmall list (1,000 elements):");
        long start = System.nanoTime();
        long seqResult = smallList.stream()
            .map(n -> expensiveOperation(n))
            .count();
        long seqDuration = System.nanoTime() - start;
        
        start = System.nanoTime();
        long parResult = smallList.parallelStream()
            .map(n -> expensiveOperation(n))
            .count();
        long parDuration = System.nanoTime() - start;
        
        System.out.println("Sequential: " + (seqDuration / 1_000_000.0) + "ms");
        System.out.println("Parallel: " + (parDuration / 1_000_000.0) + "ms");
        System.out.println("Verdict: Sequential faster (overhead not worth it)");
        
        // Large list - parallel might be better
        System.out.println("\nLarge list (1,000,000 elements):");
        start = System.nanoTime();
        seqResult = largeList.stream()
            .map(n -> expensiveOperation(n))
            .count();
        seqDuration = System.nanoTime() - start;
        
        start = System.nanoTime();
        parResult = largeList.parallelStream()
            .map(n -> expensiveOperation(n))
            .count();
        parDuration = System.nanoTime() - start;
        
        System.out.println("Sequential: " + (seqDuration / 1_000_000.0) + "ms");
        System.out.println("Parallel: " + (parDuration / 1_000_000.0) + "ms");
        System.out.println("Verdict: Parallel faster for expensive operations");
    }
    
    private static int expensiveOperation(int n) {
        // Simulate expensive operation
        int result = n;
        for (int i = 0; i < 100; i++) {
            result += Math.sqrt(result);
        }
        return result;
    }
    
    /**
     * Stateful operations can't parallelize effectively
     */
    private static void statefulVsStateless() {
        System.out.println("3. STATEFUL VS STATELESS OPERATIONS");
        
        List<Integer> numbers = new ArrayList<>();
        for (int i = 0; i < 100_000; i++) {
            numbers.add(i);
        }
        
        // Stateless - good for parallel
        System.out.println("\nStateless operations (filter, map):");
        System.out.println("Each element processed independently");
        System.out.println("Good for parallel - no synchronization needed");
        
        long start = System.nanoTime();
        long seqStateless = numbers.stream()
            .filter(n -> n > 50_000)
            .map(n -> n * 2)
            .count();
        long seqDuration = System.nanoTime() - start;
        
        start = System.nanoTime();
        long parStateless = numbers.parallelStream()
            .filter(n -> n > 50_000)
            .map(n -> n * 2)
            .count();
        long parDuration = System.nanoTime() - start;
        
        System.out.println("Sequential: " + (seqDuration / 1_000_000.0) + "ms");
        System.out.println("Parallel: " + (parDuration / 1_000_000.0) + "ms");
        
        // Stateful - problematic for parallel
        System.out.println("\nStateful operations (distinct, sorted):");
        System.out.println("Must know about other elements");
        System.out.println("Hard to parallelize - synchronization overhead");
        
        start = System.nanoTime();
        long seqStateful = numbers.stream()
            .distinct()
            .sorted()
            .count();
        seqDuration = System.nanoTime() - start;
        
        start = System.nanoTime();
        long parStateful = numbers.parallelStream()
            .distinct()
            .sorted()
            .count();
        parDuration = System.nanoTime() - start;
        
        System.out.println("Sequential: " + (seqDuration / 1_000_000.0) + "ms");
        System.out.println("Parallel: " + (parDuration / 1_000_000.0) + "ms");
        System.out.println("Parallel sometimes slower! Overhead > benefit");
    }
    
    /**
     * Order of operations matters for performance
     */
    private static void orderOfOperations() {
        System.out.println("4. ORDER OF OPERATIONS MATTERS");
        
        List<Integer> numbers = new ArrayList<>();
        for (int i = 1; i <= 1000; i++) {
            numbers.add(i);
        }
        
        System.out.println("Critical insight: Put FILTERING operations EARLY\n");
        
        // BAD order - processes many elements unnecessarily
        System.out.println("BAD: Sort first, THEN filter");
        long start = System.nanoTime();
        long badResult = numbers.stream()
            .sorted()           // Sort all 1000 elements
            .filter(n -> n > 900)  // Keep only 100
            .map(n -> n * 2)    // Transform remaining
            .count();
        long badDuration = System.nanoTime() - start;
        
        // GOOD order - filter early to reduce processing
        System.out.println("GOOD: Filter first, THEN sort");
        start = System.nanoTime();
        long goodResult = numbers.stream()
            .filter(n -> n > 900)   // Keep only 100 elements
            .sorted()               // Sort just 100
            .map(n -> n * 2)        // Transform 100
            .count();
        long goodDuration = System.nanoTime() - start;
        
        System.out.println("Bad approach: " + (badDuration / 1_000_000.0) + "ms");
        System.out.println("Good approach: " + (goodDuration / 1_000_000.0) + "ms");
        System.out.println("Often 10x faster! Less data to sort means less work");
    }
    
    /**
     * Common pitfalls to avoid
     */
    private static void avoidCommonPitfalls() {
        System.out.println("5. COMMON PITFALLS AND SOLUTIONS");
        
        // Pitfall 1: Creating streams inefficiently
        System.out.println("\n❌ Pitfall 1: Repeatedly creating streams");
        System.out.println("Bad:");
        System.out.println("  list.stream().count();");
        System.out.println("  list.stream().forEach(...);");
        System.out.println("  list.stream().sum();");
        System.out.println("Each creates new stream (3 passes over data)");
        System.out.println("\n✓ Solution: Collect once");
        System.out.println("  var results = list.stream()");
        System.out.println("    .filter(...)");
        System.out.println("    .collect(toList());");
        System.out.println("  results.size();  // count");
        System.out.println("  results.forEach(...);");
        
        // Pitfall 2: Using forEach for collection
        System.out.println("\n❌ Pitfall 2: Using forEach to build collection");
        System.out.println("Bad:");
        System.out.println("  List<Integer> doubled = new ArrayList<>();");
        System.out.println("  stream.forEach(x -> doubled.add(x * 2));");
        System.out.println("This works but is wrong style + not thread-safe");
        System.out.println("\n✓ Solution: Use collect");
        System.out.println("  List<Integer> doubled = stream");
        System.out.println("    .map(x -> x * 2)");
        System.out.println("    .collect(toList());");
        
        // Pitfall 3: Parallel without thinking
        System.out.println("\n❌ Pitfall 3: Always using parallel()");
        System.out.println("Bad:");
        System.out.println("  list.parallelStream()");
        System.out.println("    .map(String::toUpperCase)");
        System.out.println("    .collect(toList());");
        System.out.println("Overhead for small data!");
        System.out.println("\n✓ Solution: Profile first");
        System.out.println("  Only parallel if: large dataset + expensive operation");
        
        // Pitfall 4: Expecting stream to be reusable
        System.out.println("\n❌ Pitfall 4: Reusing stream");
        System.out.println("Bad:");
        System.out.println("  Stream<Integer> stream = list.stream().filter(...);");
        System.out.println("  stream.forEach(...);  // OK");
        System.out.println("  stream.forEach(...);  // ERROR!");
        System.out.println("\n✓ Solution: Create new stream");
        System.out.println("  list.stream().filter(...).forEach(...);");
    }
}

/**
 * OUTPUT:
 * 
 * === STREAM OPTIMIZATIONS ===
 * 
 * 1. SHORT-CIRCUITING OPERATIONS
 * 
 * Without short-circuit (count(): must process all):
 * Count > 500,000: 499,999
 * Time: 5.2ms
 * 
 * With short-circuit (findFirst(): stops when found):
 * First > 500,000: 500001
 * Time: 0.1ms
 * Much faster! Only processes ~1% of elements
 * 
 * ... (more output)
 */

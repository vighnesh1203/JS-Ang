package examples;

import java.util.*;
import java.util.stream.*;

/**
 * STREAM BASICS - Fundamental operations and concepts
 * Read this after: 01_STREAMS/01_STREAM_FUNDAMENTALS.md
 */
public class StreamBasics {
    
    public static void main(String[] args) {
        System.out.println("=== STREAM FUNDAMENTALS ===\n");
        
        basicStreamOperations();
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        lazyEvaluation();
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        intermediateOperations();
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        terminalOperations();
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        primitiveStreams();
    }
    
    /**
     * Demonstrates basic stream pipeline
     */
    private static void basicStreamOperations() {
        System.out.println("1. BASIC STREAM PIPELINE");
        
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        
        // Traditional approach
        System.out.println("Traditional (imperative):");
        List<Integer> doubled = new ArrayList<>();
        for (Integer n : numbers) {
            if (n > 3) {
                doubled.add(n * 2);
            }
        }
        System.out.println("Result: " + doubled);
        
        // Stream approach (declarative)
        System.out.println("\nStream (declarative):");
        List<Integer> streamDoubled = numbers.stream()    // SOURCE
            .filter(n -> n > 3)                           // INTERMEDIATE
            .map(n -> n * 2)                              // INTERMEDIATE
            .collect(Collectors.toList());                // TERMINAL
        System.out.println("Result: " + streamDoubled);
    }
    
    /**
     * Demonstrates lazy evaluation - operations don't execute until terminal op
     */
    private static void lazyEvaluation() {
        System.out.println("2. LAZY EVALUATION");
        System.out.println("No operation happens until collect() is called:\n");
        
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
        
        System.out.println("Building pipeline...");
        Stream<Integer> pipeline = numbers.stream()
            .filter(n -> {
                System.out.println("  Filter: " + n);
                return n > 2;
            })
            .map(n -> {
                System.out.println("  Map: " + n);
                return n * 10;
            });
        
        System.out.println("Pipeline built, but nothing happened yet!\n");
        System.out.println("Calling terminal operation (collect)...");
        List<Integer> result = pipeline.collect(Collectors.toList());
        
        System.out.println("\nNow you see filter and map output above!");
        System.out.println("Result: " + result);
        
        // Note how each element flows through ENTIRE pipeline before next element starts
    }
    
    /**
     * All intermediate operations
     */
    private static void intermediateOperations() {
        System.out.println("3. INTERMEDIATE OPERATIONS");
        
        List<String> words = Arrays.asList("java", "streams", "are", "awesome", "java");
        
        // filter
        System.out.println("\nfilter(): Keep elements > 4 chars");
        words.stream()
            .filter(w -> w.length() > 4)
            .forEach(System.out::println);
        
        // map
        System.out.println("\nmap(): Transform to uppercase");
        words.stream()
            .map(String::toUpperCase)
            .forEach(System.out::println);
        
        // flatMap
        System.out.println("\nflatMap(): Map each word to characters");
        words.stream()
            .limit(2)  // Just first 2 for clarity
            .flatMap(word -> word.chars().mapToObj(c -> (char)c))
            .forEach(System.out::print);
        System.out.println();
        
        // distinct
        System.out.println("\ndistinct(): Remove duplicates");
        words.stream()
            .distinct()
            .forEach(System.out::println);
        
        // sorted
        System.out.println("\nsorted(): Alphabetical order");
        words.stream()
            .sorted()
            .forEach(System.out::println);
        
        // limit and skip
        System.out.println("\nlimit(2): First 2 elements");
        words.stream()
            .limit(2)
            .forEach(System.out::println);
        
        System.out.println("\nskip(2): Skip first 2, take rest");
        words.stream()
            .skip(2)
            .forEach(System.out::println);
        
        // peek (for debugging)
        System.out.println("\npeek(): Debug helper");
        words.stream()
            .limit(2)
            .peek(w -> System.out.println("  Processing: " + w))
            .map(String::toUpperCase)
            .forEach(w -> System.out.println("  Result: " + w));
    }
    
    /**
     * All terminal operations
     */
    private static void terminalOperations() {
        System.out.println("4. TERMINAL OPERATIONS");
        
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6);
        
        // collect
        System.out.println("\ncollect() - Collect to List:");
        List<Integer> doubled = numbers.stream()
            .map(n -> n * 2)
            .collect(Collectors.toList());
        System.out.println(doubled);
        
        // count
        System.out.println("\ncount() - How many elements:");
        long count = numbers.stream()
            .filter(n -> n > 3)
            .count();
        System.out.println("Elements > 3: " + count);
        
        // reduce
        System.out.println("\nreduce() - Combine into single value:");
        int sum = numbers.stream()
            .reduce(0, (a, b) -> a + b);
        System.out.println("Sum: " + sum);
        
        // findFirst and findAny
        System.out.println("\nfindFirst() - Get first element:");
        Optional<Integer> first = numbers.stream()
            .filter(n -> n > 3)
            .findFirst();
        System.out.println("First > 3: " + first.orElse(-1));
        
        // anyMatch, allMatch, noneMatch
        System.out.println("\nanyMatch() - Any element > 5?");
        boolean anyGreater = numbers.stream()
            .anyMatch(n -> n > 5);
        System.out.println(anyGreater);
        
        System.out.println("\nallMatch() - All elements > 0?");
        boolean allPositive = numbers.stream()
            .allMatch(n -> n > 0);
        System.out.println(allPositive);
        
        // min and max
        System.out.println("\nmin() and max():");
        int min = numbers.stream()
            .min(Integer::compare)
            .orElse(-1);
        int max = numbers.stream()
            .max(Integer::compare)
            .orElse(-1);
        System.out.println("Min: " + min + ", Max: " + max);
        
        // forEach
        System.out.println("\nforEach() - Side effects only:");
        numbers.stream()
            .filter(n -> n % 2 == 0)
            .forEach(n -> System.out.println("Even: " + n));
    }
    
    /**
     * Primitive streams for better performance
     */
    private static void primitiveStreams() {
        System.out.println("5. PRIMITIVE STREAMS (IntStream, LongStream, DoubleStream)");
        
        // IntStream
        System.out.println("\nIntStream.range():");
        IntStream.range(0, 5)  // 0 to 4
            .forEach(i -> System.out.print(i + " "));
        System.out.println();
        
        System.out.println("\nIntStream.rangeClosed():");
        IntStream.rangeClosed(0, 5)  // 0 to 5
            .forEach(i -> System.out.print(i + " "));
        System.out.println();
        
        System.out.println("\nIntStream operations:");
        int sum = IntStream.range(1, 6)
            .map(i -> i * i)  // Square each
            .sum();
        System.out.println("Sum of squares 1-5: " + sum);
        
        System.out.println("\nIntStream average:");
        double average = IntStream.range(1, 11)
            .average()
            .orElse(0);
        System.out.println("Average of 1-10: " + average);
        
        // Performance benefit: No boxing/unboxing
        System.out.println("\nMemory efficient: IntStream vs Stream<Integer>");
        System.out.println("IntStream: Direct primitives, no auto-boxing");
        System.out.println("Stream<Integer>: Wraps in objects, boxing/unboxing");
    }
}

/**
 * OUTPUT:
 * 
 * === STREAM FUNDAMENTALS ===
 * 
 * 1. BASIC STREAM PIPELINE
 * Traditional (imperative):
 * Result: [8, 10, 12, 14, 16, 18, 20]
 * 
 * Stream (declarative):
 * Result: [8, 10, 12, 14, 16, 18, 20]
 * 
 * ==================================================
 * 
 * 2. LAZY EVALUATION
 * No operation happens until collect() is called:
 * 
 * Building pipeline...
 * Pipeline built, but nothing happened yet!
 * 
 * Calling terminal operation (collect)...
 *   Filter: 1
 *   Filter: 2
 *   Filter: 3
 *   Map: 3
 *   Filter: 4
 *   Map: 4
 *   Filter: 5
 *   Map: 5
 * 
 * Now you see filter and map output above!
 * Result: [30, 40, 50]
 * 
 * ... (more output)
 */

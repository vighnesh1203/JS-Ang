package examples;

import java.util.*;
import java.util.stream.*;

/**
 * INTERVIEW PROBLEMS - Practical coding challenges with stream/concurrency solutions
 * Real problems you'll face in interviews
 */
public class InterviewProblems {
    
    public static void main(String[] args) {
        System.out.println("=== INTERVIEW PROBLEMS ===\n");
        
        problem1();
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        problem2();
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        problem3();
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        problem4();
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        problem5();
    }
    
    /**
     * PROBLEM 1: Group numbers by their digit sum
     * Given: [12, 15, 21, 13, 18]
     * Expected: {3=[12, 21], 6=[15], 4=[13], 9=[18]}
     */
    private static void problem1() {
        System.out.println("PROBLEM 1: Group numbers by digit sum");
        System.out.println("Input: [12, 15, 21, 13, 18]");
        
        List<Integer> numbers = Arrays.asList(12, 15, 21, 13, 18);
        
        Map<Integer, List<Integer>> grouped = numbers.stream()
            .collect(Collectors.groupingBy(n -> {
                // Calculate digit sum: 12 → 1+2=3
                return String.valueOf(n).chars()
                    .map(Character::getNumericValue)
                    .sum();
            }));
        
        System.out.println("Output: " + grouped);
        
        // Explanation:
        // 1. Convert number to String, then get each character
        // 2. Convert character to numeric value
        // 3. Sum all digits
        // 4. Group by this sum
    }
    
    /**
     * PROBLEM 2: Find employees making more than average salary
     * Given: List of employees with names and salaries
     * Expected: Names of employees above average
     */
    private static void problem2() {
        System.out.println("PROBLEM 2: Find employees above average salary");
        
        List<Employee> employees = Arrays.asList(
            new Employee("Alice", 50000),
            new Employee("Bob", 60000),
            new Employee("Charlie", 70000),
            new Employee("David", 45000)
        );
        
        System.out.println("Employees: ");
        employees.forEach(e -> System.out.println("  " + e.name + ": $" + e.salary));
        
        double averageSalary = employees.stream()
            .mapToDouble(e -> e.salary)
            .average()
            .orElse(0);
        
        System.out.println("\nAverage salary: $" + averageSalary);
        
        List<String> aboveAverage = employees.stream()
            .filter(e -> e.salary > averageSalary)
            .map(e -> e.name)
            .collect(Collectors.toList());
        
        System.out.println("Above average: " + aboveAverage);
    }
    
    private static class Employee {
        String name;
        double salary;
        
        Employee(String name, double salary) {
            this.name = name;
            this.salary = salary;
        }
    }
    
    /**
     * PROBLEM 3: Find first 3 unique strings, sorted by length
     * Given: ["java", "python", "java", "c", "python", "ruby"]
     * Expected: ["c", "java", "ruby"]
     */
    private static void problem3() {
        System.out.println("PROBLEM 3: First 3 unique strings sorted by length");
        
        List<String> languages = Arrays.asList(
            "java", "python", "java", "c", "python", "ruby", "go"
        );
        
        System.out.println("Input: " + languages);
        
        List<String> result = languages.stream()
            .distinct()                           // Remove duplicates
            .sorted(Comparator.comparingInt(String::length))  // Sort by length
            .limit(3)                             // Take first 3
            .collect(Collectors.toList());
        
        System.out.println("Output: " + result);
        
        // This is EFFICIENT because of short-circuiting
        // distinct() processes all (needed to know what's unique)
        // sorted() processes all (needed to sort)
        // limit(3) stops early (only need 3 shortest)
    }
    
    /**
     * PROBLEM 4: Flat map - List of lists to single list with filtering
     * Given: Lists of test scores for students
     * Expected: All scores above 80, sorted
     */
    private static void problem4() {
        System.out.println("PROBLEM 4: FlatMap - Extract and filter scores");
        
        Map<String, List<Integer>> studentScores = new LinkedHashMap<>();
        studentScores.put("Alice", Arrays.asList(85, 92, 78));
        studentScores.put("Bob", Arrays.asList(88, 76, 95));
        studentScores.put("Charlie", Arrays.asList(75, 82, 79));
        
        System.out.println("Student scores:");
        studentScores.forEach((name, scores) -> 
            System.out.println("  " + name + ": " + scores)
        );
        
        List<Integer> highScores = studentScores.values().stream()
            .flatMap(List::stream)                // Flatten nested lists
            .filter(score -> score > 80)          // Keep only > 80
            .sorted()                             // Sort
            .distinct()                           // Remove duplicates
            .collect(Collectors.toList());
        
        System.out.println("Scores > 80 (sorted): " + highScores);
        
        // flatMap explanation:
        // Without flatMap: Stream<List<Integer>> (nested, hard to work with)
        // With flatMap: Stream<Integer> (flat, easy to filter)
    }
    
    /**
     * PROBLEM 5: Complex - Create a frequency map of words, sorted by frequency
     * Given: "java java is powerful java and java is amazing and java is everywhere"
     * Expected: {java=5, is=3, and=2, powerful=1, amazing=1, everywhere=1}
     */
    private static void problem5() {
        System.out.println("PROBLEM 5: Word frequency map");
        
        String text = "java java is powerful java and java is amazing and java is everywhere";
        
        System.out.println("Text: " + text);
        
        // Solution 1: Using groupingBy and counting
        Map<String, Long> frequency = Arrays.stream(text.split(" "))
            .collect(Collectors.groupingBy(
                String::trim,
                Collectors.counting()
            ));
        
        System.out.println("\nFrequency (unsorted):");
        frequency.forEach((word, count) -> 
            System.out.println("  " + word + ": " + count)
        );
        
        // Solution 2: Sort by frequency (descending)
        Map<String, Long> sortedByFrequency = frequency.entrySet().stream()
            .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
            .collect(Collectors.toLinkedHashMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
        
        System.out.println("\nFrequency (sorted, descending):");
        sortedByFrequency.forEach((word, count) ->
            System.out.println("  " + word + ": " + count)
        );
        
        // Top 3 words
        System.out.println("\nTop 3 words:");
        frequency.entrySet().stream()
            .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
            .limit(3)
            .forEach(e -> System.out.println("  " + e.getKey() + ": " + e.getValue()));
    }
}

/**
 * CONCURRENCY PROBLEM: Thread-safe counter increment
 * 
 * PROBLEM: 100 threads increment counter 1000 times
 * Wrong: No synchronization - expected 100k, gets 50-90k (random)
 * Right: Use synchronized, locks, or atomic
 */
class ConcurrencyProblem {
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("CONCURRENCY PROBLEM: Thread-safe counter\n");
        
        unsafeSolution();
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        safeSolution();
    }
    
    private static void unsafeSolution() throws InterruptedException {
        System.out.println("WRONG APPROACH: Not thread-safe");
        
        int[] count = {0};
        Thread[] threads = new Thread[10];
        
        for (int i = 0; i < 10; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 10000; j++) {
                    count[0]++;  // ← RACE CONDITION!
                }
            });
            threads[i].start();
        }
        
        for (Thread t : threads) {
            t.join();
        }
        
        System.out.println("Expected: 100,000");
        System.out.println("Actual: " + count[0]);
        System.out.println("Problem: Lost updates due to race condition\n");
    }
    
    private static void safeSolution() throws InterruptedException {
        System.out.println("RIGHT APPROACH: Thread-safe");
        
        java.util.concurrent.atomic.AtomicInteger count = 
            new java.util.concurrent.atomic.AtomicInteger(0);
        Thread[] threads = new Thread[10];
        
        for (int i = 0; i < 10; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 10000; j++) {
                    count.incrementAndGet();  // ← THREAD-SAFE!
                }
            });
            threads[i].start();
        }
        
        for (Thread t : threads) {
            t.join();
        }
        
        System.out.println("Expected: 100,000");
        System.out.println("Actual: " + count.get());
        System.out.println("Perfect! No lost updates");
    }
}

/**
 * OUTPUT EXAMPLES:
 * 
 * PROBLEM 1: Group numbers by digit sum
 * Input: [12, 15, 21, 13, 18]
 * Output: {3=[12, 21], 6=[15], 4=[13], 9=[18]}
 * 
 * PROBLEM 2: Find employees above average salary
 * Employees:
 *   Alice: $50000.0
 *   Bob: $60000.0
 *   Charlie: $70000.0
 *   David: $45000.0
 * Average salary: $56250.0
 * Above average: [Bob, Charlie]
 * 
 * PROBLEM 3: First 3 unique strings sorted by length
 * Input: [java, python, java, c, python, ruby, go]
 * Output: [c, go, java]
 * 
 * PROBLEM 4: FlatMap - Extract and filter scores
 * Student scores:
 *   Alice: [85, 92, 78]
 *   Bob: [88, 76, 95]
 *   Charlie: [75, 82, 79]
 * Scores > 80 (sorted): [82, 85, 88, 92, 95]
 * 
 * PROBLEM 5: Word frequency map
 * Text: java java is powerful java and java is amazing and java is everywhere
 * Frequency (sorted, descending):
 *   java: 5
 *   is: 3
 *   and: 2
 *   powerful: 1
 *   amazing: 1
 *   everywhere: 1
 */

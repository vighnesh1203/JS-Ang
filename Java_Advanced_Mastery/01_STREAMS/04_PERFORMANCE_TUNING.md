# Stream Performance Tuning & Advanced Topics

## Advanced Stream Operations

### Collectors - More Than toList()

```java
// Collecting to different types:
.collect(Collectors.toList())          // List<T>
.collect(Collectors.toSet())           // Set<T>
.collect(Collectors.toCollection(LinkedList::new))  // Specific collection

// Joining strings
list.stream()
    .map(Object::toString)
    .collect(Collectors.joining(","));  // "a,b,c"

// Mapping (transform to different collection)
users.stream()
    .collect(Collectors.toMap(
        User::getId,        // Key function
        User::getName       // Value function
    ));  // Map<Integer, String>

// Grouping (most powerful!)
users.stream()
    .collect(Collectors.groupingBy(User::getAge));  
// Map<Integer, List<User>> grouped by age

// Grouping with custom aggregation
users.stream()
    .collect(Collectors.groupingBy(
        User::getAge,
        Collectors.counting()
    ));  // Map<Integer, Long> - count per age

// Partitioning (split into true/false groups)
users.stream()
    .collect(Collectors.partitioningBy(u -> u.getAge() > 18));
// Map<Boolean, List<User>> - adults and minors
```

### reduce() - Combining Values

```java
List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);

// Sum with identity
int sum = numbers.stream()
    .reduce(0, (a, b) -> a + b);  // 15

// Sum without identity (returns Optional)
Optional<Integer> sum = numbers.stream()
    .reduce((a, b) -> a + b);

// Product
int product = numbers.stream()
    .reduce(1, (a, b) -> a * b);  // 120

// String concatenation
List<String> words = Arrays.asList("java", "is", "great");
String combined = words.stream()
    .reduce("", (a, b) -> a + " " + b).trim();  // "java is great"

// Complex object aggregation
class Stats {
    int count = 0;
    int sum = 0;
}

Stats stats = numbers.stream()
    .reduce(new Stats(),
        (acc, num) -> {
            acc.count++;
            acc.sum += num;
            return acc;
        },
        (acc1, acc2) -> {
            acc1.count += acc2.count;
            acc1.sum += acc2.sum;
            return acc1;
        }
    );
```

### Custom Collectors

```java
// Collect to custom collection type
.collect(new TreeSet<>(), TreeSet::add, TreeSet::addAll)

// Or as Collector
Collector<String, ?, TreeSet<String>> toTreeSet = 
    Collectors.toCollection(TreeSet::new);

words.stream()
    .collect(toTreeSet);

// More complex - collect with metadata
Collector<Integer, ?, Map<String, Object>> withStats = 
    Collectors.collectingAndThen(
        Collectors.toList(),
        list -> {
            Map<String, Object> result = new HashMap<>();
            result.put("count", list.size());
            result.put("sum", list.stream().mapToInt(Integer::intValue).sum());
            result.put("values", list);
            return result;
        }
    );
```

---

## Primitive Streams Optimization

```java
// String to IntStream
String number = "123";
int sum = number.chars()
    .map(c -> Character.getNumericValue(c))
    .sum();  // 6

// Range operations (very efficient)
IntStream.range(0, 100)          // 0-99
    .map(i -> i * i)
    .sum();

IntStream.rangeClosed(0, 100)    // 0-100
    .filter(i -> i % 2 == 0)
    .count();

// Statistics
IntSummaryStatistics stats = IntStream.range(0, 100)
    .summaryStatistics();
System.out.println("Count: " + stats.getCount());
System.out.println("Sum: " + stats.getSum());
System.out.println("Average: " + stats.getAverage());
System.out.println("Min: " + stats.getMin());
System.out.println("Max: " + stats.getMax());
```

---

## Parallel Streams Deep Dive

### When Parallel Helps

```java
// Large dataset, expensive operation
List<Integer> million = getMillionIntegers();

long start = System.nanoTime();
int result = million.parallelStream()
    .filter(n -> expensiveCheck(n))  // Very expensive!
    .map(n -> expensiveTransform(n))
    .collect(summingInt(Integer::intValue));
long parallelDuration = System.nanoTime() - start;

// Sequential for comparison
start = System.nanoTime();
result = million.stream()
    .filter(n -> expensiveCheck(n))
    .map(n -> expensiveTransform(n))
    .collect(summingInt(Integer::intValue));
long sequentialDuration = System.nanoTime() - start;

System.out.println("Parallel: " + parallelDuration / 1_000_000.0 + "ms");
System.out.println("Sequential: " + sequentialDuration / 1_000_000.0 + "ms");
// Parallel might be 3-4x faster on 4-core CPU
```

### Parallel Pitfalls

```java
// ❌ Pitfall 1: Non-thread-safe collector
List<Integer> result = list.parallelStream()
    .collect(ArrayList::new,
        (list, item) -> list.add(item),  // NOT thread-safe!
        (list1, list2) -> list1.addAll(list2));  // Race condition!

// ✓ Fix: Use thread-safe collector
List<Integer> result = list.parallelStream()
    .collect(Collectors.toList());  // Thread-safe built-in

// ❌ Pitfall 2: Stateful operations hurt parallel
list.parallelStream()
    .distinct()  // All threads must coordinate here
    .sorted()    // Again, all threads wait
    .collect(Collectors.toList());
// Often slower than sequential!

// ✓ Solution: Filter first, minimize stateful ops
list.parallelStream()
    .filter(x -> x > 1000)  // Stateless, data reduced
    .limit(100)             // Short-circuit
    .collect(Collectors.toList());
```

---

## Debugging Streams

```java
// Method 1: peek() with detailed info
stream.peek(item -> 
    System.out.printf("Item: %s, Thread: %s%n", 
        item, Thread.currentThread().getName())
)

// Method 2: Log method with detailed context
.peek(item -> log.debug("Processing: {}", item))

// Method 3: Wrap in try-catch within peek
.peek(item -> {
    try {
        validateItem(item);
    } catch (Exception e) {
        System.err.println("Invalid: " + item);
        throw e;
    }
})

// Method 4: Conditional peek (for large datasets)
.peek(item -> {
    static int count = 0;
    if (count++ % 1000 == 0) {
        System.out.println("Processed " + count);
    }
})

// Method 5: Save intermediate stream for inspection
List<String> filtered = list.stream()
    .filter(someCondition)
    .collect(Collectors.toList());

System.out.println("Filtered items: " + filtered);  // Inspect here

List<String> transformed = filtered.stream()
    .map(String::toUpperCase)
    .collect(Collectors.toList());

System.out.println("Transformed: " + transformed);
```

---

## Performance Checklist

### ✓ DO:
- [ ] Filter early (reduce data flowing through pipeline)
- [ ] Use primitive streams for numeric operations
- [ ] Place short-circuiting operations last
- [ ] Parallel only for large datasets (100k+) with expensive ops
- [ ] Use method references (.map(String::length)) not lambdas
- [ ] Collect once, then iterate multiple times if needed
- [ ] Use appropriate terminal operation (count vs forEach)

### ✗ DON'T:
- [ ] Create streams repeatedly on same data
- [ ] Use forEach to build collections (use collect)
- [ ] Chain multiple stateful operations (distinct().sorted())
- [ ] Parallel with stateful operations
- [ ] Use limit() on unsorted stream then sort
- [ ] Ignore lock ordering in concurrent streams
- [ ] Forget terminal operation (nothing happens!)

---

## Real-World Example: E-commerce Data Processing

```java
class OrderProcessor {
    // Process 1M+ orders
    public Map<String, OrderStats> processOrders(List<Order> orders) {
        return orders.parallelStream()
            // Filter: Only unprocessed orders (early filtering!)
            .filter(o -> o.getStatus() == Status.PENDING)
            // Map: Transform to enriched orders
            .map(o -> enrichOrder(o))
            // Group: By customer region
            .collect(Collectors.groupingBy(
                Order::getRegion,
                Collectors.collectingAndThen(
                    Collectors.toList(),
                    this::calculateStats
                )
            ));
    }
    
    private Map<String, OrderStats> calculateStats(List<Order> regionOrders) {
        return regionOrders.stream()
            // Additional filtering (regional rules)
            .filter(o -> isValidInRegion(o))
            // Group by customer
            .collect(Collectors.groupingBy(
                Order::getCustomerId,
                Collectors.summarizingDouble(Order::getAmount)
            ))
            // Transform to stats
            .entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> new OrderStats(entry.getValue())
            ));
    }
}

// Execution:
// 1. Filter PENDING (might reduce from 1M to 300k)
// 2. Enrich in parallel (expensive operation on 300k)
// 3. Group by region (parallel merge)
// 4. Regional processing (parallelize well)
// 5. Result: Few seconds instead of minutes!
```

---

## Monitoring Stream Performance

```java
// With timing
long start = System.nanoTime();
List<Result> results = process(largeDataset);
long duration = (System.nanoTime() - start) / 1_000_000;

System.out.println("Processed " + results.size() + 
                   " items in " + duration + "ms");

// With thread count
long startThread = Thread.activeCount();
List<Result> results = process(largeDataset);
long endThread = Thread.activeCount();

System.out.println("Max threads used: " + endThread);

// ThreadMXBean for detailed profiling
ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
long startCPU = threadBean.getCurrentThreadCpuTime();

List<Result> results = process(largeDataset);

long endCPU = threadBean.getCurrentThreadCpuTime();
System.out.println("CPU time: " + 
    ((endCPU - startCPU) / 1_000_000) + "ms");
```

---

## Conclusion

Streams are powerful for:
✓ Readable, declarative code
✓ Easy parallelization
✓ Lazy evaluation efficiency
✓ Functional composition

But remember:
- Profile before optimizing
- Understand the cost of operations
- Use appropriate tools for the job
- Not all problems are best solved with Streams

**Next**: Review [04_INTERVIEW_ANSWERS/COMPREHENSIVE_QA.md](../04_INTERVIEW_ANSWERS/COMPREHENSIVE_QA.md) for interview questions combining all these concepts.

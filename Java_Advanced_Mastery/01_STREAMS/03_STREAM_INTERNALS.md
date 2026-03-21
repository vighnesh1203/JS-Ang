# Java Streams - Internals (How They ACTUALLY Work)

## Architecture Overview

Streams use an **internal iterator** pattern, NOT the traditional external iterator:

```
External Iterator (Traditional):        Internal Iterator (Streams):
for (int x : list) {                    list.stream()
    if (x > 5) {                            .filter(x -> x > 5)
        System.out.println(x);              .forEach(System.out::println);
    }
}

Control is with the loop                Control is with the Streams library
```

---

## Under the Hood: Spliterator

Streams use a `Spliterator` (Splittable Iterator) instead of Iterator:

```java
public interface Spliterator<T> {
    boolean tryAdvance(Consumer<? super T> action);  // Process one element
    Spliterator<T> trySplit();                       // Split for parallel
    long estimateSize();                             // How many left
    int characteristics();                           // Stream properties
}
```

**Why Spliterator?**
1. Supports splitting for parallel processing
2. Provides size estimates (for optimization)
3. Lazy evaluation by design
4. Efficient for custom sources

---

## Pipeline Construction (Lazy)

When you build this pipeline:

```java
list.stream()
    .filter(x -> x > 2)
    .map(x -> x * 2)
    .collect(Collectors.toList());
```

Here's what happens:

### Stage 1: Stream Source Created
```java
list.stream()  // Creates a Stream with a Spliterator from list
// No processing yet, just wraps the source
```

### Stage 2: Filter Added (LAZY)
```java
.filter(x -> x > 2)
// Creates a StatelessOp wrapper around the Stream
// Still no processing!

// Internally something like:
// new FilterOp(source, predicate)
```

### Stage 3: Map Added (LAZY)
```java
.map(x -> x * 2)
// Creates another StatelessOp wrapper
// Pipeline: FilterOp(MapOp(source))
// Still nothing happens!
```

### Stage 4: Terminal Operation (EAGER)
```java
.collect(Collectors.toList())
// NOW the pipeline executes!
// Elements flow through the entire pipeline
```

---

## How Elements Flow

Let's trace a single element through the pipeline:

```java
Arrays.asList(1, 2, 3, 4, 5).stream()
    .filter(x -> x > 2)         // Remove if false
    .map(x -> {
        System.out.println("Mapping: " + x);
        return x * 10;
    })
    .collect(Collectors.toList());
```

**Single element (3) flow**:
1. **Source** provides element `3` to filter
2. **Filter** checks: `3 > 2?` → YES, passes to map
3. **Map** transforms: `3 → 30`, prints "Mapping: 3"
4. **Collect** receives `30`, adds to list

**Result**: `[20, 30, 40, 50]` with output:
```
Mapping: 2
Mapping: 3
Mapping: 4
Mapping: 5
```

**Key**: NOT all filters run, THEN all maps run. Each element goes through the full pipeline.

---

## Stateless vs Stateful Operations

### Stateless (Can be optimized for parallel)
- `filter()`, `map()`, `flatMap()`, `peek()`
- Each element is processed independently
- No information needed from other elements

```java
stream.filter(x -> x > 5)  // Element 10's result doesn't depend on element 20
```

### Stateful (Harder to parallelize)
- `distinct()`, `sorted()`, `limit()`, `skip()`
- Require knowing about other elements

```java
stream.distinct()  // Must know ALL previous elements
stream.sorted()    // Must see ALL elements before sorting
stream.limit(5)    // Must track position across elements
```

**Impact**: Stateful operations can't fully utilize parallel Streams until they see all data.

---

## Short-Circuiting Operations

Some operations can stop early:

```java
// Terminal short-circuit operations:
findFirst()
findAny()
anyMatch()
allMatch()
noneMatch()
limit(n)

// When limit(3) is used:
list.stream()
    .filter(x -> x > 2)
    .limit(3)               // STOP after 3 elements pass!
    .collect(Collectors.toList());

// Only processes until 3 elements are collected
// If list has 1000 elements, might only process 100
```

**Real performance impact**:
```java
IntStream.range(0, 1_000_000)
    .filter(x -> x > 500_000)
    .findFirst();
// Stops after finding first match! Very fast.

// vs

IntStream.range(0, 1_000_000)
    .filter(x -> x > 500_000)
    .count();
// Must process ALL elements
```

---

## Lazy Evaluation - Deep Dive

```java
Stream<Integer> pipeline = Arrays.asList(1, 2, 3, 4, 5).stream()
    .filter(x -> {
        System.out.println("Filter called for: " + x);
        return x > 2;
    })
    .map(x -> {
        System.out.println("Map called for: " + x);
        return x * 10;
    });

// At this point, NO OUTPUT!
// System.out.println() in filter and map NOT called yet

List<Integer> result = pipeline.collect(Collectors.toList());
// NOW the output appears and operations execute
```

**Output when terminal op called**:
```
Filter called for: 1
Filter called for: 2
Filter called for: 3
Map called for: 3
Filter called for: 4
Map called for: 4
Filter called for: 5
Map called for: 5
```

The pipeline object is built but execution is deferred.

---

## Parallel Streams Internals

### Sequential (Default)
```java
list.stream()
    .filter(...)
    .map(...)
    .collect(...)
// Uses single thread, processes linearly
```

### Parallel
```java
list.parallelStream()  // OR list.stream().parallel()
    .filter(...)
    .map(...)
    .collect(...)

// Uses ForkJoinPool (default: num_cores threads)
```

**How parallel works**:

1. **Split**: Spliterator divides data into chunks
2. **Process**: Each thread processes its chunk independently
3. **Combine**: Results are combined (Collector must be thread-safe)

```
Large List [1,2,3,4,5,6,7,8]
    ↓
Split:  [1,2] [3,4] [5,6] [7,8]
    ↓
Thread1: process [1,2]    Thread2: process [3,4]
Thread3: process [5,6]    Thread4: process [7,8]
    ↓
Combine: [result1, result2, result3, result4]
```

**But there's overhead**:
- Thread creation/management
- Data splitting
- Combining results
- Synchronization

For small lists, overhead > benefit.

```java
// GOOD for parallel:
largeList.parallelStream()  // 100k+ elements
    .filter(this::expensiveOperation)
    .collect(Collectors.toList());

// BAD for parallel:
smallList.parallelStream()  // < 1000 elements
    .filter(x -> x > 5)     // cheap operation
    .collect(Collectors.toList());
```

---

## Memory Implications

### Pipeline holds references:

```java
Stream<String> stream = list.stream()
    .filter(s -> s.length() > 5)
    .map(String::toUpperCase);

// The pipeline holds:
// - Reference to source list
// - References to filter predicate
// - References to map function
```

If you don't complete the pipeline, these references stay in memory.

### Solution: Always complete pipelines
```java
// Good:
list.stream()
    .filter(...)
    .collect(Collectors.toList());  // Completed

// Risky:
Stream<String> incompletePipeline = list.stream()
    .filter(...);  // Never completed, references held
```

---

## peek() - Only for Debugging

```java
list.stream()
    .peek(x -> System.out.println("Element: " + x))  // Sees element but passes it through
    .filter(x -> x > 5)
    .peek(x -> System.out.println("After filter: " + x))
    .collect(Collectors.toList());
```

**Important**: peek() does NOT consume the stream. It's strictly for inspection/debugging.

---

## The Collector Pattern

When you use `collect()`, you're using the Collector interface:

```java
public interface Collector<T, A, R> {
    // A = intermediate accumulation type
    // T = input element type
    // R = result type
    
    Supplier<A> supplier();           // Create accumulator
    BiConsumer<A, T> accumulator();   // How to add element to accumulator
    BinaryOperator<A> combiner();     // How to merge accumulators (parallel)
    Function<A, R> finisher();        // Convert accumulator to result
}
```

### Example: Custom Collector

```java
// Collect to comma-separated string
list.stream()
    .map(Object::toString)
    .collect(Collectors.joining(","));

// Under the hood, it's building a StringBuilder:
// 1. supplier() → new StringBuilder()
// 2. accumulator() → append each element + ","
// 3. finisher() → toString()
```

---

## Return Type Analysis

### Functions vs Consumers vs Suppliers

```java
// Function<T,R>: T → R (transform)
Function<String, Integer> length = String::length;

// Consumer<T>: T → void (side effect)
Consumer<String> print = System.out::println;

// BiFunction<T,U,R>: (T,U) → R (combine)
BiFunction<Integer, Integer, Integer> add = (a, b) -> a + b;

// Predicate<T>: T → boolean (test)
Predicate<Integer> isPositive = x -> x > 0;

// Supplier<T>: () → T (generate)
Supplier<String> supply = () -> "Hello";
```

---

## Interview Questions - Internals

**Q1: Explain lazy evaluation with an example.**
A: Lazy evaluation means operations don't execute until a terminal operation is called. Example:
```java
stream.filter(x -> print("Filter")).collect(toList());
// "Filter" prints only when collect() runs, not before
```

**Q2: What's a Spliterator and why do we need it?**
A: Spliterator is a splittable iterator that supports:
- Lazy evaluation (via Consumer.accept())
- Efficient splitting for parallel processing
- Size estimates for optimization

**Q3: Stateless vs Stateful operations?**
A: 
- Stateless: filter, map (each element independent)
- Stateful: distinct, sorted, limit (need other elements' information)

**Q4: Performance impact of parallel()?**
A: Overhead (thread management, splitting, combining) often exceeds benefit for small collections. Use only for 100k+ elements with expensive operations.

**Q5: Why does forEach() not produce a result?**
A: forEach() is a terminal operation for side effects only. To get a collection, use collect().

---

## Performance Comparison

```java
List<Integer> list = Arrays.asList(1, 2, 3, ..., 1_000_000);

// Scenario 1: Simple filter
list.stream()
    .filter(x -> x > 500_000)
    .count();
// GOOD: Lazy evaluation stops early with count short-circuit

// Scenario 2: Multiple operations with distinct
list.stream()
    .map(x -> x % 100)      // Stateless
    .distinct()              // STATEFUL - needs all elements
    .count();
// SLOWER: distinct() must see everything

// Scenario 3: Parallel benefit
list.parallelStream()
    .filter(x -> expensiveCheck(x))  // Expensive operation
    .collect(toList());
// WORTH IT: Expensive operation split across cores
```

---

## Key Takeaways

1. **Lazy Evaluation**: Pipeline built, but execution deferred
2. **Spliterator**: Enables lazy evaluation + parallel processing
3. **Flow**: Each element processed through entire pipeline sequentially
4. **Short-circuiting**: Some operations can stop early (limit, findFirst)
5. **Parallel Overhead**: Only worth it for large collections + expensive ops
6. **Stateful Ops**: distinct(), sorted(), limit() harder to parallelize
7. **Memory**: Pipeline holds references until completed

---

**Next**: Jump to [04_PERFORMANCE_TUNING.md](04_PERFORMANCE_TUNING.md) for optimization strategies, or check [05_CODE_EXAMPLES/StreamOptimizations.java](../05_CODE_EXAMPLES/StreamOptimizations.java) for runnable examples.

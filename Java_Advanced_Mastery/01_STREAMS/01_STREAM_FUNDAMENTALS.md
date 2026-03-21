# Java Streams - Fundamentals

## What Are Streams? (The Real Answer)

A Stream is NOT a new data structure. It's a **functional pipeline** that:
1. Reads data from a source (collection, array, or generator)
2. Applies transformations lazily
3. Returns results only when a terminal operation is called

**Key Insight**: Streams are stateless, immutable sequences that enable functional-style operations.

---

## Streams vs Collections

| Aspect | Collection | Stream |
|--------|-----------|--------|
| Storage | Stores data in memory | No storage, processes on-demand |
| Operations | Eager - all operations run | Lazy - runs only when needed |
| Reusability | Can be reused | Can only be used once |
| Performance | Good for small data | Better for large data with filters |

**Example**:
```java
// Collection approach (eager)
List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
List<Integer> filtered = numbers.stream()
    .filter(n -> {
        System.out.println("Filter: " + n);  // Runs 5 times
        return n > 2;
    })
    .collect(Collectors.toList());

// Even though we filtered, all 5 print statements happen immediately
```

---

## Stream Pipeline Structure

Every Stream has exactly this structure:

```
[SOURCE] → [INTERMEDIATE OPS] → [TERMINAL OP] → [RESULT]
```

```java
List<String> result = list.stream()                    // SOURCE
    .filter(x -> x.length() > 2)                      // INTERMEDIATE 1
    .map(String::toUpperCase)                         // INTERMEDIATE 2
    .limit(10)                                        // INTERMEDIATE 3
    .collect(Collectors.toList());                    // TERMINAL - Result returned here!
```

### Source Operations
Where the stream gets its data:

```java
// From collections
Collection.stream()

// From arrays
Arrays.stream(array)

// From generators
Stream.generate(() -> Math.random())
Stream.iterate(0, n -> n + 1)

// Custom
Stream.of(1, 2, 3)

// From values
IntStream.range(0, 10)  // 0 to 9
IntStream.rangeClosed(0, 10)  // 0 to 10
```

---

## Intermediate Operations (Lazy)

**Important**: Intermediate operations return a new Stream and don't execute immediately.

### 1. filter() - Keep matching elements
```java
stream.filter(x -> x > 5)
// Only passes elements where condition is true
```

### 2. map() - Transform each element
```java
stream.map(String::toUpperCase)
// Each element is transformed

// Under the hood:
stream.map(x -> x.toUpperCase())
// For each element x, apply toUpperCase() and pass result to next operation
```

### 3. flatMap() - Flatten nested structures
```java
List<List<Integer>> nested = Arrays.asList(
    Arrays.asList(1, 2),
    Arrays.asList(3, 4)
);

nested.stream()
    .flatMap(List::stream)  // Flattens to: 1, 2, 3, 4
    .collect(Collectors.toList());
```

**Why flatMap?** When each element maps to a Stream, flatMap flattens it automatically.

```java
// Without flatMap (wrong way):
list.stream()
    .map(x -> Arrays.stream(new int[]{x, x * 2}))  // Returns Stream<Stream<Integer>>
    // This is nested and hard to use!

// With flatMap (correct):
list.stream()
    .flatMap(x -> Arrays.stream(new int[]{x, x * 2}))  // Returns Stream<Integer>
    // Automatically flattens!
```

### 4. distinct() - Remove duplicates
```java
stream.distinct()
// Removes duplicate elements (uses .equals() and .hashCode())
```

### 5. sorted() - Order elements
```java
stream.sorted()  // Uses natural order
stream.sorted(Comparator.reverseOrder())  // Descending

// Custom comparator:
stream.sorted((a, b) -> a.length() - b.length())  // By length
```

### 6. limit(n) - Take first n elements
```java
stream.limit(5)  // Only first 5 elements pass through
```

### 7. skip(n) - Skip first n elements
```java
stream.skip(2)  // First 2 are skipped, rest pass through
```

### 8. peek() - Inspect (Debugging only!)
```java
stream.peek(System.out::println)  // Doesn't consume, just shows
    .collect(Collectors.toList());
```

---

## Terminal Operations (Eager)

**Important**: Terminal operations trigger the entire pipeline and return a result.

### 1. collect() - Most Common
```java
// Collect to List
stream.collect(Collectors.toList());

// Collect to Set
stream.collect(Collectors.toSet());

// Collect to Map
stream.collect(Collectors.toMap(
    Person::getName,      // key function
    Person::getAge        // value function
));

// Collect with CUSTOM logic
stream.collect(
    Collectors.groupingBy(Person::getAge)  // Group by age
);
```

### 2. forEach() - Repeat operation
```java
stream.forEach(System.out::println);  // FOR SIDE EFFECTS ONLY
// Don't use forEach if you need a result!
```

### 3. reduce() - Combine into single value
```java
int sum = stream.reduce(0, (a, b) -> a + b);
// Combines all elements: ((((0 + 1) + 2) + 3) + 4)

// Without initial value:
Optional<Integer> sum = stream.reduce((a, b) -> a + b);
```

### 4. count() - How many elements
```java
long count = stream.count();
```

### 5. findFirst() / findAny() - Get one element
```java
Optional<String> first = stream.findFirst();
Optional<String> any = stream.findAny();  // Better for parallel
```

### 6. anyMatch() / allMatch() / noneMatch()
```java
boolean hasLarge = stream.anyMatch(x -> x > 100);  // At least one?
boolean allPositive = stream.allMatch(x -> x > 0);  // All match?
boolean noneNegative = stream.noneMatch(x -> x < 0);  // None match?
```

### 7. min() / max()
```java
Optional<Integer> minimum = stream.min(Integer::compare);
Optional<Integer> maximum = stream.max(Integer::compare);
```

---

## Lazy Evaluation - The Core Concept

This is what makes Streams powerful:

```java
List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);

Stream<Integer> pipeline = numbers.stream()      // Step 1
    .filter(x -> {
        System.out.println("Filter: " + x);
        return x > 2;
    })
    .map(x -> {
        System.out.println("Map: " + x);
        return x * 2;
    });
// NOTHING HAPPENS YET! No output!

System.out.println("\n--- Terminal operation called ---\n");
List<Integer> result = pipeline.collect(Collectors.toList());

/* Output:
Filter: 1
Filter: 2
Filter: 3
Map: 3
Filter: 4
Map: 4
Filter: 5
Map: 5
*/
```

**Nothing happens until `.collect()` is called!**

This is why Streams are efficient:
- Elements are processed one at a time through the entire pipeline
- If a filter rejects an element, it doesn't go to map()
- If you call `limit(2)`, processing stops after 2 elements pass

---

## Primitive Streams (IntStream, LongStream, DoubleStream)

Use these for primitive performance:

```java
// Instead of Stream<Integer> (boxing/unboxing overhead)
IntStream stream = IntStream.range(0, 1000);

stream.filter(x -> x > 500)       // Works on primitives
    .map(x -> x * 2)              // No boxing!
    .sum();                        // Terminal

// Useful operations on primitive streams:
.sum()       // Total
.average()   // OptionalDouble
.max()       // OptionalInt
.min()       // OptionalInt
```

---

## Common Mistakes

### ❌ Mistake 1: Using Stream twice
```java
Stream<Integer> stream = list.stream();
stream.forEach(System.out::println);    // Works
stream.forEach(System.out::println);    // ERROR! Stream already consumed
```

### ❌ Mistake 2: Forgetting terminal operation
```java
list.stream()
    .filter(x -> x > 5)  // Nothing happens! Just returns Stream
    .map(String::toUpperCase);  // Still nothing! No terminal op
```

### ❌ Mistake 3: Using forEach for collection
```java
List<Integer> result = new ArrayList<>();
stream.forEach(x -> result.add(x * 2));  // Works but WRONG!

// Correct:
List<Integer> result = stream
    .map(x -> x * 2)
    .collect(Collectors.toList());
```

### ❌ Mistake 4: Parallel without understanding costs
```java
stream.parallel()  // Overhead > benefit for small streams
    .collect(Collectors.toList());

// Parallel is good only for:
// - Large collections (100k+)
// - Expensive operations
// - CPUs with multiple cores not already busy
```

---

## Interview Questions - Section 1

**Q1: What's the difference between Stream and Collection?**
A: Collection stores data; Stream processes it. Collections are eager; Streams are lazy. Collections can be reused; Streams can only be used once.

**Q2: Why use Streams instead of for loops?**
A: 
- Cleaner, more readable code
- Lazy evaluation (efficiency)
- Easier parallelization
- Functional style

**Q3: What happens if you don't call a terminal operation?**
A: Nothing! The pipeline is built but not executed. No elements are processed.

**Q4: Can you use a Stream twice?**
A: No, once consumed it's done. You need to create a new Stream.

**Q5: When should you use parallel()?**
A: Only for large datasets (100k+) with expensive operations. Parallel has overhead.

---

## Quick Reference

| Operation | Type | Returns |
|-----------|------|---------|
| filter() | Intermediate | Stream |
| map() | Intermediate | Stream |
| flatMap() | Intermediate | Stream |
| distinct() | Intermediate | Stream |
| sorted() | Intermediate | Stream |
| limit() | Intermediate | Stream |
| skip() | Intermediate | Stream |
| collect() | Terminal | Collection |
| forEach() | Terminal | void |
| reduce() | Terminal | Value |
| count() | Terminal | long |
| findFirst() | Terminal | Optional |
| anyMatch() | Terminal | boolean |

---

**Next**: Move on to [02_INTERMEDIATE_OPERATIONS.md](02_INTERMEDIATE_OPERATIONS.md) for advanced stream operations, or jump to [03_STREAM_INTERNALS.md](03_STREAM_INTERNALS.md) to understand how Streams actually work internally.

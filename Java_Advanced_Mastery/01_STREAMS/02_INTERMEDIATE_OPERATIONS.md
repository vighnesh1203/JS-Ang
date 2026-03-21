# Java Streams - Intermediate Operations Deep Dive

## Complete Guide to Every Intermediate Operation

Intermediate operations return a Stream and don't execute immediately (lazy).

---

## 1. filter(Predicate)

**What it does**: Keep only elements that match the predicate

```java
List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);

// Basic filter
numbers.stream()
    .filter(n -> n > 2)
    .collect(Collectors.toList());  // [3, 4, 5]

// Filter with complex condition
numbers.stream()
    .filter(n -> n > 2 && n < 5)
    .collect(Collectors.toList());  // [3, 4]

// Multiple filters (avoid if condition is complex)
numbers.stream()
    .filter(n -> n > 2)
    .filter(n -> n < 5)
    .collect(Collectors.toList());  // [3, 4]
// Better to combine: .filter(n -> n > 2 && n < 5)
```

**Performance**: O(n), each element checked once

---

## 2. map(Function)

**What it does**: Transform each element from type T to type R

```java
List<String> words = Arrays.asList("java", "streams");

// Type transformation
words.stream()
    .map(String::length)  // String → Integer
    .collect(Collectors.toList());  // [4, 7]

// Object to object
class Person {
    String name;
    int age;
}

List<Person> people = ...;
people.stream()
    .map(Person::getName)  // Person → String
    .collect(Collectors.toList());

// Method reference vs lambda (equivalent)
words.stream().map(String::toUpperCase);     // Method reference (cleaner)
words.stream().map(w -> w.toUpperCase());    // Lambda (explicit)

// Chaining transformations
words.stream()
    .map(String::toUpperCase)
    .map(s -> s + "!")
    .map(String::length)
    .collect(Collectors.toList());  // [5, 8]
```

**Performance**: O(n), each element transformed once

---

## 3. flatMap(Function<T, Stream<R>>)

**What it does**: Transform each element into a Stream, then flatten (remove nesting)

**Key difference from map()**: Result is Stream<Stream<T>>, flatMap flattens to Stream<T>

```java
// Without flatMap - WRONG (nested streams):
List<List<Integer>> nested = Arrays.asList(
    Arrays.asList(1, 2),
    Arrays.asList(3, 4)
);

nested.stream()
    .map(List::stream)              // Type: Stream<Stream<Integer>>
    .collect(Collectors.toList());  // List of Streams - hard to use!

// With flatMap - CORRECT (flattened):
nested.stream()
    .flatMap(List::stream)          // Type: Stream<Integer>
    .collect(Collectors.toList());  // [1, 2, 3, 4] - easy to use!

// Practical example - words to characters
List<String> words = Arrays.asList("hello", "world");

words.stream()
    .flatMap(word -> word.chars()
        .mapToObj(c -> String.valueOf((char)c)))
    .collect(Collectors.toList());  // [h,e,l,l,o,w,o,r,l,d]

// Another example - numbers to multiple results
List<Integer> numbers = Arrays.asList(1, 2, 3);

numbers.stream()
    .flatMap(n -> Stream.of(n, n*2, n*3))  // Each n → 3 values
    .collect(Collectors.toList());  // [1,2,3,2,4,6,3,6,9]

// ArrayStream from array
List<String> paths = Arrays.asList("file1,file2", "file3,file4");

paths.stream()
    .flatMap(s -> Arrays.stream(s.split(",")))
    .collect(Collectors.toList());  // [file1, file2, file3, file4]
```

**Performance**: O(n * m) where n = outer, m = inner stream size

---

## 4. distinct()

**What it does**: Remove duplicate elements (uses equals() and hashCode())

```java
List<Integer> numbers = Arrays.asList(1, 2, 2, 3, 3, 3, 1);

numbers.stream()
    .distinct()
    .collect(Collectors.toList());  // [1, 2, 3]

// For objects, must implement equals() and hashCode()
class User {
    String id;
    String name;
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return id.equals(user.id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
}

List<User> users = Arrays.asList(
    new User("1", "Alice"),
    new User("2", "Bob"),
    new User("1", "Alice")  // Duplicate
);

users.stream()
    .distinct()  // Returns only 2 users (removes duplicate id="1")
    .collect(Collectors.toList());
```

**Implementation**: Uses HashSet internally, O(n) average

**Warning**: Stateful operation, can't be fully parallelized until it sees all elements

---

## 5. sorted()

**What it does**: Sort elements (natural order or custom Comparator)

```java
List<Integer> numbers = Arrays.asList(3, 1, 4, 1, 5, 9);

// Natural sort (ascending)
numbers.stream()
    .sorted()
    .collect(Collectors.toList());  // [1, 1, 3, 4, 5, 9]

// Reverse sort (descending)
numbers.stream()
    .sorted(Comparator.reverseOrder())
    .collect(Collectors.toList());  // [9, 5, 4, 3, 1, 1]

// Custom comparator
List<String> words = Arrays.asList("java", "is", "great");

// Sort by length (ascending)
words.stream()
    .sorted(Comparator.comparingInt(String::length))
    .collect(Collectors.toList());  // [is, java, great]

// Sort by length (descending)
words.stream()
    .sorted(Comparator.comparingInt(String::length).reversed())
    .collect(Collectors.toList());  // [great, java, is]

// Multiple criteria
class Person {
    String name;
    int age;
}

List<Person> people = ...;

people.stream()
    .sorted(Comparator
        .comparingInt(Person::getAge)           // Primary: by age
        .thenComparing(Person::getName))        // Secondary: by name
    .collect(Collectors.toList());
```

**Performance**: O(n log n) - requires seeing all elements

**Warning**: Stateful operation, inefficient for parallel

---

## 6. limit(long n)

**What it does**: Take first n elements, discard rest

```java
List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);

numbers.stream()
    .limit(3)
    .collect(Collectors.toList());  // [1, 2, 3]

// Useful with sorting
numbers.stream()
    .sorted()
    .limit(2)
    .collect(Collectors.toList());  // [1, 2]

// Top N pattern (most common use)
List<Integer> numbers = Arrays.asList(1, 100, 50, 25, 75);

numbers.stream()
    .sorted(Comparator.reverseOrder())
    .limit(3)
    .collect(Collectors.toList());  // [100, 75, 50] - top 3
```

**Performance**: O(n) worst case, but can short-circuit and stop early

**Important**: Short-circuiting operation - stops processing after n elements

---

## 7. skip(long n)

**What it does**: Skip first n elements, process rest

```java
List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);

numbers.stream()
    .skip(2)
    .collect(Collectors.toList());  // [3, 4, 5]

// Paging pattern (skip 10 per page, take 10 per page)
currentPage = 2;  // Page 2, 0-indexed
pageSize = 10;

numbers.stream()
    .skip((long)currentPage * pageSize)
    .limit(pageSize)
    .collect(Collectors.toList());  // Items 10-20

// Skip while testing
numbers.stream()
    .skip(2)
    .filter(n -> n > 3)  // Only check [3, 4, 5]
    .collect(Collectors.toList());
```

**Performance**: O(n) to skip, but actual processing only on remaining

---

## 8. peek(Consumer)

**What it does**: Inspect each element without consuming the stream (for debugging)

```java
List<Integer> numbers = Arrays.asList(1, 2, 3);

numbers.stream()
    .filter(n -> n > 1)
    .peek(n -> System.out.println("After filter: " + n))
    .map(n -> n * 2)
    .peek(n -> System.out.println("After map: " + n))
    .collect(Collectors.toList());

// Output:
// After filter: 2
// After map: 4
// After filter: 3
// After map: 6

// Debug pattern with conditional output
stream.peek(item -> {
    if (item.isInvalid()) {
        System.err.println("Invalid: " + item);
    }
})
```

**Important**: peek() doesn't consume the stream! You still need a terminal operation.

**Use case**: Debugging intermediate results in a pipeline

```java
// WRONG - nothing happens!
numbers.stream()
    .filter(n -> n > 1)
    .peek(System.out::println);  // No output! No terminal op!

// Correct - add terminal operation
numbers.stream()
    .filter(n -> n > 1)
    .peek(System.out::println)
    .collect(Collectors.toList());  // Now peek is called
```

---

## 9. map() vs flatMap() vs peek() Comparison

```java
List<String> words = Arrays.asList("hello", "world");

// map() - one element → one element (type T → R)
words.stream()
    .map(String::length)           // Type: Stream<Integer>
    .collect(Collectors.toList()); // [5, 5]

// flatMap() - one element → multiple elements (T → Stream<R>)
words.stream()
    .flatMap(word ->
        word.chars()
            .mapToObj(c -> (char)c)
    )                              // Type: Stream<Character>
    .collect(Collectors.toList()); // [h,e,l,l,o,w,o,r,l,d]

// peek() - inspect without transformation
words.stream()
    .peek(word -> System.out.println("Processing: " + word))
    .map(String::toUpperCase)
    .peek(word -> System.out.println("Result: " + word))
    .collect(Collectors.toList());
```

---

## 10. Order of Operations - Performance Impact

```java
List<Integer> numbers = getMillionNumbers();

// ❌ SLOW: Sort all, then filter
numbers.stream()
    .sorted()                    // Sort 1,000,000 elements
    .filter(n -> n > 900_000)   // Keep only 100,000
    .collect(Collectors.toList());

// ✓ FAST: Filter first, then sort
numbers.stream()
    .filter(n -> n > 900_000)   // Keep only 100,000
    .sorted()                    // Sort just 100,000
    .collect(Collectors.toList());

// ✓ FASTEST: Filter, sort, then limit
numbers.stream()
    .filter(isValid())          // Reduce to 500k
    .sorted()                   // Sort 500k
    .limit(100)                 // Take first 100
    .collect(Collectors.toList());
```

**Rule**: Filter early → Stateless ops → Stateful ops → Limit

---

## Summary Table

| Operation | Returns | Lazy | Stateful | Use For |
|-----------|---------|------|----------|---------|
| filter() | Stream | Yes | No | Removing elements |
| map() | Stream | Yes | No | Transforming elements |
| flatMap() | Stream | Yes | No | Flattening nested structures |
| distinct() | Stream | Yes | Yes | Removing duplicates |
| sorted() | Stream | Yes | Yes | Ordering elements |
| limit() | Stream | Yes | No* | Taking first N |
| skip() | Stream | Yes | No | Skipping first N |
| peek() | Stream | Yes | No | Debugging/inspection |

*limit() can short-circuit, so effectively reduces processing

---

**Next**: Study [03_STREAM_INTERNALS.md](03_STREAM_INTERNALS.md) for how these work internally, or run [../05_CODE_EXAMPLES/StreamOptimizations.java](../05_CODE_EXAMPLES/StreamOptimizations.java) for performance comparisons.

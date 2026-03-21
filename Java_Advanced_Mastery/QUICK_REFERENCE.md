# Java Advanced Mastery - Quick Reference Guide

## STREAMS - Quick Lookup

### Cheat Sheet

```java
// SOURCE
Collection.stream()
Arrays.stream(array)
Stream.of(values)
Stream.generate(supplier)
Stream.iterate(seed, operator)
IntStream.range(0, 10)

// INTERMEDIATE (returns Stream)
.filter(predicate)        // Keep matching
.map(function)            // Transform each
.flatMap(function)        // Transform & flatten
.distinct()               // Remove duplicates
.sorted()                 // Order elements
.sorted(comparator)       // Custom order
.limit(n)                 // Take first n
.skip(n)                  // Skip first n
.peek(consumer)           // Debug inspection
.parallel()               // Enable parallelism
.sequential()             // Disable parallelism

// TERMINAL (returns result, consumes stream)
.collect(Collectors.toList())      // → List
.collect(Collectors.toSet())       // → Set
.collect(Collectors.joining(","))  // → String
.forEach(consumer)                 // Each element
.reduce(initialValue, operator)    // Combine all
.count()                           // How many
.findFirst()                       // Optional<T>
.findAny()                         // Optional<T>
.anyMatch(predicate)               // boolean
.allMatch(predicate)               // boolean
.noneMatch(predicate)              // boolean
.min(comparator)                   // Optional<T>
.max(comparator)                   // Optional<T>
```

### Common Patterns

```java
// Grouping
.collect(groupingBy(keyFunction))
.collect(groupingBy(keyFunction, summingInt(valueFunction)))

// Top N
.sorted(comparingInt(Integer::intValue).reversed())
.limit(10)

// Distinct with custom key
.collect(toMap(keyFunc, Function.identity(), (a,b)->a))
.values()

// Range with step
IntStream.iterate(0, i -> i + 2)  // Step by 2
.limit(n)

// Partitioning (split into two groups)
.collect(partitioningBy(predicate))
```

---

## CONCURRENCY - Quick Lookup

### Thread Creation
```java
// Recommended: Runnable + Lambda
Thread t = new Thread(() -> {
    System.out.println("Running");
});
t.start();  // NOT run()!

t.join();   // Wait for completion
```

### Synchronization
```java
// Method
public synchronized void method() { ... }

// Block
synchronized(object) { ... }

// Lock
Lock lock = new ReentrantLock();
lock.lock();
try { ... } finally { lock.unlock(); }

// With timeout
if (lock.tryLock(1, TimeUnit.SECONDS)) {
    try { ... } finally { lock.unlock(); }
}
```

### Atomic Variables
```java
AtomicInteger count = new AtomicInteger(0);
count.incrementAndGet();
count.decrementAndGet();
count.addAndGet(5);
count.compareAndSet(1, 2);
```

### Thread Pools
```java
ExecutorService executor = Executors.newFixedThreadPool(10);
executor.submit(() -> doWork());
executor.shutdown();
executor.awaitTermination(1, TimeUnit.MINUTES);
```

### Wait/Notify Pattern
```java
synchronized(monitor) {
    while (!condition) {
        monitor.wait();  // Release lock, sleep
    }
    // Do work
    monitor.notifyAll();  // Wake others
}
```

### Collections
```java
ConcurrentHashMap<K, V> map = new ConcurrentHashMap<>();
map.putIfAbsent(key, value);
map.replace(key, oldValue, newValue);

CopyOnWriteArrayList<T> list = new CopyOnWriteArrayList<>();
// Safe for concurrent iteration while modifying
```

---

## GARBAGE COLLECTION - Quick Lookup

### Heap Structure
```
Young Generation (30%)  → Minor GC (frequent, ~10ms)
├── Eden
├── Survivor 0
└── Survivor 1

Old Generation (70%)    → Major GC (rare, ~200ms)
```

### Object Lifetime
```
Creation → Allocated in Eden
↓
Minor GC → Survives → Moved to S0 (Age=1)
↓
Minor GC → Survives → Moved to S1 (Age=2)
↓
Repeat until Age=15 → Promoted to Old Gen
↓
Old Gen Full → Major GC
```

### GC Collectors

| Collector | Use Case | Pause |
|-----------|----------|-------|
| G1GC | General, recommended | Predictable |
| ParallelGC | Throughput > latency | High |
| CMS | Low latency | Low |
| ZGC | Ultra-low latency | <10ms |

### JVM Flags
```bash
# Heap sizing
-Xms2g -Xmx4g          # Min and Max heap

# GC selection
-XX:+UseG1GC           # G1 (default in Java 9+)
-XX:+UseParallelGC     # Parallel
-XX:+UseConcMarkSweepGC # CMS (deprecated)
-XX:+UseZGC            # ZGC

# Tuning
-XX:MaxGCPauseMillis=200   # Target pause time (G1)
-XX:ParallelGCThreads=8    # Threads for parallel

# Logging
-Xlog:gc*:file=gc.log:time,level,tags
-XX:+PrintGCDetails
-XX:+PrintGCDateStamps
```

---

## Interview Reminders

### Streams
- ✓ Lazy evaluation - nothing happens until terminal op
- ✓ Can't reuse same stream object
- ✓ Use peek() only for debugging
- ✓ Parallel has overhead - only for large datasets
- ✓ Order operations: filter first, then expensive ops

### Concurrency
- ✓ Race condition = multiple threads + modification + order-dependent
- ✓ synchronized = mutual exclusion + visibility
- ✓ volatile = visibility only (for flags, single writer)
- ✓ Deadlock = circular lock dependencies (prevent with lock ordering)
- ✓ wait() releases lock, sleep() keeps lock

### Garbage Collection
- ✓ GC marks reachable objects from GC roots
- ✓ Minor GC collects Young Gen (frequent, fast)
- ✓ Major GC collects full heap (rare, slow)
- ✓ Memory leaks = objects held but not needed
- ✓ Profile before optimizing

---

## Decision Tree

### Choose Streams vs Loop
```
Question: Are you transforming/filtering data?
  YES → Use Streams (cleaner, parallelizable)
  NO → Use Loop (simpler, faster)

Question: Do you need multiple passes?
  YES → Use Loop (streams consumed once)
  NO → Use Streams (laziness efficient)
```

### Choose synchronized vs Locks
```
Question: Need timeout?
  YES → ReentrantLock.tryLock()
  NO → synchronized is simpler

Question: Multiple conditions?
  YES → ReentrantLock + Condition
  NO → synchronized + wait/notify
```

### Choose Parallel?
```
Question: Dataset size?
  < 10k → NO (overhead > benefit)
  10k-100k → Maybe (depends on operation cost
  > 100k → YES (usually worth it)

Question: Operation cost?
  Cheap (String.toUpperCase) → NO
  Expensive (complex computation) → YES

Question: CPU already busy?
  YES → NO (thread contention)
  NO → Probably YES
```

---

## Common Mistakes

### ❌ Mistake 1: Calling stream.run() not stream.start()
```java
Thread t = new Thread(() -> doWork());
t.run();   // Runs in CURRENT thread, wrong!
t.start(); // Runs in NEW thread, correct
```

### ❌ Mistake 2: Forgetting finally in lock
```java
// Memory leak if exception occurs
lock.lock();
doWork();  // If throws exception, unlock never called!
lock.unlock();

// Correct
lock.lock();
try {
    doWork();
} finally {
    lock.unlock();
}
```

### ❌ Mistake 3: Using wait() outside synchronized
```java
// ERROR!
public void method() {
    object.wait();  // IllegalMonitorStateException
}

// Correct
public synchronized void method() {
    object.wait();  // OK, inside synchronized
}
```

### ❌ Mistake 4: Memory leak with ThreadLocal
```java
static ThreadLocal<byte[]> cache = new ThreadLocal<>();

// Without cleanup - memory leak!
cache.set(new byte[1MB]);

// With cleanup
try {
    cache.set(new byte[1MB]);
} finally {
    cache.remove();  // Always cleanup!
}
```

### ❌ Mistake 5: Filter after expensive operation
```java
// Bad - does expensive work on filtered-out elements
stream.map(this::expensiveOp)
    .filter(x -> x > 100);

// Good - filter first
stream.filter(x -> x > 100)
    .map(this::expensiveOp);
```

---

## Real Interview Scenarios

### Scenario 1: "Optimize this code"
**What they want**: Show knowledge of lazy evaluation, short-circuiting, operation ordering

**Answer approach**:
1. Look for stateful operations that could be later
2. Check if you can use limit() or findFirst() instead of count()
3. Filter before expensive map operations
4. Only use parallel for large datasets

### Scenario 2: "Why is this test failing?"
**Common culprits**: Race conditions, deadlocks, stream reuse, missing synchronized

**Debug approach**:
1. Run multiple times (race conditions often intermittent)
2. Add synchronization, see if it fixes it
3. Check lock ordering (prevents deadlock)
4. Verify stream is created fresh each use

### Scenario 3: "Performance problem - GC pauses"
**Investigation**: Check GC logs, analyze pause frequency and duration

**Solution options**:
1. Increase heap size (-Xmx)
2. Find memory leak (use profiler)
3. Switch GC algorithm (see GC Collectors table)
4. Set pause time target (-XX:MaxGCPauseMillis)

---

## Resources for Deeper Learning

1. **Java 17 Docs**: https://docs.oracle.com/en/java/javase/17/
2. **Oracle Collections Framework**: Stream API details
3. **Java Concurrency in Practice**: Book on threading (essential)
4. **Garbage Collection Tuning Guide**: Oracle official guide

---

## Assessments

### Can you explain...?

- [ ] Why Streams use lazy evaluation and benefit of it?
- [ ] Exact difference between filter(), map(), flatMap()?
- [ ] When and why to use parallel/Streams?
- [ ] How synchronized works (intrinsic locks)?
- [ ] What a deadlock is and how to prevent it?
- [ ] Difference between Minor GC and Major GC?
- [ ] Common memory leak patterns?
- [ ] How mark-and-sweep GC works?

If you can explain all of these clearly, you're ready for interviews!

---

**Last Updated**: March 2026

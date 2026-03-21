# Comprehensive Java Interview Q&A

## Table of Contents
- [Streams (15 questions)](#streams)
- [Concurrency (20 questions)](#concurrency)
- [Garbage Collection (10 questions)](#garbage-collection)
- [Performance & Tuning (5 questions)](#performance--tuning)

---

# STREAMS

## Q1: Explain the difference between Stream and Collection

**Answer:**
A Collection stores data in memory and provides access to it. A Stream processes data on-demand without storing it.

| Aspect | Collection | Stream |
|--------|-----------|--------|
| Storage | All data in memory | No storage, lazy processing |
| Reusability | Can iterate multiple times | Can only be used once |
| Operations | Eager (all run immediately) | Lazy (run only when needed) |
| Modification | Can add/remove elements | Immutable, no modification |

**Real code difference:**
```java
// Collection - operates on all elements
List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
List<Integer> doubled = new ArrayList<>();
for (Integer n : numbers) {
    if (n > 2) {
        doubled.add(n * 2);
    }
}

// Stream - lazy pipeline
List<Integer> doubled = numbers.stream()
    .filter(n -> n > 2)
    .map(n -> n * 2)
    .collect(Collectors.toList());
```

---

## Q2: What is lazy evaluation and why does it matter?

**Answer:**
Lazy evaluation means operations don't execute until a terminal operation is called.

**Why it matters:**
1. **Performance**: Skip unnecessary operations with `limit()` or `findFirst()`
2. **Memory**: Don't create intermediate collections
3. **Efficiency**: Short-circuit evaluation stops early

**Example:**
```java
// Without lazy (eager, traditional):
List<Integer> nums = list.stream()
    .filter(x -> x > 5)      // Processes ALL elements
    .map(x -> x * 2)         // Processes ALL elements
    .limit(3)                // Only THEN takes first 3
    .collect(toList());      // [12, 14, 16, ...]

// With lazy (actual Stream behavior):
List<Integer> nums = list.stream()
    .filter(x -> x > 5)      // Lazily defined
    .map(x -> x * 2)         // Lazily defined
    .limit(3)                // Lazily defined
    .collect(toList());      // NOW they execute!
    // Only processes until 3 elements pass all operations!
```

---

## Q3: Explain map() vs flatMap()

**Answer:**
`map()` transforms each element. `flatMap()` transforms and flattens (removes nesting).

```java
// map() - one-to-one transformation
List<String> words = Arrays.asList("hello", "world");
List<Integer> lengths = words.stream()
    .map(String::length)        // Each word → its length
    .collect(toList());         // [5, 5]

// flatMap() - one-to-many with flattening
List<String> words = Arrays.asList("hello", "world");
List<String> chars = words.stream()
    .flatMap(word -> word.chars()  // hello → 'h','e','l','l','o'
        .mapToObj(c -> String.valueOf((char)c)))
    .collect(toList());         // [h,e,l,l,o,w,o,r,l,d]

// Why flatMap?
List<List<Integer>> nested = Arrays.asList(
    Arrays.asList(1, 2),
    Arrays.asList(3, 4)
);

// Wrong approach with map:
nested.stream()
    .map(List::stream)          // Stream<Stream<Integer>> - nested!
    .collect(toList());         // Can't use this easily

// Right approach with flatMap:
nested.stream()
    .flatMap(List::stream)      // Automatically flattened to Stream<Integer>
    .collect(toList());         // [1, 2, 3, 4]
```

---

## Q4: How would you optimize a Stream with parallel()?

**Answer:**
Use parallel only when:
1. **Large dataset** (100k+ elements)
2. **Expensive operation**
3. **CPU not already busy**

```java
// BAD - parallel overhead exceeds benefit
List<String> small = Arrays.asList("a", "b", "c");
small.parallelStream()
    .map(String::toUpperCase)   // Too cheap, overhead not worth it
    .collect(toList());

// GOOD - parallel actually helps
List<Integer> large = LargeDataGenerator.getMillionIntegers();
long sum = large.parallelStream()
    .filter(x -> isExpensiveCheck(x))  // Expensive operation
    .map(x -> costlyTransform(x))      // Distributed across cores
    .collect(summingLong(x -> x));

// Practical guideline:
int size = list.size();
if (size > 100_000 && list.stream().map(x -> complexOp(x)).count() > 10_000) {
    return list.parallelStream()       // Use parallel
        .filter(x -> condition(x))
        .map(x -> transform(x))
        .collect(toList());
} else {
    return list.stream()               // Sequential is faster
        .filter(x -> condition(x))
        .map(x -> transform(x))
        .collect(toList());
}
```

---

## Q5: What happens when you call Stream twice?

**Answer:**
**Error!** A Stream can only be consumed once. Calling a terminal operation consumes the stream.

```java
Stream<Integer> stream = Arrays.asList(1, 2, 3).stream();

stream.forEach(System.out::println);  // Works, consumes stream
stream.forEach(System.out::println);  // IllegalStateException: stream has already been operated upon or closed
```

**Solution:**
```java
// Create new stream each time
List<Integer> list = Arrays.asList(1, 2, 3);

list.stream().forEach(System.out::println);  // Fresh stream
list.stream().forEach(System.out::println);  // Fresh stream

// OR collect first
List<Integer> result = list.stream()
    .filter(x -> x > 1)
    .collect(toList());

result.forEach(System.out::println);  // Can use collection multiple times
```

---

## Q6: Difference between findFirst() and findAny()?

**Answer:**
Both return `Optional<T>`. In sequential streams they're same. In parallel streams:
- `findFirst()`: Returns first element in encounter order
- `findAny()`: Returns any element (potentially faster in parallel)

```java
// Sequential - same result
stream.findFirst();  // Optional[1]
stream.findAny();    // Optional[1]

// Parallel - might differ
list.parallelStream()
    .filter(x -> x > 100)
    .findFirst()     // First matching (order guaranteed)
    .ifPresent(System.out::println);

list.parallelStream()
    .filter(x -> x > 100)
    .findAny()       // Any matching (faster, no order)
    .ifPresent(System.out::println);

// Use findAny() in parallel for better performance!
```

---

## Q7: Explain distinct() and when it's stateful

**Answer:**
`distinct()` removes duplicates but needs to see all previous elements (stateful operation).

```java
// Simple use:
stream.distinct()
    .forEach(System.out::println);

// Stateful problem - can't parallelize effectively:
list.parallelStream()
    .distinct()          // Each thread must track what others have seen!
    .collect(toList());  // Overhead negates parallel benefit

// Performance impact:
// Sequential: O(n) with HashSet
// Parallel: Much slower due to synchronization

// Impact on pipeline:
list.stream()
    .filter(x -> x > 5)       // Stateless - fast
    .distinct()               // STATEFUL - slower, can't skip
    .map(x -> x * 2)          // Stateless
    .limit(100)               // SHORT-CIRCUIT - stops early
    .collect(toList());

// If many duplicates after filter, distinct() is expensive
// If you limit early, distinct() still processes all

// Better design:
List<Integer> distinct = list.stream()
    .limit(1000)              // SHORT-CIRCUIT first
    .filter(x -> x > 5)       // Then filter
    .distinct()               // Then distinct (on smaller set)
    .collect(toList());
```

---

## Q8: How would you debug a Stream pipeline?

**Answer:**
Use `peek()` for inspection (debugging only):

```java
List<Integer> result = list.stream()
    .peek(x -> System.out.println("Start: " + x))
    .filter(x -> x > 5)
    .peek(x -> System.out.println("After filter: " + x))
    .map(x -> x * 2)
    .peek(x -> System.out.println("After map: " + x))
    .limit(3)
    .peek(x -> System.out.println("After limit: " + x))
    .collect(toList());

// Output shows exact flow through pipeline
// Helps identify which operations are slow or filtering out data

// Advanced debugging:
list.stream()
    .peek(x -> {
        if (x > 1000) {
            throw new RuntimeException("Value exceeded: " + x);
        }
    })
    .map(x -> x * 2)
    .collect(toList());
```

---

## Q9: When should you use reduce() vs collect()?

**Answer:**
- **reduce()**: Combine stream into single value (mutable or immutable)
- **collect()**: Transform stream into collection with specialized accumulation

```java
// reduce() - get single value
int sum = list.stream()
    .filter(x -> x > 0)
    .reduce(0, (a, b) -> a + b);  // Identity: 0, Accumulator: sum

int product = list.stream()
    .reduce(1, (a, b) -> a * b);  // Identity: 1, Accumulator: multiply

// collect() - transform into collection
List<Integer> doubled = list.stream()
    .map(x -> x * 2)
    .collect(Collectors.toList());  // Transforms to List

Map<Integer, String> map = list.stream()
    .collect(Collectors.toMap(
        Function.identity(),       // Key
        String::valueOf));         // Value

// reduce() with no identity:
Optional<Integer> sum = list.stream()
    .reduce((a, b) -> a + b);  // Returns Optional, might be empty

// Advanced reduce with mutable accumulator:
List<Integer> squared = list.stream()
    .reduce(new ArrayList<>(),
        (acc, x) -> { acc.add(x * x); return acc; },  // Accumulator
        (acc1, acc2) -> { acc1.addAll(acc2); return acc1; }); // Combiner
// But this is inefficient - use collect() instead!

// When to use what:
// Reduction (single value): reduce()
// Collection (group/transform): collect()
```

---

## Q10: Stream performance - what's the cost?

**Answer:**
Streams have overhead compared to loops. Use streams for:
1. Readability
2. Parallel processing potential
3. Complex transformations

```java
// Loop - FASTEST for simple sequential
List<Integer> result = new ArrayList<>();
for (Integer n : list) {
    if (n > 5) {
        result.add(n * 2);
    }
}

// Stream - CLEANER but ~10% slower
List<Integer> result = list.stream()
    .filter(n -> n > 5)
    .map(n -> n * 2)
    .collect(toList());

// Performance comparison (simple operations, 1M elements):
// Loop: ~2ms
// Stream: ~2.5ms
// Parallel: ~5ms (overhead not worth it)

// Performance with expensive operations (1M elements):
// Loop: ~200ms
// Stream: ~205ms (overhead negligible)
// Parallel: ~60ms (worth it!)

// Key: Overhead is negligible compared to actual work
// Use streams for flexibility & readability
// Optimize only if profiler shows it's a bottleneck
```

---

# CONCURRENCY

## Q11: What's a race condition? How to prevent it?

**Answer:**
Race condition: Multiple threads access shared data and at least one modifies it, and result depends on execution order.

```java
// Race condition example:
class Counter {
    private int count = 0;  // Shared
    
    public void increment() {
        count++;  // Three operations: read, add 1, write
    }
}

// Problem scenario:
Thread 1: read (0) → add (1) → [Interleaved!]
Thread 2: read (0) → add (1) →
Thread 1: write (1)
Thread 2: write (1)  ← Both wrote 1, should be 2!

// Prevention methods:

// 1. Synchronized method (mutual exclusion):
public synchronized void increment() {
    count++;  // Only one thread at a time
}

// 2. Synchronized block (only critical section):
public void increment() {
    synchronized(this) {
        count++;
    }
}

// 3. AtomicInteger (compare-and-swap, no locks):
AtomicInteger count = new AtomicInteger(0);
count.incrementAndGet();  // Thread-safe

// 4. Immutability (no race condition possible):
class ImmutableCounter {
    private final int count;
    
    public ImmutableCounter(int count) {
        this.count = count;
    }
    
    public ImmutableCounter increment() {
        return new ImmutableCounter(count + 1);  // New object
    }
}

// 5. Thread-local (no sharing):
ThreadLocal<Integer> count = ThreadLocal.withInitial(() -> 0);
count.set(count.get() + 1);  // Each thread has own copy
```

---

## Q12: synchronized vs volatile - what's the difference?

**Answer:**
- **synchronized**: Mutual exclusion + visibility (one thread at a time)
- **volatile**: Visibility only (all threads see updates, but not exclusive)

```java
// Race condition - needs synchronized:
class Counter {
    private int count = 0;
    
    public synchronized void increment() {
        count++;  // Mutual exclusion needed
    }
}

// Visibility issue - needs volatile:
class Flag {
    private volatile boolean running = true;
    
    public void stop() {
        running = false;  // All threads see this immediately
    }
    
    public void work() {
        while (running) {  // Sees updated value
            doWork();
        }
    }
}

// What synchronized does:
synchronized(object) {
    // Acquire lock (exclusive access)
    // Read latest values from main memory
    // ... do work ...
    // Write values to main memory
    // Release lock
}

// What volatile does:
private volatile int value;
// Every read is from main memory (not cache)
// Every write goes to main memory (not cached)

// Key differences:
// ┌────────────────┬──────────────────┬───────────────────┐
// │ Feature        │ synchronized     │ volatile          │
// ├────────────────┼──────────────────┼───────────────────┤
// │ Exclusive      │ YES (one at time)│ NO (not exclusive)│
// │ Visibility     │ YES              │ YES               │
// │ Performance    │ Slower           │ Faster            │
// │ Atomicity      │ YES              │ NO                │
// └────────────────┴──────────────────┴───────────────────┘

// Use synchronized when: Multiple threads modify + race condition possible
// Use volatile when: Only one writer, multiple readers OR flags
```

---

## Q13: Deadlock - causes and prevention?

**Answer:**
Deadlock: Threads wait on each other, none can proceed. Causes:
1. Thread 1 holds Lock A, waits for Lock B
2. Thread 2 holds Lock B, waits for Lock A

```java
// Deadlock example:
class Deadlock {
    static Object lock1 = new Object();
    static Object lock2 = new Object();
    
    static void method1() {
        synchronized(lock1) {
            System.out.println("T1: Has lock1, waiting for lock2");
            Thread.sleep(100);
            synchronized(lock2) {  // DEADLOCK - waiting forever
                System.out.println("T1: Got both");
            }
        }
    }
    
    static void method2() {
        synchronized(lock2) {  // Gets lock2 first!
            System.out.println("T2: Has lock2, waiting for lock1");
            Thread.sleep(100);
            synchronized(lock1) {  // WAITING - T1 has lock1
                System.out.println("T2: Got both");
            }
        }
    }
}

// Prevention strategies:

// 1. Lock ordering (always acquire in same order):
static void method1() {
    synchronized(lock1) {           // ALWAYS acquire lock1 first
        synchronized(lock2) {       // THEN lock2
            doWork();
        }
    }
}

static void method2() {
    synchronized(lock1) {           // ALWAYS acquire lock1 first
        synchronized(lock2) {       // THEN lock2
            doWork();
        }
    }
}

// 2. Timeouts (break deadlock):
Lock lock = new ReentrantLock();
if (lock.tryLock(1, TimeUnit.SECONDS)) {
    try {
        doWork();
    } finally {
        lock.unlock();
    }
} else {
    System.out.println("Couldn't acquire lock, trying again");
}

// 3. Hold locks for minimum time:
// Get lock → Do minimal work → Release lock

// 4. Use ConcurrentHashMap instead of synchronized(map):
// ConcurrentHashMap uses segment locks (less contention)

// 5. Lock-free algorithms (no locks at all):
AtomicReference<Node> head = new AtomicReference<>();
while (true) {
    Node oldHead = head.get();
    Node newHead = new Node(oldHead);
    if (head.compareAndSet(oldHead, newHead)) {
        break;  // Success, no deadlock possible
    }
}
```

---

## Q14: Difference between wait() and sleep()?

**Answer:**
- **wait()**: Release lock, sleep until notified
- **sleep()**: Keep lock, just sleep

```java
// wait() - releases lock
synchronized(monitor) {
    while (!condition) {
        monitor.wait();  // Release lock, wait for notify
    }
    doWork();           // Proceeds when notified and lock re-acquired
}

// sleep() - keeps lock
synchronized(monitor) {
    Thread.sleep(1000);  // Sleep but still hold lock!
}                        // Lock released only after sleep + exiting

// Producer-Consumer example showing difference:
class ProducerConsumer {
    private Queue<Item> queue = new LinkedList<>();
    
    public synchronized void produce(Item item) throws InterruptedException {
        while (queue.size() > MAX) {
            wait();  // Release lock, sleep until space available
        }
        queue.add(item);
        notifyAll();  // Wake consumers
    }
    
    public synchronized Item consume() throws InterruptedException {
        while (queue.isEmpty()) {
            wait();  // Release lock, sleep until item available
        }
        Item item = queue.poll();
        notifyAll();  // Wake producers
        return item;
    }
}

// If we used sleep instead of wait:
// Problem: Would hold lock while sleeping
// Result: Producers can't produce, consumers can't consume
// Total failure!

// Key differences:
// ┌──────────────────┬──────────┬────────────────┐
// │ Feature          │ wait()   │ sleep()        │
// ├──────────────────┼──────────┼────────────────┤
// │ Release lock?    │ YES      │ NO             │
// │ Woken by notify? │ YES      │ NO (timeout)   │
// │ InterruptException?│ YES    │ YES            │
// │ Works outside synced?│ NO   │ YES            │
// └──────────────────┴──────────┴────────────────┘
```

---

## Q15: ExecutorService - why use it?

**Answer:**
ExecutorService manages a thread pool instead of creating threads manually.

```java
// Without ExecutorService (bad):
for (int i = 0; i < 1000; i++) {
    new Thread(() -> doWork()).start();  // 1000 threads!
    // System overloaded, OS can't handle
}

// With ExecutorService (good):
ExecutorService executor = Executors.newFixedThreadPool(10);  // Reuse 10 threads

for (int i = 0; i < 1000; i++) {
    executor.submit(() -> doWork());  // Tasks queued, thread reused
}

executor.shutdown();  // Prevent new tasks
executor.awaitTermination(1, TimeUnit.MINUTES);  // Wait for completion

// Benefits:
// 1. Thread reuse (not creating 1000 threads)
// 2. Bounded resources (exactly 10 threads max)
// 3. Queue management (tasks wait if no threads available)
// 4. Future support (get results, cancel, check status)

// Common pool types:

// Fixed thread pool:
ExecutorService fixed = Executors.newFixedThreadPool(4);
// Always 4 threads, queue unbounded

// Cached thread pool:
ExecutorService cached = Executors.newCachedThreadPool();
// Creates threads as needed, reuses idle threads, removes unused

// Single thread executor:
ExecutorService single = Executors.newSingleThreadExecutor();
// One thread, guaranteed sequential execution

// Scheduled executor:
ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(2);
scheduled.scheduleAtFixedRate(() -> doWork(), 0, 1, TimeUnit.SECONDS);
scheduled.schedule(() -> doWork(), 5, TimeUnit.SECONDS);

// Getting results with Callable + Future:
Future<Integer> future = executor.submit(() -> {
    return expensiveComputation();  // Returns result
});

// Later...
Integer result = future.get();  // Blocks until ready
if (future.isDone()) {
    System.out.println("Task complete: " + result);
}

// Batch submit:
List<Callable<Integer>> tasks = Arrays.asList(
    () -> compute1(),
    () -> compute2(),
    () -> compute3()
);

List<Future<Integer>> results = executor.invokeAll(tasks);
// Wait for ALL to complete

// Or get first result:
Integer firstResult = executor.invokeAny(tasks);
// Returns as soon as ONE completes
```

---

## Q16: ReentrantLock vs synchronized

**Answer:**

| Feature | synchronized | ReentrantLock |
|---------|-------------|---------------|
| Acquisition | Automatic | Manual lock() |
| Release | Automatic (exit block) | Manual unlock() |
| Timeout | No | Yes (tryLock) |
| Reentrant? | Yes | Yes |
| Fair scheduling | No | Configurable |
| Flexibility | Limited | High |

```java
// synchronized (simple, automatic):
public synchronized void method() {
    doWork();
}  // Automatically released

// ReentrantLock (manual, flexible):
private Lock lock = new ReentrantLock();

public void method() {
    lock.lock();
    try {
        doWork();
    } finally {
        lock.unlock();  // MUST unlock
    }
}

// With timeout:
boolean acquired = lock.tryLock(1, TimeUnit.SECONDS);
if (acquired) {
    try {
        doWork();
    } finally {
        lock.unlock();
    }
} else {
    System.out.println("Couldn't acquire lock");
}

// Fair lock:
Lock fairLock = new ReentrantLock(true);  // true = fair
// Threads acquire lock in order (no starvation)

// Unfair (default):
Lock unfairLock = new ReentrantLock(false);
// Faster but some threads might starve

// Condition variables (like wait/notify):
private Condition notFull = lock.newCondition();

public void produce() {
    lock.lock();
    try {
        while (queue.size() > MAX) {
            notFull.await();  // Like wait()
        }
        queue.add(item);
        notFull.signalAll();  // Like notifyAll()
    } finally {
        lock.unlock();
    }
}

// When to use:
// synchronized: Simple, few contentions, JVM can optimize
// ReentrantLock: Need timeout, fairness, or multiple conditions
```

---

## Q17: Atomic classes - when and why?

**Answer:**
AtomicXxx classes provide thread-safe primitives with Compare-And-Swap (CAS) - faster than locks for simple operations.

```java
// Without atomic (needs synchronized):
class Counter {
    private int count = 0;
    public synchronized void increment() {
        count++;
    }
    public synchronized int get() {
        return count;
    }
}

// With atomic:
class Counter {
    private AtomicInteger count = new AtomicInteger(0);
    public void increment() {
        count.incrementAndGet();  // No synchronized!
    }
    public int get() {
        return count.get();  // No synchronized!
    }
}

// Performance comparison:
// synchronized: Acquires lock → execute → release lock
// Atomic: Compare-and-swap in single CPU instruction

// Common atomic classes:
AtomicInteger ai = new AtomicInteger(0);
ai.incrementAndGet();         // ++x, returns new value
ai.getAndIncrement();         // x++, returns old value
ai.addAndGet(5);              // += 5, returns new value
ai.compareAndSet(1, 2);       // if (x==1) x=2, returns boolean

AtomicReference<String> ref = new AtomicReference<>("initial");
ref.set("new value");
String old = ref.getAndSet("another");  // Atomic swap

AtomicLong al = new AtomicLong(0);
al.longValue();               // Get value

// Use atomic when:
// 1. Simple operations (increment, get, set)
// 2. No composite operations
// 3. Performance critical

// Don't use atomic when:
// 1. Need mutual exclusion for multiple operations
// 2. Modifying complex state

class FailsWithAtomic {
    private AtomicInteger x = new AtomicInteger(0);
    private AtomicInteger y = new AtomicInteger(0);
    
    // NOT thread-safe despite using atomic!
    public void transferFunds() {
        x.decrementAndGet();     // Step 1
        y.incrementAndGet();     // Step 2 - What if thread interrupted?
    }
}

class FixedWithSynchronized {
    private int x = 0;
    private int y = 0;
    
    public synchronized void transferFunds() {
        x--;  // Both steps atomic
        y++;
    }
}
```

---

## Q18: ConcurrentHashMap vs Collections.synchronizedMap()

**Answer:**
ConcurrentHashMap is faster because it uses segment locks instead of full lock.

```java
// Collections.synchronizedMap - slow:
Map<String, Integer> syncMap = Collections.synchronizedMap(
    new HashMap<>()
);
// One lock for entire map!
// get() blocks even if you only read

// ConcurrentHashMap - fast:
ConcurrentHashMap<String, Integer> concMap = new ConcurrentHashMap<>();
// Multiple segment locks, 16 default
// Many threads can write to different segments simultaneously

// Performance comparison:
// 10 threads reading from synchronizedMap: Serialized, slow
// 10 threads reading from ConcurrentHashMap: Parallel, fast

// ConcurrentHashMap operations:
ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();

// Atomic operations:
map.putIfAbsent("key", 1);     // Insert if not present
map.replace("key", 1, 2);      // Replace if value matches
map.compute("key", (k, v) -> v + 1);  // Atomic compute

// Iteration (safe during concurrent modification):
for (String key : map.keySet()) {
    System.out.println(key);  // Won't throw ConcurrentModificationException
}

// Never use this pattern:
for (String key : map.keySet()) {
    map.remove(key);  // Might lose data
}

// Use instead:
Iterator<String> iter = map.keySet().iterator();
while (iter.hasNext()) {
    map.remove(iter.next());  // Safe
}

// When to use:
// ConcurrentHashMap: Multiple readers/writers
// synchronizedMap: Legacy code or rare contention
// HashMap: Single-threaded only
```

---

## Q19: ForkJoinPool vs ExecutorService

**Answer:**
ForkJoinPool optimized for divide-and-conquer problems, works with RecursiveTask.

```java
// ExecutorService - parallel processing:
ExecutorService executor = Executors.newFixedThreadPool(4);

List<Integer> results = new ArrayList<>();
for (Integer num : largeList) {
    Future<Integer> future = executor.submit(() -> process(num));
    results.add(future.get());  // Wait for each
}

// ForkJoinPool - divide-and-conquer:
ForkJoinPool pool = ForkJoinPool.commonPool();  // Work-stealing pool

class SortTask extends RecursiveTask<int[]> {
    private int[] array;
    private int start, end;
    private static final int THRESHOLD = 1000;
    
    protected int[] compute() {
        if (end - start <= THRESHOLD) {
            Arrays.sort(array, start, end);  // Base case
            return array;
        }
        
        int mid = (start + end) / 2;
        SortTask left = new SortTask(array, start, mid);
        SortTask right = new SortTask(array, mid, end);
        
        left.fork();              // Execute in thread pool
        int[] rightResult = right.compute();
        int[] leftResult = left.join();  // Wait for result
        
        merge(leftResult, rightResult);
        return array;
    }
}

int[] result = pool.invoke(new SortTask(array, 0, array.length));

// Key differences:
// ExecutorService: Submit independent tasks
// ForkJoinPool: Divide problem recursively, work-stealing

// ForkJoinPool benefits:
// 1. Work-stealing queues (idle threads steal from busy threads)
// 2. Optimized for recursive problems
// 3. Lower overhead than ExecutorService for many small tasks

// When to use:
// ExecutorService: Parallel tasks (not recursive)
// ForkJoinPool: Divide-and-conquer (mergesort, quicksort, tree processing)
```

---

## Q20: ThreadLocal - usage and cleanup

**Answer:**
ThreadLocal provides thread-local storage. Key: Must cleanup or get memory leak.

```java
// ThreadLocal usage:
ThreadLocal<SimpleDateFormat> dateFormat = 
    ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));

// Each thread gets its own instance:
Thread t1 = new Thread(() -> {
    String formatted = dateFormat.get().format(new Date());  // Thread 1's instance
    System.out.println(formatted);
});

Thread t2 = new Thread(() -> {
    String formatted = dateFormat.get().format(new Date());  // Thread 2's instance
    System.out.println(formatted);
});

// Why? SimpleDateFormat is not thread-safe
// If shared, threads would interfere
// ThreadLocal ensures each thread has own copy

// Real-world example - User context in web app:
public class UserContext {
    private static ThreadLocal<User> currentUser = new ThreadLocal<>();
    
    public static void setUser(User user) {
        currentUser.set(user);
    }
    
    public static User getUser() {
        return currentUser.get();
    }
    
    public static void cleanup() {
        currentUser.remove();  // CRITICAL!
    }
}

// In servlet:
protected void doGet(HttpServletRequest req, ...) {
    User user = getCurrentUser(req);
    UserContext.setUser(user);
    
    try {
        handleRequest(req);
    } finally {
        UserContext.cleanup();  // Always cleanup!
    }
}

// Memory leak if not cleaned:
// ┌──────────────────┐
// │ ThreadLocal      │
// │  ↓               │
// │ Thread           │
// │  ├─ Local 1      │ ← Still referenced!
// │  ├─ Local 2      │ ← Memory leak!
// │  └─ User info    │ ← Never freed!
// └──────────────────┘

// ThreadLocalMap in thread never clears
// If 1000s of users processed, memory grows unbounded

// Solution:
finally {
    UserContext.cleanup();  // explicit.remove()
}

// Best practice:
try (ThreadLocalSession session = new ThreadLocalSession()) {
    session.doWork();
} finally {
    // Auto cleanup
}
```

---

# GARBAGE COLLECTION

## Q21: How does garbage collection work?

**Answer:**
GC marks reachable objects, then sweeps unreachable (garbage). Java uses generational GC: Young collection (frequent), Old collection (rare).

```
Mark Phase (Stop-the-World):
1. Pause all application threads
2. Start from GC roots (local vars, static vars, active threads)
3. Follow all references, mark reachable objects
4. Everything unmarked is garbage

Sweep Phase:
1. Iterate heap
2. Free memory of unmarked objects
3. Update pointers

Heap structure:
Young Generation (30% of heap)
├── Eden
├── Survivor 0
└── Survivor 1

Old Generation (70% of heap)

Process:
1. Objects created in Eden
2. After Minor GC (frequent, fast), survivors moved to S0 or S1
3. Objects that survive N Minor GCs promoted to Old Gen
4. Old Gen collected only with Major GC (rare, slow)
```

---

## Q22: Difference between Minor GC and Major GC?

**Answer:**

| Aspect | Minor GC | Major GC |
|--------|----------|----------|
| Scope | Young generation only | Full heap |
| Frequency | Every few seconds | Every minute(s) |
| Pause Time | 5-50ms | 100ms-2 seconds |
| Cause | Eden full | Old gen full |
| Impact | Acceptable | Very noticeable |

```java
// Minor GC happens frequently:
List<String> list = new ArrayList<>();
for (int i = 0; i < 10_000_000; i++) {
    list.add("String " + i);  // Created in Eden
    if (i % 1_000_000 == 0) {
        list.clear();  // Objects become garbage
    }
}
// Minor GC runs every few thousand iterations
// Pause invisible to user (~10ms)

// Major GC happens rarely:
List<String> persistent = new ArrayList<>();
for (int i = 0; i < 100_000_000; i++) {
    persistent.add("String " + i);  // Promoted to Old gen after surviving Minor GC
}
// Old gen fills up, Major GC runs
// Pause very noticeable (~200ms+)

// Monitor with JVM flags:
// -Xlog:gc*:file=gc.log:time,level,tags
// Analysis: Look for Major GC frequency
// If > 1x per minute, heap too small or memory leak
```

---

## Q23: OutOfMemoryError (OOM) - what causes it?

**Answer:**
OOM happens when:
1. Heap too small for application
2. Memory leak (objects not freed)
3. Wrong GC settings

```java
// Cause 1: Heap too small
java -Xmx512m MyApp
// If app needs 1GB, OOM

// Cause 2: Memory leak
class EventListener {
    static List<Event> events = new ArrayList<>();  // ← LEAK!
    
    void onEvent(Event e) {
        events.add(e);  // Never removed
    }
}

// Events grow forever:
// Iteration 1: 1 event in static list
// Iteration 2: 2 events
// Iteration 3: 3 events
// ...
// Eventually: OutOfMemoryError

// Cause 3: Large object creation
// byte[] huge = new byte[Integer.MAX_VALUE];  // OOM immediately

// How to debug:
// 1. Enable heap dump:
java -XX:+HeapDumpOnOutOfMemoryError MyApp

// 2. Analyze dump with Eclipse MAT
// 3. Find largest objects holding references

// Prevention:
// 1. Size heap appropriately:
java -Xms2g -Xmx4g MyApp

// 2. Regular monitoring:
MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
MemoryUsage heap = memBean.getHeapMemoryUsage();
System.out.println("Used: " + heap.getUsed());
System.out.println("Max: " + heap.getMax());

// 3. Profile for memory leaks:
// Use JProfiler or YourKit
```

---

## Q24: Which GC should I use?

**Answer:**

| GC | Best For | Pause | Throughput |
|----|----------|-------|-----------|
| G1GC | General, large heaps | Predictable | High |
| ParallelGC | Throughput, batch | High | Very High |
| CMS | Low latency | Low | Medium |
| ZGC/Shenandoah | Ultra-low latency | < 10ms | Lower |

```java
// Default - G1GC (Recommended for most):
java -XX:+UseG1GC -XX:MaxGCPauseMillis=200 MyApp

// High throughput (batch processing):
java -XX:+UseParallelGC -XX:ParallelGCThreads=8 MyApp

// Low latency (web server):
java -XX:+UseConcMarkSweepGC -XX:CMSInitiatingOccupancyFraction=70 MyApp
// Note: CMS deprecated in Java 14+

// Ultra-low latency (trading, real-time):
java -XX:+UseZGC MyApp

// Configuration for web server:
java \
  -server \
  -Xms4g \
  -Xmx4g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+PrintGCDetails \
  -Xloggc:gc.log \
  MyApp

// Analysis:
// If pauses < 100ms acceptable → G1
// If pauses > 1s unacceptable → ZGC
// If throughput critical → Parallel
```

---

## Q25: How to prevent memory leaks?

**Answer:**
Common leak patterns and fixes:

```java
// Leak 1: Static collections
class BadListener {
    static List<Event> events = new ArrayList<>();  // ← LEAK
    
    void onEvent(Event e) {
        events.add(e);  // Never removed
    }
}

// Fix: Use WeakReference or cleanup
class GoodListener {
    static List<WeakReference<Event>> events = new ArrayList<>();
    
    void onEvent(Event e) {
        events.add(new WeakReference<>(e));
    }
}

// Leak 2: ThreadLocal not cleaned
class BadThreadLocal {
    static ThreadLocal<byte[]> buffer = new ThreadLocal<>();
    
    void work() {
        buffer.set(new byte[1024 * 1024]);  // 1MB
        // Thread dies but reference not cleaned!
    }
}

// Fix: Always remove
class GoodThreadLocal {
    static ThreadLocal<byte[]> buffer = new ThreadLocal<>();
    
    void work() {
        try {
            buffer.set(new byte[1024 * 1024]);
            doWork();
        } finally {
            buffer.remove();  // Always cleanup
        }
    }
}

// Leak 3: Open resources
class BadResource {
    void read() throws IOException {
        InputStream in = new FileInputStream("file.txt");
        byte[] data = new byte[1024];
        in.read(data);
        // Stream never closed!
    }
}

// Fix: Try-with-resources
class GoodResource {
    void read() throws IOException {
        try (InputStream in = new FileInputStream("file.txt")) {
            byte[] data = new byte[1024];
            in.read(data);
        }  // Automatically closed
    }
}

// Leak 4: Listeners not removed
class BadEvent {
    private List<EventListener> listeners = new ArrayList<>();
    
    public void subscribe(EventListener listener) {
        listeners.add(listener);  // Never unsubscribed!
    }
}

// Fix: Always remove listeners
listener.subscribe(e -> handleEvent(e));
// Later...
listener.unsubscribe(e -> handleEvent(e));  // Or track and cleanup

// Memory leak detection:
// 1. Heap dumps
// 2. Profiler (JProfiler, YourKit)
// 3. Monitor heap size over time
// 4. Look for growing metrics that should be bounded
```

---

## Q26-30: Additional Important Topics

[Additional questions cover: Performance tuning flags, heap sizing, GC logs analysis, monitoring tools, and real-world scenarios]

---

**End of Interview Q&A**

These 25+ questions cover the essential topics an interviewer will ask. Focus on understanding the "why" not just the "what".

Next: Run the code examples in [05_CODE_EXAMPLES/](../05_CODE_EXAMPLES/) to solidify your understanding.

# Java Concurrency - Complete Guide

## Part 1: Threading Fundamentals

### What Is a Thread?

A thread is a **lightweight process** that shares memory with other threads in the same JVM process.

```
Process (JVM)
├── Thread 1 (Heap, Stack)
├── Thread 2 (Heap, Stack)  ← Share same HEAP, separate STACKS
└── Thread 3 (Heap, Stack)
```

**Key**: All threads share the **same heap** (objects). Each thread has its **own stack** (local variables).

---

## Creating Threads - Two Ways

### Option 1: Extend Thread Class (Not recommended)

```java
class MyThread extends Thread {
    public void run() {
        System.out.println("Thread running: " + Thread.currentThread().getName());
    }
}

// Start it:
Thread t = new MyThread();
t.start();  // ← IMPORTANT: Call start(), NOT run()

// If you call run() directly:
t.run();    // ← WRONG! Runs in current thread, not new thread
```

### Option 2: Implement Runnable (Recommended)

```java
class MyRunnable implements Runnable {
    public void run() {
        System.out.println("Running in: " + Thread.currentThread().getName());
    }
}

Thread t = new Thread(new MyRunnable());
t.start();

// Or with lambda (Java 8+):
Thread t = new Thread(() -> {
    System.out.println("Running");
});
t.start();
```

**Why Runnable?** Java doesn't support multiple inheritance. Implementing Runnable gives flexibility.

---

## Thread Lifecycle

```
new Thread()
    ↓
    NEW (Registered but not started)
    ↓
t.start()
    ↓
    RUNNABLE (Ready to run, waiting for CPU)
    ↓
    RUNNING (Executing run() method)
    ↓
    WAITING/BLOCKED/TIMED_WAITING (Waiting on lock, I/O, etc.)
    ↓
    RUNNABLE (Back again)
    ↓
    TERMINATED (run() finished)
```

### Thread States - Detailed

```java
Thread t = new Thread(() -> {
    try {
        System.out.println(Thread.currentThread().getState());  // RUNNABLE
        Thread.sleep(2000);  // Sleep doesn't release locks
        System.out.println(Thread.currentThread().getState());  // TIMED_WAITING during sleep
    } catch (InterruptedException e) {}
});

// NEW (hasn't started yet)
System.out.println(t.getState());  // NEW

t.start();
// RUNNABLE (executing)

// Later...
// If waiting on synchronized block: BLOCKED
// If waiting on wait(): WAITING
// If sleeping or in timed operations: TIMED_WAITING
// After run() completes: TERMINATED
```

---

## Part 2: Synchronization (The Problem)

### Race Condition Example

```java
public class Counter {
    private int count = 0;
    
    public void increment() {
        count++;  // NOT ATOMIC! Three operations:
        // 1. Read count
        // 2. Add 1
        // 3. Write back
    }
    
    public int getCount() {
        return count;
    }
}

// Two threads calling increment():
Thread t1 = new Thread(() -> counter.increment());
Thread t2 = new Thread(() -> counter.increment());

t1.start();
t2.start();
t1.join();  // Wait for both threads
t2.join();

System.out.println(counter.getCount());  // Expected: 2, Actual: might be 1!
```

**Why?** Both threads might read the same value before either writes back:

```
Time | Thread 1          | Thread 2          | count
-----|-------------------|-------------------|-------
0    | Read count (0)    |                   | 0
1    |                   | Read count (0)    | 0
2    | Write count++ (1) |                   | 1
3    |                   | Write count++ (1) | 1  ← Both wrote back 1!
```

### Solution: synchronized Keyword

```java
public class Counter {
    private int count = 0;
    
    public synchronized void increment() {  // ← Lock
        count++;
    }
    
    public synchronized int getCount() {
        return count;
    }
}

// Now:
// Only ONE thread can execute increment() or getCount() at a time
// Other threads WAIT for the lock to be released
```

**How it works**:
```
Thread 1: Acquire lock → Enter increment() → Release lock
          ↓
          Lock is FREE
          ↓
Thread 2: Acquire lock → Enter increment() → Release lock
```

---

## Intrinsic Locks (Monitor)

Every object in Java has an **intrinsic lock** (also called monitor):

```java
synchronized(object) {  // Acquire lock on 'object'
    // Only one thread can be here at a time
}  // Release lock
```

When you use `synchronized` on a method:

```java
public synchronized void myMethod() {  // Locks 'this'
    // Equivalent to:
    synchronized(this) {
        // ...
    }
}

public static synchronized void staticMethod() {  // Locks the Class object
    // Equivalent to:
    synchronized(Counter.class) {
        // ...
    }
}
```

---

## Deadlock - The Nightmare

```java
class DeadlockExample {
    static Object lock1 = new Object();
    static Object lock2 = new Object();
    
    static void method1() {
        synchronized(lock1) {
            System.out.println("Method 1: Has lock1, waiting for lock2");
            Thread.sleep(100);
            synchronized(lock2) {  // DEADLOCK HERE
                System.out.println("Method 1: Got both locks");
            }
        }
    }
    
    static void method2() {
        synchronized(lock2) {  // Gets lock2 first!
            System.out.println("Method 2: Has lock2, waiting for lock1");
            Thread.sleep(100);
            synchronized(lock1) {  // WAITING for lock1!
                System.out.println("Method 2: Got both locks");
            }
        }
    }
}

// Thread 1 calls method1(), Thread 2 calls method2()
// Thread 1: Has lock1, waiting for lock2
// Thread 2: Has lock2, waiting for lock1 ← DEADLOCK!
```

### Prevent Deadlock: Lock Ordering

```java
static void method1() {
    synchronized(lock1) {  // Always acquire lock1 first
        synchronized(lock2) {
            // Do work
        }
    }
}

static void method2() {
    synchronized(lock1) {  // Always acquire lock1 first (same order!)
        synchronized(lock2) {
            // Do work
        }
    }
}
// No deadlock! Same lock ordering prevents circular wait.
```

---

## Part 3: Volatile Keyword

```java
class Flag {
    private boolean flag = false;  // ← Not volatile
    
    void set() {
        flag = true;
    }
    
    boolean get() {
        return flag;
    }
}

// Two threads:
Thread writer = new Thread(() -> {
    flag.set();  // Changes flag
});

Thread reader = new Thread(() -> {
    while (!flag.get()) {  // Might not see the change!
        // Spins forever
    }
});
```

**Problem**: CPU caches and compiler optimizations mean Thread 2 might not see Thread 1's change.

**Solution**: Use volatile

```java
private volatile boolean flag = false;  // ← Volatile
```

**What volatile does**:
- Forces writes to main memory (not just cache)
- Forces reads from main memory (not from cache)
- Prevents compiler reordering

---

## Part 4: Advanced Locks (java.util.concurrent.locks)

### ReentrantLock

```java
Lock lock = new ReentrantLock();

lock.lock();  // Acquire lock
try {
    // Protected code
} finally {
    lock.unlock();  // Always unlock
}

// OR with try-with-resources (if Closeable):
// ReentrantLock is not Closeable, so manual unlock required

// With timeout:
boolean acquired = lock.tryLock(1, TimeUnit.SECONDS);
if (acquired) {
    try {
        // Do work
    } finally {
        lock.unlock();
    }
} else {
    System.out.println("Couldn't acquire lock");
}
```

**synchronized vs ReentrantLock**:

| Aspect | synchronized | ReentrantLock |
|--------|-------------|---------------|
| Syntax | Simple keyword | Manual lock/unlock |
| Timeout | No | Yes (tryLock) |
| Fairness | Not fair | Can be fair |
| Reentrancy | Yes | Yes |
| Flexibility | Limited | High |

### ReadWriteLock

For data that's read often, written rarely:

```java
ReadWriteLock rwLock = new ReentrantReadWriteLock();

// Many readers can hold lock simultaneously
rwLock.readLock().lock();
try {
    int value = sharedData;  // Multiple threads can read
} finally {
    rwLock.readLock().unlock();
}

// But only ONE writer (exclusive access)
rwLock.writeLock().lock();
try {
    sharedData = newValue;  // Only one thread can write
} finally {
    rwLock.writeLock().unlock();
}
```

**Benefit**: 100 readers can access simultaneously. Only 1 writer blocks everyone.

---

## Part 5: Atomic Variables

For thread-safe primitives without locks:

```java
AtomicInteger count = new AtomicInteger(0);

// Thread-safe operations (no synchronized needed)
count.incrementAndGet();  // ++count, returns new value
count.getAndIncrement();  // count++, returns old value
count.compareAndSet(1, 5);  // CAS: set to 5 if currently 1
count.addAndGet(3);  // += 3, returns new value

// These use Compare-And-Swap (CAS) - much faster than locks
```

**When to use**: Simple counters, flags, references (not complex logic).

---

## Part 6: wait() and notify()

For communication between threads:

```java
class ProducerConsumer {
    private Queue<Integer> queue = new LinkedList<>();
    private static final int CAPACITY = 5;
    
    public synchronized void produce(int value) throws InterruptedException {
        while (queue.size() == CAPACITY) {
            wait();  // Release lock, sleep until notify
        }
        queue.add(value);
        notifyAll();  // Wake up waiting threads
    }
    
    public synchronized Integer consume() throws InterruptedException {
        while (queue.isEmpty()) {
            wait();  // Release lock, sleep until notify
        }
        Integer value = queue.poll();
        notifyAll();  // Wake up waiting producer
        return value;
    }
}

// A producer thread:
Thread producer = new Thread(() -> {
    for (int i = 0; i < 10; i++) {
        producer.produce(i);
    }
});

// A consumer thread:
Thread consumer = new Thread(() -> {
    for (int i = 0; i < 10; i++) {
        producer.consume();
    }
});
```

**Key Points**:
- wait() only works inside synchronized
- notify() wakes **ONE** waiting thread
- notifyAll() wakes **ALL** waiting threads
- Always use while loop with wait(), not if (might spurious wake)

---

## Interview Questions

**Q1: What's the difference between Thread and Runnable?**
A: Thread is a class, Runnable is an interface. Implement Runnable for flexibility; extend Thread only if necessary.

**Q2: Difference between synchronized and volatile?**
A: 
- synchronized: Mutual exclusion (only one thread at a time)
- volatile: Visibility (changes visible across threads, no mutual exclusion)

Use volatile for flags, synchronized for shared mutable objects.

**Q3: Deadlock causes and how to prevent?**
A: Deadlock causes:
- Circular lock dependencies
- Hold locks while acquiring others

Prevention:
- Lock ordering (always acquire in same order)
- Timeouts
- Lock-free algorithms

**Q4: Why wait() in a while loop?**
A: Spurious wakeups - a thread can wake without notify(). Also, if multiple threads are waiting, notifyAll() wakes all but only one should proceed (others should wait again).

**Q5: When to use AtomicInteger instead of synchronized?**
A: Atomic is faster for simple operations (no lock overhead). Synchronized is needed for complex multi-step operations.

---

## Common Patterns

### Thread-Safe Lazy Initialization (Double-Checked Locking)

```java
class Singleton {
    private volatile static Singleton instance = null;
    
    public static Singleton getInstance() {
        if (instance == null) {  // First check (no lock)
            synchronized(Singleton.class) {
                if (instance == null) {  // Second check (with lock)
                    instance = new Singleton();
                }
            }
        }
        return instance;
    }
}
```

### ExecutorService (Thread Pools)

Instead of creating threads manually:

```java
ExecutorService executor = Executors.newFixedThreadPool(4);  // 4 threads

for (int i = 0; i < 10; i++) {
    executor.submit(() -> {
        System.out.println("Task running");
    });
}

executor.shutdown();  // Prevent new tasks
executor.awaitTermination(1, TimeUnit.MINUTES);  // Wait for all to finish
```

---

**Next**: Study [04_INTERVIEW_ANSWERS/COMPREHENSIVE_QA.md](../04_INTERVIEW_ANSWERS/COMPREHENSIVE_QA.md) for more concurrency questions, or run [05_CODE_EXAMPLES/ConcurrencyPatterns.java](../05_CODE_EXAMPLES/ConcurrencyPatterns.java) for runnable examples.

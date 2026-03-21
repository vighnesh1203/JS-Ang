# Java Concurrency - Synchronization & Locks Deep Dive

## Part 1: Understanding synchronized

### The Lock Problem

```java
class UnsafeCounter {
    private int count = 0;
    
    public void increment() {
        count++;  // This is NOT atomic!
    }
}

// What happens:
// count++ is ACTUALLY three operations:
// 1. Read current value of count (0)
// 2. Add 1 to it (1)
// 3. Write back to memory (count = 1)

// Two threads simultaneously:
Thread 1:   Read (0)  →  Add (1)  →  Write (1)
Thread 2:   Read (0)  →  Add (1)  →  Write (1)
           ↑ INTERLEAVED!
// Both wrote 1, but should be 2!
```

### synchronized Keyword

```java
// Synchronized METHOD - locks 'this' object
public class Counter {
    private int count = 0;
    
    public synchronized void increment() {
        count++;
    }
}

// Under the hood:
// Equivalent to:
public void increment() {
    synchronized(this) {  // Lock on this object
        count++;          // Only I can execute this
    }                     // Unlock
}

// Synchronized BLOCK - locks specific object
public void transfer(Account from, Account to, int amount) {
    synchronized(from) {
        from.withdraw(amount);
    }
    synchronized(to) {
        to.deposit(amount);
    }
}

// Static synchronized - locks the Class object
public static synchronized void staticMethod() {
    // Only one thread across ALL instances
    // Lock is on Counter.class
}

// Multiple locks are different things:
public synchronized void method1() {  // Lock on this
    doWork1();
}

public static synchronized void method2() {  // Lock on Counter.class
    doWork2();
}
// method1() and method2() can run simultaneously!
// Different locks, different monitor objects
```

### Intrinsic Locks & Happens-Before

```java
// synchronized provides VISIBILITY (not just exclusion)

public class VisibilityExample {
    private int value = 0;
    private boolean ready = false;
    
    public synchronized void set(int val) {
        value = val;    // Write to main memory
        ready = true;   // Write to main memory
    }                   // Lock released - all writes visible
    
    public synchronized int get() {
        if (!ready) {   // Read from main memory
            return -1;
        }
        return value;   // Read from main memory
    }                   // Lock acquired - sees latest values
}

// Without synchronized: Compiler/CPU might cache values in CPU cache
// Thread 1 writes value, but Thread 2 doesn't see it
// With synchronized: Guarantees main memory visibility
```

---

## Part 2: Advanced Locks (java.util.concurrent.locks)

### ReentrantLock

```java
// Not reentrant (fails):
final Object lock = new Object();
synchronized(lock) {
    synchronized(lock) {  // Same thread, same lock
        // Actually this WORKS because synchronized is reentrant!
    }
}

// ReentrantLock - named because it IS reentrant
Lock lock = new ReentrantLock();

lock.lock();
try {
    lock.lock();  // Same thread can lock again
    try {
        doWork();
    } finally {
        lock.unlock();  // Release (enters count 1)
    }
} finally {
    lock.unlock();  // Release (enters count 0)
}

// Fair lock - threads acquire in order (no priority inversion)
Lock fairLock = new ReentrantLock(true);  // Fair ordering

// Comparison:
// Synchronized: Not fair (threads can starve)
// ReentrantLock(true): Fair (FIFO, no starvation)
// Performance trade-off: Fair is slower
```

### tryLock() - Non-blocking attempt

```java
Lock lock = new ReentrantLock();

// Try to acquire, don't wait
if (lock.tryLock()) {
    try {
        doWork();
    } finally {
        lock.unlock();
    }
} else {
    System.out.println("Couldn't acquire lock");
    doAlternativeWork();
}

// With timeout
try {
    if (lock.tryLock(5, TimeUnit.SECONDS)) {
        try {
            doWork();
        } finally {
            lock.unlock();
        }
    } else {
        System.out.println("Lock acquisition timeout");
    }
} catch (InterruptedException e) {
    System.out.println("Interrupted waiting for lock");
}

// Practical use - prevent deadlock:
Lock lock1 = new ReentrantLock();
Lock lock2 = new ReentrantLock();

// Thread 1 and 2 might deadlock with normal lock()
// Solution: Use tryLock with timeout
while (true) {
    if (lock1.tryLock(100, TimeUnit.MILLISECONDS)) {
        try {
            if (lock2.tryLock(100, TimeUnit.MILLISECONDS)) {
                try {
                    doWorkWithBothLocks();
                    break;  // Success!
                } finally {
                    lock2.unlock();
                }
            }
        } finally {
            lock1.unlock();
        }
    }
    // If can't get both locks within timeout, retry
}
```

### ReadWriteLock

```java
// When you have many readers, few writers
// Readers can access simultaneously
// But writers need exclusive access

class Cache {
    private Map<String, String> data = new HashMap<>();
    private ReadWriteLock rwLock = new ReentrantReadWriteLock();
    
    public String read(String key) {
        // Multiple threads can read simultaneously
        rwLock.readLock().lock();
        try {
            return data.get(key);
        } finally {
            rwLock.readLock().unlock();
        }
    }
    
    public void write(String key, String value) {
        // Only one thread can write (exclusive)
        rwLock.writeLock().lock();
        try {
            data.put(key, value);
        } finally {
            rwLock.writeLock().unlock();
        }
    }
}

// Example scenario:
// 100 threads reading: All can hold readLock simultaneously
// 1 thread writing: Must have writeLock (blocks all readers)
// Result: Much faster than synchronized(map) for read-heavy workloads

// Performance comparison:
// synchronized(map): 1 reader at a time
// ReadWriteLock: 100 readers at a time!
```

### Condition Variables

```java
// Like wait/notify but more powerful

Lock lock = new ReentrantLock();
Condition notEmpty = lock.newCondition();
Condition notFull = lock.newCondition();

class BoundedQueue<T> {
    private Queue<T> queue = new LinkedList<>();
    private int maxSize;
    private Lock lock = new ReentrantLock();
    private Condition notEmpty = lock.newCondition();
    private Condition notFull = lock.newCondition();
    
    BoundedQueue(int size) {
        this.maxSize = size;
    }
    
    public void put(T item) throws InterruptedException {
        lock.lock();
        try {
            // Wait until there's space
            while (queue.size() == maxSize) {
                notFull.await();  // Wait for space to become available
            }
            queue.add(item);
            notEmpty.signal();  // Notify one consumer
        } finally {
            lock.unlock();
        }
    }
    
    public T take() throws InterruptedException {
        lock.lock();
        try {
            // Wait until there's an item
            while (queue.isEmpty()) {
                notEmpty.await();  // Wait for item to be available
            }
            T item = queue.poll();
            notFull.signal();  // Notify one producer
            return item;
        } finally {
            lock.unlock();
        }
    }
}

// Advantages over wait/notify:
// 1. Multiple conditions (notEmpty, notFull separate)
// 2. Signal individual threads or all
// 3. Cleaner code structure
```

---

## Part 3: Atomic Variables

### How CAS (Compare-And-Swap) Works

```java
// Traditional lock-based:
public synchronized void increment() {  // Aquire lock
    count++;                             // Do work
}                                        // Release lock
// Other threads blocked while we hold lock!

// Atomic (Compare-And-Swap):
AtomicInteger count = new AtomicInteger(0);
count.incrementAndGet();

// Internally:
// while (true) {
//     int current = value;
//     int next = current + 1;
//     if (compareAndSwap(value, current, next)) {
//         return next;  // Success!
//     }
//     // Retry if another thread changed it
// }
// No locks! Busy-wait if contended.
```

### Atomic Class Variants

```java
// Primitives
AtomicInteger ai = new AtomicInteger(0);
ai.incrementAndGet();
ai.getAndIncrement();
ai.addAndGet(5);
ai.decrementAndGet();
ai.compareAndSet(1, 2);  // Set to 2 only if current is 1

// References
AtomicReference<String> ref = new AtomicReference<>("initial");
ref.set("new");
String old = ref.getAndSet("another");
ref.compareAndSet("another", "final");

// Arrays
AtomicIntegerArray array = new AtomicIntegerArray(10);
array.set(0, 100);           // Thread-safe array access
array.incrementAndGet(0);
array.compareAndSet(0, 101, 200);

// Long and Double too
AtomicLong al = new AtomicLong(0);
AtomicReference<Double> ad = new AtomicReference<>(0.0);

// Mark-able references
AtomicMarkableReference<String> amr = 
    new AtomicMarkableReference<>("initial", false);
amr.set("new", true);  // Value and mark

// Stamp-able references (advanced)
AtomicStampedReference<String> asr = 
    new AtomicStampedReference<>("initial", 0);
asr.set("new", 1);  // Prevent ABA problem
```

### When to Use Atomic

```java
// ✓ GOOD - Simple operations
counter.incrementAndGet();        // One operation
value.set(newValue);              // Simple assignment
ref.compareAndSet(old, new);      // Simple CAS

// ✗ WRONG - Complex operations
// Need to increment and check value:
if (counter.incrementAndGet() > 100) {  // Atomic increment
    // ... but what if another thread changes it here?
}
// Use synchronized or Lock for multi-step atomicity

// ✓ GOOD - For counters, flags, simple objects
class ThreadSafeCounter {
    private AtomicInteger count = new AtomicInteger(0);
    public void increment() { count.incrementAndGet(); }
    public int get() { return count.get(); }
}

// ✗ WRONG - For complex logic
class BankAccount {
    private AtomicDouble balance = new AtomicDouble(0);
    
    public void transfer(BankAccount to, double amount) {
        balance.set(balance.get() - amount);  // Not atomic!
        to.balance.set(to.balance.get() + amount);  // Race condition!
        
        // What if another thread accesses between these two lines?
    }
}

class BankAccount {
    private double balance = 0;
    
    public synchronized void transfer(BankAccount to, double amount) {
        this.balance -= amount;         // Both operations
        to.balance += amount;           // Atomic as one transaction
    }
}
```

---

## Part 4: Common Patterns

### Double-Checked Locking (Lazy Initialization)

```java
class Singleton {
    private volatile static Singleton instance = null;
    
    public static Singleton getInstance() {
        // First check - no lock (fast path)
        if (instance == null) {
            synchronized(Singleton.class) {
                // Second check - inside lock (prevents race)
                if (instance == null) {
                    instance = new Singleton();
                }
            }
        }
        return instance;
    }
}

// Why this pattern:
// 1. First check (volatile) - fast path for existing instance
// 2. synchronized - protects creation
// 3. Second check - in case multiple threads bypassed first check
// 4. volatile - ensures all threads see the new instance

// Note: Java 8 has enums that do this better:
enum Singleton {
    INSTANCE;
    public void doSomething() { ... }
}
Singleton.INSTANCE.doSomething();  // Thread-safe, simple
```

### Producer-Consumer with Conditions

```java
class BoundedQueue<T> {
    private final Queue<T> queue;
    private final int capacity;
    private final Lock lock = new ReentrantLock();
    private final Condition notFull = lock.newCondition();
    private final Condition notEmpty = lock.newCondition();
    
    BoundedQueue(int capacity) {
        this.queue = new LinkedList<>();
        this.capacity = capacity;
    }
    
    public void put(T t) throws InterruptedException {
        lock.lock();
        try {
            while (queue.size() == capacity) {
                notFull.await();  // Wait for space
            }
            queue.add(t);
            notEmpty.signal();  // Wake up consumer
        } finally {
            lock.unlock();
        }
    }
    
    public T take() throws InterruptedException {
        lock.lock();
        try {
            while (queue.isEmpty()) {
                notEmpty.await();  // Wait for item
            }
            T t = queue.remove();
            notFull.signal();  // Wake up producer
            return t;
        } finally {
            lock.unlock();
        }
    }
}
```

### Try-with-resources for Lock-like objects

```java
// Elegant pattern using try-with-resources
class LockableResource implements AutoCloseable {
    private Lock lock = new ReentrantLock();
    
    public LockableResource acquire() {
        lock.lock();
        return this;
    }
    
    @Override
    public void close() {
        lock.unlock();
    }
}

// Usage:
LockableResource resource = ...;
try (resource.acquire()) {
    // Lock held automatically
    doWork();
} finally {
    // Lock released automatically!
}
```

---

## Interview Questions on Synchronization

**Q: synchronized vs ReentrantLock?**
A: synchronized is simpler, automatic lock release. ReentrantLock offers tryLock(), fairness, and multiple conditions.

**Q: Why volatile for double-checked locking?**
A: Without volatile, another thread might see partially-constructed instance. volatile ensures visibility.

**Q: When would you choose ReadWriteLock?**
A: Read-heavy scenarios (many readers, few writers). Allows multiple readers simultaneously.

**Q: What's Compare-And-Swap?**
A: Hardware instruction that atomically compares value and swaps if equal. Basis of Atomic classes.

**Q: Why atomic operations perform well?**
A: No lock overhead or context switching. Uses hardware CAS, busy-wait if contended.

---

## Performance Tips

1. **Minimize critical section size** - Hold locks for minimal time
2. **Use appropriate lock** - ReadWriteLock for read-heavy, simple lock for balanced
3. **Lock ordering** - Always acquire locks in same order (prevents deadlock)
4. **Atomic for counters** - Trades busy-wait for no context switching
5. **Monitor contention** - If threads wait often, redesign code

---

**Next**: Study real-world patterns in [05_CODE_EXAMPLES/InterviewProblems.java](../05_CODE_EXAMPLES/InterviewProblems.java) or review [COMPREHENSIVE_QA.md](../04_INTERVIEW_ANSWERS/COMPREHENSIVE_QA.md) for Q&A.

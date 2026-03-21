# Java Garbage Collection - Deep Dive

## Part 1: How Garbage Collection Works

### The Memory Model

```
JVM Memory
├── HEAP (Objects live here)
│   ├── Young Generation (Eden, S0, S1)
│   └── Old Generation (Tenured)
├── STACK (Method calls, local vars)
├── Method Area (Classes, constants)
└── Program Counter
```

**HEAP**: Where all objects are stored. GC manages this.
**STACK**: Where method calls and primitive values live. Auto-freed when method returns.

---

## GC Roots - The Starting Point

GC traces which objects are still needed by following references from **GC Roots**:

```java
Object root = new Object();           // GC Root (local variable)
Object child = new Object();
root.reference = child;               // child is reachable

// When root goes out of scope
// root is no longer a GC Root
// child is no longer reachable
// Both can be garbage collected
```

**GC Roots include**:
- Local variables (in active stack frames)
- Static variables
- Objects in method parameters
- Thread objects

---

## Mark and Sweep Algorithm

GC runs in two phases:

### Phase 1: MARK (Stop-the-World)
```
1. Start from GC roots
2. Follow all references
3. Mark all reachable objects
4. Unmarked objects are garbage

Timeline:
0ms:   GC Pause STARTS ⏸️
       Mark phase begins
10ms:  Marking complete
       All reachable objects marked
20ms:  Sweep phase begins ← 20ms PAUSE!
```

### Phase 2: SWEEP
```
1. Iterate through heap
2. Free memory of unmarked objects
3. Update heap pointers
```

---

## Generational Hypothesis

**Observation**: Most objects die young, few survive to old age.

```
Statistics:
- 90% of objects die in Young Generation
- Only 10% survive to Old Generation
- Very few survive to permanent old age
```

**Exploit this**:
```
Young Generation: Collect frequently (quick, small pause)
Old Generation: Collect less frequently
```

### Heap Generations

```
YOUNG GENERATION (30% of heap)
├── Eden (80% of young)
│   └── New objects allocated here
├── Survivor 0 (10% of young)
│   └── Objects that survived 1+ minor GC
└── Survivor 1 (10% of young)
    └── Latest survivor space

OLD GENERATION (70% of heap)
└── Objects that survived enough minor GCs (age threshold)

PERMANENT GENERATION (fixed size)
└── Class definitions, method code
```

---

## Minor GC vs Major GC

### Minor GC (Frequent, Quick)
```
1. Collect only Young Generation
2. Copy live objects from Eden + S0 → S1
3. Increment age counter
4. Free Eden + S0

Pause Time: 5-50ms (very fast)
Frequency: Every few seconds
```

**Example**:
```java
List<String> young = new ArrayList<>();
for (int i = 0; i < 1_000_000; i++) {
    young.add("String " + i);  // Created in Eden
}  // End of loop - most are now garbage
   // Next Minor GC will collect all of them

// Minor GC runs:
// - Mark live objects (very few in Young)
// - Sweep and compact
// - Pause: 10ms (acceptable)
```

### Major GC / Full GC (Infrequent, Slow)
```
1. Collect entire heap (Young + Old)
2. Mark and Sweep all generations
3. Compact memory

Pause Time: 100ms-2 seconds (SLOW!)
Frequency: Every minute(s) or specific condition
```

---

## Object Aging Process

```
Creation:
new MyObject()  →  Allocated in Eden

First Minor GC:
MyObject survives  →  Moved to S0, Age = 1

Second Minor GC:
MyObject survives  →  Moved to S1, Age = 2

Third Minor GC:
MyObject survives  →  Moved to S0, Age = 3

...
After Age Threshold (default 15):
MyObject survives  →  Promoted to Old Generation, Age = 32

Old Generation Full:
Major GC runs  →  Sweep Old Generation
```

---

## Common GC Collections (Collectors)

### 1. Serial GC (Single-threaded)
```
java -XX:+UseSerialGC MyApplication
```
- **Best for**: Single-core systems, CLI utilities
- **Pause**: Acceptable for small heaps only
- **Performance**: Not for production servers

```
Timeline for Minor GC:
User code running: ██████████
GC pause:          ░░░  (Single core collecting)
User code running: ██████████
```

### 2. Parallel GC (Multi-threaded)
```
java -XX:+UseParallelGC MyApplication
```
- **Best for**: Multi-core servers, batch processing
- **Threads**: Uses all available cores
- **Pause**: Shorter due to multiple threads

```
Timeline:
User code: ██████████
GC pause:  ░░ (Multiple cores collecting fast)
User code: ██████████
```

### 3. CMS - Concurrent Mark Sweep
```
java -XX:+UseConcMarkSweepGC MyApplication
```
- **Best for**: Low-latency applications, server side
- **Concurrent**: Most work happens while app runs (not Stop-the-World)
- **Pause**: Very short (20-50ms typically)

```
Timeline:
User code:        ██████████
Initial Mark:     ░  (very short pause)
Concurrent Mark:  ███ (app runs while GC works)
Remark:           ░  (short pause)
Concurrent Sweep: ███
User code:        ██████████
```

**Con**: Doesn't compact, leads to fragmentation over time.

### 4. G1GC - Garbage First (Modern, Recommended)
```
java -XX:+UseG1GC -XX:MaxGCPauseMillis=200 MyApplication
```
- **Best for**: Large heaps (4GB+), predictable pause times
- **Strategy**: Divide heap into regions, collect most garbage-filled regions first
- **Pause**: Predictable (target: 200ms default)

```
Heap divided into regions:
╔═══╦═══╦═══╦═══╗
║ E ║ O ║ E ║ H ║  E = Eden, O = Old, H = Humongous
║ O ║ E ║ O ║ E ║
╚═══╩═══╩═══╩═══╝

GC collects regions with most garbage first
Pause time stays predictable
```

### 5. ZGC and Shenandoah (Ultra Low-Latency)
```
java -XX:+UseZGC MyApplication
```
- **Best for**: Ultra-low latency (<10ms), financial systems
- **Pause**: < 10 seconds pause time (always!)
- **Throughput**: Slightly lower than G1
- **Java Version**: 11+ (ZGC), 12+ recommended

---

## Full GC Dump Analysis

```
[2024-03-21 10:30:45] 
GC (G1 Evacuation Pause) 2048M->512M(4096M), 0.156 secs

Breakdown:
- GC type: G1 Evacuation Pause
- Before GC: Heap was 2048M used
- After GC: Heap is 512M used
- Total Heap: 4096M
- Pause Duration: 156ms
```

**Analysis**:
- Large drop (2048→512) = Major GC happened
- 156ms pause acceptable for G1
- If this happens frequently, need heap tuning

---

## Memory Leak Examples

### Example 1: Static Collection Holding References
```java
class EventListener {
    static List<Event> events = new ArrayList<>();  // ← LEAK!
    
    void processEvent(Event e) {
        events.add(e);  // Never removed!
    }
}

// Memory grows unbounded
// Events never garbage collected
// Should use WeakReference or cleanup logic
```

### Example 2: Circular References (Actually OK in Java)
```java
class Node {
    Node parent;
    List<Node> children;
}

Node root = new Node();
Node child = new Node();
root.children.add(child);
child.parent = root;  // Circular ref

root = null;  // GC can still collect!
// Both will be collected because no GC root points to them
// (Java GC handles cycles)
```

### Example 3: ThreadLocal Not Cleaned
```java
class WorkerThread extends Thread {
    static ThreadLocal<byte[]> buffer = new ThreadLocal<>();
    
    void work() {
        buffer.set(new byte[1024 * 1024]);  // 1MB allocated
        
        // Now thread dies but ThreadLocal not cleanup
        // Memory stays until thread dies
    }
}

// Fix:
try {
    buffer.set(...);
} finally {
    buffer.remove();  // Always clean
}
```

---

## GC Tuning - Key Parameters

### Heap Size
```
-Xms1024m         # Initial heap (avoid resizing)
-Xmx4096m         # Maximum heap
-Xmn512m          # Young generation size

# Rule: Set -Xms = -Xmx (avoid pauses from resizing)
java -Xms4096m -Xmx4096m MyApp
```

### Pause Time Target
```
-XX:MaxGCPauseMillis=200  # Target 200ms pause (G1)

# Lower = more frequent collections
# Higher = longer pauses but better throughput
```

### GC Logging
```
-Xlog:gc*:file=gc.log:time,level,tags

# Or old syntax:
-Xloggc:gc.log
-XX:+PrintGCDetails
-XX:+PrintGCDateStamps

# Analyze with:
# - GCViewer (visual)
# - GCeasy (online analysis)
```

### Example: Server JVM Tuning
```bash
java \
  -server \
  -Xms8g \
  -Xmx8g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+PrintGCDetails \
  -XX:+PrintGCDateStamps \
  -Xloggc:gc.log \
  -cp ... \
  MyApplication
```

---

## Interview Questions

**Q1: How does Garbage Collection work?**
A: GC marks all reachable objects starting from GC roots, then sweeps unmarked (garbage) objects. Java uses generational GC: Minor GC for Young (fast), Major GC for Old (slow).

**Q2: Difference between Minor GC and Major GC?**
A: 
- Minor GC: Young generation only, frequent (5-50ms pause)
- Major GC: Full heap, rare (100ms+ pause)

**Q3: What causes OutOfMemoryError?**
A: 
- Heap too small
- Memory leak (objects not garbage collected)
- Creating objects faster than GC can clean

**Q4: Which GC should I use?**
A: 
- Default (G1): Good for most applications
- ParallelGC: If Throughput > Low latency
- ZGC/Shenandoah: If require <10ms pauses
- CMS: Deprecated (Java 9+), avoid

**Q5: How to prevent memory leaks?**
A: 
- Use try-finally for resources
- Remove explicit references
- Use WeakReference for caches
- Monitor heap usage
- Profile memory regularly

---

## Real-World Scenario

**Problem**: Application pauses for 2 seconds every minute
```
Timeline:
█████ (user requests handle fine)
░░░░░░░░░░░░░░░░░░ (2 second pause - Full GC!)
█████ (user requests resume)

Cause: Heap too small, forcing frequent Major GCs
```

**Solution**:
```bash
# Before:
java -Xmx1g MyApp

# After:
java -Xms4g -Xmx4g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 MyApp

# Result:
█████ (smooth)
░░░░░░░░░░░░░░░░░░ (short 150ms GC pause - user barely notices)
█████ (smooth)
```

---

**Next**: Study [04_INTERVIEW_ANSWERS/COMPREHENSIVE_QA.md](../04_INTERVIEW_ANSWERS/COMPREHENSIVE_QA.md) for GC interview questions, or run [05_CODE_EXAMPLES/MemoryManagement.java](../05_CODE_EXAMPLES/MemoryManagement.java) for hands-on GC examples.

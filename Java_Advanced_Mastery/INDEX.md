# Complete Index - Java Advanced Mastery

## Module 1: Streams (01_STREAMS/)

### Core Files
1. **01_STREAM_FUNDAMENTALS.md**
   - What are Streams?
   - Lazy vs Eager evaluation
   - Source → Intermediate → Terminal operations
   - Interview: "Difference between Stream and Collection?"

2. **02_INTERMEDIATE_OPERATIONS.md**
   - filter(), map(), flatMap() - How they work
   - sorted(), distinct(), limit(), skip()
   - Interview questions with solutions

3. **03_STREAM_INTERNALS.md**
   - How Streams execute internally
   - Pipeline construction
   - Lazy evaluation mechanics
   - Stateful vs Stateless operations
   - Why `forEach` vs `collect` matters

4. **04_PERFORMANCE_TUNING.md**
   - Parallel Streams performance analysis
   - When to use parallel (and when NOT to)
   - Common pitfalls
   - Benchmarking techniques

---

## Module 2: Concurrency (02_CONCURRENCY/)

### Core Files
1. **01_THREADING_BASICS.md**
   - Thread lifecycle
   - Creating threads (Thread class vs Runnable)
   - Thread states
   - Interview: "What is a thread?"

2. **02_SYNCHRONIZATION.md**
   - synchronized keyword
   - Intrinsic locks
   - Race conditions
   - Deadlocks and prevention
   - Interview: "Explain synchronized"

3. **03_LOCKS_AND_ATOMICS.md**
   - ReentrantLock vs synchronized
   - Condition variables
   - Atomic variables
   - Read/Write locks
   - Stamped Locks

4. **04_CONCURRENT_COLLECTIONS.md**
   - ConcurrentHashMap internals
   - CopyOnWriteArrayList
   - BlockingQueue implementations
   - Interview: "Why not use Collections.synchronizedMap?"

5. **05_EXECUTORS_AND_THREADPOOLS.md**
   - ExecutorService internals
   - Thread pool sizing
   - ForkJoinPool for parallel work
   - CallableFuture pattern

---

## Module 3: Garbage Collection (03_GARBAGE_COLLECTION/)

### Core Files
1. **01_GC_FUNDAMENTALS.md**
   - Heap structure (Young, Old, Permanent generations)
   - GC roots and mark-sweep-compact
   - Minor GC vs Major GC
   - Stop-the-world pauses
   - Interview: "How does GC work?"

2. **02_GC_ALGORITHMS.md**
   - G1GC (Garbage First)
   - Serial GC
   - Parallel GC
   - CMS (Concurrent Mark Sweep)
   - ZGC, Shenandoah
   - When to use what

3. **03_GC_TUNING.md**
   - Heap sizing (-Xmx, -Xms)
   - GC selection and flags
   - Monitoring and profiling
   - Interview: "How do you tune GC?"

4. **04_MEMORY_LEAKS.md**
   - Common memory leak patterns
   - Static references
   - Collections holding unused objects
   - Circular references
   - Detection and prevention

---

## Module 4: Interview Answers (04_INTERVIEW_ANSWERS/)

### Core Files
1. **COMPREHENSIVE_QA.md**
   - 50+ Job Interview Questions
   - Real answers from experienced engineers
   - Categorized by topic and difficulty
   - Time complexity analysis included

### Question Categories
- Streams (15+ questions)
- Concurrency (20+ questions)
- Garbage Collection (10+ questions)
- Memory Management (5+ questions)

---

## Module 5: Code Examples (05_CODE_EXAMPLES/)

### Example Files
1. **StreamBasics.java** - Runnable stream operations
2. **StreamOptimizations.java** - Performance patterns
3. **ThreadingExamples.java** - Thread creation and management
4. **ConcurrencyPatterns.java** - Real production patterns
5. **InterviewProblems.java** - Coding interview solutions
6. **MemoryManagement.java** - GC and memory examples

---

## Quick Jump By Topic

### "How do Streams work?"
→ 01_STREAMS/03_STREAM_INTERNALS.md + 05_CODE_EXAMPLES/StreamOptimizations.java

### "What about Concurrency?"
→ 02_CONCURRENCY/01_THREADING_BASICS.md → 02_CONCURRENCY/03_LOCKS_AND_ATOMICS.md

### "Debug Memory Issues"
→ 03_GARBAGE_COLLECTION/04_MEMORY_LEAKS.md

### "Interview Next Week"
→ 04_INTERVIEW_ANSWERS/COMPREHENSIVE_QA.md → Study 05_CODE_EXAMPLES/InterviewProblems.java

---

## File Statistics

| Module | Files | Topics |
|--------|-------|--------|
| Streams | 4 | 20+ |
| Concurrency | 5 | 25+ |
| GC | 4 | 15+ |
| Interviews | 1 | 50+ |
| Examples | 6 | 30+ |
| **Total** | **20+** | **140+** |

---

**Last Updated**: March 2026
**Difficulty Level**: Intermediate to Advanced
**Study Time**: 2-3 weeks

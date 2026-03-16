# JavaScript & Angular Interview Mastery - Senior Engineer Edition
**Designed for 2-Day Deep Preparation | Last Updated: March 16, 2026**

🎯 **ANSWER KEY REFERENCE**: For detailed expected answers to all 120 JavaScript interview questions in this guide, see [INTERVIEW_ANSWERS_COMPREHENSIVE_KEY.md](INTERVIEW_ANSWERS_COMPREHENSIVE_KEY.md)

---

## Table of Contents
- **JavaScript Deep Dive** (13 topics)
- **Angular Mastery** (14 topics)
- **Real-world Applications**
- **Architecture Diagrams**
- **System Design Considerations**

---

# PART 1: JAVASCRIPT FUNDAMENTALS REDESIGNED FOR SENIORS

## Topic 1: Execution Context

### 1. Core Concept
Every function call in JavaScript creates an **execution context** - a container that holds all the information needed to execute that function. Think of it as a "virtual environment" where variables, functions, and the `this` binding are resolved.

**Three types of execution contexts:**
- **Global Execution Context**: Created when the script starts (once per program)
- **Function Execution Context**: Created each time a function is called
- **Eval Execution Context**: Created when `eval()` is executed (rarely used)

**Mental Model**: Execution context is like a **stack of rooms**. Each room has its own context of what variables exist, who can access them, and what `this` refers to.

### 2. How It Works Internally

```
Creation Phase:
1. Memory Allocation → Variables and functions are "hoisted"
   - Variables: declared as `undefined`
   - Functions: fully defined and stored
2. Variable Object (VO) → Properties added for all local variables
3. Scope Chain → Links to outer scopes created
4. this Binding → Determined based on how function is called

Execution Phase:
1. Execute code line by line
2. Assign values to variables
3. Call functions (creates new contexts)
4. Return from functions (pops context from stack)
```

**Visualizing the Call Stack:**
```
Global Context
├── var x = 1
├── function greet() → Creates new context when called
│   ├── var msg = "hello"
│   ├── function sayIt() → Creates another new context
│   │   └── console.log(msg)
```

### 3. Real-world Use in Large Systems

**Netflix (Rendering System)**:
- Each component render creates an execution context
- Context must be isolated to prevent memory leaks
- Contexts are garbage collected after render completes
- Large recursive renders can cause "stack overflow" → must use iterative rendering

**Google (V8 Engine)**:
- V8 optimizes execution contexts by creating "hidden classes"
- Contexts with similar property structures share optimization information
- JIT compilation uses context information to specialize code

**Enterprise Applications**:
- Node.js servers create new execution context per request
- Express middleware creates contexts for each layer
- Context leakage = memory leak (critical in long-running servers)

### 4. Code Examples

```javascript
// Example 1: Hoisting and Execution Context
console.log(x); // undefined (hoisted)
var x = 5;
console.log(x); // 5

// Example 2: Function Context
function outer() {
  var outerVar = "I'm outer";
  
  function inner() {
    console.log(outerVar); // Can access outer's context
  }
  
  return inner;
}

const fn = outer(); // outer context created, executed, then removed
fn(); // inner's context has access to outer's variables (closure)

// Example 3: Context Stack and Memory
function recursiveFunction(n) {
  if (n === 0) return; // Base case
  console.log(n);
  recursiveFunction(n - 1); // Stack grows
  console.log(n);
// Stack shrinks here (unwinding)
}

recursiveFunction(3);
// Prints: 3, 2, 1, 1, 2, 3 (as stack unwinds)

// Example 4: This Binding in Context
const person = {
  name: "Alice",
  greet() {
    console.log(this.name); // 'this' is determined at call time
  }
};

person.greet(); // "Alice" - this = person
const fn = person.greet;
fn(); // undefined - this = global object (in non-strict)

// Example 5: Context Isolation (Critical for Production)
class RequestHandler {
  constructor() {
    this.data = [];
  }
  
  async processRequest(id) {
    // Each request gets isolated context
    const requestContext = {
      id,
      startTime: Date.now(),
      data: null
    };
    
    // Simulate async work
    await new Promise(r => setTimeout(r, 100));
    
    // Context is garbage collected after function ends
    return requestContext;
  }
}
```

### 5. Interview Questions

1. **Why does `var x = 5; console.log(x);` not throw an error, even though console.log comes before the declaration?**
   - *Hint: Think about the creation phase of execution context*

2. **In JavaScript, if you have a deeply nested function call, what happens to memory? Why would this crash a server?**
   - *Focus: Stack overflow, context memory accumulation*

3. **How would you design a system where execution contexts are reused instead of creating new ones each time?**
   - *Think: Object pool pattern, context caching*

4. **Explain the difference between execution context and scope. Are they the same?**
   - *Key point: Scope is lexical (defined at write time), context is dynamic (determined at runtime)*

5. **In Node.js, why do you need to understand execution context for debugging memory leaks in production?**
   - *Answer: Contexts not being garbage collected cause accumulation*

6. **What is the "reference to outer context" and why does it create a closure?**

7. **How does JavaScript engine use execution context information to optimize code?**
   - *Answer: Hidden classes, function specialization*

8. **If a function is called 1000 times with the same arguments, does it create 1000 different execution contexts?**
   - *Yes, each call creates a new one, but V8 optimizes them similarly*

9. **In async code with callbacks, how many execution contexts exist at once?**
   - *Only one active, others are suspended in the event loop*

10. **Design a memory-efficient system that creates 100,000 objects in a loop without running out of context memory.**
    - *Hint: Focus on when contexts get garbage collected*

### 6. Common Mistakes

**Mistake 1: Not understanding hoisting**
```javascript
console.log(typeof x); // "undefined", not "ReferenceError"
var x = 5; // Mistake: assuming x wasn't declared yet
```

**Mistake 2: This binding confusion**
```javascript
const obj = {
  value: 42,
  getValue: function() {
    setTimeout(function() {
      console.log(this.value); // undefined! 'this' is global, not obj
    }, 100);
  }
};
obj.getValue(); // Wrong context

// Fix: Arrow function preserves this
const obj2 = {
  value: 42,
  getValue: function() {
    setTimeout(() => {
      console.log(this.value); // 42 - correct!
    }, 100);
  }
};
```

**Mistake 3: Context leakage in callbacks**
```javascript
// WRONG - causes memory leak in servers
function handleRequest(data) {
  const largeData = new Array(1e7).fill(data); // large context
  
  db.query(sql, (err, result) => {
    console.log(result);
    // largeData still in memory! Not garbage collected until callback is done
  });
}
```

**Mistake 4: Recursive calls without understanding stack**
```javascript
// Stack overflow risk
function traverseTree(node) {
  // Each recursive call creates new context
  // Deep trees = stack overflow
  node.children.forEach(child => traverseTree(child));
}
```

### 7. Small Exercise

**Challenge**: Create a `createCounter` function that:
1. Maintains its own execution context
2. Returns an object with `increment()`, `decrement()`, and `getCount()` methods
3. Each counter has isolated state (not shared)
4. Implement a `createCounterFactory` that creates 1000 counters efficiently

```javascript
// Your implementation here
```

**Bonus**: Add a memory monitoring function that shows when contexts are garbage collected.

### 8. System Design Relevance

**Scalability Impact**:
- **Stack depth** → Deep recursion = server crash. Must use iterative approaches
- **Context creation rate** → Creating too many contexts = memory pressure
- **GC frequency** → Lots of contexts = frequent garbage collection = performance drops

**Performance Optimization**:
- V8 engine optimizes contexts with similar structure (hidden classes)
- Use consistent object shapes to benefit from optimization
- Avoid changing object properties after creation

**Maintainability**:
- Understanding contexts helps debug "Why is this variable undefined?"
- Prevents memory leaks in production servers
- Enables writing efficient async code

---

## Topic 2: Event Loop

### 1. Core Concept
The **event loop** is JavaScript's mechanism for handling asynchronous operations while maintaining single-threaded execution. It's the "traffic cop" that decides what code runs next.

**Mental Model**: Imagine a restaurant:
- **Call stack** = Chef actively cooking (one dish at a time)
- **Callback queue** = Orders waiting to be prepared
- **Event loop** = Manager checking if chef is free, then giving next order

### 2. How It Works Internally

The event loop operates in this order:
```
1. Execute synchronous code on Call Stack
2. Call Stack empty?
3. Check Microtask Queue (Promises, MutationObserver)
4. Microtask Queue empty?
5. Check Macrotask Queue (setTimeout, setInterval, I/O)
6. Render/Paint (if needed)
7. Back to step 1
```

**Queue Priority** (CRITICAL):
```
Macrotasks (lowest):  setTimeout, setInterval, I/O, UI events
      ↓ (much faster execution)
Microtasks (highest): Promises, async/await, queueMicrotask()
```

**One macrotask is processed, then ALL microtasks are processed before next macrotask:**

```javascript
console.log('1. Sync');
setTimeout(() => console.log('2. Macrotask'), 0);
Promise.resolve().then(() => console.log('3. Microtask'));
console.log('4. Sync');

// Output: 1. Sync, 4. Sync, 3. Microtask, 2. Macrotask
```

### 3. Real-world Use in Large Systems

**Netflix (Rendering Pipeline)**:
- Renders are scheduled as macrotasks
- Promise-based animations use microtasks
- Carefully orchestrates the queue to prevent jank (dropped frames)

**Google's Chrome (Scheduling)**:
- React uses `requestIdleCallback` to schedule low-priority work
- High-priority updates bypass event loop pools
- React 18's concurrent rendering exploits event loop knowledge

**Node.js (Production Servers)**:
- Event loop can become "blocked" (all handlers busy)
- Microtasks can starve macrotasks (DOS vulnerability)
- Must monitor event loop lag for production health

**Interactive Apps (Figma, Google Docs)**:
- Long-running computations yield to event loop
- Prioritize UI responsiveness over computation speed

### 4. Code Examples

```javascript
// Example 1: Event Loop Order
console.log('Start');

setTimeout(() => {
  console.log('setTimeout 1');
  Promise.resolve().then(() => console.log('Promise in setTimeout'));
}, 0);

Promise.resolve()
  .then(() => {
    console.log('Promise 1');
    setTimeout(() => console.log('setTimeout in Promise'), 0);
  })
  .then(() => console.log('Promise 2'));

console.log('End');

/* Output:
Start
End
Promise 1
Promise 2
setTimeout 1
Promise in setTimeout
setTimeout in Promise
*/

// Example 2: Starvation Issue (microtasks blocking macrotasks)
function blockTheLoop() {
  Promise.resolve()
    .then(() => blockTheLoop()); // Infinite microtasks
}

setTimeout(() => console.log('This never logs!'), 0);
blockTheLoop(); // Starvation - setTimeout never runs

// Example 3: Production-level event loop monitoring
class EventLoopMonitor {
  static measurements = [];
  
  static async monitorLag() {
    const start = performance.now();
    
    // This microtask tells us how long the event loop was blocked
    await Promise.resolve();
    
    const lag = performance.now() - start;
    this.measurements.push(lag);
    
    if (lag > 5) { // 5ms = bad for 60fps (16.67ms per frame)
      console.warn(`Event loop lag: ${lag}ms`);
      // Alert monitoring system
    }
    
    // Schedule next check
    setTimeout(() => this.monitorLag(), 1000);
  }
}

// Example 4: Yielding to event loop (CPU-intensive work)
async function processHugeData(items) {
  for (let i = 0; i < items.length; i += 1000) {
    // Process batch
    const batch = items.slice(i, i + 1000);
    processSync(batch);
    
    // Yield to event loop every 1000 items
    // Allows UI updates, other callbacks to run
    await new Promise(resolve => setTimeout(resolve, 0));
  }
}

// Example 5: RequestAnimationFrame (macrotask timing)
let lastFrameTime = 0;

function animate() {
  const now = performance.now();
  const deltaTime = now - lastFrameTime;
  lastFrameTime = now;
  
  // RAF is scheduled between macrotasks and rendering
  // Affects visual performance
  
  console.log(`FPS: ${(1000 / deltaTime).toFixed(1)}`);
  requestAnimationFrame(animate);
}

animate();

// Example 6: Promise.resolve() vs setTimeout(0) - understanding the queue
function demonstrateQueueDifference() {
  const start = performance.now();
  
  let microtaskTime = 0;
  let macrotaskTime = 0;
  
  Promise.resolve().then(() => {
    microtaskTime = performance.now() - start;
  });
  
  setTimeout(() => {
    macrotaskTime = performance.now() - start;
  }, 0);
  
  setTimeout(() => {
    console.log(`Microtask timing: ${microtaskTime}ms`);
    console.log(`Macrotask timing: ${macrotaskTime}ms`);
    // Microtask always faster
  }, 100);
}
```

### 5. Interview Questions

1. **Why does `setTimeout(..., 0)` not execute immediately, even though the delay is 0?**
   - *Focus: It's a macrotask, must wait for call stack to be empty and all microtasks*

2. **Design a system that processes 1 million items without blocking the UI. How would you use the event loop?**
   - *Answer: Batch processing with `await new Promise(resolve => setTimeout(resolve, 0))`*

3. **In Node.js, what happens if a Promise handler takes 100ms to execute?**
   - *Microtasks block the entire event loop, starving setTimeout and I/O operations*

4. **How would you detect if the event loop is "blocked" in a production application?**
   - *Measure lag between microtask execution times*

5. **Explain the order of execution in this code without running it:**
```javascript
for(let i = 0; i < 3; i++) {
  setTimeout(() => Promise.resolve().then(() => console.log(i)), 0);
}
```

6. **Why can `Promise` microtasks starve `setTimeout` macrotasks? How would you prevent this?**
   - *Starvation: Infinite microtasks prevent macrotasks from running*
   - *Prevention: Don't create recursive Promises*

7. **In a browser, what happens between processing macrotasks? Why is this important for visual performance?**
   - *Answer: Browser renders. If event loop is blocked, rendering is delayed = jank*

8. **Design a rate limiter that respects the event loop (doesn't block it).**

9. **Why is `requestAnimationFrame` better than `setTimeout` for animations?**
   - *RAF is synchronized with browser's render timing*

10. **In Node.js, `setImmediate` and `setTimeout` both schedule macrotasks, but in what order do they execute?**
    - *setImmediate runs in current phase, setTimeout runs in next cycle*

### 6. Common Mistakes

**Mistake 1: Assuming setTimeout(fn, 0) executes immediately**
```javascript
// WRONG
const result = getData(); // assumes getData() runs first
setTimeout(() => processData(result), 0);

// RIGHT
const result = getData();
await new Promise(resolve => setTimeout(resolve, 0));
processData(result);
```

**Mistake 2: Creating infinite microtasks**
```javascript
// WRONG - starves all setTimeout
function badLoop() {
  Promise.resolve().then(badLoop); // Infinite recursion in microtask queue
}
badLoop();

// RIGHT - yields to macrotask queue
async function goodLoop() {
  while(true) {
    await new Promise(resolve => setTimeout(resolve, 0)); // Yield
  }
}
```

**Mistake 3: Not understanding queue priority**
```javascript
// WRONG - expects this to log in order
setTimeout(() => console.log('1'), 0);
Promise.resolve().then(() => console.log('2'));
// Logs: 2, 1 (not 1, 2!)

// RIGHT - understand microtasks always win
```

**Mistake 4: UI blocking calculations**
```javascript
// WRONG - blocks the event loop
function filterMillion(items) {
  return items.filter(expensiveComputation);
}

// RIGHT - yields to event loop periodically
async function filterMillion(items) {
  let result = [];
  for (let i = 0; i < items.length; i++) {
    result.push(expensiveComputation(items[i]));
    if (i % 1000 === 0) await new Promise(r => setTimeout(r, 0));
  }
  return result;
}
```

### 7. Small Exercise

**Challenge**: Implement a `Task Queue` class that:
1. Takes a list of expensive operations
2. Processes them without blocking the UI (using event loop yields)
3. Maintains 60+ FPS during processing
4. Provides progress callbacks

```javascript
class SmartTaskQueue {
  // Your implementation
}

// Usage
const queue = new SmartTaskQueue(hugeArray);
queue.on('progress', (percent) => updateProgressBar(percent));
await queue.process();
```

**Bonus**: Add adaptive batching (automatically adjusts batch size based on event loop lag).

### 8. System Design Relevance

**Performance**:
- Event loop lag directly impacts user experience (jank, freezing)
- Microtasks run before rendering, so too many = dropped frames
- Must batch work and yield strategically

**Scalability**:
- Each user connection in Node.js shares the same event loop
- One slow operation blocks all users
- Must implement queue monitoring and alerting

**Responsiveness**:
- E-commerce sites: search results must be non-blocking
- Real-time apps: chat must respond immediately
- Maps: panning/zooming must not stutter

---

## Topic 3: Call Stack and Task Queue

### 1. Core Concept
The **call stack** tracks function calls in LIFO (Last In, First Out) order. The **task queue** (callback queue) holds callbacks waiting to be executed.

**Mental Model**: Call stack is a stack of pancakes—each function call adds one, execution removes one.

### 2. How It Works Internally

```
Call Stack               Task Queue
┌─────────────────┐     ┌──────────────┐
│ main()          │     │ callback1()  │
├─────────────────┤     ├──────────────┤
│ functionA()     │     │ callback2()  │
├─────────────────┤     ├──────────────┤
│ functionB()     │     │ callback3()  │
└─────────────────┘     └──────────────┘
   (EXECUTING)             (WAITING)

When stack is empty, event loop takes first item from queue
```

### 3. Real-world Use in Large Systems

**Browser DevTools**:
- Shows call stack in debugger
- Identifies performance bottlenecks
- Detects infinite recursion

**Error Tracking (Sentry, Bugsnag)**:
- Captures call stack on errors
- Sends to server for analysis
- Helps reproduce bugs from production

**Node.js Profiling**:
- Flame graphs show which functions consume time
- Based on call stack snapshots

### 4. Code Examples

```javascript
// Example 1: Basic Call Stack
function a() {
  b();
}

function b() {
  c();
}

function c() {
  console.log('Stack depth: three functions');
}

a(); // Stack: main → a → b → c

// Example 2: Stack visualization
function trace() {
  console.trace(); // Prints entire call stack
}

function foo() {
  trace();
}

function bar() {
  foo();
}

bar(); // Shows: bar → foo → trace

// Example 3: Stack overflow
function stackOverflow(n = 0) {
  return stackOverflow(n + 1); // RangeError: Maximum call stack exceeded
}

// Example 4: Detecting and preventing stack overflow
class RecursionSafeProcessor {
  process(data, fn, index = 0) {
    // Convert recursion to iteration
    const stack = [{ data, index }];
    
    while(stack.length > 0) {
      const { data, index } = stack.pop();
      fn(data, index);
      
      // Instead of recursing, push to iteration stack
      if(someCondition) {
        stack.push({ data: nextData, index: index + 1 });
      }
    }
  }
}

// Example 5: Call stack size in different engines
const MAX_STACK_SIZE = (function() {
  let i = 0;
  try {
    (function f() { i++; f(); })();
  } catch(e) {
    return i;
  }
})();

console.log(`Max recursion depth: ${MAX_STACK_SIZE}`);
// Chrome: ~15000, Firefox: ~26000, Safari: ~27000

// Example 6: Analyzing performance using call stack
class PerformanceAnalyzer {
  static analyzeStack() {
    const stack = new Error().stack;
    const calls = stack.split('\n').filter(line => line.includes('at '));
    
    // Show which functions are deepest (potential performance issue)
    return {
      depth: calls.length,
      deepestCall: calls[calls.length - 1],
      callPath: calls.reverse().join(' → ')
    };
  }
}
```

### 5. Interview Questions

1. **Why is stack depth limited? What's the maximum and why?**
2. **How would you convert a recursive algorithm to iterative to avoid stack overflow?**
3. **In a profiler, why does "call stack sampling" not capture all function calls?**
4. **Design a system that tracks the call stack efficiently in production.**
5. **What's the relationship between call stack depth and memory consumption?**
6. **How do JavaScript engines optimize tail call recursion?**
7. **Given a call stack of 100 functions, how much memory does it use?**
8. **In async code, can the call stack have multiple suspended stacks?**
9. **How would you implement a stack tracer for debugging async code?**
10. **Why does throwing an error show useful stack trace information?**

### 6. Common Mistakes

**Mistake 1: Unnecessary recursion**
```javascript
// WRONG - uses call stack unnecessarily
function sumArray(arr, index = 0) {
  if(index === arr.length) return 0;
  return arr[index] + sumArray(arr, index + 1); // Stack grows with array size
}

// RIGHT - iterative
function sumArray(arr) {
  let sum = 0;
  for(let i = 0; i < arr.length; i++) {
    sum += arr[i]; // Constant stack usage
  }
  return sum;
}
```

**Mistake 2: Not checking stack depth in production**
```javascript
// WRONG - can crash server
function processList(items) {
  if(items.length > 0) {
    process(items[0]);
    return processList(items.slice(1)); // O(n) stack usage
  }
}

// RIGHT - batch and yield
async function processList(items) {
  for(let i = 0; i < items.length; i++) {
    process(items[i]);
    if(i % 100 === 0) await new Promise(r => setTimeout(r, 0));
  }
}
```

### 7. Small Exercise

**Challenge**: Create a recursive tree traversal that:
1. Doesn't risk stack overflow
2. Tracking depth for analysis
3. Capable of pausing and resuming

### 8. System Design Relevance

- **Memory**: Each stack frame uses ~50-100 bytes (varies by engine)
- **Performance**: Recursion can be faster than iteration (if optimized by JIT)
- **Stability**: Must prevent stack overflow in production

---

## Topic 4: Closures

### 1. Core Concept
A **closure** is a function that has access to variables from its outer scope, even after the outer function has finished executing. It "closes over" variables from its lexical environment.

**Mental Model**: Closure is a snapshot of a function's environment + the function itself. Variables are "captured" and live as long as the function needs them.

### 2. How It Works Internally

```
// When inner() is defined:
// 1. Scope chain is created pointing to outer()
// 2. When outer() executes, inner() is returned
// 3. outer() finishes, but its execution context is NOT garbage collected
// 4. Because inner() still references variables from outer()
```

```javascript
function outer() {
  const x = 10; // Outer scope variable
  
  function inner() {
    console.log(x); // 'x' is captured in closure
  }
  
  return inner;
}

const fn = outer(); // outer() finished, but x is still accessible
fn(); // 10 - closure has x in memory
```

### 3. Real-world Use in Large Systems

**Event Handlers (React, Vue)**:
```javascript
// Each click handler closes over its own data
items.forEach(item => {
  button.addEventListener('click', () => {
    console.log(item.id); // Closure captures each item
  });
});
```

**Module Pattern (Before ES6)**:
```javascript
const counter = (function() {
  let count = 0; // Private variable (closure)
  
  return {
    increment: () => ++count,
    get: () => count
  };
})();
```

**Request Handlers (Express)**:
```javascript
app.get('/user/:id', (req, res) => {
  // Closure over req, res, and database connection
  db.findById(req.params.id)
    .then(user => res.json(user))
    .catch(err => res.status(500).json(err));
});
```

### 4. Code Examples

```javascript
// Example 1: Classic closure
function makeMultiplier(x) {
  return function(y) {
    return x * y; // Closes over x
  };
}

const double = makeMultiplier(2);
const triple = makeMultiplier(3);

console.log(double(5)); // 10
console.log(triple(5)); // 15
// Each closure has its own 'x' value

// Example 2: Memory implications of closures
class ClosureCache {
  constructor(expensiveData) {
    this.expensiveData = expensiveData; // 500MB
  }
  
  // Each listener closes over this entire object
  addListener(fn) {
    window.addEventListener('change', () => {
      fn(this.expensiveData);
    });
  }
}

// Problem: Listener persists event handler, which persists this, which persists data

// Example 3: Functional accumulator pattern (functional programming)
const accumulate = (reduceFn) => {
  let result = null;
  
  return (value) => {
    result = reduceFn(result, value);
    return result;
  };
};

const sum = accumulate((acc, val) => (acc || 0) + val);
console.log(sum(1)); // 1
console.log(sum(2)); // 3
console.log(sum(3)); // 6
// Each call closes over the same 'result' variable

// Example 4: Closure over loop variable (classic bug)
const functions = [];

// WRONG
for(var i = 0; i < 3; i++) {
  functions.push(() => console.log(i));
}

functions[0](); // 3 (not 0!) - all closures share the same 'i'
functions[1](); // 3
functions[2](); // 3

// RIGHT - ES6
for(let i = 0; i < 3; i++) {
  functions.push(() => console.log(i));
}

functions[0](); // 0 - each iteration has its own 'i' scope
functions[1](); // 1
functions[2](); // 2

// Example 5: Memoization using closures (performance optimization)
function memoize(fn) {
  const cache = {}; // Closure over cache
  
  return function memoized(...args) {
    const key = JSON.stringify(args);
    
    if(key in cache) {
      return cache[key]; // Use cached result
    }
    
    const result = fn(...args);
    cache[key] = result;
    return result;
  };
}

const fibonacci = memoize((n) => {
  if(n <= 1) return n;
  return fibonacci(n - 1) + fibonacci(n - 2);
});

fibonacci(100); // Completes instantly with cache

// Example 6: Partial application (functional programming)
const multiply = (a, b, c) => a * b * c;

const partialMultiply = (a) => {
  return (b, c) => multiply(a, b, c); // Closes over a
};

const multiplyBy2 = partialMultiply(2);
console.log(multiplyBy2(3, 4)); // 24

// Example 7: Detecting closure memory retention
class ClosureMemoryDetector {
  static detectUnintendedClosures() {
    // Each callback retains reference to entire scope
    const largeObject = new Array(1e7).fill(0); // 40MB+ object
    
    const callbacks = [];
    for(let i = 0; i < 1000; i++) {
      callbacks.push(() => {
        // Each callback closes over largeObject
        // Total memory: 1000 * 40MB = 40GB potential!
        return largeObject.length;
      });
    }
    
    return callbacks; // 1000 closures holding 40MB each
  }
}

// Example 8: Closure in async code
function createAsyncFetcher(url) {
  let cachedData = null;
  let fetchPromise = null;
  
  return async () => {
    if(cachedData) return cachedData; // Closure over cachedData
    
    if(!fetchPromise) {
      fetchPromise = fetch(url).then(r => r.json());
    }
    
    cachedData = await fetchPromise;
    return cachedData; // Closure persists data between calls
  };
}
```

### 5. Interview Questions

1. **Explain the difference between closure and scope. Can you have scope without closure?**
2. **Design a function that creates 1000 closures but only uses 1MB of memory (not 1GB).**
3. **Why does this code log 5 three times instead of 0, 1, 2?**
```javascript
for(var i = 0; i < 3; i++)
  setTimeout(() => console.log(i), 100);
```

4. **In Node.js, if an event listener closes over a large object, why is it a memory leak?**
5. **How would you implement partial application using closures? What's its use case?**
6. **Explain the memory implications of using `memoize` with unlimited cache size.**
7. **Can a closure ever access a variable it shouldn't access?**
8. **Design a module system using closures that prevents global namespace pollution.**
9. **In a React component, why do useCallback and useMemo use closures? What's the purpose?**
10. **How do you debug which variables are captured in a closure?**

### 6. Common Mistakes

**Mistake 1: Unintended closure memory retention**
```javascript
// WRONG - closes over large object
function setupHandler(largeObject) {
  element.addEventListener('click', () => {
    console.log(largeObject);
  });
  // largeObject persists in memory forever
}

// RIGHT - extract only needed data
function setupHandler(largeObject) {
  const id = largeObject.id;
  element.addEventListener('click', () => {
    console.log(id);
  });
  // Only id is in closure, largeObject is GCed
}
```

**Mistake 2: Loop variable closure bug**
```javascript
// WRONG
for(var i = 0; i < 3; i++) {
  setTimeout(() => console.log(i), 0); // All log 3
}

// RIGHT - let creates new scope per iteration
for(let i = 0; i < 3; i++) {
  setTimeout(() => console.log(i), 0); // Logs 0, 1, 2
}
```

### 7. Small Exercise

**Challenge**: Create a function factory that generates 10,000 unique closures but uses minimal memory (not one closure per item).

### 8. System Design Relevance

- **Memory Management**: Closures can cause memory leaks if not managed
- **Performance**: Excessive closures increase GC pressure
- **Encapsulation**: Enable private variables and module patterns

---

## Topic 5: Prototypes and Inheritance

### 1. Core Concept
JavaScript uses **prototype-based inheritance** where objects inherit directly from other objects. Every object has a `[[Prototype]]` (accessible via `__proto__` or `Object.getPrototypeOf()`).

**Mental Model**: Prototype chain is like a **family tree** where properties are inherited from ancestors.

### 2. How It Works Internally

```
const obj = { a: 1 }
const childObj = Object.create(obj);
childObj.b = 2;

// Property lookup:
// 1. Check own properties: childObj.b = 2 ✓
// 2. Not found? Check prototype: childObj.[[Prototype]].a = 1 ✓
// 3. Not found? Check prototype's prototype
// 4. Continue until null
// 5. If not found: undefined

childObj.a // 1 (inherited)
childObj.b // 2 (own)
childObj.c // undefined (not in chain)
```

### 3. Real-world Use in Large Systems

**DOM Inheritance (browsers)**:
```
HTMLElement
  ├── HTMLDivElement
  ├── HTMLSpanElement
  └── HTMLButtonElement
```

**Error Handling (all frameworks)**:
```
Error
  ├── TypeError
  ├── ReferenceError
  └── CustomError
```

**React Components**:
```javascript
React.Component (base class)
  └── UserProfileComponent (inherits render, setState, etc.)
```

### 4. Code Examples

```javascript
// Example 1: Prototype chain basics
function Animal(name) {
  this.name = name;
}

Animal.prototype.speak = function() {
  console.log(this.name + ' speaks');
};

function Dog(name, breed) {
  Animal.call(this, name); // Call parent constructor
  this.breed = breed;
}

// Set up inheritance
Dog.prototype = Object.create(Animal.prototype);
Dog.prototype.constructor = Dog;

// Add Dog-specific methods
Dog.prototype.bark = function() {
  console.log(this.name + ' barks');
};

const myDog = new Dog('Rex', 'Labrador');
myDog.speak(); // 'Rex speaks' (inherited from Animal)
myDog.bark();  // 'Rex barks' (own method)

// Example 2: Property lookup performance
class PropertyLookupPerf {
  constructor() {
    this.obj = {};
    // Create long prototype chain
    let proto = this.obj;
    for(let i = 0; i < 100; i++) {
      const parent = Object.create(proto);
      proto = parent;
    }
    
    proto.deepValue = 'found';
  }
  
  measure() {
    // Accessing property at end of 100-level chain is slower
    const start = performance.now();
    for(let i = 0; i < 1e7; i++) {
      this.obj.deepValue;
    }
    return performance.now() - start;
  }
}

// Example 3: Prototype method vs own property
class MethodStorage {
  // WRONG - huge memory usage
  constructor(count) {
    for(let i = 0; i < count; i++) {
      this['method' + i] = function() { return i; };
    }
  }
  
  // RIGHT - use prototype, shared across all instances
  static registerMethod(i) {
    MethodStorage.prototype['method' + i] = function() { return i; };
  }
}

// Example 4: instanceof and prototype chain
function checkInstanceof() {
  function A() {}
  function B() {}
  function C() {}
  
  B.prototype = Object.create(A.prototype);
  C.prototype = Object.create(B.prototype);
  
  const c = new C();
  
  console.log(c instanceof C); // true
  console.log(c instanceof B); // true (up the chain)
  console.log(c instanceof A); // true (all the way up)
  console.log(c instanceof Object); // true (all objects inherit from Object)
}

// Example 5: Object.create vs class syntax (same thing, different syntax)
// ES5 style
function UserES5(name) {
  this.name = name;
}
UserES5.prototype.greet = function() { return `Hi ${this.name}`; };

// ES6 class (syntactic sugar over prototype)
class UserES6 {
  constructor(name) {
    this.name = name;
  }
  greet() {
    return `Hi ${this.name}`;
  }
}

// Both are functionally identical under the hood

// Example 6: Detecting own vs inherited properties
function checkOwnership() {
  const parent = { a: 1 };
  const child = Object.create(parent);
  child.b = 2;
  
  console.log('a' in child); // true (includes inherited)
  console.log(child.hasOwnProperty('a')); // false (own only)
  console.log(child.hasOwnProperty('b')); // true
}

// Example 7: Performance - object shape matters
class ShapeOptimization {
  static testObjectShapes() {
    // V8 optimizes objects with consistent shapes
    
    // Shape 1: properties added in same order
    const obj1 = {};
    obj1.a = 1;
    obj1.b = 2;
    obj1.c = 3;
    
    // Same shape as obj1 - reuses hidden class
    const obj2 = {};
    obj2.a = 1;
    obj2.b = 2;
    obj2.c = 3;
    
    // Different shape - creates new hidden class
    const obj3 = {};
    obj3.c = 3;
    obj3.b = 2;
    obj3.a = 1;
    
    // obj1 and obj2 will be optimized faster than obj3
  }
}

// Example 8: Prototype pollution (security concern)
function demonstratePrototypePollution() {
  // DANGEROUS pattern
  function merge(target, source) {
    for(let key in source) {
      target[key] = source[key]; // No hasOwnProperty check!
    }
  }
  
  const user = {};
  merge(user, JSON.parse('{"name": "user", "__proto__": {"isAdmin": true}}'));
  
  // Now ALL objects have isAdmin = true!
  const newUser = {};
  console.log(newUser.isAdmin); // true - SECURITY LEAK!
}
```

### 5. Interview Questions

1. **Explain the prototype chain. Why when I access a property, does JavaScript check multiple objects?**
2. **What's the difference between `Function.prototype.call()` and using the class syntax?**
3. **Design an inheritance system that avoids the "deep prototype chain" performance problem.**
4. **How would you prevent prototype pollution attacks in a library?**
5. **Why is accessing properties in a long prototype chain slower? How do engines optimize this?**
6. **Explain `instanceof` in terms of the prototype chain.**
7. **In ES6 classes, what's actually happening under the hood with inheritance?**
8. **How would you implement mixins using prototype chain?**
9. **Why do you need `Object.create()` instead of directly assigning prototypes?**
10. **Design a system where 1 million objects share methods without memory issues.**

### 6. Common Mistakes

**Mistake 1: Modifying Object.prototype**
```javascript
// NEVER DO THIS
Object.prototype.myMethod = function() { ... };

// Now EVERY object has this method, breaking libraries
```

**Mistake 2: Not using Object.create() properly**
```javascript
// WRONG - modifies Parent.prototype
function Child() {}
Child.prototype = Parent.prototype;
Child.prototype.myMethod = ...; // Now Parent also has it!

// RIGHT - creates new object
Child.prototype = Object.create(Parent.prototype);
```

**Mistake 3: Forgetting to set constructor**
```javascript
// WRONG
Child.prototype = Object.create(Parent.prototype);
new Child().constructor === Child; // FALSE!

// RIGHT
Child.prototype = Object.create(Parent.prototype);
Child.prototype.constructor = Child;
new Child().constructor === Child; // TRUE
```

### 7. Small Exercise

**Challenge**: Create a multi-level inheritance system:
- Animal (name, move)
- Mammal (extends Animal, warm-blooded)
- Dog (extends Mammal, bark)

Ensure no memory waste and proper method lookup.

### 8. System Design Relevance

- **Performance**: Object shape consistency helps V8 optimize
- **Memory**: Share methods via prototype, not own properties
- **Scalability**: Can't pollute prototype of shared objects

---

## Topic 6: This Keyword

### 1. Core Concept
`this` refers to the **object that is calling the function**. It's determined by **how** the function is called, not where it's defined.

**Mental Model**: `this` is like a **name badge** that says "you are currently inside this object's context."

### 2. How It Works Internally

```
Four ways this is bound:

1. Default binding → this = global object (or undefined in strict mode)
   fn()
   
2. Implicit binding → this = the object before the dot
   obj.method()
   
3. Explicit binding → this = what you specify
   fn.call(specificThis)
   fn.apply(specificThis, args)
   fn.bind(specificThis)
   
4. New binding → this = newly created object
   new Constructor()
   
5. Arrow function → this = this from enclosing scope (doesn't rebind)
   const arrow = () => this
```

### 3. Real-world Use in Large Systems

**Event Handlers**:
```javascript
// this matters for event handlers
button.addEventListener('click', function() {
  console.log(this); // this = button
});

// Arrow function ignores this binding
button.addEventListener('click', () => {
  console.log(this); // this = global (in browsers) or module (in Node)
});
```

**Class Methods**:
```javascript
class Counter {
  count = 0;
  
  increment() {
    this.count++; // this must be the Counter instance
  }
}

const counter = new Counter();
const inc = counter.increment;
inc(); // ERROR or this = global! Need .bind() or arrow
```

### 4. Code Examples

```javascript
// Example 1: Four binding rules
function greet() {
  console.log(this.name);
}

// 1. Default binding (loose mode)
greet(); // undefined or throws error

// 2. Implicit binding
const person = { name: 'Alice', greet };
person.greet(); // 'Alice' - this = person

// 3. Explicit binding
const person2 = { name: 'Bob' };
greet.call(person2); // 'Bob' - force this = person2
greet.apply(person2); // Same as call, but args in array

// 4. New binding
function Person(name) {
  this.name = name; // this = newly created object
}
const p = new Person('Charlie'); // 'Charlie'

// Example 2: Arrow functions (no rebinding)
class Counter {
  count = 0;
  
  // Regular method - this gets rebound
  increment() {
    this.count++;
  }
  
  // Arrow method - this is always Counter instance
  incrementArrow = () => {
    this.count++;
  };
}

const c = new Counter();
const inc = c.increment;
inc(); // ERROR - this is undefined

const inc2 = c.incrementArrow;
inc2(); // Works - this is still c

// Example 3: Bind, call, apply
const user = { name: 'Alice' };

function sayHello(greeting, punctuation) {
  return `${greeting}, ${this.name}${punctuation}`;
}

// call - execute immediately with specified this
sayHello.call(user, 'Hi', '!'); // "Hi, Alice!"

// apply - same but args in array
sayHello.apply(user, ['Hello', '.']); // "Hello, Alice."

// bind - return NEW function with locked this
const greetingFunc = sayHello.bind(user);
greetingFunc('Hey', '?'); // "Hey, Alice?"

// Example 4: This in nested functions (common trap)
const obj = {
  name: 'Object',
  outer: function() {
    console.log(this.name); // 'Object'
    
    function inner() {
      console.log(this.name); // undefined! - inner is regular function
    }
    
    inner(); // Default binding
  }
};

obj.outer();

// FIX 1: Use arrow function
const obj2 = {
  name: 'Object',
  outer: function() {
    console.log(this.name); // 'Object'
    
    const inner = () => {
      console.log(this.name); // 'Object' - arrow retains this
    };
    
    inner();
  }
};

// FIX 2: Save reference
const obj3 = {
  name: 'Object',
  outer: function() {
    const self = this;
    
    function inner() {
      console.log(self.name); // 'Object'
    }
    
    inner();
  }
};

// Example 5: This in callbacks (React-like pattern)
class Component {
  data = 'Component data';
  
  // WRONG - this is undefined in callback
  handleClick1() {
    setTimeout(function() {
      console.log(this.data); // undefined
    }, 100);
  }
  
  // CORRECT - arrow function preserves this
  handleClick2() {
    setTimeout(() => {
      console.log(this.data); // 'Component data'
    }, 100);
  }
  
  // ALSO CORRECT - explicit binding with bind
  handleClick3() {
    setTimeout(function() {
      console.log(this.data);
    }.bind(this), 100); // Bind this = this Component
  }
}

// Example 6: Using call for array methods
const numbers = [1, 2, 3];

// This trick: Math.max doesn't take arrays, use call
const max = Math.max.call(null, ...numbers); // 3

// Or use apply
const max2 = Math.max.apply(null, numbers); // 3

// Example 7: This in pure functions (functional programming)
// Pure function doesn't depend on this
const add = (a, b) => a + b; // this is irrelevant

// Object-oriented: this is crucial
const calculator = {
  value: 0,
  add(n) {
    this.value += n;
    return this;
  },
  multiply(n) {
    this.value *= n;
    return this;
  }
};

calculator.add(5).multiply(2); // this.value = 10
```

### 5. Interview Questions

1. **Predict the output without running it:**
```javascript
const obj = {
  name: 'Object',
  method: function() {
    function inner() {
      console.log(this.name);
    }
    inner();
  }
};
obj.method();
```

2. **What's the difference between `call`, `apply`, and `bind`?**
3. **Why do arrow functions not have their own `this`?**
4. **In a class constructor, what is `this`?**
5. **Design a callback system that maintains `this` context without arrow functions.**
6. **How would you explain `this` to someone coming from a class-based language?**
7. **Why is understanding `this` critical for fixing production bugs?**
8. **Compare: Method vs Function vs Arrow Function - how does `this` differ?**
9. **In strict mode, what's `this` value in a regular function call?**
10. **Design a library that supports both OOP (this-based) and FP (no-this) patterns.**

### 6. Common Mistakes

**Mistake 1: Assuming arrow function has this**
```javascript
// WRONG
const obj = {
  value: 42,
  getValue: () => this.value // this is not obj!
};
console.log(obj.getValue()); // undefined

// RIGHT
const obj = {
  value: 42,
  getValue: function() { return this.value; }
};
console.log(obj.getValue()); // 42
```

**Mistake 2: Losing this in callbacks**
```javascript
// WRONG
class Button {
  constructor() {
    this.text = 'Click me';
    element.addEventListener('click', this.handleClick);
  }
  
  handleClick() {
    console.log(this.text); // undefined - this is element, not Button
  }
}

// RIGHT
class Button {
  constructor() {
    this.text = 'Click me';
    element.addEventListener('click', this.handleClick.bind(this));
  }
  
  handleClick() {
    console.log(this.text); // 'Click me'
  }
}
```

**Mistake 3: Misunderstanding this in nested functions**
```javascript
// WRONG
const obj = {
  value: 42,
  outer: function() {
    setTimeout(function() {
      console.log(this.value); // undefined - this is global
    }, 0);
  }
};

// RIGHT
const obj = {
  value: 42,
  outer: function() {
    setTimeout(() => {
      console.log(this.value); // 42 - arrow preserves this
    }, 0);
  }
};
```

### 7. Small Exercise

**Challenge**: Create a class `UserProfile` where:
1. Methods need `this` to reference the instance
2. Event handlers maintain `this` context
3. Async operations don't lose `this`

### 8. System Design Relevance

- **OOP**: Fundamental to instance methods and state
- **Performance**: Incorrect this binding can cause subtle bugs
- **Testing**: Need to mock `this` correctly in unit tests

---

## Topic 7: Promises

### 1. Core Concept
A **Promise** is an object representing the eventual completion (or failure) of an asynchronous operation and its resulting value.

**Mental Model**: Promise is like ordering food at a restaurant:
- **Pending**: Food is being prepared
- **Fulfilled**: Food is ready (success)
- **Rejected**: Kitchen is out of ingredients (failure)

### 2. How It Works Internally

```
Promise States (Immutable transition):
PENDING → FULFILLED (resolve called)
       → REJECTED (reject called)

Once fulfilled or rejected, the state NEVER changes
```

```javascript
new Promise((resolve, reject) => {
  // resolve and reject are callbacks
  // They can only be called ONCE
  resolve(value); // Transitions to FULFILLED
  reject(error);  // Transitions to REJECTED
  
  // If both called, first one wins
});
```

### 3. Real-world Use in Large Systems

**HTTP Requests (Fetch API)**:
```javascript
fetch('/api/users')
  .then(response => response.json()) // First fulfillment
  .then(data => renderUI(data))      // Chained promise
  .catch(error => showError(error));  // Handle rejection
```

**Database Operations**:
```javascript
db.query(sql)
  .then(results => processResults(results))
  .catch(error => rollback())
```

**Promise.all() for parallel operations**:
```javascript
Promise.all([
  fetch('/api/users'),
  fetch('/api/posts'),
  fetch('/api/comments')
]).then(([users, posts, comments]) => {
  // All completed before then runs
});
```

### 4. Code Examples

```javascript
// Example 1: Basic Promise
const promise = new Promise((resolve, reject) => {
  setTimeout(() => resolve('Success!'), 1000);
});

promise
  .then(value => console.log(value)) // 'Success!'
  .catch(error => console.error(error));

// Example 2: Promise states (immutable)
let state = 'pending';

const p = new Promise((resolve, reject) => {
  resolve('first');
  resolve('second'); // Ignored - already resolved
  reject('error');   // Ignored - already resolved
});

p.then(v => console.log(v)); // 'first' only

// Example 3: Promise.all vs Promise.race
async function demonstratePromiseMethods() {
  const p1 = new Promise(r => setTimeout(() => r(1), 100));
  const p2 = new Promise(r => setTimeout(() => r(2), 50));
  const p3 = new Promise((r, rj) => setTimeout(() => rj(3), 150));
  
  // all - waits for all, rejects if any rejects
  try {
    const results = await Promise.all([p1, p2, p3]);
  } catch(e) {
    console.log('Rejected:', e); // 3
  }
  
  // race - returns first to settle (resolve or reject)
  const first = await Promise.race([p1, p2, p3]);
  console.log(first); // 2 (fastest)
  
  // allSettled - waits for all, returns status of each
  const statuses = await Promise.allSettled([p1, p2, p3]);
  // [{status: 'fulfilled', value: 1}, ...]
}

// Example 4: Promise chaining (creating chains of operations)
fetchUser(userId)
  .then(user => fetchUserPosts(user.id))     // user passes to next
  .then(posts => filterImportant(posts))     // posts passes to next
  .then(important => renderPosts(important)) // important passes to next
  .catch(error => showError(error));         // Any error caught here

// Example 5: Common gotcha - forgetting return
// WRONG - loses chain
promise
  .then(value => {
    fetchMore(value);  // No return! breaks chain
  })
  .then(result => process(result)); // result is undefined!

// RIGHT - return the promise
promise
  .then(value => {
    return fetchMore(value); // Returns new promise
  })
  .then(result => process(result)); // result is correctly passed

// Example 6: Promise.all error handling
async function robustPromiseAll() {
  // Problem: If one fails, all fail
  try {
    const results = await Promise.all([
      fetch('/api/1'),
      fetch('/api/2'),
      fetch('/api/3')
    ]);
  } catch(e) {
    // One failed, but what about the others?
  }
  
  // Solution: Use allSettled
  const results = await Promise.allSettled([
    fetch('/api/1'),
    fetch('/api/2'),
    fetch('/api/3')
  ]);
  
  results.forEach(result => {
    if(result.status === 'fulfilled') {
      console.log(result.value);
    } else {
      console.log('Failed:', result.reason);
    }
  });
}

// Example 7: Promise performance gotcha
// WRONG - sequential execution (slow)
async function slowSequential(items) {
  for(let item of items) {
    const result = await fetchData(item); // Waits for each
  }
}

// RIGHT - parallel execution (fast)
async function fastParallel(items) {
  const promises = items.map(item => fetchData(item));
  const results = await Promise.all(promises); // All at once
}

// Example 8: Promisify callback-based functions
// Convert callback to Promise
function promisify(fn) {
  return (...args) => new Promise((resolve, reject) => {
    fn(...args, (err, result) => {
      if(err) reject(err);
      else resolve(result);
    });
  });
}

// Usage
const readFileAsync = promisify(fs.readFile);
const content = await readFileAsync('file.txt');
```

### 5. Interview Questions

1. **What happens if you don't have a `.catch()` at the end of a Promise chain?**
2. **Design a retry mechanism for failed Promises with exponential backoff.**
3. **Why is `Promise.all()` problematic if you have many requests? What's the better approach?**
4. **Explain the difference between `Promise.all()`, `Promise.race()`, and `Promise.allSettled()`.**
5. **Why does this code log undefined instead of waiting for the fetch?**
```javascript
async function getData() {
  const data = fetch('/api/data');
  return data;
}
```

6. **How would you implement timeout for a Promise?**
7. **Can a Promise have multiple `.then()` attached to it? Does execution order matter?**
8. **Design a system that loads 10,000 items in parallel without crashing the browser.**
9. **What's the difference between `.then(fn).catch(err)` vs `.then(fn, err)`?**
10. **In a production system, why do you need to monitor for unhandled Promise rejections?**

### 6. Common Mistakes

**Mistake 1: Not returning promises in chain**
```javascript
// WRONG - chain breaks
fetch('/api')
  .then(r => r.json())
  .then(data => {
    fetch('/api/more'); // No return!
  })
  .then(moreData => process(moreData)); // undefined

// RIGHT
fetch('/api')
  .then(r => r.json())
  .then(data => {
    return fetch('/api/more'); // Return promise to chain
  })
  .then(moreData => process(moreData));
```

**Mistake 2: Promise.all failure handling**
```javascript
// WRONG - one failure = everything fails
const results = await Promise.all([
  risky1(),
  risky2(),
  risky3()
]); // If risky2 fails, risky1 and risky3 results are lost

// RIGHT - handle individual failures
const results = await Promise.allSettled([
  risky1(),
  risky2(),
  risky3()
]);
```

### 7. Small Exercise

**Challenge**: Create a Promise-based request queue that:
1. Makes parallel requests (configurable limit)
2. Handles failures gracefully
3. Retries failed requests

### 8. System Design Relevance

- **Concurrency**: Enables non-blocking operations
- **Resource Management**: Promise.all must limit parallelism
- **Error Handling**: Unhandled rejections crash servers

---

## Topic 8: Async/Await

### 1. Core Concept
`async/await` is syntactic sugar over Promises that makes asynchronous code look synchronous and easier to read.

**Mental Model**: `async` function is a special function that always returns a Promise. `await` pauses execution until a Promise settles.

### 2. How It Works Internally

```
async function foo() {
  const result = await somePromise;
}

// Internally becomes:

function foo() {
  return new Promise((resolve, reject) => {
    // When await is hit, execution pauses
    // When Promise settles, execution resumes
  });
}
```

### 3. Real-world Use in Large Systems

**Data fetching (every web app)**:
```javascript
async function loadUserProfile(userId) {
  try {
    const user = await fetch(`/api/users/${userId}`).then(r => r.json());
    const posts = await fetch(`/api/posts?userId=${userId}`).then(r => r.json());
    return { user, posts };
  } catch(error) {
    handleError(error);
  }
}
```

**Production monitoring**:
```javascript
async function healthCheck() {
  try {
    const db = await testDatabaseConnection();
    const cache = await testCacheConnection();
    return { db: true, cache: true };
  } catch(error) {
    console.error('Health check failed:', error);
  }
}
```

### 4. Code Examples

```javascript
// Example 1: Basic async/await
async function fetchUserData(userId) {
  try {
    const response = await fetch(`/api/users/${userId}`);
    const data = await response.json();
    return data;
  } catch(error) {
    console.error('Failed to fetch user:', error);
  }
}

// Same as:
function fetchUserData(userId) {
  return fetch(`/api/users/${userId}`)
    .then(response => response.json())
    .catch(error => console.error('Failed to fetch user:', error));
}

// Example 2: Parallel vs sequential
// WRONG - sequential (slow)
async function slowapproach() {
  const user = await fetchUser();
  const posts = await fetchUserPosts(user.id);
  const comments = await fetchPostComments(posts[0].id);
  return { user, posts, comments };
}

// RIGHT - parallel where possible
async function fastapproach() {
  const user = await fetchUser();
  const [posts, profile] = await Promise.all([
    fetchUserPosts(user.id),
    fetchUserProfile(user.id)
  ]);
  return { user, posts, profile };
}

// Example 3: For loop vs map with async
// WRONG - processes one at a time
async function processSequential(ids) {
  for(let id of ids) {
    await processUser(id); // Slow!
  }
}

// RIGHT - processes in parallel
async function processParallel(ids) {
  await Promise.all(ids.map(id => processUser(id))); // Fast!
}

// Example 4: Error handling options
// Option 1: try/catch
async function tryCatch() {
  try {
    const data = await riskyOperation();
    return data;
  } catch(error) {
    console.error(error);
  }
}

// Option 2: .catch() on the promise
async function withCatch() {
  const data = await riskyOperation().catch(error => {
    console.error(error);
    return defaultValue;
  });
  return data;
}

// Option 3: Higher-order function wrapper
function handleAsync(fn) {
  return async (...args) => {
    try {
      return await fn(...args);
    } catch(error) {
      console.error(error);
      return null;
    }
  };
}

const safeOp = handleAsync(riskyOperation);

// Example 5: Timeout pattern
async function fetchWithTimeout(url, ms = 5000) {
  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), ms);
  
  try {
    const response = await fetch(url, { signal: controller.signal });
    clearTimeout(timeoutId);
    return response.json();
  } catch(error) {
    clearTimeout(timeoutId);
    throw new Error(`Timeout or request failed: ${error}`);
  }
}

// Example 6: Retry with exponential backoff
async function retryWithBackoff(fn, maxRetries = 3) {
  for(let i = 0; i < maxRetries; i++) {
    try {
      return await fn();
    } catch(error) {
      if(i === maxRetries - 1) throw error;
      
      const delay = Math.pow(2, i) * 1000; // Exponential backoff
      await new Promise(r => setTimeout(r, delay));
    }
  }
}

// Example 7: Concurrent execution control
class ConcurrencyControl {
  constructor(limit = 5) {
    this.limit = limit;
    this.running = 0;
    this.queue = [];
  }
  
  async run(fn) {
    while(this.running >= this.limit) {
      await new Promise(r => this.queue.push(r));
    }
    
    this.running++;
    try {
      return await fn();
    } finally {
      this.running--;
      const resolve = this.queue.shift();
      if(resolve) resolve();
    }
  }
}

// Usage
const fc = new ConcurrencyControl(5);
const promises = ids.map(id => 
  fc.run(() => fetchUser(id))
);
await Promise.all(promises); // Max 5 concurrent

// Example 8: Async generator (streaming/pagination)
async function* fetchAllPages(url) {
  let nextUrl = url;
  
  while(nextUrl) {
    const response = await fetch(nextUrl);
    const data = await response.json();
    
    yield data.items;
    nextUrl = data.nextPage;
  }
}

// Usage
for await (const page of fetchAllPages('/api/users')) {
  console.log(page); // Process page by page
}
```

### 5. Interview Questions

1. **What does `async function` always return?**
2. **Why is this code problematic?**
```javascript
async function process() {
  const a = await fetchA();
  const b = await fetchB();
  return a + b;
}
```

3. **How would you implement a rate limiter using async/await?**
4. **Explain the difference between sequential and parallel async operations. When would you use each?**
5. **What happens if you `await` on a non-Promise value?**
6. **Design a system that cancels pending requests when a user navigates away.**
7. **Why is error handling critical in async functions?**
8. **How would you handle partial failures in Promise.all()? What's the production pattern?**
9. **In Node.js, what happens if an async handler throws without try/catch?**
10. **Design a batch processor that uses async/await for I/O-bound work.**

### 6. Common Mistakes

**Mistake 1: Sequential instead of parallel**
```javascript
// WRONG - slow, waits 3 seconds
const a = await fetch1(); // 1s
const b = await fetch2(); // 1s
const c = await fetch3(); // 1s

// RIGHT - fast, waits 1 second
const [a, b, c] = await Promise.all([
  fetch1(),
  fetch2(),
  fetch3()
]);
```

**Mistake 2: Not handling errors**
```javascript
// WRONG - uncaught rejection crashes server
async function handler() {
  const data = await riskyOp(); // Error silently fails
  return data;
}

// RIGHT
async function handler() {
  try {
    const data = await riskyOp();
    return data;
  } catch(error) {
    console.error(error);
    return defaultValue;
  }
}
```

### 7. Small Exercise

**Challenge**: Create an async data loader that:
1. Loads data in parallel (max 5 concurrent)
2. Retries failed requests
3. Implements timeout
4. Reports progress

### 8. System Design Relevance

- **Throughput**: Async operations must be managed to not overwhelm resources
- **Latency**: Sequential operations cause cascading delays
- **Error Recovery**: Production systems need retry and fallback mechanisms

---

## Topic 9: Memory Management

### 1. Core Concept
JavaScript has **automatic memory management** through garbage collection (GC). Developers don't manually allocate/free memory, but understanding GC is crucial for production.

**Mental Model**: Garbage collection is like a **janitor** who regularly cleans up objects no longer in use. The longer objects are reachable, the longer they stay in memory.

### 2. How It Works Internally

```
Memory Lifecycle:
1. ALLOCATION → Variable assigned
2. USE → Program references the variable
3. GARBAGE COLLECTION → No more references → Memory freed

Reference Counting (simplified):
const obj = {};    // Reference count = 1
const ref = obj;   // Reference count = 2
ref = null;        // Reference count = 1
obj = null;        // Reference count = 0 → GC eligible
```

**Mark and Sweep Algorithm** (modern engines):
```
1. Mark phase: Mark all reachable objects starting from roots (global, call stack)
2. Sweep phase: Free unmarked objects
3. Compact phase: Consolidate memory
```

### 3. Real-world Use in Large Systems

**Node.js Servers (Memory Leaks)**:
- Long-running servers accumulate garbage faster than GC can clean
- Must monitor heap size in production
- Memory leak = gradual memory increase until crash

**Garbage Collection Pauses**:
- GC pause = all JavaScript execution stops
- Can cause 50-200ms pause (visible as jank in browsers)
- Generational GC reduces pauses (most objects die young)

**Browser Games/WebGL**:
- Strict object size limits due to mobile memory
- Must reuse objects to avoid GC pressure

### 4. Code Examples

```javascript
// Example 1: Memory leak - unintended reference
class Button {
  constructor() {
    this.data = new Array(1e7); // 40MB
    
    // WRONG - closure captures entire this
    document.addEventListener('click', () => {
      console.log(this.data.length);
    });
    // Button won't be GCed even after removed from DOM
  }
}

// RIGHT - capture only needed data
class Button {
  constructor() {
    this.data = new Array(1e7);
    const dataLength = this.data.length;
    
    document.addEventListener('click', () => {
      console.log(dataLength);
    });
    // Now this.data can be GCed when Button is destroyed
  }
}

// Example 2: Detached DOM nodes (common leak)
// WRONG
function appendData() {
  const div = document.createElement('div');
  div.innerHTML = 'Large content'.repeat(1e6); // Large DOM
  
  const ref = div;
  return ref;
}

const leak = appendData();
document.body.appendChild(leak);
document.body.removeChild(leak); // DOM removed, but ref still exists!
// leak variable keeps DOM in memory

// RIGHT - clean up references
const leak = appendData();
document.body.appendChild(leak);
document.body.removeChild(leak);
leak = null; // Allow GC

// Example 3: Hidden classes affecting GC
class OptimizedObject {
  constructor() {
    this.a = 1;
    this.b = 2;
    this.c = 3;
  }
}

// WRONG - inconsistent shapes hurt GC performance
function createObjects() {
  const obj1 = { a: 1, b: 2 };
  const obj2 = { b: 2, a: 1 }; // Different property order
  const obj3 = { a: 1 }; // Different shape
  
  return [ obj1, obj2, obj3 ];
}

// RIGHT - consistent shapes
function createObjects() {
  return [
    new OptimizedObject(),
    new OptimizedObject(),
    new OptimizedObject()
  ];
}

// Example 4: Cache overflow (memory leak pattern)
class Cache {
  constructor(maxSize = 100) {
    this.cache = new Map();
    this.maxSize = maxSize;
  }
  
  set(key, value) {
    // WRONG - cache grows forever
    this.cache.set(key, value);
  }
  
  // RIGHT - implement eviction
  setLimited(key, value) {
    if(this.cache.size >= this.maxSize) {
      const firstKey = this.cache.keys().next().value;
      this.cache.delete(firstKey); // LRU eviction
    }
    this.cache.set(key, value);
  }
}

// Example 5: Timer and interval leaks
// WRONG - setInterval never stops
setInterval(() => {
  doWork(); // Runs forever, keeps objects in memory
}, 1000);

// RIGHT - clean up
const timer = setInterval(() => {
  doWork();
}, 1000);

// Later:
clearInterval(timer); // Allow GC

// Example 6: Event listener leaks
// WRONG
for(let i = 0; i < 1000; i++) {
  const button = document.createElement('button');
  button.addEventListener('click', () => {
    // This handler keeps button in memory forever (by default)
    console.log(i);
  });
  document.body.appendChild(button);
}

// RIGHT - remove listeners
const buttons = [];
for(let i = 0; i < 1000; i++) {
  const button = document.createElement('button');
  const handler = () => console.log(i);
  
  button.addEventListener('click', handler);
  buttons.push({ button, handler });
  document.body.appendChild(button);
}

// Later, cleanup:
buttons.forEach(({ button, handler }) => {
  button.removeEventListener('click', handler);
  button.remove();
});

// Example 7: WeakMap for GC-friendly caching
// WRONG - cache prevents GC
const cache = new Map();

function cacheData(obj, data) {
  cache.set(obj, data);
}

let obj = { id: 1 };
cacheData(obj, largeData);
obj = null; // obj won't be GCed because cache still references it!

// RIGHT - WeakMap allows GC
const weakCache = new WeakMap();

let obj2 = { id: 2 };
weakCache.set(obj2, largeData);
obj2 = null; // obj2 CAN be GCed despite weakCache

// Example 8: Monitoring memory in Node.js
class MemoryMonitor {
  static reportMemory() {
    const usage = process.memoryUsage();
    
    return {
      heapUsed: Math.round(usage.heapUsed / 1024 / 1024) + ' MB',
      heapTotal: Math.round(usage.heapTotal / 1024 / 1024) + ' MB',
      rss: Math.round(usage.rss / 1024 / 1024) + ' MB'
    };
  }
  
  static detectLeak() {
    const start = this.reportMemory().heapUsed;
    
    // Force GC (must run Node with --expose-gc)
    if(global.gc) global.gc();
    
    const after = this.reportMemory().heapUsed;
    
    return {
      leak: after > start,
      diff: after - start
    };
  }
}
```

### 5. Interview Questions

1. **What causes memory leaks in JavaScript? Give 3 real examples.**
2. **Why do event listeners cause memory leaks if not cleaned up?**
3. **Design a cache system that doesn't grow indefinitely.**
4. **Explain mark-and-sweep garbage collection in simple terms.**
5. **How would you monitor memory usage in a Node.js production server?**
6. **What's the difference between detached DOM nodes and unreachable objects?**
7. **Why would storing large objects in closures cause memory issues?**
8. **In browsers, how would you detect and fix memory leaks?**
9. **What's WeakMap and why would you use it instead of Map?**
10. **Design a system that maintains large data with minimal GC pressure.**

### 6. Common Mistakes

**Mistake 1: Global variables cause memory leaks**
```javascript
// WRONG
function createData() {
  globalData = new Array(1e7); // Persists forever
}

// RIGHT
function createData() {
  const localData = new Array(1e7); // GCed when function ends
  return processData(localData);
}
```

**Mistake 2: Storing objects in caches without limits**
```javascript
// WRONG - unbounded cache
const userCache = {};

function cacheUser(id, user) {
  userCache[id] = user; // Cache grows forever
}

// RIGHT - bounded cache with TTL
const userCache = new Map();
function cacheUser(id, user, ttl = 60000) {
  userCache.set(id, user);
  setTimeout(() => userCache.delete(id), ttl); // Evict after TTL
}
```

### 7. Small Exercise

**Challenge**: Create a `SmartCache` class that:
1. Stores data efficiently
2. Prevents unbounded growth
3. Implements LRU (Least Recently Used) eviction
4. Reports memory usage

### 8. System Design Relevance

- **Reliability**: Memory leaks crash production servers
- **Performance**: GC pauses affect latency
- **Scalability**: Memory growth limits throughput

---

## Topic 10: Debouncing and Throttling

### 1. Core Concept
**Debouncing** delays a function call until after a specified time has elapsed since the last invocation. **Throttling** limits how often a function executes to at most once per specified interval.

**Mental Model**:
- **Debouncing**: Waiter collecting all orders for 10 seconds, then gives one order per customer batch
- **Throttling**: ATM that processes one transaction per 30 seconds maximum

### 2. How It Works Internally

```
Debounce: Reset timer on every call
call → wait → call → wait → call → Execute ONCE after wait

Throttle: Execute, then ignore calls until timer expires
call → Execute → ignore → ignore → wait → Execute → ignore
```

### 3. Real-world Use in Large Systems

**Search Input (Google Autocomplete)**:
- User types quickly: `j e s`, `e s`, `s t` events fire rapidly
- Debounce ensures API call only happens after user stops typing

**Window Resize (Responsive Layouts)**:
- Resize event fires 100s of times per second
- Throttle to recalculate layout only 30-60 times per second

**Scroll Events (Infinite Scroll)**:
- Scroll fires 100+ times while scrolling
- Throttle to check if reached bottom only every 100ms

### 4. Code Examples

```javascript
// Example 1: Basic debounce
function debounce(fn, delay) {
  let timeoutId;
  
  return function(...args) {
    clearTimeout(timeoutId); // Cancel previous
    
    timeoutId = setTimeout(() => {
      fn(...args);
    }, delay);
  };
}

// Usage
const searchAPI = debounce((query) => {
  fetch(`/api/search?q=${query}`);
}, 300);

input.addEventListener('input', (e) => {
  searchAPI(e.target.value); // Only calls after 300ms of inactivity
});

// Example 2: Basic throttle
function throttle(fn, interval) {
  let last = 0;
  
  return function(...args) {
    const now = Date.now();
    
    if(now - last >= interval) {
      fn(...args);
      last = now;
    }
  };
}

// Usage
const handleScroll = throttle(() => {
  console.log('Scroll event processed');
}, 100); // Max once per 100ms

window.addEventListener('scroll', handleScroll);

// Example 3: Debounce with immediate option
function debounceWithImmediate(fn, delay, immediate = false) {
  let timeoutId;
  
  return function(...args) {
    const later = () => {
      timeoutId = null;
      if(!immediate) fn(...args);
    };
    
    const shouldCallNow = immediate && !timeoutId;
    clearTimeout(timeoutId);
    timeoutId = setTimeout(later, delay);
    
    if(shouldCallNow) fn(...args); // Call immediately on first invocation
  };
}

// Example 4: Throttle with trailing option
function throttleWithTrailing(fn, interval) {
  let last = 0;
  let timeoutId;
  
  return function(...args) {
    const now = Date.now();
    
    if(now - last >= interval) {
      fn(...args);
      last = now;
    } else {
      clearTimeout(timeoutId);
      timeoutId = setTimeout(() => {
        fn(...args); // Call on trailing end
        last = Date.now();
      }, interval - (now - last));
    }
  };
}

// Example 5: Leading and trailing options
function throttleAdvanced(fn, interval, { leading = true, trailing = true } = {}) {
  let last = 0;
  let timeoutId;
  
  return function(...args) {
    const now = Date.now();
    const timeSinceLastCall = now - last;
    
    if(!last && !leading) {
      last = now;
    }
    
    if(timeSinceLastCall >= interval && leading) {
      fn(...args);
      last = now;
    } else if(trailing) {
      clearTimeout(timeoutId);
      timeoutId = setTimeout(() => {
        if(trailing) {
          fn(...args);
        }
        last = 0;
      }, interval - timeSinceLastCall);
    }
  };
}

// Example 6: When to use debounce vs throttle
function demonstrateUseCases() {
  // Debounce for: Search, autocomplete, resize calculations
  const optimizedSearch = debounce((query) => {
    fetch(`/api/search?q=${query}`);
  }, 500); // Wait for user to stop typing
  
  // Throttle for: Scroll, mouse move, resize events
  const optimizedScroll = throttle(() => {
    checkIfReachedBottom();
  }, 100); // Check max every 100ms
}

// Example 7: Request cancellation  with abort
function debounceWithAbort(fn, delay) {
  let timeoutId;
  let abortController = null;
  
  return function(...args) {
    clearTimeout(timeoutId);
    if(abortController) abortController.abort(); // Cancel previous request
    
    abortController = new AbortController();
    
    timeoutId = setTimeout(() => {
      fn(...args, abortController.signal);
    }, delay);
  };
}

// Usage
const debouncedFetch = debounceWithAbort((query, signal) => {
  fetch(`/api/search?q=${query}`, { signal })
    .then(r => r.json())
    .then(data => updateUI(data))
    .catch(e => {
      if(e.name !== 'AbortError') throw e;
    });
}, 300);

// Example 8: Performance comparison
class DebounceThrottlePerf {
  static demonstrateImpact() {
    let callCount = { immediate: 0, debounce: 0, throttle: 0 };
    
    // Immediate - 1000 calls
    for(let i = 0; i < 1000; i++) {
      callCount.immediate++;
    }
    
    // Debounce - 1 call after delay
    const db = debounce(() => callCount.debounce++, 100);
    for(let i = 0; i < 1000; i++) {
      db();
    }
    
    // Throttle - ~10 calls (1000ms / 100ms interval)
    const th = throttle(() => callCount.throttle++, 100);
    for(let i = 0; i < 1000; i++) {
      th();
    }
    
    setTimeout(() => {
      console.log(callCount);
      // { immediate: 1000, debounce: ~1, throttle: ~10 }
    }, 150);
  }
}
```

### 5. Interview Questions

1. **What's the difference between debounce and throttle? When would you use each?**
2. **Implement debounce and throttle from scratch.**
3. **Why is debouncing important for search inputs?**
4. **Design a smart throttle that adjusts interval based on device performance.**
5. **How would you debounce a function but still execute on the trailing edge?**
6. **What happens if you debounce with 0ms delay?**
7. **In a resize event handler with 1000 elements, would you use throttle or debounce?**
8. **Design a system that debounces 10,000 simultaneous users' input.**
9. **Why would you need to cancel pending debounced/throttled calls?**
10. **Implement a scroll handler that uses throttle to load infinite items without lag.**

### 6. Common Mistakes

**Mistake 1: Using debounce for scroll events**
```javascript
// WRONG - waits too long to process
window.addEventListener('scroll', debounce(() => {
  checkIfReachedBottom();
}, 500));

// RIGHT - process frequently but not too frequently
window.addEventListener('scroll', throttle(() => {
  checkIfReachedBottom();
}, 100));
```

**Mistake 2: Not tracking pending calls**
```javascript
// WRONG - requests overlap if debounce time expires
let pending = null;
const search = debounce((query) => {
  pending = fetch(`/api/search?q=${query}`);
}, 300);

// RIGHT - cancel previous request
const search = debounceWithAbort((query, signal) => {
  fetch(`/api/search?q=${query}`, { signal });
}, 300);
```

### 7. Small Exercise

**Challenge**: Create a `SmartSearch` component that:
1. Debounces user input (300ms)
2. Shows loading state immediately
3. Cancels previous requests if user types again
4. Caches results

### 8. System Design Relevance

- **Server Load**: Debounce reduces API calls from thousands to dozens
- **UI Responsiveness**: Throttle prevents jank from frequent updates
- **Battery/CPU**: Mobile devices benefit hugely from debounce/throttle

---

## Topic 11: Deep vs Shallow Copy

### 1. Core Concept
**Shallow copy** copies only the top level; nested objects are still referenced. **Deep copy** recursively copies everything.

**Mental Model**: Shallow copy is like photocopying a paper with references to other papers. Deep copy is like making entirely new papers including all references.

### 2. How It Works Internally

```
Shallow Copy:  obj → copy [shares nested objects]
Deep Copy:     obj → copy [new nested objects]

const original = { a: 1, nested: { b: 2 } }

Shallow:
const shallow = { ...original }
shallow.a = 10     // Only affects shallow
shallow.nested.b = 20 // AFFECTS BOTH! (shared reference)

Deep:
const deep = JSON.parse(JSON.stringify(original))
deep.a = 10        // Only affects deep
deep.nested.b = 20 // Only affects deep
```

### 3. Real-world Use in Large Systems

**React State Management**:
```javascript
// Must use deep copy to avoid unintended mutations
setState(prevState => ({
  ...prevState, // Shallow copy
  nested: {
    ...prevState.nested, // Must also shallow copy nested
    value: newValue
  }
}));
```

**Redux**:
- Immutability is enforced through deep copies in reducers

**API Responses**:
- Deep copy before mutation to prevent caching issues

### 4. Code Examples

```javascript
// Example 1: Shallow vs Deep copy
const original = {
  name: 'Alice',
  details: { age: 30, city: 'NYC' }
};

// Shallow copy - nested objects are shared
const shallow = { ...original };
shallow.details.age = 35;
console.log(original.details.age); // 35 - MODIFIED!

// Deep copy - everything is copied
const deep = JSON.parse(JSON.stringify(original));
deep.details.age = 35;
console.log(original.details.age); // 30 - NOT modified

// Example 2: Array deep vs shallow copy
const arr = [1, 2, [3, 4]];

// Shallow
const shallowArr = [...arr];
shallowArr[2][0] = 99; // Affects original!
console.log(arr[2][0]); // 99

// Deep
const deepArr = JSON.parse(JSON.stringify(arr));
deepArr[2][0] = 99; // Doesn't affect original
console.log(arr[2][0]); // 3

// Example 3: Deep copy with JSON limitations
const problematic = {
  name: 'Bob',
  callback: () => console.log('Hello'), // Functions lost!
  date: new Date(), // Becomes string!
  map: new Map(), // Becomes object!
  undefined: undefined // Lost!
};

const copied = JSON.parse(JSON.stringify(problematic));
// Result: { name: 'Bob', date: '2024...' }
// Lost: callback, map, undefined

// Example 4: Custom deep copy function
function deepCopy(obj) {
  if(obj === null || typeof obj !== 'object') return obj;
  
  if(obj instanceof Date) return new Date(obj);
  if(obj instanceof Map) return new Map(obj);
  if(obj instanceof Set) return new Set(obj);
  if(obj instanceof Array) return obj.map(item => deepCopy(item));
  
  const copy = {};
  for(let key in obj) {
    if(obj.hasOwnProperty(key)) {
      copy[key] = deepCopy(obj[key]);
    }
  }
  return copy;
}

// Example 5: Performance comparison
class CopyPerformance {
  static benchmark() {
    const data = {
      users: Array(1000).fill(0).map((_, i) => ({
        id: i,
        name: `User${i}`,
        profile: { age: 30 + i, city: 'NYC' }
      }))
    };
    
    // JSON.stringify - fast but limited
    const start1 = performance.now();
    for(let i = 0; i < 1000; i++) {
      JSON.parse(JSON.stringify(data));
    }
    const jsonTime = performance.now() - start1;
    
    // Custom deep copy - slower but more complete
    const start2 = performance.now();
    for(let i = 0; i < 1000; i++) {
      deepCopy(data);
    }
    const customTime = performance.now() - start2;
    
    console.log(`JSON: ${jsonTime}ms, Custom: ${customTime}ms`);
  }
}

// Example 6: Structural sharing (advanced pattern)
// Copy only what changed - optimal for large objects
function updateUsingStructuralSharing(original, path, newValue) {
  // Special technique: only copy the path to the change
  // Rest of object shares references
  
  // Useful for huge objects where deep copy would be expensive
  const parts = path.split('.');
  const result = { ...original };
  let current = result;
  
  for(let i = 0; i < parts.length - 1; i++) {
    current[parts[i]] = { ...current[parts[i]] };
    current = current[parts[i]];
  }
  
  current[parts[parts.length - 1]] = newValue;
  return result;
}

// Example 7: Immutable.js for production deep copies
// (External library, not built-in)
// import { fromJS, Map } from 'immutable';

class ImmutableExample {
  static demo() {
    // Immutable.js handles deep copies efficiently
    // const state = fromJS({ user: { name: 'Alice' } });
    // const newState = state.setIn(['user', 'name'], 'Bob');
    // state !== newState (new object)
    // But unchanged parts share references (efficient)
  }
}

// Example 8: Deep copy with circular references
function deepCopyWithCircular(obj, seen = new WeakMap()) {
  if(obj === null || typeof obj !== 'object') return obj;
  
  if(seen.has(obj)) return seen.get(obj); // Return existing copy
  
  let copy;
  if(obj instanceof Array) {
    copy = [];
  } else if(obj instanceof Date) {
    copy = new Date(obj);
  } else {
    copy = {};
  }
  
  seen.set(obj, copy); // Mark as seen before recursing
  
  for(let key in obj) {
    if(obj.hasOwnProperty(key)) {
      copy[key] = deepCopyWithCircular(obj[key], seen);
    }
  }
  
  return copy;
}
```

### 5. Interview Questions

1. **What's the difference between shallow and deep copy? When would you use each?**
2. **Why does `JSON.parse(JSON.stringify(obj))` lose function and Date objects?**
3. **Implement a deep copy function that handles circular references.**
4. **In React, why must you avoid mutating state even with shallow copies?**
5. **Design an algorithm for "intelligent" copying that only copies what changed.**
6. **Why is deep copy expensive? How would you optimize it?**
7. **How would you copy an object with 100,000 nested properties efficiently?**
8. **Compare: shallow copy vs mutation vs deep copy in terms of performance.**
9. **In an immutable data model, how do you avoid deep copy overhead?**
10. **Design a diff-based system that only copies parts of objects that changed.**

### 6. Common Mistakes

**Mistake 1: Thinking spread operator is deep copy**
```javascript
// WRONG
const copy = { ...original };
copy.nested.value = 999; // Modifies original too!

// RIGHT - for nested objects
const copy = {
  ...original,
  nested: { ...original.nested }
};
```

**Mistake 2: Using JSON.stringify for everything**
```javascript
// WRONG - loses functions, Dates, Maps
const data = {
  date: new Date(),
  fn: () => console.log('hi')
};
const copy = JSON.parse(JSON.stringify(data)); // Lost data!

// RIGHT - use custom function
const copy = deepCopy(data);
```

### 7. Small Exercise

**Challenge**: Create a `SmartCopier` class that:
1. Handles both shallow and deep copy
2. Preserves functions, Dates, Maps
3. Detects circular references
4. Benchmarks performance

### 8. System Design Relevance

- **Performance**: Deep copy is expensive on large objects
- **Correctness**: Shallow copy leads to unexpected mutations in React/Redux
- **Memory**: De

eper copies use more memory

---

## Topic 12: Functional Programming Concepts

### 1. Core Concept
**Functional programming** treats computation as the evaluation of mathematical functions avoiding state changes and mutable data.

**Key Principles**:
1. **Pure functions**: Same input always gives same output, no side effects
2. **Immutability**: Don't modify data, create new data
3. **First-class functions**: Functions as values, pass them around
4. **Higher-order functions**: Functions that work on other functions
5. **Function composition**: Combine simple functions into complex ones

### 2. How It Works Internally

```
Imperative (OOP):
let sum = 0;
for(let i = 0; i < arr.length; i++) {
  sum += arr[i];
}
// State changes: sum mutates

Functional:
const sum = arr.reduce((acc, val) => acc + val, 0);
// No state changes: new value computed
```

### 3. Real-world Use in Large Systems

**React** (functional components):
```javascript
// Pure function - same props = same output
function UserCard({ user }) {
  return <div>{user.name}</div>;
}
```

**Redux** (pure reducers):
```javascript
// Pure: no mutations, returns new state
const reducer = (state, action) => ({
  ...state,
  [action.type]: action.payload
});
```

**Node.js Streams**:
```javascript
// Functional composition of transformations
fs.createReadStream('input.txt')
  .pipe(transform1)
  .pipe(transform2)
  .pipe(fs.createWriteStream('output.txt'));
```

### 4. Code Examples

```javascript
// Example 1: Pure functions vs impure
// IMPURE - depends on external state
let multiplier = 2;
function impureMultiply(x) {
  return x * multiplier; // Depends on external variable
}

// PURE - deterministic
function pureMultiply(x, multiplier) {
  return x * multiplier; // Depends only on parameters
}

pureMultiply(5, 2); // Always 10
impureMultiply(5); // Depends on multiplier value

// Example 2: Higher-order functions
// Function that returns a function
function createMultiplier(factor) {
  return function(num) {
    return num * factor;
  };
}

const double = createMultiplier(2);
const triple = createMultiplier(3);

console.log(double(5)); // 10
console.log(triple(5)); // 15

// Example 3: Function composition
function compose(...fns) {
  return (value) => {
    return fns.reduceRight((acc, fn) => fn(acc), value);
  };
}

const add5 = (x) => x + 5;
const multiply2 = (x) => x * 2;
const subtract1 = (x) => x - 1;

// compose(subtract1, multiply2, add5)(10)
// = subtract1(multiply2(add5(10)))
// = subtract1(multiply2(15))
// = subtract1(30)
// = 29

const pipeline = compose(subtract1, multiply2, add5);
console.log(pipeline(10)); // 29

// Example 4: Map, filter, reduce (fundamental functional operations)
const numbers = [1, 2, 3, 4, 5];

// Map - transform each element
const doubled = numbers.map(n => n * 2);
console.log(doubled); // [2, 4, 6, 8, 10]

// Filter - keep elements that match predicate
const evens = numbers.filter(n => n % 2 === 0);
console.log(evens); // [2, 4]

// Reduce - combine into single value
const sum = numbers.reduce((acc, n) => acc + n, 0);
console.log(sum); // 15

// Example 5: Currying (breaking function into single-argument functions)
function add(a, b, c) {
  return a + b + c;
}

function curry(fn) {
  const arity = fn.length; // Number of parameters
  
  return function curried(...args) {
    if(args.length >= arity) {
      return fn(...args);
    } else {
      return (...nextArgs) => curried(...args, ...nextArgs);
    }
  };
}

const curriedAdd = curry(add);
const add5 = curriedAdd(5); // Returns function waiting for 2 more args
const add5And3 = add5(3); // Returns function waiting for 1 more arg
const result = add5And3(2); // 10

// This allows partial application
const add10To = curriedAdd(10);
console.log(add10To(5, 3)); // 18

// Example 6: Memoization (functional optimization)
function memoize(fn) {
  const cache = {};
  
  return function(...args) {
    const key = JSON.stringify(args);
    
    if(key in cache) {
      return cache[key];
    }
    
    const result = fn(...args);
    cache[key] = result;
    return result;
  };
}

// Expensive computation
function fibonacci(n) {
  if(n <= 1) return n;
  return fibonacci(n - 1) + fibonacci(n - 2);
}

const memoFib = memoize(fibonacci);
console.log(memoFib(40)); // Fast - with memoization

// Example 7: Immutable data operations
// Using functional techniques to avoid mutation
const user = { id: 1, name: 'Alice', posts: [{ id: 1, title: 'Hello' }] };

// WRONG - mutates original
user.posts[0].title = 'Hello World';

// RIGHT - functional, no mutation
const updatedUser = {
  ...user,
  posts: user.posts.map(post =>
    post.id === 1 ? { ...post, title: 'Hello World' } : post
  )
};

// Example 8: Partial application
function partialApply(fn, ...partialArgs) {
  return (...moreArgs) => fn(...partialArgs, ...moreArgs);
}

function multiply(a, b, c) {
  return a * b * c;
}

const multiplyBy2 = partialApply(multiply, 2);
const multiplyBy2And3 = partialApply(multiplyBy2, 3);
const result = multiplyBy2And3(5); // 2 * 3 * 5 = 30

// Example 9: Functional pipeline (like Unix pipes)
class Pipeline {
  constructor(value) {
    this.value = value;
  }
  
  pipe(fn) {
    return new Pipeline(fn(this.value));
  }
  
  get() {
    return this.value;
  }
}

const result = new Pipeline(5)
  .pipe(x => x * 2)
  .pipe(x => x + 10)
  .pipe(x => x / 3)
  .get();

console.log(result); // (5 * 2 + 10) / 3 = 6.67

// Example 10: Lazy evaluation (for performance)
function* lazyRange(start, end) {
  for(let i = start; i < end; i++) {
    yield i; // Doesn't compute until consumed
  }
}

// Without functional: processes all 1e6 items
const allNumbers = Array.from({length: 1e6}, (_, i) => i);
const result1 = allNumbers.filter(n => n % 2 === 0)[0];

// With functional lazy: only processes until we get one item
const lazyNumbers = lazyRange(0, 1e6);
const result2 = [...lazyNumbers]
  .filter(n => n % 2 === 0)[0];
```

### 5. Interview Questions

1. **What's a pure function? Why is it important?**
2. **Implement compose and pipe functions.**
3. **What's the difference between partial application and currying?**
4. **How does functional programming improve testability?**
5. **Design a reactive system using functional principles (think RxJS).**
6. **Why is immutability important in functional programming?**
7. **Implement a memoization decorator for expensive functions.**
8. **How would you handle state in a purely functional application?**
9. **Explain reduce. Give 5 real-world use cases.**
10. **Implement lazy evaluation for processing large datasets.**

### 6. Common Mistakes

**Mistake 1: Trying to be purely functional in an OOP codebase**
```javascript
// Don't force it - use FP where it makes sense
// Pure functions for business logic
// Classes for objects with behavior
```

**Mistake 2: Forgetting that functional code can be harder to read**
```javascript
// WRONG - clever but unreadable
const result = data
  .filter(x => x.active)
  .map(x => x.age)
  .reduce((acc, age) => acc + age) / data.length;

// BETTER - explicit and readable
const activeAges = data
  .filter(x => x.active)
  .map(x => x.age);
const averageAge = activeAges.reduce((a, b) => a + b) / activeAges.length;
```

### 7. Small Exercise

**Challenge**: Build a functional data processing pipeline that:
1. Loads CSV data
2. Filters rows
3. Transforms columns
4. Aggregates results
Using only pure functions and no mutations.

### 8. System Design Relevance

- **Testability**: Pure functions are trivial to test
- **Concurrency**: Immutability prevents race conditions
- **Debugging**: No hidden state changes
- **Reusability**: Compose functions like building blocks

---

## Key JavaScript Takeaways

1. **Execution Context** = "virtual environment" for function execution
2. **Event Loop** = orchestrates async work without blocking
3. **Closures** = functions capturing outer scope variables
4. **Prototypes** = JavaScript's inheritance mechanism
5. **This** = depends on how function is called
6. **Promises** = manage asynchronous operations reliably
7. **Async/Await** = syntactic sugar for cleaner Promise code
8. **Memory** = understand refs, closures, and GC for leaks
9. **Debounce/Throttle** = optimize event handler execution
10. **Functional Programming** = pure functions, immutability, composition

---

# PART 2: ANGULAR MASTERY FOR SENIOR ENGINEERS

## Topic 1: Angular Architecture

### 1. Core Concept
**Angular** is a full-featured frontend framework providing DI, rendering, routing, forms, HTTP, and more. It enforces a structured, scalable architecture through TypeScript, modules, components, and services.

**Mental Model**: Angular is like a **complete car manufacturing system**—not just a car. It provides the assembly line (modules), workers (services), quality control (dependency injection), and delivery (routing).

### 2. How It Works Internally

```
Angular Application Flow:
1. main.ts → Bootstrap application
2. platformBrowserDynamic().bootstrapModule(AppModule)
3. AppModule declares what's available
4. Root Component (AppComponent) initializes
5. Change Detection runs
6. Rendering to DOM
7. Event handlers set up
8. All other components bootstrap
```

### 3. Real-world Use in Large Systems

**Google** (Gmail, Google Cloud):
- Uses Angular for complex admin UIs
- Handles thousands of components across applications

**Microsoft** (Office 365):
- Uses Angular for Outlook web
- Manages complex state across many users

**Enterprise**: Banking, healthcare apps
- High complexity, strict architecture
- Angular provides guardrails

### 4. Code Examples

```typescript
// Example 1: Module setup (container for features)
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http';

import { AppComponent } from './app.component';
import { UserService } from './services/user.service';

@NgModule({
  declarations: [AppComponent], // Components, pipes, directives
  imports: [BrowserModule, HttpClientModule], // Other modules
  providers: [UserService], // Services
  bootstrap: [AppComponent] // Root component
})
export class AppModule { }

// Example 2: Service (business logic)
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root' // Tree-shakeable injection
})
export class UserService {
  constructor(private http: HttpClient) { }
  
  getUsers(): Observable<User[]> {
    return this.http.get<User[]>('/api/users');
  }
}

// Example 3: Component (UI logic)
import { Component, OnInit } from '@angular/core';
import { UserService } from './services/user.service';

@Component({
  selector: 'app-user-list',
  templateUrl: './user-list.component.html',
  styleUrls: ['./user-list.component.css']
})
export class UserListComponent implements OnInit {
  users$ = this.userService.getUsers(); // Async observable
  
  constructor(private userService: UserService) { }
  
  ngOnInit() {
    // Initialize component
  }
}

// Example 4: Dependency Injection (core concept)
// Manual DI (what Angular handles automatically)
class Logger { log(msg: string) { } }
class Service {
  constructor(logger: Logger) { }
}

// Angular DI container:
// 1. Create Logger instance
// 2. Inject into Service
// 3. Service doesn't know about Logger creation
// 4. Makes testing easy (can inject mock Logger)

// Example 5: Architecture layers
/*
Presentation Layer
  ├── Components (UI logic)
  ├── Directives (DOM manipulation)
  └── Pipes (data transformation)
  
Business Logic Layer
  ├── Services (data/logic)
  └── State (NgRx/Akita)
  
HTTP Layer
  ├── Interceptors (middleware)
  └── HttpClient
  
Routing Layer
  └── Router (navigation)
*/

// Example 6: ChangeDetectionStrategy (performance critical)
import { ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';

@Component({
  selector: 'app-optimized',
  template: '{{ data | json }}',
  changeDetection: ChangeDetectionStrategy.OnPush // Only when inputs change
})
export class OptimizedComponent {
  @Input() data: any;
  
  constructor(private cdr: ChangeDetectorRef) { }
  
  // OnPush means manual change detection
  manualUpdate() {
    this.data = newData;
    this.cdr.markForCheck(); // Trigger change detection
  }
}

// Example 7: Standalone components (Angular 14+)
// Simpler, tree-shakeable alternative to modules
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-standalone',
  standalone: true, // No module needed
  imports: [CommonModule], // Declare deps directly
  template: '<div>{{ message }}</div>'
})
export class StandaloneComponent {
  message = 'Hello';
}

// Example 8: Folder structure (enterprise)
/*
src/
  app/
    features/
      users/
        containers/
          user-list/
        components/
          user-item/
        services/
          user.service.ts
        state/
          user.reducer.ts
        users.module.ts
    shared/
      components/
        button/
      services/
        logger.service.ts
    core/
      interceptors/
        auth.interceptor.ts
      guards/
        auth.guard.ts
      services/
        http.service.ts
    app.module.ts
    app.component.ts
*/
```

### 5. Interview Questions

1. **Explain the flow from `main.ts` to rendered component.**
2. **What's the difference between declaration and provision in NgModule?**
3. **Design a feature module structure for an e-commerce app.**
4. **Why does Angular use services instead of just components?**
5. **How does tree-shaking work in Angular's DI?**
6. **Compare modules vs standalone components. When use each?**
7. **Design the architecture for an app with 100+ feature modules.**
8. **How does Angular's DI differ from a simple factory pattern?**
9. **In a large team, how would you enforce architecture constraints?**
10. **Design an upgrade path from AngularJS to modern Angular.**

### 6. Common Mistakes

**Mistake 1: Business logic in components**
```typescript
// WRONG - logic scattered in components
@Component({...})
export class UserComponent {
  users = [];
  ngOnInit() {
    fetch('/api/users')
      .then(r => r.json())
      .then(data => {
        this.users = data.map(u => ({
          ...u,
          fullName: u.first + ' ' + u.last
        }));
      });
  }
}

// RIGHT - service handles logic
@Injectable()
export class UserService {
  getFormattedUsers() {
    return this.http.get('/api/users').pipe(
      map(users => users.map(u => ({
        ...u,
        fullName: u.first + ' ' + u.last
      })))
    );
  }
}

@Component({...})
export class UserComponent {
  users$ = this.userService.getFormattedUsers();
}
```

**Mistake 2: Creating new service instances**
```typescript
// WRONG - new instance each time
@Component({...})
export class FooComponent {
  service = new UserService();
}

// RIGHT - use DI
@Component({...})
export class FooComponent {
  constructor(private userService: UserService) { } // Singleton
}
```

### 7. Small Exercise

**Challenge**: Create a feature module for "Blog" with:
1. List component
2. Detail component
3. Blog service
4. Router navigation
5. Lazy loading

### 8. System Design Relevance

- **Scalability**: Modules isolate features for independent scaling
- **Testability**: Services and DI enable unit testing
- **Maintainability**: Clear separation of concerns

---

[Due to length constraints, I will now create a condensed but comprehensive version covering the remaining topics]

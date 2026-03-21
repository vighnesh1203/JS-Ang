package examples;

import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.concurrent.atomic.*;

/**
 * CONCURRENCY PATTERNS - Real threading examples
 * Read this after: 02_CONCURRENCY/01_THREADING_BASICS.md
 */
public class ConcurrencyPatterns {
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== CONCURRENCY PATTERNS ===\n");
        
        threadCreation();
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        raceCondition();
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        synchronizedFix();
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        atomicSolution();
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        reentrantLockExample();
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        producerConsumer();
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        executorServiceExample();
    }
    
    /**
     * Thread creation - two ways
     */
    private static void threadCreation() throws InterruptedException {
        System.out.println("1. THREAD CREATION");
        
        // Approach 1: Implement Runnable (Recommended)
        System.out.println("\nApproach 1: Implement Runnable");
        Thread t1 = new Thread(() -> {
            System.out.println("Thread 1 running: " + Thread.currentThread().getName());
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Thread 1 done");
        }, "Worker-1");
        
        Thread t2 = new Thread(() -> {
            System.out.println("Thread 2 running: " + Thread.currentThread().getName());
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Thread 2 done");
        }, "Worker-2");
        
        t1.start();  // ← Important: start(), not run()!
        t2.start();
        
        t1.join();   // Wait for both to complete
        t2.join();
        
        System.out.println("All threads completed");
        
        // Important: Calling run() directly runs in current thread!
        System.out.println("\nWrong: Calling run() directly:");
        String before = Thread.currentThread().getName();
        Thread wrongThread = new Thread(() -> {
            String during = Thread.currentThread().getName();
            System.out.println("During run(): " + during);
        }, "WrongThread");
        wrongThread.run();  // Runs in current thread, not WrongThread!
    }
    
    /**
     * Demonstrates race condition
     */
    private static void raceCondition() throws InterruptedException {
        System.out.println("2. RACE CONDITION (No synchronization)");
        
        UnsafeCounter counter = new UnsafeCounter();
        
        System.out.println("Starting race condition test...");
        System.out.println("Incrementing counter 10,000 times with 10 threads");
        
        Thread[] threads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    counter.increment();
                }
            });
            threads[i].start();
        }
        
        for (Thread t : threads) {
            t.join();
        }
        
        System.out.println("Expected: 10,000");
        System.out.println("Actual: " + counter.getCount());
        System.out.println("Lost updates due to race condition!");
    }
    
    private static class UnsafeCounter {
        private int count = 0;
        public void increment() {
            count++;  // NOT ATOMIC, but no synchronization
        }
        public int getCount() {
            return count;
        }
    }
    
    /**
     * Fix race condition with synchronized
     */
    private static void synchronizedFix() throws InterruptedException {
        System.out.println("3. FIXED WITH synchronized");
        
        SafeCounter counter = new SafeCounter();
        
        Thread[] threads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    counter.increment();
                }
            });
            threads[i].start();
        }
        
        for (Thread t : threads) {
            t.join();
        }
        
        System.out.println("Expected: 10,000");
        System.out.println("Actual: " + counter.getCount());
        System.out.println("Perfect! synchronized prevents race condition");
    }
    
    private static class SafeCounter {
        private int count = 0;
        
        public synchronized void increment() {  // ← Lock
            count++;
        }
        
        public synchronized int getCount() {    // ← Lock
            return count;
        }
    }
    
    /**
     * Fix with AtomicInteger (faster for simple operations)
     */
    private static void atomicSolution() throws InterruptedException {
        System.out.println("4. FIXED WITH AtomicInteger");
        
        AtomicCounter counter = new AtomicCounter();
        
        Thread[] threads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    counter.increment();
                }
            });
            threads[i].start();
        }
        
        for (Thread t : threads) {
            t.join();
        }
        
        System.out.println("Expected: 10,000");
        System.out.println("Actual: " + counter.getCount());
        System.out.println("AtomicInteger uses Compare-And-Swap, faster than locks");
    }
    
    private static class AtomicCounter {
        private AtomicInteger count = new AtomicInteger(0);
        
        public void increment() {
            count.incrementAndGet();  // Thread-safe, no locks
        }
        
        public int getCount() {
            return count.get();
        }
    }
    
    /**
     * ReentrantLock with tryLock
     */
    private static void reentrantLockExample() throws InterruptedException {
        System.out.println("5. ReentrantLock WITH TIMEOUT");
        
        Lock lock = new ReentrantLock();
        
        Thread slowThread = new Thread(() -> {
            lock.lock();
            try {
                System.out.println("Slow thread has lock");
                Thread.sleep(2000);  // Hold lock for 2 seconds
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        });
        
        Thread fastThread = new Thread(() -> {
            System.out.println("Fast thread trying to acquire lock...");
            try {
                if (lock.tryLock(3, TimeUnit.SECONDS)) {
                    try {
                        System.out.println("Fast thread got lock!");
                    } finally {
                        lock.unlock();
                    }
                } else {
                    System.out.println("Fast thread: Timeout waiting for lock (3s)");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        
        slowThread.start();
        Thread.sleep(100);  // Ensure slow thread gets lock first
        fastThread.start();
        
        slowThread.join();
        fastThread.join();
        
        System.out.println("ReentrantLock provides timeout support that synchronized doesn't");
    }
    
    /**
     * Producer-Consumer pattern with wait/notify
     */
    private static void producerConsumer() throws InterruptedException {
        System.out.println("6. PRODUCER-CONSUMER PATTERN");
        
        ProducerConsumerQueue queue = new ProducerConsumerQueue(5);
        
        Thread producer = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                try {
                    queue.produce(i);
                    System.out.println("Produced: " + i);
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "Producer");
        
        Thread consumer = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                try {
                    int item = queue.consume();
                    System.out.println("  Consumed: " + item);
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "Consumer");
        
        producer.start();
        consumer.start();
        
        producer.join();
        consumer.join();
        
        System.out.println("Producer-Consumer complete");
    }
    
    private static class ProducerConsumerQueue {
        private java.util.Queue<Integer> queue;
        private int capacity;
        
        ProducerConsumerQueue(int capacity) {
            this.queue = new java.util.LinkedList<>();
            this.capacity = capacity;
        }
        
        public synchronized void produce(int item) throws InterruptedException {
            // Wait if queue is full
            while (queue.size() == capacity) {
                System.out.println("Queue full, producer waiting");
                wait();  // Release lock, sleep until notified
            }
            queue.add(item);
            notifyAll();  // Wake up consumer
        }
        
        public synchronized int consume() throws InterruptedException {
            // Wait if queue is empty
            while (queue.isEmpty()) {
                System.out.println("Queue empty, consumer waiting");
                wait();  // Release lock, sleep until notified
            }
            int item = queue.poll();
            notifyAll();  // Wake up producer
            return item;
        }
    }
    
    /**
     * ExecutorService for thread pool management
     */
    private static void executorServiceExample() throws InterruptedException, ExecutionException {
        System.out.println("7. ExecutorService THREAD POOL");
        
        ExecutorService executor = Executors.newFixedThreadPool(3);
        
        System.out.println("Submitting 10 tasks to 3-thread pool:");
        
        for (int i = 0; i < 10; i++) {
            final int taskNum = i;
            executor.submit(() -> {
                System.out.println("Task " + taskNum + " running in " + 
                                   Thread.currentThread().getName());
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        
        executor.shutdown();  // Don't accept new tasks
        boolean terminated = executor.awaitTermination(10, TimeUnit.SECONDS);
        
        if (terminated) {
            System.out.println("All tasks completed");
        } else {
            System.out.println("Timeout waiting for tasks");
        }
        
        System.out.println("Note: Only 3 threads run in parallel, efficiency!");
    }
}

/**
 * OUTPUT:
 * 
 * === CONCURRENCY PATTERNS ===
 * 
 * 1. THREAD CREATION
 * 
 * Approach 1: Implement Runnable
 * Thread 1 running: Worker-1
 * Thread 2 running: Worker-2
 * Thread 1 done
 * Thread 2 done
 * All threads completed
 * 
 * ==================================================
 * 
 * 2. RACE CONDITION (No synchronization)
 * Starting race condition test...
 * Incrementing counter 10,000 times with 10 threads
 * Expected: 10,000
 * Actual: 9,247    ← Lost updates!
 * Lost updates due to race condition!
 * 
 * ... (more output)
 */

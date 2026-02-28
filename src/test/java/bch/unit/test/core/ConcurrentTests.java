package bch.unit.test.core;

import bch.unit.test.core.util.Singleton;
import cn.hutool.core.thread.ThreadUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@SpringBootTest
@Slf4j
class ConcurrentTests {

    @Test
    void reentrantLockTest() throws ExecutionException, InterruptedException {
        ReentrantLock reentrantLock = new ReentrantLock(true);

        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Future<?> future = ThreadUtil.execAsync(() -> {
                if (reentrantLock.tryLock()) {
                    log.info("{} try lock success", Thread.currentThread().getName());
                    reentrantLock.unlock();
                }else {
                    log.error("{} lock fail", Thread.currentThread().getName());
                }

            });
            futures.add(future);
        }
        for (Future<?> future : futures) {
            future.get();
        }

        log.info("done");
    }

    @Test
    void conditionTest() throws Exception {
        ReentrantLock lock = new ReentrantLock();
        Condition condition = lock.newCondition();

        Thread producer = new Thread(() -> {
            lock.lock();
            try {
                log.info("producer waiting...");
                condition.await();
                log.info("producer resumed");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock();
            }
        });

        producer.start();

        Thread.sleep(1000);

        lock.lock();
        try {
            // 会唤醒上面的condition
            log.info("main signal");
            condition.signal();
        } finally {
            lock.unlock();
        }

        producer.join();
    }

    @Test
    void blockingQueueTest() throws Exception {
        BlockingQueue<Integer> queue = new LinkedBlockingQueue<>(2);

        Thread producer = new Thread(() -> {
            try {
                for (int i = 0; i < 5; i++) {
                    queue.put(i);
                    log.info("produce {}", i);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread consumer = new Thread(() -> {
            try {
                while (true) {
                    Integer val = queue.take();
                    log.info("consume {}", val);
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        producer.start();
        consumer.start();

        producer.join();
        Thread.sleep(3000);
    }

    @Test
    void countDownLatchTest() throws Exception {
        CountDownLatch latch = new CountDownLatch(3);

        for (int i = 0; i < 3; i++) {
            new Thread(() -> {
                log.info("{} working", Thread.currentThread().getName());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                latch.countDown();
            }).start();
        }

        latch.await();
        log.info("all done");
    }

    @Test
    void semaphoreTest() throws Exception {
        Semaphore semaphore = new Semaphore(2);

        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                try {
                    semaphore.acquire();
                    log.info("{} get permit", Thread.currentThread().getName());
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    semaphore.release();
                }
            }).start();
        }

        Thread.sleep(8000);
    }

    @Test
    void cyclicBarrierTest() {
        CyclicBarrier barrier = new CyclicBarrier(3,
                () -> log.info("all arrived, go!"));

        for (int i = 0; i < 3; i++) {
            new Thread(() -> {
                try {
                    log.info("{} ready", Thread.currentThread().getName());
                    Thread.sleep(1000);
                    barrier.await();
                    log.info("{} start working", Thread.currentThread().getName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }

        ThreadUtil.sleep(3000);
    }

    @Test
    void phaserTest() {
        Phaser phaser = new Phaser(1); // 主线程注册

        for (int i = 0; i < 3; i++) {
            phaser.register();
            new Thread(() -> {
                log.info("{} phase 1", Thread.currentThread().getName());
                phaser.arriveAndAwaitAdvance();

                log.info("{} phase 2", Thread.currentThread().getName());
                phaser.arriveAndDeregister();
            }).start();
        }

        phaser.arriveAndAwaitAdvance();
        log.info("main phase done");
    }

    @Test
    void completableFutureTest() throws Exception {

        CompletableFuture<String> future =
                CompletableFuture.supplyAsync(() -> {
                            ThreadUtil.sleep(1000);
                            return "hello";
                        }).thenApply(s -> s + " world")
                        .thenApply(String::toUpperCase);

        log.info("result: {}", future.get());
    }

    @Test
    void completableFutureAllOfTest() throws Exception {

        CompletableFuture<String> f1 =
                CompletableFuture.supplyAsync(() -> "A");

        CompletableFuture<String> f2 =
                CompletableFuture.supplyAsync(() -> "B");

        CompletableFuture<Void> all =
                CompletableFuture.allOf(f1, f2);

        all.get();

        log.info("{}{}", f1.get(), f2.get());
    }

    static class SumTask extends RecursiveTask<Long> {

        private final long start;
        private final long end;
        private static final long THRESHOLD = 10000;

        SumTask(long start, long end) {
            this.start = start;
            this.end = end;
        }

        @Override
        protected Long compute() {
            if (end - start < THRESHOLD) {
                long sum = 0;
                for (long i = start; i <= end; i++) {
                    sum += i;
                }
                return sum;
            }

            long mid = (start + end) / 2;
            SumTask left = new SumTask(start, mid);
            SumTask right = new SumTask(mid + 1, end);

            left.fork();
            return right.compute() + left.join();
        }
    }

    @Test
    void forkJoinTest() {
        try (ForkJoinPool pool = new ForkJoinPool()) {
            Long result = pool.invoke(new SumTask(1, 1_000_000));
            log.info("result={}", result);
        }
    }

    private synchronized void sync1() {
        log.info("锁的this");
    }

    private static synchronized void sync2() {
        log.info("锁的class");
    }

    @Test
    void waitNotifyBasicTest() throws Exception {
        // ObjectMonitor
        // ├── Owner
        // ├── EntryList   （抢锁失败的线程） 同步队列 BLOCKED
        // └── WaitSet     （调用 wait 的线程） 条件队列 WAITING
        Object lock = new Object();

        Thread t1 = new Thread(() -> {
            synchronized (lock) {
                try {
                    log.info("t1 acquired lock, waiting...");
                    // 会释放锁,不然下面主线程的sync块拿不到obj的lock
                    // Owner → WaitSet
                    lock.wait();
                    log.info("t1 resumed and got lock again");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        t1.start();

        Thread.sleep(1000);

        synchronized (lock) {
            log.info("main thread notifying...");
            // 不会释放锁,代码块结束才会释放
            // WaitSet → EntryList
            lock.notify();
            log.info("main thread still holding lock...");
            // sleep同样不会释放锁
            Thread.sleep(2000);
        }

        t1.join();
    }

    @Test
    void dclTest() throws InterruptedException, ExecutionException {
        try (ExecutorService executor = Executors.newFixedThreadPool(10000)) {
            CountDownLatch latch = new CountDownLatch(10000);

            ArrayList<Future<?>> futures = new ArrayList<>(10000);
            for (int i = 0; i < 10000; i++) {
                Future<?> future = executor.submit(() -> {
                    Singleton instance = Singleton.getInstance();

                    if (instance.getValue() != 42) {
                        System.out.println("发现异常值: " + instance.getValue());
                    }

                    latch.countDown();
                });
                futures.add(future);
            }

            for (Future<?> future : futures) {
                future.get();
            }

            latch.await();
            executor.shutdown();
        }
        log.info("done");
    }
}

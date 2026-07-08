package com.steel.perf;

import com.steel.perf.lock.InMemoryLockService;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** 分布式锁（内存实现）串行性测试。 */
class LockServiceTest {

    private final InMemoryLockService lock = new InMemoryLockService();

    @Test
    void sameKeySerialized() throws InterruptedException {
        // 无锁易出现竞态；有锁则 ++ 严格串行
        AtomicInteger counter = new AtomicInteger(0);
        int threads = 20, loops = 500;
        CountDownLatch done = new CountDownLatch(threads);
        for (int i = 0; i < threads; i++) {
            new Thread(() -> {
                for (int j = 0; j < loops; j++) {
                    lock.runExclusive("k1", () -> {
                        int v = counter.get();
                        counter.set(v + 1); // 非原子，靠锁保证串行
                        return null;
                    });
                }
                done.countDown();
            }).start();
        }
        done.await();
        assertEquals(threads * loops, counter.get());
    }

    @Test
    void returnsActionResult() {
        assertEquals(42, lock.runExclusive("k2", () -> 42));
    }
}

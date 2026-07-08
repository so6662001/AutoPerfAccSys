package com.steel.perf.lock;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * 内存锁实现（单实例/开发/测试默认）。按 key 维护可重入锁。
 */
@Service
@ConditionalOnProperty(name = "perf.lock.redis", havingValue = "false", matchIfMissing = true)
public class InMemoryLockService implements LockService {

    private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    @Override
    public <T> T runExclusive(String key, Supplier<T> action) {
        ReentrantLock lock = locks.computeIfAbsent(key, k -> new ReentrantLock());
        lock.lock();
        try {
            return action.get();
        } finally {
            lock.unlock();
        }
    }
}

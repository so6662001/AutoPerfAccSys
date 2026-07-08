package com.steel.perf.lock;

import java.util.function.Supplier;

/**
 * 分布式锁抽象：同 key 互斥执行。用于同租户同周期核算串行，防并发覆盖/重复计。
 * 默认内存实现（单实例/开发）；生产 perf.lock.redis=true 切 Redis 实现（多实例）。
 */
public interface LockService {

    <T> T runExclusive(String key, Supplier<T> action);
}

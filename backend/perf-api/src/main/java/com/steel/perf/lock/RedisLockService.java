package com.steel.perf.lock;

import com.steel.perf.common.exception.BizException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Redis 分布式锁实现（多实例，perf.lock.redis=true 启用）。
 * SET key token NX PX ttl 获取；自旋等待；释放用 Lua 校验 token 保证只删自己的锁。
 */
@Service
@ConditionalOnProperty(name = "perf.lock.redis", havingValue = "true")
public class RedisLockService implements LockService {

    private static final String UNLOCK_LUA =
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
    private static final long TTL_MS = 30_000;
    private static final long WAIT_MS = 10_000;
    private static final long SPIN_MS = 50;

    private final StringRedisTemplate redis;

    public RedisLockService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public <T> T runExclusive(String key, Supplier<T> action) {
        String lockKey = "lock:" + key;
        String token = UUID.randomUUID().toString();
        long deadline = System.currentTimeMillis() + WAIT_MS;
        boolean acquired = false;
        try {
            while (System.currentTimeMillis() < deadline) {
                Boolean ok = redis.opsForValue().setIfAbsent(lockKey, token, Duration.ofMillis(TTL_MS));
                if (Boolean.TRUE.equals(ok)) {
                    acquired = true;
                    break;
                }
                Thread.sleep(SPIN_MS);
            }
            if (!acquired) {
                throw new BizException(429, "获取分布式锁超时: " + key);
            }
            return action.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BizException("等待锁被中断");
        } finally {
            if (acquired) {
                redis.execute((org.springframework.data.redis.core.RedisCallback<Long>) conn ->
                        conn.scriptingCommands().eval(UNLOCK_LUA.getBytes(),
                                org.springframework.data.redis.connection.ReturnType.INTEGER, 1,
                                lockKey.getBytes(), token.getBytes()));
            }
        }
    }
}

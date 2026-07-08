package com.steel.perf.calc;

import com.steel.perf.common.exception.BizException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 幂等键：同租户 + 周期 + 方案版本 + 数据快照哈希 唯一。
 * 保证同数据同规则重跑结果一致、不重复计。
 */
public final class IdempotencyKey {

    private IdempotencyKey() {
    }

    public static String of(String tenantId, String period, int planVersion, String snapshotHash) {
        String raw = tenantId + "|" + period + "|v" + planVersion + "|" + snapshotHash;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : d) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new BizException("幂等键计算失败");
        }
    }
}

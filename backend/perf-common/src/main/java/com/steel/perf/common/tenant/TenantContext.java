package com.steel.perf.common.tenant;

import com.steel.perf.common.exception.BizException;

/**
 * 多租户上下文（ThreadLocal）。
 * 登录后由过滤器写入当前租户与用户；服务层与 SQL 层强制使用，禁止跨租户访问。
 */
public final class TenantContext {

    private static final ThreadLocal<Ctx> HOLDER = new ThreadLocal<>();

    private TenantContext() {
    }

    public record Ctx(String tenantId, String userId, java.util.Set<String> roles) {
    }

    public static void set(String tenantId, String userId, java.util.Set<String> roles) {
        HOLDER.set(new Ctx(tenantId, userId, roles));
    }

    public static Ctx get() {
        return HOLDER.get();
    }

    /** 获取当前租户，缺失即视为越权/未登录，直接拒绝。 */
    public static String requireTenantId() {
        Ctx ctx = HOLDER.get();
        if (ctx == null || ctx.tenantId() == null || ctx.tenantId().isBlank()) {
            throw new BizException(401, "缺少租户上下文，拒绝访问");
        }
        return ctx.tenantId();
    }

    public static String currentUserId() {
        Ctx ctx = HOLDER.get();
        return ctx == null ? null : ctx.userId();
    }

    /** 校验目标数据归属租户，防止水平越权。 */
    public static void assertSameTenant(String dataTenantId) {
        String current = requireTenantId();
        if (dataTenantId == null || !current.equals(dataTenantId)) {
            throw new BizException(403, "跨租户访问被拒绝");
        }
    }

    public static void clear() {
        HOLDER.remove();
    }
}

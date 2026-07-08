package com.steel.perf.security;

import com.steel.perf.common.exception.BizException;
import com.steel.perf.common.tenant.TenantContext;

import java.util.Set;

/**
 * 角色授权校验（方法级 RBAC）。基于当前租户上下文中的角色。
 */
public final class RoleGuard {

    private RoleGuard() {
    }

    /** 审批类角色（可执行规则审批、生效、推广等敏感操作）。 */
    public static final Set<String> APPROVER = Set.of(
            "ADMIN", "HR", "HR负责人", "绩效专员", "部门主管", "分管领导", "MANAGER");

    public static void requireAny(Set<String> allowed) {
        TenantContext.Ctx ctx = TenantContext.get();
        if (ctx == null) {
            throw new BizException(401, "未认证");
        }
        boolean ok = ctx.roles() != null && ctx.roles().stream().anyMatch(allowed::contains);
        if (!ok) {
            throw new BizException(403, "无权限执行该操作（需要角色之一：" + String.join("/", allowed) + "）");
        }
    }
}

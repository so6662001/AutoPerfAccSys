package com.steel.perf.service;

import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * 字段级脱敏（薪酬敏感数据）。仅授权角色可见明细金额，其余脱敏。
 */
@Service
public class MaskingService {

    /** 可查看薪酬明细的角色。 */
    private static final Set<String> SALARY_VIEW_ROLES = Set.of(
            "ADMIN", "HR", "MANAGER", "HR负责人", "绩效专员", "分管领导");

    public boolean canViewSalary(Set<String> roles) {
        if (roles == null) {
            return false;
        }
        for (String r : roles) {
            if (SALARY_VIEW_ROLES.contains(r)) {
                return true;
            }
        }
        return false;
    }

    /** 按角色脱敏金额：授权角色返回原值字符串，否则返回掩码。 */
    public String maskAmount(double amount, Set<String> roles) {
        return canViewSalary(roles) ? String.valueOf(amount) : "****";
    }
}

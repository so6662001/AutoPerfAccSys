package com.steel.perf.calc.model;

import java.util.List;
import java.util.Map;

/**
 * 核算结果（个人绩效单）。
 *
 * @param planCode       方案编码
 * @param planVersion    规则版本
 * @param componentAmounts 各核算项金额
 * @param traces         逐项追溯明细（下钻）
 * @param total          合计
 * @param idempotencyKey 幂等键（同租户/周期/版本/数据快照重跑一致）
 */
public record PayslipResult(
        String planCode,
        int planVersion,
        Map<String, Double> componentAmounts,
        List<ComponentTrace> traces,
        double total,
        String idempotencyKey) {
}

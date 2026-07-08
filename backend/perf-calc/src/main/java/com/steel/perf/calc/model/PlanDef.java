package com.steel.perf.calc.model;

import java.util.List;

/**
 * 绩效方案定义（用于核算）。
 *
 * @param code        方案编码
 * @param version     规则版本（写入绩效单，供复算追溯）
 * @param components  核算项列表
 * @param summaryExpr 汇总公式（可空；为空时=计入项之和）
 */
public record PlanDef(String code, int version, List<ComponentDef> components, String summaryExpr) {
}

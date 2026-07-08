package com.steel.perf.calc.model;

import java.util.Map;

/**
 * 核算项追溯明细（供个人绩效单逐项下钻）。
 *
 * @param code       核算项编码
 * @param expression 所用公式
 * @param usedVars   公式中引用到的变量及其取值（含依赖的其他核算项结果）
 * @param amount     计算结果
 */
public record ComponentTrace(String code, String expression, Map<String, Double> usedVars, double amount) {
}

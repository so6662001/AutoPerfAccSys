package com.steel.perf.calc.model;

/**
 * 核算项定义。
 *
 * @param code           核算项编码（可为中文，作为其他核算项引用的变量名）
 * @param expression     计算公式（由公式引擎解析，可引用指标/参数/其他核算项）
 * @param includeInTotal 是否计入合计（默认汇总公式为计入项之和）
 */
public record ComponentDef(String code, String expression, boolean includeInTotal) {
}

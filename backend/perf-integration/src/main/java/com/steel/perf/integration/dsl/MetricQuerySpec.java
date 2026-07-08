package com.steel.perf.integration.dsl;

import java.util.List;

/**
 * 取数 DSL 规格（名亿"绩效目标单 + 金额/成本公式 → 取数"的结构化表达）。
 * 由配置生成，经 {@link SafeSqlCompiler} 编译为参数化只读 SQL。
 */
public record MetricQuerySpec(
        String table,                 // 来源表（须在白名单）
        Agg func,                     // 聚合函数
        String field,                 // 聚合字段（须在白名单）
        GroupBy groupBy,              // 核算对象：员工 / 部门
        List<Condition> conditions    // 过滤条件（值一律参数绑定）
) {

    public enum Agg { SUM, AVG, COUNT, MIN, MAX }

    public enum GroupBy {
        EMP("emp_id"), DEPT("dept_id");
        private final String column;
        GroupBy(String column) { this.column = column; }
        public String column() { return column; }
    }

    /** 过滤条件：field op :param。op 白名单，值绑定。 */
    public record Condition(String field, String op, String paramName) {
    }
}

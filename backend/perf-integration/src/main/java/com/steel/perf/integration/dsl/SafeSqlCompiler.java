package com.steel.perf.integration.dsl;

import com.steel.perf.common.exception.BizException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 取数 DSL → SQL 安全编译器（安全敏感，重中之重）。
 * <ul>
 *   <li>表名 / 字段名 <b>白名单</b>校验（只允许 DWD 标准表/字段），并做标识符正则二次校验；</li>
 *   <li>聚合函数 / 比较运算符 白名单；</li>
 *   <li>所有值一律<b>参数绑定</b>（:name），绝不拼接用户输入；</li>
 *   <li>强制 tenant_id 过滤；仅生成单条 SELECT，无多语句/注释/DDL/DML。</li>
 * </ul>
 * 由此杜绝 SQL 注入。执行时须使用<b>只读连接 + 查询超时 + 返回行数上限</b>。
 */
public class SafeSqlCompiler {

    private static final Pattern IDENT = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");
    private static final Pattern PARAM = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");
    private static final Set<String> OPS = Set.of("=", "<>", ">", "<", ">=", "<=");

    /** 表 -> 允许的字段白名单。 */
    private static final Map<String, Set<String>> TABLE_FIELDS = Map.of(
            "dwd_sales_detail", Set.of(
                    "qty", "deal_price", "list_price", "base_price", "cost_price", "gross_profit",
                    "one_bill_cost", "proc_fee", "rebate", "other_inout", "credit_term",
                    "biz_type", "store", "category", "material", "supplier", "settle_type",
                    "is_internal", "is_low_price", "emp_id", "dept_id", "branch_id", "biz_date"),
            "dwd_snapshot", Set.of("amount", "qty", "snap_type", "snap_date", "subject_id")
    );

    public CompiledQuery compile(MetricQuerySpec spec) {
        if (spec == null) {
            throw new BizException("取数规格为空");
        }
        String table = requireTable(spec.table());
        Set<String> fields = TABLE_FIELDS.get(table);
        requireField(fields, spec.field());
        if (spec.func() == null) {
            throw new BizException("聚合函数缺失");
        }
        if (spec.groupBy() == null) {
            throw new BizException("核算对象(groupBy)缺失");
        }
        String groupCol = spec.groupBy().column();

        List<String> paramOrder = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ").append(groupCol).append(" AS subject, ")
                .append(spec.func().name()).append("(").append(spec.field()).append(") AS val")
                .append(" FROM ").append(table)
                .append(" WHERE tenant_id = :tenantId");
        paramOrder.add("tenantId");

        if (spec.conditions() != null) {
            for (MetricQuerySpec.Condition c : spec.conditions()) {
                requireField(fields, c.field());
                if (!OPS.contains(c.op())) {
                    throw new BizException("非法比较运算符: " + c.op());
                }
                requireParam(c.paramName());
                sql.append(" AND ").append(c.field()).append(" ").append(c.op())
                        .append(" :").append(c.paramName());
                paramOrder.add(c.paramName());
            }
        }
        sql.append(" GROUP BY ").append(groupCol);
        return new CompiledQuery(sql.toString(), paramOrder);
    }

    private String requireTable(String table) {
        if (table == null || !IDENT.matcher(table).matches() || !TABLE_FIELDS.containsKey(table)) {
            throw new BizException("非法或未授权的来源表: " + table);
        }
        return table;
    }

    private void requireField(Set<String> allowed, String field) {
        if (field == null || !IDENT.matcher(field).matches() || allowed == null || !allowed.contains(field)) {
            throw new BizException("非法或未授权的字段: " + field);
        }
    }

    private void requireParam(String p) {
        if (p == null || !PARAM.matcher(p).matches()) {
            throw new BizException("非法参数名: " + p);
        }
    }
}

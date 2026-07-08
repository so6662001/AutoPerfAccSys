package com.steel.perf.integration.dsl;

import com.steel.perf.common.exception.BizException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 取数 DSL→SQL 安全编译器测试（含注入防护）。 */
class SafeSqlCompilerTest {

    private final SafeSqlCompiler compiler = new SafeSqlCompiler();

    @Test
    void compileGroupByEmpWithCondition() {
        MetricQuerySpec spec = new MetricQuerySpec(
                "dwd_sales_detail", MetricQuerySpec.Agg.SUM, "qty", MetricQuerySpec.GroupBy.EMP,
                List.of(new MetricQuerySpec.Condition("biz_type", "=", "bizType")));
        CompiledQuery q = compiler.compile(spec);
        assertEquals("SELECT emp_id AS subject, SUM(qty) AS val FROM dwd_sales_detail "
                + "WHERE tenant_id = :tenantId AND biz_type = :bizType GROUP BY emp_id", q.sql());
        assertEquals(List.of("tenantId", "bizType"), q.paramOrder());
    }

    @Test
    void compileGroupByDept() {
        MetricQuerySpec spec = new MetricQuerySpec(
                "dwd_sales_detail", MetricQuerySpec.Agg.AVG, "gross_profit", MetricQuerySpec.GroupBy.DEPT, List.of());
        CompiledQuery q = compiler.compile(spec);
        assertTrue(q.sql().contains("dept_id AS subject"));
        assertTrue(q.sql().contains("AVG(gross_profit)"));
        assertTrue(q.sql().endsWith("GROUP BY dept_id"));
    }

    @Test
    void rejectInjectionInField() {
        MetricQuerySpec spec = new MetricQuerySpec(
                "dwd_sales_detail", MetricQuerySpec.Agg.SUM, "qty; DROP TABLE t_user", MetricQuerySpec.GroupBy.EMP, List.of());
        assertThrows(BizException.class, () -> compiler.compile(spec));
    }

    @Test
    void rejectUnknownField() {
        MetricQuerySpec spec = new MetricQuerySpec(
                "dwd_sales_detail", MetricQuerySpec.Agg.SUM, "secret_col", MetricQuerySpec.GroupBy.EMP, List.of());
        assertThrows(BizException.class, () -> compiler.compile(spec));
    }

    @Test
    void rejectUnknownTable() {
        MetricQuerySpec spec = new MetricQuerySpec(
                "t_user", MetricQuerySpec.Agg.SUM, "qty", MetricQuerySpec.GroupBy.EMP, List.of());
        assertThrows(BizException.class, () -> compiler.compile(spec));
    }

    @Test
    void rejectInjectionInConditionOperator() {
        MetricQuerySpec spec = new MetricQuerySpec(
                "dwd_sales_detail", MetricQuerySpec.Agg.SUM, "qty", MetricQuerySpec.GroupBy.EMP,
                List.of(new MetricQuerySpec.Condition("biz_type", "= '' OR 1=1 --", "x")));
        assertThrows(BizException.class, () -> compiler.compile(spec));
    }

    @Test
    void rejectInjectionInParamName() {
        MetricQuerySpec spec = new MetricQuerySpec(
                "dwd_sales_detail", MetricQuerySpec.Agg.SUM, "qty", MetricQuerySpec.GroupBy.EMP,
                List.of(new MetricQuerySpec.Condition("biz_type", "=", "p; DROP")));
        assertThrows(BizException.class, () -> compiler.compile(spec));
    }
}
